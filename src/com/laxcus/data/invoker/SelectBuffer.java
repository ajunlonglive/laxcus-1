/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.util.*;

/**
 * SELECT检索缓存
 * 
 * @author scott.liang
 * @version 1.0 24/1/2018
 * @since laxcus 1.0
 */
public class SelectBuffer {

	/** 最大长度 **/
	public final static long MAXSIZE = 32 * 1024 * 1024;
	
	/** 文件后缀名 **/
	public final static String SUFFIX = ".look";

	/** 调用器编号 **/
	private long invokerId;

	/** 保存文件名 **/
	private ArrayList<File> files = new ArrayList<File>();

	/** SELECT数据缓存 **/
	private ContentBuffer buffer;

	/**
	 * 销毁数据
	 */
	public void destroy() {
		// 删除磁盘文件
		if (files != null) {
			for (File file : files) {
				if (file.exists()) {
					boolean success = file.delete();
					Logger.debug(this, "destroy", success, "delete %s", file);
				}
			}
			files.clear();
			files = null;
		}
		// 释放内存
		if (buffer != null) {
			buffer.destroy();
			buffer = null;
		}
	}

	/**
	 * 构造检索数据缓存
	 * @param ondisk 数据写入磁盘
	 */
	public SelectBuffer(boolean ondisk) {
		super();
		// 如果要求写入磁盘文件，先建立一个文件
		if (ondisk) {
			initFile();
		} else {
			buffer = new ContentBuffer();
		}
	}

	/**
	 * 设置调用器编号。小于0是错误
	 * @param id 调用器编号
	 */
	public final void setInvokerId(long id) {
		if (InvokerIdentity.isInvalid(id)) {
			throw new IllegalValueException("illegal invoker identity %d", id);
		}
		invokerId = id;
	}

	/**
	 * 判断数据在磁盘
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return buffer == null && files.size() > 0;
	}

	/**
	 * 判断数据在内存
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return buffer != null && files.size() == 0;
	}
	
	/**
	 * 输出全部文件名
	 * 
	 * @return
	 */
	public File[] getFiles() {
		File[] a = new File[files.size()];
		return files.toArray(a);
	}

	/**
	 * 输出全部内存
	 * 
	 * @return
	 */
	public byte[] getMemory() {
		return buffer.toByteArray();
	}

	/**
	 * 返回调用器编号
	 * @return 调用器编号
	 */
	public final long getInvokerId() {
		return invokerId;
	}

	/**
	 * 建立一个新文件
	 * @return 文件实例
	 */
	private File createFile() {
		for (int index = 0; index < java.lang.Integer.MAX_VALUE; index++) {
			String name = String.format("%X_%X%s", invokerId, index, SUFFIX);
			File file = new File(EchoArchive.getDirectory(), name);
			// 不存在，输出
			if (!file.exists()) {
				return file;
			}
		}
		return null;
	}

	/**
	 * 初始化产生一个磁盘文件
	 */
	private void initFile() {
		File file = createFile();
		files.add(file);
	}
	
	/**
	 * 从知文件队列中，选择一个新文件
	 * @param length 数据长度
	 * @return 返回文件
	 */
	private File choice(long length) {
		int size = files.size();
		if (size > 0) {
			File last = files.get(size - 1);
			if (last.length() + length < MAXSIZE) {
				return last;
			}
		}
		// 返回一个新文件
		File file = createFile();
		files.add(file);
		return file;
	}

	/**
	 * 添加数据到磁盘或者内存
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public int append(byte[] b, int off, int len) {
		if(buffer != null){
			return writeMemory(b, off, len);
		} else {
			return writeDisk(b, off, len);
		}
	}

	/**
	 * 添加数据到磁盘或者内存
	 * @param b
	 * @return
	 */
	public int append(byte[] b) {
		return append(b, 0, b.length);
	}

	/**
	 * 数据写入磁盘
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	private int writeDisk(byte[] b, int off, int len) {
		File file = choice(len);
		long seek = 0;
		if (file.exists()) seek = file.length();
		// 在文件后面追加
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			out.write(b, off, len);
			out.flush();
			out.close();
			// 返回追加的文件尺寸
			return (int) (file.length() - seek);
		} catch (IOException e) {
			throw new EchoException(e);
		}
	}
	
	/**
	 * 写数据到内存。如果写入过程中发生内存溢出，数据将转入磁盘
	 * @param b 字节数组
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
	 * @return 返回写入的字节长度
	 * @throws EchoException
	 */
	private int writeMemory(byte[] b, int off, int len) {
		int count = 0;
		try {
			count = buffer.append(b, off, len);
		} catch (OutOfMemoryError e) {
			// 内存溢出，数据写入磁盘
			try {
				File file = choice(len);
				buffer.flushTo(file, false);
			} catch (IOException exp) {
				throw new EchoException(exp);
			}
			// 缓存置空
			buffer = null;
			// 写入新数据
			count = writeDisk(b, off, len);
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

}