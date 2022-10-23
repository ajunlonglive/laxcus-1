/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.set.*;

/**
 * 授权人CALL站点查询结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/6/2018
 * @since laxcus 1.0
 */
public class TakeAuthorizerCallProduct extends EchoProduct {

	private static final long serialVersionUID = -1551832025535727572L;
	
	/** 授权人 **/
	private Siger authorizer;

	/** CALL站点 -> 表名关系集合 **/
	private Map<Node, SpaceSet> array = new TreeMap<Node, SpaceSet>();
	
	/**
	 * 从传入的授权人CALL站点查询结果实例，生成它的浅层数据副本
	 * @param that 授权人CALL站点查询结果实例
	 */
	private TakeAuthorizerCallProduct(TakeAuthorizerCallProduct that) {
		super(that);
		authorizer = that.authorizer;
		array.putAll(that.array);
	}
	
	/**
	 * 构造默认的授权人CALL站点查询结果
	 */
	public TakeAuthorizerCallProduct(Siger authorizer) {
		super();
		setAuthorizer(authorizer);
	}

	/**
	 * 从可类化读取器中解析授权人CALL站点查询结果
	 * @param reader 可类化读取器
	 */
	public TakeAuthorizerCallProduct(ClassReader reader) {
		super();
		resolve(reader);
	}
	
	/**
	 * 设置授权人签名
	 * @param e
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);
		authorizer = e;
	}
	
	/**
	 * 返回授权人签名
	 * @return
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}
	
	/**
	 * 返回全部地址
	 * @return Node列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(array.keySet());
	}

	/**
	 * 保存一个节点和数据表名
	 * @param node CALL站点地址
	 * @param space 数据表名
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean addSpace(Node node, Space space) {
		SpaceSet set = array.get(node);
		if (set == null) {
			set = new SpaceSet();
			array.put(node, set);
		}
		return set.add(space);
	}

	/**
	 * 保存一个节点和一组空间名称
	 * @param node CALL站点地址
	 * @param a 数据表名集合
	 * @return 返回保存的数目
	 */
	public int addSpaces(Node node, Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			addSpace(node, e);
		}
		return array.size() - size;
	}

	/**
	 * 返回数据表名集合
	 * @return 
	 */
	public Map<Node, SpaceSet> getSpaces() {
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeAuthorizerCallProduct duplicate() {
		return new TakeAuthorizerCallProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 授权人
		writer.writeObject(authorizer);
		// 数据表名集合
		int size = array.size();
		writer.writeInt(size);
		if (size > 0) {
			Iterator<Map.Entry<Node, SpaceSet>> iterator = array.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, SpaceSet> entry = iterator.next();
				writer.writeObject(entry.getKey());
				writer.writeObject(entry.getValue());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 授权人
		authorizer = new Siger(reader);
		// 解析数据表名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			SpaceSet set = new SpaceSet(reader);
			array.put(node, set);
		}
	}

}
