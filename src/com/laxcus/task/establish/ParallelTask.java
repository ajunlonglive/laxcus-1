/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish;

import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.talk.*;

/**
 * 数据构建的并行任务。<br><br>
 * 
 * 并行任务分布在DATA/BUILD站点上，它们接受CALL站点上的IssueTask、AssignTask调用。<br>
 * 有SCAN、SIFT、RISE三种类型，子类包括ScanTask、SiftTask、RiseTask三种。<br>
 * 其中ScanTask、RiseTask运行在DATA主站点上，SiftTask运行在BUILD站点上。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 11/7/2009
 * @since laxcus 1.0
 */
public abstract class ParallelTask extends AccessTask {

	/** 动态交互接口 **/
	private TalkTrustor talkTrustor;

	/**
	 * 构造数据构建的并行处理任务
	 */
	protected ParallelTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		talkTrustor = null;
	}

	/**
	 * 设置CONDUCT分布任务组件运行交互代理
	 * @param e TalkTrustor实例
	 */
	public void setTalkTrustor(TalkTrustor e) {
		talkTrustor = e;
	}

	/**
	 * 返回CONDUCT分布任务组件运行交互代理
	 * @return TalkTrustor实例
	 */
	protected TalkTrustor getTalkTrustor() {
		return talkTrustor;
	}

	/**
	 * 检查目标站点上的关联组件状态
	 * @param remote 目标站点
	 * @param falg 实时会话标识
	 * @return 返回关联组件状态，包括：没有找到、等待处理、运行3种状态。
	 * @throws TaskException - 运行过程中发生分布计算异常
	 */
	protected TaskMoment check(Node remote, TalkFalg falg) throws TaskException {
		return talkTrustor.check(getInvokerId(), remote, falg);
	}

	/**
	 * 执行远程实时会话请求。<br>
	 * 方法通过RPC方式，投递到另一个节点，调用同级的“task”方法，返回会话协商结果。<br><br>
	 * 
	 * @param remote 目标站点
	 * @param quest 实时会话请求
	 * @return 返回实时会话应答结果
	 * @throws TaskException - 运行过程中发生分布计算异常
	 */
	protected TalkReply ask(Node remote, TalkQuest quest) throws TaskException {
		return talkTrustor.ask(getInvokerId(), remote, quest);
	}

	/**
	 * 同级关联组件之间的实时会话协商。<br>
	 * 本处是一个空方法，子类需要派生这个方法，实现具体的操作。<br>
	 * 
	 * @param quest 来自“ask”方法的实时会话请求
	 * @return 返回实时会话应答结果
	 */
	public TalkReply talk(TalkQuest quest) {
		return null;
	}

}