/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute;

import java.io.*;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 存取对象。<br>
 * 存取对象继承自DistributeObject，保存用户的自定义参数。<br><br>
 * 
 * 自定义参数格式: <br>
 * 自定义名称(数据类型)=数字型数值|'字符串数值'|'时间/日期'|布尔值(false|true), [下一个...] <br>
 * 
 * @author scott.liang
 * @version 1.1 11/15/2009
 * @since laxcus 1.0
 */
public abstract class AccessObject extends DistributedObject {

	private static final long serialVersionUID = 3682353624274512782L;

	/** 自定义参数集合(允许多个相同名称的参数存在) **/
	private ArrayList<TaskParameter> array = new ArrayList<TaskParameter>();

	/**
	 * 构造一个默认的存取对象
	 */
	protected AccessObject() {
		super();
	}

	/**
	 * 构造传入的存取对象，生成一个它的副本
	 * @param that AccessObject实例
	 */
	protected AccessObject(AccessObject that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 删除自定义参数
	 * @param e 自定义参数
	 * @return 成功返回真，否则假
	 */
	public boolean removeParameter(TaskParameter e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}

	/**
	 * 删除全部自定义参数
	 * @param e 自定义参数
	 * @return 返回删除单元数
	 */
	public int removeAllParameter(TaskParameter e) {
		int count = 0;
		do {
			// 删除一个，如果成功，继续删除，直到最后！
			boolean success = removeParameter(e);
			if (!success) {
				break;
			}
			count++;
		} while (true);
		return count;
	}

	/**
	 * 保存自定义参数
	 * @param e 自定义参数
	 * @return 成功返回真，否则假
	 */
	public boolean addParameter(TaskParameter e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批自定义参数
	 * @param all 自定义参数数组
	 * @return 返回新增的成员数目
	 */
	public int addParameters(Collection<TaskParameter> all) {
		int size = array.size();
		for (TaskParameter e : all) {
			addParameter(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部自定义参数
	 * @return  自定义参数列表
	 */
	public List<TaskParameter> getParameters() {
		return new ArrayList<TaskParameter>(array);
	}

	/**
	 * 返回下标位置的参数
	 * @param index 下标
	 * @return 返回TaskParameter子类实例
	 */
	public TaskParameter getParameter(int index) {
		if (index < 0 || index >= array.size()) {
			throw new IndexOutOfBoundsException();
		}
		return array.get(index);
	}

	/**
	 * 根据参数标题名称和参数下标位置，查找对应的参数。最小下标从0开始。
	 * @param title 参数标题名称
	 * @param index 指定参数的排列下标
	 * @return 返回TaskParameter子类实例
	 */
	public TaskParameter findParameter(Naming title, int index) {
		// 用户的错误
		if (index < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}

		int seek = 0;
		for (TaskParameter e : array) {
			if (Laxkit.compareTo(e.getName(), title) == 0) {
				if (seek == index) { // 达到指定位置
					return e;
				}
				seek++; // 指向下一个
			}
		}
		// 没有找到，返回空指针
		return null;
	}

	/**
	 * 根据参数标题名称和参数下标位置，查找对应的参数
	 * @param title 参数标题名称
	 * @param index 指定参数的排列下标
	 * @return 返回TaskParameter子类实例
	 */
	public TaskParameter findParameter(String title, int index) {
		return findParameter(new Naming(title), index);
	}

	/**
	 * 根据参数标题名称，找到第一个匹配参数
	 * @param title 参数标题名称
	 * @return 返回TaskParameter子类实例
	 */
	public TaskParameter findParameter(Naming title) {
		return findParameter(title, 0);
	}

	/**
	 * 根据参数标题名称，找到第一个匹配参数
	 * @param title 参数标题名称
	 * @return 返回TaskParameter子类实例
	 */
	public TaskParameter findParameter(String title) {
		return findParameter(title, 0);
	}

	/**
	 * 根据参数名称和指定下标，判断参数存在
	 * @param title 参数名称
	 * @param index 索引下标
	 * @return 返回真或者假
	 */
	public boolean hasParameter(Naming title, int index) {
		TaskParameter value = findParameter(title, index);
		return value != null;
	}

	/**
	 * 根据参数名称和指定下标，判断参数存在
	 * @param title 参数名称
	 * @param index 索引下标
	 * @return 返回真或者假
	 */
	public boolean hasParameter(String title, int index) {
		return hasParameter(new Naming(title), index);
	}

	/**
	 * 根据参数名称，判断参数存在
	 * @param title 参数名称
	 * @return 返回真或者假
	 */
	public boolean hasParameter(Naming title) {
		return hasParameter(title, 0);
	}

	/**
	 * 根据参数名称，判断参数存在
	 * @param title 参数名称
	 * @return 返回真或者假
	 */
	public boolean hasParameter(String title) {
		return hasParameter(new Naming(title));
	}

	/**
	 * 根据参数名称、下标、类定义，判断参数存在
	 * @param title 参数名称
	 * @param index 索引下标
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean hasParameter(Naming title, int index, Class<?> clazz) {
		TaskParameter value = findParameter(title, index);
		return (value != null && value.getClass() == clazz);
	}

	/**
	 * 根据参数名称、下标、参数类型，判断参数存在
	 * @param title 参数名称
	 * @param index 索引下标
	 * @param type 参数类型
	 * @return 返回真或者假
	 */
	public boolean hasParameter(Naming title, int index, byte type) {
		TaskParameter value = findParameter(title, index);
		return (value != null && value.getType() == type);
	}

	/**
	 * 根据参数名称、下标、类定义，判断参数存在
	 * @param title 参数名称
	 * @param index 索引下标
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean hasParameter(String title, int index, Class<?> clazz) {
		return hasParameter(new Naming(title), index, clazz);
	}

	/**
	 * 根据参数名称、下标、参数类型，判断参数存在
	 * @param title 参数名称
	 * @param index 索引下标
	 * @param type 参数类型
	 * @return 返回真或者假
	 */
	public boolean hasParameter(String title, int index, byte type) {
		return hasParameter(new Naming(title), index, type);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasString(Naming title, int index) {
		return hasParameter(title, index, TaskString.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasString(String title, int index) {
		return hasParameter(title, index, TaskString.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasString(Naming title) {
		return hasParameter(title, 0, TaskString.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasString(String title) {
		return hasParameter(title, 0, TaskString.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasShort(Naming title, int index) {
		return hasParameter(title, index, TaskShort.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasShort(String title, int index) {
		return hasParameter(title, index, TaskShort.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasShort(Naming title) {
		return hasParameter(title, 0, TaskShort.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasShort(String title) {
		return hasParameter(title, 0, TaskShort.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasInteger(Naming title, int index) {
		return hasParameter(title, index, TaskInteger.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasInteger(String title, int index) {
		return hasParameter(title, index, TaskInteger.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasInteger(Naming title) {
		return hasParameter(title, 0, TaskInteger.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasInteger(String title) {
		return hasParameter(title, 0, TaskInteger.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasLong(Naming title, int index) {
		return hasParameter(title, index, TaskLong.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasLong(String title, int index) {
		return hasParameter(title, index, TaskLong.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasLong(Naming title) {
		return hasParameter(title, 0, TaskLong.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasLong(String title) {
		return hasParameter(title, 0, TaskLong.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasFloat(Naming title, int index) {
		return hasParameter(title, index, TaskFloat.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasFloat(String title, int index) {
		return hasParameter(title, index, TaskFloat.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasFloat(Naming title) {
		return hasParameter(title, 0, TaskFloat.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasFloat(String title) {
		return hasParameter(title, 0, TaskFloat.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDouble(Naming title, int index) {
		return hasParameter(title, index, TaskDouble.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDouble(String title, int index) {
		return hasParameter(title, index, TaskDouble.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDouble(Naming title) {
		return hasParameter(title, 0, TaskDouble.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDouble(String title) {
		return hasParameter(title, 0, TaskDouble.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDate(Naming title, int index) {
		return hasParameter(title, index, TaskDate.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDate(String title, int index) {
		return hasParameter(title, index, TaskDate.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDate(Naming title) {
		return hasParameter(title, 0, TaskDate.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasDate(String title) {
		return hasParameter(title, 0, TaskDate.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTime(Naming title, int index) {
		return hasParameter(title, index, TaskTime.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTime(String title, int index) {
		return hasParameter(title, index, TaskTime.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTime(Naming title) {
		return hasParameter(title, 0, TaskTime.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTime(String title) {
		return hasParameter(title, 0, TaskTime.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTimestamp(Naming title, int index) {
		return hasParameter(title, index, TaskTimestamp.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTimestamp(String title, int index) {
		return hasParameter(title, index, TaskTimestamp.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTimestamp(Naming title) {
		return hasParameter(title, 0, TaskTimestamp.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasTimestamp(String title) {
		return hasParameter(title, 0, TaskTimestamp.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasBoolean(Naming title, int index) {
		return hasParameter(title, index, TaskBoolean.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasBoolean(String title, int index) {
		return hasParameter(title, index, TaskBoolean.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasBoolean(Naming title) {
		return hasParameter(title, 0, TaskBoolean.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasBoolean(String title) {
		return hasParameter(title, 0, TaskBoolean.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasCommand(Naming title, int index) {
		return hasParameter(title, index, TaskCommand.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasCommand(String title, int index) {
		return hasParameter(title, index, TaskCommand.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasCommand(Naming title) {
		return hasParameter(title, 0, TaskCommand.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasCommand(String title) {
		return hasParameter(title, 0, TaskCommand.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasRaw(Naming title, int index) {
		return hasParameter(title, index, TaskRaw.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasRaw(String title, int index) {
		return hasParameter(title, index, TaskRaw.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasRaw(Naming title) {
		return hasParameter(title, 0, TaskRaw.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasRaw(String title) {
		return hasParameter(title, 0, TaskRaw.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasSerializable(Naming title, int index) {
		return hasParameter(title, index, TaskSerializable.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasSerializable(String title, int index) {
		return hasParameter(title, index, TaskSerializable.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasSerializable(Naming title) {
		return hasParameter(title, 0, TaskSerializable.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasSerializable(String title) {
		return hasParameter(title, 0, TaskSerializable.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasClassable(Naming title, int index) {
		return hasParameter(title, index, TaskClassable.class);
	}

	/**
	 * 判断指定名称、下标的参数存在
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasClassable(String title, int index) {
		return hasParameter(title, index, TaskClassable.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasClassable(Naming title) {
		return hasParameter(title, 0, TaskClassable.class);
	}

	/**
	 * 判断指定名称、0下标的参数存在
	 * @param title 参数名称
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean hasClassable(String title) {
		return hasParameter(title, 0, TaskClassable.class);
	}


	/**
	 * 内存空间占用收缩到成员数
	 */
	public void trimParameters() {
		array.trimToSize();
	}

	/**
	 * 保存命令
	 * @param title 参数名称 
	 * @param cmd 命令实例
	 */
	public void addCommand(String title, Command cmd) {
		TaskCommand param = new TaskCommand(title, cmd);
		addParameter(param);
	}

	/**
	 * 保存命令
	 * @param title 参数名称 
	 * @param cmd 命令实例
	 */
	public void addCommand(Naming title, Command cmd) {
		TaskCommand param = new TaskCommand(title, cmd);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的命令
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的命令
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Command findCommand(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是命令类型，弹出错误
		if (param == null || !param.isCommand()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回命令
		return ((TaskCommand) param).getValue();
	}

	/**
	 * 查找指定名称和下标的命令
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的命令，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Command findCommand(String title, int index) {
		return findCommand(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的命令
	 * @param title 参数名称
	 * @return 返回指定的命令，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Command findCommand(String title) {
		return findCommand(title, 0);
	}

	/**
	 * 保存可类化对象
	 * @param title 参数名称 
	 * @param that 可类化对象实例
	 */
	public void addClassable(String title, Classable that) {
		TaskClassable param = new TaskClassable(title, that);
		addParameter(param);
	}

	/**
	 * 保存可类化对象
	 * @param title 参数名称 
	 * @param that 可类化对象实例
	 */
	public void addClassable(Naming title, Classable that) {
		TaskClassable param = new TaskClassable(title, that);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的可类化对象
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的可类化对象
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Classable findClassable(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是可类化对象类型，弹出错误
		if (param == null || !param.isClassable()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回可类化对象
		return ((TaskClassable) param).getValue();
	}

	/**
	 * 查找指定名称和下标的可类化对象
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的可类化对象，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Classable findClassable(String title, int index) {
		return findClassable(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的可类化对象
	 * @param title 参数名称
	 * @return 返回指定的可类化对象，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Classable findClassable(String title) {
		return findClassable(title, 0);
	}

	/**
	 * 保存串行化对象
	 * @param title 参数名称 
	 * @param that 串行化对象实例
	 */
	public void addSerialable(String title, Serializable that) {
		TaskSerializable param = new TaskSerializable(title, that);
		addParameter(param);
	}

	/**
	 * 保存串行化对象
	 * @param title 参数名称 
	 * @param that 串行化对象实例
	 */
	public void addSerialable(Naming title, Serializable that) {
		TaskSerializable param = new TaskSerializable(title, that);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的串行化对象
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的串行化对象
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Serializable findSerializable(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是串行化对象类型，弹出错误
		if (param == null || !param.isSerializable()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回串行化对象
		return ((TaskSerializable) param).getValue();
	}

	/**
	 * 查找指定名称和下标的串行化对象
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的串行化对象，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Serializable findSerializable(String title, int index) {
		return findSerializable(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的串行化对象
	 * @param title 参数名称
	 * @return 返回指定的串行化对象，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public Serializable findSerializable(String title) {
		return findSerializable(title, 0);
	}

	/**
	 * 保存布尔值
	 * @param title 参数名称 
	 * @param value 布尔值
	 */
	public void addBoolean(String title, boolean value) {
		TaskBoolean param = new TaskBoolean(title, value);
		addParameter(param);
	}

	/**
	 * 保存布尔值
	 * @param title 参数名称 
	 * @param value 布尔值
	 */
	public void addBoolean(Naming title, boolean value) {
		TaskBoolean param = new TaskBoolean(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的布尔值
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的布尔值
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean findBoolean(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是布尔值类型，弹出错误
		if (param == null || !param.isBoolean()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回布尔值
		return ((TaskBoolean) param).getValue();
	}

	/**
	 * 查找指定名称和下标的布尔值
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的布尔值，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean findBoolean(String title, int index) {
		return findBoolean(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的布尔值
	 * @param title 参数名称
	 * @return 返回指定的布尔值，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public boolean findBoolean(String title) {
		return findBoolean(title, 0);
	}

	/**
	 * 保存字节数组
	 * @param title 参数名称 
	 * @param value 字节数组
	 */
	public void addRaw(String title, byte[] value) {
		TaskRaw param = new TaskRaw(title, value);
		addParameter(param);
	}

	/**
	 * 保存字节数组
	 * @param title 参数名称 
	 * @param value 字节数组
	 */
	public void addRaw(Naming title, byte[] value) {
		TaskRaw param = new TaskRaw(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的字节数组
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的字节数组
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public byte[] findRaw(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是字节数组类型，弹出错误
		if (param == null || !param.isRaw()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回字节数组
		return ((TaskRaw) param).getValue();
	}

	/**
	 * 查找指定名称和下标的字节数组
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的字节数组，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public byte[] findRaw(String title, int index) {
		return findRaw(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的字节数组
	 * @param title 参数名称
	 * @return 返回指定的字节数组，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public byte[] findRaw(String title) {
		return findRaw(title, 0);
	}

	/**
	 * 保存字符串
	 * @param title 参数名称 
	 * @param str 字符串实例
	 */
	public void addString(String title, String str) {
		TaskString param = new TaskString(title, str);
		addParameter(param);
	}

	/**
	 * 保存字符串
	 * @param title 参数名称 
	 * @param str 字符串实例
	 */
	public void addString(Naming title, String str) {
		TaskString param = new TaskString(title, str);
		addParameter(param);
	}

	/**
	 * 保存标记，每个标记都是唯一的！忽略大小写
	 * 
	 * @param title 标题名称
	 * @param str 字符串
	 */
	public void setSign(String key) {
		Laxkit.nullabled(key);

		TaskString param = new TaskString(key, key);
		// 删除全部旧参数
		removeAllParameter(param);
		// 保存
		addParameter(param);
	}

	/**
	 * 判断有匹配的标记，忽略大小写。
	 * @param key 标记值
	 * @return 存在返回真，否则假
	 */
	public boolean hasSign(String key) {
		Laxkit.nullabled(key);

		TaskParameter param = findParameter(key, 0);
		// 如果没有找到，返回假
		if (param == null || !param.isString()) {
			return false;
		}
		// 返回字符串
		TaskString sub = ((TaskString) param);
		// 判断KEY/VALUE都匹配！
		return sub.getNameText().equalsIgnoreCase(key)
		&& sub.getValue().equalsIgnoreCase(key);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的字符串
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的字符串，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public String findString(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是字符串类型，弹出错误
		if (param == null || !param.isString()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回字符串
		return ((TaskString) param).getValue();
	}

	/**
	 * 查找指定名称和下标的字符串
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的字符串，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public String findString(String title, int index) {
		return findString(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的字符串
	 * @param title 参数名称
	 * @return 返回指定的字符串，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public String findString(String title) {
		return findString(title, 0);
	}



	/**
	 * 保存短整形
	 * @param title 参数名称 
	 * @param value 短整形
	 */
	public void addShort(String title, short value) {
		TaskShort param = new TaskShort(title, value);
		addParameter(param);
	}

	/**
	 * 保存短整形
	 * @param title 参数名称 
	 * @param value 短整形
	 */
	public void addShort(Naming title, short value) {
		TaskShort param = new TaskShort(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的短整形
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的短整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public short findShort(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是短整形类型，弹出错误
		if (param == null || !param.isShort()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回短整形
		return ((TaskShort) param).getValue();
	}

	/**
	 * 查找指定名称和下标的短整形
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的短整形，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public short findShort(String title, int index) {
		return findShort(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的短整形
	 * @param title 参数名称
	 * @return 返回指定的短整形，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public short findShort(String title) {
		return findShort(title, 0);
	}

	/**
	 * 保存整形
	 * @param title 参数名称 
	 * @param value 整形
	 */
	public void addInteger(String title, int value) {
		TaskInteger param = new TaskInteger(title, value);
		addParameter(param);
	}

	/**
	 * 保存整形
	 * @param title 参数名称 
	 * @param value 整形
	 */
	public void addInteger(Naming title, int value) {
		TaskInteger param = new TaskInteger(title, value);
		addParameter(param);
	}


	/**
	 * 从参数队列中，找到指定名称和所在下标的整形
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findInteger(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是整形类型，弹出错误
		if (param == null || !param.isInteger()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回整形
		return ((TaskInteger) param).getValue();
	}

	/**
	 * 查找指定名称和下标的整形
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的整形，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findInteger(String title, int index) {
		return findInteger(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的整形
	 * @param title 参数名称
	 * @return 返回指定的整形，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findInteger(String title) {
		return findInteger(title, 0);
	}

	/**
	 * 保存长整型
	 * @param title 参数名称 
	 * @param value 长整型
	 */
	public void addLong(String title, long value) {
		TaskLong param = new TaskLong(title, value);
		addParameter(param);
	}

	/**
	 * 保存长整型
	 * @param title 参数名称 
	 * @param value 长整型
	 */
	public void addLong(Naming title, long value) {
		TaskLong param = new TaskLong(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的长整型
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的长整型
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public long findLong(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是长整型类型，弹出错误
		if (param == null || !param.isLong()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回长整型
		return ((TaskLong) param).getValue();
	}

	/**
	 * 查找指定名称和下标的长整型
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的长整型，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public long findLong(String title, int index) {
		return findLong(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的长整型
	 * @param title 参数名称
	 * @return 返回指定的长整型，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public long findLong(String title) {
		return findLong(title, 0);
	}

	/**
	 * 保存单浮点
	 * @param title 参数名称 
	 * @param value 单浮点
	 */
	public void addFloat(String title, float value) {
		TaskFloat param = new TaskFloat(title, value);
		addParameter(param);
	}

	/**
	 * 保存单浮点
	 * @param title 参数名称 
	 * @param value 单浮点
	 */
	public void addFloat(Naming title, float value) {
		TaskFloat param = new TaskFloat(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的单浮点
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的单浮点
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public float findFloat(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是单浮点类型，弹出错误
		if (param == null || !param.isFloat()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回单浮点
		return ((TaskFloat) param).getValue();
	}

	/**
	 * 查找指定名称和下标的单浮点
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的单浮点，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public float findFloat(String title, int index) {
		return findFloat(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的单浮点
	 * @param title 参数名称
	 * @return 返回指定的单浮点，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public float findFloat(String title) {
		return findFloat(title, 0);
	}

	/**
	 * 保存双浮点
	 * @param title 参数名称 
	 * @param value 双浮点
	 */
	public void addDouble(String title, double value) {
		TaskDouble param = new TaskDouble(title, value);
		addParameter(param);
	}

	/**
	 * 保存双浮点
	 * @param title 参数名称 
	 * @param value 双浮点
	 */
	public void addDouble(Naming title, double value) {
		TaskDouble param = new TaskDouble(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的双浮点
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的双浮点
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public double findDouble(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是双浮点类型，弹出错误
		if (param == null || !param.isDouble()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回双浮点
		return ((TaskDouble) param).getValue();
	}

	/**
	 * 查找指定名称和下标的双浮点
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的双浮点，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public double findDouble(String title, int index) {
		return findDouble(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的双浮点
	 * @param title 参数名称
	 * @return 返回指定的双浮点，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public double findDouble(String title) {
		return findDouble(title, 0);
	}

	/**
	 * 保存日期
	 * @param title 参数名称 
	 * @param value 日期
	 */
	public void addDate(String title, int value) {
		TaskDate param = new TaskDate(title, value);
		addParameter(param);
	}

	/**
	 * 保存日期
	 * @param title 参数名称 
	 * @param value 日期
	 */
	public void addDate(Naming title, int value) {
		TaskDate param = new TaskDate(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的日期
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的日期
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findDate(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是日期类型，弹出错误
		if (param == null || !param.isDate()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回日期
		return ((TaskDate) param).getValue();
	}

	/**
	 * 查找指定名称和下标的日期
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的日期，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findDate(String title, int index) {
		return findDate(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的日期
	 * @param title 参数名称
	 * @return 返回指定的日期，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findDate(String title) {
		return findDate(title, 0);
	}

	/**
	 * 保存时间
	 * @param title 参数名称 
	 * @param value 时间
	 */
	public void addTime(String title, int value) {
		TaskTime param = new TaskTime(title, value);
		addParameter(param);
	}

	/**
	 * 保存时间
	 * @param title 参数名称 
	 * @param value 时间
	 */
	public void addTime(Naming title, int value) {
		TaskTime param = new TaskTime(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的时间
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的时间
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findTime(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是时间类型，弹出错误
		if (param == null || !param.isTime()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回时间
		return ((TaskTime) param).getValue();
	}

	/**
	 * 查找指定名称和下标的时间
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的时间，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findTime(String title, int index) {
		return findTime(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的时间
	 * @param title 参数名称
	 * @return 返回指定的时间，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public int findTime(String title) {
		return findTime(title, 0);
	}

	/**
	 * 保存时间戳
	 * @param title 参数名称 
	 * @param value 时间戳
	 */
	public void addTimestamp(String title, long value) {
		TaskTimestamp param = new TaskTimestamp(title, value);
		addParameter(param);
	}

	/**
	 * 保存时间戳
	 * @param title 参数名称 
	 * @param value 时间戳
	 */
	public void addTimestamp(Naming title, long value) {
		TaskTimestamp param = new TaskTimestamp(title, value);
		addParameter(param);
	}

	/**
	 * 从参数队列中，找到指定名称和所在下标的时间戳
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的时间戳
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public long findTimestamp(Naming title, int index) {
		TaskParameter param = findParameter(title, index);
		// 如果没有找到或者不是时间戳类型，弹出错误
		if (param == null || !param.isTimestamp()) {
			throw new IllegalValueException("cannot be find \"%s\"", title);
		}
		// 返回时间戳
		return ((TaskTimestamp) param).getValue();
	}

	/**
	 * 查找指定名称和下标的时间戳
	 * @param title 参数名称
	 * @param index 同名参数所在下标
	 * @return 返回指定的时间戳，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public long findTimestamp(String title, int index) {
		return findTimestamp(new Naming(title), index);
	}

	/**
	 * 查找指定名称和0下标的时间戳
	 * @param title 参数名称
	 * @return 返回指定的时间戳，如果没有是空指针。
	 * @throws 如果没有找到或者类型不匹配，弹出IllegalValueException异常。
	 */
	public long findTimestamp(String title) {
		return findTimestamp(title, 0);
	}

	/**
	 * 将自定义参数值写入可类化数据存储器
	 * @see com.laxcus.distribute.DistributedObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 保存当前全部参数
		writer.writeInt(array.size());
		for (TaskParameter value : array) {
			writer.writeObject(value); // 写入对象，不带类名信息
		}
	}

	/**
	 * 从可类化数据读取器中解析自定义参数值
	 * @see com.laxcus.distribute.DistributedObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析当前参数
		int size = reader.readInt();
		// 逐一解析参数
		for (int index = 0; index < size; index++) {	
			// 读取参数
			TaskParameter value = TaskParameterCreator.split(reader);
			// 保存它
			array.add(value);
		}
	}

	//	/**
	//	 * 从可类化数据读取器中解析自定义参数值
	//	 * @see com.laxcus.distribute.DistributeObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	//	 */
	//	@Override
	//	protected void resolveSuffix(ClassReader reader) {
	//		// 解析当前参数
	//		int size = reader.readInt();
	//		// 逐一解析参数
	//		for (int index = 0; index < size; index++) {
	////			// 跨过前面4个字节（参数字节数组的长度），取数据类型，不要移动指针
	////			byte family = reader.shift(4);
	////			// 根据数据类型生成一个参数值
	////			TaskParameter value = TaskParameterCreator.createDefault(family);
	//			
	//			
	//			TaskParameter value = TaskParameterCreator.split(reader);
	//			if (value == null) {
	//				throw new ClassableException("illegal family %d", family);
	//			}
	//			// 解析参数
	//			value.resolve(reader);
	//			// 保存它
	//			array.add(value);
	//		}
	//	}

}