/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.gate;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * GATE站点，注册到BANK站点。<br>
 * 
 * 参数包括站点编号和全部在线用户签名。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class GateSite extends GatewaySite {

	private static final long serialVersionUID = 3814742936691423326L;

	/** 无效的编号 **/
	public final static int INVALID_NO = -1;

	/** 编号，从0开始，默认是-1。 **/
	private int no;

	/** 在线用户数目 **/
	private int members;

	/**
	 * 根据传入的AIT站点实例，生成它的数据副本
	 * @param that GateSite实例
	 */
	private GateSite(GateSite that) {
		super(that);
		no = that.no;
		members = that.members;
	}

	/**
	 * 构造一个默认的GATE站点地址
	 */
	public GateSite() {
		super(SiteTag.GATE_SITE);
		setNo(GateSite.INVALID_NO); // 默认是-1
	}

	/**
	 * 从可类化读取器中解析GATE站点地址
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public GateSite(ClassReader reader) {
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
	 * 判断是有效的编号
	 * @return 返回真或者假
	 */
	public boolean isValidNo() {
		return no > GateSite.INVALID_NO;
	}

	/**
	 * 设置用户签名数目
	 * @param i
	 */
	public void setMembers(int i) {
		members = i;
	}

	/**
	 * 返回用户签名数目
	 * @return 用户签名数目
	 */
	public int getMembers() {
		return members;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public GateSite duplicate() {
		return new GateSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 设置编号
		writer.writeInt(no);
		// 在线用户数目
		writer.writeInt(members);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 取编号
		no = reader.readInt();
		// 在线用户数目
		members = reader.readInt();
	}

}