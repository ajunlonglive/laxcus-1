/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 诊断用户消耗的资源
 * 
 * @author scott.liang
 * @version 1.0 10/11/2022
 * @since laxcus 1.0
 */
public class CheckUserCost extends Command {

	private static final long serialVersionUID = 7448735013509223442L;

	/** 节点类型 **/
	private ArrayList<java.lang.Byte> types = new ArrayList<java.lang.Byte>();

	/** 被查询的用户签名 **/
	private ArrayList<Siger> users = new ArrayList<Siger>();

	/** 用户明文。不做可类化处理，不在网络间传输 **/
	protected TreeSet<Naming> texts = new TreeSet<Naming>();

	/** 查询的命名 **/
	private TreeSet<Naming> commands = new TreeSet<Naming>();

	/** 开始/结束时间 **/
	private long beginTime, endTime;

	/**
	 * 构造诊断用户消耗的资源
	 */
	public CheckUserCost() {
		super();
	}

	/**
	 * 生成诊断用户消耗的资源副本
	 * @param that 诊断用户消耗的资源
	 */
	private CheckUserCost(CheckUserCost that) {
		super(that);
		types.addAll( that.types);
		users.addAll(that.users);
		commands.addAll(that.commands);
		beginTime = that.beginTime;
		endTime = that.endTime;
	}

	/**
	 * 从可类化读取器中解析诊断用户消耗的资源
	 * @param reader 可类化读取器
	 */
	public CheckUserCost(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加类型
	 * @param who
	 */
	public void addType(byte who) {
		if (!types.contains(who)) {
			types.add(who);
		}
	}

	/**
	 * 返回类型
	 * @return
	 */
	public List<java.lang.Byte> getTypes() {
		return new ArrayList<Byte>(types);
	}
	
	/**
	 * 返回类型
	 * @return
	 */
	public byte[] getTypeBytes() {
		int size = types.size();
		byte[] sites = new byte[size];
		for (int i = 0; i < size; i++) {
			sites[i] = types.get(i);
		}
		return sites;
	}
	
	/**
	 * 判断某个类型的节点存在
	 * @param who 节点类型
	 * @return 存在返回真，否则假
	 */
	public boolean hasType(byte who) {
		return types.contains(who);
	}

	/**
	 * 增加用户名称
	 * @param siger
	 * @return 成功返回真，否则假
	 */
	public boolean addUser(Siger siger) {
		if (!users.contains(siger)) {
			return users.add(siger);
		}
		return false;
	}

	/**
	 * 判断用户账号存在
	 * @param siger
	 * @return
	 */
	public boolean hasUser(Siger siger) {
		return users.contains(siger);
	}

	/**
	 * 返回用户名称
	 * @return
	 */
	public List<Siger> getUsers() {
		return new ArrayList<Siger>(users);
	}
	
	/**
	 * 保存一个用户明文
	 * @param e 字符串
	 * @return 保存成功返回真，否则假
	 */
	public boolean addText(String e) {
		Laxkit.nullabled(e);
		
		return texts.add(new Naming(e));
	}

	/**
	 * 根据SHA256签名，查找匹配的明文
	 * @param hash 用户签名
	 * @return 返回用户明文，没有返回SHA256签名
	 */
	public String findText(Siger hash) {
		for (Naming e : texts) {
			String name = e.toString();
			// 判断是SHA256码，或者明文
			if (Siger.validate(name)) {
				if (Laxkit.compareTo(name, hash.getHex(), false) == 0) {
					return name;
				}
			} else {
				Siger that = SHAUser.doUsername(name);
				if (that.compareTo(hash) == 0) {
					return name;
				}
			}
		}
		return hash.toString();
	}

	/**
	 * 增加命令
	 * @param text
	 * @return
	 */
	public boolean addCommand(String text) {
		Naming naming = new Naming(text);
		return commands.add(naming);
	}

//	/**
//	 * 返回命令
//	 * @return
//	 */
//	public List<String> getCommands() {
//		return new ArrayList<String>(commands);
//	}
	
	/**
	 * 判断命令存在
	 * @param text 文本
	 * @return 返回真或者假
	 */
	public boolean hasCommand(String text) {
		Naming naming = new Naming(text);
		return commands.contains(naming);
	}
	
	/**
	 * 返回命令数目统计
	 * @return 整数
	 */
	public int getCommandCount() {
		return commands.size();
	}
	
	/**
	 * 设置开始时间
	 * @param d
	 */
	public void setBeginTime(long d) {
		beginTime = d;
	}
	
	/**
	 * 返回开始时间
	 * @return
	 */
	public long getBeginTime() {
		return beginTime ;
	}
	
	/**
	 * 设置结束时间
	 * @param d
	 */
	public void setEndTime(long d) {
		endTime = d;
	}
	
	/**
	 * 返回结束时间
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckUserCost duplicate() {
		return new CheckUserCost(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 节点类型
		int size = types.size();
		writer.writeInt(size);
		for (byte who : types) {
			writer.write(who);
		}
		// 用户
		size = users.size();
		writer.writeInt(size);
		for (Siger siger : users) {
			writer.writeObject(siger);
		}
		// 命令
		size = commands.size();
		writer.writeInt(size);
		for (Naming cmd : commands) {
			writer.writeObject(cmd);
		}
		// 时间
		writer.writeLong(beginTime);
		writer.writeLong(endTime);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 节点类型
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			byte who = reader.read();
			types.add(who);
		}
		// 用户签名
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger signer = new Siger(reader);
			users.add(signer);
		}
		// 命令
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Naming cmd = new Naming(reader);
			commands.add(cmd);
		}
		// 时间
		beginTime = reader.readLong();
		endTime = reader.readLong();
	}

}