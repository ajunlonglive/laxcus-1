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

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.lock.*;
import com.laxcus.watch.*;
import com.laxcus.watch.pool.*;

/**
 * 用户层级浏览窗口
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public class WatchMemberBrowserListPanel extends JPanel implements /*ActionListener,*/ TreeSelectionListener {

	private static final long serialVersionUID = -4433693387540638587L;

	/** 渲染器 **/
	private WatchMemberBrowserListCellRenderer renderer;

	/** 用户根目录 **/
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(); 

	/** 注册成员根节点 **/
	private WatchSiteMemberRootTreeNode registerRoot;

	/** 在线成员根节点 **/
	private WatchSiteMemberRootTreeNode onlineRoot;

	/** 次层树模型 **/
	private DefaultTreeModel model;

	/** 站点结构 **/
	private JTree tree;

	/** 浏览器任务 **/
	private BrowerTask browerTask = new BrowerTask();

	/** 用户浏览窗口 **/
	private WatchMemberBrowserPanel parnet;

	/**
	 * 构造用户层级浏览窗口
	 */
	public WatchMemberBrowserListPanel() {
		super();
	}

	/**
	 * 加入线程
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 设置父类面板
	 * @param e WatchMemberBrowserPanel实例
	 */
	public void setParntPanel(WatchMemberBrowserPanel e) {
		parnet = e;
	}

	/**
	 * 返回父类面板
	 * @return WatchMemberBrowserPanel实例
	 */
	public WatchMemberBrowserPanel getParentPanel() {
		return parnet;
	}

	/**
	 * 切换到指定的节点
	 * @param e
	 */
	private void __swatchTo(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		// 空指针，忽略它！
		if (path == null) {
			return;
		}

		Object source = path.getLastPathComponent();
		if (source.getClass() == WatchSiteMemberTreeNode.class) {
			WatchSiteMemberTreeNode sm = (WatchSiteMemberTreeNode) source;
			Siger siger = sm.getSiger();
			getParentPanel().getDetailPanel().exchange(siger);
		} else if (source.getClass() == WatchSiteMemberRootTreeNode.class) {
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
	 * 修正字体
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
	 * 保存根
	 * @param sub
	 */
	private void addRootNode(WatchSiteMemberRootTreeNode sub) {
		int index = root.getChildCount();
		model.insertNodeInto(sub, root, index);

		if (index == 0) {
			model.reload(root);
		} else {
			model.nodeChanged(sub);
		}
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
		// 指定树单元格类，这个参数必须有！
		renderer = new WatchMemberBrowserListCellRenderer();
		tree.setCellRenderer(renderer);

		// 获得标题
		String title = WatchLauncher.getInstance().findCaption("Window/MemberBrowserListPanel/title");
		FontKit.setToolTipText(tree, title);

		// EmptyBorder(int top, int left, int bottom, int right)

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setBorder(new EmptyBorder(5, 3, 5, 3));
		tree.setRowHeight(-1);
		tree.setToggleClickCount(1);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(this);
		tree.setEditable(false);

		// 修改字体
		__exchangeFont(WatchProperties.readBrowserMemberFont());

		// 滚动栏
		JScrollPane scroll = new JScrollPane(tree);
		FontKit.setToolTipText(scroll, title);
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);
		setMinimumSize(new Dimension(130, 50));

		// 注册成员/在线成员
		String str = WatchLauncher.getInstance().findCaption("Window/MemberBrowserListPanel/Register/title");
		registerRoot = new WatchSiteMemberRootTreeNode(WatchSiteMemberRootTreeNode.REGISTER_MEMBER, str);
		str = WatchLauncher.getInstance().findCaption("Window/MemberBrowserListPanel/Online/title");
		onlineRoot = new WatchSiteMemberRootTreeNode(WatchSiteMemberRootTreeNode.ONLINE_MEMBER, str);
		// 保存和显示它们
		addRootNode(registerRoot);
		addRootNode(onlineRoot);
	}

	/**
	 * 清除全部参数
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class ClearMemberThread extends SwingEvent {
		ClearMemberThread() { super(); }
		public void process() {
			// 清除任务队列中的所有
			browerTask.clear();

			// 删除界面上的全部
			registerRoot.removeAllChildren();
			onlineRoot.removeAllChildren();
			model.reload(root);
		}
	}

	/**
	 * 推送新的注册成员到显示界面
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class PushRegisterMemberThread extends SwingEvent {

		ArrayList<WatchSiteMemberTreeNode> array = new ArrayList<WatchSiteMemberTreeNode>();

		PushRegisterMemberThread(Collection<WatchSiteMemberTreeNode> a) {
			super();
			array.addAll(a);
		}

		public void process() {
			for (WatchSiteMemberTreeNode e : array) {
				__addRegisterMember(e);
			}
		}
	}

	/**
	 * 从显示界面删除注册成员
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class DropRegisterMemberThread extends SwingEvent {
		ArrayList<Siger> array = new ArrayList<Siger>();

		DropRegisterMemberThread(Collection<Siger> a) {
			super();
			array.addAll(a);
		}

		public void process() {
			for(Siger e : array) {
				__removeRegisterMember(e);
			}
		}
	}

	/**
	 * 推送新的在线成员到显示界面
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class PushOnlineMemberThread extends SwingEvent {

		ArrayList<WatchSiteMemberTreeNode> array = new ArrayList<WatchSiteMemberTreeNode>();

		PushOnlineMemberThread(Collection<WatchSiteMemberTreeNode> a) {
			super();
			array.addAll(a);
		}

		public void process() {
			for (WatchSiteMemberTreeNode e : array) {
				__addOnlineMember(e);
			}
		}
	}

	/**
	 * 从显示界面删除在线成员
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class DropOnlineMemberThread extends SwingEvent {
		ArrayList<Siger> array = new ArrayList<Siger>();

		DropOnlineMemberThread(Collection<Siger> a) {
			super();
			array.addAll(a);
		}

		public void process() {
			for(Siger e : array) {
				__removeOnlineMember(e);
			}
		}
	}

	/**
	 * 增加新的注册成员
	 * @param child 子例
	 */
	void __addRegisterMember(WatchSiteMemberTreeNode child) {
		// 插入后更新
		model.insertNodeInto(child, registerRoot, registerRoot.getChildCount());
		model.nodeChanged(registerRoot);		
	}

	/**
	 * 删除注册成员
	 * @param siger 用户签名
	 */
	void __removeRegisterMember(Siger siger) {
		int count = registerRoot.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode element = registerRoot.getChildAt(index);
			if (element.getClass() != WatchSiteMemberTreeNode.class) {
				continue;
			}
			// 判断一致，删除它！
			WatchSiteMemberTreeNode child = (WatchSiteMemberTreeNode) element;
			if (Laxkit.compareTo(child.getSiger(), siger) == 0) {
				registerRoot.remove(child);
				model.nodesWereRemoved(registerRoot, new int[] { index }, new Object[] { child });
				model.reload(registerRoot);
				break;
			}
		}
	}

	/**
	 * 增加新的在线成员
	 * @param child 子例
	 */
	void __addOnlineMember(WatchSiteMemberTreeNode child) {
		// 插入后更新
		model.insertNodeInto(child, onlineRoot, onlineRoot.getChildCount());
		model.nodeChanged(onlineRoot);		
	}

	/**
	 * 删除在线成员
	 * @param siger 用户签名
	 */
	void __removeOnlineMember(Siger siger) {
		int count = onlineRoot.getChildCount();
		for (int index = 0; index < count; index++) {
			TreeNode element = onlineRoot.getChildAt(index);
			if (element.getClass() != WatchSiteMemberTreeNode.class) {
				continue;
			}
			// 判断一致，删除它！
			WatchSiteMemberTreeNode child = (WatchSiteMemberTreeNode) element;
			if (Laxkit.compareTo(child.getSiger(), siger) == 0) {
				onlineRoot.remove(child);
				model.nodesWereRemoved(onlineRoot, new int[] { index }, new Object[] { child });
				model.reload(onlineRoot);
				break;
			}
		}
	}

	/**
	 * 推送/删除时间调用器
	 *
	 * @author scott.liang
	 * @version 1.0 1/13/2020
	 * @since laxcus 1.0
	 */
	class BrowerTask extends TimerTask {

		SingleLock lock = new SingleLock();

		/** 增加注册成员 **/
		TreeSet<Siger> pushRegisters = new TreeSet<Siger>();

		/** 删除注册成员 **/
		TreeSet<Siger> dropRegisters = new TreeSet<Siger>();

		/** 增加在线成员 **/
		TreeSet<Siger> pushOnlines = new TreeSet<Siger>();

		/** 删除在线成员 **/
		TreeSet<Siger> dropOnlines = new TreeSet<Siger>();

		/**
		 * 清除全部记录
		 */
		public void clear() {
			lock.lock();
			try {
				pushRegisters.clear();
				dropRegisters.clear();
				pushOnlines.clear();
				dropOnlines.clear();
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}

		/**
		 * 保存注册成员
		 * @param siger
		 */
		public void __pushRegister(Siger siger) {
			lock.lock();
			try {
				pushRegisters.add(siger);
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}

		private void doPushRegister() {
			ArrayList<WatchSiteMemberTreeNode> array = new ArrayList<WatchSiteMemberTreeNode>();

			lock.lock();
			try {
				for(Siger siger : pushRegisters) {
					WatchSiteMemberTreeNode e = new WatchSiteMemberTreeNode(WatchSiteMemberRootTreeNode.REGISTER_MEMBER, siger );
					String plainText = RegisterMemberBasket.getInstance().findPlainText(siger);
					if(plainText != null) {
						e.setPlainText(plainText);
					}
					array.add(e);
				}
				// 清除全部
				pushRegisters.clear();
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}

			if (array.size() > 0) {
				PushRegisterMemberThread e = new PushRegisterMemberThread(array);
				addThread(e);
			}
		}

		private void doDropRegister() {
			ArrayList<Siger> array = new ArrayList<Siger>();

			lock.lock();
			try {
				if (dropRegisters.size() > 0) {
					array.addAll(dropRegisters);
					// 清除全部
					dropRegisters.clear();
				}
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}

			if (array.size() > 0) {
				DropRegisterMemberThread e = new DropRegisterMemberThread(array);
				addThread(e);
			}
		}

		private void doPushOnline() {
			ArrayList<WatchSiteMemberTreeNode> array = new ArrayList<WatchSiteMemberTreeNode>();

			lock.lock();
			try {
				for(Siger siger : pushOnlines) {
					WatchSiteMemberTreeNode e = new WatchSiteMemberTreeNode(WatchSiteMemberRootTreeNode.ONLINE_MEMBER, siger );
					String plainText = RegisterMemberBasket.getInstance().findPlainText(siger);
					if(plainText != null) {
						e.setPlainText(plainText);
					}
					array.add(e);
				}
				// 清除全部
				pushOnlines.clear();
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}

			if (array.size() > 0) {
				PushOnlineMemberThread e = new PushOnlineMemberThread(array);
				addThread(e);
			}
		}

		private void doDropOnline() {
			ArrayList<Siger> array = new ArrayList<Siger>();

			// 锁定！
			lock.lock();
			try {
				if (dropOnlines.size() > 0) {
					array.addAll(dropOnlines);
					// 清除全部
					dropOnlines.clear();
				}
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}

			if (array.size() > 0) {
				DropOnlineMemberThread e = new DropOnlineMemberThread(array);
				addThread(e);
			}
		}

		public void __dropRegister(Siger siger) {
			lock.lock();
			try {
				dropRegisters.add(siger);
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}

		public void __pushOnline(Siger siger) {
			lock.lock();
			try {
				pushOnlines.add(siger);
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}

		public void __dropOnline(Siger siger) {
			lock.lock();
			try {
				dropOnlines.add(siger);
			} catch (Throwable e) {

			} finally {
				lock.unlock();
			}
		}

		/**
		 * 判断是空
		 * @return
		 */
		public boolean isEmpty() {
			return pushRegisters.isEmpty() && dropRegisters.isEmpty()
			&& pushOnlines.isEmpty() && dropOnlines.isEmpty();
		}

		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			doPushRegister();
			doDropRegister();
			doPushOnline();
			doDropOnline();
		}
	}

	/**
	 * 推送新的注册成员，来自ACCOUNT/CALL节点
	 * @param siger 用户签名
	 */
	public void pushRegisterMember(Siger siger) {
		browerTask.__pushRegister(siger);
	}

	/**
	 * 删除注册成员，来自ACCOUNT/CALL节点
	 * @param siger 用户签名
	 */
	public void dropRegisterMember(Siger siger) {
		browerTask.__dropRegister(siger);
	}

	/**
	 * 推送新的在线成员，来自GATE/CALL节点
	 * @param siger  用户签名
	 */
	public void pushOnlineMember(Siger siger) {
		browerTask.__pushOnline(siger);
	}

	/**
	 * 删除在线成员，来自GATE/CALL节点
	 * @param siger 用户签名
	 */
	public void dropOnlineMember(Siger siger) {
		browerTask.__dropOnline(siger);
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		// 启动线程清除
		addThread(new ClearMemberThread());
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