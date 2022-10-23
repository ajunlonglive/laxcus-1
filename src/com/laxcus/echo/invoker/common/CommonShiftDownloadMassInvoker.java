/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.io.*;

import com.laxcus.access.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.stub.transfer.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 数据块下载转发调用器。<br>
 * 
 * 此调用器做为客户端，向服务器发出下载数据块命令，然后异步接收。
 * 
 * @author scott.liang
 * @version 1.1 3/23/2012
 * @since laxcus 1.0
 */
public class CommonShiftDownloadMassInvoker extends CommonInvoker {

	/**
	 * 构造数据块下载转发调用器，指定命令
	 * @param shift 数据块下载转发命令
	 */
	public CommonShiftDownloadMassInvoker(ShiftDownloadMass shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDownloadMass getCommand() {
		return (ShiftDownloadMass) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDownloadMass shift = getCommand();
		Node hub = shift.getHub();
		DownloadMass cmd = shift.getCommand();

		// 发送命令，数据写入本地磁盘
		boolean success = launchTo(hub, cmd);
		// 不成功，唤醒钩子
		if (!success) {
			DownloadMassHook hook = shift.getHook();
			hook.done();
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/**
	 * 处理流程：<br>
	 * 1. 判断处理成功 <br>
	 * 2. 判断数据在文件或者内存，走对应的路线 <br>
	 * 3. 建立一个备份文件名 <br>
	 * 4. 文件在硬盘，走硬盘转移模式 <br>
	 * 5. 文件在内存，输出到硬盘 <br>
	 * 6. 如果有旧的同名旧数据块，删除这个数据块文件 <br>
	 * 7. 给备份文件改名，恢复到正确的数据块文件名 <br>
	 * 8. 启动这个数据块文件 <br>
	 */
	@Override
	public boolean ending() {
		ShiftDownloadMass shift = getCommand();
		DownloadMass cmd = shift.getCommand();
		DownloadMassHook hook = shift.getHook();

		// 判断成功下载
		boolean success = isSuccessCompleted();
		// 选择移到缓存映像数据，或者存储数据块
		if (success) {
			if (cmd.isCacheReflex()) {
				success = moveCacheReflex(shift);
			} else {
				success = moveChunk(shift);
			}
		}
		// 如果成功，设置文件名
		if (success) {
			hook.setFilename(shift.getFilename());
		}
		// 唤醒钩子
		hook.done();

		Logger.debug(this, "ending", success, "target file '%s'",
				shift.getFilename());

		return useful(success);
	}

	/**
	 * 将下载的缓存数据块，转移到本地的缓存映像目录下面
	 * @param shift
	 * @return 成功返回真，否则假
	 */
	private boolean moveCacheReflex(ShiftDownloadMass shift) {
		// 目标文件名
		final String target = shift.getFilename();
		// 在目标文件基础上，生成一个临时文件名
		String temp = createTempFile(target);

		// 判断数据在磁盘上，走硬盘/内存流程
		EchoBuffer buff = findBuffer(0);
		boolean success = buff.isDisk();
		if (success) {
			// 取出回显目录下的文件名
			String source = buff.getFilename();
			// 把ECHO文件移到CACHE REFLEX目录下面
			success = diskTo(source, temp);
		} else {
			// 读取内存中的数据，写到CACHE REFLEX目录下面
			success = memoryTo(buff, temp);
		}

		// 如果有旧的CACHE REFLEX文件，删除它
		if (success) {
			File file = new File(target);
			if (file.exists()) {
				success = file.delete();
			}
		}
		// 改名
		if(success) {
			success = rename(temp, target);
		}

		// 返回结果
		return success;
	}

	/**
	 * 将下载的CHUNK数据块，转移到指定的目录下面。
	 * @param shift
	 * @return 成功返回真，否则假
	 */
	private boolean moveChunk(ShiftDownloadMass shift) {
		DownloadMass cmd = shift.getCommand();
		StubFlag flag = cmd.getFlag();

		// 目标文件名
		final String target = shift.getFilename();
		// 在目标文件基础上，生成一个临时文件名
		String temp = createTempFile(target);

		// 判断数据在磁盘上，走硬盘/内存流程
		EchoBuffer buff = findBuffer(0);
		boolean success = buff.isDisk();
		if (success) {
			// 取出回显目录下的文件名
			String source = buff.getFilename();
			// 把ECHO文件移到CHUNK目录
			success = diskTo(source, temp);
		} else {
			// 读取内存中的数据，写到CHUNK目录下面
			success = memoryTo(buff, temp);
		}

		// 成功，删除旧的磁盘文件
		if (success) {
			success = deleteChunk(flag);
		}
		// 修改文件名称
		if (success) {
			success = rename(temp, target);
		}
		// 重新加载数据块文件
		if (success) {
			success = reloadStub(flag.getSpace(), target);
		}

		return success;
	}

	/**
	 * 建立一个临时文件
	 * @param filename
	 * @return
	 */
	private String createTempFile(String filename) {
		return String.format("%s.temp", filename);
	}

	/**
	 * 执行内存转磁盘的处理工作
	 * @param buf 异步缓存
	 * @param temp 目标文件
	 * @return 成功返回真，否则假
	 */
	private boolean memoryTo(EchoBuffer buf, String temp) {
		int length = buf.getMemorySize();
		long seek = 0L;
		byte[] b = new byte[10240];
		// 从内存中读出字节数组，输入到磁盘
		while (seek < length) {
			// 从缓存中读取文件
			int len = buf.readMemory(b, 0, b.length);
			// 写文件
			AccessTrustor.append(temp, b, 0, len);
			// 移动下标
			seek += len;
		}

		boolean success = (seek == length);

		Logger.debug(this, "memoryTo", success, "memory to %s, filesize compare:%d - %d",
				temp, seek, length);

		return success;
	}

	/**
	 * 调用JNI.DB接口，删除数据块
	 * @param flag
	 * @return
	 */
	private boolean deleteChunk(StubFlag flag) {
		// 判断数据块存在
		Space space = flag.getSpace();
		long stub = flag.getStub();
		boolean success = AccessTrustor.hasChunk(space, stub);
		Logger.debug(this, "deleteChunk", success, "check %s", flag);
		// 不存在，返回真
		if (!success) {
			return true;
		}
		// 删除磁盘文件
		int ret = AccessTrustor.deleteChunk(space, stub);
		success = (ret >= 0);
		Logger.debug(this, "deleteChunk", success, "delete %s is %d", flag, ret);
		// 返回结果
		return success;
	}

	/**
	 * 更改文件名
	 * @param source 源文件名
	 * @param target 目标文件名
	 * @return 返回真或者假
	 */
	private boolean rename(String source, String target) {
		File file = new File(source);
		boolean success = file.renameTo(new File(target));
		Logger.debug(this, "rename", success, "%s rename to %s, file size:%d",
				source, target, file.length());
		return success;
	}

	/**
	 * 重新加载数据块
	 * @param space 数据表名
	 * @param filename 文件名
	 * @return 返回成功或者失败
	 */
	private boolean reloadStub(Space space, String filename) {
		int ret = AccessTrustor.loadChunk(space, filename);
		boolean success = (ret >= 0);
		Logger.debug(this, "reloadStub", success, "load %s#%s is %d", space, filename, ret);
		return success;
	}

	/**
	 * 移动磁盘文件
	 * @param source 源文件
	 * @param temp 临时目标文件
	 * @return 成功返回真，否则假
	 */
	private boolean diskTo(String source, String temp) {
		// 文件长度
		final long length = AccessTrustor.length(source);
		// 每次读取长度
		int maxsize = 102400; 
		// 文件下标，从0开始
		long seek = 0L;
		// 移到文件
		while(seek < length) {			
			// 最大限制长度
			int len = (int) Laxkit.limit(seek, length, maxsize);
			// 读文件
			byte[] b = AccessTrustor.read(source, seek, len);
			// 写文件
			AccessTrustor.append(temp, b, 0, b.length);
			// 移动下标
			seek += b.length;
		}

		// 检查数据块长度
		long filen = AccessTrustor.length(temp);

		// 判断成功
		boolean success = (seek == length);
		Logger.debug(this, "diskTo", success, "%s move to %s, filesize compare: %d - %d", 
				source, temp, length, filen);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		Logger.debug(this, "destroy", "%d usedtime:%d", getInvokerId(), getRunTime());
		super.destroy();
	}
}