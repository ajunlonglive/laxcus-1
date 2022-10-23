/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * BANK编号子站单元
 * 
 * @author scott.liang
 * @version 1.0 6/28/2018
 * @since laxcus 1.0
 */
public class BankSerialSiteItem extends BankSubSiteItem {

	private static final long serialVersionUID = -732575606921708511L;

	/** 编号，从0开始，默认是-1。 **/
	private int no;
	
	/** 公网地址，只针对GATE站点，其它是空值 **/
	private Node outer;
	
	/**
	 * 构造默认的BANK编号子站单元
	 */
	protected BankSerialSiteItem() {
		super();
		setNo(-1);
	}
	
	/**
	 * 构造BANK编号子站单元，指定参数。针对HASH站点。
	 * @param site 站点地址
	 * @param no 编号
	 */
	public BankSerialSiteItem(Node site, int no) {
		super(site);
		setNo(no);
	}
	
	/**
	 * 构造BANK编号子站单元，指定参数。这个函数针对GATE站点。
	 * @param inner 内网地址
	 * @param outer 公网地址
	 * @param no 编号
	 */
	public BankSerialSiteItem(Node inner, Node outer, int no) {
		super(inner);
		setOuter(outer);
		setNo(no);
	}

	/**
	 * 从可类化数据读取器中解析BANK编号子站单元
	 * @param reader 可类化数据读取器
	 */
	public BankSerialSiteItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成BANK编号子站单元的数据副本
	 * @param that BANK编号子站单元实例
	 */
	private BankSerialSiteItem(BankSerialSiteItem that) {
		super(that);
		outer = that.outer;
		no = that.no;
	}
	
	/**
	 * 设置公网地址
	 * @param e Node实例
	 */
	public void setOuter(Node e) {
		outer = e;
	}
	
	/**
	 * 返回公网地址
	 * @return Node实例
	 */
	public Node getOuter(){
		return outer;
	}
	
	/**
	 * 返回内网地址
	 * @return Node实例
	 */
	public Node getInner() {
		return getSite();
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
	 * 生成BANK编号子站单元的数据副本
	 * @return BANK编号子站单元实例
	 */
	public BankSerialSiteItem duplicate() {
		return new BankSerialSiteItem(this);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		super.build(writer);
		writer.writeInstance(outer);
		writer.writeInt(no);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		super.resolve(reader);
		outer = reader.readInstance(Node.class);
		no = reader.readInt();
		return reader.getSeek() - seek;
	}

}