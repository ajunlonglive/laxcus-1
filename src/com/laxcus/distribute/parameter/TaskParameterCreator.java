/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 自定义参数生成器 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/7/2009
 * @since laxcus 1.0
 */
public class TaskParameterCreator {

	/**
	 * 根据数据类型，生成一个对应的自定义参数实例。如果全部不匹配，返回空指针(null)。
	 * @param type 自定义参数数据类型
	 * @return TaskParameter子类实例
	 */
	public static TaskParameter createDefault(byte type) {
		switch (type) {
		case TaskParameterType.BOOLEAN:
			return new TaskBoolean();
		case TaskParameterType.RAW:
			return new TaskRaw();
		case TaskParameterType.STRING:
			return new TaskString();
		case TaskParameterType.SHORT:
			return new TaskShort();
		case TaskParameterType.INTEGER:
			return new TaskInteger();
		case TaskParameterType.LONG:
			return new TaskLong();
		case TaskParameterType.FLOAT:
			return new TaskFloat();
		case TaskParameterType.DOUBLE:
			return new TaskDouble();
		case TaskParameterType.DATE:
			return new TaskDate();
		case TaskParameterType.TIME:
			return new TaskTime();
		case TaskParameterType.TIMESTAMP:
			return new TaskTimestamp();
		case TaskParameterType.COMMAND:
			return new TaskCommand();
		case TaskParameterType.CLASSABLE:
			return new TaskClassable();
		case TaskParameterType.SERIALABLE:
			return new TaskSerializable();
		}

		throw new IllegalValueException("illegal type %d", type);
	}

//	/**
//	 * 从可类化读取器中解析自定义参数类型，返回一个自定义参数实例
//	 * @param reader 可类化数据读取器
//	 * @return TaskParameter子类实例
//	 */
//	public static TaskParameter resolve(ClassReader reader) {
//		byte family = reader.current();
//		TaskParameter value = TaskParameterCreator.createDefault(family);
//		value.resolve(reader);
//		return value;
//	}
	

	/**
	 * 从可类化读取器中解析自定义参数类型，返回一个自定义参数实例
	 * @param reader 可类化数据读取器
	 * @return TaskParameter子类实例
	 */
	public static TaskParameter split(ClassReader reader) {
		// 跨过前面4个字节的长度，读取类型定义
		byte family = reader.shift(4);
		TaskParameter value = TaskParameterCreator.createDefault(family);
		value.resolve(reader);
		return value;
	}

}