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
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;

/**
 * FRONT远程访问客户端。<br>
 * 只能登录到ENTRANCE/GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 7/17/2018
 * @since laxcus 1.0
 */
public class FrontClient extends RemoteClient implements FrontVisit {

	private static Method methodGetVersion;

	private static Method methodGetHubFamily;
	private static Method methodGetHubSucker;
	private static Method methodGetHubDispatcher;
	private static Method methodGetTimeout;
	private static Method methodGetLingerTimeout;
	private static Method methodGetAutoReloginInterval;

	private static Method methodRelease;
	private static Method methodLogin;
	private static Method methodLogout;

	private static Method methodConferrerRelease;
	private static Method methodConferrerLogin;
	private static Method methodConferrerLogout;

	static {
		try {
			methodGetVersion = (FrontVisit.class).getMethod("getVersion", new Class<?>[0]);

			methodGetHubFamily = (FrontVisit.class).getMethod("getHubFamily", new Class<?>[0]);
			methodGetHubSucker = (FrontVisit.class).getMethod("getHubSucker", new Class<?>[] { Boolean.TYPE });
			methodGetHubDispatcher = (FrontVisit.class).getMethod("getHubDispatcher", new Class<?>[] { Boolean.TYPE });
			methodGetTimeout = (FrontVisit.class).getMethod("getTimeout", new Class<?>[0]);
			methodGetLingerTimeout = (FrontVisit.class).getMethod("getLingerTimeout", new Class<?>[0]);
			methodGetAutoReloginInterval = (FrontVisit.class).getMethod("getAutoReloginInterval", new Class<?>[0]);

			methodRelease = (FrontVisit.class).getMethod("release", new Class<?>[] { FrontSite.class });
			methodLogin = (FrontVisit.class).getMethod("login", new Class<?>[] { FrontSite.class });
			methodLogout = (FrontVisit.class).getMethod("logout", new Class<?>[] { Node.class });

			methodConferrerRelease = (FrontVisit.class).getMethod("release", new Class<?>[] { ConferrerSite.class });
			methodConferrerLogin = (FrontVisit.class).getMethod("login", new Class<?>[] { ConferrerSite.class });
			methodConferrerLogout = (FrontVisit.class).getMethod("logout", new Class<?>[] { Node.class, Siger.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 * 构造FrontVisit接口的访问客户端，指定传输模式
	 * @param stream 流模式
	 */
	public FrontClient(boolean stream) {
		super(stream);
		super.setVisitName(FrontVisit.class.getName());
	}

	/**
	 * 构造FrontVisit接口访问客户端，指定目标地址。
	 * @param endpoint 实现FrontVisit接口的站点地址。
	 */
	public FrontClient(SocketHost endpoint) {
		this(endpoint.isStream());
		super.setRemote(endpoint);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getVersion()
	 */
	@Override
	public Version getVersion() throws VisitException {
		Object param = super.invoke(FrontClient.methodGetVersion, null);
		return ((Version) param);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getFrontFamily()
	 */
	@Override
	public byte getHubFamily() throws VisitException {
		Object param = super.invoke(FrontClient.methodGetHubFamily, null);
		return ((Byte) param).byteValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubSucker(boolean)
	 */
	@Override
	public SocketHost getHubSucker(boolean wide) throws VisitException {
		Object[] params = new Object[] { new Boolean(wide) };
		Object param = super.invoke(FrontClient.methodGetHubSucker, params);
		return (SocketHost) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getHubDispatcher(boolean)
	 */
	@Override
	public SocketHost getHubDispatcher(boolean wide) throws VisitException {
		Object[] params = new Object[] { new Boolean(wide) };
		Object param = super.invoke(FrontClient.methodGetHubDispatcher, params);
		return (SocketHost) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getSiteTimeout(byte)
	 */
	@Override
	public long getTimeout() throws VisitException {
		Object param = super.invoke(FrontClient.methodGetTimeout, null);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getLingerTimeout()
	 */
	@Override
	public long getLingerTimeout() throws VisitException {
		Object param = super.invoke(FrontClient.methodGetLingerTimeout, null);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#getAutoReloginInterval()
	 */
	@Override
	public long getAutoReloginInterval() throws VisitException {
		Object param = super.invoke(FrontClient.methodGetAutoReloginInterval, null);
		return ((Long) param).longValue();
	}


	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.FrontSite)
	 */
	@Override
	public boolean release(FrontSite site) throws VisitException {
		Object[] params = new Object[] { site };
		Object param = super.invoke(FrontClient.methodRelease, params);
		return ((Boolean) param).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.Site)
	 */
	@Override
	public FrontReport login(FrontSite site) throws VisitException {
		Object[] params = new Object[] { site };
		Object param = super.invoke(FrontClient.methodLogin, params);
		return (FrontReport) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#logout(com.laxcus.site.Node)
	 */
	@Override
	public boolean logout(Node node) throws VisitException {
		Object[] params = new Object[] { node };
		Object param = super.invoke(FrontClient.methodLogout, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#release(com.laxcus.site.front.FrontSite)
	 */
	@Override
	public boolean release(ConferrerSite site) throws VisitException {
		Object[] params = new Object[] { site };
		Object param = super.invoke(FrontClient.methodConferrerRelease, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#login(com.laxcus.site.front.ConferrerSite)
	 */
	@Override
	public FrontReport login(ConferrerSite site) throws VisitException {
		Object[] params = new Object[] { site };
		Object param = super.invoke(FrontClient.methodConferrerLogin, params);
		return (FrontReport) param;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.hub.FrontVisit#logout(com.laxcus.site.Node, com.laxcus.util.Siger)
	 */
	@Override
	public boolean logout(Node node, Siger authorizer) throws VisitException {
		Object[] params = new Object[] { node, authorizer };
		Object param = super.invoke(FrontClient.methodConferrerLogout, params);
		return ((Boolean) param).booleanValue();
	}

}