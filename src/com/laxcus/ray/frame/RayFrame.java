/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.frame;

import com.laxcus.gui.frame.*;

/**
 * 桌面子窗口
 * 
 * 用于LAXCUS桌面环境上，非模态！
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public abstract class RayFrame extends LightFrame {

	private static final long serialVersionUID = 1275382373650454504L;

	/**
	 * 构造默认的轻量级桌面子窗口
	 */
	public RayFrame() {
		super();
	}

	/**
	 * 构造轻量级桌面子窗口，指定标题
	 * @param title 标题
	 */
	public RayFrame(String title) {
		super(title);
	}

	/**
	 * 构造轻量级桌面子窗口，指定参数
	 * @param title
	 * @param resizable
	 */
	public RayFrame(String title, boolean resizable) {
		super(title, resizable);
	}

	/**
	 * 构造轻量级桌面子窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 */
	public RayFrame(String title, boolean resizable, boolean closable) {
		super(title, resizable, closable);
	}

	/**
	 * 构造轻量级桌面子窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 */
	public RayFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
		super(title, resizable, closable, maximizable);
	}

	/**
	 * 构造轻量级桌面子窗口
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public RayFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}


//	/**
//	 * 根据XML路径，查找匹配的标题
//	 * @param xmlPath XML路径
//	 * @return 返回字符串
//	 */
//	protected String findCaption(String xmlPath) {
//		return DesktopLauncher.getInstance().findCaption(xmlPath); 
//	}

//	/**
//	 * 关闭窗口
//	 */
//	public abstract void closeWindow();

}


//	/**
//	 * 建立屏幕
//	 */
//	private void createScreen() {
//		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, true);
//		getInputMap(JComponent.WHEN_FOCUSED).put(ks, "CTRL C");
//		getActionMap().put("CTRL C", new DesktopFrameAction());
//	}

//	/**
//	 * 线程延时等待。单位：毫秒。
//	 * @param timeout 超时时间
//	 */
//	public synchronized void delay(long timeout) {
//		try {
//			if (timeout > 0L) {
//				wait(timeout);
//			}
//		} catch (InterruptedException e) {
//			com.laxcus.log.client.Logger.fatal(e);
//		}
//	}

//	/**
//	 * 线程加入分派器
//	 * @param thread
//	 */
//	protected void addThread(SwingEvent thread) {
//		SwingDispatcher.invokeLater(thread);
//	}


//	/**
//	 * 查找图像
//	 * @param xmlPath
//	 * @param width
//	 * @param height
//	 * @return
//	 */
//	protected ImageIcon findImage(String xmlPath, int width, int height) {
//		ResourceLoader loader = new ResourceLoader();
//		return loader.findImage(xmlPath, width, height);
//	}
//
//	/**
//	 * 返回标题图标
//	 * @param xmlPath
//	 * @return
//	 */
//	protected ImageIcon getTitleIcon(String xmlPath) {
//		return findImage(xmlPath, 16, 16);
//	}


//	/**
//	 * 返回默认的标题图标
//	 * @return
//	 */
//	public abstract ImageIcon getTitleIcon();


//	class CopyWindowThread extends SwingEvent {
//
//		CopyWindowThread() {
//			super();
//		}
//
//		public void process() {
//			copyScreen();
//		}
//	}
//
//	
//	class DesktopFrameAction extends AbstractAction {
//
//		private static final long serialVersionUID = -6491118840636710568L;
//
//		public void actionPerformed(ActionEvent e) {
//			addThread(new CopyWindowThread());
//		}
//	}
//	
//	public void copyScreen() {
//		int width = getWidth();
//		int height = getHeight();
//		
//		System.out.printf("窗口屏幕尺寸：%d %d\n", width, height);
//		
//		// 生成图像
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g2d = img.createGraphics();
//		printAll(g2d); // 关键，复制窗口到缓冲里
//		g2d.dispose();
//		
//		// 复制到磁盘
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		try {
//			ImageIO.write(img, "PNG", bs);
//			bs.flush();
//			// 生成图像
//			byte[] b = bs.toByteArray();
//			FileOutputStream out = new FileOutputStream("d:/abc.png");
//			out.write(b);
//			out.close();
//		} catch (IOException e) {
//			Logger.fatal(e);
//			e.printStackTrace();
//		}
//	}
