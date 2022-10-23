/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.init;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 初始化和检查挖矿。<br><br>
 * 
 * 这个类判断挖矿参数正确和检查分布资源有效性。
 * 
 * 语法格式：<br>
 * CONDUCT MINING <br>
 * FROM SITES:12; PREFIX(STRING)='MINING'; BEGIN(long)=122; END(long)=12222; ZEROS(int)=3; GPU(BOOL)=NO <br>
 * TO SITES:1 
 * PUT NODE(string)='矿源位置'; PLAINTEXT(STRING)='明文'; SHA256(STRING)='挖矿码' <br><br>
 * 
 * 注意：参数结束用分号（;）标注。
 * 
 * @author xiaoyang.yuan
 * @version 1.0 12/12/2015
 * @since laxcus 1.0
 */
public class MineInitTask extends InitTask {

	/**
	 * 构造默认的挖矿初始化类
	 */
	public MineInitTask() {
		super();
	}

	/**
	 * 建立分布计算的FROM任务
	 * @param conduct 分布计算命令
	 * @throws TaskException - 如果发生错误，弹出分布任务异常
	 */
	private void createFromTask(Conduct conduct) throws TaskException {
		FromObject object = conduct.getFromObject();
		FromInputter inputter = object.getInputter(0);
		Phase phase = inputter.getPhase();

		boolean gpu = findBoolean(inputter, "GPU");
		int zeros = findInteger(inputter, "zeros"); // 前缀0值统计
		String prefix = findString(inputter, "prefix"); // 前缀字符
		long begin = findLong(inputter, "begin"); // 查找搜索范围
		long end = findLong(inputter, "end");
		// 判断检索范围有效
		if (begin > end) { 
			throw new InitTaskException("%d > %d", begin, end);
		}
		long size = (end - begin + 1);

		Logger.debug(getIssuer(), this, "createFromTask", "%s - [%d - %d]", prefix, begin, end);

		// 检查命名主机数量
		NodeSet set = getFromSeeker().findFromSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new InitTaskException("没有找到FROM节点：%s", phase);
		}

		// 检查FROM阶段节点数目，以实际需求为准
		int sites = inputter.getSites();
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}
		long unit = size / sites; // 单元空间
		
		// 检查WORK节点，最少一个，如果少于一个弹出错误
		int workSites = conduct.getToObject().getInputter().getSites();
		if (workSites < 1) {
			throw new InitTaskException("TO节点数目错误！ %d", workSites);
		}

		FromDispatcher dispatcher = new FromDispatcher(phase);

		long seek = begin;
		for (int index = 0; index < sites; index++) {
			Node node = set.next();
			FromSession session = new FromSession(phase, node);
			// 设置索引下标，也是它的模值
			session.addInteger("INDEX",  index + 1);
			// 设置前缀字符
			session.addString("PREFIX", prefix);
			// 设置SHA256散列码前面0的数目
			session.addInteger("ZEROS", zeros);
			// 选择GPU处理或者否
			session.addBoolean("GPU", gpu);
			// 设置每个会话的处理范围
			session.addLong("BEGIN", seek);
			if (index < sites - 1) {
				session.addLong("END", seek + unit);
				seek += unit;
			} else {
				session.addLong("END", end);
			}
			// 保存FROM会话实例
			dispatcher.addSession(session);
		}
		object.setDispatcher(dispatcher);
	}

	/**
	 * 建立TO阶段实例，最少一个WORK.TO节点
	 * @param conduct 分布计算命令
	 * @throws TaskException - 分布任务异常
	 */
	private void createToTask(Conduct conduct) throws TaskException {
		ToObject object = conduct.getToObject();
		Phase phase = object.getPhase();
		// 检查TO阶段主机地址是否存在
		checkToSites(phase);

		// 建立TO阶段分配器
		ToDispatcher dispatcher = new ToDispatcher(phase);
		object.setDispatcher(dispatcher);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {
		// 建立FROM/TO阶段实例,分配参数
		createToTask(conduct);
		createFromTask(conduct);
		return conduct;
	}

}
