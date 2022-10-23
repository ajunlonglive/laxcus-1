/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.evaluate;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;

/**
 * 一致性记录。<br>
 * 过滤相同的记录，保留不同的记录
 * 
 * @author scott.liang
 * @version 1.0 12/08/2011
 * @since laxcus 1.0
 */
final class DistinctRecord {

	/** 索引表 **/
	private Sheet indexSheet;

	/** 行数组 **/
	private ArrayList<Row> array = new ArrayList<Row>(1024);

	/**
	 * 构造一致性记录
	 */
	public DistinctRecord(Sheet e) {
		this.indexSheet = e;
	}

	/**
	 * 比较两行记录的匹配
	 * @param row
	 * @param that
	 * @return
	 */
	private boolean match(Row row, Row that) {
		int size = row.size();
		if (size != that.size()) {
			return false;
		}

		for (int index = 0; index < size; index++) {
			Column s1 = row.get(index);
			Column s2 = that.get(index);
			if (s1.getType() != s2.getType()) {
				return false;
			}

			if (s1.isCalendar() || s1.isNumber()) {
				if(s1.compare(s2) != 0) {
					return false;
				}
			} else if(s1.isRaw()) {
				if (s1.compare(s2) != 0) {
					return false;
				}
			} else if(s1.isWord()) {
				// 进行Packing和大小写检查
				ColumnAttribute attribute = indexSheet.get(index);
				if (attribute == null) {
					throw new NullPointerException("null attribute");
				}
				if (attribute.getType() != s1.getType()) {
					throw new IllegalArgumentException("illegal column attribute");
				}
				WordAttribute s3 = (WordAttribute) attribute;

				Packing packing = s3.getPacking();
				boolean sentient = s3.isSentient();

				if (!sentient || (packing != null && packing.isEnabled())) {
					Word b1 = (Word) s1;
					Word b2 = (Word) s2;
					if (b1.compare(b2, packing, sentient, true) != 0) {
						return false;
					}
				} else if (s1.compare(s2) != 0) {
					return false;
				}

			}
		}

		// 完全匹配，返回TRUE
		return true;
	}

	/**
	 * 增加一行记录
	 * @param row
	 */
	public void add(Row row) {
		// 逐一比较，如果相同不保存
		for (Row that : array) {
			if (match(row, that)) {
				return;
			}
		}
		array.add(row);
	}

	public List<Row> list() {
		return this.array;
	}

	public int size() {
		return this.array.size();
	}
	
}