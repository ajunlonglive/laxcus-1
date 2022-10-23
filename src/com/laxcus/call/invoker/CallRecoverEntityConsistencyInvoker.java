/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.stub.sign.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 恢复表数据一致性调用器。<br>
 * 
 * CALL站点收集全部关联DATA站点的签名数据，进行比较后，返回处理结果
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class CallRecoverEntityConsistencyInvoker extends CallInvoker {

	/** 操作步骤 **/
	private int step = 1;

	/**
	 * 构造恢复表数据一致性调用器，指定命令
	 * @param cmd 恢复表数据一致性命令
	 */
	public CallRecoverEntityConsistencyInvoker(RecoverEntityConsistency cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RecoverEntityConsistency getCommand() {
		return (RecoverEntityConsistency) super.getCommand();
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
	 * 执行处理操作
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = check();
			break;
		case 3:
			success = receive();
			break;
		}
		step++;

		// 不成功，退出
		if (!success) {
			setQuit(false);
		}
		return success;
	}

//	/**
//	 * 向全部DATA站点发送数据块签名命令
//	 * @return 成功返回真，否则假
//	 */
//	private boolean send() {
//		RecoverEntityConsistency cmd = getCommand();
//		Space space = cmd.getSpace();
//
//		// 被发送的命令
//		TakeSign sub = new TakeSign(space);
//		// 命令集合
//		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
//
//		// 查找全部DATA站点
//		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
//		// 生成命令数组
//		if (set != null) {
//			for (Node node : set.show()) {
//				CommandItem item = new CommandItem(node, sub);
//				array.add(item);
//			}
//		}
//
//		// 必须有效，且全部发送成功
//		boolean success = (array.size() > 0);
//		if (success) {
//			success = completeTo(array);
//		}
//		// 不成功，通知FRONT站点
//		if (!success) {
//			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
//		}
//		// 返回处理结果
//		return success;
//	}

	/**
	 * 向全部DATA站点发送数据块签名命令
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		RecoverEntityConsistency cmd = getCommand();
		Space space = cmd.getSpace();

		// 被发送的命令
		TakeSign sub = new TakeSign(space);
		
		// 找到全部DATA站点
		ArrayList<Node> slaves = new ArrayList<Node>();
		// 查找全部DATA站点
		NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
		// 生成命令数组
		if (set != null) {
			slaves.addAll(set.show());
		}

		// 必须有效，且全部发送成功
		boolean success = (slaves.size() > 0);
		if (success) {
			success = completeTo(slaves, sub);
		}
		// 不成功，通知FRONT站点
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 判断返回结果
	 * @return 成功返回真，否则假
	 */
	private boolean check() {
		// 1. 分出主从两组数据
		ArrayList<SignSite> masters = new ArrayList<SignSite>();
		ArrayList<SignSite> slaves = new ArrayList<SignSite>();

		// 2. 接收元数据
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (!isSuccessObjectable(index)) {
					continue;
				}
				TakeSignProduct product = getObject(TakeSignProduct.class, index);

				// 判断是主从，分别保存
				SignSite site = product.getSite();
				if (site.isMaster()) {
					masters.add(site);
				} else if (site.isSlave()) {
					slaves.add(site);
				} else {
					throw new VisitException("illegal rank");
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功
		boolean success = (keys.size() == masters.size() + slaves.size());
		// 不成功，通知FRONT站点
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
			return useful(false);
		}

		// 要求修复的数据块
		Map<Node, RollTable> rolls = new TreeMap<Node, RollTable>();
		
		for(SignSite master : masters) {
			SignTable masterTable = master.getTable();

			// 比较每一个签名
			for (StubSign sign : masterTable.list()) {
				long stub = sign.getStub();
				// 找到从站点上同块签名
				for (SignSite slave : slaves) {
					Node slaveNode = slave.getNode();
					SignTable slaveTable = slave.getTable();
					StubSign that = slaveTable.find(stub);
					// 没有找到，忽略它
					if (that == null) {
						continue;
					}
					
					// 签名不一致，记录它们
					if (sign.getHash().compareTo(that.getHash()) != 0) {
						RollTable site = rolls.get(slaveNode);
						if (site == null) {
							site = new RollTable(master.getNode(), masterTable.getSpace());
							rolls.put(slaveNode, site);
						}
						site.add(sign); // 主站点数据块签名
					}
				}
			}
		}
		
		// 如果完全匹配，退出和返回
		if(rolls.isEmpty()) {
			replyProduct(new RollTableMassProduct()); // 返回一个空集合
			return useful(true);
		}
		
		// 生成命令单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		
		Iterator<Map.Entry<Node, RollTable>> iterator = rolls.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, RollTable> entry = iterator.next();
			Node node = entry.getKey();
			RollTable table = entry.getValue();
			
			RollTableMass cmd = new RollTableMass(table);
			CommandItem item = new CommandItem(node, cmd);
			array.add(item);
		}

		// 以容错方式，向目标站点发送命令单元
		int count = incompleteTo(array);
		// 判断成功
		success = (count > 0);

		Logger.debug(this, "check", success, "replace sites:%d, sends:%d", array.size(), count);

		// 返回结果
		return success;
	}

	/**
	 * 接收反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		RollTableMassProduct product = new RollTableMassProduct();
		int count = 0; // 错误统计
		
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (!isSuccessObjectable(index)) {
					count++;
				}
				// 保存全部
				RollTableMassProduct e = getObject(RollTableMassProduct.class, index);
				product.addAll(e);
			} catch (VisitException e) {
				Logger.error(e);
				count++;
			}
		}
		
		boolean success = false;
		// 反馈处理结果给FRONT站点
		if (count > 0) {
			success = replyFault(Major.FAULTED, Minor.DEFAULT);
		} else {
			success = replyProduct(product);
			Logger.debug(this, "receive", success, "item size:%d", product.size());
		}
		// 完成退出
		return useful(success);
	}

}