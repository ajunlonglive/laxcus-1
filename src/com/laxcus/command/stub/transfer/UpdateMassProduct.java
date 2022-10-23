/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.access.stub.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 更新数据块命令处理报告
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public class UpdateMassProduct extends EchoProduct {
	
	private static final long serialVersionUID = 2301517229675785552L;

	/** 数据块标识 **/
	private StubFlag flag;
	
	/** 源头是缓存映像数据块 **/
	private boolean cacheReflex;

	/** 成功标记 **/
	private boolean success;

	/**
	 * 构造默认的更新数据块命令处理报告
	 */
	private UpdateMassProduct() {
		super();
		cacheReflex = false;
		success = false;
	}

	/**
	 * 使用传入实例，生成更新数据块命令处理报告数据副本
	 * @param that UpdateMassProduct实例
	 */
	private UpdateMassProduct(UpdateMassProduct that) {
		super(that);
		flag = that.flag;
		cacheReflex = that.cacheReflex;
		success = that.success;
	}
	
	/**
	 * 构造更新数据块命令处理报告，指定全部参数
	 * @param flag 数据块标识
	 * @param success 成功标志
	 */
	public UpdateMassProduct(StubFlag flag, boolean success) {
		this();
		this.setFlag(flag);
		this.setSuccessful(success);
	}
	
	/**
	 * 从可类化数据读取器中解析更新数据块命令处理报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public UpdateMassProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 设置数据块标识
	 * @param e StubFlag实例
	 */
	public void setFlag(StubFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}

	/**
	 * 返回数据块标识
	 * @return StubFlag实例
	 */
	public StubFlag getFlag() {
		return flag;
	}

	/**
	 * 判断源头是缓存映像数据块
	 * @return 返回真或者假
	 */
	public boolean isCacheReflex() {
		return cacheReflex;
	}

	/**
	 * 设置源头数据是缓存映像数据块，或者否
	 * @param b 属性标识
	 */
	public void setCacheReflex(boolean b) {
		cacheReflex = b;
	}

	/**
	 * 设置成功标记
	 * @param b 成功标记
	 */
	public void setSuccessful(boolean b) {
		success = b;
	}

	/**
	 * 判断更新成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public UpdateMassProduct duplicate() {
		return new UpdateMassProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(flag);
		writer.writeBoolean(cacheReflex);
		writer.writeBoolean(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		flag = new StubFlag(reader);
		cacheReflex = reader.readBoolean();
		success = reader.readBoolean();
	}

}