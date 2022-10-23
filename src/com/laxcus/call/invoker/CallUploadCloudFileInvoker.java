/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.io.*;

import com.laxcus.call.pool.*;
import com.laxcus.command.cloud.store.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;

/**
 * 上传文件调用器
 * 
 * @author scott.liang
 * @version 1.0 10/29/2021
 * @since laxcus 1.0
 */
public class CallUploadCloudFileInvoker extends CallInvoker {

	/**
	 * 构造上传文件调用器
	 * @param cmd
	 */
	public CallUploadCloudFileInvoker(UploadCloudFileGuide cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public UploadCloudFileGuide getCommand() {
		return (UploadCloudFileGuide) super.getCommand();
	}

	/**
	 * 判断允许上传
	 * @param srl
	 * @return
	 */
	private int allow(UploadCloudFileGuide cmd){
		SRL srl = cmd.getSRL();

		boolean exists = StoreOnCallPool.getInstance().hasFile(getIssuer(), srl.getPath());
		// 不允许覆盖并且存在时
		if (!cmd.isOverride() && exists) {
			return StoreState.EXISTS; // 文件存在的提示
		}

		// 剩余容量
		long freeCapacity = StoreOnCallPool.getInstance().getFreeCapacity(getIssuer());

		// 判断磁盘剩余容量
		long length = cmd.getLength();
		// 剩余容量不足时...
		if (freeCapacity < length) {
			return StoreState.DISK_MISSING;
		}

		return StoreState.SUCCESSFUL;
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		UploadFileGuide cmd = getCommand();
//		SRL srl = cmd.getSRL();
//		// FRONT调用器监听地址
//		Cabin source = cmd.getSource(); 
//
//		// 判断允许或者否
//		int state = allow(cmd);
//		boolean allow = StoreState.isSuccessful(state);
//
//		// 反馈给FRONT节点
//		UploadFileProduct product = new UploadFileProduct(state);
//		product.setSRL(srl);
//
//		// 强制设置为磁盘存储 
//		setDisk(true);
//		// 反馈结果
//		boolean success = replyTo(source, product);
//
//		// 以上不成功，向FRONT返回错误
//		if (!success) {
//			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
//		}
//
//		// 退出
//		if (!allow) {
//			super.setQuit(true);
//		}
//
//		Logger.debug(this, "launch", allow, "reply to %s", source);
//
//		return allow;
//	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		UploadCloudFileGuide cmd = getCommand();
		SRL srl = cmd.getSRL();
		// FRONT调用器监听地址
		Cabin source = cmd.getSource(); 
		
		Logger.debug(this, "launch", "cabin is [%s]", source);

		// 判断允许或者否
		int state = allow(cmd);
		boolean pass = StoreState.isSuccessful(state);
		
		// 反馈给FRONT节点
		UploadCloudFileProduct product = new UploadCloudFileProduct(state);
		product.setSRL(srl);

		// 如果不允许，返回一个错误
		if (!pass) {
			replyProduct(source, product);
			return false;
		}

		// 强制设置为磁盘存储 
		boolean ondisk = isDisk();
		setDisk(true);
		// 反馈结果
		boolean success = replyTo(source, product);
		// 恢复原样
		setDisk(ondisk);

		// 以上不成功，向FRONT返回错误
		if (!success) {
			replyFault(Major.FAULTED, Minor.SYSTEM_FAILED);
		}
		
		Logger.debug(this, "launch", success, "reply to %s", source);

		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 如果接收FRONT站点数据失败，退出
		if (isFaultCompleted()) {
			super.replyFault();
			return false;
		}

		UploadCloudFileGuide cmd = getCommand();
		SRL srl = cmd.getSRL();
		
		// 承接FIRST，第一个且只有一个索引编号
		int index = findEchoKey(0);
		// 统一用findItemCabin拿到FRONT的监听地址，（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
		Cabin cabin = findItemCabin(index);
		
		Logger.debug(this, "ending", "front hub: %s", cabin);
		
		// 选择是文件或者内存
		File file = null;
		byte[] memory = null;
		// 取出异步缓存
		EchoBuffer buff = findBuffer(index);
		if (buff.isDisk()) {
			file = buff.getFile();
			Logger.debug(this, "ending", "file is '%s', length: %d", file, file.length());
		} else if(buff.isMemory()){
			memory = buff.getMemory();
			Logger.debug(this, "ending", "memory length: %d", (memory != null ? memory.length : -1));
		}

		VPath path = null;
		Siger issuer = getIssuer();
		
		// 写磁盘或者写内存...
		if (file != null) {
			path = StoreOnCallPool.getInstance().writeFile(file, issuer, srl.getPath());
		} else if (memory != null) {
			path = StoreOnCallPool.getInstance().writeFile(memory, issuer, srl.getPath());
		}
	
		// 判断写入成功，反馈结果
		int state = (path != null ? StoreState.SUCCESSFUL : StoreState.FAILED);
		UploadCloudFileProduct product = new UploadCloudFileProduct(state);
		product.setSRL(srl);
		product.setPath(path);

		// 通知FRONT站点反馈结果
		boolean success = replyProduct(cabin, product);

		// 文件存在，删除它
		if (file != null && file.exists()) {
			file.delete();
		}
		
		Logger.debug(this, "ending", success, "reply to %s", cabin);

		// 结果
		return useful(success);
		
	}
	
//	private boolean doFile(int index, EchoBuffer buffer) {
//		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
//		Cabin frontCabin = findItemCabin(index);
//		// 拿到磁盘文件
//		File file = buffer.getFile();
//
//		return false;
//	}
//	
//	private boolean doMemory(int index, EchoBuffer buffer) {
//		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
//		Cabin frontCabin = findItemCabin(index);
//
//		// 取出内存数据，防止内存溢出！
//		boolean success = false;
//		try {
//			byte[] frontContent = buffer.getMemory();
//			success = true;
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//
//		// 出错退出
//		if (!success) {
//			replyFault(frontCabin, Major.FAULTED, Minor.MEMORY_MISSING);
//			return false;
//		}
//		
//		return false;
//	}

	
//	public boolean endingX() {
//		// 如果接收FRONT站点数据失败，退出
//		if (isFaultCompleted()) {
//			super.replyFault();
//			return false;
//		}
//
//		UploadCloudFileGuide cmd = getCommand();
//		SRL srl = cmd.getSRL();
//
//		// 找到本地存储文件
//		int index = findEchoKey(0);
//		File file = findFile(index);
//		// 没有文件
//		if (file == null) {
//			super.replyFault();
//			return false;
//		}
//		Logger.debug(this, "ending", "file is '%s', length: %d", file, file.length());
//
//		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
//		Cabin cabin = findItemCabin(index);
//		// 拿到磁盘文件，写入磁盘
//		Siger issuer = getIssuer();
//		VPath path = StoreOnCallPool.getInstance().writeFile(file, issuer, srl.getPath());
//
//		// 判断写入成功，反馈结果
//		int state = (path != null ? StoreState.SUCCESSFUL : StoreState.FAILED);
//		UploadCloudFileProduct product = new UploadCloudFileProduct(state);
//		product.setSRL(srl);
//		product.setPath(path);
//
//		// 通知FRONT站点反馈结果
//		boolean success = replyProduct(cabin, product);
//
//		// 文件存在，删除它
//		boolean exists = file.exists();
//		if (exists) {
//			file.delete();
//		}
//
//		// 结果
//		return useful(success);
//	}

	//	/**
	//	 * 反馈应答
	//	 * @param cabin
	//	 * @param success
	//	 * @return
	//	 */
	//	private boolean doReply(Cabin cabin, boolean success) {
	//		int state = (success ? StoreState.SUCCESSFUL : StoreState.FAILED);
	//		UploadFileProduct product = new UploadFileProduct(state);
	//		product.setSRL(getCommand().getSRL());
	//
	//		// 通知FRONT站点，成功或者失败（0记录是失败）
	//		return replyProduct(cabin, product);
	//
	//		//		// 退出
	//		//		super.setQuit(true);
	//		//
	//		//		return success;
	//	}
	//
	//	//	private boolean write(Cabin cabin, byte[] content) {
	//	//		UploadFileGuide cmd = getCommand();
	//	//		SRL srl = cmd.getSRL();
	//	//		// 写入磁盘
	//	//		boolean success = StoreOnCallPool.getInstance().writeFile(content,
	//	//				getIssuer(), srl.getPath());
	//	//		// 反馈应答
	//	//		return doReply(cabin, success);
	//	//	}
	//
	//	/**
	//	 * 取出磁盘文件，然后改名！
	//	 * @param index 索引号
	//	 * @param buffer 异步缓存
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doDisk(int index, EchoBuffer buffer) {
	//		UploadFileGuide cmd = getCommand();
	//		SRL srl = cmd.getSRL();
	//
	//		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
	//		Cabin cabin = findItemCabin(index);
	//		// 拿到磁盘文件
	//		File file = buffer.getFile();
	//		// 写入磁盘
	//		boolean success = StoreOnCallPool.getInstance().writeFile(file, getIssuer(), srl.getPath());
	//
	//		////		int len = (int)file.length();
	//		////		byte[] content = new byte[len];
	//		////		try {
	//		////			FileInputStream in = new FileInputStream()
	//		////		}
	//		//
	//		//		Logger.debug(this, "doFile", "this is %s, length:%d, from %s", file, file.length(), cabin);
	//		//
	//		//		// 修改文件名，删除文件名放在最后
	//		//		String temp = String.format("%s.xxx", file.getAbsolutePath());
	//		//		File frontFile = new File(temp);
	//		//		// 文件改名，这个文件在后面使用
	//		//		boolean success = file.renameTo(frontFile);
	//		//
	//		//		Logger.note(this, "doSecondFile", success, "%s rename to %s, file length:%d", 
	//		//				file, frontFile, frontFile.length());
	//		//
	//		//		// 出错退出
	//		//		if (!success) {
	//		//			replyFault(cabin);
	//		//			return false;
	//		//		}
	//
	//		// 文件存在，删除它
	//		boolean exists = file.exists();
	//		if (exists) {
	//			file.delete();
	//		}
	//
	//		// 向FRONT反馈结果
	//		return doReply(cabin, success);
	//	}
	//
	//	/**
	//	 * 从异步缓存中取出内存数据，保存在本地
	//	 * @param index 索引号
	//	 * @param buffer 异步缓存
	//	 * @return 处理成功返回真，否则假
	//	 */
	//	private boolean doMemory(int index, EchoBuffer buffer) {
	//		UploadFileGuide cmd = getCommand();
	//		SRL srl = cmd.getSRL();
	//		// 拿到FRONT的监听地址，保留它，在后面使用（FRONT采用EchoInvoker.replyTo方式发送，其中包含监听地址）
	//		Cabin cabin = findItemCabin(index);
	//
	//		//		// 写入磁盘
	//		//		boolean success = StoreOnCallPool.getInstance().writeFile(content,
	//		//				getIssuer(), srl.getPath());
	//		//		// 反馈应答
	//		//		return doReply(cabin, success);
	//
	//		// 取出内存数据，防止内存溢出！
	//		boolean success = false;
	//		try {
	//			byte[] content = buffer.getMemory();
	//			success = StoreOnCallPool.getInstance().writeFile(content,
	//					getIssuer(), srl.getPath());
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//
	//		//		// 出错退出
	//		//		if (!success) {
	//		//			replyFault(cabin, Major.FAULTED, Minor.MEMORY_MISSING);
	//		//			return false;
	//		//		}
	//
	//		//		Logger.debug(this, "doSecondMemory", success, "memory length: %d", );
	//
	//		//		// 反馈结果
	//		//		return doReply(cabin, success);
	//
	//		// 反馈应答 
	//		return doReply(cabin,success);
	//	}


	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		// 如果接收FRONT站点数据失败，退出
	//		if (isFaultCompleted()) {
	//			return false;
	//		}
	//
	//		// 承接FIRST，第一个且只有一个索引编号
	//		int index = findEchoKey(0);
	//
	//		boolean success = false;
	//		// 取出异步缓存
	//		EchoBuffer buff = findBuffer(index);
	//		if (buff.isDisk()) {
	//			success = doDisk(index, buff);
	//		} else {
	//			success = doMemory(index, buff);
	//		}
	//
	//		Logger.debug(this, "ending", success, "reply result");
	//
	//		return useful( success);
	//	}

}