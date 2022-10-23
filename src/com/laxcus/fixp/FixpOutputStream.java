/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.io.*;

import com.laxcus.util.*;

/**
 * FIXP协议数据输出流。<br>
 * 数据输入支持加密传输
 * 
 * @author scott.liang
 * @version 1.3 3/9/2013
 * @since laxcus 1.0
 */
public class FixpOutputStream extends FilterOutputStream {

	/** 对称加密算法密文 **/
	private Cipher cipher;

	/**
	 * 构造FIXP协议数据输出流
	 * @param out - 数据输出流
	 */
	public FixpOutputStream(OutputStream out) {
		super(out);
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

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		byte[] s = new byte[1];
		s[0] = (byte) b;
		this.write(s, 0, s.length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	/*
	 * 发送数据，如果没有密文直接传输，否则先加密再传输
	 * 
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		if (cipher == null) {
			out.write(b, off, len);
		} else {
			byte[] s = cipher.encrypt(b, off, len);
			byte[] i = Laxkit.toBytes(s.length);
			out.write(i, 0, i.length);
			out.write(s, 0, s.length);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#flush()
	 */
	public void flush() throws IOException {
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#close()
	 */
	public void close() throws IOException {
		this.cipher = null;
		super.close();
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