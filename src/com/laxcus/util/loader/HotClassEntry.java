/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.loader;

import java.io.*;
import java.net.*;

import com.laxcus.util.*;
import com.laxcus.util.hash.*;

/**
 * 发布的JAR档案文件。<br><br>
 * 
 * 它保存JAR文件的用户签名、文件路径、文件路径URL、JAR字节数组。<br>
 * 用户签名应用在分布任务组件中，普通的JAR文件忽略这一项。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 11/3/2017
 * @since laxcus 1.0
 */
public final class HotClassEntry implements Comparable<HotClassEntry> {

	/** 用户签名。这是一个可选项，用在分布任务组件条件下。 **/
	private Siger issuer;

	/** 磁盘文件全路径 **/
	private String path;

	/** 文件的URL **/
	private URL url;
	
	/** 内容散列码 **/
	private MD5Hash hash;

	/** 磁盘文件的字节数组 **/
	private byte[] content;

	/**
	 * 构造发布的JAR档案文件，指定磁盘文件路径，同时读取磁盘文件。
	 * @param path 磁盘文件路径
	 * @throws IOException 读取磁盘文件名时发生异常
	 */
	public HotClassEntry(String path) throws IOException {
		super();
		setPath(path);
		// 读磁盘文件
		read();
	}

	/**
	 * 构造发布的JAR档案文件，指定磁盘文件路径和JAR数据内容
	 * @param path 磁盘文件路径。
	 * @param content JAR文件数据内容
	 */
	public HotClassEntry(String path, byte[] content) {
		super();
		setPath(path);
		setContent(content);
	}

	/**
	 * 构造发布的JAR档案文件，指定用户签名、磁盘文件路径、JAR数据内容
	 * @param issuer 用户签名
	 * @param path 磁盘文件路径。
	 * @param content JAR文件数据内容
	 */
	public HotClassEntry(Siger issuer, String path, byte[] content) {
		this(path, content);
		setIssuer(issuer);
	}

	/**
	 * 构造发布的JAR档案文件，指定用户签名、磁盘文件路径、JAR数据内容
	 * @param issuer 用户签名
	 * @param path 磁盘文件路径。
	 * @param url 路径的URL格式
	 * @param content JAR文件数据内容
	 */
	public HotClassEntry(Siger issuer, String path, URL url, byte[] content) {
		this(issuer, path, content);
		setURL(url);
	}

	/**
	 * 设置用户签名
	 * @param e 用户签名
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 设置磁盘文件路径。<br><br>
	 * 
	 * 注意：这个文件是磁盘的全路径名称，不可随意改变成其它名称！！！否则可能出现安全检查错误。<br>
	 * 即系统执行分布任务组件或者码位计算器的FilePermission检查时，因为与*.policy文件中的“codeBase”选项的
	 * 路径不匹配，将弹出安全错误。<br>
	 * 
	 * @param e 磁盘文件全路径
	 */
	private void setPath(String e) {
		Laxkit.nullabled(e);
		path = e;
	}

	/**
	 * 输出磁盘文件路径。<br><br>
	 * 
	 * <b>注意：这个文件是磁盘的全路径名称，随意改变可能发生安全检查错误。<br>
	 * 即系统执行分布任务组件或者码位计算器的FilePermission检查时，因为与*.policy文件中的“codeBase”的
	 * 路径不匹配，将弹出安全错误。</b><br>
	 * 
	 * @return 磁盘文件全路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 设置路径的URL，允许空指针
	 * @param e URL
	 */
	public void setURL(URL e) {
		url = e;
	}

	/**
	 * 返回磁盘文件的URL。<br>
	 * 主机和端口都是忽略，返回格式是“file:文件全路径”。
	 * 
	 * @return URL实例
	 */
	public URL getURL() throws MalformedURLException {
		if (url != null) {
			return url;
		}
		return new File(path).toURI().toURL();
	}

	/**
	 * 把JAR文件读取到内容
	 * @throws IOException
	 */
	private void read() throws IOException {
		File file = new File(path);
		int len = (int) file.length();
		byte[] b = new byte[len];
		FileInputStream in = new FileInputStream(file);
		in.read(b, 0, b.length);
		in.close();
		
		// 保存内存!
		setContent(b);
	}
	
	/**
	 * 返回MD5签名
	 * @return MD5Hash
	 */
	public MD5Hash getHash() {
		return hash;
	}

	/**
	 * 设置JAR文件数据内容
	 * @param b JAR文件字节数组
	 */
	private void setContent(byte[] b) {
		Laxkit.nullabled(b);
		// 保存字节
		content = b;
		// 生成签名
		hash = Laxkit.doMD5Hash(content);
	}

	/**
	 * 返回JAR文件数据内容
	 * @return JAR文件字节数组
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 返回JAR文件长度
	 * @return 整数
	 */
	public int getContentSize() {
		return (content == null ? -1 : content.length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != HotClassEntry.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((HotClassEntry) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return path.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(HotClassEntry that) {
		if (that == null) {
			return 1;
		}

		// 比较签名
		int ret = Laxkit.compareTo(issuer, that.issuer);
		// 判断内容签名一致！
		if (ret == 0) {
			ret = Laxkit.compareTo(hash, that.hash);
		}
		// 比较文件名，如果是WINDOWS系统，大小写不敏感，否则是敏感
		if (ret == 0) {
			ret = Laxkit.compareTo(path, that.path, true);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		issuer = null;
		path = null;
		url = null;
		hash = null;
		content = null;
	}

}