/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.product;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 异步应答报告。<br><br>
 * 
 * 异步应答报告是由服务端节点根据异步命令（Command）计算后产生，投递给客户端节点。异步应答报告中携带客户端需要的数据。
 * 
 * 在包的定义中，所有带“xxxProduct”后缀的异步应答报告都是从“EchoProduct”派生，是“EchoProduct”的子类。
 * 
 * @author scott.liang
 * @version 1.2 10/9/2019
 * @since laxcus 1.0
 */
public abstract class EchoProduct implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = 6890179726708338505L;

	/** 版本号，发送端设置，接收端判断。通过版本号，接收端决定接收发送端发送的哪些数据内容 **/
	private int version;

	/** 异步应答发起人签名。 **/
	private Siger issuer;

	/** 异步应答回显地址。说明应答来源，接收方根据这个地址向来源再反馈结果，允许空指针。**/
	private Cabin source;

	/** 命令原语，源自Command，允许空值 **/
	private String primitive;

	/** 生成时间 **/
	private long launchTime;

	/** 异步处理消耗的时间，对应SiteInvoker.processTime **/
	private long processTime;
	
//	/** 新的处理，包括CPU数，GPU核、使用的内存统计 **/
//	private int cpus;
//	
//	private int gpus;
//	
//	private long memory;

	/**
	 * 构造默认的异步应答报告
	 */
	protected EchoProduct() {
		super();
		version = 0;
		launchTime = System.currentTimeMillis();
		processTime = 0;
		source = null;
	}

	/**
	 * 根据传入的实例，生成新的异步应答报告
	 * @param that EchoProduct实例
	 */
	protected EchoProduct(EchoProduct that) {
		this();
		version = that.version;
		issuer = that.issuer;
		source = that.source;
		primitive = that.primitive;
		launchTime = that.launchTime;
		processTime = that.processTime;
	}


	/**
	 * 设置版本号，短整型。子类的resolveSuffix方法，根据版本号判断要解析的内容
	 * @param v 版本号
	 */
	public void setVersion(int v) {
		version = v;
	}

	/**
	 * 返回版本号。
	 * @return 版本号
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * 返回生成时间
	 * @return 毫秒单位
	 */
	public long getLaunchTime() {
		return launchTime;
	}

	/**
	 * 判断工作超时
	 * @param timeout 超时时间，单位：毫秒
	 * @return 返回真或者假
	 */
	public boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - launchTime >= timeout;
	}

	/**
	 * 设置命令原语，允许空值
	 * @param e 命令原语的字符串
	 */
	public void setPrimitive(String e) {
		primitive = e;
	}

	/**
	 * 返回命令原语
	 * @return 命令原语的字符串
	 */
	public String getPrimitive() {
		return primitive;
	}

	/**
	 * 设置异步处理时间
	 * @param ms 以毫秒为单位的处理时间
	 */
	public void setProcessTime(long ms) {
		processTime = ms;
	}

	/**
	 * 增加异步处理时间
	 * @param ms 以毫秒为单位的处理时间
	 */
	public void addProcessTime(long ms) {
		if (ms > 0) {
			processTime += ms;
		}
	}

	/**
	 * 返回异步处理时间
	 * @return 异步处理时间
	 */
	public long getProcessTime() {
		return processTime;
	}

	/**
	 * 设置异步应答来源地址，允许空指针。<br>
	 * @param e 异步应答来源地址
	 */
	public void setSource(Cabin e) {
		source = e;
	}

	/**
	 * 返回异步应答来源地址
	 * @return 异步应答来源地址实例
	 */
	public final Cabin getSource() {
		return source;
	}

	/**
	 * 设置异步应答发起人签名，允许空指针<br>
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		issuer = e;
	}

	/**
	 * 返回异步应答发起人签名
	 * @return Siger实例
	 */
	public final Siger getIssuer() {
		return issuer;
	}

	/**
	 * 输出数据
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	//	/**
	//	 * 将异步应答报告写入可类化存储器
	//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	//	 * @since 1.1
	//	 */
	//	@Override
	//	public int build(ClassWriter writer) {
	//		int size = writer.size();
	//		// 版本号
	//		writer.writeShort(version);
	//		// 发起人签名
	//		writer.writeInstance(issuer);
	//		// 异步应答来源地址
	//		writer.writeInstance(source);
	//		// 命令原语
	//		writer.writeString(primitive);
	//		// 异步处理时间
	//		writer.writeLong(processTime);
	//
	//		// 子类信息写入可类化存储器
	//		buildSuffix(writer);
	//		// 返回写入的数据长度
	//		return writer.size() - size;
	//	}
	//
	//	/**
	//	 * 从可类化读取器中解析异步应答报告
	//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	//	 * @since 1.1
	//	 */
	//	@Override
	//	public int resolve(ClassReader reader) {
	//		int seek = reader.getSeek();
	//		// 版本号
	//		version = reader.readShort();
	//		// 发起人签名
	//		issuer = reader.readInstance(Siger.class);
	//		// 异步应答来源地址
	//		source = reader.readInstance(Cabin.class);
	//		// 命令原语
	//		primitive = reader.readString();
	//		// 异步处理时间
	//		processTime = reader.readLong();
	//
	//		// 解析子类信息
	//		resolveSuffix(reader);
	//		// 返回解析的字节长度
	//		return reader.getSeek() - seek;
	//	}

	/**
	 * 将异步应答报告写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		ClassWriter writer = new ClassWriter();
		// 版本号
		writer.writeInt(version);
		// 发起人签名
		writer.writeInstance(issuer);
		// 异步应答来源地址
		writer.writeInstance(source);
		// 命令原语
		writer.writeString(primitive);
		// 异步处理时间
		writer.writeLong(processTime);
		// 子类信息写入可类化存储器
		buildSuffix(writer);

		// 写入EchoProduct内容
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化读取器中解析异步应答报告
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();
		
		// 读EchoProduct内容
		int len = r.readInt();
		byte[] b = r.read(len);
		
		ClassReader reader = new ClassReader(b);
		// 版本号
		version = reader.readInt();
		// 发起人签名
		issuer = reader.readInstance(Siger.class);
		// 异步应答来源地址
		source = reader.readInstance(Cabin.class);
		// 命令原语
		primitive = reader.readString();
		// 异步处理时间
		processTime = reader.readLong();
		// 解析子类信息
		resolveSuffix(reader);
		
		// 返回解析的字节长度
		return r.getSeek() - seek;
	}

	/**
	 * 克隆当前对象的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 子类实例产生一个自己的命令副本
	 * @return EchoProduct实例副本
	 */
	public abstract EchoProduct duplicate();

	/**
	 * 将异步应答报告写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析异步应答报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}
