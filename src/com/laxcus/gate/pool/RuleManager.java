/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.command.rule.*;
import com.laxcus.law.forbid.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * 事务操作管理器 <br><br>
 * 
 * 事务操作管理器基于账号，处理多任务并发状态下，每个任务与其它任务的冲突判断。<br>
 * 如果存在冲突，将放入等待队列，直到运行任务撤销后，才启动它。如果没有冲突，将放入运行队列，立即启动它。<br><br>
 * 
 * 说明：<br>
 * 1. 在运行队列中的任务都不存在冲突。<br>
 * 2. 处于等待的事务规则按照先进先出的原则启动。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.2 10/20/2015
 * @since laxcus 1.0
 */
public final class RuleManager extends MutexHandler {

	/** 用户签名 **/
	private Siger issuer;

	/** 事务处理标识 -> 已经绑定的事务操作。这是运行中的事务操作 **/
	private Map<ProcessRuleTag, AttachRule> runners = new TreeMap<ProcessRuleTag, AttachRule>();

	/** 等待中的事务操作请求 **/
	private ArrayList<AttachRule> waiters = new ArrayList<AttachRule>();

	/**
	 * 构造默认的事务操作管理器
	 */
	private RuleManager() {
		super();
	}

	/**
	 * 构造事务操作管理器，指定用户签名称 
	 * @param username Siger实例
	 */
	public RuleManager(Siger username) {
		this();
		setIssuer(username);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		if (e != null) {
			issuer = e.duplicate();
		}
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}
	
	/**
	 * 返回处于运行状态的规则
	 * @return 事务规则数组
	 */
	public List<RuleItem> getRunRules() {
		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
		super.lockMulti();
		try {
			for (AttachRule cmd : runners.values()) {
				array.addAll(cmd.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 返回处于等待状态的规则
	 * @return 事务规则数组
	 */
	public List<RuleItem> getWaitRules() {
		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
		super.lockMulti();
		try {
			for (AttachRule cmd : waiters) {
				array.addAll(cmd.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}
	
//	/**
//	 * 提交一个事务和申请获得事物操作的权利
//	 * @param cmd 绑定事务操作
//	 * @return 不冲突返回“真”，命令进入运行队列；否则“假”，进入等待队列。
//	 */
//	public boolean commit(AttachRule cmd) {
//		return commit(cmd, true);
//	}

//	/**
//	 * 提交一个事务规则，和申请获得事务操作的权利
//	 * @param cmd 绑定事务操作
//	 * @param fresh 新成员
//	 * @return 申请成功返回“真”，进入运行队列；否则“假”，进入等待队列。
//	 */
//	private boolean commit(AttachRule cmd, boolean fresh) {
//		RuleSheet sheet = cmd.createSheet();
//
//		// 锁定
//		super.lockSingle();
//		try {
//			/** 以下进行冲突判断，开始默认不冲突 **/
//			boolean conflict = false;
//			// 与运行的规则进行比较，判断存在冲突
//			for (AttachRule e : runners.values()) {
//				conflict = sheet.conflict(e.createSheet());
//				// 发生冲突，退出
//				if (conflict) break;
//			}
//
////			Logger.debug(this, "commit", conflict, "run memeber size:%d, conflict", runners.size());
//
//			// 以上，确认与运行队列不冲突后，再与等待队列中的规则进行比较
//			if (!conflict) {
//				for (AttachRule e : waiters) {
//					// 是同一个对象，忽略它
//					if (cmd == e) {
//						continue;
//					}
//					// 与等待的事务处理规则进行比较，发生冲突退出。
//					conflict = sheet.conflict(e.createSheet());
//					if (conflict) break;
//				}
//			}
//
////			Logger.debug(this, "commit", conflict, "waiter member size:%d, conflict", waiters.size());
//
//			/**
//			 * 三种可能结果：
//			 * 1. 不冲突，把命令放入运行队列，返回“真”。
//			 * 2. 运行队列处于“空”状态，并且这个命令已经在等待队列中，返回“真”。
//			 * 3. 其它情况，命令放入等待队列，返回“假”。 
//			 **/
//			if (!conflict) {
////				Logger.debug(this, "commit", "first stage okay! %s#%s", cmd.getIssuer(), cmd.getSource());
//				runners.put(cmd.getTag(), cmd);
//				return true;
//			} else if (runners.isEmpty() && !fresh) {
////				Logger.debug(this, "commit", "second stage okay! %s#%s", cmd.getIssuer(), cmd.getSource());
//				runners.put(cmd.getTag(), cmd);
//				return true;
//			} else {
////				Logger.debug(this, "commit", "third stage into wait! %s#%s", cmd.getIssuer(), cmd.getSource());
//				// 如果是新成员，保存到等待队列；否则不等待
//				if (fresh) waiters.add(cmd);
//				return false;
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return false;
//	}

	/**
	 * 提交一个新的事务规则和申请获得事物操作的权利。<br><br>
	 * 
	 * 三个可能的结果： <br>
	 * 1. 没有冲突，且保存到运行队列 ，返回 SUBMIT_ACCEPTED <br>
	 * 2. 有冲突，暂时放入等待队列，返回 SUBMIT_WAITING <br>
	 * 3. 发生故障，返回 SUBMIT_REFUSE <br>
	 * 
	 * @param cmd 申请绑定事务操作
	 * @return 返回上述三种结果之一
	 */
	public int commit(AttachRule cmd) {
		return commit(cmd, true);
	}

	/**
	 * 提交一个事务规则，和申请获得事务操作的权利。<br><br>
	 * 三个可能的结果： <br>
	 * 1. 没有冲突，命令且保存到运行队列 ，返回 SUBMIT_ACCEPTED <br>
	 * 2. 有冲突，命令暂时放入等待队列，返回 SUBMIT_WAITING <br>
	 * 3. 发生故障，返回 SUBMIT_REFUSE <br>
	 * 
	 * @param cmd 申请绑定的事务规则
	 * @param newly 新成员
	 * @return 返回上述三种结果之一
	 */
	private int commit(AttachRule cmd, boolean newly) {
		RuleSheet sheet = cmd.createSheet();

		// 锁定
		super.lockSingle();
		try {
			/** 以下进行冲突判断，开始默认不冲突 **/
			boolean conflict = false;
			// 与运行的规则进行比较，判断存在冲突
			for (AttachRule e : runners.values()) {
				conflict = sheet.conflict(e.createSheet());
				// 发生冲突，退出
				if (conflict) break;
			}

			// 以上，确认与运行队列不冲突后，再与等待队列中的规则进行比较
			if (!conflict) {
				for (AttachRule e : waiters) {
					// 是同一个对象，忽略它
					if (cmd == e) {
						continue;
					}
					// 与等待的事务处理规则进行比较，发生冲突退出。
					conflict = sheet.conflict(e.createSheet());
					if (conflict) break;
				}
			}

			/**
			 * 三种可能结果：
			 * 1. 不冲突，把命令放入运行队列，返回“接受”。
			 * 2. 运行队列处于“空”状态，并且这个命令已经在等待队列中，返回“接受”。
			 * 3. 其它情况，命令放入等待队列，返回“等待”。 
			 **/
			if (!conflict) {
				runners.put(cmd.getTag(), cmd);
				return RuleSubmit.ACCEPTED;
			} else if (runners.isEmpty() && !newly) {
				runners.put(cmd.getTag(), cmd);
				return RuleSubmit.ACCEPTED;
			} else {
				// 如果是新成员，保存到等待队列；否则不等待
				if (newly) waiters.add(cmd);
				return RuleSubmit.WAITING;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return RuleSubmit.REFUSE;
	}
	
	/**
	 * 撤销释放一个事务操作
	 * @param cmd 撤销事务操作
	 * @return 回收成功返回“真”，否则“假”。
	 */
	public boolean revoke(DetachRule cmd) {
		boolean success = false;
		super.lockSingle();
		try {
			// 1. 根据回显地址查询命令
			ProcessRuleTag tag = cmd.getTag();
			AttachRule that = runners.get(tag);
			success = (that != null);

			//			Logger.debug(this, "revoke", success, "check %s#%s", cmd.getIssuer(), cabin);

			// 判断参数一致
			if (success) {
				success = (that.createSheet().compareTo(cmd.createSheet()) == 0);
			}
			// 以上操作成功，从运行队列删除它
			if (success) {
				runners.remove(tag);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回处理结果
		return success;
	}

	/**
	 * 判断有处于等待状态的事务
	 * @return 返回真或者假
	 */
	public boolean hasIdle() {
		boolean success = false;
		super.lockMulti();
		try {
			success = (waiters.size() > 0);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 返回全部运行任务数目（包括运行和等待两种）
	 * @return 整型值
	 */
	public int size() {
		int count = 0;
		super.lockMulti();
		try {
			count = (runners.size() + waiters.size());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return count;
	}

	/**
	 * 判断是空集
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

//	/**
//	 * 启动一个新的事务操作规则
//	 * @return 成功返回被启用的事务命令，否则是空值。
//	 */
//	public AttachRule next() {
//		AttachRule cmd = null;
//		super.lockSingle();
//		try {
//			if (waiters.size() > 0) {
//				cmd = waiters.get(0);
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		// 判断有命令
//		boolean success = (cmd != null);
//		// 进入队列。如果拒绝，不要放入等待队列
//		if (success) {
//			success = commit(cmd, false);
//		}
//		// 成功，从等待队列中删除它
//		if (success) {
//			super.lockSingle();
//			try {
//				waiters.remove(cmd);
//			} catch (Throwable e) {
//				Logger.fatal(e);
//			} finally {
//				super.unlockSingle();
//			}
//			return cmd;
//		}
//		// 否则返回空值
//		return null;
//	}

	/**
	 * 启动一个新的事务操作规则
	 * @return 成功返回被启用的事务命令，否则是空值。
	 */
	public AttachRule next() {
		AttachRule cmd = null;
		super.lockSingle();
		try {
			if (waiters.size() > 0) {
				cmd = waiters.get(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 判断有命令
		boolean success = (cmd != null);
		// 进入队列。如果拒绝，不要放入等待队列
		if (success) {
			int status = commit(cmd, false);
			// 成功放入运行队列，把它从等待队列中删除
			if (RuleSubmit.isAccepted(status)) {
				super.lockSingle();
				try {
					waiters.remove(cmd);
				} catch (Throwable e) {
					Logger.fatal(e);
				} finally {
					super.unlockSingle();
				}
				return cmd;
			}
		}
		
		// 否则返回空值
		return null;
	}
	
	/**
	 * 判断冲突
	 * @param cmd
	 * @param array
	 * @return
	 */
	private boolean conflict(AttachRule cmd, List<ForbidItem> array) {
		for (RuleItem element : cmd.list()) {
			ForbidItem rule = element.createForbidItem();
			for (ForbidItem item : array) {
				if (rule.conflict(item)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断禁止操作单元存在冲突
	 * @param array 禁止操作单元数组
	 * @return 冲突返回真，否则假
	 */
	public boolean conflict(List<ForbidItem> array) {
		boolean success = false;
		super.lockSingle();
		try {
			// 与运行判断冲突
			for (AttachRule cmd : runners.values()) {
				success = conflict(cmd, array);
				if (success) return true;
			}
			// 与等待判断冲突
			for (AttachRule cmd : waiters) {
				success = conflict(cmd, array);
				if (success) return true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回判断结果
		return success;
	}
	
	/**
	 * 收集签名和地址集
	 * @return Seat集合
	 */
	protected List<Seat> collect() {
		TreeSet<Seat> array = new TreeSet<Seat>();
		super.lockMulti();
		try {
			// 已经处理中的
			Iterator<Map.Entry<ProcessRuleTag, AttachRule>> iterator = runners
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<ProcessRuleTag, AttachRule> entry = iterator.next();
				ProcessRuleTag tag = entry.getKey();
				Seat e = new Seat(issuer, tag.getLocal());
				array.add(e);
			}
			// 等待处理的
			for (AttachRule rule : waiters) {
				Seat e = new Seat(issuer, rule.getTag().getLocal());
				array.add(e);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return new ArrayList<Seat>(array);
	}
	
	/**
	 * 删除与节点关联的事务
	 * @param remote 来源地址
	 * @return 返回删除的事务数目
	 */
	public int remove(Node remote) {
		// 数组
		ArrayList<AttachRule> a = new ArrayList<AttachRule>();
		ArrayList<ProcessRuleTag> tags = new ArrayList<ProcessRuleTag>();

		// 锁定
		super.lockSingle();
		try {
			// 删除处于等待中的事务
			for (AttachRule e : waiters) {
				if (Laxkit.compareTo(e.getTag().getLocal(), remote) == 0) {
					a.add(e);
				}
			}
			for (AttachRule e : a) {
				waiters.remove(e);
			}

			// 删除处于运行中的事务
			Iterator<Map.Entry<ProcessRuleTag, AttachRule>> iterator = runners.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<ProcessRuleTag, AttachRule> entry = iterator.next();
				AttachRule e = entry.getValue();
				if (Laxkit.compareTo(e.getTag().getLocal(), remote) == 0) {
					tags.add(entry.getKey());
				}
			}
			for (ProcessRuleTag e : tags) {
				runners.remove(e);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return a.size() + tags.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("runner size:%d, waiter size:%d", runners.size(),
				waiters.size());
	}

	//	public void test() {
	//		EchoFlag flag1 = new EchoFlag(1, 1);
	//		com.laxcus.util.net.SiteHost host1 = new com.laxcus.util.net.SiteHost(com.laxcus.util.net.Address.select(), 200, 9000);
	//		com.laxcus.site.Node node1 = new com.laxcus.site.Node(com.laxcus.site.SiteTag.FRONT_SITE, host1);
	//		Cabin cabin1  = new Cabin(node1, flag1);
	//
	//		TableRule r1 = new TableRule(RuleOperator.SHARE_READ);
	//		r1.add(new com.laxcus.access.schema.Space("MEDIA", "MUSIC"));
	//		RuleSheet s1 = new RuleSheet();
	//		s1.add(r1);
	//		CommitRule c1 = new CommitRule(s1);
	//		c1.setListener(cabin1);
	//		
	//		///////////////// SECOND --------
	//		EchoFlag flag2 = new EchoFlag(2, 2);
	//		com.laxcus.util.net.SiteHost host2 = new com.laxcus.util.net.SiteHost(com.laxcus.util.net.Address.select(), 200, 9000);
	//		com.laxcus.site.Node node2 = new com.laxcus.site.Node(com.laxcus.site.SiteTag.FRONT_SITE, host2);
	//		Cabin cabin2  = new Cabin(node2, flag2);
	//		
	//		TableRule r2 = new TableRule(RuleOperator.SHARE_READ);
	//		r2.add(new com.laxcus.access.schema.Space("MEDIA", "MUSIC"));
	//		RuleSheet s2 = new RuleSheet();
	//		s2.add(r2);
	//		CommitRule c2 = new CommitRule(s2);
	//		c2.setListener(cabin2);
	//		
	//		//////////////// THIRD ---------------
	//		///////////////// SECOND --------
	//		EchoFlag flag3 = new EchoFlag(3, 3);
	//		com.laxcus.util.net.SiteHost host3 = new com.laxcus.util.net.SiteHost(com.laxcus.util.net.Address.select(), 300, 9000);
	//		com.laxcus.site.Node node3 = new com.laxcus.site.Node(com.laxcus.site.SiteTag.FRONT_SITE, host3);
	//		Cabin cabin3  = new Cabin(node3, flag3);
	//		
	//		TableRule r3 = new TableRule(RuleOperator.SHARE_READ);
	//		r3.add(new com.laxcus.access.schema.Space("MEDIA", "MUSIC"));
	//		RuleSheet s3 = new RuleSheet();
	//		s3.add(r3);
	//		CommitRule c3 = new CommitRule(s3);
	//		c3.setListener(cabin3);
	//		
	//		// 比较
	//		boolean success = commit(c1);
	//		System.out.printf("first is %s\n", success);
	//		success = commit(c2);
	//		System.out.printf("second is %s\n", success);
	//		success = commit(c3);
	//		System.out.printf("third is %s\n", success);
	//		
	//		System.out.println("=====================");
	//		
	//		byte[]  b1 = s1.build();
	//		System.out.printf("build sheet length:%d\n", b1.length);
	//		ClassReader reader =	new ClassReader(b1);
	//		RuleSheet s4 = new RuleSheet(reader);
	//		System.out.printf("resolve sheet size %d\n", reader.getSeek());
	//		
	//		byte[] b2 = s4.build();
	//		success = (Laxkit.compareTo(b1, b2) ==0);
	//		System.out.printf("compare sheet is %s\n", success);
	//	}
	//	
	//	public static void main(String[] args) {
	//		RuleManager manager = new RuleManager();
	//		manager.test();
	//	}
}