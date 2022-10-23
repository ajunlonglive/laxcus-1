/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 批量处理命令 <br>
 * 批量处理命令包含多个子命令。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class BatchCommand extends Command {

	private static final long serialVersionUID = 1190752355629800940L;

	/** 命令数组 **/
	private ArrayList<Command> array = new ArrayList<Command>();

	/**
	 * 构造默认的批量处理命令
	 */
	public BatchCommand() {
		super();
	}

	/**
	 * 使用传入实例，生成批量处理命令的数据副本
	 * @param that BatchCommand对象
	 */
	private BatchCommand(BatchCommand that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 构造批量处理命令，保存全部命令
	 * @param array 命令数组
	 */
	public BatchCommand(List<? extends Command> array) {
		this();
		addAll(array);
	}

	/**
	 * 构造批量处理命令，保存全部命令
	 * @param array 命令数组
	 */
	public BatchCommand(Command[] array) {
		this();
		addAll(array);
	}

	/**
	 * 从可类化数据读取器中解析批量处理命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public BatchCommand(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个命令
	 * @param e 命令实例
	 * @return 保存成功返回真，否则假
	 * @throws NullPointerException，如果命令实例是空指针，弹出异常
	 */
	public boolean add(Command e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除一个命令
	 * @param e 命令实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Command e) {
		Laxkit.nullabled(e);

		return array.remove(e);
	}

	/**
	 * 保存一批命令
	 * @param a 命令子类列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<? extends Command> a) {
		int size = array.size();
		if (a != null) {
			for (Command e : a) {
				add(e);
			}
		}
		return array.size() - size;
	}

	/**
	 * 保存一批命令
	 * @param a 命令数组
	 * @return 返回新增命令数目
	 */
	public int addAll(Command[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部命令
	 * @return 返回命令列表 
	 */
	public List<Command> list() {
		return new ArrayList<Command>(array);
	}

	/**
	 * 输出全部命令
	 * @return 返回命令数组
	 */
	public Command[] toArray() {
		Command[] cmds = new Command[array.size()];
		return array.toArray(cmds);
	}

	/**
	 * 清除全部命令
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 统计命令成员数目
	 * @return 返回命令数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setMemory(boolean)
	 */
	@Override
	public void setMemory(boolean b) {
		super.setMemory(b);
		for(Command cmd : array){
			cmd.setMemory(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setDisk(boolean)
	 */
	@Override
	public void setDisk(boolean b) {
		super.setDisk(b);
		for (Command cmd : array) {
			cmd.setDisk(b);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BatchCommand duplicate() {
		return new BatchCommand(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for(Command cmd : array) {
			writer.writeDefault(cmd);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Command cmd = (Command) reader.readDefault();
			array.add(cmd);
		}
	}

}