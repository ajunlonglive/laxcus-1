/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.build;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import com.laxcus.application.boot.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.choice.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 生成对话框
 * 
 * @author scott.liang
 * @version 1.0 7/25/2021
 * @since laxcus 1.0
 */
public class DesktopBuildDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -4851640768260640649L;

	/** 引导单元 **/
	private JTextField txtBoot;

	/** 选择引导 **/
	private FlatButton cmdSelectBoot;
	
	
	/** 生成软件包 **/
	private FlatButton cmdBuild, cmdReset;
	
	/** 退出 **/
	private FlatButton cmdExit; 
	
	/**
	 * 构造实例
	 */
	public DesktopBuildDialog(){
		super();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
//		addThread(new ClickThread(e));
		click(e);
	}


//	class ClickThread extends SwingEvent {
//		ActionEvent event;
//
//		ClickThread(ActionEvent e) {
//			super();
//			event = e;
//		}
//
//		public void process() {
//			click(event);
//		}
//	}

	private void click(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdExit) {
			exit();
		} else if(source == cmdBuild) {
			build();
		} else if(source == cmdReset) {
			reset();
		}
		// 其它按纽
		else if(source == cmdSelectBoot) {
			doSelectBoot();
		}
		// JAR包
		else if (source == cmdJarSelect) {
			doSelectJar();
		} else if (source == cmdJarDelete) {
			doDeleteJar();
		}
		// 动态链接库
		else if (source == cmdLibSelect) {
			doSelectLib();
		} else if (source == cmdLibDelete) {
			doDeleteLib();
		}
		// 其他文件
		else if (source == cmdOtherSelect) {
			doSelectOther();
		} else if (source == cmdOtherDelete) {
			doDeleteOther();
		}
	}

	private void exit() {
		String content = UIManager.getString("BuildDialog.exitMessageText");
		boolean success = MessageBox.showYesNoDialog(this, getTitle(), content);
		if (!success) {
			return;
		}

		// 保存范围
		saveBounds();
		// 关闭窗口
		super.closeWindow();
	}
	
	private boolean check() {
		File file = getBootstrap();
		if (file == null) {
			cmdSelectBoot.requestFocus();
			return false;
		}
		File[] files = getJarFiles();
		if (files.length < 1) {
			cmdJarSelect.requestFocus();
			return false;
		}

		return true;
	}
	
	private File getBootstrap() {
		String text = this.txtBoot.getText();
		if (text.isEmpty()) {
			return null;
		}
		File file = new File(text);
		boolean success = (file.exists() && file.isFile());
		return (success ? file : null);
	}
	
	private File[] getJarFiles() {
		int size = modelJar.getSize();
		File[] files = new File[size];
		for(int i =0; i < size; i++) {
			BuildItem item = (BuildItem)	modelJar.getElementAt(i);
			files[i] = item.getFile();
		}
		return files;
	}
	
	private File[] getLibFiles() {
		int size = modelLib.getSize();
		
		File[] files = new File[size];
		for(int i =0; i < size; i++) {
			BuildItem item = (BuildItem)	modelLib.getElementAt(i);
			files[i] = item.getFile();
		}
		return files;
	}
	
	private File[] getOtherFiles() {
		int size = modelOther.getSize();
		
		File[] files = new File[size];
		for(int i =0; i < size; i++) {
			BuildItem item = (BuildItem)	modelOther.getElementAt(i);
			files[i] = item.getFile();
		}
		return files;
	}
	
	private void build() {
		boolean success = check();
		if (!success) {
			String text = UIManager.getString("BuildDialog.missingTitleText");
			String content = UIManager.getString("BuildDialog.missingContentText");
			MessageBox.showWarning(this, text, content); // "参数不足", "参数不足，请检查重新输入！");
			return;
		}
		
		// 保存位置
		String text = UIManager.getString("BuildDialog.buildSoftwareTitleText");
		String description = UIManager.getString("BuildDialog.descriptionSoftwareText");
		String extension = UIManager.getString("BuildDialog.extensionSoftwareText");
		// 选择文件
		File dest = saveFile(text, description, extension);
		if (dest == null) {
			return;
		}

		// 文件
		File bootstrap = getBootstrap();
		File[] jars = getJarFiles();
		File[] libs = this.getLibFiles();
		File[] others = this.getOtherFiles();

		// 建立包文件
		success = false;
		try {
			ApplicationCreator.createBasket(bootstrap, jars, libs, others, dest);
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}
		
		String title = UIManager.getString("BuildDialog.buildResultTitleText");
		// 提示
		if (success) {
			String content = UIManager.getString("BuildDialog.buildResultSuccessful");
			MessageBox.showInformation(this, title, content);
		} else {
			String content = UIManager.getString("BuildDialog.buildResultFailed");
			content = String.format(content, Laxkit.canonical(dest));
			MessageBox.showFault(this, title, content);
		}
	}

	/**
	 * 重置
	 */
	private void reset() {
		txtBoot.setText("");
		modelJar.clear();
		modelLib.clear();
		modelOther.clear();
	}

	/**
	 * 引导文件
	 */
	private void doSelectBoot() {
		String text = UIManager.getString("BuildDialog.titleBootText");
		String description = UIManager.getString("BuildDialog.descriptionBootText");
		String extension = UIManager.getString("BuildDialog.extensionBootText");
		File file = selectSingleFile(text, description, extension);
		if (file != null) {
			String filename = Laxkit.canonical(file);
			txtBoot.setText(filename);
		}
	}
	
	/**
	 * 找到文件和系统图标
	 * @param file
	 * @return
	 */
	private Icon findSystemIcon(File file) {
		FileSystemView fsv = FileSystemView.getFileSystemView();
		return fsv.getSystemIcon(file);
	}
	
	/**
	 * JAR文件
	 */
	private void doSelectJar() {
		String text = UIManager.getString("BuildDialog.titleJarText");
		String description = UIManager.getString("BuildDialog.descriptionJarText");
		String extension = UIManager.getString("BuildDialog.extensionJarText");
		// 选择文件
		File[] files = selectMultiFiles(text, description, extension);
		int size = (files == null ? 0 : files.length);
		// 多选
		for (int i = 0; i < size; i++) {
			File file = files[i];
			BuildItem item = new BuildItem(file);
			item.setIcon(findSystemIcon(file));

			if (!modelJar.contains(item)) {
				modelJar.addElement(item);
			}
		}
		
		// if (file != null) {
		// BuildItem item = new BuildItem(file);
		// item.setIcon(findSystemIcon(file));
		//
		// if (!modelJar.contains(item)) {
		// modelJar.addElement(item);
		// }
		// }
	}
	
	/**
	 * 删除JAR文件
	 */
	private void doDeleteJar() {
		Object object = listJar.getSelectedValue();
		if (object != null) {
			modelJar.removeElement(object);
		}
	}

	/**
	 * Lib文件
	 */
	private void doSelectLib() {
		String text = UIManager.getString("BuildDialog.titleLibText");
		String description = UIManager.getString("BuildDialog.descriptionLibText");
		String extension = UIManager.getString("BuildDialog.extensionLibText");
		
		// 选择文件
		File[] files = selectMultiFiles(text, description, extension);
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			BuildItem item = new BuildItem(file);
			item.setIcon(findSystemIcon(file));

			if (!modelLib.contains(item)) {
				modelLib.addElement(item);
			}
		}
		
//		// 选择文件
//		File file = selectFile(text, description, extension);
//		if (file != null) {
//			BuildItem item = new BuildItem(file);
//			item.setIcon(findSystemIcon(file));
//			
//			if (!modelLib.contains(item)) {
//				modelLib.addElement(item);
//			}
//		}
	}
	
	/**
	 * 删除Lib文件
	 */
	private void doDeleteLib() {
		Object object = listLib.getSelectedValue();
		if (object != null) {
			modelLib.removeElement(object);
		}
	}
	
	/**
	 * Other文件
	 */
	private void doSelectOther() {
		String text = UIManager.getString("BuildDialog.titleOtherText");
		String description = UIManager.getString("BuildDialog.descriptionOtherText");
		String extension = UIManager.getString("BuildDialog.extensionOtherText");
		
		// 多选文件
		File[] files = selectMultiFiles(text, description, extension);
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			BuildItem item = new BuildItem(file);
			item.setIcon(findSystemIcon(file));
			// 保存
			if (!modelOther.contains(item)) {
				modelOther.addElement(item);
			}
		}
		
//		// 选择文件
//		File file = selectFile(text, description, extension);
//		if (file != null) {
//			BuildItem item = new BuildItem(file);
//			item.setIcon(findSystemIcon(file));
//			
//			if (!modelOther.contains(item)) {
//				modelOther.addElement(item);
//			}
//		}
	}
	
	/**
	 * 删除Other文件
	 */
	private void doDeleteOther() {
		Object object = listOther.getSelectedValue();
		if (object != null) {
			modelOther.removeElement(object);
		}
	}
	
	/**
	 * 询问覆盖...
	 * @param file 磁盘文件
	 * @return 返回真或者假
	 */
	protected boolean override(File file) {
		// 提示错误
		String title = UIManager.getString("BuildDialog.overrideTitleText");
		String content = UIManager.getString("BuildDialog.overrideContentText");
		String format = String.format(content, Laxkit.canonical(file));
		// 选择...
		return MessageBox.showYesNoDialog(this, title, format);
	}
	
//	private File saveFile(String title, String description, String extension) {
//		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SaveFile");
//		
//		DiskFileFilter filter = new DiskFileFilter(description, extension);
//		
//		// 显示窗口
//		JFileChooser chooser = new JFileChooser();
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.addChoosableFileFilter(filter);
//		
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle(title);
//		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
//		
//		String buttonText = UIManager.getString("BuildDialog.saveButtonText");
//		chooser.setApproveButtonText(buttonText);
//		chooser.setApproveButtonToolTipText(buttonText);
//
//		// 指定目录
//		if (filename != null) {
//			File file = new File(filename);
//			chooser.setCurrentDirectory(file.getParentFile());
//		}
//		
//		int val = chooser.showOpenDialog(this);
//		// 显示窗口
//		if (val != JFileChooser.APPROVE_OPTION) {
//			return null;
//		}
//
//		File file = chooser.getSelectedFile();
//		// 判断符合名称要求
//		if (!filter.accept(file)) {
//			filename = Laxkit.canonical(file);
//			String[] exts = filter.getExtensions();
//			filename = String.format("%s.%s", filename, exts[0]);
//			file = new File(filename);
//		}
//		
//		// 判断文件存在，是否覆盖
//		if (file.exists() && file.isFile()) {
//			boolean success = override(file);
//			if (!success) {
//				return null;
//			}
//		}
//		
//		// 保存文件名
//		filename = Laxkit.canonical(file);
//		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SaveFile", filename);
//
//		return file;
//	}
	
//	private File saveFile(String title, String description, String extension) {
//		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SaveFile");
//		
//		DiskFileMatcher filter = new DiskFileMatcher(description, extension);
//
//		// 显示窗口
//		ChoiceDialog chooser = new ChoiceDialog(title);
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.addFileMatcher(filter);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setDialogType(DialogOption.SAVE_DIALOG);
//		chooser.setFileSelectionMode(DialogOption.FILES_ONLY); // 只选择文件
//
//		//		chooser.addChoosableFileFilter(filter);
//		//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		//		chooser.setDialogTitle(title);
//		//		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
//		//		chooser.setTitle(title);
//		//		chooser.setSelectionType(DialogType.SINALE_SELECT_TYPE);
//		
//		String buttonText = UIManager.getString("BuildDialog.saveButtonText");
//		chooser.setApproveButtonText(buttonText);
//		chooser.setApproveButtonToolTipText(buttonText);
//
//		// 指定目录
//		if (filename != null) {
//			File file = new File(filename);
//			chooser.setCurrentDirectory(file.getParentFile());
//		}
//		
//		// 显示窗口
//		Object obj = chooser.showDialog(this);
//		if (obj == null) {
//			return null;
//		}
//		
////		int val = chooser.showOpenDialog(this);
////		// 显示窗口
////		if (val != JFileChooser.APPROVE_OPTION) {
////			return null;
////		}
////		File file = chooser.getSelectedFile();
//		
//		File file = ((File[]) obj)[0];
//		// 判断符合名称要求
//		if (!filter.accept(file)) {
//			filename = Laxkit.canonical(file);
//			String exts = filter.getExtension();
//			filename = String.format("%s.%s", filename, exts);
//			file = new File(filename);
//		}
//		
//		// 判断文件存在，是否覆盖
//		if (file.exists() && file.isFile()) {
//			boolean success = override(file);
//			if (!success) {
//				return null;
//			}
//		}
//		
//		// 保存文件名
//		filename = Laxkit.canonical(file);
//		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SaveFile", filename);
//
//		return file;
//	}
	
	private File saveFile(String title, String description, String extension) {
		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SaveFile");
		
		DiskFileMatcher filter = new DiskFileMatcher(description, extension);

		// 显示窗口
		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.addFileMatcher(filter);
		dialog.setMultiSelectionEnabled(false);
		dialog.setDialogType(DialogOption.SAVE_DIALOG);
		dialog.setFileSelectionMode(DialogOption.FILES_ONLY); // 只选择文件

		String buttonText = UIManager.getString("BuildDialog.saveButtonText");
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);

		// 指定目录
		if (filename != null) {
			File file = new File(filename);
			dialog.setCurrentDirectory(file.getParentFile());
		}
		
		// 显示窗口
		File[] files = dialog.showDialog(this);
		if (files == null || files.length == 0) {
			return null;
		}
		
		File file = files[0];
		
		// 判断符合名称要求
		if (!filter.accept(file)) {
			filename = Laxkit.canonical(file);
			String exts = filter.getExtension();
			filename = String.format("%s.%s", filename, exts);
			file = new File(filename);
		}
		
		// 判断文件存在，是否覆盖
		if (file.exists() && file.isFile()) {
			boolean success = override(file);
			if (!success) {
				return null;
			}
		}
		
		// 保存文件名
		filename = Laxkit.canonical(file);
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SaveFile", filename);

		return file;
	}
	
//	private File selectFile(String title, String description, String extension){
//		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SelectFile");
//		
//		DiskFileFilter filter = new DiskFileFilter(description, extension);
//		
//		// 显示窗口
//		JFileChooser chooser = new JFileChooser();
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.addChoosableFileFilter(filter);
//
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle(title);
//		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//
//		String buttonText = UIManager.getString("BuildDialog.selectButtonText");
//		chooser.setApproveButtonText(buttonText);
//		chooser.setApproveButtonToolTipText(buttonText);
//		
//		// 指定目录
//		if (filename != null) {
//			File file = new File(filename);
//			chooser.setCurrentDirectory(file.getParentFile());
//		}
//		
//		int val = chooser.showOpenDialog(this);
//		// 显示窗口
//		if (val != JFileChooser.APPROVE_OPTION) {
//			return null;
//		}
//
//		File file = chooser.getSelectedFile();
//		if (file != null) {
//			filename = Laxkit.canonical(file);
//			RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SelectFile", filename);
//		}
//		return file;
//	}
	
	private File[] selectFiles(String title, String description, String extension, boolean multiSelectionEnabled){
		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SelectFile");
		
		DiskFileMatcher filter = new DiskFileMatcher(description, extension);
		
		// 显示窗口
		ChoiceDialog chooser = new ChoiceDialog(title);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(multiSelectionEnabled); // 多选或者单选
		chooser.setFileSelectionMode(DialogOption.FILES_ONLY);
		chooser.setDialogType(DialogOption.OPEN_DIALOG);
		// 一个文件适配器
		chooser.addFileMatcher(filter);

		String buttonText = UIManager.getString("BuildDialog.selectButtonText");
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);
		
		// 指定目录
		if (filename != null) {
			File file = new File(filename);
			chooser.setCurrentDirectory(file.getParentFile());
		}

		Object obj = chooser.showDialog(this, true);
		if (obj == null) {
			return null;
		}

//		File file = chooser.getSelectedFile();
		File[] files = (File[])obj;
		File file = files[0];
		if (file != null) {
			filename = Laxkit.canonical(file);
			RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SelectFile", filename);
		}
		return files;
	}
	
	/**
	 * 单选文件
	 * @param title
	 * @param description
	 * @param extension
	 * @return
	 */
	private File selectSingleFile(String title, String description, String extension){
		File[] files = selectFiles(title, description, extension, false);
		return (files == null ? null : files[0]);
	}
	
	/**
	 * 多选文件
	 * @param title
	 * @param description
	 * @param extension
	 * @return
	 */
	private File[] selectMultiFiles(String title, String description, String extension){
		return selectFiles(title, description, extension, true);
	}
	
	
//	private File selectFile(String title, String description, String extension){
//		String filename = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SelectFile");
//		
//		DiskFileMatcher filter = new DiskFileMatcher(description, extension);
//		
//		// 显示窗口
//		ChoiceDialog chooser = new ChoiceDialog(title);
////		chooser.setAcceptAllFileFilterUsed(false);
////		chooser.addChoosableFileFilter(filter);
//
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode( DialogType.FILES_ONLY );
//		chooser.setDialogType(DialogType.OPEN_DIALOG);
//		chooser.addFileMatcher(filter);
//
////		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
////		chooser.setDialogTitle(title);
////		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
////		chooser.setSelectionType(DialogType.SINALE_SELECT_TYPE);
////		chooser.setTitle(title);
//		
//		String buttonText = UIManager.getString("BuildDialog.selectButtonText");
//		chooser.setApproveButtonText(buttonText);
//		chooser.setApproveButtonToolTipText(buttonText);
//		
//		// 指定目录
//		if (filename != null) {
//			File file = new File(filename);
//			chooser.setCurrentDirectory(file.getParentFile());
//		}
//		
////		int val = chooser.showOpenDialog(this);
////		// 显示窗口
////		if (val != JFileChooser.APPROVE_OPTION) {
////			return null;
////		}
//		
//		Object obj = chooser.showDialog(this, true);
//		if (obj == null) {
//			return null;
//		}
//
////		File file = chooser.getSelectedFile();
//		File file = ((File[]) obj)[0];
//		if (file != null) {
//			filename = Laxkit.canonical(file);
//			RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/SelectFile", filename);
//		}
//		return file;
//	}

	private void saveBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "BuildDialog/Bound", rect);
	}

	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"BuildDialog/Bound");
		if (bounds == null) {
			int w = 518;
			int h = 588;

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (d.width - w) / 2;
			int y = (d.height - h) / 2;
			y = (y > 20 ? 20 : (y < 0 ? 0 : y)); // 向上提高
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

	public void updateUI() {
		super.updateUI();

		// 更新UI界面
		if (renderer != null) {
			renderer.updateUI();
		}

		// 不要更新UI，否则会在updateUI死循环
		FontKit.updateDefaultFonts(this, false);
	}

	private FlatButton createButton(String text, char w) {
		FlatButton but = new FlatButton();
		FontKit.setButtonText(but, text);

		but.setIconTextGap(4);
		but.addActionListener(this);
		if (w > 32) {
			but.setMnemonic(w);
		}

		return but;
	}
	
	private FlatButton createButton(String text) {
		return createButton(text, (char)0);
	}

	private JPanel createSouth() {
		cmdBuild = createButton(UIManager.getString("BuildDialog.buildButtonText"), 'C');
		cmdReset = createButton(UIManager.getString("BuildDialog.resetButtonText"), 'R');
		cmdExit = createButton(UIManager.getString("BuildDialog.exitButtonText"), 'X');
		
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(1, 2, 4, 0));
		left.add(cmdBuild);
		left.add(cmdReset);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		panel.add(left, BorderLayout.WEST);
		panel.add(cmdExit, BorderLayout.EAST);
		return panel;
	}
	
	private JPanel createBootPanel() {
		String title = UIManager.getString("BuildDialog.bootLabelText");
		txtBoot = new JTextField();
		txtBoot.setEditable(false);
		cmdSelectBoot = createButton(UIManager.getString("BuildDialog.selectBootButtonText"));

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 1, 0, 4));
		sub.add(cmdSelectBoot);
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(sub, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		panel.setBorder(UITools.createTitledBorder(title, 4));
		panel.add(txtBoot, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);

		return panel;
	}
	
	private void setListCellHeight(JList list) {
		String text = UIManager.getString("BuildDialog.listCellHeightText");
		int value = ConfigParser.splitInteger(text, 28);
		list.setFixedCellHeight(value);
	}
	
	private BuildItemCellRenderer renderer;

	private JList listJar = new JList();

	private DefaultListModel modelJar = new DefaultListModel();

	private FlatButton cmdJarSelect, cmdJarDelete;

	private JPanel createJarPanel() {
		String title = UIManager.getString("BuildDialog.jarLabelText");
		listJar.setVisibleRowCount(3);
		listJar.setModel(modelJar);
		listJar.setCellRenderer(renderer);
		listJar.setBorder(new EmptyBorder(1, 1, 1, 1));
		setListCellHeight(listJar);
		JScrollPane jsp = new JScrollPane(listJar);
//		jsp.setBorder(new HighlightBorder(1));

		cmdJarSelect = createButton(UIManager.getString("BuildDialog.selectJarButtonText"));
		cmdJarDelete = createButton(UIManager.getString("BuildDialog.deleteJarButtonText"));

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 4));
		sub.add(cmdJarSelect);
		sub.add(cmdJarDelete);
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(sub, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		panel.setBorder(UITools.createTitledBorder(title, 4));
		panel.add(jsp, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		return panel;
	}

	private JList listLib = new JList();
	private DefaultListModel modelLib = new DefaultListModel();
	
	private FlatButton cmdLibSelect, cmdLibDelete;

	private JPanel createLibraryPanel() {
		String title = UIManager.getString("BuildDialog.libLabelText");
		listLib.setVisibleRowCount(3);
		listLib.setModel(modelLib);
		listLib.setCellRenderer(renderer);
		listLib.setBorder(new EmptyBorder(1,1,1,1));
		setListCellHeight(listLib);
		JScrollPane jsp = new JScrollPane(listLib);
//		jsp.setBorder(new HighlightBorder(1));
		
		cmdLibSelect = createButton(UIManager.getString("BuildDialog.selectLibButtonText"));
		cmdLibDelete = createButton(UIManager.getString("BuildDialog.deleteLibButtonText"));

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 4));
		sub.add(cmdLibSelect);
		sub.add(cmdLibDelete);
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(sub, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		panel.setBorder(UITools.createTitledBorder(title,4));
		panel.add(jsp, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		return panel;
	}

	private JList listOther = new JList();
	private DefaultListModel modelOther = new DefaultListModel();
	private FlatButton cmdOtherSelect, cmdOtherDelete;

	private JPanel createOtherPanel() {
		String title = UIManager.getString("BuildDialog.otherLabelText");
		setListCellHeight(listOther);
		listOther.setVisibleRowCount(4);
		listOther.setModel(modelOther);
		listOther.setCellRenderer(renderer);
		listOther.setBorder(new EmptyBorder(1,1,1,1));
		JScrollPane jsp = new JScrollPane(listOther);
//		jsp.setBorder(new HighlightBorder(1));
		
		cmdOtherSelect = createButton(UIManager.getString("BuildDialog.selectOtherButtonText"));
		cmdOtherDelete = createButton(UIManager.getString("BuildDialog.deleteOtherButtonText"));

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 4));
		sub.add(cmdOtherSelect);
		sub.add(cmdOtherDelete);
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout());
		right.add(sub, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 0));
		panel.setBorder(UITools.createTitledBorder(title,4));
		panel.add(jsp, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		return panel;
	}
	
	private JComponent createCenter() {
		renderer = new BuildItemCellRenderer();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createBootPanel());
		panel.add(createJarPanel());
		panel.add(createLibraryPanel());
		panel.add(createOtherPanel());

		// 滚动
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
		return scroll;
	}

	private void initDialog() {
		setTitle(UIManager.getString("BuildDialog.Title"));
		setFrameIcon(UIManager.getIcon("BuildDialog.TitleIcon"));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 4));
		panel.add(createCenter(), BorderLayout.NORTH);
		panel.add(createSouth(), BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(4, 6, 6, 6));

		setContentPane(panel);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);

		setClosable(false);
		setIconifiable(false);
		setMaximizable(false);

		setBounds();

		if (modal) {
			return super.showModalDialog(parent);
		} else {
			return super.showNormalDialog(parent);
		}
	}

}
