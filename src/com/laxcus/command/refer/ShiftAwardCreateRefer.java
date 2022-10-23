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
 * 建立用户资源引用转发命令
 * 
 * @author scott.liang
 * @version 1.0 6/10/2018
 * @since laxcus 1.0
 */
public class ShiftAwardCreateRefer extends ShiftCommand {
	
	private static final long serialVersionUID = -4772517585140940673L;

	/** 用户签名 **/
	private Siger siger;
	
	/** 目标地址 **/
	private Node remote;

	/**
	 * 构造默认的建立用户资源引用转发命令
	 */
	public ShiftAwardCreateRefer() {
		super();
	}

	/**
	 * 生成建立用户资源引用转发命令的数据副本
	 * @param cmd
	 */
	private ShiftAwardCreateRefer(ShiftAwardCreateRefer that) {
		super(that);
		siger = that.siger;
		remote = that.remote;
	}

	/**
	 * 构造建立用户资源引用转发命令，指定目标地址和用户签名
	 * @param remote 目标地址
	 * @param siger 用户签名
	 */
	public ShiftAwardCreateRefer( Node remote, Siger siger ) {
		this();
		setRemote(remote);
		setSiger(siger);
	}
	
	/**
	 * 设置目标站点
	 * @param e
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);
		remote = e;
	}
	
	/**
	 * 返回目标站点
	 * @return
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
	public ShiftAwardCreateRefer duplicate() {
		return new ShiftAwardCreateRefer(this);
	}

}