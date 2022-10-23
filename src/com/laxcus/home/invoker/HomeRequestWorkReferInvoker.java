/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.refer.*;
import com.laxcus.home.pool.*;
import com.laxcus.home.util.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.data.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 请求分配WORK站点资源引用调用器。<br>
 * 
 * 这个命令由WORK站点发出，向HOME站点申请一批账号，以及账号下的数据表名。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2013
 * @since laxcus 1.0
 */
public class HomeRequestWorkReferInvoker extends HomeInvoker {

	/**
	 * 构造请求分配WORK站点资源引用调用器，指定命令。
	 * @param cmd 请求分配WORK站点资源引用命令
	 */
	public HomeRequestWorkReferInvoker(RequestWorkRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RequestWorkRefer getCommand() {
		return (RequestWorkRefer) super.getCommand();
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		TreeMap<Siger, Vident> vidents = new TreeMap<Siger, Vident>();
//		// 从资源管理池获得全部账号
//		List<Siger> list = StaffOnHomePool.getInstance().getUsers();
//		for (Siger siger : list) {
//			Vident e = new Vident(siger);
//			vidents.put(e.getSiger(), e);
//			// 统计使用次数
//			NodeSet set = WorkOnHomePool.getInstance().findSites(siger);
//			if (set != null) {
//				e.addSites(set.size());
//			}
//			// 统计已经使用的数据块尺寸
//			Refer refer = StaffOnHomePool.getInstance().find(siger);
//			if (refer == null) {
//				continue;
//			}
//			for (Space space : refer.getTables()) {
//				set = DataOnHomePool.getInstance().findSites(space);
//				if (set == null) {
//					continue;
//				}
//				for (Node node : set.show()) {
//					DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
//					long capicity = site.findMemoryCapacity(space);
//					e.addMemory(capicity);
//				}
//			}
//		}
//
//		// 排序
//		ArrayList<Vident> array = new ArrayList<Vident>(vidents.values());
//		java.util.Collections.sort(array);
//
//		RequestWorkRefer cmd = getCommand();
//		// 取出WORK站点的可用内存空间，只使用它的10%，其它空间留给分布计算和数据存储使用
//		long size = cmd.getSize() / 10;
//
//		Logger.debug(this, "launch", "refer elements is:%d, work free memory:%d", 
//				array.size(), cmd.getSize());
//
//		// 数据处理结果
//		RequestReferProduct product = new RequestReferProduct();
//
//		for (Vident vident : array) {
//			Siger username = vident.getSiger();
//			Refer refer = StaffOnHomePool.getInstance().find(username);
//			// WORK站点可用内存空间小于用户元数据资源，不分配
//			if (size < vident.getMemory()) {
//				Logger.warning(this, "launch", "%s, capacity: %d < %d", username, size, vident.getMemory());
//				continue;
//			}
//			if (vident.getSites() == 0) {
//				// 保存这个账号和下面全部的表
//				product.add(refer);
//			} else {
//				// 已经分配的站点达到或者超过指定网关数目时，忽略它
//				if (vident.getSites() >= refer.getUser().getGateways()) {
//					Logger.warning(this, "launch", "%s, gateway %d > %d",
//							username, vident.getSites(), refer.getUser().getGateways());
//					continue;
//				}
//				product.add(refer);
//			}
//			// 减少可用内存空间
//			size -= vident.getMemory();
//		}
//
//		// 发送结果
//		boolean success = super.replyObject(product);
//
//		Logger.debug(this, "launch", success, "user size is:%d", product.size()); 
//
//		return useful(success);
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 合并
		List<Vident> array = assemble();
		// 分配
		RequestReferProduct product = assign(array);

		// 发送结果
		boolean success = replyObject(product);

		Logger.debug(this, "launch", success, "user size is:%d", product.size()); 

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
	 * 合并数据
	 * @return 数组列表
	 */
	private List<Vident> assemble() {
		// 从资源管理池获得全部账号
		List<Siger> sigers = StaffOnHomePool.getInstance().getUsers();
		Logger.debug(this, "assemble", "all users:%d", sigers.size());
		ArrayList<Vident> array = new ArrayList<Vident>(sigers.size());
		
		// 逐一提取
		for (Siger siger : sigers) {
			// 确定资源引用存在
			Refer refer = StaffOnHomePool.getInstance().find(siger);
			if (refer == null) {
				Logger.error(this, "assemble", "no refer found:%s", siger);
				continue;
			}
			
			Vident vident = new Vident(siger);
			// 统计已经注册的节点数目
			NodeSet set = WorkOnHomePool.getInstance().findSites(siger);
			if (set != null) {
				vident.addSites(set.size());
			}
			// 统计已经使用的数据块尺寸
			for (Space space : refer.getTables()) {
				set = DataOnHomePool.getInstance().findSites(space);
				if (set == null) {
					continue;
				}
				for (Node node : set.show()) {
					DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
					long capicity = site.findMemoryCapacity(space);
					vident.addMemory(capicity);
				}
			}
			// 保存
			array.add(vident);
		}

		// 排序
		Collections.sort(array);
		return array;
	}
	
	/**
	 * 再分配
	 * @param array
	 * @return
	 */
	private RequestReferProduct assign(List<Vident> array) {
		RequestWorkRefer cmd = getCommand();
		// 取出CALL站点的可用内存空间，只使用它的10%，其它空间留给分布计算和数据存储使用
		long limitSize = cmd.getSize() / 10;

		Logger.debug(this, "assign", "refer elements:%d, free memory:%d", 
				array.size(), cmd.getSize());

		// 数据处理结果
		RequestReferProduct product = new RequestReferProduct();

		for (Vident vident : array) {
			Siger siger = vident.getSiger();
			Refer refer = StaffOnHomePool.getInstance().find(siger);
			// CALL站点可用内存空间小于用户元数据资源，不分配
			if (limitSize < vident.getMemory()) {
				Logger.warning(this, "assign", "%s, capacity: %d < %d", siger, limitSize, vident.getMemory());
				continue;
			}
			if (vident.getSites() == 0) {
				// 保存这个账号和下面全部的表
				product.add(refer);
			} else {
				// 已经分配的站点达到或者超过指定网关数目时，忽略它
				if (vident.getSites() >= refer.getUser().getWorkers()) {
					Logger.warning(this, "assign", "%s, work sites %d > %d",
							siger, vident.getSites(), refer.getUser().getWorkers());
					continue;
				}
				product.add(refer);
			}
			// 减少可用内存空间
			limitSize -= vident.getMemory();
		}
		
		return product;
	}

}
