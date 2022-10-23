/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.laxcus.access.schema.*;
import com.laxcus.front.terminal.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.naming.*;

/**
 * 用户网络资源浏览面板。显示的内容包括数据库、表、阶段命名三种。
 * 因为SWING是线程不安全的，数据的写/读操作被放入SWING事件队列中执行。
 * 
 * @author scott.liang
 * @version 1.2 7/13/2015
 * @since laxcus 1.0
 */
public class TerminalRemoteDataListPanel extends JPanel implements /* ActionListener,*/ TreeSelectionListener {

	private static final long serialVersionUID = 570252287832258830L;

	private TerminalRemoteDataPanel master;

	/**
	 * 设置父类面板
	 * @param e
	 */
	public void setParentPanel(TerminalRemoteDataPanel e) {
		master = e;
	}

	/**
	 * 返回父类面板
	 * @return
	 */
	public TerminalRemoteDataPanel getParentPanel() {
		return master;
	}

	/** 数据库配置 **/
	private Map<Fame, Schema> schemas = new TreeMap<Fame, Schema>();

	/** 阶段命名配置 **/
	private Set<Phase> phases = new TreeSet<Phase>();

	/** 被授权单元 **/
	private Set<PassiveItem> passiveItems = new TreeSet<PassiveItem>();

	/** 根地址 **/
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

	/** 渲染器 **/
	private TerminalRemoteTreeCellRenderer renderer;

	/** 树模型 **/
	private DefaultTreeModel model;

	/** 树框架 **/
	private JTree tree;

	/**
	 * 构造默认的用户网络资源浏览面板
	 */
	public TerminalRemoteDataListPanel() {
		super();
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 返回字体
	 * @return
	 */
	public Font getSelectFont() {
		return tree.getFont();
	}

	/**
	 * 设置字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

	/**
	 * 修正字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			tree.setFont(font);
		}
	}

	class FontThread extends SwingEvent {
		Font font;

		FontThread(Font e) {
			super();
			font = e;
		}

		public void process() {
			__exchangeFont(font);
		}
	}

	/**
	 * 建立显示面板
	 */
	public void init() {		
		// 保存子节点
		root.setAllowsChildren(true);
		// 树模型
		model = new DefaultTreeModel(root);
		// 树型框架
		tree = new JTree(model);
		// 渲染器
		renderer = new TerminalRemoteTreeCellRenderer();
		tree.setCellRenderer(renderer);

		String title = TerminalLauncher.getInstance().findCaption("Window/BrowserDataListPanel/title");
		FontKit.setToolTipText(tree, title);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setBorder(new EmptyBorder(5, 3, 5, 3));
		tree.setRowHeight(-1);
		tree.setToggleClickCount(1);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		tree.setEditable(false);

		// 修正字体
		__exchangeFont(TerminalProperties.readRemoteDataFont());

		JScrollPane scroll = new JScrollPane(tree);
		FontKit.setToolTipText(scroll, title);
		setLayout(new BorderLayout(0, 0));
		add(scroll, BorderLayout.CENTER);
		setMinimumSize(new Dimension(160, 50));
	}

//	/* (non-Javadoc)
//	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//	 */
//	@Override
//	public void actionPerformed(ActionEvent e) {
//
//	}

	/**
	 * 执行线程的切换操作
	 * @param e
	 */
	private void __swatchTo(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		// 空指针，忽略它！
		if (path == null) {
			return;
		}
		Object source = path.getLastPathComponent();
		
		if (source.getClass() == TerminalTreeSchemaNode.class) {
			// 如果是数据库，忽略它！
		} else if (source.getClass() == TerminalTreeTableNode.class) {
			TerminalTreeTableNode sub = (TerminalTreeTableNode) source;
			Space space = sub.getSpace();
			getParentPanel().getDetailPanel().exchange(space);
		}
//		else if (source.getClass() == TerminalTreePhaseNode.class) {
//			TerminalTreePhaseNode sub = (TerminalTreePhaseNode) source;
//			Phase phase = sub.getPhase();
//			getParentPanel().getDetailPanel().exchange(phase);
//		}
		else if (source.getClass() == TerminalTreePassiveItemNode.class) {
			TerminalTreePassiveItemNode sub = (TerminalTreePassiveItemNode) source;
			PassiveItem item = sub.getPassiveItem();
			getParentPanel().getDetailPanel().exchange(item);
		} else {
			getParentPanel().getDetailPanel().clear();
		}
	}

	/**
	 * 切换选项线程
	 * @author scott.liang
	 * @version 1.0 3/30/2020
	 * @since laxcus 1.0
	 */
	class SwatchThread extends SwingEvent {

		TreeSelectionEvent event;

		SwatchThread(TreeSelectionEvent e) {
			super();
			event = e;
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			__swatchTo(event);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		addThread(new SwatchThread(e));
	}

	class ClearElementThread extends SwingEvent {
		ClearElementThread(){super();}
		public void process() {
			// 清除显示队列
			root.removeAllChildren();
			model.reload(root);
			// 清除记录
			schemas.clear();
			// 清除阶段命名
			phases.clear();
			// 清除共享表
			passiveItems.clear();
		}
	}

	class ClearAllSchemaThread extends SwingEvent {
		ClearAllSchemaThread() {
			super();
		}

		public void process() {
			// 清除显示队列
			TerminalTreeSchemaRootNode parent = getSchemaRootNode();
			if (parent == null) {
				return;
			}

			model.removeNodeFromParent(parent);
			// 清除记录
			schemas.clear();
		}
	}

	class CreateElementThread extends SwingEvent {
		Object element;

		CreateElementThread(Object e) {
			super();
			element = e;
		}

		public void process() {
			if (element.getClass() == Schema.class) {
				createSchema((Schema) element);
			} else if (element.getClass() == Table.class) {
				createTable((Table) element);
			} 
//			else if (element.getClass() == Phase.class) {
//				createPhase((Phase) element);
//			} 
			else if (element.getClass() == PassiveItem.class) {
				createPassiveItem((PassiveItem) element);
			}
		}
	}

	class DropElementThread extends SwingEvent {
		Object element;

		DropElementThread(Object e) {
			super();
			element = e;
		}

		public void process() {
			if (element.getClass() == Fame.class) {
				dropSchema((Fame) element);
			} else if (element.getClass() == Space.class) {
				dropTable((Space) element);
			} 
//			else if (element.getClass() == Phase.class) {
//				dropPhase((Phase) element);
//			} 
			else if (element.getClass() == PassiveItem.class) {
				dropPassiveItem((PassiveItem) element);
			}
		}
	}

	/**
	 * 删除全部配置
	 */
	public void clear() {
		ClearElementThread e = new ClearElementThread();
		addThread(e);
	}

	/**
	 * 删除全部数据
	 */
	public void removeAllSchema() {
		ClearAllSchemaThread e = new ClearAllSchemaThread();
		addThread(e);
	}

	private TerminalTreeSchemaRootNode getSchemaRootNode() {
		int count = root.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode node = root.getChildAt(index);
			if (node.getClass() == TerminalTreeSchemaRootNode.class) {
				return (TerminalTreeSchemaRootNode) node;
			}
		}
		return null;
	}

//	private TerminalTreePhaseRootNode getPhaseRootNode() {
//		int count = root.getChildCount();
//		for (int index = 0; index < count; index++) {
//			TreeNode node = root.getChildAt(index);
//			if (node.getClass() == TerminalTreePhaseRootNode.class) {
//				return (TerminalTreePhaseRootNode) node;
//			}
//		}
//		return null;
//	}

	//	/**
	//	 * 返回快捷组件根节点
	//	 * @return TerminalTreeSwiftRootNode实例
	//	 */
	//	private TerminalTreeSwiftRootNode getSwiftRootNode() {
	//		int count = root.getChildCount();
	//		for (int index = 0; index < count; index++) {
	//			TreeNode node = root.getChildAt(index);
	//			if (node.getClass() == TerminalTreeSwiftRootNode.class) {
	//				return (TerminalTreeSwiftRootNode) node;
	//			}
	//		}
	//		return null;
	//	}

	/**
	 * 返回共享表的根节点
	 * @return TerminalTreeCrossTableRootNode
	 */
	private TerminalTreePassiveItemRootNode getCrossTableRootNode() {
		int count = root.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode node = root.getChildAt(index);
			if (node.getClass() == TerminalTreePassiveItemRootNode.class) {
				return (TerminalTreePassiveItemRootNode) node;
			}
		}
		return null;
	}

	/**
	 * 建立根数据库节点
	 * @return 返回根数据库节点
	 */
	private TerminalTreeSchemaRootNode createSchemaRootNode() {
		TerminalTreeSchemaRootNode parent = getSchemaRootNode();
		if (parent == null) {
			int index = root.getChildCount();
			parent = new TerminalTreeSchemaRootNode();
			model.insertNodeInto(parent, root, index);
			if (index == 0) {
				model.reload(root);
			} else {
				model.nodeChanged(parent);
			}
		}

		return parent;
	}

//	/**
//	 * 建立阶段命名根节点
//	 * @return 返回阶段命名根节点
//	 */
//	private TerminalTreePhaseRootNode createPhaseRootNode() {
//		TerminalTreePhaseRootNode parent = getPhaseRootNode();
//		// 没有，建立一个新的根目录
//		if (parent == null) {
//			int index = root.getChildCount();
//			parent = new TerminalTreePhaseRootNode();
//			model.insertNodeInto(parent, root, index);
//			if (index == 0) {
//				model.reload(root);
//			} else {
//				model.nodeChanged(parent);
//			}
//		}
//
//		return parent;
//	}

	//	/**
	//	 * 建立快捷组件根节点
	//	 * @return 返回快捷组件根节点
	//	 */
	//	private TerminalTreeSwiftRootNode createSwiftRootNode() {
	//		TerminalTreeSwiftRootNode parent = getSwiftRootNode();
	//		// 没有，建立一个新的根目录
	//		if (parent == null) {
	//			int index = root.getChildCount();
	//			parent = new TerminalTreeSwiftRootNode();
	//			model.insertNodeInto(parent, root, index);
	//			if (index == 0) {
	//				model.reload(root);
	//			} else {
	//				model.nodeChanged(parent);
	//			}
	//		}
	//
	//		return parent;
	//	}

	/**
	 * 建立共享表根节点
	 * @return 返回共享表根节点
	 */
	private TerminalTreePassiveItemRootNode createCrossTableRootNode() {
		TerminalTreePassiveItemRootNode parent = getCrossTableRootNode();
		// 没有，建立一个新的根目录
		if (parent == null) {
			int index = root.getChildCount();
			parent = new TerminalTreePassiveItemRootNode();
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
	 * 查找指定数据库命的根节点
	 * @param title Fame实例
	 * @return 返回TerminalTreeSchemaNode实例
	 */
	private TerminalTreeSchemaNode getSchemaNode(Fame title) {
		TerminalTreeSchemaRootNode parent = createSchemaRootNode();
		int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			TreeNode node = parent.getChildAt(i);
			if (node.getClass() == TerminalTreeSchemaNode.class) {
				TerminalTreeSchemaNode real = (TerminalTreeSchemaNode) node;
				if (real.getFame().compareTo(title) == 0) {
					return real;
				}
			}
		}
		return null;
	}

	/**
	 * 在数据库根节点基础上，建立并且显示数据库 
	 * @param schema 数据库配置
	 */
	private void createSchema(Schema schema) {
		Fame fame = schema.getFame();
		if (schemas.containsKey(fame)) {
			return;
		}

		TerminalTreeSchemaRootNode parent = createSchemaRootNode();
		TerminalTreeSchemaNode child = new TerminalTreeSchemaNode(schema);

		//		int index = parent.getChildCount();
		//		model.insertNodeInto(child, parent, index);
		//		// 将某些 TreeNodes 插入节点之后，调用此方法
		//		model.nodesWereInserted(parent, new int[] { index });
		//		// 如果已修改此模型依赖的 TreeNode，则调用此方法
		//		model.reload(parent);

		// 加新节点
		model.insertNodeInto(child, parent, parent.getChildCount());
		model.nodeChanged(parent);

		schemas.put(fame, schema);
	}

	//	/**
	//	 * 在一个数据库基础上，建立表节点
	//	 * @param table Table实例
	//	 */
	//	private void createTable(Table table) {
	//		Space space = table.getSpace();
	//		Fame title = space.getSchema();
	//
	//		TerminalTreeSchemaNode parent = getSchemaNode(title);
	//		// 数据库不存在不处理，必须已经建立
	//		if (parent == null) {
	//			Logger.error(this, "createTable", "cannot find %s", title);
	//			return;
	//		}
	//
	//		Schema schema = schemas.get(title);
	//		// 已经存在不处理
	//		if (schema.contains(space)) {
	//			return;
	//		}
	//
	//		TerminalTreeTableNode child = new TerminalTreeTableNode( space.getTable() );
	//		int index = parent.getChildCount();
	//		model.insertNodeInto(child, parent, index);
	//		model.nodesWereInserted(parent, new int[] { index });
	//
	//		Collection<ColumnAttribute> values = table.list();
	//
	//		for (ColumnAttribute attribute : values) {
	//			TerminalTreeAttributeNode sub = new TerminalTreeAttributeNode(attribute);
	//			int seek = child.getChildCount();
	//			model.insertNodeInto(sub, child, seek);
	//			model.nodesWereInserted(child, new int[] { seek });
	//		}
	//
	//		// 如果已修改此模型依赖的 TreeNode，则调用此方法
	//		model.reload(parent);
	//
	//		// 保存表
	//		schema.add(table);
	//	}

//	/**
//	 * 在一个数据库基础上，建立表节点
//	 * @param table Table实例
//	 */
//	private void createTable(Table table) {
//		Space space = table.getSpace();
//		Fame title = space.getSchema();
//
//		TerminalTreeSchemaNode parent = getSchemaNode(title);
//		// 数据库不存在不处理，必须已经建立
//		if (parent == null) {
//			Logger.error(this, "createTable", "cannot find %s", title);
//			return;
//		}
//
//		Schema schema = schemas.get(title);
//		// 已经存在不处理
//		if (schema.contains(space)) {
//			return;
//		}
//
//		// 显示表名和更新
//		TerminalTreeTableNode child = new TerminalTreeTableNode(space);
//		model.insertNodeInto(child, parent, parent.getChildCount());
//		model.nodeChanged(parent);
//
//		// 显示属性
//		Collection<ColumnAttribute> values = table.list();
//		for (ColumnAttribute attribute : values) {
//			TerminalTreeAttributeNode sub = new TerminalTreeAttributeNode(attribute);
//			model.insertNodeInto(sub, child, child.getChildCount());
//			model.nodeChanged(child);
//		}
//
//		// 保存表
//		schema.add(table);
//	}

	/**
	 * 在一个数据库基础上，建立表节点
	 * @param table Table实例
	 */
	private void createTable(Table table) {
		Space space = table.getSpace();
		Fame fame = space.getSchema();

		TerminalTreeSchemaNode parent = getSchemaNode(fame);
		// 数据库不存在不处理，必须已经建立
		if (parent == null) {
			Logger.error(this, "createTable", "cannot be find %s", fame);
			return;
		}

		Schema schema = schemas.get(fame);
		// 已经存在不处理
		if (schema.contains(space)) {
			return;
		}

		// 显示表名和更新
		TerminalTreeTableNode child = new TerminalTreeTableNode(space);
		model.insertNodeInto(child, parent, parent.getChildCount());
		model.nodeChanged(parent);

//		// 显示属性
//		Collection<ColumnAttribute> values = table.list();
//		for (ColumnAttribute attribute : values) {
//			TerminalTreeAttributeNode sub = new TerminalTreeAttributeNode(attribute);
//			model.insertNodeInto(sub, child, child.getChildCount());
//			model.nodeChanged(child);
//		}
		
		model.nodeChanged(child);

		// 保存表
		schema.add(table);
	}

	
	/**
	 * 删除数据库
	 * @param fame Fame实例
	 */
	private void dropSchema(Fame fame) {
		// 判断存在
		if (!schemas.containsKey(fame)) {
			return;
		}

		TerminalTreeSchemaRootNode schemaRoot = getSchemaRootNode();
		if (schemaRoot == null) {
			return;
		}

		int count = schemaRoot.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode node = schemaRoot.getChildAt(index);
			// 必须匹配
			if (node.getClass() != TerminalTreeSchemaNode.class) {
				continue;
			}

			TerminalTreeSchemaNode child = (TerminalTreeSchemaNode) node;
			if (child.getFame().compareTo(fame) == 0) {
				// 删除树节点
				schemaRoot.remove(child);
				// 更新图形界面
				model.nodesWereRemoved(schemaRoot, new int[] { index }, new Object[] { child });
				break;
			}
		}

		// 如果是空集合，这个节点
		if (schemaRoot.getChildCount() == 0) {
			model.removeNodeFromParent(schemaRoot);
		} else {
			model.reload(schemaRoot); // 如果已修改此模型依赖的 TreeNode，则调用此方法
		}

		// 删除
		schemas.remove(fame);
	}

	/**
	 * 删除表
	 * @param space Space实例
	 */
	private void dropTable(Space space) {
		Schema schema = schemas.get(space.getSchema());
		if(schema == null) {
			return;
		}

		TerminalTreeSchemaNode parent = getSchemaNode(space.getSchema());
		if (parent == null) {
			return;
		}

		int count = parent.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode node = parent.getChildAt(index);
			// 必须匹配
			if (node.getClass() != TerminalTreeTableNode.class) {
				continue;
			}

			TerminalTreeTableNode child = (TerminalTreeTableNode) node;
			if (child.getTableName().compareTo(space.getTable()) == 0) {
				// 删除树节点
				parent.remove(child);
				// 更新图形界面
				model.nodesWereRemoved(parent, new int[] { index }, new Object[] { child });
				break;
			}
		}

		// 刷新
		model.reload(parent);

		// 删除数据库记录
		schema.remove(space);
		
		// 清除显示
		TerminalRemoteDataDetailPanel panel = master.getDetailPanel();
		boolean match = panel.isAttachObject(space);
		if (match) {
			panel.clear();
		}
	}

	
	/**
	 * 在被授权单元根节点基础上，建立子级的被授权单元
	 * @param item PassiveItem实例
	 */
	private void createPassiveItem(PassiveItem item) {
		// 判断和找到已经存在的被授权单元
		PassiveItem old = null;
		for (PassiveItem e : passiveItems) {
			boolean success = (Laxkit.compareTo(e.getAuthorizer(), item.getAuthorizer()) == 0 && 
					Laxkit.compareTo(e.getSpace(), item.getSpace()) == 0);
			// 找到匹配退出!
			if (success) {
				old = e;
				break;
			}
		}

		TerminalTreePassiveItemRootNode parent = createCrossTableRootNode();
		if (parent == null) {
			return;
		}
		
		// 更新或者追加
		if (old != null) {
			int count = parent.getChildCount();
			for (int index = 0; index < count; index++) {
				TreeNode node = parent.getChildAt(index);
				// 必须匹配
				if (node.getClass() != TerminalTreePassiveItemNode.class) {
					continue;
				}
				TerminalTreePassiveItemNode child = (TerminalTreePassiveItemNode) node;
				PassiveItem e = child.getPassiveItem();
				boolean success = (Laxkit.compareTo(e.getAuthorizer(), item.getAuthorizer()) ==0 && 
						Laxkit.compareTo(e.getSpace(), item.getSpace())==0);
				// 更新授权单元
				if(success){
					// 删除旧的，增加新的，完成替换操作
					passiveItems.remove(old);
					passiveItems.add(item);
					// 更新授权单元
					child.setPassiveItem(item);
					getParentPanel().getDetailPanel().update(item);
					break;
				}
			}
		} else {
			TerminalTreePassiveItemNode child = new TerminalTreePassiveItemNode(item);
			// 加新的节点
			model.insertNodeInto(child, parent, parent.getChildCount());
			model.nodeChanged(parent);
			passiveItems.add(item);
		}
	}

	/**
	 * 删除被授权单元
	 * @param item PassiveItem实例
	 */
	private void dropPassiveItem(PassiveItem item) {
		// 不存在，退出
		if (!passiveItems.contains(item)) {
			return;
		}

		// 找到阶段命名根节点
		TerminalTreePassiveItemRootNode tableRoot = getCrossTableRootNode();
		if (tableRoot == null) {
			return;
		}

		int count = tableRoot.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode node = tableRoot.getChildAt(index);
			// 必须匹配
			if (node.getClass() != TerminalTreePassiveItemNode.class) {
				continue;
			}
			TerminalTreePassiveItemNode child = (TerminalTreePassiveItemNode) node;
			if (child.getPassiveItem().compareTo(item) == 0) {
				// 删除树节点
				tableRoot.remove(child);
				// 更新图形界面
				model.nodesWereRemoved(tableRoot, new int[] { index }, new Object[] { child });
				break;
			}
		}

		// 如果是空集合，这个节点
		if (tableRoot.getChildCount() == 0) {
			model.removeNodeFromParent(tableRoot);
		} else {
			model.reload(tableRoot);
		}

		// 删除阶段命名
		passiveItems.remove(item);
		
		// 如果匹配，清除关联
		TerminalRemoteDataDetailPanel panel = master.getDetailPanel();
		boolean match = panel.isAttachObject(item);
		if (match) {
			panel.clear();
		}
	}

	/**
	 * 把数据库放入SWING事件队列，在窗口上增加一个数据库显示
	 * @param schema Schema实例
	 */
	public void addSchema(Schema schema) {
		Schema clone = schema.duplicate();

		// 删除可能存在的表
		List<Table> tables = clone.list();
		for (Table table : tables) {
			clone.remove(table.getSpace());
		}

		CreateElementThread e = new CreateElementThread(clone);
		addThread(e);

		// 保存表
		for (Table table : tables) {
			addTable(table);
		}
	}

	/**
	 * 把数据表放入SWING事件队列，在窗口上增加一个数据表显示
	 * @param table Table实例
	 */
	public void addTable(Table table) {
		Table sub = table.duplicate();
		CreateElementThread e = new CreateElementThread(sub);
		addThread(e);
	}

//	/**
//	 * 把阶段命名放入SWING事件队列，在窗口上增加一个阶段命名显示
//	 * @param phase Phase实例
//	 */
//	public void addPhase(Phase phase) {
//		CreateElementThread e = new CreateElementThread(phase);
//		addThread(e);
//	}

	/**
	 * 增加被授权单元
	 * @param item PassiveItem实例
	 */
	public void addPassiveItem(PassiveItem item) {
		CreateElementThread e = new CreateElementThread(item);
		addThread(e);
	}

	/**
	 * 把数据库命名放入SWING事件队列，删除窗口上的数据库
	 * @param fame 数据库名称
	 */
	public void removeSchema(Fame fame) {
		DropElementThread e = new DropElementThread(fame);
		addThread(e);
	}

	/**
	 * 把表名放入SWING事件队列，删除窗口上的表
	 * @param space Space实例
	 */
	public void removeTable(Space space) {
		DropElementThread e = new DropElementThread(space);
		addThread(e);
	}


	/**
	 * 删除被授权单元
	 * @param item PassiveItem实例
	 */
	public void removePassiveItem(PassiveItem item) {
		DropElementThread e = new DropElementThread(item);
		addThread(e);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();
	}
}