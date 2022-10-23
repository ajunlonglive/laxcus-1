/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 命令处理单元。<br>
 * 
 * 由服务器地址和命令两部分组成，是向目标地址发送命令。
 * 
 * @author scott.liang
 * @version 1.1 8/24/2015
 * @since laxcus 1.0
 */
public final class CommandItem implements Classable, Serializable, Cloneable, Comparable<CommandItem> {

	private static final long serialVersionUID = -2877912638987128394L;

	/** 服务器地址 **/
	private Node hub;

	/** 发送的命令 **/
	private Command command;

	/** 发送成功 **/
	private boolean completed;

	/**
	 * 构造默认和私有的命令处理单元
	 */
	private CommandItem() {
		super();
		completed = false;
	}

	/**
	 * 根据实例，生成命令处理单元的数据副本
	 * @param that
	 */
	private CommandItem(CommandItem that) {
		this();
		hub = that.hub;
		command = that.command;
		completed = that.completed;
	}

	/**
	 * 构造命令处理单元，指定参数
	 * @param hub 服务器地址
	 * @param command 命令
	 */
	public CommandItem(Node hub, Command command) {
		this();
		setHub(hub);
		setCommand(command);
	}

	/**
	 * 从可类化数据读取器中解析命令处理单元参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CommandItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置服务器地址
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回服务器地址
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}
	
	/**
	 * 返回服务器类型
	 * @return 节点类型
	 */
	public byte getHubFamily() {
		return hub.getFamily();
	}

	/**
	 * 设置命令
	 * @param e Command实例
	 */
	public void setCommand(Command e) {
		Laxkit.nullabled(e);

		command = e;
	}

	/**
	 * 返回命令
	 * @return Command实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 设置发送成功
	 * @param b 成功标记
	 */
	public void setCompleted(boolean b) {
		completed = b;
	}

	/**
	 * 判断发送成功
	 * @return 返回真或者假
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * 返回当前命令处理单元实例的浅层副本
	 * @return CommandItem副本
	 */
	public CommandItem duplicate() {
		return new CommandItem(this);
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CommandItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((CommandItem) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hub.hashCode() ^ command.hashCode();
	}

	/**
	 * 根据当前实例克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回命令处理单元的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", hub, command);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CommandItem that) {
		if(that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(hub, that.hub);
		if (ret == 0) {
			ret = Laxkit.compareTo(command, that.command);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(hub);
		writer.writeDefault(command);
		writer.writeBoolean(completed);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		hub = new Node(reader);
		command = (Command) reader.readDefault();
		completed = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}