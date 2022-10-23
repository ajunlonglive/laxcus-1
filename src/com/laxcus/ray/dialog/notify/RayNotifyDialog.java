/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.notify;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.register.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.display.graph.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;

/**
 * 提示对话框。<br>
 * 来自后台的提示，在这个窗口上显示。
 * 
 * @author scott.liang
 * @version 1.0 9/16/2021
 * @since laxcus 1.0
 */
public class RayNotifyDialog extends LightDialog implements MeetDisplay {

	private static final long serialVersionUID = 1L;

	class History {
		ShowTitle title;

		ArrayList<ShowItem> array = new ArrayList<ShowItem>();

		public History(ShowTitle e, Collection<ShowItem> a) {
			super();
			title = e;
			array.addAll(a);
		}
	}
	
	/** 桌面 **/
	private JDesktopPane desktop;

	/** 固定的窗口句柄 **/
	private static RayNotifyDialog selfHandle;

	/**
	 * 返回实例句柄
	 * @return 句柄
	 */
	public static RayNotifyDialog getInstance(JDesktopPane desktop) {
		if (desktop == null) {
			throw new NullPointerException("DesktopPane is null!");
		}
		// 没有定义，构造函数
		if (RayNotifyDialog.selfHandle == null) {
			RayNotifyDialog.selfHandle = new RayNotifyDialog(desktop);
		}
		return RayNotifyDialog.selfHandle;
	}
	
	/**
	 * 返回窗口句柄
	 * @return 实例
	 */
	public static RayNotifyDialog getInstance() {
		if (RayNotifyDialog.selfHandle == null) {
			throw new NullPointerException("DesktopPane is null!");
		}
		return RayNotifyDialog.selfHandle;
	}

	/** 已经启动或者否 **/
	private volatile boolean loaded = false;

//	/** 菜单 **/
//	private JMenuBar menubar;

	/** 选项卡 **/
	private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.BOTTOM);
	
	private ArrayList<String> texts = new ArrayList<String>();
	private ArrayList<String> messages = new ArrayList<String>();
	private ArrayList<String> warnings = new ArrayList<String>();
	private ArrayList<String> faults = new ArrayList<String>();
	
	private ArrayList<History> tables = new ArrayList<History>();
	
	private ArrayList<GraphItem> graphs = new ArrayList<GraphItem>();

	/** 消息面板 **/
	private NotifyMessagePanel messagePanel = new NotifyMessagePanel();
	
	/** 通知面板 **/
	private NotifyTablePanel tablePanel = new NotifyTablePanel();
	
	/** 图形面板 **/
	private NotifyGraphPanel graphPanel = new NotifyGraphPanel();
	
	/**
	 * 构造默认的提示对话框
	 */
	private RayNotifyDialog(JDesktopPane d) {
		super();
		desktop = d;
//		// 使用固定的UI界面
//		setFixedNimbusBorder(true);
		// 初始参数
		init();
	}

	/**
	 * 判断窗口已经加载，即被关闭，也可以显示
	 * @return 返回真或者否
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * 加载窗口
	 * @param b
	 */
	private void setLoaded(boolean b) {
		loaded = b;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#closeWindow()
	 */
	@Override
	public void closeWindow() {
		// 保存范围
		writeBounds();
		// 调用上级关闭窗口
		super.closeWindow();
		// 释放
		setLoaded(false);
		// 清除...
		RayNotifyDialog.selfHandle = null;
		
//		System.out.println("销毁系统消息窗口");
	}

//	/**
//	 * 关闭窗口
//	 */
//	private void exit1() {
//		String title = UIManager.getString("NotifyDialog.ExitTitle");
//		String content = UIManager.getString("NotifyDialog.ExitContent");
//		boolean exit = MessageBox.showYesNoDialog(this, title, content);
//		// 判断是关闭窗口
//		if (!exit) {
//			return;
//		}
//
//		// 保存范围
//		saveBounds();
//		// 区分模态/非模态，关闭窗口
//		if (isModal()) {
//			setSelectedValue(null);
//			setLoaded(false);
//		} else {
//			addThread(new DestroyThread());
//		}
//	}
	
	private void close() {
		String title = UIManager.getString("NotifyDialog.ExitTitle");
		String content = UIManager.getString("NotifyDialog.ExitContent");
		boolean exit = MessageBox.showYesNoDialog(this, title, content);
		// 判断是关闭窗口
		if (!exit) {
			return;
		}

		setVisible(false);
	}
	
	private void cancelFocusSubFrames() {
		JInternalFrame[] frames = desktop.getAllFrames();
		int size = (frames == null ? 0 : frames.length);
		for (int i = 0; i < size; i++) {
			// 判断当前子窗口被选中
			if (frames[i].isSelected()) {
				// setSelectSubFrame(frames[i], false);
				try {
					frames[i].setSelected(false);
				} catch (java.beans.PropertyVetoException e) {
					Logger.error(e);
				}
				// return;
			}
		}
	}
	
	/**
	 * 显示窗口
	 */
	public void doShow() {
		// 窗口没有加载
		if (!isLoaded()) {
			// 打开窗口
			RayNotifyDialog.selfHandle.showDialog(desktop);
		}
		// 如果窗口加载，这时要切换到最前面
		else {
			JInternalFrame[] frames = desktop.getAllFrames();
			for (int i = 0; frames != null && i < frames.length; i++) {
				JInternalFrame frame = frames[i];
				if(frame == this) {
					// 取消焦点状态窗口
					cancelFocusSubFrames();
					// 切换到选中状态，然后显示它!
					// 如果不可视，显示它
					if (!frame.isVisible()) {
						setVisible(true);
					}
					try {
						if (!isSelected()) {
							setSelected(true);
						}
					} catch (java.beans.PropertyVetoException e) {
						Logger.error(e);
					}
					// 显示文本
					doShowText();
				}
			}
		}
	}
	
//	class DestroyThread extends SwingEvent {
//		public DestroyThread() {
//			super();
//		}
//		public void process() {
////			setVisible(false);
////			dispose();
//			// 关闭窗口
//			closeWindow();
//			setLoaded(false);
//		}
//	}
	
	class NotifyCloseAdapter extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			close();
		}
	}

//	class NotifyMenuCloseAdapter implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent event) {
//			close();
//		}
//	}
	
	/**
	 * 关闭窗口
	 */
	void doExit() {
		close();
	}

	/**
	 * 保存位置信息
	 */
	public boolean isAlwaysShow() {
		String text = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM,
				"NotifyDialog/AlwayShow");
		if (text == null) {
			return true;
		}
		return text.equalsIgnoreCase("YES");
	}

//	/**
//	 * 设置显示或者否
//	 * @param b
//	 */
//	private void setAlwaysShow(boolean b) {
//		String text = (b ? "YES" : "NO");
//		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM,
//				"NotifyDialog/AlwayShow", text);
//	}

	/**
	 * 保存位置信息
	 */
	private void writeBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "NotifyDialog/Bound", rect);
	}

	/**
	 * 读位置信息
	 * @return
	 */
	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"NotifyDialog/Bound");
		if (bounds == null) {
			int w = 250;
			int h = 428;

			//			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			//			int x = (d.width - w) / 2;
			//			int y = (d.height - h) / 2;
			//			bounds = new Rectangle(x, y, w, h);

			// 显示在最右侧顶部
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = d.width - w;
			int y = 0;
			bounds = new Rectangle(x, y, w, h);
		}
		return bounds;
	}
	
	/**
	 * 设置显示范围
	 * @param desktop
	 */
	private void setBounds() {
		setBounds(readBounds());
	}
	
//	class AlwaysShowThread implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			Object source = event.getSource();
//			if (source instanceof JCheckBoxMenuItem) {
//				JCheckBoxMenuItem item = (JCheckBoxMenuItem) source;
//				boolean value = item.getState();
//				setAlwaysShow(value);
//			}
//		}
//	}

//	private JMenu createOperateMenu() {
//		JMenu menu = new JMenu(UIManager.getString("NotifyDialog.MenuOpreateText"));
//		menu.setMnemonic('O');
//		
//		boolean state = isAlwaysShow();
//		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(UIManager.getString("NotifyDialog.MenuitemAutoShowText"));
//		mi.setMnemonic('S');
//		mi.setState(state);
//		mi.addActionListener(new AlwaysShowThread());
//		menu.add(mi);
//		
//		menu.addSeparator();
//
//		JMenuItem item = new JMenuItem(UIManager.getString("NotifyDialog.MenuitemExitText"));
//		item.setMnemonic('X');
//		menu.add(item);
//		item.addActionListener(new NotifyMenuCloseAdapter());
//
//		return menu;
//	}
	
//	class DeleteMessageThread implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			clearPrompt();
//			//			addList();
//		}
//	}
//	
//	class DeleteTableThread implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			clearShowItems();
//			//			addTable();
//		}
//	}
//	
//	class DeleteGraphThread implements ActionListener {
//		public void actionPerformed(ActionEvent event) {
//			clearGraph();
//			//			addGraph();
//		}
//	}

//	private JMenu createExecuteMenu() {
//		JMenu menu = new JMenu(UIManager.getString("NotifyDialog.MenuExecuteText"));
//		menu.setMnemonic('E');
//
//		JMenuItem item = new JMenuItem(UIManager.getString("NotifyDialog.MenuitemDeleteMessageText"));
//		item.setMnemonic('M');
//		item.addActionListener(new DeleteMessageThread());
//		menu.add(item);
//
//		item = new JMenuItem(UIManager.getString("NotifyDialog.MenuitemDeleteTableText"));
//		item.setMnemonic('T');
//		item.addActionListener(new DeleteTableThread());
//		menu.add(item);
//
//		item = new JMenuItem(UIManager.getString("NotifyDialog.MenuitemDeleteGraphicText"));
//		item.setMnemonic('G');
//		item.addActionListener(new DeleteGraphThread());
//		menu.add(item);
//
//		return menu;
//	}
	
//	private void initMenuBar() {
//		// 1. 操作
//		// 2. 执行
//		// 3. 关于
//		menubar = new JMenuBar();
//		menubar.add(createOperateMenu());
//		menubar.add(createExecuteMenu());
//		menubar.setBorder(BorderFactory.createEmptyBorder(1, 2, 0, 2));
//	}

	/**
	 * 生成面板
	 * @return
	 */
	private JComponent createCenter() {
		// 消息
		messagePanel.init();
		// 二维表
		tablePanel.init();
		// 图形
		graphPanel.init();
		
//		graphPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
//		
//		this.setRefreshUI(false);

		// TAB面板
		tabbed.addTab(UIManager.getString("NotifyDialog.MessagePanelText"), messagePanel);
		tabbed.addTab(UIManager.getString("NotifyDialog.TablePanelText"), tablePanel);
		tabbed.addTab(UIManager.getString("NotifyDialog.GraphPanelText"), graphPanel);
//		tabbed.setBorder(BorderFactory.createEmptyBorder());
		return tabbed;
	}

	private void initDialog() {
		setTitle(UIManager.getString("NotifyDialog.Title"));
		setFrameIcon(UIManager.getIcon("NotifyDialog.TitleIcon"));
		// 位置
		setMinimumSize(new Dimension(238, 238));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(createCenter(), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

		// 设置
		setContentPane(panel);
	}
	
	/**
	 * 初始参数
	 */
	private void init() {
		// 初始化窗口
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		// 保存关闭按纽
		setIconifiable(false);
		setMaximizable(false);

		// 出现关闭按纽
		setClosable(true);
		setCloseIcon(UIManager.getIcon("NotifyDialog.CloseIcon"));

		// 定义在窗口的范围
		setBounds();

		// 销毁窗口
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// 内部事件
		addInternalFrameListener(new NotifyCloseAdapter());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		// 不允许模态窗口
		if (modal) {
			throw new IllegalArgumentException("must be no modal!");
		}

		// 以非模态显示!
		showNormalDialog(parent);

		// 进入状态
		setLoaded(true);

		return null;
	}
	
	private void doShowText() {
		for (String text : texts) {
			messagePanel.message(text, false);
		}
		for (String text : messages) {
			messagePanel.message(text, false);
		}
		for (String text : warnings) {
			messagePanel.warning(text, false);
		}
		for (String text : faults) {
			messagePanel.fault(text, false);
		}
		for (History e : tables) {
			tablePanel.showTable(e.title, e.array);
		}
		for (GraphItem item : graphs) {
			graphPanel.flash(item);
		}
		texts.clear();
		messages.clear();
		warnings.clear();
		faults.clear();
		tables.clear();
		graphs.clear();
	}
	
	/**
	 * 弹出对话框
	 * @param parent
	 */
	public void showDialog(Component parent) {
		// 显示窗口
		showDialog(parent, false);

		// 更新全部UI，否则表格会有多余的线条出现
		SwingUtilities.updateComponentTreeUI(this);

//		 addList();
		// addTable();
		// addGraph();
		
		doShowText();
	}
	
//	void addList(){
//		message("单车欲问边，属国过居延。征蓬出汉塞，归雁入胡天。大漠孤烟真，长河落日圆。萧关逢候骑，都护在燕然。");
//		fault("木落雁南渡，北风江上寒。我家湘水曲，遥隔楚云端。乡泪客中尽，孤帆天际看。迷津欲有问，平海昔漫漫。");
//		
//		for (int i = 0; i < 10; i++) {
//			message(String.format("分布式并行消息 %d", i + 1));
//			warning(String.format("分布式并行警告 %d", i + 1));
//			fault(String.format("分布式并行错误 %d", i + 1));
//		}
//		
//	}
	
//	void addTable() {
//		// 标题
//		ShowTitle title = new ShowTitle();
//		title.add(new ShowTitleCell(0, "分布式节点地址", 150));
//		title.add(new ShowTitleCell(1, "分布式节点端口", 130));
//		// 显示记录
//		ArrayList<ShowItem> a = new ArrayList<ShowItem>();
//		for (int i = 0; i < 30; i++) {
//			ShowItem si = new ShowItem();
//			String s = String.format("129.128.23.%d", i + 29);
//			si.add(new ShowStringCell(0, s));
//			si.add(new ShowIntegerCell(1, i + 1200));
//			a.add(si);
//		}
//		
//		showTable(title, a);
//	}
//	
//	void addGraph() {
//		Icon icon = UIManager.getIcon("NotifyDialog.ExampleImage");
//		GraphItem item = new GraphItem(icon, "黄沙百战穿金甲，不破楼兰终不还", "少年行");
//		flash(item);
//	}
	
	public void updateUI() {
		super.updateUI();

//		if (menubar != null) {
//			menubar.updateUI();
//		}
	}

	class FocusMessage extends SwingEvent {
		FocusMessage() {
			super(true); // 用同步处理
		}
		public void process() {
			int index = tabbed.getSelectedIndex();
			if (index != 0) {
				tabbed.setSelectedIndex(0);
			}
		}
	}

	class FocusTable extends SwingEvent {
		FocusTable() {
			super(true); // 用同步处理
		}
		public void process() {
			int index = tabbed.getSelectedIndex();
			if (index != 1) {
				tabbed.setSelectedIndex(1);
			}
		}
	}

	class FocusGraph extends SwingEvent {
		FocusGraph() {
			super(true); // 用同步处理
		}
		public void process() {
			int index = tabbed.getSelectedIndex();
			if (index != 2) {
				tabbed.setSelectedIndex(2);
			}
		}
	}
	/**
	 * 将焦点移至消息栏
	 */
	public void focusMessage() {
		addThread(new FocusMessage());
	}

	/**
	 * 将焦点移至表格栏
	 */
	public void focusTable() {
		addThread(new FocusTable());
	}

	/**
	 * 将焦点移到站点栏
	 */
	public void focusGraph() {
		addThread(new FocusGraph());
	}

	
	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#isUsabled()
	 */
	@Override
	public boolean isUsabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#approveLicence(java.lang.String)
	 */
	@Override
	public boolean approveLicence(String text) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String, boolean)
	 */
	@Override
	public void message(String text, boolean sound) {
		if (isLoaded()) {
			boolean show = isAlwaysShow();
			if (show) {
				// 非可视状态下，显示它
				if (!isVisible()) {
					doShow();
				}
			}
			if (show) {
				focusMessage();
				messagePanel.message(text, sound);
			} else {
				messages.add(text);
			}
		} else {
			messages.add(text);
		}
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String, boolean)
	//	 */
	//	@Override
	//	public void message(String text, boolean sound) {
	//		if (isLoaded()) {
	//			if (isAlwaysShow()) {
	//				doShow();
	//				messagePanel.message(text, sound);
	//				focusMessage();
	//			} else {
	//				messages.add(text);
	//			}
	//		} else {
	//			messages.add(text);
	//		}
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#message(java.lang.String)
	 */
	@Override
	public void message(String text) {
		message(text, true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String, boolean)
	 */
	@Override
	public void warning(String text, boolean sound) {
		if (isLoaded()) {
			boolean show = isAlwaysShow();
			if (show) {
				// 非可视状态下，显示它
				if (!isVisible()) {
					doShow();
				}
			}
			if (show) {
				focusMessage();
				messagePanel.warning(text, sound);
			} else {
				warnings.add(text);
			}
		} else {
			warnings.add(text);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#warning(java.lang.String)
	 */
	@Override
	public void warning(String text) {
		warning(text, true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String, boolean)
	 */
	@Override
	public void fault(String text, boolean sound) {
		if (isLoaded()) {
			boolean show = isAlwaysShow();
			if (show) {
				// 非可视状态下，显示它
				if (!isVisible()) {
					doShow();
				}
			}
			if (show) {
				focusMessage();
				messagePanel.fault(text, sound);
			} else {
				faults.add(text);
			}
		} else {
			faults.add(text);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#fault(java.lang.String)
	 */
	@Override
	public void fault(String text) {
		fault(text, true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearPrompt()
	 */
	@Override
	public void clearPrompt() {
		messagePanel.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getTitleCellCount()
	 */
	@Override
	public int getTitleCellCount() {
		return tablePanel.getTitleCellCount();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setShowTitle(com.laxcus.util.display.show.ShowTitle)
	 */
	@Override
	public void setShowTitle(ShowTitle title) {
		doShow();
		tablePanel.setTitle(title);
		focusTable();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#addShowItem(com.laxcus.util.display.show.ShowItem)
	 */
	@Override
	public void addShowItem(ShowItem item) {
		doShow();
		tablePanel.addItem(item);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#showTable(com.laxcus.util.display.show.ShowTitle, java.util.Collection)
	 */
	@Override
	public void showTable(ShowTitle title, Collection<ShowItem> items) {
		if (isLoaded()) {
			boolean show = isAlwaysShow();
			if (show) {
				// 非可视状态下，显示它
				if (!isVisible()) {
					doShow();
				}
			}
			if (show) {
				focusTable();
				tablePanel.showTable(title, items);
			} else {
				tables.add(new History(title, items));
			}
		} else {
			tables.add(new History(title, items));
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearShowItems()
	 */
	@Override
	public void clearShowItems() {
		tablePanel.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#setStatusText(java.lang.String)
	 */
	@Override
	public void setStatusText(String text) {
		if (text == null) {
			return;
		}
		// 做为消息显示
		message(text, false);

		//		if (isLoaded()) {
		//			if (isAlwaysShow()) {
		//				message(text, false); // 消息，不播放声音
		//			} else {
		//				texts.add(text);
		//			}
		//		} else {
		//			texts.add(text);
		//		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#flash(com.laxcus.util.display.graph.GraphItem)
	 */
	@Override
	public void flash(GraphItem item) {
		if (isLoaded()) {
			boolean show = isAlwaysShow();
			if (show) {
				// 非可视状态下，显示它
				if (!isVisible()) {
					doShow();
				}
			}
			if (show) {
				focusGraph();
				graphPanel.flash(item);
			} else {
				graphs.add(item);
			}
		} else {
			graphs.add(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#clearGraph()
	 */
	@Override
	public void clearGraph() {
		graphPanel.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.display.MeetDisplay#getProductListener()
	 */
	@Override
	public ProductListener getProductListener() {
		return null;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.ui.display.MeetDisplay#ratify(java.lang.String)
//	 */
//	@Override
//	public boolean ratify(String content) {
//		return false;
//	}

}