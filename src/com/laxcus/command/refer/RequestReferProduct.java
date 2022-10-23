/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 请求分配资源引用应答报告。<br>
 * 
 * CALL/WORK/BUILD发出命令，HOME产生这个实例，分派给它们。
 * 
 * @author scott.liang
 * @version 1.1 06/02/2015
 * @since laxcus 1.0
 */
public final class RequestReferProduct extends EchoProduct {

	private static final long serialVersionUID = 3901254629421750350L;

	/** 请求分配资源引用集合 **/
	private TreeSet<Refer> array = new TreeSet<Refer>();

	/**
	 * 根据传入的请求分配资源引用应答报告实例，生成它的数据副本
	 * @param that 请求分配资源引用实例
	 */
	private RequestReferProduct(RequestReferProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造默认的请求分配资源引用应答报告
	 */
	public RequestReferProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析请求分配资源引用应答报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RequestReferProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加一个用户资源引用，不允许空指针
	 * @param e 用户资源引用
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean add(Refer e) {
		Laxkit.nullabled(e);
		
		return array.add(e);
	}

	/**
	 * 输出全部用户资源引用
	 * @return 用户资源引用列表
	 */
	public List<Refer> list() {
		return new ArrayList<Refer>(array);
	}

	/**
	 * 统计用户资源引用数目
	 * @return 返回用户资源引用数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RequestReferProduct duplicate() {
		return new RequestReferProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for(Refer e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Refer e = new Refer(reader);
			array.add(e);
		}
	}

}
