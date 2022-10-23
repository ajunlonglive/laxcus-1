/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.parameter;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 引导参数生成器 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/7/2009
 * @since laxcus 1.0
 */
public class InputParameterCreator {

	/**
	 * 根据数据类型，生成一个对应的引导参数实例。如果全部不匹配，返回空指针(null)。
	 * @param type 引导参数数据类型
	 * @return BootParameter子类实例
	 */
	public static InputParameter createDefault(byte type) {
		switch (type) {
		case InputParameterType.BOOLEAN:
			return new InputBoolean();
		case InputParameterType.STRING:
			return new InputString();
		case InputParameterType.SHORT:
			return new InputShort();
		case InputParameterType.INTEGER:
			return new InputInteger();
		case InputParameterType.LONG:
			return new InputLong();
		case InputParameterType.FLOAT:
			return new InputFloat();
		case InputParameterType.DOUBLE:
			return new InputDouble();
		case InputParameterType.DATE:
			return new InputDate();
		case InputParameterType.TIME:
			return new InputTime();
		case InputParameterType.TIMESTAMP:
			return new InputTimestamp();
		}

		throw new IllegalValueException("illegal type %d", type);
	}

	/**
	 * 从可类化读取器中解析引导参数类型，返回一个引导参数实例
	 * @param reader 可类化数据读取器
	 * @return BootParameter子类实例
	 */
	public static InputParameter resolve(ClassReader reader) {
		byte family = reader.current();
		InputParameter value = InputParameterCreator.createDefault(family);
		value.resolve(reader);
		return value;
	}

}