/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.zone;

import java.util.*;

import com.laxcus.access.index.zone.*;
import com.laxcus.command.*;
import com.laxcus.util.*;

/**
 * 列索引值分区钩子。
 * 
 * 服务端返回的列索引值区域保存到这里。
 * 
 * @author scott.liang
 * @version 1.0 12/03/2012
 * @since laxcus 1.0
 */
public final class IndexZoneHook extends CommandHook {

	/** 索引域数组 **/
	private ArrayList<IndexZone> array = new ArrayList<IndexZone>();

	/**
	 * 构造默认的查找索引列区域的钩子
	 */
	public IndexZoneHook() {
		super();
	}

	/**
	 * 保存列索引值区域
	 * @param e IndexZone实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(IndexZone e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批列索引值区域
	 * @param a 列索引值区域列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<IndexZone> a) {
		int size = array.size();
		if (a != null) {
			for (IndexZone e : a) {
				add(e);
			}
		}
		return array.size() - size;
	}

	/**
	 * 输出全部列索引值区域
	 * @return 返回IndexZone列表
	 */
	public List<IndexZone> list() {
		return array;
	}

	/**
	 * 统计接收的列索引值区域数目
	 * @return 返回列索引值区域数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

}
