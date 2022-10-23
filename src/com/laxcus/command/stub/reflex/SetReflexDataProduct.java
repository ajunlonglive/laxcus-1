/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 设置映像数据报告。<br>
 * 这个回显结果是一个DATA主站点向DATA从站点设置映像数据后的回应。
 * 
 * @author scott.liang
 * @version 1.1 7/10/2015
 * @since laxcus 1.0
 */
public final class SetReflexDataProduct extends EchoProduct {
	
	private static final long serialVersionUID = -7433489014489670132L;
	
	/** 结果状态  **/
	private int state;

	/**
	 * 构造默认和私有的设置映像数据报告
	 */
	private SetReflexDataProduct() {
		super();
		state = -1;
	}

	/**
	 * 根据传入的设置映像数据报告，产生它的浅层数据副本
	 * @param that SetReflexDataProduct实例
	 */
	private SetReflexDataProduct(SetReflexDataProduct that) {
		super(that);
		state = that.state;
	}

	/**
	 * 构造设置映像数据报告，设置处理状态
	 * @param state 处理状态
	 */
	public SetReflexDataProduct(int state) {
		this();
		this.setState(state);
	}

	/**
	 * 从可类化数据读取器中解析设置映像数据报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SetReflexDataProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置处理状态
	 * @param id 处理状态
	 */
	public void setState(int id) {
		state = id;
	}

	/**
	 * 返回处理状态
	 * @return 处理状态
	 */
	public int getState() {
		return state;
	}
	
	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return state >= 0;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SetReflexDataProduct duplicate() {
		return new SetReflexDataProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(state);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		state = reader.readInt();
	}

}