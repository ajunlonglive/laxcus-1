/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.access.schema.*;
import com.laxcus.site.*;

/**
 * “CREATE TABLE”命令处理结果。
 * 
 * @author scott.liang
 * @version 1.1 6/26/2015
 * @since laxcus 1.0
 */
public class CreateTableProduct extends DefaultTableProduct {

	private static final long serialVersionUID = -7308837668965408885L;

	/** 被部署的CALL站点集合（最少一个，可能多个）**/
	private TreeSet<GatewayNode> array = new TreeSet<GatewayNode>();

	/**
	 * 生成当前建表命令处理结果的数据副本
	 * @param that CreateTableProduct实例
	 */
	private CreateTableProduct(CreateTableProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构建默认的建表命令处理结果
	 */
	public CreateTableProduct() {
		super();
	}

	/**
	 * 构建建表命令处理结果，指定参数
	 * @param successful 成功或者否
	 */
	public CreateTableProduct(boolean successful) {
		super();
		setSuccessful(successful);
	}
	
	/**
	 * 构造建表命令处理结果，指定参数
	 * @param space 表名
	 */
	public CreateTableProduct(Space space) {
		super();
		setSpace(space);
	}
	
	/**
	 * 构造建表命令处理结果，指定参数
	 * @param space 表名
	 * @param successful 成功或者否
	 */
	public CreateTableProduct(Space space, boolean successful) {
		super();
		setSpace(space);
		setSuccessful(successful);
	}
	
	/**
	 * 从可类化数据读取器中解析建表命令处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CreateTableProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个节点
	 * @param e GatewayNode实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(GatewayNode e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 保存一批节点
	 * @param a 节点集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<GatewayNode> a) {
		if (a == null) {
			return 0;
		}
		// 逐个增加!
		int size = array.size();
		for (GatewayNode e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 输出全部节点
	 * @return 返回节点列表
	 */
	public List<GatewayNode> list() {
		return new ArrayList<GatewayNode>(array);
	}
	
	/**
	 * 保存一个结果
	 * @param e
	 */
	public void add(CreateTableProduct e) {
		super.add(e);
		addAll(e.array);
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 清除记录
	 */
	public void clear() {
		array.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateTableProduct duplicate() {
		return new CreateTableProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (GatewayNode node : array) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			GatewayNode node = new GatewayNode(reader);
			array.add(node);
		}
	}

}