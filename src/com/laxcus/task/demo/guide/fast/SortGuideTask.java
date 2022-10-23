/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.guide.fast;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.task.guide.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.util.naming.*;

/**
 * 快速排序引导程序
 * 
 * @author scott.liang
 * @version 1.0 8/1/2020
 * @since laxcus 1.0
 */
public class SortGuideTask extends GuideTask {
	
	/**
	 * 构造快速排序引导程序
	 */
	public SortGuideTask(){
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#getSocks()
	 */
	@Override
	public List<Sock> getSocks() {
		ArrayList<Sock> array = new ArrayList<Sock>();
		array.add(SortFT.sock);
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
	
	private InputParameterUnit createInputFrom() {
		InputParameterUnit unit = new InputParameterUnit(PhaseTag.FROM);
		unit.add(new InputInteger(SortFT.inputFromSites, 2));
		unit.add(new InputInteger(SortFT.inputFromBegin, 0, "数字初始值"));
		unit.add(new InputInteger(SortFT.inputFromEnd, 1000000, "数字结束值"));
		unit.add(new InputInteger(SortFT.inputFromTotal, 100, "产生的数字数目"));
		// 全部选中
		for(InputParameter e : unit.list()) {
			e.setSelect(true);
		}
		return unit;
	}
	
	private InputParameterUnit createInputTo() {
		InputParameterUnit unit = new InputParameterUnit(PhaseTag.TO);
		unit.add(new InputInteger(SortFT.inputToSites, 2));
		unit.add(new InputBoolean(SortFT.inputToAlign, true , "采用升序排序"));
		
		// 全部选中
		for(InputParameter e : unit.list()) {
			e.setSelect(true);
		}
		return unit;
	}
	
	private InputParameterUnit createInputPut() {
		InputParameterUnit unit = new InputParameterUnit(PhaseTag.PUT);
		unit.add(new InputString(SortFT.inputPutWriteto, "" , "结束数据将写入这个文件"));
		// 全部选中
		for(InputParameter e : unit.list()) {
			e.setSelect(true);
		}
		return unit;
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

		InputParameterList list = new InputParameterList();
		list.add(createInputFrom());
		list.add(createInputTo());
		list.add(createInputPut());
		return list;
	}
	
	/**
	 * 建立FROM阶段对象
	 * @param sock
	 * @param list
	 * @return
	 */
	private FromObject createFromObject(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.FROM);
		InputInteger sites = (InputInteger) unit.find(SortFT.inputFromSites);
		InputInteger total = (InputInteger)unit.find(SortFT.inputFromTotal);
		InputInteger begin = (InputInteger)unit.find(SortFT.inputFromBegin);
		InputInteger end = (InputInteger)unit.find(SortFT.inputFromEnd);
		
		Phase phase = new Phase(PhaseTag.FROM, sock);
		
		FromInputter inputter = new FromInputter(phase);
		inputter.setSites(sites.getValue());
		inputter.addInteger("total", (total.getValue() < 100 ? 100 : total.getValue()));
		inputter.addInteger("begin", begin.getValue());
		inputter.addInteger("end", end.getValue());

		FromObject object = new FromObject(phase);
		object.addInputter(inputter);
		return object;
	}
	
	/**
	 * 建立TO阶段对象
	 * @param sock
	 * @param list
	 * @return
	 */
	private ToObject createToObject(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.TO);
		InputInteger sites = (InputInteger) unit.find(SortFT.inputToSites);
		InputBoolean align = (InputBoolean)unit.find(SortFT.inputToAlign);

		Phase phase = new Phase(PhaseTag.TO, sock);

		// 指定ToMode.EVALUATE计算模式
		ToInputter inputter = new ToInputter(ToMode.EVALUATE, phase);
		inputter.setSites(sites.getValue());
		inputter.addString("orderby", (align.getValue() ? "asc" : "desc"));

		ToObject object = new ToObject(inputter.getMode(), phase);
		object.setInputter(inputter);
		return object;
	}

	private PutObject createPutObject(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.PUT);
		InputString write = (InputString) unit.find(SortFT.inputPutWriteto);

		Phase phase = new Phase(PhaseTag.PUT, sock);
		PutObject object = new PutObject(phase);
		object.setWriteTo(write.getValue());
		return object;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#create(com.laxcus.util.naming.Sock, com.laxcus.task.guide.parameter.InputParameterList)
	 */
	@Override
	public DistributedCommand create(Sock sock, InputParameterList list)
			throws GuideTaskException {
		if (!isSupport(sock)) {
			throw new GuideTaskException("illegal sock: %s", sock);
		}
		
		Conduct cmd = new Conduct(sock);

		cmd.setFromObject(createFromObject(sock, list));
		cmd.setToObject(createToObject(sock, list));
		cmd.setPutObject(createPutObject(sock, list));

		// 生成一个默认的初始化阶段命名
		Phase phase = new Phase(PhaseTag.INIT, sock);
		cmd.setInitObject(new InitObject(phase));

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
