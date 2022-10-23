/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 显示本地分布任务组件 <br><br>
 * 
 * 这个命令只在FRONT节点使用，显示END/PUT阶段的分布任务组件
 * 
 * @author scott.liang
 * @version 1.0 9/20/2019
 * @since laxcus 1.0
 */
public class CheckLocalTask extends Command {
	
	private static final long serialVersionUID = 8679196984154043206L;

	/** 分布任务组件根命名集合 **/
	private ArrayList<Sock> roots = new ArrayList<Sock>();
	
	/** 完整的格式，默认是真 **/
	private boolean full;

	/**
	 * 构造默认的显示本地分布任务组件
	 */
	public CheckLocalTask() {
		super();
		setFull(true);
	}

	/**
	 * 构造显示本地分布任务组件，指定分布任务组件根命名
	 * @param root 分布任务组件根命名
	 */
	public CheckLocalTask(Sock root) {
		this();
		add(root);
	}

	/**
	 * 构造显示本地分布任务组件，指定分布任务组件根命名
	 * @param root 分布任务组件根命名
	 */
	public CheckLocalTask(String root) {
		this(new Sock(root));
	}

	/**
	 * 构造显示本地分布任务组件，指定全部分布任务组件根命名
	 * @param roots 分布任务组件根命名列表
	 */
	public CheckLocalTask(Collection<Sock> roots) {
		this();
		addAll(roots);
	}

	/**
	 * 生成显示本地分布任务组件的数据副本
	 * @param that 显示本地分布任务组件
	 */
	private CheckLocalTask(CheckLocalTask that) {
		super(that);
		roots.addAll(that.roots);
		full = that.full;
	}

	/**
	 * 从可类化数据读取器中解析显示本地分布任务组件
	 * @param reader 可类化数据读取器
	 */
	public CheckLocalTask(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置完整格式
	 * @param b 真或者假
	 */
	public void setFull(boolean b) {
		full = b;
	}

	/**
	 * 判断是完整格式
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return full;
	}

	/**
	 * 判断是简单格式
	 * @return 返回真或者假
	 */
	public boolean isSimple() {
		return !isFull();
	}

	/**
	 * 保存分布任务组件根命名
	 * @param e 分布任务组件根命名
	 * @return 返回真或者假
	 */
	public boolean add(Sock e) {
		Laxkit.nullabled(e);
		// 存在，忽略它
		if (roots.contains(e)) {
			return false;
		}
		return roots.add(e);
	}
	
	/**
	 * 保存一批分布任务组件根命名
	 * @param a 分布任务组件根命名数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Sock> a) {
		int size = roots.size();
		for (Sock e : a) {
			add(e);
		}
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
	public CheckLocalTask duplicate() {
		return new CheckLocalTask(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(roots.size());
		for (Sock e : roots) {
			writer.writeObject(e);
		}
		writer.writeBoolean(full);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Sock e = new Sock(reader);
			roots.add(e);
		}
		full = reader.readBoolean();
	}

}