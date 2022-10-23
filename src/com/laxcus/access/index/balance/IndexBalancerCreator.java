/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;

/**
 * 列索引分割生成器
 * 
 * @author scott.liang
 * @version 1.2 12/06/2012
 * @since laxcus 1.0
 */
public class IndexBalancerCreator {

	/**
	 * 构造列索引分割生成器
	 */
	public IndexBalancerCreator() {
		super();
	}

	/**
	 * 根据列数据类型，根据对应的列索引范围分割器
	 * @param family 数据类型，见com.laxcus.access.ColumnType中的定义
	 * @return 返回IndexBalancer子类实例，没有匹配返回空指针
	 */
	public static IndexBalancer create(byte family) {
		switch (family) {
		// 字节数组
		case ColumnType.RAW:
			return new RawBalancer();
		// 媒体类型
		case ColumnType.DOCUMENT:
			return new DocumentBalancer();
		case ColumnType.IMAGE:
			return new ImageBalancer();
		case ColumnType.AUDIO:
			return new AudioBalancer();
		case ColumnType.VIDEO:
			return new VideoBalancer();
		// 字符串
		case ColumnType.CHAR:
			return new CharBalancer();
		case ColumnType.WCHAR:
			return new WCharBalancer();
		case ColumnType.HCHAR:
			return new HCharBalancer();
		// 数值
		case ColumnType.SHORT:
			return new ShortBalancer();
		case ColumnType.INTEGER:
			return new IntegerBalancer();
		case ColumnType.LONG:
			return new LongBalancer();
		case ColumnType.FLOAT:
			return new FloatBalancer();
		case ColumnType.DOUBLE:
			return new DoubleBalancer();
		// 日期/时间
		case ColumnType.DATE:
			return new DateBalancer();
		case ColumnType.TIME:
			return new TimeBalancer();
		case ColumnType.TIMESTAMP:
			return new TimestampBalancer();
		}
		return null;
	}

	/**
	 * 根据列属性，建立对应的列索引范围分割器 
	 * @param attribute 列属性
	 * @return 返回IndexBalancer子类实例，没有匹配的数据类型返回空指针
	 */
	public static IndexBalancer create(ColumnAttribute attribute) {
		IndexBalancer balancer = null;

		// 判断是可变长数组类型
		if (attribute.isVariable()) {
			// 字节数组
			if (attribute.isRaw()) {
				balancer = new RawBalancer();
			}
			// 媒体类型
			else if (attribute.isDocument()) {
				balancer = new DocumentBalancer();
			} else if (attribute.isImage()) {
				balancer = new ImageBalancer();
			} else if (attribute.isAudio()) {
				balancer = new AudioBalancer();
			} else if (attribute.isVideo()) {
				balancer = new VideoBalancer();
			}
			// 字符类型
			else if (attribute.isChar()) {
				balancer = new CharBalancer();
			} else if (attribute.isWChar()) {
				balancer = new WCharBalancer();
			} else if (attribute.isHChar()) {
				balancer = new HCharBalancer();
			} 
			// 无效
			else {
				return null;
			}

			// 是字节数组或者媒体类型
			if (attribute.isRaw() || attribute.isMedia()) {
				VariableAttribute that = (VariableAttribute) attribute;
				((VariableBalancer) balancer).setPacking(that.getPacking());
			}
			// 是字符类型
			if (attribute.isWord()) {
				WordAttribute that = (WordAttribute) attribute;
				((WordBalancer) balancer).setSentient(that.isSentient());
			}
		}
		// 数值类型
		else if (attribute.isShort()) {
			balancer = new ShortBalancer();
		} else if (attribute.isInteger()) {
			balancer = new IntegerBalancer();
		} else if (attribute.isLong()) {
			balancer = new LongBalancer();
		} else if (attribute.isFloat()) {
			balancer = new FloatBalancer();
		} else if (attribute.isDouble()) {
			balancer = new DoubleBalancer();
		}
		// 日期类型
		else if (attribute.isDate()) {
			balancer = new DateBalancer();
		} else if (attribute.isTime()) {
			balancer = new TimeBalancer();
		} else if (attribute.isTimestamp()) {
			balancer = new TimestampBalancer();
		}

		//		if(balancer == null) {
		//			throw new TaskException("illegal family: %d", attribute.getFamily());
		//		}
		// 返回列索引范围分割器
		return balancer;
	}

}