/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.skin.*;

/**
 * 切换面板UI
 * 
 * @author scott.liang
 * @version 1.0 6/24/2022
 * @since laxcus 1.0
 */
public class FlatTabbedPaneUI extends MetalTabbedPaneUI {

	/** 静态变量，记录皮肤名称 **/
	private static String skinName;

	private void setGrayColor() {
		UIManager.getDefaults().put("TabbedPane.selected", new ColorUIResource(246,246,246)); 
		UIManager.getDefaults().put("TabbedPane.selectHighlight", new ColorUIResource(210,210,210)); 
		UIManager.getDefaults().put("TabbedPane.unselectedBackground",  MetalLookAndFeel.getControl());
		UIManager.getDefaults().put("TabbedPane.tabAreaBackground", MetalLookAndFeel.getControl());
	}
	
	private void setOtherColor() {
		UIManager.getDefaults().put("TabbedPane.selected", MetalLookAndFeel.getWindowTitleBackground());
		UIManager.getDefaults().put("TabbedPane.selectHighlight", MetalLookAndFeel.getWindowTitleBackground());
		UIManager.getDefaults().put("TabbedPane.unselectedBackground", MetalLookAndFeel.getWindowTitleInactiveBackground());
		UIManager.getDefaults().put("TabbedPane.tabAreaBackground", MetalLookAndFeel.getWindowTitleInactiveBackground());
	}

	/**
	 * 构造默认的切换面板UI
	 */
	public FlatTabbedPaneUI() {
		super();
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		String name = Skins.getSkinName();
		if (skinName == null || skinName.compareToIgnoreCase(name) != 0) {
			skinName = name;
			if (Skins.isGraySkin()) {
				setGrayColor();
			} else {
				setOtherColor();
			}
		}
	}
	
	/**
	 * 返回实例
	 * @param x
	 * @return
	 */
	public static ComponentUI createUI(JComponent x) {
		return new FlatTabbedPaneUI();
	}
	
	private void doInsets() {
		//		if (tabInsets == null) tabInsets = new Insets(0,4,1,4);
		//        if (selectedTabPadInsets == null) selectedTabPadInsets = new Insets(2,2,2,1);
		//        if (tabAreaInsets == null) tabAreaInsets = new Insets(3,2,0,2);
		//        if (contentBorderInsets == null) contentBorderInsets = new Insets(2,2,3,3);

		//		UIManager.getDefaults().put("TabbedPane.selectedTabPadInsets", new Insets(2,2,2,1));
		//		UIManager.getDefaults().put("TabbedPane.tabAreaInsets", new Insets(2,2,1,2));
		//		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(1,1,1,1));

		// 单个选项卡的边框范围
		tabInsets = new Insets(4, 12, 4, 8);
		// 所有选项卡的范围
		tabAreaInsets = new Insets(4, 4, 0, 2);
		// 单个选项卡选中时的范围
		selectedTabPadInsets = new Insets(0, 0, 0, 0);
		// 整个边框范围，全部是0
		contentBorderInsets = new Insets(0, 0, 0, 0);
	}
	
	protected void installDefaults() {
		super.installDefaults();
		
		doInsets();
	}

}


//private Color unselectedBackground;
//private boolean ocean = false;

///**
// * 返回实例
// * @param x
// * @return
// */
//public static ComponentUI createUI(JComponent x) {
//	
////	UIManager.getDefaults().put("TabbedPane.focus", MetalLookAndFeel.getControlShadow());
////	UIManager.getDefaults().put("TabbedPane.tabAreaBackground", MetalLookAndFeel.getControlHighlight());
//	
////	UIManager.getDefaults().put("TabbedPane.selected", new ColorUIResource(Color.RED)); // MetalLookAndFeel.GETCONTR )
////	UIManager.getDefaults().put("TabbedPane.unselectedBackground", new ColorUIResource(Color.BLUE));
////	UIManager.getDefaults().put("TabbedPane.selectHighlight", new ColorUIResource(Color.GREEN));
//	
////	UIManager.getDefaults().put("TabbedPane.selected", MetalLookAndFeel.getControlShadow());
//	
////	UIManager.getDefaults().put("TabbedPane.selectHighlight", MetalLookAndFeel.getControl());
////	UIManager.getDefaults().put("TabbedPane.unselectedBackground", MetalLookAndFeel.getControlHighlight());
////	
////	ColorUIResource rs = new ColorUIResource( MetalLookAndFeel.getControl() );
////	if (!Skins.isGraySkin()) {
////		int flag = 10;
////		if (Skins.isDarkSkin()) {
////			flag = 15;
////		} else if (Skins.isBronzSkin()) {
////			flag = 38;
////		} else if (Skins.isCyanoSkin()) {
////			flag = 38;
////		}
////		Color color = MetalLookAndFeel.getControlShadow();
////		ESL esl = new ESL(color);
////		rs = new ColorUIResource(esl.toBrighter(flag).toColor());
////	}
////	if (rs != null) {
////		UIManager.getDefaults().put("TabbedPane.selected", rs);
////	}
//	
////	if (tabInsets == null) tabInsets = new Insets(0,4,1,4);
////    if (selectedTabPadInsets == null) selectedTabPadInsets = new Insets(2,2,2,1);
////    if (tabAreaInsets == null) tabAreaInsets = new Insets(3,2,0,2);
////    if (contentBorderInsets == null) contentBorderInsets = new Insets(2,2,3,3);
//    
////	// 边框
////	UIManager.getDefaults().put("TabbedPane.tabInsets", new Insets(3,8,3,8));
////	UIManager.getDefaults().put("TabbedPane.selectedTabPadInsets", new Insets(2,2,2,1));
////	
////	UIManager.getDefaults().put("TabbedPane.tabAreaInsets", new Insets(2,2,1,2));
////	
////	UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(1,1,1,1));
//
////	unselectedBackground = UIManager.getColor("TabbedPane.selected");
////	selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
////	selectColor = UIManager.getColor("TabbedPane.unselectedBackground");
//
////	setInsets();
//	
//	if (Skins.isGraySkin()) {
//		setGrayColor();
//	} else {
//		setOtherColor();
//	}
//	
//	return new FlatTabbedPaneUI();
//}


//private static void setInsets() {
//	//		if (tabInsets == null) tabInsets = new Insets(0,4,1,4);
//	//        if (selectedTabPadInsets == null) selectedTabPadInsets = new Insets(2,2,2,1);
//	//        if (tabAreaInsets == null) tabAreaInsets = new Insets(3,2,0,2);
//	//        if (contentBorderInsets == null) contentBorderInsets = new Insets(2,2,3,3);
//
//	// 边框
//	UIManager.getDefaults().put("TabbedPane.tabInsets", new Insets(3,12,3,8));
//
//	//		UIManager.getDefaults().put("TabbedPane.selectedTabPadInsets", new Insets(2,2,2,1));
//	//		UIManager.getDefaults().put("TabbedPane.tabAreaInsets", new Insets(2,2,1,2));
//	//		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(1,1,1,1));
//
//	// 所有选项卡的范围
//	UIManager.getDefaults().put("TabbedPane.tabAreaInsets", new Insets(4, 4, 0, 2));
//	// 单个选项卡选中时的范围
//	UIManager.getDefaults().put("TabbedPane.selectedTabPadInsets", new Insets(2, 2, 2, 2));
//	// 整个边框范围，全部是0
//	UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
//}

//private static void setGrayColor() {
//	UIManager.getDefaults().put("TabbedPane.selected", new ColorUIResource(246,246,246)); // Color.LIGHT_GRAY));
//	UIManager.getDefaults().put("TabbedPane.selectHighlight", new ColorUIResource(210,210,210)); // MetalLookAndFeel.getControlHighlight());
//	UIManager.getDefaults().put("TabbedPane.unselectedBackground",  MetalLookAndFeel.getControl());
//	UIManager.getDefaults().put("TabbedPane.tabAreaBackground", MetalLookAndFeel.getControl());
//}
//
//private static void setOtherColor() {
////	UIManager.getDefaults().put("TabbedPane.selectHighlight", MetalLookAndFeel.getControl());
////	UIManager.getDefaults().put("TabbedPane.unselectedBackground", MetalLookAndFeel.getControlHighlight());
////	UIManager.getDefaults().put("TabbedPane.tabAreaBackground", MetalLookAndFeel.getControlHighlight());
//	
////	int flag = 20;
////	if (Skins.isDarkSkin()) {
////		flag = 28;
////	} else if (Skins.isBronzSkin()) {
////		flag = 58;
////	} else if (Skins.isCyanoSkin()) {
////		flag = 58;
////	}
////	ESL esl = new ESL(MetalLookAndFeel.getControlShadow()); // 这时是高亮
////	ColorUIResource rs = new ColorUIResource(esl.toBrighter(flag).toColor());
////	
////	UIManager.getDefaults().put("TabbedPane.selected", rs);
//	
//	UIManager.getDefaults().put("TabbedPane.selected", MetalLookAndFeel.getWindowTitleBackground());
//	UIManager.getDefaults().put("TabbedPane.selectHighlight", MetalLookAndFeel.getWindowTitleBackground());
//	UIManager.getDefaults().put("TabbedPane.unselectedBackground", MetalLookAndFeel.getWindowTitleInactiveBackground());
//	UIManager.getDefaults().put("TabbedPane.tabAreaBackground", MetalLookAndFeel.getWindowTitleInactiveBackground());
//}

//	

//private void ft() {
//	highlight = UIManager.getColor("TabbedPane.light");
//	lightHighlight = UIManager.getColor("TabbedPane.highlight");
//	shadow = UIManager.getColor("TabbedPane.shadow");
//	darkShadow = UIManager.getColor("TabbedPane.darkShadow");
//	focus = UIManager.getColor("TabbedPane.focus");
//	selectedColor = UIManager.getColor("TabbedPane.selected");
//
//	 tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
//        selectColor = UIManager.getColor("TabbedPane.selected");
//        selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
//        tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");
//        unselectedBackground = UIManager.getColor(
//                                         "TabbedPane.unselectedBackground");
//}



//private  void doGrayColor() {
////	UIManager.getDefaults().put("TabbedPane.selectHighlight", new ColorUIResource(240,240,240)); // MetalLookAndFeel.getControlHighlight());
//	UIManager.getDefaults().put("TabbedPane.unselectedBackground",  MetalLookAndFeel.getControl());
////
////	UIManager.getDefaults().put("TabbedPane.selected", new ColorUIResource(240,240,240)); // Color.LIGHT_GRAY));
//	
////	selectHighlight = 
//	selectHighlight = new Color(240,240,240); //MetalLookAndFeel.getControlHighlight();
//	selectColor = new Color(210,210,210);
//	tabAreaBackground = new Color( MetalLookAndFeel.getControl().getRGB());
//	
////	unselectedBackground = new Color( MetalLookAndFeel.getControl().getRGB());
//}
//
//private void doOtherColor() {
//	//		UIManager.getDefaults().put("TabbedPane.selectHighlight", MetalLookAndFeel.getControl());
//			UIManager.getDefaults().put("TabbedPane.unselectedBackground", MetalLookAndFeel.getControlHighlight());
//
//	int flag = 20;
//	if (Skins.isDarkSkin()) {
//		flag = 28;
//	} else if (Skins.isBronzSkin()) {
//		flag = 58;
//	} else if (Skins.isCyanoSkin()) {
//		flag = 58;
//	}
//	ESL esl = new ESL(MetalLookAndFeel.getControlShadow()); // 这时是高亮
//	esl = esl.toBrighter(flag);
//	//		ColorUIResource rs = new ColorUIResource(esl.toBrighter(flag).toColor());
//
//	//		UIManager.getDefaults().put("TabbedPane.selected", rs);
//
//	selectHighlight = MetalLookAndFeel.getControl();
//	selectColor = esl.toColor(); // new Color(240,240,240);
//	tabAreaBackground = MetalLookAndFeel.getControlHighlight();
//}



//   private Color getUnselectedBackgroundAt(int index) {
//        Color color = tabPane.getBackgroundAt(index);
//        if (color instanceof UIResource) {
//            if (unselectedBackground != null) {
//                return unselectedBackground;
//            }
//        }
//        return color;
//    }
//
// protected Color getColorForGap( int currentRun, int x, int y ) {
//        final int shadowWidth = 4;
//        int selectedIndex = tabPane.getSelectedIndex();
//        int startIndex = tabRuns[ currentRun + 1 ];
//        int endIndex = lastTabInRun( tabPane.getTabCount(), currentRun + 1 );
//        int tabOverGap = -1;
//        // Check each tab in the row that is 'on top' of this row
//        for ( int i = startIndex; i <= endIndex; ++i ) {
//            Rectangle tabBounds = getTabBounds( tabPane, i );
//            int tabLeft = tabBounds.x;
//            int tabRight = (tabBounds.x + tabBounds.width) - 1;
//            // Check to see if this tab is over the gap
//	    if ( FlatUtil.isLeftToRight(tabPane) ) {
//                if ( tabLeft <= x && tabRight - shadowWidth > x ) {
//                    return selectedIndex == i ? selectColor : getUnselectedBackgroundAt( i );
//                }
//            }
//            else {
//	        if ( tabLeft + shadowWidth < x && tabRight >= x ) {
//                    return selectedIndex == i ? selectColor : getUnselectedBackgroundAt( i );
//                }
//            }
//        }
//
//        return tabPane.getBackground();
//    }

//protected void paintBottomTabBorder(int tabIndex, Graphics g, int x, int y, int w, int h, int btm, int rght, boolean isSelected) {
//	int tabCount = tabPane.getTabCount();
//	int currentRun = getRunForTab(tabCount, tabIndex);
//	int lastIndex = lastTabInRun(tabCount, currentRun);
//	int firstIndex = tabRuns[currentRun];
//	boolean leftToRight = true;// FlatUtil.isLeftToRight(tabPane); // MetalUtils.isLeftToRight(tabPane);
//
//	int bottom = h - 1;
//	int right = w - 1;
//	
////	bottom = h + 2;
////	right = w + 2;
//
//	//
//	// Paint Gap
//	//
//	if (shouldFillGap(currentRun, tabIndex, x, y)) {
//		g.translate(x, y);
//
//		if (leftToRight) {
//			g.setColor(getColorForGap(currentRun, x, y));
//			g.fillRect(1, bottom - 4, 3, 5);
//			g.fillRect(4, bottom - 1, 2, 2);
//		} else {
//			g.setColor(getColorForGap(currentRun, x + w - 1, y));
//			g.fillRect(right - 3, bottom - 3, 3, 4);
//			g.fillRect(right - 5, bottom - 1, 2, 2);
//			g.drawLine(right - 1, bottom - 4, right - 1, bottom - 4);
//		}
//
//		g.translate(-x, -y);
//	}
//
//	g.translate(x, y);
//
//	//
//	// Paint Border
//	//
//
//	if (ocean && isSelected) {
//		//			g.setColor(oceanSelectedBorderColor);
//	} else {
//		g.setColor(darkShadow);
//	}
//
//	if (leftToRight) {
//
//		// Paint slant
//		g.drawLine(1, bottom - 5, 6, bottom);
//
//		// Paint bottom
//		g.drawLine(6, bottom, right, bottom);
//		
////		// Paint slant
////		g.drawLine(1, bottom - 5, 6, bottom);
////
////		// Paint bottom
////		g.drawLine(6, bottom, right+2, bottom);
//
//		// Paint right
//		if (tabIndex == lastIndex) {
//			g.drawLine(right, 0, right, bottom);
//		}
//
//		// Paint left
//		if (ocean && isSelected) {
//			g.drawLine(0, 0, 0, bottom - 6);
//			if ((currentRun == 0 && tabIndex != 0) || (currentRun > 0 && tabIndex != tabRuns[currentRun - 1])) {
//				g.setColor(darkShadow);
//				g.drawLine(0, bottom - 5, 0, bottom);
//			}
//		} else {
//			if (ocean && tabIndex == tabPane.getSelectedIndex() + 1) {
//				//					g.setColor(oceanSelectedBorderColor);
//			}
//			if (tabIndex != tabRuns[runCount - 1]) {
//				g.drawLine(0, 0, 0, bottom);
//			} else {
//				g.drawLine(0, 0, 0, bottom - 6);
//			}
//		}
//	} else {
//
//		// Paint slant
//		g.drawLine(right - 1, bottom - 5, right - 6, bottom);
//
//		// Paint bottom
//		g.drawLine(right - 6, bottom, 0, bottom);
//
//		// Paint left
//		if (tabIndex == lastIndex) {
//			// last tab in run
//			g.drawLine(0, 0, 0, bottom);
//		}
//
//		// Paint right
//		if (ocean && tabIndex == tabPane.getSelectedIndex() + 1) {
//			//				g.setColor(oceanSelectedBorderColor);
//			g.drawLine(right, 0, right, bottom);
//		} else if (ocean && isSelected) {
//			g.drawLine(right, 0, right, bottom - 6);
//			if (tabIndex != firstIndex) {
//				g.setColor(darkShadow);
//				g.drawLine(right, bottom - 5, right, bottom);
//			}
//		} else if (tabIndex != tabRuns[runCount - 1]) {
//			// not the first tab in the last run
//			g.drawLine(right, 0, right, bottom);
//		} else {
//			// the first tab in the last run
//			g.drawLine(right, 0, right, bottom - 6);
//		}
//	}
//
//	//
//	// Paint Highlight
//	//
//
//	g.setColor(isSelected ? selectHighlight : highlight);
//
//	if (leftToRight) {
//
//		// Paint slant
//		g.drawLine(1, bottom - 6, 6, bottom - 1);
//
//		// Paint left
//		g.drawLine(1, 0, 1, bottom - 6);
//		
////		// Paint slant
////		g.drawLine(1, bottom - 8, 8, bottom - 1);
////
////		// Paint left
////		g.drawLine(1, 0, 1, bottom - 8);
//
//		// paint highlight in the gap on tab behind this one
//		// on the left end (where they all line up)
//		if (tabIndex == firstIndex && tabIndex != tabRuns[runCount - 1]) {
//			// first tab in run but not first tab in last run
//			if (tabPane.getSelectedIndex() == tabRuns[currentRun + 1]) {
//				// tab in front of selected tab
//				g.setColor(selectHighlight);
//			} else {
//				// tab in front of normal tab
//				g.setColor(highlight);
//			}
//			g.drawLine(1, bottom - 4, 1, bottom);
//		}
//	} else {
//
//		// Paint left
//		if (tabIndex == lastIndex) {
//			// last tab in run
//			g.drawLine(1, 0, 1, bottom - 1);
//		} else {
//			g.drawLine(0, 0, 0, bottom - 1);
//		}
//	}
//
//	g.translate(-x, -y);
//}

//protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
//		int selectedIndex, int x, int y, int w, int h) {
//	
//	boolean leftToRight = true; // MetalUtils.isLeftToRight(tabPane);
//	int bottom = y + h - 1;
//	int right = x + w - 1;
//	
////	int bottom = y + h +10;
////	int right = x + w  + 10;
//	
//	Rectangle selRect = selectedIndex < 0 ? null : getTabBounds(
//			selectedIndex, calcRect);
//
//	g.setColor(darkShadow);
//
//	// Draw unbroken line if tabs are not on BOTTOM, OR
//	// selected tab is not in run adjacent to content, OR
//	// selected tab is not visible (SCROLL_TAB_LAYOUT)
//	//
//	if (tabPlacement != BOTTOM || selectedIndex < 0 ||
//			(selRect.y - 1 > h) ||
//			(selRect.x < x || selRect.x > x + w)) {
//		if (ocean && tabPlacement == BOTTOM) {
//			// g.setColor(oceanSelectedBorderColor);
//		}
//		g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
//	} else {
//		// Break line to show visual connection to selected tab
//		boolean lastInRun = true; // isLastInRun(selectedIndex);
//
//		if (ocean) {
//			// g.setColor(oceanSelectedBorderColor);
//		}
//
//		if ( leftToRight || lastInRun ) {
//			g.drawLine(x, bottom, selRect.x, bottom);
//		} else {
//			g.drawLine(x, bottom, selRect.x - 1, bottom);
//		}
//
//		if (selRect.x + selRect.width < x + w - 2) {
//			if ( leftToRight && !lastInRun ) {
//				g.drawLine(selRect.x + selRect.width, bottom, right, bottom);
//			} else {
//				g.drawLine(selRect.x + selRect.width - 1, bottom, right, bottom);
//			}
//		} 
//	}
//}
   

//public void paint( Graphics g, JComponent c ) {
//	int tabPlacement = tabPane.getTabPlacement();
//
//	Insets insets = c.getInsets();
//	Dimension size = c.getSize();
//	
////	System.out.printf("%d - %d - %d - %d\n", insets.top, insets.left, insets.bottom,insets.right);
//	
//	// 重新定义
//	insets = new Insets(-4,-4,-4,-4);
//	insets.bottom -= 2;
//	insets.right -= 2;
//
//	// Paint the background for the tab area
//	if ( tabPane.isOpaque() ) {
//		Color bg = UIManager.getColor("TabbedPane.tabAreaBackground");
//		if (bg != null) {
//			g.setColor(bg);
//		}
//		else {
//			g.setColor( c.getBackground() );
//		}
//		
//		switch ( tabPlacement ) {
//		case LEFT:
//			g.fillRect( insets.left, insets.top, 
//					calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth ),
//					size.height - insets.bottom - insets.top );
//			break;
//		case BOTTOM:
//			int totalTabHeight = calculateTabAreaHeight( tabPlacement, runCount, maxTabHeight );
//			g.fillRect( insets.left, size.height - insets.bottom - totalTabHeight, 
//					size.width - insets.left - insets.right,
//					totalTabHeight );
//			break;
//		case RIGHT:
//			int totalTabWidth = calculateTabAreaWidth( tabPlacement, runCount, maxTabWidth );
//			g.fillRect( size.width - insets.right - totalTabWidth,
//					insets.top, totalTabWidth, 
//					size.height - insets.top - insets.bottom );
//			break;
//		case TOP:
//		default:
//			g.fillRect( insets.left, insets.top, 
//					size.width - insets.right - insets.left, 
//					calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight) );
//			paintHighlightBelowTab();
//		}
//	}
//	
////	g.setColor( Color.RED);
////	g.fillRect(0, 0, size.width -1 , size.height - 1);
//
//	super.paint( g, c );
//}