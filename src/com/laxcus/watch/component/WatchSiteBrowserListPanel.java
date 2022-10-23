/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;
import com.laxcus.watch.*;
import com.laxcus.watch.pool.*;

/**
 * 分布站点浏览面板。在窗口的左侧。
 * 因为SWING组件是线程不安全的，所有关于JTREE的“添加、删除、释放”操作，放在SWING事件队列里执行。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2012
 * @since laxcus 1.0
 */
public class WatchSiteBrowserListPanel extends JPanel implements TreeSelectionListener {

	private static final long serialVersionUID = 8958854364644337263L;

	/**
	 * 插入的两种方案，都能正常显示，区别：第一种如果有焦点时，焦点会消失；第二种保持原样不变，这个很重要！
	 * 
	 * 第一种，三行代码
	 * int index = parent.getChildCount();
	 * model.insertNodeInto(child, parent, index);
	 * 将某些 TreeNodes 插入节点之后，调用此方法
	 * model.nodesWereInserted(parent, new int[] { index });
	 * 如果已修改此模型依赖的 TreeNode，则调用此方法
	 * model.reload(parent);
	 * 
	 * 
	 * 第二种，两行代码
	 * 插入后更新
	 * 
	 * model.insertNodeInto(child, parent, parent.getChildCount());
	 * model.nodeChanged(parent);
	 **/

	/** 站点根目录 **/
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(); 

	/** 渲染器 **/
	private WatchSiteBrowserListCellRenderer renderer;

	/** 次层树模型 **/
	private DefaultTreeModel model;

	/** 站点结构 **/
	private JTree tree;

	/** 记录节点地址 **/
	//	private TreeSet<Node> sites = new TreeSet<Node>();

	/** 浏览器任务 **/
	private BrowerTask browerTask = new BrowerTask();

	private WatchSiteBrowserPanel parnet;

	/**
	 * 构造默认的注册站点地址面板
	 */
	public WatchSiteBrowserListPanel() {
		super();
	}

	/**
	 * 设置面板
	 * @param e
	 */
	public void setParntPanel(WatchSiteBrowserPanel e) {
		parnet = e;
	}

	public WatchSiteBrowserPanel getParentPanel() {
		return parnet;
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 初始化面板
	 */
	public void init() {
		Timer timer = WatchLauncher.getInstance().getTimer();
		timer.schedule(browerTask, 0, 2000); // 2秒钟触发一次

		// 允许保存子节点
		root.setAllowsChildren(true);
		// 建立树模型
		model = new DefaultTreeModel(root);
		// 建立树型结构
		tree = new JTree(model);

		// 获得标题
		String title = WatchLauncher.getInstance().findCaption("Window/SiteBrowser/title");
		FontKit.setToolTipText(tree, title);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setBorder(new EmptyBorder(5, 3, 5, 3));
		tree.setRowHeight(-1);
		tree.setToggleClickCount(1);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		tree.setEditable(false);

		// 渲染器
		renderer = new WatchSiteBrowserListCellRenderer();
		tree.setCellRenderer(renderer);

		// 修改窗口字体
		__exchangeFont(WatchProperties.readBrowserSiteFont());

		JScrollPane scroll = new JScrollPane(tree);
		FontKit.setToolTipText(scroll, title);
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);
		setMinimumSize(new Dimension(130, 50));
	}

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
		if (source.getClass() == WatchSiteBrowserAddressTreeNode.class) {
			WatchSiteBrowserAddressTreeNode tn = (WatchSiteBrowserAddressTreeNode) source;
			Node node = tn.getNode();
			// 找到对象
			SiteRuntime runtime = SiteRuntimeBasket.getInstance().findRuntime(node);
			if (runtime != null) {
				getParentPanel().getDetailPanel().exchange(runtime);
			}
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
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		addThread(new SwatchThread(e));
	}

	/**
	 * 清除全部参数
	 * 
	 */
	class ClearSiteThread extends SwingEvent {
		ClearSiteThread() {
			super();
		}

		public void process() {
			// 清除任务队列中的所有
			browerTask.clear();

			root.removeAllChildren();
			model.reload(root);
			//			sites.clear();
		}
	}

	/**
	 * 清除全部旧的记录
	 */
	public void clear() {
		addThread(new ClearSiteThread());
	}

	/**
	 * 查找站点的类型树节点
	 * @param node
	 * @return
	 */
	private WatchSiteBrowserFamilyTreeNode findSiteFamilyNode(Node node) {
		int count = root.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode element = root.getChildAt(index);
			if (element.getClass() == WatchSiteBrowserFamilyTreeNode.class) {
				WatchSiteBrowserFamilyTreeNode that = (WatchSiteBrowserFamilyTreeNode) element;
				if (that.getFamily() == node.getFamily()) {
					return that;
				}
			}
		}
		return null;
	}

	/**
	 * 建立站点的类型树节点
	 * @param node
	 * @return
	 */
	private WatchSiteBrowserFamilyTreeNode createSiteFamilyNode(Node node) {
		WatchSiteBrowserFamilyTreeNode parent = findSiteFamilyNode(node);

		if (parent == null) {
			int index = root.getChildCount();
			parent = new WatchSiteBrowserFamilyTreeNode(node.getFamily());
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
	 * 添加节点
	 * @param node
	 */
	private void add(Node node) {
		//		// 如果节点存在，不再记录
		//		if (sites.contains(node)) {
		//			return;
		//		}

		WatchSiteBrowserFamilyTreeNode parent = createSiteFamilyNode(node);
		WatchSiteBrowserAddressTreeNode child = new WatchSiteBrowserAddressTreeNode(node);


		//		int index = parent.getChildCount();
		//		model.insertNodeInto(child, parent, index);
		//		// 将某些 TreeNodes 插入节点之后，调用此方法
		//		model.nodesWereInserted(parent, new int[] { index });
		//		// 如果已修改此模型依赖的 TreeNode，则调用此方法
		//		model.reload(parent);


		// 插入后更新
		model.insertNodeInto(child, parent, parent.getChildCount());
		model.nodeChanged(parent);

		//		// 保存到站点地址集合
		//		sites.add(node);
	}

	/**
	 * 删除节点
	 * @param node
	 */
	private void remove(Node node) {
		//		// 节点不存在，退出！
		//		if (!sites.contains(node)) {
		//			return;
		//		}

		// 找到类型树节点
		WatchSiteBrowserFamilyTreeNode parent = findSiteFamilyNode(node);
		if (parent == null) {
			return;
		}

		int count = parent.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode element = parent.getChildAt(index);
			if (element.getClass() != WatchSiteBrowserAddressTreeNode.class) {
				continue;
			}
			WatchSiteBrowserAddressTreeNode child = (WatchSiteBrowserAddressTreeNode) element;
			if (child.getNode().compareTo(node) == 0) {
				parent.remove(child);
				model.nodesWereRemoved(parent, new int[] { index }, new Object[] { child });
				model.reload(parent);
				break;
			}
		}

		// 如果是空集合，删除这个类型
		if (parent.getChildCount() == 0) {
			model.removeNodeFromParent(parent);
		}

		//		// 删除记录
		//		sites.remove(node);
	}

	/**
	 * 推送一个新的注册站点
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean pushSite(Node node) {
		//		// 如果节点存在，不再记录
		//		if (sites.contains(node)) {
		//			return false;
		//		}

		// 推入队列，暂时保存，定时触发，批量处理
		return browerTask.push(node);
	}

	/**
	 * 正常删除一个注册站点
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean dropSite(Node node) {
		//		// 节点不存在，退出！
		//		if (!sites.contains(node)) {
		//			return false;
		//		}

		// 清除。暂时保存，定时触发，批量处理
		return browerTask.drop(node);
	}

	/**
	 * 以故障状态删除一个注册站点
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean destroy(Node node) {
		return dropSite(node);
	}

	/**
	 * 返回当前字体
	 * @return
	 */
	public Font getSelectFont() {
		return tree.getFont();
	}

	/**
	 * 设置当前字体
	 * @param e
	 */
	public void setSelectFont(Font e) {
		addThread(new FontThread(e));
	}

	/**
	 * 切换字体
	 * @param font
	 */
	private void __exchangeFont(Font font) {
		if (font != null) {
			tree.setFont(font);
		}
	}

	/**
	 * 字体线程
	 *
	 * @author scott.liang
	 * @version 1.0 8/28/2018
	 * @since laxcus 1.0
	 */
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
	 * 增加一批站点
	 */
	class PushSiteThread extends SwingEvent {

		// 站点地址
		ArrayList<Node> array = new ArrayList<Node>();

		public PushSiteThread() {
			super();
		}

		public void addElement(Node e) {
			array.add(e);
		}

		public void addElements(Collection<Node> a) {
			array.addAll(a);
		}

		public int size() {
			return array.size();
		}

		public void process() {
			for (Node e : array) {
				add(e);
			}
		}
	}

	class DropSiteThread extends SwingEvent {
		// 站点地址
		ArrayList<Node> array = new ArrayList<Node>();

		public DropSiteThread() {
			super();
		}

		public void addAll(Collection<Node> a) {
			array.addAll(a);
		}

		public int size() {
			return array.size();
		}

		public void process() {
			for (Node e : array) {
				remove(e);
			}
		}
	}

	class BrowerTask extends TimerTask {

		SingleLock lock = new SingleLock();

		/** 推送进来 **/
		TreeSet<Node> pushs = new TreeSet<Node>();

		/** 释放出去 **/
		TreeSet<Node> drops = new TreeSet<Node>();

		/**
		 * 清除全部记录
		 */
		public void clear() {
			lock.lock();
			try {
				pushs.clear();
				drops.clear();
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}

		/**
		 * 保存一个节点
		 * @param node 节点
		 * @return 返回真或者假
		 */
		public boolean push(Node node) {
			lock.lock();
			try {
				// 不存在，保存！
				if (!pushs.contains(node)) {
					return pushs.add(node);
				}
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
			return false;
		}

		/**
		 * 销毁一个节点
		 * @param node 节点
		 * @return 返回真或者假
		 */
		public boolean drop(Node node) {
			lock.lock();
			try {
				// 不存在，保存！
				if (!drops.contains(node)) {
					return drops.add(node);
				}
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			// 没有，忽略它！
			if (pushs.isEmpty() && drops.isEmpty()) {
				return;
			}

			PushSiteThread pushThread = new PushSiteThread();
			DropSiteThread dropThread = new DropSiteThread();

			// 锁定
			lock.lock();
			try {
				if (pushs.size() > 0) {
					pushThread.addElements(pushs);
					pushs.clear();
				}
				if (drops.size() > 0) {
					dropThread.addAll(drops);
					drops.clear();
				}
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}

			// 推入SWING线程队列
			if (pushThread.size() > 0) {
				addThread(pushThread);
			}
			if (dropThread.size() > 0) {
				addThread(dropThread);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		// 重新绘制
		if (renderer != null) {
			renderer.updateUI();
		}
		super.updateUI();
	}

}