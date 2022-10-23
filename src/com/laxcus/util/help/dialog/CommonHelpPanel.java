/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.help.dialog;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.laxcus.gui.component.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.help.*;
import com.laxcus.util.skin.*;

/**
 * 分布站点浏览面板。在窗口的左侧。
 * 因为SWING组件是线程不安全的，所有关于JTREE的“添加、删除、释放”操作，放在SWING事件队列里执行。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2012
 * @since laxcus 1.0
 */
public class CommonHelpPanel extends JPanel implements ActionListener, TreeSelectionListener, CaretListener,HyperlinkListener {

	private static final long serialVersionUID = 8958854364644337263L;

	/** 搜索图标 **/
	private JLabel lblSearch = new JLabel();

	/** 搜索框 **/
	private FlatTextField txtSearch = new FlatTextField();

	/** 搜索按纽 **/
	private FlatButton cmdSearch = new FlatButton();

	/** 底栏 **/
	private JLabel lblBottom = new JLabel(" ", SwingConstants.LEFT);

	/** 站点根目录 **/
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(); 

	/** 样板 **/
	private CommonHelpTreeCellRenderer renderer; // = new CommonHelpTreeCellRenderer();
	
	/** 次层树模型 **/
	private DefaultTreeModel model;

	/** 站点结构 **/
	private JTree tree;

	/** 记录节点地址 **/
	private TreeSet<CommentElement> elements = new TreeSet<CommentElement>();

	/** 显示命令的辅助面板 **/
	private JTextPane htmlPanel;// = new JTextPane();

	/** 命令解释上下文 **/
	private CommentContext context;

	/**
	 * 构造默认的注册站点地址面板
	 */
	public CommonHelpPanel() {
		super();
		renderer = new CommonHelpTreeCellRenderer();
		//	diagram = "<HTML><body bgcolor=\"#ffffff\"><p>&nbsp;</p><p align=\"center\"><img src=\"jar:file:/E:/parallel/watch/lib/laxcus_watch.jar!/conf/watch/html/topology.jpg\" width=\"600\" height=\"720\"></p><p>&nbsp;</p></body></HTML>";
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		doSearch(e);
	}

	/**
	 * 设置图标
	 * @param close
	 * @param open
	 * @param command
	 */
	public void setHelpIcon(Icon close, Icon open, Icon command, Icon search, Icon go) {
		renderer.setIcons(close, open, command);
		lblSearch.setIcon(search);
		cmdSearch.setIcon(go);
	}

	/**
	 * 设置命令解释上下文
	 * @param e 命令解释上下文
	 */
	public void setCommentContext(CommentContext e) {
		context = e;
	}

	/**
	 * 返回命令解释上下文
	 * @return 命令解释上下文
	 */
	public CommentContext getCommentContext() {
		return context;
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 设置字体
	 * @param name 字体名称
	 */
	private void __exchangeFont(String name) {
		if (name == null || name.trim().isEmpty()) {
			return;
		}
		Font font = tree.getFont();
		tree.setFont(new Font(name, Font.PLAIN, 14));
		font = htmlPanel.getFont();
		htmlPanel.setFont(new Font(name, Font.PLAIN, font.getSize()));
		font = lblBottom.getFont();
		lblBottom.setFont(new Font(name, Font.PLAIN, 14));
		font = txtSearch.getFont();
		txtSearch.setFont(new Font(name, Font.PLAIN, 14));
	}
	

	private int readDeviderLocation() {
		return RTKit.readInteger(RTEnvironment.ENVIRONMENT_SYSTEM, 
				"HelpFrame/DeviderLocation", 178);
	}
	
	/**
	 * 查找指定的组件
	 * 
	 * @param <T>
	 * @param container
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findComponent(Container container, Class<?> clazz) {
		// 逐个检测
		Component[] objects = container.getComponents();
		int size = (objects != null && objects.length > 0 ? objects.length : 0);
		for (int i = 0; i < size; i++) {
			Component object = objects[i];
			if (object.getClass() == clazz) {
				return (T) object;
			} else if (Laxkit.isClassFrom(object, Container.class)) {
				Object o = findComponent((Container) object, clazz);
				if (o != null) {
					return (T) o;
				}
			}
		}
		return null;
	}
	
	public void writeDeviderLocation() {
		JSplitPane jsp = findComponent(this, JSplitPane.class);
		if (jsp != null) {
			int pixel = jsp.getDividerLocation();
			RTKit.writeInteger(RTEnvironment.ENVIRONMENT_SYSTEM,
					"HelpFrame/DeviderLocation", pixel);
		}
	}
	
	class TitlePanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public TitlePanel() {
			super();
		}

		protected void paintBorder(Graphics g) {
			Color old = g.getColor();
			int width = getWidth();
			int y = 0; //getHeight() - 1;

			if (Skins.isGraySkin()) {
				Color c = Color.GRAY;
				g.setColor(c);
				g.drawLine(0, y, width, y);
			} else {
				Color c = UIManager.getColor("Panel.background");
				if (c == null) {
					c = getBackground();
				}
				ESL esl = new ESL(c);
				esl.brighter(50);
				c = esl.toColor();
				g.setColor(c);
				g.drawLine(0, y, width, y);
			}
			g.setColor(old);
		}
	}
	
	/**
	 * 初始化面板
	 */
	public void init() {
		// 允许保存子节点
		root.setAllowsChildren(true);
		// 建立树模型
		model = new DefaultTreeModel(root);
		// 建立树型结构
		tree = new JTree(model);
		// 指定树单元格类，这个参数必须有！
		tree.setCellRenderer(renderer);
//		tree.setCellRenderer(renderer = new CommonHelpTreeCellRenderer());
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setBorder(new EmptyBorder(4, 3, 4, 3));
		tree.setRowHeight(26); // -1);
		tree.setToggleClickCount(1);
//		tree.setShowsRootHandles(true);
		tree.setShowsRootHandles(false);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		tree.setEditable(false);
		
		// 不显示连接线
		tree.putClientProperty("JTree.lineStyle", "None");

		// 面板
		htmlPanel = new JTextPane();
		htmlPanel.setEditable(false);
		htmlPanel.addCaretListener(this);
		htmlPanel.addHyperlinkListener(this); // 链接
		htmlPanel.setContentType("text/html;");
//		doHtmlBackground();
		
//		TitlePanel tp = new TitlePanel();
		JPanel tp = new JPanel();
		tp.setLayout(new BorderLayout(0,0)); // new GridLayout(1, 1, 0, 0));
		tp.setBorder(new EmptyBorder(2, 2, 2, 2));
		tp.add(lblBottom, BorderLayout.CENTER);

		// 左侧面板
		JScrollPane left = new JScrollPane(tree);
//		left.setBorder(new HighlightBorder(1));
//		left.putClientProperty("NotBorder", Boolean.TRUE);
//		left.setBorder(new EmptyBorder(1, 1, 1, 1));
		// 右侧上部面板
//		htmlPanel.setBorder(new EmptyBorder(0,0,0,0));
		htmlPanel.putClientProperty("NotBorder", Boolean.TRUE);
		JScrollPane right_top = new JScrollPane(htmlPanel);
//		right_top.setBorder(new HighlightBorder(1));
//		right_top.putClientProperty("NotBorder", Boolean.TRUE);
//		right_top.setBorder(new EmptyBorder(1, 1, 1, 1));
		// 右侧下部面板
		lblBottom.setBorder(new EmptyBorder(2, 5, 5, 5));
		// 右侧面板，合并！
		JPanel right = new JPanel();
		right.setLayout(new BorderLayout(0, 0));
		right.setBorder(new EmptyBorder(0, 1, 0, 1));
		right.add(right_top, BorderLayout.CENTER);
		right.add(tp, BorderLayout.SOUTH);
//		right.add(lblBottom, BorderLayout.SOUTH);

		// 分割符
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, left, right);
		jsp.setContinuousLayout(true);
		jsp.setOneTouchExpandable(false);
		jsp.setDividerSize(4);
		// 像素
		jsp.setDividerLocation(readDeviderLocation());
		jsp.putClientProperty("NotBorder", Boolean.TRUE);
		jsp.putClientProperty("FlatDivider", Boolean.TRUE);
		jsp.setBorder(new HighlightBorder(0));
		
		// 顶部
		cmdSearch.addActionListener(this);
		txtSearch.addActionListener(this);

		// 顶部
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout(8, 0));
		top.setBorder(new EmptyBorder(2, 5, 3, 5));
		top.add(lblSearch, BorderLayout.WEST);
		top.add(txtSearch, BorderLayout.CENTER);
		top.add(cmdSearch, BorderLayout.EAST);

		// 布局
		setLayout(new BorderLayout(8, 5));
		add(top, BorderLayout.NORTH);
		add(jsp, BorderLayout.CENTER);

		// 在线程中使用改变字体
		setSelectFont(context.getTemplateFontName());

		// 显示在界面
		for (CommentGroup group : context.list()) {
			push(group);
			for (CommentElement element : group.list()) {
				push(element);
			}
		}
	}

	/**
	 * 显示HTML帮助文档
	 * @param element
	 */
	private void showHTML(CommentElement element) {
		String text = context.formatHTML(element);
		showHtmlText(text);
		showBottom(element.getCommand());
		htmlPanel.setCaretPosition(0);
//		doHtmlBackground();
	}
	
	private String htmlText;
	
	/**
	 * 记录和显示HTML文本
	 * @param text
	 */
	private void showHtmlText(String text) {
		htmlText = text;
		htmlPanel.setText(htmlText);
	}
	
	private void updateHtmlBackground() {
		if (htmlText == null) {
			return;
		}
		String background = CommentTemplate.createBackground();
		
		String text = new String(htmlText);
		
		String flag = "bgcolor=";
		int index = text.indexOf(flag);
		if (index == -1) {
			return;
		}
		int last = text.indexOf(" ", index + flag.length());
		if (last == -1) {
			return;
		}

//		String bg = text.substring(index, last);
//		System.out.printf("{%s} {%s}\n", bg, background);
		
		String prefix = text.substring(0, index);
		String suffix = text.substring(last);
		text = prefix + background + suffix;
		
		// 前景色
		String foreground = CommentTemplate.createTextForeground();
		flag = "text=";
		index = text.indexOf(flag);
		if (index > 0) {
			last = text.indexOf(" ", index + flag.length());
			if (last > index) {
//				String fg = text.substring(index, last);
//				System.out.printf("{%s} {%s}\n\n", fg, foreground);
				
				prefix = text.substring(0, index);
				suffix = text.substring(last);
				text = prefix + foreground + suffix;
			}
		}

		// 更新
//		text.replaceFirst(bg, background);
		
		showHtmlText(text);
		htmlPanel.setCaretPosition(0);
	}

	/**
	 * 切换到指定的线程
	 * @param e
	 */
	private void __exchangeHTML() {
		Object node = tree.getLastSelectedPathComponent();
		// 空对象，忽略它
		if (node == null) {
			return;
		}
		// 判断是选择的对象
		if (node.getClass() == CommonCommentElementTreeNode.class) {
			CommonCommentElementTreeNode sub = (CommonCommentElementTreeNode) node;
			CommentElement element = sub.getElement();

//			ShowHTMLThread thread = new ShowHTMLThread(element);
//			addThread(thread);
			
			showHTML(element);
		}
	}
	
	/**
	 * 更新字体
	 */
	private void updateContextFont() {
		// 找到字体
		if (context != null && htmlPanel != null) {
			Font font = FontKit.findFont(htmlPanel.getClass());
			if (font != null) {
				context.setTemplateFontName(font.getName());
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent event) {
//		addThread(new SwitchHTMLThread(event));
		__exchangeHTML();
	}

	/**
	 * 显示底栏
	 * @param text
	 */
	private void showBottom(String text) {
		String empty = "<HTML><body>&nbsp;</body></HTML>";
		if (!text.isEmpty()) {
			empty = String.format("<HTML><body>%s</body></HTML>", text);
		}
		lblBottom.setText(empty);
	}

	/**
	 * 显示空白
	 */
	private void showEmpty() {
		String background = CommentTemplate.createBackground(); 

		String empty = String.format("<HTML><body %s><p>&nbsp;</p></body></HTML>", background);
		showHtmlText(empty);

		// 清空底栏
		showBottom("");
	}

	/**
	 * 搜索和显示数据
	 */
	private void doShow(String command) {
		if (command == null || command.trim().isEmpty()) {
			showEmpty();
			return;
		}

		// 过滤掉*号
		boolean really = true;
		final String HELP_SUFFIX = "^\\s*(?:[\\\\*]*)([^\\\\*]+)\\s*([\\\\*]+)\\s*$"; 
		Pattern pattern = Pattern.compile(HELP_SUFFIX);
		Matcher matcher = pattern.matcher(command);
		if (matcher.matches()) {
			really = false;
			command = matcher.group(1);
		}

		// 查找关键字
		if (really) {
			CommentElement element = context.findComment(command);
			if (element != null) {
				String text = context.formatHTML(element);
				showHtmlText(text);
				showBottom(element.getCommand());
				// 开始位置
				htmlPanel.setCaretPosition(0);
				return;
			}
		}

		// 部分匹配的命令
		List<CommentElement> elements = context.findAllComments(command);
		if (elements.size() > 0) {
			String text = context.formatHTMLCommands(elements);
			showHtmlText(text);
			showBottom("");
			// 开始位置
			htmlPanel.setCaretPosition(0);
			return;
		}

		showEmpty();
	}
	
	/**
	 * 搜索和显示数据
	 */
	private void search() {
		doShow(txtSearch.getText());
	}

	/**
	 * 查找一个命令并且演示
	 * @param command
	 */
	private void doFind(String command) {
		// 查找关键字
		CommentElement element = context.findComment(command);
		if (element != null) {
			String text = context.formatHTML(element);
			showHtmlText(text);
			showBottom(element.getCommand());
			htmlPanel.setCaretPosition(0); // 光标到最前面
		} else {
			showEmpty();
		}
	}

	
	private void doSearch(ActionEvent event) {
		if (event.getSource() == txtSearch || event.getSource() == cmdSearch) {
			search();
		}
	}

	
	class ShowCommandThread extends SwingEvent {
		String command;

		ShowCommandThread(String str) {
			super();
			if (str != null) {
				str = str.trim();
			}
			if (str != null) {
				command = str;
			}
		}

		public void process() {
			// 显示文本
			if (command != null) {
				txtSearch.setText(command);
				// 显示结果
				doShow(command);
			} else {
				// 清除显示
				showEmpty();
				// 清除
				txtSearch.setText("");
			}
		}
		
//		public void process() {
//			// 显示文本
//			if (command != null) {
//				txtSearch.setText(command);
//			} 
//			// 显示结果
//			doShow(command);
//		}
		
	}
	
	/**
	 * 显示命令
	 * @param command 命令的任何格式，或者空指针
	 */
	public void showCommand(String command) {
		addThread(new ShowCommandThread(command));
	}


	/* (non-Javadoc)
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	@Override
	public void caretUpdate(CaretEvent e) {

	}

	/**
	 * 清除全部参数
	 * 
	 */
	class ClearSiteThread extends SwingEvent {
		ClearSiteThread(){
			super();
		}
		public void process() {
			root.removeAllChildren();
			model.reload(root);
			elements.clear();
		}
	}

	/**
	 * 增加一个命令解释集
	 *
	 * @author scott.liang
	 * @version 1.0 9/10/2018
	 * @since laxcus 1.0
	 */
	class PushGroupThread extends SwingEvent {
		CommentGroup group;

		PushGroupThread(CommentGroup e) {
			super();
			group = e;
		}

		public void process() {
			add(group);
		}
	}

	/**
	 * 增加一个命令解释单元
	 */
	class PushElementThread extends SwingEvent {
		CommentElement element;

		PushElementThread(CommentElement e) {
			super();
			element = e;
		}
		public void process() {
			add(element);
		}
	}

	/**
	 * 删除一个站点
	 */
	class DropSiteThread extends SwingEvent {
		CommentElement node;

		DropSiteThread(CommentElement e) {
			super();
			node = e;
		}
		public void process() {
			remove(node);
		}
	}

	/**
	 * 清除全部旧的记录
	 */
	public void clear() {
		ClearSiteThread thread = new ClearSiteThread();
		addThread(thread);
	}

	/**
	 * 查找站点的类型树节点
	 * @param element
	 * @return
	 */
	private CommonCommentGroupTreeNode findSiteFamilyNode(int no) {
		int count = root.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode element = root.getChildAt(index);
			if (element.getClass() == CommonCommentGroupTreeNode.class) {
				CommonCommentGroupTreeNode that = (CommonCommentGroupTreeNode) element;
				if (that.getNo() == no) {
					return that;
				}
			}
		}
		return null;
	}

	/**
	 * 查找站点的类型树节点
	 * @param node
	 * @return
	 */
	private CommonCommentGroupTreeNode findSiteFamilyNode(CommentElement node) {
		int count = root.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode element = root.getChildAt(index);
			if (element.getClass() == CommonCommentGroupTreeNode.class) {
				CommonCommentGroupTreeNode that = (CommonCommentGroupTreeNode) element;
				if (that.getNo() == node.getNo()) {
					return that;
				}
			}
		}
		return null;
	}

	/**
	 * 添加命令解释单元
	 * @param node
	 */
	private void add(CommentElement node) {
		// 如果节点存在，不再记录
		if (elements.contains(node)) {
			return;
		}
		// 查找与编号相关了的集
		CommonCommentGroupTreeNode parent = findSiteFamilyNode(node.getNo());
		// 没有找到，忽略它
		if (parent == null) {
			return;
		}

		CommonCommentElementTreeNode child = new CommonCommentElementTreeNode(node);

		//		int index = parent.getChildCount();
		//		model.insertNodeInto(child, parent, index);
		//		model.nodesWereInserted(parent, new int[] { index });
		//		model.reload(parent);

		// 更新
		model.insertNodeInto(child, parent, parent.getChildCount());
		model.nodeChanged(parent);

		// 保存到站点地址集合
		elements.add(node);
	}

	/**
	 * 建立站点的类型树节点
	 * @param element
	 * @return
	 */
	private CommonCommentGroupTreeNode createHelpGroupTreeNode(CommentGroup group) {
		CommonCommentGroupTreeNode parent = findSiteFamilyNode(group.getNo());

		if (parent == null) {
			int index = root.getChildCount();
			parent = new CommonCommentGroupTreeNode(group);
			model.insertNodeInto(parent, root, index);

			if (index == 0) {
				model.reload(root);
			} else {
				model.nodeChanged(parent);
			}
		}

		return parent;
	}

	/**
	 * 保存命令解释集
	 * @param node
	 */
	private void add(CommentGroup node) {
		createHelpGroupTreeNode(node);
	}

	/**
	 * 删除命令解释单元
	 * @param element 命令解释单元
	 */
	private void remove(CommentElement element) {
		// 节点不存在，退出！
		if (!elements.contains(element)) {
			return;
		}
		// 找到类型树节点
		CommonCommentGroupTreeNode parent = findSiteFamilyNode(element);
		if (parent == null) {
			return;
		}

		int count = parent.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode treeNode = parent.getChildAt(index);
			if (treeNode.getClass() != CommonCommentElementTreeNode.class) {
				continue;
			}
			CommonCommentElementTreeNode child = (CommonCommentElementTreeNode) treeNode;
			if (child.getElement().compareTo(element) == 0) {
				parent.remove(child);
				model.nodesWereRemoved(parent, new int[] { index }, new Object[] { child });
				model.reload(parent);
				break;
			}
		}

		// 如果是空集合，这个节点
		if(parent.getChildCount() == 0) {
			model.removeNodeFromParent(parent);
		}
		// 删除记录
		elements.remove(element);
	}

	/**
	 * 推送一个命令解释集
	 * @param group 命令解释集
	 * @return 成功返回真，否则假
	 */
	public boolean push(CommentGroup group) {
		PushGroupThread thread = new PushGroupThread(group);
		addThread(thread);
		return true;
	}

	/**
	 * 推送一个命令解释单元
	 * @param element 命令解释单元
	 * @return 成功返回真，否则假
	 */
	public boolean push(CommentElement element) {
		PushElementThread thread = new PushElementThread(element);
		addThread(thread);
		return true;
	}

	/**
	 * 删除命令解释单元
	 * @param element
	 * @return
	 */
	public boolean drop(CommentElement element) {
		DropSiteThread thread = new DropSiteThread(element);
		addThread(thread);
		return true;
	}

	/**
	 * 以故障状态删除一个注册站点
	 * @param node
	 * @return
	 */
	public boolean destroy(CommentElement node) {
		return drop(node);
	}

	/**
	 * 返回当前字体
	 * @return
	 */
	public Font getSelectFont() {
		return tree.getFont();
	}

	/**
	 * 线程修改字体
	 * @param e
	 */
	public void setSelectFont(String name) {
		addThread(new FontThread(name));
	}

	/**
	 * 字体线程
	 *
	 * @author scott.liang
	 * @version 1.0 8/28/2018
	 * @since laxcus 1.0
	 */
	class FontThread extends SwingEvent {
		String name;

		FontThread(String e) {
			super();
			name = e;
		}

		public void process() {
			__exchangeFont(name);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		// 点击命令
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String cmd = e.getDescription();
			// addThread(new FindThread(cmd));
			doFind(cmd);
		}
	}
	
	/**
	 * 更新组件UI
	 */
	private void updateComponentsUI() {
		if (tree != null) {
			FontKit.setDefaultFont(tree);
			tree.updateUI();
		}
		if (renderer != null) {
			renderer.updateUI();
		}
		FontKit.setDefaultFont(txtSearch);
		FontKit.setDefaultFont(cmdSearch);
		FontKit.setDefaultFont(lblBottom);
		
		// 更新界面
		updateContextFont();
		updateHtmlBackground();
	}

	class UpdateUIThread extends SwingEvent {

		UpdateUIThread() {
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			updateComponentsUI();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		// 更新字体
		if (context != null) {
			context.updateFont();
		}
		
		// 更新界面
		super.updateUI();
		
		// 更新
		if (renderer != null) {
			renderer.updateUI();
		}
		
		// 更新参数
		if (htmlPanel != null && tree != null) {
			// 更新界面字体和调整背景
			addThread(new UpdateUIThread());
		}
	}
	
}


//private void doHtmlBackground() {
//	Color c = UIManager.getColor("TextPane.background");
//	if (c != null) {
//		c = new Color(c.getRGB());
//		htmlPanel.setBackground(c);
//	}
//}


//class ShowHTMLThread extends SwingEvent {
//	CommentElement element;
//
//	ShowHTMLThread(CommentElement e) {
//		super();
//		element = e;
//	}
//
//	public void process() {
//		showHTML(element);
//	}
//}

///**
// * 切换到指定的线程
// * @param e
// */
//private void __exchange(TreeSelectionEvent e) {
//	Object node = tree.getLastSelectedPathComponent();
//
//	// 空对象，忽略它
//	if (node == null) {
//		return;
//	}
//	// 判断是选择的对象
//	if (node.getClass() == CommonCommentElementTreeNode.class) {
//		CommonCommentElementTreeNode sub = (CommonCommentElementTreeNode) node;
//		CommentElement element = sub.getElement();
//
//		ShowHTMLThread thread = new ShowHTMLThread(element);
//		addThread(thread);
//	}
//}


//	class UpdateHTMLFontThread extends SwingEvent {
//		
//		UpdateHTMLFontThread(){
//			super();
//		}
//		
//		public void process() {
//			__switchFont();
//		}
//	}

//	/**
//	 * 切换选项线程
//	 * @author scott.liang
//	 * @version 1.0 3/30/2020
//	 * @since laxcus 1.0
//	 */
//	class SwitchHTMLThread extends SwingEvent {
//
//		TreeSelectionEvent event;
//
//		SwitchHTMLThread(TreeSelectionEvent e) {
//			super();
//			event = e;
//		}
//
//		/* (non-Javadoc)
//		 * @see com.laxcus.util.display.SwingEvent#process()
//		 */
//		@Override
//		public void process() {
//			__exchangeHTML();
//		}
//	}

//	class UpdateHTMLThread extends SwingEvent {
//
//		UpdateHTMLThread() {
//			super();
//		}
//
//		/* (non-Javadoc)
//		 * @see com.laxcus.util.display.SwingEvent#process()
//		 */
//		@Override
//		public void process() {
//			__exchangeHTML();
//		}
//	}


///**
// * 搜索和显示数据
// */
//private void search() {
//	String command = txtSearch.getText();
//	command = command.trim();
//	if (command.isEmpty()) {
//		showEmpty();
//		return;
//	}
//
//	// 过滤掉*号
//	boolean really = true;
//	final String HELP_SUFFIX = "^\\s*(?:[\\\\*]*)([^\\\\*]+)\\s*([\\\\*]+)\\s*$"; 
//	Pattern pattern = Pattern.compile(HELP_SUFFIX);
//	Matcher matcher = pattern.matcher(command);
//	if (matcher.matches()) {
//		really = false;
//		command = matcher.group(1);
//	}
//
//	// 查找关键字
//	if (really) {
//		CommentElement element = context.findComment(command);
//		if (element != null) {
//			String text = context.formatHTML(element);
//			htmlPanel.setText(text);
//			showBottom(element.getCommand());
//			return;
//		}
//	}
//
//	// 部分匹配的命令
//	List<CommentElement> elements = context.findAllComments(command);
//	if (elements.size() > 0) {
//		String text = context.formatHTMLCommands(elements);
//		htmlPanel.setText(text);
//		return;
//	}
//
//	showEmpty();
//}


//class SearchThread extends SwingEvent {
//
//	ActionEvent event;
//
//	SearchThread(ActionEvent e) {
//		super();
//		event = e;
//	}
//
//	public void process() {
//		if (event.getSource() == txtSearch || event.getSource() == cmdSearch) {
//			search();
//		}
//	}
//}


//class FindThread extends SwingEvent {
//	String command;
//
//	FindThread(String str) {
//		super();
//		command = str;
//	}
//
//	public void process() {
//		doFind(command);
//	}
//}

///*
// * (non-Javadoc)
// * @see javax.swing.JLabel#updateUI()
// */
//@Override
//public void updateUI() {
//	// 更新界面
//	super.updateUI();
//	
//	// 更新
//	if (renderer != null) {
//		renderer.updateUI();
//	}
//	
////	if (htmlPanel != null) {
////		htmlPanel.setBorder(new EmptyBorder(1,1,1,1));
////	}
//
//	// 更新参数
//	if (htmlPanel != null && tree != null) {
//		//			// 更新界面字体
//		//			addThread(new UpdateHTMLFontThread());
//		//			// 调整背景
//		//			addThread(new UpdateHTMLThread());
//
//		// 更新界面字体和调整背景
//		addThread(new UpdateUIThread());
//	}
//}
