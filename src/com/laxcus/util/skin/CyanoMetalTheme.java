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
 * 深兰主题
 * 
 * @author scott.liang
 * @version 1.0 3/21/2020
 * @since laxcus 1.0
 */
public class CyanoMetalTheme extends DefaultMetalTheme implements ThemeLoader {	
	
	// 颜色由深向淡
	private ColorUIResource primary1 ;
	private ColorUIResource primary2 ;
	private ColorUIResource primary3 ;

	// 颜色由深入淡
	private ColorUIResource secondary1;
	private ColorUIResource secondary2;
	private ColorUIResource secondary3;

	// 黑/白
	private ColorUIResource black;
	private ColorUIResource white;

	/** 桌面颜色 **/
	private ColorUIResource desktop;
	
	/** 标题栏激活状态下的前景/背景 **/
	private ColorUIResource windowTitleForeground;
	private ColorUIResource windowTitleBackground;

	/** 标题栏非激活背景 **/
	private ColorUIResource windowTitleInactiveForeground;
	private ColorUIResource windowTitleInactiveBackground;
	
	/**
	 * 构造默认深兰主题
	 */
	public CyanoMetalTheme() {
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
		primary1 = findColor("cyano.primary1", new  ColorUIResource(0x6382BF));
		primary2 = findColor("cyano.primary2", new ColorUIResource(0xA3B8CC));
		primary3 = findColor("cyano.primary3", new ColorUIResource(0xB8CFE5));

		secondary1 = findColor("cyano.secondary1", new ColorUIResource(0x7A8A99));
		secondary2 = findColor("cyano.secondary2", new ColorUIResource(0xB8CFE5));
		secondary3 = findColor("cyano.secondary3", new ColorUIResource(0xEEEEEE));

		black = findColor("cyano.black", new ColorUIResource(Color.BLACK.getRGB()));
		white = findColor("cyano.white", new ColorUIResource(Color.WHITE.getRGB()));
		
		// 桌面
		desktop = findColor("cyano.desktop", new ColorUIResource(16, 40, 52));

//		// 标题栏激活/非激活颜色
//		windowTitleBackground = findColor("cyano.windowTitleBackground", primary3); 
//		windowTitleInactiveBackground = findColor("cyano.windowTitleInactiveBackground", secondary3); 

		// 标题栏激活状态下的前景/背景
		windowTitleForeground = findColor("cyano.windowTitleForeground", black);
		windowTitleBackground = findColor("cyano.windowTitleBackground", primary3);
		
		// 标题栏非激活状态下的前景/背景
		windowTitleInactiveForeground = findColor("cyano.windowTitleInactiveForeground", black);
		windowTitleInactiveBackground = findColor("cyano.windowTitleInactiveBackground", secondary3);

		upateAcceleratorForeground();
	}

	public String getName() { return "LaxcusCyanoTheme"; }

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
     * 高亮文本颜色，颜色更亮！
     * @see javax.swing.plaf.metal.MetalTheme#getHighlightedTextColor()
     */
    @Override
	public ColorUIResource getHighlightedTextColor() {
    	ColorUIResource e = super.getControlTextColor();
    	// 更亮！
		return new ColorUIResource(e.brighter());
	}
    
	/** 加速器前景颜色 **/
	private ColorUIResource acceleratorForeground;
	
	/**
	 * 更新快捷键的颜色
	 */
	private void upateAcceleratorForeground(){
		// 加速器颜色，加亮50
		ESL esl = new ESL(primary1);
		esl.brighter(50);
		acceleratorForeground = new ColorUIResource(esl.toColor().getRGB());
	}
	
	public ColorUIResource getAcceleratorForeground() {
		return acceleratorForeground;
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


///*
//* 非激活文本颜色，比目前颜色暗！
//* @see javax.swing.plaf.metal.MetalTheme#getInactiveControlTextColor()
//*/
//@Override
//public ColorUIResource getInactiveControlTextColor() {
////	ColorUIResource e = super.getControlTextColor();
////	return new ColorUIResource(e.darker().darker());
//	
//	return new ColorUIResource(Color.RED);
//}
//
///*
//* (non-Javadoc)
//* @see javax.swing.plaf.metal.MetalTheme#getInactiveSystemTextColor()
//*/
//@Override
//public ColorUIResource getInactiveSystemTextColor() {
//	return new ColorUIResource(Color.RED);
//}


///*
//* 失效颜色，更暗！
//* @see javax.swing.plaf.metal.MetalTheme#getControlDisabled()
//*/
//@Override
//public ColorUIResource getControlDisabled() {
////	Color e = this.getControl();
//	Color e = getSecondary3();
//	return new ColorUIResource(e.darker());
//}

//public ColorUIResource getFocusColor() {
//	ColorUIResource
//}

///*
//* (non-Javadoc)
//* @see javax.swing.plaf.metal.MetalTheme#getWindowTitleInactiveBackground()
//*/
//@Override
//public ColorUIResource getWindowTitleInactiveBackground() {
//	ColorUIResource e = super.getWindowTitleInactiveBackground();
//	return new ColorUIResource(e.darker());
//}