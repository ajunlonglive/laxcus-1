/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 数据写入调用器，执行INSERT操作。<br><br>
 * 
 * 处理流程：<br>
 * 第1步：判断来自FRONT站点的命令可以支持，包括表和主节点。<br>
 * 第2步：接收来自FRONT站点的INSERT数据，同时找到一个DATA主站点。<br>
 * 第3步：接收来自DATA主站点的反馈，正确发INSERT数据给DATA主站点，否则通知FRONT失败和退出。<br>
 * 第4步：接收来自DATA主站点的写入结果通知，并且把这个写入结果转发给FRONT站点。<br><br>
 * 
 * 说明：<br>
 * 1. 本处只转发FRONT站点的写入数据，不做正确性判断，正确性判断交给DATA站点处理。<br>
 * 
 * @author scott.liang
 * @version 1.3 9/16/2013
 * @since laxcus 1.0
 */
public class CallInsertInvoker extends CallInvoker {

	/** 处理步骤。从1开始 **/
	private int step = 1;

	/** FRONT发送数据，保存在磁盘文件 **/
	private File frontFile;
	
	/** FRONT发送数据，保存在内存 **/
	private byte[] frontContent;

	/** FRONT站点监听地址 **/
	private Cabin frontCabin;
	
	/** 插入结果成功 **/
	private boolean resultSuccessful;

	/*
	 * 释放CALL.INSERT调用器资源
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		// 释放磁盘文件
		if (frontFile != null) {
			// 判断磁盘文件存在
			if (frontFile.exists()) frontFile.delete();
			frontFile = null;
		}
		// 释放内存
		if (frontContent != null) {
			frontContent = null;
		}
		// 释放FRONT监听地址
		if (frontCabin != null) {
			frontCabin = null;
		}
		// 调用上层接口
		super.destroy();
	}

	/**
	 * 构造数据写入调用器
	 * @param cmd 确认命令，来自FRONT站点
	 */
	public CallInsertInvoker(InsertGuide cmd) {
		super(cmd);
		resultSuccessful = false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public InsertGuide getCommand() {
		return (InsertGuide) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 按照顺序执行异步调用
	 * @return 成功返回“真”，否则“假”。
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		case 4:
			success = doFourth();
			break;
		case 5:
			success = doFifth();
			setQuit(true);
			break;
		}
		// 无论结果，自增1
		step++;

		Logger.debug(this, "todo", success, "step is %d", step - 1);

		return success;
	}

	/**
	 * 数据写入流程第一步：<br>
	 * 1. 根据FRONT站点发送的InsertPrompt命令，判断本地是否支持。<br>
	 * 2. 向FRONT站点发送反馈数据。<br>
	 * @return 成功返回真，否则假。
	 */
	private boolean doFirst() {
		InsertGuide cmd = getCommand();
		// 数据表名
		Space space = cmd.getSpace();
		// FRONT调用器监听地址
		Cabin source = cmd.getSource(); 
		
		// 默认次错误码
		short minor = Minor.SYSTEM_FAILED;
		
		// 1. 判断操作许可
		boolean success = allow(cmd.getIssuer(), space);
		// 2. 判断DATA主节点存在
		if (success) {
			NodeSet set = DataOnCallPool.getInstance().findPrimeTableSites(space);
			success = (set != null && set.size() > 0);
			if (!success) {
				minor = Minor.SITE_MISSING;
			}
		}
		// 3. 磁盘空间溢出判断
		if (success) {
			boolean full = StaffOnCallPool.getInstance().isTableCapacityFull(cmd.getIssuer());
			if (full) {
				success = false;
				minor = Minor.CAPACITY_MISSING;
			}
		}
		
		// 3. 向FRONT站点反馈结果，FRONT发送的数据要写入硬盘（回显缓存要用来实现FRONT/DATA交互，所以数据在写入硬盘）
		if (success) {
			boolean ondisk = isDisk();
			setDisk(true);
			
			// 向FRONT反馈结果，强制为磁盘存储模式
			InsertGuide guide = new InsertGuide(space, cmd.getCapacity());
			success = replyTo(source, guide);
			
			setDisk(ondisk);
		} 
		// 以上不成功，向FRONT返回错误
		if (!success) {
			replyFault(Major.FAULTED, minor);
		}
		
		Logger.debug(this, "doFirst", success, "reply to %s", source);

		return success;
	}

	/**
	 * 数据写入流程第二步：<br>
	 * 1. 判断FRONT发送数据成功 <br>
	 * 2. 磁盘上的回显文件改名 <br>
	 * 3. 以顺序和均衡的方式，枚举一个DATA主站点<br>
	 * 4. 发送一个INSERT协商命令给DATA主站点。<br>
	 * 
	 * @return 成功返回真，否则假。
	 */
	private boolean doSecond() {
		// 如果接收FRONT站点数据失败，退出
		if (isFaultCompleted()) {
			return false;
		}

		// 承接FIRST，第一个且只有一个索引编号
		int index = findEchoKey(0);

		// 取出异步缓存
		EchoBuffer buff = findBuffer(index);
		if (buff.isDisk()) {
			return doSecondFile(index, buff);
		} else {
			return doSecondMemory(index, buff);
		}
	}
	
	/**
	 * 获取一个指定的DATA主站点
	 * @param space 表名
	 * @param capacity 数据需要的磁盘空间
	 * @return 返回DATA主站点地址，或者空指针
	 */
	private Node reach(Space space, long capacity) {
		NodeSet set = DataOnCallPool.getInstance().findPrimeTableSites(space);
		if(set == null) {
			return null;
		}

		int size = set.size();
		for (int index = 0; index < size; index++) {
			Node hub = set.next();
			// 判断剩余空间（未完成）
			boolean success = DataOnCallPool.getInstance().conform(hub, space, capacity);
			// 在空间充足情况下，返回这个DATA主站点地址
			if (success) {
				return hub;
			}
		}
		return null;
	}

	/**
	 * 第二阶段，向FRONT反馈结果
	 * @param capacity 数据容量
	 * @return 成功返回真，否则假
	 */
	private boolean doSecondReply(long capacity) {
		InsertGuide guide = getCommand();
		Space space = guide.getSpace();
		
		// 根据表名，找到DATA主站点，按照顺序调用一个DATA主站点（这种处理将实现平衡调用DAT主A站点）
		Node hub = reach(space, capacity);
		// 判断拿到DATA主站点，成功！
		boolean success = (hub != null);
		// 生成一个协商命令，发给DATA主站点. INSERT先不发送，而是保存在本地
		if (success) {
			InsertGuide sub = new InsertGuide(space, capacity);
			success = launchTo(hub, sub);
		}

		Logger.debug(this, "doSecondReply", success, "send to %s", hub);

		// 以上不成功，通知FRONT站点
		if (!success) {
			replyFault(frontCabin, Major.FAULTED, Minor.CAPACITY_MISSING);
		}

		return success;
	}

	/**
	 * 取出磁盘文件，然后改名！
	 * @param index 索引号
	 * @param buffer 异步缓存
	 * @return 成功返回真，否则假
	 */
	private boolean doSecondFile(int index, EchoBuffer buffer) {
		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
		frontCabin = findItemCabin(index);
		// 拿到磁盘文件
		File file = buffer.getFile();

		Logger.debug(this, "doSecondFile", "this is %s, length:%d, from %s", file, file.length(), frontCabin);

		// 修改文件名，删除文件名放在最后
		String temp = String.format("%s.inject", file.getAbsolutePath());
		frontFile = new File(temp);
		// 文件改名，这个文件在后面使用
		boolean success = file.renameTo(frontFile);

		Logger.note(this, "doSecondFile", success, "%s rename to %s, file length:%d", 
				file, frontFile, frontFile.length());

		// 出错退出
		if (!success) {
			replyFault(frontCabin);
			return false;
		}

		// 向FRONT反馈结果
		return doSecondReply(frontFile.length());
	}
	
	/**
	 * 从异步缓存中取出内存数据，保存在本地
	 * @param index 索引号
	 * @param buffer 异步缓存
	 * @return 处理成功返回真，否则假
	 */
	private boolean doSecondMemory(int index, EchoBuffer buffer) {
		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
		frontCabin = findItemCabin(index);

		// 取出内存数据，防止内存溢出！
		boolean success = false;
		try {
			frontContent = buffer.getMemory();
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 出错退出
		if (!success) {
			replyFault(frontCabin, Major.FAULTED, Minor.MEMORY_MISSING);
			return false;
		}
		
		Logger.debug(this, "doSecondMemory", success, "memory length: %d", frontContent.length);

		// 反馈结果
		return doSecondReply(frontContent.length);
	}

	/**
	 * 数据写入流程第三段：<br>
	 * 1. 判断DATA主站点同意接收 <br>
	 * 2. 发送数据给DATA主站点 <br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		int index = findEchoKey(0);
		// 判断DATA站点反馈成功
		InsertGuide guide = null;
		try {
			if (isSuccessObjectable(index)) {
				guide = getObject(InsertGuide.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 确认成功
		boolean success = (guide != null);
		
		// 成功，向DATA主站点发送文件格式或者内存格式的INSERT命令
		if (success) {
			Cabin hub = guide.getSource();
			if (frontFile != null) {
				success = replyTo(hub, frontFile);
				Logger.debug(this, "doThird", success, "%s length %d", frontFile, frontFile.length());
			} else if (frontContent != null) {
				success = replyTo(hub, frontContent);
				Logger.debug(this, "doThird", success, "memory length %d", frontContent.length);
			} else {
				success = false;
			}
		}

		// 以上如果不成功，向FRONT站点发送错误通知
		if (!success) {
			replyFault(frontCabin);
		}

		return success;
	}

	/**
	 * 第四阶段：接收DATA主站点反馈报告，再向DATA站点反结果
	 * @return 成功返回真，否则假
	 */
	private boolean doFourth() {
		int index = findEchoKey(0);
		AssumeInsert assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeInsert.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 以上不成功，退出
		boolean success = (assume != null);

		// 根据处理结果，以命令格式，向DATA主站点反馈“确认或者取消”
		if (success) {
			// 结果成功
			resultSuccessful = assume.isSuccess();
			// 反馈报告
			byte status = (resultSuccessful ? ConsultStatus.CONFIRM: ConsultStatus.CANCEL);
			AssertInsert reply = new AssertInsert(assume.getSpace(), status);
			
			// 关联地址（是自己，这个情况用于测试。检测通过后删除这行代码）
//			reply.addSeekSite(assume.getListener());
			
			Logger.debug(this, "doFourth", "insert result is %s",
					(assume.isSuccess() ? "success" : "failed"));

			success = replyTo(assume.getSource(), reply);
		}

		// 以上不成功，通知FRONT站点，记录没有写入
		if (!success) {
			InsertGuide cmd = getCommand();
			InsertProduct product = new InsertProduct(false, cmd.getSpace());
			replyProduct(frontCabin, product);
		}

		return success;
	}

	/**
	 * 第五阶段：接收DATA主站点反馈报告
	 * @return 成功返回真，否则假
	 */
	private boolean doFifth() {
		int index = findEchoKey(0);
		AssumeInsert assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeInsert.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 确认记录写入成功
		boolean success = (assume != null && assume.isConfirmSuccess());

		// 发送结果给FRONT站点
		InsertProduct product = new InsertProduct(resultSuccessful, assume.getSpace());
		if (success) {
			product.setRows(assume.getRows());
		}

		// 通知FRONT站点，成功或者失败（0记录是失败）
		replyProduct(frontCabin, product);

		Logger.debug(this, "doFifth", success, "insert \"%s\"", product);

		return success;
	}



}