/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.log;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 日志节点
 * 
 * @author scott.liang
 * @version 1.0 6/12/2009
 * @since laxcus 1.0
 */
public final class LogSite extends Site {
	
	private static final long serialVersionUID = 6178473587173788189L;

	/** 被分配的次数 **/
	private int count;

	/** 节点类型 -> 节点配置 **/
	private Map<Integer, LogNode> logNodes = new TreeMap<Integer, LogNode>();
	
	/** 节点类型 -> 节点配置 **/
	private Map<Integer, TigNode> tigNodes = new TreeMap<Integer, TigNode>();

	/** 节点类型 -> 节点配置 **/
	private Map<Integer, BillNode> billNodes = new TreeMap<Integer, BillNode>();

	/**
	 * 将日志节点配置写入可类化写入器
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(count);
//		// 节点数目
//		writer.writeInt(logNodes.size());
//		// 节点
//		Iterator<Map.Entry<Integer, LogNode>> iterator = logNodes.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<Integer, LogNode> entry = iterator.next();
//			writer.writeInt(entry.getKey());
//			writer.writeObject(entry.getValue());
//		}
		
		// 日志节点数目
		writer.writeInt(logNodes.size());
		for (LogNode e : logNodes.values()) {
			writer.writeObject(e);
		}

		// 事件节点数目
		writer.writeInt(tigNodes.size());
		for (TigNode e : tigNodes.values()) {
			writer.writeObject(e);
		}
		
		// 消耗节点数目
		writer.writeInt(billNodes.size());
		for (BillNode e : billNodes.values()) {
			writer.writeObject(e);
		}
	}
	
	/**
	 * 从可类化读取器中解析日志节点配置
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		count = reader.readInt();
		// 节点数目
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			LogNode node = new LogNode(reader);
			logNodes.put(node.getFamily(), node);
		}
		// 事件
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			TigNode node = new TigNode(reader);
			tigNodes.put(node.getFamily(), node);
		}
		// 事件
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			BillNode node = new BillNode(reader);
			billNodes.put(node.getFamily(), node);
		}
	}

	/**
	 * 根据传入的日志节点实例，生成它的数据副本
	 * @param that LogSite实例
	 */
	private LogSite(LogSite that) {
		super(that);
		// 统计
		count = that.count;
		
		logNodes.putAll(that.logNodes);
		tigNodes.putAll(that.tigNodes);
		billNodes.putAll(that.billNodes);
		
//		// 服务终端
//		Iterator<Map.Entry<Integer, LogNode>> iterator = that.logNodes.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<Integer, LogNode> entry = iterator.next();
//			logNodes.put(entry.getKey().intValue(), (LogNode) entry.getValue().clone());
//		}
	}

	/**
	 * 构造一个默认的日志节点
	 */
	public LogSite() {
		super(SiteTag.LOG_SITE);
		count = 0;
	}
	
	/**
	 * 从可类化读取器中解析日志节点记录
	 * @param reader 可类化读取器
	 */
	public LogSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加统计值
	 * @param num 统计值
	 */
	public void addCount(int num) {
		count += num;
	}
	
	/**
	 * 返回统计值
	 * @return 统计值
	 */
	public int getCount() {
		return count;
	}

	/**
	 * 保存一个日志节点
	 * @param node LogNode实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addLog(LogNode node) {
		return logNodes.put(node.getFamily(), (LogNode) node.clone()) == null;
	}

	/**
	 * 删除一类日志节点
	 * @param family 节点类型
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeLog(int family) {
		return logNodes.remove(family) != null;
	}

	/**
	 * 查找一类日志节点
	 * @param family 节点类型
	 * @return LogNode实例
	 */
	public LogNode findLog(int family) {
		return logNodes.get(family);
	}

	/**
	 * 判断某一类节点存在
	 * @param family 节点类型
	 * @return 删除成功返回真，否则假
	 */
	public boolean hasLog(int family) {
		return logNodes.containsKey(family);
	}

	/**
	 * 输出全部类型的日志节点
	 * @return LogNode实例
	 */
	public List<LogNode> getLogNodes() {
		return new ArrayList<LogNode>( logNodes.values());
	}

	/**
	 * 输出全部类型的日志节点的数组
	 * @return LogNode数组
	 */
	public LogNode[] toLogNodeArray() {
		LogNode[] array = new LogNode[logNodes.size()];
		return logNodes.values().toArray(array);
	}

	/**
	 * 保存一个操作消息节点
	 * @param node TigNode实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addTig(TigNode node) {
		return tigNodes.put(node.getFamily(), (TigNode) node.clone()) == null;
	}

	/**
	 * 删除一类操作消息节点
	 * @param family 节点类型
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeTig(int family) {
		return tigNodes.remove(family) != null;
	}

	/**
	 * 查找一类操作消息节点
	 * @param family 节点类型
	 * @return TigNode实例
	 */
	public TigNode findTig(int family) {
		return tigNodes.get(family);
	}

	/**
	 * 判断某一类节点存在
	 * @param family 节点类型
	 * @return 删除成功返回真，否则假
	 */
	public boolean hasTig(int family) {
		return tigNodes.containsKey(family);
	}

	/**
	 * 输出全部类型的操作消息节点
	 * @return TigNode实例
	 */
	public List<TigNode> getTigNodes() {
		return new ArrayList<TigNode>( tigNodes.values());
	}

	/**
	 * 输出全部类型的操作消息节点的数组
	 * @return TigNode数组
	 */
	public TigNode[] toTigNodeArray() {
		TigNode[] array = new TigNode[tigNodes.size()];
		return tigNodes.values().toArray(array);
	}

	/**
	 * 保存一个消耗记录节点
	 * @param node BillNode实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addBill(BillNode node) {
		return billNodes.put(node.getFamily(), (BillNode) node.clone()) == null;
	}

	/**
	 * 删除一类消耗记录节点
	 * @param family 节点类型
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeBill(int family) {
		return billNodes.remove(family) != null;
	}

	/**
	 * 查找一类消耗记录节点
	 * @param family 节点类型
	 * @return BillNode实例
	 */
	public BillNode findBill(int family) {
		return billNodes.get(family);
	}

	/**
	 * 判断某一类节点存在
	 * @param family 节点类型
	 * @return 删除成功返回真，否则假
	 */
	public boolean hasBill(int family) {
		return billNodes.containsKey(family);
	}

	/**
	 * 输出全部类型的消耗记录节点
	 * @return BillNode实例
	 */
	public List<BillNode> getBillNodes() {
		return new ArrayList<BillNode>( billNodes.values());
	}

	/**
	 * 输出全部类型的消耗记录节点的数组
	 * @return BillNode数组
	 */
	public BillNode[] toBillNodeArray() {
		BillNode[] array = new BillNode[billNodes.size()];
		return billNodes.values().toArray(array);
	}

	/**
	 * 判断当前是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return logNodes.isEmpty() && tigNodes.isEmpty() && billNodes.isEmpty();
	}

	/**
	 * 返回日志节点成员数目
	 * @return 成员数目
	 */
	public int size() {
		return logNodes.size() + tigNodes.size() + billNodes.size();
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		logNodes.clear();
		tigNodes.clear();
		billNodes.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public LogSite duplicate() {
		return new LogSite(this);
	}

}