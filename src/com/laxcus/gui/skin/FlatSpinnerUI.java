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
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 *
 * @author scott.liang
 * @version 1.0 6/16/2022
 * @since laxcus 1.0
 */
public class FlatSpinnerUI extends BasicSpinnerUI {

	class SpinnerBorder extends AbstractBorder  {

		private static final long serialVersionUID = 1L;

		public SpinnerBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();

			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(0, 0, w - 1, h - 1);

			g.setColor(old);
		}

		public Insets getBorderInsets(Component c) {
//			return new Insets(3, 3, 3, 3);
			return new Insets(1,1,1,1);
		}
	}

	class ButtonBorder extends AbstractBorder  {

		private static final long serialVersionUID = 1L;

		public ButtonBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();

			AbstractButton button = (AbstractButton)c;
			ButtonModel model = button.getModel();
			
			if (model.isEnabled()) {
				boolean isPressed = model.isPressed() && model.isArmed();
				boolean isDefault = (button instanceof JButton && ((JButton)button).isDefaultButton());
				
				g.translate(x, y);
				
				g.setColor(MetalLookAndFeel.getControlDarkShadow());

				if (isPressed && isDefault) {
					//	MetalUtils.drawDefaultButtonPressedBorder(g, x, y, w, h);
					g.drawRect(0, 0, w - 1, h - 1);
				} else if (isPressed) {
					//	MetalUtils.drawPressed3DBorder( g, x, y, w, h );
					g.drawRect(0, 0, w - 1, h - 1);
				} else if (isDefault) {
					//	MetalUtils.drawDefaultButtonBorder( g, x, y, w, h, false);
					g.drawRect(0, 0, w - 1, h - 1);
				} else {
					//	MetalUtils.drawButtonBorder( g, x, y, w, h, false);
					g.drawRect(0, 0, w - 1, h - 1);
				}
				
				g.translate(-x, -y);
				
			} else { // disabled state
				g.translate( x, y);

				// g.setColor(MetalLookAndFeel.getControlShadow());

				g.setColor(MetalLookAndFeel.getControlDarkShadow());

				g.drawRect(0, 0, w - 1, h - 1);
				g.translate(-x, -y);
				//				MetalUtils.drawDisabledBorder( g, x, y, w-1, h-1 );
			}
			
			//			Color old = g.getColor();
			//
			//			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			//			g.drawRect(0, 0, w - 1, h - 1);

			g.setColor(old);
		}
		
//		/* (non-Javadoc)
//		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
//		 */
//		@Override
//		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//			AbstractButton button = (AbstractButton) c;
//			width = button.getWidth();
//			height = button.getHeight();
//			if (button.isEnabled()) {
//				ButtonModel bm = button.getModel();
//				boolean pressed = (bm.isPressed() && bm.isArmed());
//				if (pressed) {
//					paintMetalFlatBorder(button, g, 0, 0, width, height);
//				} else {
//					paintMetalRaisedBorder(button, g, 0, 0, width, height); // 不是平面，浮起
//				}
//			} else {
//				paintMetalRaisedBorder(button, g, 0, 0, width, height); // 不是平面，浮起
//			}
//		}
//		
//		/**
//		 * 绘制METAL界面的阳刻浮雕效果
//		 *
//		 * @param c
//		 * @param g
//		 * @param x
//		 * @param y
//		 * @param width
//		 * @param height
//		 */
//		private void paintMetalRaisedBorder(Component c, Graphics g, int x, 
//				int y, int width, int height) {
//
////			Color color = c.getBackground();
////			ESL esl = new RGB(color).toESL();
//
////			// 区分颜色
////			Color dark, light;
////			if (Skins.isGraySkin()) {
////				dark = esl.toDraker(FlatButtonOutsideBorder.GRAY_DRAK_VALUE).toColor();
////				light = esl.toBrighter(FlatButtonOutsideBorder.GRAY_LIGHT_VALUE).toColor();
////			} else {
////				dark = esl.toDraker(FlatButtonOutsideBorder.DRAK_VALUE).toColor();
////				light = esl.toBrighter(FlatButtonOutsideBorder.LIGHT_VALUE).toColor();
////			}
//
//			int x2 = x + width - 1;
//			int y2 = y + height - 1;
//
//			// 原色
//			Color old = g.getColor();
//			
//
//			// 亮色
////			g.setColor(light);
//			g.translate(x, y);
//			g.setColor( MetalLookAndFeel.getControlShadow() );
//			
//			g.drawLine(x, y, x2, y); // 上线
//			g.drawLine(x, y, x, y2); // 左线
//
//			// 暗色
////			g.setColor(dark);
//			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.drawLine(x, y2, x2, y2); // 下线
//			g.drawLine(x2, y, x2, y2); // 右线
//
//			g.translate(-x, -y);
//			
//			// 设置颜色
//			g.setColor(old);
//		}
//
//		private void paintMetalFlatBorder(Component c, Graphics g, int x, int y, int width, int height) {
//			Color old = g.getColor();
//			
////			Color color = c.getBackground();
////			// 取组件的背景色
////			if (color == null) {
////				color = c.getBackground();
////			}
////			// 颜色
////			ESL esl = new RGB(color).toESL();
////			if (Skins.isGraySkin()) {
////				color = esl.toDraker(FlatButtonOutsideBorder.GRAY_DRAK_VALUE).toColor();
////			} else {
////				color = esl.toBrighter(FlatButtonOutsideBorder.LIGHT_VALUE).toColor();
////			}
////
////			// 设置颜色
////			g.setColor(color);
//			
//			g.setColor( MetalLookAndFeel.getControlShadow() );
//			g.drawRect(x, y, width - 1, height - 1);
//
//			// 恢复为旧颜色
//			g.setColor(old);
//		}

		public Insets getBorderInsets(Component c) {
			return new Insets(3, 3, 3, 3);
//			return new Insets(3, 3, 3, 3);
//			 return new Insets(1,1,1,1);
		}
	}
	
	class FlatArrowButton extends BasicArrowButton {

		private static final long serialVersionUID = 1L;

		/**
		 * @param arg0
		 */
		public FlatArrowButton(int direction) {
			super(direction);
		}
		
		private Color getDisableColor() {
			Color c = MetalLookAndFeel.getControlShadow();
			if (!Skins.isGraySkin()) {
				// c = Color.DARK_GRAY;
				if (Skins.isDarkSkin()) {
					c = new Color(52,52,52);
				} else {
					c = new Color(98, 98, 98);
				}
			}
			return c;
		}
		
		public void paintTriangle(Graphics g, int x, int y, int size, int direction, boolean isEnabled) {
			Color old = g.getColor();
			int mid, i, j;

			j = 0;
			size = Math.max(size, 2);
//			mid = (size / 2) - 1;
			
			mid = (size / 2) + 1;
			g.translate(x, y);

			if (isEnabled) {
				if (Skins.isGraySkin()) {
					g.setColor(Color.BLACK);
				} else {
					ESL esl = new ESL(MetalLookAndFeel.getControl()); // .getControlShadow());
					int value = 50;
					if(Skins.isBronzSkin()) {
						value = 68;
					} else if(Skins.isCyanoSkin()) {
						value = 58;
					} else if(Skins.isDarkSkin()) {
						value = 98;
					}
					esl = esl.toBrighter(value);
					g.setColor(esl.toColor());
				}
			} else {
				g.setColor(getDisableColor());

				// g.setColor(MetalLookAndFeel.getControlShadow());
			}
			
			switch(direction)       {
			case NORTH:
				for (i = 0; i < size; i++) {
					g.drawLine(mid - i, i, mid + i, i);
				}
//				if (!isEnabled) {
//					g.setColor(MetalLookAndFeel.getControlHighlight());
//					g.drawLine(mid - i + 2, i, mid + i, i);
//				}
				break;
			case SOUTH:
//				if (!isEnabled) {
////					g.translate(1, 1);
////					g.setColor(MetalLookAndFeel.getControlHighlight());
////					for (i = size - 1; i >= 0; i--) {
////						g.drawLine(mid - i, j, mid + i, j);
////						j++;
////					}
////					g.translate(-1, -1);
////					g.setColor(getDisableColor());
//					
////					g.setColor(MetalLookAndFeel.getControlShadow());
//				}

				j = 0;
				for(i = size-1; i >= 0; i--)   {
					g.drawLine(mid-i, j, mid+i, j);
					j++;
				}
				break;
			case WEST:
				for(i = 0; i < size; i++)      {
					g.drawLine(i, mid-i, i, mid+i);
				}
//				if(!isEnabled)  {
//					g.setColor(MetalLookAndFeel.getControlHighlight());
//					g.drawLine(i, mid-i+2, i, mid+i);
//				}
				break;
			case EAST:
//				if(!isEnabled)  {
//					g.translate(1, 1);
//					g.setColor(MetalLookAndFeel.getControlHighlight());
//					for(i = size-1; i >= 0; i--)   {
//						g.drawLine(j, mid-i, j, mid+i);
//						j++;
//					}
//					g.translate(-1, -1);
//					g.setColor(getDisableColor());
////					g.setColor(MetalLookAndFeel.getControlShadow() );
//				}

				j = 0;
				for(i = size-1; i >= 0; i--)   {
					g.drawLine(j, mid-i, j, mid+i);
					j++;
				}
				break;
			}
			g.translate(-x, -y);	
			g.setColor(old);
		}
	}
	
	/**
	 * 
	 */
	public FlatSpinnerUI() {
		super();
	}

	/**
	 * 生成UI
	 * @param c
	 * @return
	 */
	public static ComponentUI createUI(JComponent c) {
		return new FlatSpinnerUI();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicSpinnerUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();
		
		
		// 更新边框
		Border b = spinner.getBorder();
		if (Laxkit.isClassFrom(b, CompoundBorder.class)) {
			CompoundBorder cb = (CompoundBorder) b;
			spinner.setBorder(new CompoundBorder(new SpinnerBorder(), cb .getInsideBorder()));
		}

//		// 更新
//		updateEditor(spinner.getEditor());

		// spinner.setBorder(new CompoundBorder(new SpinnerBorder(), new
		// EmptyBorder(0,0,0,0)));
		// spinner.setBorder(new SpinnerBorder());
	}
	
	private void updateEditorBorder(JComponent editor) {
		// 更新边框
		if (editor != null) {
			editor.setBorder(new EmptyBorder(0, 0, 0, 0));

			if (editor instanceof JSpinner.DefaultEditor) {
				JTextField field = ((JSpinner.DefaultEditor)editor).getTextField();
				if (field != null) {
					field.setBorder(new EmptyBorder(2, 2, 2, 2));
					field.putClientProperty("NotBorder", Boolean.TRUE); //不要显示边框
				}
			}

			//			editor.putClientProperty(arg0, arg1)

			//			if (Laxkit.isClassFrom(editor, javax.swing.JSpinner.DefaultEditor.class)) {
			//				javax.swing.JSpinner.DefaultEditor def = (javax.swing.JSpinner.DefaultEditor) editor;
			//				javax.swing.JFormattedTextField field = def.getTextField();
			//				if (field != null) {
			//					field.setBorder(new EmptyBorder(2, 2, 2, 2));
			//				}
			//			}
		}
	}
	
	protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
		super.replaceEditor(oldEditor, newEditor);
		updateEditorBorder(newEditor);
	}
	
	protected JComponent createEditor() {
		JComponent editor = super.createEditor();
		// 更新编辑器文本边框
		updateEditorBorder(editor);

		//		// 更新边框
		//		if (editor != null) {
		//			editor.setBorder(new EmptyBorder(0, 0, 0, 0));
		//
		//			System.out.printf("editor is %s\n", editor.getClass().getName());
		//
		//			if (editor instanceof JSpinner.DefaultEditor) {
		//				JTextField field = ((JSpinner.DefaultEditor)editor).getTextField();
		//				if (field != null) {
		//					field.setBorder(new EmptyBorder(2, 2, 2, 2));
		//				}
		//			}
		//
		//			//			if (Laxkit.isClassFrom(editor, javax.swing.JSpinner.DefaultEditor.class)) {
		//			//				javax.swing.JSpinner.DefaultEditor def = (javax.swing.JSpinner.DefaultEditor) editor;
		//			//				javax.swing.JFormattedTextField field = def.getTextField();
		//			//				if (field != null) {
		//			//					field.setBorder(new EmptyBorder(2, 2, 2, 2));
		//			//				}
		//			//			}
		//		}

		return editor;
	}

	protected Component createNextButton() {
		Component c = createArrowButton(SwingConstants.NORTH, true);
		c.setName("Spinner.nextButton");
		installNextButtonListeners(c);
		return c;
	}

	protected Component createPreviousButton() {
		Component c = createArrowButton(SwingConstants.SOUTH, false);
		c.setName("Spinner.previousButton");
		installPreviousButtonListeners(c);
		return c;
	}

	private Component createArrowButton(int direction, boolean north) {
//		JButton button = new BasicArrowButton(direction);
		
		JButton button = new FlatArrowButton(direction);

//		Border border = UIManager.getBorder("Spinner.arrowButtonBorder");
//
//		//		CompoundBorder cb = (CompoundBorder)border;
//		//		System.out.printf("outside %s, inside %s\n", cb.getOutsideBorder().getClass().getName(), 
//		//			cb.getInsideBorder().getClass().getName()	);
//
//		if (border instanceof UIResource) {
//			// Wrap the border to avoid having the UIResource be replaced by
//			// the ButtonUI. This is the opposite of using BorderUIResource.
//
//			if( Laxkit.isClassFrom(border, CompoundBorder.class)) {
//				CompoundBorder cb = (CompoundBorder)border;
//				button.setBorder(new CompoundBorder(new ButtonBorder(), cb.getInsideBorder()));
//
//				//				CompoundBorder cb = (CompoundBorder)border;
//				//				button.setBorder(new CompoundBorder( cb.getOutsideBorder(),  new ButtonBorder()));
//
//				//				button.setBorder(new CompoundBorder(new EmptyBorder(2,2,2,2),  new ButtonBorder()));
//
//				//				button.setBorder(new ButtonBorder());
//			}else {
//				button.setBorder(new CompoundBorder(border, null));
//
//				//				button.setBorder(new CompoundBorder(new EmptyBorder(2,2,2,2),  new ButtonBorder()));
//			}
//			//			b.setBorder(new CompoundBorder(new SpinnerBorder(), null));
//		} else {
//			button.setBorder(border);
//
//			//			button.setBorder(new CompoundBorder(new EmptyBorder(2,2,2,2),  new ButtonBorder()));
//		}
//
//		//		b.setBorder(new SpinnerBorder());
		
//		button.setBorder(new CompoundBorder(new EmptyBorder(2,2,2,2),  new ButtonBorder()));
		
		if (north) {
			button.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 0),
					new ButtonBorder()));
		} else {
			button.setBorder(new CompoundBorder(new EmptyBorder(1, 0, 0, 0),
					new ButtonBorder()));
		}
		
		button.setInheritsPopupMenu(true);
		return button;
	}

}