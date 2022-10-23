/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 获得分布组件标识命令。<br>
 * 
 * 这个命令由CALL/DATA/BUILD/WORK站点发出，目标是ACCOUNT站点，要求ACCOUNT返回指定的分布任务组件。
 * 集群的全部分布组件以“.dtc”后缀保存ACCOUNT站点，ACCOUNT返回的内容是分布任务文件的数字内容。
 * 
 * @author scott.liang
 * @version 1.1 05/06/2015
 * @since laxcus 1.0
 */
public final class TakeTaskTag extends Command {

	private static final long serialVersionUID = -6265878927617624757L;

	/** 分布任务组件的工作部件 **/
	private TaskPart part;
	
	/** 投递的地址，允许空值 **/
	private Node remote;

	/**
	 * 根据传入的获得分布组件标识命令，生成它的数据副本
	 * @param that TakeTaskTag实例
	 */
	private TakeTaskTag(TakeTaskTag that) {
		super(that);
		part = that.part;
		remote = that.remote;
	}

	/**
	 * 构造默认和私有的获得分布组件标识命令。
	 */
	private TakeTaskTag() {
		super();
	}

	/**
	 * 构造获得分布组件标识命令，指定分布任务组件的工作部件。
	 * @param part 分布任务组件的工作部件
	 */
	public TakeTaskTag(TaskPart part) {
		this();
		setPart(part);
	}

	/**
	 * 构造获得分布组件标识命令，指定分布任务组件的工作部件。
	 * @param part 分布任务组件的工作部件
	 */
	public TakeTaskTag(Node remote, TaskPart part) {
		this(part);
		setRemote(remote);
	}
	
	/**
	 * 从可类化数据读取器中解析获得分布组件标识命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeTaskTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置目标地址，允许空值
	 * @param e Node实例
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回目标地址
	 * @return Node实例
	 */
	public Node getRemote() {
		return remote;
	}

	/**
	 * 设置分布任务组件的工作部件，不允许空指针。
	 * @param e TaskPart实例
	 * @throws NullPointerException
	 */
	public void setPart(TaskPart e) {
		Laxkit.nullabled(e);

		part = e;
	}

	/**
	 * 返回分布任务组件的工作部件
	 * @return TaskPart实例
	 */
	public TaskPart getPart() {
		return part;
	}

	/**
	 * 返回任务组件签名
	 * @return 数据签名
	 */
	public Siger getTaskIssuer() {
		if (part == null) {
			return null;
		}
		return part.getIssuer();
	}

	/**
	 * 判断是系统级阶段命名
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isSystemLevel() {
		return part.isSystemLevel();
	}

	/**
	 * 判断是用户级阶段命名
	 * @return 判断成立返回“真”，否则“假”。
	 */
	public boolean isUserLevel() {
		return part.isUserLevel();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeTaskTag duplicate() {
		return new TakeTaskTag(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(part);
		writer.writeInstance(remote);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		part = reader.readInstance(TaskPart.class);
		remote = reader.readInstance(Node.class);
	}

}