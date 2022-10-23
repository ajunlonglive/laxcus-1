/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.shutdown;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 远程关闭命令<br>
 * 
 * 这个命令是WATCH站点发出，通知TOP/HOME集群内的一个站点，结束服务并且退出运行状态。
 * 
 * @author scott.liang
 * @version 1.1 5/19/2015
 * @since laxcus 1.0
 */
public final class Shutdown extends Command {

	private static final long serialVersionUID = 5535177344427859981L;

	/** 关闭前延时时间，默认是5秒，这个时间用来把应答发送出去 **/
	private long delay;
	
	/** 站点地址 **/
	private Set<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认的远程关闭命令
	 */
	public Shutdown() {
		super();
		setDelay(5000);
	}

	/**
	 * 根据传入的远程关闭命令，生成它的浅层数据副本
	 * @param that 远程关闭命令
	 */
	private Shutdown(Shutdown that) {
		super(that);
		delay = that.delay;
		sites.addAll(that.sites);
	}

	/**
	 * 从可类化数据读取器中解析远程关闭命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Shutdown(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置延时时间，必须大于0
	 * @param ms 以毫秒为单位的时间
	 */
	public void setDelay(long ms) {
		if (ms > 0) {
			delay = ms;
		}
	}

	/**
	 * 返回延时时间
	 * @return 以毫秒为单位的延时时间
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * 判断是全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return sites.isEmpty();
	}

	/**
	 * 增加一个站点
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}
	
	/**
	 * 设置一批站点地址
	 * @param a Node集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 输出全部站点
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 清除地址
	 */
	public void clear() {
		sites.clear();
	}
	
	/**
	 * 返回站点数目
	 * @return 站点数目
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断有站点
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Shutdown duplicate() {
		return new Shutdown(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(delay);
		int size = sites.size();
		writer.writeInt(size);
		for (Node node : sites) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		delay = reader.readLong();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			sites.add(node);
		}
	}

}