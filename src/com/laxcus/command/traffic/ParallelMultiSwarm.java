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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 并行流量测试命令。<br>
 * 同时在两个节点之间并行处理多个流量测试。
 * 
 * @author scott.liang
 * @version 1.0 10/4/2018
 * @since laxcus 1.0
 */
public class ParallelMultiSwarm extends Command {

	private static final long serialVersionUID = 6281151948538252536L;

	/** 发送次数 **/
	private int iterate;

	/** 数据传输流量 **/
	private MultiSwarm swarm;

	/**
	 * 构造默认的并行流量测试命
	 */
	public ParallelMultiSwarm() {
		super();
	}

	/**
	 * 构造命令，指定参数
	 * @param count 发送次数
	 * @param cmd 数据传输流量
	 */
	public ParallelMultiSwarm(int count, MultiSwarm cmd) {
		this();
		setIterate(count);
		setMultiSwarm(cmd);
	}

	/**
	 * 解析并行流量测试命命令
	 * @param reader 可类化读取器
	 */
	public ParallelMultiSwarm(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成数据流量检测命令副本
	 * @param that 并行流量测试命命令
	 */
	private ParallelMultiSwarm(ParallelMultiSwarm that) {
		super(that);
		iterate = that.iterate;
		swarm = that.swarm;
	}

	/**
	 * 设置发送次数
	 * @param n
	 */
	public void setIterate(int n) {
		if (n < 0) {
			throw new IllegalValueException("illegal argument:%d", n);
		}
		iterate = n;
	}

	/**
	 * 返回发送次数
	 * @return
	 */
	public int getIterate() {
		return iterate;
	}

	/**
	 * 设置数据传输流量
	 * @param e
	 */
	public void setMultiSwarm(MultiSwarm e) {
		Laxkit.nullabled(e);
		swarm = e;
	}

	/**
	 * 返回数据传输流量
	 * @return 数据传输流量
	 */
	public MultiSwarm getMultiSwarm() {
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
	public ParallelMultiSwarm duplicate() {
		return new ParallelMultiSwarm(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(iterate);
		writer.writeObject(swarm);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		iterate = reader.readInt();
		swarm = new MultiSwarm(reader);
	}

}