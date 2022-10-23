/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.install;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import com.laxcus.application.factory.*;
import com.laxcus.application.manage.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.choice.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.event.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.skin.*;

/**
 * 安装软件对话框
 * 
 * @author scott.liang
 * @version 1.0 7/6/2021
 * @since laxcus 1.0
 */
public class DesktopInstallDialog extends LightDialog implements ActionListener, TreeSelectionListener {

	private static final long serialVersionUID = -7751194458131770526L;

	/** 站点根目录 **/
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(); 

	/** 树形渲染器 **/
	private InstallTreeCellRenderer treeRenderer;

	/** 次层树模型 **/
	private DefaultTreeModel treeModel;

	/** 站点结构 **/
	private JTree tree;

	/** 显示表格 **/
	private JTable table;

	private InstallTableModel tableModel;

	private InstallTableCellRenderer tableRenderer;

	/** 左右分割栏面板 **/
	private JSplitPane splitPane;

	/** 显示文本 **/
	private JLabel lblText = new JLabel();

	/** 选择文件 **/
	private FlatButton cmdChoice;

	/** 安装和清除 **/
	private FlatButton cmdInstall;

	private FlatButton cmdReset;

	/** 退出按纽 **/
	private FlatButton cmdExit;

	/** 部署到桌面 **/
	private JComboBox cbxDesktop;

	/** 部署到应用坞 **/
	private JComboBox cbxDock;

	//	private LaunchMenu launchMenu;

	/** 安装工厂 **/
	private InstallFactory factory;

	/** 部署到桌面的参数 **/
	private DefaultCellEditor desktopEditor ;

	private InstallTableDeployDesktopIconCellRenderer tableDeployDesktopIconRenderer;

	private InstallComboBoxDeployDesktopCellRenderer comboxDesktopRenderer;

	/** 部署到应用坞的参数 **/
	private DefaultCellEditor dockEditor ;

	private InstallTableDeployDockIconCellRenderer tableDeployDockIconRenderer;

	private InstallComboBoxDeployDockCellRenderer comboxDockRenderer;

	static final String DESKTOP_DEPLOYICON = "DeployToDesktop";

	static final String DOCK_DEPLOYICON = "DeployToDock";

	private WElement currentBoot;

	private WRoot boot = null;

	/**
	 * 构造安装软件对话框
	 * @param e 安装工厂实例
	 */
	public DesktopInstallDialog(InstallFactory e) {
		super();
		factory = e;
		setRefreshUI(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent event) {
		switchTo(event);
	}

	/**
	 * 执行线程的切换操作
	 * @param e
	 */
	private void switchTo(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		// 空指针，忽略它！
		if (path == null) {
			return;
		}
		Object source = path.getLastPathComponent();

		if (source.getClass() != InstallTreeNode.class) {
			return;
		}

		// 单元
		InstallTreeNode n = (InstallTreeNode)source;

		// 记录当前实例
		currentBoot = n.getItem();

		//		System.out.printf("%s\n", currentBoot.getClass().getName() );

		// 清除
		tableModel.clear();

		//		InstallDialog.tableCellTitleText 标题
		//		InstallDialog.tableCellIconText 图标
		//		InstallDialog.tableCellDeployDesktopText 部署到桌面

		// 显示新的
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, UIManager.getString("InstallDialog.tableCellTitleText"))); // "标题"));
		item.add(new ShowStringCell(1, currentBoot.getTitle()));
		tableModel.addRow(item);

		// 菜单菜单
		if (currentBoot.isStart()) {
			String attachMenu = boot.getAttachMenu();
			if (attachMenu != null) {
				item = new ShowItem();
				item.add(new ShowStringCell(0, UIManager.getString("InstallDialog.tableCellAttachMenuText")));
				item.add(new ShowStringCell(1, attachMenu));
				tableModel.addRow(item);
			}
		}

		item = new ShowItem();
		item.add(new ShowStringCell(0, UIManager.getString("InstallDialog.tableCellIconText"))); // "图标"));
		item.add(new ShowImageCell(1, n.getIcon()));
		tableModel.addRow(item);

		//		item = new ShowItem();
		//		currentBoot.get

		// 判断是应用，显示部署到桌面的提示
		if (Laxkit.isClassFrom(currentBoot, WProgram.class)) {
			item = new ShowItem();
			item.add(new ShowStringCell(0, UIManager.getString("InstallDialog.tableCellDeployDesktopText"))); // "部署到桌面"));
			ShowBooleanCell cell = new ShowBooleanCell(1, n.isDesktop());
			cell.setSymbol(DESKTOP_DEPLOYICON); // 隐性标记值："deploy desktop"; 
			cell.setEditable(true); //
			item.add(cell);
			tableModel.addRow(item);
		}

		// 判断是应用，显示部署到应用坞的提示
		if (Laxkit.isClassFrom(currentBoot, WProgram.class)) {
			item = new ShowItem();
			item.add(new ShowStringCell(0, UIManager.getString("InstallDialog.tableCellDeployDockText"))); // 部署到应用坞
			ShowBooleanCell cell = new ShowBooleanCell(1, n.isDock());
			cell.setSymbol(DOCK_DEPLOYICON); // 隐性标记值： "deploy dock";
			cell.setEditable(true); //
			item.add(cell);
			tableModel.addRow(item);
		}

		//				table.getColumnModel().getColumn(0).setCellEditor(editor);

		//				 JComboBox comboBox = new JComboBox(); 
		//				 	comboBox.addItem("FUCK"); 
		//				 	comboBox.addItem("SHIT"); 
		//				 	
		//				 	table.setCellEditor(new DefaultCellEditor(comboBox));
		//				 	
		//				 	  TableColumn column = table.getColumn(ShowBooleanCell.class);
		//				 	  column.setCellEditor(new DefaultCellEditor(comboBox)); 
		////				 	  column.setCellRenderer();
		////				 	  
		////				 table.getCellEditor(0, 0);
		////				
		////				table.prepareEditor(new DefaultCellEditor(comboBox), 0, 0);



		//		if(source.getClass() == DesktopInstallListCellRenderer.class) {
		//			DesktopInstallListCellRenderer n  = (DesktopInstallListCellRenderer)source;
		//			
		//		} 

		//		if (source.getClass() == WatchSiteBrowserAddressTreeNode.class) {
		//			WatchSiteBrowserAddressTreeNode tn = (WatchSiteBrowserAddressTreeNode) source;
		//			Node node = tn.getNode();
		//			// 找到对象
		//			SiteRuntime runtime = SiteRuntimeBasket.getInstance().findRuntime(node);
		//			if (runtime != null) {
		//				getParentPanel().getDetailPanel().exchange(runtime);
		//			}
		//		} else {
		//			getParentPanel().getDetailPanel().clear();
		//		}
	}

	//	class ClickThread extends SwingEvent {
	//		ActionEvent event;
	//		ClickThread(ActionEvent e){
	//			super();
	//			event = e;
	//		}
	//		public void process() {
	//			click(event);
	//		}
	//	}

	private void exit() {
		String content = UIManager.getString("InstallDialog.exitMessageText");
		boolean success = MessageBox.showYesNoDialog(this, getTitle(), content);
		if (!success) {
			return;
		}

		// 保存范围
		saveBounds();
		writeDeviderLocation(splitPane.getDividerLocation());

		// 调用上级的关闭窗口!
		super.closeWindow();
	}

	private void reset() {
		lblText.setText("");
		root.removeAllChildren();
		treeModel.reload(root);
		boot = null;

		// 清除表格数据
		tableModel.clear();
	}

	/** 选择的文件类型选项 **/
	private static String selectRead;

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

	/**
	 * 从中找到匹配的选项
	 * @param chooser 文件选择器
	 * @param selectDescription 选中的描述
	 */
	protected void chooseFileFilter(ChoiceDialog chooser, String selectDescription) {
		if (selectDescription == null) {
			return;
		}
		// 选择
		FileMatcher[] elements = chooser.getFileMatchers(); // .getChoosableFileFilters();
		for (int i = 0; elements != null && i < elements.length; i++) {
			//			javax.swing.filechooser.FileFilter e = (javax.swing.filechooser.FileFilter) elements[i];
			//			if (e.getClass() != DiskFileFilter.class) {
			//				continue;
			//			}
			FileMatcher filter = elements[i]; // (DiskFileFilter) e;
			String type = filter.getDescription();
			if (Laxkit.compareTo(selectDescription, type) == 0) {
				chooser.setSelectFileMatcher(filter);
				break;
			}
		}
	}

	/**
	 * 保存读的脚本文件
	 * @param file
	 */
	private void setReadFile(File file) {
		String filename = Laxkit.canonical(file);
		RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "InstallDialog/ChoiceFile", filename);
	}

	//	/**
	//	 * 设置开放目录
	//	 * @param chooser
	//	 */
	//	protected void setReadDirectory(JFileChooser chooser) {
	//		//		Object memory = UITools.getProperity(READ_FILE);
	//		//		if (memory != null && memory.getClass() == String.class) {
	//		//			File file = new File((String) memory);
	//		//			if (file.exists() && file.isFile()) {
	//		//				chooser.setCurrentDirectory(file.getParentFile());
	//		//			}
	//		//		}
	//
	//		String memory = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "InstallDialog/ChoiceFile");
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
	protected void setReadDirectory(ChoiceDialog chooser) {
		//		Object memory = UITools.getProperity(READ_FILE);
		//		if (memory != null && memory.getClass() == String.class) {
		//			File file = new File((String) memory);
		//			if (file.exists() && file.isFile()) {
		//				chooser.setCurrentDirectory(file.getParentFile());
		//			}
		//		}

		String memory = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "InstallDialog/ChoiceFile");
		if (memory != null) {
			File file = new File(memory);
			if (file.exists() && file.isFile()) {
				chooser.setCurrentDirectory(file.getParentFile());
			}
		}
	}

	//	/**
	//	 * 保存选项
	//	 * @param chooser 文件选择器
	//	 */
	//	protected String saveFileFileter(JFileChooser chooser) {
	//		javax.swing.filechooser.FileFilter e = chooser.getFileFilter();
	//		if (e == null || e.getClass() != DiskFileFilter.class) {
	//			return null;
	//		}
	//
	//		DiskFileFilter filter = (DiskFileFilter) e;
	//		return filter.getDescription();
	//	}

	/**
	 * 保存选项
	 * @param chooser 文件选择器
	 */
	private String saveFileFileter(ChoiceDialog chooser) {
		FileMatcher e = chooser.getSelectFileMatcher();
		if (e == null ) {
			return null;
		}

		return e.getDescription();
	}

	//	private void choice() {
	////		InstallDialog.choiceSoftwareTitle 选择应用软件
	////		InstallDialog.choiceSoftwareButtonText 选择
	//		
	//		String title = UIManager.getString("InstallDialog.choiceSoftwareTitle"); // "文本框"; // findCaption("Dialog/ImageTransform/open-chooser/title/title");
	//		String buttonText = UIManager.getString("InstallDialog.choiceSoftwareButtonText"); // "确定"; // findCaption("Dialog/ImageTransform/open-chooser/choose/title");
	//
	////		InstallDialog.descriptionDASText 分布式应用软件包 (*.das)
	////		InstallDialog.extensionDASText das
	//		
	//		String ds_das = UIManager.getString("InstallDialog.descriptionDASText");
	//		String das = UIManager.getString("InstallDialog.extensionDASText");
	//		String ds_sas = UIManager.getString("InstallDialog.descriptionSASText");
	//		String sas = UIManager.getString("InstallDialog.extensionSASText");
	//		String ds_eas = UIManager.getString("InstallDialog.descriptionEASText");
	//		String eas = UIManager.getString("InstallDialog.extensionEASText");
	//		
	////		// JPEG文件
	////		String ds_jpeg = "分布式应用软件, (*.das)"; //findCaption("Dialog/ImageTransform/open-chooser/jpeg/description/title");
	////		String jpeg = "das"; //findCaption("Dialog/ImageTransform/open-chooser/jpeg/extension/title");
	////		// GIF文件
	////		String ds_gif = "边缘应用软件, (*.eas)"; // findCaption("Dialog/ImageTransform/open-chooser/gif/description/title");
	////		String gif = "eas";//findCaption("Dialog/ImageTransform/open-chooser/gif/extension/title");
	////		// PNG文件
	////		String ds_png = "服务应用软件, (*.sas)";// findCaption("Dialog/ImageTransform/open-chooser/png/description/title");
	////		String png = "sas";//findCaption("Dialog/ImageTransform/open-chooser/png/extension/title");
	//
	////		DiskFileFilter f1 = new DiskFileFilter(ds_jpeg, jpeg);
	////		DiskFileFilter f2 = new DiskFileFilter(ds_gif, gif);
	////		DiskFileFilter f3 = new DiskFileFilter(ds_png, png);
	//
	////		chooser.addChoosableFileFilter(f3);
	////		chooser.addChoosableFileFilter(f2);
	////		chooser.addChoosableFileFilter(f1);
	//		
	//		// 显示窗口
	//		JFileChooser chooser = new JFileChooser();
	//		chooser.setAcceptAllFileFilterUsed(false);
	//		chooser.addChoosableFileFilter(new DiskFileFilter(ds_eas, eas));
	//		chooser.addChoosableFileFilter(new DiskFileFilter(ds_sas, sas));
	//		chooser.addChoosableFileFilter(new DiskFileFilter(ds_das, das));
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
	//		setReadDirectory(chooser);
	//
	//		int val = chooser.showOpenDialog(this);
	//		// 显示窗口
	//		if (val != JFileChooser.APPROVE_OPTION) {
	//			return;
	//		}
	//
	//		selectRead = saveFileFileter(chooser);
	//
	//		File file = chooser.getSelectedFile();
	//		boolean success = (file.exists() && file.isFile());
	//		if (success) {
	//			addThread(new ReadThread(file));
	//		}
	//	}

	private void choice() {
		String title = UIManager.getString("InstallDialog.choiceSoftwareTitle"); // "文本框"; // findCaption("Dialog/ImageTransform/open-chooser/title/title");
		String buttonText = UIManager.getString("InstallDialog.choiceSoftwareButtonText"); // "确定"; // findCaption("Dialog/ImageTransform/open-chooser/choose/title");

		String ds_das = UIManager.getString("InstallDialog.descriptionDASText");
		String das = UIManager.getString("InstallDialog.extensionDASText");
		String ds_sas = UIManager.getString("InstallDialog.descriptionSASText");
		String sas = UIManager.getString("InstallDialog.extensionSASText");
		String ds_eas = UIManager.getString("InstallDialog.descriptionEASText");
		String eas = UIManager.getString("InstallDialog.extensionEASText");

		// 显示窗口
		ChoiceDialog dialog = new ChoiceDialog(title);
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.addFileMatcher(new DiskFileMatcher(ds_das, das));
		dialog.addFileMatcher(new DiskFileMatcher(ds_sas, sas));
		dialog.addFileMatcher(new DiskFileMatcher(ds_eas, eas));
		// 找到选项
		chooseFileFilter(dialog, selectRead);

		dialog.setMultiSelectionEnabled(false);
		dialog.setFileSelectionMode(DialogOption.FILES_ONLY); // 只能是文件
		dialog.setDialogType(DialogOption.OPEN_DIALOG);
		dialog.setApproveButtonText(buttonText);
		dialog.setApproveButtonToolTipText(buttonText);

		setReadDirectory(dialog);

		// 显示窗口
		File[] files = dialog.showDialog(this);
		if (files == null) {
			return;
		}

		// 选择
		selectRead = saveFileFileter(dialog);

		File file = files[0];
		boolean success = (file.exists() && file.isFile());
		if (success) {
			addThread(new ReadThread(file));
		}
	}

	class ReadThread extends SwingEvent {
		File file;

		ReadThread(File e) {
			super();
			file = e;
		}

		public void process() {
			try {
				importFile(file);
				setReadFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void click(ActionEvent e) {
		Object source = e.getSource();
		if (source == cmdExit) {
			exit();
		} else if(source == cmdChoice) {
			choice();
		} else if(source == cmdInstall) {
			install();
		} else if(source == cmdReset) {
			reset();
		} else if(source == cbxDesktop) {
			switchDesktop();
		} else if(source == cbxDock) {
			switchDock();
		}
	}

	/**
	 * 切换桌面
	 */
	private void switchDesktop() {
		if (currentBoot == null) {
			return;
		}

		Object obj = cbxDesktop.getSelectedItem();
		// 修改结果
		if (obj != null && Laxkit.isClassFrom(obj, InstallDesktopIcon.class)) {
			InstallDesktopIcon item = (InstallDesktopIcon) obj;

			if (Laxkit.isClassFrom(currentBoot, WProgram.class)) {
				((WProgram) currentBoot).setDesktop(item.isDeploy());
			}

			//			currentBoot.setDesktop(item.isDeploy());
		}
	}

	/**
	 * 切换桌面
	 */
	private void switchDock() {
		if (currentBoot == null) {
			return;
		}

		Object obj = cbxDock.getSelectedItem();
		// 修改结果
		if (obj != null && Laxkit.isClassFrom(obj, InstallDockIcon.class)) {
			InstallDockIcon item = (InstallDockIcon) obj;

			if (Laxkit.isClassFrom(currentBoot, WProgram.class)) {
				((WProgram) currentBoot).setDock(item.isDeploy());
			}

			//			currentBoot.setDock(item.isDeploy());
		}
	}
	
//	class InstallThread extends WorkThread {
//
//		public InstallThread() {
//			super();
//		}
//
//		/* (non-Javadoc)
//		 * @see com.laxcus.thread.WorkThread#process()
//		 */
//		@Override
//		public void process() {
//			doInstall();
//			// 停止!
//			setInterrupted(true);
//		}
//	}
	
//	/**
//	 * 安装应用
//	 */
//	private void install() {
//		InstallThread thread = new InstallThread();
//		thread.start();
//	}
	
	class InstallThread extends SwingEvent {
		
		InstallThread(){
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.event.SwingEvent#process()
		 */
		@Override
		public void process() {
			doInstall();
		}
	}
	
	/**
	 * 安装应用
	 */
	private void install() {
		//		InstallThread thread = new InstallThread();
		//		thread.start();

		addThread(new InstallThread());
	}

	/**
	 * 安装软件包
	 */
	private void doInstall() {
		if (boot == null) {
			return;
		}

		//		InstallDialog.installApplicationTitle 安装应用软件
		//		InstallDialog.installApplicationExists 这个应用软件已经存在
		//		InstallDialog.installApplicationSuccessful 应用软件安装成功！ 
		//		InstallDialog.installApplicationFailed 应用软件安装失败！

		String title = UIManager.getString("InstallDialog.installApplicationTitle");
		// 判断应用存在
		boolean exists = factory.hasApplication(boot);
		if (exists) {
			String content = UIManager.getString("InstallDialog.installApplicationExists");
			content = String.format(content, boot.getElement().getTitle());
			MessageBox.showWarning(this, title, content);
			return;
		}

		// 把应用移到指定的目录下面
		File file = move(boot.getPath());
		if (file == null) {
			String content = UIManager.getString("InstallDialog.moveApplicationFailed");
			MessageBox.showFault(this, title, content);
			return;
		}
		// 生成副本，保存新的目录
		WRoot duplicate = boot.duplicate();
		duplicate.setPath(file);

		// 安装应用
		boolean success = factory.setup(duplicate);
		if (success) {
			String content = UIManager.getString("InstallDialog.installApplicationSuccessful");
			MessageBox.showInformation(this, title, content);
		} else {
			String content = UIManager.getString("InstallDialog.installApplicationFailed");
			MessageBox.showFault(this, title, content);
		}
	}

	/**
	 * 移动应用软件包
	 * @param file
	 * @return
	 */
	private File move(File file) {
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return null;
		}

		// 生成数字签名
		SHA256Hash hash = null;
		try {
			hash = Laxkit.doSHA256Hash(file);
		} catch (Exception e) {
			Logger.error(e);
		}
		if (hash == null) {
			return null;
		}

		String filename = Laxkit.canonical(file);
		int last = filename.lastIndexOf('.');
		if (last == -1) {
			return null;
		}

		String suffix = filename.substring(last + 1);
		String name = String.format("%s.%s", hash.toString(), suffix);
		
		// 生成目录
		File root = PlatformKit.getUserStoreRoot();
		success = (root.exists() && root.isDirectory());
		if (!success) {
			success = root.mkdirs();
		}
		if (!success) {
			return null;
		}

		// 移到应用目录
		File temp = new File(root, name);
		byte[] src = new byte[10240];
		try {
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(temp);
			do {
				int len = in.read(src);
				if (len < 1) {
					break;
				}
				out.write(src, 0, len);
			} while (true);
			out.close();
			in.close();
			return temp;
		} catch (Exception e) {
			Logger.error(e);
		}
		return null;
	}

	//	private void install() {
	//		if (boot == null) {
	//			return;
	//		}
	//		
	//		String associate = boot.getAttachMenu();
	//		
	//		// 1. 生成菜单
	//		JMenuItem item = null;
	//		try {
	//			BootMenuCreator creator = new BootMenuCreator();
	//			item = creator.create(boot.getFile());
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		if (item == null) {
	//			Logger.warning(this, "install", "cannot be create boot-menu!");
	//			MessageBox.showFaultMessageBox(this, "错误", "不能生成菜单！");
	//			return;
	//		}
	//		
	//		// 找到关联的菜单
	//		LaunchMenuCreator creator = new LaunchMenuCreator();
	//		JMenu menu = creator.findMenu(launchMenu, associate);
	//		if (menu == null) {
	//			Logger.warning(this, "install", "cannot be find %s", associate);
	//			MessageBox.showFaultMessageBox(this, "错误", "没有找到关联菜单");
	//			return;
	//		}
	//		
	//		// 在内存里保存映射关系
	//		RTManager.getInstance().set(boot);
	//		// 加入到菜单队列中
	//		menu.add(item);
	//		
	//		// 更新字体和边框
	//		launchMenu.updateFontAndBorder(false);
	//		// 更新监听事件
	//		launchMenu.updateActionListener();
	//		
	//		// 判断安装到桌面
	//		if(boot.isDesktop()) {
	//			
	//		}
	//	}

	//	private void addChild(BasketBuffer buffer, InstallTreeNode parent, BootItem boot) throws IOException {
	////		Icon icon = boot.getIcon(buffer, 16, 16);
	//		Icon icon = boot.getIcon(buffer, 32, 32);
	//		InstallTreeNode child = new InstallTreeNode(boot, icon);
	//		
	//		// 插入后更新
	//		treeModel.insertNodeInto(child, parent, parent.getChildCount());
	//		treeModel.nodeChanged(parent);
	//		
	//		if (boot.hasSubObject()) {
	//			for (BootItem sub : boot.list()) {
	//				addChild(buffer, child, sub);
	//			}
	//		}
	//	}
	//	
	//	private BootItem boot = null;
	//
	//	private void importFile(File file) throws IOException {
	//		// 清除树形目录
	//		reset();
	//		
	//		// 在界面显示
	//		String filename = Laxkit.canonical(file);
	////		lblText.setText(filename);
	//		FontKit.setLabelText(lblText, filename);
	//
	//		BasketBuffer buffer = new BasketBuffer();
	//		// 加载软件包
	//		buffer.load(file);
	//		byte[] b = buffer.readBootstrap();
	//		BootSplitter bs = new BootSplitter();
	//		try {
	//			boot = null;
	//			boot = bs.split(b);
	//			boot.setFile(file);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		
	//		if (boot == null) {
	//			String title = UIManager.getString("InstallDialog.importSoftwareErrorTitle");
	//			String content = UIManager.getString("InstallDialog.importSoftwareErrorContent");
	//			content = String.format(content, filename);
	//			
	//			MessageBox.showFault(this, title, content);
	//			return;
	//		}
	//		
	//		
	////		ImageIcon icon = boot.getIcon(buffer, 16, 16);
	//		ImageIcon icon = boot.getIcon(buffer, 32, 32);
	//		InstallTreeNode parent = new InstallTreeNode(boot, icon);
	//
	//		int index = root.getChildCount();
	////		parent = new WatchSiteBrowserFamilyTreeNode(node.getFamily());
	//		treeModel.insertNodeInto(parent, root, index);
	//
	//		if (index == 0) {
	//			treeModel.reload(root);
	//		} else {
	//			treeModel.nodeChanged(parent);
	//		}
	//		
	//		if (boot.hasSubObject()) {
	//			for (BootItem sub : boot.list()) {
	//				addChild(buffer, parent, sub);
	//			}
	//		}
	//
	////		final String associate = boot.getAttachMenu(); // "TOOL"; // "SERVICE";// "SkinMenu";
	//		
	////		// 1. 生成菜单
	////		JMenuItem item = null;
	////		try {
	////			BootMenuCreator creator = new BootMenuCreator();
	////			item = creator.create(file);
	////		} catch (IOException e) {
	////			Logger.error(e);
	////		} catch (Throwable e) {
	////			Logger.fatal(e);
	////		}
	////		if (item == null) {
	////			Logger.warning(this, "loadApplication", "cannot be create boot-menu!");
	////			return;
	////		}
	////		
	////		BootItem boot = null;
	////		if (Laxkit.isClassFrom(item, BootMenu.class)) {
	////			boot = ((BootMenu) item).getBootItem();
	////		} else if (Laxkit.isClassFrom(item, BootMenuItem.class)) {
	////			boot = ((BootMenuItem) item).getBootItem();
	////		} else {
	////			return;
	////		}
	//		
	////		System.out.printf("这个应用的标题是：%s\n", boot.getTitle());
	//		
	////		// 关联的菜单
	////		final String associate = boot.getAttachMenu(); // "TOOL"; // "SERVICE";// "SkinMenu";
	//////		System.out.printf("attch menu %s\n", associate);
	////		
	////		// 找到关联的菜单
	////		LaunchMenuCreator creator = new LaunchMenuCreator();
	////		JMenu menu = creator.findMenu(launchMenu, associate);
	////		if (menu == null) {
	////			Logger.warning(this, "loadApplication", "cannot be find %s", associate);
	////			return;
	////		}
	//	}


	private void addChild(InstallTreeNode parent, WElement boot) throws IOException {
		//		Icon icon = boot.getIcon(buffer, 16, 16);
		//		Icon icon = boot.getIcon(buffer, 32, 32);

		ImageIcon icon = ImageUtil.scale(boot.getIcon().getImage() , 32, 32, true);

		InstallTreeNode child = new InstallTreeNode(boot, icon);

		// 插入后更新
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
		treeModel.nodeChanged(parent);

		if( Laxkit.isClassFrom(boot, WDirectory.class) ) {
			WDirectory dir = (WDirectory)boot;
			for(WElement sub : dir.getTokens()) {
				addChild(parent, sub);
			}
		}

		//		if (boot.hasSubObject()) {
		//			for (BootItem sub : boot.list()) {
		//				addChild(buffer, child, sub);
		//			}
		//		}
	}

	private void importFile(File file) throws IOException {
		// 清除树形目录
		reset();

		// 在界面显示
		String filename = Laxkit.canonical(file);
		//		lblText.setText(filename);
		FontKit.setLabelText(lblText, filename);

		// 解析数据
		boot = null;
		try {
			boot = WTokenChanger.split(file);
		} catch (IOException e) {
			Logger.error(e);
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		}

		//		BasketBuffer buffer = new BasketBuffer();
		//		// 加载软件包
		//		buffer.load(file);
		//		byte[] b = buffer.readBootstrap();
		//		BootSplitter bs = new BootSplitter();
		//		try {
		//			boot = null;
		//			boot = bs.split(b);
		//			boot.setFile(file);
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//		}

		if (boot == null) {
			String title = UIManager.getString("InstallDialog.importSoftwareErrorTitle");
			String content = UIManager.getString("InstallDialog.importSoftwareErrorContent");
			content = String.format(content, filename);

			MessageBox.showFault(this, title, content);
			return;
		}

		WElement element = boot.getElement(); 

		//		ImageIcon icon = boot.getIcon(buffer, 16, 16);
		ImageIcon icon = ImageUtil.scale(element.getIcon().getImage(), 32, 32, true); // boot.getIcon(buffer, 32, 32);

		InstallTreeNode parent = new InstallTreeNode(element, icon);

		int index = root.getChildCount();
		//		parent = new WatchSiteBrowserFamilyTreeNode(node.getFamily());
		treeModel.insertNodeInto(parent, root, index);

		if (index == 0) {
			treeModel.reload(root);
		} else {
			treeModel.nodeChanged(parent);
		}

		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				addChild(parent, sub);
			}
		}

		//		if (boot.hasSubObject()) {
		//			for (BootItem sub : boot.list()) {
		//				addChild(buffer, parent, sub);
		//			}
		//		}

		//		final String associate = boot.getAttachMenu(); // "TOOL"; // "SERVICE";// "SkinMenu";

		//		// 1. 生成菜单
		//		JMenuItem item = null;
		//		try {
		//			BootMenuCreator creator = new BootMenuCreator();
		//			item = creator.create(file);
		//		} catch (IOException e) {
		//			Logger.error(e);
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//		}
		//		if (item == null) {
		//			Logger.warning(this, "loadApplication", "cannot be create boot-menu!");
		//			return;
		//		}
		//		
		//		BootItem boot = null;
		//		if (Laxkit.isClassFrom(item, BootMenu.class)) {
		//			boot = ((BootMenu) item).getBootItem();
		//		} else if (Laxkit.isClassFrom(item, BootMenuItem.class)) {
		//			boot = ((BootMenuItem) item).getBootItem();
		//		} else {
		//			return;
		//		}

		//		System.out.printf("这个应用的标题是：%s\n", boot.getTitle());

		//		// 关联的菜单
		//		final String associate = boot.getAttachMenu(); // "TOOL"; // "SERVICE";// "SkinMenu";
		////		System.out.printf("attch menu %s\n", associate);
		//		
		//		// 找到关联的菜单
		//		LaunchMenuCreator creator = new LaunchMenuCreator();
		//		JMenu menu = creator.findMenu(launchMenu, associate);
		//		if (menu == null) {
		//			Logger.warning(this, "loadApplication", "cannot be find %s", associate);
		//			return;
		//		}
	}

	private JScrollPane createTree() {
		// 允许保存子节点
		root.setAllowsChildren(true);
		// 建立树模型
		treeModel = new DefaultTreeModel(root);
		// 建立树型结构
		tree = new InstallTree(treeModel); // new JTree(treeModel);

		// 渲染器
		treeRenderer = new InstallTreeCellRenderer();
		tree.setCellRenderer(treeRenderer);

		//		// 获得标题
		//		String title = WatchLauncher.getInstance().findCaption("Window/SiteBrowser/title");
		//		FontKit.setToolTipText(tree, title);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setBorder(new EmptyBorder(5, 3, 5, 3));
		tree.setRowHeight(-1);
		tree.setToggleClickCount(1);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		tree.setEditable(false);

		JScrollPane jsp = new JScrollPane(tree);
//		jsp.setBorder(new HighlightBorder(1));
		
//		scroll.setBorder(new EmptyBorder(2, 2, 2, 2));

		//		scroll.getViewport().setBorder(new EmptyBorder(2,2,2,2));
		//		FontKit.setToolTipText(scroll, title);

		return jsp;
	}

	private int readDeviderLocation() {
		return RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, "InstallDialog/Devider", 158);
	}

	private boolean writeDeviderLocation(int value) {
		return RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM,
				"InstallDialog/Devider", value);
	}

	/**
	 * 初始化表头
	 */
	private void initTableHeader() {
		// 键/值的宽度
		//		String value = "80";// WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/key-width");
		int key_width = ConfigParser.splitInteger(UIManager.getString("InstallDialog.tableTitleKeyWidth"), 80);
		//		value = "240" ; // WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/value-width");
		int value_width = ConfigParser.splitInteger(UIManager.getString("InstallDialog.tableTitleValueWidth"), 130);

		String keyText = UIManager.getString("InstallDialog.tableTitleKeyText");
		String valueText = UIManager.getString("InstallDialog.tableTitleValueText");

		//		String keyText = "名称";// WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/Header/Key/title");
		//		String valueText = "属性";//WatchLauncher.getInstance().findCaption("Window/SiteBrowserDetailPanel/Header/Value/title");

		// 标题！
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, keyText, key_width));
		title.add(new ShowTitleCell(1, valueText, value_width));

		int count = title.size();
		for (int i = 0; i < count; i++) {
			ShowTitleCell cell = title.get(i);
			TableColumn column = new TableColumn(cell.getIndex(), cell.getWidth());
			column.setHeaderValue(cell.getName());
			// 保存
			table.addColumn(column);
		}

		// 保存标题
		tableModel.setTitle(title);
	}

	private void copyToClipboard() {
		// 选中的行
		int[] rows = table.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			rows[i] = table.convertRowIndexToModel(rows[i]);
		}
		// 选中的列
		int[] columns = table.getSelectedColumns();
		// 保存的缓存
		StringBuilder buff = new StringBuilder();
		for (int row : rows) {
			StringBuilder bf = new StringBuilder();
			for (int column : columns) {
				if (bf.length() > 0) bf.append(" ");

				TableColumn element = table.getTableHeader().getColumnModel().getColumn(column);
				String name = (String) element.getHeaderValue();

				// 列定位到实际位置，没有返回-1
				column = tableModel.findColumn(name);
				if(column < 0) continue;

				ShowItemCell cell = tableModel.getCellAt(row, column);
				if (Laxkit.isClassFrom(cell, ShowImageCell.class)) {
					Object node = ((ShowImageCell) cell).getSymbol();
					if (node != null) bf.append(node.toString());
				} else {
					Object str = cell.visible();
					if (Laxkit.isClassFrom(str, String.class)) {
						bf.append((String) str);
					}
				}
			}
			if (buff.length() > 0) buff.append("\r\n");
			buff.append(bf.toString());
		}

		// 复制到系统剪贴板
		Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transfer = new StringSelection(buff.toString());
		board.setContents(transfer, null);
	}

	/**
	 * 复制参数到系统剪贴板
	 */
	class ClipboardCopyThread extends SwingEvent {

		ClipboardCopyThread() {
			super();
		}

		public void process() {
			copyToClipboard();
		}
	}

	private boolean isDesktopDeployIcon(Object object) {
		boolean success = (object != null && Laxkit.isClassFrom(object, ShowBooleanCell.class));
		if (success) {
			ShowBooleanCell cell = (ShowBooleanCell) object;
			Object symbol = cell.getSymbol();
			success = (symbol != null && symbol.getClass() == String.class);
			if (success) {
				String s = (String) symbol;
				success = DESKTOP_DEPLOYICON.equals(s);
			}
		}
		return success;
	}

	private boolean isDockDeployIcon(Object object) {
		boolean success = (object != null && Laxkit.isClassFrom(object, ShowBooleanCell.class));
		if (success) {
			ShowBooleanCell cell = (ShowBooleanCell) object;
			Object symbol = cell.getSymbol();
			success = (symbol != null && symbol.getClass() == String.class);
			if (success) {
				String s = (String) symbol;
				success = DOCK_DEPLOYICON.equals(s); 
			}
		}
		return success;
	}

	private JComboBox createDeployDesktopIconComboBox() {
		JComboBox box = new JComboBox();
		box.setRenderer(comboxDesktopRenderer = new InstallComboBoxDeployDesktopCellRenderer());
		// NO
		String value = UIManager.getString("InstallDialog.noDeployDesktopIconText");
		if (value == null) {
			value = "No";
		}
		box.addItem(new InstallDesktopIcon(value, false));
		// YES
		value = UIManager.getString("InstallDialog.yesDeployDesktopIconText");
		if (value == null) {
			value = "Yes";
		}
		box.addItem(new InstallDesktopIcon(value, true));

		// 事件
		box.addActionListener(this);
		return box;
	}

	private JComboBox createDeployDockIconComboBox() {
		JComboBox box = new JComboBox();
		box.setRenderer(comboxDockRenderer = new InstallComboBoxDeployDockCellRenderer());
		// NO
		String value = UIManager.getString("InstallDialog.noDeployDockIconText");
		if (value == null) {
			value = "No";
		}
		box.addItem(new InstallDockIcon(value, false));
		// YES
		value = UIManager.getString("InstallDialog.yesDeployDockIconText");
		if (value == null) {
			value = "Yes";
		}
		box.addItem(new InstallDockIcon(value, true));

		// 事件
		box.addActionListener(this);
		return box;
	}

	/**
	 * 生成表格
	 * @return
	 */
	@SuppressWarnings("serial")
	private JComponent createTable() {
		// 生成桌面编辑器
		cbxDesktop = createDeployDesktopIconComboBox();
		desktopEditor = new DefaultCellEditor(cbxDesktop);

		// 生成应用坞编辑器
		cbxDock = createDeployDockIconComboBox();
		dockEditor = new DefaultCellEditor(cbxDock);

		//		JCheckBox box = new JCheckBox();
		//		box.setSelected(false);
		//		editor = new DefaultCellEditor(box);

		tableDeployDesktopIconRenderer = new InstallTableDeployDesktopIconCellRenderer();
		tableDeployDockIconRenderer = new InstallTableDeployDockIconCellRenderer();

		// 模型
		tableModel = new InstallTableModel();
		// 生成表
		table = new JTable(tableModel) {
			// DesktopInstallTableModel.isCellEditable确认可编辑后，在这里找匹配的编辑器
//			public TableCellEditor getCellEditor(int row, int column) {
//				TableCellEditor editor = null;
//				// 找到这个对象
//				Object object = tableModel.getValueAt(row, column);
//				// 是桌面部署图标...
//				if (isDesktopDeployIcon(object)) {
//					editor = desktopEditor;
//				}
//				// 是应用坞部署图标...
//				else if (isDockDeployIcon(object)) {
//					editor = dockEditor;
//				} else {
//					editor = super.getCellEditor(row, column);
//				}
//				
//				System.out.printf("%d %d object class: %s, editor class: %s\n", 
//						row, column, object.getClass().getName(), editor.getClass().getName());
//				return editor;
//			}
			
			public TableCellEditor getCellEditor(int row, int column) {
				// 找到这个对象
				Object object = tableModel.getValueAt(row, column);
				// 是桌面部署图标...
				if (isDesktopDeployIcon(object)) {
					return desktopEditor;
				}
				// 是应用坞部署图标...
				if (isDockDeployIcon(object)) {
					return dockEditor;
				}
				return super.getCellEditor(row, column);
			}

//			public TableCellRenderer getCellRenderer(int row, int column){
//				TableCellRenderer renderer=null;
//				// 找到这个对象
//				Object object = tableModel.getValueAt(row, column);
//				// 是桌面部署图标...
//				if (isDesktopDeployIcon(object)) {
//					renderer = tableDeployDesktopIconRenderer;
//				}
//				// 是应用坞部署图标
//				else if (isDockDeployIcon(object)) {
//					renderer = tableDeployDockIconRenderer;
//				} else {
//					renderer = super.getCellRenderer(row, column);
//				}
//				System.out.printf("%d %d object class: %s, renderer class: %s\n", 
//						row, column, object.getClass().getName(), renderer.getClass().getName());
//				return renderer;
//			}
			
			public TableCellRenderer getCellRenderer(int row, int column) {
				// 找到这个对象
				Object object = tableModel.getValueAt(row, column);
				// 是桌面部署图标...
				if (isDesktopDeployIcon(object)) {
					return tableDeployDesktopIconRenderer;
				}
				// 是应用坞部署图标
				if (isDockDeployIcon(object)) {
					return tableDeployDockIconRenderer;
				}
				return super.getCellRenderer(row, column);
			}
		};

		//		// 设置默认的字体
		//		FontKit.setDefaultFont(table);

		// 渲染器只支持ShowItemCell类
		tableRenderer = new InstallTableCellRenderer();
		table.setDefaultRenderer(ShowItemCell.class, tableRenderer);

		// 初始化表头
		initTableHeader();

		int rowHeight = ConfigParser.splitInteger(UIManager.getString("InstallDialog.tableRowHeight"), 38);
		table.setRowHeight(rowHeight); // 行高度
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(false);
		table.getTableHeader().setReorderingAllowed(true);
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setIntercellSpacing(new Dimension(1, 1));
		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);
		table.setBorder(new EmptyBorder(1, 1, 1, 1));


		//		table.editCellAt(0, 0);

		//		table.getColumnModel().getColumn(0).setCellEditor(editor);
		//		DefaultTableCellRenderer rf = new DefaultTableCellRenderer();
		//		rf.setToolTipText("Text Kit");
		//		table.getColumnModel().getColumn(0).setCellRenderer(rf);

		//		JComboBox comboBox = new JComboBox();
		//		comboBox.addItem("FUCK");
		//		comboBox.addItem("SHIT");
		//
		//		 	table.setCellEditor(new DefaultCellEditor(comboBox));


		//		table.addAncestorListener(arg0)
		//		
		//		table.setsh

		//		JComboBox box = new JComboBox();
		//		box.add


		//		table.getcol
		//		
		//		 TableColumn colorColumn = tableView.getColumn(getString("TableDemo.favorite_color")); 
		//         // Use the combo box as the editor in the "Favorite Color" column. 
		//         colorColumn.setCellEditor(new DefaultCellEditor(comboBox)); 



		//		if (Skins.isNimbus()) {
		//			table.setIntercellSpacing(new Dimension(2, 2)); // NIMBUS界面不要空格
		//		} else {
		//			table.setIntercellSpacing(new Dimension(2, 2));
		//		}

		// 定位复制键
		table.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, true), "CTRL C");
		table.getActionMap().put("CTRL C", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// 放入SWING队列
				ClipboardCopyThread thread = new ClipboardCopyThread();
				addThread(thread);
			}
		});

		//		// 表格单元高度与高度，宽度与宽度之间的间距
		//		UITools.setTableIntercellSpacing(table);

		JScrollPane jsp = new JScrollPane(table);
		// 这是一个JAVA BUG的修改方法，修正表头高度，取代JTable.getTableHeader().setPreferredSize(new Dimension(width, height));否则拖动表头会出现显示异常
		int columnHeaderHeight = ConfigParser.splitInteger(UIManager.getString("InstallDialog.tableColumnHeaderHeight"), 25);
		jsp.setColumnHeader(new HeightViewport(columnHeaderHeight));
//		jsp.setBorder(new HighlightBorder(1));

		return jsp;
	}

	private JSplitPane createHi() {
		JPanel sub = new JPanel();
		sub.setBorder(new EmptyBorder(0, 0, 0, 0));
		sub.setLayout(new BorderLayout(0,0));
		sub.add(createTable(), BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTree(), sub);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerSize(4); // 4像素
		
		splitPane.setDividerLocation(readDeviderLocation());
		splitPane.putClientProperty("NotBorder", Boolean.TRUE);
		splitPane.putClientProperty("FlatDivider", Boolean.TRUE);

		return splitPane;
	}

	private FlatButton createButton(String text, char w) {
		FlatButton but = new FlatButton();
		FontKit.setButtonText(but, text);

		but.setIconTextGap(4);
		but.addActionListener(this);
		but.setMnemonic(w);

		return but;
	}

	private JPanel createCenter() {
		cmdChoice = createButton(UIManager.getString("InstallDialog.choiceButtonText"), 'S');
		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(4, 0));
		north.add(cmdChoice, BorderLayout.WEST);
		north.add(lblText, BorderLayout.CENTER);
		north.setBorder(new EmptyBorder(0,0,0,0));

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(0, 4));
		center.add(north, BorderLayout.NORTH);
		//		center.add(createTree(), BorderLayout.CENTER);
		center.add(createHi(), BorderLayout.CENTER);
		return center;
	}

	//	private JPanel createSouth() {
	//		cmdInstall = createButton(UIManager.getString("InstallDialog.installButtonText"), 'I');
	//		cmdReset = createButton(UIManager.getString("InstallDialog.resetButtonText"), 'R');
	//		JPanel p = new JPanel();
	//		p.setLayout(new GridLayout(1, 2, 5, 0));
	//		p.add(cmdInstall);
	//		p.add(cmdReset);
	//		
	//		cmdExit = createButton(UIManager.getString("InstallDialog.exitButtonText"), 'X');
	//		
	//		JPanel panel = new JPanel();
	//		panel.setLayout(new BorderLayout(0, 4));
	//		panel.add(new JSeparator(), BorderLayout.NORTH);
	//		panel.add(p, BorderLayout.WEST);
	//		panel.add(cmdExit, BorderLayout.EAST);
	//		return panel;
	//	}

	private JPanel createSouth() {
		cmdInstall = createButton(UIManager.getString("InstallDialog.installButtonText"), 'I');
		cmdReset = createButton(UIManager.getString("InstallDialog.resetButtonText"), 'R');
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(1, 2, 5, 0));
		left.add(cmdInstall);
		left.add(cmdReset);

		cmdExit = createButton(UIManager.getString("InstallDialog.exitButtonText"), 'X');

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 4));
		panel.add(left, BorderLayout.WEST);
		panel.add(cmdExit, BorderLayout.EAST);
		return panel;
	}

	private void initDialog() {
		setTitle(UIManager.getString("InstallDialog.Title"));
		setFrameIcon(UIManager.getIcon("InstallDialog.TitleIcon"));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(createCenter(), BorderLayout.CENTER);
		panel.add(createSouth(), BorderLayout.SOUTH);
		panel.setBorder(new EmptyBorder(4, 6, 6, 6));

		//		// 设置默认的字体
		//		FontKit.updateDefaultFonts(panel);

		setContentPane(panel);
	}

	private void saveBounds() {
		Rectangle rect = super.getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "InstallDialog/Bound", rect);
	}

	private Rectangle readBounds() {
		Rectangle bounds = RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM,"InstallDialog/Bound");
		if (bounds == null) {
			int w = 500;
			int h = 488;

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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		if (desktopEditor != null) {
			Component c = desktopEditor.getComponent();
			if (c != null && Laxkit.isClassFrom(c, JComboBox.class)) {
				JComboBox box = (JComboBox) c;
				box.updateUI();
			}
		}
		if (dockEditor != null) {
			Component c = dockEditor.getComponent();
			if (c != null && Laxkit.isClassFrom(c, JComboBox.class)) {
				JComboBox box = (JComboBox) c;
				box.updateUI();
			}
		}

		if (treeRenderer != null) {
			treeRenderer.updateUI();
		}
		if (tableRenderer != null) {
			tableRenderer.updateUI();
		}
		// 桌面
//		InstallComboBoxDeployDesktopCellRenderer comboxDesktopRenderer;
		if (comboxDesktopRenderer != null) {
			comboxDesktopRenderer.updateUI();
		}
//		InstallTableDeployDesktopIconCellRenderer tableDeployDesktopIconRenderer;
		if (tableDeployDesktopIconRenderer != null) {
			tableDeployDesktopIconRenderer.updateUI();
		}
		// 应用坞
//		InstallComboBoxDeployDockCellRenderer comboxDockRenderer;
		if (comboxDockRenderer != null) {
			comboxDockRenderer.updateUI();
		}
//		InstallTableDeployDockIconCellRenderer tableDeployDockIconRenderer;
		if (tableDeployDockIconRenderer != null) {
			tableDeployDockIconRenderer.updateUI();
		}
		// 注意，不要更新UI，否则在updateUI里会死循环!
		FontKit.updateDefaultFonts(this, false);
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