/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.entrance.*;
import com.laxcus.visit.*;
import com.laxcus.util.*;

/**
 * 建立用户账号调用器。
 * 这是串行处理命令。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class BankCreateUserInvoker extends BankInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/** ACCOUNT节点 **/
	private Node remote;

	/**
	 * 构造建立用户账号命令，指定转发命令
	 * @param shift 建立用户账号命令
	 */
	public BankCreateUserInvoker(CreateUser shift) {
		super(shift);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateUser getCommand() {
		return (CreateUser) super.getCommand();
	}
	
	/**
	 * 输出签名
	 * @return
	 */
	private Siger getSiger() {
		CreateUser cmd = getCommand();
		return cmd.getUsername();
	}

	/**
	 * 通知HASH站点增加一个用户签名
	 */
	private void pushToHashSites() {
		// 生成命令
		Siger siger = getSiger();
		RefreshAccount cmd = new RefreshAccount(remote, siger);
		
		// 拿到全部HASH站点
		List<Node> slaves = HashOnBankPool.getInstance().detail();
		directTo(slaves, cmd);
	}
	
	/**
	 * 通知全部HASH站点，释放一个账号
	 */
	private void dropFromHashSites() {
		Siger siger = getSiger();
		AwardDropRefer award = new AwardDropRefer(siger);

		// 拿到全部HASH站点
		List<Node> slaves = HashOnBankPool.getInstance().detail();
		directTo(slaves, award);
	}

	/**
	 * 反馈处理结果
	 * @param successful 建立账号成功
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean successful) {
		CreateUserProduct product = new CreateUserProduct(getSiger(), successful);
		if (successful) {
			// 选择一个ENTRANCE站点通知FRONT站点
			Node node = EntranceOnBankPool.getInstance().next();
			// 找到站点实例，输出内网/公网地址
			EntranceSite site = (EntranceSite) EntranceOnBankPool.getInstance().find(node);
			successful = (site != null);
			if (successful) {
				product.setEntrance(site.getPrivate(), site.getPublic());
			} else {
				// 如果没有找到站点，撤销账号
				product.setSuccessful(false);
				// 记录这个账号，交给另一个管理池撤销它！（这段代码没有做！！！）
			}
		}
		// 发送给GATE站点
		return replyProduct(product);
	}
	
	/**
	 * 判断账号存在于故障池中
	 * @return 返回真或者假
	 */
	private boolean isFaultUser() {
		// 如果故障池中存储某个签名，拒绝操作
		Siger siger = getSiger();
		return FaultOnBankPool.getInstance().hasAccount(siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 1. 如果故障池中存储某个签名，拒绝操作
		boolean success = isFaultUser();
		if (success) {
			refuse();
			return false;
		}
		// 执行
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 执行操作
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		case 4:
			success = doFourth();
			break;
		}
		step++;

		// 符合退出要求
		if (!success || step > 4) {
			// 不成功，从HASH站点释放这个签名
			if (!success) {
				dropFromHashSites();
			}
			// 反馈结果给GATE，GATE将原样转发给FRONT
			reply(success);

			// 通知线程退出
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 第一步：向全部ACCOUNT节点查询某个账号
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger siger = getSiger();
		AssertUser cmd = new AssertUser(siger);

		// 投递到全部ACCOUNT站点
		List<Node> slaves = AccountOnBankPool.getInstance().detail();
		return launchTo(slaves, cmd);
	}

	/**
	 * 第二步：顺序选择一个ACCOUNT站点，发送给它！
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		// 诊断查询结果
		int count = 0;
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssertUserProduct e = getObject(AssertUserProduct.class, index);
					if (e.isSuccessful()) {
						count++;
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 判断找到
		boolean success = (count > 0);
		if (success) {
			return false;
		}
		
		/*
		 * 根据签名和当前ACCOUNT站点总数，定位一个ACCOUNT站点，命令提交给它！
		 */
		CreateUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		remote = AccountOnBankPool.getInstance().locate(siger);
		
		// 投递命令到ACCOUNT站点
		success = (remote != null);
		if (success) {
			success = launchTo(remote, cmd);
		}
		return success;
	}

	/**
	 * 第三步：接收ACCOUNT反馈，成功投递给TOP站点继续完成，失败返回给GATE站点。
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		CreateUser cmd = getCommand();

		CreateUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		Logger.debug(this, "doThird", success, "create %s", cmd.getUser());

		// 不成功，直接反馈拒绝！
		if (!success) {
			return false;
		}
		
		// 向全部HASH站点推送账号
		pushToHashSites();

		// 把命令投递给TOP站点
		success = launchToHub(cmd);
		// 如果不成功，在线撤销删除账号操作
		if (!success) {
			// 撤销
			boolean released = rescind();
			// 如果撤销不成功，把这个账号记录保存下来，留以后删除
			if (!released) {
				FaultOnBankPool.getInstance().push(cmd);
			}
		}

		return success;
	}

	/**
	 * 第四步：接收来自TOP站点的反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doFourth() {
		CreateUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		boolean success = (product != null && product.isSuccessful());
		
		Logger.debug(this, "doFourth", success, "from %s", getHub());
		
		// 在TOP站点建立账号不成功，撤销它
		if (!success) {
			boolean released = rescind();
			if (!released) {
				FaultOnBankPool.getInstance().push(getCommand());
			}
		}

		return success;
	}

	/**
	 * 如果账号建立不成功，删除ACCOUNT上记录
	 */
	private boolean rescind() {
		Siger siger = getSiger();
		DropUser sub = new DropUser(siger);
		DropUserHook hook = new DropUserHook();
		ShiftDropUser shift = new ShiftDropUser(remote, sub, hook);
		shift.setIssuer(getIssuer());

		boolean success = getCommandPool().press(shift);
		if (!success) {
			return false;
		}
		// 等待
		hook.await();
		// 反馈结果
		DropUserProduct product = hook.getProduct();
		success = (product != null && product.isSuccessful());

		Logger.debug(this, "rescind", success, "from %s", remote);
		
		return success;
	}

}