/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.establish;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.distribute.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 分布数据构建命令：ESTABLISH。<br><br>
 * 
 * ESTABLISH命令是“SCAN/SIFT”算法的编程实现。它做为数据构建的“容器命令”，遵循“阶段化”的数据处理原则。在运行过程中，通过命名关联到对应的分布任务组件，执行分布、复杂环境下的数据再生产工作。<br><br>
 * 
 * ESTABLISH命令由6个阶段组成：<br>
 * 1. ISSUE阶段：位于CALL节点，负责检查参数，分配后续数据处理的节点资源。<br>
 * 2. SCAN阶段：位于DATA主节点（<b>PRIME SITE</b>），对分布的数据资源进行扫描。<br>
 * 3. SIFT(SUBSIFT)阶段：位于BUILD节点，对实体数据执行再处理工作，包括检查、删除、修改、插入。<br>
 * 4. RISE阶段：位于DATA主节点（<b>PRIME SITE</b>），工作内容：<1>从BUILD节点下载新生产的数据块 <2>删除本地旧数据块 <3>分发到DATA从站点。<br>
 * 5. ASSIGN阶段：位于CALL节点，对SCAN/SIFT阶段产生的元数据进行汇总、分析、调度分配。<br>
 * 6. END阶段：位于FRONT节点，负责数据构造结果的显示和保存。<br><br>
 * 
 * ESTABLISH处理流程：<br>
 * FRONT -> CALL.ISSUE -> DATA.SCAN -> CALL.ASSIGN -> BUILD.SIFT -> CALL.ASSIGN -> BUILD.SUBSIFT（迭代，可选）-> CALL.ASSIGN（可选）-> DATA.RISE -> FRON.END <br><br>
 * 
 * 数据构建的两种方式：<br>
 * <1> 数据优化(regulate)，是在原数据基础上，对数据块中的数据进行再组织，它清除过期但是仍然遗留在硬盘上的垃圾数据，不改变数据内容，使数据变小和紧凑，提高数据检索效率和释放硬盘空间。这项工作的实质就是把DATA主节点的Access.regulate操作转移到BUILD节点上进行，目前已经由“MODULATE”命令实现。<br>
 * <2> 数据重组(reshuffle)，或者称为“洗牌”。是从任意多个表的数据中，提取需要的数据，按照新的要求，生成新数据。旧数据内容可以改变或者不改变，新数据是脱离旧数据的存在。<br><br>
 * 
 * ESTABLISH业务说明：<br>
 * 1. ISSUE、SCAN、RISE、END只执行一次。<br>
 * 2. SIFT是迭代处理，它是数据构建业务的核心，执行最少一次或者任意多次。如是一次，只有SIFT，多次的流程是：SIFT -> SUBSIFT -> SUBSIFT。<br>
 * 3. ASSIGN介于SCAN/SIFT、SIFT/SUBSIFT、SIFT/RISE、SUBSIFT/RISE之间。<br>
 * 4. ESTABLISH主要是消除冗余数据和产生新数据的作用，经它处理过的数据，理论上会更快，可以理解为是CONDUCT的加速器。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/12/2015
 * @since laxcus 1.0
 */
public final class Establish extends DistributedCommand {

	private static final long serialVersionUID = 8725499755354920809L;

	/** ISSUE阶段对象 **/
	private IssueObject issueObject;

	/** SCAN阶段对象 **/
	private ScanObject scanObject;

	/** ASSIGN阶段对象 **/
	private AssignObject assignObject;

	/** SIFT阶段对象 **/
	private SiftObject siftObject;

	/** RISE阶段对象 **/
	private RiseObject riseObject;

	/** END阶段对象 **/
	private EndObject endObject;

	/**
	 * 根据传入的数据构建实例，生成它的浅层数据副本
	 * @param that Establish实例
	 */
	private Establish(Establish that) {
		super(that);
		issueObject = that.issueObject;
		scanObject = that.scanObject;
		assignObject = that.assignObject;
		siftObject = that.siftObject;
		riseObject = that.riseObject;
		endObject = that.endObject;
	}

	/**
	 * 构造一个默认和私有的数据构建命令
	 */
	private Establish() {
		super();
	}

	/**
	 * 构造数据构建命令，指定它的根命名
	 * @param root 根命名
	 */
	public Establish(Sock root) {
		this();
		setSock(root);
	}

//	/**
//	 * 构造数据构建命令，指定它的根命名
//	 * @param root 根命名
//	 */
//	public Establish(String root) {
//		this(new Naming(root));
//	}

	/**
	 * 从可类化数据读取器中解析“ESTABLISH”命令参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Establish(ClassReader reader) {
		this();
		super.resolve(reader);
	}

	/**
	 * 设置ISSUE阶段对象
	 * @param e IssueObject实例
	 */
	public void setIssueObject(IssueObject e) {
		issueObject = e;
	}

	/**
	 * 返回ISSUE阶段对象
	 * @return IssueObject实例
	 */
	public IssueObject getIssueObject() {
		return issueObject;
	}

	/**
	 * 设置SCAN阶段对象
	 * @param e ScanObject实例
	 */
	public void setScanObject(ScanObject e) {
		scanObject = e;
	}

	/**
	 * 返回SCAN阶段对象
	 * @return ScanObject实例
	 */
	public ScanObject getScanObject() {
		return scanObject;
	}

	/**
	 * 设置ASSIGN阶段对象
	 * @param e AssignObject实例
	 */
	public void setAssignObject(AssignObject e) {
		assignObject = e;
	}

	/**
	 * 返回ASSIGN阶段对象
	 * @return AssignObject实例
	 */
	public AssignObject getAssignObject() {
		return assignObject;
	}

	/**
	 * 设置SIFT阶段对象
	 * @param e SiftObject实例
	 */
	public void setSiftObject(SiftObject e) {
		siftObject = e;
	}

	/**
	 * 返回SIFT阶段对象
	 * @return SiftObject实例
	 */
	public SiftObject getSiftObject() {
		return siftObject;
	}

	/**
	 * 设置RISE阶段对象
	 * @param e RiseObject实例
	 */
	public void setRiseObject(RiseObject e) {
		riseObject = e;
	}

	/**
	 * 返回RISE阶段对象
	 * @return RiseObject实例
	 */
	public RiseObject getRiseObject() {
		return riseObject;
	}

	/**
	 * 设置END阶段对象
	 * @param e EndObject实例
	 */
	public void setEndObject(EndObject e) {
		endObject = e;
	}

	/**
	 * 返回END阶段对象
	 * @return EndObject实例
	 */
	public EndObject getEndObject() {
		return endObject;
	}

	/**
	 * 为数据构建命令和命令中的阶段对象设置命令持有人
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger e) {
		super.setIssuer(e);
		// 设置对象签名
		if (issueObject != null) {
			issueObject.setIssuer(e);
		}
		if (scanObject != null) {
			scanObject.setIssuer(e);
		}
		if (assignObject != null) {
			assignObject.setIssuer(e);
		}
		if (siftObject != null) {
			siftObject.setIssuer(e);
		}
		if(riseObject != null) {
			riseObject.setIssuer(e);
		}
		if (endObject != null) {
			endObject.setIssuer(e);
		}
	}
	
	/**
	 * ESTABLISH收集子类的事务规则，合并后输出。<br>
	 * 如果所有子对象中没有定义自己的事务规则，返回一个最高级和最严格的“用户级独享事务”。
	 * 
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		return collect(new DistributedObject[] { issueObject, scanObject,
				siftObject, riseObject, assignObject, endObject });
	}

	/**
	 * 根据当前"ESTABLISH"命令实例，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Establish duplicate() {
		return new Establish(this);
	}

	/**
	 * 将数据构建命令参数写入可类化存储器
	 * @see com.laxcus.command.DistributedCommand#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 父类信息
		super.buildSuffix(writer);
		// ESTABLISH阶段对象
		writer.writeInstance(issueObject);	// ISSUE阶段
		writer.writeInstance(scanObject);	// SCAN阶段
		writer.writeInstance(siftObject);	// SIFT阶段
		writer.writeInstance(riseObject);	// RISE阶段
		writer.writeInstance(assignObject);	// ASSIGN阶段
		writer.writeInstance(endObject);	// END阶段
	}

	/**
	 * 从可类化读取器中解析数据构建命令参数
	 * @see com.laxcus.command.DistributedCommand#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析父类信息
		super.resolveSuffix(reader);
		// ESTABLISH阶段对象
		issueObject = reader.readInstance(IssueObject.class);	// ISSUE阶段
		scanObject = reader.readInstance(ScanObject.class);		// SCAN阶段
		siftObject = reader.readInstance(SiftObject.class);		// SIFT阶段
		riseObject = reader.readInstance(RiseObject.class);		// RISE阶段
		assignObject = reader.readInstance(AssignObject.class);	// ASSIGN阶段
		endObject = reader.readInstance(EndObject.class);		// END阶段
	}

}