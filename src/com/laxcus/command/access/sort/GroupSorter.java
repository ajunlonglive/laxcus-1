/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.function.table.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.log.client.*;

/**
 * <code>SQL "GROUP BY"</code>归类排列器。用过SELECT检索过程中的GROUP BY操作。
 * 
 * @author scott.liang
 * @version 1.0 11/28/2011
 * @since laxcus 1.0
 */
public class GroupSorter {

	/** 数据库表句柄 */
	private Table table;

	/** SQL SELECT语句实例 **/
	private Select select;

	/** 列成员分组存储器 */
	private Map<GroupKey, GroupSet> results;

	/**
	 * 构造SQL "GROUP BY" 归类排列器，包括GROUP BY句柄和数据表句柄<br>
	 * 
	 * @param select SELECT命令
	 * @param table 数据表
	 */
	public GroupSorter(Select select, Table table) {
		super();
		setSelect(select);
		setTable(table);
	}

	/**
	 * 设置SELECT命令句柄
	 * @param e SELECT命令句柄
	 */
	public void setSelect(Select e) {
		select = e;
	}

	/**
	 * 返回SELECT命令句柄
	 * @return SELECT命令句柄
	 */
	public Select getSelect() {
		return select;
	}

	/**
	 * 设置数据表
	 * @param e 数据表实例
	 */
	public void setTable(Table e) {
		table = e;
	}

	/**
	 * 返回数据表
	 * @return 数据表实例
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 根据"GROUP BY"语句中定义的列名和一行中的列值，生成一个GroupKey
	 * @param row 行记录
	 * @return 返回GroupKey实例
	 */
	private GroupKey createKey(Row row) {
		short[] columnIds = select.getGroup().getColumnIds();
		// 取出列成员，生成一个GroupKey
		Column[] keys = new Column[columnIds.length];
		for (int i = 0; i < columnIds.length; i++) {
			keys[i] = row.find(columnIds[i]);
		}
		return new GroupKey(keys);
	}

	/**
	 * 数据归类分组接口
	 * @param array 行列表
	 * @return 返回重组后行列表
	 */
	public List<Row> align(List<Row> array) {
		// 初始化存储记录集(定义排序比较器)
		if (results == null) {
			GroupKeyComparator comparator = new GroupKeyComparator(table);
			results = new TreeMap<GroupKey, GroupSet>(comparator);
		}

		// 按照KEY进行记录分组
		for(Row row : array) {
			// 生成分组键
			GroupKey key = createKey(row);
			// 查找匹配的对象集合
			GroupSet set = results.get(key);
			if(set == null) {
				set = new GroupSet();
				results.put(key, set);
			}
			set.add(row);
		}

		// 收缩空间，节省内存
		for (GroupSet set : results.values()) {
			set.trim();
		}

		Logger.info("GroupSorter.align, into having! element size is %d", results.size());

		GroupByAdapter adapter = select.getGroup();

		// 根据HAVING子句，进一步筛选合格的结果
		ArrayList<GroupKey> removes = new ArrayList<GroupKey>();
		// 如果有HAVING子句时...
		if(adapter.getSituation() != null) {
			Iterator<Map.Entry<GroupKey, GroupSet>> iterators = results.entrySet().iterator();
			while (iterators.hasNext()) {
				Map.Entry<GroupKey, GroupSet> entry = iterators.next();

				Situation situation = adapter.getSituation();
				boolean ret = situation.sifting(entry.getValue().list());
				while (situation != null) {
					// 如果有同级的关联对象时...
					for (Situation partner : situation.getPartners()) {
						boolean rs = partner.sifting(entry.getValue().list());
						if (partner.isAND()) ret = (ret && rs);
						else if (partner.isOR()) ret = (ret || rs);
					}
					// 下一个
					situation = situation.next();
				}
				if (!ret) removes.add(entry.getKey());
			}

			//			for (GroupKey key : elements.keySet()) {
			//				GroupSet set = elements.get(key);
			//
			//				Situation situation = instance.getSituation();
			//				boolean ret = situation.sifting(set.list());
			//				while (situation != null) {
			//					// 如果有同级的关联对象时...
			//					for (Situation partner : situation.getPartners()) {
			//						boolean rs = partner.sifting(set.list());
			//						if (partner.isAND()) ret = (ret && rs);
			//						else if (partner.isOR()) ret = (ret || rs);
			//					}
			//					// 下一个
			//					situation = situation.getNext();
			//				}
			//				if (!ret) removes.add(  key);
			//			}
		}

		// 删除不匹配的结果，保留匹配的
		if (removes.size() > 0) {
			for (GroupKey key : removes) {
				results.remove(key);
			}
		}

		Logger.info("GroupSorter.align, having okay! element size is %d", results.size());

		// 更新集合并且输出
		List<Row> flush = new ArrayList<Row>(results.size());

		// 剩下的记录集，合并为一行记录
		for(GroupSet set: results.values()) {
			Row rs = new Row();
			List<Row> rows = set.list();

			ListSheet sheet = select.getListSheet();
			for (ListElement element : sheet.list()) {
				// 如果是列成员，原样保存
				if (element.isColumn()) {
					Column column = rows.get(0).find(element.getColumnId());
					if (column == null) {
						throw new NullPointerException();
					}
					rs.add(column);
				} else if (element.isFunction()) {
					// 调用聚合函数处理分组集合
					ColumnFunction function = ((FunctionElement) element).getFunction();
					Column column = function.makeup(set.list());
					if (column == null) {
						throw new NullPointerException();
					}
					// 设置标识号(区别与列属性的标识号，这是函数标识号，在SELECT语句建立时临时生成)
					column.setId(element.getIdentity());
					rs.add(column);
				}
			}
			// 保存一行记录
			flush.add(rs);
		}

		Logger.info("GroupSorter.align, finished! element size is %d", flush.size());

		return flush;
	}

}