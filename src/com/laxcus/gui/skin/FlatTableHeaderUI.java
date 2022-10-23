/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * 平面表头UI
 * 
 * 目前暂时用超类来实现
 * 
 * @author scott.liang
 * @version 1.0 6/21/2022
 * @since laxcus 1.0
 */
public class FlatTableHeaderUI extends BasicTableHeaderUI {

	/**
	 * 构造平面表头UI
	 */
	public FlatTableHeaderUI() {
		super();
	}

	/**
	 * 返回实例
	 * @param h
	 * @return
	 */
	public static ComponentUI createUI(JComponent h) {
		return new FlatTableHeaderUI();
	}

	public void installUI(JComponent c) {
		super.installUI(c);

		// 修改默认的...
		header.setDefaultRenderer(new FlatTableHeaderCellRenderer());
	}

	//	public void installUI(JComponent c) {
	//		header = (JTableHeader) c;
	//
	//		// rendererPane = new CellRendererPane();
	//
	//		rendererPane = new FlatCellRendererPane();
	//		header.add(rendererPane);
	//		
	//		// 默认的...
	//		header.setDefaultRenderer(new MessageHeaderCellRenderer());
	//
	//		installDefaults();
	//		installListeners();
	//		installKeyboardActions();
	//	}

	//	   public void paint(Graphics g, JComponent c) {
	//			if (header.getColumnModel().getColumnCount() <= 0) { 
	//			    return; 
	//			}
	//		        boolean ltr = header.getComponentOrientation().isLeftToRight();
	//
	//			Rectangle clip = g.getClipBounds(); 
	//		        Point left = clip.getLocation();
	//		        Point right = new Point( clip.x + clip.width - 1, clip.y );
	//			TableColumnModel cm = header.getColumnModel(); 
	//		        int cMin = header.columnAtPoint( ltr ? left : right );
	//		        int cMax = header.columnAtPoint( ltr ? right : left );
	//		        // This should never happen. 
	//		        if (cMin == -1) {
	//			    cMin =  0;
	//		        }
	//		        // If the table does not have enough columns to fill the view we'll get -1.
	//		        // Replace this with the index of the last column.
	//		        if (cMax == -1) {
	//			    cMax = cm.getColumnCount()-1;  
	//		        }
	//
	//			TableColumn draggedColumn = header.getDraggedColumn(); 
	//			int columnWidth;
	//		        Rectangle cellRect = header.getHeaderRect(ltr ? cMin : cMax); 
	//			TableColumn aColumn;
	//			if (ltr) {
	//			    for(int column = cMin; column <= cMax ; column++) { 
	//				aColumn = cm.getColumn(column); 
	//				columnWidth = aColumn.getWidth();
	//				cellRect.width = columnWidth;
	//				if (aColumn != draggedColumn) {
	//				    paintCell(g, cellRect, column);
	//				} 
	//				cellRect.x += columnWidth;
	//			    }
	//			} else {
	//			    for(int column = cMax; column >= cMin; column--) {
	//				aColumn = cm.getColumn(column);
	//				columnWidth = aColumn.getWidth();
	//				cellRect.width = columnWidth;
	//				if (aColumn != draggedColumn) {
	//				    paintCell(g, cellRect, column);
	//				}
	//		                cellRect.x += columnWidth;
	//			    }
	//			} 
	//
	//		        // Paint the dragged column if we are dragging. 
	//		        if (draggedColumn != null) { 
	//		            int draggedColumnIndex = viewIndexForColumn(draggedColumn); 
	//			    Rectangle draggedCellRect = header.getHeaderRect(draggedColumnIndex); 
	//		            
	//		            // Draw a gray well in place of the moving column. 
	//		            g.setColor(header.getParent().getBackground());
	//		            g.fillRect(draggedCellRect.x, draggedCellRect.y,
	//		                               draggedCellRect.width, draggedCellRect.height);
	//
	//		            draggedCellRect.x += header.getDraggedDistance();
	//
	//			    // Fill the background. 
	//			    g.setColor(header.getBackground());
	//			    g.fillRect(draggedCellRect.x, draggedCellRect.y,
	//				       draggedCellRect.width, draggedCellRect.height);
	//		 
	//		            paintCell(g, draggedCellRect, draggedColumnIndex);
	//		        }
	//
	//			// Remove all components in the rendererPane. 
	//			rendererPane.removeAll(); 
	//		    }

}