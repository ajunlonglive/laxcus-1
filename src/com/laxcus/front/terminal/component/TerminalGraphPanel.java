/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.terminal.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.graph.*;
import com.laxcus.util.event.*;

/**
 * 图形面板，在右侧下方的选项页。<br>
 * 图形面板的读写操作要放入SWING队列。
 * 
 * @author scott.liang
 * @version 1.0 12/07/2011
 * @since laxcus 1.0
 */
public class TerminalGraphPanel extends JPanel {

	private static final long serialVersionUID = -7457792983328455463L;

	/** 渲染器 **/
	private TerminalGraphCellRenderer renderer;

	/** 列表框 **/
	private JList list = new JList();

	/** 显示模型 **/
	private DefaultListModel model = new DefaultListModel();

	/**
	 * 构造图形面板
	 */
	public TerminalGraphPanel() {
		super();
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 返回字体
	 * @return
	 */
	public Font getSelectFont() {
		return list.getFont();
	}

	/**
	 * 设置字体
	 * @param font
	 */
	public void setSelectFont(Font font) {
		addThread(new FontThread(font));
	}

	/**
	 * 修正字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			list.setFont(font);
		}
	}

	/**
	 * 字体线程
	 *
	 * @author scott.liang
	 * @version 1.0 8/28/2018
	 * @since laxcus 1.0
	 */
	class FontThread extends SwingEvent {
		Font font;

		FontThread(Font e) {
			super();
			font = e;
		}

		public void process() {
			__exchangeFont(font);
		}
	}

	/**
	 * 初始化
	 */
	public void init() {
		renderer = new TerminalGraphCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);

		// 工具提示
		String title = TerminalLauncher.getInstance().findCaption("Window/GraphPanel/title");
		FontKit.setToolTipText(list, title);

		// 自动选择单元高度，显示多行
		list.setFixedCellHeight(-1); 
		// 边框
		list.setBorder(new EmptyBorder(2, 2, 2, 2));
		// 多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		list.setEnabled(true);

		// 设置字体
		__exchangeFont(TerminalProperties.readTabbedGraphFont());

		// 滚动框
		JScrollPane scroll = new JScrollPane(list);
		FontKit.setToolTipText(scroll, title);
		scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		// 布局
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);

//		test("laxcus_logo.jpg", "行到水穷处，坐看云起时。\n大江东去，浪淘尽，千古风流人物");
		//		test("demo2.png", "空山新雨后，天气晚来秋。\r\n明月松间照，清泉石上流。\r\n竹喧归浣女，莲动下渔舟。\r\n随意春芳歇，王孙自可留。");
		//		test("demo3.png", "锦瑟无端五十弦，一弦一柱思华年。\r\n庄生小梦迷蝴蝶，望月春心托杜鹃。\r\n沧海月明珠有泪，蓝田日暖玉生烟。\r\n此情可待成追忆，只是回首已惘然。");
		//		test("demo4.png", "See you, see me");
		//		test("demo5.png", "木落雁南渡，北风江上寒。\n我家湘水曲，遥离楚云端");
		//		test("demo6.png", "秋风萧瑟天气寒，草木摇落露为霜。\n群鹄辞归雁南翔，念君客游多思肠。\n谦谦思归恋故乡，群何淹留寄他方。");
		//		test("demo7.png", "俺曾见，金陵玉殿莺啼晓，秦淮水榭花开早，谁知道容易冰销。\n眼看他起朱楼，眼看他宴宾客，眼看他楼塌了。");
	}

	//			void test(String demo, String text) {
	//				com.laxcus.util.res.ResourceLoader loader = new com.laxcus.util.res.ResourceLoader("conf/front/image");
	//				ImageIcon icon = loader.findImage(demo); 
	//				GraphItem item = new GraphItem(icon, text, text); //"千里江山寒色远");
	//				flash(item);
	//			}

//	void test(String demo, String text) {
//		com.laxcus.util.res.ResourceLoader loader = new com.laxcus.util.res.ResourceLoader("conf/front/terminal/image/about");
//		ImageIcon icon = loader.findImage(demo); 
//		GraphItem item = new GraphItem(icon, text, text); //"千里江山寒色远");
//		flash(item);
//	}

	class ClearThread extends SwingEvent {
		ClearThread(){super();}
		public void process() {
			model.clear();
			list.removeAll();
		}
	}

	class InfluxGraphThread extends SwingEvent {
		Object cell;
		InfluxGraphThread(Object e) {
			super();
			cell = e;
		}
		public void process() {
			model.addElement(cell);
		}
	}

	/**
	 * 清除全部图形
	 */
	public void clear() {
		addThread(new ClearThread());
	}

	/**
	 * 显示一个图形实例
	 * @param item GraphItem实例
	 */
	public void flash(GraphItem item) {
		GraphIconCell icon = new GraphIconCell(item.getIcon(), item.getTooltip());
		GraphTextCell text = new GraphTextCell(item.getText(), item.getTooltip());
		InfluxGraphThread s1 = new InfluxGraphThread(icon);
		InfluxGraphThread s2 = new InfluxGraphThread(text);
		// 加入SWING队列
		addThread(s1);
		addThread(s2);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	public void updateUI() {
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();
	}
}