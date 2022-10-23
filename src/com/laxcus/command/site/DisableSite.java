/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 失效站点命令。<br>
 * 在注册站点失联的状态下，TOP/HOME/BANK管理站点删除注册站点，属于故障状态。与“DropSite”的正常退出有根本区别。
 * 
 * @author scott.liang
 * @version 1.0 12/11/2019
 * @since laxcus 1.0
 */
public abstract class DisableSite extends CastSite {

	private static final long serialVersionUID = 556588931891481515L;

	/** 用户数据表 **/
	private TreeSet<DisableMember> array = new TreeSet<DisableMember>();
	
	/**
	 * 构造失效站点命令
	 */
	protected DisableSite() {
		super();
	}

	/**
	 * 根据传入的失效站点命令，生成它的数据副本
	 * @param that DisableSite实例
	 */
	protected DisableSite(DisableSite that) {
		super(that);
	}

	/**
	 * 保存失效成员
	 * @param e 成员对象
	 */
	public void add(DisableMember e) {
		Laxkit.nullabled(e);
		array.add(e);
	}
	
	/**
	 * 保存一批失效成员
	 * @param a 失效成员数组
	 */
	public void addAll(Collection<DisableMember> a) {
		array.addAll(a);
	}

	/**
	 * 输出全部失效成员
	 * @return DisableMember成员
	 */
	public List<DisableMember> list() {
		return new ArrayList<DisableMember>(array);
	}

	/**
	 * 构造失效站点命令，指定站点地址
	 * @param site Node实例
	 */
	protected DisableSite(Node site) {
		super(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 失效成员
		writer.writeInt(array.size());
		for (DisableMember e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 失效成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			DisableMember e = new DisableMember(reader);
			array.add(e);
		}
	}

}