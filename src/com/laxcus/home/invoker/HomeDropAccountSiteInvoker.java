/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.account.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * DropArchiveSite命令调用器
 * 
 * @author scott.liang
 * @version 1.0
 * @since laxcus 1.0
 */
public class HomeDropAccountSiteInvoker extends HomeInvoker {

	/**
	 * 构造DropArchiveSite命令调用器
	 * @param cmd - DropArchiveSite命令
	 */
	public HomeDropAccountSiteInvoker(DropAccountSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropAccountSite getCommand() {
		return (DropAccountSite) super.getCommand();
	}
	
	private Node getSource() {
		return getCommand().getNode();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {		
		Node node = getSource();

		List<Siger> list = AccountOnCommonPool.getInstance().remove(node);

		boolean success = (list != null && list.size() > 0);
		// 查找下属工作站点，通知它们
		if (success) {
			// 收集关联的JOB类站点
			TreeMap<Node, DropAccountSite> records = new TreeMap<Node, DropAccountSite>();
			this.collect(list, CallOnHomePool.class, records);
			this.collect(list, DataOnHomePool.class, records);
			this.collect(list, WorkOnHomePool.class, records);
			this.collect(list, BuildOnHomePool.class, records);
			// 投递命令
			this.post(records);
		}
		
		Logger.debug(this, "launch", success, "drop %s", node);

		// 退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 查找JOB类站点
	 * @param clazz
	 * @param username
	 * @return
	 */
	private NodeSet findSites(Class<?> clazz, Siger username) {
		if(CallOnHomePool.class == clazz) {
			return CallOnHomePool.getInstance().findSites(username);
		} else if(DataOnHomePool.class == clazz) {
			return DataOnHomePool.getInstance().findSites(username);
		} else if(WorkOnHomePool.class == clazz) {
			return WorkOnHomePool.getInstance().findSites(username);
		} else if(BuildOnHomePool.class == clazz) {
			return BuildOnHomePool.getInstance().findSites(username);
		}
		return null;
	}
	
	/**
	 * 收集JOB类站点地址
	 * @param list
	 * @param clazz
	 * @param records
	 */
	private void collect(List<Siger> list, Class<?> clazz, Map<Node, DropAccountSite> records) {
		Node archive = getSource();
		
		for (Siger username : list) {
			NodeSet set = findSites(clazz, username);
			if(set == null) {
				continue;
			}
			
			for(Node job : set.show()) {
				DropAccountSite cd = records.get(job);
				if(cd == null) {
					cd = new DropAccountSite(archive);
					records.put(job, cd);
				}
			}
		}
	}
	
	/**
	 * 投递DROP ARCHIVE SITE命令到JOB类站点
	 * @param records
	 */
	private void post(Map<Node, DropAccountSite> records) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, DropAccountSite>> iterator = records.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, DropAccountSite> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			array.add(item);
		}

		// 以容错模式发送
		this.directTo(array, false);
	}
}
