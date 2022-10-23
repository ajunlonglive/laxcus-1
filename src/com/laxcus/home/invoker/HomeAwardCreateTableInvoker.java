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
import com.laxcus.command.access.table.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 授权建表调用器 <br>
 * 
 * TOP站点要求HOME站点，在HOME集群建立一个表。HOME分发给下属的工作节点。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2012
 * @since laxcus 1.0
 */
public class HomeAwardCreateTableInvoker extends HomeInvoker {

	/** 分布站点记录 **/
	private ArrayList<Node> slaves = new ArrayList<Node>();

	/**
	 * 构造授权建表调用器，指定命令
	 * @param cmd 授权建表命令
	 */
	public HomeAwardCreateTableInvoker(AwardCreateTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardCreateTable getCommand() {
		return (AwardCreateTable) super.getCommand();
	}

	/**
	 * 返回表名
	 * @return
	 */
	private Space getSpace() {
		return getCommand().getTable().getSpace();
	}

	/**
	 * 向BANK站点反馈结果
	 * @param success
	 */
	private boolean reply(boolean success) {
		CreateTableProduct product = new CreateTableProduct(getSpace(), success);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardCreateTable cmd = getCommand();
		Table table = cmd.getTable();
		Siger siger = cmd.getIssuer();

		// 判断签名一致！
		if (Laxkit.compareTo(table.getIssuer(), siger) != 0) {
			reply(false);
			return false;
		}

		// 找到本地的资源引用
		Refer refer = StaffOnHomePool.getInstance().find(siger);
		boolean success = (refer != null);
		// 引用不存在，不能建表
		if (!success) {
			Logger.error(this, "launch", "cannot be find '%s'", siger);
			reply(false);
			return false;
		}
		// 保存资源引用
		cmd.setRefer(refer);

		// 如果表已经存在，不能再建表
		success = StaffOnHomePool.getInstance().allow(table.getSpace());
		if (success) {
			Logger.error(this, "launch", "%s duplex", table.getSpace());
			reply(false);
			return false;
		}

		// 收集CALL/DATA/BUILD/WORK站点，首先向CALL站点发送命令
		success = askSites(siger);
		if (!success) {
			reply(false);
			Logger.error(this, "launch", "job site missing! %s", siger);
			return false;
		}

		// 以容错模式向目标站点发送命令，应答保存到内存
		int count = incompleteTo(slaves, cmd);
		// 只要有一个发送成功，即认为是成功
		success = (count > 0);

		Logger.debug(this, "launch", success, "send '%s - %s'", siger, cmd.getTable());

		// 不成功退出
		if (!success) {
			reply(false);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CreateTableProduct product = new CreateTableProduct(getSpace());
		ArrayList<Node> nodes = new ArrayList<Node>();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CreateTableProduct sub = getObject(CreateTableProduct.class, index);
					// 统计发送成功的站点
					if (sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 判断成功或者失败
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());

		// 在本地建表
		if (success) {
			AwardCreateTable cmd = getCommand();
			Table table = cmd.getTable();
			success = StaffOnHomePool.getInstance().createTable(table);
		}

		Logger.note(this, "ending", success, "record is %d, reply is %d",
				slaves.size(), nodes.size());
		
		// 显示网关节点
		for (GatewayNode node : product.list()) {
			Logger.debug(this, "ending", "gateway is \'%s\'", node);
		}

		// 向TOP发送建表报告，成功或者否
		success = replyProduct(product);

		// 重新注册或者通知子站点删除表
		if (success) {
			getLauncher().checkin(false);
		} else {
			AwardCreateTable cmd = getCommand();
			Table table = cmd.getTable();
			AwardDropTable drop = new AwardDropTable(table.getSpace());
			directTo(nodes, drop);
			// 向TOP发送错误报文
			reply(false);
		}

		// 完成
		return useful(success);
	}

	/**
	 * 收集CALL/WORK/BUILD/DATA站点地址
	 * @param siger 账号签名
	 * @return 成功返回真，否则假
	 */
	private boolean askSites(Siger siger) {
		// 收集CALL站点地址
		List<Node> nodes = askCallSites(siger);
		if (nodes == null || nodes.isEmpty()) {
			Logger.error(this, "askSites", "call site missing");
			return false;
		}
		slaves.addAll(nodes);
		// 收集WORK站点地址
		nodes = askWorkSites(siger);
		if (nodes == null || nodes.isEmpty()) {
			Logger.error(this, "askSites", "work site missing");
			return false;
		}
		slaves.addAll(nodes);

		// 收集BUILD站点地址
		nodes = askBuildSites(siger);
		if (nodes == null || nodes.isEmpty()) {
			Logger.error(this, "askSites", "build site missing");
			return false;
		}
		slaves.addAll(nodes);
		// 收集DATA站点地址
		AwardCreateTable cmd = getCommand();
		Table table = cmd.getTable();
		nodes = askDataSites(siger, table.getPrimeSites(), table.getChunkCopy());
		if (nodes == null || nodes.isEmpty()) {
			Logger.error(this, "askSites", "data site missing");
			return false;
		}
		slaves.addAll(nodes);

		// 成功
		return true;
	}

	/**
	 * 根据账号签名筛选CALL站点
	 * @param issuer
	 * @return
	 */
	private List<Node> askCallSites(Siger issuer) {
		// 找到被部署的CALL站点
		NodeSet set = CallOnHomePool.getInstance().findSites(issuer);
		// 不成立， 返回全部地址集合，随机选择一个。
		if (set == null || set.isEmpty()) {
			set = CallOnHomePool.getInstance().list();
		}
		return set.show();
	}

	/**
	 * 根据账号签名筛选WORK站点
	 * @param issuer
	 * @return
	 */
	private List<Node> askWorkSites(Siger issuer) {
		NodeSet set = WorkOnHomePool.getInstance().findSites(issuer);
		if(set == null || set.isEmpty()) {
			set = WorkOnHomePool.getInstance().list();
		}
		return set.show();
	}

	/**
	 * 申请BUILD站点
	 * @param issuer
	 * @return
	 */
	private List<Node> askBuildSites(Siger issuer) {
		NodeSet set = BuildOnHomePool.getInstance().findSites(issuer);
		if(set == null || set.isEmpty()) {
			set = BuildOnHomePool.getInstance().list();
		}
		return set.show();
	}

	/**
	 * 申请DATA站点，必须满足要求的主机数目
	 * @param issuer
	 * @param primeSites
	 * @param chunkCopy
	 * @return 返回指定的主机数目，或者空值
	 */
	private List<Node> askDataSites(Siger issuer, int primeSites, int chunkCopy) {
		// 格式值
		if (primeSites < 1) primeSites = 1;
		if (chunkCopy < 1) chunkCopy = 1;

		// 要求的总节点数目
		final int count = primeSites * chunkCopy;
		// 记录
		TreeSet<Node> array = new TreeSet<Node>();

		// 1 . 找到全部匹配的DATA主站点
		List<Node> list = DataOnHomePool.getInstance().findPrimeSites(issuer);
		if (list.size() < primeSites) {
			array.addAll(list);
		} else {
			array.addAll(list.subList(0, primeSites));
		}
		// 2. 主节点数小于规定值，取出全部DATA节点，继续分配
		if (array.size() < primeSites) {
			NodeSet set = DataOnHomePool.getInstance().getMasters();

			// 优化！基于成员数、表数目、内存空间、磁盘空间，选择最少使用的节点

			int size = set.size();
			for (int index = 0; index < size; index++) {
				Node node = set.next();
				array.add(node);
				if (array.size() == primeSites) {
					break; // 达到要求，退出
				}
			}
		}

		// 主节点数目少于规定值，是错误！
		if (array.size() < primeSites) {
			Logger.error(this, "askDataSites", "data prime site MISSING! %d < %d", array.size(), primeSites);
			return null;
		}

		// 3. 剩下是DATA从节点
		int left = count - array.size();
		list = DataOnHomePool.getInstance().findSlaveSites(issuer);
		if (list.size() < left) {
			array.addAll(list);
		} else {
			// 基于用户数、表数目、磁盘空间，选择最少的节点
			array.addAll(list.subList(0, left));
		}

		// 4. 如果不足，从没有的账号中分配
		if (array.size() < count) {
			NodeSet set = DataOnHomePool.getInstance().getSlaves();
			// 优化！基于人数、内存空间、表数目、磁盘空间，选择最少使用的节点。

			int size = set.size();
			for (int index = 0; index < size; index++) {
				Node node = set.next();
				array.add(node);
				// 达到规定值，退出！
				if (array.size() == count) {
					break;
				}
			}
		}

		Logger.debug(this, "askDataSites", "%s site count:%d, result size:%d ",
				issuer, count, array.size());

		// 5. 如果不足返回空值，否则返回选择的站点
		if (array.size() < count) {
			Logger.error(this, "askDataSites", "data site MISSING! %d < %d", array.size(), count);
			return null;
		}
		// 输出全部节点
		return new ArrayList<Node>(array);
	}

	//	/**
	//	 * 申请DATA站点，必须满足要求的主机数目
	//	 * @param issuer
	//	 * @param primeSites
	//	 * @param chunkCopy
	//	 * @return 返回指定的主机数目，或者空值
	//	 */
	//	private List<Node> askDataSites1(Siger issuer, int primeSites, int chunkCopy) {
	//		TreeSet<Node> array = new TreeSet<Node>();
	//
	//		int count = primeSites * chunkCopy;
	//		// 1 . 找到全部匹配的DATA主站点
	//		List<Node> list = DataOnHomePool.getInstance().findPrimeSites(issuer);
	//		array.addAll(list);
	//		// 2. DATA主节点不足时，获取其它DATA主站点
	//		if (array.size() < primeSites) {
	//			NodeSet set = DataOnHomePool.getInstance().getMasters();
	//			for (Node node : set.show()) {
	//				array.add(node);
	//				if (array.size() == primeSites) {
	//					break; // 达到要求，退出
	//				}
	//			}
	//		}
	//		// 主机数目不足，返回空值
	//		if (array.size() < primeSites) {
	//			return null;
	//		}
	//
	//		// 3. 找匹配的DATA从节点
	//		int left = count - array.size();
	//		list = DataOnHomePool.getInstance().findSlaveSites(issuer);
	//		if (list.size() < left) {
	//			array.addAll(list);
	//		} else {
	//			array.addAll(list.subList(0, left));
	//		}
	//
	//		// 4. 找剩下的DATA从节点
	//		if (array.size() < count) {
	//			NodeSet set = DataOnHomePool.getInstance().getSlaves();
	//			for (Node node : set.show()) {
	//				array.add(node);
	//				if (array.size() == count) {
	//					break; // 达到要求退出
	//				}
	//			}
	//		}
	//		
	//		Logger.debug(this, "askDataSite", "%s site count:%d, result size:%d ",
	//				issuer, count, array.size());
	//
	//		// 5. 如果不足返回空值，否则返回选择的站点
	//		if (array.size() != count) {
	//			return null;
	//		}
	//		return new ArrayList<Node>(array);
	//	}

}