/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.color.*;

/**
 * 灰色主题
 * 
 * @author scott.liang
 * @version 1.0 10/18/2021
 * @since laxcus 1.0
 */
public class GrayMetalTheme extends DefaultMetalTheme implements ThemeLoader {

	// 颜色由深向淡
	private ColorUIResource primary1;

	private ColorUIResource primary2;

	private ColorUIResource primary3;

    // 颜色由深入淡
    private ColorUIResource secondary1;
    private ColorUIResource secondary2;
    private ColorUIResource secondary3;
    
	/** 黑/白基色 **/
	private ColorUIResource black;

	private ColorUIResource white;
	
	/** 桌面颜色 **/
	private ColorUIResource desktop;

	/** 加速器前景颜色 **/
	private ColorUIResource acceleratorForeground;
	
	/** 失效菜单前景 **/
	private ColorUIResource menuDisabledForeground;

	/** 标题栏激活状态下的前景/背景 **/
	private ColorUIResource windowTitleForeground;
	private ColorUIResource windowTitleBackground;

	/** 标题栏非激活背景 **/
	private ColorUIResource windowTitleInactiveForeground;
	private ColorUIResource windowTitleInactiveBackground;
	
	/**
	 * 构造默认的METAL灰色主题
	 */
	public GrayMetalTheme() {
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

	/* (non-Javadoc)
	 * @see com.laxcus.util.skin.ThemeLoader#loadConfigure()
	 */
	@Override
	public void loadConfigure() {
		primary1 = findColor("gray.primary1", new ColorUIResource(142, 172, 202));
		primary2 = findColor("gray.primary2", new ColorUIResource(120, 162, 203));
		primary3 = findColor("gray.primary3", new ColorUIResource(193, 215, 236));

		secondary1 = findColor("gray.secondary1", new ColorUIResource(128,128,128));
		secondary2 = findColor("gray.secondary2", new ColorUIResource(179,179,179));
		secondary3 = findColor("gray.secondary3", new ColorUIResource(230,230,230));
		
		black = findColor("gray.black", new ColorUIResource(3, 3, 3));
		white = findColor("gray.white", new ColorUIResource(250, 250, 250));
		
		desktop = findColor("gray.desktop", new ColorUIResource(58, 110, 165));
		
		menuDisabledForeground = findColor("gray.menuDisabledForeground", new ColorUIResource(88,88,88));
		
		// 标题栏激活状态下的前景/背景
		windowTitleForeground = findColor("gray.windowTitleForeground", black);
		windowTitleBackground = findColor("gray.windowTitleBackground", primary3);
		
		// 标题栏非激活状态下的前景/背景
		windowTitleInactiveForeground = findColor("gray.windowTitleInactiveForeground", black);
		windowTitleInactiveBackground = findColor("gray.windowTitleInactiveBackground", secondary3);

		upateAcceleratorForeground();
	}
	
	/**
	 * 更新快捷键的颜色
	 */
	private void upateAcceleratorForeground(){
		// 加速器颜色，调暗50
		ESL esl = new ESL(Color.BLACK);
		esl.toBrighter(80);
		acceleratorForeground = new ColorUIResource(esl.toColor().getRGB());
	}
	
	public String getName() { return "LaxcusGrayTheme"; }
	
	protected ColorUIResource getPrimary1() { return primary1; }
	protected ColorUIResource getPrimary2() { return primary2; }
	protected ColorUIResource getPrimary3() { return primary3; }

	protected ColorUIResource getSecondary1() { return secondary1; }
	protected ColorUIResource getSecondary2() { return secondary2; }
	protected ColorUIResource getSecondary3() { return secondary3; }

	protected ColorUIResource getBlack() { return black; }
	protected ColorUIResource getWhite() { return white; }

	public ColorUIResource getDesktopColor() {
		return desktop;
	}
	
	public ColorUIResource getAcceleratorForeground() {
		return acceleratorForeground;
	}
	
	public ColorUIResource getMenuDisabledForeground(){
		return menuDisabledForeground;
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