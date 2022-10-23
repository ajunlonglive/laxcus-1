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
import com.laxcus.util.classable.*;

/**
 * FIXP协议标头。<br>
 * FIXP协议标头是FIXP协议规定的首段，分为请求和应答两种。具体介绍参见FIXP协议文档。<br><br>
 * 
 * FIXP协议标头格式：<br><br>
 * 
 * 1. FIXP标识：1个字节 。前2位是请求或者应答标识，后6位是命令长度。<br>
 * 2. FIXP版本号：2字节。<br>
 * 3. 主码、从码 或者 应答码 ：2字节。<br>
 * 4. 消息成员数：2字节。<br><br>
 * 
 * FIXP协议标头总长度固定是7个字节，写在第一个字节的后6位。这个参数一定不会变化。<br>
 * FIXP版本号固定是0x100。<br>
 * 如果首字符的前2位是请求标识，第三段是主码和从码，否则是应答码。<br>
 * 在命令之后是消息成员，这个数量不固定，可以任意组织，最大是0xFFFF。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/12/2015
 * @since laxcus 1.0
 */
//public final class Mark implements Classable, Serializable, Cloneable, Comparable<Mark> {
public final class Mark implements Serializable, Cloneable, Comparable<Mark> {

	private static final long serialVersionUID = -1030325325900474964L;

	/** FIXP标记，首字节。定位后续信息 **/
	private byte tag;

	/** FIXP协议版本号，固定2个字节 **/
	private short version;

	/** 请求/应答码。请求码分为主码和从码，应答码是短整型，都是2个字节 **/
	private short code;

	/** 消息成员数，固定2个字节 */
	private short messages;

	/**
	 * 重置参数为默认值
	 */
	private void reset() {
		tag = 0;
		version = MarkIdentity.VERSION;
		code = 0;
		messages = 0;
	}

	/**
	 * 构造私有默认的FIXP协议标头
	 */
	private Mark() {
		super();
		reset();
	}

	/**
	 * 根据传入的FIXP协议标头，生成一个它的副本
	 * @param that FIXP传入命令
	 */
	private Mark(Mark that) {
		super();
		tag = that.tag;
		version = that.version;
		code = that.code;
		messages = that.messages;
	}

	/**
	 * 从可类化数据读取器中解析FIXP协议标头参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Mark(ClassReader reader) throws FixpProtocolException {
		this();
		resolve(reader);
	}

	/**
	 * 构造FIXP的请求命令，并且设置请求码(主/从) 
	 * @param major 主码
	 * @param minor 从码
	 * @throws FixpProtocolException - 如果命令不正确弹出异常
	 */
	public Mark(byte major, byte minor) throws FixpParameterException {
		this();
		setAsk(major, minor);
	}

	/**
	 * 构造FIXP的应答命令，并且设置它的应答码
	 * @param reply 应答码
	 * @throws FixpProtocolException - 如果应答不正确会弹出异常
	 */
	public Mark(short reply) throws FixpParameterException {
		this();
		setAnswer(reply);
	}

	/**
	 * 从字节数组中解析FIXP协议标头
	 * @param b  字节数组
	 * @param off 解析开始下标
	 * @param len 解析的长度
	 */
	public Mark(byte[] b, int off, int len) throws FixpProtocolException {
		this();
		resolve(b, off, len);
	}

	/**
	 * 判断是请求命令
	 * @return 返回真或者假
	 */
	public boolean isAsk() {
		return (byte) ((tag >>> 6) & 0x3) == MarkIdentity.ASK;
	}

	/**
	 * 判断是应答命令
	 * @return 返回真或者假
	 */
	public boolean isAnswer() {
		return (byte) ((tag >>> 6) & 0x3) == MarkIdentity.ANSWER;
	}

	/**
	 * 不区分请求或者应答，输出原始码
	 * @return FIXP原始码
	 */
	public short getCode() {
		return code;
	}

//	/**
//	 * 设置请求命令
//	 * @param major 主码
//	 * @param minor 从码
//	 */
//	public void setAsk(byte major, byte minor) throws FixpProtocolException {
//		if (!Ask.isRequest(major, minor)) {
//			throw new FixpProtocolException("illegal command, %d,%d", major, minor);
//		}
//		tag = (byte) (MarkIdentity.ASK << 6 | tag & 0x3F);
//		code = major;
//		code <<= 8;
//		code |= minor;
//	}

	/**
	 * 设置请求命令
	 * @param major 主码
	 * @param minor 从码
	 */
	public void setAsk(byte major, byte minor) throws FixpParameterException {
		if (!Ask.isRequest(major, minor)) {
			throw new FixpParameterException("illegal command, %d,%d", major, minor);
		}
		tag = (byte) (MarkIdentity.ASK << 6 | tag & 0x3F);
		code = major;
		code <<= 8;
		code |= minor;
	}

	/**
	 * 返回两个字节的请求码
	 * @return 主码和从码
	 */
	public byte[] getAsk() {
		return new byte[] { getMajor(), getMinor() };
	}

	/**
	 * 返回请求主码
	 * @return 主码字节
	 */
	public byte getMajor() {
		return (byte) ((code >>> 8) & 0xFF);
	}

	/**
	 * 返回请求次码
	 * @return 次码字节
	 */
	public byte getMinor() {
		return (byte) (code & 0xFF);
	}

//	/**
//	 * 设置应答码
//	 * @param value 应答码
//	 */
//	public void setAnswer(short value) throws FixpProtocolException {
//		if (!Answer.isAnswer(value)) {
//			throw new FixpProtocolException("illegal reply : %d", value);
//		}
//		tag = (byte) (MarkIdentity.ANSWER << 6 | tag & 0x3F);
//		code = value;
//	}

	/**
	 * 设置应答码
	 * @param value 应答码
	 */
	public void setAnswer(short value){
		if (!Answer.isAnswer(value)) {
			throw new FixpParameterException("illegal reply : %d", value);
		}
		tag = (byte) (MarkIdentity.ANSWER << 6 | tag & 0x3F);
		code = value;
	}
	
	/**
	 * 返回应答码
	 * @return 应答码
	 */
	public short getAnswer() {
		return code;
	}

	/**
	 * 设置协议版本号
	 * @param ver 版本号
	 */
	public void setVersion(short ver) {
		version = ver;
	}

	/**
	 * 返回协议版本号
	 * @return  FIXP协议版本号
	 */
	public short getVersion() {
		return version;
	}

	/**
	 * 设置消息成员总数
	 * @param size short数值
	 */
	public void setMessages(short size) {
		messages = size;
	}

	/**
	 * 返回消息成员总数
	 * @return short数值
	 */
	public short getMessages() {
		return messages;
	}

	/**
	 * 打印指定范围的字节数组，按照要求的数字进制数显示
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 长度
	 * @param radix 数字显示基数，默认是10进制
	 * @return 字符串
	 */
	private String print(byte[] b, int off, int len, int radix) {
		StringBuilder buff = new StringBuilder();
		int end = off + len;
		for (int seek = off; seek < end; seek++) {
			if (seek > off) buff.append(',');
			if (radix == 16) {
				buff.append(String.format("%x", b[seek]));
			} else {
				buff.append(String.format("%d", b[seek]));
			}
		}
		return buff.toString();
	}

	/**
	 * 打印字节数组信息，并且指定它的显示格式
	 * @param b 字节数组
	 * @param radix 显示基数
	 * @return 字符串
	 */
	private String print(byte[] b, int radix) {
		return print(b, 0, b.length, radix);
	}

	/**
	 * 比较两个FIXP协议标头是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Mark.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Mark) that) == 0;
	}

	/**
	 * 返回当前FIXP协议标头的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return tag & 0xFF;
	}

	/**
	 * 返回FIXP协议标头的十进制数字描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isAsk()) {
			return String.format("0x%X 0x%X %d,%d %d", tag, version, getMajor(), getMinor(), messages);
		} else {
			return String.format("0x%X 0x%X %d %d", tag, version, code, messages);
		}
	}

	/**
	 * 生成当前实例的数据副本 
	 * @return FIXP协议标头实例
	 */
	public Mark duplicate() {
		return new Mark(this);
	}

	/**
	 * 根据当前的FIXP协议标头，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * FIXP协议标头比较
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Mark that) {
		// 空对象排在前面，有效对象排在后面
		if (that == null) {
			return 1;
		}

		// 请求/应答比较
		int ret = Laxkit.compareTo((tag >>> 6) & 0x3, (that.tag >>> 6) & 0x3);
		// 命令长度比较
		if (ret == 0) {
			ret = Laxkit.compareTo(tag & 0x3F, that.tag & 0x3F);
		}
		// 版本比较
		if (ret == 0) {
			ret = Laxkit.compareTo(version, that.version);
		}
		// 命令码比较
		if (ret == 0) {
			ret = Laxkit.compareTo(code, that.code);
		}
		// 清算长度比较
		if (ret == 0) {
			ret = Laxkit.compareTo(messages, that.messages);
		}
		return ret;
	}

	/**
	 * 将FIXP协议标头保存到可类化存储器
	 * @param writer 类写入器
	 * @return 返回解析字节长度
	 * @throws FixpProtocolException
	 */
	public int build(ClassWriter writer) throws FixpParameterException {
		// 检查
		if (isAsk()) {
			tag = (byte) ((MarkIdentity.ASK << 6) | (MarkIdentity.SIZE & 0x3F));
		} else if (isAnswer()) {
			tag = (byte) ((MarkIdentity.ANSWER << 6) | (MarkIdentity.SIZE & 0x3F));
		} else {
			throw new FixpParameterException("illegal command!");
		}

		tag = (byte) ((tag & 0xC0) | (MarkIdentity.SIZE & 0x3F));

		final int scale = writer.size();
		// 命令标识
		writer.write(tag);
		// FIXP协议版本号
		writer.writeShort(version);
		// 请求或者应答
		writer.writeShort(code);
		// 消息成员数
		writer.writeShort(messages);
		// 返回写入的字节数，共7个
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析FIXP协议标头
	 * @param reader 可类化读取器
	 * @return 返回解析长度
	 * @throws FixpProtocolException
	 */
	public int resolve(ClassReader reader) throws FixpProtocolException {
		// 小于指定长度是错误
		if (reader.getLeft() < MarkIdentity.SIZE) {
			throw new FixpProtocolException("size missing");
		}

		// 清除旧数据
		reset();

		final int scale = reader.getSeek();
		// 请求或者应答命令
		tag = reader.read();

		// 必须是请求或者应答中的任何一项，否则是错误
		if (!(isAsk() || isAnswer())) {
			byte[] b = reader.at(scale, MarkIdentity.SIZE);
			String s = print(b, 10);
			throw new FixpProtocolException("illegal fixp command! %s", s);
		}

		// 命令长度
		byte cmdsize = (byte) (tag & 0x3F);
		if (cmdsize != MarkIdentity.SIZE) {
			throw new FixpProtocolException("size error %d", cmdsize);
		}
		// 协议版本号
		version = reader.readShort();
		// 请求/应答码
		code = reader.readShort();

		// 请求或者应答
		if (isAsk()) { 
			if (!Ask.isRequest(getMajor(), getMinor())) {
				throw new FixpProtocolException("illegal request %d %d", getMajor(), getMinor());
			}
		} else if (isAnswer()) { 
			if (!Answer.isAnswer(code)) {
				throw new FixpProtocolException("illegal response %d", code);
			}
		}
		// 消息成员数
		messages = reader.readShort();
		// 返回解析的字节长度，7个字节
		return reader.getSeek() - scale;
	}
	
	/**
	 * 将FIXP协议标头参数转化为字节数组输出。
	 * @return 字节数组
	 */
	public byte[] build() throws FixpParameterException {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从字节数组里解析FIXP协议标头参数。
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 规定长度
	 * @return 解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) throws FixpProtocolException {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

}