/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.modulate.issue;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.establish.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.set.*;

/**
 * 数据优化的“ISSUE”阶段。<br><br>
 * 
 * MODULATE.ISSUE检查ESTABLISH中的参数，为后续SCAN阶段分配会话。
 * 
 * @author scott.liang
 * @version 1.1 1/23/2013
 * @since laxcus 1.0
 */
public class ModulateIssueTask extends IssueTask {

	/**
	 * 构造数据优化的“ISSUE”阶段任务
	 */
	public ModulateIssueTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.issue.IssueTask#create(com.laxcus.command.establish.Establish)
	 */
	@Override
	public Establish create(Establish cmd) throws TaskException {		
		/**
		 * 检查条件：
		 * 1. SCAN 阶段的数据表只能有一个
		 * 2. SIFT 阶段的数据表只能有一个
		 * 3. SCAN/SIFT阶段的数据表名必须一致
		 */
		ArrayList<Space> spaces = new ArrayList<Space>();
		ArrayList<Dock> docks = new ArrayList<Dock>();
		// 收集表名（只有一个）
		ScanObject scan = cmd.getScanObject();
		for (ScanInputter inputter : scan.getInputters()) {
			spaces.addAll(inputter.getSpaces());
		}
		if (spaces.size() != 1) {
			throw new IssueTaskException("space size too large:%d", spaces.size());
		}

		// 收集列空间，只有一个
		SiftObject sift = cmd.getSiftObject();
		while (sift != null) {
			SiftInputter inputter = sift.getInputter();
			if (inputter != null) {
				docks.addAll(inputter.getDocks());
			}
			sift = sift.next();
		}
		if (docks.size() != 1) {
			throw new IssueTaskException("dock size too large:%d", docks.size());
		}

		// 必须匹配
		Dock dock = docks.get(0);
		Space space = spaces.get(0);
		if (dock.getSpace().compareTo(space) != 0) {
			throw new IssueTaskException("cannot be match [%s - %s]", dock, space);
		}

		// 找到表配置
		Table table = findTable(space);
		// 检查键。如果没有定义列，以主键为基础进行优化；否则，以指定的键进行优化（包括主键）
		short columnId = dock.getColumnId();
		if (columnId == 0) {
			// 找到主键，以主键为基础进行优化
			for (ColumnAttribute e : table.list()) {
				if (e.isPrimeKey()) {
					dock.setColumnId(columnId = e.getColumnId());
					break;
				}
			}
		} else {
			ColumnAttribute attribute = table.find(columnId);
			if (!attribute.isKey()) {
				throw new IssueTaskException("illegal key %s", attribute.getName());
			}
		}

		// 检查SIFT阶段命名的BUILD站点存在
		sift = cmd.getSiftObject();
		NodeSet set = findSiftSites(sift.getPhase());
		// 查找SCAN阶段主站点，如果没有，弹出异常
		set = findScanSites(space);
		// 为SCAN阶段分配会话
		ScanDispatcher dispatcher = new ScanDispatcher(scan.getPhase());
		for (Node node : set.show()) {
			ScanSession session = new ScanSession(scan.getPhase(), node);
			// 建立SCAN扫描成员，表空间必须锁定
			ScanMember memeber = new ScanMember(space);
			session.addMember(memeber);
			// 保存会话到SCAN分派器
			dispatcher.addSession(session);
		}
		// 设置SCAN资源分派器
		scan.setDispatcher(dispatcher);

		// 返回保存了配置后的ESTABLISH命令
		return cmd;
	}

}