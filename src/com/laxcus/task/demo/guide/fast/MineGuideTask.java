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
 * 挖矿的引导命令
 * 
 * @author scott.liang
 * @version 1.0 7/31/2020
 * @since laxcus 1.0
 */
public class MineGuideTask extends GuideTask {

	/**
	 * 构造默认的引导命令
	 */
	public MineGuideTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.guide.GuideTask#getSocks()
	 */
	@Override
	public List<Sock> getSocks() {
		ArrayList<Sock> array = new ArrayList<Sock>();
		array.add(MineFT.sock);
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
		unit.add(new InputInteger(MineFT.inputFromSites, 2));
		unit.add(new InputBoolean(MineFT.inputFromGPU, true, "启用GPU计算" ));
		unit.add(new InputString(MineFT.inputFromPrefix, "LaxcusBigdata", "输入任何字符串，然后基于这个字符串进行挖掘"));
		unit.add(new InputInteger(MineFT.inputFromZeros, 2, "前置0"));
		unit.add(new InputLong(MineFT.inputFromBegin, 0, "开始位"));
		unit.add(new InputLong(MineFT.inputFromEnd, 1000000, "结束位"));
		// 全部选中
		for(InputParameter e : unit.list()) {
			e.setSelect(true);
		}
		return unit;
	}

	private InputParameterUnit createInputTo() {
		InputParameterUnit unit = new InputParameterUnit(PhaseTag.TO);
		unit.add(new InputInteger(MineFT.inputToSites, 2));
		// 全部选中
		for(InputParameter e : unit.list()) {
			e.setSelect(true);
		}
		return unit;
	}

	/**
	 * 建立FROM阶段对象
	 * @param sock
	 * @param list
	 * @return
	 */
	private FromObject createFromObject(Sock sock, InputParameterList list) {
		InputParameterUnit unit = list.find(PhaseTag.FROM);
		InputInteger sites = (InputInteger) unit.find(MineFT.inputFromSites);
		InputBoolean gpu = (InputBoolean)unit.find(MineFT.inputFromGPU);
		InputString prefix = (InputString)unit.find(MineFT.inputFromPrefix);
		InputInteger zeros = (InputInteger)unit.find(MineFT.inputFromZeros);
		InputLong begin = (InputLong)unit.find(MineFT.inputFromBegin);
		InputLong end = (InputLong)unit.find(MineFT.inputFromEnd);

		Phase phase = new Phase(PhaseTag.FROM, sock);

		FromInputter inputter = new FromInputter(phase);
		inputter.setSites(sites.getValue());
		inputter.addBoolean("GPU", gpu.getValue());
		inputter.addString("PREFIX", prefix.getValue());
		inputter.addInteger("ZEROS", (zeros.getValue() < 1 ? 1 : zeros.getValue()));
		inputter.addLong("BEGIN", (begin.getValue() < 1 ? 0 : begin.getValue()));
		inputter.addLong("END", (end.getValue() < 10000 ? 10000 : end.getValue()));

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
		InputInteger sites = (InputInteger) unit.find(MineFT.inputToSites);

		Phase phase = new Phase(PhaseTag.TO, sock);

		ToInputter inputter = new ToInputter(ToMode.EVALUATE, phase);
		inputter.setSites(sites.getValue());

		ToObject object = new ToObject(inputter.getMode(), phase);
		object.setInputter(inputter);
		return object;
	}

	/**
	 * 建立PUT阶段对象
	 * @param sock
	 * @param list
	 * @return
	 */
	private PutObject createPutObject(Sock sock,InputParameterList list) {
		Phase phase = new Phase(PhaseTag.PUT, sock);
		PutObject object = new PutObject(phase);

		object.addString(MineFT.putNodes , "挖掘节点");
		object.addString(MineFT.putText, "矿码明文");
		object.addString(MineFT.putSHA256, "矿码（SHA256）");
		return object;
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
		return list;
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