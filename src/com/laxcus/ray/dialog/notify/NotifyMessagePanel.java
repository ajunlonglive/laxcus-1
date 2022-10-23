/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.notify;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.sound.*;

/**
 * 提示信息显示面板，在右侧下方的选择页中。<br>
 * 
 * 因为SWING组件是线程不安全的，所有单元的“添加、删除、释放”操作，全部放入SWING线程队列中执行。
 * 
 * @author scott.liang
 * @version 1.0 9/17/2021
 * @since laxcus 1.0
 */
final class NotifyMessagePanel extends JPanel {

	private static final long serialVersionUID = 4389975218023288488L;
	
	/** 渲染器 **/
	private NotifyMessageCellRenderer renderer;

	/** 列表框 **/
	private JList list = new JList();

	/** 显示模型 **/
	private DefaultListModel model = new DefaultListModel();

	/** 弹出菜单 **/
	private JPopupMenu rockMenu;

	/**
	 * 构造消息显示面板
	 */
	public NotifyMessagePanel() {
		super();
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
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
	private JMenuItem createMenuItem(JMenuItem item, String textKey, String method) {
		String text = UIManager.getString(textKey);
		item.setText(text);
		item.setName(method);
		item.addActionListener(new ActionAdapter());
		
		// 快捷键
		setMnemonic(item, text);
		
//		// 如果是快捷吸
//		if ((w >= 'a' && w <= 'z') || (w >= 'A' && w <= 'Z')) {
//			item.setMnemonic(w);
//		}
		
		item.setBorder(new EmptyBorder(2, 4, 2, 4));
		return item;
	}
	
	private JMenuItem createMenuItem(String textKey, String method) {
		return createMenuItem(new JMenuItem(), textKey, method);
	}

	private JCheckBoxMenuItem createCheckBoxMenuItem(String textKey, String method) {
		return (JCheckBoxMenuItem) createMenuItem(new JCheckBoxMenuItem(), textKey, method);
	}


	private boolean hasClear() {
		return model.size() > 0;
	}

	private boolean hasCopy() {
		int[] vs = list.getSelectedIndices();
		return vs != null && vs.length > 0;
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
			if (element.getClass() == NoteItem.class) {
				NoteItem e = (NoteItem) element;
				if (bf.length() > 0) {
					bf.append("\r\n");
				}
				bf.append(e.toString());
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
	
	void doAutoShow() {
		JMenuItem item = MenuBuilder.findMenuItemByMethod(rockMenu, "doAutoShow");
		if (item != null && Laxkit.isClassFrom(item, JCheckBoxMenuItem.class)) {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) item;
			boolean value = mi.getState();
			setAlwaysShow(value);
		}
	}
	
	/**
	 * 设置显示或者否
	 * @param b
	 */
	private void setAlwaysShow(boolean b) {
		String text = (b ? "YES" : "NO");
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM,
				"NotifyDialog/AlwayShow", text);
	}

	/**
	 * 关闭窗口
	 */
	void doExit() {
		RayNotifyDialog.getInstance().doExit();
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

		// 从方法中找到
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

	/**
	 * 初始化弹出菜单
	 */
	private void initMenu() {                                                       
		String[] texts = new String[] {"NotifyDialog.MenuitemSelectAllMessageText", 
				"NotifyDialog.MenuitemCopyMessageText", "NotifyDialog.MenuitemDeleteMessageText", 
				"NotifyDialog.MenuitemAutoShowText", "NotifyDialog.MenuitemExitText"};
		String[] methods = new String[] { "doSelectAll", "doCopy", "doClear", "doAutoShow", "doExit" };

		//	char[] shorts = new char[] { 'A', 'C', 'D', 'S', 'X' };

		JMenuItem selectAllItem = createMenuItem(texts[0], methods[0]);
		JMenuItem copyItem = createMenuItem(texts[1], methods[1]);
		JMenuItem mnuDelete = createMenuItem(texts[2], methods[2]);
		// 自动出现
		JCheckBoxMenuItem autoShowItem = createCheckBoxMenuItem(texts[3], methods[3]);
		autoShowItem.setState(isAlwaysShow());
		// 退出
		JMenuItem exitItem = createMenuItem(texts[4], methods[4]);

		//		//		String[] texts = new String[] { "MessagePanel.MenuDelete" };
		//		//		// 快捷键
		//		//		char[] shorts = new char[] {  'D' };
		//		//		// 操作方法
		//		//		String[] methods = new String[] { "doClear" };
		//
		//		//		mnuDelete = createMenuItem(texts[0], methods[0], shorts[0]);
		//
		//		mnuDelete = new JMenuItem(UIManager.getString("NotifyDialog.MenuitemDeleteMessageText"));
		//		mnuDelete.setMnemonic('M');
		//		mnuDelete.setName("doClear");
		//		//		mnuDelete.addActionListener(new DeleteMessageThread());
		////		rockMenu.add(mnuDelete);
		//		
		//		boolean state = isAlwaysShow();
		//		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(UIManager.getString("NotifyDialog.MenuitemAutoShowText"));
		//		mi.setMnemonic('S');
		//		mi.setState(state);
		////		mi.addActionListener(new AlwaysShowThread());
		////		menu.add(mi);
		//		
		//		JMenuItem item = new JMenuItem(UIManager.getString("NotifyDialog.MenuitemExitText"));
		//		item.setMnemonic('X');

		// 生成弹出菜单
		rockMenu = new JPopupMenu();
		rockMenu.add(copyItem);
		rockMenu.add(mnuDelete);
		rockMenu.add(selectAllItem);
		rockMenu.addSeparator();
		rockMenu.add(autoShowItem);
		rockMenu.addSeparator();
		rockMenu.add(exitItem);

		rockMenu.setInvoker(list);
		list.addMouseListener(new CommandMouseAdapter());
	}

	/**
	 * 初始化
	 */
	public void init() {
		renderer = new NotifyMessageCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);

		// 显示单元范围随显示文本需要变化
		String s = UIManager.getString("NotifyDialog.MessageCellHeight");
		int cellHeight = ConfigParser.splitInteger(s, 30);
		list.setFixedCellHeight(cellHeight);

		// 边框
		list.setBorder(new EmptyBorder(1, 1, 1, 1));
		// 多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setEnabled(true);

		// 滚动框，不要定义边框"Border"，使用默认的！
		JScrollPane jsp = new JScrollPane(list);
		jsp.setBorder(new HighlightBorder(1));
		
		// 窗口布局
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder());
		add(jsp, BorderLayout.CENTER);
		
		// 初始化菜单
		initMenu();
	}

	/**
	 * 以锁定方式清除全部旧的显示记录
	 */
	public void clear() {
		addThread(new ClearThread());
	}
	
	class ClearThread extends SwingEvent {
		ClearThread() { super(); }
		public void process() {
			model.clear();
			list.removeAll();
		}
	}
	
	class AddThread extends SwingEvent {
		NoteItem item;
		
		public AddThread(NoteItem e){
			super();
			item = e;
		}
		
		public void process() {
			model.addElement(item);
			// 播放声音
			if (item.isSound()) {
				if (item.isMessage()) {
					SoundKit.playMessage();
				} else if (item.isWarning()) {
					SoundKit.playWarning();
				} else if (item.isFault()) {
					SoundKit.playError();
				}
			}
		}
	}

	/**
	 * 显示一个成员，追加到SWING事件队列中
	 * @param e
	 */
	public void add(NoteItem e) {
		addThread(new AddThread(e));
	}

	/**
	 * 普通消息
	 * @param text 显示文本
	 * @param sound 声音
	 */
	public void message(String text, boolean sound) {
		add(new NoteItem(NoteItem.MESSAGE, text, sound));
	}

	/**
	 * 警告
	 * @param text 显示文本
	 * @param sound 声音
	 */
	public void warning(String text, boolean sound) {
		add(new NoteItem(NoteItem.WARNING, text, sound));
	}

	/**
	 * 故障
	 * @param text 显示文本
	 * @param sound 播放声音
	 */
	public void fault(String text, boolean sound) {
		add(new NoteItem(NoteItem.FAULT, text, sound));
	}
	
	/**
	 * 普通消息
	 * @param text
	 */
	public void message(String text) {
		message(text, false);
	}

	/**
	 * 警告
	 * @param text
	 */
	public void warning(String text) {
		warning(text, true);
	}

	/**
	 * 故障
	 * @param text
	 */
	public void fault(String text) {
		fault(text, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		// 更新
		super.updateUI();
		// 渲染器更新
		if (renderer != null) {
			renderer.updateUI();
		}
		
		if (rockMenu != null) {
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}
	}

}

//class PushNoteThread extends SwingEvent {
//	ArrayList<NoteItem> array = new ArrayList<NoteItem>();
//
//	public PushNoteThread() {
//		super();
//	}
//
//	public void addAll(Collection<NoteItem> e) {
//		array.addAll(e);
//	}
//
//	public int size() {
//		return array.size();
//	}
//
//	public void process() {
//		//			WatchLauncher launcher = WatchLauncher.getInstance();
//
//		for (NoteItem item : array) {
//			model.addElement(item);
//			//				// 声音
//			//				if (item.isMessage()) {
//			//					if (item.isSound()) launcher.playSound(SoundTag.MESSAGE);
//			//				} else if (item.isWarning()) {
//			//					if (item.isSound()) launcher.playSound(SoundTag.WARNING);
//			//				} else if (item.isFault()) {
//			//					if (item.isSound()) launcher.playSound(SoundTag.ERROR);
//			//				}
//		}
//	}
//}

///**
// * 站点参数显示/删除任务
// *
// * @author scott.liang
// * @version 1.0 8/22/2018
// * @since laxcus 1.0
// */
//class NoteTask extends TimerTask {
//
//	SingleLock lock = new SingleLock();
//
//	ArrayList<NoteItem> array = new ArrayList<NoteItem>();
//
//	/**
//	 * 清除全部记录
//	 */
//	public void clear() {
//		lock.lock();
//		try {
//			array.clear();
//		} catch (Throwable e) {
//			
//		} finally {
//			lock.unlock();
//		}
//	}
//
//	/**
//	 * 保存单元
//	 * @param e
//	 */
//	public void add(NoteItem e) {
//		lock.lock();
//		try {
//			if (e != null) {
//				array.add(e);
//			}
//		} finally {
//			lock.unlock();
//		}
//	}
//	
//	/* (non-Javadoc)
//	 * @see java.util.TimerTask#run()
//	 */
//	@Override
//	public void run() {
//		int size = array.size();
//		if (size == 0) {
//			return;
//		}
//
//		PushNoteThread thread = new PushNoteThread();
//		// 锁定保存
//		lock.lock();
//		try {
//			thread.addAll(array);
//			array.clear();
//		} catch (Throwable e) {
//			
//		} finally {
//			lock.unlock();
//		}
//		// 输出线程
//		if (thread.size() > 0) {
//			addThread(thread);
//		}
//	}
//}


///**
// * 返回当前选择的字体
// * @return
// */
//public Font getSelectFont() {
//	return list.getFont();
//}

///**
// * 设置新选择的字体
// * @param font
// */
//public void setSelectFont(Font font) {
//	addThread(new FontThread(font));
//}
//
///**
// * 修正字体
// * @param font
// */
//private void __exchangeFont(Font font) {
//	if (font != null) {
//		list.setFont(font);
//	}
//}

///**
// * 字体线程
// * @author scott.liang
// * @version 1.0 8/28/2018
// * @since laxcus 1.0
// */
//class FontThread extends SwingEvent {
//	Font font;
//
//	FontThread(Font e) {
//		super();
//		font = e;
//	}
//
//	public void process() {
//		__exchangeFont(font);
//	}
//}


///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license Laxcus Public License (LPL)
// */
//package com.laxcus.ray.dialog.notify;
//
//import java.awt.*;
//
//import javax.swing.*;
//import javax.swing.border.*;
//
//import com.laxcus.util.*;
//import com.laxcus.util.display.*;
//import com.laxcus.util.event.*;
//import com.laxcus.util.sound.*;
//
///**
// * 提示信息显示面板，在右侧下方的选择页中。<br>
// * 
// * 因为SWING组件是线程不安全的，所有单元的“添加、删除、释放”操作，全部放入SWING线程队列中执行。
// * 
// * @author scott.liang
// * @version 1.0 9/17/2021
// * @since laxcus 1.0
// */
//final class NotifyMessagePanel extends JPanel {
//
//	private static final long serialVersionUID = 4389975218023288488L;
//	
//	/** 渲染器 **/
//	private NotifyMessageCellRenderer renderer;
//
//	/** 列表框 **/
//	private JList list = new JList();
//
//	/** 显示模型 **/
//	private DefaultListModel model = new DefaultListModel();
//
//	/**
//	 * 构造消息显示面板
//	 */
//	public NotifyMessagePanel() {
//		super();
//	}
//
//	/**
//	 * 加入线程
//	 * @param thread
//	 */
//	private void addThread(SwingEvent thread) {
//		SwingDispatcher.invokeThread(thread);
//	}
//	
//	/**
//	 * 初始化
//	 */
//	public void init() {
//		renderer = new NotifyMessageCellRenderer();
//		list.setCellRenderer(renderer);
//		list.setModel(model);
//
//		// 显示单元范围随显示文本需要变化
//		String s = UIManager.getString("NotifyDialog.MessageCellHeight");
//		int cellHeight = ConfigParser.splitInteger(s, 30);
//		list.setFixedCellHeight(cellHeight);
//
//		// 边框
//		list.setBorder(new EmptyBorder(1, 1, 1, 1));
//		// 多选
//		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		list.setEnabled(true);
//
//		// 滚动框，不要定义边框"Border"，使用默认的！
//		JScrollPane jsp = new JScrollPane(list);
//		
//		// 窗口布局
//		setLayout(new BorderLayout());
//		setBorder(BorderFactory.createEmptyBorder());
//		add(jsp, BorderLayout.CENTER);
//	}
//
//	/**
//	 * 以锁定方式清除全部旧的显示记录
//	 */
//	public void clear() {
//		addThread(new ClearThread());
//	}
//	
//	class ClearThread extends SwingEvent {
//		ClearThread() { super(); }
//		public void process() {
//			model.clear();
//			list.removeAll();
//		}
//	}
//	
//	class AddThread extends SwingEvent {
//		NoteItem item;
//		
//		public AddThread(NoteItem e){
//			super();
//			item = e;
//		}
//		
//		public void process() {
//			model.addElement(item);
//			// 播放声音
//			if (item.isSound()) {
//				if (item.isMessage()) {
//					SoundKit.playMessage();
//				} else if (item.isWarning()) {
//					SoundKit.playWarning();
//				} else if (item.isFault()) {
//					SoundKit.playError();
//				}
//			}
//		}
//	}
//
//	/**
//	 * 显示一个成员，追加到SWING事件队列中
//	 * @param e
//	 */
//	public void add(NoteItem e) {
//		addThread(new AddThread(e));
//	}
//
//	/**
//	 * 普通消息
//	 * @param text 显示文本
//	 * @param sound 声音
//	 */
//	public void message(String text, boolean sound) {
//		add(new NoteItem(NoteItem.MESSAGE, text, sound));
//	}
//
//	/**
//	 * 警告
//	 * @param text 显示文本
//	 * @param sound 声音
//	 */
//	public void warning(String text, boolean sound) {
//		add(new NoteItem(NoteItem.WARNING, text, sound));
//	}
//
//	/**
//	 * 故障
//	 * @param text 显示文本
//	 * @param sound 播放声音
//	 */
//	public void fault(String text, boolean sound) {
//		add(new NoteItem(NoteItem.FAULT, text, sound));
//	}
//	
//	/**
//	 * 普通消息
//	 * @param text
//	 */
//	public void message(String text) {
//		message(text, false);
//	}
//
//	/**
//	 * 警告
//	 * @param text
//	 */
//	public void warning(String text) {
//		warning(text, true);
//	}
//
//	/**
//	 * 故障
//	 * @param text
//	 */
//	public void fault(String text) {
//		fault(text, true);
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JPanel#updateUI()
//	 */
//	@Override
//	public void updateUI() {
//		// 更新
//		super.updateUI();
//		// 渲染器更新
//		if (renderer != null) {
//			renderer.updateUI();
//		}
//	}
//
//}
//
//
////class PushNoteThread extends SwingEvent {
////	ArrayList<NoteItem> array = new ArrayList<NoteItem>();
////
////	public PushNoteThread() {
////		super();
////	}
////
////	public void addAll(Collection<NoteItem> e) {
////		array.addAll(e);
////	}
////
////	public int size() {
////		return array.size();
////	}
////
////	public void process() {
////		//			WatchLauncher launcher = WatchLauncher.getInstance();
////
////		for (NoteItem item : array) {
////			model.addElement(item);
////			//				// 声音
////			//				if (item.isMessage()) {
////			//					if (item.isSound()) launcher.playSound(SoundTag.MESSAGE);
////			//				} else if (item.isWarning()) {
////			//					if (item.isSound()) launcher.playSound(SoundTag.WARNING);
////			//				} else if (item.isFault()) {
////			//					if (item.isSound()) launcher.playSound(SoundTag.ERROR);
////			//				}
////		}
////	}
////}
//
/////**
//// * 站点参数显示/删除任务
//// *
//// * @author scott.liang
//// * @version 1.0 8/22/2018
//// * @since laxcus 1.0
//// */
////class NoteTask extends TimerTask {
////
////	SingleLock lock = new SingleLock();
////
////	ArrayList<NoteItem> array = new ArrayList<NoteItem>();
////
////	/**
////	 * 清除全部记录
////	 */
////	public void clear() {
////		lock.lock();
////		try {
////			array.clear();
////		} catch (Throwable e) {
////			
////		} finally {
////			lock.unlock();
////		}
////	}
////
////	/**
////	 * 保存单元
////	 * @param e
////	 */
////	public void add(NoteItem e) {
////		lock.lock();
////		try {
////			if (e != null) {
////				array.add(e);
////			}
////		} finally {
////			lock.unlock();
////		}
////	}
////	
////	/* (non-Javadoc)
////	 * @see java.util.TimerTask#run()
////	 */
////	@Override
////	public void run() {
////		int size = array.size();
////		if (size == 0) {
////			return;
////		}
////
////		PushNoteThread thread = new PushNoteThread();
////		// 锁定保存
////		lock.lock();
////		try {
////			thread.addAll(array);
////			array.clear();
////		} catch (Throwable e) {
////			
////		} finally {
////			lock.unlock();
////		}
////		// 输出线程
////		if (thread.size() > 0) {
////			addThread(thread);
////		}
////	}
////}
//
//
/////**
//// * 返回当前选择的字体
//// * @return
//// */
////public Font getSelectFont() {
////	return list.getFont();
////}
//
/////**
//// * 设置新选择的字体
//// * @param font
//// */
////public void setSelectFont(Font font) {
////	addThread(new FontThread(font));
////}
////
/////**
//// * 修正字体
//// * @param font
//// */
////private void __exchangeFont(Font font) {
////	if (font != null) {
////		list.setFont(font);
////	}
////}
//
/////**
//// * 字体线程
//// * @author scott.liang
//// * @version 1.0 8/28/2018
//// * @since laxcus 1.0
//// */
////class FontThread extends SwingEvent {
////	Font font;
////
////	FontThread(Font e) {
////		super();
////		font = e;
////	}
////
////	public void process() {
////		__exchangeFont(font);
////	}
////}
