/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.near;

import java.io.*;

import javax.swing.*;

import com.laxcus.command.contact.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.log.client.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.graph.*;
import com.laxcus.util.display.show.*;

/**
 * CONTACT.NEAR阶段组件，打印在DISTANT阶段产生的数据结果
 * 
 * @author scott.liang
 * @version 1.0 5/6/2020
 * @since laxcus 1.0
 */
public class PrintNearTask extends NearTask {

	/**
	 * 构造默认的CONTACT.NEAR阶段组件
	 */
	public PrintNearTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.task.swift.near.NearTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		
		String notice = "Message";
		int width = 300;
		
		Contact cmd = getCommand();
		NearObject near = cmd.getNearObject();
		if (near != null) {
			try {
				if (near.hasString("title")) {
					notice = near.findString("title");
				}
				if (near.hasInteger("width")) {
					width = near.findInteger("width");
				}
			} catch (Throwable e) {
				Logger.fatal(e);
				notice = "Throwable";
			}
		}
		
		// 显示内容
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, notice, width));
		setShowTitle(title);
		
		ClassReader reader = new ClassReader(b, off, len);
		int count = 0;
		while (reader.hasLeft()) {
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				SwiftPrintLine e = new SwiftPrintLine(reader);
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, e.line()));
				addShowItem(item);
			}
			count += size;
			
			// 间隔
			if (reader.hasLeft()) {
				ShowItem item = new ShowItem();
				item.add(new ShowStringCell(0, "-------"));
				addShowItem(item);
			}
		}
		
		// 打印信息在窗口状态栏
		getDisplay().setStatusText(String.format("显示 %d 行记录。", count));

		// 如果是图形界面，显示图像
		if (isTerminal()) {
			printImage();
		} else if(isDesktop()) {
			printImage();
		}
		
		return reader.getSeek();
	}
	
	/**
	 * 打印图像
	 * @throws TaskException
	 */
	private void printImage() throws TaskException {
		// 生成图像
		byte[] stream = readResource("conf/task/print/cloud.jpg");
		if (stream != null) {
			ImageIcon icon = new ImageIcon(stream);			
			GraphItem item = new GraphItem(icon, 
					"明月出天山，苍茫云海间。\n长风几万里，吹度玉门关。\n汉下白登道，胡窥青海湾。\n由来征战地，不见有人还。\n戎客望边色，思归多苦颜。\n高楼当此夜，叹息未应闲。\n\n单车欲问边，属国过居延。\n征蓬出汉塞，归雁入胡天。\n大漠孤烟直，长河落日圆。\n萧关逢候骑，都护在燕然。\n", 
					"显示生成动态的智能图表");
			getDisplay().flash(item);
		} else {
			getDisplay().setStatusText("读取图像数据失败！");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.task.swift.near.NearTask#display(java.io.File[])
	 */
	@Override
	public long display(File[] files) throws TaskException {
		return defaultDisplay(files);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.task.swift.near.NearTask#display(java.io.File)
	 */
	@Override
	public long display(File file) throws TaskException {
		return defaultDisplay(new File[] { file });
	}

}
