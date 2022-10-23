/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.put;

import java.io.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.hash.*;

/**
 * 在计算机屏幕上显示挖矿结果
 * 
 * @author xiaoyang.yuan
 * @version 1.0 12/12/2015
 * @since laxcus 1.0
 */
public class MinePutTask extends PutTask {

	/**
	 * 构造默认的挖矿输出类
	 */
	public MinePutTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.put.PutTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		// 数据写入磁盘
		Conduct conduct = getCommand();
		PutObject object = conduct.getPutObject();

		// 读取要命令中的自定义参数
		MinePutBox box = new MinePutBox();
		box.readAll(object);
		// 拿出这些关键字
		String node = box.getNode();
		String text = box.getText();
		String sha256 = box.getSHA256();

		// if (object != null) {
		// node = object.findString("NODE");
		// sha256 = object.findString("SHA256");
		// text = object.findString("TEXT");
		// }

		//		if (node == null) node = "Mine Site";
		//		if (sha256 == null) sha256 = "Mine Code";
		//		if (text == null) text = "明文";

		// 显示内容
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, sha256, 430));
		title.add(new ShowTitleCell(1, text, 200));
		title.add(new ShowTitleCell(2, node, 200));
		setShowTitle(title);

		// 如果没有数据，是空值
		if (len < 1) {
			return 0;
		}

		// 统计数目
		int count = 0;
		// 读字节内容，显示参数
		ClassReader reader = new ClassReader(b, off, len);
		while (reader.hasLeft()) {
			Node site = new Node(reader);
			SHA256Hash hash = new SHA256Hash(reader);
			String plain = reader.readString();

			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, hash));
			item.add(new ShowStringCell(1, plain));
			item.add(new ShowStringCell(2, site));
			addShowItem(item);
			count++;
		}

		// 打印信息在窗口状态栏
		getDisplay().setStatusText(String.format("本次一共挖出 %d 个符合要求的矿码!", count));

		return reader.getSeek();
	}

	/* (non-Javadoc)
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
