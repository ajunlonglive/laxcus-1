/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.run;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.application.manage.*;
import com.laxcus.container.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.sound.*;

/**
 * 应用运行窗口
 * 
 * @author scott.liang
 * @version 1.0 6/30/2021
 * @since laxcus 1.0
 */
public class RayRunDialog extends LightDialog implements ActionListener {

	private static final long serialVersionUID = -5107692562372768968L;
	
	/** 实例 **/
	static RayRunDialog selfHandle;
	
	/**
	 * 返回句柄
	 * @return 实例
	 */
	public static RayRunDialog getInstance() {
		return RayRunDialog.selfHandle;
	}

	/** 命令集合 **/
	private DefaultComboBoxModel model = new DefaultComboBoxModel();

	/** 对话框 **/
	private JComboBox field = new JComboBox();

	private RunCommandCellRenderer renderer;

	/** OKAY 按纽 **/
	private FlatButton running;

	/** 取消按纽 **/
	private FlatButton cancel;

	/** 命令集合 **/
	private ArrayList<String> array = new ArrayList<String>();

	/**
	 * 构造应用运行窗口
	 */
	public RayRunDialog() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		click(event);
	}

	/**
	 * 单击事件
	 * @param e
	 */
	private void click(ActionEvent e) {
		Object source = e.getSource();
		if (source == running) {
			doRun();
		} else if (source == cancel) {
			cancel();
		}
	}

	/**
	 * 回车键事件
	 *
	 * @author scott.liang
	 * @version 1.0 8/10/2021
	 * @since laxcus 1.0
	 */
	class EnterAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				doRun();
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#closeWindow()
	 */
	@Override
	public void closeWindow() {
		super.closeWindow();
		RayRunDialog.selfHandle = null;
	}

	/**
	 * 销毁窗口
	 */
	private void cancel() {
		// 保存宽度和高度
		saveDimension();

		// 关闭窗口
		closeWindow();
	}

	private static String[] splitText(String text) {
		return text.split(" ");
	}
	
//	class RunApplicationThread extends SwingWorker<Integer, Object> {
//
//		WKey key;
//
//		String[] links;
//
//		RunApplicationThread(WKey k, String[] args) {
//			super();
//			key = k;
//			links = args;
//		}
//
//		/* (non-Javadoc)
//		 * @see javax.swing.SwingWorker#doInBackground()
//		 */
//		@Override
//		protected Integer doInBackground() throws Exception {
//			// 启动应用
//			int ret = -1;
//			try {
//				ret = ApplicationStarter.start(key, links);
//			} catch (SecurityException e) {
//				Logger.error(e);
//			} catch (IllegalArgumentException e) {
//				Logger.error(e);
//			} catch (IOException e) {
//				Logger.error(e);
//			} catch (InstantiationException e) {
//				Logger.error(e);
//			} catch (IllegalAccessException e) {
//				Logger.error(e);
//			} catch (NoSuchMethodException e) {
//				Logger.error(e);
//			} catch (InvocationTargetException e) {
//				Logger.error(e);
//			}
//			return new Integer(ret);
//		}
//
//		@Override
//		protected void done() {
//			Integer ret = null;
//			try {
//				ret = get();
//			} catch (Exception e) {
//				Logger.error(e);
//			}
//
//			boolean failed = (ret == null || ret.intValue() != 0);
//			if (failed) {
//				// 弹出错误!
//				String title = UIManager.getString("ApplicationStart.startFaultTitle");
//				String content = UIManager.getString("ApplicationStart.startFaultContent");
//				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
//			}
//		}
//	}

	/**
	 * 启动应用
	 * @param input 输入命令
	 * @return 成功返回真，否则假
	 */
	private boolean startApplication(String input) {
		// 分割
		String[] elements = splitText(input);
		
		// 命令
		String command = elements[0];
		// 参数
		String[] args = new String[0];
		if (elements.length > 1) {
			args = new String[elements.length - 1];
			for (int i = 1; i < elements.length; i++) {
				args[i - 1] = elements[i];
			}
		}

		// 通过命令找到对应的单元
		WKey[] keys = RTManager.getInstance().findFromCommand(command);
		if (keys == null || keys.length == 0) {
			playError();
			// 弹出错误!
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			MessageBox.showFault(this, title, content);
			return false;
		}
		
		WKey key = null;
		// 一个...
		if (keys.length == 1) {
			key = keys[0];
		} else {
			// 多个选择项
			ArrayList<MultiSelectCommand> array = new ArrayList<MultiSelectCommand>();
			for (int i = 0; i < keys.length; i++) {
				WProgram program = RTManager.getInstance().findProgram(keys[i]);
				// 增加到记录
				if (program != null) {
					MultiSelectCommand e = new MultiSelectCommand();
					e.setCommand(program.getCommand());
					e.setIcon(program.getIcon());
					e.setToolTip(program.getToolTip());
					e.setTitle(program.getTitle());
					e.setKey(keys[i]);
					array.add(e);
				}
			}
			// 显示窗口
			RayMultiSelectDialog dlg = new RayMultiSelectDialog(array);
			Object value = dlg.showDialog(this, true); // 模态
			if (value == null || value.getClass() != WKey.class) {
				return false;
			}
			key = (WKey) value;
		}
		
//		RunApplicationThread rt = new RunApplicationThread(key, args);
//		rt.execute();
//		return true;
		
		// 启动应用
		int ret = -1;
		try {
			ret = ApplicationStarter.start(key, args);
		} catch (SecurityException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IOException e) {
			Logger.error(e);
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		}

		boolean success = (ret == 0);
		if (!success) {
			playError();
			// 弹出错误!
			String title = UIManager.getString("ApplicationStart.startFaultTitle");
			String content = UIManager.getString("ApplicationStart.startFaultContent");
			MessageBox.showFault(this, title, content);
		}
		return success;
	}

	/**
	 * 播出警告声音
	 */
	private void playWarning() {
		SoundPlayer.getInstance().play(SoundTag.WARNING);
	}

	/**
	 * 播出错误声音
	 */
	private void playError() {
		SoundPlayer.getInstance().play(SoundTag.ERROR);
	}

	/**
	 * 运行窗口
	 */
	private void doRun() {
		// 选中的对象
		Object object = model.getSelectedItem();
		if (object == null) {
			playWarning();
			String title = UIManager.getString("RunDialog.notfoundTitle");
			String content = UIManager.getString("RunDialog.notfoundContent");
			MessageBox.showWarning(this, title, content);
			return;
		}

		// 取出字符串
		String input = null;
		if (object.getClass() == String.class) {
			input = (String) object;
		} else if (Laxkit.isClassFrom(object, RunCommandItem.class)) {
			input = ((RunCommandItem) object).getText();
		}
		
		// 无效，弹出菜单
		if (input == null || input.trim().isEmpty()) {
			playError();
			String title = UIManager.getString("RunDialog.invalidTitle");
			String content = UIManager.getString("RunDialog.invalidContent");
			MessageBox.showFault(this, title, content);
			return;
		}
		
//		System.out.printf("参数是：%s\n", input);

		// 启动应用
		boolean success = startApplication(input);
		// 不成功，弹出启动失败
		if (success) {
			// 定义位置
			saveDimension();
			addCommand(input);
			writeElements();

			//			// 关闭窗口
			//			setVisible(false);
			//			dispose();

			closeWindow();
		} 
		
//		else{
//			playError();
//			// 弹出错误!
//			String title = UIManager.getString("RunDialog.startErrorTitle");
//			String content = UIManager.getString("RunDialog.startErrorContent");
//			MessageBox.showFault(this, title, content);
//		}
	}

	private FlatButton createButton(String text, char w) {
		FlatButton button = new FlatButton();
		FontKit.setButtonText(button, text);

		button.setIconTextGap(4);
		button.addActionListener(this);
		button.setMnemonic(w);

		return button;
	}

	/**
	 * 把参数加载到组件中
	 */
	private void loadElement() {
		// 从注册器中加载
		readElements();
		// 导入记录
		for (String value : array) {
			model.addElement(new RunCommandItem(value));
		}
		if (model.getSize() > 0) {
			Object o = model.getElementAt(0);
			model.setSelectedItem(o);
		}
	}

	/**
	 * 加入命令
	 * @param cmd
	 */
	private void addCommand(String cmd) {
		cmd = cmd.trim();
		for (String value : array) {
			boolean exists = value.equalsIgnoreCase(cmd);
			if (exists) {
				return;
			}
		}
		// 放在前面
		array.add(0, cmd);
		model.insertElementAt(new RunCommandItem(cmd), 0);
	}

	/**
	 * 加载单元
	 */
	private void readElements() {
		RFolder folder = RTKit.findFolder(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/Commands");
		if (folder == null) {
			return;
		}

		RInteger e = folder.findInteger("elements");
		if (e == null) {
			return;
		}
		int size = e.getValue();
		for (int i = 0; i < size; i++) {
			String key = String.format("command%d", i + 1);
			RString str = folder.findString(key);
			if (str != null) {
				array.add(str.getValue());
			}
		}
	}

	/**
	 * 写入全部成员
	 */
	private void writeElements() {
		RTKit.remove(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/Commands", RTokenAttribute.FOLDER);
		RFolder folder = RTKit.buildFolder(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/Commands");
		// 参数
		int size = array.size();
		folder.add(new RInteger("elements", size));
		for (int i = 0; i < size; i++) {
			String value = array.get(i);
			String key = String.format("command%d", i + 1);
			folder.add(new RString(key, value));
		}
	}
	
	/**
	 * 给文本编辑器增加回车键事件
	 * 发生updateUI，旧的回车事件会被清除，这时就需要重新设置一个
	 */
	private void addEnterListener() {
		if (field == null) {
			return;
		}
		Component source = field.getEditor().getEditorComponent();
		if (source == null) {
			return;
		}

		KeyListener[] vs = source.getKeyListeners();
		boolean success = false;
		int size = (vs != null ? vs.length : 0);
		// 如果有匹配的忽略它
		for (int i = 0; i < size; i++) {
			if (vs[i] != null && vs[i].getClass() == EnterAdapter.class) {
				success = true;
				break;
			}
		}
		// 没有，增加一个
		if (!success) {
			source.addKeyListener(new EnterAdapter());
		} 
	}

	/**
	 * 初始化对话框
	 */
	private void initDialog() {
		setFrameIcon(UIManager.getIcon("RunDialog.LogoIcon")); 
		setTitle(UIManager.getString("RunDialog.Title"));

		String text = UIManager.getString("RunDialog.contentText"); // "显示您的文档";
		String html = String.format("<html>%s</html>", text);

		JLabel label = new JLabel(); // html, SwingConstants.LEFT); // 居中显示
		label.setIcon(UIManager.getIcon("RunDialog.contentIcon"));
		label.setIconTextGap(6);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.CENTER);
		FontKit.setLabelText(label, html);

		running = createButton(UIManager.getString("RunDialog.runButtonText"), 'R'); // "运行");
		cancel = createButton(UIManager.getString("RunDialog.cancelButtonText"), 'C'); // "取消");

		renderer = new RunCommandCellRenderer();
		field.setRenderer(renderer);
		field.setModel(model);
		field.setEditable(true);
		field.setLightWeightPopupEnabled(false); // 重量级组件
		// 最小尺寸
		field.setMinimumSize(new Dimension(10, 32));
		field.setPreferredSize(new Dimension(10, 32));
		// 给输入组件加上回车键!
		addEnterListener();
		
//		Component component = field.getEditor().getEditorComponent();
//		component.addKeyListener(new EnterAdapter());
		
//		field.addKeyListener(new EnterAdapter());
//		field.getEditor().addActionListener( new ActionAdapter());
//		field.addActionListener(new ActionAdapter());
		
//		field.addKeyListener(new EnterAdapter());

//		field.getEditor().g.addActionListener(this);

		// 加载单元，这些单元从内存或者其它地方取得
		loadElement();

		JLabel open = new JLabel();
		FontKit.setLabelText(open, UIManager.getString("RunDialog.openLabelText"));
		JPanel x = new JPanel();
		x.setLayout(new BorderLayout(6, 0));
		x.add(open, BorderLayout.WEST);
		x.add(field, BorderLayout.CENTER);

		JPanel j = new JPanel();
		j.setLayout(new BorderLayout());
		j.add(label, BorderLayout.CENTER);
		j.add(x, BorderLayout.SOUTH);

		JPanel n = new JPanel();
		n.setLayout(new GridLayout(1, 2, 6, 0));
		n.add(running); 
		n.add(cancel);
		JPanel w = new JPanel();
		w.setLayout(new BorderLayout());
		w.add(n, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 10));
		panel.add(j, BorderLayout.CENTER);
		panel.add(w, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		// 设置面板
		setContentPane(panel);
	}

	/**
	 * 保存尺寸
	 */
	private void saveDimension() {
		Rectangle rect = super.getBounds();
		Dimension dim = new Dimension(rect.width, rect.height);
		RTKit.writeDimension(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/Dimension", dim);
	}

	/**
	 * 设置显示范围
	 * @param desktop
	 */
	private void setBounds(JDesktopPane desktop) {
		int w = 410;
		int h = 192;
		Dimension dim = RTKit.readDimension(RTEnvironment.ENVIRONMENT_SYSTEM, "RunDialog/Dimension");
		if (dim != null) {
			w = dim.width;
			h = dim.height;
		}
		int x = 4;
		int y = desktop.getHeight() - (h + 4);
		Rectangle bounds = new Rectangle(x, y, w, h);

		// 设置显示范围
		setBounds(bounds);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.dialog.LightDialog#showDialog(java.awt.Component, boolean)
	 */
	@Override
	public Object showDialog(Component parent, boolean modal) {
		JDesktopPane desktop = findDesktopPaneForComponent(parent);
		if (desktop == null) {
			return null;
		}

		// 显示对话框
		initDialog();

		// 只可以调整窗口，其它参数忽略
		setResizable(true);
		setClosable(false);

		setBounds(desktop);
		
		RayRunDialog.selfHandle = this;

		if (modal) {
			return super.showModalDialog(parent);
		} else {
			return super.showNormalDialog(parent);
		}
	}
	
	class EnterEvent extends SwingEvent {
		
		public EnterEvent() {
			super(true);
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.event.SwingEvent#process()
		 */
		@Override
		public void process() {
			addEnterListener();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		// 更新UI界面
		if (renderer != null) {
			renderer.updateUI();
		}
		
		// 更新字体，注意！不要更新UI，否则在updateUI方法里会形成死循环
		FontKit.updateDefaultFonts(this, false);
		
		// 加上回车键判断
		addThread(new EnterEvent());
	}

}