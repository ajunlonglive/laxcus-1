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
 * 主动推送ARCHIVE站点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/21/2013
 * @since laxcus 1.0
 */
public class HomePushArchiveSiteInvoker extends HomeInvoker {

	/**
	 * 构造主动推送ARCHIVE站点命令调用器
	 * @param cmd 主动推送ARCHIVE站点命令
	 */
	public HomePushArchiveSiteInvoker(PushAccountSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushAccountSite getCommand() {
		return (PushAccountSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {		
		PushAccountSite cmd = getCommand();
		// ARCHIVE站点
		Node node = cmd.getNode();
		
		Logger.debug(this, "launch", "archive site size %d", cmd.size());
		
		// 有效的签名
		ArrayList<Siger> sigers = new ArrayList<Siger>();

		// 判断签名，保存ARCHIVE站点地址
		for (Siger siger : cmd.list()) {
			boolean success = StaffOnHomePool.getInstance().contains(siger);
			if (success) {
				AccountOnCommonPool.getInstance().add(siger, node);
				sigers.add(siger);
			}
		}
		
		// 收集关联的下属类站点，包括CALL/DATA/WORK/BUILD站点。
		TreeMap<Node, PushAccountSite> records = new TreeMap<Node, PushAccountSite>();
		this.amass(sigers, CallOnHomePool.class, records);
		this.amass(sigers, DataOnHomePool.class, records);
		this.amass(sigers, WorkOnHomePool.class, records);
		this.amass(sigers, BuildOnHomePool.class, records);
		// 投递命令
		this.post(records);
		
		Logger.debug(this, "launch", "size is %d", records.size());
		
		return useful(true);
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
	 * @param siger
	 * @return
	 */
	private NodeSet findSites(Class<?> clazz, Siger siger) {
		if(CallOnHomePool.class == clazz) {
			return CallOnHomePool.getInstance().findSites(siger);
		} else if(DataOnHomePool.class == clazz) {
			return DataOnHomePool.getInstance().findSites(siger);
		} else if(WorkOnHomePool.class == clazz) {
			return WorkOnHomePool.getInstance().findSites(siger);
		} else if(BuildOnHomePool.class == clazz) {
			return BuildOnHomePool.getInstance().findSites(siger);
		}
		return null;
	}

	/**
	 * 收集与用户签名关联的JOB类站点
	 * @param sigers 用户签名列表
	 * @param clazz JOB类管理池
	 * @param records 参数集合
	 */
	private void amass(List<Siger> sigers, Class<?> clazz, Map<Node, PushAccountSite> records) {
		PushAccountSite cmd = getCommand();
		// ARCHIVE站点
		Node archive = cmd.getNode();
		
		for(Siger siger : sigers) {
			NodeSet set = this.findSites(clazz, siger);
			if(set == null) {
				continue;
			}
			
			for (Node job : set.show()) {
				PushAccountSite cd = records.get(job);
				if (cd == null) {
					cd = new PushAccountSite(archive);
					records.put(job, cd);
				}
				cd.add(siger);
			}
		}
	}
	
	/**
	 * 投递主动推送ARCHIVE站点命令到指定的站点
	 * @param records
	 */
	private void post(Map<Node, PushAccountSite> records) {
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		Iterator<Map.Entry<Node, PushAccountSite>> iterator = records.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, PushAccountSite> entry = iterator.next();
			CommandItem item = new CommandItem(entry.getKey(), entry.getValue());
			array.add(item);
		}

		// 以容错模式发送
		this.directTo(array, false);
	}

}