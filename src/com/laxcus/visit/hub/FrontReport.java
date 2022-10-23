/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.hub;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT登录报告 <br>
 * 
 * FRONT执行登录请求，ENTRANCE/GATE返回这个报告。
 * 
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public final class FrontReport implements Classable, Cloneable, Serializable {

	private static final long serialVersionUID = 8122434962791583041L;

	/** 重定向站点地址 **/
	private Node redirect;

	/** 登录反馈状态 **/
	private int status;
	
	/**
	 * 构造默认和私有的FRONT登录报告
	 */
	private FrontReport() {
		super();
		status = 0;
	}
	
	/**
	 * 建立FRONT登录报告的数据副本
	 * @param that FrontReport实例
	 */
	private FrontReport(FrontReport that) {
		this();
		redirect = that.redirect;
		status = that.status;
	}

	/**
	 * 构造FRONT登录报告，指定登录反馈状态
	 * @param status 登录反馈状态
	 */
	public FrontReport(int status) {
		this();
		setStatus(status);
	}

	/**
	 * 构造FRONT登录报告，指定重定向站点地址
	 * @param site 站点地址
	 */
	public FrontReport(Node site) {
		this();
		setRedirect(site);
		setStatus(FrontStatus.REDIRECT);
	}

	/**
	 * 从可类化数据读取器中解析FRONT登录报告
	 * @param reader FRONT登录报告
	 */
	public FrontReport(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 判断发生重定向
	 * @return 返回真或者假
	 */
	public boolean isRedirect() {
		return redirect != null;
	}

	/**
	 * 设置重定向站点地址
	 * @param e Node实例
	 */
	public void setRedirect(Node e) {
		redirect = e;
	}

	/**
	 * 返回重定向站点地址
	 * @return Node实例
	 */
	public Node getRedirect() {
		return redirect;
	}

	/**
	 * 设置登录反馈状态
	 * @param who 登录反馈状态
	 */
	public void setStatus(int who) {
		if (!FrontStatus.isStatus(who)) {
			throw new IllegalValueException("illegal status:%d", who);
		}
		status = who;
	}

	/**
	 * 判断操作成功
	 * @return 返回真或者假
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * 判断是重定向
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public boolean isDirect() {
		return this.redirect != null && FrontStatus.isDirect(status);
	}
	
	/**
	 * 判断是逗留状态
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public boolean isLinger() {
		return FrontStatus.isLinger(status);
	}
	
	/**
	 * 判断是已经登录
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public boolean isLogined() {
		return FrontStatus.isLogined(status);
	}
	
	/**
	 * 判断是登录失败
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public boolean isFailed() {
		return FrontStatus.isFailed(status);
	}

	/**
	 * 达到最大用户登录数目
	 * @param who 登录状态
	 * @return 返回真或者假
	 */
	public boolean isMaxUser() {
		return FrontStatus.isMaxUser(status);
	}

	/**
	 * 判断是服务不足，是由服务端造成
	 * @return 返回真或者假
	 */
	public boolean isServiceMissing() {
		return FrontStatus.isServiceMissing(status);
	}

	/**
	 * 判断达到最大重试次数
	 * @return 返回真或者假
	 */
	public boolean isMaxRetry() {
		return FrontStatus.isMaxRetry(status);
	}

	/**
	 * 生成当前实例的数据副本
	 * @return FrontReport实例
	 */
	public FrontReport duplicate() {
		return new FrontReport(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInstance(redirect);
		writer.writeInt(status);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		redirect = reader.readInstance(Node.class);
		status = reader.readInt();
		return reader.getSeek() - seek;
	}

}