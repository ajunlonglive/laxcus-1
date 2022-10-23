/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * WATCH公共命令调度调用器  <br>
 * 处理来自WATCH节点的公共命令，涉及主域集群、子域集群、管理节点和工作节点之间的分配调度。<br>
 * 这里命令以“RELOAD”开头为的重载资源命令，如SET DYNAMIC LIBRARY TO ALL;  以“SET”开头的重置参数命令，如SET CIPHER TIMEOUT 300S TO ALL。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/24/2019
 * @since laxcus 1.0
 */
public abstract class CommonWatchShareInvoker extends CommonInvoker {

	/**
	 * 构造WATCH公共命令调度调用器，指定命令
	 * @param cmd WATCH公共处理命令
	 */
	protected CommonWatchShareInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 判断是HOME集群的子站点
	 * @param subFamily 节点类型
	 * @return 返回真或者假
	 */
	public boolean isHomeClusterSite(byte subFamily) {
		switch (subFamily) {
		case SiteTag.LOG_SITE:
		case SiteTag.DATA_SITE:
		case SiteTag.BUILD_SITE:
		case SiteTag.WORK_SITE:
		case SiteTag.CALL_SITE:
			return true;
		}
		return false;
	}

	/**
	 * 判断是BANK集群的子站点
	 * @param subFamily 节点类型
	 * @return 返回真或者假
	 */
	public boolean isBankClusterSite(byte subFamily) {
		switch (subFamily) {
		case SiteTag.LOG_SITE:
		case SiteTag.ACCOUNT_SITE:
		case SiteTag.HASH_SITE:
		case SiteTag.GATE_SITE:
		case SiteTag.ENTRANCE_SITE:
			return true;
		}
		return false;
	}

	/**
	 * 判断节点依从关系，传入的节点类型属于当前节点的子节点。<BR>
	 * TOP节点的子节点包括：LOG、HOME/CALL/DATA/WORK/BUILD、BANK/ACCOUNT/HASH/GATE/ENTRACE，除FRONT/WATCH之外的所有节点。 <BR>
	 * HOME节点的子节点包括：LOG, CALL/DATA/WORK/BUILD <BR>
	 * BANK节点的子节点包括：LOG, ACCOUNT/HASH/GATE/ENTRANCE <BR><br>
	 * 
	 * 说明：LOG节点是跨集群的节点，可以在主域集群和子域集群中运行。FRONT站点不属于集群中的子节点，WATCH节点同LOG节点类似，跨集群存在，不直接属于
	 * 
	 * @param hubFamily 管理节点类型
	 * @param subFamily 子节点类型
	 * @return 传入节点属于当前节点的子节点，返回真，否则假。
	 */
	public boolean isSlaveSite(byte hubFamily, byte subFamily) {
		if (SiteTag.isTop(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
				// HOME CLUSTER
			case SiteTag.HOME_SITE:
			case SiteTag.DATA_SITE:
			case SiteTag.BUILD_SITE:
			case SiteTag.WORK_SITE:
			case SiteTag.CALL_SITE:
				// BANK CLUSTER
			case SiteTag.BANK_SITE:
			case SiteTag.ACCOUNT_SITE:
			case SiteTag.HASH_SITE:
			case SiteTag.GATE_SITE:
			case SiteTag.ENTRANCE_SITE:
				return true;
			}
		} else if (SiteTag.isBank(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.ACCOUNT_SITE:
			case SiteTag.HASH_SITE:
			case SiteTag.GATE_SITE:
			case SiteTag.ENTRANCE_SITE:
				return true;
			}
		} else if (SiteTag.isHome(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.DATA_SITE:
			case SiteTag.BUILD_SITE:
			case SiteTag.WORK_SITE:
			case SiteTag.CALL_SITE:
				return true;
			}
		}
		// 返回假
		return false;
	}

	/**
	 * 判断是真属子站点
	 * @param hubFamily
	 * @param subFamily
	 * @return
	 */
	public boolean isDirectlySlaveSite(byte hubFamily, byte subFamily) {
		if (SiteTag.isTop(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.HOME_SITE:
			case SiteTag.BANK_SITE:
				return true;
			}
		} else if (SiteTag.isBank(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.ACCOUNT_SITE:
			case SiteTag.HASH_SITE:
			case SiteTag.GATE_SITE:
			case SiteTag.ENTRANCE_SITE:
				return true;
			}
		} else if (SiteTag.isHome(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.DATA_SITE:
			case SiteTag.BUILD_SITE:
			case SiteTag.WORK_SITE:
			case SiteTag.CALL_SITE:
				return true;
			}
		}
		// 返回假
		return false;
	}
	
	/**
	 * 判断节点依从关系，传入的节点类型属于当前节点的子节点。<BR>
	 * TOP节点的子节点包括：LOG、HOME/CALL/DATA/WORK/BUILD、BANK/ACCOUNT/HASH/GATE/ENTRACE，除FRONT/WATCH之外的所有节点。 <BR>
	 * HOME节点的子节点包括：LOG, CALL/DATA/WORK/BUILD <BR>
	 * BANK节点的子节点包括：LOG, ACCOUNT/HASH/GATE/ENTRANCE <BR><br>
	 * 
	 * 说明：LOG节点是跨集群的节点，可以在主域集群和子域集群中运行。FRONT站点不属于集群中的子节点，WATCH节点同LOG节点类似，跨集群存在，不直接属于
	 * 
	 * @param subFamily 子节点类型
	 * @return 传入节点属于当前节点的子节点，返回真，否则假。
	 */
	public boolean isSlaveSite(byte subFamily) {
		byte hubFamily = getSiteFamily();
		if (SiteTag.isTop(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
				// HOME CLUSTER
			case SiteTag.HOME_SITE:
			case SiteTag.DATA_SITE:
			case SiteTag.BUILD_SITE:
			case SiteTag.WORK_SITE:
			case SiteTag.CALL_SITE:
				// BANK CLUSTER
			case SiteTag.BANK_SITE:
			case SiteTag.ACCOUNT_SITE:
			case SiteTag.HASH_SITE:
			case SiteTag.GATE_SITE:
			case SiteTag.ENTRANCE_SITE:
				return true;
			}
		} else if (SiteTag.isBank(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.ACCOUNT_SITE:
			case SiteTag.HASH_SITE:
			case SiteTag.GATE_SITE:
			case SiteTag.ENTRANCE_SITE:
				return true;
			}
		} else if (SiteTag.isHome(hubFamily)) {
			switch (subFamily) {
			case SiteTag.LOG_SITE:
			case SiteTag.DATA_SITE:
			case SiteTag.BUILD_SITE:
			case SiteTag.WORK_SITE:
			case SiteTag.CALL_SITE:
				return true;
			}
		}
		// 返回假
		return false;
	}

	/**
	 * 判断节点依从关系，传入的节点类型是否属于当前节点的子节点。<BR>
	 * TOP节点的子节点包括：LOG、HOME/CALL/DATA/WORK/BUILD、BANK/ACCOUNT/HASH/GATE/ENTRACE <BR>
	 * HOME节点的子节点包括：LOG, CALL/DATA/WORK/BUILD <BR>
	 * BANK节点的子节点包括：LOG, ACCOUNT/HASH/GATE/ENTRANCE <BR>
	 * 
	 * @param subFamily 子节点类型
	 * @return 传入节点属于当前节点的子节点，返回真，否则假。
	 */
	public boolean isSlaveSite(Node node) {
		byte family = node.getFamily();
		return isSlaveSite(family);
	}

	/**
	 * 判断节点依从关系，传入的节点类型是当前管理节点的子管理节点。<BR>
	 * 这个条件的成立只能在TOP节点和HOME/BANK节点之间成立。<BR>
	 * 
	 * @param subFamily 子节点类型
	 * @return 传入节点属于当前节点的子节点，返回真，否则假。
	 */
	public boolean isSlaveHub(byte subFamily) {
		byte hubFamily = getSiteFamily();
		if (SiteTag.isTop(hubFamily)) {
			switch (subFamily) {
			case SiteTag.HOME_SITE:
			case SiteTag.BANK_SITE:
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断节点依从关系，传入的节点类型是当前管理节点的子管理节点。<BR>
	 * 这个条件的成立只能在TOP节点和HOME/BANK节点之间成立。<BR>
	 * 
	 * @param subFamily 子节点类型
	 * @return 传入节点属于当前节点的子节点，返回真，否则假。
	 */
	public boolean isSlaveHub(Node node) {
		byte family = node.getFamily();
		return isSlaveHub(family);
	}
	
	/**
	 * 当前HUB节点已经存在于单元中
	 * @param hub
	 * @param array
	 * @return
	 */
	private boolean exists(Node hub, List<CommandItem> array) {
		int count = 0;
		for (CommandItem item : array) {
			boolean success = (Laxkit.compareTo(item.getHub(), hub) == 0);
			if (success) {
				count++;
			}
		}
		return (count > 0);
	}
	
	/**
	 * 把一个工作站点分配给HOME/BANK站点
	 * @param hubs 当前HUB站点的子类站点
	 * @param array 输入的命令单元列表
	 * @param slave 工作站点
	 * @return 成功返回真，否则假
	 */
	protected boolean dispatchToSlaveHub(List<Node> hubs, List<CommandItem> array, Node slave) {
		int count = 0;
		for (Node hub : hubs) {
			// 1. 非直属主机，忽略
			if (!isDirectlySlaveSite(hub.getFamily(), slave.getFamily())) {
				continue;
			}
			// 2. 判断节点存在于CommandItem集合
			boolean exists = exists(hub, array);
			// 3. 不成立，生成命令副本，保存它!
			if (!exists) {
				Command sub = getCommand().duplicate();
				CommandItem item = new CommandItem(hub, sub);
				array.add(item);
				count++;
			}
		}
		
		// 如果是日志节点，特殊处理，分配给每一个Manager节点
		if (SiteTag.isLog(slave.getFamily())) {
			for (Node hub : hubs) {
				boolean exists = exists(hub, array);
				boolean manager = RankTag.isManager(hub.getRank());
				// 不存在，并且是管理节点时
				if (!exists && manager) {
					Command sub = getCommand().duplicate();
					CommandItem item = new CommandItem(hub, sub);
					array.add(item);
					count++;
				}
			}
		}
		
		return (count > 0);
	}

}
