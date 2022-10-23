/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找元数据命令。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public abstract class FindField extends ProcessField {
	
	private static final long serialVersionUID = 775710156209173104L;

	/** 用户签名集合 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();
	
	/**
	 * 构造默认的查找域数据命令
	 */
	protected FindField() {
		super();
	}

	/**
	 * 根据传入的查找域数据命令实例，生成它的数据副本
	 * @param that FindField实例
	 */
	protected FindField(FindField that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 增加用户签名，不允许空指针
	 * @param e Siger实例
	 * @return 返回真或者假
	 */
	public boolean addUser(Siger e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批用户签名
	 * @param a 用户签名数组
	 * @return 返回新增数目
	 */
	public int addUsers(Collection<Siger> a) {
		int size = array.size();
		if (a != null) {
			array.addAll(a);
		}
		return array.size() - size;
	}
	
	/**
	 * 返回用户签名集合
	 * @return 用户签名列表
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(array);
	}
	
	/**
	 * 统计用户签名数目
	 * @return 用户签名数目
	 */
	public int getUserCount() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for (Siger e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		array.clear();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			array.add(e);
		}
	}


}