/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 账号所有人CALL站点查询结果。<br>
 * 
 * @author scott.liang
 * @version 1.1 10/29/2015
 * @since laxcus 1.1
 */
public class TakeOwnerCallProduct extends EchoProduct {

	private static final long serialVersionUID = -1551832025535727572L;

	/** 检查间隔时间 **/
	private long checkInterval;

	/** 表空间集合集合 **/
	private Map<Node, SpaceSet> spaces = new TreeMap<Node, SpaceSet>();

	/** 阶段命名集合 **/
	private Map<Node, PhaseSet> phases = new TreeMap<Node, PhaseSet>();

	/** 云空间 **/
	private Map<Node, CloudField> fields = new TreeMap<Node, CloudField>();

	/**
	 * 从传入的账号所有人CALL站点查询结果实例，生成它的浅层数据副本
	 * @param that FindRelateCallProduct实例
	 */
	private TakeOwnerCallProduct(TakeOwnerCallProduct that) {
		super(that);
		checkInterval = that.checkInterval;
		spaces.putAll(that.spaces);
		phases.putAll(that.phases);
		fields.putAll(that.fields);
	}

	/**
	 * 构造默认的账号所有人CALL站点查询结果
	 */
	public TakeOwnerCallProduct() {
		super();
		checkInterval = 0;
	}

	/**
	 * 从可类化读取器中解析账号所有人CALL站点查询结果
	 * @param reader 可类化读取器
	 */
	public TakeOwnerCallProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置检查间隔时间 
	 * @param ms 毫秒
	 */
	public void setCheckInterval(long ms) {
		checkInterval = ms;
	}

	/**
	 * 返回检查间隔时间
	 * @return 毫秒
	 */
	public long getCheckInterval() {
		return checkInterval;
	}

	/**
	 * 保存一批云端空间，同时指定它的节点
	 * @param node 云端节点
	 * @param a 云空间
	 */
	public void addCloudField(Node node, CloudField e) {
		if(e == null){
			return;
		}
		// 生成副本，重置节点
		CloudField field = e.duplicate();
		field.setSite(node);
		fields.put(field.getSite(), field);
	}

	/**
	 * 返回全部云端空间
	 * @return CloudField数组
	 */
	public List<CloudField> getCloudFields() {
		return new ArrayList<CloudField>(fields.values());
	}

	//	public void addCloudField(CloudField field) {
	//		Node node = field.getSite();
	//		if (node != null) {
	//			fields.put(field.getSite(), field);
	//		}
	//	}
	//
	//	public List<Node> getCloudSites() {
	//		return new ArrayList<Node>(fields.keySet());
	//	}
	//
	//	public CloudField findCloudField(Node node) {
	//		return fields.get(node);
	//	}

	/**
	 * 保存一个节点和数据表名
	 * @param node CALL站点地址
	 * @param space 数据表名
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean addSpace(Node node, Space space) {
		SpaceSet set = spaces.get(node);
		if (set == null) {
			set = new SpaceSet();
			spaces.put(node, set);
		}
		return set.add(space);
	}

	/**
	 * 保存一个节点和一组空间名称
	 * @param node CALL站点地址
	 * @param a 数据表名集合
	 * @return 返回保存的数目
	 */
	public int addSpaces(Node node, Collection<Space> a) {
		int size = spaces.size();
		for (Space e : a) {
			addSpace(node, e);
		}
		return spaces.size() - size;
	}

	/**
	 * 保存一个节点和阶段命名
	 * @param node - 节点
	 * @param phase - 阶段命名
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean addPhase(Node node, Phase phase) {
		PhaseSet set = phases.get(node);
		if (set == null) {
			set = new PhaseSet();
			phases.put(node, set);
		}
		return set.add(phase);
	}

	/**
	 * 保存一个节点和一批阶段命名
	 * @param node Node实例
	 * @param a 阶段命名数组
	 * @return 保存新增成员数目
	 */
	public int addPhases(Node node, Collection<Phase> a) {
		int size = phases.size();
		for (Phase e : a) {
			addPhase(node, e);
		}
		return phases.size() - size;
	}

	/**
	 * 返回数据表名集合
	 * @return 
	 */
	public Map<Node, SpaceSet> getSpaces() {
		return spaces;
	}

	/**
	 * 返回阶段命名集合
	 * @return
	 */
	public Map<Node, PhaseSet> getPhases() {
		return phases;
	}

	/**
	 * 合并数据
	 * @param that
	 */
	public void join(TakeOwnerCallProduct that) {
		for (Node node : that.spaces.keySet()) {
			SpaceSet set = that.spaces.get(node);
			for (Space space : set.list()) {
				addSpace(node, space);
			}
		}
		for (Node node : that.phases.keySet()) {
			PhaseSet set = that.phases.get(node);
			for (Phase space : set.list()) {
				addPhase(node, space);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public EchoProduct duplicate() {
		return new TakeOwnerCallProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 检测间隔
		writer.writeLong(checkInterval);

		// 数据表名集合
		int size = spaces.size();
		writer.writeInt(size);
		if (size > 0) {
			Iterator<Map.Entry<Node, SpaceSet>> iterator = spaces.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, SpaceSet> entry = iterator.next();
				writer.writeObject(entry.getKey());
				writer.writeObject(entry.getValue());
			}
		}
		// 阶段命名集合
		size = phases.size();
		writer.writeInt(size);
		if (size > 0) {
			Iterator<Map.Entry<Node, PhaseSet>> iterator = phases.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, PhaseSet> entry = iterator.next();
				writer.writeObject(entry.getKey());
				writer.writeObject(entry.getValue());
			}
		}

		// 云端空间
		size = fields.size();
		writer.writeInt(size);
		if(size >0){
			Iterator<Map.Entry<Node, CloudField>> iterator = fields.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, CloudField> entry = iterator.next();
				writer.writeObject(entry.getValue());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 检测间隔
		checkInterval = reader.readLong();

		// 解析数据表名
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			SpaceSet set = new SpaceSet(reader);
			spaces.put(node, set);
		}
		// 解析阶段命名
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			PhaseSet set = new PhaseSet(reader);
			phases.put(node, set);
		}
		// 云端空间
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CloudField field = new CloudField(reader);
			fields.put(field.getSite(), field);
		}
	}

}