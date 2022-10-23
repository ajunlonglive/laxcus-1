/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 刷新一个或者几个账号的元数据 <br><br>
 * 
 * 流程：WATCH -> TOP/HOME -> DATA/BUILD/WORK -> CALL <br>
 * 
 * 命令由WATCH站点发出，经过TOP/HOME站点根据用户签名筛选，发送“SelectFieldToCall”命令到关联的DATA/BUILD/WORK站点。
 * DATA/BUILD/WORK站点向关联的CALL站点提交的自己资源元数据（PushField子命令）。
 * 
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public final class RefreshMetadata extends RefreshResource {
	
	private static final long serialVersionUID = -3550955564127190298L;

	/**
	 * 根据传入的刷新元数据命令，生成它的数据副本
	 * @param that 刷新元数据命令
	 */
	private RefreshMetadata(RefreshMetadata that) {
		super(that);
	}

	/**
	 * 构造刷新元数据命令
	 */
	public RefreshMetadata() {
		super();
	}
	
	/**
	 * 构造刷新元数据命令，指定用户签名
	 * @param siger 用户签名
	 */
	public RefreshMetadata(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析刷新元数据命令
	 * @param reader 可类化数据读取器
	 */
	public RefreshMetadata(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RefreshMetadata duplicate() {
		return new RefreshMetadata(this);
	}

}