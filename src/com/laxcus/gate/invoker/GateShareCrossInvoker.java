/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.cross.*;
import com.laxcus.command.refer.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 共享资源授权调用器。<br><br>
 * 
 * 由数据持有人操作。<br>
 * 流程：<br>
 * 1. 找到授权人的ACCOUNT站点地址（没有找到错误退出）<br>
 * 2. 找到全部被授权人的ACCOUNT站点地址（不足或者失效，错误退出）<br>
 * 3. 授权人向自己的账号注入授权单元，返回授权结果<br>
 * 4. 根据授权结果，向被授权人账号注入授权记录 <br>
 * 5. 返回被授权人的注入记录，最终结果以被授权人的注入记录为准。<br><br>
 * 
 * 说明：如果存在授权人和被授权人之间的授权记录不一致的情况，应是网络故障所致，这里以被授权人记录为准，授权人重新授权一次即可。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public abstract class GateShareCrossInvoker extends GateSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/** 授权人的ACCOUNT站点 **/
	private Node master;

	/** 被授权人的ACCOUNT站点 **/
	private TreeSet<Seat> slaves = new TreeSet<Seat>();

	/** 授权人报告 **/
	private ShareCrossProduct product;

	/**
	 * 构造共享资源授权调用器，制定命令
	 * @param cmd 共享资源授权
	 */
	protected GateShareCrossInvoker(ShareCross cmd) {
		super(cmd);
		step = 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShareCross getCommand() {
		return (ShareCross) super.getCommand();
	}
	
	/**
	 * 输出授权人签名
	 * @return 签名
	 */
	protected Siger getAuthorizer() {
		return getCommand().getIssuer();
	}

	/**
	 * 输出全部被授权人签名
	 * @return 签名集合
	 */
	protected List<Siger> getConferrers() {
		return getCommand().getConferrers();
	}

	/**
	 * 在BANK子域集群范围内更新账号
	 */
	private void doRefreshAccount() {
		// 在本地更新自己的账号
		ArrayList<Command> array = new ArrayList<Command>();
		// 自己的账号
		RefreshAccount sub = new RefreshAccount(master, getAuthorizer());
		array.add(sub);
		// 被授权者账号
		for (Seat seat : slaves) {
			array.add(new RefreshAccount(seat));
		}
		directToHub(array);
	}

	/**
	 * TOP/HOME集群更新资源引用
	 */
	private void doRefreshRefer() {
		// 投递到BANK站点，通过TOP站点转发给HOME站点
		ArrayList<Command> array = new ArrayList<Command>();
		// 自己的账号
		Seat sub = new Seat(getAuthorizer(), master);
		array.add(new RefreshRefer(sub));
		// 被授权者
		for (Seat seat : slaves) {
			array.add(new RefreshRefer(seat));
		}
		// 发送全部
		directToHub(array);
	}

	/**
	 * 广播
	 */
	protected void multicast() {
		doRefreshAccount();
		doRefreshRefer();
	}

	/**
	 * 第一步：去找授权人账号地址
	 * @return 成功返回真，否则假
	 */
	protected boolean doFirst() {
		// 授权人
		Siger authorizer = getAuthorizer();
		return seekSite(authorizer);
	}

	/**
	 * 第二步：找到授权人站点，去找被授权人站点
	 * @return 成功返回真，否则假
	 */
	protected boolean doSecond() {
		// 找到授权人
		master = replySite();
		boolean success = (master != null);
		// 找被授权人
		if (success) {
			success = seekSites(getConferrers(), true);
		}
		return success;
	}

	/**
	 * 第三步：找到全部被授权人站点，向ACCONT站点发送授权命令
	 * @return 成功返回真，否则假
	 */
	protected boolean doThird() {
		List<Seat> a = replySites();
		slaves.addAll(a);
		boolean success = (slaves.size() == getConferrers().size());
		if (success) {
			Command cmd = getCommand();
			success = launchTo(master, cmd);
		}
		return success;
	}
	
	/**
	 * 第四步：接受ACCOUNT的反馈结果，把结果投递给被授权人
	 * @return 成功返回真，否则假
	 */
	protected boolean doFourthly() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShareCrossProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 两种情况：1，发生错误，2.处理是空集
		if (product == null) {
			return false;
		} else if (product.isEmpty()) {
			replyProduct(product);
			return useful(true);
		}

		// 把结果分配给不同的ACCOUNT站点
		Map<Siger, CrossField> fields = new TreeMap<Siger, CrossField>();
		for (ShareCrossItem item : product.list()) {
			Siger conferrer = item.getSiger(); // 被授权人
			CrossField field = fields.get(conferrer);
			if (field == null) {
				field = new CrossField(getAuthorizer(), conferrer);
				fields.put(conferrer, field);
			}
			// 保存共享标识
			field.add(item.getFlag());
		}
		
		// 保存参数
		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		for (Seat seat : slaves) {
			Siger conferrer = seat.getSiger(); // 被授权人
			CrossField field = fields.get(conferrer);
			if (field != null) {
				// 子类建立对应的强制授权命令，分别是AwardOpenActiveItem / AwardCloseActiveItem
				AwardShareCross award = createAward();
				// 保存共享资源
				award.setField(field);
				CommandItem item = new CommandItem(seat.getSite(), award);
				items.add(item);
			}
		}
		
		// 以容错模式，向多个ACCOUNT站点发送强制开放被授权单元命令
		int count = incompleteTo(items);
		boolean success = (count > 0);
		return success;
	}
	
	/**
	 * 第五步：接收多个被授权单元的反馈结果
	 * @return 成功返回真，否则假。
	 */
	protected boolean doFifthly() {
		List<Integer> keys = getEchoKeys();
		ShareCrossProduct reply = new ShareCrossProduct();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					ShareCrossProduct sub = getObject(ShareCrossProduct.class, index);
					reply.addAll(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 输出解除关联的授权单元
		boolean success = replyProduct(reply);

		Logger.debug(this, "doFifthly", "keys:%d, flag item is %d", keys.size(), reply.size());

		return success;
	}

	/**
	 * 迭代执行
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
			success = doFourthly();
			break;
		case 5:
			success = doFifthly();
			break;
		}
		// 自增1
		step++;

		// 达到以下条件，退出
		if (!success || isQuit() || step > 5) {
			if (success) {
				multicast();
			} else {
				failed();
			}
			setQuit(true);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
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
	 * 子类建立一个对应的强制授权命令
	 * @return 强制分享资源
	 */
	protected abstract AwardShareCross createAward();
	
}