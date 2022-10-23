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
 * WATCH节点发出，测试集群其他任意两个节点之间的并行处理流量。
 * 
 * @author scott.liang
 * @version 1.0 10/5/2018
 * @since laxcus 1.0
 */
public class ParallelMultiGust extends Command {

	private static final long serialVersionUID = 6281151948538252536L;

	/** 发送次数 **/
	private int iterate;

	/** 数据传输流量 **/
	private MultiGust gust;

	/**
	 * 构造默认的并行流量测试命
	 */
	public ParallelMultiGust() {
		super();
	}

	/**
	 * 构造命令，指定参数
	 * @param count 发送次数
	 * @param cmd 数据传输流量
	 */
	public ParallelMultiGust(int count, MultiGust cmd) {
		this();
		setIterate(count);
		setMultiGust(cmd);
	}

	/**
	 * 解析并行流量测试命命令
	 * @param reader 可类化读取器
	 */
	public ParallelMultiGust(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成数据流量检测命令副本
	 * @param that 并行流量测试命命令
	 */
	private ParallelMultiGust(ParallelMultiGust that) {
		super(that);
		iterate = that.iterate;
		gust = that.gust;
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
	public void setMultiGust(MultiGust e) {
		Laxkit.nullabled(e);
		gust = e;
	}

	/**
	 * 返回数据传输流量
	 * @return 数据传输流量
	 */
	public MultiGust getMultiGust() {
		return gust;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setMemory(boolean)
	 */
	@Override
	public void setMemory(boolean b) {
		super.setMemory(b);
		if (gust != null) {
			gust.setMemory(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setDisk(boolean)
	 */
	@Override
	public void setDisk(boolean b) {
		super.setDisk(b);
		if (gust != null) {
			gust.setDisk(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setDirect(boolean)
	 */
	@Override
	public void setDirect(boolean b) {
		super.setDirect(b);
		if (gust != null) {
			gust.setDirect(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setReply(boolean)
	 */
	@Override
	public void setReply(boolean b) {
		super.setReply(b);
		if (gust != null) {
			gust.setReply(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger e) {
		super.setIssuer(e);
		if (gust != null) {
			gust.setIssuer(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setPriority(byte)
	 */
	@Override
	public void setPriority(byte no) {
		super.setPriority(no);
		if (gust != null) {
			gust.setPriority(no);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setQuick(boolean)
	 */
	@Override
	public void setQuick(boolean b) {
		super.setQuick(b);
		if (gust != null) {
			gust.setQuick(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setFast(boolean)
	 */
	@Override
	public void setFast(boolean b) {
		super.setFast(b);
		if (gust != null) {
			gust.setFast(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setLocalId(long)
	 */
	@Override
	public void setLocalId(long invokerId) {
		super.setLocalId(invokerId);
		if (gust != null) {
			gust.setLocalId(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setRelateId(long)
	 */
	@Override
	public void setRelateId(long invokerId) {
		super.setRelateId(invokerId);
		if (gust != null) {
			gust.setRelateId(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setSource(com.laxcus.echo.Cabin)
	 */
	@Override
	public void setSource(Cabin e) {
		super.setSource(e);
		if (gust != null) {
			gust.setSource(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.swarm.Command#setTimeout(long)
	 */
	@Override
	public void setTimeout(long ms) {
		super.setTimeout(ms);
		if (gust != null) {
			gust.setTimeout(ms);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ParallelMultiGust duplicate() {
		return new ParallelMultiGust(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(iterate);
		writer.writeObject(gust);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		iterate = reader.readInt();
		gust = new MultiGust(reader);
	}

}