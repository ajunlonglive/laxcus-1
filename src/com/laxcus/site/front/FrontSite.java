/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.front;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.net.*;

/**
 * 前端站点。<br><br>
 * 
 * 六种模式：CONSOLE、TERMINAL、DRIVER、EDGE、DESKTOP、APPLICATION
 * 
 * @author scott.liang
 * @version 1.3 6/9/2015
 * @since laxcus 1.0
 */
public final class FrontSite extends Site {

	private static final long serialVersionUID = -2745612411768136184L;
	
	/** FRONT标记码，唯一性标记 **/
	private ClassCode hash;
	
	/** 账号权级(管理员或者普通用户) **/
	private int grade;

	/** 用户账号(每个登录用户只能拥有一个账号) **/
	private User user;
	
	/** 边缘容器通信地址 **/
	private SocketHost tub;

	/** 账号所属的数据库表集合。如果是管理员，此项空。  **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 将FRONT站点信息写入可类化写入器
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 散列码
		writer.writeObject(hash);
		
		// 用户级别
		writer.writeInt(grade);
		// 用户账号
		writer.writeObject(user);
		
		// 表的数目和写入每一个表
		writer.writeInt(array.size());
		for (Space space : array) {
			writer.writeObject(space);
		}
		// 边缘容器地址
		writer.writeInstance(tub);
	}

	/**
	 * 从可类化读取器中解析FRONT站点信息
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		hash = new ClassCode(reader);
		
		// 用户权级
		grade = reader.readInt();
		// 用户账号
		user = new User(reader);
		
		// 表的数目
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space space = new Space(reader);
			array.add(space);
		}
		// 边缘容器通信地址
		tub = reader.readInstance(SocketHost.class);
	}

	/**
	 * 根据传入的FRONT站点实例，生成它的数据副本
	 * @param that FrontSite站点
	 */
	private FrontSite(FrontSite that) {
		super(that);
		hash = that.hash;
		
		grade = that.grade;
		user = that.user.duplicate();
		array.addAll(that.array);
		if (that.tub != null) {
			tub = that.tub.duplicate();
		}
	}

	/**
	 * 构造一个默认的FRONT站点
	 */
	public FrontSite() {
		super(SiteTag.FRONT_SITE);
		
		// 离线未使用
		grade = GradeTag.OFFLINE;
	}

	/**
	 * 从可类化读取器中解析FRONT站点信息
	 * @param reader 可类化数据读取器
	 * @since 1.3
	 */
	public FrontSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置散列码
	 * @param e
	 */
	public void setHash(ClassCode e) {
		hash = e;
	}

	/**
	 * 返回散列码
	 * @return
	 */
	public ClassCode getHash() {
		return hash;
	}

	/**
	 * 设置边缘容器通信地址
	 * @param e SocketHost
	 */
	public void setTub(SocketHost e) {
		tub = e;
	}

	/**
	 * 返回边缘容器通信地址
	 * @return SocketHost
	 */
	public SocketHost getTub() {
		return tub;
	}

	/**
	 * 设置用户级别
	 * @param who 用户级别
	 */
	public void setGrade(int who) {
		if (!GradeTag.isGrade(who)) {
			throw new IllegalValueException("illegal grade %d", who);
		}
		grade = who;
	}

	/**
	 * 返回用户级别
	 * @return 用户级别的数字描述
	 */
	public int getGrade() {
		return grade;
	}

	/**
	 * 判断是离线未使用的状态
	 * @return 返回真或者假
	 */
	public boolean isOffline() {
		return GradeTag.isOffline(grade);
	}

	/**
	 * 判断是系统管理员
	 * @return 返回真或者假
	 */
	public boolean isAdministrator() {
		return GradeTag.isAdministrator(grade);
	}

	/**
	 * 判断是普通注册用户
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		return GradeTag.isUser(grade);
	}

	/**
	 * 设置明文格式的注册用户账号
	 * @param username 用户名称
	 * @param password 密码
	 */
	public void setUser(String username, String password) {
		user = new User(username, password);
	}

	/**
	 * 设置SHA256编码格式的注册用户账号
	 * @param username 用户名称
	 * @param password 密码
	 */
	public void setUser(Siger username, SHA512Hash password) {
		user = new User(username, password);
	}

	/**
	 * 设置注册用户账号
	 * @param e User实例
	 */
	public void setUser(User e) {
		user = e;
	}

	/**
	 * 返回注册用户账号
	 * @return User实例
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 返回注册用户名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		if (user != null) {
			return user.getUsername();
		}
		return null;
	}

	/**
	 * 保存一个数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean add(Space e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个数据表名
	 * @param e Space实例
	 * @return 返回真或者假
	 */
	public boolean remove(Space e) {
		return array.remove(e);
	}

	/**
	 * 是否包含一个表
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean inside(Space space) {
		return array.contains(space);
	}

	/**
	 * 返回全部数据表名 
	 * @return Space列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 返回表名数目
	 * @return 表名数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public FrontSite duplicate() {
		return new FrontSite(this);
	}
}