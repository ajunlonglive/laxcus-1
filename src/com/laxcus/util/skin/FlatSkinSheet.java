/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

/**
 * 平面皮肤参数表
 * 
 * @author scott.liang
 * @version 1.0 10/3/2021
 * @since laxcus 1.0
 */
public class FlatSkinSheet extends SkinSheet {

	/**
	 * 构造平面皮肤参数表
	 */
	public FlatSkinSheet() {
		super();
		init();
	}

	/**
	 * 初始化皮肤值
	 */
	private void init() {
		add("ScrollPaneUI", "com.laxcus.gui.skin.FlatScrollPaneUI");
		add("ScrollBarUI", "com.laxcus.gui.skin.FlatScrollBarUI");

		add("InternalFrameUI", "com.laxcus.gui.skin.FlatInternalFrameUI");
		add("SplitPaneUI", "com.laxcus.gui.skin.FlatSplitPaneUI");
		add("SliderUI", "com.laxcus.gui.skin.FlatSliderUI");
		
		add("SeparatorUI","com.laxcus.gui.skin.FlatSeparatorUI");
		
		add("ButtonUI", "com.laxcus.gui.skin.FlatButtonUI");
		add("CheckBoxUI", "com.laxcus.gui.skin.FlatCheckBoxUI");
		add("RadioButtonUI", "com.laxcus.gui.skin.FlatRadioButtonUI");
		
		add("TextFieldUI", "com.laxcus.gui.skin.FlatTextFieldUI");
		add("PasswordFieldUI", "com.laxcus.gui.skin.FlatPasswordFieldUI");
		add("TextPaneUI", "com.laxcus.gui.skin.FlatTextPaneUI");
		add("TextAreaUI", "com.laxcus.gui.skin.FlatTextAreaUI");
		
		add("ComboBoxUI", "com.laxcus.gui.skin.FlatComboBoxUI");
		
		add("SpinnerUI", "com.laxcus.gui.skin.FlatSpinnerUI");
		add("TableUI", "com.laxcus.gui.skin.FlatTableUI");
		add("TableHeaderUI", "com.laxcus.gui.skin.FlatTableHeaderUI");
		
		add("FormattedTextFieldUI", "com.laxcus.gui.skin.FlatFormattedTextFieldUI");
		
		add("TabbedPaneUI", "com.laxcus.gui.skin.FlatTabbedPaneUI");
		
		add("PopupMenuUI",  "com.laxcus.gui.skin.FlatPopupMenuUI");
		add("MenuItemUI",  "com.laxcus.gui.skin.FlatMenuItemUI");
		add("MenuUI",  "com.laxcus.gui.skin.FlatMenuUI");
		add("CheckBoxMenuItemUI", "com.laxcus.gui.skin.FlatCheckBoxMenuItemUI");
		add("RadioButtonMenuItemUI","com.laxcus.gui.skin.FlatRadioButtonMenuItemUI");
		add("MenuBarUI","com.laxcus.gui.skin.FlatMenuBarUI");
		
		// 弹出菜单分隔符
		add("PopupMenuSeparatorUI", "com.laxcus.gui.skin.FlatPopupMenuSeparatorUI");
		// 工具栏UI不能生效
		add("ToolBarSeparatorUI", "com.laxcus.gui.skin.FlatToolBarSeparatorUI");
		// 工具栏UI
		add("ToolBarUI", "com.laxcus.gui.skin.FlatToolBarUI");
	}

}