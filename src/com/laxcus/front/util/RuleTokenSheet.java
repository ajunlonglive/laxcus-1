/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.util;

import java.util.*;

import com.laxcus.command.rule.*;
import com.laxcus.util.*;

/**
 * 事务标记表
 * 
 * @author scott.liang
 * @version 1.0 8/8/2017
 * @since laxcus 1.0
 */
public class RuleTokenSheet {

	/** 事务标记集合 **/
	private TreeSet<RuleToken> array = new TreeSet<RuleToken>();

	/**
	 * 构造事务标记表
	 */
	public RuleTokenSheet() {
		super();
	}

	/**
	 * 保存建立事务标记
	 * @param e 建立事务标记
	 * @return 返回真或者假
	 */
	public boolean add(RuleToken e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 查询匹配的命令单元
	 * @param tag 事务处理标识
	 * @return 返回RuleToken，没有找到返回空指针。
	 */
	public RuleToken find(ProcessRuleTag tag) {
		for (RuleToken e : array) {
			if (Laxkit.compareTo(e.getCommandTag(), tag) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 输出全部事务标记
	 * @return 事务标记列表
	 */
	public List<RuleToken> list() {
		return new ArrayList<RuleToken>(array);
	}

	/**
	 * 统计成员数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

}