/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;

import com.laxcus.gui.*;
import com.laxcus.gui.dialog.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 平面标题栏
 * 
 * @author scott.liang
 * @version 1.0 9/30/2021
 * @since laxcus 1.0
 */
public class FlatInternalFrameTitlePane extends MetalInternalFrameTitlePane {

	private static final long serialVersionUID = -2125956541386427976L;

	class SignButton extends JButton {

		private static final long serialVersionUID = 1L;

		private String UIKey;

		/** 暗 **/
		private int darkValue;

		/** 高亮调整 **/
		private int lightValue;

		/** 暗色的图标 **/
		private Icon darkIcon;

		/** 高亮图标 **/
		private Icon lightIcon;

		public SignButton(String uiKey, String opacityKey) {
			super();
			// 设置高亮/暗值
			setLightValue(30);
			setDarkValue(-30);

			setFocusPainted(false);
			setBorderPainted(false); // 不绘制边框
			setContentAreaFilled(true); // 平面
			setRolloverEnabled(true);
			setMargin(new Insets(0, 0, 0, 0));
			UIKey = uiKey;

			Object opacity = UIManager.get(opacityKey);
			if (opacity instanceof Boolean) {
				setOpaque(((Boolean) opacity).booleanValue());
			}
		}

		/**
		 * 设置调暗色值
		 * @param i
		 */
		public void setDarkValue(int i) {
			if (i <= 0) {
				darkValue = i;
			}
		}

		public int getDarkValue() {
			return darkValue;
		}

		/**
		 * 设置高亮值
		 * @param i
		 */
		public void setLightValue(int i) {
			if (i >= 0) {
				lightValue = i;
			}
		}

		/**
		 * 返回高亮值
		 * @return
		 */
		public int getLightValue() {
			return lightValue;
		}

		public boolean isFocusTraversable() {
			return false;
		}

		public void requestFocus() {

		}

		public AccessibleContext getAccessibleContext() {
			AccessibleContext ac = super.getAccessibleContext();
			if (UIKey != null) {
				ac.setAccessibleName(UIManager.getString(UIKey));
				UIKey = null;
			}
			return ac;
		}

		/**
		 * 返回选择的背景颜色
		 * @return
		 */
		private Color getBackgroundColor() {
			if (frame.isSelected()) {
				return MetalLookAndFeel.getWindowTitleBackground();
			} else {
				return MetalLookAndFeel.getWindowTitleInactiveBackground();
			}
		}

		//		/*
		//		 * (non-Javadoc)
		//		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		//		 */
		//		@Override
		//		public void paintComponent(Graphics g) {
		//			// 根据窗口状态，确定标题栏颜色，按纽背景颜色保持与标题栏一致
		//			Color color = getBackgroundColor();
		//			if (color == null) {
		//				color = getBackground();
		//			}
		//			
		//			Color oldColor = g.getColor();
		//			int width = getWidth();
		//			int height = getHeight();
		//			// 绘制背景
		//			g.setColor(color);
		//			g.fillRect(0, 0, width, height);
		//			
		//			// 绘制图标
		//			Icon icon = getIcon();
		//			if (icon != null) {
		//				// 调整图标
		//				if (icon.getClass() == ImageIcon.class) {
		//					ButtonModel bm = getModel();
		//
		//					//				if (bm.isRollover()) {
		//					//					//					if (bm.isPressed()) {
		//					//					//						icon = ImageUtil.dark((ImageIcon) icon, -30); // 调暗
		//					//					//					} 
		//					//					//					else if (!(!bm.isSelected() && bm.isArmed())) {
		//					//					//						icon = ImageUtil.brighter((ImageIcon) icon, 30); // 高亮
		//					//					//					}
		//					//
		//					//					//					else if (bm.isSelected()) {
		//					//					//						// 忽略，用默认的图标
		//					//					//					} else {
		//					//					//						if (!bm.isArmed()) {
		//					//					//							// icon = ImageUtil.brighter((ImageIcon) icon, 30); // 高亮
		//					//					//						}
		//					//					//					}
		//					//					
		//					//					
		//					//					
		//					//					if (!bm.isPressed() && !bm.isSelected() && !bm.isArmed()) {
		//					//						icon = ImageUtil.brighter((ImageIcon) icon, 30); // 高亮
		//					//					} else if (bm.isPressed()) {
		//					//						icon = ImageUtil.dark((ImageIcon) icon, -30); // 调暗
		//					//					}
		//					//				}
		//
		//					// 调暗
		//					if (bm.isRollover() && bm.isPressed()) {
		//						icon = ImageUtil.dark((ImageIcon) icon, -30);
		//					}
		//				}
		//
		//				int x = (width - icon.getIconWidth()) / 2;
		//				int y = (height - icon.getIconHeight()) / 2;
		//				if (x < 0) x = 0;
		//				if (y < 0) y = 0;
		//				icon.paintIcon(this, g, x, y);
		//			}
		//
		//			g.setColor(oldColor);
		//		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		public void paintComponent(Graphics g) {
			// 根据窗口状态，确定标题栏颜色，按纽背景颜色保持与标题栏一致
			Color color = getBackgroundColor();
			if (color == null) {
				color = getBackground();
			}

			Color oldColor = g.getColor();
			int width = getWidth();
			int height = getHeight();
			// 绘制背景
			g.setColor(color);
			g.fillRect(0, 0, width, height);

			// 绘制图标
			Icon icon = getIcon();
			if (icon != null) {

				//				// 调整图标
				//				if (icon.getClass() == ImageIcon.class) {
				//					ButtonModel bm = getModel();
				//					// 按下
				//					if (bm.isPressed()) {
				//						icon = ImageUtil.dark((ImageIcon) icon, darkValue);
				//					}
				//					// 进入按纽范围
				//					else if (bm.isRollover()) {
				//						if (lightValue > 0 ) {
				//							icon = ImageUtil.brighter((ImageIcon) icon, lightValue); // 高亮
				//						}
				//					}
				//					
				//					// 如果被选中，翻转为假，就是下次不要再使用了
				//					if (bm.isArmed()) {
				//						bm.setRollover(false);
				//					}
				//				}

				// 调整图标
				if (icon.getClass() == ImageIcon.class) {
					ButtonModel bm = getModel();
					// 在按纽区域内按下
					boolean press = (bm.isPressed() && bm.isArmed());
					if (press) {
						if (darkIcon != null) {
							icon = darkIcon;
						} else {
							icon = ImageUtil.dark((ImageIcon) icon, darkValue);
							if (icon != null) {
								darkIcon = icon;
							}
						}
					}
					// 进入按纽范围
					else if (bm.isRollover()) {
						if (lightValue > 0) {
							if (lightIcon != null) {
								icon = lightIcon;
							} else {
								icon = ImageUtil.brighter((ImageIcon) icon, lightValue); // 高亮
								if (icon != null) {
									lightIcon = icon;
								}
							}
						}
					}

					// 如果被选中，翻转为假，就是下次不要再使用了
					if (bm.isArmed()) {
						bm.setRollover(false);
					}
				}

				int x = (width - icon.getIconWidth()) / 2;
				int y = (height - icon.getIconHeight()) / 2;
				if (x < 0) x = 0;
				if (y < 0) y = 0;
				icon.paintIcon(this, g, x, y);
			}

			g.setColor(oldColor);
		}
	}

	//	class CloseButton extends JButton {
	//
	//		private static final long serialVersionUID = 1L;
	//
	//		private String UIKey;
	//
	//		public CloseButton(String uiKey, String opacityKey) {
	//			super();
	//			setFocusPainted(false);
	//			setBorderPainted(false); // 不绘制边框
	//			setContentAreaFilled(true); // 平面
	//			setRolloverEnabled(true);
	//			setMargin(new Insets(0, 0, 0, 0));
	//			UIKey = uiKey;
	//
	//			Object opacity = UIManager.get(opacityKey);
	//			if (opacity instanceof Boolean) {
	//				setOpaque(((Boolean) opacity).booleanValue());
	//			}
	//		}
	//
	//		public boolean isFocusTraversable() {
	//			return false;
	//		}
	//
	//		public void requestFocus() {
	//			
	//		}
	//
	//		public AccessibleContext getAccessibleContext() {
	//			AccessibleContext ac = super.getAccessibleContext();
	//			if (UIKey != null) {
	//				ac.setAccessibleName(UIManager.getString(UIKey));
	//				UIKey = null;
	//			}
	//			return ac;
	//		}
	//		
	////		/**
	////		 * 返回选择的背景颜色
	////		 * @return
	////		 */
	////		private Color getBackgroundColor() {
	////			if (frame.isSelected()) {
	////				return MetalLookAndFeel.getWindowTitleBackground();
	////			} else {
	////				return MetalLookAndFeel.getWindowTitleInactiveBackground();
	////			}
	////		}
	//
	////		/*
	////		 * (non-Javadoc)
	////		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	////		 */
	////		@Override
	////		public void paintComponent(Graphics g) {
	////			// 根据窗口状态，确定标题栏颜色，按纽背景颜色保持与标题栏一致
	////			Color color = getBackgroundColor();
	////			if (color == null) {
	////				color = getBackground();
	////			}
	////			
	////			Color oldColor = g.getColor();
	////			int width = getWidth();
	////			int height = getHeight();
	////			// 绘制背景
	////			g.setColor(color);
	////			g.fillRect(0, 0, width, height);
	////			
	////			// 绘制图标
	////			Icon icon = getIcon();
	////			if (icon != null && icon.getClass() == ImageIcon.class) {
	////				// 调整图标
	////				ButtonModel bm = getModel();
	////				if (bm.isRollover()) {
	////					if (bm.isPressed()) {
	////						icon = ImageUtil.dark((ImageIcon) icon, -30); // 调暗
	////					} else if (bm.isSelected()) {
	////						// 忽略，用默认的图标
	////					} else {
	////						if (!bm.isArmed()) {
	////							icon = ImageUtil.brighter((ImageIcon) icon, 30); // 高亮
	////						}
	////					}
	////				}
	////
	////				int x = (width - icon.getIconWidth()) / 2;
	////				int y = (height - icon.getIconHeight()) / 2;
	////				if (x < 0) x = 0;
	////				if (y < 0) y = 0;
	////				icon.paintIcon(this, g, x, y);
	////			}
	////
	////			g.setColor(oldColor);
	////		}
	//	}

	//	class RepaintActionListener implements java.awt.event.ActionListener {
	//
	//		/* (non-Javadoc)
	//		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	//		 */
	//		@Override
	//		public void actionPerformed(ActionEvent e) {
	//			Object source = e.getSource();
	//			if (source != null && source.getClass() == SignButton.class) {
	//				SignButton but = (SignButton) source;
	//				ButtonModel bm = but.getModel();
	//				bm.setPressed(false);
	//				bm.setRollover(false);
	//				but.repaint();
	//			}
	//		}
	//	}

	//	boolean entered = false;
	//	
	//	class ButtonMouseAdapter extends MouseAdapter {
	//		
	//		public void mouseEntered(MouseEvent e) {
	//			entered = true;
	//		}
	//		
	//		public void mouseExited(MouseEvent e){
	//			entered = false;
	//		}
	//	}

	class FlatTitlePaneLayout extends TitlePaneLayout {    
		public void addLayoutComponent(String name, Component c) {}
		public void removeLayoutComponent(Component c) {}   
		public Dimension preferredLayoutSize(Container c)  {
			return minimumLayoutSize(c);
		}

		public Dimension minimumLayoutSize(Container c) {
			// Compute width.
			int width = 30;
			if (frame.isClosable()) {
				width += 21;
			}
			if (frame.isMaximizable()) {
				width += 16 + (frame.isClosable() ? 10 : 4);
			}
			if (frame.isIconifiable()) {
				width += 16 + (frame.isMaximizable() ? 2 :
					(frame.isClosable() ? 10 : 4));
			}
			FontMetrics fm = frame.getFontMetrics(getFont());
			String frameTitle = frame.getTitle();
			//            int title_w = frameTitle != null ? SwingUtilities2.stringWidth(
					//                               frame, fm, frameTitle) : 0;

			int title_w = (frameTitle != null ? fm.stringWidth(frameTitle) : 0);
			//            	SwingUtilities2.stringWidth(
			//                    frame, fm, frameTitle) : 0;
			int title_length = frameTitle != null ? frameTitle.length() : 0;

			if (title_length > 2) {
				String s = frame.getTitle().substring(0, 2) + "...";
				int subtitle_w = fm.stringWidth(s);
				//                int subtitle_w = SwingUtilities2.stringWidth(frame, fm,
				//                                     frame.getTitle().substring(0, 2) + "...");
				width += ((title_w < subtitle_w) ? title_w : subtitle_w);
			}
			else {
				width += title_w;
			}

			// Compute height.
			int height = 0;
			if (isPalette) {
				height = paletteTitleHeight;
			} else {
				int fontHeight = fm.getHeight();
				fontHeight += 7;
				Icon icon = frame.getFrameIcon();
				int iconHeight = 0;
				if (icon != null) {
					// SystemMenuBar forces the icon to be 16x16 or less.
					iconHeight = Math.min(icon.getIconHeight(), 16);
				}
				iconHeight += 5;
				height = Math.max(fontHeight, iconHeight);
			}

			return new Dimension(width, height);
		} 

		public void layoutContainer(Container c) {
			boolean leftToRight = FlatUtil.isLeftToRight(frame); // MetalUtils.isLeftToRight(frame);

			int w = getWidth();
			int x = leftToRight ? w : 0;
			//            int y = 2;
			int spacing;

			// assumes all buttons have the same dimensions
			// these dimensions include the borders
			int buttonHeight = closeButton.getIcon().getIconHeight(); 
			int buttonWidth = closeButton.getIcon().getIconWidth();

			// Y坐标
			int height = getHeight();
			int y = (height - buttonHeight) / 2;
			y -= 1; // 向上提高2个像素
			if (y < 0) y = 0;

			// 间隔用6个像素

			if(frame.isClosable()) {
				if (isPalette) {
					spacing = 3;
					x += leftToRight ? -spacing -(buttonWidth+2) : spacing;
					closeButton.setBounds(x, y, buttonWidth+2, getHeight()-4);
					if( !leftToRight ) x += (buttonWidth+2);
				} else {
					spacing = 6;
					x += leftToRight ? -spacing -buttonWidth : spacing;
					closeButton.setBounds(x, y, buttonWidth, buttonHeight);
					if( !leftToRight ) x += buttonWidth;
				}
			}

			if(frame.isMaximizable() && !isPalette ) {
				//                spacing = frame.isClosable() ? 10 : 4;
				spacing = frame.isClosable() ? 6 : 6;
				x += leftToRight ? -spacing -buttonWidth : spacing;
				maxButton.setBounds(x, y, buttonWidth, buttonHeight);
				if( !leftToRight ) x += buttonWidth;
			} 

			if(frame.isIconifiable() && !isPalette ) {
				//                spacing = frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4);
				spacing = frame.isMaximizable() ? 6 : (frame.isClosable() ? 6 : 6);
				x += leftToRight ? -spacing -buttonWidth : spacing;
				iconButton.setBounds(x, y, buttonWidth, buttonHeight);      
				if( !leftToRight ) x += buttonWidth;
			}

			buttonsWidth = leftToRight ? w - x : x;
		} 
	}

	int buttonsWidth = 0;	
	
	/** 弹出菜单 **/
	private JPopupMenu rockMenu;

//	private final String REGEX = "^\\s*(?:[\\w\\W]+)[\\(\\[]([a-zA-Z]{1})[\\]\\)]\\s*$";
	
	/**
	 * 构造平面标题栏
	 * @param frame 窗口
	 */
	public FlatInternalFrameTitlePane(JInternalFrame frame) {
		super(frame);

		//		setPalette(true);

		//		MetalLookAndFeel.
	}

	protected LayoutManager createLayout() {
		return new FlatTitlePaneLayout();
	}

	//	static boolean installToolTips = false;

	private static String metalRestoreText;

	private static String metalMinimizeText;

	private static String metalMaximizeText;

	private static String metalCloseText;

	// 初始化时加载
	static {
		installToolTips();
	}

	/**
	 * 预加载
	 */
	private static void installToolTips() {
		//		 closeButtonToolTip =
		//             UIManager.getString("InternalFrame.closeButtonToolTip");
		//     iconButtonToolTip =
		//             UIManager.getString("InternalFrame.iconButtonToolTip");
		//     restoreButtonToolTip =
		//             UIManager.getString("InternalFrame.restoreButtonToolTip");
		//     maxButtonToolTip =
		//             UIManager.getString("InternalFrame.maxButtonToolTip");
		//     
		//     MetalMinimizeText 恢复
		//     MetalMaximizeText 放大
		//     MetalIconText 隐藏
		//     MetalCloseText 关闭

		// 恢复
		metalRestoreText = UIManager.getString("MetalRestoreText");
		UIManager.put("InternalFrame.restoreButtonToolTip", metalRestoreText);

		// 最小化
		metalMinimizeText = UIManager.getString("MetalMinimizeText");
		UIManager.put("InternalFrame.iconButtonToolTip", metalMinimizeText);

		// 最大化
		metalMaximizeText = UIManager.getString("MetalMaximizeText");
		UIManager.put("InternalFrame.maxButtonToolTip", metalMaximizeText);

		// 关闭
		metalCloseText = UIManager.getString("MetalCloseText");
		UIManager.put("InternalFrame.closeButtonToolTip", metalCloseText);
	}

//	/**
//	 * 这个方法是重新设置图标
//	 */
//	@Override
//	protected void installDefaults() {
//		//		// 加载工具提示
//		//		if (!installToolTips) {
//		//			installToolTips();
//		//			installToolTips = true;
//		//		}
//
//		super.installDefaults();
//		
//		// 取得FRAME的窗口，判断有默认的关闭图标
//		if (Laxkit.isClassFrom(super.frame, LightForm.class)) {
//			LightForm form = (LightForm) super.frame;
//			// 最小化图标
//			iconIcon = form.getMinimizeIcon();
//			if (iconIcon == null) {
//				iconIcon = UIManager.getIcon("MetalMinimizeIcon");
//			}
//			// 最大化图标
//			maxIcon = form.getMaximizeIcon();
//			if (maxIcon == null) {
//				maxIcon = UIManager.getIcon("MetalMaximizeIcon");
//			}
//			// 恢复图标
//			minIcon = form.getRestoreIcon();
//			if (minIcon == null) {
//				minIcon = UIManager.getIcon("MetalRestoreIcon");
//			}
//			// 关闭图标
//			closeIcon = form.getCloseIcon();
//			if (closeIcon == null) {
//				closeIcon = UIManager.getIcon("MetalCloseIcon");
//			}
//		} else {
//			iconIcon = UIManager.getIcon("MetalMinimizeIcon");
//			maxIcon = UIManager.getIcon("MetalMaximizeIcon");
//			minIcon = UIManager.getIcon("MetalRestoreIcon");
//			closeIcon = UIManager.getIcon("MetalCloseIcon");
//		}
//
//		//		// 关闭按纽调亮一些
//		//		ImageIcon icon = (ImageIcon) UIManager.getIcon("MetalCloseIcon");
//		//		closeIcon = ImageUtil.brighter(icon, 30);
//	}

	/**
	 * 这个方法是重新设置图标
	 */
	@Override
	protected void installDefaults() {
		//		// 加载工具提示
		//		if (!installToolTips) {
		//			installToolTips();
		//			installToolTips = true;
		//		}

		super.installDefaults();
		
		// 取得FRAME的窗口，判断有默认的关闭图标
		if (Laxkit.isClassFrom(super.frame, LightForm.class)) {
			LightForm form = (LightForm) super.frame;
			// 最小化图标
			iconIcon = form.getMinimizeIcon();
			// 最大化图标
			maxIcon = form.getMaximizeIcon();
			// 恢复图标
			minIcon = form.getRestoreIcon();
			// 关闭图标
			closeIcon = form.getCloseIcon();
		}
		
		// 判断如果是高分辨率，使用大图标
		boolean maxTitle = GUIKit.isHighScreen();

		// 系统图标
		if (iconIcon == null) {
			iconIcon = UIManager.getIcon(maxTitle ? "MaxMetalMinimizeIcon" : "MetalMinimizeIcon");
		}
		if (maxIcon == null) {
			maxIcon = UIManager.getIcon(maxTitle ? "MaxMetalMaximizeIcon" : "MetalMaximizeIcon");
		}
		if (minIcon == null) {
			minIcon = UIManager.getIcon(maxTitle ? "MaxMetalRestoreIcon" : "MetalRestoreIcon");
		}
		if (closeIcon == null) {
			closeIcon = UIManager.getIcon(maxTitle ? "MaxMetalCloseIcon" : "MetalCloseIcon");
		}
		
		// 弹出菜单
		initMenu();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getInsets()
	 */
	@Override
	public Insets getInsets() {
		return new Insets(0, 0, 0, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalInternalFrameTitlePane#createButtons()
	 */
	@Override
	protected void createButtons() {
		// 最小化
		iconButton = new SignButton("InternalFrameTitlePane.iconifyButtonAccessibleName",
		"InternalFrameTitlePane.iconifyButtonOpacity");
		iconButton.addActionListener(iconifyAction);
		iconButton.setToolTipText(UIManager.getString("MetalIconText"));
		((SignButton) iconButton).setDarkValue(-20);
		((SignButton) iconButton).setLightValue(32);

		//		        if (iconButtonToolTip != null && iconButtonToolTip.length() != 0) {
		//		            iconButton.setToolTipText(iconButtonToolTip);
		//		        }

		// 最大化
		maxButton = new SignButton("InternalFrameTitlePane.maximizeButtonAccessibleName",
		"InternalFrameTitlePane.maximizeButtonOpacity");
		maxButton.addActionListener(maximizeAction);
		((SignButton) maxButton).setDarkValue(-20);
		((SignButton) maxButton).setLightValue(28);

		//		// 关闭
		//		closeButton = new SignButton("InternalFrameTitlePane.closeButtonAccessibleName",
		//				"InternalFrameTitlePane.closeButtonOpacity");
		//		closeButton.addActionListener(closeAction);
		//		closeButton.setToolTipText(UIManager.getString("MetalCloseText"));
		////		closeButton.addActionListener(new RepaintActionListener());
		////		closeButton.addMouseListener(new ButtonMouseAdapter());

		//		        if (closeButtonToolTip != null && closeButtonToolTip.length() != 0) {
		//		            closeButton.setToolTipText(closeButtonToolTip);
		//		        }

		// 关闭
		closeButton = new SignButton("InternalFrameTitlePane.closeButtonAccessibleName",
		"InternalFrameTitlePane.closeButtonOpacity");
		closeButton.addActionListener(closeAction);
		((SignButton) closeButton).setDarkValue(-20);
		((SignButton) closeButton).setLightValue(22);
		if (metalCloseText != null && metalCloseText.length() != 0) {
			closeButton.setToolTipText(metalCloseText);
		}

		//		closeButton.setToolTipText(UIManager.getString("MetalCloseText"));
		//		ImageIcon icon = (ImageIcon) UIManager.getIcon("MetalCloseIcon");
		//		Icon light = ImageUtil.brighter(icon, 30); // 高亮
		//		closeButton.setRolloverIcon(light);
		//		Icon dark = ImageUtil.dark(icon, -30); // 调暗
		//		closeButton.setPressedIcon(dark);
		//		closeButton.setSelectedIcon(icon);
		//		closeButton.setRolloverSelectedIcon(icon);

		//		closeButton.addActionListener(new RepaintActionListener());
		//		closeButton.addMouseListener(new ButtonMouseAdapter());

		Border handyEmptyBorder = new EmptyBorder(0,0,0,0);
		Boolean paintActive = frame.isSelected() ? Boolean.TRUE:Boolean.FALSE;

		iconButton.putClientProperty("paintActive", paintActive);
		iconButton.setBorder(handyEmptyBorder);

		maxButton.putClientProperty("paintActive", paintActive);
		maxButton.setBorder(handyEmptyBorder);

		closeButton.putClientProperty("paintActive", paintActive);
		closeButton.setBorder(handyEmptyBorder);

		// The palette close icon isn't opaque while the regular close icon is.
		// This makes sure palette close buttons have the right background.
		//        closeButton.setBackground(MetalLookAndFeel.getPrimaryControlShadow());

		//        if (MetalLookAndFeel.usingOcean()) {
		//            iconButton.setContentAreaFilled(false);
		//            maxButton.setContentAreaFilled(false);
		//            closeButton.setContentAreaFilled(false);
		//        }

		setButtonIcons();

		//		// BasicInternalFrameTitlePane.setButtonIcons有问题，这里重新设置最小化图标
		//		iconButton.setIcon(iconIcon);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicInternalFrameTitlePane#setButtonIcons()
	 */
	@Override
	protected void setButtonIcons() {
		// super.setButtonIcons();

		if(frame.isIcon()) {
			if (minIcon != null) {
				// iconButton.setIcon(minIcon);
				iconButton.setIcon(iconIcon);
			}
			if (metalRestoreText != null && metalRestoreText.length() != 0) {
				iconButton.setToolTipText(metalRestoreText);
			}
			if (maxIcon != null) {
				maxButton.setIcon(maxIcon);
			}
			if (metalMaximizeText != null && metalMaximizeText.length() != 0) {
				maxButton.setToolTipText(metalMaximizeText);
			}
		} else if (frame.isMaximum()) {
			if (iconIcon != null) {
				iconButton.setIcon(iconIcon);
			}
			if (metalMinimizeText != null && metalMinimizeText.length() != 0) {
				iconButton.setToolTipText(metalMinimizeText);
			}
			if (minIcon != null) {
				maxButton.setIcon(minIcon);
			}
			if (metalRestoreText != null && metalRestoreText.length() != 0) {
				maxButton.setToolTipText(metalRestoreText);
			}
		} else {
			if (iconIcon != null) {
				iconButton.setIcon(iconIcon);
			}
			if (metalMinimizeText != null && metalMinimizeText.length() != 0) {
				iconButton.setToolTipText(metalMinimizeText);
			}
			if (maxIcon != null) {
				maxButton.setIcon(maxIcon);
			}
			if (metalMaximizeText != null && metalMaximizeText.length() != 0) {
				maxButton.setToolTipText(metalMaximizeText);
			}
		}
		if (closeIcon != null) {
			closeButton.setIcon(closeIcon);
		}
	}

//	protected void enableActions() {
//		super.enableActions();
//		
//		sizeAction.setEnabled(false);
//        moveAction.setEnabled(false);
//	}

	@Override
	protected void installListeners() {
		super.installListeners();
		// 支持鼠标事件
		addMouseListener(new TitleMouseAdapter());
	}
	
	private boolean hasMenuRestore() {
		if (frame.isMaximizable() && frame.isMaximum() && frame.isIcon()) {
			//			try {
			//				frame.setIcon(false);
			//			} catch (PropertyVetoException e) {
			//
			//			}
			return true;
		} else if (frame.isMaximizable() && frame.isMaximum()) {
			//			try {
			//				frame.setMaximum(false);
			//			} catch (PropertyVetoException e) {
			//			}
			return true;
		} else if (frame.isIconifiable() && frame.isIcon()) {
			//			// try {
			//			frame.setIcon(false);
			//			// } catch (PropertyVetoException e) { }
			return true;
		}
		return false;
	}

	void doPopupMenuRestore() {
		// 还原窗口
		if (frame.isMaximizable() && frame.isMaximum() && frame.isIcon()) {
			 try {
			frame.setIcon(false);
			 } catch (PropertyVetoException e) {
			
			 }
		} else if (frame.isMaximizable() && frame.isMaximum()) {
			try {
				frame.setMaximum(false);
			} catch (PropertyVetoException e) {
			}
		} else if (frame.isIconifiable() && frame.isIcon()) {
			 try {
			frame.setIcon(false);
			 } catch (PropertyVetoException e) { }
		}
	}

	private boolean hasMenuMini() {
		// 最小化
		if (frame.isIconifiable()) {
			if (!frame.isIcon()) {
				// // try {
				// frame.setIcon(true);
				// // } catch (PropertyVetoException e1) {
				// // }
				return true;
			} else {
				// // try {
				// frame.setIcon(false);
				// // } catch (PropertyVetoException e1) {
				// // }
				return false;
			}
		}
		return false;
	}

	void doPopupMenuMini() {
		// 最小化
		if (frame.isIconifiable()) {
			if (!frame.isIcon()) {
				 try {
				frame.setIcon(true);
				 } catch (PropertyVetoException e1) {
				 }
			} else {

				 try {
				frame.setIcon(false);
				 } catch (PropertyVetoException e1) {
				 }
			}
		}
	}
	
	private boolean hasMenuMax() {
		if (frame.isMaximizable()) {
			// 最小化后，最大化不可用，需要经过还原才能再用
			if (frame.isIcon()) {
				return false;
			}
			// 已经最大化，返回假
			if (frame.isMaximum()) {
				return false;
			} else {
				return true;
			}

			//			if (frame.isMaximum() && frame.isIcon()) {
			//				// frame.setIcon(false);
			//				return true;
			//			} else if (!frame.isMaximum()) {
			//				// try {
			//				// frame.setMaximum(true);
			//				// } catch (PropertyVetoException e) { }
			//				return true;
			//			} else {
			//				// try {
			//				// frame.setMaximum(false);
			//				// } catch (PropertyVetoException e) { }
			//				return false;
			//			}
		}
		return false;
	}


	void doPopupMenuMax() {
		if (frame.isMaximizable()) {
			if (frame.isMaximum() && frame.isIcon()) {
				try {
				frame.setIcon(false);
				} catch (PropertyVetoException e) { }
			} else if (!frame.isMaximum()) {
				try {
					frame.setMaximum(true);
				} catch (PropertyVetoException e) { }
			} else {
				try { 
					frame.setMaximum(false); 
				} catch (PropertyVetoException e) { }
			}
		}
	}
	
	private boolean hasMenuClose() {
		return frame.isClosable();
	}

	void doPopupMenuClose() {
		// 关闭
		// if (frame.isClosable()) {
		// frame.doDefaultCloseAction();
		// }

		// 关闭
		frame.doDefaultCloseAction();
	}	

	/**
	 * 显示弹出菜单
	 * @param e
	 */
	private void showPopupMenu(int newX, int newY, MouseEvent e) {
//		// 不满足SWING条件的POPUP触发，不处理
//		if (!e.isPopupTrigger()) {
//			return;
//		}

		JMenuItem item = MenuBuilder.findMenuItemByMethod(rockMenu, "doPopupMenuRestore");
		if (item != null) {
			item.setEnabled(hasMenuRestore());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doPopupMenuMini");
		if (item != null) {
			item.setEnabled(hasMenuMini());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doPopupMenuMax");
		if (item != null) {
			item.setEnabled(hasMenuMax());
		}
		item = MenuBuilder.findMenuItemByMethod(rockMenu, "doPopupMenuClose");
		if (item != null) {
			item.setEnabled(hasMenuClose());
		}
		
//		int newX = e.getX();
//		int newY = e.getY();
		
		Component invoker = e.getComponent(); // rockMenu.getInvoker();
//		if (invoker.getClass() == JScrollPane.class) {
//			JScrollPane jsp = (JScrollPane) invoker;
//			JViewport port = jsp.getViewport();
//			Point pt = port.getViewPosition();
//			//	System.out.printf("view:%d %d, mouse:%d %d\n", pt.x, pt.y, newX, newY);
//
//			// 调整坐标
//			if (pt.x > 0) {
//				newX = newX - pt.x;
//				if (newX < 0) newX = 0;
//			}
//			if (pt.y > 0) {
//				newY = newY - pt.y;
//				if (newY < 0) newY = 0;
//			}
//		}
		
		rockMenu.show(invoker, newX, newY);
		
//		rockMenu.show(rockMenu.getInvoker(), newX, newY);
	}
	
	private boolean isModalDialog() {
		// 如果是LightDialog
		if (Laxkit.isClassFrom(frame, LightDialog.class)) {
			LightDialog dlg = (LightDialog) frame;
			// 如果是模态窗口，不支持弹出菜单
			if (dlg.isModal()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 菜单事件
	 * @param event
	 */
	private void click(ActionEvent event) {
		Object object = event.getSource();
		// 必须是继承自“JMenuItem”
		if (Laxkit.isClassFrom(object, JMenuItem.class)) {
			JMenuItem source = (JMenuItem) object;
			String methodName = source.getName();
			invoke(methodName);
		}
	}

	/**
	 * 调用实例
	 * @param methodName
	 */
	private void invoke(String methodName) {
		if (methodName == null || methodName.isEmpty()) {
			return;
		}

		try {
			Method method = (getClass()).getDeclaredMethod(methodName, new Class<?>[0]);
			method.invoke(this, new Object[0]);
		} catch (NoSuchMethodException e) {
			Logger.error(e);
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (InvocationTargetException e) {
			Logger.error(e);
		}
	}
	
	class MenuItemClick implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			click(e);
		}
	}

	/**
	 * 生成菜单项
	 * @param iconKey
	 * @param textKey
	 * @param mnemonicKey
	 * @param methodKey
	 * @return
	 */
	private JMenuItem createMenuItem(Icon icon, String iconKey, String textKey,
			String mnemonicKey, String methodKey) {
		return MenuBuilder.createMenuItem(icon, iconKey, textKey, mnemonicKey, null, methodKey, new MenuItemClick());
	}
	
	/**
	 * 初始化弹出菜单
	 */
	private void initMenu() {
		
		
//		//		NotifyDialog.MenuitemCopyTableText 复制[C]
//		//NotifyDialog.MenuitemDeleteTableText 清除系统记录 [T]
//		//NotifyDialog.MenuitemSelectAllTableText 选择全部[A]
//		                                                                                   
//		String[] texts = new String[] { "NotifyDialog.MenuitemCopyTableText",
//				"NotifyDialog.MenuitemDeleteTableText","NotifyDialog.MenuitemSelectAllTableText" };
////		// 快捷键
////		char[] shorts = new char[] {  'C','D','A' };
//		
//		// 操作方法
//		String[] methods = new String[] { "doCopy", "doClear","doSelectAll" };
//
//		JMenuItem copyItem = createMenuItem(texts[0], methods[0]);
//		JMenuItem mnuDelete = createMenuItem(texts[1], methods[1]);
//		JMenuItem selectAllItem = createMenuItem(texts[2], methods[2]);
//		
//		rockMenu = new JPopupMenu();
//		rockMenu.add(copyItem);
//		rockMenu.add(mnuDelete);
//		rockMenu.add(selectAllItem);
		
		rockMenu = new JPopupMenu();
		
		LightForm form = (LightForm) frame;
		
		JMenuItem recover = createMenuItem(form.getRestoreIcon(), "FlatTitlePane.PopupMenu.RestoreIcon","FlatTitlePane.PopupMenu.RestoreText", "FlatTitlePane.PopupMenu.RestoreMWord", "FlatTitlePane.PopupMenu.RestoreMethod");
		JMenuItem mini = createMenuItem(form.getMinimizeIcon(), "FlatTitlePane.PopupMenu.MiniIcon", "FlatTitlePane.PopupMenu.MiniText", "FlatTitlePane.PopupMenu.MiniMWord", "FlatTitlePane.PopupMenu.MiniMethod");
		JMenuItem max = createMenuItem(form.getMaximizeIcon(), "FlatTitlePane.PopupMenu.MaxIcon","FlatTitlePane.PopupMenu.MaxText", "FlatTitlePane.PopupMenu.MaxMWord", "FlatTitlePane.PopupMenu.MaxMethod");
		JMenuItem close = createMenuItem(form.getCloseIcon(), "FlatTitlePane.PopupMenu.CloseIcon","FlatTitlePane.PopupMenu.CloseText", "FlatTitlePane.PopupMenu.CloseMWord", "FlatTitlePane.PopupMenu.CloseMethod");

		rockMenu.add(recover);
		rockMenu.add(mini);
		rockMenu.add(max);
		rockMenu.addSeparator();
		rockMenu.add(close);
		
//		rockMenu.add(new JMenuItem("恢复"));
//		rockMenu.add(new JMenuItem("最大"));
//		rockMenu.add(new JMenuItem("最小"));
//		rockMenu.add(new JMenuItem("关闭"));
//		rockMenu.addSeparator();
//		rockMenu.add(new JMenuItem("大江歌罢掉头东"));
//		rockMenu.add(new JMenuItem("海日升残夜，江春入旧年"));
////		rockMenu.setBorder(new HighlightBorder(1) );

		rockMenu.setInvoker(this);
//		table.addMouseListener(new CommandMouseAdapter());
	}
	
//	class ActionAdapter implements ActionListener {
//		public void actionPerformed(ActionEvent e) {
////			click(e);
//		}
//	}
	
//	/**
//	 * 生成菜单项
//	 * @param textKey
//	 * @param method
//	 * @param w
//	 * @return
//	 */
//	private JMenuItem createMenuItem(String textKey, String method) {
//		String text = UIManager.getString(textKey);
//		JMenuItem item = new JMenuItem(text);
//		item.setName(method);
//		item.addActionListener(new ActionAdapter());
//		setMnemonic(item, text);
//
//		// // 如果是快捷吸
//		// if ((w >= 'a' && w <= 'z') || (w >= 'A' && w <= 'Z')) {
//		// item.setMnemonic(w);
//		// }
//
//		item.setBorder(new EmptyBorder(2, 4, 2, 4));
//		return item;
//	}
//			
//	/**
//	 * 设置快捷键
//	 * @param but
//	 * @param input
//	 */
//	public void setMnemonic(JMenuItem but, String input) {
//		if (input == null) {
//			return;
//		}
//		Pattern pattern = Pattern.compile(REGEX);
//		Matcher matcher = pattern.matcher(input);
//		if (matcher.matches()) {
//			String s = matcher.group(1);
//			char w = s.charAt(0);
//			but.setMnemonic(w);
//		}
//	}

//	/**
//	 * 生成菜单项
//	 * @param textKey
//	 * @param method
//	 * @param w
//	 * @return
//	 */
//	private JMenuItem createMenuItem(String textKey, String method) {
//		String text = UIManager.getString(textKey);
//		JMenuItem item = new JMenuItem(text);
//		item.setName(method);
//		item.addActionListener(new ActionAdapter());
//		setMnemonic(item, text);
//		
////		// 如果是快捷吸
////		if ((w >= 'a' && w <= 'z') || (w >= 'A' && w <= 'Z')) {
////			item.setMnemonic(w);
////		}
//		
//		item.setBorder(new EmptyBorder(2,4,2,4));
//		return item;
//	}

//	/**
//	 * 判断是从左到右的布局
//	 * @param c
//	 * @return
//	 */
//	private boolean isLeftToRight(Component c) {
//		return c.getComponentOrientation().isLeftToRight();
//	}
	
	/**
	 * 图标区域
	 */
	private Rectangle getIconBounds() {
		boolean leftToRight = FlatUtil.isLeftToRight(frame);
		int xOffset = leftToRight ? 5 : getWidth() - 5;
		Rectangle rect = null;

		Icon icon = frame.getFrameIcon();
		if (icon != null) {
			if (!leftToRight) {
				xOffset -= icon.getIconWidth();
			}
			int iconY = ((getHeight() / 2) - (icon.getIconHeight() / 2));
			rect = new Rectangle(xOffset, iconY, icon.getIconWidth(), icon.getIconHeight());
		}
		return rect;
	}
	
	/**
	 * 双击面板的处理
	 */
	private void doubleClickTitlePane() {
		if (frame.isIconifiable() && frame.isIcon()) {
			try {
				frame.setIcon(false);
			} catch (PropertyVetoException e2) {
			}
		} else if (frame.isMaximizable()) {
			if (!frame.isMaximum()) {
				try {
					frame.setMaximum(true);
				} catch (PropertyVetoException e2) {
				}
			} else {
				try {
					frame.setMaximum(false);
				} catch (PropertyVetoException e3) {
				}
			}
		}
	}
	
//	class TitleMouseAdapter extends MouseAdapter {
//
//		public void mouseClicked(MouseEvent e) {
//			if (e.getButton() != MouseEvent.BUTTON1) {
//				return;
//			}
//			// 图标区域
//			Rectangle rect = getIconBounds();
//			if (rect == null) {
//				return;
//			}
//
//			int x = e.getX();
//			int y = e.getY();
//
//			int clicks = e.getClickCount();
//			// 单击一次或者多次
//			if (clicks == 1) {
//				// 在标题图标区域，并且不是模态窗口，显示弹出菜单
//				if (rect.contains(x, y) && !isModalDialog()) {
//					// System.out.println("弹出菜单!");
//					// showPopupMenu(rect.x, rect.y + rect.height, e);
//					
//					int newX = (x != 4 ? 4 : x);
//					int newY = rect.x + rect.height;
//					if (newY < getHeight()) {
//						newY = getHeight() + 1;
//					}
//					showPopupMenu(newX, newY, e);
//				}
//			}
//			// 超过一次时
//			else {
//				// 不在标题图标区域
//				if (!rect.contains(x, y)) {
//					doubleClickTitlePane();
////					System.out.println("单击标题面板!!!");
//				}
//			}
//		}
//	}
	
	class TitleMouseAdapter extends MouseAdapter {
		
		private long lastTime;
		private long interval; // 单击间隔
		private int clicks;
		
		public TitleMouseAdapter() {
			super();
			init();
		}
		
		private void init() {
			clicks = 0;
			lastTime = System.currentTimeMillis();
			String str = UIManager.getString("MetalTitleClickInterval");
			interval = ConfigParser.splitLong(str, 600);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}

			// 图标区域
			Rectangle rect = getIconBounds();
			if (rect == null) {
				return;
			}

			int x = e.getX();
			int y = e.getY();
			
			// 超过间隔时间，重置
			long now = System.currentTimeMillis();
			if (now - lastTime >= interval) {
				clicks = 0;
			}
			lastTime = now;
			clicks++;

			// 单击一次或者多次
			if (clicks == 1) {
				// 在标题图标区域，并且不是模态窗口，显示弹出菜单
				if (rect.contains(x, y) && !isModalDialog()) {
					int newX = (x != 4 ? 4 : x);
					int newY = rect.x + rect.height;
					if (newY < getHeight()) {
						newY = getHeight() + 1;
					}
					showPopupMenu(newX, newY, e);
					clicks = 0;
				}
			}
			// 超过一次时
			else {
				// 不在标题图标区域
				if (!rect.contains(x, y)) {
					doubleClickTitlePane();
					clicks = 0;
				}
			}
		}
	}
	
//	
//	private boolean isTitleIcon(int x, int y) {
//		boolean leftToRight = isLeftToRight(frame);
//		int width = getWidth();
//		int height = getHeight();
//
//		int xOffset = leftToRight ? 5 : width - 5;
//
//		Icon icon = frame.getFrameIcon();
//		// 绘制图标
//		if (icon != null) {
//			if (!leftToRight) xOffset -= icon.getIconWidth();
//
//			int iconY = ((height / 2) - (icon.getIconHeight() / 2));
//
//			// 判断在范围内
//			if (xOffset <= x && x < xOffset + icon.getIconWidth()
//					&& iconY <= y && y < iconY + icon.getIconHeight()) {
//				return true;
//			}
//
//			//			icon.paintIcon(frame, g, xOffset, iconY);
//			// 定位到文本标题位置
//			//			xOffset += (leftToRight ? icon.getIconWidth() + 5 : -5);
//		}
//
//		return false;
//	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalInternalFrameTitlePane#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (isPalette) {
			paintPalette(g);
			return;
		}

		Color oldColor = g.getColor();

		boolean leftToRight = FlatUtil.isLeftToRight(frame); //  MetalUtils.isLeftToRight(frame);
		boolean isSelected = frame.isSelected();

		int width = getWidth();
		int height = getHeight();

		Color background = null;
		Color foreground = null;
		Color shadow = null;

		if (isSelected) {
			background = MetalLookAndFeel.getWindowTitleBackground();
			foreground = MetalLookAndFeel.getWindowTitleForeground();
			shadow = MetalLookAndFeel.getPrimaryControlDarkShadow();

			//			// 调整背景
			//			if (background != null) {
				//				ESL esl = new RGB(background).toESL();
			//				background = (Skins.isGraySkin() ? esl.toDraker(18).toColor() : esl.toBrighter(15).toColor());
			//			}
		} else {
			background = MetalLookAndFeel.getWindowTitleInactiveBackground();
			foreground = MetalLookAndFeel.getWindowTitleInactiveForeground();
			shadow = MetalLookAndFeel.getControlDarkShadow();

			//			// 调整背景
			//			if (background != null) {
			//				ESL esl = new RGB(background).toESL();
			////				background = (Skins.isGraySkin() ? esl.toDraker(15).toColor() : esl.toDraker(4).toColor());
			//				background = (Skins.isGraySkin() ? esl.toBrighter(4).toColor() : esl.toDraker(4).toColor());
			//			}
		}

		// 默认值
		if (background == null) {
			background = getBackground();
		}
		if (shadow == null) {
			shadow = getBackground();
		}
		if (foreground == null) {
			foreground = getForeground();
		}

		g.setColor(background);
		g.fillRect(0, 0, width, height);

		g.setColor(shadow);
		g.drawLine(0, height - 1, width, height - 1);

		int xOffset = leftToRight ? 5 : width - 5;

		// 图标
		Icon icon = frame.getFrameIcon();
		// 标题
		String title = frame.getTitle();

		// 绘制图标
		if (icon != null) {
			if (!leftToRight) xOffset -= icon.getIconWidth();

			int iconY = ((height / 2) - (icon.getIconHeight() / 2));
			icon.paintIcon(frame, g, xOffset, iconY);
			// 定位到文本标题位置
			xOffset += (leftToRight ? icon.getIconWidth() + 5 : -5);
		}

		// 绘制标题
		if (title != null) {
			Font f = getFont();
			g.setFont(f);
			FontMetrics fm = g.getFontMetrics(f);

			// 前景颜色
			g.setColor(foreground);

			int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();

			Rectangle rect = new Rectangle(0, 0, 0, 0);
			if (frame.isIconifiable()) {
				rect = iconButton.getBounds();
			} else if (frame.isMaximizable()) {
				rect = maxButton.getBounds();
			} else if (frame.isClosable()) {
				rect = closeButton.getBounds();
			}
			int titleW = 0;
			String oldTitle = title;

			// 判断字符串不一致时
			if (leftToRight) {
				if (rect.x == 0) {
					rect.x = frame.getWidth() - frame.getInsets().right - 2;
				}
				titleW = rect.x - xOffset - 4;
				title = getTitle(title, fm, titleW);
			} else {
				titleW = xOffset - rect.x - rect.width - 4;
				title = getTitle(title, fm, titleW);
				xOffset -= 5;
			}

			// 计算字符宽度，居中显示
			int w = fm.stringWidth(title);
			int x = (width - w) / 2;
			// 两个条件：最小左侧X下标，或者前后两个参数不一致时
			if (x < xOffset || oldTitle.compareTo(title) != 0) {
				x = xOffset;
			}

			g.drawString(title, x, yOffset);
		}

		// 修改颜色
		g.setColor(oldColor);
	}	
}

///*
// * (non-Javadoc)
// * @see javax.swing.plaf.metal.MetalInternalFrameTitlePane#paintComponent(java.awt.Graphics)
// */
//@Override
//public void paintComponent(Graphics g) {
//	if (isPalette) {
//		paintPalette(g);
//		return;
//	}
//    
//    Color oldColor = g.getColor();
//    
//    boolean leftToRight = true; //  MetalUtils.isLeftToRight(frame);
//	boolean isSelected = frame.isSelected();
//	
//	int width = getWidth();
//    int height = getHeight();
//
//    Color background = null;
//    Color foreground = null;
//    Color shadow = null;
//    
//	if (isSelected) {
////		closeButton.setContentAreaFilled(true);
////		maxButton.setContentAreaFilled(true);
////		iconButton.setContentAreaFilled(true);
//
//		background = MetalLookAndFeel.getWindowTitleBackground();
//		shadow = MetalLookAndFeel.getPrimaryControlDarkShadow();
//		foreground = MetalLookAndFeel.getWindowTitleForeground();
//	} else {
////		closeButton.setContentAreaFilled(false);
////		maxButton.setContentAreaFilled(false);
////		iconButton.setContentAreaFilled(false);
//
//		background = MetalLookAndFeel.getWindowTitleInactiveBackground();
//		foreground = MetalLookAndFeel.getWindowTitleInactiveForeground();
//		shadow = MetalLookAndFeel.getControlDarkShadow();
//	}
//	
//	// 默认值
//	if (background == null) {
//		background = getBackground();
//	}
//	if (shadow == null) {
//		shadow = getBackground();
//	}
//	if (foreground == null) {
//		foreground = getForeground();
//	}
//	
//	g.setColor(background);
//	g.fillRect(0, 0, width, height);
//
//	g.setColor(shadow);
//	g.drawLine(0, height - 1, width, height - 1);
//	g.drawLine(0, 0, 0, 0);
//	g.drawLine(width - 1, 0, width - 1, 0);
//    
////    int titleLength = 0;
//	
//    int xOffset = leftToRight ? 5 : width - 5;
//    String frameTitle = frame.getTitle();
//
//	Icon icon = frame.getFrameIcon();
//	if (icon != null) {
//		if (!leftToRight) xOffset -= icon.getIconWidth();
//		
//		int iconY = ((height / 2) - (icon.getIconHeight() / 2));
//		icon.paintIcon(frame, g, xOffset, iconY);
//		// 定位到文本标题位置
//		xOffset += (leftToRight ? icon.getIconWidth() + 5 : -5);
//	}
//    
//	if (frameTitle != null) {
//		Font f = getFont();
//		g.setFont(f);
//		FontMetrics fm = g.getFontMetrics(f); // SwingUtilities2.getFontMetrics(frame, g, f);
////		int fHeight = fm.getHeight();
//
//		// 前景颜色
//		g.setColor(foreground);
//
//		int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();
//
//		Rectangle rect = new Rectangle(0, 0, 0, 0);
//		if (frame.isIconifiable()) {
//			rect = iconButton.getBounds();
//		} else if (frame.isMaximizable()) {
//			rect = maxButton.getBounds();
//		} else if (frame.isClosable()) {
//			rect = closeButton.getBounds();
//		}
//		int titleW;
//
//		if (leftToRight) {
//			if (rect.x == 0) {
//				rect.x = frame.getWidth() - frame.getInsets().right - 2;
//			}
//			titleW = rect.x - xOffset - 4;
//			frameTitle = getTitle(frameTitle, fm, titleW);
//		} else {
//			titleW = xOffset - rect.x - rect.width - 4;
//			frameTitle = getTitle(frameTitle, fm, titleW);
//			xOffset -= 5; // SwingUtilities2.stringWidth(frame, fm, frameTitle);
//		}
//
////		titleLength = SwingUtilities2.stringWidth(frame, fm, frameTitle);
////		SwingUtilities2.drawString(frame, g, frameTitle, xOffset, yOffset);
//		
//		// 计算字符宽度，居中显示
//		int w = fm.stringWidth(frameTitle);
//		int x = (width - w) / 2;
//		if (x < xOffset) x = xOffset; // 最小的左侧X下标
//		g.drawString(frameTitle, x, yOffset);
//		
//		System.out.printf("%d %d, - %s\n", x, xOffset, frameTitle);
//		
////		g.drawString(frameTitle, xOffset, yOffset);
////		xOffset += leftToRight ? titleLength + 5 : -5;
//	}
//	
//	// 修改颜色
//	g.setColor(oldColor);
//}