/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.distribute.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 自定义参数存取接口。提供自定义参数查找。<BR>
 * 
 * @author scott.liang
 * @version 1.0 11/03/2012
 * @since laxcus 1.0
 */
public class AccessTask extends DistributedTask {

	/**
	 * 构造默认的自定义参数存取接口
	 */
	protected AccessTask() {
		super();
	}

	/**
	 * 根据标题名称，找到存取对象中指定序列中的参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 序列编号，从0开始
	 * @return 自定义参数
	 */
	public TaskParameter findParameter(AccessObject access, Naming title, int index) {
		return access.findParameter(title, index);
	}

	/**
	 * 根据标题名称，找到存取对象中第一个参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 自定义参数
	 */
	public TaskParameter findParameter(AccessObject access, Naming title) {
		return access.findParameter(title);
	}

	/**
	 * 根据标题名称，找到存取对象中指定序列中的参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 序列编号，从0开始
	 * @return 自定义参数
	 */
	public TaskParameter findParameter(AccessObject access, String title, int index) {
		return access.findParameter(title, index);
	}

	/**
	 * 根据标题名称，找到存取对象中第一个参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 自定义参数
	 */
	public TaskParameter findParameter(AccessObject access, String title) {
		return access.findParameter(title);
	}

	/**
	 * 从实例对象的参数集合中，找到第N个命令
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回命令
	 * @throws TaskException
	 */
	public Command findCommand(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isCommand()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskCommand) param).getValue();
	}

	/**
	 * 从实例对象的参数集合中，找到第1个命令
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回命令
	 * @throws TaskException
	 */
	public Command findCommand(AccessObject access, Naming title) throws TaskException {
		return findCommand(access, title, 0);
	}

	/**
	 * 从实例对象的参数集合中，找到第N个命令
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回命令
	 * @throws TaskException
	 */
	public Command findCommand(AccessObject access, String title, int index) throws TaskException {
		return findCommand(access, new Naming(title), index);
	}

	/**
	 * 从实例对象的参数集合中，找到第1个命令
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回命令
	 * @throws TaskException
	 */
	public Command findCommand(AccessObject access, String title) throws TaskException {
		return findCommand(access, title, 0);
	}

	/**
	 * 从实例对象的参数集合中，找到第N个可类化参数值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回可类化对象
	 * @throws TaskException
	 */
	public Classable findClassable(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isClassable()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskClassable) param).getValue();
	}

	/**
	 * 从实例对象的参数集合中，找到第1个可类化参数值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回可类化对象
	 * @throws TaskException
	 */
	public Classable findClassable(AccessObject access, Naming title) throws TaskException {
		return findClassable(access, title, 0);
	}

	/**
	 * 从实例对象的参数集合中，找到第N个可类化参数值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回可类化对象
	 * @throws TaskException
	 */
	public Classable findClassable(AccessObject access, String title, int index) throws TaskException {
		return findClassable(access, new Naming(title), index);
	}

	/**
	 * 从实例对象的参数集合中，找到第1个可类化参数值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回可类化对象
	 * @throws TaskException
	 */
	public Classable findClassable(AccessObject access, String title) throws TaskException {
		return findClassable(access, title, 0);
	}

	/**
	 * 从实例对象的参数集合中，找到第N个参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 参数下标
	 * @return 返回串行化对象
	 * @throws TaskException
	 */
	public Serializable findSerialable(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isSerializable()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskSerializable) param).getValue();
	}

	/**
	 * 从实例对象的参数集合中，找到第1个参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回串行化对象
	 * @throws TaskException
	 */
	public Serializable findSerialable(AccessObject access, Naming title) throws TaskException {
		return findSerialable(access, title, 0);
	}

	/**
	 * 从实例对象的参数集合中，找到第N个参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 参数下标
	 * @return 返回串行化对象
	 * @throws TaskException
	 */
	public Serializable findSerialable(AccessObject access, String title, int index) throws TaskException {
		return findSerialable(access, new Naming(title), index);
	}

	/**
	 * 从实例对象的参数集合中，找到第1个参数
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回串行化对象
	 * @throws TaskException
	 */
	public Serializable findSerialable(AccessObject access, String title) throws TaskException {
		return findSerialable(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的字节数组
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回字节数组
	 * @throws TaskException
	 */
	public byte[] findRaw(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isRaw()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskRaw) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个字节数组
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回字节数组
	 * @throws TaskException
	 */
	public byte[] findRaw(AccessObject access, Naming title) throws TaskException {
		return findRaw(access, title, 0);
	}
	
	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的字节数组
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回字节数组
	 * @throws TaskException
	 */
	public byte[] findRaw(AccessObject access, String title, int index) throws TaskException {
		return findRaw(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个字节数组
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回字节数组
	 * @throws TaskException
	 */
	public byte[] findRaw(AccessObject access, String title) throws TaskException {
		return findRaw(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的布尔值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回布尔值
	 * @throws TaskException
	 */
	public boolean findBoolean(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isBoolean()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskBoolean) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个布尔值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回布尔值
	 * @throws TaskException
	 */
	public boolean findBoolean(AccessObject access, Naming title) throws TaskException {
		return findBoolean(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的布尔值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回布尔值
	 * @throws TaskException
	 */
	public boolean findBoolean(AccessObject access, String title, int index) throws TaskException {
		return findBoolean(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个布尔值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回布尔值
	 * @throws TaskException
	 */
	public boolean findBoolean(AccessObject access, String title) throws TaskException {
		return findBoolean(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的字符串
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回字符串
	 * @throws TaskException
	 */
	public String findString(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isString()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskString) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个字符串
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回字符串
	 * @throws TaskException
	 */
	public String findString(AccessObject access, Naming title) throws TaskException {
		return findString(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的字符串
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回字符串
	 * @throws TaskException
	 */
	public String findString(AccessObject access, String title, int index) throws TaskException {
		return findString(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个字符串
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回字符串
	 * @throws TaskException
	 */
	public String findString(AccessObject access, String title) throws TaskException {
		return findString(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的短整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回短整型
	 * @throws TaskException
	 */
	public short findShort(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isShort()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskShort) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个短整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回短整型
	 * @throws TaskException
	 */
	public short findShort(AccessObject access, Naming title) throws TaskException {
		return findShort(access, title, 0);
	}
	
	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的短整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回短整型
	 * @throws TaskException
	 */
	public short findShort(AccessObject access, String title, int index) throws TaskException {
		return findShort(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个短整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回短整型
	 * @throws TaskException
	 */
	public short findShort(AccessObject access, String title) throws TaskException {
		return findShort(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 索引下标
	 * @return 返回整形值
	 * @throws TaskException
	 */
	public int findInteger(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = access.findParameter(title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isInteger()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskInteger) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回整形值
	 * @throws TaskException
	 */
	public int findInteger(AccessObject access, Naming title) throws TaskException {
		return findInteger(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 索引下标
	 * @return 返回整形值
	 * @throws TaskException
	 */
	public int findInteger(AccessObject access, String title, int index) throws TaskException {
		return findInteger(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回整形值
	 * @throws TaskException
	 */
	public int findInteger(AccessObject access, String title) throws TaskException {
		return findInteger(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的长整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回长整型
	 * @throws TaskException
	 */
	public long findLong(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isLong()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskLong) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个长整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回长整型
	 * @throws TaskException
	 */
	public long findLong(AccessObject access, Naming title) throws TaskException {
		return findLong(access, title, 0);
	}
	
	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的长整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回长整型
	 * @throws TaskException
	 */
	public long findLong(AccessObject access, String title, int index) throws TaskException {
		return findLong(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个长整型
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回长整型
	 * @throws TaskException
	 */
	public long findLong(AccessObject access, String title) throws TaskException {
		return findLong(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的单浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回单浮点
	 * @throws TaskException
	 */
	public float findFloat(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isFloat()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskFloat) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个单浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回单浮点
	 * @throws TaskException
	 */
	public float findFloat(AccessObject access, Naming title) throws TaskException {
		return findFloat(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的单浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回单浮点
	 * @throws TaskException
	 */
	public float findFloat(AccessObject access, String title, int index) throws TaskException {
		return findFloat(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个单浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回单浮点
	 * @throws TaskException
	 */
	public float findFloat(AccessObject access, String title) throws TaskException {
		return findFloat(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的双浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回双浮点
	 * @throws TaskException
	 */
	public double findDouble(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isDouble()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskDouble) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个双浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回双浮点
	 * @throws TaskException
	 */
	public double findDouble(AccessObject access, Naming title) throws TaskException {
		return findDouble(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的双浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回双浮点
	 * @throws TaskException
	 */
	public double findDouble(AccessObject access, String title, int index) throws TaskException {
		return findDouble(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个双浮点
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回双浮点
	 * @throws TaskException
	 */
	public double findDouble(AccessObject access, String title) throws TaskException {
		return findDouble(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的日期值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回日期值
	 * @throws TaskException
	 */
	public int findDate(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isDate()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskDate) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个日期值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回日期值
	 * @throws TaskException
	 */
	public int findDate(AccessObject access, Naming title) throws TaskException {
		return findDate(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的日期值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回日期值
	 * @throws TaskException
	 */
	public int findDate(AccessObject access, String title, int index) throws TaskException {
		return findDate(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个日期值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回日期值
	 * @throws TaskException
	 */
	public int findDate(AccessObject access, String title) throws TaskException {
		return findDate(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的时间值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回时间值
	 * @throws TaskException
	 */
	public int findTime(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isTime()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskTime) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个时间值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回时间值
	 * @throws TaskException
	 */
	public int findTime(AccessObject access, Naming title) throws TaskException {
		return findTime(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的时间值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回时间值
	 * @throws TaskException
	 */
	public int findTime(AccessObject access, String title, int index) throws TaskException {
		return findTime(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个时间值
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回时间值
	 * @throws TaskException
	 */
	public int findTime(AccessObject access, String title) throws TaskException {
		return findTime(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的时间戳
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回时间戳
	 * @throws TaskException
	 */
	public long findTimestamp(AccessObject access, Naming title, int index) throws TaskException {
		TaskParameter param = findParameter(access, title, index);
		if (param == null) {
			throw new TaskException("cannot be find %s", title);
		} else if (!param.isTimestamp()) {
			throw new TaskException("illegal task value:%d", param.getType());
		}
		return ((TaskTimestamp) param).getValue();
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个时间戳
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回时间戳
	 * @throws TaskException
	 */
	public long findTimestamp(AccessObject access, Naming title) throws TaskException {
		return findTimestamp(access, title, 0);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到下标匹配的时间戳
	 * @param access 存取对象
	 * @param title 参数名称
	 * @param index 对象下标，从0开始
	 * @return 返回时间戳
	 * @throws TaskException
	 */
	public long findTimestamp(AccessObject access, String title, int index) throws TaskException {
		return findTimestamp(access, new Naming(title), index);
	}

	/**
	 * 根据参数名称，从存取对象内部，找到第1个时间戳
	 * @param access 存取对象
	 * @param title 参数名称
	 * @return 返回时间戳
	 * @throws TaskException
	 */
	public long findTimestamp(AccessObject access, String title) throws TaskException {
		return findTimestamp(access, title, 0);
	}

	/**
	 * 从磁盘读文件
	 * @param file 文件名
	 * @return 如果文件不存在，返回0长度字节数组。否则读取全部数据返回。
	 */
	protected byte[] readFile(File file) throws TaskException {
		if (!(file.exists() && file.isFile())) {
			return new byte[0];
		}
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();
		} catch (IOException e) {
			throw new TaskException(e);
		}
		return b;
	}
}