/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.util.*;

/**
 * 删除云端应用软件包调用器
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public abstract class MeetDropCloudPackageInvoker extends MeetRuleInvoker {

	/** 执行步骤 **/
	private int step;

	/** 结果 **/
	private DropCloudPackageProduct product;

	/**
	 * 构造删除云端应用软件包调用器，指定命令
	 * @param cmd 删除云端应用软件包
	 */
	protected MeetDropCloudPackageInvoker(DropCloudPackage cmd) {
		super(cmd, true); // 锁定资源
		// 拒绝管理员操作！
		setRefuseAdministrator(true);
		// 建立事务规则！发布过程中，拒绝所有操作！
		createRule();
		// 执行步骤
		step = 1;
	}

	/**
	 * 建立锁定规则，用户级！
	 */
	private void createRule() {
		UserRuleItem item = new UserRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		addRule(item);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropCloudPackage getCommand() {
		return (DropCloudPackage) super.getCommand();
	}

	/**
	 * 判断只删除本地应用
	 * 
	 * @return 返回真或者假
	 */
	protected boolean isLocal() {
		return getCommand().isLocal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		if (step == 1) {
			// 1. 判断允许删除云端应用软件包
			boolean allow = checkPermission();
			if (!allow) {
				faultX(FaultTip.PERMISSION_MISSING);
				return true;
			}
			// 判断本地有文件
			boolean exists = hasLocal();
			if (!exists) {
				Naming software = getCommand().getWare();
				warningX(WarningTip.NOT_FOUND_LOCAL_X, software);
			}
		}

		// 如果是在本地删除云端时...
		if (isLocal()) {
//			int count = dropLocal();
//			boolean success = (count >= 0);
			
			int count = 0;
			boolean success = dropLocal();
			// 删除引导包，大于等于0是正确，小于0是错误!
			if (success) {
				int ret = dropGuideTask();
				success = (ret >= 0); // 大于等于0是正确！
				if (success) {
					count += ret;
				}
			}
			print(success, count);
			// 无论在本地删除云端成功或者失败，都返回true，表示可以退出！
			return true;
		}

		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			// 清除内存和界面上的显示
			if (success) {
				erase();
			}
			break;
		}
		step++;

		// 不成功，或者大于2时，返回“真”退出！
		if (!success || step > 2) {
			// 打印结果
			print();
			return true;
		}

		return false;
	}

	/**
	 * 清除界面上显示的阶段命名
	 */
	private void erase() {
		DropCloudPackage cmd = getCommand();
		Naming software = cmd.getWare();
		Siger issuer = null;
		if (!cmd.isLocal()) {
			issuer = getUsername();
		}
		if (issuer == null) {
			return;
		}

		List<Phase> phases = getStaffPool().getPhases();
		ArrayList<Phase> array = new ArrayList<Phase>();
		for (Phase phase : phases) {
			boolean success = (Laxkit.compareTo(phase.getIssuer(), issuer) == 0 && 
					Laxkit.compareTo(phase.getWare(), software) == 0);
			if (success) {
				array.add(phase);
			}
		}
		// 清除内存中保存的阶段命名，同时清除界面上的显示
		for (Phase phase : array) {
			getStaffPool().dropTask(phase);
		}
	}

	/**
	 * 打印结果
	 */
	private void print() {
		boolean success = (product != null && product.hasSuccessful());
		int elements = 0;
		if (success) {
			elements = product.getRights();
		}
		print(success, elements);
	}

	/**
	 * 打印结果
	 * @param success 成功
	 * @param elements 删除云端单元数目
	 */
	private void print(boolean success, int elements) {
		DropCloudPackage cmd = getCommand();
		Naming software = cmd.getWare();

		// 显示处理时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "DROP-CLOUD-PACKAGE/STATUS", "DROP-CLOUD-PACKAGE/COUNT", "DROP-CLOUD-PACKAGE/SOFTWARE" });
		// 显示结果
		Object[] a = new Object[] { success, elements, software };
		printRow(a);

		// 输出全部记录
		flushTable();
	}

	/**
	 * 尝试向GATE节点发出命令
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 投递给GATE节点
		return fireToHub();
	}

	/**
	 * 接收GATE节点反馈
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		product = readProduct();
		boolean success = (product != null && product.isSuccessful());
		// 删除云端本地
		if (success) {
//			int count = dropLocal();
//			success = (count > 0);
			
			int count = 0;
			success = dropLocal();
			// 删除GUIDE组件
			if (success) {
				int ret = dropGuideTask();
				success = (ret >= 0); // 大于等于0是正确！
				if (success) {
					count += ret;
				}
			}
			// 重置结果
			product.setSuccessful(success);
			if (success) product.addRights(count);
		}
		return success;
	}

	/**
	 * 从硬盘或者内存读取结果
	 * @return DropCloudPackageProduct实例
	 */
	private DropCloudPackageProduct readProduct() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				return getObject(DropCloudPackageProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 删除引导文件
	 * @return 返回删除的文件数目，负数是错误
	 */
	private int dropGuideTask() {
		DropCloudPackage cmd = getCommand();
		Naming software = cmd.getWare();
		return GuideTaskPool.getInstance().drop(software);
	}

	/**
	 * 检查权限，判断允许发布云计算应用！
	 * @return 返回真或者假
	 */
	protected abstract boolean checkPermission();

	/**
	 * 检查本地是否有应用的软件
	 * @return 存在返回真，否则假
	 */
	protected abstract boolean hasLocal();

	/**
	 * 删除本地的文件
	 * @return 返回删除文件数目
	 */
	protected abstract boolean dropLocal();



}	