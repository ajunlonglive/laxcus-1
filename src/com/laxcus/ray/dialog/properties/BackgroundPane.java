/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.properties;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import com.laxcus.ray.panel.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.choice.*;
import com.laxcus.gui.dialog.color.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 桌面背景选择面板
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class BackgroundPane extends RayPanel implements ActionListener {

	private static final long serialVersionUID = -2494734743823066873L;

	/** 实例 **/
	private PlatformDesktop desktop;

	/** 桌面背景方案，包括颜色、图像、图像排列方式 **/
	private DesktopWall wall = new DesktopWall();

	/** 图像 **/
	private JLabel image = new JLabel();

	/** 背景图像 **/
	private DefaultListModel imgModel = new DefaultListModel();
	private JList imgList = new JList();
	private BackgroundImageCellRenderer imageRenderer;

	/** 布局 **/
	private DefaultComboBoxModel mdLayout = new DefaultComboBoxModel();
	private JComboBox boxLayout = new JComboBox();
	private BackgroundLayoutCellRenderer layoutRenderer;

	/** 选择图像按纽 **/
	private FlatButton cmdImage = new FlatButton();

	/** 删除图像 **/
	private FlatButton cmdDelete = new FlatButton();
	
	/** 选择颜色按纽 **/
	private ColorButton cmdColor = new ColorButton();
	
	/** 重置颜色 **/
	private FlatButton cmdResetColor = new FlatButton("...");

	/** 激活按纽 **/
	private FlatButton cmdActive = new FlatButton();

	/**
	 * 构造默认的桌面背景选择面板
	 */
	public BackgroundPane() {
		super();
	}

	/**
	 * 设置桌面
	 * @param e
	 */
	public void setDesktopPane(PlatformDesktop e){
		desktop = e;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdImage) {
			chooseFile();
//			choiceCloudFile();
		} else if(source == cmdDelete) {
			deleteImage();
		} else if (source == cmdColor) {
			doSelectColor();
		} else if(source == cmdResetColor) {
			doResetColor();
		} else if (source == cmdActive) {
			active();
		}
	}
	
	private void active() {
		Object e = mdLayout.getSelectedItem();
		// 选择一个布局方式
		if (e.getClass() == BackgroundLayoutItem.class) {
			BackgroundLayoutItem item = (BackgroundLayoutItem) e;
			wall.setLayout(item.getLayout());
		}

		// 保存颜色，无论是否有
		Color c = cmdColor.getColor();
		wall.setColor(c);

		// 保存图像
		Image icon = readImage(selectImageFile);
		// 图标有效或者无效时...
		if (icon != null) {
			wall.setImage(icon);
			// 设置文件
			wall.setFile(selectImageFile);
		} else {
			wall.setImage(null);
			// 设置文件
			wall.setFile(null);
		}

		// 调整背景方案
		desktop.setDesktopWall(wall);
		// 保存背景色
		saveBackground();
	}

	/**
	 * 保存背景参数
	 */
	private void saveBackground() {
		Color color = wall.getColor();
		if (color != null) {
			RTKit.writeColor(RTEnvironment.ENVIRONMENT_SYSTEM,
					"RayWindow/Background/Color", color);
		} else {
			RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/Color",
					RTokenAttribute.FOLDER); // 注意，颜色是目录
		}
		
		// 图像
		File file = wall.getFile();
		if (file != null) {
			String filename = Laxkit.canonical(file);
			RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM,  "RayWindow/Background/File", filename);
			RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/Layout",
					wall.getLayout());
		} else {
			RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/File",
					RTokenAttribute.PARAMETER);
			RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, "RayWindow/Background/Layout",
					RTokenAttribute.PARAMETER);
		}
	}
	
	/**
	 * 取出背景图像
	 * @return
	 */
	private File readBackgroundImageFile() {
		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM,
				"RayWindow/Background/File");
		return (filename != null ? new File(filename) : null);
	}

//	/**
//	 * 选择颜色
//	 */
//	protected void chooseColor() {
//		String title = UIManager.getString("PropertiesDialog.backgroundColorChoiceText");
//		Color c = (background.getColor() != null ? background.getColor() : Color.BLACK);
//		c = JColorChooser.showDialog(this, title, c);
//		if (c != null) {
//			background.setColor(c);
//			cmdColor.setColor(c);
//		} else {
//			background.setColor(null);
//		}
//	}
	
//	/**
//	 * 选择颜色
//	 */
//	protected void chooseColor() {
//		Color defaultColor = background.getColor(); // cmdColor.getColor();
//		ColorDialog dialog = new ColorDialog(defaultColor); // background.getColor());
//		Color c = (Color) dialog.showDialog(this, true);
//
//		//		String title = UIManager.getString("PropertiesDialog.backgroundColorChoiceText");
//		//		Color c = (background.getColor() != null ? background.getColor() : Color.BLACK);
//		//		c = JColorChooser.showDialog(this, title, c);
//
//		if (c != null) {
////			background.setColor(c);
//			cmdColor.setColor(c);
//		} else {
//			background.setColor(null);
//		}
//	}
	
	/**
	 * 选择颜色
	 */
	private void doSelectColor() {
		Color dc = cmdColor.getColor();
		ColorDialog dialog = new ColorDialog(dc);
		
		Color back = (Color) dialog.showDialog(this, true);
		// 如果返回空值，是取消，此时被忽略
		if (back != null) {
			cmdColor.setColor(back);
			saveColorButtonValue(back);
		}
	}
	
	/**
	 * 重置颜色
	 */
	private void doResetColor() {
		cmdColor.setColor(null);
		saveColorButtonValue(null);
	}
	
	/**
	 * 保存颜色按纽的颜色值
	 * @param c
	 */
	private void saveColorButtonValue(Color c) {
		if (c != null) {
			RTKit.writeColor(RTEnvironment.ENVIRONMENT_SYSTEM,
					"PropertiesDialog/Background/Button/Color", c);
		} else {
			RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog/Background/Button/Color",
					RTokenAttribute.FOLDER); // 注意，颜色是目录
		}
	}

//	/**
//	 * 从中找到匹配的选项
//	 * @param chooser 文件选择器
//	 * @param selectDescription 选中的描述
//	 */
//	private void chooseFileFilter(JFileChooser chooser, String selectDescription) {
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
	
	/**
	 * 从中找到匹配的选项
	 * @param dialog 文件选择器
	 * @param selectDescription 选中的描述
	 */
	private void setEnabledDescription(ChoiceDialog dialog, String selectDescription) {
		if (selectDescription == null) {
			return;
		}
		// 选择
		FileMatcher[] matchers = dialog.getFileMatchers();
		for (int i = 0; matchers != null && i < matchers.length; i++) {
			FileMatcher e = matchers[i];
			String des = e.getDescription();
			if (Laxkit.compareTo(selectDescription, des) == 0) {
				dialog.setSelectFileMatcher(e);
				break;
			}
		}
	}
	
//	/**
//	 * 从中找到匹配的选项
//	 * @param dialog 文件选择器
//	 * @param selectDescription 选中的描述
//	 */
//	private void setEnabledDescription(CloudChoiceDialog dialog, String selectDescription) {
//		if (selectDescription == null) {
//			return;
//		}
//		// 选择
//		FileMatcher[] matchers = dialog.getFileMatchers();
//		for (int i = 0; matchers != null && i < matchers.length; i++) {
//			FileMatcher e = matchers[i];
//			String des = e.getDescription();
//			if (Laxkit.compareTo(selectDescription, des) == 0) {
//				dialog.setSelectFileMatcher(e);
//				break;
//			}
//		}
//	}
	
//	/**
//	 * 保存选项
//	 * @param chooser 文件选择器
//	 */
//	protected String getSelectFileType(JFileChooser chooser) {
//		javax.swing.filechooser.FileFilter e = chooser.getFileFilter();
//		if (e == null || e.getClass() != DiskFileFilter.class) {
//			return null;
//		}
//
//		DiskFileFilter filter = (DiskFileFilter) e;
//		return filter.getDescription();
//	}

//	/**
//	 * 设置开放目录
//	 * @param chooser
//	 */
//	private void setReadDirectory(JFileChooser chooser) {
////		Object memory = UITools.getProperity(READ_FILE);
////		if (memory != null && memory.getClass() == String.class) {
////			File file = new File((String) memory);
////			if (file.exists() && file.isFile()) {
////				chooser.setCurrentDirectory(file.getParentFile());
////			}
////		}
//		
//		String memory = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM,
//				"PropertiesDialog/Background/ChoiceFile");
//		if (memory != null) {
//			File file = new File(memory);
//			if (file.exists() && file.isFile()) {
//				chooser.setCurrentDirectory(file.getParentFile());
//			}
//		}
//	}
	
	/**
	 * 设置开放目录
	 * @param chooser
	 */
	private void setReadDirectory(ChoiceDialog chooser) {
		//		Object memory = UITools.getProperity(READ_FILE);
		//		if (memory != null && memory.getClass() == String.class) {
		//			File file = new File((String) memory);
		//			if (file.exists() && file.isFile()) {
		//				chooser.setCurrentDirectory(file.getParentFile());
		//			}
		//		}

		String memory = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM,
				"PropertiesDialog/Background/ChoiceFile");
		if (memory != null) {
			File file = new File(memory);
			if (file.exists() && file.isFile()) {
				File dir = file.getParentFile();
				if (dir == null) {
					dir = file;
				}
				chooser.setCurrentDirectory(dir);
			}
		}
	}
	
	/**
	 * 保存读的脚本文件
	 * @param file
	 */
	private void setReadFile(File file) {
		// 最后选中的文件
		String filename = Laxkit.canonical(file);
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog/Background/ChoiceFile", filename);

		// 图像文件
		long code = com.laxcus.util.each.EachTrustor.sign(filename);
		String paths = String.format("PropertiesDialog/Background/ImageFile/%X",code);
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, paths, filename);

		//		System.out.printf("write %s -> %s | %s\n", paths, filename, (b?"成功":"失败"));
		//		RTEnvironment environment = RTEnvironment.getInstance();
		//		RElement element = environment.findElement(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog");
		//		if (element != null) {
		//			element.print();
		//		}
	}
	
	/**
	 * 删除图像
	 */
	private void deleteImage() {
		Object object =	imgList.getSelectedValue();

		//		if (object == null) {
		//			return;
		//		}
		//		System.out.println(object.getClass().getName());
		//		
		//		// BackgroundLayoutItem

		boolean success = (object != null && Laxkit.isClassFrom(object, BackgroundImageItem.class));
		if (!success) {
			return;
		}

		// 没有路径，忽略它
		BackgroundImageItem item = (BackgroundImageItem) object;
		File path = item.getPath();
		if (path == null) {
			return;
		}
		imgModel.removeElement(object);

		// 清除图标
		image.setIcon(null);
		
		// 删除记录
		String filename = Laxkit.canonical(path);
		long code = com.laxcus.util.each.EachTrustor.sign(filename);
		String paths = String.format("PropertiesDialog/Background/ImageFile/%X",code);
		RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, paths, RTokenAttribute.PARAMETER);
		
//		System.out.printf("delete %s -> %s is %s\n", paths, filename, (b ? "成功" : "失败"));
	}
	
//	/**
//	 * 保存选项
//	 * @param chooser 文件选择器
//	 */
//	protected String getSelectFileType(ChoiceDialog dialog) {
////		javax.swing.filechooser.FileFilter e = chooser.getFileFilter();
////		if (e == null || e.getClass() != DiskFileFilter.class) {
////			return null;
////		}
////
////		DiskFileFilter filter = (DiskFileFilter) e;
////		return filter.getDescription();
//	}

//	/** 选择的文件类型选项 **/
//	private static String selectType;
	
	/**
	 * 读类型定义
	 * @return
	 */
	private String readSelectDescription() {
		return RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM,
				"PropertiesDialog/Background/SelectDescription");
	}

	/**
	 * 写入选择的类型
	 * @param type
	 */
	private void writeSelectDescription(String type) {
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM,
				"PropertiesDialog/Background/SelectDescription", type);
	}
	
//	private void chooseFile() {
//		String title = UIManager.getString("PropertiesDialog.backgroundChoiceTitle"); // findCaption("Dialog/ImageTransform/open-chooser/title/title");
//		String buttonText = UIManager.getString("PropertiesDialog.backgroundChoiceButtonText"); // findCaption("Dialog/ImageTransform/open-chooser/choose/title");
//
//		// JPEG文件
//		String ds_jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionJpegText"); // findCaption("Dialog/ImageTransform/open-chooser/jpeg/description/title");
//		String jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionJpegText"); // findCaption("Dialog/ImageTransform/open-chooser/jpeg/extension/title");
//		// GIF文件
//		String ds_gif = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionGifText"); // findCaption("Dialog/ImageTransform/open-chooser/gif/description/title");
//		String gif = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionGifText"); // findCaption("Dialog/ImageTransform/open-chooser/gif/extension/title");
//		// PNG文件
//		String ds_png = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionPngText"); // findCaption("Dialog/ImageTransform/open-chooser/png/description/title");
//		String png = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionPngText"); // findCaption("Dialog/ImageTransform/open-chooser/png/extension/title");
//
//		DiskFileMatcher f1 = new DiskFileMatcher(ds_jpeg, jpeg);
//		DiskFileMatcher f2 = new DiskFileMatcher(ds_gif, gif);
//		DiskFileMatcher f3 = new DiskFileMatcher(ds_png, png);
//
//		ChoiceDialog dialog = new ChoiceDialog();
//		dialog.setTitle(title);
//		
//		dialog.setAcceptAllFileFilterUsed(false);
//		dialog.addFileMatcher(f3);
//		dialog.addFileMatcher(f2);
//		dialog.addFileMatcher(f1);
//		// 选择类型
//		String description = readSelectDescription();
//		if (description != null) {
//			setEnabledDescription(dialog, description);
//		}
//		
//		// 打开模式
//		dialog.setMultiSelectionEnabled(false);
//		dialog.setDialogType(DialogType.OPEN_DIALOG);
//		dialog.setFileSelectionMode(DialogType.FILES_ONLY);
//		dialog.setApproveButtonText(buttonText);
//		dialog.setApproveButtonToolTipText(buttonText);
//
//		setReadDirectory(dialog);
//		
//		// 返回的是File数组
//		Object obj = dialog.showDialog(this, true);
//		if (obj == null) {
//			return;
//		}
//		
//		FileMatcher matcher = dialog.getSelectFileMatcher(); 
//		if (matcher != null) {
//			description = matcher.getDescription();
//			writeSelectDescription(description);
//		}
//
//		File[] files = (File[]) obj;
//		File file = files[0];
//		// 必须是文件!
//		boolean success = (file.exists() && file.isFile());
//		if (success) {
//			success = !isExistsFile(file); // 文件不存在
//			if (!success) {
//				addThread(new ShowExistsFileThread(file));
//			}
//		}
//		if (success) {
//			addThread(new ReadShowImageThread(file, true));
//		}
//	}
	
//	/**
//	 * 测试，打开云端目录
//	 */
//	private void choiceCloudFile() {
//		String title = UIManager.getString("PropertiesDialog.backgroundChoiceTitle"); 
//		String buttonText = UIManager.getString("PropertiesDialog.backgroundChoiceButtonText"); 
//
//		// JPEG文件
//		String ds_jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionJpegText"); 
//		String jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionJpegText"); 
//		// GIF文件
//		String ds_gif = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionGifText"); 
//		String gif = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionGifText"); 
//		// PNG文件
//		String ds_png = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionPngText"); 
//		String png = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionPngText"); 
//
//		DiskFileMatcher f1 = new DiskFileMatcher(ds_jpeg, jpeg);
//		DiskFileMatcher f2 = new DiskFileMatcher(ds_gif, gif);
//		DiskFileMatcher f3 = new DiskFileMatcher(ds_png, png);
//
//		CloudChoiceDialog dialog = new CloudChoiceDialog();
//		dialog.setTitle(title);
//		
//		dialog.setAcceptAllFileFilterUsed(false);
//		dialog.addFileMatcher(f1);
//		dialog.addFileMatcher(f2);
//		dialog.addFileMatcher(f3);
//		// 选择类型
//		String description = readSelectDescription();
//		if (description != null) {
//			setEnabledDescription(dialog, description);
//		}
//		
//		// 打开模式
//		dialog.setMultiSelectionEnabled(true); // 选择多个
//		dialog.setDialogType(DialogOption.OPEN_DIALOG);
//		dialog.setFileSelectionMode(DialogOption.FILES_ONLY);
//		dialog.setApproveButtonText(buttonText);
//		dialog.setApproveButtonToolTipText(buttonText);
//
////		setReadDirectory(dialog);
//		
//		// 返回的是File数组
//		Object obj = dialog.showDialog(this, true);
//		if (obj == null) {
//			return;
//		}
//		
////		FileMatcher matcher = dialog.getSelectFileMatcher(); 
////		if (matcher != null) {
////			description = matcher.getDescription();
////			writeSelectDescription(description);
////		}
//		
//	}

	
//	private boolean hasMemoryFile(File file) {
//		String paths = "PropertiesDialog/Background/ImageFile";
//		RTEnvironment environment = RTEnvironment.getInstance();
//		RFolder folder = environment.findFolder(RTEnvironment.ENVIRONMENT_SYSTEM, paths);
//		// 找到目录
//		if (folder != null) {
//			java.util.List<RParameter> list = folder.getParameters();
//			for (RParameter param : list) {
//				// 非字符串，忽略它
//				if (!Laxkit.isClassFrom(param, RString.class)) {
//					continue;
//				}
//				// 判断文件存在
//				RString str = (RString) param;
//				File other = new File(str.getValue());
//				// 文件一致
//				if (file.compareTo(other) == 0) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	private void chooseFile() {
		String title = UIManager.getString("PropertiesDialog.backgroundChoiceTitle"); 
		String buttonText = UIManager.getString("PropertiesDialog.backgroundChoiceButtonText"); 

		// JPEG文件
		String ds_jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionJpegText");
		String jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionJpegText");
		// GIF文件
		String ds_gif = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionGifText");
		String gif = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionGifText");
		// PNG文件
		String ds_png = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionPngText");
		String png = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionPngText");

//		PropertiesDialog.backgroundChoiceDescriptionAllImageText 所有图像文件
//		PropertiesDialog.backgroundChoiceExtensionAllImageText  JPG,JPEG,PNG,GIF
		
		DiskFileMatcher f1 = new DiskFileMatcher(ds_jpeg, jpeg);
		DiskFileMatcher f2 = new DiskFileMatcher(ds_gif, gif);
		DiskFileMatcher f3 = new DiskFileMatcher(ds_png, png);
		
		DiskFileMatcher all = new DiskFileMatcher(
				UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionAllImageText"),
				UIManager.getString("PropertiesDialog.backgroundChoiceExtensionAllImageText"));

		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.addFileMatcher(all);
		dialog.addFileMatcher(f1);
		dialog.addFileMatcher(f2);
		dialog.addFileMatcher(f3);
		
		// 选择类型
		String description = readSelectDescription();
		if (description != null) {
			setEnabledDescription(dialog, description);
		}
		
//		// 测试，显示字符集
//		dialog.setShowCharsetEncode(true);
		
		// 打开模式
		dialog.setMultiSelectionEnabled(true); // 选择多个
		dialog.setDialogType(DialogOption.OPEN_DIALOG);
		dialog.setFileSelectionMode(DialogOption.FILES_ONLY);
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);

		setReadDirectory(dialog);
		
		// 返回的是File数组
		File[] files = dialog.showDialog(this);
		int size = (files != null ? files.length : 0);
		if (size < 1) {
			return;
		}
		
		FileMatcher matcher = dialog.getSelectFileMatcher(); 
		if (matcher != null) {
			description = matcher.getDescription();
			writeSelectDescription(description);
		}
		
		//		File[] files = (File[]) obj;
		//		// 区分是一个或者多个
		//		if (files.length > 0) {

		ArrayList<File> a = new ArrayList<File>();
		for (File file : files) {
			boolean success = (file.exists() && file.isFile());
			if (success) {
				success = !isExistsFile(file); // 文件存在
				if (!success) {
					showExistsFile(file);
				}
			}
			if (success) {
				a.add(file);
			}
		}
		for (File file : a) {
			addThread(new ReadShowImageThread(file, true));
		}
		//		} 
		
//		else {
//			File file = files[0];
//			// 必须是文件!
//			boolean success = (file.exists() && file.isFile());
//			if (success) {
//				success = !isExistsFile(file); // 文件不存在
//				if (!success) {
//					addThread(new ShowExistsFileThread(file));
//				}
//			}
//			if (success) {
//				addThread(new ReadShowImageThread(file, true));
//			}
//		}
	}
	
	private void showExistsFile(File file) {
		String title = UIManager.getString("PropertiesDialog.backgroundImageFileExistTitle");
		String content = UIManager.getString("PropertiesDialog.backgroundImageFileExistContent");
		String filename = Laxkit.canonical(file);
		content = String.format(content, filename);

		MessageBox.showWarning(this, title, content);
	}
	
//	class ShowExistsFileThread extends SwingEvent {
//		File file;
//		public ShowExistsFileThread(File f){
//			super();
//			file = f;
//		}
//		public void process() {
//			showExistsFile(file);
//		}
//	}

//	/**
//	 * 选择图像文件
//	 */
//	private void chooseFile2() {
//		String title = UIManager.getString("PropertiesDialog.backgroundChoiceTitle"); // findCaption("Dialog/ImageTransform/open-chooser/title/title");
//		String buttonText = UIManager.getString("PropertiesDialog.backgroundChoiceButtonText"); // findCaption("Dialog/ImageTransform/open-chooser/choose/title");
//
//		// JPEG文件
//		String ds_jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionJpegText"); // findCaption("Dialog/ImageTransform/open-chooser/jpeg/description/title");
//		String jpeg = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionJpegText"); // findCaption("Dialog/ImageTransform/open-chooser/jpeg/extension/title");
//		// GIF文件
//		String ds_gif = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionGifText"); // findCaption("Dialog/ImageTransform/open-chooser/gif/description/title");
//		String gif = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionGifText"); // findCaption("Dialog/ImageTransform/open-chooser/gif/extension/title");
//		// PNG文件
//		String ds_png = UIManager.getString("PropertiesDialog.backgroundChoiceDescriptionPngText"); // findCaption("Dialog/ImageTransform/open-chooser/png/description/title");
//		String png = UIManager.getString("PropertiesDialog.backgroundChoiceExtensionPngText"); // findCaption("Dialog/ImageTransform/open-chooser/png/extension/title");
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
//		chooseFileFilter(chooser, selectType);
//
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle(title);
//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//		chooser.setApproveButtonText(buttonText);
//		chooser.setApproveButtonToolTipText(buttonText);
//
//		// 读取的目录
//		setReadDirectory(chooser);
//
//		// 打开的对话框
//		int val = chooser.showOpenDialog(this);
//		// 显示窗口
//		if (val != JFileChooser.APPROVE_OPTION) {
//			return;
//		}
//
//		// 新的选择类型
//		selectType = getSelectFileType(chooser);
//
//		File file = chooser.getSelectedFile();
//		boolean success = (file.exists() && file.isFile());
//		if (success) {
//			success = !hasImageFile(file); // 文件不存在
//		}
//		if (success) {
//			addThread(new ReadShowImageThread(file, true));
//		}
//	}
	
	/**
	 * 判断图像文件已经存在
	 * @param file
	 * @return
	 */
	private boolean isExistsFile(File file) {
		int size = imgModel.size();
		for (int i = 0; i < size; i++) {
			BackgroundImageItem e = (BackgroundImageItem) imgModel.get(i);
			boolean success = (Laxkit.compareTo(e.getPath(), file) == 0);
			if (success) {
				return true;
			}
		}
		return false;
	}

	class ReadShowImageThread extends SwingEvent {
		File file;

		boolean save;
		
//		long startTime ;

		ReadShowImageThread(File file, boolean save) {
			super(true);
			setFile(file);
			setSave(save);
//			startTime = System.currentTimeMillis();
		}

		void setFile(File e) {
			file = e;
		}

		void setSave(boolean b) {
			save = b;
		}

		public void process() {
			if (RayPropertiesDialog.selfHandle == null) {
				return;
			}
			
			readAndShowImage(file, save);
			
//			System.out.printf("%s 耗时 %d ms\n", Laxkit.canonical(file), System.currentTimeMillis() - startTime);
		}
	}

//	/**
//	 * 读图像
//	 * @param file
//	 * @return
//	 */
//	private ImageIcon readImage(File file) {
//		if (file == null) {
//			return null;
//		}
//		boolean success = (file.exists() && file.isFile());
//		if (!success) {
//			return null;
//		}
//		try {
//			// 从磁盘中读取文件，生成图像
//			byte[] b = new byte[(int) file.length()];
//			FileInputStream in = new FileInputStream(file);
//			in.read(b);
//			in.close();
//			return new ImageIcon(b);
//		} catch (IOException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		return null;
//	}
	
	/**
	 * 读图像
	 * @param file
	 * @return Image实例
	 */
	private Image readImage(File file) {
		if (file == null) {
			return null;
		}
		boolean success = (file.exists() && file.isFile());
		// 读文件
		try {
			if (success) {
				return ImageIO.read(file);
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

//	/**
//	 * 转换图像
//	 * @param source
//	 * @param compressWidth
//	 * @param compressHeight
//	 * @param type
//	 * @return
//	 * @throws IOException
//	 */
//	private ImageIcon convert(File source, int compressWidth, int compressHeight)  throws IOException {
//		FileInputStream in = new FileInputStream(source);
//		BufferedImage image = ImageIO.read(in);
//		in.close();
//
//		// 平滑缩小图象
//		Image compress = image.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
//		// 生成一个新图像
//		BufferedImage buff = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
//		Graphics2D gra = buff.createGraphics();
//		buff = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
//		buff.getGraphics().drawImage(compress, 0, 0, null);
//		
//		return new ImageIcon(buff);
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
	private Image convert(File source, int compressWidth, int compressHeight)  throws IOException {
		FileInputStream in = new FileInputStream(source);
		BufferedImage image = ImageIO.read(in);
		in.close();

		// 平滑缩小图象
		Image compress = image.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
		// 生成一个新图像
		BufferedImage buff = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = buff.createGraphics();
		buff = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
		buff.getGraphics().drawImage(compress, 0, 0, null);
		gra.dispose();
		
		return buff; // new ImageIcon(buff);
	}
	
	/**
	 * 显示错误
	 * @param file
	 */
	private void showReadError(File file) {
		String title = UIManager.getString("PropertiesDialog.backgroundReadImageErrorTitle");
		String content = UIManager.getString("PropertiesDialog.backgroundReadImageErrorContent");
		MessageBox.showFault(this, title, content);
	}
	
	class ShowReadErrorThread extends SwingEvent {
		File file;

		public ShowReadErrorThread(File f) {
			super();
			file = f;
		}

		public void process() {
			showReadError(file);
		}
	}
	
	// 记录下来，等待激活
//	private ImageIcon selectImage;
	private Image selectImage;
	private File selectImageFile;

	/**
	 * 找到文件和系统图标
	 * @param file
	 * @return
	 */
	private Icon findSystemIcon(File file) {
		FileSystemView fsv = FileSystemView.getFileSystemView();
		return fsv.getSystemIcon(file);
	}
	
//	/**
//	 * 等比例缩小图片尺寸
//	 * @param screen 屏幕尺寸
//	 * @param imageWidth 图像原始宽度
//	 * @param imageHeight 图像原始高度
//	 * @return 缩小后的尺寸，或者空指针
//	 */
//	private Dimension zoomX(Dimension screen, int imageWidth, int imageHeight) {
//		double rate = 1.0f;
//		do {
//			int w = (int) ((double) imageWidth * rate);
//			int h = (int) ((double) imageHeight * rate);
//			if (w < screen.width && h < screen.height) {
//				return new Dimension(w, h);
//			}
//			rate = rate - 0.01f;
//		} while (rate > 0.0f);
//
//		return null;
//	}
	
	/**
	 * 等比例缩小图片尺寸
	 * @param screen 屏幕尺寸
	 * @param imageWidth 图像原始宽度
	 * @param imageHeight 图像原始高度
	 * @return 缩小后的尺寸，或者空指针
	 */
	private Dimension zoom(Dimension screen, int imageWidth, int imageHeight) {
		// 取最小比例值
		double rw = (double) (screen.width) / (double) imageWidth;
		double rh = (double) (screen.height) / (double) imageHeight;
		double rate = (rw < rh ? rw : rh);

		// 压缩后的尺寸
		int w = (int) (rate * (double) imageWidth);
		int h = (int) (rate * (double) imageHeight);
		
//		System.out.printf("screen:%d,%d, image:%d,%d, rw:%.3f, rh:%.3f, rate:%.3f, compress:%d,%d\n",
//						screen.width, screen.height, imageWidth, imageHeight, rw, rh, rate, w, h);

		// 小于指定值时...
		if (w < 1 || h < 1) {
			return null;
		}

		return new Dimension(w, h);
	}
	
	/**
	 * 输入新的图像文件
	 * @param file
	 */
	private void readAndShowImage(File file, final boolean saveItem) {
		// 清除和退出
		if (file == null) {
			image.setIcon(null);
			selectImage = null;
			selectImageFile = null;
			wall.setImage(null);
			wall.setFile(null);
			return;
		}

		// 读取图像
		selectImage = readImage(file);
		if (selectImage == null) {
			addThread(new ShowReadErrorThread(file));
			return;
		}
//		int width = selectImage.getIconWidth();
//		int height = selectImage.getIconHeight();
		
		int width = selectImage.getWidth(null);
		int height = selectImage.getHeight(null);
		selectImageFile = file;
		
		// 提示
		String tooltip = String.format("%d x %d", width, height);
		image.setToolTipText(tooltip);
		
		// 图像标签的屏幕尺寸
		Dimension screen = image.getSize();
		
		// 大于图像尺寸时...
		if (screen.width >= width && screen.height >= height) {
			image.setIcon(new ImageIcon(selectImage));
		} else {
			// 计算等比例缩小尺寸
			Dimension zoom = zoom(screen, width, height);
			// 等比例
			if (zoom != null) {
				try {
					// selectIcon = convert(file, d.width, d.height);
					selectImage = convert(file, zoom.width, zoom.height);
					image.setIcon(new ImageIcon(selectImage));
				} catch (IOException e) {
					Logger.error(e);
				}
			} else {
				image.setIcon(null); // 清除图像
			}
		}

		// 显示单元
		if (saveItem) {
			BackgroundImageItem item = new BackgroundImageItem(file);
			item.setIcon(findSystemIcon(file));
			item.setWidth(width);
			item.setHeight(height);
			
			imgModel.addElement(item);
			// 移动选择项
			int size = imgModel.getSize();
			if (size > 0) {
//				imgList.setSelectedIndex(size - 1);
				imgList.setSelectedValue(item, true);
			}
		}

		// 保存文件名称
		if (saveItem) {
			setReadFile(file);
		}
	}

//	private Color getBorderColor() {
//		if (LightKit.isNimbusUI()) {
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
	
	private JPanel createImagePanel() {
		JLabel label = new JLabel();
		String str = UIManager.getString("PropertiesDialog.backgroundIntroduceText");
		str = String.format("<html>%s</html>", str);
		FontKit.setLabelText(label, str);
//		label.setText(UIManager.getString("PropertiesDialog.backgroundIntroduceText"));
		
		image.setHorizontalAlignment(SwingConstants.CENTER);
		
		JScrollPane jsp = new JScrollPane(image);
		jsp.setBorder(new HighlightBorder(1));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 10));
		panel.add(label, BorderLayout.NORTH);
		panel.add(jsp, BorderLayout.CENTER);
		return panel;
	}
	
	class ReadImageSizeThread extends SwingEvent {
		File file;
		BackgroundImageItem item;
		boolean last;
		
		ReadImageSizeThread(File f , BackgroundImageItem e) {
			super(true); // 同步处理
			file = f;
			item = e;
			last = false;
		}

		public void process() {
			if (RayPropertiesDialog.selfHandle == null) {
				return;
			}
			// 读图标
			Image icon = readImage(file);
			if (icon != null) {
				item.setWidth(icon.getWidth(null));
				item.setHeight(icon.getHeight(null));
			}
			
			// 如果是最后一个，更新全部
			if (last) {
				imgList.revalidate();
				imgList.repaint();
			}
		}
	}
	
	private JPanel createLayoutPanel() {
		imgModel.addElement(new BackgroundImageItem(null, UIManager.getString("PropertiesDialog.backgroundImageListNone"))); // "无"));
		
		ArrayList<SwingEvent> threads = new ArrayList<SwingEvent>();
		
		// 读记录
		File recordFile = readBackgroundImageFile();
		BackgroundImageItem focus = null;
		
		String paths = "PropertiesDialog/Background/ImageFile";
		RTEnvironment environment = RTEnvironment.getInstance();
		RFolder folder = environment.findFolder(RTEnvironment.ENVIRONMENT_SYSTEM, paths);
		// 找到目录
		if (folder != null) {
			java.util.List<RParameter> list = folder.getParameters();
			for (RParameter param : list) {
				// 非字符串，忽略它
				if (!Laxkit.isClassFrom(param, RString.class)) {
					continue;
				}
				// 判断文件存在
				RString str = (RString) param;
				File file = new File(str.getValue());
				// 判断文件存在且有效
				boolean success = (file.exists() && file.isFile());
				if (success) {
					BackgroundImageItem item = new BackgroundImageItem(file);
					item.setIcon(findSystemIcon(file));

//					// 读图标
//					Icon icon = readImage(file);
//					if (icon != null) {
//						item.setWidth(icon.getIconWidth());
//						item.setHeight(icon.getIconHeight());
//					}

					// 保存到队列中
					imgModel.addElement(item);
					
					// 记录图片线程，延时处理
					ReadImageSizeThread rt = new ReadImageSizeThread(file, item);
					rt.setTouchTime(System.currentTimeMillis() + 2000);
					threads.add(rt);

					// 判断一致...
					boolean match = (recordFile != null && recordFile.compareTo(file) == 0);
					if (match) {
						focus = item;
					}
				}
			}
		}
		imgList.setModel(imgModel);
		imgList.setCellRenderer(imageRenderer = new BackgroundImageCellRenderer());
//		imgList.addListSelectionListener(new ImageSelectionListener());
		imgList.addMouseListener(new ImageMouseAdapter());
		imgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选，只能选一个
		imgList.setVisibleRowCount(6); // 6行
		imgList.setFixedCellHeight(26); // 26个像素
		imgList.setBorder(new EmptyBorder(1,1,1,1));
		// 显示选中的对象
		if (focus != null) {
			imgList.setSelectedValue(focus, true);
		}
//		// 鼠标事件
//		imgList.addMouseListener(new ListMouseAdapter());

		JScrollPane jsp = new JScrollPane(imgList);
		jsp.setBorder(new HighlightBorder(1));
		
		// 输出到对象中
		int members = threads.size();
		if (members > 0) {
			ReadImageSizeThread rt = (ReadImageSizeThread) threads.get(members - 1);
			rt.last = true;
			SwingDispatcher.invokeThreads(threads);
		}
		
		// 滚动
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(jsp, BorderLayout.CENTER);
		return panel;
	}
	
	
	private FlatButton createButton(String text, char w) {
		FlatButton button = new FlatButton();
		FontKit.setButtonText(button, UIManager.getString(text));
		button.setMnemonic(w);
		button.addActionListener(this);
		return button;
	}
	
	private JLabel createLabel(String text, char w, Component component) {
		JLabel label = new JLabel();
		String str = UIManager.getString(text);
		str = String.format("<html>%s</html>", str);

		FontKit.setLabelText(label, str);
		label.setDisplayedMnemonic(w);
		label.setLabelFor(component);
		return label;
	}
	
	private JPanel createButtonPanel() {
		mdLayout.addElement(new BackgroundLayoutItem(UIManager.getString("PropertiesDialog.backgroundFullText"), DesktopWall.FULL));
		mdLayout.addElement(new BackgroundLayoutItem(UIManager.getString("PropertiesDialog.backgroundCenterText"), DesktopWall.MIDDLE));
		mdLayout.addElement(new BackgroundLayoutItem(UIManager.getString("PropertiesDialog.backgroundMultiText"), DesktopWall.MULTI));
		boxLayout.setLightWeightPopupEnabled(false); // 重量级组件
		boxLayout.setRenderer(layoutRenderer = new BackgroundLayoutCellRenderer());
		boxLayout.setModel(mdLayout);
		boxLayout.setSelectedIndex(0);
		
		// 生成图像
		cmdImage = createButton("PropertiesDialog.backgroundChoiceImageButtonText",'B'); // "选择图像");
		cmdDelete = createButton("PropertiesDialog.backgroundDeleteImageButtonText",'D'); // "选择图像");
		cmdActive = createButton("PropertiesDialog.backgroundActiveButtonText",'A'); //"应用");
		
		// 参数...
		cmdColor.setFocusPainted(false);
		cmdColor.addActionListener(this);
		// 读环境中的颜色
		Color c = RTKit.readColor(RTEnvironment.ENVIRONMENT_SYSTEM, "PropertiesDialog/Background/Button/Color");
		cmdColor.setColor(c);
		// 如果有默认的背景颜色，设置它
		c = desktop.getDesktopWallColor();
		if (c != null) {
			cmdColor.setColor(c);
		}
		
		// 重置颜色按纽
		cmdResetColor.setIconTextGap(0);
		cmdResetColor.addActionListener(this);
		
		JPanel s1 = new JPanel();
		s1.setLayout(new GridLayout(1, 2, 6, 0));
		s1.add(cmdImage);
		s1.add(cmdDelete);

		JPanel s2 = new JPanel();
		s2.setLayout(new BorderLayout(4, 0));
		JLabel label = createLabel("PropertiesDialog.backgroundLayoutChoiceText", 'L', boxLayout); 
		s2.add(label, BorderLayout.WEST); // 布局
		s2.add(boxLayout, BorderLayout.CENTER);
//		s2.add(new JPanel(), BorderLayout.EAST);
		s2.setBorder(new EmptyBorder(0, 4, 0, 0));

		JPanel s3 = new JPanel();
		s3.setLayout(new BorderLayout(4, 0));
		label = createLabel("PropertiesDialog.backgroundColorChoiceText", 'C', cmdColor);
		s3.add(label, BorderLayout.WEST); // 颜色
		
		JPanel ss = new JPanel();
		ss.setLayout(new BorderLayout(4,0));
		ss.add(cmdColor, BorderLayout.CENTER);
		ss.add(cmdResetColor, BorderLayout.EAST);
		ss.setBorder(new EmptyBorder(0, 0, 0, 0));
		
//		 s3.add(cmdColor, BorderLayout.CENTER);
		s3.add(ss, BorderLayout.CENTER);
//		s3.add(new JPanel(), BorderLayout.EAST);
		s3.setBorder(new EmptyBorder(0, 4, 0, 0));

		JPanel north = new JPanel();
		north.setLayout(new GridLayout(3, 1, 0, 4));
		north.add(s1);
		north.add(s2);
		north.add(s3);
		
		
		JPanel s4 = new JPanel();
		s4.setLayout(new GridLayout(1, 2, 0, 0));
		s4.add(cmdActive);
		s4.add(new JLabel());
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(north, BorderLayout.NORTH);
		panel.add(s4, BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(2, 0, 0, 0));
		
		return panel;
	}
	
	private JPanel createBottomPanel() {
		// 背景
		JLabel label = createLabel("PropertiesDialog.backgroundBackgroundText", 'X', imgList); 

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(4, 0));
		center.add(createLayoutPanel(), BorderLayout.CENTER);
		center.add(createButtonPanel(), BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 4));
		panel.add(label, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 建立面板
	 * @param e
	 */
	public void init(PlatformDesktop e) {
		setDesktopPane(e);
		
		setLayout(new BorderLayout(0, 10));
		setBorder(new EmptyBorder(10, 4, 4, 4));
		add(createImagePanel(), BorderLayout.CENTER);
		add(createBottomPanel(), BorderLayout.SOUTH);
	}

//	/**
//	 * 建立面板
//	 * @param e
//	 */
//	public void createPane2(PlatformPane e) {
//		setDesktopPane(e);
//
//		mdLayout.addElement(new BackgroundLayoutItem(UIManager.getString("PropertiesDialog.backgroundFullText"), PlatfromBackground.FULL));
//		mdLayout.addElement(new BackgroundLayoutItem(UIManager.getString("PropertiesDialog.backgroundCenterText"), PlatfromBackground.CENTER));
//		mdLayout.addElement(new BackgroundLayoutItem(UIManager.getString("PropertiesDialog.backgroundMultiText"), PlatfromBackground.MULTI));
//		boxLayout.setLightWeightPopupEnabled(false); // 重量级组件
//		boxLayout.setRenderer(layoutRenderer = new BackgroundLayoutCellRenderer());
//		boxLayout.setModel(mdLayout);
//		boxLayout.setSelectedIndex(0);
//
//		imgModel.addElement(new BackgroundImageItem(null, UIManager.getString("PropertiesDialog.backgroundImageListNone"))); // "无"));
//		
//		String paths = "PropertiesDialog/Background/ImageFile";
//		RTEnvironment environment = RTEnvironment.getInstance();
//		RFolder folder = environment.findFolder(RTEnvironment.ENVIRONMENT_SYSTEM, paths);
//		// 找到目录
//		if (folder != null) {
//			java.util.List<RParameter> list = folder.getParameters();
//			for (RParameter param : list) {
//				// 非字符串，忽略它
//				if (!Laxkit.isClassFrom(param, RString.class)) {
//					continue;
//				}
//				// 判断文件存在
//				RString str = (RString) param;
//				File file = new File(str.getValue());
//				if (file.exists() && file.isFile()) {
//					BackgroundImageItem item = new BackgroundImageItem(file);
//					imgModel.addElement(item);
//				}
//			}
//		}
//		
//		
//		//		imageModel.addElement(new BackgroundImageItem("ONE"));
//		//		imageModel.addElement(new BackgroundImageItem("TWO"));
//		//		imageModel.addElement(new BackgroundImageItem("THREE"));
//		//		imageModel.addElement(new BackgroundImageItem("FOUR"));
//		//		imageModel.addElement(new BackgroundImageItem("SIX"));
//		//		imageModel.addElement(new BackgroundImageItem("EVENT"));
//		//		imageModel.addElement(new BackgroundImageItem("NIGHT"));
//		//		imageModel.addElement(new BackgroundImageItem("TEN"));
//		//		imageModel.addElement(new BackgroundImageItem("TEN2"));
//		//		imageModel.addElement(new BackgroundImageItem("今人不见古时月，今月曾经照古人!"));
//		imgList.setModel(imgModel);
//		imgList.setCellRenderer(imageRenderer = new BackgroundImageCellRenderer());
//		imgList.addListSelectionListener(this);
//		imgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//
//		//		boxColor.addActionListener(new DuckActionListener());
//		//		
//		//		boxColor.setEnabled(true);
//		//		boxColor.setVisible(true);
//
//		//		JLabel text = new JLabel("图像布局");
//
//		//		text.setLabelFor(boxColor);
//
//		//		boxColor.setSelectedIndex(1);
//
//		//		boxColor.setLightWeightPopupEnabled(false); // 重量级组件
//		//		boxColor.addItem("ONE");
//		//		boxColor.addItem("TWO");
//		//		boxColor.addItem("THREE");
//		//		boxColor.addItem("FOUR");
//		//		boxColor.setSelectedIndex(2);
//
//		//		colorBox.setEditable(false);
//		//		cbxColor.setSelectedIndex(0);
//
//
//
//		FontKit.setButtonText(cmdImage, UIManager
//				.getString("PropertiesDialog.backgroundChoiceImageButtonText")); // "选择图像");
//		FontKit.setButtonText(cmdDelete, UIManager
//				.getString("PropertiesDialog.backgroundDeleteImageButtonText")); // "选择图像");
//
//		FontKit.setButtonText(cmdActive, UIManager.getString("PropertiesDialog.backgroundActiveButtonText")); //"应用");
//		cmdColor.setFocusPainted(false);
//
//		// 事件
//		cmdImage.addActionListener(this);
//		cmdDelete.addActionListener(this);
//		cmdColor.addActionListener(this);
//		cmdActive.addActionListener(this);
//
//		image.setHorizontalAlignment(SwingConstants.CENTER);
//		//		ImageIcon icon = findImage("conf/ray/image/window/background/ef.png");
//		//		image.setBorder(new MatteBorder(new Insets(4, 4, 4, 4), icon));
//
//		JScrollPane scroll = new JScrollPane(image);
//		//		scroll.getViewport().setAutoscrolls(true);
//		//		scroll.setBorder(new EmptyBorder(2, 2, 2, 2));
//
//		//		ImageIcon icon = createDanceImage();
//		//		scroll.setBorder(new MatteBorder(new Insets(3, 3, 3, 3), icon));
//
//		Color c = getBorderColor();
//		scroll.setBorder(new MatteBorder(new Insets(2,2,2,2), c));
//
//		// 可视化统计
//		imgList.setVisibleRowCount(6);
//		imgList.setFixedCellHeight(26);
//		imgList.setFixedCellWidth(110);
//		JScrollPane cb = new JScrollPane(imgList);
//
//		JPanel s1 = new JPanel();
//		s1.setLayout(new GridLayout(4, 1, 0, 4));
//		s1.add(cmdImage);
//		s1.add(cmdDelete);
//		s1.add(boxLayout);
//		s1.add(cmdColor);
//
//		JPanel s2 = new JPanel();
//		s2.setLayout(new BorderLayout());
//		s2.add(s1, BorderLayout.NORTH);
//		s2.add(cmdActive, BorderLayout.SOUTH);
//
//		JPanel s3 = new JPanel();
//		s3.setLayout(new BorderLayout());
//		s3.add(new JPanel(), BorderLayout.CENTER);
//		//		s3.add(boxColor, BorderLayout.CENTER);
//		s3.add(s2, BorderLayout.WEST);
//
//		JPanel m = new JPanel();
//		m.setLayout(new BorderLayout(0, 4));
//		m.add(cb, BorderLayout.NORTH);
//		m.add(s3, BorderLayout.CENTER);
//
//		setLayout(new BorderLayout(4, 0));
//		setBorder(new EmptyBorder(10, 4, 4, 4));
//		add(scroll, BorderLayout.CENTER);
//		add(m, BorderLayout.EAST);
//	}



//	private void exchange(ListSelectionEvent e) {
//		Object source = e.getSource();
//		if (source == null) {
//			return;
//		}
//
//		// 选择项
//		if (source == imgList) {
//			int index = imgList.getSelectedIndex();
//			if (index >= 0 && index < imgModel.size()) {
//				Object obj = imgModel.getElementAt(index);
//				if (!Laxkit.isClassFrom(obj, BackgroundImageItem.class)) {
//					return;
//				}
//				BackgroundImageItem item = (BackgroundImageItem) obj;
//				// 显示图像
//				addThread(new ReadShowImageThread(item.getPath(), false));
//			}
//		}
//	}

//	class ChangeThread extends SwingEvent {
//		ListSelectionEvent event;
//
//		ChangeThread(ListSelectionEvent e) {
//			super();
//			event = e;
//		}
//
//		public void process() {
//			exchange(event);
//		}
//	}

//	/* (non-Javadoc)
//	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
//	 */
//	@Override
//	public void valueChanged(ListSelectionEvent event) {
//		if (!event.getValueIsAdjusting()) {
//			addThread(new ChangeThread(event));
//		}
//	}
	
//	private void doSelectImageIndex() {
//		int index = imgList.getSelectedIndex();
//		if (index >= 0 && index < imgModel.size()) {
//			Object obj = imgModel.getElementAt(index);
//			if (!Laxkit.isClassFrom(obj, BackgroundImageItem.class)) {
//				return;
//			}
//			BackgroundImageItem item = (BackgroundImageItem) obj;
//			// 显示图像
//			addThread(new ReadShowImageThread(item.getPath(), false));
//		}
//	}
	
	
	
//	class ImageSelectionListener implements ListSelectionListener {
//
//		/* (non-Javadoc)
//		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
//		 */
//		@Override
//		public void valueChanged(ListSelectionEvent e) {
//			if (lockReadImage) {
//				return;
//			}
//			// 调整中不处理，调整结束后处理
//			if (!e.getValueIsAdjusting()) {
//				doSelectImageIndex();
//			}
//		}
//	}
	
	class ImageMouseAdapter extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			// doSelectImageIndex();

			// 找到鼠标位置的  
			int index = imgList.locationToIndex(e.getPoint());
			if (index >= 0 && index < imgModel.size()) {
				Object obj = imgModel.getElementAt(index);
				if (!Laxkit.isClassFrom(obj, BackgroundImageItem.class)) {
					return;
				}
				BackgroundImageItem item = (BackgroundImageItem) obj;
				// 显示图像
				addThread(new ReadShowImageThread(item.getPath(), false));
			}
		}
	}

	public void updateUI() {
		super.updateUI();

		if (imageRenderer != null) {
			imageRenderer.updateUI();
		}
		if (layoutRenderer != null) {
			layoutRenderer.updateUI();
		}
	}
}

//	class ListMouseAdapter extends MouseAdapter {
//
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			int count = e.getClickCount();
//			if (count == 1) {
//				int index = imgList.getSelectedIndex();
//				if (index >= 0 && index < imgModel.size()) {
//					Object obj = imgModel.getElementAt(index);
//					if (!Laxkit.isClassFrom(obj, BackgroundImageItem.class)) {
//						return;
//					}
//					BackgroundImageItem item = (BackgroundImageItem) obj;
//					// 显示图像
//					addThread(new ReadShowImageThread(item.getPath(), false