/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检索站点在线命令处理报告。
 * 
 * @author scott.liang
 * @version 1.0 4/16/2018
 * @since laxcus 1.0
 */
public class SeekOnlineCommandProduct extends EchoProduct {

	private static final long serialVersionUID = 8864216247252498843L;

	/** 目标站点 **/
	private Node node;
	
	/** 处理结果单元数组 **/
	private ArrayList<SeekOnlineCommandItem> array = new ArrayList<SeekOnlineCommandItem>();

	/**
	 * 从传入的检索站点在线命令报告，生成它的数据副本
	 * @param that SeekOnlineCommandProduct实例
	 */
	private SeekOnlineCommandProduct(SeekOnlineCommandProduct that) {
		super(that);
		node = that.node;
		array.addAll(that.array);
	}

	/**
	 * 构造检索站点在线命令报告
	 */
	public SeekOnlineCommandProduct() {
		super();
	}
	
	/**
	 * 构造检索站点在线命令报告，指定在线命令单元
	 * @param item SeekOnlineCommandItem实例
	 */
	public SeekOnlineCommandProduct(SeekOnlineCommandItem item) {
		this();
		add(item);
	}
	
	/**
	 * 从可类化数据读取器中解析检索站点在线命令报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SeekOnlineCommandProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置目标节点
	 * @param e 目标节点
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		node = e;
	}

	/**
	 * 返回目标节点（命令被发送的节点）
	 * @return 目标节点
	 */
	public Node getSite() {
		return node;
	}
	
	/**
	 * 增加一个在线命令单元，不允许空指针
	 * @param e SeekOnlineCommandItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SeekOnlineCommandItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 保存一批在线命令单元数组
	 * @param a SeekOnlineCommandItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<SeekOnlineCommandItem> a) {
		int size = array.size();
		for (SeekOnlineCommandItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存另一组检索站点在线命令报告
	 * @param e SeekOnlineCommandProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SeekOnlineCommandProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部在线命令单元
	 * @return SeekOnlineCommandItem列表
	 */
	public List<SeekOnlineCommandItem> list() {
		return new ArrayList<SeekOnlineCommandItem>(array);
	}
	
	/**
	 * 统计在线命令单元数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SeekOnlineCommandProduct duplicate() {
		return new SeekOnlineCommandProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
		writer.writeInt(array.size());
		for(SeekOnlineCommandItem item: array) {
			writer.writeObject(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SeekOnlineCommandItem item = new SeekOnlineCommandItem(reader);
			array.add(item);
		}
	}

}
