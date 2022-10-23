/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.guide.print;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.contact.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.task.guide.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.naming.*;

/**
 * 分布打印引导任务
 * 
 * @author scott.liang
 * @version 1.0 7/29/2020
 * @since laxcus 1.0
 */
public class PrintGuideTask extends GuideTask {

	/**
	 * 分布打印引导任务
	 */
	public PrintGuideTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#getSocks()
	 */
	@Override
	public List<Sock> getSocks() {
		ArrayList<Sock> array = new ArrayList<Sock>();
		array.add(FT.print);
		array.add(FT.track);
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#isSupport(com.laxcus.util.naming.Sock)
	 */
	@Override
	public boolean isSupport(Sock e) {
		if (e == null) {
			throw new NullPointerException();
		}
		List<Sock> socks = getSocks();
		for (Sock sock : socks) {
			if (sock.compareTo(e) == 0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#markup(com.laxcus.util.naming.Sock)
	 */
	@Override
	public InputParameterList markup(Sock sock) throws GuideTaskException {
		if (!isSupport(sock)) {
			throw new GuideTaskException("illegal sock: %s", sock);
		}

		InputInteger sites = new InputInteger(FT.inputSites, 1, "Distant阶段节点数量，运行过程中自动适配！");
		sites.setSelect(true); // 必选项!
		
		InputParameterUnit distant = new InputParameterUnit(PhaseTag.DISTANT);
		distant.add(sites);
		
//		distant.add(new InputDouble("分布计算规则", 12.23f, "测试双浮点数，没有其他作用"));
//		distant.add(new InputFloat("分布存储规则", 8888.888f, "测试单浮点数，没有其他作用"));
//		distant.add(new InputLong("分布数据实时流量", 788823, "测试长整数而已，没有其他作用"));
//		distant.add(new InputString("大规则实时并行计算1", "不知道", "也许知道呢？"));
//		distant.add(new InputString("大规则实时并行计算2", "不知道", "也许知道呢？"));
//		distant.add(new InputString("大规则实时并行计算3", "不知道", "也许知道呢？"));
//		distant.add(new InputString("大规则实时并行计算4", "不知道", "也许知道呢？"));
//		distant.add(new InputString("大规则实时并行计算5", "不知道", "也许知道呢？"));
//		distant.add(new InputDouble("检测双浮点数", 2222.222 , "按照标准浮点数格式输入"));	
//		distant.add(new InputDate("日期检测", SimpleDate.format() , "按照标准日期格式输入"));
//		distant.add(new InputTime("时间检测", SimpleTime.format() , "按照标准时间格式输入"));
//		distant.add(new InputTimestamp("日期时间检测", SimpleTimestamp.format() , "按照标准日期/时间戳格式输入"));
//		distant.add(new InputBoolean("支持GUP", true , "支持不支持是你的事"));

		InputString title = new InputString(FT.inputTitle, "结果", "打印结果的标题");
		InputInteger width = new InputInteger(FT.inputWidth, 788, "打印结果的表格宽度，宽度过小可能不能正确显示");
		title.setSelect(true); // 必选项
		width.setSelect(true); // 必选项
		
		InputParameterUnit near = new InputParameterUnit(PhaseTag.NEAR);
		near.add(title);
		near.add(width);

		InputParameterList list = new InputParameterList();
		list.add(distant);
		list.add(near);

		return list;
	}

	/**
	 * 建立DISTANT对象实例
	 * @param sock
	 * @param list
	 * @return
	 */
	private DistantObject createDistant(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.DISTANT);
		InputInteger sites = (InputInteger) unit.find(FT.inputSites);

		Phase phase = new Phase(PhaseTag.DISTANT, sock);
		DistantInputter inputter = new DistantInputter(DistantMode.EVALUATE, phase);
		inputter.setSites(sites.getValue());

		DistantObject object = new DistantObject(inputter.getMode(), phase);
		object.setInputter(inputter);
		return object;
	}

	/**
	 * 建立NEAR对象实例
	 * @param sock
	 * @param list
	 * @return
	 */
	private NearObject createNear(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.NEAR);
		InputInteger width = (InputInteger) unit.find(FT.inputWidth);
		InputString title = (InputString) unit.find(FT.inputTitle);

		Phase phase = new Phase(PhaseTag.NEAR, sock);
		NearObject object = new NearObject(phase);

		// 改名称，对应Near阶段
		object.addString(FT.nearTitle, title.getValue());
		object.addInteger(FT.nearWidth, width.getValue());
		return object;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#create(com.laxcus.util.naming.Sock, com.laxcus.task.guide.parameter.InputParameterList)
	 */
	@Override
	public DistributedCommand create(Sock sock, InputParameterList list) throws GuideTaskException {
		if (!isSupport(sock)) {
			throw new GuideTaskException("illegal sock: %s", sock);
		}

		Contact cmd = new Contact(sock);
		cmd.setDistantObject(createDistant(sock, list));
		cmd.setNearObject(createNear(sock, list));

		// 生成一个默认的初始化阶段命名
		Phase phase = new Phase(PhaseTag.FORK, sock);
		cmd.setForkObject(new ForkObject(phase));

		return cmd;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#next(byte[], com.laxcus.util.naming.Sock)
	 */
	@Override
	public DistributedCommand next(byte[] predata, Sock sock)
		throws GuideTaskException {
		return null;
	}

}
