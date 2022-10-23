/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得所签名匹配的工作节点地址。<br><br>
 * 工作节点包括DATA/WORK/BUILD/CALL节点。<br>
 * 
 * 命令从GATE/ACCOUNT节点发出，通过BANK/TOP节点，发送到HOME节点。
 * 
 * @author scott.liang
 * @version 1.0 3/17/2020
 * @since laxcus 1.0
 */
public class TakeJobSite extends Command {
	
	private static final long serialVersionUID = 9113304056351610285L;

	/** 用户签名 **/
	private Siger username;

	/**
	 * 构造默认和私有的获取工作节点地址
	 */
	public TakeJobSite() {
		super();
	}

	/**
	 * 构造获取工作节点地址，指定用户签名
	 * @param username 用户签名
	 */
	public TakeJobSite(Siger username) {
		super();
		setUsername(username);
	}

	/**
	 * 从可类化数据读取器解析获取工作节点地址
	 * @param reader 可类化读取器
	 */
	public TakeJobSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成获取工作节点地址副本
	 * @param that 获取工作节点地址
	 */
	private TakeJobSite(TakeJobSite that) {
		super(that);
		username = that.username;
	}
	
	/**
	 * 设置用户签名，允许空指针
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		username = e;
	}
	
	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeJobSite duplicate() {
		return new TakeJobSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(username);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		username = reader.readInstance(Siger.class);
	}

}