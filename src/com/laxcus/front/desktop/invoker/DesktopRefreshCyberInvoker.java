/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.permit.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * 更新私有网络空间调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopRefreshCyberInvoker extends DesktopInvoker {
	
	/** 延时处理  **/
	private long delayTime;

	/**
	 * 构造更新私有网络空间调用器，指定命令
	 * @param cmd 更新私有网络空间
	 */
	public DesktopRefreshCyberInvoker(RefreshCyber cmd) {
		super(cmd);
		setDelayTime(0);
	}

	/**
	 * 设置为延时处理时间
	 * @param ms 延时时间
	 */
	public void setDelayTime(long ms) {
		delayTime = ms;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshCyber getCommand() {
		return (RefreshCyber) super.getCommand();
	}

	/**
	 * 注销全部和清除全部参数
	 */
	private void logoutAll() {
		// 清除被授权人
		AuthroizerGateOnFrontPool.getInstance().logoutAll();
		// 注销全部地址
		CallOnFrontPool.getInstance().logoutAll();
		// 清除内存记录
		getStaffPool().clear();
		// 显示底栏图标
		getStaffPool().reveal();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
//		// 离线状态
//		if (isOffline() || !isLogined()) {
//			faultX(FaultTip.OFFLINE_REFUSE);
//			return false;
//		}
		
		// 设置延时时间
		if (delayTime > 0) {
			delay(delayTime);
		}

		// 清除全部
		logoutAll();
		
		// 打印标题
		printTitle();

		// 逐一判断和检查
		boolean success = checkTakeGrade();
		// 如果是注册用户，取账号和授权地址
		if (success && isUser()) {
			// 检索账号
			success = doAccount();
			// 检索关联的CALL节点地址！
			if (success) {
				success = doCallSites();
			}
		}
		
		// 显示运行时间
		printRuntime();

		// 输出全部
		flushTable();

		// 在状态栏显示
		if (success) {
			showGrade();
		}
		
		Logger.note(this, "launch", success, "refresh cyber");

		// 完成，退出！
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
//	/**
//	 * 如果位于内网，确认NAT地址
//	 * @param set
//	 */
//	private void checkPocks(Set<Node> set) {
//		// 逐一判断
//		for(Node node : set) {
//			checkPock(node);
//		}
//	}
	
	/**
	 * 显示标题
	 */
	private void printTitle() {
		// 设置标题
		createShowTitle(new String[] { "REFRESH-CYBER/T1", "REFRESH-CYBER/T2" });
	}

	/**
	 * 打印空行
	 */
	private void printGap() {
		int count = 2;
		ShowItem item = new ShowItem();
		for (int i = 0; i < count; i++) {
			ShowStringCell e = new ShowStringCell(i, "  ");
			item.add(e);
		}
		addShowItem(item);
	}

	/**
	 * 显示一行
	 * @param xmlTitlePath XML标题路径
	 * @param value 参数值
	 */
	private void printItem(String xmlTitlePath, String value) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlTitlePath);
		java.awt.Color color = findXMLForeground(xmlTitlePath, java.awt.Color.BLACK);
		item.add(new ShowStringCell(0, name, color));
		item.add(new ShowStringCell(1, value));
		addShowItem(item);
	}

	/**
	 * 显示一行
	 * @param xmlTitlePath
	 * @param value
	 */
	private void printItem(String xmlTitlePath, Object value) {
		printItem(xmlTitlePath, value.toString());
	}

	/**
	 * 检查账号
	 * @return 成功返回真，否则假
	 */
	private boolean doAccount() {
		TakeAccount cmd = new TakeAccount(getUsername());
		TakeAccountHook hook = new TakeAccountHook();
		hook.setTimeoutWithSecond(120); // 2分钟超时
		ShiftTakeAccount shift = new ShiftTakeAccount(cmd, hook);

		// 交给管理池处理
		boolean success = getCommandPool().press(shift, getDisplay());
		if (success) {
			hook.await();
		}

		Account account = hook.getAccount();
		success = (account != null);
		// 保存账号和账号下的配置
		if (success) {
			// 保存账号
			setAccount(account);

			// 判断有被授权人，如果有，取他们的GATE站点地址
			List<Siger> authorizers = account.getPassiveAuthorizers();
			if (authorizers.size() > 0) {
				checkAuthorizerSite(authorizers);
			}
		}
		
		return success;
	}

	/**
	 * 显示账号参数
	 * @param account
	 */
	private void setAccount(Account account) {
		// 设置账号
		getStaffPool().setAccount(account);

		// 以下是数据显示部分！
		List<Table> tables = account.getTables();
		List<PassiveItem> items = account.getPassiveItems();
//		List<Progress> progress = account.getProgresses();
		// 统计，如果是空的，忽略后面显示
//		int size = tables.size() + items.size() + progress.size();
		
		int size = tables.size() + items.size();
		if (size == 0) {
			return;
		}
		
		// 显示表
		for (int index = 0; index < tables.size(); index++) {
			if (index == 0) printGap();
			Table table = tables.get(index);
			printItem("REFRESH-CYBER/ACCOUNT/Table", table);
		}
		// 显示被授权单元
		for (int index = 0; index < items.size(); index++) {
			if (index == 0) printGap();
			PassiveItem item = items.get(index);
			printItem("REFRESH-CYBER/ACCOUNT/PassiveTable", item.getSpace());
		}
	}

	/**
	 * 检测授权人地址
	 * @param authorizers 授权人签名集合
	 * @return 成功返回真，否则假
	 */
	private boolean checkAuthorizerSite(List<Siger> authorizers) {
		TakeAuthorizerSite cmd = new TakeAuthorizerSite(authorizers);
		TakeAuthorizerSiteHook hook = new TakeAuthorizerSiteHook();
		hook.setTimeoutWithSecond(120); // 2分钟超时
		ShiftTakeAuthorizerSite shift = new ShiftTakeAuthorizerSite(cmd, hook);
		
		// 去ENTRANCE站点获取授权人的GATE站点地址
		boolean success = getCommandPool().press(shift, getDisplay());
		if (success) {
			hook.await();
		}
		TakeAuthorizerSiteProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			setAuthorizerSites(product);
		}
		
		Logger.debug(this, "checkAuthorizerSite", success, "result is");
		
		// 处理结果
		return success;
	}

	/**
	 * 显示授权人站点
	 * @param product
	 */
	private void setAuthorizerSites(TakeAuthorizerSiteProduct product) {
		Logger.debug(this, "setAuthorizerSites", "authorizer item size:%d", product.size());
		
		for (AuthorizerItem item : product.list()) {
			Siger authorizer = item.getAuthorizer(); // 授权人
			Node gate = item.getSite(); // GATE站点
			
			Logger.debug(this, "setAuthorizerSites", "authorizer:%s login to: %s", authorizer, gate);
			
			// 建立FRONT -> 授权者GATE节点映射
			checkPock(gate);
			// 注册到GATE站点
			boolean success = AuthroizerGateOnFrontPool.getInstance().login(gate, authorizer);
			Logger.note(this, "setAuthorizerSites", success, "[%s] login to %s", authorizer, gate);

			// 注册成功，去获取授权人的CALL站点和授权表名
			if (success) {
				// 显示在窗口
				printGap();
				printItem("REFRESH-CYBER/AUTHORIZER/Authorizer", authorizer);
				printItem("REFRESH-CYBER/AUTHORIZER/Hub", gate);

				// 被授权人，即当前用户。当前用户的身份是被授权人
				Siger conferrer = getUsername();
				// 加载授权人的CALL站点
				loadAuthorizerCallSite(authorizer, conferrer);
				// 加载授权人的表实例
				loadAuthorizerTable(authorizer, conferrer);
			}
		}
	}

//	/**
//	 * 加载授权人的CALL站点
//	 * @param authorizer 授权人
//	 * @param conferrer 被授权人
//	 */
//	private boolean loadAuthorizerCallSite(Siger authorizer, Siger conferrer) {
//		TakeAuthorizerCall cmd = new TakeAuthorizerCall(authorizer, conferrer);
//		TakeAuthorizerCallHook hook = new TakeAuthorizerCallHook();
//		hook.setTimeoutWithSecond(120); // 2分钟超时
//		ShiftTakeAuthorizerCall shift = new ShiftTakeAuthorizerCall(cmd, hook);
//		boolean success = getCommandPool().press(shift);
//		if (success) {
//			hook.await();
//		}
//		
//		TakeAuthorizerCallProduct product = hook.getProduct();
//		success = (product != null);
//		if (success) {
//			Map<Node, SpaceSet> spaces = product.getSpaces();
//			Logger.debug(this, "loadAuthorizerCallSite", "[%s] call site count %d",
//					cmd.getAuthorizer(), spaces.size());
//
//			// 如果在NAT环境，连接CALL节点
//			checkPocks(spaces.keySet());
//
//			// 保存数据表名和它的站点
//			for (Node node : spaces.keySet()) {
//				SpaceSet set = spaces.get(node);
//				for (Space space : set.list()) {
//					getStaffPool().addTableSite(node, space);
//					
//					printGap();
//					printItem("REFRESH-CYBER/AUTHORIZER-SITE/Authorizer", authorizer);
//					printItem("REFRESH-CYBER/AUTHORIZER-SITE/Table", space);
//					printItem("REFRESH-CYBER/AUTHORIZER-SITE/Hub", node);
//					
//					Logger.debug(this, "loadAuthorizerCallSite", "save '%s %s', hasTableSite %s",
//							node, space, getStaffPool().hasTableSite(space));
//				}
//			}
//		}
//		return success;
//	}


	/**
	 * 加载授权人的CALL站点
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 */
	private boolean loadAuthorizerCallSite(Siger authorizer, Siger conferrer) {
		TakeAuthorizerCall cmd = new TakeAuthorizerCall(authorizer, conferrer);
		TakeAuthorizerCallHook hook = new TakeAuthorizerCallHook();
		hook.setTimeoutWithSecond(120); // 2分钟超时
		ShiftTakeAuthorizerCall shift = new ShiftTakeAuthorizerCall(cmd, hook);
		boolean success = getCommandPool().press(shift, getDisplay());
		if (success) {
			hook.await();
		}

		TakeAuthorizerCallProduct product = hook.getProduct();
		success = (product != null);
		if (!success) {
			Logger.error(this, "loadAuthorizerCallSite", "cannot be git TakeAuthorizerCallProduct!");
			return false;
		}

		Map<Node, SpaceSet> spaces = product.getSpaces();
		Logger.debug(this, "loadAuthorizerCallSite", "[%s] call site count %d",
				cmd.getAuthorizer(), spaces.size());

//		// 如果在NAT环境，连接CALL节点
//		checkPocks(spaces.keySet());

		// 保存数据表名和它的站点
		int count = 0;
		for (Node node : spaces.keySet()) {
			SpaceSet set = spaces.get(node);
			for (Space space : set.list()) {
				// 建立与授权CALL地址的连接注册，保存表地址映像
				success = loadAuthorizerCallSite(node, space);
				// 成功，显示！
				if (success) {
					printGap();
					printItem("REFRESH-CYBER/AUTHORIZER-SITE/Authorizer", authorizer);
					printItem("REFRESH-CYBER/AUTHORIZER-SITE/Table", space);
					printItem("REFRESH-CYBER/AUTHORIZER-SITE/Hub", node);
					count++;
				}
			}
		}

		return count > 0;
	}

	private boolean loadAuthorizerCallSite(Node node, Space space) {
		boolean success = CallOnFrontPool.getInstance().contains(node);
		if (!success) {
			// 如果在NAT环境，连接定位自己
			checkPock(node);
			// 注册到指定CALL节点
			success = CallOnFrontPool.getInstance().login(node);
		}
		if (success) {
			getStaffPool().addTableSite(node, space);
		}

		Logger.note(this, "loadAuthorizerCallSite", success, "save <%s %s>",node, space);

		return success;
	}
	
	/**
	 * 被授权人加载授权人的数据表，保存到本地
	 * @param authorizer 授权人
	 * @param conferrer 被授权人
	 */
	private boolean loadAuthorizerTable(Siger authorizer, Siger conferrer) {
		TakeAuthorizerTable cmd = new TakeAuthorizerTable(authorizer, conferrer);
		TakeAuthorizerTableHook hook = new TakeAuthorizerTableHook();
		hook.setTimeoutWithSecond(120); // 2分钟等待
		ShiftTakeAuthorizerTable shift = new ShiftTakeAuthorizerTable(cmd, hook);
		// 提交给GATE获取
		boolean success = getCommandPool().press(shift, getDisplay());
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
	 * 检查关联CALL站点
	 * @return 返回真或者假
	 */
	private boolean doCallSites() {
		TakeOwnerCall cmd = new TakeOwnerCall(getUsername());
		TakeOwnerCallHook hook = new TakeOwnerCallHook();
		hook.setTimeoutWithSecond(120); // 2分钟超时
		ShiftTakeOwnerCall shift = new ShiftTakeOwnerCall(cmd, hook);
		boolean success = getCommandPool().press(shift, getDisplay());
		if (success) {
			hook.await();
		}
		TakeOwnerCallProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			setCallSites(product);
		}
		return success;
	}
	
	/**
	 * 更新云端空间
	 */
	private void updateCloudFields(TakeOwnerCallProduct product) {
		// 更新云端空间
		getStaffPool().removeAllCloudFields();

		List<CloudField> fields = product.getCloudFields();

		if (fields.size() > 0) {
			printGap();
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
				
				printItem("REFRESH-CYBER/CALL-SITE/CloudHub", hub);
			}
		}
	}

	/**
	 * 显示CALL站点地址
	 * @param product
	 */
	private void setCallSites(TakeOwnerCallProduct product) {		
		// 取参数
		Map<Node, SpaceSet> spaces = product.getSpaces();
		Map<Node, PhaseSet> phases = product.getPhases();
		
		// 显示阶段命名，只包括INIT/ISSUE阶段的命名
		TreeSet<Phase> elements = new TreeSet<Phase>();
		for (PhaseSet set : phases.values()) {
			elements.addAll(set.list());
		}
		ArrayList<String> a = new ArrayList<String>();
		for (Phase e : elements) {
			boolean success = (PhaseTag.isInit(e) || PhaseTag.isIssue(e) ||PhaseTag.isFork(e));
			if (success) {
				a.add(e.getSockText());
			}
		}
		// 组件名称
		if (a.size() > 0) {
			printGap();
			for (String e : a) {
				printItem("REFRESH-CYBER/ACCOUNT/Phase", e);
			}
		}

		Logger.debug(this, "setCallSites", "space size:%d, phase size:%d", spaces.size(), phases.size());
		
		// 共同节点
		TreeSet<Node> sites = new TreeSet<Node>();
		sites.addAll(spaces.keySet());
		sites.addAll(phases.keySet());

		// DEBUG START
		for (Node node : sites) {
			Logger.debug(this, "setCallSites", "login site: %s", node);
		}
		// DEBUG END

		// 注册站点
		for (Node node : sites) {
			// 增加FRONT -> CALL 映像站点
			checkPock(node);
			// 注册到CALL站点
			boolean success = CallOnFrontPool.getInstance().login(node);
			// 如果注册不成功，删除这个节点
			if (!success) {
				spaces.remove(node);
				phases.remove(node);
			}
		}

		// 保存数据表名和它的站点
		for (Node node : spaces.keySet()) {
			SpaceSet set = spaces.get(node);
			for (Space space : set.list()) {
				// 保存
				getStaffPool().addTableSite(node, space);
				
				// 在窗口显示
				printGap();
				printItem("REFRESH-CYBER/CALL-SITE/Table", space);
				printItem("REFRESH-CYBER/CALL-SITE/TableHub", node);
				
				Logger.debug(this, "setCallSites", "save '%s %s', hasTableSite %s",
						node, space, getStaffPool().hasTableSite(space));
			}
		}
		// 保存阶段命名和它的站点
		for (Node node : phases.keySet()) {
			PhaseSet set = phases.get(node);
			for (Phase phase : set.list()) {
				getStaffPool().addTaskSite(node, phase);
				
				// 在窗口上显示
				if (PhaseTag.isInit(phase) || PhaseTag.isIssue(phase) || PhaseTag.isFork(phase)) {
					printGap();
					printItem("REFRESH-CYBER/CALL-SITE/Task", phase);
					printItem("REFRESH-CYBER/CALL-SITE/TaskHub", node);
				}
				
				Logger.debug(this, "setCallSites", "save '%s %s', hasTaskSite %s",
						node, phase, getStaffPool().hasTaskSite(phase));
			}
		}
		
		// 更新云存储
		updateCloudFields(product);
	}
	
	/**
	 * 检查用户权限
	 * @return 返回真或者假
	 */
	private boolean checkTakeGrade() {
		TakeGrade cmd = new TakeGrade();
		TakeGradeHook hook = new TakeGradeHook();
		hook.setTimeoutWithSecond(120); // 2分钟超时
		ShiftTakeGrade shift = new ShiftTakeGrade(cmd, hook);

		boolean success = getCommandPool().press(shift, getDisplay());
		if (success) {
			messageX(MessageTip.COMMAND_ACCEPTED);
			hook.await();
		}
		TakeGradeProduct product = hook.getProduct();
		success = (product != null);
		if (success) {
			setTakeGrade(product);
		}
		return success;
	}
	
	/**
	 * 显示当前用户身份
	 * @param product
	 */
	private void setTakeGrade(TakeGradeProduct product) {
		int grade = product.getGrade();
		FrontLauncher launcher = getLauncher();

		// 设置权级
		FrontSite site = launcher.getSite();
		site.setGrade(grade);

		// 显示
		if (GradeTag.isAdministrator(grade)) {
			String admin = getXMLContent("REFRESH-CYBER/GRADE/Admin");
			printItem("REFRESH-CYBER/GRADE", admin);
		} else if (GradeTag.isUser(grade)) {
			String user = getXMLContent("REFRESH-CYBER/GRADE/User");
			printItem("REFRESH-CYBER/GRADE", user);
		}
		
		// 设置级别
		setClientGrade(grade);
	}
	
	/**
	 * 显示用户级别
	 */
	private void showGrade() {
//		// 如果是桌面用户，显示它
//		if (isDesktop()) {
//			int grade = getGrade();
//			getLauncher().showGrade(grade);
//		}
		
		// 设置级别
		int grade = getGrade();
		getLauncher().showGrade(grade);

		// 设置级别
		setClientGrade(grade);
	}
	
	/**
	 * 设置客户端的级别，与当前一致
	 * @param grade
	 */
	private void setClientGrade(int grade) {
		// 找到注册的数据库客户端，定义他们
		DatabaseClient[] as = PlatformKit.findListeners(DatabaseClient.class);
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			DatabaseClient client = as[i];
			client.setGrade(grade);
		}
	}

}