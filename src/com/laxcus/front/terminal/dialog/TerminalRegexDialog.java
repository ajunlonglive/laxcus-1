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
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 正则表达式语法测试器
 *
 * @author scott.liang
 * @version 1.0 9/29/2018
 * @since laxcus 1.0
 */
public class TerminalRegexDialog extends TerminalCommonFontDialog implements ActionListener {
	
	private static final long serialVersionUID = -48188996874030505L;

	private JLabel lblRegex = new JLabel("Syntax:", SwingConstants.RIGHT);
	private JTextArea txtRegex = new JTextArea();

	private JLabel lblContent = new JLabel("Value:", SwingConstants.RIGHT);
	private JTextArea txtContent = new JTextArea();

	private JLabel lblResult = new JLabel("", SwingConstants.LEFT);

	private JButton cmdTest = new JButton("Test");
	private JButton cmdClear = new JButton("Clear");
	private JButton cmdExit = new JButton("Exit");
	
	/**
	 * 构造正则表达式语法测试器，指定参数
	 * @param parent 窗口
	 * @param modal 模态或者否
	 * @throws HeadlessException
	 */
	public TerminalRegexDialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
	}

//	/**
//	 * 解析标签
//	 * @param xmlPath
//	 * @return 抽取的标签属性
//	 */
//	private String getCaption(String xmlPath) {
//		return TerminalLauncher.getInstance().findCaption(xmlPath);
//	}
//
//	/**
//	 * 解析内容
//	 * @param xmlPath
//	 * @return 抽取的文本
//	 */
//	private String findContent(String xmlPath) {
//		return TerminalLauncher.getInstance().findContent(xmlPath);
//	}

	class InvokeThread extends SwingEvent {
		ActionEvent event;
		InvokeThread(ActionEvent e) {
			super();
			event = e;
		}
		public void process() {
			active(event);
		}
	}

	/**
	 * 执行按纽操作
	 * @param e
	 */
	private void active(ActionEvent e) {
		if (e.getSource() == cmdTest) {
			check();
		} else if(e.getSource() == cmdClear) {
			clear();
		} else if(e.getSource() == cmdExit) {
			exit();
		} 
	}
	
	/**
	 * 关闭窗口
	 */
	private void exit() {
		String title = findCaption("Dialog/regex/close/title");
		String content = findContent("Dialog/regex/close");

		
		int who = MessageDialog.showMessageBox(this, title, JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
		if (who == JOptionPane.YES_OPTION) {
			dispose();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new InvokeThread(e));
	}
	
	/**
	 * 侵害字符串
	 * @param text
	 * @return
	 */
	private String[] split(String text) {
		ArrayList<String> array = new ArrayList<String>();
		try {
			StringReader str = new StringReader(text);
			BufferedReader reader = new BufferedReader(str);
			do {
				String sub = reader.readLine();
				if (sub == null) {
					break;
				}
				array.add(sub);
			} while (true);
			reader.close();
			str.close();
		} catch (IOException ex) {

		}
		String[] all = new String[array.size()];
		return array.toArray(all);
	}
	
	/**
	 * 转义文本
	 * @param text
	 * @return
	 */
	private String translate(String text) {
		if (text == null) {
			return "null";
		}
		char[] src = {'<', '>', ' '};
		String[] dest = {"&lt;", "&gt;", "&nbsp;"};
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char w = text.charAt(i);
			boolean find = false;
			for (int j = 0; j < src.length; j++) {
				find = (w == src[j]);
				if (find) {
					buff.append(dest[j]);
					break;
				}
			}
			if (!find) buff.append(w);
		}
		return buff.toString();
	}
	
	/**
	 * 语法检查
	 */
	private void check() {
		String regex = txtRegex.getText();
		String text = txtContent.getText();

		// 判断参数有效
		if (regex.trim().isEmpty()) {
			txtRegex.requestFocus();
			return;
		}
		if (text.trim().isEmpty()) {
			txtContent.requestFocus();
			return;
		}

		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text);
			if (matcher.matches()) {
				StringBuilder bf = new StringBuilder();
				int count = matcher.groupCount();
				
				// 输出文本，定位每一段位置
				for (int index = 1; index <= count; index++) {
					if (bf.length() > 0) bf.append("<br>");
					String sub = matcher.group(index);	
					bf.append(String.format("%d: %s", index, translate(sub)));
					bf.append(String.format(" [%d : %d]", matcher.start(index), (sub == null ? -1 : sub.length()) ));
				}
				
				// 如果没有，显示一个正确的提示
				if (bf.length() == 0) {
					String content = findContent("Dialog/regex/texts/syntax/okay");
					bf.append(content); 
				}
				String html = String.format("<html><body>%s</body></html>", bf.toString());
//				lblResult.setForeground(Color.BLACK);
				setLabelText(lblResult, html);
			} else {
				String content = findContent("Dialog/regex/texts/syntax/error");
				String html = String.format("<html><body>%s</body></html>", content);
				lblResult.setForeground(Color.RED);
				setLabelText(lblResult, html);
			}
		} catch (PatternSyntaxException ex) {
			String msg = ex.getMessage();
			String[] all = split(msg);
			StringBuilder buff = new StringBuilder();
			for (int i = 0; i < all.length; i++) {
				if (buff.length() > 0) buff.append("<br>");
				buff.append(all[i]);
			}
			String html = String.format("<html><body>%s</body></html>", buff.toString());
			lblResult.setForeground(Color.RED);
			setLabelText(lblResult, html);
		}
	}

	/**
	 * 清除参数
	 */
	private void clear() {
		txtRegex.setText("");
		txtContent.setText("");
		lblResult.setText("");
		txtRegex.requestFocus();
	}
	
	/**
	 * 定义范围
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		int width = 580;
		int height = 480;
		
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * 设置文本
	 */
	private void initTexts() {
		// 默认字体
		Font font = UIManager.getFont("TextArea.font");
		Font textFont = new Font(font.getName(), Font.PLAIN, 16);

		txtRegex.setRows(4);
		txtRegex.setTabSize(3);
		txtRegex.setLineWrap(true);
		txtRegex.setFont(textFont);

		txtContent.setRows(4);
		txtContent.setTabSize(3);
		txtContent.setLineWrap(true);
		txtContent.setFont(textFont);

		setLabelText(lblRegex, findCaption("Dialog/regex/texts/syntax/title"));
		lblRegex.setDisplayedMnemonic('S');
		lblRegex.setLabelFor(txtRegex);

		setLabelText(lblContent, findCaption("Dialog/regex/texts/content/title"));
		lblContent.setDisplayedMnemonic('C');
		lblContent.setLabelFor(txtContent);

		lblResult.setFont(textFont);
	}

	/**
	 * 初始化按纽
	 */
	private void initButtons() {
		setButtonText(cmdTest, findCaption("Dialog/regex/buttons/test/title"));
		cmdTest.setMnemonic('T');
		cmdTest.addActionListener(this);
		
		setButtonText(cmdClear, findCaption("Dialog/regex/buttons/reset/title"));
		cmdClear.setMnemonic('R');
		cmdClear.addActionListener(this);
		
		setButtonText(cmdExit, findCaption("Dialog/regex/buttons/exit/title"));
		cmdExit.setMnemonic('X');
		cmdExit.addActionListener(this);
	}
	
	/**
	 * 初始化各组件，设置新的字体
	 */
	private void initControls() {
		initTexts();
		initButtons();
	}

	/**
	 * 正则表达式面板
	 * @return
	 */
	private JPanel initRegexPanel() {
		// 左侧面板
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 0, 5));
		left.add(lblRegex);
		left.add(lblContent);

		// 右侧面板
		txtRegex.setBorder(BorderFactory.createEmptyBorder(5, 3, 3, 3));
		txtContent.setBorder(BorderFactory.createEmptyBorder(5, 3, 3, 3));
		
		JScrollPane jsp1 = new JScrollPane(txtRegex);
		JScrollPane jsp2 = new JScrollPane(txtContent);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1, 0, 5));
		right.add(jsp1);
		right.add(jsp2);
		
		// 合并面板
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(8, 0));
		
		String title = findCaption("Dialog/regex/texts/title");
		panel.setBorder(UITools.createTitledBorder(title, 5));
		panel.add(left, BorderLayout.WEST);
		panel.add(right, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * 处理结果面板
	 * @return
	 */
	private JPanel initResultPanel() {
		JScrollPane jsp = new JScrollPane(lblResult);
		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));

		// 中间
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		String title = findCaption("Dialog/regex/result/title");
		panel.setBorder(UITools.createTitledBorder(title, 5));
		panel.add(jsp, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * 按纽面板
	 * @return
	 */
	private JPanel initButtonPanel() {
		// 按纽
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 3, 8, 0));
		buttons.add(cmdTest);
		buttons.add(cmdClear);
		buttons.add(cmdExit);

		// 底部
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.EAST);
		
		return panel;
	}
	
	/**
	 * 初始化界面
	 * @return
	 */
	private JPanel initPanel() {
		// 初始化控件
		initControls();

		// 三个面板
		JPanel north = initRegexPanel();
		JPanel center = initResultPanel();
		JPanel south = initButtonPanel();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(1, 3));
		setRootBorder(panel);
		panel.add(north, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		panel.add(south, BorderLayout.SOUTH);
		
		return panel;
	}
	
	/**
	 * 打开界面
	 */
	public void showDialog() {	
		// 初始化界面
		setContentPane(initPanel());
		
		// 标题
		setTitle(findCaption("Dialog/regex/title"));
		// 按纽无效
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		// 位置
		Rectangle rect = getBound();
		setBounds(rect);
		setMinimumSize(new Dimension(rect.width / 2, rect.height / 2));
		setPreferredSize(rect.getSize());
		
		setAlwaysOnTop(true);
		
		// 检查对话框字体
		checkDialogFonts();

		setVisible(true);
	}
	
}