/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.awt.Color;

import javax.swing.*;

/**
 * 图形界面颜色键值集合。<br><br>
 * 
 * 这些颜色键值和参数在颜色的"conf/color/config.xml"配置里面的 "nimbus_normal.txt", "metal_drak.txt"文件中定义，TERMINAL/WATCH图形界面启动时加载！<br><br>
 * 
 * 所有参数的查找，优先选择“Nimbus”外观的定义，其次是“Metal”外观。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/15/2020
 * @since laxcus 1.0
 */
public class Skins {

	/** Nimbus外观 **/
	public static final int Nimbus = 1;

	/** Metal外观 **/
	public static final int Metal = 2;

	/** 外观界面关键字 **/
	private static int lookAndFeel = 0;
	
	/** 皮肤名称 **/
	private static String skinName;
	
	/**
	 * 设置感知外观
	 * @param who 类型
	 * @return 设置成功返回真，否则假
	 */
	public static boolean setLookAndFeel(int who) {
		switch (who) {
		case Skins.Nimbus:
		case Skins.Metal:
			Skins.lookAndFeel = who;
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 设置感知外观。关键字与系统的"UIManager.LookAndFeelInfo"中名称保持一致！<br><br>
	 * 目前只有“Nimbus”和“Metal”两种。<br><br>
	 * 
	 * @param name 关键字名称
	 * @return 设置成功返回真，否则假
	 */
	public static boolean setLookAndFeel(String name) {
		if ("Nimbus".equalsIgnoreCase(name)) {
			return Skins.setLookAndFeel(Skins.Nimbus);
		} else if ("Metal".equalsIgnoreCase(name)) {
			return Skins.setLookAndFeel(Skins.Metal);
		}
		return false;
	}
	
	/**
	 * 返回感知外观
	 * @return 整数
	 */
	public static int getLookAndFeel() {
		return Skins.lookAndFeel;
	}

	/**
	 * 判断是Nimbus外观
	 * @return 返回真或者假
	 */
	public static boolean isNimbus() {
		return Skins.lookAndFeel == Skins.Nimbus;
	}

	/**
	 * 判断是Metal外观
	 * @return 返回真或者假
	 */
	public static boolean isMetal() {
		return Skins.lookAndFeel == Skins.Metal;
	}
	
	/**
	 * 当前皮肤名称，对应配置脚本文件中的name="dark"参数
	 * @param s 字符名称
	 */
	public static void setSkinName(String s) {
		Skins.skinName = s;
	}

	/**
	 * 返回当前皮肤名称
	 * @return
	 */
	public static String getSkinName() {
		return Skins.skinName;
	}
	
	/**
	 * 判断是灰色皮肤
	 * @return 返回真或者假
	 */
	public static boolean isGraySkin() {
		String s = Skins.skinName;
		if (s != null) {
			return s.toLowerCase().indexOf("gray") >= 0;
		}
		return false;
		
		// return "gray".equalsIgnoreCase(Skins.skinName);
	}

	/**
	 * 判断是暗黑皮肤
	 * @return 返回真或者假
	 */
	public static boolean isDarkSkin() {
		return "dark".equalsIgnoreCase(Skins.skinName);
	}

	/**
	 * 判断是绿皮肤
	 * @return 返回真或者假
	 */
	public static boolean isBronzSkin() {
		return "bronze".equalsIgnoreCase(Skins.skinName);
	}

	/**
	 * 判断是深兰皮肤
	 * @return 返回真或者假
	 */
	public static boolean isCyanoSkin() {
		return "cyano".equalsIgnoreCase(Skins.skinName);
	}

	/**
	 * 查找颜色值 
	 * @param key 关键值
	 * @param defaultKey 默认关键
	 * @return 返回颜色，或者空指针
	 */
	public static Color findColor(String key, String defaultKey) {
		Color color = UIManager.getDefaults().getColor(key);
		// 没有找到，取默认值
		if (color == null && defaultKey != null) {
			color = UIManager.getDefaults().getColor(defaultKey);
		}
		return color;
	}

	/**
	 * 查找颜色值
	 * @param key 关键字
	 * @return 返回颜色，或者空指针
	 */
	public static Color findColor(String key) {
		return Skins.findColor(key, null);
	}
	

	/**
	 * 命令窗口前景颜色（字体颜色）
	 * @return
	 */
	public static Color findCommandPaneCommandForeground() {
		return Skins.findColor("CommandPane.textCommand");
	}

	/**
	 * 命令窗口关键字颜色
	 * @return
	 */
	public static Color findCommandPaneKeywordForeground() {
		return Skins.findColor("CommandPane.textKeyword");
	}

	/**
	 * 命令窗口类型颜色
	 * @return
	 */
	public static Color findCommandPaneTypeForeground() {
		return Skins.findColor("CommandPane.textType");
	}

	/**
	 * 命令窗口正常颜色（字体颜色）
	 * @return
	 */
	public static Color findCommandPaneNormalForeground() {
		return Skins.findColor("CommandPane.textNormal");
	}

	/**
	 * 命令窗口背景颜色
	 * @return
	 */
	public static Color findCommandPaneBackground() {
		return Skins.findColor("CommandPane.background");
	}
	
	/**
	 * 命令窗口的光标颜色
	 * @return 返回指定颜色，或者空指针
	 */
	public static Color findCommandPaneCursor() {
		return Skins.findColor("CommandPane.cursor");
	}

	/**
	 * 命令窗口的警告颜色
	 * @return
	 */
	public static Color findCommandPaneWarningForeground() {
		return Skins.findColor("CommandPane.textWarning");
	}

	/**
	 * 命令窗口的故障颜色
	 * @return
	 */
	public static Color findCommandPaneFaultForeground() {
		return Skins.findColor("CommandPane.textFault");
	}

	
//	/** 命令面板的命令字颜色 **/
//	public static final String CommandPane_textCommand = "CommandPane.textCommand";
//
//	public static final String CommandPane_textKeyword = "CommandPane.textKeyword";
//
//	public static final String CommandPane_textType = "CommandPane.textType";
//
//	public static final String CommandPane_textNormal = "CommandPane.textNormal";
//	
//	public static final String CommandPane_background = "CommandPane.background";

	// // 命令/关键字/数据类型/普通文本，四种颜色
	// setCommandColor(new Color(0, 177, 106).getRGB());
	// setKeywordColor(new Color(20, 162, 212).getRGB());
	// setTypeColor(new Color(0, 92, 255).getRGB());
	// setNormalColor(Color.black.getRGB());
	
	/**
	 * 返回树形结构背景色
	 */
	public static Color findTreeBackground() {
		return Skins.findColor("Tree.background");
	}

	/**
	 * 返回树文本前景色
	 * @return
	 */
	public static Color findTreeTextForeground() {
		return findColor("Tree.textForeground");
	}

	/**
	 * 返回树文本背景色
	 * @return
	 */
	public static Color findTreeTextBackground() {
		return findColor("Tree.textBackground");
	}
	
	/**
	 * 返回树文本选中后的前景色
	 * @return
	 */
	public static Color findTreeTextSelectForeground() {
		return findColor("Tree.selectionForeground");
	}

	/**
	 * 返回树文本选中后的背景色
	 * @return
	 */
	public static Color findTreeTextSelectBackground() {
		return findColor("Tree.selectionBackground");
	}

	/**
	 * 返回面板前景色
	 * @return
	 */
	public static Color findPanelForeground() {
		return findColor("Panel.foreground");
	}

	/**
	 * 返回面板背景色
	 * @return
	 */
	public static Color findPanelBackground() {
		return findColor("Panel.background");
	}

	
//	public static final String Tree_background = "Tree.background";
//	public static final String Tree_selectionForeground = "Tree.selectionForeground";
//	public static final String Tree_textForeground = "Tree.textForeground";
//	public static final String Tree_selectionBackground = "Tree.selectionBackground";
//	public static final String Tree_textBackground = "Tree.textBackground";
	
//	public static final String Panel_background = "Panel.background";
//	public static final String Panel_foreground = "Panel.foreground";
	
	/** 表格颜色 **/
//	public static final String Table_background = "Table.background";
//	public static final String Table_textForeground = "Table.textForeground";
//	public static final String Table_cellRenderer_background = "Table:\"Table.cellRenderer\".background";
//	public static final String Table_Enabled_Selected_textBackground = "Table[Enabled+Selected].textBackground";
//	public static final String Table_Enabled_Selected_textForeground = "Table[Enabled+Selected].textForeground";
	
	/**
	 * 返回表格的背景色
	 * @return
	 */
	public static Color findTableBackground() {
		return findColor("Table.background");
	}

	/**
	 * 返回表格的文本前景色
	 * @return
	 */
	public static Color findTableForeground() {
		return findColor("Table.foreground");
	}

	/**
	 * 返回表本文本的前景色
	 * @return
	 */
	public static Color findTableTextForeground() {
		if (isNimbus()) {
			return findColor("Table.textForeground");
		}
		// 是METAL界面
		return findColor("Table.foreground");
	}
	
	/**
	 * 返回表本文本的背景色
	 * @return
	 */
	public static Color findTableTextBackground() {
		if (isNimbus()) {
			return findColor("Table.background");
		}
		// 是METAL界面
		return findColor("Table.background");
	}

	/**
	 * 返回表格文本被选中的背景色
	 * @return
	 */
	public static Color findTableTextSelectBackground() {
		if (isNimbus()) {
			return findColor("Table[Enabled+Selected].textBackground");
		}
		return findColor("Table.selectionBackground");
	}

	/**
	 * 返回表格文本被选中的前景色
	 * @return
	 */
	public static Color findTableTextSelectForeground() {
		if (isNimbus()) {
			return findColor("Table[Enabled+Selected].textForeground");
		}
		return findColor("Table.selectionForeground");
	}

	/**
	 * 返回表格头背景
	 * @return
	 */
	public static Color findTableHeaderBackground() {
		return findColor("TableHeader.background");
	}
	
	/**
	 * 返回表格头前景色
	 * @return 颜色
	 */
	public static Color findTableHeaderForeground() {
		return findColor("TableHeader.foreground");
	}
	
	/**
	 * 返回表格头的选中后的背景色
	 * @return
	 */
	public static Color findTableHeaderFocusBackground() {
		if (isNimbus()) {
			return findColor("TableHeader.focusCellBackground");
		}
		return findColor("Table.dropCellBackground");
	}
	
//	Table[Enabled+Selected].textBackground {57,105,138}
//	Table[Enabled+Selected].textForeground {255,255,255}
	
	
//	/** 表格头背景 **/
//	public static final String TableHeader_background ="TableHeader.background";
	
	
	
//	/** 亮白 **/
//	public static final String controlHighlight = "controlHighlight";
	
//	// LIST背景\前景
//	public static final String List_Selected_textBackground = "List[Selected].textBackground";
//	public static final String List_Selected_textForeground = "List[Selected].textForeground";
//	public static final String List_background ="List.background";
//	public static final String List_foreground ="List.foreground";

	/**
	 * 返回列表的文本选中背景色
	 * @return
	 */
	public static Color findListTextSelectBackground() {
		if (isNimbus()) {
			return findColor("List[Selected].textBackground");
		}
		return findColor("List.selectionBackground");
	}

	/**
	 * 返回列表的文本选中背景色
	 * @return
	 */
	public static Color findListTextSelectForeground() {
		if (isNimbus()) {
			return findColor("List[Selected].textForeground");
		}
		return findColor("List.selectionForeground");
	}
	
	/**
	 * 返回列表背景色
	 * @return
	 */
	public static Color findListBackground() {
		return findColor("List.background");
	}
	
	/**
	 * 返回列表前景色
	 * @return
	 */
	public static Color findListForeground() {
		return findColor("List.foreground");
	}
	
//	// 面板颜色
//	public static final String TextPane_background = "TextPane.background";
	
	/**
	 * 返回列表前景色
	 * @return
	 */
	public static Color findTextPanelBackground() {
		return findColor("TextPane.background");
	}
	
//	/** SITE RUNTIME 模型，空间满 **/
//	public static final String SiteRuntime_fullText ="SiteRuntime.fullText";
	
	/**
	 * 返回WATCH节点的集群站点参数溢出时的前景色，nimbus_normal.txt中定义，默认是红色
	 * @return Color实例，或者空指针
	 */
	public static Color findSiteRuntimeFullText() {
		return findColor("SiteRuntime.fullText");
	}
	
//	// 提示前景色
//	public static final String Notice_CommentTextForeground = "MessagePanel.CommentTextForeground";
//	public static final String Notice_WarningTextForeground = "MessagePanel.WarningTextForeground";
//	public static final String Notice_FaultTextForeground = "MessagePanel.FaultTextForeground";
	
	/**
	 * 返回提示栏注释前景色
	 * @return
	 */
	public static Color findMessagePanelMessageForeground() {
		return findColor("MessagePanel.CommentTextForeground");
	}

	/**
	 * 返回提示栏警告前景色
	 * @return
	 */
	public static Color findMessagePanelWarningForeground() {
		return findColor("MessagePanel.WarningTextForeground");
	}

	/**
	 * 返回提示栏故障前景色
	 * @return
	 */
	public static Color findMessagePanelFaultForeground() {
		return findColor("MessagePanel.FaultTextForeground");
	}
	
//	/**
//	 * 返回列表前景色
//	 * @return
//	 */
//	public static Color findListForeground() {
//		return findColor("List.foreground");
//	}
//	
//	/**
//	 * 返回列表前景色
//	 * @return
//	 */
//	public static Color findListForeground() {
//		return findColor("List.foreground");
//	}
//	
//	/**
//	 * 返回列表前景色
//	 * @return
//	 */
//	public static Color findListForeground() {
//		return findColor("List.foreground");
//	}
	
//	// 标题框边缘线
//	public static final String Border_lineBackground = "Border.lineBackground";
//	public static final String Border_textForeground = "Border.textForeground";
	
	/**
	 * 返回边框线背景色
	 * @return
	 */
	public static Color findBorderLineBackground() {
		return findColor("Border.lineBackground");
	}
	
	/**
	 * 返回边框线前景色。首选标签前景色，不成立再选边框文本色
	 * @return
	 */
	public static Color findBorderLineForeground() {
		Color color = findColor("Label.foreground");
		if (color == null) {
			color = findColor("Border.textForeground");
		}
		return color;
	}
	
	/**
	 * 返回HTML帮助面板的背景色（这个参数是自定义）
	 * @return 颜色
	 */
	public static Color findHTMLHelpPanelBackground() {
		return findColor("HTMLHelpPane.background");
	}
	
	/**
	 * 返回HTML帮助面板的前景色（这个参数是自定义）
	 * @return 颜色
	 */
	public static Color findHTMLHelpPanelTextForeground() {
		return findColor("HTMLHelpPane.textForeground");
	}
	
	/**
	 * 返回HTML帮助面板的链接颜色（这个参数是自定义）
	 * @return
	 */
	public static Color findHTMLHelpPanelHerf() {
		return findColor("HTMLHelpPane.textHerf");
	}

//	/**
//	 * 返回控件的暗黑阴影颜色
//	 * @return
//	 */
//	public static Color findControlDarkShadown() {
//		return findColor("controlDkShadow");
//		//		Color shadown = UIManager.getColor("controlDkShadow");
//		//		if (shadown == null) {
//		//			shadown = Color.BLACK;
//		//		}
//		//		Color highlight = UIManager.getColor("controlHighlight");
//		//		if (highlight == null) {
//		//			highlight = Color.WHITE;
//		//		}
//	}
//
//	/**
//	 * 返回控件的明亮色
//	 * @return
//	 */
//	public static Color findControlHighlight() {
//		return findColor("controlHighlight");
//	}

	/**
	 * 边框阴影线
	 * @return
	 */
	public static Color findBorderShadownLine() {
		return findColor("Border.lineShadow");
	}

	/**
	 * 边框亮线
	 * @return
	 */
	public static Color findBorderLightLine() {
		return findColor("Border.lineLight");
	}

}