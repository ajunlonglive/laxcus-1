/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.command.access.*;

/**
 * CallDeleteInvoker的“ending”阶段备注。
 * 记录被删除的数据处理结果
 * 
 * @author scott.liang
 * @version 1.0 5/20/2015
 * @since laxcus 1.0
 */
final class DeleteEndingRemark {

	/** 反馈记录 **/
	private ArrayList<AssumeDelete> array = new ArrayList<AssumeDelete>();

	/**
	 * END阶段备注
	 */
	public DeleteEndingRemark() {
		super();
	}

	/**
	 * 保存
	 * @param e
	 * @return
	 */
	public boolean add(AssumeDelete e) {
		return array.add(e);
	}

	/**
	 * 返回列表
	 * @return
	 */
	public List<AssumeDelete> list() {
		return new ArrayList<AssumeDelete>(array);
	}
	
	/**
	 * 统计成功数目
	 * @return
	 */
	public int getSuccessCount() {
		int count = 0;
		for (AssumeDelete cmd : array) {
			if (cmd.isSuccess()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 被删除的行数
	 * @return
	 */
	public long getRows() {
		long rows = 0L;
		for (AssumeDelete cmd : array) {
			rows += cmd.getRows();
		}
		return rows;
	}

	/**
	 * 返回保存数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

}