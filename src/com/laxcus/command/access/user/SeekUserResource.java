/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检索用户的分布资源。<br>
 * 
 * 这个命令由WATCH站点发出，提交到TOP/HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public abstract class SeekUserResource extends MultiUser {

	private static final long serialVersionUID = -7103907602936676617L;

	/** 被检索站点标记数组 **/
	private TreeSet<SeekSiteTag> array = new TreeSet<SeekSiteTag>();
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 被检索站点标记
		writer.writeInt(array.size());
		for (SeekSiteTag e : array) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.MultiUser#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 被检索站点标记
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SeekSiteTag e = new SeekSiteTag(reader);
			array.add(e);
		}
	}
	
	/**
	 * 构造默认的检索用户的分布资源
	 */
	protected SeekUserResource() {
		super();
	}

	/**
	 * 生成检索用户的分布资源的数据副本
	 * @param that 检索用户的分布资源
	 */
	protected SeekUserResource(SeekUserResource that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 设置查被检索站点标记
	 * @param e PrintSiteTag实例
	 */
	public boolean addTag(SeekSiteTag e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 返回查被检索站点标记
	 * @return PrintSiteTag实例
	 */
	public List<SeekSiteTag> getTags() {
		return new ArrayList<SeekSiteTag>(array);
	}

	/**
	 * 判断要求显示全部用户
	 * @return 返回真或者假
	 */
	public boolean isAllUser() {
		return getUsers().size() == 0;
	}

}