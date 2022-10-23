/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.hub;

import java.lang.reflect.*;

import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * HubVisit接口的远程访问客户端。<br><br>
 * 
 * HubClient与实现HubVisit接口服务器的站点通信。这些服务器包括TOP/HOME/AID/CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 8/16/2009
 * @since laxcus 1.0
 */
public class HubClient extends RemoteClient implements HubVisit {

	private static Method methodGetVersion;
	
	private static Method methodGetHubFamily;
	private static Method methodGetHub;
	private static Method methodCurrentTime;
	private static Method methodGetSiteTimeout;
	
	private static Method methodGetHubRegisterInterval;
	private static Method methodGetHubMaxRegisterInterval;
	
	private static Method methodFindLogSite;
	private static Method methodFindTigSite;
	private static Method methodFindBillSite;

	private static Method methodLogin;
	private static Method methodLogout;

	static {
		try {
			methodGetVersion = (HubVisit.class).getMethod("getVersion", new Class<?>[0]);
			
			methodGetHubFamily = (HubVisit.class).getMethod("getHubFamily", new Class<?>[0]);
			methodGetHub = (HubVisit.class).getMethod("getHub", new Class<?>[0]);
			methodCurrentTime = (HubVisit.class).getMethod("currentTime", new Class<?>[0]);
			methodGetSiteTimeout = (HubVisit.class).getMethod("getSiteTimeout", new Class<?>[] { Byte.TYPE });
			methodGetHubRegisterInterval = (HubVisit.class).getMethod("getHubRegisterInterval", new Class<?>[0]);
			methodGetHubMaxRegisterInterval = (HubVisit.class).getMethod("getHubMaxRegisterInterval", new Class<?>[0]);
			
			methodFindLogSite = (HubVisit.class).getMethod("findLogSite", new Class<?>[] { Byte.TYPE });
			methodFindTigSite = (HubVisit.class).getMethod("findTigSite", new Class<?>[] { Byte.TYPE });
			methodFindBillSite = (HubVisit.class).getMethod("findBillSite", new Class<?>[] { Byte.TYPE });
			
			methodLogin = (HubVisit.class).getMethod("login", new Class<?>[] { Site.class });
			methodLogout = (HubVisit.class).getMethod("logout", new Class<?>[] { Node.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 * 构造HubVisit接口的访问客户端，指定传输模式
	 * @param stream 流模式
	 */
	public HubClient(boolean stream) {
		super(stream);
		super.setVisitName(HubVisit.class.getName());
	}

	/**
	 * 构造HubVisit接口访问客户端，指定目标地址。
	 * @param endpoint 实现HubVisit接口的站点地址。
	 */
	public HubClient(SocketHost endpoint) {
		this(endpoint.isStream());
		super.setRemote(endpoint);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		Object param = super.invoke(HubClient.methodGetVersion, null);
		return ((Version) param);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubFamily()
	 */
	@Override
	public byte getHubFamily() throws VisitException {
		Object param = super.invoke(HubClient.methodGetHubFamily, null);
		return ((Byte) param).byteValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubFamily()
	 */
	@Override
	public Node getHub() throws VisitException {
		Object param = super.invoke(HubClient.methodGetHub, null);
		return (Node) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#currentTime()
	 */
	@Override
	public long currentTime() throws VisitException {
		Object param = super.invoke(HubClient.methodCurrentTime, null);
		return ((Long) param).longValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getSiteTimeout(byte)
	 */
	@Override
	public long getSiteTimeout(byte siteFamily) throws VisitException {
		Object[] params = new Object[] { new Byte(siteFamily) };
		Object param = super.invoke(HubClient.methodGetSiteTimeout, params);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getLoginInterval()
	 */
	@Override
	public long getHubRegisterInterval() throws VisitException {
		Object param = super.invoke(HubClient.methodGetHubRegisterInterval, null);
		return ((Long) param).longValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#getHubMaxRegisterInterval()
	 */
	@Override
	public long getHubMaxRegisterInterval() throws VisitException {
		Object param = super.invoke(HubClient.methodGetHubMaxRegisterInterval, null);
		return ((Long) param).longValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findLogSite(byte)
	 */
	@Override
	public Node findLogSite(byte siteFamily) throws VisitException {
		Object[] params = new Object[] { new Byte(siteFamily) };
		Object param = super.invoke(HubClient.methodFindLogSite, params);
		return (Node) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findTigSite(byte)
	 */
	@Override
	public Node findTigSite(byte siteFamily) throws VisitException {
		Object[] params = new Object[] { new Byte(siteFamily) };
		Object param = super.invoke(HubClient.methodFindTigSite, params);
		return (Node) param;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#findBillSite(byte)
	 */
	@Override
	public Node findBillSite(byte siteFamily) throws VisitException {
		Object[] params = new Object[] { new Byte(siteFamily) };
		Object param = super.invoke(HubClient.methodFindBillSite, params);
		return (Node) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#login(com.laxcus.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		Object[] params = new Object[] { site };
		Object param = super.invoke(HubClient.methodLogin, params);
		return ((Boolean) param).booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.HubVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		Object[] params = new Object[] { node };
		Object param = super.invoke(HubClient.methodLogout, params);
		return ((Boolean) param).booleanValue();
	}

	

}