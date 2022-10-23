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
import com.laxcus.command.access.permit.*;
import com.laxcus.command.relate.*;
import com.laxcus.command.site.entrance.*;
import com.laxcus.command.site.front.*;
import com.laxcus.front.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * 加载分布资源调用器 <br>
 * FRONT节点登录成功后操作！
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class FrontLoadScheduleInvoker extends FrontInvoker {

	/**
	 * 构造加载分布资源调用器，指定命令
	 * @param cmd 刷新网络空间
	 */
	public FrontLoadScheduleInvoker(LoadSchedule cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LoadSchedule getCommand() {
		return (LoadSchedule) super.getCommand();
	}
	
	/**
	 * 返回转发命令超时时间
	 * @return 以毫秒计
	 */
	private long getShiftTimeout() {
		return getCommand().getShiftTimeout();
	}
	
	/**
	 * 判断获得许可，必须是在登录状态
	 * @return 返回真或者假
	 */
	private boolean allow() {
		return isLogined();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 没有许可，不执行
		if (!allow()) {
			return useful(false);
		}
		// 不能锁定，忽略它
		if (!LoadSchedule.lock()) {
			Logger.error(this, "launch", "cannot be lock!");
			return useful(false);
		}
		
		// 在窗口显示文本，驱动则忽略
		String text = getLauncher().message(MessageTip.LOADING_USER_RESOURCE);
		getLauncher().showStatusText(text);
		
		// 首先确定当前用户的级别。如果是管理员，没有账号。如果是用户，拿他/她的账号
		boolean success = checkTakeGrade();
		// 成功，当前账号属于用户时，拿账号和配置参数
		if (success) {
			// 刷新启动资源池，无论是管理员或者普通注册用户
			refreshGuidePool();
			
			// 管理员/注册用户
			if (isAdministrator()) {

			} else if (isUser()) {
				// 加载账号资源
				success = doAccount();
				// 拿到关联的CALL
				if (success) {
					success = doCallSites();
				}
			}
		}

		// 显示用户级别
		if (success) {
			// 显示加载成功
			text = getLauncher().message(MessageTip.LOADED_USER_RESOURCE);
			getLauncher().showStatusText(text);
			// 显示运行时间 
			printRuntime();
			// 显示用户级别
			showGrade();
			// 更新END/PUT/NEAR/GUIDE分布任务组件池
			refreshTaskPool();
		} else {
			// 显示加载失败！
			text = getLauncher().fault(FaultTip.LOAD_USER_RESOURCE_FAILED);
			getLauncher().showStatusText(text);
		}

		Logger.note(this, "launch", success, "load distribute resource");
		
		// 解决锁定！
		LoadSchedule.unlock();

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
	 * 检查账号
	 * @return 成功返回真，否则假
	 */
	private boolean doAccount() {
		TakeAccount cmd = new TakeAccount(getUsername());
		TakeAccountHook hook = new TakeAccountHook();
		ShiftTakeAccount shift = new ShiftTakeAccount(cmd, hook);
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		
		// 交给管理池处理
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}

		Account account = hook.getAccount();
		success = (account != null && allow());
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
	}

	/**
	 * 检测授权人地址
	 * @param authorizers 授权人签名集合
	 * @return 成功返回真，否则假
	 */
	private boolean checkAuthorizerSite(List<Siger> authorizers) {
		TakeAuthorizerSite cmd = new TakeAuthorizerSite(authorizers);
		TakeAuthorizerSiteHook hook = new TakeAuthorizerSiteHook();
		ShiftTakeAuthorizerSite shift = new ShiftTakeAuthorizerSite(cmd, hook);
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		
		// 提交
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}
		TakeAuthorizerSiteProduct product = hook.getProduct();
		success = (product != null && allow());
		if (success) {
			setAuthorizerSites(product);
		}
		// 处理结果
		return success;
	}

	/**
	 * 显示授权人站点
	 * @param product
	 */
	private void setAuthorizerSites(TakeAuthorizerSiteProduct product) {
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
//		ShiftTakeAuthorizerCall shift = new ShiftTakeAuthorizerCall(cmd, hook);
//		hook.setTimeoutWithSecond(120); // 2分钟超时
//		
//		// 提交
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
		ShiftTakeAuthorizerCall shift = new ShiftTakeAuthorizerCall(cmd, hook);
		hook.setTimeout(getShiftTimeout()); // 转发延时时间

		// 提交
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}

		TakeAuthorizerCallProduct product = hook.getProduct();
		success = (product != null && allow());
		if (!success) {
			Logger.error(this, "loadAuthorizerCallSite", "cannot be git TakeAuthorizerCallProduct!");
			return false;
		}

		Map<Node, SpaceSet> spaces = product.getSpaces();
		Logger.debug(this, "loadAuthorizerCallSite", "[%s] call site count %d",
				cmd.getAuthorizer(), spaces.size());

		// 保存数据表名和它的站点
		int count = 0;
		for (Node node : spaces.keySet()) {
			SpaceSet set = spaces.get(node);
			for (Space space : set.list()) {
				success = loadAuthorizerCallSite(node, space);
			}
		}

		return count > 0;
	}
	
	private boolean loadAuthorizerCallSite(Node node, Space space) {
		// 判断节点存在
		boolean success = CallOnFrontPool.getInstance().contains(node);
		// 不存在，尝试定位和注册到批定的CALL节点
		if (!success) {
			// 如果在NAT环境，连接CALL节点
			checkPock(node);

			// 注册到指定节点
			success = CallOnFrontPool.getInstance().login(node);
		}

		// 以上成功，保存表
		if (success) {
			getStaffPool().addTableSite(node, space);
		}

		Logger.note(this, "loadAuthorizerCallSite", success, "save <%s %s>", node, space);
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
		ShiftTakeAuthorizerTable shift = new ShiftTakeAuthorizerTable(cmd, hook);
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		
		// 提交
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}
		
		TakeAuthorizerTableProduct product = hook.getProduct();
		success = (product != null && allow());
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
		// 非许可，忽略！
		if (!allow()) {
			return false;
		}
		
		TakeOwnerCall cmd = new TakeOwnerCall(getUsername());
		TakeOwnerCallHook hook = new TakeOwnerCallHook();
		ShiftTakeOwnerCall shift = new ShiftTakeOwnerCall(cmd, hook);
		hook.setTimeout(getShiftTimeout()); // 转发延时时间
		
		// 交给命令池处理
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}
		TakeOwnerCallProduct product = hook.getProduct();
		success = (product != null && allow());
		if (success) {
			// GATE站点的定时检测时间，设置给FRONT节点
			Logger.info(this, "doCallSite", "gate check interval: %d ms", product.getCheckInterval());
			getStaffPool().setCheckInterval(product.getCheckInterval());

			// 设置参数
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
			for (CloudField field : fields) {
				getStaffPool().addCloudField(field);
				
				// 注册到CALL节点
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
	 * 显示CALL站点地址
	 * @param product
	 */
	private void setCallSites(TakeOwnerCallProduct product) {
		// 非许可，忽略！
		if (!allow()) {
			return;
		}
		
		// 取参数
		Map<Node, SpaceSet> spaces = product.getSpaces();
		Map<Node, PhaseSet> phases = product.getPhases();

		Logger.debug(this, "setCallSites", "space size:%d, phase size:%d", spaces.size(), phases.size());
		
		// 共同节点
		TreeSet<Node> sites = new TreeSet<Node>();
		sites.addAll(spaces.keySet());
		sites.addAll(phases.keySet());

//		// DEBUG START
//		for (Node node : sites) {
//			Logger.debug(this, "setCallSites", "login site: %s", node);
//		}
//		// DEBUG END

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
				getStaffPool().addTableSite(node, space);
				
				Logger.debug(this, "setCallSites", "save '%s %s', hasTableSite %s",
						node, space, getStaffPool().hasTableSite(space));
			}
		}
		// 保存阶段命名和它的站点
		for (Node node : phases.keySet()) {
			PhaseSet set = phases.get(node);
			for (Phase phase : set.list()) {
				getStaffPool().addTaskSite(node, phase);
				
				Logger.debug(this, "setCallSites", "save '%s %s', hasTaskSite %s",
						node, phase, getStaffPool().hasTaskSite(phase));
			}
		}
		
		// 更新云存储空间
		updateCloudFields(product);
	}
	
	/**
	 * 检查用户权限
	 * @return 返回真或者假
	 */
	private boolean checkTakeGrade() {
		TakeGrade cmd = new TakeGrade();
		TakeGradeHook hook = new TakeGradeHook();
		ShiftTakeGrade shift = new ShiftTakeGrade(cmd, hook);
		hook.setTimeout(getShiftTimeout()); // 转发延时时间

		// 提交命令
		boolean success = getCommandPool().press(shift);
		if (success) {
			hook.await();
		}
		TakeGradeProduct product = hook.getProduct();
		success = (product != null && allow());
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

		// 设置权限级别!
		FrontSite site = launcher.getSite();
		site.setGrade(grade);

		Logger.debug(this, "setTakeGrade", "user is %s", GradeTag.translate(grade));
	}

	/**
	 * 显示用户权级
	 */
	private void showGrade() {
		// 用文字显示用户级别
		getLauncher().showGrade(getGrade());
	}
	
	/**
	 * 显示本次调用运行时间
	 */
	private void printRuntime() {
		long time = getRunTime();
		if (time < 1) {
			return;
		}

		FrontLauncher launcher = getLauncher();
		String input = launcher.message(MessageTip.COMMAND_USEDTIME_X);
		RuntimeFormat e = new RuntimeFormat();
		String text = e.format(input, time);
		launcher.showStatusText(text);
	}
	
	/**
	 * 刷新启动资源池
	 */
	private void refreshGuidePool() {
		GuideTaskPool.getInstance().refresh();
	}
	
	/**
	 * 更新分布任务组件
	 */
	private void refreshTaskPool() {
		// 通知组件线程，更新参数
		EndTaskPool.getInstance().update();
		PutTaskPool.getInstance().update();
		NearTaskPool.getInstance().update();
	}

}


///**
// * 注销全部和清除全部参数
// */
//private void logoutAll() {
//	// 清除被授权人
//	AuthroizerGateOnFrontPool.getInstance().logoutAll();
//	// 注销全部地址
//	CallOnFrontPool.getInstance().logoutAll();
//	// 清除内存记录
//	getStaffPool().clear();
//	// 显示底栏图标
//	getStaffPool().reveal();
//}

///* (non-Javadoc)
// * @see com.laxcus.echo.invoke.EchoInvoker#launch()
// */
//@Override
//public boolean launch() {
//	// 第一步，清除全部
//	logoutAll();
//	
//	// 在窗口显示文本，驱动则忽略
//	String text = getLauncher().message(MessageTip.LOADING_USER_RESOURCE);
//	getLauncher().showStatusText(text);
//	
//	// 首先确定当前用户的级别。如果是管理员，没有账号。如果是用户，拿他/她的账号
//	boolean success = checkTakeGrade();
//	// 成功，当前账号属于用户时，拿账号和配置参数
//	if (success) {
//		// 管理员/注册用户
//		if (isAdministrator()) {
//
//		} else if (isUser()) {
//			success = doAccount();
//			// 拿到关联的CALL
//			if (success) {
//				success = doCallSites();
//			}
//		}
//	}
//
//	// 显示用户级别
//	if (success) {
//		// 显示加载成功
//		text = getLauncher().message(MessageTip.LOADED_USER_RESOURCE);
//		getLauncher().showStatusText(text);
//		// 显示运行时间 
//		printRuntime();
//		// 显示用户级别
//		showGrade();
//		// 更新END/PUT/NEAR分布任务组件池
//		refreshTaskPool();
//	} else {
//		// 显示加载失败！
//		text = getLauncher().fault(FaultTip.LOAD_USER_RESOURCE_FAILED);
//		getLauncher().showStatusText(text);
//	}
//
//	Logger.note(this, "launch", success, "refersh cyber");
//
//	return useful(success);
//}


///**
// * 构造默认的加载分布资源调用器
// */
//public FrontScheduleLoadInvoker() {
//	this(new ScheduleLoad());
//}
