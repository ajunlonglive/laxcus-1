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

import com.laxcus.command.*;
import com.laxcus.command.cloud.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.contact.*;
import com.laxcus.command.establish.*;
import com.laxcus.front.terminal.*;
import com.laxcus.front.terminal.dialog.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.guide.*;
import com.laxcus.task.guide.parameter.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.naming.*;

/**
 * 云端应用软件列表窗口。显示基于阶段命名的云端应用。
 * 因为SWING是线程不安全的，数据的写/读操作被放入SWING事件队列中执行。
 * 
 * @author scott.liang
 * @version 1.0 7/26/2020
 * @since laxcus 1.0
 */
public class TerminalRemoteSoftwareListPanel extends TerminalPanel implements ActionListener {

	private static final long serialVersionUID = 570252287832258830L;

	private TerminalRemoteSoftwarePanel master;

	/**
	 * 设置父类面板
	 * @param e
	 */
	public void setParentPanel(TerminalRemoteSoftwarePanel e) {
		master = e;
	}

	/**
	 * 返回父类面板
	 * @return
	 */
	public TerminalRemoteSoftwarePanel getParentPanel() {
		return master;
	}

	/** 阶段命名配置 **/
	private TreeSet<Phase> array = new TreeSet<Phase>();

	/** 渲染器 **/
	private TerminalSoftwareCellRenderer renderer;

	/** 日志列表 **/
	private JList list = new JList();

	/** 日志模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 弹出菜单 **/
	private JPopupMenu menu = new JPopupMenu();

	private JMenuItem menuRun;
	private JMenuItem menuName;
	private JMenuItem menuIcon;
	private JMenuItem menuDelete;
	private JMenuItem menuRefresh;
	private JMenuItem menuAbout;

	/**
	 * 构造默认的用户网络资源浏览面板
	 */
	public TerminalRemoteSoftwareListPanel() {
		super();
	}

	//	private void test() {
	//		String[] s = new String[] {
	//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/AXIBIT.基准排序测试",
	//				"ISSUE:网络蚂蚁.挖掘机.NOT_IN",
	//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/追踪.MINING",
	//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/大雅时代.3D渲染",
	//				"TO:{8950ABFDA7B727630760DD35BCF5C3DAA7631AFF223A90F7728C0D2521DDE10C}/系统应用.DEMO_SORT",
	//				"SCAN:空气流体.3D计算.GROUPBY",
	//				"DISTANT:核动机.电磁弹射"};
	//		for (String input : s) {
	//			Phase e = new Phase(input);
	//			addPhase(e);
	//		}
	//	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		addThread(new MenuThread(event));
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
			int index = list.getSelectedIndex();
			if (index < 0 || index >= model.size()) {
				return;
			}
			Object value = model.elementAt(index);
			if (value.getClass() != SoftwareItem.class) {
				return;
			}

			if (event.getSource() == menuRun) {
				doubleClick((SoftwareItem) value);
			} else if (event.getSource() == menuName) {
				modifyName(index, (SoftwareItem) value);
			} else if (event.getSource() == menuIcon) {
				modifyIcon(index, (SoftwareItem) value);
			} else if (event.getSource() == menuDelete) {
				dropWare(index, (SoftwareItem) value);
			} else if (event.getSource() == menuRefresh) {
				refreshWare(index, (SoftwareItem) value);
			} else if (event.getSource() == menuAbout) {
				showWare(index, (SoftwareItem) value);
			}
		}
	}


	/**
	 * 显示软件的信息
	 * @param index
	 * @param item
	 */
	private void showWare(int index, SoftwareItem item) {
		Phase phase = item.getPhase();
		Sock sock = phase.getSock();
		WareTag tag = GuideTaskPool.getInstance().findWare(sock);

		// 显示信息
		if (tag == null) {
			showWareTagNotFound(sock);
		} else {
			TerminalWareTagDialog dialog = new TerminalWareTagDialog(getWindow(), true);
			dialog.setWare(tag, sock);
			dialog.showDialog();
		}
	}

	/**
	 * 修改名称
	 * @param index
	 * @param item
	 */
	private void modifyName(int index, SoftwareItem item) {
		// 找到自定义名称
		Phase phase = item.getPhase();
		Sock sock = phase.getSock();
		String text = GuideTaskPool.getInstance().readCaption(sock);

		TerminalExchangeNameDialog dialog = new TerminalExchangeNameDialog(getWindow(), true);
		dialog.setNewName(text);
		dialog.showDialog();

		// 取出新的名称
		text = dialog.getNewName();
		if(text != null) {
			// 保存标题
			GuideTaskPool.getInstance().setCaption(sock, text);

			// 生成副本
			SoftwareItem clone = item.duplicate();
			clone.setTitle(text);
			// 修改...
			model.set(index, clone);
		}
	}

	/**
	 * 修改图标
	 * @param index
	 * @param item
	 */
	private void modifyIcon(int index, SoftwareItem item) {
		Phase phase = item.getPhase();
		Sock sock = phase.getSock();
		byte[] stream = GuideTaskPool.getInstance().readIcon(sock);

		TerminalExchangeIconDialog dialog = new TerminalExchangeIconDialog(getWindow(), true);
		dialog.setStream(stream);
		dialog.showDialog();

		// 取出新的名称
		stream = dialog.getStream();
		if(stream != null) {
			// 保存标题
			GuideTaskPool.getInstance().setIcon(sock, stream);

			// 生成副本
			SoftwareItem clone = item.duplicate();
			clone.setIcon(createIcon(stream));
			// 修改...
			model.set(index, clone);
		}
	}

	/**
	 * 刷新软件
	 * @param index
	 * @param item
	 */
	private void refreshWare(int index, SoftwareItem item) {
		Phase phase = item.getPhase();
		Sock sock = phase.getSock();
		byte[] stream = GuideTaskPool.getInstance().readIcon(sock);
		String text = GuideTaskPool.getInstance().readCaption(sock);

		// 生成副本
		boolean success = (stream != null && text != null);
		if (success) {
			SoftwareItem clone = item.duplicate();
			clone.setIcon(createIcon(stream));
			clone.setTitle(text);
			// 修改...
			model.set(index, clone);
		} else {
			// 弹出，本地参数不足！
			String content = findContent("Window/RemoteSoftwareListPanel/error/local-mssing");
			showError(content);
		}
	}

	/**
	 * 删除应用
	 * @param index
	 * @param item
	 */
	private void dropWare(int index, SoftwareItem item) {
		Phase phase = item.getPhase();
		// 忽略
		if (!PhaseTag.isPhase(phase.getFamily())) {
			return;
		}

		// xxx 是必选项，请输入！
		String title = findCaption("Window/RemoteSoftwareListPanel/delete/title");
		String content = findContent("Window/RemoteSoftwareListPanel/delete"); 

		// 弹出对话框
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, null, content, JOptionPane.YES_NO_OPTION);
		// 忽略
		if (who != JOptionPane.YES_OPTION) {
			return;
		}

		// 判断类型，生成命令，删除
		if (PhaseTag.isConduct(phase.getFamily())) {
			String input = String.format("DROP CONDUCT PACKAGE %s", phase.getWare());
			getWindow().getImplementPanel().doDropConductPackage(input, true);
		} else if (PhaseTag.isEstablish(phase.getFamily())) {
			String input = String.format("DROP ESTABLISH PACKAGE %s", phase.getWare());
			getWindow().getImplementPanel().doDropEstablishPackage(input, true);
		} else if (PhaseTag.isContact(phase.getFamily())) {
			String input = String.format("DROP CONTACT PACKAGE %s", phase.getWare());
			getWindow().getImplementPanel().doDropContactPackage(input, true);
		}
	}

	class FoucsItem {
		int index;

		SoftwareItem item;

		FoucsItem(int i, SoftwareItem e) {
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
		if (value.getClass() == SoftwareItem.class) {
			return new FoucsItem(index, (SoftwareItem) value);
		}
		return null;
	}

	class WareKeyApater extends KeyAdapter {

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_R) {
				FoucsItem value = getSelectItem();
				if (value != null) {
					doubleClick(value.item);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_N) {
				FoucsItem value = getSelectItem();
				if (value != null) {
					modifyName(value.index, value.item);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_I) {
				FoucsItem value = getSelectItem();
				if (value != null) {
					modifyIcon(value.index, value.item);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_P) {
				FoucsItem value = getSelectItem();
				if (value != null) {
					showWare(value.index, value.item);
				}
			}
		}
	}

	/**
	 * 设置鼠标有效或者无效
	 * @param b 有效
	 */
	private void setMenuEnabled(boolean b) {
		menuRun.setEnabled(b);

		menuName.setEnabled(b);
		menuIcon.setEnabled(b);

		menuDelete.setEnabled(b);
		menuRefresh.setEnabled(b);
		menuAbout.setEnabled(b);
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
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			showPopupMenu(e);
			super.mousePressed(e);
		}

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			showPopupMenu(e);
			super.mouseReleased(e);
		}

		//		/*
		//		 * (non-Javadoc)
		//		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		//		 */
		//		@Override
		//		public void mouseReleased(MouseEvent e) {
		//			// 事件
		//			boolean success = (e.getButton() == MouseEvent.BUTTON3);
		//			if (!success) {
		//				success = e.isPopupTrigger();
		//			}
		//
		//			if (success) {
		//				int newX = e.getX();
		//				int newY = e.getY();
		//
		//				Component invoker = menu.getInvoker();
		//				if (invoker.getClass() == JScrollPane.class) {
		//					JScrollPane pane = (JScrollPane) invoker;
		//					JViewport port = pane.getViewport();
		//					Point pt = port.getViewPosition();
		//					//	System.out.printf("view:%d %d, mouse:%d %d\n", pt.x, pt.y, newX, newY);
		//
		//					// 调整坐标
		//					if (pt.x > 0) {
		//						newX = newX - pt.x;
		//						if (newX < 0) newX = 0;
		//					}
		//					if (pt.y > 0) {
		//						newY = newY - pt.y;
		//						if (newY < 0) newY = 0;
		//					}
		//				}
		//
		//				// 菜单有效或者无效
		//				FoucsItem value = getSelectItem();
		//				setMenuEnabled(value != null);
		//
		//				// 显示菜单
		//				menu.show(invoker, newX, newY);
		//			}
		//		}
	}

	private void showPopupMenu(MouseEvent e) {
		//		// 事件
		//		boolean success = (e.getButton() == MouseEvent.BUTTON3);
		//		if (!success) {
		//			success = e.isPopupTrigger();
		//		}

		boolean success = e.isPopupTrigger();

		if (!success) {
			return;
		}
		int newX = e.getX();
		int newY = e.getY();

		Component invoker = menu.getInvoker();
		if (invoker.getClass() == JScrollPane.class) {
			JScrollPane pane = (JScrollPane) invoker;
			JViewport port = pane.getViewport();
			Point pt = port.getViewPosition();
			//	System.out.printf("view:%d %d, mouse:%d %d\n", pt.x, pt.y, newX, newY);

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
		if (value.getClass() == SoftwareItem.class) {
			singleClick((SoftwareItem) value);
		}
	}

	/**
	 * 单击鼠标
	 * @param item
	 */
	private void singleClick(SoftwareItem item) {
		Phase phase = item.getPhase();
		getParentPanel().getDetailPanel().exchange(phase);
	}

	/**
	 * 显示错误
	 * @param content
	 */
	private void showError(String content) {
		TerminalWindow window =	getWindow();
		String title = findCaption("Window/RemoteSoftwareListPanel/error/title");

		MessageDialog.showMessageBox(window, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 显示没有找到
	 * @param sock
	 */
	private void showWareTagNotFound(Sock sock) {
		String text = GuideTaskPool.getInstance().readCaption(sock);
		if (text == null) {
			text = sock.toString();
		}
		String content = findContent("Window/RemoteSoftwareListPanel/error/notfound-waretag");
		content = String.format(content, text);

		showError(content);
	}

	/**
	 * 没有找到启动程序
	 * @param sock 根命名
	 */
	private void showTaskNotFound(Sock sock) {		
		String text = GuideTaskPool.getInstance().readCaption(sock);
		if (text == null) {
			text = sock.toString();
		}

		String content = findContent("Window/RemoteSoftwareListPanel/error/notfound-task");
		content = String.format(content, text);

		showError(content);
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
		if (value.getClass() == SoftwareItem.class) {
			doubleClick((SoftwareItem) value);
		}
	}

	/**
	 * 弹出启动失败
	 * @param sock
	 */
	private void showStartupFailed(Sock sock) {
		String content = findContent("Window/RemoteSoftwareListPanel/error/startup-failed");
		String title = GuideTaskPool.getInstance().readCaption(sock);
		if (title == null) {
			title = sock.toString();
		}
		content = String.format(content, title);
		showError(content);
	}

	/**
	 * 弹出异常
	 */
	private void showThrowable() {
		String content = findContent("Window/RemoteSoftwareListPanel/error/throwable");
		showError(content);
	}

	/**
	 * 双击事件
	 * @param value
	 */
	private void doubleClick(SoftwareItem item) {
		Phase phase = item.getPhase();
		Sock sock = phase.getSock();
		runTask(sock);
	}

	/**
	 * 执行启动分布式应用
	 * @param sock 基础字
	 */
	private void runTask(Sock sock) {
		// 产生任务实例
		GuideTask task = GuideTaskPool.getInstance().createTask(sock);
		if (task == null) {
			showTaskNotFound(sock);
			return;
		}

		InputParameterList list = null;
		try {
			list = task.markup(sock);
		} catch (GuideTaskException e) {
			Logger.error(e);
			showError(e.getMessage()); // 弹出错误
			return;
		} catch (Throwable e) {
			Logger.fatal(e);
			showThrowable(); // 弹出错误!
			return;
		}

		// 判断有效，修改参数！
		if (list != null) {
			String caption = GuideTaskPool.getInstance().readCaption(sock);

			TerminalGuideParameterDialog dialog = new TerminalGuideParameterDialog(getWindow(), true);
			int ret = dialog.showDialog(caption, list);
			// 忽略退出
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}

		// 产生分布错误
		DistributedCommand command = null;
		try {
			command = task.create(sock, list);
		} catch (GuideTaskException e) {
			Logger.error(e);
			showError(e.getMessage()); // 弹出错误
			return;
		} catch (Throwable e) {
			Logger.fatal(e);
			showThrowable(); // 弹出错误
			return;
		}

		TerminalRightPanel panel = getWindow().getImplementPanel();
		boolean success = false;
		// 判断命令
		if (command.getClass() == Conduct.class) {
			success = panel.runConduct((Conduct) command);
		} else if (command.getClass() == Contact.class) {
			success = panel.runContact((Contact) command);
		} else if (command.getClass() == Establish.class) {
			success = panel.runEstablish((Establish) command);
		}

		// 以上不成功，弹出错误
		if (!success) {
			showStartupFailed(sock);
		}
	}

	/**
	 * 初始化菜单
	 */
	private void initMenu() {
		// 运行菜单
		String text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/run/title");
		menuRun = new JMenuItem(text);
		menuRun.setMnemonic('R');
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/run/tooltip");
		menuRun.setToolTipText(text);
		menuRun.addActionListener(this);

		// 修改名称菜单
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/name/title");
		menuName = new JMenuItem(text);
		menuName.setMnemonic('N');
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/name/tooltip");
		menuName.setToolTipText(text);
		menuName.addActionListener(this);

		// 修改图标菜单
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/icon/title");
		menuIcon = new JMenuItem(text);
		menuIcon.setMnemonic('I');
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/icon/tooltip");
		menuIcon.setToolTipText(text);
		menuIcon.addActionListener(this);

		// 删除应用
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/delete/title");
		menuDelete = new JMenuItem(text);
		menuDelete.setMnemonic('I');
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/delete/tooltip");
		menuDelete.setToolTipText(text);
		menuDelete.addActionListener(this);

		// 刷新应用
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/refresh/title");
		menuRefresh = new JMenuItem(text);
		menuRefresh.setMnemonic('E');
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/refresh/tooltip");
		menuRefresh.setToolTipText(text);
		menuRefresh.addActionListener(this);

		// 软件属性
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/about/title");
		menuAbout = new JMenuItem(text);
		menuAbout.setMnemonic('P');
		text = findCaption("Window/RemoteSoftwareListPanel/popup-menu/about/tooltip");
		menuAbout.setToolTipText(text);
		menuAbout.addActionListener(this);

		menu.add(menuRun);
		menu.addSeparator();
		menu.add(menuName);
		menu.add(menuIcon);
		menu.addSeparator();
		menu.add(menuDelete);
		menu.addSeparator();
		menu.add(menuRefresh);
		menu.addSeparator();
		menu.add(menuAbout);

		// 鼠标事件
		menu.addMouseListener(new WareMouseAdapter());
	}

	/**
	 * 初始化
	 */
	public void init() {
		initMenu();

		renderer = new TerminalSoftwareCellRenderer();
		list.setCellRenderer(renderer); 
		list.setModel(model);

		String tooltip = findCaption("Window/RemoteSoftwareListPanel/title");
		FontKit.setToolTipText(list, tooltip);

		// 行高度
		String value = findCaption("Window/RemoteSoftwareListPanel/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 40);

		list.setFixedCellHeight(rowHeight);
		list.setBorder(new EmptyBorder(3, 3, 2, 3)); // top, left, bottom, right
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选
		// 截获鼠标事件
		list.addMouseListener(new WareMouseAdapter());
		list.addKeyListener(new WareKeyApater());


		//		list.addMouseListener(  new MouseAdapter() {
		//			public void mouseClicked(MouseEvent e) {
		//				int index = list.getSelectedIndex();
		//				System.out.printf("fuck click! index %d, click count:%d\n", index, e.getClickCount());
		//				
		//				// 选中且双击...
		//				if (index != -1) {
		//					if (e.getClickCount() == 2) {
		//						doubleClick(list.getSelectedValue());
		//					}
		//				}
		//			}
		//		});

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
		ImageIcon icon = new ImageIcon(stream);
		Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}

	/**
	 * 在阶段命名根节点基础上，建立子级的阶段命名
	 * @param phase Phase实例
	 */
	private void createPhase(Phase phase) {
		if (array.contains(phase)) {
			return;
		}

		// 保存和显示一行软件
		SoftwareItem item = new SoftwareItem(phase);
		Sock sock = phase.getSock();
		// 找到软件名称
		String text = GuideTaskPool.getInstance().readCaption(sock);
		item.setTitle(text);
		// 找到工具提示
		text = GuideTaskPool.getInstance().readTooltip(sock);
		item.setTooltip(text);
		// 找到适配的图标
		byte[] stream = GuideTaskPool.getInstance().readIcon(sock);
		if (stream != null) {
			item.setIcon(createIcon(stream));
		}

		// 显示
		model.addElement(item);
		// 保存
		array.add(phase);
	}

	/**
	 * 
	 *
	 * @author scott.liang
	 * @version 1.0 2020-7-24
	 * @since laxcus 1.0
	 */
	class CreateSoftwareThread extends SwingEvent {
		Phase phase;

		CreateSoftwareThread(Phase e) {
			super();
			phase = e;
		}

		public void process() {
			createPhase(phase);
		}
	}

	public void addPhase(Phase phase) {
		CreateSoftwareThread e = new CreateSoftwareThread(phase);
		addThread(e);
	}

	/**
	 * 删除任务组件
	 * @param phase Phase实例
	 */
	private void dropPhase(Phase phase) {
		// 不存在，退出
		if (!array.contains(phase)) {
			return;
		}

		boolean success = false;
		int count = model.size();
		for (int index = 0; index < count; index++) {
			Object element = model.elementAt(index);
			if (element != null && element.getClass() == SoftwareItem.class) {
				SoftwareItem item = (SoftwareItem) element;
				boolean match = (Laxkit.compareTo(phase, item.getPhase()) == 0);
				if (match) {
					// 删除下标位置对象
					model.removeElementAt(index);
					// 删除内存中的记录
					array.remove(phase);
					success = true;
					break;
				}
			}
		}

		// 匹配，清除
		if (success) {
			TerminalRemoteSoftwareDetailPanel panel = master.getDetailPanel();
			boolean match = panel.isAttachObject(phase);
			if (match) {
				panel.clear();
			}
		}
	}

	class DropElementThread extends SwingEvent {
		Phase element;

		DropElementThread(Phase e) {
			super();
			element = e;
		}

		public void process() {
			dropPhase(element);
		}
	}

	/**
	 * 把阶段命名放入SWING事件队列，删除窗口上的阶段命名
	 * @param phase Phase实例
	 */
	public void removePhase(Phase phase) {
		DropElementThread e = new DropElementThread(phase);
		addThread(e);
	}


	class ClearThread extends SwingEvent {
		ClearThread(){super();}
		public void process() {
			model.clear();
			list.removeAll();
			// 清除阶段命名对象
			array.clear();
		}
	}

	/**
	 * 清除全部日志
	 */
	public void clear() {
		addThread(new ClearThread());
	}

	class RunTaskThread extends SwingEvent {
		RunTask cmd;

		RunTaskThread(RunTask e) {
			super();
			cmd = e;
		}

		public void process() {
			runTask(cmd.getSock());
		}
	}

	/**
	 * 将转发命令交给线程处理
	 * @param cmd 命令实例
	 */
	public void shift(RunTask cmd) {
		addThread(new RunTaskThread(cmd));
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