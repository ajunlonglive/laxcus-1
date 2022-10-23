/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.put;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.task.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.show.*;

/**
 * 查询插入PUT阶段任务
 * 
 * @author scott.liang
 * @version 1.0 11/30/2020
 * @since laxcus 1.0
 */
public class InjectSelectPutTask extends DefaultSelectPutTask {

	/**
	 * 构造默认的查询插入PUT阶段任务
	 */
	public InjectSelectPutTask() {
		super();
	}

	/**
	 * 打印出结果
	 * @param b
	 * @param off
	 * @param len
	 */
	private long printX(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		Space space = null;
		long rows = 0;
		int sites = 0;
		while (reader.hasLeft()) {
			Space temp = new Space(reader);
			// 如果没有定义，设置表名
			if (space == null) {
				space = temp;
			}
			long count = reader.readLong();
			rows += count;
			sites++; // 统计执行节点
		}

		// 判断当前是中文环境
		boolean simple = isSimplfiedChinese();
		
		// 表头
		ShowTitle title = new ShowTitle();
		if (simple) {
			title.add(new ShowTitleCell(0, "结果", 80));
			title.add(new ShowTitleCell(1, "执行节点", 120));
			title.add(new ShowTitleCell(2, "表名", 220));
			title.add(new ShowTitleCell(3, "插入行数", 80));
		} else {
			title.add(new ShowTitleCell(0, "Status", 80));
			title.add(new ShowTitleCell(1, "Sites", 120));
			title.add(new ShowTitleCell(2, "Table", 220));
			title.add(new ShowTitleCell(3, "Rows", 80));
		}

		boolean success = (space != null && rows > 0);

		// 行数
		ShowItem item = new ShowItem();
		if (success) {
			if (simple) {
				item.add(new ShowStringCell(0, "成功"));
			} else {
				item.add(new ShowStringCell(0, "Successful"));
			}
			item.add(new ShowIntegerCell(1, sites));
			item.add(new ShowStringCell(2, space.toString()));
			item.add(new ShowLongCell(3, rows));
		} else {
			if (simple) {
				item.add(new ShowStringCell(0, "错误"));
			} else {
				item.add(new ShowStringCell(0, "Failed"));
			}
			item.add(new ShowStringCell(1, "--"));
			item.add(new ShowStringCell(2, "--"));
			item.add(new ShowStringCell(3, "--"));
		}

		// 显示结果
		setShowTitle(title);
		addShowItem(item);

		return reader.getUsed();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.LocalTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		return printX(b, off, len);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.LocalTask#display(java.io.File[])
	 */
	@Override
	public long display(File[] files) throws TaskException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (File file : files) {
			byte[] b = read(file);
			out.write(b, 0, b.length);
		}
		byte[] b = out.toByteArray();
		return printX(b, 0, b.length);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File)
	 */
	@Override
	public long display(File file) throws TaskException {
		return display(new File[] { file });
	}

}