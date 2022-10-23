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
public class MinerGuideTask extends GuideTask {

	/**
	 * 分布打印引导任务
	 */
	public MinerGuideTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#getSocks()
	 */
	@Override
	public List<Sock> getSocks() {
		ArrayList<Sock> array = new ArrayList<Sock>();
		array.add(MinerFT.sock);
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

	private InputParameterUnit createInputDistant() {
		InputParameterUnit unit = new InputParameterUnit(PhaseTag.DISTANT);
		unit.add(new InputInteger(MinerFT.inputDistantSites, 20));
		unit.add(new InputBoolean(MinerFT.inputDistantGPU, true, "启用GPU计算" ));
		unit.add(new InputString(MinerFT.inputDistantPrefix, "分布式散列码值", "输入任何字符串，然后基于这个字符串进行散列计算"));
		unit.add(new InputInteger(MinerFT.inputDistantZeros, 2, "前置0"));
		unit.add(new InputLong(MinerFT.inputDistantBegin, 1000000, "开始位"));
		unit.add(new InputLong(MinerFT.inputDistantEnd, 2000000, "结束位"));
		// 全部选中
		for(InputParameter e : unit.list()) {
			e.setSelect(true);
		}
		return unit;
	}
	

	private InputParameterUnit createInputNcear() {
		//		InputInteger sha256 = new InputInteger(MinerFT.codeWidth, 300, "散列码（SHA256）显示宽度");
		//		InputInteger text = new InputInteger(MinerFT.textWidth, 300, "明文字符串显示宽度");
		//		InputInteger site = new InputInteger(MinerFT.siteWidt, 300, "计算节点显示宽度");
		//		sha256.setSelect(true); // 必选项
		//		text.setSelect(true); // 必选项
		//		site.setSelect(true); // 必选项
		//		near.add(sha256);
		//		near.add(text);
		//		near.add(site);

		InputParameterUnit near = new InputParameterUnit(PhaseTag.NEAR);
		near.add(new InputInteger(MinerFT.codeWidth, 300, "散列码（SHA256）显示宽度"));
		near.add(new InputInteger(MinerFT.textWidth, 300, "明文字符串显示宽度"));
		near.add(new InputInteger(MinerFT.siteWidth, 300, "散列节点显示宽度"));
		// 全部选中
		for(InputParameter e : near.list()) {
			e.setSelect(true);
		}
		return near;
	}

//	private InputParameterUnit createInputNcear() {
//		//		InputParameterUnit unit = new InputParameterUnit(PhaseTag.NEAR);
//		//		unit.add(new InputInteger(MineFT.inputToSites, 2));
//		//		// 全部选中
//		//		for(InputParameter e : unit.list()) {
//		//			e.setSelect(true);
//		//		}
//		//		return unit;
//
//		InputString title = new InputString(MinerFT.inputTitle, "结果", "打印结果的标题");
//		InputInteger width = new InputInteger(MinerFT.inputWidth, 788, "打印结果的表格宽度，宽度过小可能不能正确显示");
//		title.setSelect(true); // 必选项
//		width.setSelect(true); // 必选项
//
//		InputParameterUnit near = new InputParameterUnit(PhaseTag.NEAR);
//		near.add(title);
//		near.add(width);
//		return near;
//	}

	
	//	private InputParameterUnit createInputTo() {
	//		InputParameterUnit unit = new InputParameterUnit(PhaseTag.TO);
	//		unit.add(new InputInteger(MinerFT.inputToSites, 2));
	//		// 全部选中
	//		for(InputParameter e : unit.list()) {
	//			e.setSelect(true);
	//		}
	//		return unit;
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#markup(com.laxcus.util.naming.Sock)
	 */
	@Override
	public InputParameterList markup(Sock sock) throws GuideTaskException {
		if (!isSupport(sock)) {
			throw new GuideTaskException("illegal sock: %s", sock);
		}

		InputParameterList list = new InputParameterList();
		list.add(createInputDistant());
		list.add(createInputNcear());
		return list;

		//		InputInteger sites = new InputInteger(MinerFT.inputDistantSites, 1, "Distant阶段节点数量，运行过程中自动适配！");
		//		sites.setSelect(true); // 必选项!
		//		
		//		InputParameterUnit distant = new InputParameterUnit(PhaseTag.DISTANT);
		//		distant.add(sites);
		//		
		////		distant.add(new InputDouble("分布计算规则", 12.23f, "测试双浮点数，没有其他作用"));
		////		distant.add(new InputFloat("分布存储规则", 8888.888f, "测试单浮点数，没有其他作用"));
		////		distant.add(new InputLong("分布数据实时流量", 788823, "测试长整数而已，没有其他作用"));
		////		distant.add(new InputString("大规则实时并行计算1", "不知道", "也许知道呢？"));
		////		distant.add(new InputString("大规则实时并行计算2", "不知道", "也许知道呢？"));
		////		distant.add(new InputString("大规则实时并行计算3", "不知道", "也许知道呢？"));
		////		distant.add(new InputString("大规则实时并行计算4", "不知道", "也许知道呢？"));
		////		distant.add(new InputString("大规则实时并行计算5", "不知道", "也许知道呢？"));
		////		distant.add(new InputDouble("检测双浮点数", 2222.222 , "按照标准浮点数格式输入"));	
		////		distant.add(new InputDate("日期检测", SimpleDate.format() , "按照标准日期格式输入"));
		////		distant.add(new InputTime("时间检测", SimpleTime.format() , "按照标准时间格式输入"));
		////		distant.add(new InputTimestamp("日期时间检测", SimpleTimestamp.format() , "按照标准日期/时间戳格式输入"));
		////		distant.add(new InputBoolean("支持GUP", true , "支持不支持是你的事"));
		//
		//		InputString title = new InputString(MinerFT.inputTitle, "结果", "打印结果的标题");
		//		InputInteger width = new InputInteger(MinerFT.inputWidth, 788, "打印结果的表格宽度，宽度过小可能不能正确显示");
		//		title.setSelect(true); // 必选项
		//		width.setSelect(true); // 必选项
		//		
		//		InputParameterUnit near = new InputParameterUnit(PhaseTag.NEAR);
		//		near.add(title);
		//		near.add(width);
		//
		//		InputParameterList list = new InputParameterList();
		//		list.add(distant);
		//		list.add(near);
		//
		//		return list;
	}

	/**
	 * 建立DISTANT对象实例
	 * @param sock
	 * @param list
	 * @return
	 */
	private DistantObject createDistant(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.DISTANT);
		InputInteger sites = (InputInteger) unit.find(MinerFT.inputDistantSites);

		InputBoolean gpu = (InputBoolean)unit.find(MinerFT.inputDistantGPU);
		InputString prefix = (InputString)unit.find(MinerFT.inputDistantPrefix);
		InputInteger zeros = (InputInteger)unit.find(MinerFT.inputDistantZeros);
		InputLong begin = (InputLong)unit.find(MinerFT.inputDistantBegin);
		InputLong end = (InputLong)unit.find(MinerFT.inputDistantEnd);

		Phase phase = new Phase(PhaseTag.DISTANT, sock);
		DistantInputter inputter = new DistantInputter(DistantMode.GENERATE, phase);
		inputter.setSites(sites.getValue());

		inputter.setSites(sites.getValue());
		inputter.addBoolean("GPU", gpu.getValue());
		inputter.addString("PREFIX", prefix.getValue());
		inputter.addInteger("ZEROS", (zeros.getValue() < 1 ? 1 : zeros.getValue()));
		inputter.addLong("BEGIN", (begin.getValue() < 1 ? 0 : begin.getValue()));
		inputter.addLong("END", (end.getValue() < 10000 ? 10000 : end.getValue()));

		DistantObject object = new DistantObject(inputter.getMode(), phase);
		object.setInputter(inputter);
		return object;
	}

//	/**
//	 * 建立NEAR对象实例
//	 * @param sock
//	 * @param list
//	 * @return
//	 */
//	private NearObject createNear(Sock sock, InputParameterList list) {
//		InputParameterUnit unit = list.find(PhaseTag.NEAR);
//		InputInteger width = (InputInteger) unit.find(MinerFT.inputWidth);
//		InputString title = (InputString) unit.find(MinerFT.inputTitle);
//
//		Phase phase = new Phase(PhaseTag.NEAR, sock);
//		NearObject object = new NearObject(phase);
//
//		// 改名称，对应Near阶段
//		object.addString(MinerFT.nearTitle, title.getValue());
//		object.addInteger(MinerFT.nearWidth, width.getValue());
//		return object;
//	}
	
	/**
	 * 建立NEAR对象实例
	 * @param sock
	 * @param list
	 * @return
	 */
	private NearObject createNear(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.NEAR);
		InputInteger sha256 = (InputInteger) unit.find(MinerFT.codeWidth);
		InputInteger text = (InputInteger) unit.find(MinerFT.textWidth);
		InputInteger site = (InputInteger) unit.find(MinerFT.siteWidth);

		Phase phase = new Phase(PhaseTag.NEAR, sock);
		NearObject object = new NearObject(phase);

		// 改名称，对应Near阶段
		object.addInteger(MinerFT.codeWidthTitle, sha256.getValue());
		object.addInteger(MinerFT.textWidthTitle, text.getValue());
		object.addInteger(MinerFT.siteWidthTitle, site.getValue());
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
	public DistributedCommand next(byte[] predata, Sock sock) throws GuideTaskException {
		return null;
	}

}