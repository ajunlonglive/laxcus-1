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
import javax.swing.table.*;
import javax.swing.plaf.metal.*;

/**
 * 默认表头标题栏渲染器。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/8/2021
 * @since laxcus 1.0
 */
public class FlatTableHeaderCellRenderer extends DefaultTableCellRenderer implements javax.swing.plaf.UIResource {

	private static final long serialVersionUID = 4884522276519326610L;
	
	public class FullBorder extends AbstractBorder implements javax.swing.plaf.UIResource {

		private static final long serialVersionUID = -250301825758010106L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(x, y, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}
	
	public class BottomRightBorder extends AbstractBorder implements javax.swing.plaf.UIResource {

		private static final long serialVersionUID = 1L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			// 绘制矩形
			g.setColor(MetalLookAndFeel.getControl());
			g.drawRect(x, y, w - 1, h - 1);
			
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			
			// 下侧线
			g.drawLine(x, h - 1, w - 1, h - 1);
			// 右侧线
			g.drawLine(w - 1, y, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}
	
	public class BottomBorder extends AbstractBorder implements javax.swing.plaf.UIResource {

		private static final long serialVersionUID = 1L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			// 绘制矩形
			g.setColor(MetalLookAndFeel.getControl());
			g.drawRect(x, y, w - 1, h - 1);
			
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			
			// 下侧线
			g.drawLine(x, h - 1, w - 1, h - 1);
//			// 右侧线
//			g.drawLine(w - 1, y, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}

	public class TopBottomRightBorder extends AbstractBorder implements javax.swing.plaf.UIResource {
	
		private static final long serialVersionUID = 1L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			// 绘制矩形
			g.setColor(MetalLookAndFeel.getControl());
			g.drawRect(x, y, w - 1, h - 1);
			
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			// 上侧线
			g.drawLine(x, y, w - 1, y);
			// 下侧线
			g.drawLine(x, h - 1, w - 1, h - 1);
			// 右侧线
			g.drawLine(w - 1, y, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}
		
		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}
	
	public class LeftBottomRightBorder extends AbstractBorder implements javax.swing.plaf.UIResource {
		
		private static final long serialVersionUID = 1L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			// 绘制矩形
			g.setColor(MetalLookAndFeel.getControl());
			g.drawRect(x, y, w - 1, h - 1);
			
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			// 左侧线
			g.drawLine(x, y, x, h - 1);
			// 下侧线
			g.drawLine(x, h - 1, w - 1, h - 1);
			// 右侧线
			g.drawLine(w - 1, y, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}
		
		public Insets getBorderInsets(Component c) {
			return new Insets(1, 1, 1, 1);
		}
	}

	/** 颜色值 **/
	private Color unselectedForeground; 
	private Color unselectedBackground; 
	
	/**
	 * 构造WATCH站点表格渲染器
	 */
	public FlatTableHeaderCellRenderer() {
		super();
		init();
	}
	
	private void loadColor() {
		unselectedBackground = UIManager.getColor("Label.background");
		unselectedForeground = UIManager.getColor("Label.foreground");

		if (unselectedBackground != null) {
			unselectedBackground = new Color(unselectedBackground.getRGB());
		}
		if (unselectedForeground != null) {
			unselectedForeground = new Color(unselectedForeground.getRGB());
		}
	}

	/**
	 * 初始颜色
	 */
	private void init() {
		loadColor();
		setIconTextGap(6);

		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.LEADING);
	}
	
    public void setForeground(Color c) {
        super.setForeground(c); 
        unselectedForeground = c; 
    }
    
    public void setBackground(Color c) {
        super.setBackground(c); 
        unselectedBackground = c; 
    }
	
	/**
	 * 判断表格有边框
	 * @param table
	 * @return
	 */
	private boolean hasTableBorder(JTable table) {
		// 默认表格没有边框
		boolean border = false;

		Container parent = table.getParent(); // should be viewport
		if (parent != null) {
			parent = parent.getParent(); // should be the scrollpane
			if (parent != null && parent instanceof JScrollPane) {
				JScrollPane jsp = (JScrollPane) parent;
				// 判断无边框
				border = !FlatUtil.isNotBorder(jsp);
			}
		}

		return border;
	}
	
	private boolean isTableout(JTable table) {
		Container parent = table.getParent(); // should be viewport
		if (parent != null) {
			parent = parent.getParent(); // should be the scrollpane
			if (parent != null && parent instanceof JScrollPane) {
				JScrollPane jsp = (JScrollPane) parent;
				// 判断无边框
				int viewWidth = jsp.getViewport().getWidth();
				return table.getWidth() >= viewWidth;
			}
		}
		return false;
	}
	
	/**
	 * 返回滚动面板
	 * @param table 表格
	 * @return 它的滚动面板
	 */
	private JScrollPane getScrollPane(JTable table) {
		Container parent = table.getParent(); // should be viewport
		if (parent != null) {
			parent = parent.getParent(); // should be the scrollpane
			if (parent != null && parent instanceof JScrollPane) {
				return (JScrollPane) parent;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// 任何一个没有定义，忽略它
		if (table == null || value == null) {
			setBorder(new EmptyBorder(0, 0, 0, 0));
			setIcon(null);
			setText("");
			return this;
		}
		
		Color fg = null;
		Color bg = null;
		
		Icon sortIcon = null;
		boolean isPaintingForPrint = false;
		boolean dragged = false;
		// 判断处于拖放状态
		JTableHeader header = table.getTableHeader();
		if (header != null) {
			TableColumn tc = header.getDraggedColumn();
			if (tc != null) {
				dragged = (tc.getModelIndex() == table.convertColumnIndexToModel(column));
			}
			isPaintingForPrint = header.isPaintingForPrint();
		}

		if (isSelected) {
			super.setForeground(fg == null ? table.getSelectionForeground() : fg);
			super.setBackground(bg == null ? table.getSelectionBackground() : bg);
		} else {
			// 前景/背景
			super.setForeground(unselectedForeground != null ? unselectedForeground : table.getForeground());
			super.setBackground(unselectedBackground != null ? unselectedBackground : table.getBackground());
		}

		int columns = table.getColumnCount();
		
		// 判断排序箭头
		if (!isPaintingForPrint && table.getRowSorter() != null) {
			SortOrder so = getColumnSortOrder(table, column);
			if (so != null) {
				switch (so) {
				case ASCENDING:
					sortIcon = ArrowIcon.createUp(8, 8);
					break;
				case DESCENDING:
					sortIcon = ArrowIcon.createDown(8, 8);
					break;
				case UNSORTED:
					break;
				}
			}
		}

		// 光标
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		// 字体
		setFont(table.getFont());

		// 有边框时
		if (hasTableBorder(table)) {
			if (dragged) {
				// 拖放状态，边框线：左，底，右
				setBorder(new LeftBottomRightBorder());
			} else if (column + 1 == columns) {
				// 如果JTable的尺寸超过JScrollPane，只绘制底部，否则绘制底部和右侧
				if (isTableout(table)) {
					boolean hasBar = false;
					JScrollPane jsp = getScrollPane(table);
					if (jsp != null) {
						JScrollBar bar = jsp.getVerticalScrollBar();
						// 垂直滚动条显示，绘制底和右侧线
						hasBar = (bar != null && bar.isVisible()); 
					}
					// 有垂直滚动条时，绘制底部和右侧边线，否则是绘制底部线条
					setBorder(hasBar ? new BottomRightBorder() : new BottomBorder());
				} else {
					setBorder(new BottomRightBorder());
				}
			} else {
				// 绘制：下，右，忽略上、左
				setBorder(new BottomRightBorder());
			}
		}
		// 无边框时
		else {
			if (dragged) {
				setBorder(new FullBorder());
			}
			// 绘制最左侧边框
			else if (column == 0) {
				// 一个完整的矩形边框
				setBorder(new FullBorder());
			}
			// 绘制最右侧边框
			else if (column + 1 == columns) {
				// 绘制：上、下，右，忽略左侧
				setBorder(new TopBottomRightBorder());
			}
			// 绘制其它情况的边框
			else {
				// 绘制：上，下，右，忽略左侧
				setBorder(new TopBottomRightBorder());
			}
		}
		
		// 文本和图标
		setIcon(sortIcon);
		setText(value != null ? value.toString() : "");

		// 返回实例
		return this;
	}
	
	/**
	 * 返回当前的排序
	 * @param table
	 * @param column
	 * @return
	 */
	private SortOrder getColumnSortOrder(JTable table, int column) {
		SortOrder rv = null;
		if (table == null || table.getRowSorter() == null) {
			return null;
		}
		java.util.List<? extends RowSorter.SortKey> sortKeys = table
				.getRowSorter().getSortKeys();
		if (sortKeys.size() > 0
				&& sortKeys.get(0).getColumn() == table.convertColumnIndexToModel(column)) {
			rv = sortKeys.get(0).getSortOrder();
		}
		return rv;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		loadColor();
	}

//	public static class UIResource extends FlatTableHeaderCellRenderer implements javax.swing.plaf.UIResource {
//
//		private static final long serialVersionUID = 1L;
//		
//	}

}


//abstract class ArrowIcon implements Icon, javax.swing.plaf.UIResource, Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	@Override
//	public int getIconHeight() {
//		return 6;
//	}
//
//	@Override
//	public int getIconWidth() {
//		return 6;
//	}
//	
//	protected Color createColor(Component parent) {
//		// 默认是暗灰色
//		Color color = Color.DARK_GRAY;
//		// 高亮色
//		if (!Skins.isGraySkin()) {
//			color = UIManager.getColor("Panel.background");
//			if (color == null) {
//				color = parent.getBackground();
//			}
//			ESL esl = new ESL(color);
//			esl.brighter(50);
//			color = esl.toColor();
//		}
//		return color;
//	}
//}
//
//class UpArrowIcon extends ArrowIcon  {
//
//	private static final long serialVersionUID = 1L;
//
//	/* (non-Javadoc)
//	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
//	 */
//	@Override
//	public void paintIcon(Component c, Graphics g, int w, int h) {
//		Color old = g.getColor();
//		g.setColor(createColor(c));
//		
//		int arrowHeight = getIconHeight();
//		if (arrowHeight > h) {
//			arrowHeight = h;
//		}
//		int arrowWidth = getIconWidth();
//		if (arrowWidth > w) {
//			arrowWidth = w;
//		}
//		
//		int startY = (h - arrowHeight) / 2 - 1;
//		if (startY < 0) startY = 0;
//		
//		int startX = (w - arrowWidth) / 2 - 1;
//		if (startX < 0) startX = 0;
//		
//		// up arrow, w:30 h:11, startX:11, startY:1
//		
////		int startY = (((h + 1) - arrowHeight) % 2 == 0 ? ((h + 1) - arrowHeight) / 2 : (((h + 1) - arrowHeight) / 2 -1));
////		if(startY < 0) startY = 0;
////		int startX = ((w % 2 == 0) ? (w / 2) : (w / 2) - 1);
////		if(startX < 0) startX = 0;
//		
//		// System.out.println( "startX :" + startX + " startY :"+startY);
//		for (int line = 0; line < arrowHeight; line++) {
//			g.drawLine(startX - line, startY + line, startX + line + 1, startY + line);
//		}
//		
//		System.out.printf("up arrow, w:%d h:%d, startX:%d, startY:%d\n", w,
//				h, startX, startY);
//		
//
//		g.setColor(old);
//	}
//}
//
//class DownArrowIcon extends ArrowIcon {
//
//	private static final long serialVersionUID = 1L;
//
//	/* (non-Javadoc)
//	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
//	 */
//	@Override
//	public void paintIcon(Component c, Graphics g, int w, int h) {
//		Color old = g.getColor();
//		g.setColor(createColor(c));
//		
////		int arrowHeight = getIconHeight();
////		if (arrowHeight > h) {
////			arrowHeight = h;
////		}
////		
////		int startY = (((h + 1) - arrowHeight) / 2) + arrowHeight - 1;
////		int startX = ((w % 2 == 0) ? (w / 2) : (w / 2) - 1);
////		if (startX < 0) startX = 0;
//		
//		int arrowHeight = getIconHeight();
//		if (arrowHeight > h) {
//			arrowHeight = h;
//		}
//		int arrowWidth = getIconWidth();
//		if (arrowWidth > w) {
//			arrowWidth = w;
//		}
//		
//		int startY = (h - arrowHeight) / 2 - 1;
//		if (startY < 0) startY = 0;
//		int startX = (w - arrowWidth) / 2 - 1;
//		if (startX < 0) startX = 0;
//
//		for (int line = 0; line < arrowHeight; line++) {
//			g.drawLine(startX - line, startY - line, startX + line + 1, startY - line);
//		}
//		g.setColor(old);
//	}
//}


//private boolean horizontalTextPositionSet = false;


///**
// * 加载颜色!
// */
//private void loadColor() {
//	textSelectForeground = Skins.findTableTextSelectForeground();
//	textSelectBackground = Skins.findTableTextSelectBackground();
//	textForeground = Skins.findTableTextForeground();
//	textBackground = Skins.findTableTextBackground();
//}


//public void setHorizontalTextPosition(int textPosition) {
//	horizontalTextPositionSet = true;
//	setHorizontalTextPosition(textPosition);
//}


//// System.out.println( "startX :" + startX + " startY :"+startY);
//for (int line = 0; line < arrowHeight; line++) {
//g.drawLine(startX - line, startY + line, startX + line + 1, startY + line);
//}

//private Color createColor() {
//// 默认是暗灰色
//Color color = Color.GRAY;
//// 高亮色
//if (!Skins.isGraySkin()) {
//color = UIManager.getColor("Panel.background");
//if (color != null) {
//ESL esl = new ESL(color);
//esl.brighter(50);
//color = esl.toColor();
//} else {
//color = new Color(223, 223, 223);
//}
//}
//return color;
//}
//
//private ImageIcon createUpIcon(int w, int h) {
//// 生成一个透明的新图像
//BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//Graphics2D g2d = buff.createGraphics();
//buff = g2d.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
//Graphics g = buff.getGraphics();
//
//int midWidth = w / 2;
//if (w % 2 == 0) midWidth -= 1;
//int midHeight = h / 2;
//if (h % 2 == 0) midHeight -= 1;
//
//// 设置颜色
//g.setColor(createColor());
//
//int startX = midWidth; // 从中间开始
//int startY = midHeight - midWidth / 2;
//if (startY < 0) startY = 0;
//
//int extend = 0;
//for (int y = startY; y < h; y++) {
//if (extend > midWidth) {
//break;
//}
//g.drawLine(startX - extend, y, startX + extend, y);
//extend++;
//}
//
//g.dispose();
//
//// 返回图标
//return new ImageIcon(buff);
//}
//
//private ImageIcon createDownIcon(int w, int h) {
//// 生成一个透明的新图像
//BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//Graphics2D g2d = buff.createGraphics();
//buff = g2d.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
//Graphics g = buff.getGraphics();
//
//int midWidth = w / 2;
//if (w % 2 == 0) midWidth -= 1;
//int midHeight = h / 2;
//if (h % 2 == 0) midHeight -= 1;
//
//// 设置颜色
//g.setColor(createColor());
//
//int startX = midWidth; // 从中间开始
//int startY = midHeight - midWidth / 2;
//if (startY < 0) startY = 0;
//
//int extend = midWidth;
//for (int y = startY; y < h; y++) {
//if (extend < 0) {
//break;
//}
//g.drawLine(startX - extend, y, startX + extend, y);
//extend--;
//}
//
//g.dispose();
//
//// 返回图标
//return new ImageIcon(buff);
//}

//private Icon findIcon(boolean up) {
//if (Skins.isGraySkin()) {
//if (up) {
//return UIManager.getIcon("TableHeaderCellUpIcon");
//} else {
//return UIManager.getIcon("TableHeaderCellDownIcon");
//}
//} else {
//if (up) {
//return UIManager.getIcon("TableHeaderCellUpIcon");
//} else {
//return UIManager.getIcon("TableHeaderCellDownIcon");
//}
//}
//}


//// 拖放操作时...
//JTable.DropLocation dropLocation = table.getDropLocation();
//if (dropLocation != null
//		&& !dropLocation.isInsertRow()
//		&& !dropLocation.isInsertColumn()
//		&& dropLocation.getRow() == row
//		&& dropLocation.getColumn() == column) {
//
////	fg = DefaultLookup.getColor(this, ui, "Table.dropCellForeground");
////	bg = DefaultLookup.getColor(this, ui, "Table.dropCellBackground");
//
//	bg = UIManager.getColor("Table.dropCellBackground");
//	
//	isSelected = true;
//}

