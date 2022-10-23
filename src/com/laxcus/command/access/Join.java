/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.util.*;

import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.access.select.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL SELECT JOIN</code>操作。
 * 
 * @author scott.liang
 * @version 1.1 2/23/2013
 * @since laxcus 1.0
 */
public final class Join extends Manipulate {

	private static final long serialVersionUID = 3956735713323601806L;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
	
	}
	
	/** JOIN操作的类型 **/
	public final static byte INNER_JOIN = 1; 	// 内连接
	public final static byte LEFT_JOIN = 2;		// 左连接
	public final static byte RIGHT_JOIN = 3;	// 右连接
	public final static byte FULL_JOIN = 4;		// 全连接
	
	/** JOIN操作类型 **/
	private byte kind;
	
	/** JOIN 左表 **/
	private Space leftSpace;

	/** JOIN 右表 **/
	private Space rightSpace;
	
	/** 显示列集合(实际列和函数列) */
	private ListSheet sheet;
	
	/** JOIN检索索引 **/
	private OnIndex index;
	
	/** ORDER BY 语句 **/
	private OrderByAdapter orderby;
	
	/**
	 * 根据传入的JOIN实例，生成它的副本
	 * @param that
	 */
	private Join(Join that) {
		super(that);
		this.setKind(that.kind);
		this.setLeftSpace(that.leftSpace);
		this.setRightSpace(that.rightSpace);
		// 设置索引
		if (that.index != null) {
			this.setIndex((OnIndex) that.index.clone());
		}
		// 设置显示列表
		if (that.sheet != null) {
			this.setListSheet((ListSheet) that.sheet.clone());
		}
		// 设置ORDER BY实例
		if (that.orderby != null) {
			this.setOrderBy((OrderByAdapter) that.orderby.clone());
		}
	}

	/**
	 * 构造一个JOIN实例，并且指定它的JOIN检索类型
	 * @param kind - JOIN检索类型
	 */
	public Join(byte kind) {
		super(SQLTag.JOIN_METHOD); //CommandTag.JOIN_METHOD);
		this.setKind(kind);
	}
	
	/**
	 * @param reader
	 */
	public Join(ClassReader reader) {
		super(SQLTag.JOIN_METHOD); //CommandTag.JOIN_METHOD);
		this.resolve(reader);
	}

	/**
	 * 设置JOIN检索操作的类型
	 * @param b
	 */
	public void setKind(byte b) {
		switch(b) {
		case Join.INNER_JOIN:
		case Join.LEFT_JOIN:
		case Join.RIGHT_JOIN:
		case Join.FULL_JOIN:
			break;
		default:
			throw new IllegalArgumentException("illegal kind");
		}
		this.kind = b;
	}
	
	/**
	 * 返回JOIN检索操作的类型
	 * @return
	 */
	public byte getKind() {
		return this.kind;
	}
	
	/**
	 * 设置JOIN操作的左表
	 * @param s
	 */
	public void setLeftSpace(Space s) {
		if (s == null) {
			this.leftSpace = null;
		} else {
			this.leftSpace = (Space) s.clone();
		}
	}
	
	/**
	 * 返回JOIN操作的左表
	 * @return
	 */
	public Space getLeftSpace() {
		return this.leftSpace;
	}
	
	/**
	 * 设置JOIN操作的右表
	 * @param s
	 */
	public void setRightSpace(Space s) {
		if(s == null) {
			this.rightSpace = null;
		} else {
			this.rightSpace = (Space)s.clone();
		}
	}
	
	/**
	 * 返回JOIN操作的右表
	 * @return
	 */
	public Space getRightSpace() {
		return this.rightSpace;
	}
	
	/**
	 * 设置显示列集合
	 * @param s
	 */
	public void setListSheet(ListSheet s) {
		this.sheet = s;
	}
	
	/**
	 * 返回显示列集合
	 * @return
	 */
	public ListSheet getListSheet() {
		return this.sheet;
	}
	
	/**
	 * 设置JOIN检索索引
	 * @param s
	 */
	public void setIndex(OnIndex s) {
		if (s == null) {
			this.index = null;
		} else {
			this.index = (OnIndex) s.clone();
		}
	}

	/**
	 * 返回JOIN检索索引
	 * @return
	 */
	public OnIndex getIndex() {
		return this.index;
	}
	
	/**
	 * 设置"ORDER BY"实例
	 * @param s
	 */
	public void setOrderBy(OrderByAdapter s) {
		this.orderby = s;
	}

	/**
	 * 返回"ORDER BY"实例
	 * @return
	 */
	public OrderByAdapter getOrderBy() {
		return this.orderby;
	}
 
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Command duplicate() {
		return new Join(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		// TODO Auto-generated method stub
		return null;
	}
}