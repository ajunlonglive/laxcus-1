/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.display.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.skin.*;

/**
 * 生成应用软件脚本文件
 * 
 * @author scott.liang
 * @version 1.0 8/12/2020
 * @since laxcus 1.0
 */
public class TerminalCreateWareScriptDialog extends TerminalCommonFontDialog {

	private static final long serialVersionUID = 1L;

	/** 保存/打开文件 **/
	private final static String READ_FILE = TerminalCreateWareScriptDialog.class.getSimpleName() + "_OPEN";

	private final static String WRITE_FILE = TerminalCreateWareScriptDialog.class.getSimpleName() + "_SAVE";

	/**
	 * 任务实体
	 *
	 * @author scott.liang
	 * @version 1.0 8/12/2020
	 * @since laxcus 1.0
	 */
	public class TaskBody {

		/** 阶段类型 **/
		private int type;

		// 入口文件
		public JTextField txtEntry = new JTextField();

		// 按纽
		public JButton cmdSelect = new JButton();

		/** dtc后缀软件包 **/
		public File dtc;

		// JAR模型
		public JList jarList = new JList();

		public DefaultListModel jarModel = new DefaultListModel();

		public JButton jarAdd = new JButton();

		public JButton jarRemove = new JButton();

		public ArrayList<FileKey> jarArray = new ArrayList<FileKey>();

		// LIB模型
		public JList libList = new JList();

		public DefaultListModel libModel = new DefaultListModel();

		public JButton libAdd = new JButton();

		public JButton libRemove = new JButton();

		public ArrayList<FileKey> libArray = new ArrayList<FileKey>();

		/**
		 * 生成实例
		 * @param who
		 */
		public TaskBody(int who) {
			super();
			setType(who);
		}

		public void setType(int who) {
			if (!PhaseTag.isPhase(who)) {
				throw new IllegalValueException("illegal type:%d", who);
			}
			type = who;
		}

		public int getType() {
			return type;
		}

		public String getTypeText() {
			return PhaseTag.translate(type);
		}
	}

	/**
	 * 启动实体
	 *
	 * @author scott.liang
	 * @version 1.0 8/12/2020
	 * @since laxcus 1.0
	 */
	public class GuideBody {
		// 入口文件
		public JTextField txtEntry = new JTextField();

		// 按纽
		public JButton cmdSelect = new JButton();

		/** gtc后缀软件包 **/
		public File gtc;

		// JAR模型
		public JList jarList = new JList();

		public DefaultListModel jarModel = new DefaultListModel();

		public JButton jarAdd = new JButton();

		public JButton jarRemove = new JButton();

		public ArrayList<FileKey> jarArray = new ArrayList<FileKey>();

		// LIB模型
		public JList libList = new JList();

		public DefaultListModel libModel = new DefaultListModel();

		public JButton libAdd = new JButton();

		public JButton libRemove = new JButton();

		public ArrayList<FileKey> libArray = new ArrayList<FileKey>();

		/**
		 * 
		 */
		public GuideBody() {
			super();
		}

		public String getTypeText() {
			return "GUIDE";
		}
	}

	/**
	 * README区域实例
	 *
	 * @author scott.liang
	 * @version 1.0 8/12/2020
	 * @since laxcus 1.0
	 */
	public class ReadmeBody {
		public JTextField txtLogo = new JTextField();

		public JButton cmdLogo = new JButton();

		/** 图像文件 **/
		public File logo;

		public JTextField txtLicence = new JTextField();

		public JButton cmdLicence = new JButton();

		/** 许可证文件 **/
		public File licence;

		public ReadmeBody() {
			super();
		}

		public String getTypeText() {
			return "README";
		}
	}

	private JLabel logo = new JLabel();

	/** 自读 **/
	protected ReadmeBody readme = new ReadmeBody();

	/** 启动 **/
	protected GuideBody guide = new GuideBody(); 

	/** 重置全部参数 **/
	protected JButton cmdReset = new JButton();

	/** 导入脚本 **/
	protected JButton cmdImport = new JButton();

	/** 导出脚本 **/
	protected JButton cmdExport = new JButton();

	/** 退出窗口 **/
	protected JButton cmdExit = new JButton();

	/**
	 * @param owner
	 * @param modal
	 */
	public TerminalCreateWareScriptDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	protected void initReadmeButtons() {
		setButtonText(readme.cmdLogo, findContent("Dialog/CreateWareScript/buttons/select"));
		setButtonText(readme.cmdLicence, findContent("Dialog/CreateWareScript/buttons/select"));
	}

	protected void initGuideButtons() {
		setButtonText(guide.jarAdd, findContent("Dialog/CreateWareScript/buttons/add"));
		setButtonText(guide.jarRemove, findContent("Dialog/CreateWareScript/buttons/remove"));
		setButtonText(guide.libAdd, findContent("Dialog/CreateWareScript/buttons/add"));
		setButtonText(guide.libRemove, findContent("Dialog/CreateWareScript/buttons/remove"));
		setButtonText(guide.cmdSelect, findContent("Dialog/CreateWareScript/buttons/select"));
	}

	protected void initTaskButtons(TaskBody task) {
		setButtonText(task.jarAdd, findContent("Dialog/CreateWareScript/buttons/add"));
		setButtonText(task.jarRemove, findContent("Dialog/CreateWareScript/buttons/remove"));
		setButtonText(task.libAdd, findContent("Dialog/CreateWareScript/buttons/add"));
		setButtonText(task.libRemove, findContent("Dialog/CreateWareScript/buttons/remove"));
		setButtonText(task.cmdSelect, findContent("Dialog/CreateWareScript/buttons/select"));
	}

	protected void initBottomButtons() {
		setButtonText(cmdReset, findContent("Dialog/CreateWareScript/buttons/reset"));
		setButtonText(cmdImport, findContent("Dialog/CreateWareScript/buttons/import"));
		setButtonText(cmdExport, findContent("Dialog/CreateWareScript/buttons/export"));
		setButtonText(cmdExit, findContent("Dialog/CreateWareScript/buttons/exit"));

		cmdReset.setMnemonic('R');
		cmdImport.setMnemonic('I');
		cmdExport.setMnemonic('E');
		cmdExit.setMnemonic('X');
	}

	private Dimension createLabelPreferredSize() {
		return new Dimension(50, 10);
	}

	private Dimension createButtonPreferredSize() {
		return new Dimension(78, 32);
	}

	private String buildPhase(String str) {
		String text = findContent("Dialog/CreateWareScript/stage");
		return String.format(text, str);
	}

	private Border createUnitEmptyBorder() {
		return new EmptyBorder(2, 1, 2, 1);
	}

	/**
	 * 生成一个标签
	 * @param xmlPath
	 * @return
	 */
	private JLabel createLabel(String xmlPath) {
		String caption = findContent(xmlPath);
		JLabel label = new JLabel(caption);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setPreferredSize(createLabelPreferredSize());
		return label;
	}

	private BorderLayout createRowLayout() {
		return new BorderLayout(4, 0);
	}

	private JPanel createTextFieldPanel(String labelXML, JTextField field, JButton button) {
		JLabel label = createLabel(labelXML);
		button.setPreferredSize(createButtonPreferredSize());

		// 提示
		String xml = labelXML + "/tooltip";
		String tooltip = findCaption(xml);
		field.setToolTipText(tooltip);
		field.setEditable(false); // 不可以编辑

		JPanel sub = new JPanel();
		sub.setLayout(createRowLayout());
		sub.setBorder(createUnitEmptyBorder());
		sub.add(label, BorderLayout.WEST);
		sub.add(field, BorderLayout.CENTER);
		sub.add(button, BorderLayout.EAST);
		return sub;
	}


	private JPanel createTextLogoFieldPanel(String labelXML, JTextField field, JButton button) {
		JLabel label = createLabel(labelXML);

		logo.setVerticalAlignment(SwingConstants.TOP);
		logo.setHorizontalAlignment(SwingConstants.LEFT);
//		logo.setPreferredSize(new Dimension(32, 32));

		button.setPreferredSize(createButtonPreferredSize());

		// 提示
		String xml = labelXML + "/tooltip";
		String tooltip = findCaption(xml);
		field.setToolTipText(tooltip);
		field.setEditable(false); // 不可以编辑

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(0, 0));
		left.add(label, BorderLayout.WEST);
		left.add(logo, BorderLayout.EAST);

		JPanel sub = new JPanel();
		sub.setLayout(createRowLayout());
		sub.setBorder(createUnitEmptyBorder());
		sub.add(left, BorderLayout.WEST);
		sub.add(field, BorderLayout.CENTER);
		sub.add(button, BorderLayout.EAST);
		return sub;
	}

	/**
	 * 生成列表面板
	 * @param labelXML
	 * @param list
	 * @param model
	 * @param add
	 * @param remove
	 * @return
	 */
	private JPanel createListPanel(String labelXML, String listXML, JList list, DefaultListModel model, JButton add, JButton remove) {
		JLabel label = createLabel(labelXML);
		// 固定尺寸
		add.setPreferredSize(createButtonPreferredSize());
		remove.setPreferredSize(createButtonPreferredSize());

		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(2, 1, 0, 4));
		sub.add(add);
		sub.add(remove);
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout(0, 0));
		right.add(sub, BorderLayout.SOUTH);
		right.add(new JPanel(), BorderLayout.CENTER);

		// 显示单元
		list.setCellRenderer(new TerminalWareScriptRenderer()); 
		list.setModel(model);

		String tooltip = findContent(listXML); 
		list.setToolTipText(tooltip);

		// 行高度
		String value = findCaption("Dialog/CreateWareScript/list/row-height");
		int rowHeight = ConfigParser.splitInteger(value, 30);

		list.setFixedCellHeight(rowHeight);
		list.setBorder(new EmptyBorder(3, 2, 2, 2)); // top, left, bottom, right
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选

		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(10, 52));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(6, 0));
		panel.setBorder(createUnitEmptyBorder());
		panel.add(label, BorderLayout.WEST);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(right, BorderLayout.EAST);
		return panel;
	}

	private Border createPanelBorder(String title) {
		return UITools.createTitledBorder(title, 3);
	}

	protected JPanel createReadmePanel() {
		JPanel icon = createTextLogoFieldPanel("Dialog/CreateWareScript/label/icon",  readme.txtLogo, readme.cmdLogo);
		JPanel licence = createTextFieldPanel("Dialog/CreateWareScript/label/licence",  readme.txtLicence, readme.cmdLicence);

		JPanel sub = new JPanel();
		sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
		sub.setBorder(createPanelBorder(readme.getTypeText()));
		sub.add(icon);
		sub.add(licence);
		return sub;
	}

	protected JPanel createGuidePanel() {
		JPanel one = createTextFieldPanel("Dialog/CreateWareScript/label/guide", guide.txtEntry,guide.cmdSelect);
		JPanel jar = createListPanel("Dialog/CreateWareScript/label/jar", "Dialog/CreateWareScript/list/tooltip/jar", 
				guide.jarList, guide.jarModel, guide.jarAdd, guide.jarRemove);
		JPanel lib = createListPanel("Dialog/CreateWareScript/label/lib", "Dialog/CreateWareScript/list/tooltip/lib", 
				guide.libList, guide.libModel, guide.libAdd, guide.libRemove);

		String text = buildPhase(guide.getTypeText());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(createPanelBorder(text));
		panel.add(one);
		panel.add(jar);
		panel.add(lib);
		return panel;
	}

	protected JPanel createTaskPanel(TaskBody task) {
		JPanel one = createTextFieldPanel("Dialog/CreateWareScript/label/task", task.txtEntry, task.cmdSelect);
		JPanel jar = createListPanel("Dialog/CreateWareScript/label/jar", "Dialog/CreateWareScript/list/tooltip/jar", 
				task.jarList, task.jarModel, task.jarAdd, task.jarRemove);
		JPanel lib = createListPanel("Dialog/CreateWareScript/label/lib", "Dialog/CreateWareScript/list/tooltip/lib", 
				task.libList, task.libModel, task.libAdd, task.libRemove);

		String text = buildPhase(task.getTypeText());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(createPanelBorder(text));
		panel.add(one);
		panel.add(jar);
		panel.add(lib);
		return panel;
	}

	/**
	 * 生成按纽面板
	 * @return 返回JPanel实例
	 */
	protected JPanel createButtomButtonPanel() {
		JPanel sub = new JPanel();
		sub.setLayout(new GridLayout(1, 3, 6, 0));
		sub.add(cmdReset);
		sub.add(cmdImport);
		sub.add(cmdExport);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		panel.add(sub, BorderLayout.WEST);
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(cmdExit, BorderLayout.EAST);
		return panel;
	}

	private File focus;

	/**
	 * 打开对话框，选择文件
	 * @param filters
	 * @param select
	 * @return
	 */
	private File[] chooseFile(DiskFileFilter[] filters, File select, boolean multi) {
		String title = findCaption("Dialog/CreateWareScript/open-chooser/title/title");
		String buttonText = findCaption("Dialog/CreateWareScript/open-chooser/choose/title");

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		for (DiskFileFilter f1 : filters) {
			chooser.addChoosableFileFilter(f1);
		}
		
		chooser.setMultiSelectionEnabled(multi);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);

		// 文件存在且有效
		boolean success = (select != null && select.exists() && select.isFile());
		if (success) {
			chooser.setCurrentDirectory(select.getParentFile());
		} else {
			if (focus != null) {
				chooser.setCurrentDirectory(focus.getParentFile());
			}
		}

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		
		// 选择文件
		if (multi) {
			File[] files = chooser.getSelectedFiles();
			if (files != null && files.length > 0) {
				focus = files[0];
			}
			return files;
		} else {
			File file = chooser.getSelectedFile();
			focus = file;
			return new File[] { file };
		}
	}

	/**
	 * 设置LOGO图标
	 * 
	 * @param file
	 */
	private void setLogo(File file) {
		byte[] b = new byte[(int)file.length()];

		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
		} catch (IOException e) {
			return;
		}

		// 显示图像
		ImageIcon icon = new ImageIcon(b);
		boolean success = (icon.getIconHeight() == 32 && icon.getIconWidth() == 32);
		if (!success) {
			String content = findContent("Dialog/CreateWareScript/logo-warning");
			String title = findCaption("Dialog/CreateWareScript/logo-warning/title");
			showWarming(content, title);
			return;
		}

		// 显示图标
		logo.setIcon(icon);
		// 保存参数
		readme.logo = file;
		readme.txtLogo.setText(Laxkit.canonical(file));
	}

	/**
	 * 设置LOGO图标
	 */
	protected void setLogo() {
		// 图像文件
		String ds_image = findCaption("Dialog/CreateWareScript/open-chooser/image/description/title");
		String image = findCaption("Dialog/CreateWareScript/open-chooser/image/extension/title");
		DiskFileFilter filter = new DiskFileFilter(ds_image, image);

		File[] files = chooseFile(new DiskFileFilter[] { filter }, readme.logo, false);
		if (files != null && files.length == 1) {
			setLogo(files[0]);
		}
	}

	/**
	 * 设置许可证文件
	 * @param file
	 */
	private void setLicence(File file) {
		readme.licence = file;
		readme.txtLicence.setText(Laxkit.canonical(file));
	}

	/**
	 * 设置许可证文件
	 */
	protected void setLicence() {
		// 文本文件
		String ds_text = findCaption("Dialog/CreateWareScript/open-chooser/text/description/title");
		String text = findCaption("Dialog/CreateWareScript/open-chooser/text/extension/title");
		DiskFileFilter filter = new DiskFileFilter(ds_text, text);

		File[] files = chooseFile(new DiskFileFilter[] { filter }, readme.licence, false);
		if (files != null && files.length == 1) {
			setLicence(files[0]);
		}
	}

	private void setGTC(File file) {
		guide.gtc = file;
		guide.txtEntry.setText(Laxkit.canonical(file));
	}

	protected void setGTC() {
		// GTC文件
		String ds_gtc = findCaption("Dialog/CreateWareScript/open-chooser/gtc/description/title");
		String gtc = findCaption("Dialog/CreateWareScript/open-chooser/gtc/extension/title");
		DiskFileFilter filter = new DiskFileFilter(ds_gtc, gtc);

		File[] files = chooseFile(new DiskFileFilter[] { filter }, guide.gtc, false);
		if (files != null && files.length == 1) {
			setGTC(files[0]);
		}
	}

	private void setDTC(TaskBody task, File file) {
		task.dtc = file;
		task.txtEntry.setText(Laxkit.canonical(file));
	}

	protected void setDTC(TaskBody task) {
		// DTC文件
		String ds_dtc = findCaption("Dialog/CreateWareScript/open-chooser/dtc/description/title");
		String dtc = findCaption("Dialog/CreateWareScript/open-chooser/dtc/extension/title");
		DiskFileFilter filter = new DiskFileFilter(ds_dtc, dtc);

		File[] files = chooseFile(new DiskFileFilter[] { filter }, task.dtc, false);
		if (files != null && files.length == 1) {
			setDTC(task, files[0]);
		}
	}

	private void addJar(DefaultListModel model, ArrayList<FileKey> array, FileKey key) {
		boolean exists = array.contains(key);
		if (!exists) {
			model.addElement(key);
			array.add(key);
		}
	}

	private void addLib(DefaultListModel model, ArrayList<FileKey> array, FileKey key) {
		boolean exists = array.contains(key);
		if (!exists) {
			model.addElement(key);
			array.add(key);
		}
	}

	protected void addJar(JList list , DefaultListModel model, ArrayList<FileKey> array) {
		File path = null;
		int size = array.size();
		if (size > 0) {
			FileKey key = array.get(size - 1);
			path = key.getFile();
		}

		// JAR文件
		String ds_jar = findCaption("Dialog/CreateWareScript/open-chooser/jar/description/title");
		String jar = findCaption("Dialog/CreateWareScript/open-chooser/jar/extension/title");
		DiskFileFilter filter = new DiskFileFilter(ds_jar, jar);

		File[] files = chooseFile(new DiskFileFilter[] { filter }, path, true);
		if (files == null || files.length == 0) {
			return;
		}

		for (File file : files) {
			// 显示和保存
			FileKey key = new FileKey(file);
			addJar(model, array, key);
		}
	}

	protected void addLib(JList list , DefaultListModel model, ArrayList<FileKey> array) {
		File path = null;
		int size = array.size();
		if (size > 0) {
			FileKey key = array.get(size - 1);
			path = key.getFile();
		}

		// LIB文件
		String ds_lib = findCaption("Dialog/CreateWareScript/open-chooser/lib/description/title");
		String lib = findCaption("Dialog/CreateWareScript/open-chooser/lib/extension/title");
		DiskFileFilter filter = new DiskFileFilter(ds_lib, lib);

		// 选择文件
		File[] files = chooseFile(new DiskFileFilter[] { filter }, path, true);
		if (files == null || files.length == 0) {
			return;
		}

		// 显示和保存
		for (File file : files) {
			FileKey key = new FileKey(file);
			addLib(model, array, key);
		}
	}

	protected FileKey getSelectFile(JList list, DefaultListModel model) {
		// 定位下标
		int index = list.getSelectedIndex();
		// 小于0或者大于成员数目时，忽略
		if (index < 0 || index >= model.size()) {
			return null;
		}
		// 找到指定位置的成员，处理它！
		Object value = model.elementAt(index);
		if (value.getClass() != FileKey.class) {
			return null;
		}
		return (FileKey) value;
	}

	protected void removeJar(JList list , DefaultListModel model, ArrayList<FileKey> array) {
		FileKey key = getSelectFile(list, model);
		if (key != null) {
			array.remove(key);
			model.removeElement(key);
		}
	}

	protected void removeLib(JList list , DefaultListModel model, ArrayList<FileKey> array) {
		FileKey key = getSelectFile(list, model);
		if (key != null) {
			array.remove(key);
			model.removeElement(key);
		}
	}

	protected boolean checkReadme() {
		if (readme.logo == null) {
			readme.txtLogo.requestFocus();
			return false;
		}
		if (readme.licence == null) {
			readme.txtLicence.requestFocus();
			return false;
		}
		return true;
	}

	protected boolean checkGuide() {
		if (guide.gtc == null) {
			guide.txtEntry.requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * 检查组件
	 * @param tasks
	 * @return
	 */
	protected boolean checkTasks(TaskBody[] tasks) {
		for (TaskBody e : tasks) {
			if (e.dtc == null) {
				e.txtEntry.requestFocus();
				return false;
			}
		}
		return true;
	}

	/**
	 * 询问覆盖...
	 * @param file 磁盘文件
	 * @return 返回真或者假
	 */
	protected boolean override(File file) {
		// 提示错误
		String title = findCaption("Dialog/CreateWareScript/override/title");
		String content = findContent("Dialog/CreateWareScript/override");
		String format = String.format(content, file.toString());
		// 选择...
		int who = MessageDialog.showMessageBox(this, title,
				JOptionPane.QUESTION_MESSAGE, null, format,
				JOptionPane.YES_NO_OPTION);
		return (who == JOptionPane.YES_OPTION) ;
	}

	/**
	 * 显示不足
	 */
	protected void showMissing() {
		String title = findCaption("Dialog/CreateWareScript/missing/title");
		String content = findContent("Dialog/CreateWareScript/missing"); 
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
	 * 显示错误消息
	 * @param content
	 * @param title
	 */
	private void showWarming(String content, String title) {
		MessageDialog.showMessageBox(this, title, JOptionPane.WARNING_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	/**
	 * 显示错误
	 * @param content
	 * @param title
	 */
	private void showFault(String content, String title) {
		MessageDialog.showMessageBox(this, title, JOptionPane.ERROR_MESSAGE, content, JOptionPane.DEFAULT_OPTION);
	}

	protected void reset(TaskBody[] tasks) {
		// 自读文件
		readme.logo = null;
		readme.txtLogo.setText("");
		readme.licence = null;
		readme.txtLicence.setText("");

		// 引导程序
		guide.gtc = null;
		guide.txtEntry.setText("");

		guide.jarArray.clear();
		guide.jarModel.clear();

		guide.libArray.clear();
		guide.libModel.clear();

		for (TaskBody e : tasks) {
			e.dtc = null;
			e.txtEntry.setText("");

			e.jarArray.clear();
			e.jarModel.clear();

			e.libArray.clear();
			e.libModel.clear();
		}
	}

	/**
	 * 格式化CDATA格式的XML标签
	 * @param tag
	 * @param value
	 * @return 字符串
	 */
	private String formatXML_CDATA(String tag, String value) {
		Laxkit.nullabled(tag);
		if (value == null) {
			value = "";
		}
		return String.format("<%s><![CDATA[%s]]></%s>\n", tag, value, tag);
	}

	/**
	 * 格式化XML标签
	 * @param tag
	 * @param value
	 * @return
	 */
	private String formatXML(String tag, String value) {
		Laxkit.nullabled(tag);
		if (value == null) {
			value = "";
		}
		return String.format("<%s>\n%s</%s>\n", tag, value, tag);
	}

	private boolean isConduct(TaskBody[] tasks) {
		for (TaskBody e : tasks) {
			if (!PhaseTag.isConduct(e.getType())) {
				return false;
			}
		}
		return true;
	}

	private boolean isEstablish(TaskBody[] tasks) {
		for (TaskBody e : tasks) {
			if (!PhaseTag.isEstablish(e.getType())) {
				return false;
			}
		}
		return true;
	}

	private boolean isContact(TaskBody[] tasks) {
		for (TaskBody e : tasks) {
			if (!PhaseTag.isContact(e.getType())) {
				return false;
			}
		}
		return true;
	}

	private String translate(TaskBody[] tasks) {
		if (isConduct(tasks)) {
			return "conduct";
		}
		if (isEstablish(tasks)) {
			return "establish";
		}
		if (isContact(tasks)) {
			return "contact";
		}
		return "none";
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
	 * 数据写入脚本文件
	 * @param tasks
	 */
	protected boolean writeScript(File file, TaskBody[] tasks) {
		StringBuilder buff = new StringBuilder();
		StringBuilder sub = new StringBuilder();
		// 1. 自读文件
		sub.append(formatXML_CDATA("logo", Laxkit.canonical(readme.logo)));
		sub.append(formatXML_CDATA("licence", Laxkit.canonical(readme.licence)));
		buff.append(formatXML(readme.getTypeText(), sub.toString()));

		// 2. GUIDE任务
		sub.delete(0, sub.length());
		sub.append(formatXML_CDATA("boot", Laxkit.canonical(guide.gtc))); // 引导包
		for (FileKey key : guide.jarArray) {
			sub.append(formatXML_CDATA("jar", key.getPath()));
		}
		for (FileKey key : guide.libArray) {
			sub.append(formatXML_CDATA("lib", key.getPath()));
		}
		buff.append(formatXML(guide.getTypeText(), sub.toString()));

		// 3. 计算任务
		for (TaskBody task : tasks) {
			sub.delete(0, sub.length());
			sub.append(formatXML_CDATA("boot", Laxkit.canonical(task.dtc))); // 引导包
			for (FileKey key : task.jarArray) {
				sub.append(formatXML_CDATA("jar", key.getPath()));
			}
			for (FileKey key : task.libArray) {
				sub.append(formatXML_CDATA("lib", key.getPath()));
			}
			buff.append(formatXML(task.getTypeText(), sub.toString()));
		}

		String head = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		//		String body = CloudToken.formatXML("root", buff.toString());

		String body = String.format("<root family=\"%s\">\n%s</root>\n",
				translate(tasks), buff.toString());

		boolean success = false;
		try {
			byte[] b = new UTF8().encode(head + body);
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}

		// 保存
		if (success) {
			//			saveFile = file;
			//			String filename = Laxkit.canonical(saveFile);
			//			UITools.putProperity(SAVE_KEY, filename);

			setExportFile(file);
			String filename = Laxkit.canonical(file);

			String title = findCaption("Dialog/CreateWareScript/write/success/title");
			String content = findContent("Dialog/CreateWareScript/write/success");
			content = String.format(content, filename);
			showMessage(content, title);
		} else {
			String text = findCaption("Dialog/CreateWareScript/write/failed/title");
			showWarming(text, getTitle());
		}

		return success;
	}

	/**
	 * 显示XML解析错误
	 * @param file
	 * @param stage
	 */
	private void showXMLFault(File file, String stage) {
		String content = "";
		if (stage == null) {
			content = findContent("Dialog/CreateWareScript/script-error/single");
			content = String.format(content, file);
		} else {
			content = findContent("Dialog/CreateWareScript/script-error/multi");
			content = String.format(content, file, stage);
		}
		String title = findCaption("Dialog/CreateWareScript/script-error/title");
		showFault(content, title);
	}

	//	/** 读取选择中的文件 **/
	//	protected File readFile;

	/**
	 * 保存读的脚本文件
	 * @param file
	 */
	private void setImportFile(File file) {
		String filename = Laxkit.canonical(file);
		UITools.putProperity(READ_FILE, filename);
	}

	/**
	 * 设置开放目录
	 * @param chooser
	 */
	protected void setImportDirectory(JFileChooser chooser) {
		Object memory = UITools.getProperity(READ_FILE);
		if (memory != null && memory.getClass() == String.class) {
			File file = new File((String) memory);
			if (file.exists() && file.isFile()) {
				chooser.setCurrentDirectory(file.getParentFile());
			}
		}
	}

//	/**
//	 * 读取脚本
//	 * @param file
//	 * @param tasks
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean readScript(File file, TaskBody[] tasks) {
//		reset(tasks);
//
//		PackageScriptReader reader = null;
//		try {
//			reader = new PackageScriptReader(file);
//		} catch (IOException e) {
//			Logger.error(e);
//			showXMLFault(file, null);
//			return false;
//		}
//
//		boolean success = false;
//		// 判断一致！
//		if (isConduct(tasks)) {
//			success = reader.isConduct();
//		} else if (isEstablish(tasks)) {
//			success = reader.isEstablish();
//		} else if (isContact(tasks)) {
//			success = reader.isContact();
//		}
//
//		if (!success) {
//			showXMLFault(file, null);
//			return false;
//		}
//
//		// 保存
//		setImportFile(file);
//
//		// 自读
//		ReadmePackageElement me = reader.readReadme();
//		if (me == null) {
//			showXMLFault(file, "README");
//			return false;
//		}
//		setLogo(new File(me.getLogo().getPath()));
//		setLicence(new File(me.getLicence().getPath()));
//
//		// 启动包
//		CloudPackageElement element = reader.readGuide();
//		if (element == null) {
//			showXMLFault(file, "GUIDE");
//			return false;
//		}
//		// 赋值
//		setGTC(new File(element.getBoot().getPath()));
//		for (FileKey key : element.getAssists()) {
//			addJar(guide.jarModel, guide.jarArray, key);
//		}
//		for (FileKey key : element.getLibraries()) {
//			addLib(guide.libModel, guide.libArray, key);
//		}
//
//		// 任务包
//		for (TaskBody task : tasks) {
//			element = reader.readTask(task.getType());
//			if (element == null) {
//				showXMLFault(file, PhaseTag.translate(task.getType()));
//				return false;
//			}
//			// 赋值
//			setDTC(task, new File(element.getBoot().getPath()));
//			for (FileKey key : element.getAssists()) {
//				addJar(task.jarModel, task.jarArray, key);
//			}
//			for (FileKey key : element.getLibraries()) {
//				addLib(task.libModel, task.libArray, key);
//			}
//		}
//
//		// 最后成功
//		return true;
//	}
	
	/**
	 * 读取脚本
	 * @param file
	 * @param tasks
	 * @return 成功返回真，否则假
	 */
	protected boolean readScript(File file, TaskBody[] tasks) {
		reset(tasks);

		// 读脚本文件
		PackageScriptReader reader = null;
		try {
			reader = new PackageScriptReader(file);
		} catch (IOException e) {
			Logger.error(e);
			showXMLFault(file, null);
			return false;
		}

		boolean success = false;
		// 判断一致！
		if (isConduct(tasks)) {
			success = reader.isConduct();
		} else if (isEstablish(tasks)) {
			success = reader.isEstablish();
		} else if (isContact(tasks)) {
			success = reader.isContact();
		}

		if (!success) {
			showXMLFault(file, null);
			return false;
		}

		// 保存
		setImportFile(file);
		
		// 统计出错
		int count = 0;
		// 自读
		ReadmePackageElement me = reader.readReadme();
		if (me == null) {
			showXMLFault(file, "README");
			count++;
		} else {
			setLogo(new File(me.getLogo().getPath()));
			setLicence(new File(me.getLicence().getPath()));
		}

		// 启动包
		CloudPackageElement element = reader.readGuide();
		if (element == null) {
			showXMLFault(file, "GUIDE");
			count++;
		} else {
			// 赋值
			setGTC(new File(element.getBoot().getPath()));
			for (FileKey key : element.getAssists()) {
				addJar(guide.jarModel, guide.jarArray, key);
			}
			for (FileKey key : element.getLibraries()) {
				addLib(guide.libModel, guide.libArray, key);
			}
		}

		// 任务包
		for (TaskBody task : tasks) {
			element = reader.readTask(task.getType());
			if (element == null) {
				showXMLFault(file, PhaseTag.translate(task.getType()));
				count++;
			} else {
				// 赋值
				setDTC(task, new File(element.getBoot().getPath()));
				for (FileKey key : element.getAssists()) {
					addJar(task.jarModel, task.jarArray, key);
				}
				for (FileKey key : element.getLibraries()) {
					addLib(task.libModel, task.libArray, key);
				}
			}
		}

		// 判断成功
		return (count == 0);
	}

}