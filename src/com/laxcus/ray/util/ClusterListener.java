/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.util;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 集群监听器
 * 
 * @author scott.liang
 * @version 1.0 3/3/2022
 * @since laxcus 1.0
 */
public interface ClusterListener {

	/**
	 * 显示运行记录
	 * @param cmd
	 */
	void showRuntime(SiteRuntime cmd);
	
	void modify(SiteRuntime runtime);
	
	void updateStatusMembers();

	void pushOnlineMember(Siger siger);
	
	void pushRegisterMember(Siger siger);

	void dropRegisterMember(Siger siger);

	void dropOnlineMember(Siger siger);

	void updateOnlineMember(Siger siger);

	boolean pushSite(Node node);

	boolean dropSite(Node node);

	boolean destroySite(Node node);
}
