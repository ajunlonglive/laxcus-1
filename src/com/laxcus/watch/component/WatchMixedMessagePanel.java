/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.sound.*;
import com.laxcus.watch.*;

/**
 * 提示信息显示面板，在右侧下方的选择页中。<br>
 * 
 * 因为SWING组件是线程不安全的，所有单元的“添加、删除、释放”操作，全部放入SWING线程队列中执行。
 * 
 * @author scott.liang
 * @version 1.23 8/23/2015
 * @since laxcus 1.0
 */
public class WatchMixedMessagePanel extends JPanel {

	private static final long serialVersionUID = 4389975218023288488L;
	
	/** 渲染器 **/
	private WatchMixedMessageCellRenderer renderer;

	/** 列表框 **/
	private JList list = new JList();

	/** 显示模型 **/
	private DefaultListModel model = new DefaultListModel();
	
	/** 显示任务，一秒钟触发一次 **/
	private NoteTask noteTask = new NoteTask();

	/**
	 * 构造消息显示面板
	 */
	public WatchMixedMessagePanel() {
		super();
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	/**
	 * 返回当前选择的字体
	 * @return
	 */
	public Font getSelectFont() {
		return list.getFont();
	}

	/**
	 * 设置新选择的字体
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
		renderer = new WatchMixedMessageCellRenderer();
		list.setCellRenderer(renderer);
		list.setModel(model);
		
		Timer timer = WatchLauncher.getInstance().getTimer();
		timer.schedule(noteTask, 0, 1500); // 1.5秒钟触发一次
		
		String tooltip = WatchLauncher.getInstance().findCaption("Window/MessagePanel/title");

		// 显示单元范围随显示文本需要变化
		list.setFixedCellHeight(-1);
		// 提示文本
		FontKit.setToolTipText(list, tooltip);
		// 边框
		list.setBorder(new EmptyBorder(2, 2, 2, 2));
		// 多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		list.setEnabled(true);
		
		// 修正字体
		__exchangeFont(WatchProperties.readTabbedMessageFont());

		// 滚动框，不要定义边框"Border"，使用默认的！
		JScrollPane scroll = new JScrollPane(list);
//		scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		FontKit.setToolTipText(scroll, tooltip);
		
		// 窗口布局
		setLayout(new BorderLayout(0, 0));
//		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setBorder(BorderFactory.createEmptyBorder());
		add(scroll, BorderLayout.CENTER);
	}

	/**
	 * 以锁定方式清除全部旧的显示记录
	 */
	public void clear() {
		//		Runnable thread = new Runnable() {
		//			public void run() {
		//				noteTask.clear();
		//				model.clear();
		//				list.removeAll();
		//			}
		//		};
		//		addThread(thread);

		addThread(new ClearThread());
	}
	
	class ClearThread extends SwingEvent {
		ClearThread() { super(); }
		public void process() {
			noteTask.clear();
			model.clear();
			list.removeAll();
		}
	}

	/**
	 * 显示一个成员，追加到SWING事件队列中
	 * @param e
	 */
	public void add(NoteItem e) {
		noteTask.add(e);
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
		message(text, true);
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
	
	class PushNoteThread extends SwingEvent {
		ArrayList<NoteItem> array = new ArrayList<NoteItem>();

		public PushNoteThread() {
			super();
		}

		public void addAll(Collection<NoteItem> e) {
			array.addAll(e);
		}

		public int size() {
			return array.size();
		}

		public void process() {
			WatchLauncher launcher = WatchLauncher.getInstance();
			
			for (NoteItem item : array) {
				model.addElement(item);
				// 声音
				if (item.isMessage()) {
					if (item.isSound()) launcher.playSound(SoundTag.MESSAGE);
				} else if (item.isWarning()) {
					if (item.isSound()) launcher.playSound(SoundTag.WARNING);
				} else if (item.isFault()) {
					if (item.isSound()) launcher.playSound(SoundTag.ERROR);
				}
			}
		}
	}
	
	/**
	 * 站点参数显示/删除任务
	 *
	 * @author scott.liang
	 * @version 1.0 8/22/2018
	 * @since laxcus 1.0
	 */
	class NoteTask extends TimerTask {

		SingleLock lock = new SingleLock();

		ArrayList<NoteItem> array = new ArrayList<NoteItem>();

		/**
		 * 清除全部记录
		 */
		public void clear() {
			lock.lock();
			try {
				array.clear();
			} catch (Throwable e) {
				
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 保存单元
		 * @param e
		 */
		public void add(NoteItem e) {
			lock.lock();
			try {
				if (e != null) {
					array.add(e);
				}
			} finally {
				lock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			int size = array.size();
			if (size == 0) {
				return;
			}

			PushNoteThread thread = new PushNoteThread();
			// 锁定保存
			lock.lock();
			try {
				thread.addAll(array);
				array.clear();
			} catch (Throwable e) {
				
			} finally {
				lock.unlock();
			}
			// 输出线程
			if (thread.size() > 0) {
				addThread(thread);
			}
		}
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
	}

}