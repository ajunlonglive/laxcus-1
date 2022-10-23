/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.put;

import java.io.*;
import java.util.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;

/**
 * 随机数排序的CONDUCT.PUT阶段。<br><br>
 * 
 * CONDUCT.PUT阶段显示随机数排序结果，PUT阶段是DIFFUSE/CONVERGE算法的最后一个阶段。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/11/2011
 * @since laxcus 1.0
 */
public class SortPutTask extends PutTask {

	/**
	 * 构造随机数排序的PUT阶段任务。
	 */
	public SortPutTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		// 数据写入磁盘
		Conduct conduct = getCommand();
		PutObject object = conduct.getPutObject();
		if (object != null) {
			String filename = object.getWriteTo();
			if (filename != null) {
				super.writeTo(filename, b, off, len);
			}
		}

		int seek = off;
		int end = off + len;

		Logger.debug(getIssuer(), this, "display", "seek:%d, end:%d, size:%d", seek, end, end - seek);

		// 根据JRE语言环境，选择不同语言标题
		String text = "Random Number";
		Locale local = Locale.getDefault();
		// 判断是简体中文
		if (local.equals(Locale.SIMPLIFIED_CHINESE)) {
			text = "随机数";
		}

		// 显示内容
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, text, 100));
		setShowTitle(title);

		while (seek < end) {
			int digit = Laxkit.toInteger(b, seek, 4);
			seek += 4;

			ShowItem item = new ShowItem();
			item.add(new ShowIntegerCell(0, digit));
			addShowItem(item);
		}

		Logger.debug(getIssuer(), this, "display", "read size:%d", seek - off);

		return seek - off;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File[])
	 */
	@Override
	public long display(File[] files) throws TaskException {
		return defaultDisplay(files);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(java.io.File)
	 */
	@Override
	public long display(File file) throws TaskException {
		return defaultDisplay(new File[]{file});
	}

}