/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.balance;

import java.io.*;

import com.laxcus.command.access.*;
import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * @author scott.liang
 *
 */
public class JoinSelectBalanceTask extends BalanceTask {

	/**
	 * 
	 */
	public JoinSelectBalanceTask() {
		super();
	}

	public ToObject dispatch(ToObject object, FluxArea[] areas) throws TaskException {
		Phase phase = object.getPhase();
		ToDispatcher dispatcher = object.getDispatcher();
		ToInputter inputter = object.getInputter();

		// 比较后续TO阶段的节点数目，选择一个合理的值
		int sites = inputter.getSites(); 
		NodeSet set = super.getToSeeker().findToSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new BalanceTaskException("cannot find sites by '%s'", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		Logger.info("SelectJoinBalanceTask.split, phase:%s, site is %d", phase, sites);

		// 取出JOIN语句，它在初始化时定义
		TaskParameter value = dispatcher.findParameter(JoinTaskKit.JOIN_OBJECT);
		if (value == null || !value.isCommand()) {
			throw new BalanceTaskException("SelectBalanceTask.split, cannot find select");
		}
		Join join = (Join) ((TaskCommand) value).getValue();

		//		// debug code, start
		//		try {
		//			Logger.debug("JoinSelectBalanceTask.balance, write to: %d - %d", off, len);
		//			java.io.FileOutputStream out = new java.io.FileOutputStream("/notes/join.bin", true);
		//			out.write(b, off, len);
		//			out.close();
		//		} catch (java.io.IOException e) {
		//			Logger.fatal(e);
		//		}
		//		// debug code, end

		// 解析元数据，按照有效节点数进行重新排列
		//		FluxArea[] areas = super.resolveMetaData(b, off, len);
		CyberMatrix matrix = new CyberMatrix(areas);
		CyberSphere[] spheres = matrix.balance(sites);

		// debug code, start
		for(FluxArea area : areas) {
			Logger.debug("JoinSelectBalanceTask.balance, task id:%d, from:%s", area.getTaskId(), area.getSource());
		}
		// debug code, end

		for (int index = 0; index < spheres.length; index++) {
			Node node = set.next();
			ToSession session = new ToSession(phase, node);
			// 数据元信息
			session.setSphere(spheres[index]);
			// JOIN对象
			session.addCommand(JoinTaskKit.JOIN_OBJECT, join);

			// 保存会话句柄
			dispatcher.addSession(session);			
		}

		return object;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, byte[], int, int)
	 */
	@Override
	public ToObject admix(ToObject object, byte[] b, int off, int len) throws TaskException {
		FluxArea[] areas = super.splitFluxArea(b, off, len);
		return this.dispatch(object, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, java.io.File[])
	 */
	@Override
	public ToObject admix(ToObject current, File[] files) throws TaskException {
		return super.defaultAdmix(current, files);
	}
	
	//	public ToObject dispatch(ToObject object, byte[] b, int off, int len) throws TaskException {
	//		Phase phase = object.getPhase();
	//		ToDispatcher dispatcher = object.getDispatcher();
	//		ToInputter inputter = object.getInputter();
	//
	//		// 比较后续TO阶段的节点数目，选择一个合理的值
	//		int sites = inputter.getSites(); 
	//		NodeSet set = super.getToSeeker().findToSites(phase);
	//		if (set == null || set.isEmpty()) {
	//			throw new BalanceTaskException("cannot find sites by '%s'", phase);
	//		}
	//		if (sites < 1 || sites > set.size()) {
	//			sites = set.size();
	//		}
	//		
	//		Logger.info("SelectJoinBalanceTask.split, phase:%s, site is %d", phase, sites);
	//		
	//		// 取出JOIN语句，它在初始化时定义
	//		TaskParameter value = dispatcher.findValue(JoinTaskKit.JOIN_OBJECT);
	//		if (value == null || !value.isSerialable()) {
	//			throw new BalanceTaskException("SelectBalanceTask.split, cannot find select");
	//		}
	//		Join join = (Join) ((TaskSerialable) value).getValue();
	//		
	////		// debug code, start
	////		try {
	////			Logger.debug("JoinSelectBalanceTask.balance, write to: %d - %d", off, len);
	////			java.io.FileOutputStream out = new java.io.FileOutputStream("/notes/join.bin", true);
	////			out.write(b, off, len);
	////			out.close();
	////		} catch (java.io.IOException e) {
	////			Logger.fatal(e);
	////		}
	////		// debug code, end
	//
	//		// 解析元数据，按照有效节点数进行重新排列
	//		FluxArea[] areas = super.resolveMetaData(b, off, len);
	//		CyberMatrix matrix = new CyberMatrix(areas);
	//		CyberSphere[] spheres = matrix.balance(sites);
	//		
	//		// debug code, start
	//		for(FluxArea area : areas) {
	//			Logger.debug("JoinSelectBalanceTask.balance, jobid:%d, from:%s", area.getTaskId(), area.getSource());
	//		}
	//		// debug code, end
	//
	//		for (int index = 0; index < spheres.length; index++) {
	//			Node node = set.next();
	//			ToSession session = new ToSession(phase, node);
	//			// 数据元信息
	//			session.setSphere(spheres[index]);
	//			// JOIN对象
	//			session.addValue(new TaskSerialable(JoinTaskKit.JOIN_OBJECT, join));
	//
	//			// 保存会话句柄
	//			dispatcher.addSession(session);			
	//		}
	//		
	//		return object;
	//	}



}