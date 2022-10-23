/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 授权建立用户账号转发命令
 * 
 * @author scott.liang
 * @version 1.0 6/10/2018
 * @since laxcus 1.0
 */
public class ShiftAwardCreateAccount extends ShiftCommand {
	
	private static final long serialVersionUID = -4772517585140940673L;

	/** 用户签名 **/
	private Siger siger;
	
	/** 目标地址，固定是GATE站点 **/
	private Node remote;

	/**
	 * 构造默认的授权建立用户账号转发命令
	 */
	public ShiftAwardCreateAccount() {
		super();
	}

	/**
	 * 生成授权建立用户账号转发命令的数据副本
	 * @param cmd
	 */
	private ShiftAwardCreateAccount(ShiftAwardCreateAccount that) {
		super(that);
		siger = that.siger;
		remote = that.remote;
	}

	/**
	 * 构造授权建立用户账号转发命令，指定目标地址和用户签名
	 * @param remote 目标地址
	 * @param siger 用户签名
	 */
	public ShiftAwardCreateAccount( Node remote, Siger siger ) {
		this();
		setRemote(remote);
		setSiger(siger);
	}
	
	/**
	 * 设置目标站点，固定是GATE站点
	 * @param e 站点地址
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);
		remote = e;
	}
	
	/**
	 * 返回目标站点，固定是GATE站点
	 * @return 站点地址
	 */
	public Node getRemote(){
		return remote;
	}
	
	/**
	 * 设置用户签名
	 * @param e
	 */
	public void setSiger(Siger e){
		Laxkit.nullabled(e);
		siger = e;
	}
	
	/**
	 * 返回用户签名
	 * @return
	 */
	public Siger getSiger(){
		return siger;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftAwardCreateAccount duplicate() {
		return new ShiftAwardCreateAccount(this);
	}

}