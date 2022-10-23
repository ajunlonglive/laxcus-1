/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.account.pool.*;
import com.laxcus.command.*;
import com.laxcus.command.account.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.command.task.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.*;

/**
 * 删除云端应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public abstract class AccountDropCloudPackageInvoker extends AccountInvoker {

	/** 工作节点地址集 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();
	
	/** 执行步骤，从1开始 **/
	private int step;
	
	/**
	 * 构造删除云端应用调用器，指定命令
	 * @param cmd 删除云端应用
	 */
	protected AccountDropCloudPackageInvoker(DropCloudPackage cmd) {
		super(cmd);
		step = 1;
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
	 * 判断有执行权限
	 * @return 返回真或者假
	 */
	private boolean checkPermission() {
		DropCloudPackage cmd = getCommand();
		Node hub = cmd.getSourceSite();
		// 来自BANK节点，必须是系统组件命名
		if (hub.isBank()) {
			return cmd.isSystem() && cmd.isSystemWare();
		}
		// 判断来自GATE节点
		else if (hub.isGate()) {
			Account account = readAccount();
			boolean success = (account != null);
			// 判断允许发布任务
			if (success) {
				success = account.canPublishTask() && !cmd.isSystemWare();
			}
			return success;
		}
		// 其它条件都是不成立!
		return false;
	}
	
	/**
	 * 判断是空集合。<br>
	 * 1. 检测有错误，是空<br>
	 * 2. 只有“GROUP-INF/groups.xml”，没有其它内容，是空<br><br>
	 * 
	 * @param content 内容
	 * @return 返回真或者假
	 */
	private boolean isEmptyTaskComponent(File file) {
		int count = 0;
		try {
			TaskComponentGroupReader reader = new TaskComponentGroupReader(file);
			count = reader.getAvailableItems();
		} catch (IOException e) {
			Logger.error(e);
		}
		// 小于等于0是空集合
		return count <= 0;
	}
	
	/**
	 * 根据分区删除某个应用软件包
	 * @param section 任务分区
	 * @return 成功返回真，否则假
	 */
	private boolean dropTaskBoot(TaskSection section) {
		// 1. 找到文件
		String path = TaskOnAccountPool.getInstance().findPath(section.getTaskPart());
		File file = (path != null ? new File(path) : null);
		boolean exists = (file != null && file.exists() && file.isFile());
		if (!exists) {
			Logger.error(this, "dropTaskBoot", "cannot be find boot-group! %s", section);
			return false;
		}
		
		boolean success = false;
		try {
			int count = 0;
			TaskComponentCombiner combiner = new TaskComponentCombiner();
			TaskComponentGroupReader group = new TaskComponentGroupReader(file);
			// 读取单元
			List<CloudPackageItem> items = group.readTaskComponents();

			for (CloudPackageItem item : items) {
				// 如果是“GROUP-INF/groups.xml”，则忽略
				if (item.getName().matches(TF.GROUP_INF)) {
					continue;
				}

				// 读一个单元
				TaskComponentReader sub = new TaskComponentReader(item.getContent());
				WareTag tag = sub.readWareTag();
				// 没有找到软件名称标记，忽略
				if (tag == null) {
					Logger.error(this, "dropTaskBoot", "cannot be git ware-tag! %s", item.getName());
					continue;
				}
				// 软件名称一致时，忽略它
				if (Laxkit.compareTo(tag.getNaming(), section.getWare()) == 0) {
					Logger.info(this, "dropTaskBoot", "ignore %s -> %s", tag, section);
					count++;
					continue;
				}
				// 保存
				combiner.write(item.getContent());
			}

			// 输入到磁盘上
			if (count > 0) {
				combiner.flush(section.getTaskPart(), file);
				combiner.close();
				success = true;
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		
		// 注册到缓存池，等待更新完成！
		if (success) {
			// 判断，如果是空集合就删除它；否则更新加载它！
			boolean empty = isEmptyTaskComponent(file);
			if (empty) {
				success = TaskOnAccountPool.getInstance().delete(file);
			} else {
				success = TaskOnAccountPool.getInstance().load(file);
			}
		}
		
		Logger.note(this, "dropTaskBoot", success, "drop %s", section);

		return success;
	}

	/**
	 * 发布分布任务组件的附件包
	 * @param remote 目标地址
	 * @param ware 软件名称
	 * @param phaseFamily 阶段命名
	 * @param item 包单元
	 * @return 成功返回真，否则假
	 */
	protected int dropTaskAppliction(Node remote, TaskSection section) {
		DropTaskApplication sub = new DropTaskApplication(section);
		DropTaskApplicationHook hook = new DropTaskApplicationHook();
		ShiftDropTaskApplication shift = new ShiftDropTaskApplication(remote, sub, hook);
		// 设置签名，这个很重要
		shift.setIssuer(getIssuer());

		int elements = -1;
		// 发送并且等待
		boolean success = getCommandPool().press(shift);
		if (success) {
			// 等待
			hook.await();
			// 取出结果！
			DropTaskApplicationProduct result = hook.getProduct();
			success = (result != null && result.isSuccessful());
			if (success) {
				elements = result.getRights();
			}
		}
		return elements;
	}

	/**
	 * 部署应用附件
	 * @param phaseFamily
	 * @param siteFamily
	 * @param reader
	 * @return 返回成功部署的节点数目
	 */
	private int dropTaskAppliction(TaskSection section, byte siteFamily, byte rank) {
		// 读取内容
		int count = 0;

		List<Node> nodes = choice(siteFamily, rank);
		for (Node remote : nodes) {
			// 部署分布任务组件的附件
			int elements = dropTaskAppliction(remote, section);
			if (elements > 0) {
				count += elements;
			}
		}

		return count;
	}
	
	/**
	 * 删除一个阶段的应用包，包括本地和DATA/WORK/BUILD/CALL工作节点上的全部包
	 * 
	 * @param phaseFamily 阶段类型
	 * @param siteFamily 站点类型
	 * @param rank 级别
	 * @return 返回发送统计数
	 */
	protected int drop(int phaseFamily, byte siteFamily, byte rank) {
		DropCloudPackage cmd = getCommand();
		Naming software = cmd.getWare();
		TaskSection section = new TaskSection(getIssuer(), phaseFamily, software);
		// 删除引导包
		boolean success = dropTaskBoot(section);
		// 不成功返回-1
		if (!success) {
			return -1;
		}

		// 2. 删除工作节点上的应用包
		int ret = dropTaskAppliction(section, siteFamily, rank);
		if (ret < 0) return -1;
	
		Logger.debug(this, "drop", "count is %d", 1+ret);

		// 返回统计值,（引导包+JAR附件+动态链接库）
		return 1 + ret;
	}

	/**
	 * 分发通知
	 * @param families
	 * @return
	 */
	protected boolean refreshPublish(int[] families) {
		ArrayList<Command> commands = new ArrayList<Command>();
		Node local = getLocal();
		Siger issuer = getIssuer();

		// 生成和保存命令
		for (int taskFamily : families) {
			RefreshPublish sub = new RefreshPublish(local, issuer);
			sub.setTaskFamily(taskFamily);
			commands.add(sub);
		}

		// 发送给BANK节点
		int count = directToHub(commands);
		Logger.debug(this, "refreshPublish", "send count %d",  count);

		return count > 0;
	}

	/**
	 * 第一步：向BANK节点发送查询工作节点命令。<br>
	 * 被查询的节点包括：DATA/WORK/BUILD/CALL。<br><br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		TakeJobSite sub = new TakeJobSite(getIssuer());
		return launchToHub(sub);
	}

	/**
	 * 第二步，删除本地和其它工作节点上的应用包
	 * @return 成功返回真，否则假
	 */
	private boolean process() {
		// 1. 读取工作节点
		boolean success = readJobSites();
		// 2. 向GATE反馈结果
		if (success) {
			int elements = drop();
			success = (elements > 0);
			// 向GATE节点反馈结果！
			if (success) {
				// 删除磁盘本地软件包
				delete();
				
				// 通知HOME集节点节点
				refreshPublish();
				
				// 返回结果给GATE节点
				DropCloudPackageProduct sub = new DropCloudPackageProduct(elements, 0);
				success = replyProduct(sub);
			}
		}
		
//		// 成功后，通知HOME集群节点
//		if (success) {
//			refreshPublish();
//		}
		
		return success;
	}

	/**
	 * 读取查询结果
	 * @return 成功返回真，否则假
	 */
	private boolean readJobSites() {
		TakeJobSiteProduct result = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessCompleted(index)) {
				result = getObject(TakeJobSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		// 判断有效，保存工作节点
		boolean success = (result != null);
		if (success) {
			// 保存关联的工作节点
			slaves.addAll(result.list());
			success = (slaves.size() > 0);
		}

		Logger.debug(this, "readJobSites", success, "count job sites:%d", slaves.size());

		return success;
	}

	/**
	 * 筛选匹配的节点
	 * @param siteFamily 节点类型
	 * @param rank 节点权级
	 * @return 返回匹配的节点地址
	 */
	protected List<Node> choice(byte siteFamily, byte rank) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node e : slaves) {
			boolean success = (SiteTag.isSite(siteFamily) && e.getFamily() == siteFamily);
			if (success) {
				if (RankTag.isRank(rank)) {
					success = (e.getRank() == rank);
				}
			}
			if (success) {
				nodes.add(e);
			}
		}
		
		Logger.debug(this, "choice", "%s#%s sites %d",
				SiteTag.translate(siteFamily), RankTag.translate(rank), nodes.size());

		return nodes;
	}
	
	/**
	 * 筛选匹配的节点
	 * @param siteFamilies 多个节点类型
	 * @return 全部匹配的节点地址
	 */
	protected List<Node> choice(byte[] siteFamilies) {
		ArrayList<Node> a = new ArrayList<Node>();
		for (byte siteFamily : siteFamilies) {
			a.addAll(choice(siteFamily, RankTag.NONE));
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = checkPermission();
		Logger.debug(this, "launch", success, "check permission");
		// 不成功
		if (!success) {
			replyFault(Major.FAULTED, Minor.CANNOT_PUBLISH);
			return false;
		}
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
	 * 执行分阶段操作
	 * @return 成功返回真，失败返回假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = process();
			break;
		}
		step++;

		// 判断结束
		if (!success || step > 2) {
			if (!success) {
				refuse();
			}
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 删除本地保存的引导包和分布在DATA/WORK/BUILD/CALL节点上的应用
	 * 
	 * @return 返回删除数目，失败是小于1。
	 */
	protected abstract int drop();
	
	/**
	 * 删除本地磁盘上的软件包
	 * @return 成功返回真，否则假
	 */
	protected abstract boolean delete();
	
	/**
	 * 发送命令给HOME集群的相关节点，通知它们更新
	 * 
	 * @return 命令发送成功返回真，否则假
	 */
	protected abstract boolean refreshPublish();

}