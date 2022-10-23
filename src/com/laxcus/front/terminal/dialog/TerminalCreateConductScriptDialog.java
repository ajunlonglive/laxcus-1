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
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.skin.*;

/**
 * 生成CONDUCT脚本文件
 * 
 * @author scott.liang
 * @version 1.0 8/13/2020
 * @since laxcus 1.0
 */
public class TerminalCreateConductScriptDialog extends TerminalCreateWareScriptDialog implements ActionListener {

	private static final long serialVersionUID = -2001226139409618699L;

	protected final static String BOUND = TerminalCreateConductScriptDialog.class.getSimpleName() + "_BOUND";
	
	/** 窗口任务实体 **/
	
	private TaskBody init = new TaskBody(PhaseTag.INIT);

	private TaskBody balance = new TaskBody(PhaseTag.BALANCE);

	private TaskBody from = new TaskBody(PhaseTag.FROM);
	
	private TaskBody to = new TaskBody(PhaseTag.TO);

	private TaskBody put = new TaskBody(PhaseTag.PUT);

	/**
	 * 全部任务
	 * @return
	 */
	private TaskBody[] getTasks() {
		return new TaskBody[] { init, from, balance, to, put };
	}

	/**
	 * @param owner
	 * @param modal
	 */
	public TerminalCreateConductScriptDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e) );
	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;

		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			click(event);
		}
	}

	class ReadScript extends SwingEvent {
		File file;

		ReadScript(File e) {
			super();
			file = e;
		}

		public void process() {
			readScript(file, getTasks());
		}
	}
	
	class WriteScript extends SwingEvent {
		File file;

		WriteScript(File e) {
			super();
			file = e;
		}

		public void process() {
			writeScript(file, getTasks());
		}
	}

	/**
	 * 鼠标点击
	 * @param e
	 */
	private void click(ActionEvent e) {
		if (e.getSource() == cmdExit) {
			boolean success = exit();
			if (success) {
				saveBound();
				dispose();
			}
		} 
		// 导入/导出/重置
		else if(e.getSource() == cmdImport) {
			readScript();
		} else if(e.getSource() == cmdExport) {
			writeScript();
		} else if(e.getSource() == cmdReset) {
			reset(getTasks());
		}
		// 自读
		else if (e.getSource() == readme.cmdLogo) {
			setLogo();
		} else if (e.getSource() == readme.cmdLicence) {
			setLicence();
		}
		// 启动
		else if (e.getSource() == guide.cmdSelect) {
			setGTC();
		} else if (e.getSource() == guide.jarAdd) {
			addJar(guide.jarList, guide.jarModel, guide.jarArray);
		} else if (e.getSource() == guide.jarRemove) {
			removeJar(guide.jarList, guide.jarModel, guide.jarArray);
		} else if (e.getSource() == guide.libAdd) {
			addLib(guide.libList, guide.libModel, guide.libArray);
		} else if (e.getSource() == guide.libRemove) {
			removeLib(guide.libList, guide.libModel, guide.libArray);
		}
		// 其它
		else {
			TaskBody[] tasks = getTasks();
			for (int i = 0; i < tasks.length; i++) {
				TaskBody task = tasks[i];
				if (e.getSource() == task.cmdSelect) {
					setDTC(task);
				} else if (e.getSource() == task.jarAdd) {
					addJar(task.jarList, task.jarModel, task.jarArray);
				} else if (e.getSource() == task.jarRemove) {
					removeJar(task.jarList, task.jarModel, task.jarArray);
				} else if (e.getSource() == task.libAdd) {
					addLib(task.libList, task.libModel, task.libArray);
				} else if (e.getSource() == task.libRemove) {
					removeLib(task.libList, task.libModel, task.libArray);
				}
			}
		}
	}

	/**
	 * 退出运行
	 */
	private boolean exit() {
		String title = getTitle();
		String content = findCaption("Dialog/CreateWareScript/exit/message/title");
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, null, content, JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 保存范围
	 */
	private void saveBound() {
		Rectangle e = super.getBounds();
		if (e != null) {
			UITools.putProperity(TerminalCreateConductScriptDialog.BOUND, e);
		}
	}
	
	/**
	 * 打开对话框，读取脚本文件
	 */
	private void readScript() {
		String title = findCaption("Dialog/CreateWareScript/open-chooser/title/title");
		String buttonText = findCaption("Dialog/CreateWareScript/open-chooser/choose/title");

		// 脚本文件
		String ds_script = findCaption("Dialog/CreateWareScript/save-chooser/script/description/title");
		String script = findCaption("Dialog/CreateWareScript/save-chooser/script/extension/title");

		DiskFileFilter f1 = new DiskFileFilter(ds_script, script);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f1);

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);
		
//		System.out.printf("read file:%s \n", readFile);
//
//		// 文件！
//		if (readFile != null) {
//			chooser.setCurrentDirectory(readFile.getParentFile());
//		}
//		// 没有定义，从系统中取
//		else {
//			Object memory = UITools.getProperity(OPEN_KEY);
//			if (memory != null && memory.getClass() == String.class) {
//				File file = new File((String) memory);
//				if (file.exists() && file.isFile()) {
//					chooser.setCurrentDirectory(file.getParentFile());
//				}
//			}
//		}
		
		// 设置目录
		setImportDirectory(chooser);

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isFile());
		if (success) {
			addThread(new ReadScript(file));
		}
	}
	
	private void writeScript() {
		boolean success = checkReadme();
		if(success) {
			success = checkGuide();
		}
		if(success) {
			success = checkTasks(getTasks());
		}
		// 以上不成功，退出！
		if(!success) {
			showMissing();
			return;
		}
		
		String title = findCaption("Dialog/CreateWareScript/save-chooser/title/title");
		String buttonText = findCaption("Dialog/CreateWareScript/save-chooser/save/title");

		// XML文件
		String ds_script = findCaption("Dialog/CreateWareScript/save-chooser/script/description/title");
		String script = findCaption("Dialog/CreateWareScript/save-chooser/script/extension/title");
		DiskFileFilter f1 = new DiskFileFilter(ds_script, script);
		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f1);
		
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);

		//		// 文件！
		//		if (saveFile != null) {
		//			chooser.setCurrentDirectory(saveFile.getParentFile());
		//		}
		//		// 没有定义，从系统中取
		//		else {
		//			Object memory = UITools.getProperity(SAVE_KEY);
		//			if (memory != null && memory.getClass() == String.class) {
		//				File file = new File((String) memory);
		//				if (file.exists() && file.isFile()) {
		//					chooser.setCurrentDirectory(file.getParentFile());
		//				}
		//			}
		//		}

		// 设置写入文件
		setExportDirectory(chooser);

		int val = chooser.showSaveDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		DiskFileFilter filter = (DiskFileFilter) chooser.getFileFilter();
		File file = chooser.getSelectedFile();
		// 判断符合名称要求
		if (!filter.accept(file)) {
			String filename = Laxkit.canonical(file);
			String[] exts = filter.getExtensions();
			filename = String.format("%s.%s", filename, exts[0]);
			file = new File(filename);
		}
		
		// 判断文件存在，是否覆盖
		if (file.exists() && file.isFile()) {
			success = override(file);
			if (!success) return;
		}
		
		// 写入磁盘
		addThread(new WriteScript(file));
	}
	

	private void initControls() {
		initReadmeButtons();
		initGuideButtons();
		
		TaskBody[] tasks = getTasks();
		for (TaskBody task : tasks) {
			initTaskButtons(task);
		}

		initBottomButtons();
	}
	
	private void initListeners() {
		cmdReset.addActionListener(this);
		cmdImport.addActionListener(this);
		cmdExport.addActionListener(this);
		cmdExit.addActionListener(this);
		
		readme.cmdLogo.addActionListener(this);
		readme.cmdLicence.addActionListener(this);

		// 定义启动事件
		guide.cmdSelect.addActionListener(this);
		guide.jarAdd.addActionListener(this);
		guide.jarRemove.addActionListener(this);
		guide.libAdd.addActionListener(this);
		guide.libRemove.addActionListener(this);

		// 定义任务事件
		TaskBody[] tasks = getTasks();
		for (int i = 0; i < tasks.length; i++) {
			TaskBody task = tasks[i];
			task.cmdSelect.addActionListener(this);
			task.jarAdd.addActionListener(this);
			task.jarRemove.addActionListener(this);
			task.libAdd.addActionListener(this);
			task.libRemove.addActionListener(this);
		}
	}

	/**
	 * 生成中心面板
	 * @return
	 */
	private JPanel createCenterPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
		sub.add(createReadmePanel());
		sub.add(createGuidePanel());
		
		TaskBody[] tasks = getTasks();
		for(TaskBody task : tasks) {
			sub.add(createTaskPanel(task));
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(sub, BorderLayout.NORTH);
		panel.add(new JPanel(), BorderLayout.CENTER);
		return panel;
	}

	private JPanel initPanel() {
		initControls();
		initListeners();

		JScrollPane scroll = new JScrollPane(createCenterPanel());
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		setRootBorder(panel);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(createButtomButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 返回范围
	 * @return Rectangle实例
	 */
	private Rectangle getBound() {
		// 面板范围
		Object obj = UITools.getProperity(TerminalCreateConductScriptDialog.BOUND);
		if (obj != null && obj.getClass() == Rectangle.class) {
			return (Rectangle) obj;
		}

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 570;
		int height = 600;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		// 设置面板
		setContentPane(initPanel());

		Rectangle rect = getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(298, 500));
		setAlwaysOnTop(true);

		// 标题
		String title = findContent("Dialog/CreateWareScript/title/conduct");
		setTitle(title);

		// 检查对话框字体
		checkDialogFonts();

		setVisible(true);
	}

}