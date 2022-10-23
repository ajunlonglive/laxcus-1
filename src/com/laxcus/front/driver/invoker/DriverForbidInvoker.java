/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.command.forbid.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.forbid.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * FRONT.DRIVER站点的禁止操作调用器 <br>
 * 
 * 禁止操作调用器提供了处理步骤流程，子类将按照规定的步骤实现抽象方法和执行处理。
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public abstract class DriverForbidInvoker extends DriverInvoker {

	/** 序列化处理步骤 **/
	private SerialStage stage = SerialStage.INSURE;

	/** 禁止操作表 **/
	private ForbidSheet sheet = new ForbidSheet();

	/**
	 * 构造FRONT.DRIVER站点禁止操作调用器
	 * @param mission 驱动任务
	 */
	protected DriverForbidInvoker(DriverMission mission) {
		super(mission);
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
	 * 保存一批禁止操作单元
	 * @param e 禁止操作单元表
	 * @return 返回新保存的成员数目
	 */
	protected int addForbidItems(ForbidSheet e) {
		Laxkit.nullabled(e);

		// 保存全部
		return sheet.addAll(e);
	}

	/**
	 * 保存一个禁止操作
	 * @param item 禁止操作单元
	 * @return 返回新增加的成员数目
	 */
	protected int addForbidItem(ForbidItem item) {
		return addForbidItems(new ForbidSheet(item));
	}

	/**
	 * 保存一组禁止操作单元
	 * @param items 禁止操作单元数组
	 * @return 返回新增加的成员数目
	 */
	protected int addForbidItem(ForbidItem[] items) {
		return addForbidItems(new ForbidSheet(items));
	}

	/**
	 * 保存一组禁止操作单元
	 * @param items 禁止操作表
	 * @return 返回新增加的成员数目
	 */
	protected int addForbidItems(List<ForbidItem> items) {
		return addForbidItems(new ForbidSheet(items));
	}

	/**
	 * 返回保存的禁止操作单元
	 * @return 禁止操作单元列表
	 */
	protected List<ForbidItem> getForbidItems() {
		return sheet.list();
	}

	/**
	 * 执行禁止操作的数据处理流程
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		// 1. 向AID站点申请禁止操作
		if (stage == SerialStage.INSURE) {
			boolean success = insure();
			// 成功，进入“检查申请”阶段和正常退出；失败，退出和释放资源
			if (success) {
				stage = SerialStage.CHECK_INSURE;
				return true;
			} else {
				return useful(false);
			}
		}
		// 2. 判断AID的禁止操作处理结果
		if (stage == SerialStage.CHECK_INSURE) {
			boolean success = checkInsure();
			// 不成功退出和释放资源，否则进入“数据处理”阶段
			if (!success) {
				return useful(false);
			}
			stage = SerialStage.PROCESS;
		}
		// 3. 执行数据处理工作
		if (stage == SerialStage.PROCESS) {
			boolean finished = false;
			try {
				finished = process();
			} catch (Throwable e) {
				Logger.fatal(e);
				finished = true; // 出现未知故障，默认处理完成
			}
			// 没有完成，返回“真”退出（以后继续处理）；否则进入“撤销业务”阶段。
			if (!finished) {
				return true;
			}
			stage = SerialStage.REVOKE;
		}
		// 4. 撤销禁止操作
		if (stage == SerialStage.REVOKE) {
			boolean success = revoke();
			// 成功，进入检查撤销阶段和正常退出；失败，退出和释放资源
			if (success) {
				stage = SerialStage.CHECK_REVOKE;
				return true;
			} else {
				return useful(false); // 撤销失败
			}
		}
		// 5.检查撤销的处理结果，完全退出和释放资源
		if (stage == SerialStage.CHECK_REVOKE) {
			boolean success = checkRevoke();
			return useful(success);
		}
		// 不是以上任何一个阶段，是错误，退出
		return false;
	}

	/**
	 * 向AID站点申请禁止操作请求
	 * @return 禁止操作投递到AID站点，AID站点接受返回真，否则假。
	 */
	private boolean insure() {
		if (sheet.isEmpty()) {
			Logger.error(this, "insure", "forbid sheet is empty!");
			return false;
		}

		// 建立申请禁止操作的命令
		CreateForbid cmd = new CreateForbid(sheet.list());
		// 提交命令
		Node hub = getHub();
		boolean success = completeTo(hub, cmd);
		
		// 不成功，生成错误提示
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		Logger.debug(this, "insure", success, "submit to %s", hub);

		return success;
	}

	/**
	 * 检查向AID投递的禁止操作申请
	 * @return 成功返回真，否则假
	 */
	private boolean checkInsure() {
		int index = findEchoKey(0); 
		
		// 如果出错，显示错误提示
		if (isFaultCompleted(index)) {
			faultX(FaultTip.FAILED_X, getCommand());
			return false;
		}

		CreateForbidProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateForbidProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}
		boolean success = (product != null);
		if (success) {
			Logger.debug(this, "checkInsure", "this is '%s'", product);
			// 判断返回成员数目一致，是成功
			success = (product.size() == sheet.size());
		}
		
		// 不成功，提示错误
		if (!success) {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		Logger.debug(this, "checkInsure", success, "result is");
		return success;
	}

	/**
	 * 撤销禁止操作申请
	 * @return 投递到AID站点，且AID接受返回真，否则假
	 */
	private boolean revoke() {
		DropForbid cmd = new DropForbid(sheet.list());
		// 提交命令到AID站点
		Node hub = getHub();
		boolean success = completeTo(hub, cmd);
		
		Logger.debug(this, "revoke", success, "submit to %s", hub);

		return success;
	}

	/**
	 * 检查撤销禁止操作申请
	 * @return 撤销成功返回真，否则假。
	 */
	private boolean checkRevoke() {
		int index = findEchoKey(0);
		
		DropForbidProduct product = null;
		try {
			if (isObjectable(index)) {
				product = getObject(DropForbidProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}
		boolean success = (product != null);
		if (success) {
			Logger.debug(this, "checkRevoke", "this is '%s'", product);
			success = (product.size() == sheet.size());
		}

		Logger.debug(this, "checkRevoke", success, "helo");
		return success;
	}

	/**
	 * 执行禁止操作阶段中的数据处理 <br>
	 * 这个方法由子类根据各自需求去实现。
	 * 
	 * @return 当数据处理工作全部完成时，返回真（无论数据处理是错误或者失败）；否则假。<b>特别说明：数据处理错误也要返回“真”。<b>
	 */
	protected abstract boolean process();

}