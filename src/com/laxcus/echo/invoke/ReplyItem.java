/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;

/**
 * 异步应答单元。<br>
 * 
 * 由回显地址和被传输对象两部分组成，是向指定的回显地址发送应答对象。
 * 
 * @author scott.liang
 * @version 1.1 12/09/2015
 * @since laxcus 1.0
 */
public final class ReplyItem implements Serializable {

	private static final long serialVersionUID = -5187731058000275536L;

	/** 命令来源地址。即异步调用器监听地址 **/
	private Cabin source;

	/** 被传输的对象 **/
	private Object object;

	/**
	 * 构造默认和私有的异步应答单元
	 */
	private ReplyItem() {
		super();
	}

	/**
	 * 根据实例，生成异步应答单元的数据副本
	 * @param that ReplyItem实例
	 */
	private ReplyItem(ReplyItem that) {
		this();
		source = that.source;
		object = that.object;
	}

	/**
	 * 构造异步应答单元，指定参数
	 * @param source 异步调用器监听地址
	 * @param object 被传输的对象
	 */
	public ReplyItem(Cabin source, Object object) {
		this();
		setSource(source);
		setObject(object);
	}

	/**
	 * 设置命令来源地址，即异步调用器监听地址，这个调用器处于监听状态。
	 * @param e 异步调用器监听地址
	 */
	public void setSource(Cabin e) {
		Laxkit.nullabled(e);

		source = e;
	}

	/**
	 * 返回异步调用器监听地址
	 * @return 异步调用器监听地址
	 */
	public Cabin getSource() {
		return source;
	}

	/**
	 * 判断传输的对象是原始字节数组
	 * @return 返回真或者假
	 */
	public boolean isPrimitive() {
		return object.getClass() == byte[].class;
	}

	/**
	 * 判断传输的对象是文件
	 * @return 返回真或者假
	 */
	public boolean isFile() {
		return Laxkit.isClassFrom(object, java.io.File.class);
	}

	/**
	 * 判断传输的对象是LAXCUS命令
	 * @return 返回真或者假
	 */
	public boolean isCommand() {
		return Laxkit.isClassFrom(object, Command.class);
	}
	
	/**
	 * 判断传输的对象是异步应答结果
	 * @return 返回真或者假
	 */
	public boolean isEchoProduct() {
		return Laxkit.isClassFrom(object, EchoProduct.class);
	}

	/**
	 * 设置原始传输对象，是任意Object子类
	 * @param e 被传输的对象
	 */
	public void setObject( Object e) {
		// 不允许空值
		Laxkit.nullabled(e);

		// 赋值
		object = e;
	}

	/**
	 * 返回被传输的对象，是任意Object子类
	 * @return Object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * 返回当前异步应答单元实例的浅层副本
	 * @return 当前ReplyItem的数据副本
	 */
	public ReplyItem duplicate() {
		return new ReplyItem(this);
	}

	/**
	 * 返回异步应答单元的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", source, object.getClass().getName());
	}

}