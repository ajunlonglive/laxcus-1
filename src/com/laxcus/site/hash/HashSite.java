/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.hash;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * HASH站点。站点信息注册到BANK站点
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class HashSite extends Site {

	private static final long serialVersionUID = 3814742936691423326L;
	
	/** 无效的编号 **/
	public final static int INVALID_NO = -1;

	/** 编号，从0开始，默认是-1。 **/
	private int no;
	
	/** 保存的用户签名数目 **/
	private int members;

	/**
	 * 根据传入的HASH站点实例，生成它的数据副本
	 * @param that HASH站点实例
	 */
	private HashSite(HashSite that) {
		super(that);
		no = that.no;
		members = that.members;
	}

	/**
	 * 构造一个默认的HASH站点地址
	 */
	public HashSite() {
		super(SiteTag.HASH_SITE);
		setNo(HashSite.INVALID_NO); // 默认是-1
	}

	/**
	 * 从可类化读取器中解析HASH站点地址
	 * @param reader 可类化数据读取器
	 */
	public HashSite(ClassReader reader) {
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
		return no > HashSite.INVALID_NO;
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
	public HashSite duplicate() {
		return new HashSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(no);
		writer.writeInt(members);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		no = reader.readInt();
		members = reader.readInt();
	}

}