/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.util.*;

import com.laxcus.util.*;

/**
 * 组件皮肤配置参数表 <br>
 * 保存不同的UI组件皮肤参数 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/3/2021
 * @since laxcus 1.0
 */
public class SkinSheet {

	/**
	 * 组件皮肤参数，由KEY/VALUE组成 <br><br>
	 * 
	 * Key 名称如：InternalFrameUI，由JRE定义，用来替换JRE中默认的配置。<br>
	 * Value 名称如：com.laxcus.gui.FlatInternalFrameUI，全路径格式。<br><br>
	 *
	 * @author scott.liang
	 * @version 1.0 10/3/2021
	 * @since laxcus 1.0
	 */
	public final class SkinElement implements Comparable<SkinElement> {
		
		/** KEY **/
		private String key;

		/** 指向值 **/
		private String value;

		/**
		 * 构造组件皮肤参数
		 * @param key
		 * @param value
		 */
		public SkinElement(String key, String value) {
			super();
			setKey(key);
			setValue(value);
		}

		/**
		 * 设置键
		 * @param s
		 */
		public void setKey(String s) {
			Laxkit.nullabled(s);
			key = s;
		}

		/**
		 * 返回键
		 * @return
		 */
		public String getKey() {
			return key;
		}

		/**
		 * 设置值
		 * @param s
		 */
		public void setValue(String s) {
			Laxkit.nullabled(s);
			value = s;
		}

		/**
		 * 返回值
		 * @return
		 */
		public String getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%s %s", key, value);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return key.hashCode() ^ value.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object that) {
			if (that == null || that.getClass() != SkinElement.class) {
				return false;
			}

			return compareTo((SkinElement) that) == 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(SkinElement that) {
			if (that == null) {
				return 1;
			}
			int ret = key.compareTo(that.key);
			if (ret == 0) {
				ret = value.compareTo(that.value);
			}
			return ret;
		}
	}

	/** 皮肤参数 **/
	private ArrayList<SkinElement> array = new ArrayList<SkinElement>();

	/**
	 * 构造组件皮肤配置参数表
	 */
	public SkinSheet() {
		super();
	}

	/**
	 * 增加皮肤参数
	 * @param e
	 * @return
	 */
	public boolean add(SkinElement e) {
		Laxkit.nullabled(e);
		if (array.contains(e)) {
			return false;
		}
		return array.add(e);
	}
	
	/**
	 * 增加皮肤参数
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, String value) {
		SkinElement e = new SkinElement(key, value);
		return add(e);
	}
	
	/**
	 * 删除皮肤参数
	 * @param e
	 * @return
	 */
	public boolean remove(SkinElement e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}
	
	/**
	 * 输出全部皮肤参数
	 * @return
	 */
	public List<SkinElement> list() {
		return new ArrayList<SkinElement>(array);
	}
	
	/**
	 * 输出皮肤参数数组
	 * @return
	 */
	public SkinElement[] toArray() {
		SkinElement[] a = new SkinElement[array.size()];
		return array.toArray(a);
	}
	
	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty(){
		return array.isEmpty();
	}
	
	/**
	 * 返回成员数目
	 * @return
	 */
	public int size(){
		return array.size();
	}
}
