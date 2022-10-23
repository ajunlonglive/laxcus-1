/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.desktop.panel.*;
import com.laxcus.gui.frame.*;
import com.laxcus.util.event.*;

/**
 * 应用面板 <br>
 * 应用以按纽的形式显示 <br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/19/2021
 * @since laxcus 1.0
 */
final class DesktopFrameBar extends DesktopPanel {

	private static final long serialVersionUID = 883607744303682322L;

	/** FRAME按纽的默认像素宽度 **/
	//	final static int FRAMEBUTTON_WIDTH = 138;

	//	final static int FRAMEBUTTON_WIDTH = 36;

	/** FRAME按纽之间的间隔像素 **/
	final static int FRAMEBUTTON_GAP = 4;

	//	/**
	//	 * 前头按纽鼠标追踪器
	//	 *
	//	 * @author scott.liang
	//	 * @version 1.0 9/21/2021
	//	 * @since laxcus 1.0
	//	 */
	//	class ArrowMouseTracker extends MouseAdapter {
	//
	//		/**
	//		 * 构造鼠标追踪器
	//		 */
	//		public ArrowMouseTracker() {
	//			super();
	//		}
	//
	//		/**
	//		 * 按下左侧鼠标
	//		 * @param e
	//		 */
	//		@Override
	//		public void mousePressed(MouseEvent e) {
	//			Object source = e.getSource();
	//			// 不是按纽，忽略
	//			if (source.getClass() != FrameArrowButton.class) {
	//				return;
	//			}
	//
	//			// 左侧按纽
	//			if (e.getButton() == MouseEvent.BUTTON1) {
	//				FrameArrowButton but = (FrameArrowButton) source;
	//				if (but.isEnabled()) {
	//					but.setPressed(true);
	//				}
	//			}
	//		}
	//		
	//		/**
	//		 * 弹起左侧鼠标
	//		 */
	//		public void mouseReleased(MouseEvent e) {
	//			Object source = e.getSource();
	//			// 不是按纽，忽略
	//			if (source.getClass() != FrameArrowButton.class) {
	//				return;
	//			}
	//
	//			// 左侧按纽
	//			if (e.getButton() == MouseEvent.BUTTON1) {
	//				FrameArrowButton but = (FrameArrowButton) source;
	//				if (but.isEnabled()) {
	//					but.setPressed(false);
	//				}
	//			}
	//		}
	//	}

	/**
	 * 鼠标追踪器
	 *
	 * @author scott.liang
	 * @version 1.0 9/20/2021
	 * @since laxcus 1.0
	 */
	class FrameMouseTracker extends MouseAdapter {

		/** 前一个按纽 **/
		private FrameButton preview;

		/** 当前触发的按纽 **/
		private FrameButton now;

		/**
		 * 构造鼠标追踪器
		 */
		public FrameMouseTracker() {
			super();
		}

		/**
		 * 取消全部的焦点
		 */
		public void cancelAll() {
			if (preview != null) {
				if (preview.isPressed()) {
					preview.setPressed(false);
				}
				preview = null;
			}
			if (now != null) {
				if (now.isPressed()) {
					now.setPressed(false);
				}
				now = null;
			}
		}

		/**
		 * 解除关联
		 * @param button
		 */
		public void detach(FrameButton button) {
			if (preview != null && preview == button) {
				preview = null;
			}
			if (now != null && now == button) {
				now = null;
			}
		}

		/**
		 * 激活
		 * @param button
		 */
		public void activate(FrameButton button) {
			// 前一个按纽取消焦点，恢复状态
			if (preview != null && preview.isPressed()) {
				preview.setPressed(false); // 取消焦点
			}
			if (now != null && now.isPressed()) {
				now.setPressed(false); // 取消焦点
			}
			// 前一个按纽
			preview = now;
			// 当前按纽
			now = button;

			// 生成焦点重新绘制
			now.setPressed(true);
		}

		/**
		 * 撤销激活
		 * @param button
		 */
		public void deactivate(FrameButton button) {
			// 前一个按纽取消焦点，恢复状态
			if (preview != null && preview.isPressed()) {
				preview.setPressed(false); // 取消焦点
			}
			if (now != null && now.isPressed()) {
				now.setPressed(false);// 取消焦点
			}

			// 前一个按纽
			preview = now;
			// 当前按纽
			now = button;

			// 生成焦点重新绘制
			now.setPressed(false);
		}

		/**
		 * 按下左侧鼠标
		 * @param e
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			Object source = e.getSource();
			// 不是按纽，忽略
			if (source.getClass() != FrameButton.class) {
				return;
			}

			// 左侧按纽
			if (e.getButton() == MouseEvent.BUTTON1) {
				FrameButton button = (FrameButton) source;
				// 如果是当前按纽，忽略...
				if (now != null && now == button) {
					return;
				}

				// 前一个按纽取消焦点，恢复状态
				if (preview != null && preview != now) {
					preview.setPressed(false); // 取消焦点
				}
				// 当前按纽，恢复状态
				if (now != null) {
					now.setPressed(false);
				}
				// 前一个按纽
				preview = now;
				// 当前按纽
				now = button;

				// 生成焦点重新绘制
				now.setPressed(true);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			Object source = e.getSource();
			// 比较是标题时，不一致更新
			if (source instanceof FrameButton) {
				FrameButton but = (FrameButton) source;
				LightFrame frame = but.getFrame();
				String frameTitle = frame.getTitle();
				String tooltip = but.getToolTipText();
				// 判断标题，显示对应信息
				if (frameTitle == null) {
					but.setToolTipText("");
				} else if (tooltip == null || frameTitle.compareTo(tooltip) != 0) {
					frameTitle = createTooltip(frameTitle, but);
					but.setToolTipText(frameTitle);
				}
			}
		}

		/**
		 * 生成自动换行的HTML格式文本提示
		 * @param text 提示文本
		 * @param but 按纽
		 * @return 返回经过格式化处理的提示文本
		 */
		private String createTooltip(String text, FrameButton but) {
			FontMetrics fm = but.getFontMetrics(but.getFont());

			char[] words = text.toCharArray();
			int len = fm.charsWidth(words, 0, words.length);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

			if (len < dim.width) {
				return text;
			}

			String title = "";
			int start = 0;
			int last = 0;
			int lastSP = -1; // 最后一个空格

			for (last++; last < words.length; last++) {
				// 记录最后一个空格位置
				if (words[last] == 0x20) {
					lastSP = last;
				}

				len = fm.charsWidth(words, start, last - start);

				if (len > dim.width) {
					if (lastSP == -1) {
						String str = text.substring(start, last - 1);
						if (title.isEmpty()) {
							title = str;
						} else {
							title = String.format("%s<br>%s", title, str);
						}
						start = last - 1;
					} else {
						String str = text.substring(start, lastSP + 1);
						if (title.isEmpty()) {
							title = str;
						} else {
							title = String.format("%s<br>%s", title, str);
						}
						// 下标从最后个空格开始
						start = lastSP + 1;
						last = lastSP + 1;
						lastSP = -1;
					}
				}
			}
			if (start < last) {
				String str = text.substring(start, last);
				if (title.isEmpty()) {
					title = str;
				} else {
					title = String.format("%s<br>%s", title, str);
				}
			}

			return String.format("<html>%s</html>", title);
		}

		//		/*
		//		 * (non-Javadoc)
		//		 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
		//		 */
		//		@Override
		//		public void mouseEntered(MouseEvent e) {
		//			Object source = e.getSource();
		//			// 比较是标题时，不一致更新
		//			if (source instanceof FrameButton) {
		//				FrameButton but = (FrameButton) source;
		//				but.showDriftMenu(e);
		//				
		////				LightFrame frame = but.getFrame();
		////				String frameTitle = frame.getTitle();
		////				String tooltip = but.getToolTipText();
		////				// 判断标题，显示对应信息
		////				if (frameTitle == null) {
		////					but.setToolTipText("");
		////				} else {
		////					if (tooltip == null || frameTitle.compareTo(tooltip) != 0) {
		////						but.setToolTipText(frameTitle);
		////					}
		////				}
		//			}
		//		}
		//		
		//		@Override
		//		public void mouseExited(MouseEvent e) {
		//			Object source = e.getSource();
		//			// 比较是标题时，不一致更新
		//			if (source instanceof FrameButton) {
		//				FrameButton but = (FrameButton) source;
		//				but.hideDriftMenu();
		//			}
		//		}

	}

	class FrameButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source instanceof FrameButton) {
				FrameButton button = (FrameButton) source;
				mouseTicker.activate(button);
				// 通知桌面，选择那个成为焦点的窗口
				LightFrame frame = button.getFrame();
				frameListener.callSelectFrame(frame);
			}
		}
	}

	//	class LeftArrowThread extends SwingEvent {
	//
	//		LeftArrowThread() {
	//			super(true);
	//		}
	//
	//		public void process() {
	//			doLeftArrow();
	//		}
	//	}

	//	class RightArrowThread extends SwingEvent {
	//
	//		RightArrowThread() {
	//			super(true);
	//		}
	//
	//		public void process() {
	//			doRightArrow();
	//		}
	//	}

	/** 桌面 **/
	private DesktopSelectFrameListener frameListener;

	/** 鼠标追踪器 **/
	private FrameMouseTracker mouseTicker = new FrameMouseTracker();

	/** 按纽 **/
	private ArrayList<FrameButton> array = new ArrayList<FrameButton>();

	/** 左侧按纽 **/
	private DesktopFrameArrowButton cmdLeft;

	/** 右侧按纽 **/
	private DesktopFrameArrowButton cmdRight;

	/** 中心面板 **/
	private FrameBanner banner;

	/**
	 * 构造应用面板
	 */
	public DesktopFrameBar() {
		super();
		init();
	}

	/**
	 * 返回一个按纽的宽度，以BANNER的高度做为基准
	 * @return 整数
	 */
	private int getButtonDefaultWidth() {
		if (banner == null) {
			return 36;
		}
		return banner.getHeight();
	}

	/**
	 * 左侧鼠标点击
	 *
	 * @author scott.liang
	 * @version 1.0 9/21/2021
	 * @since laxcus 1.0
	 */
	class LeftButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 向左移动界面
			doLeftArrow();
			//			addThread(new LeftArrowThread());
		}
	}

	/**
	 * 右侧鼠标点击
	 *
	 * @author scott.liang
	 * @version 1.0 9/21/2021
	 * @since laxcus 1.0
	 */
	class RightButtonAdapter implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 向右移动界面
			doRightArrow();
			//			addThread(new RightArrowThread());
		}
	}

	/**
	 * 初始化按纽
	 */
	private void createArrowButtons() {
		// 左侧箭头按纽
		cmdLeft = new DesktopFrameArrowButton();
		ImageIcon icon = (ImageIcon) UIManager.getIcon("StatusBar.LeftIcon");
		cmdLeft.setIcon(icon, 12, -18);
		// 右侧箭头按纽
		cmdRight = new DesktopFrameArrowButton();
		icon = (ImageIcon) UIManager.getIcon("StatusBar.RightIcon");
		cmdRight.setIcon(icon, 12, -18);

		// 提示文本
		cmdLeft.setToolTipText(UIManager.getString("StatusBar.LeftTooltipText"));
		cmdRight.setToolTipText(UIManager.getString("StatusBar.RightTooltipText"));

		// 鼠标单点事件
		cmdLeft.addActionListener(new LeftButtonAdapter());
		cmdRight.addActionListener(new RightButtonAdapter());

		//		cmdLeft.addMouseListener(new ArrowMouseTracker());
		//		cmdRight.addMouseListener(new ArrowMouseTracker());
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 初始化按纽
		createArrowButtons();
		// 部署面板
		banner = new FrameBanner();
		banner.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		banner.setLayout(new FlowLayout(FlowLayout.LEFT, FRAMEBUTTON_GAP, 0));
		banner.setBorder(new EmptyBorder(0, 0, 0, 0));

		// banner.setLayout(new BoxLayout(banner, BoxLayout.X_AXIS));
		// banner.setLayout(new GridLayout(0, 4, 4, 0));

		// 中心面板
		setLayout(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(0, 0, 0, 0));
		add(cmdLeft, BorderLayout.WEST);
		add(banner, BorderLayout.CENTER);
		add(cmdRight, BorderLayout.EAST);

		cmdLeft.setVisible(false);
		cmdRight.setVisible(false);

		// // 记录高度
		// bannerHeight = banner.getHeight();
	}

	/**
	 * 根据窗口句柄找到按纽实例
	 * @param frame 窗口实例
	 * @return 返回按纽实例
	 */
	private FrameButton findButton(LightFrame frame) {
		// 找到对应的按纽
		for (FrameButton but : array) {
			if (but.getFrame() == frame) {
				return but;
			}
		}
		return null;
	}

	/**
	 * 判断窗口存在
	 * @param frame 窗口句柄
	 * @return 返回真或者假
	 */
	private boolean hasFrame(LightFrame frame) {
		return findButton(frame) != null;
	}


	private void doLeftEnabled() {
		if (!cmdLeft.isVisible()) {
			return;
		}

		if (banner.getComponentCount() < 1 || array.size() < 1) {
			return;
		}

		Component first = banner.getComponent(0);
		FrameButton button = array.get(0);
		// 已经是第一个时，左侧按纽无效
		boolean success = (first == button);
		if (success) {
			cmdLeft.setEnabled(false);
		} else {
			cmdLeft.setEnabled(true);
		}
	}

	private void doRightEnabled() {
		if (!cmdRight.isVisible()) {
			return;
		}

		if (banner.getComponentCount() < 1 || array.size() < 1) {
			return;
		}

		Component[] components = banner.getComponents();
		Component last = components[components.length - 1];
		FrameButton button = array.get(array.size() - 1);
		// 已经是最后时，左侧按纽无效
		boolean success = (last == button);
		if (success) {
			cmdRight.setEnabled(false);
		} else {
			cmdRight.setEnabled(true);
		}
	}

	/**
	 * 根据面板长度，计算可显示按纽数目
	 * @param barWidth
	 * @return
	 */
	private int doButtons(int barWidth) {
		int buttons = 0;
		int width = 0;
		int buttonWidth = getButtonDefaultWidth();

		do {
			// 记录间隔
			if (width > 0) {
				width += DesktopFrameBar.FRAMEBUTTON_GAP;
			}
			//			width += DesktopFrameBar.FRAMEBUTTON_WIDTH;
			//			width += this.getButtonDefaultWidth();

			width += buttonWidth;
			// 超过宽度，退出
			if (width > barWidth) {
				break;
			}
			buttons++;
		} while(true);

		return buttons;
	}

	/**
	 * 向左移动FRAME按纽，确定最左侧的按纽，减法，向前移动
	 */
	private void doLeftArrow() {
		// 没有，忽略它
		if (banner.getComponentCount() == 0) {
			return;
		}

		Component begin = banner.getComponent(0);
		// 确定第一个显示按纽在队列数组中的下标
		int index = -1;
		int size = array.size();
		for (int i = 0; i < size; i++) {
			FrameButton button = array.get(i);
			// 第一个显示的按纽
			if (button == begin) {
				index = i;
				break;
			}
		}
		if (index < 0) {
			// 这是错误，忽略
			//			System.out.println("没有找到按纽位置，这是一个错误！");
			return;
		} else if (index == 0) {
			// 已经移到最前面，忽略
			//			System.out.println("向左移动，已经在最左边！");
			return;
		}

		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);

		// 左侧的按纽索引
		int beginIndex = (index - buttons >= 0 ? index - buttons : 0);
		int endIndex = (beginIndex + buttons <= size ? beginIndex + buttons : size);

		//		System.out.printf("向左，begin: %d, end: %d, 按纽数：%d\n", beginIndex, endIndex,buttons);

		//		// 取消全部的关联
		//		mouseTicker.cancelAll();

		// 清除全部按纽
		banner.removeAll();
		// 增加显示一个按纽
		for (int i = beginIndex; i < endIndex; i++) {
			FrameButton button = array.get(i);
			banner.setButtonSize(button);
			banner.add(button);

			//			System.out.printf("向左 %d %s\n", i, button.getText());
		}

		doLeftEnabled();
		doRightEnabled();

		//		validateTree();
		revalidate();
		repaint();
	}

	/**
	 * 向右移动FRAME按纽，确定显示的最右侧按纽，加法，向后移动
	 */
	private void doRightArrow() {
		// 没有，忽略它
		if (banner.getComponentCount() == 0) {
			return;
		}

		Component[] components = banner.getComponents();
		Component last = components[components.length - 1];
		// 最后一个显示按纽在队列数组中的下标
		int index = -1;
		int size = array.size();
		for (int i = 0; i < size; i++) {
			FrameButton button = array.get(i);
			// 找到最后显示的按纽，然后向后移动一个，这是新的开始按纽
			if (button == last) {
				index = i + 1; 
				break;
			}
		}

		if (index < 0) {
			//			System.out.println("向右移动，没有找到最后的可视按纽");
			return;
		} else if (index >= size) {
			// 已经在最后了，这里不要显示
			//			System.out.println("向右移动，已经在最后了");
			return;
		}

		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);

		// 结束位置
		int endIndex = (index + buttons >= size ? size : index + buttons);
		int beginIndex = (endIndex - buttons >= 0 ? endIndex - buttons : 0);

		//		System.out.printf("向右，begin: %d, end: %d, 按纽数：%d\n", beginIndex, endIndex,buttons);

		//		// 取消全部的关联
		//		mouseTicker.cancelAll();

		// 清除全部按纽
		banner.removeAll();
		// 增加显示一个按纽
		for (int i = beginIndex; i < endIndex; i++) {
			FrameButton button = array.get(i);
			banner.setButtonSize(button);
			banner.add(button);

			//			System.out.printf("向右 %d %s\n", i, button.getText());
		}

		doLeftEnabled();
		doRightEnabled();

		//		validateTree();
		revalidate();
		repaint();
	}

	/**
	 * 判断左右箭头按纽是显示状态，任何一侧处于显示皆可
	 * @return 返回真或者假
	 */
	private boolean isArrowShowing() {
		boolean left = (cmdLeft.isVisible() && cmdLeft.isShowing());
		boolean right = (cmdRight.isVisible() && cmdRight.isShowing());
		return left || right;
	}

	/**
	 * 添加
	 * @param frame
	 */
	private void doAdd(LightFrame frame) {
		// 如果已经存在，就忽略它
		if (hasFrame(frame)) {
			return;
		}

		FrameButton button = new FrameButton(frame);
		button.addMouseListener(mouseTicker);
		button.addActionListener(new FrameButtonAdapter());

		// 保存按纽到队列中
		array.add(button);

		boolean showing = isArrowShowing();

		//		System.out.printf("%s - %s - %s\n", (showing ? "show" : "hide"), array.size(), frame.getTitle());

		if (!showing) {
			int barWidth = getWidth();
			int buttons = array.size();

			//			int buttonsWidth = buttons * DesktopFrameBar.FRAMEBUTTON_WIDTH + (buttons - 1) * DesktopFrameBar.FRAMEBUTTON_GAP;

			int butWidth = getButtonDefaultWidth();
			int buttonsWidth = (buttons * butWidth) + ((buttons - 1) * DesktopFrameBar.FRAMEBUTTON_GAP);
			// 如果全部按纽统计长度大于面板长度，显示左右按纽，这里新的按纽不加入
			if (buttonsWidth > barWidth) {
				cmdLeft.setVisible(true);
				cmdRight.setVisible(true);
				cmdLeft.setEnabled(true);
				cmdRight.setEnabled(true);
				// 计算有效
				doLeftEnabled();
				doRightEnabled();
			} else {
				// 上面条件不成立时，按纽加到面板中显示
				banner.add(button);
			}
		} else {
			// 新增按纽后，重新计算左右按纽的有效
			doLeftEnabled();
			doRightEnabled();
		}

		// 刷新
		//		validate();
		revalidate();
		repaint();
	}

	/**
	 * 输出全部运行中的窗口句柄
	 * @return LightFrame数组，没有返回一个空数组
	 */
	public LightFrame[] getLightFrames() {
		int size = array.size();
		LightFrame[] a = new LightFrame[size];
		for (int i = 0; i < size; i++) {
			FrameButton but = array.get(i);
			a[i] = but.getFrame();
		}
		return a;
	}

	/**
	 * 删除后重新组件
	 */
	private void repaintRemoveButtons(int index) {
		//		// 没有，忽略它
		//		if (banner.getComponentCount() == 0) {
		//			return;
		//		}

		//		Component[] components = banner.getComponents();
		//		Component last = components[components.length - 1];
		// 最后一个显示按纽在队列数组中的下标
		//		int index = -1;
		//		int size = array.size();
		//		for (int i = 0; i < size; i++) {
		//			FrameButton button = array.get(i);
		//			// 找到最后显示的按纽，然后向后移动一个，这是新的开始按纽
		//			if (button == last) {
		//				index = i + 1;
		//				break;
		//			}
		//		}
		//		
		//		
		//
		//		if (index < 0) {
		//			return;
		//		} else if (index >= size) {
		//			// 已经在最后了，这里不要显示
		//			return;
		//		}

		int size = array.size();
		// 没有了，忽略！
		if (size == 0) {
			return;
		}
		// 定位下标
		if (index < 0) {
			index = 0;
		} else if (index >= size) {
			index = 0;
		}

		// 滚动面板长度
		final int barWidth = banner.getWidth();
		// 计算面板可显示的按纽
		int buttons = doButtons(barWidth);

		// 结束位置
		int endIndex = (index + buttons >= size ? size : index + buttons);
		int beginIndex = (endIndex - buttons >= 0 ? endIndex - buttons : 0);

		//		// 取消全部的关联
		//		mouseTicker.cancelAll();

		// 清除全部按纽
		banner.removeAll();
		// 增加显示一个按纽
		for (int i = beginIndex; i < endIndex; i++) {
			FrameButton button = array.get(i);
			banner.setButtonSize(button);
			banner.add(button);
		}

		// 新增按纽后，重新计算左右按纽的有效
		doLeftEnabled();
		doRightEnabled();
	}


	/**
	 * 删除
	 * @param frame
	 */
	private void doRemove(LightFrame frame) {
		FrameButton button = findButton(frame);
		// 按纽没有找到，忽略
		if (button == null) {
			return;
		}

		// 解决关联焦点按纽
		mouseTicker.detach(button);
		// 从面板中删除
		banner.remove(button);

		// // 从数组中删除
		// int buttonIndex = array.indexOf(button);

		// 从数组中删除
		array.remove(button);

		// 找到第一个显示的应用
		FrameButton first = null;
		if (banner.getComponentCount() > 0) {
			first = (FrameButton) banner.getComponent(0);
		}
		int buttonIndex = (first != null ? array.indexOf(first) : -1);

		//		int butWidth = first.getWidth();

		boolean showing = isArrowShowing();
		// 如果在显示状态，重新计算它的显示结果
		if (showing) {
			int barWidth = getWidth(); // 当前面板长度
			// 按纽成员
			int buttons = array.size();
			//			int buttonsWidth = buttons * DesktopFrameBar.FRAMEBUTTON_WIDTH + (buttons - 1) * DesktopFrameBar.FRAMEBUTTON_GAP;
			//			int buttonsWidth = buttons * getButtonDefaultWidth() + (buttons - 1) * DesktopFrameBar.FRAMEBUTTON_GAP;

			int butWidth = getButtonDefaultWidth();
			int buttonsWidth = (buttons * butWidth) + ((buttons - 1) * DesktopFrameBar.FRAMEBUTTON_GAP);
			// 如果按纽宽度小于状态条，左右按纽取消
			if (buttonsWidth <= barWidth) {
				cmdLeft.setVisible(false);
				cmdRight.setVisible(false);

				// 删除全部后，重新加载
				banner.removeAll();
				// 重新加载
				for (FrameButton b : array) {
					banner.add(b);
				}
			} else {
				// 重新排列
				repaintRemoveButtons(buttonIndex);
			}
		} 

		// 刷新
		revalidate();
		repaint();
	}

	/**
	 * 设置选择窗口监听器
	 * @param s
	 */
	public void setSelectFrameListener(DesktopSelectFrameListener s) {
		frameListener = s;
	}

	/**
	 * 返回选择窗口监听器
	 * @return
	 */
	public DesktopSelectFrameListener getSelectFrameListener() {
		return frameListener;
	}

	/**
	 * 激活窗口
	 * @param frame
	 */
	private void doActivate(LightFrame frame) {
		for (FrameButton but : array) {
			if (but.getFrame() == frame) {
				// 激活焦点
				mouseTicker.activate(but);
			}
		}
	}

	/**
	 * 取消激活的窗口
	 * @param frame
	 */
	private void doDeactivate(LightFrame frame) {
		for (FrameButton but : array) {
			if (but.getFrame() == frame) {
				// 撤销焦点
				mouseTicker.deactivate(but);
			}
		}
	}

	/**
	 * 激活窗口
	 * 
	 * @author scott.liang
	 * @version 1.0 9/21/2021
	 * @since laxcus 1.0
	 */
	class ActivateThread extends SwingEvent {
		LightFrame frame;

		ActivateThread(LightFrame e) {
			super(true); // 同步处理
			frame = e;
		}

		public void process() {
			doActivate(frame);
		}
	}

	/**
	 * 取消激活
	 * 
	 * @author scott.liang
	 * @version 1.0 9/21/2021
	 * @since laxcus 1.0
	 */
	class DeactivateThread extends SwingEvent {
		LightFrame frame;

		DeactivateThread(LightFrame e) {
			super(true); // 同步处理
			frame = e;
		}

		public void process() {
			doDeactivate(frame);
		}
	}

	/**
	 * 注册应用
	 * 
	 * @author scott.liang
	 * @version 1.0 9/19/2021
	 * @since laxcus 1.0
	 */
	class RegisterThread extends SwingEvent {
		LightFrame frame;

		RegisterThread(LightFrame e) {
			super(true); // 同步处理
			frame = e;
		}

		public void process() {
			doAdd(frame);
		}
	}

	/**
	 * 注销应用
	 * 
	 * @author scott.liang
	 * @version 1.0 9/19/2021
	 * @since laxcus 1.0
	 */
	class UnregisterThread extends SwingEvent {
		LightFrame frame;

		UnregisterThread(LightFrame e) {
			super(true); // 同步处理
			frame = e;
		}

		public void process() {
			doRemove(frame);
		}
	}

	/**
	 * 激活
	 * @param frame
	 */
	public void activate(LightFrame frame) {
		addThread(new ActivateThread(frame));
	}

	/**
	 * 取消激活
	 * @param frame
	 */
	public void deactivate(LightFrame frame) {
		addThread(new DeactivateThread(frame));
	}

	/**
	 * 注册一个窗口，改成按纽显示 
	 * @param frame
	 */
	public void register(LightFrame frame) {
		addThread(new RegisterThread(frame));
	}

	/**
	 * 注销一个窗口
	 * @param frame
	 */
	public void unregister(LightFrame frame, boolean immediately) {
		if (immediately) {
			doRemove(frame);
		} else {
			addThread(new UnregisterThread(frame));
		}
	}

	//	/**
	//	 * 更新BANNER的UI界面
	//	 */
	//	private void updateBannerUI() {
	//		boolean success = (banner != null && array != null);
	//		if (!success) {
	//			return;
	//		}
	//		//		// 高度不一致时，更新
	//		//		int height = banner.getHeight();
	//		//		if (height != bannerHeight) {
	//		//			bannerHeight = height;
	//		//			banner.updateTree();
	//		//		}
	//
	////		banner.updateTree();
	//
	//		//		if (height != bannerHeight) {
	//		
	////					// 记录高度
	////					bannerHeight = height;
	//					
	////					banner.invalidate();
	////					banner.validate();
	////					banner.repaint();
	//		
	//		banner.updateTree();
	//		banner.repaint();
	//		
	//					// 调整按纽
	//					for (FrameButton but : array) {
	//						setFrameButtonSize(but);
	//						if (but.isVisible()) {
	////							but.updateTree();
	//							but.validate();
	//							but.repaint();
	//							
	////							but.invalidate();
	////							but.validate();
	////							but.repaint();
	//						}
	//					}
	//					
	//					banner.updateTree();
	//					banner.repaint();
	//		//		}
	//	}

	//	/**
	//	 * 更新BANNER的UI界面
	//	 */
	//	private void updateBannerUI() {
	//		boolean success = (banner != null && array != null);
	//		if (!success) {
	//			return;
	//		}
	////		// 高度不一致时，更新
	//		int height = banner.getHeight();
	////		if (height == bannerHeight) {
	////			return;
	////		}
	//		System.out.printf("UPATE Old %d -> New %d\n", bannerHeight, height);
	//		bannerHeight = height;
	//					
	////		banner.validate();
	////		banner.repaint();
	//		
	//
	//		// 调整按纽
	//		for (FrameButton but : array) {
	//			setFrameButtonSize(but);
	////			if (but.isVisible()) {
	////				but.validate();
	////				but.repaint();
	////			}
	//		}
	//
	//		banner.updateTree();
	//		banner.validate();
	//		banner.repaint();
	//	}

	private void updateButtonUI() {
		// 更新
		if (cmdLeft != null) {
			cmdLeft.updateUI();
		}
		if (cmdRight != null) {
			cmdRight.updateUI();
		}
		for (FrameButton button : array) {
			//			FontKit.setDefaultFont(button);
			button.updateUI();
		}
	}

	//	class UpdateButtonUIThread extends SwingEvent {
	//
	//		UpdateButtonUIThread() {
	//			super();
	//		}
	//
	//		public void process() {
	//			updateButtonUI();
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 更新
		if (cmdLeft != null && cmdRight != null) {
			//			addThread(new UpdateButtonUIThread());
			updateButtonUI();
		}
	}

}