/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.hit;

import java.lang.reflect.*;

import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hit.*;

/**
 * HitVisit接口的远程访问客户端。<br><br>
 * 
 * HitClient与实现HitVisit接口服务器的站点通信。这些服务器包括TOP/HOME/BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 2/22/2009
 * @since laxcus 1.0
 */
public class HitClient extends RemoteClient implements HitVisit {

	private static Method methodIsManager;
	private static Method methodIsHubHitch;
	private static Method methodDiscuss;

	static {
		try {
			HitClient.methodIsManager = (HitVisit.class).getMethod("isManager", new Class<?>[0]);
			HitClient.methodIsHubHitch = (HitVisit.class).getMethod("isManagerDisabled", new Class<?>[] { Node.class });
			HitClient.methodDiscuss = (HitVisit.class).getMethod("discuss", new Class<?>[] { Node.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 * 构造HitVisit接口访问客户端，指定传输模式
	 * @param stream 流模式
	 */
	public HitClient(boolean stream) {
		super(stream);
		setVisitName(HitVisit.class.getName());
	}

	/**
	 * 构造HitVisit接口访问客户端，指定目标地址
	 * @param endpoint 实现HitVisit接口的服务器地址
	 */
	public HitClient(SocketHost endpoint) {
		this(endpoint.isStream());
		setRemote(endpoint);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hit.HitVisit#isManager()
	 */
	@Override
	public boolean isManager() throws VisitException {
		Object param = super.invoke(HitClient.methodIsManager, null);
		return ((Boolean) param).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hit.HitVisit#isManagerDisabled(com.laxcus.site.Node)
	 */
	@Override
	public boolean isManagerDisabled(Node hub) throws VisitException {
		Object[] params = new Object[] { hub };
		Object param = super.invoke(HitClient.methodIsHubHitch, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hit.HitVisit#discuss(com.laxcus.site.Node)
	 */
	@Override
	public boolean discuss(Node sponsor) throws VisitException {
		Object[] params = new Object[] { sponsor };
		Object param = super.invoke(HitClient.methodDiscuss, params);
		return ((Boolean) param).booleanValue();
	}

}