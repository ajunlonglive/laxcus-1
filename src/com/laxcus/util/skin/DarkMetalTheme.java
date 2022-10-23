/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.color.*;

/**
 * 暗黑主题
 * 
 * @author scott.liang
 * @version 1.0 2/29/2020
 * @since laxcus 1.0
 */
public class DarkMetalTheme extends DefaultMetalTheme implements ThemeLoader {

	// 颜色由深向淡
	private ColorUIResource primary1;

	private ColorUIResource primary2;

	private ColorUIResource primary3;

	// 颜色由深入淡
	private ColorUIResource secondary1;
	private ColorUIResource secondary2;
	private ColorUIResource secondary3;

	// 黑/白
	private ColorUIResource black;
	private ColorUIResource white;

	/** 加速器前景颜色 **/
	private ColorUIResource acceleratorForeground;

	/** 桌面颜色 **/
	private ColorUIResource desktop;

	/** 标题栏激活状态下的前景/背景 **/
	private ColorUIResource windowTitleForeground;
	private ColorUIResource windowTitleBackground;

	/** 标题栏非激活背景 **/
	private ColorUIResource windowTitleInactiveForeground;
	private ColorUIResource windowTitleInactiveBackground;

	/**
	 * 构造默认暗黑主题
	 */
	public DarkMetalTheme() {
		super();
	}

	/**
	 * 从系统中找到匹配的颜色值
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	private ColorUIResource findColor(String name, ColorUIResource defaultValue) {
		java.awt.Color color = UIManager.getColor(name);
		if (color == null) {
			return defaultValue;
		}
		return new ColorUIResource(color);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.skin.ThemeLoader#loadConfigure()
	 */
	@Override
	public void loadConfigure() {
		primary1 = findColor("dark.primary1", new  ColorUIResource(55, 55, 55));
		primary2 = findColor("dark.primary2", new ColorUIResource(77, 77, 77));
		primary3 = findColor("dark.primary3", new ColorUIResource(99, 99, 99));

		secondary1 = findColor("dark.secondary1", new ColorUIResource(0, 0, 0));
		secondary2 = findColor("dark.secondary2", new ColorUIResource(51, 51, 51));
		secondary3 = findColor("dark.secondary3", new ColorUIResource(102, 102, 102));

		black = findColor("dark.black", new ColorUIResource(198, 198, 198));
		white = findColor("dark.white", new ColorUIResource(33, 33, 33));

		// 桌面
		desktop = findColor("dark.desktop", new ColorUIResource(45, 45, 45));

		// 标题栏背景
		windowTitleForeground = findColor("dark.windowTitleForeground", black);
		windowTitleBackground = findColor("dark.windowTitleBackground", primary3);

		// 标题栏激活状态下的前景/背景
		windowTitleForeground = findColor("dark.windowTitleForeground", black);
		windowTitleBackground = findColor("dark.windowTitleBackground", primary3);

		// 标题栏非激活状态下的前景/背景
		windowTitleInactiveForeground = findColor("dark.windowTitleInactiveForeground", black);
		windowTitleInactiveBackground = findColor("dark.windowTitleInactiveBackground", secondary3);

		//		// 标题栏激活/非激活颜色 
		//		windowTitleBackground = findColor("dark.windowTitleBackground", primary3);
		//		windowTitleInactiveBackground = findColor("dark.windowTitleInactiveBackground", secondary3); 

		upateAcceleratorForeground();
	}

	private void upateAcceleratorForeground(){
		// 加速器颜色，加亮50
		ESL esl = new ESL(primary1);
		esl.brighter(50);
		acceleratorForeground = new ColorUIResource(esl.toColor().getRGB());
	}


	public String getName() { return "LaxcusDarkTheme"; }

	protected ColorUIResource getPrimary1() { return primary1; }
	protected ColorUIResource getPrimary2() { return primary2; }
	protected ColorUIResource getPrimary3() { return primary3; }

	protected ColorUIResource getSecondary1() { return secondary1; }
	protected ColorUIResource getSecondary2() { return secondary2; }
	protected ColorUIResource getSecondary3() { return secondary3; }

	protected ColorUIResource getBlack() { return black; }
	protected ColorUIResource getWhite() { return white; }

	public ColorUIResource getAcceleratorForeground(){
		return acceleratorForeground;
	}

	public ColorUIResource getDesktopColor() {
		return desktop;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalTheme#getMenuDisabledForeground()
	 */
	@Override
	public ColorUIResource getMenuDisabledForeground() {
		// 基于菜单前景，调暗颜色！
		ColorUIResource c = super.getMenuForeground();
		return new ColorUIResource(c.darker());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalTheme#getMenuSelectedBackground()
	 */
	@Override
	public ColorUIResource getMenuSelectedBackground() {
		ColorUIResource c = super.getMenuForeground();
		return new ColorUIResource(c);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalTheme#getMenuSelectedForeground()
	 */
	@Override
	public ColorUIResource getMenuSelectedForeground() {
		ColorUIResource c = super.getMenuBackground();
		return new ColorUIResource(c);
	}

	/*
	 * 高亮文本颜色
	 * @see javax.swing.plaf.metal.MetalTheme#getHighlightedTextColor()
	 */
	@Override
	public ColorUIResource getHighlightedTextColor() {
		ColorUIResource e = super.getControlTextColor();
		// 更亮！
		return new ColorUIResource(e.brighter());
	}

	/*
	 * 非激活文本颜色，比目前颜色暗！
	 * @see javax.swing.plaf.metal.MetalTheme#getInactiveControlTextColor()
	 */
	@Override
	public ColorUIResource getInactiveControlTextColor() {
		ColorUIResource e = super.getControlTextColor();
		return new ColorUIResource(e.darker());
	}

	public ColorUIResource getWindowTitleForeground(){
		return windowTitleForeground;
	}

	public ColorUIResource getWindowTitleBackground() {
		return windowTitleBackground;
	}
	
	public ColorUIResource getWindowTitleInactiveForeground() {
		return windowTitleInactiveForeground;
	}

	public ColorUIResource getWindowTitleInactiveBackground() {
		return windowTitleInactiveBackground;
	}

}

////    private final ColorUIResource primary1 = new ColorUIResource(66, 33, 66);
////    private final ColorUIResource primary2 = new ColorUIResource(90, 86, 99);
////    private final ColorUIResource primary3 = new ColorUIResource(99, 99, 99);
//    
//    // 颜色由深向淡
//    private final ColorUIResource primary1 = new ColorUIResource(55, 55, 55);
//    private final ColorUIResource primary2 = new ColorUIResource(77, 77, 77);
//    private final ColorUIResource primary3 = new ColorUIResource(99, 99, 99);
//
//    private final ColorUIResource secondary1 = new ColorUIResource(0, 0, 0);
//    private final ColorUIResource secondary2 = new ColorUIResource(51, 51, 51);
//    private final ColorUIResource secondary3 = new ColorUIResource(102, 102, 102);
//
////    private final ColorUIResource black = new ColorUIResource(222, 222, 222);
////    private final ColorUIResource white = new ColorUIResource(0, 0, 0);
//    
//    private final ColorUIResource black = new ColorUIResource(198, 198, 198);
//    private final ColorUIResource white = new ColorUIResource(33, 33, 33);
//  private ColorUIResource primary1 = new ColorUIResource(66, 33, 66);
//  private ColorUIResource primary2 = new ColorUIResource(90, 86, 99);
//  private ColorUIResource primary3 = new ColorUIResource(99, 99, 99);



//	/* (non-Javadoc)
//	 * @see com.laxcus.util.display.ThemeLoader#loadDefault()
//	 */
//	@Override
//	public boolean loadDefault() {
//		primary1 = new ColorUIResource(55, 55, 55);
//	    primary2 = new ColorUIResource(77, 77, 77);
//	    primary3 = new ColorUIResource(99, 99, 99);
//
//	    secondary1 = new ColorUIResource(0, 0, 0);
//	    secondary2 = new ColorUIResource(51, 51, 51);
//	    secondary3 = new ColorUIResource(102, 102, 102);
//
//	    black = new ColorUIResource(198, 198, 198);
//	    white = new ColorUIResource(33, 33, 33);
//		return true;
//	}
//	
//	/* (non-Javadoc)
//	 * @see com.laxcus.util.display.ThemeLoader#loadDefine()
//	 */
//	@Override
//	public boolean loadDefine() {
//		ColorUIResource[] elements = new ColorUIResource[]{primary1, primary2, primary3, secondary1, secondary2, secondary3, black, white };
//		// 这些参数在metal_dark.txt文件中定义
//		String[] names = new String[] { "dark.primary1", "dark.primary2", "dark.primary3",
//				"dark.secondary1", "dark.secondary2", "dark.secondary3","dark.black", "dark.white" };
//		// 从配置中导入颜色
//		for (int i = 0; i < names.length; i++) {
//			java.awt.Color color = UIManager.getColor(names[i]);
//			if (color == null) {
//				System.out.printf("not found %s\n", names[i]);
//				return false;
//			}
//			elements[i] = new ColorUIResource(color);
//			
//			System.out.printf("%s is %x\n", names[i], elements[i].getRGB());
//		}
//
//		return true;
//	}