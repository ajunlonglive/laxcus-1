/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.border.*;
import com.laxcus.util.color.*;

/**
 * 图形界面工具
 * 
 * @author scott.liang
 * @version 1.0 2/20/2020
 * @since laxcus 1.0
 */
public class UITools {

	/**
	 * 判断是METAL界面
	 * @return
	 */
	public static boolean isMetalUI() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf == null) {
			return false;
		}
		String name = laf.getID();
		return (name != null && name.equalsIgnoreCase("Metal"));
	}

	/**
	 * 判断是NIMBUS界面
	 * @return
	 */
	public static boolean isNimbusUI() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf == null) {
			return false;
		}
		String name = laf.getID();
		return (name != null && name.equalsIgnoreCase("Nimbus"));
	}

	//	private static TreeSet<String> names = new TreeSet<String>();
	//
	//	public static void print() {
	//		for (String name : names) {
	//			System.out.println(name);
	//		}
	//	}
	//
	//	public static void clear() {
	//		UITools.names.clear();
	//	}

	//	/**
	//	 * 更新面板背景颜色和前景颜色
	//	 * 
	//	 * @param component 组件
	//	 */
	//	public static void updateComponentGround(Component component) {
	//		// 忽略空指针
	//		if (component == null) {
	//			return;
	//		}
	//
	//		// 判断有弹出菜单！
	//		if (Laxkit.isClassFrom(component, JComponent.class)) {
	//			JComponent jc = (JComponent) component;
	//			JPopupMenu jpm =jc.getComponentPopupMenu();
	//			if(jpm != null && jpm.isVisible() && jpm.getInvoker() == jc) {
	//				UITools.updateComponentGround(jpm);
	//			}
	//		}
	//
	//		// 名称，过滤“J”字符
	//		String name = component.getClass().getSimpleName();
	//		// 忽略!
	//		if (name.length() < 1) {
	//			return;
	//		}
	//		// 三种情况，忽略前缀符
	//		if (name.charAt(0) == 'J') {
	//			name = name.substring(1);
	//			// 如果是弹出弹出菜单的分割符时，选择分隔符
	//			if (name.equalsIgnoreCase("PopupMenu$Separator")) {
	//				name = "Separator";
	//			}
	//		} else if (name.startsWith("Watch")) {
	//			int len = "Watch".length();
	//			name = name.substring(len);
	//		} else if (name.startsWith("Terminal")) {
	//			int len = "Terminal".length();
	//			name = name.substring(len);
	//		}
	//
	//		// 找到对应的背景
	//		String key = String.format("%s.background", name);
	//		Color background = UIManager.getColor(key);
	//		if (background != null) {
	//			component.setBackground(background);
	//		}
	//		// 找到对应的前景
	//		key = String.format("%s.foreground", name);
	//		Color foreground = UIManager.getColor(key);
	//		if (foreground != null) {
	//			component.setForeground(foreground);
	//		}
	//
	////		// 任何一个存在，保存！
	////		boolean success = (background != null || foreground != null);
	////		if (success) {
	////			UITools.names.add(name);
	////		}
	//		
	//		// 判断是滚动面板
	//		if (Laxkit.isClassFrom(component, JScrollPane.class)) {
	//			JScrollPane sub = (JScrollPane) component;
	//			JScrollBar h = sub.getHorizontalScrollBar();
	//			JScrollBar v = sub.getVerticalScrollBar();
	//			if (h != null) {
	//				UITools.updateComponentGround(h);
	//			}
	//			if (v != null) {
	//				UITools.updateComponentGround(v);
	//			}
	//			JViewport view = sub.getViewport();
	//			if (view != null) {
	//				UITools.updateComponentGround(view);
	//			}
	//		}
	//		// 判断是菜单条
	//		else if (Laxkit.isClassFrom(component, JMenuBar.class)) {
	//			JMenuBar bar = ((JMenuBar)component);
	//			int count = bar.getMenuCount();
	//			for(int i =0; i < count; i++) {
	//				JMenu menu = bar.getMenu(i);
	//				if(menu != null) {
	//					UITools.updateComponentGround(menu);
	//				}
	//			}
	//		} 
	//		// 判断是菜单
	//		else if (Laxkit.isClassFrom(component, JMenu.class)) {
	////			JMenu menu = ((JMenu) component);
	////			int count = menu.getItemCount();
	////			for (int i = 0; i < count; i++) {
	////				JMenuItem item = menu.getItem(i);
	////				// 必须判断空指针，因为出现分隔符时返回空指针
	////				if (item != null) {
	////					UITools.updateComponentGround(item);
	////				}
	////			}
	////			// JPopupMenu.Separator()
	//			
	//			// 测试
	//			JMenu menu = ((JMenu) component);
	//			int count = menu.getMenuComponentCount();
	//			for (int i = 0; i < count; i++) {
	//				Component item = menu.getMenuComponent(i);
	//				if (item != null) {
	//					UITools.updateComponentGround(item);
	//				}
	//			}
	//		}
	////		// 如果是文本框，修改光标颜色
	////		else if(Laxkit.isClassFrom(component, JTextComponent.class)) {
	////			Color arrow = UIManager.getColor("arrow.cursor");
	////			if(arrow != null) {
	////				JTextComponent sub = (JTextComponent)component;
	////				sub.setCaretColor(arrow);
	////			}
	////		}
	//		// 判断是“Container”的子类
	//		else if (Laxkit.isClassFrom(component, Container.class)) {
	//			Component[] subs = ((Container) component).getComponents();
	//			if (subs != null && subs.length > 0) {
	//				for (int i = 0; i < subs.length; i++) {
	//					UITools.updateComponentGround(subs[i]);
	//				}
	//			}
	//		}
	//	}

	//	/**
	//	 * 更新系统中的字体样式
	//	 * @param style 样式
	//	 * @return 返回更新数目
	//	 */
	//	public static int updateFontStyle(int style) {
	//		int count = 0;
	//		java.util.Enumeration<Object> enums = UIManager.getDefaults().keys();
	//		while (enums.hasMoreElements()) {
	//			Object key = enums.nextElement();
	//
	//			Font value = UIManager.getFont(key);
	//			// 更新成新的字体
	//			if (value != null) {
	//				Font font = new Font(value.getFamily(), style, value.getSize());
	//				UIManager.getDefaults().put(key, font);
	//				count++;
	//				// System.out.printf("%s {%s,%d,%d}\r\n", key.toString(), value.getFamily(), value.getStyle(), value.getSize());
	//			}
	//		}
	//		return count;
	//	}

	//	private static void println() {
	//
	//		TreeSet<String> a = new TreeSet<String>();
	//		Enumeration<Object> iterator = UIManager.getDefaults().keys();
	//		while (iterator.hasMoreElements()) {
	//			Object key = iterator.nextElement();
	//
	//			Object value = UIManager.getDefaults().get(key);
	//			if (value != null) {
	//				a.add(value.getClass().getName());
	//			}
	//		}
	//		for (String e : a) {
	//			System.out.println(e);
	//		}
	//	}

	//	/**
	//	 * 更新环境系统字体
	//	 * @param font 输入字体
	//	 * @return 返回更新数目
	//	 */
	//	public static int updateSystemFonts(Font font) {
	//		// 判断空指针
	//		Laxkit.nullabled(font);
	//
	//		int count = 0;
	//		Enumeration<Object> iterator = UIManager.getDefaults().keys();
	//		while (iterator.hasMoreElements()) {
	//			Object key = iterator.nextElement();
	//
	//			Font value = UIManager.getDefaults().getFont(key);
	//			// 空指针，忽略它！
	//			if (value == null) {
	//				continue;
	//			}
	//			// 忽略前缀是“font.”关键字，这是本地的定义！
	//			String s = key.toString();
	//			if (s != null && s.startsWith("font.")) {
	//				continue;
	//			}
	//			// 更新成新的字体
	//			if (value.getClass() == FontUIResource.class) {
	//				FontUIResource sub = new FontUIResource(font.getFamily(), font.getStyle(), font.getSize());
	//				UIManager.getDefaults().put(key, sub);
	//				count++;
	//				//				System.out.printf("FontUIResource! %s {%s,%d,%d}\r\n", key.toString(),
	//				//						value.getFamily(), value.getStyle(), value.getSize());
	//			} else if(value.getClass() == Font.class){
	//				Font sub = new Font(font.getFamily(), font.getStyle(), font.getSize());
	//				UIManager.getDefaults().put(key, sub);
	//				count++;
	//				//				System.out.printf("Font! %s {%s,%d,%d}\r\n", key.toString(),
	//				//						value.getFamily(), value.getStyle(), value.getSize());
	//			} 
	//			//			else {
	//			//				System.out.printf("not match! class is %s \r\n", value.getClass().getName());
	//			//			}
	//		}
	//
	//		//		System.out.printf("update font count %d\n", count);
	//		
	//		println();
	//
	//		return count;
	//	}

//	/**
//	 * 更新环境系统字体
//	 * @param font 输入字体
//	 * @return 返回更新数目
//	 */
//	public static int updateSystemFonts(Font font) {
//		// 判断空指针
//		Laxkit.nullabled(font);
//
//		int count = 0;
//		Enumeration<Object> iterator = UIManager.getDefaults().keys();
//		while (iterator.hasMoreElements()) {
//			Object key = iterator.nextElement();
//			// 忽略前缀是“font.”关键字，这是本地的定义！
//			String str = key.toString();
//			if (str != null && str.startsWith("font.")) {
//				continue;
//			}
//
//			Object value = UIManager.getDefaults().get(key);
//			// 空指针，忽略！
//			if (value == null) {
//				continue;
//			}
//
//			// 更新成新的字体
//			if (value.getClass() == FontUIResource.class) {
//				FontUIResource sub = new FontUIResource(font.getName(), font.getStyle(), font.getSize());
//				UIManager.getDefaults().put(key, sub);
//				count++;
//			} else if(value.getClass() == Font.class){
//				Font sub = new Font(font.getName(), font.getStyle(), font.getSize());
//				UIManager.getDefaults().put(key, sub);
//				count++;
//			} 
//		}
//
//		return count;
//	}

	/**
	 * 更新环境系统字体
	 * @param font 输入字体
	 * @return 返回更新数目
	 */
	public static int updateSystemFonts(Font font) {
		// 判断空指针
		Laxkit.nullabled(font);
		
		FontUIResource sysfrs = new FontUIResource(font.getName(), font.getStyle(), font.getSize());
		Font sysfont = new Font(font.getName(), font.getStyle(), font.getSize());

		int count = 0;
		Enumeration<Object> iterator = UIManager.getDefaults().keys();
		while (iterator.hasMoreElements()) {
			Object key = iterator.nextElement();
			// 忽略前缀是“font.”关键字，这是本地的定义！
			String str = key.toString();
			
			// "font."前缀是自定义配置参数，忽略它...
			if (str != null && str.startsWith("font.")) {
				continue;
			}

			Object value = UIManager.getDefaults().get(key);
			// 空指针，忽略！存在这种可能性！
			if (value == null) {
				continue;
			}

			// 更新成新的字体
			if (value.getClass() == FontUIResource.class) {
				UIManager.getDefaults().put(key, sysfrs);
				count++;
			} else if(value.getClass() == Font.class){
				UIManager.getDefaults().put(key, sysfont);
				count++;
			} 
		}

		return count;
	}
	
	/**
	 * 更新默认的NIMBUS组件字体
	 * @param font 更新字体
	 * @return 返回更新数目
	 */
	public static int updateNimbusSystemFonts(Font font) {
		// 这些是NIMBUS系统组件名称
		String input = "Table.font,Label.font,OptionPane.font,List.font,MenuItem.acceleratorFont,RadioButtonMenuItem.acceleratorFont,Menu.acceleratorFont,Spinner.font,ToolBar.font,TableHeader.font,CheckBox.font,FormattedTextField.font,Viewport.font,MenuItem.font,Button.font,MenuBar.font,ScrollPane.font,ProgressBar.font,RadioButton.font,CheckBoxMenuItem.font,ToggleButton.font,TextArea.font,InternalFrame.titleFont,PasswordField.font,CheckBoxMenuItem.acceleratorFont,EditorPane.font,TextPane.font,TabbedPane.font,Menu.font,DesktopIcon.font,TextField.font,ColorChooser.font,PopupMenu.font,RadioButtonMenuItem.font,ToolTip.font,TitledBorder.font,Panel.font,Tree.font,Slider.font,ComboBox.font,Panel.font,FormattedTextField.font,ScrollPane.font,Table.font,CheckBoxMenuItem.font,FileChooser.font,TextArea.font,PopupMenu.font,TitledBorder.font,SplitPane.font,EditorPane.font,Spinner.font,SliderTrack.font,CheckBox.font,MenuBar.font,Tree.font,ArrowButton.font,MenuItem.font,Menu.font,DesktopIcon.font,ScrollBar.font,ComboBox.font,InternalFrameTitlePane.font,Viewport.font,DesktopPane.font,ColorChooser.font,TabbedPane.font,PopupMenuSeparator.font,SliderThumb.font,RadioButtonMenuItem.font,TextField.font,Separator.font,PasswordField.font,ToolBar.font,ToolTip.font,InternalFrame.font,RootPane.font,ToggleButton.font,List.font,Label.font,defaultFont,ScrollBarTrack.font,TextPane.font,OptionPane.font,ProgressBar.font,Slider.font,RadioButton.font,ScrollBarThumb.font,InternalFrame.titleFont,TableHeader.font,Button.font";
		String[] texts = input.split(",");
		FontUIResource resource = new FontUIResource(font.getName(), font.getStyle(), font.getSize());
		int count = 0;
		for (String key : texts) {
			key = key.trim();
			if (key.length() > 0) {
				UIManager.getDefaults().put(key, resource);
				count++;
			}
		}
		return count;
	}

	/**
	 * 更新METAL组件字体
	 * @param font 字体实例
	 * @return 返回更新数目
	 */
	public static int updateMetalSystemFonts(Font font) {
		// 这些是METAL系统组件名称
		String input = "Table.font,Label.font,OptionPane.font,List.font,MenuItem.acceleratorFont,RadioButtonMenuItem.acceleratorFont,Menu.acceleratorFont,Spinner.font,ToolBar.font,TableHeader.font,CheckBox.font,FormattedTextField.font,Viewport.font,MenuItem.font,Button.font,MenuBar.font,ScrollPane.font,ProgressBar.font,RadioButton.font,CheckBoxMenuItem.font,ToggleButton.font,TextArea.font,InternalFrame.titleFont,PasswordField.font,CheckBoxMenuItem.acceleratorFont,EditorPane.font,TextPane.font,TabbedPane.font,Menu.font,DesktopIcon.font,TextField.font,ColorChooser.font,PopupMenu.font,RadioButtonMenuItem.font,ToolTip.font,TitledBorder.font,Panel.font,Tree.font,Slider.font,ComboBox.font,List.font,TableHeader.font,Panel.font,TextArea.font,ToggleButton.font,ComboBox.font,ScrollPane.font,Spinner.font,RadioButtonMenuItem.font,Slider.font,EditorPane.font,OptionPane.font,ToolBar.font,Tree.font,CheckBoxMenuItem.font,TitledBorder.font,Table.font,MenuBar.font,PopupMenu.font,DesktopIcon.font,Label.font,MenuItem.font,MenuItem.acceleratorFont,TextField.font,TextPane.font,CheckBox.font,ProgressBar.font,FormattedTextField.font,CheckBoxMenuItem.acceleratorFont,Menu.acceleratorFont,ColorChooser.font,Menu.font,PasswordField.font,InternalFrame.titleFont,RadioButtonMenuItem.acceleratorFont,Viewport.font,TabbedPane.font,RadioButton.font,ToolTip.font,Button.font";
		String[] texts = input.split(",");
		FontUIResource resource = new FontUIResource(font.getName(), font.getStyle(), font.getSize());
		int count = 0;
		for (String key : texts) {
			key = key.trim();
			if (key.length() > 0) {
				UIManager.getDefaults().put(key, resource);
				count++;
			}
		}
		return count;
	}

	/**
	 * 更改L&F默认值
	 * @param key 键值
	 * @param value 属性值
	 * 
	 * @return 返回被替换的值，没有是空指针
	 */
	public static Object putLookAndFeelProperity(Object key, Object value) {
		return UIManager.getLookAndFeelDefaults().put(key, value);
	}

	/**
	 * 更改开发者默认值
	 * @param key 键值
	 * @param value 属性值
	 * 
	 * @return 返回被替换的值，没有是空指针
	 */
	public static Object putProperity(Object key, Object value) {
		return UIManager.getDefaults().put(key, value);
	}

	/**
	 * 提取开发者默认值
	 * @param key 键值
	 * @return 返回实例，或者空指针
	 */
	public static Object getProperity(Object key) {
		return UIManager.getDefaults().get(key);
	}
	
	/**
	 * 加载类
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static Class<?> loadSystemClass(String className) throws ClassNotFoundException {
		return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * 更新界面主题
	 * @param themeClazz 主题类
	 * @return 成功返回真，否则假
	 */
	private static boolean updateTheme(String themeClazz) {
		try {
			Class<?> clazz = loadSystemClass(themeClazz);
			Object theme = clazz.newInstance();
			// 判断继承自ThemeLoader接口
			if (!Laxkit.isInterfaceFrom(theme, ThemeLoader.class)) {
				return false;
			}
			// 判断是Metal主题子类
			if (!Laxkit.isClassFrom(theme, MetalTheme.class)) {
				return false;
			}
			// 1. 加载配置颜色，这些配置颜色决定图形界面
			((ThemeLoader) theme).loadConfigure();
			// 2. 设置当前主题，系统使用这些主题生效！
			MetalLookAndFeel.setCurrentTheme((MetalTheme) theme);
			return true;
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		}
		return false;
	}
	
//	private static void loadDefaultsUI(String laf) {
//		if (!"Metal".equalsIgnoreCase(laf)) {
//			return;
//		}
//
//		//		TreeMap<String, String> tm = new TreeMap<String, String>();
//		//		tm.put("", "");
//		//		tm.put("", "");
//
//		// ScrollBarUI , java.lang.String] -> [javax.swing.plaf.metal.MetalScrollBarUI
//
//		// 注意！只修改对应适配的LookAndFeel实例！
//		UIDefaults def = UIManager.getLookAndFeelDefaults();
//		def.put("ScrollBarUI", "com.laxcus.ui.FlatScrollBarUI");
//		def.put("InternalFrameUI", "com.laxcus.ui.FlatInternalFrameUI");
//		def.put("SplitPaneUI", "com.laxcus.ui.FlatSplitPaneUI");
//		
////		UIManager.put("ScrollBarUI", "com.laxcus.ui.FlatScrollBarUI");
////		UIManager.put("InternalFrameUI", "com.laxcus.ui.FlatInternalFrameUI");
////		UIManager.put("SplitPaneUI", "com.laxcus.ui.FlatSplitPaneUI");
//	}
	
	/**
	 * 加载自定义的皮肤UI配置
	 * 
	 * @param sheet 皮肤UI表
	 * @return 成功返回真，否则假
	 */
	private static boolean loadSkinSheet(SkinSheet sheet) {
		if (sheet == null) {
			return false;
		}
		// 注意！只修改对应适配的LookAndFeel实例！
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		if (defaults == null) {
			return false;
		}

		// 输出到对应的LookAndFeel，只是对应的LookAndFeel !!!
		SkinSheet.SkinElement[] elements = sheet.toArray();
		for (SkinSheet.SkinElement e : elements) {
			defaults.put(e.getKey(), e.getValue());
		}
		return true;
	}

	/**
	 * 改成成指定的界面
	 * @param laf 外观关键字，包括“Metal,Nimbus.CDE/Motif,Windows,Windows Classic”
	 * @param skinName 皮肤名字
	 * @param themeClazz 主题加载类
	 * @param sheet 皮肤配置表
	 * 
	 * @return 更新成功返回真，否则假
	 */
	public static boolean updateLookAndFeel(final String laf, final String themeClazz, SkinSheet sheet) {
		// 取出系统定义中的L&F外面集合
		UIManager.LookAndFeelInfo[] elements = UIManager.getInstalledLookAndFeels();

		// 找到指定的界面，加载它！
		for (int i = 0; elements != null && i < elements.length; i++) {
			UIManager.LookAndFeelInfo element = elements[i];
			String name = element.getName();
			// 名称不一致，忽略！
			if (!laf.equalsIgnoreCase(name)) {
				continue;
			}

			// 如果是标准JAVA界面，不要粗字体
			if ("Metal".equalsIgnoreCase(laf)) {
				UIManager.getDefaults().put("swing.boldMetal", Boolean.FALSE);
			}

			// 主题类有效，生成实例，设置为当前主题
			if (themeClazz != null) {
				boolean success = updateTheme(themeClazz);
				if (!success) return false;
			}

			// 设置外观
			try {
				UIManager.setLookAndFeel(element.getClassName());
				//				// 在这里，判断是METAL界面，更新自定义的javax.swing.palf.ComponentUI
				//				loadDefaultsUI(laf);

				// 输出自定义的皮肤UI，系统会根据这些皮肤UI，产生各种自定义的皮肤界面
				UITools.loadSkinSheet(sheet);

				return true;
			} catch (UnsupportedLookAndFeelException e) {
				Logger.error(e);
			} catch (ClassNotFoundException e) {
				Logger.error(e);
			} catch (InstantiationException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			}
		}
		return false;
	}

	/**
	 * 在表有效情况下，设置表格的单元格之间，高度与高度，宽度与宽度之间的间距。固定是2个像系！
	 * @param table 表格实例
	 */
	public static void setTableIntercellSpacing(JTable table) {
		if (table != null) {
			// 将 rowMargin 和 columnMargin（单元格之间间距的高度和宽度）设置为 intercellSpacing
			if (Skins.isNimbus()) {
				table.setIntercellSpacing(new Dimension(0, 0)); // NIMBUS界面不要空格
			} else {
				table.setIntercellSpacing(new Dimension(2, 2));
			}
		}
	}

	/**
	 * 判断字体能够正确显示一行文本
	 * @param font 字体
	 * @param str 文本
	 * @return 返回真或者假
	 */
	private static boolean canDisplay(Font font, String str) {
		return font.canDisplayUpTo(str) == -1;
	}

	/**
	 * 选择一个有效的标题栏字体
	 * @param str 显示文本
	 * @return 匹配的字体
	 */
	public static Font createTitledBorderFont(String str) {
		Font defaults = UIManager.getFont("TitledBorder.font");
		// 避免是FontUIResource, 必须是一个实际的Font实例
		if (defaults != null && canDisplay(defaults, str)) {
			return new Font(defaults.getName(), Font.BOLD, defaults.getSize());
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
	 * 重新定义头部字体，在12 - 18磅之间!
	 * @param font 字体实例
	 * @return 返回修改后的字体！
	 */
	public static Font createHeaderFont(Font font) {
		if (font != null) {
			int size = font.getSize(); // (Laxkit.isLinux() ? 14 : 12);
			if (size < 12) {
				size = 12;
			} else if (size > 18) {
				size = 18;
			}
			return new Font(font.getName(), font.getStyle(), size);
		}
		return null;
	}

	/**
	 * 建立标题平面边框
	 * @param gap 间隔，以像素为单位！
	 * @return Border实例
	 */
	private static Border createTitledEmptyBorder(int gap) {
		if (gap < 0) {
			gap = 0;
		}
		// top, left, bottom, right
		return new EmptyBorder(gap, gap, gap, gap);
	}

	/**
	 * 生成平面边框
	 * @param title 标题名称
	 * @return TitledBorder实例
	 */
	private static TitledBorder createPlainTitledBorder(String title, int gap) {
		if (title == null) {
			title = "";
		}

		Font font = createTitledBorderFont(title);
//		// 连缘线条色
//		Color background = Skins.findBorderLineBackground(); 
//		if (background == null) {
//			background = Color.BLACK; // 默认是黑色
//		}
		
		Color foreground = Skins.findBorderLineForeground();
		if (foreground == null) {
			foreground = Color.BLACK; // 默认是黑色
		}

//		Border insider = new LineBorder(background);
		Border insider = new HighlightBorder(1);
		Border compound = new CompoundBorder(insider, createTitledEmptyBorder(gap));

		return new FlatTitledBorder(compound, title, TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
				font, foreground);
	}

	/**
	 * 生成3D标题边框
	 * @param title 标题
	 * @return Border实例
	 */
	private static TitledBorder create3DTitledBoder(String title, int gap) {
		if (title == null) {
			title = "";
		}
		Font font = createTitledBorderFont(title);
		CompoundBorder compound = new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), createTitledEmptyBorder(gap));
		return new FlatTitledBorder(compound, title, TitledBorder.CENTER, TitledBorder.ABOVE_TOP, font);
	}

	/**
	 * 判断当前UI界面，生成带标题的边框
	 * @param title 标题
	 * @return  TitledBorder实例
	 */
	public static TitledBorder createTitledBorder(String title, int gap) {
		if (Skins.isNimbus()) {
			return create3DTitledBoder(title, gap);
		} else {
			return createPlainTitledBorder(title, gap);
		}
	}

	/**
	 * 判断当前UI界面，生成带标题的边框
	 * @param title 标题
	 * @return  TitledBorder实例
	 */
	public static TitledBorder createTitledBorder(String title) {
		return createTitledBorder(title, 8);
	}
	
	/**
	 * 判断当前UI界面，生成不带标题的边框
	 * @return  TitledBorder实例
	 */
	public static TitledBorder createTitledBorder() {
		return createTitledBorder(null);
	}
	
	/**
	 * 根据属性名称，查找匹配的颜色值。
	 * 
	 * @param name 名称
	 * @param defaultColor 默认颜色
	 * @return 返回颜色
	 */
	public static Color findColor(String name, Color defaultColor) {
		Object element = UIManager.getDefaults().getColor(name);
		if (element != null) {
			if (element.getClass() == ColorUIResource.class) {
				ColorUIResource e = (ColorUIResource) element;
				return new Color(e.getRGB());
			} else if (element.getClass() == Color.class) {
				return (Color) element;
			}
		} else {
			// 从颜色模板中查找参数
			return ColorTemplate.findColor(name, defaultColor);
		}
		return defaultColor;
	}

	/**
	 * 根据属性名称，查找匹配的颜色值 
	 * @param name 名称
	 * @return 返回匹配的颜色，或者空指针
	 */
	public static Color findColor(String name) {
		return UITools.findColor(name, null);
	}

	// UIManager 类实例，参数的输出和输入调用接口

	//	/**
	//     * Returns the defaults. The returned defaults resolve using the
	//     * logic specified in the class documentation.
	//     *
	//     * @return a <code>UIDefaults</code> object containing the default values
	//     */
	//    public static UIDefaults getDefaults() {
	//        maybeInitialize();
	//        return getLAFState().multiUIDefaults;
	//    }
	//    
	//    /**
	//     * Returns a font from the defaults. If the value for {@code key} is
	//     * not a {@code Font}, {@code null} is returned.
	//     *
	//     * @param key  an <code>Object</code> specifying the font
	//     * @return the <code>Font</code> object
	//     * @throws NullPointerException if {@code key} is {@code null}
	//     */
	//    public static Font getFont(Object key) { 
	//        return getDefaults().getFont(key); 
	//    }
	//    
	//     /**
	//     * Returns a color from the defaults. If the value for {@code key} is
	//     * not a {@code Color}, {@code null} is returned.
	//     *
	//     * @param key  an <code>Object</code> specifying the color
	//     * @return the <code>Color</code> object
	//     * @throws NullPointerException if {@code key} is {@code null}
	//     */
	//    public static Color getColor(Object key) { 
	//        return getDefaults().getColor(key); 
	//    }
	//    
	//     /**
	//     * Stores an object in the developer defaults. This is a cover method
	//     * for {@code getDefaults().put(key, value)}. This only effects the
	//     * developer defaults, not the system or look and feel defaults.
	//     *
	//     * @param key    an <code>Object</code> specifying the retrieval key
	//     * @param value  the <code>Object</code> to store; refer to
	//     *               {@code UIDefaults} for details on how {@code null} is
	//     *               handled
	//     * @return the <code>Object</code> returned by {@link UIDefaults#put}
	//     * @throws NullPointerException if {@code key} is {@code null}
	//     * @see UIDefaults#put
	//     */
	//    public static Object put(Object key, Object value) { 
	//        return getDefaults().put(key, value); 
	//    }

}