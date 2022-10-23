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
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;
import com.laxcus.visit.*;

/**
 * 部署云端系统包调用器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public abstract class AccountDeploySystemPackageInvoker extends AccountInvoker {

	class PlayFruit {
		boolean success = false;
		int count = 0;
		
		public PlayFruit(boolean b, int i) {
			success = b;
			count = i;
		}
	}
	
	/** 工作节点地址集 **/
	private TreeSet<Node> slaves = new TreeSet<Node>();

	/** 执行步骤，从1开始 **/
	private int step;
	
	/** 当发生错误时，强制返回错误！默认是真**/
	private boolean replyFatal;
	
	/**
	 * 构造默认的部署云端系统包调用器
	 * @param cmd 发布云应用包
	 */
	protected AccountDeploySystemPackageInvoker(DeploySystemPackage cmd) {
		super(cmd);
		step = 1;
		replyFatal = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeploySystemPackage getCommand() {
		return (DeploySystemPackage) super.getCommand();
	}
	
	/**
	 * 判断要求分布通知
	 * @return 返回真或者假
	 */
	private boolean isPublish() {
		DeploySystemPackage cmd = getCommand();
		return cmd.isPublish();
	}
	
	/**
	 * 建立一个文件名称（只是文件名称，不包含目录）
	 * @return 返回文件名称的字符串
	 */
	private String createFilename(TaskPart part) {
		String prefix = (part.isSystemLevel() ? "SYSTEM" : part.getIssuer().toString());
		String suffix = PhaseTag.translate(part.getFamily());
		return prefix + "_" + suffix + TF.DTG_SUFFIX; // TaskBoot.DTG_SUFFIX;
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
	 * 判断存在基于某个节点类型和级别的站点
	 * @param siteFamily 节点类型
	 * @param rank 节点级别
	 * @return 存在返回真，否则假
	 */
	private boolean exists(byte siteFamily, byte rank) {
		List<Node> a = choice(siteFamily, rank);
		return a.size() > 0;
	}
	
	/**
	 * 判断存在基于某些节点类型和级别的站点。<br>
	 * 注意！类型数组和级别数组的数量要一致！<br>
	 * 
	 * @param siteFamilies 节点类型
	 * @param ranks 节点级别
	 * @return 存在返回真，否则假
	 */
	protected boolean exists(byte[] siteFamilies, byte[] ranks) {
		if (siteFamilies.length != ranks.length) {
			Logger.error(this, "exists", "%d != %d", siteFamilies.length, ranks.length);
			return false;
		}
		for (int i = 0; i < siteFamilies.length; i++) {
			boolean success = exists(siteFamilies[i], ranks[i]);
			if (!success) {
				Logger.error(this, "exists", "%s:%s sites missing!",
						SiteTag.translate(siteFamilies[i]), RankTag.translate(ranks[i]));
				return false;
			}
		}
		return true;
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
		switch(step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		}
		step++;

		// 判断结束
		if (!success || step > 3) {
			if (!success) {
				if (replyFatal) {
					refuse();
				}
			}
			setQuit(true);
		}
		return success;
	}

	/**
	 * 第一步：向BANK节点发送查询工作节点命令。<br>
	 * 被查询的节点包括：DATA/WORK/BUILD/CALL。<br><br>
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		TakeJobSite sub = new TakeJobSite(); //不带用户签名，全部节点！
		return launchToHub(sub);
	}

	/**
	 * 第二步：读取BANK节点应答，向WATCH节点反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		// 1. 读取工作节点
		boolean success = readJobSites();
		// 2. 向WATCH反馈结果
		if (success) {
			MailSystemPackageProduct sub = new MailSystemPackageProduct(true);
			DeploySystemPackage cmd = getCommand();
			Cabin source = cmd.getSource();
			// 异步发送！
			success = replyTo(source, sub);
		}
		return success;
	}
	
	/**
	 * 投递反馈结果
	 * @param success
	 * @param count
	 * @return
	 */
	private boolean replyMailProduct(boolean success, int count) {
		MailSystemPackageProduct product = new MailSystemPackageProduct(success, count);
		return replyProduct(product);
	}
	
	/**
	 * 从硬盘或者内存读取云端应用包
	 * @return CloudPackageComponent实例
	 */
	private CloudPackageComponent readComponent() {
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				return getObject(CloudPackageComponent.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 第三步：接收和分布云应用包
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		CloudPackageComponent component = readComponent();
		// 判断有效！
		boolean success = (component != null && component.confirm());
		// 1. 判断节点数目符合要求 
		if(success) {
			success = checkSites();
			if(!success) {
				boolean b = replyMailProduct(false, 0);
				if (b) replyFatal = false; // 不要再投递结果
			}
		}
		// 2. 分发云应用包
		if (success) {
			PlayFruit fruit = deploy(component);
			success = fruit.success;
			// 向GATE节点反馈结果！
			if (success) {
				success = replyMailProduct(true, fruit.count);
			} else {
				boolean b = replyMailProduct(false, fruit.count);
				if (b) replyFatal = false; // 不要再投递结果
			}
		}
		// 成功后，软件包写入本地指定目录，然后通知HOME集群节点
		if (success) {
			// 写入本地
			WareOnAccountPool.getInstance().write(null, component);
			// 发送软件包
			refreshPublish();
		}

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
	protected boolean distributeTaskAssist(Node remote, Naming ware, int phaseFamily, CloudPackageItem item) {
		// 文件名（去掉路径！）和内容
		String name = item.getSimpleName();
		byte[] content = item.getContent();
		// 生成内容签名
		MD5Hash sign = Laxkit.doMD5Hash(content);
		// 生成系统工作部件
		TaskPart part = new TaskPart(phaseFamily);
		part.setIssuer(null);

		// 定义组件包
		TaskAssistComponent component = new TaskAssistComponent(part, sign, ware, name, content);

		PublishSingleTaskAssistComponent sub = new PublishSingleTaskAssistComponent(component);
		PublishSingleTaskAssistComponentHook hook = new PublishSingleTaskAssistComponentHook();
		ShiftPublishSingleTaskAssistComponent shift = new ShiftPublishSingleTaskAssistComponent(remote, sub, hook);
		// 系统应用，设置签名，这个很重要。
		shift.setIssuer(null);

		// 发送并且等待
		boolean success = getCommandPool().press(shift);
		if (success) {
			// 等待
			hook.await();
			// 取出结果！
			PublishTaskAssistComponentProduct result = hook.getProduct();
			success = (result != null && result.isSuccessful());
		}
		return success;
	}

	/**
	 * 发布分布任务组件的动态链接库
	 * @param remote 目标地址
	 * @param ware 软件名称
	 * @param phaseFamily 阶段命名
	 * @param item 包单元
	 * @return 成功返回真，否则假
	 */
	protected boolean distributeTaskLibrary(Node remote, Naming ware, int phaseFamily, CloudPackageItem item) {
		// 文件名（去掉路径！）和内容
		String name = item.getSimpleName();
		byte[] content = item.getContent();
		// 生成内容签名
		MD5Hash sign = Laxkit.doMD5Hash(content);
		// 生成工作部件
		TaskPart part = new TaskPart(phaseFamily);
		part.setIssuer(null);

		// 定义组件包
		TaskLibraryComponent component = new TaskLibraryComponent(part, sign, ware, name, content);

		PublishSingleTaskLibraryComponent sub = new PublishSingleTaskLibraryComponent(component);
		PublishSingleTaskLibraryComponentHook hook = new PublishSingleTaskLibraryComponentHook();
		ShiftPublishSingleTaskLibraryComponent shift = new ShiftPublishSingleTaskLibraryComponent(remote, sub, hook);
		// 设置签名，这个很重要
		shift.setIssuer(null);

		// 发送并且等待
		boolean success = getCommandPool().press(shift);
		if (success) {
			// 等待
			hook.await();
			// 取出结果！
			PublishTaskLibraryComponentProduct result = hook.getProduct();
			success = (result != null && result.isSuccessful());
		}
		return success;
	}

	/**
	 * 分发通知
	 * @param families
	 * @return
	 */
	protected boolean refreshPublish(int[] families) {
		// 如果不执行分发操作，退出返回
		if (!isPublish()) {
			return false;
		}
		
		ArrayList<Command> commands = new ArrayList<Command>();
		Node local = getLocal();
//		Siger issuer = null;

		// 生成和保存命令
		for (int family : families) {
			RefreshPublish sub = new RefreshPublish(local);
			sub.setTaskFamily(family);
			commands.add(sub);
		}

		// 发送给BANK节点
		int count = directToHub(commands);
		Logger.debug(this, "refreshPublish", "send count %d",  count);

		return count > 0;
	}

	/**
	 * 根据阶段命名，从软件包中读取软件名称
	 * @param phaseFamily 阶段类型
	 * @param reader 软件包读取器
	 * @return 返回软件名称
	 */
	private Naming readWareName(int phaseFamily, CloudPackageReader reader) {
		try {
			// 1. 读取引导文件
			CloudPackageItem item = reader.readDTC(phaseFamily);
			if (item != null) {
				TaskComponentReader sub = new TaskComponentReader(item.getContent());
				WareTag tag = sub.readWareTag();
				// 从内容中读取软件名称
				if (tag != null) {
					Logger.info(this, "readWareName", "this is \"%s\"", tag);
					return tag.getNaming();
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		// 不成功，返回空指针
		return null;
	}

	/**
	 * 部署组件引导包
	 * @param part
	 * @param reader
	 * @return
	 */
	private boolean deployTaskBoot(TaskPart part, CloudPackageReader reader) {
		File file = null;
		String path = TaskOnAccountPool.getInstance().findPath(part);
		final boolean exists = (path != null);
		if (exists) {
			file = new File(path);
		} else {
			// 生成新的文件
			String name = createFilename(part);
			// 找到组件根目录，生成文件
			File root = TaskOnAccountPool.getInstance().getRoot();
			file = new File(root, name);
		}

		// 合并文件
		boolean success = false;
		try {
			TaskComponentCombiner combiner = new TaskComponentCombiner();
			CloudPackageItem item = reader.readDTC(part.getFamily());
			if (item == null) {
				Logger.error(this, "deployTaskBoot", "cannot be find task boot! %s", part);
				return false;
			}
			// 这是第三方应用！
			combiner.write(item.getContent());

			// 其它包，如果存在，追加进去！
			if (exists) {
				combiner.writeGroup(file, false);
			}

			// 输入到磁盘上
			combiner.flush(part, file);
			combiner.close();
			// 成功
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}

		// 注册到缓存池
		if (success) {
			success = TaskOnAccountPool.getInstance().load(file);
		}

		return success;
	}

	/**
	 * 分发应用附件到相关节点
	 * 
	 * @param phaseFamily
	 * @param siteFamily
	 * @param reader
	 * @return 返回成功部署的节点数目
	 */
	private PlayFruit distributeAssists(Naming ware, int phaseFamily, byte siteFamily, byte siteRank, CloudPackageReader reader) {
		// 如果不要求发布时，返回成功
		if (!isPublish()) {
			return new PlayFruit(true, 0);
		}
		
		// 读取关联阶段的附件
		List<CloudPackageItem> items = null;
		try {
			items = reader.readAssists(phaseFamily);
		} catch (IOException e) {
			Logger.error(e);
		}

		if (items == null) {
			return new PlayFruit(false, 0);
		}

		// 读取内容
		int count = 0;
		for (CloudPackageItem item : items) {
			List<Node> nodes = choice(siteFamily, siteRank);
			for(Node remote : nodes) {
				// 部署分布任务组件的附件
				boolean success = distributeTaskAssist(remote, ware, phaseFamily, item);
				if (success) {
					count++;
				} else {
					return new PlayFruit(false, count);
				}
			}
		}

		return new PlayFruit(true, count);
	}

	/**
	 * 分发动态链接库到关联节点
	 * 
	 * @param phaseFamily
	 * @param siteFamily
	 * @param reader
	 * @return
	 */
	private PlayFruit distributeLibraries(Naming ware, int phaseFamily, byte siteFamily, byte siteRank, CloudPackageReader reader) {
		// 不要求发布时...
		if (!isPublish()) {
			return new PlayFruit(true, 0);
		}
		
		List<CloudPackageItem> items = null;
		try {
			items = reader.readLibraries(phaseFamily);
		} catch (IOException e) {
			Logger.error(e);
		}

		if (items == null) {
			return new PlayFruit(false, 0);
		}
		// 读取内容
		int count = 0;
		for (CloudPackageItem item : items) {
			List<Node> nodes = choice(siteFamily, siteRank);
			for(Node remote : nodes) {
				// 部署分布任务组件的附件
				boolean success = distributeTaskLibrary(remote, ware, phaseFamily, item);
				// 成功，统计值增1
				if (success) {
					count++;
				} else {
					return new PlayFruit(false, count);
				}
			}
		}

		return new PlayFruit(true, count);
	}

	/**
	 * 分发某个阶段的应用包
	 * @param phaseFamily 阶段命名
	 * @param siteFamily 节点类型
	 * @param siteRank 节点级别
	 * @param component 组件包
	 * @return 返回分发的数目
	 */
	protected PlayFruit distribute(int phaseFamily, byte siteFamily, byte siteRank, CloudPackageComponent component) {
		// 用读取器取出内容
		CloudPackageReader reader = new CloudPackageReader(component.getContent());
		// 检测数据内容，必须保证全部有效
		int items = reader.check();
		if (items < 1) {
			Logger.error(this, "distribute", "check error!");
			return new PlayFruit(false, 0);
		}

		// 找到软件名称
		Naming software = readWareName(phaseFamily, reader);
		if (software == null) {
			Logger.error(this, "distribute", "cannot be find ware name!");
			return new PlayFruit(false, 0);
		}

		// 1. 更新引导包
		TaskPart part = new TaskPart(phaseFamily);
		part.setIssuer(null); // 系统应用
		
		// 部署任务引导包
		boolean success = deployTaskBoot(part, reader);
		if (!success) {
			return new PlayFruit(false, 0);
		}

		// 2. 分发附件包
		PlayFruit jars = distributeAssists(software, phaseFamily, siteFamily, siteRank, reader);
		if (!jars.success) return new PlayFruit(false, 1 + jars.count);
		// 3. 分发动态链接库
		PlayFruit libs = distributeLibraries(software, phaseFamily, siteFamily, siteRank, reader);
		if (!libs.success) return new PlayFruit(false, 1 + jars.count + libs.count);

		// 返回统计值
		return new PlayFruit(true, 1 + jars.count + libs.count);
	}

	/**
	 * 检查节点符合要求 
	 * @return 返回真或者假
	 */
	protected abstract boolean checkSites();

	/**
	 * 分发云应用组件包
	 * @param component
	 * @return 返回分发数目，失败是小于1。
	 */
	protected abstract PlayFruit deploy(CloudPackageComponent component);

	/**
	 * 发送命令给HOME集群的相关节点，通知它们更新
	 * @return 命令发送成功返回真，否则假
	 */
	protected abstract boolean refreshPublish();
}
