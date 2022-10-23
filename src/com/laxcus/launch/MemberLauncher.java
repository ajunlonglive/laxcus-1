/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import org.w3c.dom.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.xml.*;

/**
 * 成员站点启动器
 * 
 * @author scott.liang
 * @version 1.0 10/27/2019
 * @since laxcus 1.0
 */
public abstract class MemberLauncher extends SlaveLauncher {

	/** 用户虚拟空间 **/
	private MemberCyber memberCyber = new MemberCyber();
	
	/**
	 * 构造成员站点启动器
	 * @param printer 日志打印器，或者空打针
	 */
	protected MemberLauncher(LogPrinter printer) {
		super(printer);
		
		// 默认不限制用户数
		setMaxPersons(0);
		setMemberThreshold(0.0f);
	}

	/**
	 * 构造成员站点启动器
	 */
	protected MemberLauncher() {
		this(null);
	}

	/**
	 * 返回成员虚拟空间
	 * @return MemberCyber实例
	 */
	public final MemberCyber getMemberCyber() {
		return memberCyber;
	}

	/**
	 * 设置可以支持的最多用户数目。<br>
	 * 在集群里，因为计算机性能的限制，包括：内存/CPU/磁盘，每个节点只能支持有限人数。设置这个参数加以限制。<br>
	 * 这个方法针对人员注册使用的节点，包括：ACCOUNT/GATE/CALL/DATA/WORK/BUILD。<br>
	 * 
	 * @param more 用户数目
	 */
	public void setMaxPersons(int more) {
		memberCyber.setPersons(more);
	}
	
	/**
	 * 返回可以支持的最多用户数目
	 * @return 整数
	 */
	public int getMaxPersons() {
		return memberCyber.getPersons();
	}
	
	/**
	 * 用户阀值
	 * @param f
	 */
	public void setMemberThreshold(double f) {
		memberCyber.setThreshold(f);
	}

	/**
	 * 返回阀值
	 * @return
	 */
	public double getMemberThreshold() {
		return memberCyber.getThreshold();
	}

	/**
	 * 解析成员虚拟空间
	 * @param document
	 */
	protected void splitMemberCyber(Document document) {
		Element root = (Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);

		// 管理节点规定子节点的一般延时注册时间，只针对TOP/HOME/BANK三类管理节点

		// 允许最大用户数
		String input = XMLocal.getValue(root, SiteMark.MAX_PERSONS);
		memberCyber.setPersons(ConfigParser.splitInteger(input, memberCyber.getPersons()));
		// 最大阀值
		input = XMLocal.getAttribute(root, SiteMark.MAX_PERSONS, SiteMark.MAX_PERSONS_THRESHOLD);
		memberCyber.setThreshold(ConfigParser.splitRate(input, memberCyber.getThreshold()));
		// 超时时间
		input = XMLocal.getAttribute(root, SiteMark.MAX_PERSONS, SiteMark.MAX_PERSONS_CHECKTIMEOUT);
		memberCyber.setTimeout(ConfigParser.splitTime(input, memberCyber.getTimeout()));

		Logger.info(this, "splitMemberCyber", "max persons: %d, threshold: %.2f, check timeout: %d ms",
				memberCyber.getPersons(), memberCyber.getThreshold(), memberCyber.getTimeout());
	}
}