/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.near;

import java.io.*;

import com.laxcus.command.contact.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.hash.*;

/**
 * 挖矿NEAR阶段组件，显示DISTANT阶段产生的矿码
 * 
 * @author scott.liang
 * @version 1.0 12/26/2020
 * @since laxcus 1.0
 */
public class MinerNearTask extends NearTask {

	/**
	 * 构造默认的挖矿NEAR阶段组件
	 */
	public MinerNearTask() {
		super();
	}
	
	private int readCode(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		int count =0;
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
		return count;
	}
	
	/**
	 * 找到匹配的宽度定义
	 * @param key 关键字名称
	 * @param defaultWidth 默认值
	 * @return 返回对应数值
	 */
	private int findWidth(String key, int defaultWidth) {
		Contact cmd = this.getCommand();
		NearObject object = cmd.getNearObject();
		if (object.hasInteger(key)) {
			Integer value = object.findInteger(key);
			return value.intValue();
		} else {
			return defaultWidth;
		}
	}
	
	private String formatGap(long gap) {
		String s = Long.toString(gap);
		int last = s.length() - 1;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (i > 0 && i % 3 == 0) {
				sb.insert(0, ',');
			}
			char w = s.charAt(last);
			last--;
			sb.insert(0, w);
		}
		return sb.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.display.LocalTask#display(byte[], int, int)
	 */
	@Override
	public long display(byte[] b, int off, int len) throws TaskException {
		// 数据写入磁盘
		//		Contact conduct = getCommand();
		//		NearObject object = conduct.getNearObject();

		// 读取要命令中的自定义参数
		//		MineNearBox box = new MineNearBox();
		//		box.readAll(object);
		
		// 拿出这些关键字
		String node = "Site"; // box.getNode();
		String text = "Mine Text"; // box.getText();
		String sha256 = "Mine Code"; // box.getSHA256();

		// 中文
		if (isSimplfiedChinese()) {
			node = "节点";
			text = "明文";
			sha256 = "散列码";
		}

		// if (object != null) {
		// node = object.findString("NODE");
		// sha256 = object.findString("SHA256");
		// text = object.findString("TEXT");
		// }

		//		if (node == null) node = "Mine Site";
		//		if (sha256 == null) sha256 = "Mine Code";
		//		if (text == null) text = "明文";

//		public final static String codeWidthTitle = "CODE-WIDH-TITLE";
//		public final static String textWidthTitle = "TEXT-WIDTH-TITLE";
//		public final static String siteWidthTitle = "SITE-WIDTH-TITLE";

		// 显示内容
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, sha256, findWidth("CODE-WIDH-TITLE",430)));
		title.add(new ShowTitleCell(1, text, findWidth("TEXT-WIDTH-TITLE",200)));
		title.add(new ShowTitleCell(2, node, findWidth("SITE-WIDTH-TITLE",200)));
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
			int size = reader.readInt();
			if (size == 0) {
				continue;
			}

			// 读取长度
			byte[] array = reader.read(size);
			int elements = readCode(array, 0, array.length);
			count += elements;
		}
		
		long gap = 0;
		try {
			Contact cmd = super.getCommand();
			DistantObject obj = cmd.getDistantObject();
			DistantInputter inputter = obj.getInputter();
			long begin = inputter.findLong("BEGIN");
			long end = inputter.findLong("END");
			gap = end - begin;
		} catch (Throwable e) {

		}

		// 打印信息在窗口状态栏
		if(isSimplfiedChinese()) {
			setStatusText(String.format("%s次散列计算，一共计算出来%d个符合要求的散列码!", formatGap(gap), count));
		} else {
			setStatusText(String.format("%s, Hash codes: %d", formatGap(gap), count));
		}

		return reader.getSeek();
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.laxcus.task.swift.near.NearTask#display(byte[], int, int)
//	 */
//	@Override
//	public long display(byte[] b, int off, int len) throws TaskException {
//		
//		String notice = "Message";
//		int width = 300;
//		
//		Contact cmd = getCommand();
//		NearObject near = cmd.getNearObject();
//		if (near != null) {
//			try {
//				if (near.hasString("title")) {
//					notice = near.findString("title");
//				}
//				if (near.hasInteger("width")) {
//					width = near.findInteger("width");
//				}
//			} catch (Throwable e) {
//				Logger.fatal(e);
//				notice = "Throwable";
//			}
//		}
//		
//		// 显示内容
//		ShowTitle title = new ShowTitle();
//		title.add(new ShowTitleCell(0, notice, width));
//		setShowTitle(title);
//		
//		ClassReader reader = new ClassReader(b, off, len);
//		int count = 0;
//		while (reader.hasLeft()) {
//			int size = reader.readInt();
//			for (int i = 0; i < size; i++) {
//				SwiftMinerLine e = new SwiftMinerLine(reader);
//				ShowItem item = new ShowItem();
//				item.add(new ShowStringCell(0, e.line()));
//				addShowItem(item);
//			}
//			count += size;
//			
//			// 间隔
//			if (reader.hasLeft()) {
//				ShowItem item = new ShowItem();
//				item.add(new ShowStringCell(0, "-------"));
//				addShowItem(item);
//			}
//		}
//		
//		// 打印信息在窗口状态栏
//		getDisplay().setStatusText(String.format("显示 %d 行记录。", count));
//
////		// 如果是图形界面，显示图像
////		if (isTerminal()) {
////			printImage();
////		}
//		
//		return reader.getSeek();
//	}
	
//	/**
//	 * 打印图像
//	 * @throws TaskException
//	 */
//	private void printImage() throws TaskException {
//		// 生成图像
//		byte[] stream = readResource("conf/task/print/cloud.jpg");
//		if (stream != null) {
//			ImageIcon icon = new ImageIcon(stream);			
//			GraphItem item = new GraphItem(icon, 
//					"明月出天山，苍茫云海间。\n长风几万里，吹度玉门关。\n汉下白登道，胡窥青海湾。\n由来征战地，不见有人还。\n戎客望边色，思归多苦颜。\n高楼当此夜，叹息未应闲。\n\n单车欲问边，属国过居延。\n征蓬出汉塞，归雁入胡天。\n大漠孤烟直，长河落日圆。\n萧关逢候骑，都护在燕然。\n", 
//					"显示生成动态的智能图表");
//			getDisplay().flash(item);
//		} else {
//			getDisplay().setStatusText("读取图像数据失败！");
//		}
//	}

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
