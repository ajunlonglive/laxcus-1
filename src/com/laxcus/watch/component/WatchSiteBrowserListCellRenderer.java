/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;

import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
* 分布站点的类型或者地址单元
* 
* @author scott.liang
* @version 1.0 12/8/2012
* @since laxcus 1.0
*/
public class WatchSiteBrowserListCellRenderer extends JLabel implements TreeCellRenderer { // DefaultTreeCellRenderer 

	private static final long serialVersionUID = 2909452960955966987L;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 节点和网络地址图标 **/
	private Icon topIcon;
	
	private Icon logIcon;
	
	/** HOME子域集群 **/
	private Icon homeIcon;
	private Icon callIcon;
	private Icon workIcon;
	private Icon dataIcon;
	private Icon buildIcon;

	/** BANK子域集群 **/
	private Icon bankIcon;
	private Icon accountIcon;
	private Icon hashIcon;
	private Icon gateIcon;
	private Icon entranceIcon;

	/** 计算机节点图标 **/
	private Icon siteIcon;

	/**
	 * 分布站点参数
	 */
	public WatchSiteBrowserListCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化界面参数
	 */
	private void init() {
		loadIcons();
		loadColor();
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(5);
//		setBorder(new EmptyBorder(2, 2, 2, 2));
	}
	
	/**
	 * 从内存中加载颜色
	 */
	private void loadColor() {
		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
	}

	/**
	 * 初始化图标
	 */
	private void loadIcons() {
		ResourceLoader loader = new ResourceLoader("conf/watch/image/sites/");
		
		int width = 16, height = 16;

		topIcon = loader.findImage("top.png", width, height); // 显示TOP图标

		homeIcon = loader.findImage("home.png", width, height);// 显示HOME图标

		logIcon = loader.findImage("log.png", width, height); // 显示LOG图标

		callIcon = loader.findImage("call.png", width, height); // 显示CALL图标

		dataIcon = loader.findImage("data.png", width, height); // 显示DATA图标

		workIcon = loader.findImage("work.png", width, height); // 显示WORK图标

		buildIcon = loader.findImage("build.png", width, height); // 显示BUILD图标

		bankIcon = loader.findImage("bank.png", width, height); // 显示BANK图标

		accountIcon = loader.findImage("account.png", width, height); // 显示ACCOUNT图标

		hashIcon = loader.findImage("hash.png", width, height); // 显示HASH图标

		gateIcon = loader.findImage("gate.png", width, height); // 显示GATE图标

		entranceIcon = loader.findImage("entrance.png", width, height); // 显示ENTRANCE图标

		// 网络地址图标
		siteIcon = loader.findImage("site.png", width, height);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		if (value == null) {
			setIcon(null);
			setText("");
			return this;
		}
		
		// 字体
		setFont(tree.getFont());

		// 前景/背景
		if (hasFocus || selected) {
			setForeground(textSelectForeground);
			setBackground(textSelectBackground);
		} else {
			setForeground(textForeground);
			setBackground(textBackground);
		}

		if (value.getClass() == WatchSiteBrowserFamilyTreeNode.class) {
			WatchSiteBrowserFamilyTreeNode e = (WatchSiteBrowserFamilyTreeNode) value;
			Icon icon = null;

			if (e.isTop()) {
				icon = topIcon;
			} else if (e.isHome()) {
				icon = homeIcon;
			} else if (e.isLog()) {
				icon = logIcon;
			} else if (e.isCall()) {
				icon = callIcon;
			} else if (e.isData()) {
				icon = dataIcon;
			} else if (e.isWork()) {
				icon = workIcon;
			} else if (e.isBuild()) {
				icon = buildIcon;
			} else if(e.isBank()) {
				icon = bankIcon;
			} else if(e.isAccount()) {
				icon = accountIcon;
			} else if(e.isHash()) {
				icon = hashIcon;
			} else if(e.isGate()) {
				icon = gateIcon;
			} else if(e.isEntrance()) {
				icon = entranceIcon;
			} else {
				icon = siteIcon;
			}

			// 设置图标
			setIcon(icon);
			
//			FontKit.setLabelText(this, e.getText());
			setText(e.getText());
			FontKit.setToolTipText(this, e.getText());
			
		} else if (value.getClass() == WatchSiteBrowserAddressTreeNode.class) {
			WatchSiteBrowserAddressTreeNode e = (WatchSiteBrowserAddressTreeNode) value;

			setIcon(siteIcon);
			
//			FontKit.setLabelText(this, e.getText());
			setText(e.getText());
			FontKit.setToolTipText(this, e.getText());
		} else {
			setIcon(null);
			setText("");
		}

		setComponentOrientation(tree.getComponentOrientation());
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setEnabled(tree.isEnabled());
		setOpaque(true);

		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		loadColor();
		super.updateUI();
	}

	
//	/**
//	 * Sets the color the text is drawn with when the node is selected.
//	 */
//	public void setTextSelectForeground(Color newColor) {
//		textSelectForeground = newColor;
//	}
//
//	/**
//	 * Returns the color the text is drawn with when the node is selected.
//	 */
//	public Color getTextSelectForeground() {
//		return textSelectForeground;
//	}
//
//	/**
//	 * Sets the color the text is drawn with when the node isn't selected.
//	 */
//	public void setTextForeground(Color newColor) {
//		textForeground = newColor;
//	}
//
//	/**
//	 * Returns the color the text is drawn with when the node isn't selected.
//	 */
//	public Color getTextForeground() {
//		return textForeground;
//	}
//
//	/**
//	 * Sets the color to use for the background if node is selected.
//	 */
//	public void setTextSelectBackground(Color newColor) {
//		textSelectBackground = newColor;
//	}
//
//	/**
//	 * Returns the color to use for the background if node is selected.
//	 */
//	public Color getTextSelectBackground() {
//		return textSelectBackground;
//	}
//
//	/**
//	 * Sets the background color to be used for non selected nodes.
//	 */
//	public void setTextBackground(Color newColor) {
//		textBackground = newColor;
//	}

//	/**
//	 * Returns the background color to be used for non selected nodes.
//	 */
//	public Color getTextBackground() {
//		return textBackground;
//	}
	
//	/**
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 */
//	public void validate() {}
//
//	/*
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 */
//	public void invalidate() {}
//
//	/**
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 */
//	@Override
//	public void revalidate() {}
//
//	/**
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 */
//	public void repaint(long tm, int x, int y, int width, int height) {}
//
//	/**
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 */
//	public void repaint(Rectangle r) {}
//
//	/**
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 *
//	 * @since 1.5
//	 */
//	public void repaint() {}
//
//	/*
//	 * 重画界面，这个很重要！
//	 * @see javax.swing.JLabel#updateUI()
//	 */
//	@Override
//	public void updateUI() {
//		super.updateUI();
//		init();
//	}
	
}

///**
// * 分布站点的类型或者地址单元
// * 
// * @author scott.liang
// * @version 1.0 12/8/2012
// * @since laxcus 1.0
// */
//public class WatchSiteTreeCellRenderer extends JLabel implements TreeCellRenderer {
//
//	private static final long serialVersionUID = 2909452960955966987L;
//	
//	/** 根 **/
//	private JTree root;
//
//	 /** Is the value currently selected. */
//    protected boolean selected;
//    
//    /** True if has focus. */
//    protected boolean hasFocus;
//    
//    /** True if draws focus border around icon as well. */
//    private boolean drawsFocusBorderAroundIcon;
//    
//    /** If true, a dashed line is drawn as the focus indicator. */
//    private boolean drawDashedFocusIndicator;
//    
//    // If drawDashedFocusIndicator is true, the following are used.
//    /**
//     * Background color of the tree.
//     */
//    private Color treeBGColor;
//    /**
//     * Color to draw the focus indicator in, determined from the background.
//     * color.
//     */
//    private Color focusBGColor;
//   
//	/** 选中前景颜色 **/
//	protected Color textSelectionColor;
//
//	/** 没选中前景颜色 **/
//	protected Color textNonSelectionColor;
//
//	/** 选中背景颜色 **/
//	protected Color backgroundSelectionColor;
//
//	/** 没选中背景颜色 **/
//	protected Color backgroundNonSelectionColor;
//
//	/** Color to use for the focus indicator when the node has focus. */
//	protected Color borderSelectionColor;
//
//	private boolean isDropCell;
//
//	private boolean fillBackground = true;
//	    
//	/**
//	 * 分布站点参数
//	 */
//	public WatchSiteTreeCellRenderer() {
//		super();
//		init();
//		initIcons();
//		setIconTextGap(5);
////		setBorder(new EmptyBorder(1, 2, 1, 2));
//	}
//
//	/**
//	 * 初始化界面参数
//	 */
//	private void init() {
//		setHorizontalAlignment(JLabel.LEFT);
//		setTextSelectionColor(UIManager.getColor(SkinColor.Tree_selectionForeground)); // "Tree.selectionForeground"));
//		setTextNonSelectionColor(UIManager.getColor(SkinColor.Tree_textForeground)); // "Tree.textForeground"));
//		setBackgroundSelectionColor(UIManager.getColor(SkinColor.Tree_selectionBackground)); // "Tree.selectionBackground"));
//		setBackgroundNonSelectionColor(UIManager.getColor(SkinColor.Tree_textBackground)); // "Tree.textBackground"));
//		
//		drawsFocusBorderAroundIcon = DefaultLookup.getBoolean(this, ui, "Tree.drawsFocusBorderAroundIcon", false);
//		drawDashedFocusIndicator = DefaultLookup.getBoolean(this, ui, "Tree.drawDashedFocusIndicator", false);
//	}
//
//	/** 节点和网络地址图标 **/
//	private Icon topIcon;
//	
//	private Icon logIcon;
//	
//	/** HOME子域集群 **/
//	private Icon homeIcon;
//	private Icon callIcon;
//	private Icon workIcon;
//	private Icon dataIcon;
//	private Icon buildIcon;
//
//	/** BANK子域集群 **/
//	private Icon bankIcon;
//	private Icon accountIcon;
//	private Icon hashIcon;
//	private Icon gateIcon;
//	private Icon entranceIcon;
//
//	/** 计算机节点图标 **/
//	private Icon siteIcon;
//
//	/**
//	 * 初始化图标
//	 */
//	private void initIcons() {
//		ResourceLoader loader = new ResourceLoader("conf/watch/image/sites/");
//		
//		int width = 16, height = 16;
//
//		topIcon = loader.findImage("top.png", width, height); // 显示TOP图标
//
//		homeIcon = loader.findImage("home.png", width, height);// 显示HOME图标
//
//		logIcon = loader.findImage("log.png", width, height); // 显示LOG图标
//
//		callIcon = loader.findImage("call.png", width, height); // 显示CALL图标
//
//		dataIcon = loader.findImage("data.png", width, height); // 显示DATA图标
//
//		workIcon = loader.findImage("work.png", width, height); // 显示WORK图标
//
//		buildIcon = loader.findImage("build.png", width, height); // 显示BUILD图标
//
//		bankIcon = loader.findImage("bank.png", width, height); // 显示BANK图标
//
//		accountIcon = loader.findImage("account.png", width, height); // 显示ACCOUNT图标
//
//		hashIcon = loader.findImage("hash.png", width, height); // 显示HASH图标
//
//		gateIcon = loader.findImage("gate.png", width, height); // 显示GATE图标
//
//		entranceIcon = loader.findImage("entrance.png", width, height); // 显示ENTRANCE图标
//
//		// 网络地址图标
//		siteIcon = loader.findImage("site.png", width, height);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
//	 */
//	@Override
//	public Component getTreeCellRendererComponent(JTree tree, Object value,
//			boolean sel, boolean expanded, boolean leaf, int row,
//			boolean focus) {
//		
//		// 根记录
//		root = tree;
//		// 焦点
//		hasFocus = focus;
//		
//		Color fg = null;
//		isDropCell = false;
//
//		JTree.DropLocation dropLocation = tree.getDropLocation();
//		if (dropLocation != null && dropLocation.getChildIndex() == -1
//				&& tree.getRowForPath(dropLocation.getPath()) == row) {
//
//			Color col = DefaultLookup.getColor(this, ui, "Tree.dropCellForeground");
//			if (col != null) {
//				fg = col;
//			} else {
//				fg = getTextSelectionColor();
//			}
//
//			isDropCell = true;
//		} else if (sel) {
//			// fg = getTextSelectionColor();
//			fg = textSelectionColor;
//		} else {
//			// fg = getTextNonSelectionColor();
//			fg = textNonSelectionColor;
//		}
//
//		// 字体
//		setFont(tree.getFont());
//
////		// 前景/背景
////		if (hasFocus || selected) {
////			setForeground(textSelectionColor);
//////			setBackground(backgroundSelectionColor);
////		} else {
////			setForeground(textNonSelectionColor);
//////			setBackground(backgroundNonSelectionColor);
////		}
//		
//		setForeground(fg);
//
//		if (value.getClass() == WatchSiteBrowserFamilyTreeNode.class) {
//			WatchSiteBrowserFamilyTreeNode e = (WatchSiteBrowserFamilyTreeNode) value;
//			Icon icon = null;
//
//			if (e.isTop()) {
//				icon = topIcon;
//			} else if (e.isHome()) {
//				icon = homeIcon;
//			} else if (e.isLog()) {
//				icon = logIcon;
//			} else if (e.isCall()) {
//				icon = callIcon;
//			} else if (e.isData()) {
//				icon = dataIcon;
//			} else if (e.isWork()) {
//				icon = workIcon;
//			} else if (e.isBuild()) {
//				icon = buildIcon;
//			} else if(e.isBank()) {
//				icon = bankIcon;
//			} else if(e.isAccount()) {
//				icon = accountIcon;
//			} else if(e.isHash()) {
//				icon = hashIcon;
//			} else if(e.isGate()) {
//				icon = gateIcon;
//			} else if(e.isEntrance()) {
//				icon = entranceIcon;
//			} else {
//				icon = siteIcon;
//			}
//
//			// 设置图标
//			setIcon(icon);
//			
//			FontKit.setLabelText(this, e.getText());
//			FontKit.setToolTipText(this, e.getText());
//			
//		} else if (value.getClass() == WatchSiteBrowserAddressTreeNode.class) {
//			WatchSiteBrowserAddressTreeNode e = (WatchSiteBrowserAddressTreeNode) value;
//
//			setIcon(siteIcon);
//			
//			FontKit.setLabelText(this, e.getText());
//			FontKit.setToolTipText(this, e.getText());
//		}
//
//		setComponentOrientation(tree.getComponentOrientation());
//		
////		setBorder(new EmptyBorder(2, 2, 2, 2));
//		setEnabled(tree.isEnabled());
//		setOpaque(true);
//		
//		selected = sel;
//
//		return this;
//	}
//
//	public void paint(Graphics g) {
//		Color bColor;
//
//		if (isDropCell) {
////			bColor = DefaultLookup.getColor(this, ui, "Tree.dropCellBackground");
////			if (bColor == null) {
////				bColor = getBackgroundSelectionColor();
////			}
//			bColor = backgroundSelectionColor;
//		} else if (selected) {
////			bColor = getBackgroundSelectionColor();
//			bColor = backgroundSelectionColor;
//		} else {
////			bColor = getBackgroundNonSelectionColor();
//			bColor = backgroundNonSelectionColor;
//			if (bColor == null) {
//				bColor = getBackground();
//			}
//		}
//
//		int imageOffset = -1;
//		if (bColor != null && fillBackground) {
//			// Icon currentI = getIcon();
//
//			imageOffset = getLabelStart();
//			g.setColor(bColor);
//			if (getComponentOrientation().isLeftToRight()) {
//				g.fillRect(imageOffset, 0, getWidth() - imageOffset, getHeight());
//			} else {
//				g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
//			}
//		}
//
//		if (hasFocus) {
//			if (drawsFocusBorderAroundIcon) {
//				imageOffset = 0;
//			} else if (imageOffset == -1) {
//				imageOffset = getLabelStart();
//			}
//			if (getComponentOrientation().isLeftToRight()) {
//				paintFocus(g, imageOffset, 0, getWidth() - imageOffset, getHeight(), bColor);
//			} else {
//				paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight(), bColor);
//			}
//		}
//		
//		super.paint(g);
//	}
//
//	private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
//		Color bsColor = getBorderSelectionColor();
//
//		if (bsColor != null && (selected || !drawDashedFocusIndicator)) {
//			g.setColor(bsColor);
//			g.drawRect(x, y, w - 1, h - 1);
//		}
//		if (drawDashedFocusIndicator && notColor != null) {
//			if (treeBGColor != notColor) {
//				treeBGColor = notColor;
////				focusBGColor = new Color(~notColor.getRGB());
//				focusBGColor = notColor;
//			}
//			g.setColor(focusBGColor);
//			BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
//		}
//	}
//	 
//	 private int getLabelStart() {
//		 Icon currentI = getIcon();
//		 if(currentI != null && getText() != null) {
//			 return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
//		 }
//		 return 0;
//	 }
//	 
//	/**
//	 * Overrides <code>JComponent.getPreferredSize</code> to return slightly
//	 * wider preferred size value.
//	 */
//	public Dimension getPreferredSize() {
//		Dimension retDimension = super.getPreferredSize();
//
//		if (retDimension != null)
//			retDimension = new Dimension(retDimension.width + 3,
//					retDimension.height);
//		return retDimension;
//	}
//
//	/**
//	 * Overridden for performance reasons.
//	 * See the <a href="#override">Implementation Note</a>
//	 * for more information.
//	 */
//	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {	
//		// Strings get interned...
//		if (propertyName == "text"
//			|| ((propertyName == "font" || propertyName == "foreground")
//					&& oldValue != newValue
//					&& getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
//
//			super.firePropertyChange(propertyName, oldValue, newValue);
//		}
//	}
//
//	/**
//	 * Returns the color the border is drawn.
//	 */
//	public Color getBorderSelectionColor() {
//		return borderSelectionColor;
//	}
//    
//	/**
//	 * Sets the color the text is drawn with when the node is selected.
//	 */
//	public void setTextSelectionColor(Color newColor) {
//		textSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the color the text is drawn with when the node is selected.
//	 */
//	public Color getTextSelectionColor() {
//		return textSelectionColor;
//	}
//
//	/**
//	 * Sets the color the text is drawn with when the node isn't selected.
//	 */
//	public void setTextNonSelectionColor(Color newColor) {
//		textNonSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the color the text is drawn with when the node isn't selected.
//	 */
//	public Color getTextNonSelectionColor() {
//		return textNonSelectionColor;
//	}
//
//	/**
//	 * Sets the color to use for the background if node is selected.
//	 */
//	public void setBackgroundSelectionColor(Color newColor) {
//		backgroundSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the color to use for the background if node is selected.
//	 */
//	public Color getBackgroundSelectionColor() {
//		return backgroundSelectionColor;
//	}
//
//	/**
//	 * Sets the background color to be used for non selected nodes.
//	 */
//	public void setBackgroundNonSelectionColor(Color newColor) {
//		backgroundNonSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the background color to be used for non selected nodes.
//	 */
//	public Color getBackgroundNonSelectionColor() {
//		return backgroundNonSelectionColor;
//	}
//
//	 public void setFont(Font font) {
//			if(font instanceof FontUIResource)
//			    font = null;
//			super.setFont(font);
//		    }
//	 
//	 /**
//	     * Gets the font of this component.
//	     * @return this component's font; if a font has not been set
//	     * for this component, the font of its parent is returned
//	     */
//	    public Font getFont() {
//	        Font font = super.getFont();
//
//	        if (font == null && root != null) {
//	            // Strive to return a non-null value, otherwise the html support
//	            // will typically pick up the wrong font in certain situations.
//	            font = root.getFont();
//	        }
//	        return font;
//	    }
//	    
//	    /**
//	     * Subclassed to map <code>ColorUIResource</code>s to null. If 
//	     * <code>color</code> is null, or a <code>ColorUIResource</code>, this
//	     * has the effect of letting the background color of the JTree show
//	     * through. On the other hand, if <code>color</code> is non-null, and not
//	     * a <code>ColorUIResource</code>, the background becomes
//	     * <code>color</code>.
//	     */
//	    public void setBackground(Color color) {
//		if(color instanceof ColorUIResource)
//		    color = null;
//		super.setBackground(color);
//	    }
//
//	
//}

///**
// * 分布站点的类型或者地址单元
// * 
// * @author scott.liang
// * @version 1.0 12/8/2012
// * @since laxcus 1.0
// */
//public class WatchSiteTreeCellRenderer extends  DefaultTreeCellRenderer { // JLabel implements TreeCellRenderer {
//
//	private static final long serialVersionUID = 2909452960955966987L;
//
//	/** 选中前景颜色 **/
//	protected Color textSelectionColor;
//
//	/** 没选中前景颜色 **/
//	protected Color textNonSelectionColor;
//
//	/** 选中背景颜色 **/
//	protected Color backgroundSelectionColor;
//
//	/** 没选中背景颜色 **/
//	protected Color backgroundNonSelectionColor;
//
//	/**
//	 * 分布站点参数
//	 */
//	public WatchSiteTreeCellRenderer() {
//		super();
//		init();
//		initIcons();
//		setIconTextGap(5);
////		setBorder(new EmptyBorder(1, 2, 1, 2));
//	}
//
//	/**
//	 * 初始化界面参数
//	 */
//	private void init() {
//		setHorizontalAlignment(JLabel.LEFT);
//		setTextSelectionColor(UIManager.getColor(SkinColor.Tree_selectionForeground)); // "Tree.selectionForeground"));
//		setTextNonSelectionColor(UIManager.getColor(SkinColor.Tree_textForeground)); // "Tree.textForeground"));
//		setBackgroundSelectionColor(UIManager.getColor(SkinColor.Tree_selectionBackground)); // "Tree.selectionBackground"));
//		setBackgroundNonSelectionColor(UIManager.getColor(SkinColor.Tree_textBackground)); // "Tree.textBackground"));
//	}
//
//	/** 节点和网络地址图标 **/
//	private Icon topIcon;
//	
//	private Icon logIcon;
//	
//	/** HOME子域集群 **/
//	private Icon homeIcon;
//	private Icon callIcon;
//	private Icon workIcon;
//	private Icon dataIcon;
//	private Icon buildIcon;
//
//	/** BANK子域集群 **/
//	private Icon bankIcon;
//	private Icon accountIcon;
//	private Icon hashIcon;
//	private Icon gateIcon;
//	private Icon entranceIcon;
//
//	/** 计算机节点图标 **/
//	private Icon siteIcon;
//
//	/**
//	 * 初始化图标
//	 */
//	private void initIcons() {
//		ResourceLoader loader = new ResourceLoader("conf/watch/image/sites/");
//		
//		int width = 16, height = 16;
//
//		topIcon = loader.findImage("top.png", width, height); // 显示TOP图标
//
//		homeIcon = loader.findImage("home.png", width, height);// 显示HOME图标
//
//		logIcon = loader.findImage("log.png", width, height); // 显示LOG图标
//
//		callIcon = loader.findImage("call.png", width, height); // 显示CALL图标
//
//		dataIcon = loader.findImage("data.png", width, height); // 显示DATA图标
//
//		workIcon = loader.findImage("work.png", width, height); // 显示WORK图标
//
//		buildIcon = loader.findImage("build.png", width, height); // 显示BUILD图标
//
//		bankIcon = loader.findImage("bank.png", width, height); // 显示BANK图标
//
//		accountIcon = loader.findImage("account.png", width, height); // 显示ACCOUNT图标
//
//		hashIcon = loader.findImage("hash.png", width, height); // 显示HASH图标
//
//		gateIcon = loader.findImage("gate.png", width, height); // 显示GATE图标
//
//		entranceIcon = loader.findImage("entrance.png", width, height); // 显示ENTRANCE图标
//
//		// 网络地址图标
//		siteIcon = loader.findImage("site.png", width, height);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
//	 */
//	@Override
//	public Component getTreeCellRendererComponent(JTree tree, Object value,
//			boolean selected, boolean expanded, boolean leaf, int row,
//			boolean hasFocus) {
//		
//		// 字体
//		setFont(tree.getFont());
//
//		// 前景/背景
//		if (hasFocus || selected) {
//			setForeground(textSelectionColor);
//			setBackground(backgroundSelectionColor);
//		} else {
//			setForeground(textNonSelectionColor);
//			setBackground(backgroundNonSelectionColor);
//		}
//
//		if (value.getClass() == WatchSiteBrowserFamilyTreeNode.class) {
//			WatchSiteBrowserFamilyTreeNode e = (WatchSiteBrowserFamilyTreeNode) value;
//			Icon icon = null;
//
//			if (e.isTop()) {
//				icon = topIcon;
//			} else if (e.isHome()) {
//				icon = homeIcon;
//			} else if (e.isLog()) {
//				icon = logIcon;
//			} else if (e.isCall()) {
//				icon = callIcon;
//			} else if (e.isData()) {
//				icon = dataIcon;
//			} else if (e.isWork()) {
//				icon = workIcon;
//			} else if (e.isBuild()) {
//				icon = buildIcon;
//			} else if(e.isBank()) {
//				icon = bankIcon;
//			} else if(e.isAccount()) {
//				icon = accountIcon;
//			} else if(e.isHash()) {
//				icon = hashIcon;
//			} else if(e.isGate()) {
//				icon = gateIcon;
//			} else if(e.isEntrance()) {
//				icon = entranceIcon;
//			} else {
//				icon = siteIcon;
//			}
//
//			// 设置图标
//			setIcon(icon);
//			
//			FontKit.setLabelText(this, e.getText());
//			FontKit.setToolTipText(this, e.getText());
//			
//		} else if (value.getClass() == WatchSiteBrowserAddressTreeNode.class) {
//			WatchSiteBrowserAddressTreeNode e = (WatchSiteBrowserAddressTreeNode) value;
//
//			setIcon(siteIcon);
//			
//			FontKit.setLabelText(this, e.getText());
//			FontKit.setToolTipText(this, e.getText());
//		}
//
////		setComponentOrientation(tree.getComponentOrientation());
//		
//		setBorder(new EmptyBorder(2, 2, 2, 2));
//		setEnabled(tree.isEnabled());
//		setOpaque(true);
//
//		return this;
//	}
//
//	/**
//	 * Sets the color the text is drawn with when the node is selected.
//	 */
//	public void setTextSelectionColor(Color newColor) {
//		textSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the color the text is drawn with when the node is selected.
//	 */
//	public Color getTextSelectionColor() {
//		return textSelectionColor;
//	}
//
//	/**
//	 * Sets the color the text is drawn with when the node isn't selected.
//	 */
//	public void setTextNonSelectionColor(Color newColor) {
//		textNonSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the color the text is drawn with when the node isn't selected.
//	 */
//	public Color getTextNonSelectionColor() {
//		return textNonSelectionColor;
//	}
//
//	/**
//	 * Sets the color to use for the background if node is selected.
//	 */
//	public void setBackgroundSelectionColor(Color newColor) {
//		backgroundSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the color to use for the background if node is selected.
//	 */
//	public Color getBackgroundSelectionColor() {
//		return backgroundSelectionColor;
//	}
//
//	/**
//	 * Sets the background color to be used for non selected nodes.
//	 */
//	public void setBackgroundNonSelectionColor(Color newColor) {
//		backgroundNonSelectionColor = newColor;
//	}
//
//	/**
//	 * Returns the background color to be used for non selected nodes.
//	 */
//	public Color getBackgroundNonSelectionColor() {
//		return backgroundNonSelectionColor;
//	}
//
////	/**
////	 * Overridden for performance reasons.
////	 * See the <a href="#override">Implementation Note</a>
////	 * for more information.
////	 */
////	public void validate() {}
////
////	/*
////	 * Overridden for performance reasons.
////	 * See the <a href="#override">Implementation Note</a>
////	 * for more information.
////	 */
////	public void invalidate() {}
////
////	/**
////	 * Overridden for performance reasons.
////	 * See the <a href="#override">Implementation Note</a>
////	 * for more information.
////	 */
////	@Override
////	public void revalidate() {}
////
////	/**
////	 * Overridden for performance reasons.
////	 * See the <a href="#override">Implementation Note</a>
////	 * for more information.
////	 */
////	public void repaint(long tm, int x, int y, int width, int height) {}
////
////	/**
////	 * Overridden for performance reasons.
////	 * See the <a href="#override">Implementation Note</a>
////	 * for more information.
////	 */
////	public void repaint(Rectangle r) {}
////
////	/**
////	 * Overridden for performance reasons.
////	 * See the <a href="#override">Implementation Note</a>
////	 * for more information.
////	 *
////	 * @since 1.5
////	 */
////	public void repaint() {}
////
////	/*
////	 * 重画界面，这个很重要！
////	 * @see javax.swing.JLabel#updateUI()
////	 */
////	@Override
////	public void updateUI() {
////		super.updateUI();
////		init();
////	}
//	
//}