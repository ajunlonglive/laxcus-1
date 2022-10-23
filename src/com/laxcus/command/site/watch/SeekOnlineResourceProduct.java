/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 检索站点在线资源报告
 * 
 * @author scott.liang
 * @version 1.0 4/22/2018
 * @since laxcus 1.0
 */
public class SeekOnlineResourceProduct extends EchoProduct {

	private static final long serialVersionUID = 6312104682661697522L;

	/** 资源引用 **/
	private TreeMap<Siger, SeekOnlineResourceItem> items = new TreeMap<Siger, SeekOnlineResourceItem>();
	
	/** 系统层阶段命名 **/
	private TreeSet<Phase> systems = new TreeSet<Phase>();

	/**
	 * 构造默认的检索站点在线资源报告
	 */
	public SeekOnlineResourceProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析检索站点在线资源报告
	 * @param reader 可类化数据读取器
	 */
	public SeekOnlineResourceProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造检索站点在线资源报告的数据副本
	 * @param that SeekOnlineResourceProduct实例
	 */
	private SeekOnlineResourceProduct(SeekOnlineResourceProduct that) {
		super(that);
		items.putAll(that.items);
		systems.addAll(that.systems);
	}

	/**
	 * 保存一个用户资源引用，不允许空指针
	 * @param refer Refer实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addRefer(Refer refer) {
		Laxkit.nullabled(refer);

		SeekOnlineResourceItem item = items.get(refer.getUsername());
		if (item == null) {
			item = new SeekOnlineResourceItem(refer);
			items.put(item.getSiger(), item);
		} else {
			item.setRefer(refer);
		}
		return true;
	}

	/**
	 * 保存一批用户资源引用
	 * @param a Refer数组
	 * @return 返回新增成员数目
	 */
	public int addRefers(List<Refer> a) {
		int size = items.size();
		for (Refer e : a) {
			addRefer(e);
		}
		return items.size() - size;
	}

	/**
	 * 输出全部用户资源引用
	 * @return 用户资源引用列表
	 */
	public List<SeekOnlineResourceItem> getItems() {
		return new ArrayList<SeekOnlineResourceItem>(items.values());
	}

	/**
	 * 保存阶段命名
	 * @param phase 阶段命名
	 * @return 返回真或者假
	 */
	public boolean addPhase(Phase phase) {
		Laxkit.nullabled(phase);

		// 判断是系统或者用户级
		if (phase.isSystemLevel()) {
			return systems.add(phase);
		} else {
			SeekOnlineResourceItem item = items.get(phase.getIssuer());
			if (item == null) {
				item = new SeekOnlineResourceItem(phase.getIssuer());
				items.put(item.getSiger(), item);
			}
			return item.addPhase(phase);
		}
	}

	/**
	 * 保存一批阶段命名，包括系统级和用户级
	 * @param a 阶段命名数组
	 * @return 
	 */
	public void addPhases(List<Phase> a) {
		for (Phase e : a) {
			addPhase(e);
		}
	}

	/**
	 * 输出全部系统级阶段命名
	 * @return 阶段命名列表
	 */
	public List<Phase> getSystemPhases() {
		return new ArrayList<Phase>(systems);
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SeekOnlineResourceProduct duplicate() {
		return new SeekOnlineResourceProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(items.size());
		for (SeekOnlineResourceItem e : items.values()) {
			writer.writeObject(e);
		}
		writer.writeInt(systems.size());
		for(Phase e : systems) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SeekOnlineResourceItem e = new SeekOnlineResourceItem(reader);
			items.put(e.getSiger(), e);
		}
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			systems.add(e);
		}
	}

}