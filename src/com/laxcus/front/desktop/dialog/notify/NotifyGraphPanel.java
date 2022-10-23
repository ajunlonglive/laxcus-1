/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.notify;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.border.*;
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
class NotifyGraphPanel extends JPanel {

	private static final long serialVersionUID = -7457792983328455463L;

	/** 渲染器 **/
	private NotifyGraphCellRenderer renderer;

	/** 列表框 **/
	private JList list = new JList();

	/** 显示模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 弹出菜单 **/
	private JPopupMenu rockMenu;
	
//	private JMenuItem mnuDelete;

	/**
	 * 构造图形面板
	 */
	public NotifyGraphPanel() {
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

	private boolean hasClear() {
		// int[] rows = list.getSelectedIndices();
		// return rows != null && rows.length > 0;

		return model.getSize() > 0;
	}
	
	private boolean hasCopy() {
		int[] rows = list.getSelectedIndices();
		return rows != null && rows.length > 0;
	}
	
	private boolean hasSelectAll() {
		int size = model.getSize();
		if (size < 1) {
			return false;
		}
		int[] rows = list.getSelectedIndices();
		if (rows == null || rows.length == 0) {
			return true;
		}
		return rows.length < size;
	}
	
	/**
	 * 选择全部
	 */
	void doSelectAll() {
		int size = model.getSize();
		if (size == 0) {
			return;
		}
		int[] indexs = new int[size];
		for (int index = 0; index < size; index++) {
			indexs[index] = index;
		}
		list.setSelectedIndices(indexs);
	}

	/**
	 * 复制
	 */
	void doCopy() {
		StringBuilder bf = new StringBuilder();
		int[] vs = list.getSelectedIndices();
		for (int i = 0; i < vs.length; i++) {
			int index = vs[i];
			Object element = model.getElementAt(index);
			if (element.getClass() == GraphTextCell.class) {
				GraphTextCell e = (GraphTextCell) element;
				if (bf.length() > 0) {
					bf.append("\r\n");
				}
				bf.append(e.getText());
			}
		}
		// 复制到内存
		if (bf.length() > 0) {
			try {
				String text = bf.toString();
				// 复制到系统剪贴板
				Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable transfer = new StringSelection(text);
				board.setContents(transfer, null);
			} catch (Throwable e) {

			}
		}
	}

	void doClear() {
		clear();
	}

	/**
	 * 显示弹出菜单
	 * @param e
	 */
	private void showPopupMenu(MouseEvent e) {
		// 不满足SWING条件的POPUP触发，不处理
		if (!e.isPopupTrigger()) {
			return;
		}

		JMenuItem item = MenuBuilder.findMenuItemByMethod(rockMenu, "doClear");
		if (item != null) {
			item.setEnabled(hasClear());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doSelectAll");
		if (item != null) {
			item.setEnabled(hasSelectAll());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doCopy");
		if (item != null) {
			item.setEnabled(hasCopy());
		}
		
		int newX = e.getX();
		int newY = e.getY();
		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}

	class ActionAdapter implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			click(e);
		}
	}

	/**
	 * 菜单事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object object = event.getSource();
		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
			JMenuItem source = (JMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}

	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {

		} catch (IllegalArgumentException e) {

		} catch (IllegalAccessException e) {

		} catch (InvocationTargetException e) {

		}
	}

	class CommandMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}
	}

	private final String REGEX = "^\\s*(?:[\\w\\W]+)[\\(\\[]([a-zA-Z]{1})[\\]\\)]\\s*$";
	
	/**
	 * 设置快捷键
	 * @param but
	 * @param input
	 */
	public void setMnemonic(JMenuItem but, String input) {
		if (input == null) {
			return;
		}
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String s = matcher.group(1);
			char w = s.charAt(0);
			but.setMnemonic(w);
		}
	}

	/**
	 * 生成菜单项
	 * @param textKey
	 * @param method
	 * @param w
	 * @return
	 */
	private JMenuItem createMenuItem(String textKey, String method) {
		String text = UIManager.getString(textKey);
		JMenuItem item = new JMenuItem(text);
		item.setName(method);
		item.addActionListener(new ActionAdapter());
		
		setMnemonic(item, text);
		
//		// 如果是快捷吸
//		if ((w >= 'a' && w <= 'z') || (w >= 'A' && w <= 'Z')) {
//			item.setMnemonic(w);
//		}
		
		item.setBorder(new EmptyBorder(2,4,2,4));
		return item;
	}
                                                                                        
	/**
	 * 初始化弹出菜单
	 */
	private void initMenu() {                                                    
		String[] texts = new String[] { "NotifyDialog.MenuitemCopyGraphicText",
				"NotifyDialog.MenuitemDeleteGraphicText","NotifyDialog.MenuitemSelectAllGraphicText" };
		// 快捷键
//		char[] shorts = new char[] {  'C','D','A' };
		
		// 操作方法
		String[] methods = new String[] { "doCopy", "doClear","doSelectAll" };

		JMenuItem copyItem = createMenuItem(texts[0], methods[0]);
		JMenuItem mnuDelete = createMenuItem(texts[1], methods[1]);
		JMenuItem selectAllItem = createMenuItem(texts[2], methods[2]);
		
		rockMenu = new JPopupMenu();
		rockMenu.add(copyItem);
		rockMenu.add(mnuDelete);
		rockMenu.add(selectAllItem);

		rockMenu.setInvoker(list);
		list.addMouseListener(new CommandMouseAdapter());
	}

	/**
	 * 初始化
	 */
	public void init() {
		renderer = new NotifyGraphCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);

		//		// 工具提示
		//		String title = NotifyLauncher.getInstance().findCaption("Window/GraphPanel/title");
		//		FontKit.setToolTipText(list, title);

		// 自动选择单元高度，显示多行
		list.setFixedCellHeight(-1); 
		// 边框
		list.setBorder(new EmptyBorder(1, 1, 1, 1));
		// 多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		list.setEnabled(true);

		//		// 设置字体
		//		__exchangeFont(NotifyProperties.readTabbedGraphFont());

		// 滚动框
		JScrollPane jsp = new JScrollPane(list);
		jsp.setBorder(new HighlightBorder(1));

		//		FontKit.setToolTipText(scroll, title);
		//		scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		// 布局
		setLayout(new BorderLayout(0, 0));
		add(jsp, BorderLayout.CENTER);

		// 初始化菜单
		initMenu();

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
		super.updateUI();
		if (renderer != null) {
			renderer.updateUI();
		}

		if (rockMenu != null) {
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}
	}
}