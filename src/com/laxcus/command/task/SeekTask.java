/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 搜索分布任务组件 <br><br>
 * 
 * 此命令由WATCH站点发出，通过TOP站点分发给ACCOUNT/HOME站点，或者由HOME站点分发给下属关联站点，然后返回到WATCH站点。<br><br>
 * 
 * 三种检索情况：<br>
 * 1. 有用户签名和组件名称 <br>
 * 2. 有用户签名 <br>
 * 3. 有组件名称 <br>
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class SeekTask extends Command {

	private static final long serialVersionUID = 8032628907427334147L;

	/** 用户签名。如果是系统组件，此参数忽略 **/
	private Siger username;
	
	/** 分布任务组件根命名集合 **/
	private TreeSet<Sock> roots = new TreeSet<Sock>();

	/**
	 * 构造默认的搜索分布任务组件
	 */
	public SeekTask() {
		super();
	}

	/**
	 * 构造搜索分布任务组件，指定分布任务组件根命名
	 * @param root 分布任务组件根命名
	 */
	public SeekTask(Sock root) {
		this();
		add(root);
	}

	/**
	 * 构造搜索分布任务组件，指定分布任务组件根命名
	 * @param root 分布任务组件根命名
	 */
	public SeekTask(String root) {
		this(new Sock(root));
	}

	/**
	 * 构造搜索分布任务组件，指定用户签名 
	 * @param username 用户签名
	 */
	public SeekTask(Siger username) {
		this();
		setUsername(username);
	}

	/**
	 * 构造搜索分布任务组件，指定用户签名和分布任务组件根命名
	 * @param username 用户签名
	 * @param root 分布任务组件根命名
	 */
	public SeekTask(Siger username, Sock root) {
		this(username);
		add(root);
	}

	/**
	 * 构造搜索分布任务组件，指定用户签名和分布任务组件根命名
	 * @param username 用户签名
	 * @param root 分布任务组件根命名
	 */
	public SeekTask(Siger username, String root) {
		this(username, new Sock(root));
	}

	/**
	 * 构造搜索分布任务组件，指定用户签名和全部分布任务组件根命名
	 * @param username 用户签名
	 * @param roots 分布任务组件根命名列表
	 */
	public SeekTask(Siger username, Collection<Sock> roots) {
		this(username);
		addAll(roots);
	}
	
	/**
	 * 构造搜索分布任务组件，指定全部分布任务组件根命名
	 * @param roots 分布任务组件根命名列表
	 */
	public SeekTask(Collection<Sock> roots) {
		this();
		addAll(roots);
	}

	/**
	 * 生成搜索分布任务组件的数据副本
	 * @param that 搜索分布任务组件
	 */
	private SeekTask(SeekTask that) {
		super(that);
		username = that.username;
		roots.addAll(that.roots);
	}

	/**
	 * 从可类化数据读取器中解析搜索分布任务组件
	 * @param reader 可类化数据读取器
	 */
	public SeekTask(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);

		username = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/**
	 * 保存分布任务组件根命名
	 * @param e 分布任务组件根命名
	 * @return 返回真或者假
	 */
	public boolean add(Sock e) {
		Laxkit.nullabled(e);
		return roots.add(e);
	}
	
	/**
	 * 保存一批分布任务组件根命名
	 * @param a 分布任务组件根命名数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Sock> a) {
		int size = roots.size();
		roots.addAll(a);
		return roots.size() - size;
	}

	/**
	 * 输出全部分布任务组件根命名
	 * @return 分布任务组件根命名列表
	 */
	public List<Sock> list() {
		return new ArrayList<Sock>(roots);
	}

	/**
	 * 统计分布任务组件根命名数目
	 * @return 分布任务组件根命名数目
	 */
	public int size() {
		return roots.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * 要求显示全部
	 * @return 真或者假
	 */
	public boolean isAll() {
		return isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekTask duplicate() {
		return new SeekTask(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(username);
		writer.writeInt(roots.size());
		for (Sock e : roots) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = reader.readInstance(Siger.class);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Sock e = new Sock(reader);
			roots.add(e);
		}
	}

}