/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 删除密钥令牌
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class DropSecureToken extends ExecuteSecureToken {
	
	private static final long serialVersionUID = 6394925262827828213L;

	/** 单元命名 **/
	private ArrayList<Naming> array = new ArrayList<Naming>();

	/**
	 * 构造默认的删除密钥令牌命令
	 */
	public DropSecureToken() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析删除密钥令牌命令
	 * @param reader 可类化数据读取器
	 */
	public DropSecureToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成删除密钥令牌命令的数据副本
	 * @param that DropSecureToken实例
	 */
	private DropSecureToken(DropSecureToken that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 保存密钥令牌名称
	 * @param e
	 * @return 成功返回真，否则假 
	 */
	public boolean addName(Naming e) {
		Laxkit.nullabled(e);
		if (array.contains(e)) {
			return false;
		}
		return array.add(e);
	}
	
	/**
	 * 保存密钥令牌名称
	 * @param a
	 * @return
	 */
	public int addNames(Collection<Naming> a) {
		int size = array.size();
		for (Naming e : a) {
			addName(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 输出密钥令牌名称
	 * @return 命名列表
	 */
	public List<Naming> getNames() {
		return new ArrayList<Naming>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropSecureToken duplicate() {
		return new DropSecureToken(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(array.size());
		for(Naming e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		int size = reader.readInt();
		for(int i=0; i < size; i++) {
			Naming e = new Naming(reader);
			array.add(e);
		}
	}

}