/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.each.*;

/**
 * 账号标记 <br><br>
 * 
 * 账号在磁盘文件开始位置头域描述。<br>
 * 
 * 包括三个参数：状态码、总长度、内容长度、内容校验和，共17个字节。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/1/2018
 * @since laxcus 1.0
 */
public final class AccountTag implements Classable, Serializable, Cloneable, Comparable<AccountTag> {

	private static final long serialVersionUID = 9035864448657792688L;

	/** 有效 **/
	public final static byte ENABLED = 127;

	/** 无效 **/
	public final static byte DISABLE = 121;

	/** 标记校验和，EACH算法 **/
	private long tagSum;

	/** 状态码：有效/删除 **/
	private byte status;

	/** 总长度 **/
	private int length;

	/** 内容长度 **/
	private int contentLength;

	/** 内容校验和，EACH算法 **/
	private long contentSum;

	/**
	 * 占用字节空间，固定25个字节长度
	 * @return 25个字节
	 */
	public static final int capacity() {
		return 25;
	}

	/**
	 * 将账号标记写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 标记校验和
		writer.writeLong(tagSum);
		// 状态
		writer.write(status);
		// 数据总长度
		writer.writeInt(length);
		// 内容长度
		writer.writeInt(contentLength);
		// 内容校验和
		writer.writeLong(contentSum);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析账号标记
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		tagSum = reader.readLong();
		// 状态码
		status = reader.read();
		// 数据总长度
		length = reader.readInt();
		// 内容长度
		contentLength = reader.readInt();
		// 内容校验和
		contentSum = reader.readLong();
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个私有的账号标记
	 */
	private AccountTag() {
		super();
		status = AccountTag.DISABLE;
	}

	/**
	 * 根据传入的账号标记，生成它的数据副本
	 * @param that AccountTag实例
	 */
	private AccountTag(AccountTag that) {
		this();
		tagSum = that.tagSum;
		status = that.status;
		length = that.length;
		contentLength = that.contentLength;
		contentSum = that.contentSum;
	}

	/**
	 * 构造账号标记，指定状态码、总长度、内容长度、内容校验和
	 * @param status 状态码
	 * @param length 总长度
	 * @param contentLength 内容长度
	 * @param contentSum 内容校验和
	 */
	public AccountTag(byte status, int length, int contentLength, long contentSum) {
		this();
		setStatus(status);
		setLength(length);
		setContentLength(contentLength);
		setContentSum(contentSum);
	}

	/**
	 * 从可类化数据读取器中解析账号标记
	 * @param reader 可类化数据读取器
	 */
	public AccountTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析账号标记参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public AccountTag(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}
	
	/**
	 * 从传入的字节数组中解析账号标记参数
	 * @param b 字节数组
	 */
	public AccountTag(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 输出标记校验和字节流，包括：状态码、总长度、内容长度、内容校验和4个参数。
	 * @return 返回字节流
	 */
	private byte[] getPrefixStream() {
		ClassWriter writer = new ClassWriter();
		writer.write(status);
		writer.writeInt(length);
		writer.writeInt(contentLength);
		writer.writeLong(contentSum);
		return writer.effuse();
	}

	/**
	 * 生成标记校验和
	 */
	public long lookup() {
		byte[] b = getPrefixStream();
		return EachTrustor.sign(b);
	}

	/**
	 * 检查标记校验和
	 * @return 匹配返回真，否则假
	 */
	public boolean checkLookup() {
		return tagSum == lookup();
	}

	/**
	 * 返回标记校验和
	 * @return 长整型
	 */
	public long getTagSum() {
		return tagSum;
	}
	
	/**
	 * 设置标记校验和
	 * @param sum
	 */
	public void setTagSum(long sum) {
		this.tagSum = sum;
	}

	/**
	 * 返回状态码
	 * @return 状态码
	 */
	public byte getStatus() {
		return status;
	}

	/**
	 * 设置状态码
	 * @param who 状态码
	 */
	public void setStatus(byte who) {
		status = who;		
	}

	/**
	 * 判断有效
	 * @return 返回真或者假
	 */
	public boolean isEnabled() {
		return status == AccountTag.ENABLED;
	}

	/**
	 * 判断无效
	 * @return 返回真或者假
	 */
	public boolean isDisabled() {
		return status == AccountTag.DISABLE;
	}

	/**
	 * 设置内容长度
	 * @param who 整型
	 */
	public void setContentLength(int who) {
		contentLength = who;
	}

	/**
	 * 返回内容长度
	 * @return 整型
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * 设置内容校验和
	 * @param who 长整型
	 */
	public void setContentSum(long who) {
		contentSum = who;
	}

	/**
	 * 返回内容校验和
	 * @return 长整型
	 */
	public long getContentSum() {
		return contentSum;
	}

	/**
	 * 设置账号域总长度
	 * @param who 整型
	 */
	public void setLength(int who) {
		length = who;
	}

	/**
	 * 返回账号域总长度
	 * @return 整型
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 返回填充字节尺寸。这部分尺寸坐为格式化填充和扩展使用存在
	 * @return 填充字节尺寸
	 */
	public int getPadding() {
		return length - (AccountTag.capacity() + contentLength);
	}

	/**
	 * 建立一个当前账号标记的数据副本
	 * @return AccountTag实例
	 */
	public AccountTag duplicate() {
		return new AccountTag(this);
	}

	/**
	 * 比较两个账号标记一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != AccountTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((AccountTag) that) == 0;
	}

	/**
	 * 返回账号标记的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)( contentLength ^ contentSum);
	}

	/**
	 * 返回账号标记的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d/%d/%d", status, contentLength, contentSum);
	}

	/**
	 * 根据当前账号标记，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个账号标记的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AccountTag that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较状态码、内容长度、文件下标
		int ret = Laxkit.compareTo(tagSum, that.tagSum);
		if (ret == 0) {
			ret = Laxkit.compareTo(status, that.status);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(length, that.length);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(contentLength, that.contentLength);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(contentSum, that.contentSum);
		}
		return ret;
	}

	/**
	 * 账号标记生成数据流输出
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析账号标记，返回解析的长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}