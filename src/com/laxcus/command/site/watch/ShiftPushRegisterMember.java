/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;

/**
 * 转发注册用户命令
 * 
 * @author scott.liang
 * @version 1.0 1/13/2020
 * @since laxcus 1.0
 */
public class ShiftPushRegisterMember extends ShiftCommand {

	private static final long serialVersionUID = 3358387687839045668L;
	
	/** 用户签名 */
	private ArrayList<Siger> array = new ArrayList<Siger>();

	/**
	 * 构造默认的转发注册用户命令
	 */
	public ShiftPushRegisterMember() {
		super();
	}

	/**
	 * 构造转发注册用户命令，指定用户签名
	 * @param siger 用户签名
	 */
	public ShiftPushRegisterMember(Collection<Siger> a) {
		super();
		addAll(a);
	}

	/**
	 * 生成转发注册用户命令副本
	 * @param that 转发注册用户命令
	 */
	public ShiftPushRegisterMember(ShiftPushRegisterMember that) {
		super(that);
		array.addAll(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftPushRegisterMember duplicate() {
		return new ShiftPushRegisterMember(this);
	}

	/**
	 * 保存签名集合
	 * @param a 签名集合
	 */
	public void addAll(Collection<Siger> a) {
		Laxkit.nullabled(a);
		array.addAll(a);
	}

	/**
	 * 输出签名集合
	 * @return 签名集合
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计成员数
	 * @return 整数
	 */
	public int size() {
		return array.size();
	}

}