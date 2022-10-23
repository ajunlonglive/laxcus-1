/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 申请获得用户签名。<br>
 * 这个命令由GATE发出，要求TOP站点分配已经指定的账号，和GATE未指定TOP随机分配的账号。
 * 
 * @author scott.liang
 * @version 1.1 11/09/2015
 * @since laxcus 1.0
 */
public class TakeSiger extends Command {

	private static final long serialVersionUID = 7404772453907134937L;

	/** 在本地已经配置的账号，这些账号是必须的 **/
	private Set<Siger> users = new TreeSet<Siger>();

	/** 允许的最大账号数目 **/
	private int maxsize;

	/**
	 * 根据传入的申请获得用户签名命令，生成它的数据副本
	 * @param that
	 */
	private TakeSiger(TakeSiger that) {
		super(that);
		users.addAll(that.users);
		maxsize = that.maxsize;
	}

	/**
	 * 建立申请获得用户签名命令
	 */
	public TakeSiger() {
		super();
		maxsize = 1; // 最少1个
	}

	/**
	 * 建立申请获得用户签名命令，指定一批帐号。
	 * @param a Siger数组
	 */
	public TakeSiger(Collection<Siger> a) {
		this();
		addAll(a);
	}
	
	/**
	 * 从可类化数据读取器中解析申请获得用户签名命令参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeSiger(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个账号，不允许空指针。
	 * 
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);
		return users.add(e);
	}

	/**
	 * 保存一批帐号
	 * @param a Siger数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Siger> a) {
		int size = users.size();
		for (Siger e : a) {
			add(e);
		}
		return users.size() - size;
	}

	/**
	 * 删除一个帐号
	 * @param e Siger实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Siger e) {
		Laxkit.nullabled(e);
		return users.remove(e);
	}

	/**
	 * 输出全部账号
	 * @return Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(users);
	}

	/**
	 * 统计帐号数目
	 * @return 账号数目
	 */
	public int size() {
		return users.size();
	}

	/**
	 * 判断帐号是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 设置可使用最大空间
	 * @param i 可使用最大空间
	 */
	public void setMaxSize(int i) {
		if (i < 1) {
			throw new IllegalArgumentException("illegal " + i);
		}
		maxsize = i;
	}

	/**
	 * 返回可使用最大空间
	 * @return 可使用最大空间
	 */
	public int getMaxSize() {
		return maxsize;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeSiger duplicate() {
		return new TakeSiger(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(maxsize);
		writer.writeInt(users.size());
		for (Siger e : users) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		maxsize = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			users.add(e);
		}
	}

}