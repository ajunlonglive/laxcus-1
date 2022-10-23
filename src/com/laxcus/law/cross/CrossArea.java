/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.cross;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 共享资源区。<br>
 * 
 * 分为授权集合和被授权集合两个部分。
 * 
 * @author scott.liang
 * @version 1.0 8/12/2017
 * @since laxcus 1.0
 */
public class CrossArea implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = 7811558046859513207L;

	/** 授权集合 **/
	private Set<CrossField> actives = new TreeSet<CrossField>();
	
	/** 被授权集合 **/
	private Set<CrossField> passives = new TreeSet<CrossField>();
	
	/**
	 * 构造默认的共享资源区
	 */
	public CrossArea() {
		super();
	}

	/**
	 * 保存授权单元
	 * @param e CrossField实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addActiveField(CrossField e) {
		Laxkit.nullabled(e);
		
		return actives.add(e);
	}
	
	/**
	 * 返回全部授权单元
	 * @return CrossField列表
	 */
	public List<CrossField> getActiveFields() {
		return new ArrayList<CrossField>(actives);
	}
	
	/**
	 * 返回授权单元数目
	 * @return 授权单元数目的整型值
	 */
	public int getActiveSize() {
		return actives.size();
	}
	
	/**
	 * 保存被授权单元
	 * @param e CrossField实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addPassiveField(CrossField e) {
		Laxkit.nullabled(e);
		
		return passives.add(e);
	}
	
	/**
	 * 返回全部被授权单元
	 * @return CrossField列表
	 */
	public List<CrossField> getPassiveFields() {
		return new ArrayList<CrossField>(passives);
	}
	
	/**
	 * 返回被授权单元数目
	 * @return 被授权单元数目的整型值
	 */
	public int getPassiveSize() {
		return passives.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		// TODO Auto-generated method stub
		return 0;
	}

}