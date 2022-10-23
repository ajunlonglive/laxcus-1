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
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.Node;
import com.laxcus.site.build.*;
import com.laxcus.site.call.*;
import com.laxcus.site.data.*;
import com.laxcus.site.work.*;

/**
 * 建立用户资源引用调用器。<br>
 * 来自TOP站点，HOME站点根据参数要求，找到合适的下属的CALL/WORK/DATA/BUILD，把命令分配给它们，要求全部成功。
 * 
 * @author scott.liang
 * @version 1.1 11/06/2013
 * @since laxcus 1.0
 */
public class HomeAwardCreateReferInvoker extends HomeInvoker {

	/** 分配发布的站点 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/**
	 * 构造建立用户资源引用调用器，指定命令
	 * @param cmd 建立用户资源引用命令
	 */
	public HomeAwardCreateReferInvoker(AwardCreateRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	public AwardCreateRefer getCommand() {
		return (AwardCreateRefer) super.getCommand();
	}

	/**
	 * 返回账号的用户签名
	 * @return Siger实例
	 */
	private Siger getUsername() {
		AwardCreateRefer cmd = getCommand();
		Refer refer = cmd.getRefer();
		return refer.getUsername();
	}

	/**
	 * 向TOP站点反馈结果
	 * @param successful
	 */
	private void reply(boolean successful) {
		AwardCreateRefer cmd = getCommand();
		if (cmd.isDirect()) {
			return;
		}
		CreateUserProduct product = new CreateUserProduct(cmd.getUsername(), successful);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 不是管理站点，直接拒绝！
		if (!isManager()) {
			refuse();
			return false;
		}

		// 找到资源引用
		AwardCreateRefer cmd = getCommand();
		Refer refer = cmd.getRefer();
		Siger siger = refer.getUsername();
		// 如果本地存在资源引用，拒绝它！
		boolean success = StaffOnHomePool.getInstance().contains(siger);
		if (success) {
			Logger.error(this, "launch", "%s is actived!", siger);
			refuse();
			return false;
		}

		// 搜索HOME站点下面的分布站点
		success = choice(refer);
		if (!success) {
			Logger.error(this, "launch", "choice %s error", siger);
			replyFault(Major.FAULTED, Minor.SITE_MISSING);
			return false;
		}

		// 检查签名存在！
		success = StaffOnHomePool.getInstance().contains(siger);
		// 如果存在，拒绝它
		if (success) {
			Logger.error(this, "launch", "%s is contains", siger);
			replyFault(Major.FAULTED, Minor.DUPLEX);
			return false;
		}

		// 建立用户资源引用
		success = StaffOnHomePool.getInstance().create(refer);
		// 不成功返回拒绝
		if (!success) {
			Logger.error(this, "launch", "cannot be create '%s;", siger);
			refuse();
			return false;
		}

		// 向TOP站点查询与这个账号关联的BANK站点。这个操作是同步的，必须在给工作站发送命令前进行。等待它们来获取ACCOUNT站点。
		AccountOnCommonPool.getInstance().load(siger);

		// 以容错模式向工作站点发送AwardCreateRefer命令
		int count = incompleteTo(slaves, cmd);
		success = (count > 0);
		if (!success) {
			// 删除本地的注册
			StaffOnHomePool.getInstance().drop(siger);
			// 删除与用户签名关联的ACCOUNT站点地址
			AccountOnCommonPool.getInstance().remove(siger);

			Logger.error(this, "launch", "cannot be send sub sites");
			refuse();
		}

		Logger.debug(this, "launch", success, "result is");

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 注册用户签名
		Siger siger = getUsername();
		// 结果
		CreateUserProduct product = new CreateUserProduct(siger);
		// 记录成功的站点
		ArrayList<Node> nodes = new ArrayList<Node>();

		// 统计结果
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CreateUserProduct sub = getObject(CreateUserProduct.class, index);
					if (sub.isSuccessful()) {
						nodes.add(getBufferHub(index));
					}
					product.add(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 发送和接收站点数目一致，且记录完全成功
		boolean success = (slaves.size() == nodes.size() && product.isSuccessful());

		// 成功，向TOP反馈结果
		if (success) {
			success = replyProduct(product);
		}

		Logger.debug(this, "ending", success, "right count:%d, failed count:%d", 
				product.getRights(), product.getFaults());

		// 成功，通知启动器重新注册；不成功，删除记录。通知工作站点
		if (success) {
			getLauncher().checkin(false);
		} else {
			// 删除本地的注册
			StaffOnHomePool.getInstance().drop(siger);
			// 删除与用户签名关联的ACCOUNT站点地址
			AccountOnCommonPool.getInstance().remove(siger);

			// 向已经注册成功的站点发送删除命令，不用反馈结果
			AwardDropRefer award = new AwardDropRefer(siger);
			// 通知下属工作站点删除
			directTo(nodes, award);
			// 反馈结果
			reply(false);
		}

		// 完成退出
		return useful(success);
	}

	/**
	 * 查找筛选合适的CALL节点
	 * @param refer 资源引用
	 * @return 返回真或者假
	 */
	private boolean choiceCallSites(Refer refer) {
		Siger siger = refer.getUsername();
		// 如果账号已经存在，是错误！
		boolean success = CallOnHomePool.getInstance().contains(siger);
		if (success) {
			Logger.error(this, "choiceCallSites", "%s is EXISTED!", siger);
			return false;
		}

		TreeSet<Node> array = new TreeSet<Node>();

		// 取出全部节点
		NodeSet nodes = CallOnHomePool.getInstance().list();
		int size = nodes.size();

		// 按照指定的网关要求，分配CALL节点数目
		int gateways = refer.getUser().getGateways();
		for (int index = 0; index < size; index++) {
			// 循环取出每一个
			Node node = nodes.next();
			CallSite site = (CallSite) CallOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			// 判断空间，如果已经满就不处理
			Moment moment = site.getMoment();
			if (moment.getMember().isFull()) {
				Logger.warning(this, "choiceCallSites", "%s is member full", node);
				continue;
			}
			if (moment.getOnline().isFull()) {
				Logger.warning(this, "choiceCallSites", "%s is front full", node);
				continue;
			}
			// 保存！
			array.add(node);
			if (array.size() >= gateways) {
				break;
			}
		}

		// 低于规定值，忽略！
		if (array.size() < gateways) {
			Logger.error(this, "choiceCallSites", "site MISSING! %d < %d", array.size(), gateways);
			return false;
		}

		// 保存记录
		slaves.addAll(array);
		return true;
	}

	/**
	 * 查找筛选合适的DATA主节点
	 * @param refer 资源引用
	 * @return 返回真或者假
	 */
	private boolean choiceMasterDataSites(Refer refer) {
		Siger siger = refer.getUsername();
		// 如果账号已经存在，是错误！
		List<Node> nodes = DataOnHomePool.getInstance().findPrimeSites(siger);
		if (nodes.size() > 0) {
			Logger.error(this, "choiceMasterDataSites", "%s is EXISTED!", siger);
			return false;
		}

		// 顺序取DATA主站点，把账号发布到DATA主站点上
		int bases = refer.getUser().getBases();
		TreeSet<Node> array = new TreeSet<Node>();

		// 取出全部DATA主节点
		NodeSet set = DataOnHomePool.getInstance().getMasters();
		int size = set.size();
		for (int i = 0; i < size; i++) {
			Node node = set.next();
			DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			// 如果空间已经满，忽略这个！
			Moment moment = site.getMoment();
			if (moment.getMember().isFull()) {
				Logger.warning(this, "choiceMasterDataSites", "%s is FULL!", node);
				continue;
			}
			// 保存它！
			array.add(node);
			if (array.size() >= bases) {
				break;
			}
		}

		// 低于规定值，忽略！
		if (array.size() < bases) {
			Logger.error(this, "choiceMasterDataSites", "site MISSING! %d < %d", array.size(), bases);
			return false;
		}
		// 保存记录
		slaves.addAll(array);
		return true;
	}

	/**
	 * 查找筛选合适的DATA从节点
	 * @param refer 资源引用
	 * @return 返回真或者假
	 */
	private boolean choiceSlaveDataSites(Refer refer) {
		Siger siger = refer.getUsername();
		// 如果账号已经存在，是错误！
		List<Node> nodes = DataOnHomePool.getInstance().findSlaveSites(siger);
		if (nodes.size() > 0) {
			Logger.error(this, "choiceSlaveDataSites", "%s is EXISTED!", siger);
			return false;
		}
		
		// 顺序取DATA从站点，把账号发布到DATA从站点上
		int subBases = refer.getUser().getSubBases();
		if (subBases < 1) {
			return true;
		}
		
		TreeSet<Node> array = new TreeSet<Node>();

		// 取出全部DATA从节点
		NodeSet set = DataOnHomePool.getInstance().getSlaves();
		int size = set.size();
		for (int i = 0; i < size; i++) {
			Node node = set.next();
			DataSite site = (DataSite) DataOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}
			// 如果空间已经满，忽略这个！
			Moment moment = site.getMoment();
			if (moment.getMember().isFull()) {
				Logger.warning(this, "choiceSlaveDataSites", "%s is FULL!", node);
				continue;
			}
			// 保存它！
			array.add(node);
			if (array.size() >= subBases) {
				break;
			}
		}

		// 低于规定值，忽略！
		if (array.size() < subBases) {
			Logger.error(this, "choiceSlaveDataSites", "site MISSING! %d < %d", array.size(), subBases);
			return false;
		}
		// 保存记录
		slaves.addAll(array);
		return true;
	}
	
	/**
	 * 查找筛选合适的WORK节点
	 * @param refer 资源引用
	 * @return 返回真或者假
	 */
	private boolean choiceWorkSites(Refer refer) {
		Siger siger = refer.getUsername();
		// 判断存在！
		boolean success = WorkOnHomePool.getInstance().contains(siger);
		if (success) {
			Logger.error(this, "choiceWorkSites", "%s is EXISTED!", siger);
			return false;
		}

		// 节点
		int workers = refer.getUser().getWorkers();
		TreeSet<Node> array = new TreeSet<Node>();

		// 取出全部节点
		NodeSet nodes = WorkOnHomePool.getInstance().list();
		int size = nodes.size();

		for (int i = 0; i < size; i++) {
			Node node = nodes.next();
			// 查找站点
			WorkSite site = (WorkSite) WorkOnHomePool.getInstance().find(node);
			if (site == null) {
				continue;
			}

			// 如果空间已经满，忽略这个！
			Moment moment = site.getMoment();
			if (moment.getMember().isFull()) {
				Logger.warning(this, "choiceWorkSites", "%s is FULL!", node);
				continue;
			}
			// 保存它！
			array.add(node);
			if (array.size() >= workers) {
				break;
			}
		}

		// 低于规定值，忽略！
		if (array.size() < workers) {
			Logger.error(this, "choiceWorkSites", "site MISSING! %d < %d", array.size(), workers);
			return false;
		}
		// 保存记录
		slaves.addAll(array);
		return true;
	}

	/**
	 * 查找合适的BUILD节点
	 * @param refer 资源引用 
	 * @return 成功返回真，否则假
	 */
	private boolean choiceBuildSites(Refer refer) {
		Siger siger = refer.getUsername();
		// 判断存在！
		boolean success = BuildOnHomePool.getInstance().contains(siger);
		if (success) {
			Logger.error(this, "choiceBuildSites", "%s is EXISTED!", siger);
			return false;
		}

		// BUILD节点
		int builders = refer.getUser().getBuilders();
		TreeSet<Node> array = new TreeSet<Node>();

		// 逐一检查筛选
		NodeSet set = BuildOnHomePool.getInstance().list();
		int size = set.size();
		for (int i = 0; i < size; i++) {
			Node node = set.next();
			BuildSite site = (BuildSite) BuildOnHomePool.getInstance().find(node);

			// 如果空间已经满，忽略这个！
			Moment moment = site.getMoment();
			if (moment.getMember().isFull()) {
				Logger.warning(this, "choiceBuildSites", "%s is FULL!", node);
				continue;
			}
			// 保存它！
			array.add(node);
			if (array.size() >= builders) {
				break;
			}
		}

		// 低于规定值，忽略！
		if (array.size() < builders) {
			Logger.error(this, "choiceBuildSites", "site MISSING! %d < %d", array.size(), builders);
			return false;
		}
		// 保存记录
		slaves.addAll(array);
		return true;
	}

	/**
	 * 比较参数，选择CALL/WORK/DATA主从/BUILD节点。
	 * @param refer 用户资源引用
	 * @return 成功返回真，否则假
	 */
	private boolean choice(Refer refer) {
		boolean success = choiceCallSites(refer);
		if (success) {
			success = choiceMasterDataSites(refer);
		}
		if (success) {
			success = choiceSlaveDataSites(refer);
		}
		if (success) {
			success = choiceWorkSites(refer);
		}
		if (success) {
			success = choiceBuildSites(refer);
		}

		Logger.debug(this, "choice", success, "sub sites: %d", slaves.size());

		return success;
	}

	//	/**
	//	 * 随机选择和分配分布站点
	//	 * @param refer 用户资源引用
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean choice1(Refer refer) {
	//		Siger siger = refer.getUsername();
	//		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
	//		// 如果用户账号存在，是一个错误
	//		if (set != null && set.size() > 0) {
	//			Logger.error(this, "choice", "%s in CallOnHomePool, size:%d", siger, set.size());
	//			return false;
	//		}
	//
	//		// 找到全部CALL站点
	//		set = CallOnHomePool.getInstance().list();
	//		if (set == null || set.isEmpty()) {
	//			Logger.error(this, "choice", "canote be find call site");
	//			return false;
	//		}
	//		// 按照指定的网关要求，分配CALL节点数目
	//		int gateways = refer.getUser().getGateways();
	//		for (int i = 0; i < gateways; i++) {
	//			slaves.add(set.next());
	//		}
	//
	//		// 顺序取DATA主站点，把账号发布到DATA主站点上
	//		set = DataOnHomePool.getInstance().getMasters();
	//		int storagers = refer.getUser().getStoragers();
	//		if (set != null) {
	//			for (int i = 0; i < storagers; i++) {
	//				slaves.add(set.next());
	//			}
	//		}
	//		
	//		// 顺序取一个BUILD站点，把账号发布到BUILD站点上
	//		set = BuildOnHomePool.getInstance().list();
	//		int builders = refer.getUser().getBuilders();
	//		if (set != null) {
	//			for (int i = 0; i < builders; i++) {
	//				slaves.add(set.next());
	//			}
	//		}
	//
	//		// 顺序取一个WORK站点，把账号发布到WORK站点上
	//		set = WorkOnHomePool.getInstance().list();
	//		int workers = refer.getUser().getWorkers();
	//		if (set != null) {
	//			for (int i = 0; i < workers; i++) {
	//				slaves.add(set.next());
	//			}
	//		}
	//
	//		Logger.debug(this, "choice", "sub sites: %d", slaves.size());
	//
	//		return true;
	//	}

}