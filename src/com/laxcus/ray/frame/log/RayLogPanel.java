/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.frame.log;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.dialog.message.*;
import com.laxcus.gui.log.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.skin.*;

/**
 * 日志显示面板，位于右侧下方的选择页中。
 * 因为SWING组件是线程不安全，所有读写操作被放入SWING事件队列中执行。
 * 
 * @author scott.liang
 * @version 1.0 5/26/2021
 * @since laxcus 1.0
 */
class RayLogPanel extends JPanel implements ActionListener,LogTransmitter {

	private static final long serialVersionUID = 2178305234736392484L;

	/** 单向锁 **/
	private SingleLock lock = new SingleLock();

	/** 最多显示日志数字 **/
	private final int MAX_ELEMENTS = 2000;

	/** 渲染器 **/
	private RayLogCellRenderer renderer;

	/** 日志最大尺寸 **/
	private volatile int maxItems;

	/** 日志列表 **/
	private JList list = new JList();

	/** 日志列表 **/
	private DefaultListModel model = new DefaultListModel();

	/** 禁止显示日志 **/
	private volatile boolean forbid;

	/** 禁止显示日志按纽 **/
	private JCheckBox cmdForbid = new JCheckBox();

	/** 弹出菜单 **/
	private JPopupMenu rockMenu;

	/** 菜单项 **/
	private JMenuItem mnuSelectAll, mnuCopy, mnuFont;

	/**
	 * 构造日志显示面板
	 */
	public RayLogPanel() {
		super();
		// 规定的最大日志数目
		setMaxItems(MAX_ELEMENTS);
		// 默认是假
		forbid = false;
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	void __doSelectAll() {
		int size = model.size();
		if (size < 1) {
			return;
		}
		int[] serials = new int[size];
		for (int i = 0; i < size; i++) {
			serials[i] = i;
		}
		list.setSelectedIndices(serials);
	}

	void doSelectAll() {
		lock.lock();
		try {
			__doSelectAll();
		} catch (Throwable e) {

		} finally {
			lock.unlock();
		}	
	}

	void __doCopy() {
		int[] serials = list.getSelectedIndices();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < serials.length; i++) {
			Object object = model.getElementAt(serials[i]);
			if (object.getClass() == LogItem.class) {
				LogItem item = (LogItem) object;
				if (buf.length() > 0) {
					buf.append('\n');
				}
				buf.append(item.getPrimitive());
			}
		}

		// 复制到系统剪贴板
		if (buf.length() > 0) {
			String text = buf.toString();
			Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable transfer = new StringSelection(text);
			board.setContents(transfer, null);
		}
	}

	void doCopy() {
		lock.lock();
		try {
			__doCopy();
		} catch (Throwable e) {

		} finally {
			lock.unlock();
		}	
	}

	void doFont() {
		Font font = getSelectFont();
		font = MessageBox.choiceFont(this, font);
		if (font != null) {
			setSelectFont(font);
			RTKit.writeFont(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Font", font);
		}
	}

//	/**
//	 * 生成菜单a项
//	 * @param xmlCaption
//	 * @param xmlKey
//	 * @param method
//	 * @return
//	 */
//	private JMenuItem createMenuItem(String xmlCaption, String xmlKey, String method) {
//		JMenuItem item = new JMenuItem();
//		// 字体
//		String caption = findCaption(xmlCaption);
//		FontKit.setButtonText(item, caption);
//		// 热键
//		String key = findCaption(xmlKey);
//		if (key != null) {
//			key = key.trim();
//			if (key.length() > 0) {
//				item.setMnemonic(key.charAt(0));
//			}
//		}
//		// 方法
//		item.setName(method);
//		item.addActionListener(this);
//		return item;
//	}

//	/**
//	 * 初始化菜单
//	 */
//	private void initMenu() {
//		String[] texts = new String[] {"Window/LogPanel/PopupMenu/SelectAll/Text",
//				"Window/LogPanel/PopupMenu/Copy/Text", "Window/LogPanel/PopupMenu/Font/Text" };
//		String[] keys = new String[] { "Window/LogPanel/PopupMenu/SelectAll/Key",
//				"Window/LogPanel/PopupMenu/Copy/Key", "Window/LogPanel/PopupMenu/Font/Key" };
//		String[] methods = new String[] { "doSelectAll", "doCopy", "doFont" };
//
//		mnuSelectAll = createMenuItem(texts[0], keys[0], methods[0]);
//		mnuCopy = createMenuItem(texts[1], keys[1], methods[1]);
//		mnuFont = createMenuItem(texts[2], keys[2], methods[2]);
//
//		// 生成菜单
//		rockMenu = new JPopupMenu();
//		rockMenu.add(mnuSelectAll);
//		rockMenu.addSeparator();
//		rockMenu.add(mnuCopy);
//		rockMenu.addSeparator();
//		rockMenu.add(mnuFont);
//	}

	/**
	 * 生成菜单a项
	 * @param text
	 * @param key
	 * @param method
	 * @return
	 */
	private JMenuItem createMenuItem(String text, char key, String method) {
		JMenuItem item = new JMenuItem();
		// 字体
		FontKit.setButtonText(item, text);
		// 快捷键
		if (key > 32) {
			item.setMnemonic(key);
		}

		// 方法
		item.setName(method);
		item.addActionListener(this);
		item.setBorder(new EmptyBorder(4,4,4,4));
		return item;
	}
	
	/**
	 * 初始化菜单
	 */
	private void initMenu() {   
		String[] texts = new String[] {
				UIManager.getString("LogFrame.SelectAllText"),
				UIManager.getString("LogFrame.CopyText"),
				UIManager.getString("LogFrame.FontText") };
		char[] keys = new char[] { 'A', 'C', 'F' };
		String[] methods = new String[] { "doSelectAll", "doCopy", "doFont" };

		mnuSelectAll = createMenuItem(texts[0], keys[0], methods[0]);
		mnuCopy = createMenuItem(texts[1], keys[1], methods[1]);
		mnuFont = createMenuItem(texts[2], keys[2], methods[2]);

		// 生成菜单
		rockMenu = new JPopupMenu();
		rockMenu.add(mnuSelectAll);
		rockMenu.addSeparator();
		rockMenu.add(mnuCopy);
		rockMenu.addSeparator();
		rockMenu.add(mnuFont);
	}
	
	class LogMouseAdapter extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
		}
	}
	
	/**
	 * 显示弹出菜单
	 * @param event
	 */
	private void showPopupMenu(MouseEvent event) {
		// 不满足SWING条件的POPUP触发，不处理
		if (!event.isPopupTrigger()) {
			return;
		}
		
		// 如果有选择项，"COPY"菜单才有效
		int[] serials = list.getSelectedIndices();
		mnuCopy.setEnabled(serials.length > 0);

		// 显示
		int newX = event.getX();
		int newY = event.getY();
		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}

//	class ClickThread extends SwingEvent {
//		ActionEvent event;
//
//		ClickThread(ActionEvent e) {
//			super();
//			event = e;
//		}
//
//		public void process() {
//			doClickMenu(event);
//		}
//	}

	private void doClickMenu(ActionEvent event) {
		Object object = event.getSource();
		if (object.getClass() == JMenuItem.class) {
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
			Method method = getClass().getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {

		} catch (IllegalArgumentException e) {

		} catch (IllegalAccessException e) {

		} catch (InvocationTargetException e) {

		}
	}

//	class ForbidThread extends SwingEvent {
//		ActionEvent event;
//
//		ForbidThread(ActionEvent e) {
//			super();
//			event = e;
//		}
//
//		public void process() {
//			if (event.getSource() == cmdForbid) {
//				doClickForbid();
//			}
//		}
//	}

	/**
	 * 屏蔽或者显示日志
	 */
	private void doClickForbid() {
		boolean select = cmdForbid.isSelected();
		if (select) {
			forbid = true;
			// 清除全部日志
			clear();
		} else {
			forbid = false;
		}
		// 拒绝显示日志
		RTKit.writeBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Forbid", forbid);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cmdForbid) {
			// addThread(new ForbidThread(e));
			doClickForbid();
		} else if (e.getSource().getClass() == JMenuItem.class) {
			// addThread(new ClickThread(e));
			doClickMenu(e);
		}
	}

	/**
	 * 设置最大显示单元数目。在规定范围内！
	 * @param n 日志单元数
	 */
	public int setMaxItems(int n) {
		if (n > MAX_ELEMENTS) {
			maxItems = MAX_ELEMENTS;
		} else if (n >= 0 && n <= MAX_ELEMENTS) {
			maxItems = n;
		} else if (n < 0) {
			maxItems = 0;
		}
		// 清除日志
		if (maxItems < 1) {
			clear();
		}
		
		
		// 写入环境
		RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Logs", maxItems);
		
		// 显示日志
		addThread(new NumberThread());
		// 返回结果
		return maxItems;
	}

	/**
	 * 返回最大显示单元数目
	 * @return 日志单元数
	 */
	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * 判断是拒绝显示日志
	 * @return 真或者假
	 */
	public boolean isForbid() {
		return forbid;
	}

	/**
	 * 显示文本
	 */
	private void setLogTooltip() {
		String text = UIManager.getString("LogFrame.NumberText");
		String value = String.format(text, maxItems);
		FontKit.setToolTipText(cmdForbid, value);
	}

	/**
	 * 做为工具提示，显示在选择按纽上
	 *
	 * @author scott.liang
	 * @version 1.0 1/6/2021
	 * @since laxcus 1.0
	 */
	class NumberThread extends SwingEvent {

		public NumberThread(){
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			setLogTooltip();
		}
	}

	class TitlePanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public TitlePanel() {
			super();
		}

		protected void paintBorder(Graphics g) {
			Color old = g.getColor();
			int width = getWidth();
			int y = getHeight() - 1;

			if (Skins.isGraySkin()) {
				Color c = Color.GRAY;
				g.setColor(c);
				g.drawLine(0, y, width, y);
			} else {
				Color c = UIManager.getColor("Panel.background");
				if (c == null) {
					c = getBackground();
				}
				ESL esl = new ESL(c);
				esl.brighter(50);
				c = esl.toColor();
				g.setColor(c);
				g.drawLine(0, y, width, y);
			}
			g.setColor(old);
		}
	}
	
	/**
	 * 顶部日志按纽
	 * @return
	 */
	private JPanel createNorth() {
		// 按纽
		String text = UIManager.getString("LogFrame.ForbidText"); 
		FontKit.setButtonText(cmdForbid, text);
		cmdForbid.addActionListener(this);
		cmdForbid.setRolloverEnabled(true);
		
		forbid = RTKit.readBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Forbid");
		cmdForbid.setSelected(forbid);

		// 面板
		TitlePanel panel = new TitlePanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		panel.add(cmdForbid, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 日志记录
	 * @return
	 */
	private JScrollPane createCenter(){
		renderer = new RayLogCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);

		// 取出XML中的参数
		String tooltip = UIManager.getString("LogFrame.PanelTooltip"); // findCaption("Window/LogPanel/title");		
		// 行高度
		String value = UIManager.getString("LogFrame.RowHeight"); // findCaption("Window/LogPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);

		list.setFixedCellHeight(rowHeight);
		FontKit.setToolTipText(list, tooltip);
		list.setBorder(new EmptyBorder(2, 1, 1, 1));
		// 支持多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// 修正字体
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Font");
		addThread(new FontThread(font));
		
		// 最大日志数目
		int max = RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Logs");
		if (max > 0) {
			setMaxItems(max);
		}

		JScrollPane jsp = new JScrollPane(list);
		jsp.setBorder(new HighlightBorder(0));
		jsp.putClientProperty("NotBorder", Boolean.TRUE);
		
//		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
//		jsp.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		FontKit.setToolTipText(jsp, tooltip);

		return jsp;
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 初始化菜单
		initMenu();

		// 显示面板
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(createNorth(), BorderLayout.NORTH);
		panel.add(createCenter(), BorderLayout.CENTER);

		rockMenu.setInvoker(list);
		// 面板增加鼠标事件
		list.addMouseListener(new LogMouseAdapter());

		// 窗口布局!
		setLayout(new BorderLayout(0, 0));
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * 返回选择的字体
	 * @return
	 */
	public Font getSelectFont() {
		return list.getFont();
	}

	/**
	 * 设置选择的字体
	 * @param font
	 */
	public void setSelectFont(Font font) {
		addThread(new FontThread(font));
	}

	/**
	 * 推送日志进SWING队列线程
	 *
	 * @author scott.liang
	 * @version 1.0 9/17/2019
	 * @since laxcus 1.0
	 */
	class PushLogThread extends SwingEvent {
		java.util.List<LogItem> logs;

		protected void finalize() {
			if (logs != null) {
				logs = null;
			}
		}

		PushLogThread(java.util.List<LogItem> a) {
			super(true); // 同步处理
			logs = a;
		}

		public void process() {
			printLogs(logs);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.desktop.pool.LogTransmitter#pushLogs(java.util.List)
	 */
	@Override
	public void pushLogs(java.util.List<LogItem> logs) {
		addThread(new PushLogThread(logs));
	}

	/**
	 * 修正字体
	 * @param font
	 */
	private void exchangeFont(Font font) {
		if (font != null) {
			list.setFont(font);
//			cmdForbid.setFont(font);
		}
	}

	/**
	 * 字体线程
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
			exchangeFont(font);
		}
	}


	class ClearThread extends SwingEvent {
		ClearThread() {
			super();
		}

		public void process() {
			lock.lock();
			try {
				model.clear();
//				list.removeAll();
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * 清除全部日志
	 */
	private void clear() {
		addThread(new ClearThread());
	}

	/**
	 * 在图形窗口显示日志
	 * @param logs 日志集合
	 */
	private void __printLogs(java.util.List<LogItem> logs) {
		final int modelSize = model.size();

		// 如果屏蔽日志，或者日志数目小于1时，清除旧记录，新是日志忽略！
		if (forbid || maxItems < 1) {
			if (modelSize > 0) {
				// 清除记录
				model.clear();
//				list.removeAll();
			}
			return;
		}

		int deleteTo = 0;
		int addFrom = 0;
		int addTo = 0;
		// 判断:
		// 1. 传入日志超出最大范围
		// 2. 当前模型中的日志和传入日志超出范围
		// 3. 在范围内
		if (logs.size() >= maxItems) {
			deleteTo = modelSize;
			addFrom = logs.size() - maxItems;
			addTo = logs.size();
		} else if (modelSize + logs.size() >= maxItems) {
			addFrom = 0;
			addTo = logs.size();
			int save = maxItems - (addTo - addFrom);
			if (save >= modelSize) {
				deleteTo = modelSize;
			} else {
				deleteTo = modelSize - save;
			}
		} else {
			deleteTo = 0;
			addFrom = 0;
			addTo = logs.size();
		}

		// 清除显示
		if (deleteTo > 0) {
			model.removeRange(0, deleteTo - 1);
		}
		// 增加新的日志
		for (int i = addFrom; i < addTo; i++) {
			model.addElement(logs.get(i));
		}
	}

	/**
	 * 在图形窗口显示日志
	 * @param logs 日志集合
	 */
	private void printLogs(java.util.List<LogItem> logs) {
		lock.lock();
		try {
			__printLogs(logs);
		} catch (Throwable e) {

		} finally {
			lock.unlock();
		}	
	}

//	/**
//	 * 更新菜单UI界面
//	 * @param root 根
//	 */
//	private void updateMenuUI(JComponent root) {
//		// 更新UI界面
//		Component[] components = root.getComponents();
//		int size = (components == null ? 0 : components.length);
//		for (int index = 0; index < size; index++) {
//			Component component = components[index];
//			if (component == null) {
//				continue;
//			}
//			// 判断是组件
//			if (Laxkit.isClassFrom(component, JComponent.class)) {
//				((JComponent) component).updateUI();
//				updateMenuUI((JComponent) component);
//			}
//		}
//		root.updateUI();
//	}
	
	private void updateComponentUI() {
		if (renderer != null) {
			renderer.updateUI();
		}
		if (rockMenu != null) {
			// 同时修改字体和UI。注意，只更新菜单本身！
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}

		// 修改提示字体和提示
		FontKit.setDefaultFont(cmdForbid);
		FontKit.updateToolTipText(cmdForbid);
		FontKit.updateToolTipText(list);
	}
	
	class UpdateUIThread extends SwingEvent {

		UpdateUIThread() {
			super();
		}

		public void process() {
			updateComponentUI();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		if (rockMenu != null) {
			addThread(new UpdateUIThread());
		}
	}

}