/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检测两个节点之间的数据传输流量。
 * 
 * @author scott.liang
 * @version 1.0 8/15/2018
 * @since laxcus 1.0
 */
public class Gust extends Command {

	private static final long serialVersionUID = 7491393897031556776L;

	/** 启始站点 **/
	private Node from;

	/** 流量测试命令 **/
	private Swarm swarm;

	/**
	 * 构造默认的检测两个节点之间的数据传输流量
	 */
	public Gust() {
		super();
	}

	/**
	 * 构造命令，指定参数
	 * @param from 发起站点
	 * @param cmd 流量测试命令
	 */
	public Gust(Node from, Swarm cmd) {
		this();
		setFrom(from);
		setSwarm(cmd);
	}

	/**
	 * 解析检测两个节点之间的数据传输流量命令
	 * @param reader 可类化读取器
	 */
	public Gust(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成数据流量检测命令副本
	 * @param that 检测两个节点之间的数据传输流量命令
	 */
	private Gust(Gust that) {
		super(that);
		from = that.from;
		swarm = that.swarm;
	}

	/**
	 * 设置发起地址
	 * @param e
	 */
	public void setFrom(Node e) {
		Laxkit.nullabled(e);
		from = e;
	}

	/**
	 * 返回发起地址
	 * @return
	 */
	public Node getFrom() {
		return from;
	}

	/**
	 * 设置流量测试命令
	 * @param e
	 */
	public void setSwarm(Swarm e) {
		Laxkit.nullabled(e);
		swarm = e;
	}

	/**
	 * 返回流量测试命令
	 * @return 流量测试命令
	 */
	public Swarm getSwarm() {
		return swarm;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setMemory(boolean)
	 */
	@Override
	public void setMemory(boolean b) {
		super.setMemory(b);
		if (swarm != null) {
			swarm.setMemory(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setDisk(boolean)
	 */
	@Override
	public void setDisk(boolean b) {
		super.setDisk(b);
		if (swarm != null) {
			swarm.setDisk(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setDirect(boolean)
	 */
	@Override
	public void setDirect(boolean b) {
		super.setDirect(b);
		if (swarm != null) {
			swarm.setDirect(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setReply(boolean)
	 */
	@Override
	public void setReply(boolean b) {
		super.setReply(b);
		if (swarm != null) {
			swarm.setReply(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger e) {
		super.setIssuer(e);
		if (swarm != null) {
			swarm.setIssuer(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setPriority(byte)
	 */
	@Override
	public void setPriority(byte no) {
		super.setPriority(no);
		if (swarm != null) {
			swarm.setPriority(no);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setQuick(boolean)
	 */
	@Override
	public void setQuick(boolean b) {
		super.setQuick(b);
		if (swarm != null) {
			swarm.setQuick(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setFast(boolean)
	 */
	@Override
	public void setFast(boolean b) {
		super.setFast(b);
		if (swarm != null) {
			swarm.setFast(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setLocalId(long)
	 */
	@Override
	public void setLocalId(long invokerId) {
		super.setLocalId(invokerId);
		if (swarm != null) {
			swarm.setLocalId(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setRelateId(long)
	 */
	@Override
	public void setRelateId(long invokerId) {
		super.setRelateId(invokerId);
		if (swarm != null) {
			swarm.setRelateId(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setSource(com.laxcus.echo.Cabin)
	 */
	@Override
	public void setSource(Cabin e) {
		super.setSource(e);
		if (swarm != null) {
			swarm.setSource(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setTimeout(long)
	 */
	@Override
	public void setTimeout(long ms) {
		super.setTimeout(ms);
		if (swarm != null) {
			swarm.setTimeout(ms);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Gust duplicate() {
		return new Gust(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(from);
		writer.writeObject(swarm);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		from = new Node(reader);
		swarm = new Swarm(reader);
	}

}
