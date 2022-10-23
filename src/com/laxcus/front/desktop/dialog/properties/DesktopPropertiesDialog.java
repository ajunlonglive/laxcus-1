/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.properties;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.desktop.*;
import com.laxcus.front.desktop.dialog.*;
import com.laxcus.gui.component.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.display.*;

/**
 * 版本窗口
 * 
 * @author scott.liang
 * @version 1.0 5/22/2021
 * @since laxcus 1.0
 */
public class DesktopPropertiesDialog extends DesktopLightDialog implements ActionListener {

	private static final long serialVersionUID = -5666450111548528063L;
	
	/** 自定义句柄 **/
	static DesktopPropertiesDialog selfHandle;
	
	/**
	 * 返回句柄
	 * @return 句柄
	 */
	public static DesktopPropertiesDialog getInstance() {
		return DesktopPropertiesDialog.selfHandle;
	}

	/** 更新句柄 **/
	private DesktopUIUpdater updater;

	/** 选项卡 **/
	private JTabbedPane tabbedPane = new JTabbedPane();

	/** 按纽 **/
	private FlatButton cmdOK = new FlatButton();

	/** 桌面 **/
	private PlatformDesktop desktop;

	/**
	 * 构造版本窗口
	 */
	public DesktopPropertiesDialog(PlatformDesktop d, DesktopUIUpdater u) {
		super();
		desktop = d;
		updater = u;
	}
	
	/**
	 * 保存范围
	 */
	private void writeBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog/Bound", rect);
	}
	
	/**
	 * 读范围
	 * @return
	 */
	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"PropertiesDialog/Bound");
		if (bounds == null) {
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			int width = 448;
			int height = 600;
			int x = (size.width - width) / 2;
			int y = (size.height - height) / 2;
			y = (y > 20 ? 20 : (y < 0 ? 0 : y)); // 向上提高
			return new Rectangle(x, y, width, height);
		}
		return bounds;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/**
	 * 关闭窗口
	 */
	public void closeWindow() {
		super.closeWindow();
		DesktopPropertiesDialog.selfHandle = null;
	}

	private void click(ActionEvent event) {
		// 退出
		if (event.getSource() == cmdOK) {
			// 保存范围
			writeBounds();
			// 调用父类，关闭窗口
			closeWindow();
		} 
	}

	//	protected void chooseColor() {
	////		JColorChooser chooser = new JColorChooser();
	//		
	////		chooser.showDialog(arg0, arg1, arg2);
	//		
	//		Color c = JColorChooser.showDialog(this, "颜色选择器", Color.GRAY);
	//		if(c != null) {
	////			System.out.printf("color is %s\n", c.toString());
	//			cmdColor.setColor(c);
	//			cmdColor.repaint();
	//		}
	//	}
	//	
	//	private String imageFile;
	//	
	//	/** 选择的文件类型选项 **/
	//	private static String selectRead;

	//	/**
	//	 * 从中找到匹配的选项
	//	 * @param chooser 文件选择器
	//	 * @param selectDescription 选中的描述
	//	 */
	//	protected void chooseFileFilter(JFileChooser chooser, String selectDescription) {
	//		if (selectDescription == null) {
	//			return;
	//		}
	//		// 选择
	//		javax.swing.filechooser.FileFilter[] elements = chooser.getChoosableFileFilters();
	//		for (int i = 0; elements != null && i < elements.length; i++) {
	//			javax.swing.filechooser.FileFilter e = (javax.swing.filechooser.FileFilter) elements[i];
	//			if (e.getClass() != DiskFileFilter.class) {
	//				continue;
	//			}
	//			DiskFileFilter filter = (DiskFileFilter) e;
	//			String type = filter.getDescription();
	//			if (Laxkit.compareTo(selectDescription, type) == 0) {
	//				chooser.setFileFilter(filter);
	//				break;
	//			}
	//		}
	//	}

	//	class ReadThread extends SwingEvent {
	//		File file;
	//
	//		ReadThread(File e) {
	//			super();
	//			file = e;
	//		}
	//
	//		public void process() {
	//			readImage(file);
	//		}
	//	}

	//	/**
	//	 * 输入新的图像文件
	//	 * @param file
	//	 */
	//	private void readImage(File file) {
	//		byte[] b = new byte[(int)file.length()];
	//
	//		try {
	//			FileInputStream in = new FileInputStream(file);
	//			in.read(b);
	//			in.close();
	//		} catch (IOException e) {
	////			showError();
	//			return;
	//		} catch (Throwable e) {
	////			showError();
	//			return;
	//		}
	//
	////		// 保存文件名
	////		selectFile = file;
	//
	////		setReadFile(file);
	//		
	//		int width = -1, height = -1;
	//
	//		// 显示图像
	//		ImageIcon icon = new ImageIcon(b);
	//		boolean success = (icon != null);
	//		if (success) {
	//			width = icon.getIconWidth();
	//			height = icon.getIconHeight();
	//			success = (width > 0 && height > 0);
	//		}
	//		
	//		if (success) {
	//			image.setIcon(icon);
	//			desktop.setBackgroundImage(icon.getImage());
	//		}
	//		
	////		// 显示结果
	////		if (success) {
	////			sourceLabel.setIcon(icon);
	////			// 提示
	////			String tooltip = findCaption("Dialog/ImageTransform/label/source/title");
	////			tooltip = String.format("%s (%d:%d)", tooltip, width, height);
	////			setToolTipText(sourceLabel, tooltip);
	////		} else {
	////			sourceLabel.setIcon(null);
	//////			setToolTipText(sourceLabel, "");
	////			setToolTipText(sourceLabel, findCaption("Dialog/ImageTransform/label/source/title"));
	////			showError();
	////		}
	//	}

	//	/**
	//	 * 选择图像文件
	//	 */
	//	private void chooseFile() {
	//		String title = findCaption("Dialog/ImageTransform/open-chooser/title/title");
	//		String buttonText = findCaption("Dialog/ImageTransform/open-chooser/choose/title");
	//
	//		// JPEG文件
	//		String ds_jpeg = findCaption("Dialog/ImageTransform/open-chooser/jpeg/description/title");
	//		String jpeg = findCaption("Dialog/ImageTransform/open-chooser/jpeg/extension/title");
	//		// GIF文件
	//		String ds_gif = findCaption("Dialog/ImageTransform/open-chooser/gif/description/title");
	//		String gif = findCaption("Dialog/ImageTransform/open-chooser/gif/extension/title");
	//		// PNG文件
	//		String ds_png = findCaption("Dialog/ImageTransform/open-chooser/png/description/title");
	//		String png = findCaption("Dialog/ImageTransform/open-chooser/png/extension/title");
	//
	//		DiskFileFilter f1 = new DiskFileFilter(ds_jpeg, jpeg);
	//		DiskFileFilter f2 = new DiskFileFilter(ds_gif, gif);
	//		DiskFileFilter f3 = new DiskFileFilter(ds_png, png);
	//
	//		// 显示窗口
	//		JFileChooser chooser = new JFileChooser();
	//		chooser.setAcceptAllFileFilterUsed(false);
	//		chooser.addChoosableFileFilter(f3);
	//		chooser.addChoosableFileFilter(f2);
	//		chooser.addChoosableFileFilter(f1);
	//		// 找到选项
	//		chooseFileFilter(chooser, selectRead);
	//		
	//		chooser.setMultiSelectionEnabled(false);
	//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	//		chooser.setDialogTitle(title);
	//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	//		chooser.setApproveButtonText(buttonText);
	//		chooser.setApproveButtonToolTipText(buttonText);
	//		
	////		setReadDirectory(chooser);
	//
	//		int val = chooser.showOpenDialog(this);
	//		// 显示窗口
	//		if (val != JFileChooser.APPROVE_OPTION) {
	//			return;
	//		}
	//
	////		selectRead = saveFileFileter(chooser);
	//
	//		File file = chooser.getSelectedFile();
	//		boolean success = (file.exists() && file.isFile());
	//		if (success) {
	//			imageFile = file.getAbsolutePath();
	//			addThread(new ReadThread(file));
	//		}
	//	}


	private JPanel createBottomPane() {
		//		cmdOK.setText("确定");
		//		cmdOK.addActionListener(this);

		String buttonTitle = UIManager.getString("PropertiesDialog.closeButtonText"); // "关闭";// findCaption("Dialog/about/okay/title");
		FontKit.setButtonText(cmdOK, buttonTitle);
		cmdOK.addActionListener(this);
		cmdOK.setMnemonic('O');

		//		JPanel bottom = new JPanel();
		//		bottom.setLayout(new BorderLayout(0, 0));
		//		bottom.add(cmdOK, BorderLayout.EAST);
		//		
		//		// 做出一个分割线
		//		JPanel js = new JPanel();
		//		js.setLayout(new BorderLayout(0, 8));
		//		js.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		//		js.add(bottom, BorderLayout.CENTER);
		//		
		////		SoftBevelBorder outside = new SoftBevelBorder(SoftBevelBorder.RAISED); 
		//////		EtchedBorder outside = new EtchedBorder(EtchedBorder.RAISED);
		//////		BevelBorder outside = new BevelBorder(BevelBorder.RAISED);
		////		EmptyBorder inside = new EmptyBorder(10, 8, 10, 8);
		////		CompoundBorder combound = new CompoundBorder(outside, inside);
		//
		//		JPanel root = new JPanel();
		//		root.setBorder(new EmptyBorder(10, 8, 10, 8));
		////		root.setBorder(combound);
		//		root.setLayout(new BorderLayout(0, 0));
		//		root.add(center, BorderLayout.CENTER);
		//		root.add(js, BorderLayout.SOUTH);
		//		return root;

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 1, 8, 0));
		sub.add(cmdOK);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		panel.add(sub, BorderLayout.EAST);
		return panel;
	}

	//	private JCheckBox box = new JCheckBox();

	//	private JComboBox colorBox = new JComboBox();
	//	
	//	private JLabel image = new JLabel();
	//	
	//	private JList colorList = new JList();
	//	
	//	private JButton cmdImage = new JButton();
	//	
	////	private JLabel color = new JLabel();
	//	
	//	private ColorButton cmdColor = new ColorButton();
	//	
	//	private JButton cmdDO = new JButton();
	//	
	////	private ImageIcon 

	//	/**
	//	 * 生成暗的颜色
	//	 * @param c
	//	 * @param flag
	//	 * @return
	//	 */
	//	private Color toDrak(Color c, int flag) {
	//		RGB rgb = new RGB(c);
	//		ESL esl = rgb.toESL();
	//		esl.setL(esl.getL() - flag);
	//		return esl.toColor();
	//	}
	//
	//	/**
	//	 * 生成亮的颜色
	//	 * @param c
	//	 * @param flag
	//	 * @return
	//	 */
	//	private Color toLight(Color c, int flag) {
	//		RGB rgb = new RGB(c);
	//		ESL esl = rgb.toESL();
	//		esl.setL(esl.getL() + flag);
	//		return esl.toColor();
	//	}

	//	private Color[] createDarkLights() {
	//		
	//		//		background = new Color(c.getRGB());
	//
	//		// NIMBUS界面
	//		// 淡兰：RGB {190,211,230}, ESL{139,107,198}
	//		// 深兰：RGB {52, 96, 135}, ESL{139,107,88}
	//
	//		//		ESL dark = new ESL(139, 107, 128);
	//		//		ESL light = new ESL(139, 107, 168);
	//		//		return new Color[] { dark.toColor(), light.toColor() };
	//
	//		if (Skins.isNimbus()) {
	//			ESL dark = new ESL(139, 107, 128);
	//			ESL light = new ESL(139, 107, 188);
	//			return new Color[] { dark.toColor(), light.toColor() };
	//		} else {
	//			Color c = super.getBackground();
	//			if(c == null) {
	//				c = Color.LIGHT_GRAY;
	//			}
	//			Color dark = toDrak(c, 20);
	//			Color light = toLight(c, 80);
	//			return new Color[] { dark, light };
	//		}
	//	}

	//	private ImageIcon createDanceImage() {
	//		// 创建缓冲图片类变量		
	//		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	//		// 获得缓冲图片的2D绘图类变量
	//		Graphics2D g2d = image.createGraphics();
	//
	//		// 建立暗、亮两套颜色，绘制渐变背景
	//		Color[] colors = createDarkLights();
	//		GradientPaint paint = new GradientPaint(0, 0, colors[0], 100, 100, colors[1], true);
	//		g2d.setPaint(paint);
	//		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
	//		g2d.dispose();
	//		
	//		return new ImageIcon(image);
	//	}

	//	private Color getBorderColor() {
	//		if (Skins.isNimbus()) {
	////			ESL light = new ESL(139, 107, 188);
	//			ESL light = new ESL(150, 30, 135);
	//			return light.toColor();
	//		} else {
	//			Color c = super.getBackground();
	//			if(c == null) {
	//				c = Color.LIGHT_GRAY;
	//			}
	//			ESL e = new RGB(c).toESL();
	//			return e.toBrighter(50).toColor();
	//		}
	//	}

	//	private JPanel createBackgroundPane() {
	//		cmdImage.setText("选择图像");
	//		cmdColor.setText("选择颜色");
	//		cmdDO.setText("应用");
	//		cmdColor.setFocusPainted(false);
	//		
	//		// 事件
	//		cmdImage.addActionListener(this);
	//		cmdColor.addActionListener(this);
	//		cmdDO.addActionListener(this);
	//		
	//		image.setHorizontalAlignment(SwingConstants.CENTER);
	////		ImageIcon icon = findImage("conf/desktop/image/window/background/ef.png");
	////		image.setBorder(new MatteBorder(new Insets(4, 4, 4, 4), icon));
	//		
	//		JScrollPane scroll = new JScrollPane(image);
	////		scroll.setBorder(new EmptyBorder(2, 2, 2, 2));
	//		
	////		ImageIcon icon = createDanceImage();
	////		scroll.setBorder(new MatteBorder(new Insets(3, 3, 3, 3), icon));
	//		
	//		Color c = getBorderColor();
	//		scroll.setBorder(new MatteBorder(new Insets(2,2,2,2), c));
	//		
	//		// 可视化统计
	//		colorList.setVisibleRowCount(5);
	//		colorList.setFixedCellHeight(26);
	//		colorList.setFixedCellWidth(110);
	//		JScrollPane cb = new JScrollPane(colorList);
	//		
	//		JPanel s1 = new JPanel();
	//		s1.setLayout(new GridLayout(3, 1, 0, 4));
	//		s1.add(cmdImage);
	//		colorBox.setMaximumRowCount(6);
	//		s1.add(colorBox);
	//		s1.add(cmdColor);
	//		
	//		JPanel s2 = new JPanel();
	//		s2.setLayout(new BorderLayout());
	//		s2.add(s1, BorderLayout.NORTH);
	//		s2.add(cmdDO, BorderLayout.SOUTH);
	//		
	//		JPanel s3 = new JPanel();
	//		s3.setLayout(new BorderLayout());
	//		s3.add(new JPanel(), BorderLayout.CENTER);
	//		s3.add(s2, BorderLayout.WEST);
	//		
	//		JPanel m = new JPanel();
	//		m.setLayout(new BorderLayout(0, 4));
	//		m.add(cb, BorderLayout.NORTH);
	//		m.add(s3, BorderLayout.CENTER);
	//		
	//		JPanel panel = new JPanel();
	//		panel.setLayout(new BorderLayout(4, 0));
	//		panel.setBorder(new EmptyBorder(2, 2, 2, 2));
	//		panel.add(scroll, BorderLayout.CENTER);
	//		panel.add(m, BorderLayout.EAST);
	//		return panel;
	//	}

	//	private JPanel createBackgroundPane2() {
	//		
	//		cmdImage.setText("选择图像");
	//		box.setText("二选一");
	//		cmdColor.setText("选择颜色");
	//		image.setHorizontalAlignment(SwingConstants.CENTER);
	//		
	//		JScrollPane scroll = new JScrollPane(image);
	//		scroll.setBorder(new EmptyBorder(2, 2, 2, 2));
	//		// scroll.setBackground(left.getBackground());
	//		// scroll.getViewport().setBackground(left.getBackground());
	//
	//		JPanel center = new JPanel();
	//		center.setLayout(new BorderLayout(0, 8));
	//		center.add(scroll, BorderLayout.CENTER);
	//		center.add(color, BorderLayout.SOUTH);
	//		
	//		JPanel buttons = new JPanel();
	//		buttons.setLayout(new GridLayout(3, 1, 0, 8));
	//		buttons.add(box);
	//		buttons.add(cmdImage);
	//		buttons.add(cmdColor);
	//		
	//		JPanel right = new JPanel();
	//		right.setLayout(new BorderLayout());
	//		right.add(buttons, BorderLayout.SOUTH);
	//
	//		JPanel panel = new JPanel();
	//		panel.setLayout(new BorderLayout(5, 0));
	//		panel.setBorder(new EmptyBorder( 2, 2, 2, 2));
	//		panel.add(center, BorderLayout.CENTER);
	//		panel.add(right, BorderLayout.EAST);
	//		
	//		// 事件
	//		cmdImage.addActionListener(this);
	//		cmdColor.addActionListener(this);
	//		
	//		return panel;
	//	}

	//	/**
	//	 * 构造布局
	//	 * @return
	//	 */
	//	private JPanel createAboutPane() {
	//		JLabel image = new JLabel();
	//
	//		JLabel html = new JLabel();
	//		html.setHorizontalAlignment(SwingConstants.LEFT);
	//		html.setVerticalAlignment(SwingConstants.TOP);
	//
	//		LocalSelector selector = new LocalSelector("conf/desktop/about/config.xml");	
	//		String path = selector.findPath("resource");
	//
	//		// 设置图片
	//		ResourceLoader loader = new ResourceLoader();
	//		ImageIcon icon = loader.findImage("conf/desktop/image/about/logo.png");
	//		if (icon != null) {
	//			image.setIcon(icon);
	//		}
	//
	//		// 生成文本，显示它！
	//		try {
	//			byte[] b = loader.findAbsoluteStream(path);
	//			String content = new UTF8().decode(b);
	//			html.setText(content);
	//		} catch (Throwable e) {
	//			e.printStackTrace();
	//		}
	//
	//		Font font = DesktopProperties.readSystemFont();
	//		if (font != null) {
	//			html.setFont(font);
	//		}
	//
	//
	//		JPanel left = new JPanel();
	//		left.setLayout(new BorderLayout());
	//		left.add(image, BorderLayout.NORTH);
	//		left.add(new JPanel(), BorderLayout.CENTER);
	//
	//		// 同一个背景色
	//		html.setBackground(left.getBackground());
	//		JScrollPane scroll = new JScrollPane(html);
	//		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
	//		scroll.setBackground(left.getBackground());
	//		scroll.getViewport().setBackground(left.getBackground());
	//
	//		// 主面板
	//		JPanel center = new JPanel();
	//		center.setLayout(new BorderLayout(10, 0));
	//		center.add(left, BorderLayout.WEST);
	//		center.add(scroll, BorderLayout.CENTER);
	//
	//		JPanel bottom = new JPanel();
	//		bottom.setLayout(new BorderLayout(0, 0));
	//		bottom.add(cmdOK, BorderLayout.EAST);
	//		
	//		// 做出一个分割线
	//		JPanel js = new JPanel();
	//		js.setLayout(new BorderLayout(0, 8));
	//		js.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
	//		js.add(bottom, BorderLayout.CENTER);
	//		
	////		SoftBevelBorder outside = new SoftBevelBorder(SoftBevelBorder.RAISED); 
	//////		EtchedBorder outside = new EtchedBorder(EtchedBorder.RAISED);
	//////		BevelBorder outside = new BevelBorder(BevelBorder.RAISED);
	////		EmptyBorder inside = new EmptyBorder(10, 8, 10, 8);
	////		CompoundBorder combound = new CompoundBorder(outside, inside);
	//
	//		JPanel root = new JPanel();
	//		root.setBorder(new EmptyBorder(10, 8, 10, 8));
	////		root.setBorder(combound);
	//		root.setLayout(new BorderLayout(0, 0));
	//		root.add(center, BorderLayout.CENTER);
	//		root.add(js, BorderLayout.SOUTH);
	//		return root;
	//	}

	private JPanel createThemePane() {
		ThemePane panel = new ThemePane(updater);
		panel.init();
		return panel;
	}
	
	private JPanel createAboutPane() {
		AboutPane pane = new AboutPane();
		pane.init();
		return pane;
	}

//	/**
//	 * 构造布局
//	 * @return
//	 */
//	private JPanel createAboutPane() {
//		JLabel image = new JLabel();
//
//		JLabel html = new JLabel();
//		html.setHorizontalAlignment(SwingConstants.LEFT);
//		html.setVerticalAlignment(SwingConstants.TOP);
//
//		LocalSelector selector = new LocalSelector("conf/desktop/about/config.xml");	
//		String path = selector.findPath("resource");
//
//		// 设置图片
//		ResourceLoader loader = new ResourceLoader();
//		ImageIcon icon = loader.findImage("conf/desktop/image/about/logo.png");
//		if (icon != null) {
//			image.setIcon(icon);
//		}
//
//		// 生成文本，显示它！
//		try {
//			byte[] b = loader.findAbsoluteStream(path);
//			String content = new UTF8().decode(b);
//			html.setText(content);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		//		Font font = DesktopProperties.readSystemFont();
//		Font font = UIKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
//		if (font != null) {
//			html.setFont(font);
//		}
//
//		JPanel left = new JPanel();
//		left.setLayout(new BorderLayout());
//		left.add(image, BorderLayout.NORTH);
//		left.add(new JPanel(), BorderLayout.CENTER);
//
//		// 同一个背景色
//		html.setBackground(left.getBackground());
//		JScrollPane scroll = new JScrollPane(html);
//		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
//		scroll.setBackground(left.getBackground());
//		scroll.getViewport().setBackground(left.getBackground());
//
//		// 主面板
//		JPanel center = new JPanel();
//		center.setLayout(new BorderLayout(10, 0));
//		center.add(left, BorderLayout.WEST);
//		center.add(scroll, BorderLayout.CENTER);
//
//		return center;
//
//		//		JPanel bottom = new JPanel();
//		//		bottom.setLayout(new BorderLayout(0, 0));
//		//		bottom.add(cmdOK, BorderLayout.EAST);
//		//		
//		//		// 做出一个分割线
//		//		JPanel js = new JPanel();
//		//		js.setLayout(new BorderLayout(0, 8));
//		//		js.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
//		//		js.add(bottom, BorderLayout.CENTER);
//		//		
//		////		SoftBevelBorder outside = new SoftBevelBorder(SoftBevelBorder.RAISED); 
//		//////		EtchedBorder outside = new EtchedBorder(EtchedBorder.RAISED);
//		//////		BevelBorder outside = new BevelBorder(BevelBorder.RAISED);
//		////		EmptyBorder inside = new EmptyBorder(10, 8, 10, 8);
//		////		CompoundBorder combound = new CompoundBorder(outside, inside);
//		//
//		//		JPanel root = new JPanel();
//		//		root.setBorder(new EmptyBorder(10, 8, 10, 8));
//		////		root.setBorder(combound);
//		//		root.setLayout(new BorderLayout(0, 0));
//		//		root.add(center, BorderLayout.CENTER);
//		//		root.add(js, BorderLayout.SOUTH);
//		//		return root;
//	}	

	//	/**
	//	 * 返回运行时面板，基础参数
	//	 * @return JPanel
	//	 */
	//	private JPanel createRuntimePanel() {
	//		JComboBox box = new JComboBox();
	//		box.setLightWeightPopupEnabled(false);
	//		for(int i =0; i < 100; i++) {
	//		box.addItem(String.format("Fuck %d", i +1));
	//		}
	//		
	//		JPanel panel = new JPanel();
	//		panel.setLayout(new BorderLayout());
	//		panel.add(box, BorderLayout.NORTH);
	//		return panel;
	//	}

	//	/**
	//	 * 显示窗口
	//	 */
	//	public void showDialog() {
	//		JPanel pane = createAboutPane();
	//		setContentPane(pane);
	//
	//		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	//		int width = (int)(size.getWidth() * 0.36);
	//		int height = (int)(size.getHeight() * 0.37);
	//		
	//		width = 428; height = 355;
	//		int x = (size.width - width)/2;
	//		int y = (size.height - height)/2;
	//		
	//		setBounds(new Rectangle(x, y, width, height));
	//		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	//
	//		setMinimumSize(new Dimension(386, 252));
	////		setAlwaysOnTop(true);
	//		// 标题
	//		String title = getCaption("Dialog/about/title");
	//		setTitle(title);
	//		
	////		// 不显示边框
	////		try {
	////			super.setUndecorated(true);
	////		} catch (IllegalComponentStateException  e) {
	////			
	////		}
	//		
	//		setVisible(true);
	//	}

	//	/**
	//	 * 定义范围
	//	 * @return
	//	 */
	//	private Rectangle getBound() {
	//		// 系统中取出参数
	//		Object e = UITools.getProperity(BOUND);
	//		if (e != null && e.getClass() == Rectangle.class) {
	//			return (Rectangle) e;
	//		}
	//		
	//		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	//		// 四分之一
	//		int width = size.width / 2;
	//		int height = size.height / 2;
	//		
	//		int x = (size.width - width) / 2;
	//		int y = (size.height - height) / 2;
	//		return new Rectangle(x, y, width, height);
	//	}

	private JPanel createBackgroundPane() {
		BackgroundPane panel = new BackgroundPane();
		panel.init(desktop);
		return panel;
	}

	//	private void initDialog() {
	////		setTitle("系统属性");
	//////		setFrameIcon(findImage("conf/desktop/image/window/system/screen.png"));
	////		setFrameIcon(PlatfromUtilities.getPlatformIcon());
	//
	//		setTitle(UIManager.getString("PropertiesDialog.Title"));
	//		setFrameIcon(UIManager.getIcon("PropertiesDialog.TitleIcon"));
	//
	//		// 位置
	//		setMinimumSize(new Dimension(300, 150));
	//		setBounds(readBounds());
	//		
	//		tabbedPane.addTab("主题", createThemePane());
	//		tabbedPane.addTab("桌面", createBackgroundPane());
	//		tabbedPane.addTab("系统信息", createAboutPane());
	//		
	////		tabbedPane.addTab("系统", createRuntimePanel());
	////		tabbedPane.addTab("集群", createRuntimePanel());
	////		tabbedPane.addTab("高级", createRuntimePanel());
	////		tabbedPane.addTab("设置", createRuntimePanel());
	//		
	////		tabbedPane.addTab(graph, graphPanel);
	////		tabbedPane.addTab(log, logPanel);
	////		tabbed.setBorder(new EmptyBorder(1, 1, 1, 1));
	//		
	//		tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	//		
	//		JPanel panel = new JPanel();
	//		panel.setLayout(new BorderLayout(0, 8));
	////		panel.setBorder(new EmptyBorder(6, 8, 6, 8));
	////		panel.add(tabbedPane, BorderLayout.CENTER);
	//		panel.add(createBottomPane(), BorderLayout.SOUTH);
	//		
	//		panel.setBorder(new EmptyBorder(4,4,4,4));
	//
	////		Container canvas = getContentPane();
	////		canvas.setLayout(new BorderLayout(0, 0));
	////		canvas.add(panel, BorderLayout.CENTER);
	//		
	////		setLayeredPane(arg0)
	//		
	////		JLayeredPane p = getLayeredPane();
	////		System.out.printf("JLayeredPane is %s\n", (p != null ? p.getClass().getName() : "Null"));
	//		
	////		JLayeredPane jp = new JLayeredPane();
	////		jp.setBorder(new EmptyBorder(8,8,8,8));
	////		setLayeredPane(jp);
	//		
	//		setContentPane(panel);
	//	}

	private void initDialog() {
		setTitle(UIManager.getString("PropertiesDialog.Title"));
		setFrameIcon(UIManager.getIcon("PropertiesDialog.TitleIcon"));

		// 位置
		setMinimumSize(new Dimension(300, 150));

//		PropertiesDialog.tabbedThemeText  主题
//		PropertiesDialog.tabbedDesktopText 桌面
//		PropertiesDialog.tabbedSystemInfo 系统信息
		
		tabbedPane.addTab(UIManager.getString("PropertiesDialog.tabbedThemeText"), createThemePane());
		tabbedPane.addTab(UIManager.getString("PropertiesDialog.tabbedDesktopText"), createBackgroundPane());
		tabbedPane.addTab(UIManager.getString("PropertiesDialog.tabbedSystemInfo"), createAboutPane());

		//		tabbedPane.addTab("系统", createRuntimePanel());
		//		tabbedPane.addTab("集群", createRuntimePanel());
		//		tabbedPane.addTab("高级", createRuntimePanel());
		//		tabbedPane.addTab("设置", createRuntimePanel());

		//		tabbedPane.addTab(graph, graphPanel);
		//		tabbedPane.addTab(log, logPanel);
		//		tabbed.setBorder(new EmptyBorder(1, 1, 1, 1));

		tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 8));
		panel.add(tabbedPane, BorderLayout.CENTER);
		panel.add(createBottomPane(), BorderLayout.SOUTH);

		panel.setBorder(new EmptyBorder(4, 4, 4, 4));

		//		Container canvas = getContentPane();
		//		canvas.setLayout(new BorderLayout(0, 0));
		//		canvas.add(panel, BorderLayout.CENTER);

		//		setLayeredPane(arg0)

		//		JLayeredPane p = getLayeredPane();
		//		System.out.printf("JLayeredPane is %s\n", (p != null ? p.getClass().getName() : "Null"));

		//		JLayeredPane jp = new JLayeredPane();
		//		jp.setBorder(new EmptyBorder(8,8,8,8));
		//		setLayeredPane(jp);

		setContentPane(panel);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.desktop.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component bind, boolean modal) {
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		//		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		//		// 去掉边框
		//		if (Skins.isMetal()) {
		//			setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		//		}

		//		// 清除标题栏
		//		javax.swing.plaf.InternalFrameUI ui = getUI();
		//		if (ui.getClass() == javax.swing.plaf.basic.BasicInternalFrameUI.class) {
		//			((javax.swing.plaf.basic.BasicInternalFrameUI) ui).setNorthPane(null);
		//		}

		//		// 清除标题栏
		//		javax.swing.plaf.basic.BasicInternalFrameUI ui = (javax.swing.plaf.basic.BasicInternalFrameUI)getUI();
		//		ui.setNorthPane(null);

		//		Border b = getBorder();
		//		System.out.printf("Properties Border is %s\n", (b == null ? "Null" : b.getClass().getName()));
		//		if(b != null && b.getClass() == javax.swing.border.EmptyBorder.class) {
		//			System.out.printf("%s", b.toString());
		//			javax.swing.border.EmptyBorder c = (javax.swing.border.EmptyBorder)b;
		//			Insets in = c.getBorderInsets();
		//			System.out.printf("top:%d, left:%d, bottom:%d, right:%d\n", in.top, in.left, in.bottom, in.right);
		//		}

//		// 边框说明：Nimbus/Metal界面，系统默认的边框是EmptyBorder，Nimbus的Border是EmptyBorder(2,4,4,4), Metal的Border是EmptyBorder(0,0,0,0).
//		// 这里，Nimbus界面边框不改变，只修改Metal界面，在底部设置4个像素。
//		if (isMetalUI()) {
//			setBorder(new EmptyBorder(0, 0, 6, 0));
//		}

		setBounds(readBounds());
		
		// 赋值句柄
		DesktopPropertiesDialog.selfHandle = this;

		// 显示窗口
		if (modal) {
			return showModalDialog(bind);
		} else {
			return showNormalDialog(bind);
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		
		// 更新字体，注意！不要更新UI，否则在updateUI方法里会形成死循环
		FontKit.updateDefaultFonts(this, false);
	}

}