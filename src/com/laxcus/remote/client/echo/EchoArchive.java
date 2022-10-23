/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 回显数据存储地。
 * 
 * @author scott.liang
 * @version 1.0 10/12/2009
 * @since laxcus 1.0
 */
public class EchoArchive {
	
	/** 最大缓存尺寸，超过这个参数，数据将转入硬盘存取。默认是0，不限制 **/
	private static volatile long maxBufferSize = 0;
	
	/** 文件后缀名 **/
	public final static String FILE_SUFFIX = ".echo";

	/** 回显根目录 **/
	private static File root;

	/**
	 * 设置当前站点存储异步应答数据的目录
	 * @param dir 目录
	 */
	public static void setDirectory(File dir) {
		// 目录不允许是空指针
		Laxkit.nullabled(dir);
		
		// 如果目录不存在，建立它
		if (!(dir.exists() && dir.isDirectory())) {
			boolean success = dir.mkdirs();
			if (!success) {
				throw new EchoException("cannot create \"%s\"", dir);
			}
		}
		EchoArchive.root = dir.getAbsoluteFile();
	}

	/**
	 * 设置当前站点存储异步应答数据的目录
	 * @param path 目录路径
	 */
	public static void setDirectory(String path) {
		path = ConfigParser.splitPath(path);
		EchoArchive.setDirectory(new File(path));
	}

	/**
	 * 返回站点的回显目录
	 * @return 回显目录的java.io.File实例
	 */
	public static File getDirectory() {
		return EchoArchive.root;
	}

	/**
	 * 设置最大内存空间
	 * @param len 内存尺寸
	 */
	public static void setMaxBufferSize(long len) {
		EchoArchive.maxBufferSize = len;
	}

	/**
	 * 返回最大内存空间
	 * @return 内存空间
	 */
	public static long getMaxBufferSize() {
		return EchoArchive.maxBufferSize;
	}

	/**
	 * 判断内存空间超过最大限制值
	 * @param len 内存尺寸
	 * @return 返回真或者假
	 */
	private boolean isMaxBuffer(long len) {
		return EchoArchive.maxBufferSize > 0
				&& len >= EchoArchive.maxBufferSize;
	}

	/** 回显标识 **/
	private EchoFlag flag;

	/** 应答数据缓存 **/
	private ContentBuffer buffer;

	/** 磁盘文件。如有效，数据与入磁盘，而不是内存 **/
	private File file;

	/**
	 * 构造回显数据存储文件，指定所需参数
	 * @param flag 回显标识
	 * @param ondisk 回显数据写入磁盘
	 */
	public EchoArchive(EchoFlag flag, boolean ondisk) {
		super();
		setFlag(flag);
		// 判断建立文件或者内存空间
		if (ondisk) {
			createFile();
		} else {
			buffer = new ContentBuffer();
		}
	}

	/**
	 * 设置回显标识，不允许空指针
	 * @param e 回显标识
	 */
	private void setFlag(EchoFlag e) {
		Laxkit.nullabled(e);
		flag = e;
	}

	/**
	 * 判断数据是写入磁盘
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return buffer == null && file != null;
	}

	/**
	 * 判断数据写入缓存
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return buffer != null && file == null;
	}

	/**
	 * 返回磁盘文件
	 * @return 磁盘文件名
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 返回文件的绝对路径名称
	 * @return 磁盘文件名绝对路径
	 */
	public String getFilename() {
		return file.getAbsolutePath();
	}

	/**
	 * 在系统定义的回显目录里建立一个磁盘文件
	 * @param e 回显标识
	 */
	private void createFile() {
		// 不允许空值
		if (EchoArchive.root == null) {
			throw new EchoException("cannot be null");
		}
		// 在回显目录下建立一个文件实例
		String name = String.format("%d_%d%s", flag.getInvokerId(), flag.getIndex(), EchoArchive.FILE_SUFFIX);
		file = new File(EchoArchive.root, name);
		// 如果文件存在，删除它
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 返回缓存中的数据长度
	 * @return 数据长度的整型值
	 */
	public int getMemorySize() {
		return buffer.length();
	}

	/**
	 * 输出缓存数据，原数据在内存中仍然保留
	 * @return byte数组
	 */
	public byte[] getMemory() {
		return buffer.toByteArray();
	}

	/**
	 * 从内存中读出全部数据（原数据从缓存中清除）
	 * @return byte数组
	 */
	public byte[] readFullMemory() {
		return buffer.readFully();
	}

	/**
	 * 从内存中读取一段数据
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 指定长度
	 * @return 读取长度
	 */
	public int readMemory(byte[] b, int off, int len) {
		return buffer.read(b, off, len);
	}

	/**
	 * 把内存中的数据写入到磁盘上
	 * @param file 磁盘文件名
	 * @param append 采用追加模式
	 * @throws IOException
	 */
	public void flushMemoryToDisk(File file, boolean append) throws IOException {
		if (isDisk()) {
			throw new IOException("on disk!");
		}
		buffer.flushTo(file, append);
	}

	/**
	 * 写数据到内存或者磁盘文件，返回写入的字节长度
	 * 
	 * @param b 字节数组
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
	 * 
	 * @return 返回追加的文件尺寸，以字节为单位。如果出错，返回-1
	 * @throws EchoException
	 */
	public int write(byte[] b, int off, int len) {
		// 先检查数据在内存，否则写入磁盘
		if (buffer != null) {
			return writeMemory(b, off, len);
		} else {
			return writeDisk(b, off, len);
		}
	}
	
	/**
	 * 数据内容转向硬盘存取
	 * @param b 字节数组
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
	 * @return 返回写入的字节长度
	 */
	private int switchToDisk(byte[] b, int off, int len) {
		// 建立磁盘文件
		createFile();
		
		Logger.debug(this, "switchToDisk", "change to %s", file);
		
		// 数据写入磁盘
		try {
			buffer.flushTo(file, false);
		} catch (IOException exp) {
			throw new EchoException(exp);
		}
		// 缓存置空
		buffer = null;
		// 写入新数据
		return writeDisk(b, off, len);
	}
	
	/**
	 * 写数据到内存。<br>
	 * 如果写入过程中发生内存溢出，数据将转入磁盘。
	 * 
	 * @param b 字节数组
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
	 * @return 返回写入的字节长度
	 * @throws EchoException
	 */
	private int writeMemory(byte[] b, int off, int len) {
		// 判断内存达到最大限制值
		int size = (buffer.length() + len);
		if (isMaxBuffer(size)) {
			return switchToDisk(b, off, len);
		}

		// 仍然写内存，超出内存尺寸转入硬盘
		int count = 0;
		try {
			count = buffer.append(b, off, len);
		} catch (OutOfMemoryError e) {
			Logger.error(e);
			Logger.error(this, "writeMemory",
					"memory out! switch to local file, current memory size:%d",
					buffer.length());

			// 内存数据写入硬盘
			count = switchToDisk(b, off, len);
		}
		// 返回写入的字节长度
		return count;
	}

//	/**
//	 * 写数据到内存。<br>
//	 * 如果写入过程中发生内存溢出，数据将转入磁盘。
//	 * 
//	 * @param b 字节数组
//	 * @param off 有效数据开始下标
//	 * @param len 有效数据长度
//	 * @return 返回写入的字节长度
//	 * @throws EchoException
//	 */
//	private int writeMemory(byte[] b, int off, int len) {
//		int count = 0;
//		try {
//			count = buffer.append(b, off, len);
//		} catch (OutOfMemoryError e) {
//			Logger.error(e);
//			Logger.error(this, "writeMemory",
//					"memory out! switch to local file, current memory size:%d", buffer.length());
//
//			// 建立磁盘文件
//			createFile();
//			// 数据写入磁盘
//			try {
//				buffer.flushTo(file, false);
//			} catch (IOException exp) {
//				throw new EchoException(exp);
//			}
//			// 缓存置空
//			buffer = null;
//			// 写入新数据
//			count = writeDisk(b, off, len);
//		}
//		// 返回写入的字节长度
//		return count;
//	}

	/**
	 * 写数据到磁盘
	 * @param b 字节数组
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
	 * @return 返回写入的字节长度
	 * @throws EchoException
	 */
	private int writeDisk(byte[] b, int off, int len) {
		long filen = file.length();
		// 在文件后面追加
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			out.write(b, off, len);
			out.flush();
			out.close();
			// 返回追加的文件尺寸
			return (int) (file.length() - filen);
		} catch (IOException e) {
			throw new EchoException(e);
		}
	}

	/**
	 * 销毁全部资源
	 */
	public void destroy() {
		if (file != null) {
			if (file.exists()) {
				boolean success = file.delete();
				Logger.debug(this, "destroy", success, "delete %s", file);
			}
			file = null;
		}
		if (buffer != null) {
			buffer.clear();
			buffer = null;
			
			Logger.debug(this, "destroy", "release memory!");
		}
	}

	/**
	 * 垃圾回收
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

	//	private static void test() {
	//		System.setProperty("laxcus.echo.dir", "g:/echo");
	//		EchoFlag flag = new EchoFlag(99, 1);
	//		EchoArchive arch = new EchoArchive(flag, true);
	//		
	//		byte[] b =new byte[7];
	//		for(char c = 'A'; c <='Z'; c++) {
	////			System.out.printf("size is %d\n", arch.file.length());
	//			for(int i =0; i <b.length; i++) {
	//				b[i] = (byte)c;
	//			}
	//			arch.write(b, 0, b.length);
	//			
	//		}
	//		System.out.println("okay");
	//		
	////		System.out.printf("buffer size:%d\n", 	arch.getBuffsrSize());
	////		b = arch.getBuffer();
	////		System.out.printf("%s\n", new String(b, 0, b.length));
	//	}
	//	
	//	public static void main(String[] args) {
	//		EchoArchive.test();
	//	}

}
