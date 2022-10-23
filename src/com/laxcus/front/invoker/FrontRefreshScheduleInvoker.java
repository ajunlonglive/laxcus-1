/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.command.site.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.site.Node;

/**
 * 定时更新当前账号的所有资源。
 * 包括：
 * 1. 账号
 * 2. 登录账号关联的CALL节点地址
 * 3. 登录账号被授权人身份关联的CALL节点地址
 * 
 * 替换FrontRefreshCallInvoker
 * 
 * 更新账号持有人的CALL站点调用器。<br>
 * 此操作由FRONT资源管理池定时触发，向GATE节点查询关联的CALL节点，更新到本地。
 * 
 * @author scott.liang
 * @version 1.2 10/28/2015
 * @since laxcus 1.0
 */
public class FrontRefreshScheduleInvoker extends FrontInvoker {

	/** 更新的账号 **/
	private Account account;

	/** 自有的CALL节点资源 **/
	private TakeOwnerCallProduct selflyProduct;

	/** 授权者的GATE节点地址集合 **/
	private TakeAuthorizerSiteProduct authorGateProduct;

	/** 授权人的CALL节点地址 **/
	private ArrayList<TakeAuthorizerCallProduct> authorCallProducts = new ArrayList<TakeAuthorizerCallProduct>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();

		// 清除记录
		if (account != null) {
			account = null;
		}
		if (selflyProduct != null) {
			selflyProduct = null;
		}
		if (authorGateProduct != null) {
			authorGateProduct = null;
		}
		if (authorCallProducts.size() > 0) {
			authorCallProducts.clear();
		}
	}

	/**
	 * 构造更新账号持有人的CALL站点调用器，指定命令
	 * @param cmd 获取账号持有人的CALL站点
	 */
	public FrontRefreshScheduleInvoker(RefreshSchedule cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshSchedule getCommand() {
		return (RefreshSchedule) super.getCommand();
	}

	/**
	 * 返回转发命令超时时间
	 * @return 以毫秒计
	 */
	private long getShiftTimeout() {
		return getCommand().getShiftTimeout();
	}
	
	/**
	 * 判断允许执行！
	 * @return 返回真或者假
	 */
	private boolean allow() {
		// 如果是管理员账号，忽略它！
//		if (isOffline() || isAdministrator()) {
		
		// 如果没有注册，或者是管理员，忽略它！
		if (!isLogined() || isAdministrator()) {
			return false;
		}
		// 判断已经注册
		boolean logined = getLauncher().isLogined();
		if (!logined) {
			return false;
		}

		// 判断账号存在且有效
		return getStaffPool().hasAccount();

		//		// 如果没有定义账号，是错误，忽略！
		//		Account old = getStaffPool().getAccount();
		//		if (old == null) {
		//			Logger.error(this, "allow", "account is null pointer!");
		//			return false;
		//		}
		//		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断允许执行
		if (!allow()) {
			return useful(false);
		}

		// 锁定处理！任意时间只能有一个！如果失败退出！（同一时间，可能有MeetCreateTableInvoker或者其他在处理）
		boolean lock = ScheduleLock.lock();
		if (!lock) {
			return useful(false);
		}
		
		// 1. 加载账号
		boolean success = loadAccount();
		Logger.note(this, "launch", success, "load selfly's account");
		// 2. 加载CALL节点地址
		if (success) {
			success = loadCallSite();
			Logger.note(this, "launch", success, "load selfly's call site");
		}
		// 3. 加载授权者的GATE节点地址
		if (success) {
			success = loadAuthorizerGateSites();
			Logger.note(this, "launch", success, "load authorizer's gate sites %d",
					(authorGateProduct == null ? -1 : authorGateProduct.size()));
		}
		// 4. 加载授权者的CALL节点地址
		if (success) {
			success = loadAuthorizerCallSites();
			Logger.note(this, "launch", success, "load authorizer's call sites");
		}

		// 5. 最后，更新参数
		if (success) {
			update();
		}

		// 解除锁定
		ScheduleLock.unlock();

		Logger.debug(this, "launch", success, "result is");

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 检查账号
	 * @return 成功返回真，否则假
	 */
	private boolean loadAccount() {
		// 不允许时，退出
		if (!allow()) {
			return false;
		}
		
		TakeAccount cmd = new TakeAccount(getUsername());
		TakeAccountHook hook = new TakeAccountHook();
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		ShiftTakeAccount shift = new ShiftTakeAccount(cmd, hook);

		// 交给管理池处理
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}

		// 获取账号
		account = hook.getAccount();
		// 判断结果
		return (account != null);
	}

	/**
	 * 加载新的CALL节点资源
	 * @return 返回真或者假
	 */
	private boolean loadCallSite() {
		// 不允许时，退出
		if (!allow()) {
			return false;
		}
		
		TakeOwnerCall cmd = new TakeOwnerCall(getUsername());
		TakeOwnerCallHook hook = new TakeOwnerCallHook();
		hook.setTimeout(getShiftTimeout());
		ShiftTakeOwnerCall shift = new ShiftTakeOwnerCall(cmd, hook);

		// 转发和等待
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}
		// 判断有效！
		selflyProduct = hook.getProduct();
		return (selflyProduct != null);
	}

	/**
	 * 去ENTRANCE节点加载授权者的GATE节点。
	 * 注意！ENTRANCE的是根据用户签名去分配GATE节点地址！
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean loadAuthorizerGateSites() {
		// 不允许时，退出
		if (!allow()) {
			return false;
		}
		
		// 判断有被授权人，如果有，取他们的GATE站点地址
		List<Siger> authorizers = account.getPassiveAuthorizers();
		
		Logger.debug(this, "loadAuthorizerGateSites", "to entrance, authorizer count %d", authorizers.size());
		
		// 如果没有，忽略它
		if (authorizers.isEmpty()) {
			// 生成一个空的对象!
			authorGateProduct = new TakeAuthorizerSiteProduct();
			return true;
		}

		// 去ENTRANCE节点定位授权人的GATE节点地址
		TakeAuthorizerSite cmd = new TakeAuthorizerSite(authorizers);
		TakeAuthorizerSiteHook hook = new TakeAuthorizerSiteHook();
		hook.setTimeout(getShiftTimeout());
		ShiftTakeAuthorizerSite shift = new ShiftTakeAuthorizerSite(cmd, hook);

		// 去ENTRANCE站点获取授权人的GATE站点地址
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}
		// 判断
		authorGateProduct = hook.getProduct();
		success = (authorGateProduct != null);

		// 立即登录授权人的GATE站点
		if (success && authorGateProduct.size() > 0) {
			updateAuthroizerGateSites();
		}
		
		return success;
	}

	/**
	 * 根据授权人签名，查找它关联的CALL节点地址
	 * @param authorizer 授权人签名
	 * @return 返回节点实例，或者空指针
	 */
	private Node findAuthorizerGateSite(Siger authorizer) {
		// 没有定义时，返回空指针
		if (authorGateProduct == null) {
			return null;
		}

		List<AuthorizerItem> items = authorGateProduct.list();
		
		for (AuthorizerItem e : items) {
			if (Laxkit.compareTo(e.getAuthorizer(), authorizer) == 0) {
				return e.getSite();
			}
		}
		return null;
	}

	/**
	 * 加载授权者的CALL节点地址
	 * @return 成功返回真，否则假
	 */
	private boolean loadAuthorizerCallSites() {
		// 不允许时，退出
		if (!allow()) {
			return false;
		}
		
		// 取得授权人签名
		List<Siger> authorizers = account.getPassiveAuthorizers();
		// 如果没有，忽略它
		if (authorizers.isEmpty() ) {
			return true;
		}

		for(Siger authorizer : authorizers) {
			// 查找这个授权人的GATE节点地址
			Node hub = findAuthorizerGateSite(authorizer);
			if (hub == null) {
				Logger.error(this, "loadAuthorizerCallSites", "cannot be find %s gate site!", authorizer);
				continue;
			}

			// 生成命令
			Siger conferrer = getUsername();
			TakeAuthorizerCall cmd = new TakeAuthorizerCall(authorizer, conferrer);
			TakeAuthorizerCallHook hook = new TakeAuthorizerCallHook();
			hook.setTimeout(getShiftTimeout());
			ShiftTakeAuthorizerCall shift = new ShiftTakeAuthorizerCall(cmd, hook);
			shift.setHub(hub); // 设置GATE节点地址!

			// 提交，等待！
			boolean success = getCommandPool().press(shift);
			if (success) {
				hook.await();
			}

			TakeAuthorizerCallProduct product = hook.getProduct();
			// 失败，退出！
			if (product == null) {
				Logger.error(this, "loadAuthorizerCallSites", "cannot be git %s call-site", authorizer);
				return false;
			}

			// 保存它！
			authorCallProducts.add(product);
		}

		Logger.debug(this, "loadAuthorizerCallSites", "product count %d", authorCallProducts.size());

		return true;
	}

	/**
	 * 以新账号为基础，旧账号没有的，保存它
	 */
	private void addNewAccount() {
		// 若不允许，退出！
		if (!allow()) {
			return;
		}
		
		Account old = getStaffPool().getAccount();
		// 新记录保存到这里
		ArrayList<Schema> schemas = new ArrayList<Schema>();
		ArrayList<Table> tables = new ArrayList<Table>();
		// 增加新的授权单元
		ArrayList<PassiveItem> appendItems = new ArrayList<PassiveItem>();
		// 被授权单元不完全一致时，用来更新
		ArrayList<PassiveItem> updateItems = new ArrayList<PassiveItem>();

		// 被授权单元
		for (PassiveItem item : account.getPassiveItems()) {
			// 完成一致时，忽略它
			if (old.hasPassiveItem(item)) {
				continue;
			}
			
			// 旧账号没有这个被授权表，保存它
			if (!old.hasPassiveTable(item.getSpace())) {
				appendItems.add(item);
			}
			// 旧账号有这个表，但是参数不匹配，更新它
			else {
				updateItems.add(item);
			}
		}

		// 以新账号为基础，增加新的数据库和表
		for (Schema schema : account.getSchemas()) {
			for (Table table : schema.list()) {
				// 旧账号没有这个表，保存它!
				if (!old.hasTable(table.getSpace())) {
					tables.add(table);
				}
			}

			Fame fame = schema.getFame();
			// 有这个数据库，忽略它！
			if (old.hasSchema(fame)) {
				continue;
			}
			// 生成一个副本，清除全部数据表
			Schema sub = schema.duplicate();
			for (Table table : schema.list()) {
				sub.remove(table.getSpace());
			}
			schemas.add(sub);
		}

		// 保存新的数据库和数据表
		for (Schema schema : schemas) {
			getStaffPool().createSchema(schema);
		}
		for (Table table : tables) {
			getStaffPool().createTable(table);
		}
		// 更新被授权单元
		for (PassiveItem item : updateItems) {
			getStaffPool().updatePassiveItem(item);
		}

		if (appendItems.size() > 0) {
			createPassiveItems(appendItems);
		}
	}
	
	/**
	 * 生成授权表
	 * @param items
	 */
	private void createPassiveItems(List<PassiveItem> items) {
		// 统计授权人
		TreeSet<Siger> authorizers = new TreeSet<Siger>();
		for (PassiveItem item : items) {
			authorizers.add(item.getAuthorizer());
		}

		// 去授权人的GATE站点获取全部授权表
		Siger conferrer = getUsername();
		Map<Space, Table> passTables = new TreeMap<Space, Table>();
		for (Siger authorizer : authorizers) {
			TakeAuthorizerTableProduct product = loadAuthorizerTables(
					authorizer, conferrer);
			if (product == null || product.isEmpty()) {
				Logger.warning(this, "createPassiveItems", "cannot be git authorizer's table!");
				continue;
			}
			// 保存被授权表
			for (Table table : product.list()) {
				passTables.put(table.getSpace(), table);
			}
		}

		// 输出被授权单元
		for (PassiveItem item : items) {
			Table table = passTables.get(item.getSpace());
			if (table != null) {
				getStaffPool().createPassiveItem(item);
				getStaffPool().addPassiveTable(table);
			}
		}
	}

	/**
	 * 以旧账号为基础，如果新账号中没有的，删除它！
	 */
	private void removeOldAccount() {
		// 若不允许，退出！
		if (!allow()) {
			return;
		}

		Account old = getStaffPool().getAccount();
		// 旧的无用记录保存到这里
		ArrayList<Schema> schemas = new ArrayList<Schema>();
		ArrayList<Table> tables = new ArrayList<Table>();
		ArrayList<PassiveItem> passItems = new ArrayList<PassiveItem>();

		for (PassiveItem item : old.getPassiveItems()) {
			// 被授权单元表在新账号中不存在，保存准备删除它
			if (!account.hasPassiveTable(item.getSpace())) {
				passItems.add(item);
			}
		}

		for (Schema schema : old.getSchemas()) {
			for (Table table : schema.list()) {
				// 新账号没有这个表，删除它
				if (!account.hasTable(table.getSpace())) {
					tables.add(table);
				}
			}
			Fame fame = schema.getFame();
			// 新的账号没有这个数据库，删除它
			if (!account.hasSchema(fame)) {
				schemas.add(schema);
			}
		}

		// 清除表、数据库、被授权单元
		for (Table table : tables) {
			getStaffPool().dropTable(table.getSpace());
		}
		for (Schema schema : schemas) {
			getStaffPool().dropSchema(schema.getFame());
		}
		for (PassiveItem item : passItems) {
			getStaffPool().dropPassiveItem(item);
		}
	}

	/**
	 * 被授权人加载授权人的数据表，保存到本地
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 */
	private TakeAuthorizerTableProduct loadAuthorizerTables(Siger authorizer, Siger conferrer) {
		TakeAuthorizerTable cmd = new TakeAuthorizerTable(authorizer, conferrer);
		TakeAuthorizerTableHook hook = new TakeAuthorizerTableHook();
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		ShiftTakeAuthorizerTable shift = new ShiftTakeAuthorizerTable(cmd, hook);
		
		// 提交给GATE获取
		boolean success = getCommandPool().press(shift);
		// 延时等待结果
		if (success) {
			hook.await();
		}

		return hook.getProduct();
	}

	/**
	 * 更新授权人站点
	 */
	private void updateAuthroizerGateSites() {
		// 空指针是错误，忽略后面的处理！
		if (authorGateProduct == null) {
			return;
		}

		List<AuthorizerItem> newItems = authorGateProduct.list();

		List<AuthorizerItem> oldItems = AuthroizerGateOnFrontPool.getInstance().getAuthroizeItems();

		ArrayList<AuthorizerItem> items = new ArrayList<AuthorizerItem>();
		// 新单元有，旧单元没有，保存增加它
		for (AuthorizerItem e : newItems) {
			if (!oldItems.contains(e)) {
				items.add(e);
			}
		}
		for (AuthorizerItem item : items) {
			Node hub = item.getSite();
			Siger authorizer = item.getAuthorizer();
			
			// 建立FRONT -> 授权者GATE节点映射
			checkPock(hub);
			// 注册
			boolean success = AuthroizerGateOnFrontPool.getInstance().login(hub, authorizer);
			Logger.note(this, "updateAuthroizerGateSite", success, "%s login to %s", authorizer, hub);
		}

		// 旧单元有，新单元没有，清除它
		items.clear();
		for (AuthorizerItem e : oldItems) {
			if (!newItems.contains(e)) {
				items.add(e);
			}
		}
		for (AuthorizerItem item : items) {
			Node hub = item.getSite();
			Siger authorizer = item.getAuthorizer();
			boolean success = AuthroizerGateOnFrontPool.getInstance().logout(hub, authorizer);
			Logger.note(this, "updateAuthroizerGateSite", success, "%s logout from %s", authorizer, hub);
		}
	}

	/**
	 * 从来自GATE节点的记录集中，提取新警种的CAL节点
	 * @return
	 */
	private List<Node> doNewCallSite() {
		TreeSet<Node> selfSites = new TreeSet<Node>();

		// 自有资源
		if (selflyProduct != null) {
			Map<Node, SpaceSet> spaces = selflyProduct.getSpaces();
			Map<Node, PhaseSet> phases = selflyProduct.getPhases();

			// 自有CALL节点地址
			selfSites.addAll(spaces.keySet());
			selfSites.addAll(phases.keySet());

//			Logger.debug(this, "doNewCallSite", "selfly space sites: %d, selfly phase sites: %d", spaces.size(), phases.size());
		}

		// 授权人CALL节点地址
		TreeSet<Node> authorizerSites = new TreeSet<Node>(); 
		for(TakeAuthorizerCallProduct e : authorCallProducts) {
			authorizerSites.addAll(e.getSites());
		}
//		Logger.debug(this, "doCombinCallSite", "authorizer sites: %d", authorizerSites.size());

		// 合并CALL节点地址
		TreeSet<Node> sites = new TreeSet<Node>();
		sites.addAll(selfSites);
		sites.addAll(authorizerSites);

		return new ArrayList<Node>(sites);
	}

	/**
	 * 清除过期不用的CALL节点
	 */
	private void dropOldCallSites() {
		// 合并CALL节点地址
		List<Node> newCallSites = doNewCallSite();

		// 管理池中存在，但是新记录中没有的，注销它们！
		List<Node> oldCallSites = CallOnFrontPool.getInstance().getHubs();

//		// 显示它们的不同
//		print(newCallSites, oldCallSites);

		ArrayList<Node> sites = new ArrayList<Node>();
		// 旧节点有，新节点没有，删除它
		for (Node node : oldCallSites) {
			if (!newCallSites.contains(node)) {
				sites.add(node);
			}
		}
		// 删除注册的旧节点
		for (Node node : sites) {
			// 注册这个节点
			CallOnFrontPool.getInstance().logout(node);
			// 删除与这个节点关联的表/阶段命名
			boolean success = getStaffPool().remove(node);
			Logger.note(this, "dropOldCallSite", success, "remove %s", node);
		}
	}

	/**
	 * 更新云端空间
	 */
	private void updateCloudFields() {
		// 更新云端空间
		getStaffPool().removeAllCloudFields();
		
		List<CloudField> fields = selflyProduct.getCloudFields();
		if (fields.size() > 0) {
			for (CloudField field : fields) {
				getStaffPool().addCloudField(field);
				
				Node hub = field.getSite();
				// 增加FRONT -> CALL 映像站点
				checkPock(hub);
				// 注册到CALL节点
				boolean exists = CallOnFrontPool.getInstance().hasSite(hub);
				if (!exists) {
					CallOnFrontPool.getInstance().login(hub);
				}
			}
		}
	}
	
	/**
	 * 更新自己账号与CALL节点的联系
	 */
	private void updateSelflyCallSites() {
		// 忽略
		if (selflyProduct == null) {
			return;
		}

		// 更新自己的CALL节点地址
		Map<Node, SpaceSet> spaces = selflyProduct.getSpaces();
		Map<Node, PhaseSet> phases = selflyProduct.getPhases();
		
		// 自有CALL节点地址
		TreeSet<Node> sites = new TreeSet<Node>();
		sites.addAll(spaces.keySet());
		sites.addAll(phases.keySet());

		// 逐个更新
		for (Node node : sites) {
			// 判断节点存在
			boolean success = CallOnFrontPool.getInstance().contains(node);
			if (success) {
				updateTables(node, spaces.get(node));
				updatePhases(node, phases.get(node));
				Logger.debug(this, "updateSelflyCallSites", "existed! %s", node);
				continue;
			}

			// 增加 FRONT -> CALL 映像站点
			checkPock(node);
			// 注册到新的CALL节点
			success = CallOnFrontPool.getInstance().login(node);
			// 成功，更新
			if (success) {
				updateTables(node, spaces.get(node));
				updatePhases(node, phases.get(node));
			}
			Logger.note(this, "updateSelflyCallSites", success, "login to %s", node);
		}
		
		// 更新云端空间
		updateCloudFields();
	}

	/**
	 * 更新一个授权人与CALL节点记录
	 * @param product
	 */
	private void updateAuthorizerCallSite(TakeAuthorizerCallProduct product) {
		Map<Node, SpaceSet> authorizerSet = product.getSpaces();
		for (Node node : authorizerSet.keySet()) {
			// 判断节点存在
			boolean success = CallOnFrontPool.getInstance().contains(node);
			if (success) {
				updateTables(node, authorizerSet.get(node));
				Logger.debug(this, "updateAuthorizerCallSite", "authorizer existed! %s", node);
				continue;
			}

			// 增加 FRONT -> CALL 映像站点
			checkPock(node);
			// 注册到新的CALL节点
			success = CallOnFrontPool.getInstance().login(node);
			// 成功，更新
			if (success) {
				SpaceSet set = authorizerSet.get(node);
				updateTables(node, set);

				// 加载授权表
				Siger conferrer = getUsername(); 
				Siger authorizer = product.getAuthorizer();
				loadAuthorizerTable(authorizer, conferrer);
			}
			Logger.note(this, "updateAuthorizerCallSite", success, "authorizer login to %s", node);
		}
	}

	/**
	 * 更新授权人与CALL节点的记录
	 */
	private void updateAuthorizerCallSites() {
		// 更新授权人提供的表
		for (TakeAuthorizerCallProduct sub : authorCallProducts) {
			updateAuthorizerCallSite(sub);
		}
	}

	/**
	 * 根据节点，更新它关联的表
	 * @param node 节点
	 * @param set 表集合
	 */
	private void updateTables(Node node, SpaceSet set) {
		if (node == null || set == null) {
			return;
		}
		// 保存数据表名和它的站点
		for (Space space : set.list()) {
			getStaffPool().addTableSite(node, space);
			//			boolean success = getStaffPool().addTableSite(node, space);
			//			Logger.debug(this, "updateTables", success, "save '%s %s'", node, space);
		}
	}

	/**
	 * 根据节点，更新它关联的阶段命名
	 * @param node 节点
	 * @param set 表集合
	 */
	private void updatePhases(Node node, PhaseSet set) {
		if (node == null || set == null) {
			return;
		}
		// 保存阶段命名和它的站点
		for (Phase phase : set.list()) {
			getStaffPool().addTaskSite(node, phase);
			//			boolean success = getStaffPool().addTaskSite(node, phase);
			//			Logger.debug(this, "updatePhases", success, "save '%s %s'", node, phase);
		}
	}

//	/**
//	 * 打印结果
//	 * @param froms
//	 * @param locals
//	 */
//	private void print(Collection<Node> froms, Collection<Node> locals) {
//		Logger.debug(this, "print", "invoker id:%d, new count: %d, old count: %d",
//				getInvokerId(),	froms.size(), locals.size());
//		for(Node e : froms) {
//			Logger.debug(this, "print", "id: %d, new site: %s", getInvokerId(), e);
//		}
//		for(Node e : locals) {
//			Logger.debug(this, "print", "id: %d, old site: %s", getInvokerId(), e);
//		}
//	}

	/**
	 * 被授权人加载授权人的数据表，保存到本地
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 */
	private boolean loadAuthorizerTable(Siger authorizer, Siger conferrer) {
		TakeAuthorizerTable cmd = new TakeAuthorizerTable(authorizer, conferrer);
		TakeAuthorizerTableHook hook = new TakeAuthorizerTableHook();
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		ShiftTakeAuthorizerTable shift = new ShiftTakeAuthorizerTable(cmd, hook);
		// 提交给GATE获取
		boolean success = getCommandPool().press(shift);
		// 延时等待结果
		if (success) {
			hook.await();
		}

		TakeAuthorizerTableProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			// 保存被授权的数据表
			for (Table table : product.list()) {
				boolean b = getStaffPool().addPassiveTable(table);
				Logger.debug(this, "loadAuthorizerTable", b, "save table: %s", table);
			}
		}
		return success;
	}

	/**
	 * 更新账号中的资源
	 */
	private void updateAccount() {
		addNewAccount();
		removeOldAccount();
	}
	
	/**
	 * 更新CALL节点
	 */
	private void updateCallSites() {
		// 注销无用、废弃的CALL
		dropOldCallSites();
		// 更新自己的与CALL节点关联的表
		updateSelflyCallSites();
		// 更新授权表
		updateAuthorizerCallSites();
	}

	/**
	 * 更新资源，顺序包括：<br><br>
	 * 
	 * 1. 更新授权人的GATE站点（必须第一步，只有当建立连接后才能获取相关各种资源）<br>
	 * 2. 增加/删除原来账号中的数据库、数据表、授权单元 <br>
	 * 3. 更新CALL节点和授权人的CALL节点地址 <br><br>
	 */
	private void update() {
		// 若不允许，退出
		if (!allow()) {
			return;
		}
		// 更新...
		updateAuthroizerGateSites();
		updateAccount();
		updateCallSites();
	}

}