/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 图像文件转换工具。<br>
 * 
 * 支持“JPEG、GIF、PNG”格式的相互转换
 * 
 * @author scott.liang
 * @version 1.0 7/20/2020
 * @since laxcus 1.0
 */
public class TerminalTransformImageDialog extends TerminalCommonFontDialog implements ActionListener , ChangeListener{

	private static final long serialVersionUID = -5425097446525141862L;

	/** 属性，保存到UIManager的KEY值 **/
	private final static String READ_FILE = TerminalTransformImageDialog.class.getSimpleName() + "_OPEN";

	private final static String WRITE_FILE = TerminalTransformImageDialog.class.getSimpleName() + "_SAVE";

	private final static String BOUND = TerminalTransformImageDialog.class.getSimpleName() + "_BOUND";
	
	private JLabel sourceLabel = new JLabel(); 

	private JLabel targetLabel = new JLabel(); 

	// 高/宽
	private SpinnerNumberModel spnHeightModel = new SpinnerNumberModel(32, 2, 10240, 1);
	private SpinnerNumberModel spnWidthModel = new SpinnerNumberModel(32, 2, 10240, 1);
	private JSpinner spnHeight = new JSpinner();
	private JSpinner spnWidth = new JSpinner();
	
//	// 亮度
//	private SpinnerNumberModel spnLightModel = new SpinnerNumberModel(0, -240, 240, 1);
//	private JSpinner spnLight = new JSpinner();
	
	/** 比例 **/
	private JCheckBox boxRate = new JCheckBox();
	// SpinnerNumberModel(int value, int minimum, int maximum, int stepSize) 
	private SpinnerNumberModel spnRateModel = new SpinnerNumberModel(100, 5, 500, 1);
	private JSpinner spnRate = new JSpinner();

	private JButton cmdOpen = new JButton();
	private JButton cmdTransform = new JButton();
	private JCheckBox cmdTranslucent = new JCheckBox(); // 背景透明
	private JButton cmdSave = new JButton();
	private JButton cmdExit = new JButton();

	/** 选中的磁盘文件 **/
	private File selectFile;

	/** 转换的文件 **/
	private File transformFile;

	/** 保存的文件 **/
	private File saveFile;

	/**
	 * @param frame 窗口
	 * @param modal 模态
	 */
	public TerminalTransformImageDialog(Frame frame, boolean modal) {
		super(frame, modal);
	}


	class ClickThread extends SwingEvent {
		ActionEvent event;

		ClickThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}

	class ReadThread extends SwingEvent {
		File file;

		ReadThread(File e) {
			super();
			file = e;
		}

		public void process() {
			readImage(file);
		}
	}

	class WriteThread extends SwingEvent {
		File file;
		boolean translucent;

		WriteThread(File e, boolean b) {
			super();
			file = e;
			translucent = b;
		}

		public void process() {
			writeImage(file, translucent);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ClickThread(e));
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {

	}

	/**
	 * 触发操作
	 * @param event 事件
	 */
	private void click(ActionEvent event) {
		// 退出
		if (event.getSource() == cmdExit) {
			boolean success = exit();
			if (success) {
				saveBound();
				dispose();
			}
		} else if (event.getSource() == cmdOpen) {
			chooseFile();
		} else if (event.getSource() == cmdSave) {
			saveFile();
		} else if (event.getSource() == cmdTransform) {
			transform();
		} else if(event.getSource() == boxRate) {
			doRate();
		}
	}

	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/ImageTransform/exit/message/title");

		ResourceLoader loader = new ResourceLoader();
		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/question.png", 32, 32);

		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, icon, content,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle e = super.getBounds();
		if (e != null) {
			UITools.putProperity(BOUND, e);
		}
	}

	/** 选择的文件类型选项 **/
	private static String selectRead;

	/**
	 * 选择图像文件
	 */
	private void chooseFile() {
		String title = findCaption("Dialog/ImageTransform/open-chooser/title/title");
		String buttonText = findCaption("Dialog/ImageTransform/open-chooser/choose/title");

		// JPEG文件
		String ds_jpeg = findCaption("Dialog/ImageTransform/open-chooser/jpeg/description/title");
		String jpeg = findCaption("Dialog/ImageTransform/open-chooser/jpeg/extension/title");
		// GIF文件
		String ds_gif = findCaption("Dialog/ImageTransform/open-chooser/gif/description/title");
		String gif = findCaption("Dialog/ImageTransform/open-chooser/gif/extension/title");
		// PNG文件
		String ds_png = findCaption("Dialog/ImageTransform/open-chooser/png/description/title");
		String png = findCaption("Dialog/ImageTransform/open-chooser/png/extension/title");

		DiskFileFilter f1 = new DiskFileFilter(ds_jpeg, jpeg);
		DiskFileFilter f2 = new DiskFileFilter(ds_gif, gif);
		DiskFileFilter f3 = new DiskFileFilter(ds_png, png);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f3);
		chooser.addChoosableFileFilter(f2);
		chooser.addChoosableFileFilter(f1);
		// 找到选项
		chooseFileFilter(chooser, selectRead);
		
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);
		
		setReadDirectory(chooser);

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		selectRead = saveFileFileter(chooser);

		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isFile());
		if (success) {
			addThread(new ReadThread(file));
		}
	}
	
	/**
	 * 保存读的脚本文件
	 * @param file
	 */
	private void setReadFile(File file) {
		String filename = Laxkit.canonical(file);
		UITools.putProperity(READ_FILE, filename);
	}
	
	/**
	 * 设置开放目录
	 * @param chooser
	 */
	protected void setReadDirectory(JFileChooser chooser) {
		Object memory = UITools.getProperity(READ_FILE);
		if (memory != null && memory.getClass() == String.class) {
			File file = new File((String) memory);
			if (file.exists() && file.isFile()) {
				chooser.setCurrentDirectory(file.getParentFile());
			}
		}
	}

	/**
	 * 输入新的图像文件
	 * @param file
	 */
	private void readImage(File file) {
		byte[] b = new byte[(int)file.length()];

		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
		} catch (IOException e) {
			showError();
			return;
		} catch (Throwable e) {
			showError();
			return;
		}

		// 保存文件名
		selectFile = file;

		setReadFile(file);
		
		int width = -1, height = -1;

		// 显示图像
		ImageIcon icon = new ImageIcon(b);
		boolean success = (icon != null);
		if (success) {
			width = icon.getIconWidth();
			height = icon.getIconHeight();
			success = (width > 0 && height > 0);
		}
		
		// 显示结果
		if (success) {
			sourceLabel.setIcon(icon);
			// 提示
			String tooltip = findCaption("Dialog/ImageTransform/label/source/title");
			tooltip = String.format("%s (%d:%d)", tooltip, width, height);
			setToolTipText(sourceLabel, tooltip);
		} else {
			sourceLabel.setIcon(null);
//			setToolTipText(sourceLabel, "");
			setToolTipText(sourceLabel, findCaption("Dialog/ImageTransform/label/source/title"));
			showError();
		}
	}

	/** 选择的文件类型选项 **/
	private static String selectWrite;

	/**
	 * 保存图像文件
	 */
	private void saveFile() {
		String title = findCaption("Dialog/ImageTransform/save-chooser/title/title");
		String buttonText = findCaption("Dialog/ImageTransform/save-chooser/save/title");

		// JPEG文件
		String ds_jpeg = findCaption("Dialog/ImageTransform/save-chooser/jpeg/description/title");
		String jpeg = findCaption("Dialog/ImageTransform/save-chooser/jpeg/extension/title");
		// GIF文件
		String ds_gif = findCaption("Dialog/ImageTransform/save-chooser/gif/description/title");
		String gif = findCaption("Dialog/ImageTransform/save-chooser/gif/extension/title");
		// PNG文件
		String ds_png = findCaption("Dialog/ImageTransform/save-chooser/png/description/title");
		String png = findCaption("Dialog/ImageTransform/save-chooser/png/extension/title");

		DiskFileFilter f1 = new DiskFileFilter(ds_jpeg, jpeg);
		DiskFileFilter f2 = new DiskFileFilter(ds_gif, gif);
		DiskFileFilter f3 = new DiskFileFilter(ds_png, png);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f1);
		chooser.addChoosableFileFilter(f2);
		chooser.addChoosableFileFilter(f3);
		// 找到选项
		chooseFileFilter(chooser, selectWrite);
		
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);
		
		setExportDirectory(chooser);

		int val = chooser.showSaveDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		selectWrite = saveFileFileter(chooser);
		
		DiskFileFilter filter = (DiskFileFilter) chooser.getFileFilter();
		File file = chooser.getSelectedFile();
		// 判断符合名称要求
		if (!filter.accept(file)) {
			String filename = Laxkit.canonical(file);
			String[] exts = filter.getExtensions();
			filename = String.format("%s.%s", filename, exts[0]);
			file = new File(filename);
		}
		
		// 背景透明
		boolean translucent = cmdTranslucent.isSelected();
		
//		int lightValue = spnLightModel.getNumber().intValue();
		
		// 写入磁盘
		addThread(new WriteThread(file, translucent));
	}

	/**
	 * 保存写的脚本文件
	 * @param file
	 */
	private void setExportFile(File file) {
		String filename = Laxkit.canonical(file);
		UITools.putProperity(WRITE_FILE, filename);
	}
	
	/**
	 * 设置开放目录
	 * @param chooser
	 * @return
	 */
	protected void setExportDirectory(JFileChooser chooser) {
		Object memory = UITools.getProperity(WRITE_FILE);
		if (memory != null && memory.getClass() == String.class) {
			File file = new File((String) memory);
			if (file.exists() && file.isFile()) {
				chooser.setCurrentDirectory(file.getParentFile());
			}
		}
	}

	/**
	 * 图像写入磁盘文件
	 * @param file
	 * @param translucent 透明
	 */
	private void writeImage(File file, boolean translucent) {
		if (transformFile == null) {
			return;
		}

		setExportFile(file);
		
		saveFile = file;
		String name = saveFile.getName();
		int index = name.lastIndexOf('.');
		String type = name.substring(index + 1).toUpperCase();

		// 转换图像
		ImageIcon icon = (ImageIcon) targetLabel.getIcon();
		int h = icon.getIconHeight();
		int w = icon.getIconWidth();
		boolean success = false;
		try {
			success = write(transformFile, saveFile, w, h, translucent, type);
		} catch (IOException e) {
			Logger.error(e);
		}

		// 保存
		if (success) {
			String text = findCaption("Dialog/ImageTransform/write/success/title");
			text = String.format(text, Laxkit.canonical(saveFile));
			showMessage(text, getTitle());
		} else {
			String text = findCaption("Dialog/ImageTransform/write/failed/title");
			showWarming(text, getTitle());
		}
	}

	/**
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showWarming(String content, String title) {
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 显示正确消息
	 * @param content
	 * @param title
	 */
	private void showMessage(String content, String title) {
		MessageDialog.showMessageBox(this, title, JOptionPane.INFORMATION_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 显示图像转换失败
	 */
	private void showError() {
		// xxx 是必选项，请输入！
		String title = findCaption("Dialog/ImageTransform/failed/title");
		String content = findContent("Dialog/ImageTransform/failed"); 
		content = String.format("<html><body>%s</body></html>", content);

		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}
	

	/**
	 * 转换图像
	 */
	private void transform() {
		int height = 32;
		int width = 32;
		
		// 高度值
//		int lightValue = spnLightModel.getNumber().intValue();
//		System.out.printf("light value %d\n", lightValue);
		
		// 按照比例显示
		boolean select = boxRate.isSelected();
		if (select) {
			double rate = (spnRateModel.getNumber().doubleValue() / 100.0f);
			
			Icon icon = sourceLabel.getIcon();
			height = (int) (icon.getIconHeight() * rate);
			width = (int) (icon.getIconWidth() * rate);
		} else {
			height = spnHeightModel.getNumber().intValue();
			width = spnWidthModel.getNumber().intValue();
		}

		String name = selectFile.getName();
		int index = name.lastIndexOf('.');
		String type = name.substring(index + 1);
		
		// 转换图像
		boolean success = false;
		try {
			ImageIcon icon = convert(selectFile, width, height, type);
			success = (icon != null);
			// 成功
			if (success) {
				height = icon.getIconHeight();
				width = icon.getIconWidth();
				success = (width > 0 && height > 0);
			}
			
			if (success) {
				targetLabel.setIcon(icon);
				// 提示
				String title = findCaption("Dialog/ImageTransform/label/target/title");
				String tooltip = String.format("%s (%d:%d)", title, height, width);
				setToolTipText(targetLabel, tooltip);
				transformFile = selectFile;
			} else {
				targetLabel.setIcon(null);
				setToolTipText(targetLabel, findCaption("Dialog/ImageTransform/label/target/title"));
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e ) {
			Logger.fatal(e);
		}
		
		// 提示出错
		if (!success) {
			showError();
		}
	}
	
	/**
	 * 相反选择
	 */
	private void doRate() {
		boolean select = boxRate.isSelected();
		
		// 有效
		spnRate.setEnabled(select);
		// 反向：无效!
		spnHeight.setEnabled(!select);
		spnWidth.setEnabled(!select);
	}
	
//	private void doDarkerOrBrighter(BufferedImage buff, int lightValue) {
//		// 如果是0，忽略它
//		if (lightValue == 0) {
//			return;
//		}
//		
//		int width = buff.getWidth();
//		int height = buff.getHeight();
//		
//		// 大于0是亮，否则是暗
//		boolean brighter = (lightValue > 0);
//		if (!brighter) {
//			lightValue = -lightValue;
//		}
//
//		for (int x = 0; x < width; x++) {
//			for (int y = 0; y < height; y++) {
//				int value = buff.getRGB(x, y);
//				if (value == 0) {
//					// 透明
//					buff.setRGB(x, y, 0);
//				} else {
//					RGB rgb = new RGB(value);
//					ESL esl = rgb.toESL();
//					// 加亮或者暗
//					if (brighter) {
//						esl.brighter(lightValue);
//					} else {
//						esl.darker(lightValue);
//					}
//					// 加亮后的返回值
//					value = esl.toRGB().getRGB();
//					buff.setRGB(x, y, value);
//				}
//			}
//		}
//	}
	
//	private ImageIcon doIcon(File source, int compressWidth, int compressHeight, String type) throws IOException {
//		
////		// 生成一个新图像
////		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
////		Graphics2D gra = dest.createGraphics();
////		dest = gra.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
////		dest.getGraphics().drawImage(image, 0, 0, null);
//
//		FileInputStream in = new FileInputStream(source);
//		BufferedImage img = ImageIO.read(in);
//		Image compressImage = img.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
//		
//		// 生成一个新图像
//		BufferedImage dest = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
//		Graphics2D gra = dest.createGraphics();
//		dest = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
//		dest.getGraphics().drawImage(compressImage, 0, 0, null);
//
//		// 输出
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		ImageIO.write(dest, type, out);
//		out.flush();
//
//		// 读出字节
//		byte[] b = out.toByteArray();
//
//		// 关闭！
//		out.close();
//		in.close();
//
//		return new ImageIcon(b);
//	}


	/**
	 * 转换图像
	 * @param source
	 * @param compressWidth
	 * @param compressHeight
	 * @param type
	 * @return
	 * @throws IOException
	 */
	private ImageIcon convert(File source, int compressWidth, int compressHeight, String type)  throws IOException {
		FileInputStream in = new FileInputStream(source);
		BufferedImage img = ImageIO.read(in);

		// 平滑缩小图象
		Image compressImage = img.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
		// 生成一个新图像
		BufferedImage buff = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = buff.createGraphics();
		buff = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
		buff.getGraphics().drawImage(compressImage, 0, 0, null);
		
//		// 调整亮度
//			doDarkerOrBrighter(buff, lightValue);
		

		// 输出
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(buff, type, out);
		out.flush();

		// 读出字节
		byte[] b = out.toByteArray();

		// 关闭！
		out.close();
		in.close();

		return new ImageIcon(b);
	}

	/**
	 * 写入
	 * @param source
	 * @param target
	 * @param compressWidth
	 * @param compressHeight
	 * @param type
	 * @throws IOException
	 */
	private boolean write(File source, File target, int compressWidth,
			int compressHeight, boolean translucent, final String type) throws IOException {

		// 判断文件存在
		boolean success = (source.exists() && source.isFile());
		if (!success) {
			return false;
		}

		// 读磁盘
		FileInputStream in = new FileInputStream(source);
		BufferedImage img = ImageIO.read(in);

		// 平滑缩小图象
		Image compressImage = img.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
		// 生成一个新图像
		BufferedImage buff = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
		if (translucent) {
			Graphics2D gra = buff.createGraphics();
			buff = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
		}
		buff.getGraphics().drawImage(compressImage, 0, 0, null);
		
//		// 调整暗或者亮
//		this.doDarkerOrBrighter(buff, lightValue);

		// 写入磁盘
		FileOutputStream out = new FileOutputStream(target);
		ImageIO.write(buff, type, out);
		out.flush();
		out.close();

		in.close();

		return true;
	}

	/**
	 * 确定范围
	 * @return
	 */
	private Rectangle getBound() {
		// 系统中取出参数
		Object e = UITools.getProperity(BOUND);
		if (e != null && e.getClass() == Rectangle.class) {
			return (Rectangle) e;
		}
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 560;
		int height = 386;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 初始化组件
	 */
	private void initControls() {
		spnHeight.setModel(spnHeightModel);
		spnWidth.setModel(spnWidthModel);
		spnRate.setModel(spnRateModel);
		
		spnHeight.addChangeListener(this);
		spnWidth.addChangeListener(this);
		spnRate.addChangeListener(this);
		
//		spnLight.setModel(spnLightModel);
//		spnLight.addChangeListener(this);

		sourceLabel.setHorizontalAlignment(SwingConstants.CENTER);
		targetLabel.setHorizontalAlignment(SwingConstants.CENTER);

		setToolTipText(sourceLabel, findCaption("Dialog/ImageTransform/label/source/title"));
		setToolTipText(targetLabel, findCaption("Dialog/ImageTransform/label/target/title"));
		
		// 按照比例调整
		setButtonText(boxRate, findCaption("Dialog/ImageTransform/buttons/rate/title"));
		boxRate.setMnemonic('R');
		boxRate.addActionListener(this);
		boxRate.setHorizontalTextPosition(SwingConstants.RIGHT);

		setButtonText(cmdOpen, findCaption("Dialog/ImageTransform/buttons/open/title"));
		cmdOpen.setMnemonic('O');
		cmdOpen.addActionListener(this);

		// 背景透明
		setButtonText(cmdTranslucent, findCaption("Dialog/ImageTransform/buttons/translucent/title"));
		cmdTranslucent.setMnemonic('T');
		// 保存图像
		setButtonText(cmdSave, findCaption("Dialog/ImageTransform/buttons/save/title"));
		cmdSave.setMnemonic('S');
		cmdSave.addActionListener(this);

		// 转换尺寸
		setButtonText(cmdTransform,  findCaption("Dialog/ImageTransform/buttons/transform/title"));
		cmdTransform.setMnemonic('C');
		cmdTransform.addActionListener(this);

		setButtonText(cmdExit, findCaption("Dialog/ImageTransform/buttons/exit/title"));
		cmdExit.setMnemonic('X');
		cmdExit.addActionListener(this);
	}

	/**
	 * 窗口
	 * @return
	 */
	private JPanel createLeftScrollPane() {
		JScrollPane scroll = new JScrollPane(sourceLabel);
		scroll.setBorder(UITools.createTitledBorder(null, 0));

		JPanel south = new JPanel();
		south.setLayout(new BorderLayout());
		south.add(cmdOpen, BorderLayout.WEST);
		south.add(new JPanel(), BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(south, BorderLayout.SOUTH);
		return panel;
	}
	
	private JPanel createSpinner(String text, char word, JSpinner spinner) {
		JLabel label = new JLabel(text);
		label.setToolTipText(text);
		spinner.setToolTipText(text);
		label.setDisplayedMnemonic(word);
		label.setLabelFor(spinner);
		
//		label.setPreferredSize(new Dimension(60, 26));
		spinner.setPreferredSize(new Dimension(60, 26));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(label, BorderLayout.CENTER);
		panel.add(spinner, BorderLayout.EAST);
		return panel;
	}
	
//	/**
//	 * 
//	 * @return
//	 */
//	private JPanel createLeftSpinnerPanel() {
//		String heightText = findCaption("Dialog/ImageTransform/spinner/height/title");
//		String widthText = findCaption("Dialog/ImageTransform/spinner/width/title");
//		String rateText = findCaption("Dialog/ImageTransform/spinner/rate/title");
//		String lightText = findCaption("Dialog/ImageTransform/spinner/light/title");
//		
//		JPanel e1 = createSpinner(widthText, 'W', spnWidth);
//		JPanel e2 = createSpinner(heightText, 'H', spnHeight);
//		JPanel e3 = createSpinner(rateText, 'R', spnRate);
//		JPanel e4 = createSpinner(lightText, 'L', spnLight);
//
//		JPanel left = new JPanel();
//		left.setLayout(new GridLayout(2,1, 0, 5));
//		left.setBorder(new EmptyBorder(0, 0, 0, 5));
//		left.add(e1);
//		left.add(e2);
//
//		JPanel right = new JPanel();
//		right.setLayout(new GridLayout(2, 1, 0, 5));
//		// right.add( boxRate );
//		right.add(e4);
//		right.add(e3);
//		
//		JPanel n = new JPanel();
//		n.setLayout(new GridLayout(2, 1, 0, 5));
//		n.add(boxRate);
//		n.add(cmdTransform);
//
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//		panel.add(left);
//		panel.add(right);
//		panel.add(n);
//		return panel;
//	}
	
	/**
	 * 
	 * @return
	 */
	private JPanel createLeftSpinnerPanel() {
		String heightText = findCaption("Dialog/ImageTransform/spinner/height/title");
		String widthText = findCaption("Dialog/ImageTransform/spinner/width/title");
		String rateText = findCaption("Dialog/ImageTransform/spinner/rate/title");
//		String lightText = findCaption("Dialog/ImageTransform/spinner/light/title");

		JPanel width = createSpinner(widthText, 'W', spnWidth);
		JPanel height = createSpinner(heightText, 'H', spnHeight);
		JPanel rate = createSpinner(rateText, 'R', spnRate);

		//		JPanel light = createSpinner(lightText, 'L', spnLight);

		//		JPanel panel = new JPanel();
		//		panel.setLayout(new GridLayout(2, 3, 6, 8));
		//		panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		//		panel.add(width); 
		//		panel.add(rate);
		////		panel.add(boxRate);
		//		panel.add(new JPanel());
		//		panel.add(height); // 第二行，高度
		//		panel.add(boxRate);
		//		panel.add(cmdTransform);
		//
		//		return panel;

		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2,1, 0, 5));
		left.setBorder(new EmptyBorder(0, 0, 0, 5));
		left.add(width);
		left.add(height);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1, 0, 5));
		right.add(boxRate);
		right.add(rate);
		//				right.add(e3);

		//				JPanel side = new JPanel();
		//				side.setLayout(new FlowLayout(FlowLayout.LEFT));
		//				side.add(cmdTransform);

		JPanel n = new JPanel();
		n.setLayout(new GridLayout(2, 1, 0, 5));
		n.add(new JPanel());
		n.add(cmdTransform);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(left);
		panel.add(right);
		panel.add(n);
		return panel;

		//		JPanel n = new JPanel();
		//		n.setLayout(new GridLayout(2, 1, 0, 5));
		//		n.add(boxRate);
		//		n.add(cmdTransform);
		//
		//		JPanel panel = new JPanel();
		//		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//		panel.add(left);
		//		panel.add(right);
		//		panel.add(n);
		//		return panel;
	}
	
	/**
	 * 滚动窗口
	 * @return
	 */
	private JPanel createRightScrollPane() {
		JScrollPane scroll = new JScrollPane(targetLabel);
		scroll.setBorder(UITools.createTitledBorder(null, 0));

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(5, 0));
		center.add(scroll, BorderLayout.CENTER);

		JPanel southEast = new JPanel();
		southEast.setLayout(new BorderLayout(10, 0));
		southEast.add(cmdTranslucent, BorderLayout.WEST);
		southEast.add(cmdSave, BorderLayout.EAST);
		
		JPanel south = new JPanel();
		south.setLayout(new BorderLayout());
		south.add(southEast, BorderLayout.EAST);
		south.add(new JPanel(), BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(center, BorderLayout.CENTER);
		panel.add(south, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 中间的面板
	 * @return
	 */
	private JPanel createCenterPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
		panel.setLayout(new GridLayout(1, 2, 5, 0));
		panel.add(createLeftScrollPane());
		panel.add(createRightScrollPane());
		return panel;
	}
	
	private JPanel createRightButtonPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout());
//		sub.add(cmdTransform, BorderLayout.WEST);
		sub.add(new JPanel(), BorderLayout.CENTER);
		sub.add(cmdExit, BorderLayout.EAST);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(sub, BorderLayout.SOUTH);
		return panel;
	}
	
	/**
	 * 按纽面板
	 * @return
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		
		panel.add(createLeftSpinnerPanel());
		panel.add(createRightButtonPanel());
		return panel;
	}
	
	/**
	 * 初始化面板
	 * @return
	 */
	private JPanel initPanel() {
		// 初始化显示控件
		initControls();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 5));
		setRootBorder(panel);
		panel.add(createCenterPanel(), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		doRate();
		
		return panel;
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		JPanel pane = initPanel();
		setContentPane(pane);

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setMinimumSize(new Dimension(400, 300));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/ImageTransform/title");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();
		setVisible(true);
	}

}