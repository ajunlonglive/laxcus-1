/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.skin.*;

/**
 * 日志显示单元。
 * 
 * @author scott.liang
 * @version 1.0 09/06/2009
 * @since laxcus 1.0
 */
public class TerminalSoftwareCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 987871799148924041L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	private Icon forkImage;
	
	private Icon initImage;
	
	private Icon issueImage;


	/**
	 * 构造日志单元委托
	 */
	public TerminalSoftwareCellRenderer() {
		super();
		init();
	}
	
	private void loadImages() {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object");
		forkImage = loader.findImage("fork32.png");
		issueImage = loader.findImage("issue32.png");
		initImage = loader.findImage("init32.png");
	}

	private void loadColors() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}
	
	/**
	 * 初始化参数！
	 */
	private void init() {
		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);
		loadImages();
		loadColors();
		
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (value == null || value.getClass() != SoftwareItem.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());
		SoftwareItem log = (SoftwareItem) value;
		Phase phase = log.getPhase();

		Icon icon = log.getIcon();
		// 没有图标，使用默认
		if (icon == null) {
			if (PhaseTag.isConduct(phase.getFamily())) {
				icon = initImage;
			} else if (PhaseTag.isContact(phase.getFamily())) {
				icon = forkImage;
			} else if (PhaseTag.isEstablish(phase.getFamily())) {
				icon = issueImage;
			}
		}
		// 显示图标
		if (icon != null) {
			setIcon(icon);
		}
		// 文本
		String text = log.getTitle();
		if (text != null) {
			//			FontKit.setLabelText(this, text);
			setText(text);
		} else {
			//			FontKit.setLabelText(this, phase.getSockText());
			setText(phase.getSockText());
		}
		// 提示
		String tooltip = log.getTooltip();
		if (tooltip != null) {
			FontKit.setToolTipText(this, tooltip);
		} else {
			FontKit.setToolTipText(this, phase.toString(true)); // 精简模式，去掉签名！
		}

		if (isSelected) {
			setBackground(textSelectBackground);
			setForeground(textSelectForeground);
		} else {
			/** 
			 * 注意！这个一个JAVA的BUG！
			 * 情况：textBackground和Color.WHITE相等情况下，用setBackground(textBackground)
			 * 而不是setBackground(Color.WHITE)，背景变灰（不正常！！！），setBackground(Color.WHITE)则是显示正常。
			 * 所以采用下面办法避免故障！！！
			 **/
			if (Color.WHITE.equals(textBackground)) {
				setBackground(Color.WHITE);
			} else {
				setBackground(textBackground);
			}
			setForeground(textForeground);
		}

		// 边框!
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setEnabled(list.isEnabled());
		setOpaque(true);
		
		return this;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		loadColors();
		super.updateUI();
	}

}