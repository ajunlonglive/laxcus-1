/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检查FRONT在线用户结果集合。<br>
 * 汇集不同GATE站点的检查FRONT在线用户结果。
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class FrontUserProduct extends EchoProduct {

	private static final long serialVersionUID = -3930112956745837897L;
	
	/** FRONT登录 **/
	private TreeSet<FrontDetail> array = new TreeSet<FrontDetail>();

	/**
	 * 构造FRONT登录的浅层副本
	 * @param that FrontUserProduct实例
	 */
	private FrontUserProduct(FrontUserProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造检查FRONT在线用户结果集合
	 */
	public FrontUserProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public FrontUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存检查FRONT在线用户结果
	 * @param e FrontDetail实例
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean add(FrontDetail e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批检查FRONT在线用户结果
	 * @param a FrontDetail数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<FrontDetail> a) {
		int size = array.size();
		for (FrontDetail e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批检查FRONT在线用户结果
	 * @param a 结果实例
	 * @return 返回新增成员数目
	 */
	public int addAll(FrontUserProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出检查FRONT在线用户结果
	 * @return FrontDetail列表
	 */
	public List<FrontDetail> list() {
		return new ArrayList<FrontDetail>(array);
	}

	/**
	 * 统计检查FRONT在线用户结果数目
	 * @return FrontDetail成员数目
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
	public FrontUserProduct duplicate() {
		return new FrontUserProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (FrontDetail e : array) {
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
			FrontDetail e = new FrontDetail(reader);
			array.add(e);
		}
	}

}