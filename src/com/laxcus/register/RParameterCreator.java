/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;


import com.laxcus.util.*;

/**
 * 环境变量生成器 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/7/2009
 * @since laxcus 1.0
 */
public class RParameterCreator {

	/**
	 * 根据数据类型，生成一个对应的环境变量实例。如果全部不匹配，返回空指针(null)。
	 * @param type 环境变量数据类型
	 * @return RParameter子类实例
	 */
	public static RParameter createDefault(byte type) {
		switch (type) {
		case RParameterType.BOOLEAN:
			return new RBoolean();
		case RParameterType.RAW:
			return new RRaw();
		case RParameterType.STRING:
			return new RString();
		case RParameterType.SHORT:
			return new RShort();
		case RParameterType.INTEGER:
			return new RInteger();
		case RParameterType.LONG:
			return new RLong();
		case RParameterType.FLOAT:
			return new RFloat();
		case RParameterType.DOUBLE:
			return new RDouble();
		case RParameterType.DATE:
			return new RDate();
		case RParameterType.TIME:
			return new RTime();
		case RParameterType.TIMESTAMP:
			return new RTimestamp();
		case RParameterType.COMMAND:
			return new RCommand();
		case RParameterType.CLASSABLE:
			return new RClassable();
		case RParameterType.SERIALABLE:
			return new RSerializable();
		}

		throw new IllegalValueException("illegal type %d", type);
	}
	
//	public static RParameter createDefault(byte type, String name) {
//		switch (type) {
//		case RParameterType.BOOLEAN:
//			return new RBoolean();
//		case RParameterType.RAW:
//			return new RRaw();
//		case RParameterType.STRING:
//			return new RString();
//		case RParameterType.SHORT:
//			return new RShort();
//		case RParameterType.INTEGER:
//			return new RInteger();
//		case RParameterType.LONG:
//			return new RLong();
//		case RParameterType.FLOAT:
//			return new RFloat();
//		case RParameterType.DOUBLE:
//			return new RDouble();
//		case RParameterType.DATE:
//			return new RDate();
//		case RParameterType.TIME:
//			return new RTime();
//		case RParameterType.TIMESTAMP:
//			return new RTimestamp();
//		case RParameterType.COMMAND:
//			return new RCommand();
//		case RParameterType.CLASSABLE:
//			return new RClassable();
//		case RParameterType.SERIALABLE:
//			return new RSerializable();
//		}
//
//		throw new IllegalValueException("illegal type %d", type);
//	}

//	/**
//	 * 从可类化读取器中解析环境变量类型，返回一个环境变量实例
//	 * @param reader 可类化数据读取器
//	 * @return RParameter子类实例
//	 */
//	public static RParameter resolve(ClassReader reader) {
//		byte family = reader.current();
//		RParameter value = RParameterCreator.createDefault(family);
//		value.resolve(reader);
//		return value;
//	}
	

//	/**
//	 * 从可类化读取器中解析环境变量类型，返回一个环境变量实例
//	 * @param reader 可类化数据读取器
//	 * @return RParameter子类实例
//	 */
//	public static RParameter split(ClassReader reader) {
//		
//		byte[] b = reader.current(4);
//		int len = Laxkit.toInteger(b);
//		
//		// 全部数据
//		byte[] hi = reader.current(4 + len);
//		
//		ClassReader r = new ClassReader(hi);
//		 len = r.readInt();
//		 byte attribute = r.read();
//		 if( RTokenAttribute.isFolder(attribute) ) {
//			RFolder foler = new RFolder(reader); 
//		 } else {
//			 Naming n = new Naming(r);
//			 byte type = r.read();
//		 	RParameter param =	RParameterCreator.createDefault(type);
//		 	param.resolve(reader);
//		 
//		 	
//		 }
//		
//		// 跨过前面4个字节的长度，读取类型定义
//		byte family = reader.shift(4);
//		RParameter value = RParameterCreator.createDefault(family);
//		value.resolve(reader);
//		return value;
//	}

}