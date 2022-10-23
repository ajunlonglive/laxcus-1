/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.contact;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.distribute.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 迭代计算。<br><br>
 * 
 * 命令格式：“CONTACT 组件名称 FORK [自定义参数...] DISTANT [自定义参数...] MERGE [自定义参数...] NEAR [自定义参数...] ”。<br>
 * 面向两种业务：<br><br>
 * 1. 基于客户机/服务器模式的小规模数据处理，如EJB。<br>
 * 2. 纯粹的数据计算工作，不依赖于初始数据，通过自定义参数就可以处理。比如挖矿。<br> <br>
 * 
 * 命令从FRONT节点发出，CALL节点进行中继调度，分配到WORK节点执行（1-N个节点），产生计算结果后在FRONT节点显示和输出。<br><br>
 * 
 * CONTACT命令是对CONDUCT大规模计算的弱化和补充，减少了FORM阶段，专注于TO阶段。
 * 
 * @author scott.liang
 * @version 1.0 5/4/2020
 * @since laxcus 1.0
 */
public class Contact extends DistributedCommand {

	private static final long serialVersionUID = 3146670759344573376L;

	/** 初始化分配命名对象 (可选；位于CALL站点) */
	private ForkObject forkObject;

	/** 平衡数据分配接口(可选，没有定义使用系统默认接口；位于CALL站点) */
	private MergeObject mergeObject;

	/** 计算对象，位于WORK节点，最重要的计算部分。*/
	private DistantObject distantObject;

	/** 显示和输出对象，位于FRONT节点 **/
	private NearObject nearObject;

	/**
	 * 根据传入的CONTACT实例，生成它的浅层数据副本
	 * @param that Swift实例
	 */
	private Contact(Contact that) {
		super(that);
		forkObject = that.forkObject;
		mergeObject = that.mergeObject;
		distantObject = that.distantObject;
		nearObject = that.nearObject;
	}

	/**
	 * 构造一个默认和私有的CONTACT命令
	 */
	private Contact() {
		super();
	}

	/**
	 * 构造CONTACT命令，指定它的根命名
	 * @param root 根命名
	 */
	public Contact(Sock root) {
		this();
		setSock(root);
	}

//	/**
//	 * 构造CONTACT命令，指定它的根命名
//	 * @param root 根命名
//	 */
//	public Contact(String root) {
//		this(new Naming(root));
//	}

	/**
	 * 从可类化读取器中解析"CONTACT"命令实例
	 * @param reader 可类化数据读取器
	 */
	public Contact(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置FORK阶段对象
	 * @param e ForkObject实例
	 */
	public void setForkObject(ForkObject e) {
		forkObject = e;
	}

	/**
	 * 返回FORK阶段对象
	 * @return ForkObject实例
	 */
	public ForkObject getForkObject() {
		return forkObject;
	}
	
	
	/**
	 * 设置MERGE阶段对象
	 * @param e MergeObject实例
	 */
	public void setMergeObject(MergeObject e) {
		mergeObject = e;
	}

	/**
	 * 返回MERGE阶段对象
	 * @return MergeObject实例
	 */
	public MergeObject getMergeObject() {
		return mergeObject;
	}
	
	/**
	 * 设置DISTANT阶段对象
	 * @param e DistantObject实例
	 */
	public void setDistantObject(DistantObject e) {
		distantObject = e;
	}

	/**
	 * 返回DISTANT阶段对象
	 * @return DistantObject实例
	 */
	public DistantObject getDistantObject() {
		return distantObject;
	}

	/**
	 * 设置NEAR阶段对象
	 * @param e NearObject实例
	 */
	public void setNearObject(NearObject e) {
		nearObject = e;
	}

	/**
	 * 返回NEAR阶段对象
	 * @return NearObject实例
	 */
	public NearObject getNearObject() {
		return nearObject;
	}

	/**
	 * 为CONTACT命令和命令中的阶段对象设置命令持有人
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 * @since 1.3
	 */
	@Override
	public void setIssuer(Siger e) {
		super.setIssuer(e);
		// 设置对象签名
		if (distantObject != null) {
			distantObject.setIssuer(e);
		}
		if (nearObject != null) {
			nearObject.setIssuer(e);
		}
		if (forkObject != null) {
			forkObject.setIssuer(e);
		}
		if (mergeObject != null) {
			mergeObject.setIssuer(e);
		}
	}
	
	/**
	 * CONTACT命令调用子对象，生成和合并它们的事务规则。<br>
	 * 如果所有对象中没有定义自己的事务规则，返回一个最高级的用户级独享事务。<br>
	 * 
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		return collect(new DistributedObject[] { forkObject, mergeObject,
				distantObject, nearObject });
	}

	/**
	 * 生成当前CONTACT实例的浅层数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Contact duplicate() {
		return new Contact(this);
	}

	/**
	 * 将CONTACT参数写入可类化存储器
	 * @see com.laxcus.command.DistributedCommand#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级信息
		super.buildSuffix(writer);
		// CONTACT处理阶段
		writer.writeInstance(forkObject); // FORK阶段
		writer.writeInstance(mergeObject); // MERGE阶段
		writer.writeInstance(distantObject); // DISTANT阶段
		writer.writeInstance(nearObject); // NEAR阶段
	}

	/**
	 * 从可类化读取器中解析CONTACT参数
	 * @see com.laxcus.command.DistributedCommand#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级信息
		super.resolveSuffix(reader);
		// CONTACT处理阶段
		forkObject = reader.readInstance(ForkObject.class);
		mergeObject = reader.readInstance(MergeObject.class);
		distantObject = reader.readInstance(DistantObject.class);
		nearObject = reader.readInstance(NearObject.class);
	}

}