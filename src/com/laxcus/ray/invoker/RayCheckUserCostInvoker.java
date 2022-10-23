/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.text.*;
import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.ray.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检测用户消耗资源调用器
 * 
 * @author scott.liang
 * @version 1.0 10/11/2022
 * @since laxcus 1.0
 */
public class RayCheckUserCostInvoker extends RayInvoker {

	/**
	 * 检测用户消耗调用器
	 * @param cmd 检测用户消耗记录
	 */
	public RayCheckUserCostInvoker(CheckUserCost cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckUserCost getCommand() {
		return (CheckUserCost) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckUserCost cmd = getCommand();
		// BANK/HOME集群时
		if (isBankHub()) {
			// 拒绝HOME\DATA\WORK\CALL\BUILD节点
			byte[] sites = new byte[]{SiteTag.HOME_SITE, SiteTag.DATA_SITE, SiteTag.WORK_SITE, SiteTag.CALL_SITE, SiteTag.BUILD_SITE};
			for (byte site : sites) {
				if (cmd.hasType(site)) {
					faultX(FaultTip.NOTMATCH_X, SiteTag.translate(site));
					return false;
				}
			}
		} else if (isHomeHub()) {
			// 拒绝BANK\ENTRANCE\GATE\HASH\ACCOUNT节点
			byte[] sites = new byte[]{SiteTag.BANK_SITE, SiteTag.ENTRANCE_SITE, SiteTag.GATE_SITE, SiteTag.HASH_SITE, SiteTag.ACCOUNT_SITE};
			for (byte site : sites) {
				if (cmd.hasType(site)) {
					faultX(FaultTip.NOTMATCH_X, SiteTag.translate(site));
					return false;
				}
			}
		}

		// 投递到HUB站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		CheckUserCostProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(CheckUserCostProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
	}

	private void print(CheckUserCostProduct product) {
		CheckUserCost cmd = getCommand();

		// 显示处理结果
		printRuntime();
		
		RayLauncher launcher = getLauncher();
		String input = launcher.message(MessageTip.SIMPLE_USEDTIME_X);
		RuntimeFormat rt = new RuntimeFormat();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

		// 生成标题
		createShowTitle(new String[] { "CHECK-USER-COST/ISSUER",
				"CHECK-USER-COST/SITE", "CHECK-USER-COST/COMMAND",
				"CHECK-USER-COST/ITERATE",
				"CHECK-USER-COST/INIT-TIME", "CHECK-USER-COST/END-TIME", 
				"CHECK-USER-COST/USED-TIME", 
				 "CHECK-USER-COST/PROCESS-TIME" });

		List<Siger> users = cmd.getUsers();
		int size = users.size();
		int count = 0;

		for (int index = 0; index < size; index++) {
			Siger siger = users.get(index);
			UserCostElement element = product.find(siger);
			// 判断成员有效
			if (element == null) {
				continue;
			}
			if (count > 0) {
				printGap(8);
			}
			count++;

			Siger issuer = element.getIssuer();
			String text = cmd.findText(issuer);
			if (text == null) {
				text = issuer.toString();
			}

			for (UserCostItem item : element.list()) {
				ShowItem sub = new ShowItem();
				sub.add(new ShowStringCell(0, text));
				sub.add(new ShowStringCell(1, SiteTag.translate(item.getFamily())));
				sub.add(new ShowStringCell(2, item.getCommand()));
				sub.add(new ShowIntegerCell(3, item.getIterateIndex())); // 迭代次数
				sub.add(new ShowStringCell(4, sdf.format(new Date(item.getInitTime())))); // 初始时间
				sub.add(new ShowStringCell(5, sdf.format(new Date(item.getEndTime())))); // 结束时间
				long usedTime = item.getEndTime()- item.getInitTime();
				sub.add(new ShowStringCell(6, rt.format(input, usedTime))); // 运行时间
				sub.add(new ShowStringCell(7, rt.format(input, item.getProcessTime()))); // 线程运行时间
				addShowItem(sub);
			}
		}

		// 输出全部记录
		flushTable();
	}

}