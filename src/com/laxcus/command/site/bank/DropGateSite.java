/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 撤销GATE站点到ENTRANCE站点
 * 
 * @author scott.liang
 * @version 1.0 23/9/2018
 * @since laxcus 1.0
 */
public class DropGateSite extends Command {

	private static final long serialVersionUID = -3656850306190000338L;

	/** GATE站点编号 **/
	private int no;

	/** GATE站点内网地址 **/
	private Node inner;

	/** GATE站点公网地址 **/
	private Node outer;

	/**
	 * 构造默认和私有的撤销GATE站点到ENTRANCE站点命令
	 */
	private DropGateSite() {
		super();
	}

	/**
	 * 构造实例副本
	 * @param that
	 */
	private DropGateSite(DropGateSite that) {
		super(that);
		no = that.no;
		inner = that.inner;
		outer =that.outer;
	}

	/**
	 * 构造实例，指定参数
	 * @param inner 内网地址
	 * @param outer 外网地址
	 * @param no 编号
	 */
	public DropGateSite(Node inner, Node outer, int no) {
		this();
		setInner(inner);
		setOuter(outer);
		setNo(no);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public DropGateSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置编号
	 * @param who
	 */
	public void setNo(int who) {
		no = who;
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return no;
	}

	/**
	 * 设置GATE公网节点地址
	 * @param e Node实例
	 */
	public void setOuter(Node e) {
		Laxkit.nullabled(e);
		outer = e;
	}

	/**
	 * 返回GATE公网节点地址
	 * @return Node实例
	 */
	public Node getOuter() {
		return outer;
	}

	/**
	 * 设置GATE内网节点地址
	 * @param e Node实例
	 */
	public void setInner(Node e) {
		Laxkit.nullabled(e);
		inner = e;
	}

	/**
	 * 返回GATE内网节点地址
	 * @return Node实例
	 */
	public Node getInner() {
		return inner;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropGateSite duplicate() {
		return new DropGateSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(no);
		writer.writeObject(inner);
		writer.writeObject(outer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		no = reader.readInt();
		inner = new Node(reader);
		outer = new Node(reader);
	}

}


///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license GNU Lesser General Public License (LGPL)
// */
//package com.laxcus.command.site.bank;
//
//import com.laxcus.command.*;
//import com.laxcus.site.*;
//import com.laxcus.util.*;
//import com.laxcus.util.classable.*;
//
///**
// * 撤销GATE站点到ENTRANCE站点
// * 
// * @author scott.liang
// * @version 1.0 23/9/2018
// * @since laxcus 1.0
// */
//public class DropGateSite extends Command {
//	
//	private static final long serialVersionUID = -3224672264987002487L;
//
//	/** GATE站点编号 **/
//	private int no;
//	
//	/** GATE站点地址 **/
//	private Node node;
//
//	/**
//	 * 构造默认和私有的撤销GATE站点到ENTRANCE站点命令
//	 */
//	private DropGateSite() {
//		super();
//	}
//
//	/**
//	 * 构造实例副本
//	 * @param that
//	 */
//	private DropGateSite(DropGateSite that) {
//		super(that);
//		no = that.no;
//		node = that.node;
//	}
//	
//	/**
//	 * 构造实例，指定参数
//	 * @param no
//	 * @param node
//	 */
//	public DropGateSite(int no, Node node) {
//		this();
//		setNo(no);
//		setNode(node);
//	}
//
//	/**
//	 * 从可类化数据读取器中解析参数
//	 * @param reader 可类化数据读取器
//	 */
//	public DropGateSite(ClassReader reader) {
//		this();
//		resolve(reader);
//	}
//
//	/**
//	 * 设置编号
//	 * @param who
//	 */
//	public void setNo(int who) {
//		no = who;
//	}
//
//	/**
//	 * 返回编号
//	 * @return
//	 */
//	public int getNo() {
//		return no;
//	}
//	
//	/**
//	 * 设置节点地址
//	 * @param e
//	 */
//	public void setNode(Node e) {
//		Laxkit.nullabled(e);
//		node = e;
//	}
//
//	/**
//	 * 返回节点地址
//	 * @return
//	 */
//	public Node getNode() {
//		return node;
//	}
//	
//	/* (non-Javadoc)
//	 * @see com.laxcus.command.Command#duplicate()
//	 */
//	@Override
//	public DropGateSite duplicate() {
//		return new DropGateSite(this);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
//	 */
//	@Override
//	protected void buildSuffix(ClassWriter writer) {
//		writer.writeInt(no);
//		writer.writeObject(node);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
//	 */
//	@Override
//	protected void resolveSuffix(ClassReader reader) {
//		no = reader.readInt();
//		node = new Node(reader);
//	}
//
//}
