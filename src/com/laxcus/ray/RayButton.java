/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;

import com.laxcus.application.manage.*;
import com.laxcus.container.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;

/**
 * 桌面图标按纽
 * 
 * @author scott.liang
 * @version 1.0 5/1/2022
 * @since laxcus 1.0
 */
public class RayButton extends JPanel {

	private static final long serialVersionUID = 1L;

	/** 双击间隔 **/
	static long DOUBLECLICK_INTERVAL = 2000L; 

	class TextMouseAdapter extends MouseAdapter {
		private boolean entered;
		private boolean press;

		public TextMouseAdapter() {
			super();
			entered = false;
			press = false;
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			entered = true;
			cmdIcon.setIcon(cmdIcon.getRolloverIcon());
			lblName.setForeground(textActiveForeground); //Color.WHITE);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			entered = false;
			if (press) {
				cmdIcon.setIcon(cmdIcon.getPressedIcon());
				lblName.setForeground(textActiveForeground); //Color.WHITE);
			} else {
				cmdIcon.setIcon(cmdIcon.getDisabledIcon());
				lblName.setForeground(textInactiveForeground);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			press = true;
			cmdIcon.setIcon(cmdIcon.getPressedIcon());
			lblName.setForeground(textActiveForeground); //Color.WHITE);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			press = false;
			if (entered) {
				cmdIcon.setIcon(cmdIcon.getRolloverIcon());
				lblName.setForeground(textActiveForeground); //Color.WHITE);
			} else {
				cmdIcon.setIcon(cmdIcon.getDisabledIcon());
				lblName.setForeground(textInactiveForeground);
			}
		}
	}
	
	class TextComponentAdapter extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			String text = lblName.getName();
			setText(text);
		}
	}

	class ButtonMouseAdapter extends MouseAdapter {

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
	 * 取消改名适配器
	 *
	 * @author scott.liang
	 * @version 1.0 6/9/2021
	 * @since laxcus 1.0
	 */
	class CancelRenameKeyAdapter extends KeyAdapter {

		/**
		 * 构造默认的取消改名适配器
		 */
		public CancelRenameKeyAdapter(){
			super();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_ENTER:
				cancelRename();
				break;
			}
		}
	}

	/**
	 * 弹出菜单适配器
	 *
	 * @author scott.liang
	 * @version 1.0 5/3/2022
	 * @since laxcus 1.0
	 */
	class MenuItemClick implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			click(e);
		}
	}
	
	/** 激活前景颜色 **/
	private Color textActiveForeground;

	/** 未激活的前景颜色 **/
	private Color textInactiveForeground;

	/** 焦点 **/
	private boolean focus;

	/** 图标按纽 **/
	private JButton cmdIcon;
	
	/** 显示文本 **/
	private JButton lblName;

	/** 文本 **/
	private JTextArea txtName;
	
	/** 参数 **/
	private RayButtonItem item;
	
	/** 弹出菜单 **/
	private JPopupMenu rockMenu;

	/**
	 * 构造默认的桌面图标按纽
	 */
	public RayButton() {
		super();
		// 初始化
		init();
	}

	/**
	 * 设置单元参数
	 * @param e DesktopButtonItem实例
	 */
	public void setItem(RayButtonItem e) {
		item = e;
	}

	/**
	 * 返回单元参数
	 * @return DesktopButtonItem实例
	 */
	public RayButtonItem getItem() {
		return item;
	}
	
	/**
	 * 更新文本颜色
	 */
	private void updateButtonColor() {
		// 文本未激活下的前景颜色
		Color c = UIManager.getColor("DesktopButton.TextInactiveForeground");
		if (c == null) {
			c = MetalLookAndFeel.getWindowTitleInactiveForeground();
		}
		if (c == null) {
			c = new Color(218, 218, 218);
		}
		// 未激活的颜色
		textInactiveForeground = new Color(c.getRGB());
		
		// 文本激活下的前景颜色
		c = UIManager.getColor("DesktopButton.TextActiveForeground");
		if (c == null) {
			c = MetalLookAndFeel.getWindowTitleForeground();
		}
		if (c == null) {
			c = new Color(218, 218, 218);
		}
		// 激活的颜色
		textActiveForeground = new Color(c.getRGB());		
	}

	/**
	 * 初始化参数
	 */
	private void init() {
		// 非焦点状态
		focus = false;
		// 更新文本颜色
		updateButtonColor();

		// 图标
		cmdIcon = new JButton();
		cmdIcon.setContentAreaFilled(false); // 平面
		cmdIcon.setBorderPainted(false); // 不绘制边框
		cmdIcon.setFocusPainted(false); // 不绘制焦点边框
		cmdIcon.setHorizontalAlignment(SwingConstants.CENTER);
		cmdIcon.setVerticalAlignment(SwingConstants.CENTER);

		// 文本
		lblName = new JButton();
		lblName.setContentAreaFilled(false); // 平面
		lblName.setBorderPainted(false); // 不绘制边框
		lblName.setFocusPainted(false); // 不绘制焦点边框
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setVerticalAlignment(SwingConstants.TOP);
		lblName.setBorder(new EmptyBorder(0, 2, 0, 2));
		lblName.setForeground(textInactiveForeground);
		lblName.addComponentListener(new TextComponentAdapter());
		lblName.setIconTextGap(0);
		
		txtName = new JTextArea();
		txtName.setBorder(new EmptyBorder(0, 0, 0, 0));
		txtName.setLineWrap(true);
		// txtContent.setOpaque(false); // 透明
		txtName.addKeyListener(new CancelRenameKeyAdapter());

		// 必须使用同一个，否则出错
		TextMouseAdapter textTracker = new TextMouseAdapter();
		cmdIcon.addMouseListener(textTracker);
		lblName.addMouseListener(textTracker);
		super.addMouseListener(textTracker);

		// 加入面板
		setLayout(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(1, 2, 2, 2));
		add(cmdIcon, BorderLayout.NORTH);
		add(lblName, BorderLayout.CENTER);

		// 要求透明
		setOpaque(false);

		// 绑定弹出菜单
		initPopupMenu();
	}

	/**
	 * 初始化弹出菜单
	 */
	private void initPopupMenu() {
		// 菜单
		rockMenu = new JPopupMenu();
		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.RunText", "DesktopButton.PopupMenu.RunMWord",
				null, "DesktopButton.PopupMenu.RunMethodName", new MenuItemClick()));
		rockMenu.addSeparator();
		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.DeleteText", "DesktopButton.PopupMenu.DeleteMWord",
				null, "DesktopButton.PopupMenu.DeleteMethodName", new MenuItemClick()));
		rockMenu.addSeparator();
		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.RenameText", "DesktopButton.PopupMenu.RenameMWord",
				null, "DesktopButton.PopupMenu.RenameMethodName", new MenuItemClick()));

		// 调用
		rockMenu.setInvoker(this);
		
		super.addMouseListener(new ButtonMouseAdapter());
		cmdIcon.addMouseListener(new ButtonMouseAdapter());
		lblName.addMouseListener(new ButtonMouseAdapter());
	}

	/**
	 * 菜单事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object object = event.getSource();
		// 必须是继承自“JMenuItem”
		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
			JMenuItem source = (JMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}

	/**
	 * 调用实例
	 * @param methodName
	 */
	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		}
	}

	/**
	 * 运行
	 */
	void doRun() {
		start();
	}

	class RunApplicationThread extends SwingEvent {
		WKey key;

		public RunApplicationThread(WKey e) {
			super();
			key = e;
		}

		@Override
		public void process() {
			int ret = -1;
			try {
				ret = ApplicationStarter.start(key);
			} catch (SecurityException e) {
				Logger.error(e);
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			} catch (IOException e) {
				Logger.error(e);
			} catch (InstantiationException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			} catch (NoSuchMethodException e) {
				Logger.error(e);
			} catch (InvocationTargetException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
			// 弹出错误
			if (ret != 0) {
				String title = UIManager.getString("ApplicationStart.startFaultTitle");
				String content = UIManager.getString("ApplicationStart.startFaultContent");
				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
			}
		}
	}

	/**
	 * 弹出错误
	 * @param title
	 * @param content
	 */
	private void showFault(String title, String content) {
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		desktop.playSound(SoundTag.ERROR);
		MessageBox.showFault(desktop, title, content);
	}

	/**
	 * 启动
	 */
	public void start() {
		if (item == null) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			showFault(title, content);
			return;
		}

		WKey key = item.getKey();

		// 不存在时
		if (!RTManager.getInstance().hasProgram(key)) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			showFault(title, content);
			return;
		}

		// 放入线程
		SwingDispatcher.invokeThread(new RunApplicationThread(key));

		//		RunApplicationThread rt = new RunApplicationThread(item.getKey());
		//		rt.execute();

		//		int ret = startApplication(item.getKey());
		//		if (ret != 0) {
		//			String title = UIManager.getString("ApplicationStart.startFaultTitle");
		//			String content = UIManager.getString("ApplicationStart.startFaultContent");
		//			showFault(title, content);
		//		}
	}

	void doDelete() {
		String title = UIManager.getString("DesktopButton.DeleteTitle"); // "删除按纽";
		String content = UIManager.getString("DesktopButton.DeleteConfirmText");
		content = String.format(content, item.getTitle());
		// 确认
		boolean allow = MessageBox.showYesNoDialog(PlatformKit.getPlatformDesktop(), title, content);
		if (!allow) {
			return;
		}

		WKey key = item.getKey();
		boolean success = RayController.getInstance().hasDesktopButton(key);
		if (success) {
			success = RayController.getInstance().deleteDesktopButton(key);
			if (success) {
				RTManager.getInstance().setShiftout(key, true);
			} else {
				title = UIManager.getString("DesktopButton.DeleteErrorTitle"); 
				content = UIManager.getString("DesktopButton.DeleteErrorContent");
				content = String.format(content, item.getTitle());
				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
			}
		}
	}

	void doRename() {
		String text = lblName.getName();
		txtName.setText(text);

		// 删除
		remove(lblName);
		// 保存
		add(txtName, BorderLayout.CENTER);

		validate();
		repaint();

		// 线程处理
		SwingDispatcher.invokeThread(new SelectAllThread());
	}
	
	class SelectAllThread extends SwingEvent {
		SelectAllThread() {
			super();
		}

		@Override
		public void process() {
			// 设置选中区域
			int len = txtName.getDocument().getLength();
			if (len > 0) {
				txtName.requestFocus();
				txtName.setCaretPosition(len);
			}
		}
	}
	
	void cancelRename() {
		String text = txtName.getText();
		if (text == null) {
			text = "";
		}
		
		// 过滤全部回车换行符
		text = text.replace('\n', (char) 0x20);
		text = text.replace('\r', (char) 0x20);

		// 过滤回车符
		String regex = "^\\s*([\\w\\W]+?)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.matches()) {
			text = matcher.group(1);
		}

		// 删除
		remove(txtName);
		// 保存
		add(lblName, BorderLayout.CENTER);
		// 撤销
		validate();
		repaint();

		// 设置文本
		setText(text);
		// 保存标题
		item.setTitle(text);
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

		// 如果是系统组件，不允许删除
		JMenuItem mi = MenuBuilder.findMenuItemByMethod(rockMenu, "doDelete");
		if (mi != null) {
			mi.setEnabled(item.isSystem() ? false : true);
		}

		int newX = e.getX();
		int newY = e.getY();
		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		if (cmdIcon != null) {
			cmdIcon.addMouseListener(l);
		}
		if (lblName != null) {
			lblName.addMouseListener(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void addMouseMotionListener(MouseMotionListener l) {
		super.addMouseMotionListener(l);
		if (cmdIcon != null) {
			cmdIcon.addMouseMotionListener(l);
		}
		if (lblName != null) {
			lblName.addMouseMotionListener(l);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		if (cmdIcon != null) {
			cmdIcon.addKeyListener(l);
		}
		if (lblName != null) {
			lblName.addKeyListener(l);
		}
	}

	/**
	 * 设置图标
	 * @param icon
	 * @param brighter
	 * @param dark
	 */
	public void setIcon(ImageIcon icon, int brighter, int dark) {
		cmdIcon.setIcon(icon);
		cmdIcon.setDisabledIcon(icon);

		// ESL的亮度增加
		if (brighter > 0) {
			// 亮
			ImageIcon image = ImageUtil.brighter(icon, brighter);
			if (image != null) {
				cmdIcon.setRolloverIcon(image);
			}
		}
		// 调暗
		if (dark < 0) {
			cmdIcon.setRolloverEnabled(true); // 浮入
			// 暗
			ImageIcon image = ImageUtil.dark(icon, dark);
			if (image != null) {
				cmdIcon.setPressedIcon(image);
			}
		}
	}

	/**
	 * 取消焦点
	 */
	private void cancelModify() {
		// 删除
		remove(txtName);
		// 增加
		add(lblName, BorderLayout.CENTER);
		// 刷新
		validate();
		repaint();
	}
	
	class CancelModifyThread extends SwingEvent {
		public CancelModifyThread() {
			super();
		}

		@Override
		public void process() {
			cancelModify();
		}
	}

	/**
	 * 设置按纽为焦点
	 * @param b
	 */
	public void setFocus(boolean b) {
		focus = b;
		repaint();

		// 获得焦点时触发
		if (focus) {
			RayController.getInstance().doCancelRename();
		}
		
		//		// 如果取消焦点，如果文本框可视时，同步恢复
		//		if (!focus && txtName.isShowing()) {
		//			SwingDispatcher.invokeThread(new CancelFocusThread());
		//		} else {
		//			repaint();
		//		}
	}

	/**
	 * 判断是焦点
	 * @return
	 */
	public boolean isFocus() {
		return focus;
	}

	/**
	 * 判断是修改模式
	 * @return 返回真或者假
	 */
	public boolean isRenameMode() {
		return txtName.isShowing();
	}

	/**
	 * 撤销改名状态
	 */
	public void doCancelRename() {
		if (isRenameMode()) {
			SwingDispatcher.invokeThread(new CancelModifyThread());
		}
	}

	/**
	 * 生成自动换行的HTML格式文本提示
	 * @param text 提示文本
	 * @param label 按纽
	 * @return 返回经过格式化处理的提示文本
	 */
	private String createText(String text, JButton label) {
		FontMetrics fm = label.getFontMetrics(label.getFont());
		int fontHeight = fm.getHeight();

		char[] words = text.toCharArray();
		int len = fm.charsWidth(words, 0, words.length);
		int width = label.getWidth();
		int height = label.getHeight();

		// 小于指定宽度
		if (len < width) {
			return text;
		}
		
		// 两边空格
		width -= 10;
		
		// 最少一行
		int rows = height / fontHeight;
		if (rows < 1) rows = 1;
		
		// 总长度
		int length = width * rows;
		
		String title = null;
		int start = 0;
		int last = 0;
		
		for (last++; last < words.length; last++) {
			len = fm.charsWidth(words, start, last - start);
			if (len >= length) {
				title = text.substring(start, (last > 2 ? last - 2 : last - 1));
				title = title + "...";
				break;
			}
		}

		if (title == null) {
			title = text;
		}

		return String.format("<html><center>%s</center></html>", title);
	}
	
	/**
	 * 设置文本
	 * @param text
	 */
	public void setText(String text) {
		String html = createText(text, lblName);

		lblName.setText(html);
		lblName.setName(text);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */
	@Override
	public void setToolTipText(String text) {
		super.setToolTipText(text);
		if (cmdIcon != null) {
			cmdIcon.setToolTipText(text);
		}
		if (lblName != null) {
			lblName.setToolTipText(text);
		}
	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//
//		if (focus) {
//			Color old = g.getColor();
//
////			int x1 = 0;
////			int y1 = 0;
////			int x2 = x1 + getWidth() - 1;
////			int y2 = y1 + getHeight() - 1;
//			
//			int x1 = 1;
//			int y1 = 1;
//			int x2 = x1 + getWidth() - 2;
//			int y2 = y1 + getHeight() - 2;
//
//			g.setColor(Color.WHITE);
//
//			for (int x = 0; x < x2; x += 2) {
//				g.drawLine(x, y1, x, y1); // 上横线点
//				g.drawLine(x, y2, x, y2); // 下横线点
//			}
//			for (int y = 0; y < y2; y += 2) {
//				g.drawLine(x1, y, x1, y); // 左侧线点
//				g.drawLine(x2, y, x2, y); // 右侧线点
//			}
//
//			//			// 测试..
//			//			int w = getWidth();
//			//			int h = getHeight();
//			//			g.drawRoundRect(0, 0, w-1, h-1, 12, 12);
//
//			// 恢复原来的颜色
//			g.setColor(old);
//		}
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//
//		if (focus) {
//			Color old = g.getColor();
//			
//			int x1 = 1;
//			int y1 = 1;
//			int x2 = x1 + getWidth() - 2;
//			int y2 = y1 + getHeight() - 2;
//
//			g.setColor(Color.WHITE);
//
//			for (int x = x1; x <= x2; x += 2) {
//				g.drawLine(x, y1, x, y1); // 上横线点
//				g.drawLine(x, y2, x, y2); // 下横线点
//			}
//			for (int y = y1; y <= y2; y += 2) {
//				g.drawLine(x1, y, x1, y); // 左侧线点
//				g.drawLine(x2, y, x2, y); // 右侧线点
//			}
//
//			// 恢复原来的颜色
//			g.setColor(old);
//		}
//	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (focus) {
			Color old = g.getColor();

			int x1 = 0;
			int y1 = 0;
			int x2 = x1 + getWidth() - 1;
			int y2 = y1 + getHeight() - 1;

			g.setColor(Color.WHITE);

			for (int x = 0; x <= x2; x += 2) {
				g.drawLine(x, y1, x, y1); // 上横线点
				g.drawLine(x, y2, x, y2); // 下横线点
			}
			for (int y = 0; y <= y2; y += 2) {
				g.drawLine(x1, y, x1, y); // 左侧线点
				g.drawLine(x2, y, x2, y); // 右侧线点
			}

			// 恢复原来的颜色
			g.setColor(old);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.desktop.status.CraftButton#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		if (rockMenu != null) {
			FontKit.updateDefaultFonts(rockMenu, true);
			rockMenu.updateUI();
		}
		
		// 更新
		if (lblName != null) {
			// 更新颜色
			updateButtonColor();
			
			lblName.updateUI();
			lblName.setForeground(textInactiveForeground);
			String text = lblName.getName();
			setText(text);
		}
		// 更新
		if (txtName != null) {
			txtName.updateUI();
		}
	}

}


///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license Laxcus Public License (LPL)
// */
//package com.laxcus.ray;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.io.*;
//import java.lang.reflect.*;
//
//import javax.swing.*;
//import javax.swing.border.*;
//
//import com.laxcus.application.manage.*;
//import com.laxcus.container.*;
//import com.laxcus.gui.component.*;
//import com.laxcus.gui.dialog.message.*;
//import com.laxcus.log.client.*;
//import com.laxcus.platform.*;
//import com.laxcus.util.*;
//import com.laxcus.util.display.*;
//import com.laxcus.util.event.*;
//import com.laxcus.util.sound.*;
//
///**
// * 桌面图标按纽
// * 
// * @author scott.liang
// * @version 1.0 5/31/2021
// * @since laxcus 1.0
// */
//final class RayButton extends CraftButton {
//
//	private static final long serialVersionUID = -7696835544565522221L;
//
//	/** 边框颜色 **/
//	private static Color borderColor = new Color(197, 145, 90);
//
//	private static Color darkText = new Color(224, 224, 224);
//
//	/** 焦点 **/
//	private boolean focus;
//
//	private RayButtonItem item;
//
//	/** 弹出菜单 **/
//	private JPopupMenu rockMenu;
//
//	class ButtonMouseAdapter extends MouseAdapter {
//
//		@Override
//		public void mousePressed(MouseEvent e) {
//			showPopupMenu(e);
//		}
//
//		@Override
//		public void mouseReleased(MouseEvent e) {
//			showPopupMenu(e);
//		}
//	}
//
//	/**
//	 * 构造默认的桌面图标按纽
//	 */
//	public RayButton() {
//		super();
//		init();
//	}
//
//	/**
//	 * 构造桌面图标按纽，按定文本和图标
//	 * @param text
//	 * @param icon
//	 */
//	public RayButton(String text) {
//		this();
//		setText(text);
//
//		// super(text);
//		// // 初始化
//		// init();
//	}
//
//	/**
//	 * 设置单元参数
//	 * @param e DesktopButtonItem实例
//	 */
//	public void setItem(RayButtonItem e) {
//		item = e;
//	}
//
//	/**
//	 * 返回单元参数
//	 * @return DesktopButtonItem实例
//	 */
//	public RayButtonItem getItem() {
//		return item;
//	}
//
//	/**
//	 * 设置图标
//	 * @param icon
//	 * @param brighter 高亮增加值
//	 * @param dark 暗色
//	 */
//	public void setIcon(ImageIcon icon, int brighter, int dark) {
//		super.setIcon(icon);
//
//		//		// ESL的亮度增加
//		//		if (light > 0) {
//		//			ImageIcon image = brighter(icon, light);
//		//			if (image != null) {
//		//				super.setPressedIcon(image);
//		//				super.setSelectedIcon(image);
//		//				super.setRolloverIcon(image);
//		//				super.setRolloverSelectedIcon(image);
//		//			}
//		//		}
//
//		// ESL的亮度增加
//		if (brighter > 0) {
//			// 亮
//			ImageIcon image = brighter(icon, brighter);
//			if (image != null) {
//				// super.setPressedIcon(image);
//				super.setRolloverIcon(image);
//			}
//		}
//		// 调暗
//		if (dark < 0) {
//			this.setRolloverEnabled(true); // 浮入
//			// 暗
//			ImageIcon image = dark(icon, dark);
//			if (image != null) {
//				super.setPressedIcon(image);
//				// super.setSelectedIcon(image);
//				//				 super.setRolloverSelectedIcon(image);
//			}
//		}
//	}
//
//	//	private static Color darkText = new Color(192, 192, 192);
//	//	
//	//	/**
//	//	 * 设置按纽为焦点
//	//	 * @param b
//	//	 */
//	//	public void setFocus(boolean b) {
//	//		focus = b;
//	//		if (focus) {
//	//			setForeground(Color.WHITE);
//	//		} else {
//	////			setForeground(new Color(233, 233, 233));
//	//			setForeground(darkText); // new Color(233, 233, 233));
//	//		}
//	//	}
//
//
//	/**
//	 * 设置按纽为焦点
//	 * @param b
//	 */
//	public void setFocus(boolean b) {
//		focus = b;
//		if (focus) {
//			setForeground(Color.WHITE);
//		} else {
//			setForeground(darkText);
//		}
//	}
//
//	/**
//	 * 判断是焦点
//	 * @return
//	 */
//	public boolean isFocus() {
//		return focus;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.AbstractButton#setText(java.lang.String)
//	 */
//	@Override
//	public void setText(String text) {
//		String html = String.format("<html><body><center>%s</center></body></html>", text);
//		super.setText(html);
//	}
//
//	/**
//	 * 初始化参数
//	 */
//	private void init() {
//		setFocus(false);
//		setContentAreaFilled(false); // 平面
//		setBorderPainted(false); // 不绘制边框
//		setFocusPainted(false); // 不绘制焦点边框
//
//		setVerticalTextPosition(SwingConstants.BOTTOM); // 文本在图标的下面
//
//		setHorizontalTextPosition(SwingConstants.CENTER); // 居中布置
//
//		setVerticalAlignment(SwingConstants.TOP); // 图文从下向下
//
//		setBorder(new EmptyBorder(4, 4, 4, 4));
//
//		// 绑定弹出菜单
//		initPopupMenu();
//
//		//		// 黑色字体
//		//		setForeground(Color.black);
//	}
//
//	/**
//	 * 初始化弹出菜单
//	 */
//	private void initPopupMenu() {
//		// 菜单
//		rockMenu = new JPopupMenu();
//		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.RunText", "DesktopButton.PopupMenu.RunMWord",
//				null, "DesktopButton.PopupMenu.RunMethodName", new MenuItemClick()));
//		rockMenu.addSeparator();
//		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.DeleteText", "DesktopButton.PopupMenu.DeleteMWord",
//				null, "DesktopButton.PopupMenu.DeleteMethodName", new MenuItemClick()));
//		rockMenu.addSeparator();
//		rockMenu.add(MenuBuilder.createMenuItem(null, "DesktopButton.PopupMenu.RenameText", "DesktopButton.PopupMenu.RenameMWord",
//				null, "DesktopButton.PopupMenu.RenameMethodName", new MenuItemClick()));
//
//		// 调用
//		rockMenu.setInvoker(this);
//		addMouseListener(new ButtonMouseAdapter());
//	}
//
//	class MenuItemClick implements ActionListener {
//
//		/* (non-Javadoc)
//		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//		 */
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			click(e);
//		}
//	}
//
//	/**
//	 * 菜单事件
//	 * @param event
//	 */
//	private void click(ActionEvent event) {
//		Object object = event.getSource();
//		// 必须是继承自“JMenuItem”
//		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
//			JMenuItem source = (JMenuItem) object;
//			String methodName = source.getName();
//			invoke(methodName);
//		}
//	}
//
//	/**
//	 * 调用实例
//	 * @param methodName
//	 */
//	private void invoke(String methodName) {
//		if (methodName == null || methodName.isEmpty()) {
//			return;
//		}
//
//		try {
//			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
//			method.invoke(this, new Object[0]);
//		} catch (NoSuchMethodException e) {
//			Logger.error(e);
//		} catch (IllegalArgumentException e) {
//			Logger.error(e);
//		} catch (IllegalAccessException e) {
//			Logger.error(e);
//		} catch (InvocationTargetException e) {
//			Logger.error(e);
//		}
//	}
//
//	/**
//	 * 运行
//	 */
//	void doRun() {
//		start();
//	}
//
//	class RunApplicationThread extends SwingEvent {
//		WKey key;
//
//		public RunApplicationThread(WKey e) {
//			super();
//			key = e;
//		}
//
//		@Override
//		public void process() {
//			int ret = -1;
//			try {
//				ret = ApplicationStarter.start(key);
//			} catch (SecurityException e) {
//				Logger.error(e);
//			} catch (IllegalArgumentException e) {
//				Logger.error(e);
//			} catch (IOException e) {
//				Logger.error(e);
//			} catch (InstantiationException e) {
//				Logger.error(e);
//			} catch (IllegalAccessException e) {
//				Logger.error(e);
//			} catch (NoSuchMethodException e) {
//				Logger.error(e);
//			} catch (InvocationTargetException e) {
//				Logger.error(e);
//			} catch (Throwable e) {
//				Logger.fatal(e);
//			}
//			// 弹出错误
//			if (ret != 0) {
//				String title = UIManager.getString("ApplicationStart.startFaultTitle");
//				String content = UIManager.getString("ApplicationStart.startFaultContent");
//				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//			}
//		}
//	}
//
//	//	class RunApplicationThread extends SwingWorker<Integer, Object> {
//	//		WKey key;
//	//
//	//		RunApplicationThread(WKey e) {
//	//			super();
//	//			key = e;
//	//		}
//	//
//	//		/**
//	//		 * 启动应用
//	//		 * @param input 输入命令
//	//		 * @return 成功返回真，否则假
//	//		 */
//	//		private int startApplication(WKey key) {
//	//			try {
//	//				return ApplicationStarter.start(key);
//	//			} catch (SecurityException e) {
//	//				Logger.error(e);
//	//			} catch (IllegalArgumentException e) {
//	//				Logger.error(e);
//	//			} catch (IOException e) {
//	//				Logger.error(e);
//	//			} catch (InstantiationException e) {
//	//				Logger.error(e);
//	//			} catch (IllegalAccessException e) {
//	//				Logger.error(e);
//	//			} catch (NoSuchMethodException e) {
//	//				Logger.error(e);
//	//			} catch (InvocationTargetException e) {
//	//				Logger.error(e);
//	//			}
//	//			return -1;
//	//		}
//	//	
//	//		/* (non-Javadoc)
//	//		 * @see javax.swing.SwingWorker#doInBackground()
//	//		 */
//	//		@Override
//	//		protected Integer doInBackground() throws Exception {
//	//			int ret = startApplication(key);
//	//			return new Integer(ret);
//	//		}
//	//
//	//		/*
//	//		 * (non-Javadoc)
//	//		 * @see javax.swing.SwingWorker#done()
//	//		 */
//	//		@Override
//	//		protected void done() {
//	//			Integer ret = null;
//	//			try {
//	//				ret = get();
//	//			} catch (Exception e) {
//	//				Logger.error(e);
//	//			}
//	//
//	//			// 以下情况，弹出错误
//	//			boolean failed = (ret == null || ret.intValue() != 0);
//	//			if (failed) {
//	//				String title = UIManager.getString("ApplicationStart.startFaultTitle");
//	//				String content = UIManager.getString("ApplicationStart.startFaultContent");
//	//				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
//	//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//	//			}
//	//		}
//	//	}
//
////	/**
////	 * 启动应用
////	 * @param input 输入命令
////	 * @return 成功返回真，否则假
////	 */
////	private int startApplication(WKey key) {
////		try {
////			return ApplicationStarter.start(key);
////		} catch (SecurityException e) {
////			Logger.error(e);
////		} catch (IllegalArgumentException e) {
////			Logger.error(e);
////		} catch (IOException e) {
////			Logger.error(e);
////		} catch (InstantiationException e) {
////			Logger.error(e);
////		} catch (IllegalAccessException e) {
////			Logger.error(e);
////		} catch (NoSuchMethodException e) {
////			Logger.error(e);
////		} catch (InvocationTargetException e) {
////			Logger.error(e);
////		}
////		return -1;
////	}
//
//	/**
//	 * 弹出错误
//	 * @param title
//	 * @param content
//	 */
//	private void showFault(String title, String content) {
//		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
//		desktop.playSound(SoundTag.ERROR);
//		MessageBox.showFault(desktop, title, content);
//	}
//
//	/**
//	 * 启动
//	 */
//	public void start() {
//		if (item == null) {
//			String title = UIManager.getString("ApplicationStart.notfoundTitle");
//			String content = UIManager.getString("ApplicationStart.notfoundContent");
//			showFault(title, content);
//			return;
//		}
//
//		WKey key = item.getKey();
//
//		// 不存在时
//		if (!RTManager.getInstance().hasProgram(key)) {
//			String title = UIManager.getString("ApplicationStart.notfoundTitle");
//			String content = UIManager.getString("ApplicationStart.notfoundContent");
//			showFault(title, content);
//			return;
//		}
//
//		// 放入线程
//		SwingDispatcher.invokeThread(new RunApplicationThread(key));
//
//		//		RunApplicationThread rt = new RunApplicationThread(item.getKey());
//		//		rt.execute();
//
//		//		int ret = startApplication(item.getKey());
//		//		if (ret != 0) {
//		//			String title = UIManager.getString("ApplicationStart.startFaultTitle");
//		//			String content = UIManager.getString("ApplicationStart.startFaultContent");
//		//			showFault(title, content);
//		//		}
//	}
//
//	void doDelete() {
//		String title = UIManager.getString("DesktopButton.DeleteTitle"); // "删除按纽";
//		String content = UIManager.getString("DesktopButton.DeleteConfirmText");
//		content = String.format(content, item.getTitle());
//		// 确认
//		boolean allow = MessageBox.showYesNoDialog(PlatformKit.getPlatformDesktop(), title, content);
//		if (!allow) {
//			return;
//		}
//
//		WKey key = item.getKey();
//		boolean success = RayController.getInstance().hasDesktopButton(key);
//		if (success) {
//			success = RayController.getInstance().deleteDesktopButton(key);
//			if (success) {
//				RTManager.getInstance().setShiftout(key, true);
//			} else {
//				title = UIManager.getString("DesktopButton.DeleteErrorTitle"); 
//				content = UIManager.getString("DesktopButton.DeleteErrorContent");
//				content = String.format(content, item.getTitle());
//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//			}
//		}
//	}
//
//	void doRename() {
//		NameInputDialog dlg = new NameInputDialog(UIManager.getString("DesktopButton.InputDialog.RenameTitle"));
//		dlg.setApproveText(UIManager.getString("DesktopButton.InputDialog.RenameText"));
//		dlg.setInitInputText(item.getTitle());
//
//		String text = (String) dlg.showDialog(PlatformKit.getPlatformDesktop());
//		if (text == null) {
//			return;
//		}
//
//		// 设置新的名称
//		item.setTitle(text);
//		setText(text);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
//	 */
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//
//		if (focus) {
//			Color back = g.getColor();
//
//			int x1 = 0;
//			int y1 = 0;
//			int x2 = x1 + getWidth() - 1;
//			int y2 = y1 + getHeight() - 1;
//
//			// 边框颜色
//			if (isNimbusUI()) {
//				g.setColor(borderColor);
//			} else {
//				g.setColor(Color.WHITE);
//			}
//
//			for (int x = 0; x < x2; x += 2) {
//				g.drawLine(x, y1, x, y1); // 上横线点
//				g.drawLine(x, y2, x, y2); // 下横线点
//			}
//			for (int y = 0; y < y2; y += 2) {
//				g.drawLine(x1, y, x1, y); // 左侧线点
//				g.drawLine(x2, y, x2, y); // 右侧线点
//			}
//
//			//			// 测试..
//			//			int w = this.getWidth();
//			//			int h = this.getHeight();
//			//			g.drawRoundRect(0, 0, w-1, h-1, 12, 12);
//
//			// 恢复原来的颜色
//			g.setColor(back);
//		}
//
//	}
//
//	/**
//	 * 显示弹出菜单
//	 * @param e
//	 */
//	private void showPopupMenu(MouseEvent e) {
//		// 不满足SWING条件的POPUP触发，不处理
//		if (!e.isPopupTrigger()) {
//			return;
//		}
//
//		// 如果是系统组件，不允许删除
//		JMenuItem mi = MenuBuilder.findMenuItemByMethod(rockMenu, "doDelete");
//		if (mi != null) {
//			mi.setEnabled(item.isSystem() ? false : true);
//		}
//
//		//		// 还原
//		//		JMenuItem item = findMenuItemByMethod("doMenuRestore");
//		//		if (item != null) {
//		//			item.setEnabled(hasMenuRestore());
//		//		}
//		//		item = findMenuItemByMethod("doMenuMini");
//		//		if (item != null) {
//		//			item.setEnabled(hasMenuMini());
//		//		}
//		//		item = findMenuItemByMethod("doMenuMax");
//		//		if (item != null) {
//		//			item.setEnabled(hasMenuMax());
//		//		}
//
//		int newX = e.getX();
//		int newY = e.getY();
//		rockMenu.show(rockMenu.getInvoker(), newX, newY);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.desktop.status.CraftButton#updateUI()
//	 */
//	@Override
//	public void updateUI() {
//		super.updateUI();
//
//		if (rockMenu != null) {
//			FontKit.updateDefaultFonts(rockMenu, true);
//			rockMenu.updateUI();
//		}
//	}
//}
//
//
/////**
//// * 构造桌面图标按纽，按定文本和图标
//// * @param text
//// * @param icon
//// */
////public DesktopButton(String text, ImageIcon icon) {
////	super(text);
////	// 初始化
////	init();
////	setIcon(icon);
////}
//
/////**
//// * 图标高度显示
//// * @param image
//// * @param flag ESL的亮度
//// * @return 
//// */
////private ImageIcon brighter(ImageIcon image, double flag) {
////	int width = image.getIconWidth();
////	int height = image.getIconHeight();
////	
////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics g = sourceBI.createGraphics();
////	g.drawImage(image.getImage(), 0, 0, width, height, null); 
////
////	BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	for (int x = 0; x < width; x++) {
////		for (int y = 0; y < height; y++) {
////			RGB rgb = new RGB(sourceBI.getRGB(x, y));
//////			ESL esl = rgb.toESL();
//////			// 加亮
//////			esl.brighter(flag);
//////			// 加亮后的返回值
//////			int value = esl.toRGB().getRGB();
////			
////			int value = rgb.getRGB();
////
////			// 加亮调整后的颜色
////			newBI.setRGB(x, y, value);
////		}
////	}
////	
////	// 转成输出流
////	try {
//////		// 透明转换
//////		Graphics2D gra = newBI.createGraphics();
//////		newBI = gra.getDeviceConfiguration().createCompatibleImage(width, width, Transparency.TRANSLUCENT);
////
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(newBI, "png", imOut);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
////}
//
/////**
//// * 图标高度显示
//// * @param image
//// * @param flag ESL的亮度
//// * @return 
//// */
////private ImageIcon brighter3(ImageIcon image, double flag) {
//////	int width = image.getIconWidth();
//////	int height = image.getIconHeight();
////	
//////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//////	Graphics g = sourceBI.createGraphics();
//////	g.drawImage(image.getImage(), 0, 0, width, height, null);
////	
////	BufferedImage sourceBI = null;
////	try {
////		sourceBI = ImageIO.read(new File("g:/desktop/icon/work2.png"));
////	} catch (Exception e) {
////		e.printStackTrace();
////	}
////	int width = sourceBI.getWidth();
////	int height = sourceBI.getHeight();
////	
////	// 平滑缩小图象
////	Image compressImage = sourceBI.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
////	// 测试
////	Graphics2D gra = sourceBI.createGraphics();
////	sourceBI = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////	sourceBI.getGraphics().drawImage(compressImage, 0, 0, null);
////	
////	for (int x = 0; x < width; x++) {
////		for (int y = 0; y < height; y++) {
////			RGB rgb = new RGB(sourceBI.getRGB(x, y));
////			//			ESL esl = rgb.toESL();
////			//			// 加亮
////			//			esl.brighter(flag);
////			//			// 加亮后的返回值
////			//			int value = esl.toRGB().getRGB();
////
////			int value = rgb.getRGB();
////
////			// 加亮调整后的颜色
////			sourceBI.setRGB(x, y, value);
////		}
////	}
////	
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageIO.write(sourceBI, "png", bs);
////		bs.flush();
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
////	
//////	// 转成输出流
//////	try {
//////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//////		ImageIO.write(sourceBI, "png", imOut);
//////		byte[] b = bs.toByteArray();
//////		// 输出为图像对象
//////		return new ImageIcon(b);
//////	} catch (IOException e) {
//////		e.printStackTrace();
//////	}
//////	return null;
////	
//////	BufferedImage newBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//////	for (int x = 0; x < width; x++) {
//////		for (int y = 0; y < height; y++) {
//////			RGB rgb = new RGB(sourceBI.getRGB(x, y));
////////			ESL esl = rgb.toESL();
////////			// 加亮
////////			esl.brighter(flag);
////////			// 加亮后的返回值
////////			int value = esl.toRGB().getRGB();
//////			
//////			int value = rgb.getRGB();
//////
//////			// 加亮调整后的颜色
//////			newBI.setRGB(x, y, value);
//////		}
//////	}
//////	
//////	// 转成输出流
//////	try {
////////		// 透明转换
////////		Graphics2D gra = newBI.createGraphics();
////////		newBI = gra.getDeviceConfiguration().createCompatibleImage(width, width, Transparency.TRANSLUCENT);
//////
//////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//////		ImageIO.write(newBI, "png", imOut);
//////		byte[] b = bs.toByteArray();
//////		// 输出为图像对象
//////		return new ImageIcon(b);
//////	} catch (IOException e) {
//////		e.printStackTrace();
//////	}
//////	return null;
////}
//
/////**
//// * 图标高度显示
//// * @param image
//// * @param flag ESL的亮度
//// * @return 
//// */
////private ImageIcon brighter(ImageIcon image, double flag) {
//////	int width = image.getIconWidth();
//////	int height = image.getIconHeight();
//////	
//////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//////	Graphics g = sourceBI.createGraphics();
//////	g.drawImage(image.getImage(), 0, 0, width, height, null); 
////
////	BufferedImage sourceBI = null;
////	try {
////		sourceBI = ImageIO.read(new File("g:/desktop/icon/work2.png"));
////	} catch (Exception e) {
////		e.printStackTrace();
////	}
////	int width = sourceBI.getWidth();
////	int height = sourceBI.getHeight();
////	
//////	try {
//////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//////		ImageIO.write(image.getImage(), "png", bs);
//////		byte[] b = bs.toByteArray();
//////		// 输出为图像对象
//////		return new ImageIcon(b);
//////	} catch (IOException e) {
//////		e.printStackTrace();
//////	}
////	
////	// 透明色
////	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics2D gra = buff.createGraphics();
////	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////
////	// 如果是0，忽略。如果是其它颜色，加亮
////	for (int x = 0; x < width; x++) {
////		for (int y = 0; y < height; y++) {
////			int value = sourceBI.getRGB(x, y);
////			if (value == 0) {
////				// 透明
////				buff.setRGB(x, y, 0);
////			} else {
////				RGB rgb = new RGB(value);
////				ESL esl = rgb.toESL();
////				// 加亮
////				esl.brighter(flag);
////				// 加亮后的返回值
////				value = esl.toRGB().getRGB();
////				buff.setRGB(x, y, value);
////			}
////		}
////	}
////	
////	// 转成输出流
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(buff, "png", bs);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
////}
//
////private byte[] compress(String xmlPath, int width, int height) throws IOException  {
////	ResourceLoader loader = new ResourceLoader();
////	byte[] b = loader.findAbsoluteStream(xmlPath);
////	ByteArrayInputStream in = new ByteArrayInputStream(b);
////	BufferedImage img = ImageIO.read(in);
////	
////	// 平滑缩小图象
////	Image compressImage = img.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
////	// 生成一个新图像
////	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics2D gra = buff.createGraphics();
////	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////
////	buff.getGraphics().drawImage(compressImage, 0, 0, null);
////
////	// 写入磁盘
////	ByteArrayOutputStream out = new ByteArrayOutputStream();
////	ImageIO.write(buff, "PNG", out);
////	out.flush();
////	// 生成图像
////	b = out.toByteArray();
////	
////	// 关闭
////	out.close();
////	in.close();
////	
////	return b;
////}
//
/////**
//// * 图标高度显示
//// * @param image
//// * @param flag ESL的亮度
//// * @return 
//// */
////private ImageIcon brighter(String xmlPath, double flag) {
//////	ResourceLoader loader = new ResourceLoader();
//////	byte[] b = loader.findAbsoluteStream(xmlPath);
//////
//////	BufferedImage sourceBI = null;
//////	try {
//////		sourceBI = ImageIO.read(new ByteArrayInputStream(b));
//////	} catch (Exception e) {
//////		e.printStackTrace();
//////	}
//////	int width = sourceBI.getWidth();
//////	int height = sourceBI.getHeight();
////
//////	try {
//////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//////		ImageIO.write(image.getImage(), "png", bs);
//////		byte[] b = bs.toByteArray();
//////		// 输出为图像对象
//////		return new ImageIcon(b);
//////	} catch (IOException e) {
//////		e.printStackTrace();
//////	}
////	
//////	ResourceLoader loader = new ResourceLoader();
//////	byte[] b = loader.findAbsoluteStream(xmlPath);
//////	ImageIcon image = new ImageIcon(b);
//////	int width = image.getIconWidth();
//////	int height = image.getIconHeight();
//////	Image im = image.getImage().getScaledInstance(width, height,Image.SCALE_SMOOTH);
//////	
//////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//////	Graphics2D gd = sourceBI.createGraphics();
//////	sourceBI = gd.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////////	Graphics g = sourceBI.createGraphics();
//////	gd.drawImage(image.getImage(), 0, 0, width, height, null); 
////
////	
////	
////	
////	BufferedImage sourceBI = null;
////	try {
////		byte[] b = compress(xmlPath, 32, 32); // loader.findAbsoluteStream(xmlPath);
////		sourceBI = ImageIO.read(new ByteArrayInputStream(b));
////	} catch (Exception e) {
////		e.printStackTrace();
////	}
////	int width = sourceBI.getWidth();
////	int height = sourceBI.getHeight();
////
////	// 透明色
////	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics2D gra = buff.createGraphics();
////	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////
////	// 如果是0，忽略。如果是其它颜色，加亮
////	for (int x = 0; x < width; x++) {
////		for (int y = 0; y < height; y++) {
////			int value = sourceBI.getRGB(x, y);
////			if (value == 0) {
////				// 透明
////				buff.setRGB(x, y, 0);
////			} else {
////				RGB rgb = new RGB(value);
////				ESL esl = rgb.toESL();
////				// 加亮
////				esl.brighter(flag);
////				// 加亮后的返回值
////				value = esl.toRGB().getRGB();
////				buff.setRGB(x, y, value);
////			}
////		}
////	}
////	
////	// 转成输出流
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//////		ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
////		ImageIO.write(buff, "png", bs);
////		 byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
////}
//
////private BufferedImage createBufferedImage(ImageIcon image) {
////	int width = image.getIconWidth();
////	int height = image.getIconHeight();
////	
////	// 生成一个新图像
////	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics2D gra = buff.createGraphics();
////	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////	buff.getGraphics().drawImage(image.getImage(), 0, 0, null);
////
////	// 写入磁盘
////	try {
////		ByteArrayOutputStream out = new ByteArrayOutputStream();
////		ImageIO.write(buff, "PNG", out);
////		out.flush();
////		// 生成图像
////		byte[] b = out.toByteArray();
////		return ImageIO.read(new ByteArrayInputStream(b));
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
////}
/////**
//// * 图标高度显示
//// * @param image
//// * @param flag ESL的亮度
//// * @return 
//// */
////private ImageIcon light(ImageIcon image, double flag) {
//////	int width = image.getIconWidth();
//////	int height = image.getIconHeight();
////	
//////	BufferedImage sourceBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//////	Graphics g = sourceBI.createGraphics();
//////	g.drawImage(image.getImage(), 0, 0, width, height, null);
////	
//////	BufferedImage sourceBI = null;
//////	try {
//////		sourceBI = ImageIO.read(new File("g:/desktop/icon/work2.png"));
//////	} catch (Exception e) {
//////		e.printStackTrace();
//////	}
//////	int width = sourceBI.getWidth();
//////	int height = sourceBI.getHeight();
////
////	// 生成图像
////	BufferedImage sourceBI = createBufferedImage(image);
////	int width = sourceBI.getWidth();
////	int height = sourceBI.getHeight();
////
////	// 透明色
////	BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////	Graphics2D gra = buff.createGraphics();
////	buff = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////
////	// 如果是0，忽略。如果是其它颜色，加亮
////	for (int x = 0; x < width; x++) {
////		for (int y = 0; y < height; y++) {
////			int value = sourceBI.getRGB(x, y);
////			if (value == 0) {
////				// 透明
////				buff.setRGB(x, y, 0);
////			} else {
////				RGB rgb = new RGB(value);
////				ESL esl = rgb.toESL();
////				// 加亮
////				esl.brighter(flag);
////				// 加亮后的返回值
////				value = esl.toRGB().getRGB();
////				buff.setRGB(x, y, value);
////			}
////		}
////	}
////	
////	// 转成输出流
////	try {
////		ByteArrayOutputStream bs = new ByteArrayOutputStream();
////		ImageIO.write(buff, "png", bs);
////		byte[] b = bs.toByteArray();
////		// 输出为图像对象
////		return new ImageIcon(b);
////	} catch (IOException e) {
////		e.printStackTrace();
////	}
////	return null;
////}
//
/////**
//// * 设置图标
//// * @param icon
//// */
////public void setStateIcon(String xmlPath) {
//////	super.setIcon(icon);
////
////	// ESL的亮度增加50
////	ImageIcon image = brighter(xmlPath, 50);
////	if (image != null) {
////		super.setPressedIcon(image);
////		super.setSelectedIcon(image);
////		super.setRolloverIcon(image);
////		super.setRolloverSelectedIcon(image);
////	}
////}
//
//
////private void drawDashedLine(Graphics g, int x1, int x2, int y1, int y2) {
////	Graphics2D g2 = (Graphics2D)g.create();
////	Stroke dashed= new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
////	g2.setStroke(dashed);
////	g2.drawLine(x1, y2, x2, y2);
////	g2.dispose();
////}
//
//
/////**
//// * @param arg0
//// */
////public DesktopButton(Icon arg0) {
////	super(arg0);
////	// TODO Auto-generated constructor stub
////}
////
/////**
//// * @param arg0
//// */
////public DesktopButton(String arg0) {
////	super(arg0);
////	// TODO Auto-generated constructor stub
////}
////
/////**
//// * @param arg0
//// */
////public DesktopButton(Action arg0) {
////	super(arg0);
////	// TODO Auto-generated constructor stub
////}