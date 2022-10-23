/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.font;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;

/**
 * 轻量字体对话框
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
public abstract class LightFontDialog extends LightDialog {

	private static final long serialVersionUID = -7162602539414211258L;
	
	/**
	 * 构造轻量字体对话框
	 */
	public LightFontDialog(){
		super();
	}

	/**
	 * 设置标签文本
	 * @param label
	 * @param text
	 */
	protected void setLabelText(JLabel label, String text) {
		FontKit.setLabelText(label, text);
	}

	/**
	 * 设置标签文本
	 * @param label 标签
	 * @param horizontalAlignment 排列位置
	 * @param text 显示文本 
	 */
	protected void setLabelText(JLabel label, int horizontalAlignment, String text) {
		label.setHorizontalAlignment(horizontalAlignment);
		FontKit.setLabelText(label, text);
	}
	
	/**
	 * 设置按纽文本
	 * @param button
	 * @param text
	 */
	protected void setButtonText(AbstractButton button, String text) {
		FontKit.setButtonText(button, text);
	}

	/**
	 * 设置提示文本
	 * @param component
	 * @param text
	 */
	protected void setToolTipText(JComponent component, String text) {
		FontKit.setToolTipText(component, text);
	}

	/**
	 * 设置文本框文本
	 * @param component
	 * @param text
	 */
	protected void setFieldText(JTextComponent component, String text) {
		FontKit.setFieldText(component, text);
	}

	/** 根面板 **/
	private Class<?>[] roots = new Class<?>[] { JRootPane.class, JPanel.class,
			JLayeredPane.class, ScrollPane.class, JScrollPane.class, JViewport.class, Container.class, };

	/**
	 * 选择一个有效的标题栏字体
	 * @param str 显示文本
	 * @return 匹配的字体
	 */
	protected Font createTitledBorderFont(String str) {
		Font defaultFont = UIManager.getFont("TitledBorder.font");
		if (defaultFont != null && canDisplay(defaultFont, str)) {
			return new Font(defaultFont.getFamily(), Font.BOLD, defaultFont.getSize());
		}

		// 从集合中找匹配
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		if (fonts != null) {
			for (Font font : fonts) {
				if (canDisplay(font, str)) {
					return new Font(font.getFamily(), Font.BOLD, font.getSize());
				}
			}
		}

		return null;
	}

	/**
	 * 检查字体和组件匹配，如果不匹配，选择可用的字体
	 * @param text 文本
	 * @param component 组件
	 */
	private void check(String text, Component component) {
		if (text == null || text.isEmpty()) {
			return;
		}

		// 判断字体有效，可以正确显示
		Font defaultFont = component.getFont();
		boolean success = (defaultFont != null && canDisplay(defaultFont, text));

		// 以上不成功，从当前环境中找到匹配的字体，设置给按纽
		if (!success) {
			Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for (Font font : fonts) {
				if (canDisplay(font, text)) {
					component.setFont(font);
					break;
				}
			}
		}
	}

	/**
	 * 判断是容器
	 * @param component
	 * @return 返回真或者假
	 */
	private boolean isContainer(Component component) {
		// 判断是内部命令
		for (int i = 0; i < roots.length; i++) {
			if (component.getClass() == roots[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 当加载完成后，检查每个组件的文本。
	 * 包括：按纽：标签、文本框。
	 */
	protected void checkContainer(Container container) {
		Component[] components = container.getComponents();
		for (Component element : components) {
			// 判断是容器实例
			if(isContainer(element)) {
				checkContainer((Container) element);
				continue;
			}

			if (Laxkit.isClassFrom(element, AbstractButton.class)) { 
				AbstractButton but = (AbstractButton) element;
				check(but.getText(), but);
			} else if (Laxkit.isClassFrom(element, JLabel.class)) {
				JLabel lbl = (JLabel) element;
				check(lbl.getText(), lbl);
			} else if(Laxkit.isClassFrom(element, JTextComponent.class)) {
				JTextComponent text = (JTextComponent)element;
				check(text.getText(), text);
			}
		}
	}

	/**
	 * 检查对话框中组件的字体能够正确显示，否则选择一个。
	 */
	protected void checkDialogFonts() {
		checkContainer(this);
	}

	/**
	 * 判断字体能够正确显示一行文本
	 * @param font 字体
	 * @param str 文本
	 * @return 返回真或者假
	 */
	private boolean canDisplay(Font font, String str) {
		return font.canDisplayUpTo(str) == -1;
	}

	/**
	 * 检查字体线程
	 *
	 * @author scott.liang
	 * @version 1.0 6/14/2021
	 * @since laxcus 1.0
	 */
	class CheckFontThread extends SwingEvent {
		public CheckFontThread() {
			super();
		}

		@Override
		public void process() {
			checkDialogFonts();
		}
	}

	/**
	 * 检查字体
	 */
	protected void checkFont() {
		addThread(new CheckFontThread());
	}

	/**
	 * 设置根面板的边框范围
	 * @param panel 面板
	 */
	protected void setRootBorder(JPanel panel) {
		panel.setBorder(new EmptyBorder(5, 5, 6, 5));
	}
	
	/**
	 * 保存选项
	 * @param chooser 文件选择器
	 */
	protected String saveFileFileter(JFileChooser chooser) {
		javax.swing.filechooser.FileFilter e = chooser.getFileFilter();
		if (e == null || e.getClass() != DiskFileFilter.class) {
			return null;
		}

		DiskFileFilter filter = (DiskFileFilter) e;
		return filter.getDescription();
	}
	
	/**
	 * 从中找到匹配的选项
	 * @param chooser 文件选择器
	 * @param selectDescription 选中的描述
	 */
	protected void chooseFileFilter(JFileChooser chooser, String selectDescription) {
		if (selectDescription == null) {
			return;
		}
		// 选择
		javax.swing.filechooser.FileFilter[] elements = chooser.getChoosableFileFilters();
		for (int i = 0; elements != null && i < elements.length; i++) {
			javax.swing.filechooser.FileFilter e = (javax.swing.filechooser.FileFilter) elements[i];
			if (e.getClass() != DiskFileFilter.class) {
				continue;
			}
			DiskFileFilter filter = (DiskFileFilter) e;
			String type = filter.getDescription();
			if (Laxkit.compareTo(selectDescription, type) == 0) {
				chooser.setFileFilter(filter);
				break;
			}
		}
	}
	
}