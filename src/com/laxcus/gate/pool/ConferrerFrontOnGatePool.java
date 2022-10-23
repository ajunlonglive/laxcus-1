/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 被授权FRONT站点管理池 <br>
 * 
 * 记录被授权账号和授权账号之间的关系
 * 
 * @author scott.liang
 * @version 1.0 7/17/2017
 * @since laxcus 1.0
 */
public final class ConferrerFrontOnGatePool extends HubPool {

	/** 授权站点管理池 **/
	private static ConferrerFrontOnGatePool selfHandle = new ConferrerFrontOnGatePool();

	/** 被授权用户站点地址 -> 站点配置 **/
	private Map<Node, ConferrerSite> mapSites = new TreeMap<Node, ConferrerSite>();

	/** FRONT散列码 -> 站点配置 **/
	private Map<ClassCode, ConferrerSite> mapHashs = new TreeMap<ClassCode, ConferrerSite>();

	/** 授权人签名 -> 被授权人签名集合 **/
	private Map<Siger, SigerSet> mapAuthorizers = new TreeMap<Siger, SigerSet>();

	/** 被授权人签名 -> 授权人签名集合 **/
	private Map<Siger, SigerSet> mapConferrers = new TreeMap<Siger, SigerSet>();

	/**
	 * 构造授权站点管理池
	 */
	private ConferrerFrontOnGatePool() {
		super(SiteTag.FRONT_SITE);
	}

	/**
	 * 返回授权站点管理池静态句柄
	 * @return
	 */
	public static ConferrerFrontOnGatePool getInstance() {
		return ConferrerFrontOnGatePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSites.clear();
		mapConferrers.clear();
		mapAuthorizers.clear();
	}

	/**
	 * 判断被授权账号存在
	 * @param conferrer 被授权人签名
	 * @return 返回真或者假
	 */
	public boolean hasConferrer(Siger conferrer) {
		boolean success = false;
		super.lockMulti();
		try {
			if (conferrer != null) {
				success = (mapConferrers.get(conferrer) != null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断被授权账号存在
	 * @param conferrer 被授权人签名
	 * @param conferrerNode 被授权人注册地址
	 * @return 返回真或者假
	 */
	public boolean hasConferrer(Siger conferrer, Node conferrerNode) {
		boolean success = false;
		super.lockMulti();
		try {
			if (conferrer != null && conferrerNode != null) {
				SigerSet set = mapConferrers.get(conferrer);
				if (set != null && set.contains(conferrer)) {
					success = (mapSites.get(conferrerNode) != null);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 判断授权账号存在
	 * @param authorizer 授权人签名
	 * @return 返回真或者假
	 */
	public boolean hasAuthorizer(Siger authorizer) {
		boolean success = false;
		super.lockMulti();
		try {
			if (authorizer != null) {
				success = (mapAuthorizers.get(authorizer) != null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 判断账号存在
	 * @param authorizer 授权人签名
	 * @param conferrerNode 被授权人注册地址
	 * @return 存在返回真，否则假
	 */
	public boolean hasAuthorizer(Siger authorizer, Node conferrerNode) {
		boolean success = false;
		super.lockMulti();
		try {
			if (authorizer != null && conferrerNode != null) {
				SigerSet set = mapAuthorizers.get(authorizer);
				// 被授权人的地址
				if (set != null && set.contains(authorizer)) {
					success = (mapSites.get(conferrerNode) != null);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 删除授权人账号，相关的被授权人也要被删除
	 * @param authorizer 授权人签名
	 * @return 成功返回真，否则假
	 */
	public boolean removeAuthorizer(Siger authorizer) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 删除授权人
			SigerSet aset = mapAuthorizers.remove(authorizer);
			success = (aset != null);
			// 被授权人签名
			if (success) {
				// 解除授权人->被授权人之间的关联
				for (Siger conferrer : aset.list()) {
					SigerSet cset = mapConferrers.remove(conferrer);
					if (cset != null) {
						cset.remove(authorizer); // 删除被授权人保存的授权人签名
						if (cset.isEmpty()) mapConferrers.remove(conferrer);
					}
				}
				
				ArrayList<Node> a = new ArrayList<Node>();
				// 遍历，删除授权人签名
				Iterator<Map.Entry<Node, ConferrerSite>> iterator = mapSites.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<Node, ConferrerSite> entry = iterator.next();
					ConferrerSite site = entry.getValue();
					// 删除授权人
					site.removeAuthorizer(authorizer);
					// 如果没有签名，保存和准备删除它！
					if (!site.hasAuthorizers()) {
						a.add(entry.getKey());
					}
				}
				// 删除这个节点
				for (Node node : a) {
					ConferrerSite e = mapSites.remove(node);
					if (e != null) {
						mapHashs.remove(e.getHash());
					}
				}
			}
		} catch (Throwable e) {
			Logger.error(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#find(com.laxcus.site.Node)
	 */
	@Override
	public Site find(Node node) {
		super.lockMulti();
		try {
			if (node != null) {
				return mapSites.get(node);
			}
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#iterator()
	 */
	@Override
	protected Map<Node, ? extends Site> iterator() {
		return mapSites;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	//	 */
	//	@Override
	//	protected boolean infuse(Site site) {
	//		// FRONT站点参数
	//		FrontSite front = (FrontSite) site;
	//		Node node = front.getNode();
	//		User user = front.getUser();
	//		// 被授权用户
	//		Siger conferrer = user.getUsername();
	//		// 授权用户（宿主用户）
	//		Siger authorizer = front.getAuthorizer();
	//
	//		// 1. 地址不存在
	//		boolean success = (mapSites.get(node) == null);
	//		if (!success) {
	//			Logger.error(this, "infuse", "duplicate node %s", node);
	//			return false;
	//		}
	//		// 2. 查找被授权人存在
	//		User real = ConferrerStaffOnGatePool.getInstance().find(conferrer);
	//		success = (real != null && real.equals(user));
	//		if (!success) {
	//			Logger.error(this, "infuse", "illegal '%s'", user);
	//			return false;
	//		}
	//		// 3. 保存被授权人地址
	//		NodeSet nset = mapConferrers.get(conferrer);
	//		if(nset == null) {
	//			nset = new NodeSet();
	//			mapConferrers.put(conferrer, nset);
	//		}
	//		nset.add(node);
	//		// 4. 保存站点
	//		mapSites.put(node, front);
	//		front.refreshTime();
	//		// 5. 保存授权人
	//		SigerSet sset = mapAuthorizers.get(authorizer);
	//		if(sset == null) {
	//			sset = new SigerSet();
	//			mapAuthorizers.put(authorizer, sset);
	//		}
	//		sset.add(conferrer);
	//		
	//		Logger.debug(this, "infuse", success, "login %s", node);
	//		
	//		return true;
	//	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	//	 */
	//	@Override
	//	protected Site effuse(Node node) {
	//		// 删除注册站点
	//		FrontSite site = mapSites.remove(node);
	//		// 1. 判断存在
	//		boolean success = (site != null);
	//		if (success) {
	//			// 2. 删除被授权人站点地址
	//			Siger conferrer = site.getUsername();
	//			NodeSet nset = mapConferrers.get(conferrer);
	//			if (nset != null) {
	//				nset.remove(node);
	//				if (nset.isEmpty()) mapConferrers.remove(conferrer);
	//			}
	//			// 3. 删除授权人
	//			Siger authorizer = site.getAuthorizer();
	//			SigerSet sset = mapAuthorizers.get(authorizer);
	//			if (sset != null) {
	//				sset.remove(conferrer);
	//				if (sset.isEmpty()) mapAuthorizers.remove(authorizer);
	//			}
	//		}
	//		
	//		Logger.note(this, "effuse", success, "logout %s", node);
	//		
	//		// 返回被删除站点
	//		return site;
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	 */
	@Override
	protected boolean infuse(Site site) {
		ConferrerSite front = (ConferrerSite) site;
		Node node = front.getNode();
		User user = front.getConferrer();
		Siger conferrer = user.getUsername();
		Siger authorizer = front.getAuthorizers().get(0);

		// 1. 查找被授权人账号匹配
		User real = ConferrerStaffOnGatePool.getInstance().find(conferrer);
		boolean success = (real != null && real.equals(user));
		if (!success) {
			Logger.error(this, "infuse", "illegal '%s'", user);
			return false;
		}

		// 2. 二选1
		ConferrerSite that = mapSites.get(node);
		if (that == null) {
			mapSites.put(node, front);
			mapHashs.put(front.getHash(), front);
			front.refreshTime(); // 刷新时间
		} else {
			that.addAuthorizer(authorizer); // 增加一个授权人
			that.refreshTime(); // 刷新时间
		}

		// 3. 保存被授权人 -> 授权人
		SigerSet cset = mapConferrers.get(conferrer);
		if (cset == null) {
			cset = new SigerSet();
			mapConferrers.put(conferrer, cset);
		}
		cset.add(authorizer);

		// 4. 保存授权人 -> 被授权人
		SigerSet aset = mapAuthorizers.get(authorizer);
		if(aset == null) {
			aset = new SigerSet();
			mapAuthorizers.put(authorizer, aset);
		}
		aset.add(conferrer);

		Logger.debug(this, "infuse", success, "login %s", node);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 删除注册站点
		ConferrerSite site = mapSites.remove(node);
		// 1. 判断存在
		boolean success = (site != null);
		if (success) {
			Siger conferrer = site.getConferrerUsername();
			for (Siger authorizer : site.getAuthorizers()) {
				// 2. 删除授权人
				SigerSet aset = mapAuthorizers.get(authorizer);
				if (aset != null) {
					aset.remove(conferrer); // 删除被授权人
					if (aset.isEmpty()) mapAuthorizers.remove(authorizer);
				}
				// 3. 删除被授权人站点地址
				SigerSet cset = mapConferrers.get(conferrer);
				if (cset != null) {
					cset.remove(authorizer); // 删除授权人
					if (cset.isEmpty()) mapConferrers.remove(conferrer);
				}
			}
			// 删除散列码
			mapHashs.remove(site.getHash());
		}

		Logger.note(this, "effuse", success, "logout %s", node);

		// 返回被删除站点
		return site;
	}

	/**
	 * 撤销记录
	 * @param node
	 * @param authorizer
	 * @return
	 */
	private boolean effuse(Node node, Siger authorizer) {
		// 删除注册站点
		ConferrerSite site = mapSites.get(node);
		// 1. 判断存在
		boolean success = (site != null);
		if(success) {
			Siger conferrer = site.getConferrerUsername(); // 被授权人签名

			// 1. 删除授权人记录
			success = site.removeAuthorizer(authorizer);
			if (success) {
				// 没有授权人时，删除这个站点
				if (!site.hasAuthorizers()) {
					// 删除Conferrer站点
					mapSites.remove(node);
					mapHashs.remove(site.getHash());
					
					// 删除被授权人站点地址
					SigerSet set = mapConferrers.get(conferrer);
					if (set != null) {
						set.remove(authorizer);
						if (set.isEmpty()) mapConferrers.remove(conferrer);
					}
				}
			}

			// 2. 删除授权人
			if (success) {
				SigerSet set = mapAuthorizers.get(authorizer);
				if (set != null) {
					set.remove(conferrer);
					if (set.isEmpty()) mapAuthorizers.remove(authorizer);
				}
			}
		}

		Logger.note(this, "effuse", success, "logout %s#%s", node, authorizer);

		return success;
	}

	/**
	 * 删除站点关联的账号
	 * @param node 节点地址
	 * @param authorizer 授权人账号
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node, Siger authorizer) {
		boolean success = false;
		// 锁定删除
		super.lockSingle();
		try {
			success = effuse(node, authorizer);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			// 延时触发重新注册
			getLauncher().touch();
		}

		return success;
	}

	/**
	 * 当前“add”方法覆盖“HubPool.add”方法。
	 * 
	 * @see com.laxcus.pool.HubPool#add(com.laxcus.site.Site)
	 */
	@Override
	public boolean add(Site site) {
		if (site == null) {
//			throw new NullPointerException();
			return false;
		} else if (site.getFamily() != getFamily()) {
			throw new IllegalValueException("not match! %d - %d", site.getFamily(), getFamily());
		}

		Node node = site.getNode();

		boolean success = false;
		// 单向锁定
		super.lockSingle();
		try {
			// 1. 保存新的信息
			success = infuse(site);
			// 2. 保存成功，记录这个站点地址
			if (success) {
				addNode(node);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 以上操作成功，向集群传播这个站点
		if (success) {
			// 延时触发重新注册
			getLauncher().touch();
		}

		Logger.debug(this, "add", success, "from %s", node);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// TODO Auto-generated method stub

	}

	/**
	 * 根据FRONT哈希码，删除一个节点。<br>
	 * 
	 * FRONT哈希码，基于MAC地址和所属类名生成，是FRONT节点的唯一值。
	 * 这要求每台计算机，只能有一个FRONT节点登录。
	 * 
	 * @param hash
	 * @return 返回被删除的节点地址
	 */
	public Node remove(ClassCode hash) {
		// 如果是空值，忽略！
		if (hash == null) {
			Logger.error(this, "remove", "FrontHash is null pointer!");
			return null;
		}
		
		Node node = null;
	
		// 以锁定方式进行删除操作
		super.lockSingle();
		try {
			ConferrerSite site = mapHashs.remove(hash);
			if (site != null) {
				node = site.getNode();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		// 判断有效，删除其它值
		if (node != null) {
			remove(node);
		}
		
		Logger.debug(this, "remove", node != null, "drop %s # %s", hash, node);

		// 返回FRONT运行节点
		return node;
	}
}