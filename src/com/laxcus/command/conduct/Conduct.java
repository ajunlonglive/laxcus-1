/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.conduct;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.distribute.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 分布数据计算命令：CONDUCT。<br><br>
 * 
 * CONDUCT命令是"DIFFUSE/CONVERGE"算法的编程实现，它是一个“容器/框架命令”。在它的内部可以包含各种命令，如使用最多的SQL命令。CONDUCT命令通过命名，调用关联的分布任务组件，实现多数据、多层次、复杂环境下的分布数据计算工作。<br><br>
 * 
 * CONDUCT命令由五个阶段组成：<br>
 * 1. INIT阶段：位于CALL站点，负责参数检查、初始化和分配后续的数据资源。<br>
 * 2. FROM阶段：位于DATA站点（不分主从），负责产生初始数据。<br>
 * 3. TO(SUBTO)阶段：位于WORK站点，执行数据计算工作。<br>
 * 4. BALANCE阶段：位于CALL站点，对FROM/TO(SUBTO)阶段返回的元数据，进行汇总、分析、和分布资源的平均分配工作。所谓平均分配，即汇总以上阶段的元数据，为跟下来的计算提供大致相同的数据量，来保证用最少的时间获得最大的计算效率。<br>
 * 5. PUT阶段：位于FRONT站点，负责最后计算结果数据的显示和保存。<br><br>
 * 
 * CONDUCT处理流程: <BR>
 * FRONT -> CALL.INIT -> DATA.FROM(DIFFUSE OR SINGLE) -> CALL.BALANCE -> WORK.TO -> CALL.BALANCE -> WORK.SUBTO(迭代，可选) -> CALL.BALANCE（可选） -> FRONT.PUT <BR><BR>
 * 
 * 说明：<br>
 * 1. INIT、FROM、PUT只执行一次。<br>
 * 2. TO是迭代的，执行最少一次或者任意多次，即它可以执行一次TO处理，或者“TO -> SUBTO -> SUTBO”的处理。<br>
 * 3. BALANCE介于FROM/TO，TO/SUBTO，SUBTO/SUBTO之间。<br>
 * 
 * @author scott.liang
 * @version 1.3 3/12/2015
 * @since laxcus 1.0
 */
public final class Conduct extends DistributedCommand {

	private static final long serialVersionUID = 5343111905527443326L;

	/** 初始化分配命名对象 (可选；位于CALL站点) */
	private InitObject initObject;

	/** FROM命名对象 (DIFFUSE，必须；位于DATA站点) **/
	private FromObject fromObject;

	/** TO命名对象 (CONVERGE，必须；位于WORK站点) **/
	private ToObject toObject;

	/** 平衡数据分配接口(可选，没有定义使用系统默认接口；位于CALL站点) */
	private BalanceObject balanceObject;

	/** 将计算结果数据收集、显示、保存(可选；位于FRONT站点) */
	private PutObject putObject;

	/**
	 * 根据传入的CONDUCT实例，生成它的浅层数据副本
	 * @param that Conduct实例
	 */
	private Conduct(Conduct that) {
		super(that);
		initObject = that.initObject;
		fromObject = that.fromObject;
		toObject = that.toObject;
		balanceObject = that.balanceObject;
		putObject = that.putObject;
	}

	/**
	 * 构造一个默认和私有的CONDUCT命令
	 */
	private Conduct() {
		super();
	}

	/**
	 * 构造CONDUCT命令，指定它的根命名
	 * @param root 根命名
	 */
	public Conduct(Sock root) {
		this();
		setSock(root);
	}

//	/**
//	 * 构造CONDUCT命令，指定它的根命名
//	 * @param root 根命名
//	 */
//	public Conduct(String root) {
//		this(new Naming(root));
//	}

	/**
	 * 从可类化读取器中解析"CONDUCT"命令实例
	 * @param reader 可类化数据读取器
	 * @since 1.3
	 */
	public Conduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置INIT阶段对象
	 * @param e InitObject实例
	 */
	public void setInitObject(InitObject e) {
		initObject = e;
	}

	/**
	 * 返回INIT阶段对象
	 * @return InitObject实例
	 */
	public InitObject getInitObject() {
		return initObject;
	}

	/**
	 * 设置FROM阶段对象
	 * @param e FromObject实例
	 */
	public void setFromObject(FromObject e) {
		fromObject = e;
	}

	/**
	 * 返回FROM阶段对象
	 * @return FromObject实例
	 */
	public FromObject getFromObject() {
		return fromObject;
	}

	/**
	 * 设置TO阶段对象
	 * @param e ToObject实例
	 */
	public void setToObject(ToObject e) {
		toObject = e;
	}

	/**
	 * 关联TO阶段对象
	 * @param e ToObject实例
	 */
	public void attachToObject(ToObject e) {
		if (toObject == null) {
			toObject = e;
		} else {
			toObject.attach(e);
		}
	}

	/**
	 * 返回开始的TO阶段对象
	 * @return ToObject实例
	 */
	public ToObject getToObject() {
		return toObject;
	}

	/**
	 * 返回最后一个TO阶段对象
	 * @return ToObject实例
	 */
	public ToObject getLastToObject() {
		if (toObject != null) {
			return toObject.last();
		}
		return null;
	}

	/**
	 * 设置PUT阶段对象
	 * @param e PutObject实例
	 */
	public void setPutObject(PutObject e) {
		putObject = e;
	}

	/**
	 * 返回PUT阶段对象
	 * @return PutObject实例
	 */
	public PutObject getPutObject() {
		return putObject;
	}

	/**
	 * 设置BALANCE阶段对象
	 * @param e BalanceObject实例
	 */
	public void setBalanceObject(BalanceObject e) {
		balanceObject = e;
	}

	/**
	 * 返回BALANCE阶段对象
	 * @return BalanceObject实例
	 */
	public BalanceObject getBalanceObject() {
		return balanceObject;
	}

	/**
	 * 为CONDUCT命令和命令中的阶段对象设置命令持有人
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 * @since 1.3
	 */
	@Override
	public void setIssuer(Siger e) {
		super.setIssuer(e);
		// 设置对象签名
		if (initObject != null) {
			initObject.setIssuer(e);
		}
		if (fromObject != null) {
			fromObject.setIssuer(e);
		}
		if (toObject != null) {
			toObject.setIssuer(e);
		}
		if (balanceObject != null) {
			balanceObject.setIssuer(e);
		}
		if (putObject != null) {
			putObject.setIssuer(e);
		}
	}
	
	/**
	 * CONDUCT命令调用子对象，生成和合并它们的事务规则。<br>
	 * 如果所有对象中没有定义自己的事务规则，返回一个最高级的用户级独享事务。<br>
	 * 
	 * @see com.laxcus.command.RuleCommand#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		return collect(new DistributedObject[] { initObject, fromObject,
				toObject, balanceObject, putObject });
	}

	/**
	 * 生成当前CONDUCT实例的浅层数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Conduct duplicate() {
		return new Conduct(this);
	}

	/**
	 * 将CONDUCT参数写入可类化存储器
	 * @see com.laxcus.command.DistributedCommand#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级信息
		super.buildSuffix(writer);
		// CONDUCT处理阶段
		writer.writeInstance(initObject); // INIT阶段
		writer.writeInstance(fromObject); // FROM阶段
		writer.writeInstance(toObject); // TO阶段
		writer.writeInstance(balanceObject); // BALANCE阶段
		writer.writeInstance(putObject); // PUT阶段
	}

	/**
	 * 从可类化读取器中解析CONDUCT参数
	 * @see com.laxcus.command.DistributedCommand#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级信息
		super.resolveSuffix(reader);
		// CONDUCT处理阶段
		initObject = reader.readInstance(InitObject.class);
		fromObject = reader.readInstance(FromObject.class);
		toObject = reader.readInstance(ToObject.class);
		balanceObject = reader.readInstance(BalanceObject.class);
		putObject = reader.readInstance(PutObject.class);
	}

	//	public void test() {
	//		TableRule r1 = new TableRule(RuleOperator.SHARE_READ);
	//		r1.add(new com.laxcus.access.schema.Space("MEDIA", "MUSIC"));
	//		initObject = new InitObject();
	//		initObject.addRule(r1);
	//		
	//		List<Rule> rules = gatRules();
	//		System.out.printf("rule size is %d\n", rules.size());
	//		
	//		RuleSheet sheet1 = new RuleSheet();
	//		sheet1.addAll(rules);
	//		
	//		byte[] b = sheet1.build();
	//		System.out.printf("sheet1 length is %d\n", b.length);
	//		
	//		ClassReader reader = new ClassReader(b);
	//		RuleSheet sheet2 = new RuleSheet(reader);
	//		byte[] b2 = sheet2.build();
	//		System.out.printf("sheet2 length is %d\n", b.length);
	//		
	//		boolean success = (Laxkit.compareTo(b, b2)==0);
	//		
	//		System.out.printf("compare is %s\n", success);
	//		
	//		for(Rule e : sheet1.list()) {
	//			System.out.println(e);
	//		}
	//		System.out.println("==========");
	//		for(Rule e : sheet2.list()) {
	//			System.out.println(e);
	//		}
	//	}
	//	
	//	public static void main(String[] args) {
	//		Conduct cmd = new Conduct();
	//		cmd.test();
	//	}
}