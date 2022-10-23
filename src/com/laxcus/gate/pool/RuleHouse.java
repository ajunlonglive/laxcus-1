/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.law.forbid.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.Siger;
import com.laxcus.command.rule.*;

/**
 * 事务规则管理池。<br><br>
 * 
 * 提供对事务的管理、启用、回收工作。
 * 
 * @author scott.liang
 * @version 1.2 10/20/2015
 * @since laxcus 1.0
 */
public final class RuleHouse extends VirtualPool { // MutexHandler {

	/** 事务规约规则管理池 **/
	private static RuleHouse selfHandle = new RuleHouse();

	/** 账号 -> 规则管理器 **/
	private Map<Siger, RuleManager> rules = new TreeMap<Siger, RuleManager>();

	/**
	 * 构造事务规约规则管理池
	 */
	private RuleHouse() {
		super();
		setSleepTime(60);
	}

	/**
	 * 返回事务规约规则管理池静态句柄
	 * @return
	 */
	public static RuleHouse getInstance() {
		return RuleHouse.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");
		while (!isInterrupted()) {
			// 检查有过期的事务
			check();
			// 进行延时
			sleep();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		rules.clear();
	}
	
	/**
	 * 统计处于运行中的事务数目
	 * @return 返回当前的事务数目
	 */
	public int size() {
		int count = 0;

		// 锁定它！
		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, RuleManager>> iterator = rules.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, RuleManager> entry = iterator.next();
				RuleManager e = entry.getValue();
				count += e.size();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return count;
	}

	/**
	 * 根据用户签名，得到它的事务管理。如果没有，建立一个新的
	 * @param siger 用户签名
	 * @return 返回对应的事务管理器句柄，或者空值
	 */
	private RuleManager fetch(Siger siger) {
		// 判断用户签名有效且存在
		boolean success = (siger != null);
		if (success) {
			success = StaffOnGatePool.getInstance().contains(siger);
		}
		// 以上不成功，这是非法的用户签名或者空值
		if (!success) {
			Logger.error(this, "fetch", "illegal '%s'", siger);
			return null;
		}

		// 查询队列中的管理器，没有就建立一个新的
		super.lockSingle();
		try {
			RuleManager manager = rules.get(siger);
			if (manager == null) {
				manager = new RuleManager(siger);
				rules.put(manager.getIssuer(), manager);
			}
			return manager;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 根据用户签名，查找关联的事务管理器
	 * @param issuer 用户签名
	 * @return 返回RuleManager或者空指针
	 */
	public RuleManager find(Siger issuer) {
		super.lockMulti();
		try {
			if (issuer != null) {
				return rules.get(issuer);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据用户签名，判断有资源管理串联存在
	 * @param issuer 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger issuer) {
		RuleManager manager = find(issuer);
		return (manager != null && manager.size() > 0);
	}

	/**
	 * 判断锁定的资源在范围内
	 * @param issuer 用户签名
	 * @return 没有超过最大数限制，返回真，否则假
	 */
	public boolean allow(Siger issuer) {
		int jobs = StaffOnGatePool.getInstance().findJobs(issuer);
		if (jobs < 0) {
			Logger.error(this, "allow", "cannot be find %s", issuer);
			return false;
		}

		// 查找事务规则管理器，没有建立一个新的。
		RuleManager manager = fetch(issuer);
		if (manager == null) {
			Logger.error(this, "allow", "cannot be fatch manager by %s", issuer);
			return false;
		}

		// 判断这个用户的运行任务超过最大限制数目
		boolean success = (manager.size() <= jobs);

		Logger.debug(this, "allow", success, "manager size:%d <= jobs:%d",
				manager.size(), jobs);

		return success;
	}

	/**
	 * 提交和申请一个事务操作
	 * @param cmd 绑定事务操作
	 * @return 返回申请的状态码
	 */
	public int submit(AttachRule cmd) {
		Siger issuer = cmd.getIssuer();
		// 在账号存在的情况下，获得一个这个用户的事务管理器。如果没有新建一个
		RuleManager manager = fetch(issuer);
		// 提交事务到管理器队列。接受返回真，不接受（进入等待）返回假。
		if (manager == null) {
			return RuleSubmit.REFUSE;
		}
		// 提交到管理器，返回结果状态码
		return manager.commit(cmd);
//		return (success ? RuleSubmitResult.SUBMIT_ACCEPTED : RuleSubmitResult.SUBMIT_WAITING);
	}

	/**
	 * 撤销和回收一个事务操作
	 * @param cmd 撤销事务命令
	 * @return 回收成功返回“真”，否则“假”。
	 */
	public boolean revoke(DetachRule cmd) {
		Siger issuer = cmd.getIssuer();
		// 查找一个存在的事务管理器
		RuleManager manager = find(issuer);
		boolean success = (manager != null);
		if (success) {
			success = manager.revoke(cmd);
			// 以下条件成立，删除管理器
			if (success && manager.isEmpty()) {
				removeManager(issuer);
			}
		}
		return success;
	}

	/**
	 * 根据用户名判断它有处于等待状态的事务操作
	 * @param issuer 用户签名
	 * @return 返回真或者假
	 */
	public boolean hasIdle(Siger issuer) {
		RuleManager manager = find(issuer);
		boolean success = (manager != null);
		if (success) {
			success = manager.hasIdle();
		}
		return success;
	}

	/**
	 * 根据用户名启动一个新的事务操作
	 * @param issuer 用户签名
	 * @return 成功返回被启用的事务命令，否则是空值。
	 */
	public AttachRule next(Siger issuer) {
		RuleManager manager = find(issuer);
		if (manager != null) {
			return manager.next();
		}
		return null;
	}

	/**
	 * 判断传入的禁止操作单元与事务（已经运行/等待提交两种）存在冲突
	 * @param issuer 用户签名
	 * @param array 禁止操作单元
	 * @return 冲突返回真，否则假
	 */
	public boolean conflict(Siger issuer, List<ForbidItem> array) {
		RuleManager manager = find(issuer);
		boolean success = (manager != null);
		if (success) {
			success = manager.conflict(array);
		}
		return success;
	}
	
	/**
	 * 删除某个用户的事务管理器，必须是在空状态时
	 * @param issuer 用户签名
	 * @return 成功返回真，否则假
	 */
	private boolean removeManager(Siger issuer) {
		boolean success = false;
		super.lockSingle();
		try {
			RuleManager manager = rules.get(issuer);
			if (manager != null) {
				if (manager.isEmpty()) {
					success = (rules.remove(issuer) != null);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		Logger.note(this, "removeManager", success, "delete %s", issuer);
		
		return success;
	}
	
	/**
	 * FRONT节点在退出前调用，释放存在的事务
	 * @param issuer 发布人
	 * @param remote FRONT来源节点
	 * @return 返回被删除的事务数目
	 */
	public int remove(Siger issuer, Node remote) {
		int count = 0;
		RuleManager manager = find(issuer);
		if (manager != null) {
			count = manager.remove(remote);
			if (manager.isEmpty()) {
				removeManager(issuer);
			}
		}

		Logger.info(this, "remove", "release %s # %s, count %d", issuer, remote, count);

		return count;
	}
	
	/**
	 * 检查过期或者超时的事务，这些事务将被逐一删除
	 */
	private void check() {
		// 统计在线的全部事务规则数目
		int size = size();
		Logger.info(this, "check", "count rules:%d", size);
		if (size == 0) {
			return;
		}

		ArrayList<Seat> array = new ArrayList<Seat>();

		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, RuleManager>> iterator = 
					rules.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, RuleManager> entry = iterator.next();
				RuleManager e = entry.getValue();
				array.addAll(e.collect());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		
		// 没有，退出！
		if(array.isEmpty()) {
			return;
		}
		
		int count = 0;
		
		HashSet<Siger> sigers = new HashSet<Siger>();

		// 检查，不存在的事务，删除它！
		for (Seat seat : array) {
			// 判断签名和地址存在于FRONT管理池
			boolean success = FrontOnGatePool.getInstance().contains(
					seat.getSiger(), seat.getSite());
			// 以上不成立，判断签名和地址存在于被授权FRONT管理池
			if (!success) {
				success = ConferrerFrontOnGatePool.getInstance().hasAuthorizer(
						seat.getSiger(), seat.getSite());
			}
			// 以上都不存在，删除它！
			if (!success) {
				int ret = remove(seat.getSiger(), seat.getSite());
				count += ret;
				// 保存被删除的用户签名
				if (ret > 0) {
					sigers.add(seat.getSiger());
				}
			}
		}
		
		Logger.info(this, "check", "remove rules:%d", count);
		
		// 启动下一个事务
		for (Siger siger : sigers) {
			AttachRule rule = next(siger);
			if (rule == null) {
				continue;
			}
			// 向等待中的FRONT站点转发命令
			AttachRuleHook hook = new AttachRuleHook();
			hook.setTimeout(300000); // 5 minutes timeout
			ShiftAttachRule shift = new ShiftAttachRule(rule, hook);
			// 交给管理池处理
			boolean success = GateCommandPool.getInstance().press(shift);
			if (!success) {
				continue;
			}
			// 进入 等待
			hook.await();
			success = hook.isSuccessful();
			Logger.debug(this, "check", success, "shift %s", rule);
		}
	}

	//	private void addIt(Siger username, int invokerId, int tcport, int udport) {
	//		User user = new User(username, username);
	//		Refer refer = new Refer(user);
	//
	//		UserRule rule = new UserRule(RuleOperator.EXCLUSIVE_WRITE);
	//		rule.setUsername(username);
	//
	//		StaffOnAidPool.getInstance().create(refer);
	//
	//		RuleSheet sheet = new RuleSheet();
	//		sheet.add(rule);
	//		CommitRule commit = new CommitRule(sheet);
	//
	//		EchoFlag flag = new EchoFlag(invokerId, 1);
	//		SiteHost host = new SiteHost(Address.select(), tcport, udport);
	//		com.laxcus.site.Node node = new com.laxcus.site.Node(SiteTag.FRONT_SITE, host);
	//		Cabin cabin  = new Cabin(node, flag);
	//
	//		commit.setIssuer(username);
	//		commit.setListener(cabin);
	//
	//		boolean b = RulePool.getInstance().commit(commit);
	//		System.out.printf("commit %s#%s %s\n", commit.getIssuer(), commit.getListener(),  b);
	//	}
	//	
	//	private void revoke(Siger username, long invokerId, int tcport, int udport) {
	//		UserRule rule = new UserRule(RuleOperator.EXCLUSIVE_WRITE);
	//		rule.setUsername(username);
	//		RuleSheet sheet = new RuleSheet();
	//		sheet.add(rule);
	//		
	//		EchoFlag flag = new EchoFlag(invokerId, 1);
	//		SiteHost host = new SiteHost(Address.select(), tcport, udport);
	//		com.laxcus.site.Node node = new com.laxcus.site.Node(SiteTag.FRONT_SITE, host);
	//		Cabin cabin  = new Cabin(node, flag);
	//
	//		RevokeRule revoke = new RevokeRule(sheet);
	//		revoke.setIssuer(username);
	//		revoke.setListener(cabin);
	//		
	//		boolean b = this.revoke(revoke);
	//		System.out.printf("REVOKE %s#%s %s\n", revoke.getIssuer(), revoke.getListener(), b);
	//	
	//		// 启动新的处理
	//		while (true) {
	//			// 判断有处于等待状态中的事务
	//			boolean affirm = RulePool.getInstance().hasIdle(username);
	//			if (!affirm) {
	//				System.out.printf( "%s is empty!\n", username);
	//				break;
	//			}
	//
	//			// 获得一个等待的事务
	//			CommitRule nextRule = RulePool.getInstance().next(username);
	//			if (nextRule == null) {
	//				System.out.printf("CANNOT BE NEXT [%s]!\n", username);
	//				break;
	//			}
	//			Cabin listener = nextRule.getListener();
	//			
	//			// 发送到指定站点
	//			System.out.printf("send %s to %s\n", username, listener);
	//		}
	//	}
	//	
	//	public static void main(String[] args) {
	//		Siger username = new Siger("AD65AC06362F703BB11312831B09677404EBF494");
	//		int invokerId = 1;
	//		int tcport = 200, udport = 200;
	//		for (; invokerId < 6; invokerId++) {
	//			RulePool.getInstance().addIt(username, invokerId, tcport++,
	//					udport++);
	//		}
	//		
	//		RuleManager manager = RulePool.getInstance().find(username);
	//		System.out.println(manager.toString()+"\r\n");
	//		
	//		invokerId = 1;
	//		 tcport = 200;  udport = 200;
	//		for (; invokerId < 6; invokerId++) {
	//			RulePool.getInstance().revoke(username, invokerId, tcport++,
	//					udport++);
	//		}
	//	}


	//	public static void main1(String[] args) {
	//		Siger username = new Siger("AD65AC06362F703BB11312831B09677404EBF494");
	//		User user = new User(username, username);
	//		Refer refer = new Refer(user);
	//
	//		//		TableRule rule = new TableRule( RuleOperator.SHARE_READ );
	//		//		Space space = new Space("Media", "Music");
	//		//		rule.add(space);
	//		//		rule.setUsername(username);
	//
	//		UserRule rule = new UserRule(RuleOperator.EXCLUSIVE_WRITE);
	//		rule.setUsername(username);
	//
	//		StaffOnAidPool.getInstance().create(refer);
	//
	//		RuleSheet sheet = new RuleSheet();
	//		sheet.add(rule);
	//		CommitRule commit = new CommitRule(sheet);
	//
	//		EchoFlag flag = new EchoFlag(1, 1);
	//		SiteHost host = new SiteHost(Address.select(), 200, 9000);
	//		com.laxcus.site.Node node = new com.laxcus.site.Node(SiteTag.FRONT_SITE, host);
	//		Cabin cabin  = new Cabin(node, flag);
	//
	//		commit.setIssuer(username);
	//		commit.setListener(cabin);
	//
	//		// 第1个授权
	//		boolean b = RulePool.getInstance().commit(commit);
	//		System.out.printf("commit %s#%s %s\n", commit.getIssuer(), commit.getListener(),  b);
	//
	//		
	//		RevokeRule revoke = new RevokeRule(sheet);
	//		revoke.setIssuer(username);
	//		revoke.setListener(cabin);
	//
	//		b = RulePool.getInstance().revoke(revoke);
	//		System.out.printf("revoke %s#%s %s\n", revoke.getIssuer(), revoke.getListener(),  b);
	//
	//		//		// 第2个授权
	//		//		UserRule userRule = new UserRule(username);
	//		//		SchemaRule schemaRule = new SchemaRule(username);
	//		//		schemaRule.add(space.getSchema());
	//		//		CommitRule cmd2 = new CommitRule(schemaRule); // userRule);
	//		//		cmd2.setIssuer(username);
	//		//		cmd2.setCabin(new Cabin(node, new EchoFlag(2,2)));
	//		//		b = RulePool.getInstance().commit(cmd2);
	//		//		System.out.printf("second consent is %s\n", b);
	//		//
	//		//		// 回收第1个授权
	//		//		RevokeRule revoke = new RevokeRule(rule);
	//		//		revoke.setIssuer(username);
	//		//		revoke.setCabin(cabin);
	//		//		
	//		//		b = RulePool.getInstance().revoke(revoke);
	//		//		System.out.printf("revoke is %s\n", b);
	//		//		
	//		//		// 启动第2个授权
	//		//		CommitRule second = RulePool.getInstance().next(username);
	//		//		System.out.printf("next rule is '%s'\n", second);
	//	}

}