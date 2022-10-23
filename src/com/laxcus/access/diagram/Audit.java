/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.util.*;
import java.io.Serializable;

import com.laxcus.access.schema.*;
import com.laxcus.law.cross.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户基础资料<br>
 * 用户基础资料是资源记录器，包括用户登录账号，用户、数据库、表三级操纵权限许可<br><br>
 * 
 * 每个用户基础资料下拥有任意多个数据库，一个数据库下拥有任意多个表。<br>
 * 用户操作权限有三级：用户级、数据库级、数据库表级(见Permit定义)。<br>
 * 
 * @author scott.liang
 * @version 1.4 7/5/2017
 * @since laxcus 1.0
 */
public abstract class Audit implements Classable, Markable, Serializable, Cloneable, Comparable<Audit> {

	private static final long serialVersionUID = -6452187096603988165L;

	/** 最后一次刷新时间，用于超时判断，是临时参数，可类化和标记化都不记录它。**/
	private transient long refreshTime;

	/** 用户登录账号 **/
	private User user = new User();

	/** 用户级权限表 **/
	private UserPermit userPermit = new UserPermit();

	/** 数据库级权限表 **/
	private SchemaPermit schemaPermit = new SchemaPermit();

	/** 表级权限表 **/
	private TablePermit tablePermit = new TablePermit();

	/** 限制操作集合 **/
	private TreeSet<LimitItem> limits = new TreeSet<LimitItem>();

	/** 授权共享单元，由授权人持有 **/
	private TreeSet<ActiveItem> actives = new TreeSet<ActiveItem>();

	/** 被授权共享单元，由被授权人持有 **/
	private TreeSet<PassiveItem> passives = new TreeSet<PassiveItem>();

	/**
	 * 将用户基础资料参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		// 用户
		writer.writeObject(user);
		// 用户权限
		writer.writeObject(userPermit);
		// 数据库权限
		writer.writeObject(schemaPermit);
		// 表权限
		writer.writeObject(tablePermit);
		// 限制操作集合
		writer.writeInt(limits.size());
		for (LimitItem e : limits) {
			writer.writeObject(e);
		}
		// 授权单元
		writer.writeInt(actives.size());
		for (ActiveItem e : actives) {
			writer.writeObject(e);
		}
		// 被授权单元
		writer.writeInt(passives.size());
		for (PassiveItem e : passives) {
			writer.writeObject(e);
		}

		// 写入子类信息
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析用户基础资料参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 注册用户
		user.resolve(reader);
		// 用户权限
		userPermit.resolve(reader);
		// 数据库权限
		schemaPermit.resolve(reader);
		// 表权限
		tablePermit.resolve(reader);
		// 限制操作集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			LimitItem e = LimitItemCreator.resolve(reader);
			limits.add(e);
		}
		// 授权单元
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ActiveItem e = new ActiveItem(reader);
			actives.add(e);
		}
		// 被授权单元
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PassiveItem e = new PassiveItem(reader);
			passives.add(e);
		}

		// 读取子类信息
		resolveSuffix(reader);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的用户基础资料。同时定义构造时间
	 */
	protected Audit() {
		super();
		refreshTime();
	}

	/**
	 * 根据传入的用户基础资料实例，生成它的副本
	 * @param that 用户基础资料实例
	 */
	protected Audit(Audit that) {
		this();
		user = that.user.duplicate();
		// 权限
		userPermit.add(that.userPermit);
		schemaPermit.add(that.schemaPermit);
		tablePermit.add(that.tablePermit);
		// 限制操作
		limits.addAll(that.limits);
		// 授权单元集合
		actives.addAll(that.actives);
		// 被授权单元集合
		passives.addAll(that.passives);
	}

	/**
	 * 刷新时间
	 */
	public void refreshTime() {
		refreshTime = System.currentTimeMillis();
	}

	/**
	 * 判断超时
	 * @param timeout 超时时间，单位：毫秒
	 * @return 返回真或者假
	 */
	public boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - refreshTime >= timeout;
	}

	/**
	 * 设置用户账号
	 * @param e 用户账号
	 */
	public void setUser(User e) {
		Laxkit.nullabled(e);
		// 赋值
		user = e;
	}

	/**
	 * 返回用户账号
	 * @return User实例
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * 判断为有效状态
	 * @return 返回真或者假
	 */
	public boolean isEnabled() {
		return user != null && user.isEnabled();
	}

	/**
	 * 判断为无效状态
	 * @return 返回真或者假
	 */
	public boolean isDisabled() {
		return user != null && user.isDisabled();
	}

	/**
	 * 返回注册用户名
	 * @return 数据签名人
	 */
	public Siger getUsername() {
		return user.getUsername();
	}

	/**
	 * 清除授权单元
	 */
	public void clearActiveItems() {
		actives.clear();
	}

	/**
	 * 清除被授权单元
	 */
	public void clearPassiveItems() {
		passives.clear();
	}

	/**
	 * 保存授权单元。由授权人持有
	 * @param that 授权单元
	 * @return 返回真或者假
	 */
	public boolean addActiveItem(ActiveItem that) {
		Laxkit.nullabled(that);

		for (ActiveItem item : actives) {
			// 如果相似，执行或操作，合并成一个（被授权账号和表名一致即是相似）
			if (item.alike(that)) {
				int operator = CrossOperator.or(item.getOperator(), that.getOperator());
				item.setOperator(operator);
				item.getFlag().refreshCreateTime();
				return true;
			}
		}
		// 以上没有找到，保存它
		return actives.add(that);
	}

	/**
	 * 保存授权单元。
	 * @param conferrer 被授权人
	 * @param flag 共享标识
	 * @return 返回真或者假
	 */
	public boolean addActiveItem(Siger conferrer, CrossFlag flag) {
		return addActiveItem(new ActiveItem(conferrer, flag));
	}

	/**
	 * 保存一批授权单元
	 * @param a 授权单元集合
	 * @return 返回新增成员数目
	 */
	public int addActiveItems(Collection<ActiveItem> a) {
		int count = 0;
		for (ActiveItem e : a) {
			boolean success = addActiveItem(e);
			if (success) count++;
		}
		return count;
	}

	/**
	 * 保存一批授权单元
	 * @param conferrer 被授权人
	 * @param flags 共享标识集合
	 * @return 返回新增成员数目
	 */
	public int addActiveItems(Siger conferrer, Collection<CrossFlag> flags) {
		ArrayList<ActiveItem> array = new ArrayList<ActiveItem>();
		for (CrossFlag flag : flags) {
			ActiveItem e = new ActiveItem(conferrer, flag);
			array.add(e);
		}
		return addActiveItems(array);
	}

	/**
	 * 删除授权单元
	 * @param that 授权单元
	 * @return 返回真或者假
	 */
	public boolean removeActiveItem(ActiveItem that) {
		Laxkit.nullabled(that);

		for (ActiveItem item : actives) {
			if (item.alike(that)) {
				// 异或操作，同值清零
				int operator = CrossOperator.xor(item.getOperator(), that.getOperator());
				// 清零，删除它，否则重置操作符
				if (CrossOperator.isNone(operator)) {
					return actives.remove(item);
				} else {
					item.setOperator(operator);
					item.getFlag().refreshCreateTime();
					return true;
				}
			}
		}

		// 删除内存记录
		return actives.remove(that);
	}

	/**
	 * 删除指定表的授权单元
	 * @param e 数据表名
	 * @return 返回删除成员数目
	 */
	public int removeActiveItem(Space e) {
		Laxkit.nullabled(e);

		List<ActiveItem> items = findActiveItems(e);
		for (ActiveItem item : items) {
			removeActiveItem(item);
		}
		return items.size();
	}

	/**
	 * 删除授权单元
	 * @param conferrer 被授权人
	 * @param flag 共享标识
	 * @return 返回真或者假
	 */
	public boolean removeActiveItem(Siger conferrer, CrossFlag flag) {
		return removeActiveItem(new ActiveItem(conferrer, flag));
	}

	/**
	 * 删除一批授权单元
	 * @param a 授权单元集合
	 * @return 返回被删除成员数目
	 */
	public int removeActiveItems(Collection<ActiveItem> a) {
		int count = 0;
		for (ActiveItem e : a) {
			boolean success = removeActiveItem(e);
			if(success) count++;
		}
		return count;
	}

	/**
	 * 删除一批授权单元
	 * @param conferrer 被授权人
	 * @param flags 授权共享标识集合
	 * @return 返回被删除成员数目
	 */
	public int removeActiveItems(Siger conferrer, Collection<CrossFlag> flags) {
		ArrayList<ActiveItem> array = new ArrayList<ActiveItem>();
		for (CrossFlag flag : flags) {
			ActiveItem e = new ActiveItem(conferrer, flag);
			array.add(e);
		}
		return removeActiveItems(array);
	}

	/**
	 * 返回全部授权单元
	 * @return 授权单元列表
	 */
	public List<ActiveItem> getActiveItems() {
		return new ArrayList<ActiveItem>(actives);
	}

	/**
	 * 判断授权单元是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmptyActiveItems() {
		return actives.isEmpty();
	}

	/**
	 * 返回账号中的被授权人签名
	 * @return 被授权人签名列表
	 */
	public List<Siger> getActiveConferrers() {
		TreeSet<Siger> array = new TreeSet<Siger>();
		for(ActiveItem e : actives) {
			array.add(e.getConferrer());
		}
		return new ArrayList<Siger>(array);
	}

	/**
	 * 返回全部授权表名
	 * @return 数据表名列表
	 */
	public List<Space> getActiveTables() {
		TreeSet<Space> array = new TreeSet<Space>();
		for (ActiveItem e : actives) {
			array.add(e.getSpace());
		}
		return new ArrayList<Space>(array);
	}

	/**
	 * 判断当前账号中有被授权人
	 * @param conferrer 被授权人签名
	 * @return 返回真或者假
	 */
	public boolean hasActiveConferrer(Siger conferrer) {
		List<Siger> sigers = getActiveConferrers();
		return sigers.contains(conferrer);
	}

	/**
	 * 无条件释放基于某个表的授权单元
	 * @param space 表名
	 * @return 返回删除的单元数目
	 */
	public int dropActiveItems(Space space) {
		List<ActiveItem> array = findActiveItems(space);
		int size = array.size();
		if (size > 0) {
			actives.removeAll(array);
		}
		return size;
	}

	/**
	 * 查找与某个表名关联的授权单元
	 * @param space 表名 
	 * @return 授权单元数组
	 */
	public List<ActiveItem> findActiveItems(Space space) {
		TreeSet<ActiveItem> array = new TreeSet<ActiveItem>();
		for (ActiveItem e : actives) {
			if (Laxkit.compareTo(e.getSpace(), space) == 0) {
				array.add(e);
			}
		}
		return new ArrayList<ActiveItem>(array);
	}

	/**
	 * 查找与某个被授权人关联的授权单元
	 * 
	 * @param conferrer 被授权人签名
	 * @return 授权单元列表
	 */
	public List<ActiveItem> findActiveItems(Siger conferrer) {
		TreeSet<ActiveItem> array = new TreeSet<ActiveItem>();
		for (ActiveItem e : actives) {
			if (Laxkit.compareTo(e.getConferrer(), conferrer) == 0) {
				array.add(e);
			}
		}
		return new ArrayList<ActiveItem>(array);
	}

	/**
	 * 无条件删除基于某个表的被授权单元
	 * 
	 * @param space 表名
	 * @return 返回删除的被授权单元数目
	 */
	public int dropPassiveItems(Space space) {
		List<PassiveItem> array = findPassiveItems(space);
		int size = array.size();
		if (size > 0) {
			passives.removeAll(array);
		}
		return size;
	}

	/**
	 * 查找与某个表名关联的被授权单元
	 * 
	 * @param space 表名
	 * @return 返回被授权单元列表
	 */
	public List<PassiveItem> findPassiveItems(Space space) {
		TreeSet<PassiveItem> array = new TreeSet<PassiveItem>();
		for (PassiveItem e : passives) {
			if (Laxkit.compareTo(e.getSpace(), space) == 0) {
				array.add(e);
			}
		}
		return new ArrayList<PassiveItem>(array);
	}

	/**
	 * 查找与某个授权人关联的被授权单元
	 * 
	 * @param authorizer 授权人
	 * @return 返回被授权单元列表
	 */
	public List<PassiveItem> findPassiveItems(Siger authorizer) {
		TreeSet<PassiveItem> array = new TreeSet<PassiveItem>();
		for (PassiveItem e : passives) {
			if (Laxkit.compareTo(e.getAuthorizer(), authorizer) == 0) {
				array.add(e);
			}
		}
		return new ArrayList<PassiveItem>(array);
	}
	
	/**
	 * 查找与某个表名关联的被授权单元
	 * 
	 * @param space 表名
	 * @return 返回被授权单元列表
	 */
	public List<PassiveItem> findPassiveItems(Siger authorizer, Space space) {
		TreeSet<PassiveItem> array = new TreeSet<PassiveItem>();
		// 逐个检查！
		for (PassiveItem e : passives) {
			if (Laxkit.compareTo(e.getAuthorizer(), authorizer) == 0
					&& Laxkit.compareTo(e.getSpace(), space) == 0) {
				array.add(e);
			}
		}
		return new ArrayList<PassiveItem>(array);
	}

	/**
	 * 保存被授权单元。由被授权人持有
	 * @param that 被授权单元
	 * @return 返回真或者假
	 */
	public boolean addPassiveItem(PassiveItem that) {
		Laxkit.nullabled(that);

		// 找到关联单元，进行或操作
		for (PassiveItem item : passives) {
			// 如果相似，执行或操作，合并成一个
			if (item.alike(that)) {
				int operator = CrossOperator.or(item.getOperator(), that.getOperator());
				item.setOperator(operator);
				item.getFlag().refreshCreateTime();
				return true;
			}
		}
		// 没有相似的，保存它
		return passives.add(that);
	}

	/**
	 * 保存被授权单元。
	 * @param authorizer 授权人
	 * @param flag 共享标识
	 * @return 返回真或者假
	 */
	public boolean addPassiveItem(Siger authorizer, CrossFlag flag) {
		return addPassiveItem(new PassiveItem(authorizer, flag));
	}

	/**
	 * 保存一批被授权单元。由被授权人持有。
	 * @param a 被授权单元集合
	 * @return 返回新增成员数目
	 */
	public int addPassiveItems(Collection<PassiveItem> a) {
		int count = 0;
		for (PassiveItem e : a) {
			boolean success = addPassiveItem(e);
			if (success) count++;
		}
		return count;
	}

	/**
	 * 保存一批被授权单元
	 * @param authorizer 授权人
	 * @param flags 共享标识集合
	 * @return 返回新增成员数目
	 */
	public int addPassiveItems(Siger authorizer, Collection<CrossFlag> flags) {
		ArrayList<PassiveItem> array = new ArrayList<PassiveItem>();
		for (CrossFlag flag : flags) {
			PassiveItem e = new PassiveItem(authorizer, flag);
			array.add(e);
		}
		return addPassiveItems(array);
	}

	/**
	 * 删除被授权单元
	 * @param that 被授权单元
	 * @return 返回真或者假
	 */
	public boolean removePassiveItem(PassiveItem that) {
		Laxkit.nullabled(that);

		for (PassiveItem item : passives) {
			// 判断除操作符外，其它参数相似
			if (item.alike(that)) {
				// 异或操作，同值清零，否则重置操作符
				int operator = CrossOperator.xor(item.getOperator(), that.getOperator());
				if (CrossOperator.isNone(operator)) {
					return passives.remove(item);
				} else {
					item.setOperator(operator);
					item.getFlag().refreshCreateTime();
					return true;
				}
			}
		}

		return passives.remove(that);
	}

	/**
	 * 删除指定表的被授权单元
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean removePassiveItem(Space space) {
		Laxkit.nullabled(space);

		List<PassiveItem> items = findPassiveItems(space);
		int count = 0;
		for (PassiveItem item : items) {
			boolean success = removePassiveItem(item);
			if (success) count++;
		}
		return count > 0;
	}

	/**
	 * 删除指定表的被授权单元
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean removePassiveItem(Siger authorizer, Space space) {
		Laxkit.nullabled(space);
		Laxkit.nullabled(authorizer);

		List<PassiveItem> items = findPassiveItems(authorizer, space);
		int count = 0;
		for (PassiveItem item : items) {
			boolean success = removePassiveItem(item);
			if (success) count++;
		}
		return count > 0;
	}

	/**
	 * 删除被授权单元
	 * @param authorizer 授权人
	 * @param flag 共享标识
	 * @return 返回真或者假
	 */
	public boolean removePassiveItem(Siger authorizer, CrossFlag flag) {
		return removePassiveItem(new PassiveItem(authorizer, flag));
	}

	/**
	 * 删除一批被授权单元
	 * @param a 被授权单元集合
	 * @return 返回被删除成员数目
	 */
	public int removePassiveItems(Collection<PassiveItem> a) {
		int count = 0;
		for (PassiveItem e : a) {
			boolean success = removePassiveItem(e);
			if (success) count++;
		}
		return count;
	}

	/**
	 * 删除一批被授权单元
	 * @param authorizer 授权人
	 * @param flags 授权共享标识集合
	 * @return 返回被删除成员数目
	 */
	public int removePassiveItems(Siger authorizer, Collection<CrossFlag> flags) {
		ArrayList<PassiveItem> array = new ArrayList<PassiveItem>();
		for (CrossFlag flag : flags) {
			PassiveItem e = new PassiveItem(authorizer, flag);
			array.add(e);
		}
		return removePassiveItems(array);
	}

	/**
	 * 返回全部被授权单元
	 * @return 被授权单元列表
	 */
	public List<PassiveItem> getPassiveItems() {
		return new ArrayList<PassiveItem>(passives);
	}

	/**
	 * 判断被授权单元是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmptyPassiveItems() {
		return passives.isEmpty();
	}

	/**
	 * 返回账号中的授权人
	 * @return 授权人签名
	 */
	public List<Siger> getPassiveAuthorizers() {
		Set<Siger> array = new TreeSet<Siger>();
		for (PassiveItem e : passives) {
			array.add(e.getAuthorizer());
		}
		return new ArrayList<Siger>(array);
	}

	/**
	 * 判断当前被授权单元中有授权人签名
	 * @param authorizer 授权人签名
	 * @return 返回真或者否
	 */
	public boolean hasPassiveAuthorizer(Siger authorizer) {
		List<Siger> sigers = getPassiveAuthorizers();
		return sigers.contains(authorizer);
	}

	/**
	 * 根据表名，找到关联的被授权单元
	 * @param e 表名
	 * @return 返回被授权单元，或者空指针
	 */
	public PassiveItem findPassiveItem(Space e) {
		for (PassiveItem item : passives) {
			if (Laxkit.compareTo(item.getSpace(), e) == 0) {
				return item;
			}
		}
		return null;
	}

	/**
	 * 根据表名，找到关联的授权人
	 * @param space 表名
	 * @return Siger实例
	 */
	public Siger findPassiveAuthorizer(Space space) {
		PassiveItem e = findPassiveItem(space);
		return (e != null ? e.getAuthorizer() : null);
	}

	/**
	 * 返回全部被授权表名
	 * @return 数据表名列表
	 */
	public List<Space> getPassiveTables() {
		TreeSet<Space> array = new TreeSet<Space>();
		for (PassiveItem e : passives) {
			array.add(e.getSpace());
		}
		return new ArrayList<Space>(array);
	}

	/**
	 * 判断有被授权表存在
	 * @param e 表名
	 * @return 返回真或者假
	 */
	public boolean hasPassiveTable(Space e) {
		List<Space> a = getPassiveTables();
		return a.contains(e);
	}

	/**
	 * 判断有授权表存在
	 * @param e 表名
	 * @return 返回真或者假
	 */
	public boolean hasActiveTable(Space e) {
		List<Space> a = getActiveTables();
		return a.contains(e);
	}

	/**
	 * 判断传入的共享标识匹配某个被授权单元
	 * 
	 * @param flag 传入的共享标识
	 * @return 存在返回真，否则假
	 */
	public boolean hasPassiveFlag(CrossFlag flag) {
		for (PassiveItem item : passives) {
			if (item.allow(flag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断有完全匹配的被授权单元
	 * @param item 被授权单元实例
	 * @return 存在返回真，否则假
	 */
	public boolean hasPassiveItem(PassiveItem item) {
		return passives.contains(item);
	}

	/**
	 * 判断传入的共享标识匹配某个授权单元
	 * @param flag 共享标识
	 * @return 存在返回真，否则假
	 */
	public boolean hasActiveFlag(CrossFlag flag) {
		for (ActiveItem item : actives) {
			if (item.allow(flag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 拥有管理员权限（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canDBA() {
		return userPermit.allow(ControlTag.DBA_OPTIONS);
	}

	/**
	 * 判断执行用户账号操作
	 * @param operator 操作符
	 * @return 返回真或者假
	 */
	public boolean canUser(short operator) {
		return userPermit.allow(operator);
	}

	/**
	 * 允许建立账号（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canCreateUser() {
		return userPermit.allow(ControlTag.CREATE_USER);
	}

	/**
	 * 允许删除账号（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canDropUser() {
		return userPermit.allow(ControlTag.DROP_USER);
	}

	/**
	 * 允许修改账号密码（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canAlterUser() {
		return userPermit.allow(ControlTag.ALTER_USER);
	}

	/**
	 * 判断支持开放共享资源（用户级别）
	 * @return 返回真或者假
	 */
	public boolean canOpenResource() {
		return userPermit.allow(ControlTag.OPEN_RESOURCE);
	}

	/**
	 * 判断支持关闭共享资源（用户级别）
	 * @return 返回真或者假
	 */
	public boolean canCloseResource() {
		return userPermit.allow(ControlTag.CLOSE_RESOURCE);
	}

	/**
	 * 允许设置权限(用户级别)
	 * @return 返回真或者假。
	 */
	public boolean canGrant() {
		return userPermit.allow(ControlTag.GRANT);
	}

	/**
	 * 允许回收权限（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canRevoke() {
		return userPermit.allow(ControlTag.REVOKE);
	}

	/**
	 * 允许发布分布任务组件（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canPublishTask() {
		return userPermit.allow(ControlTag.PUBLISH_TASK);
	}

	/**
	 * 允许发布分布任务组件动态链接库（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canPublishTaskLibrary() {
		return userPermit.allow(ControlTag.PUBLISH_TASK_LIBRARY);
	}

	/**
	 * 允许加载数据块索引到内存（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canLoadIndex() {
		return userPermit.allow(ControlTag.LOAD_INDEX);
	}

	/**
	 * 允许加载数据块到内存（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canLoadEntity() {
		return userPermit.allow(ControlTag.LOAD_ENTITY);
	}

	/**
	 * 允许独享计算机资源（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canExclusive() {
		return userPermit.allow(ControlTag.EXCLUSIVE);
	}

	/**
	 * 判断能够执行允许的数据库操作
	 * @param fame 数据库名
	 * @param operator 操作符
	 * @return 返回真或者假
	 */
	public boolean canSchema(Fame fame, short operator) {
		boolean success = userPermit.allow(operator);
		if (!success) {
			success = schemaPermit.allow(fame, operator);
		}
		return success;
	}

	/**
	 * 判断有建立数据库权限（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canCreateSchema() {
		return userPermit.allow(ControlTag.CREATE_SCHEMA);
	}

	/**
	 * 判断有建立数据库权限
	 * @param fame 数据库名称
	 * @return 返回真或者假。
	 */
	public boolean canCreateSchema(Fame fame) {
		boolean success = userPermit.allow(ControlTag.CREATE_SCHEMA);
		if (!success) {
			success = schemaPermit.allow(fame, ControlTag.CREATE_SCHEMA);
		}
		return success;
	}

	/**
	 * 允许删除数据库（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canDropSchema() {
		return userPermit.allow(ControlTag.DROP_SCHEMA);
	}

	/**
	 * 允许删除数据库。从高到低，用户级/数据库级逐次检查
	 * @param fame 数据库名称
	 * @return 返回真或者假。
	 */
	public boolean canDropSchema(Fame fame) {
		boolean success = userPermit.allow(ControlTag.DROP_SCHEMA);
		if (!success) {
			success = schemaPermit.allow(fame, ControlTag.DROP_SCHEMA);
		}
		return success;
	}

	/**
	 * 判断能够执行数据表操作
	 * @param space 数据表名
	 * @param operator 操作符
	 * @return 返回真或者假
	 */
	public boolean canTable(Space space, short operator) {
		boolean success = userPermit.allow(operator);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), operator);
		}
		if (!success) {
			success = tablePermit.allow(space, operator);
		}
		return success;
	}

	/**
	 * 允许建立表(用户和数据库级别)
	 * @param space 表名
	 * @return 返回真或者假。
	 */
	public boolean canCreateTable(Space space) {
		boolean success = userPermit.allow(ControlTag.CREATE_TABLE);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.CREATE_TABLE);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.CREATE_TABLE);
		}
		return success;
	}

	/**
	 * 允许建表（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canCreateTable() {
		return userPermit.allow(ControlTag.CREATE_TABLE);
	}

	/**
	 * 允许删除表。从高到低依次执行：用户级别、数据库级别、表级别
	 * @param space 数据表名
	 * @return 返回真或者假。
	 */
	public boolean canDropTable(Space space) {
		// 用户级
		boolean success = userPermit.allow(ControlTag.DROP_TABLE);
		// 数据库级
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.DROP_TABLE);
		}
		// 表级
		if (!success) {
			success = tablePermit.allow(space, ControlTag.DROP_TABLE);
		}
		return success;
	}

	/**
	 * 允许删除表（用户级别）
	 * @return 返回真或者假。
	 */
	public boolean canDropTable() {
		return userPermit.allow(ControlTag.DROP_TABLE);
	}

	/**
	 * 允许执行SELECT操作
	 * @param space
	 * @return 返回真或者假。
	 */
	public boolean canSelect(Space space) {
		boolean success = userPermit.allow(ControlTag.SELECT);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.SELECT);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.SELECT);
		}
		return success;
	}

	/**
	 * 允许执行用户级别的SELECT操作
	 * @return 返回真或者假。
	 */
	public boolean canSelect() {
		return userPermit.allow(ControlTag.SELECT);
	}

	/**
	 * 允许执行DELETE操作
	 * @param space
	 * @return 返回真或者假。
	 */
	public boolean canDelete(Space space) {
		boolean success = userPermit.allow(ControlTag.DELETE);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.DELETE);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.DELETE);
		}
		return success;
	}

	/**
	 * 允许执行用户级别的DELETE操作
	 * @return 返回真或者假。
	 */
	public boolean canDelete() {
		return userPermit.allow(ControlTag.DELETE);
	}

	/**
	 * 允许执行INSERT/INJECT操作
	 * @param space Space实例
	 * @return 返回真或者假。
	 */
	public boolean canInsert(Space space) {
		boolean success = userPermit.allow(ControlTag.INSERT);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.INSERT);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.INSERT);
		}
		return success;
	}

	/**
	 * 允许执行用户级别的INSERT/INJECT操作
	 * @return 返回真或者假。
	 */
	public boolean canInsert() {
		return userPermit.allow(ControlTag.INSERT);
	}

	/**
	 * 允许执行UPDATE操作
	 * @param space Space实例
	 * @return 返回真或者假。
	 */
	public boolean canUpdate(Space space) {
		boolean success = userPermit.allow(ControlTag.UPDATE);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.UPDATE);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.UPDATE);
		}
		return success;
	}

	/**
	 * 允许执行用户级别的UPDATE操作
	 * @return 返回真或者假。
	 */
	public boolean canUpdate() {
		return userPermit.allow(ControlTag.UPDATE);
	}

	/**
	 * 允许执行CONDUCT操作
	 * @param space Space实例
	 * @return 返回真或者假。
	 */
	public boolean canConduct(Space space) {
		boolean success = userPermit.allow(ControlTag.CONDUCT);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.CONDUCT);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.CONDUCT);
		}
		return success;
	}

	/**
	 * 允许执行用户级别的CONDUCT操作
	 * @return 返回真或者假。
	 */
	public boolean canConduct() {
		return userPermit.allow(ControlTag.CONDUCT);
	}

	/**
	 * 允许执行用户级别的CONTACT操作
	 * @return 返回真或者假。
	 */
	public boolean canContact() {
		return userPermit.allow(ControlTag.CONTACT);
	}

	/**
	 * 允许执行用户级别的ESTABLISH操作
	 * @return 返回真或者假。
	 */
	public boolean canEstablish() {
		return userPermit.allow(ControlTag.ESTABLISH);
	}

	/**
	 * 允许执行ESTABLISH操作
	 * @param space Space实例
	 * @return 返回真或者假。
	 */
	public boolean canEstablish(Space space) {
		boolean success = userPermit.allow(ControlTag.ESTABLISH);
		if (!success) {
			success = schemaPermit.allow(space.getSchema(), ControlTag.ESTABLISH);
		}
		if (!success) {
			success = tablePermit.allow(space, ControlTag.ESTABLISH);
		}
		return success;
	}

	/**
	 * 授权（增加账号的操作权限）
	 * @param permit 待增加的权限
	 * @return 授权成功返回“真”，否则“假”。
	 */
	public boolean grant(Permit permit) {
		if (permit.getClass() == tablePermit.getClass()) {
			return tablePermit.add(permit);
		} else if (permit.getClass() == schemaPermit.getClass()) {
			return schemaPermit.add(permit);
		} else if (permit.getClass() == userPermit.getClass()) {
			return userPermit.add(permit);
		}
		return false;
	}

	/**
	 * 解除权限（删除账号的操作权限）
	 * @param permit 待解除的权限
	 * @return 解除成功返回“真”，否则“假”。
	 */
	public boolean revoke(Permit permit) {
		if (permit.getClass() == tablePermit.getClass()) {
			return tablePermit.remove(permit);
		} else if (permit.getClass() == schemaPermit.getClass()) {
			return schemaPermit.remove(permit);
		} else if (permit.getClass() == userPermit.getClass()) {
			return userPermit.remove(permit);
		}
		return false;
	}

	/**
	 * 返回权限许可数组
	 * @return Permit数组
	 */
	public Permit[] toPermitArray() {
		return new Permit[] { tablePermit, schemaPermit, userPermit };
	}

	/**
	 * 返回用户级操作许可
	 * @return UserPermit实例
	 */
	public UserPermit getUserPermit() {
		return userPermit;
	}

	/**
	 * 返回数据库操作许可
	 * @return
	 */
	public SchemaPermit getSchemaPermit() {
		return schemaPermit;
	}

	/**
	 * 返回数据表操作许可
	 * @return
	 */
	public TablePermit getTablePermit() {
		return tablePermit;
	}

	/**
	 * 返回权限许可列表
	 * @return List<Permit>
	 */
	public List<Permit> getPermits() {
		ArrayList<Permit> array = new ArrayList<Permit>();
		array.add(tablePermit);
		array.add(schemaPermit);
		array.add(userPermit);
		return array;
	}

	/**
	 * 建立一批限制操作规则
	 * @param input
	 * @return 返回被建立的限制操作规则
	 */
	public List<LimitItem> createLimitItems(Collection<LimitItem> input) {
		ArrayList<LimitItem> array = new ArrayList<LimitItem>();
		for (LimitItem item : input) {
			boolean success = limits.add(item);
			// 保存成功，记录它
			if (success) array.add(item);
		}
		return array;
	}

	/**
	 * 删除一批限制操作规则
	 * @param input
	 * @return 返回被删除的限制操作规则
	 */
	public List<LimitItem> dropLimitItems(Collection<LimitItem> input) {
		ArrayList<LimitItem> array = new ArrayList<LimitItem>();
		for (LimitItem item : input) {
			boolean success = limits.remove(item);
			// 保存成功，记录它
			if (success) array.add(item);
		}
		return array;
	}

	/**
	 * 输出全部限制操作
	 * @return
	 */
	public List<LimitItem> getLimitItems() {
		return new ArrayList<LimitItem>(limits);
	}

	/**
	 * 判断包含一个限制操作
	 * @param e
	 * @return 返回真或者假
	 */
	public boolean hasLimit(LimitItem e) {
		if (e != null) {
			return limits.contains(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * 返回用户基础资料的字符串描述 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return user.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Audit that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(user.getUsername(), that.user.getUsername());
	}

	/**
	 * 返回字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 根据实际的子类操作权限，生成一个它的副本
	 * @return Audit子类实例
	 */
	public abstract Audit duplicate();

	/**
	 * 将子类参数写入可类化存储器
	 * @param writer 可类化存储器
	 */
	public abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类参数
	 * @param reader 可类化读取器
	 */
	public abstract void resolveSuffix(ClassReader reader);
}