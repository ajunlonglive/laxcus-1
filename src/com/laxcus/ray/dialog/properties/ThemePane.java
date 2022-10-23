/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.properties;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.ray.*;
import com.laxcus.ray.panel.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 桌面主题面板
 * 
 * @author scott.liang
 * @version 1.0 7/21/2021
 * @since laxcus 1.0
 */
class ThemePane extends RayPanel implements ActionListener {

	private static final long serialVersionUID = 3754991024830026902L;

	private DefaultComboBoxModel model = new DefaultComboBoxModel();
	private ThemeCellRenderer renderer;
	private JComboBox cbxTheme = new JComboBox();
	private FlatButton cmdActive = new FlatButton(); // 激活

	private FlatButton cmdSystemFont = new FlatButton();

	private JLabel lblImage = new JLabel();

	private RayUIUpdater updater;

	/**
	 * 构造桌面主题面板
	 */
	public ThemePane(RayUIUpdater u) {
		super();
		updater = u;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
//		addThread(new ClickThread(event));
		clieck(event);
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
//			clieck(event);
//		}
//	}

	private void clieck(ActionEvent event) {
		Object source = event.getSource();
		if (source == cmdSystemFont) {
			doSystemFont();
		} else if (source == cbxTheme) {
			doSelect();
		} else if (source == cmdActive) {
			doActive();
		}
	}

	private void doSystemFont() {
		Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM");
		// 没有，以按纽字体做标准
		if (font == null) {
			font = cmdSystemFont.getFont();
		}
		Font select = MessageBox.choiceFont(this, font);
		if (select == null) {
			return;
		}
		String value = UIManager.getString("PropertiesDialog.MinFontSize");
		int miniFontSize = ConfigParser.splitInteger(value, 12);
		value = UIManager.getString("PropertiesDialog.MaxFontSize");
		int maxFontSize = ConfigParser.splitInteger(value, 16);
		
		// 字体在12 - 16磅之间
		if (select.getSize() < miniFontSize) {
			select = new Font(select.getName(), select.getStyle(), miniFontSize);
		} else if (select.getSize() > maxFontSize) {
			select = new Font(select.getName(), select.getStyle(), maxFontSize);
		}
		RTKit.writeFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM", select);

		// 通知更新字体
		updater.updateSystemFont(select);


		// 应该是调用FRAME更新UI，全部！

		////		// 更新菜单字体
		////		addThread(new ExchangeLaunchMenuFont(select));
		//
		//		//			// 更新字体
		//		//			updateSystemFonts();
		//
		//		// 更新字体
		//		UITools.updateSystemFonts(font);
		//		if (Skins.isNimbus()) {
		//			UITools.updateNimbusSystemFonts(font);
		//		} else {
		//			UITools.updateMetalSystemFonts(font);
		//		}
		//
		//		// 更新UI
		//		reloadUI();
	}

	private void doSelect() {
		Object e = model.getSelectedItem();
		if (e.getClass() != ThemeItem.class) {
			return;
		}

		ThemeItem item = (ThemeItem) e;
		// 找到对应的ID，理新UI
		SkinToken token = item.getToken();
		
		// 效果图在JAR中的链接
		String link = token.getImpress();
		if (link == null) {
			return;
		}

		// 取出JAR中的效果图
		ResourceLoader res = new ResourceLoader();
		ImageIcon image = res.findImage(link); // 固定是16*16的像素
		if (image != null) {
			lblImage.setIcon(image);
		}
	}

	private void doActive() {
		Object e = model.getSelectedItem();
		if (e == null || e.getClass() != ThemeItem.class) {
			return;
		}

		ThemeItem item = (ThemeItem) e;
		// 找到对应的ID，理新UI
		SkinToken token = item.getToken();

		// 更新UI界面
		updater.updateLookAndFeel(token);

		//		// 切换主题界面！
		//		boolean success = token.switchTheme();
		//		// 保存焦点
		//		if (success) {
		//			SkinTokenLoader loader = DesktopLauncher.getInstance().getSkinLoader();
		//			String skinName = token.getName();
		//			// 重置选中的的皮肤方案
		//			loader.exchangeCheckedSkinToken(skinName);
		//			// 记录界面外观
		//			Skins.setLookAndFeel(token.getLookAndFeel());
		//
		//			// // 更新系统环境字体！
		//			// updateSystemFonts();
		//
		//			// doSystemFont();
		//
		//			// // 重置UI界面
		//			// reloadUI();
		//		}
	}


//	private Color getBorderColor() {
//		if (Skins.isNimbus()) {
//			ESL light = new ESL(150, 30, 135);
//			return light.toColor();
//		} else {
//			Color c = super.getBackground();
//			if(c == null) {
//				c = Color.LIGHT_GRAY;
//			}
//			ESL e = new RGB(c).toESL();
//			return e.toBrighter(50).toColor();
//		}
//	}

	private void createThemeToken() {
		SkinTokenLoader loader = RayLauncher.getInstance().getSkinLoader();
		java.util.List<SkinToken> tokens = loader.getSkinTokens();
		// 保存进入
		int index = 0;
		int select = -1;
		
		for (SkinToken token : tokens) {
			model.addElement(new ThemeItem(token));
			// 找到选中的
			if (token.isChecked()) {
				select = index;
			}
			index++;
		}

		renderer = new ThemeCellRenderer();
		cbxTheme.setLightWeightPopupEnabled(false); // 重量级组件
		cbxTheme.setRenderer(renderer);
		cbxTheme.setModel(model);
		cbxTheme.addActionListener(this);
		cbxTheme.setEditable(false);
		cbxTheme.setPreferredSize(new Dimension(10, 32));
		if (select >= 0) {
			cbxTheme.setSelectedIndex(select);
		}
	}

	private JPanel createThemePane() {
		createThemeToken();

		JLabel label = new JLabel(UIManager.getString("PropertiesDialog.themeThemeText"));
		label.setLabelFor(cbxTheme);

		FontKit.setButtonText(cmdActive, UIManager.getString("PropertiesDialog.themeButtonActiveText"));
		cmdActive.setMnemonic('A');
		cmdActive.addActionListener(this);

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(4, 0));
		left.add(label, BorderLayout.WEST);
		left.add(cbxTheme, BorderLayout.CENTER);
		left.add(cmdActive, BorderLayout.EAST);

		FontKit.setButtonText(cmdSystemFont, UIManager.getString("PropertiesDialog.themeButtonSystemFontText"));
		cmdSystemFont.setMnemonic('S');
		cmdSystemFont.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(20,0));
		panel.add(left, BorderLayout.CENTER);
		panel.add(cmdSystemFont, BorderLayout.EAST);
		return panel;
	}


	private JPanel createExamplePane() {
		JLabel label = new JLabel(UIManager.getString("PropertiesDialog.themeExampleText"));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.CENTER);

		lblImage.setHorizontalAlignment(SwingConstants.LEFT);
		lblImage.setVerticalAlignment(SwingConstants.TOP);
		lblImage.setBorder(new EmptyBorder(2,2,2,2));
		
		JScrollPane jsp = new JScrollPane(lblImage);		
		jsp.setBorder(new HighlightBorder(1));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 5));
		panel.add(label, BorderLayout.NORTH);
		panel.add(jsp, BorderLayout.CENTER);
		return panel;
	}

	public void init() {
		String text = UIManager.getString("PropertiesDialog.themeIntroduceText");
		text = String.format("<html>%s</html>", text);
		JLabel label = new JLabel(text);
		label.setBorder(new EmptyBorder(10, 0, 10, 0));

		JPanel themePane = createThemePane();
		themePane.setBorder(new EmptyBorder(10, 0, 0, 0));
		JPanel examplePane = createExamplePane();
		examplePane.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(0, 0));
		north.add(label, BorderLayout.NORTH);
		north.add(themePane, BorderLayout.CENTER);
		north.setBorder(new EmptyBorder(0, 0, 10, 0));

		setLayout(new BorderLayout(0, 10));
		add(north, BorderLayout.NORTH);
		add(examplePane, BorderLayout.CENTER);
		setBorder(new EmptyBorder(0, 4, 4, 4));
		
		// 选中和显示
		doSelect();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		if (renderer != null) {
			renderer.updateUI();
		}
	}
}
