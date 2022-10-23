/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.terminal.*;
import com.laxcus.front.terminal.dialog.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 边缘应用软件列表窗口
 * 
 * @author scott.liang
 * @version 1.0 9/25/2020
 * @since laxcus 1.0
 */
public class TerminalTubSoftwareListPanel extends TerminalPanel implements ActionListener {

	private static final long serialVersionUID = -5415977376953696499L;

	/** 父类实例 **/
	private TerminalLocalTubPanel parent;

	/**
	 * 设置父类面板
	 * @param e
	 */
	public void setParentPanel(TerminalLocalTubPanel e) {
		parent = e;
	}

	/**
	 * 返回父类面板
	 * @return
	 */
	public TerminalLocalTubPanel getParentPanel() {
		return parent;
	}
	
	/** 阶段命名配置 **/
	private TreeSet<TubTag> array = new TreeSet<TubTag>();

	/** 渲染器 **/
	private TerminalTubCellRenderer renderer;

	/** 日志列表 **/
	private JList list = new JList();

	/** 日志模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 弹出菜单 **/
	private JPopupMenu menu = new JPopupMenu();
	
	/** 启动运行 / 停止  **/
	private JMenuItem menuStart;
	private JMenuItem menuStop;
	private JMenuItem menuPrint;
	private JMenuItem menuShow;
	
	/**
	 * 构造默认的用户本地资源浏览面板
	 */
	public TerminalTubSoftwareListPanel() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new MenuThread(event));
	}

	/**
	 * 菜单
	 *
	 * @author scott.liang
	 * @version 1.0 9/26/2020
	 * @since laxcus 1.0
	 */
	class MenuThread extends SwingEvent {
		ActionEvent event;

		public MenuThread(ActionEvent e) {
			super();
			event = e;
		}

		/*
		 * (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			if (event.getSource() == menuPrint) {
				printTub();
				return;
			} else if (event.getSource() == menuShow) {
				showTub();
				return;
			}
			
			int index = list.getSelectedIndex();
			if (index < 0 || index >= model.size()) {
				return;
			}
			Object value = model.elementAt(index);
			if (value.getClass() != TubItem.class) {
				return;
			}

			if (event.getSource() == menuStart) {
				doubleClick((TubItem) value);
			} else if (event.getSource() == menuStop) {
				stopTub((TubItem) value);
			} 
		}
	}
	
	class FoucsItem {
		int index;

		TubItem item;

		FoucsItem(int i, TubItem e) {
			super();
			index = i;
			item = e;
		}
	}

	/**
	 * 获得焦点单元
	 * @return 返回FocusItem实例，或者空指针
	 */
	private FoucsItem getSelectItem() {
		// 定位下标
		int index = list.getSelectedIndex(); 
		// 小于0或者大于成员数目时，忽略
		if (index < 0 || index >= model.size()) {
			return null;
		}
		// 找到指定位置的成员，处理它！
		Object value = model.elementAt(index);
		if (value.getClass() == TubItem.class) {
			return new FoucsItem(index, (TubItem) value);
		}
		return null;
	}
	
	/**
	 * 单击鼠标
	 * @param e
	 */
	private void singleClick(MouseEvent e) {
		// 定位下标
		int index = list.locationToIndex(e.getPoint());
		// 小于0或者大于成员数目时，忽略
		if (index < 0 || index >= model.size()) {
			return;
		}
		// 找到指定位置的成员，处理它！
		Object value = model.elementAt(index);
		if (value.getClass() == TubItem.class) {
			singleClick((TubItem) value);
		}
	}

	/**
	 * 单击鼠标
	 * @param item
	 */
	private void singleClick(TubItem item) {
		TubTag tag = item.getTubTag();
		getParentPanel().getDetailPanel().exchange(tag);
	}
	
	/**
	 * 双击事件
	 * @param e 鼠标双击事件
	 */
	private void doubleClick(MouseEvent e) {
		// 定位下标
		int index = list.locationToIndex(e.getPoint());
		// 小于0或者大于成员数目时，忽略
		if (index < 0 || index >= model.size()) {
			return;
		}
		// 找到指定位置的成员，处理它！
		Object value = model.elementAt(index);
		if (value.getClass() == TubItem.class) {
			doubleClick((TubItem) value);
		}
	}
	
	/**
	 * 双击事件
	 * @param value
	 */
	private void doubleClick(TubItem item) {
		TubTag tag = item.getTubTag();
		runTub(tag);
	}
	
	/**
	 * 运行任务
	 * @param tag
	 */
	private void runTub(TubTag tag) {
		TerminalRunTubDialog dialog = new TerminalRunTubDialog(getWindow(), true);
		int ret = dialog.showDialog(tag);
		// 忽略退出
		if (ret != JOptionPane.YES_OPTION) {
			return;
		}

		// 启动运行
		String cmd = String.format("RUN TUB SERVICE %s", tag.getNaming());
		
		String params = dialog.getParams();
		if (params != null && params.trim().length() > 0) {
			cmd = String.format("%s %s", cmd, params);
		}

		TerminalRightPanel panel = getWindow().getImplementPanel();
		panel.doRunTubService(cmd);
	}

	/**
	 * 停止任务
	 * @param item
	 */
	private void stopTub(TubItem item) {
		TubTag tag = item.getTubTag();
		
		TerminalStopTubDialog dialog = new TerminalStopTubDialog(getWindow(), true);
		int ret = dialog.showDialog(tag);
		// 忽略退出
		if (ret != JOptionPane.YES_OPTION) {
			return;
		}

		long pid = dialog.getPID();
		String cmd = String.format("STOP TUB SERVICE %d", pid);
		
		String params = dialog.getParams();
		if (params != null && params.trim().length() > 0) {
			cmd = String.format("%s %s", cmd, params);
		}
		TerminalRightPanel panel = getWindow().getImplementPanel();
		panel.doStopTubService(cmd);
	}

	/**
	 * 显示运行中的边缘应用
	 */
	private void printTub() {
		TerminalPrintTubServiceDialog dialog = new TerminalPrintTubServiceDialog(getWindow(), true);
		int ret = dialog.showDialog();
		// 忽略退出
		if (ret != JOptionPane.YES_OPTION) {
			return;
		}
		
		String naming = dialog.getNaming();
		
		String cmd = "PRINT TUB SERVICE";
		if (naming != null) {
			cmd = String.format("%s %s", cmd, naming);
		}
		
		// 打印运行中的应用
		TerminalRightPanel panel = getWindow().getImplementPanel();
		panel.doPrintTubService(cmd);
	}

	/**
	 * 显示全部边缘应用，无论是否运行
	 */
	private void showTub() {
		TerminalShowTubContainerDialog dialog = new TerminalShowTubContainerDialog(getWindow(), true);
		int ret = dialog.showDialog();
		// 忽略退出
		if (ret != JOptionPane.YES_OPTION) {
			return;
		}
		
		String naming = dialog.getNaming();
		
		String cmd = "SHOW TUB CONTAINER";
		if (naming != null) {
			cmd = String.format("%s %s", cmd, naming);
		}
		
		// 打印运行中的应用
		TerminalRightPanel panel = getWindow().getImplementPanel();
		panel.doShowTubContainer(cmd);
	}
	
	/**
	 * 设置鼠标有效或者无效
	 * @param b 有效
	 */
	private void setMenuEnabled(boolean b) {
		menuStart.setEnabled(b);
		menuStop.setEnabled(b);
	}

	
	class WareMouseAdapter extends MouseAdapter {

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			//			System.out.printf("mouse click: %d %d: click count:%d\n", e.getX(), e.getY(), e.getClickCount());

			int index = list.getSelectedIndex();

			// 选中且双击...
			if (index != -1) {
				int clicks = e.getClickCount();
				if (clicks == 1) {
					singleClick(e);
				} else if (clicks == 2) {
					doubleClick(e);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			// 事件
			boolean success = (e.getButton() == MouseEvent.BUTTON3);
			if (!success) {
				success = e.isPopupTrigger();
			}

			if (success) {
				int newX = e.getX();
				int newY = e.getY();

				Component invoker = menu.getInvoker();
				if (invoker.getClass() == JScrollPane.class) {
					JScrollPane pane = (JScrollPane) invoker;
					JViewport port = pane.getViewport();
					Point pt = port.getViewPosition();
					// 调整坐标
					if (pt.x > 0) {
						newX = newX - pt.x;
						if (newX < 0) newX = 0;
					}
					if (pt.y > 0) {
						newY = newY - pt.y;
						if (newY < 0) newY = 0;
					}
				}

				// 菜单有效或者无效
				FoucsItem value = getSelectItem();
				setMenuEnabled(value != null);

				// 显示菜单
				menu.show(invoker, newX, newY);
			}
		}
	}
	
	class WareKeyApater extends KeyAdapter {

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_R) {
				FoucsItem value = getSelectItem();
				if (value != null) {
					doubleClick(value.item);
				}
			}

			//			else if(e.getKeyCode() == KeyEvent.VK_S) {
			//				FoucsItem value = getSelectItem();
			//				if (value != null) {
			//					modifyName(value.index, value.item);
			//				}
			//			} 
			//			else if(e.getKeyCode() == KeyEvent.VK_I) {
			//				FoucsItem value = getSelectItem();
			//				if (value != null) {
			//					modifyIcon(value.index, value.item);
			//				}
			//			} else if(e.getKeyCode() == KeyEvent.VK_P) {
			//				FoucsItem value = getSelectItem();
			//				if (value != null) {
			//					showWare(value.index, value.item);
			//				}
			//			}
		}
	}

	
	/**
	 * 初始化菜单
	 */
	private void initMenu() {
		// 运行边缘应用
		String text = findCaption("Window/LocalTubListPanel/popup-menu/run/title");
		menuStart = new JMenuItem(text);
		menuStart.setMnemonic('R');
		text = findCaption("Window/LocalTubListPanel/popup-menu/run/tooltip");
		menuStart.setToolTipText(text);
		menuStart.addActionListener(this);

		// 停止边缘应用
		text = findCaption("Window/LocalTubListPanel/popup-menu/stop/title");
		menuStop = new JMenuItem(text);
		menuStop.setMnemonic('N');
		text = findCaption("Window/LocalTubListPanel/popup-menu/stop/tooltip");
		menuStop.setToolTipText(text);
		menuStop.addActionListener(this);
		
		// 打印处于运行状态的边缘应用
		text = findCaption("Window/LocalTubListPanel/popup-menu/print/title");
		menuPrint = new JMenuItem(text);
		menuPrint.setMnemonic('I');
		text = findCaption("Window/LocalTubListPanel/popup-menu/print/tooltip");
		menuPrint.setToolTipText(text);
		menuPrint.addActionListener(this);

		// 显示已经存在的边缘应用
		text = findCaption("Window/LocalTubListPanel/popup-menu/show/title");
		menuShow = new JMenuItem(text);
		menuShow.setMnemonic('I');
		text = findCaption("Window/LocalTubListPanel/popup-menu/show/tooltip");
		menuShow.setToolTipText(text);
		menuShow.addActionListener(this);
		
		menu.add(menuStart);
		menu.add(menuStop);
		menu.add(menuPrint);
		menu.addSeparator();
		menu.add(menuShow);
		
		// 鼠标事件
		menu.addMouseListener(new WareMouseAdapter());
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		initMenu();

		renderer = new TerminalTubCellRenderer();
		list.setCellRenderer(renderer); 
		list.setModel(model);

		String tooltip = findCaption("Window/LocalTubListPanel/title");
		FontKit.setToolTipText(list, tooltip);

		// 行高度
		String value = findCaption("Window/LocalTubListPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 40);

		list.setFixedCellHeight(rowHeight);
		list.setBorder(new EmptyBorder(3, 3, 2, 3)); // top, left, bottom, right
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选
		// 截获鼠标事件
		list.addMouseListener(new WareMouseAdapter());
		list.addKeyListener(new WareKeyApater());

		// 修正字体
		__exchangeFont(TerminalProperties.readRemoteSoftwareFont());

		JScrollPane scroll = new JScrollPane(list);
		FontKit.setToolTipText(scroll, tooltip);

		// 设置调用者
		menu.setInvoker(scroll);

		// 本地布局
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);

		//		// 测试显示
		//		test();
		////		System.out.println("list fuck!");
	}
	
	/**
	 * 根据输入的数据流，生成一个32*32的图标
	 * @param stream 图像数据流
	 * @return 返回图标实例
	 */
	private ImageIcon createIcon(byte[] stream) {
		if(stream == null || stream.length ==0) {
			return null;
		}
		ImageIcon icon = new ImageIcon(stream);
		Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}

	/**
	 * 在阶段命名根节点基础上，建立子级的阶段命名
	 * @param tag TubTag实例
	 */
	private void createTubTag(TubTag tag) {
		if (array.contains(tag)) {
			return;
		}

		// 保存和显示一行软件
		TubItem item = new TubItem(tag);
		// 生成图标
		item.setIcon(createIcon(tag.getIcon()));

		// 显示
		model.addElement(item);
		// 保存
		array.add(tag);
	}

	/**
	 * 删除任务组件
	 * @param tag TubTag实例
	 */
	private void dropTubTag(TubTag tag) {
		// 不存在，退出
		if (!array.contains(tag)) {
			return;
		}
		
		boolean success = false;
		int count = model.size();
		for (int index = 0; index < count; index++) {
			Object element = model.elementAt(index);
			if (element != null && element.getClass() == TubItem.class) {
				TubItem item = (TubItem) element;
				boolean match = (Laxkit.compareTo(tag, item.getTubTag()) == 0);
				if (match) {
					// 删除下标位置对象
					model.removeElementAt(index);
					// 删除内存中的记录
					array.remove(tag);
					success = true;
					break;
				}
			}
		}
		
		// 匹配，清除
		if (success) {
			TerminalLocalTubDetailPanel panel = parent.getDetailPanel();
			boolean match = panel.isAttachObject(tag);
			if (match) {
				panel.clear();
			}
		}
		

//		// 找到阶段命名根节点
//		TerminalTreeTubTagRootNode tagRoot = getTubTagRootNode();
//		if (tagRoot == null) {
//			return;
//		}
//
//		int count = tagRoot.getChildCount();
//		for (int index = 0; index < count; index++) {
//			TreeNode node = tagRoot.getChildAt(index);
//			// 必须匹配
//			if (node.getClass() != TerminalTreeTubTagNode.class) {
//				continue;
//			}
//			TerminalTreeTubTagNode child = (TerminalTreeTubTagNode) node;
//			if (child.getTubTag().compareTo(tag) == 0) {
//				// 删除树节点
//				tagRoot.remove(child);
//				// 更新图形界面
//				model.nodesWereRemoved(tagRoot, new int[] { index }, new Object[] { child });
//				break;
//			}
//		}
//
//		// 如果是空集合，这个节点
//		if (tagRoot.getChildCount() == 0) {
//			model.removeNodeFromParent(tagRoot);
//		} else {
//			model.reload(tagRoot);
//		}
//
//		// 删除阶段命名
//		tags.remove(tag);
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	class ClearThread extends SwingEvent {
		ClearThread() { super(); }
		public void process() {
			model.clear();
			list.removeAll();
			// 清除阶段命名对象
			array.clear();
		}
	}
	
	/**
	 * 清除全部
	 */
	public void clear() {
		addThread(new ClearThread());
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
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
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
	
	class CreateElementThread extends SwingEvent {
		Object element;

		CreateElementThread(Object e) {
			super();
			element = e;
		}

		public void process() {
			if (element.getClass() == TubTag.class) {
				createTubTag((TubTag) element);
			}

		}
	}
	
	class DropElementThread extends SwingEvent {
		Object element;

		DropElementThread(Object e) {
			super();
			element = e;
		}

		public void process() {
			if (element.getClass() == TubTag.class) {
				dropTubTag((TubTag) element);
			}
		}
	}
	
	class ClearAllTubTagThread extends SwingEvent {
		ClearAllTubTagThread() { super(); }
		public void process() {
			model.clear();
			// 清除记录
			array.clear();
		}
	}

	/**
	 * 加入边缘标记
	 * @param tag
	 */
	public void addTubTag(TubTag tag) {
		CreateElementThread e = new CreateElementThread(tag);
		addThread(e);
	}
	
	/**
	 * 把阶段命名放入SWING事件队列，删除窗口上的阶段命名
	 * @param tag TubTag实例
	 */
	public void removeTubTag(TubTag tag) {
		DropElementThread e = new DropElementThread(tag);
		addThread(e);
	}

	/**
	 * 删除全部数据
	 */
	public void removeAllTubTag() {
		ClearAllTubTagThread e = new ClearAllTubTagThread();
		addThread(e);
	}
	
	/**
	 * 更新菜单UI界面
	 * @param root 根
	 */
	private void updateMenuUI(JComponent root) {
		// 更新UI界面
		Component[] subs = root.getComponents();
		int size = (subs == null ? 0 : subs.length);
		for (int index = 0; index < size; index++) {
			Component sub = subs[index];
			if (Laxkit.isClassFrom(sub, JComponent.class)) {
				((JComponent) sub).updateUI();
			}
		}
		root.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();

		if (menu != null) {
			updateMenuUI(menu);
		}
	}	
}