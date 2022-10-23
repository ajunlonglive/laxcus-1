/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.call;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;

/**
 * CALL站点成员<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class CallMember extends TableMember {

	private static final long serialVersionUID = -2703364673507908645L;

	/** 云端空间 **/
	private CloudField field;
	
	/**
	 * 根据传入CALL站点成员参数，生成它的数据副本
	 * @param that CallMember实例
	 */
	private CallMember(CallMember that) {
		super(that);
		field = that.field;
	}

	/**
	 * 构造默认的CALL站点成员
	 */
	private CallMember() {
		super();
	}

	/**
	 * 构造CALL站点成员，指定数据持有人
	 * @param siger 持有人
	 */
	public CallMember(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CallMember(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置云端空间
	 * @param e
	 */
	public void setCloudField(CloudField e) {
		field = e;
	}

	/**
	 * 返回云端空间
	 * @return
	 */
	public CloudField getCloudField() {
		return field;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#duplicate()
	 */
	@Override
	public CallMember duplicate() {
		return new CallMember(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.PhaseMember#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 云端空间
		writer.writeInstance(field);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.PhaseMember#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 云端空间
		field = reader.readInstance(CloudField.class);
	}

}