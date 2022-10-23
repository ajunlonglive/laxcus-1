/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 数据库管理员账号。<br>
 * 管理员账号文件配置在TOP节点的local.xml中指定，也是经过SHA256散列后的密文。<br>
 * 明文数据只能是管理员账号建立者拥有，建立管理员账号的程序在附件包中。<br><br>
 * 
 * 设计规定：管理员拥有全部管理权限，但是不具有数据操作权限，管理员权限可以再权限，分配给普通注册用户使用。<br><br>
 * 
 * 数据操作权限包括：<br>
 * 1. 建库/删库（CREATE SCHEMA 、 DROP SCHEMA）。<br>
 * 2. 建表/删表（CREATE TABLE 、DROP TABLE）。<br>
 * 3. 数据操纵（INSERT 、SELECT 、DELETE 、UPDATE 、CONDUCT 、ESTABLISH）<br>
 * 4. 数据资源检查（以“SCAN/ANALYSE”等为前缀关键字的各种命令）。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class Administrator extends SHAUser {

	private static final long serialVersionUID = -8278788709634940070L;

	/** 最多同时在线用户数目 **/
	private int members;

	/**
	 * 从传入的参数中解析管理员账号信息
	 * @param that Administrator实例
	 */
	private Administrator(Administrator that) {
		super(that);
		members = that.members;
	}
	
	/**
	 * 构造一个默认的管理员账号
	 */
	public Administrator() {
		super();
		members = 1;
	}
	
	/**
	 * 从可类化数据读取器中解析管理员账号信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Administrator(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出管理员账号参数
	 * @param reader 标记化读取器
	 */
	public Administrator(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 构造管理员账号，指定用户名和密码
	 * @param username 用户名
	 * @param password 密码
	 */
	public Administrator(String username, String password) {
		super(username, password);
	}
	
	/**
	 * 设置成员数目
	 * @param n 在线成员数目
	 */
	public void setMembers(int n) {
		if (n > 0) {
			members = n;
		}
	}

	/**
	 * 返回在线成员数目
	 * @return 在线成员数目
	 */
	public int getMembers() {
		return members;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %d", super.toString(), members);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#duplicate()
	 */
	@Override
	public Administrator duplicate() {
		return new Administrator(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeInt(members);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.diagram.SHAUser#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		members = reader.readInt();
	}
}