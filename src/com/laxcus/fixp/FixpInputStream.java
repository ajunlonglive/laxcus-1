/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;

/**
 * FIXP协议数据输入流。<br>
 * 
 * @author scott.liang
 * @version 1.3 3/9/2013
 * @since laxcus 1.0
 */
public class FixpInputStream extends FilterInputStream {

	/** 密文 **/
	private Cipher cipher;

	/** 缓存数据 **/
	private byte[] buff;

	/** 缓存实际数据尺寸 **/
	private int count;
	
	/**
	 * 构造FIXP协议数据输入流
	 * @param in 数据输入流
	 */
	public FixpInputStream(InputStream in) {
		super(in);
		this.count = 0;
	}
	
	/**
	 * 判断是否加密状态
	 * @return 返回真或者假
	 */
	public boolean isSecured() {
		return this.cipher != null;
	}

	/**
	 * 设置密文实例
	 * @param e Cipher实例
	 */
	public void setCipher(Cipher e) {
		this.cipher = e;
	}
	
	/**
	 * 返回密文实例
	 * @return Cipher实例
	 */
	public Cipher getCipher() {
		return this.cipher;
	}
	
	/**
	 * 返回内存中保存的数据长度
	 * @return int类型
	 */
	public int getBuffSize() {
		return this.count;
	}
	
	/**
	 * 向内存中添加数据
	 * @param b 字节数组
	 * @param off 下标位置
	 * @param len 数据长度
	 */
	private void append(byte[] b, int off, int len) {
		if (buff == null) {
			buff = Arrays.copyOfRange(b, off, off + len);
			count = buff.length;
		} else if (count + len <= buff.length) {
			System.arraycopy(b, off, buff, count, len);
			count += len;
		} else {
			byte[] s = Arrays.copyOfRange(buff, 0, count);
			buff = new byte[s.length + len];
			System.arraycopy(s, 0, buff, 0, s.length);
			System.arraycopy(b, off, buff, s.length, len);
			count = buff.length;
		}
	}

	/**
	 * 把数据从内存读到缓存中
	 * @param b 缓存字节数组
	 * @param off 指定下标位置
	 * @param len 最大数据长度
	 * @return 返回读取的字节长度
	 */
	private int readBuff(byte[] b, int off, int len) {
		if (b == null || off < 0 || len < 1 || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		}
		if (count < 1) return 0;
		// 取有效值
		int size = (count < len ? count : len);
		int left = count - size;
		// 复制数据
		System.arraycopy(buff, 0, b, off, size);
		// 数据向前迟动
		if (left > 0) {
			System.arraycopy(buff, size, buff, 0, left);
		}
		count -= size;
		return size;
	}

	/**
	 * 从SOCKET读一段长度的数据流
	 * 
	 * @param size 指定长度
	 * @return 读取的字节数组
	 * @throws IOException
	 */
	private byte[] full(int size) throws IOException {
		if (size < 1) {
			throw new IndexOutOfBoundsException();
		}
		int seek = 0;
		byte[] b = new byte[size];
		while (seek < size) {
			int len = in.read(b, seek, size - seek);
			if (len < 0) throw new EOFException();
			seek += len;
		}
		return b;
	}
	
	/*
	 * 读一个字节
	 * @see java.io.FilterInputStream#read()
	 */
	public int read() throws IOException {
		byte[] b = new byte[1];
		this.read(b, 0, b.length);
		return b[0];
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	/*
	 * 读数据流，如果有密文，先解密
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		// 1. 如果有数据,先读它
		if (count > 0) {
			return readBuff(b, off, len);
		}
		// 2. 如果没有定义密文,直接读
		if (cipher == null) {
			return in.read(b, off, len);
		} else {
			// 3. 如果有加密数据时,先读四个字节的长度
			byte[] s = full(4);
			int size = Laxkit.toInteger(s, 0, s.length);
			// 读一段完整的数据
			s = full(size);
			// 解密数据
			s = cipher.decrypt(s, 0, s.length);
			// 保存
			this.append(s, 0, s.length);
			// 从缓存读取
			return readBuff(b, off, len);
		}
	}

	/**
	 * 从SOCKET读取指定长度的字节流到缓存中。<br>
	 * 注意：如有密文，这是解密后的数据。
	 * 
	 * @param b 缓存字节数组
	 * @param off 指定下标位置
	 * @param len 最大数据长度
	 * @return 返回读取的字节长度
	 * @throws IOException
	 */
	public int readFully(byte[] b, int off, int len) throws IOException {
		if(b == null || off < 0 || len < 1 || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		}
		int seek = off;
		int end = off + len;
		while(seek < end) {
			int size = this.read(b, seek, end - seek);
			if(size < 0) throw new EOFException();
			seek += size;
		}
		return seek - off;
	}

	/**
	 * 从SOCKET读取指定长度的字节流到缓存，并且输出缓存数据。<br>
	 * 注意：如有密文，这是解密后的数据。
	 * 
	 * @param len 指定长度
	 * @return 读取的缓存字节数组
	 * @throws IOException
	 */
	public byte[] readFully(int len) throws IOException {
		if (len < 1) {
			throw new IndexOutOfBoundsException();
		}
		byte[] b = new byte[len];
		this.readFully(b, 0, b.length);
		return b;
	}

	/**
	 * 关闭FIXP流连接
	 * @see java.io.FilterInputStream#close()
	 */
	public void close() throws IOException {
		buff = null;
		count = 0;
		cipher = null;
		super.close();
	}

	/*
	 * 不支持
	 * @see java.io.FilterInputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.FilterInputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		throw new IOException("cannot support!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.FilterInputStream#available()
	 */
	public int available() throws IOException {
		throw new IOException("cannot support!");
	}
	
	/*
	 * jvm clear
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		this.close();
	}
}