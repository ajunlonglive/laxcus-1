/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.laxcus.util.*;
import com.laxcus.util.event.*;

/**
 * 字体对话窗口。<br>
 * 
 * 用来解决LINUX字体不兼容的情况而设置。
 * 
 * @author scott.liang
 * @version 1.0 10/4/2018
 * @since laxcus 1.0
 */
public class CommonFontDialog extends JDialog {

	private static final long serialVersionUID = -6845244234164117136L;
	
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
			return new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize());
		}

		// 从集合中找匹配
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		if (fonts != null) {
			for (Font font : fonts) {
				if (canDisplay(font, str)) {
					return new Font(font.getName(), Font.BOLD, font.getSize());
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
	 * 加入线程
	 * @param thread
	 */
	protected void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	/**
	 * 保存一批线程
	 * @param threads 线程数组
	 */
	protected void addThreads(Collection<SwingEvent> threads) {
		SwingDispatcher.invokeThreads(threads);
	}

	/**
	 * 检查字体
	 */
	protected void checkFont() {
		addThread(new CheckFontThread());
	}

	class CheckFontThread extends SwingEvent {
		CheckFontThread() { super(); }
		public void process() {
			checkDialogFonts();
		}
	}

	/**
	 * 构造默认的字体对话窗口
	 */
	public CommonFontDialog() {
		super();
	}

	/**
	 * 构造字体对话窗口，指定参数
	 * @param frame 窗口
	 */
	public CommonFontDialog(Frame frame) {
		super(frame);
	}

	/**
	 * 构造字体对话窗口，指定参数
	 * @param frame 窗口
	 * @param modal 模态或者否
	 */
	public CommonFontDialog(Frame frame, boolean modal) {
		super(frame, modal);
	}

	/**
	 * 构造字体对话窗口，指定参数
	 * @param dialog 对话窗口
	 * @param modal 模态或者否
	 */
	public CommonFontDialog(Dialog dialog, boolean modal) {
		super(dialog, modal);
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
	
	//	/**
	//	 * @param dialog
	//	 */
	//	public CommonFontDialog(Dialog dialog) {
	//		super(dialog);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param window
	//	 */
	//	public CommonFontDialog(Window window) {
	//		super(window);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//
	//	/**
	//	 * @param arg0
	//	 * @param arg1
	//	 */
	//	public CommonFontDialog(Frame arg0, String arg1) {
	//		super(arg0, arg1);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//
	//	/**
	//	 * @param dialog
	//	 * @param arg1
	//	 */
	//	public CommonFontDialog(Dialog dialog, String arg1) {
	//		super(dialog, arg1);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param frame
	//	 * @param title
	//	 * @param modal
	//	 */
	//	public CommonFontDialog(Frame frame, String title, boolean modal) {
	//		super(frame, title, modal);
	//		// TODO Auto-generated constructor stub
	//	}
	//
	//	/**
	//	 * @param dialog
	//	 * @param title
	//	 * @param modal
	//	 */
	//	public CommonFontDialog(Dialog dialog, String title, boolean modal) {
	//		super(dialog, title, modal);
	//		// TODO Auto-generated constructor stub
	//	}


	//	/**
	//	 * 外部接口设置字体类型。只在LINUX环境下才生效。
	//	 * @param family 字体类型
	//	 */
	//	public void setFontFamily(String family) {
	////			fontFamily = family;
	//	}
	//
	//	/**
	//	 * 返回字体类型
	//	 * @return
	//	 */
	//	public String getFontFamily(){
	//		return fontFamily;
	//	}

	//	/**
	//	 * 设置组件字体
	//	 * @param component
	//	 */
	//	protected void setFont(Component component) {
	//		Font font = new Font(fontFamily, Font.PLAIN, component.getFont().getSize());
	//		component.setFont(font);
	//	}


	//	/**
	//	 * 初始化一个本地的默认字体类型
	//	 */
	//	private void initFontFamily() {
	//		// 使用按纽字体做为默认字体
	//		Font font = UIManager.getFont("Button.font");
	//		if(font != null) {
	//			fontFamily = font.getFamily();
	//		}
	//	}

	//	/** 字体类型 **/
	//	private String fontFamily;
	//	/**
	//	 * 返回标题栏字体
	//	 * @return 字体实例
	//	 */
	//	protected Font createTitleFont() {
	//		return new Font(fontFamily, Font.BOLD, 13);
	//	}
	

	//	private void check(String text, AbstractButton but) {
	//		Font font = but.getFont();
	//		boolean success = (font != null && canDisplay(font, text));
	//
	//		// 以上不成功，从当前环境中找到匹配的字体，设置给按纽
	//		if (!success) {
	//			setComponentFont(text, but);
	//		}
	//	}



	//	private static Properties loadSwingProperties() {
	//		/*
	//		 * Don't bother checking for Swing properties if untrusted, as there's
	//		 * no way to look them up without triggering SecurityExceptions.
	//		 */
	//		if (UIManager.class.getClassLoader() != null) {
	//			return new Properties();
	//		} else {
	//			final Properties props = new Properties();
	//
	//			java.security.AccessController
	//					.doPrivileged(new java.security.PrivilegedAction() {
	//						public Object run() {
	//							try {
	//								File file = new File(makeSwingPropertiesFilename());
	//
	//								if (file.exists()) {
	//									// InputStream has been buffered in
	//									// Properties
	//									// class
	//									FileInputStream ins = new FileInputStream(
	//											file);
	//									props.load(ins);
	//									ins.close();
	//								}
	//							} catch (Exception e) {
	//								// No such file, or file is otherwise
	//								// non-readable.
	//							}
	//
	//							// Check whether any properties were overridden at
	//							// the
	//							// command line.
	//							checkProperty(props, defaultLAFKey);
	//							checkProperty(props, auxiliaryLAFsKey);
	//							checkProperty(props, multiplexingLAFKey);
	//							checkProperty(props, installedLAFsKey);
	//							checkProperty(props, disableMnemonicKey);
	//							// Don't care about return value.
	//							return null;
	//						}
	//					});
	//			return props;
	//		}
	//    }

	//	private void testSwingPriority() {
	//		String sep = java.io.File.separator;
	//		// No need to wrap this in a doPrivileged as it's called from
	//		// a doPrivileged.
	//		String javaHome = System.getProperty("java.home");
	//		if (javaHome == null) {
	//			javaHome = "<java.home undefined>";
	//		}
	//		String filename = javaHome + sep + "lib" + sep + "swing.properties";
	//		System.out.printf("FILENAME:%s\n", filename);
	//		System.out.printf("exists:%s\n", new java.io.File(filename).exists());
	//	}

	//	/**
	//	 * 初始化一个本地的默认字体类型
	//	 */
	//	private void initFontFamily() {
	////		testSwingPriority();
	////		
	////		Font f = UIManager.getFont("TitledBorder.font");
	////		System.out.printf("title border font is: %s\n", f.getFamily());
	////		f = UIManager.getFont("Tree.font");
	////		System.out.printf("tree font is: %s\n", f.getFamily());
	////		f = UIManager.getFont("Button.font");
	////		System.out.printf("button font is: %s\n", f.getFamily());
	////		f = UIManager.getFont("Frame.font");
	////		System.out.printf("frame font is: %s\n", (f == null ? "NULL" : f.getFamily()));
	////		f = UIManager.getFont("Component.font");
	////		System.out.printf("component font is: %s\n", (f == null ? "NULL" : f.getFamily()));
	////		f = UIManager.getFont("Panel.font");
	////		System.out.printf("panel font is: %s\n\n", (f == null ? "NULL" : f.getFamily()));
	////
	////
	//////		String[] names = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(java.util.Locale.getDefault());
	//////		for(String e : names) {
	//////			System.out.printf("font is: %s\n", e);
	//////		}
	//		
	//		
	//		// 使用按纽字体做为默认字体
	//		Font font = UIManager.getFont("Button.font");
	//		if(font != null) {
	//			fontFamily = font.getFamily();
	//		}
	//		
	////		fontFamily = Font.DIALOG; // getFont().getFamily();
	//	}
}