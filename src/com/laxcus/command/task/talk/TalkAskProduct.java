/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task.talk;

import com.laxcus.echo.product.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.classable.*;

/**
 * 分布任务组件对话应答结果
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class TalkAskProduct extends EchoProduct {

	private static final long serialVersionUID = -8594507855811715886L;

	/** 对话应用结果结果 **/
	private TalkReply reply;

	/**
	 * 构造默认和私有的分布任务组件对话应答结果
	 */
	private TalkAskProduct() {
		super();
	}

	/**
	 * 构造分布任务组件对话应答结果，指定对话应用结果
	 * @param reply
	 */
	public TalkAskProduct(TalkReply reply) {
		this();
		setReply(reply);
	}

	/**
	 * 从可类化读取器中解析分布任务组件对话应答结果
	 * @param reader 可类化读取器
	 */
	public TalkAskProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成分布任务组件对话应答结果的数据副本
	 * @param that 分布任务组件对话应答结果
	 */
	private TalkAskProduct(TalkAskProduct that) {
		super(that);
		reply = that.reply;
	}

	/**
	 * 设置对话应用结果，允许空指针
	 * @param e 对话应用结果
	 */
	public void setReply(TalkReply e) {
		reply = e;
	}

	/**
	 * 返回对话应用结果
	 * @return 对话应用结果
	 */
	public TalkReply getReply(){
		return reply;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TalkAskProduct duplicate() {
		return new TalkAskProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeDefault(reply);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		reply = (TalkReply) reader.readDefault();
	}

}