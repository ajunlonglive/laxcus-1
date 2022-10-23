/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.menu;

import java.io.*;

import javax.swing.*;

import com.laxcus.application.boot.*;
import com.laxcus.util.skin.*;

/**
 * 生成引导菜单
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class TokenMenuCreator {

	/**
	 * 从配置参数中读取参数，生成菜单
	 * 
	 * @param file das/eas后缀格式的文件
	 * @return 返回JMenuItem子类实例
	 * @throws IOException
	 */
	private JMenuItem create(BasketBuffer buffer, File file) throws IOException {
		// 读取引导
		byte[] b = buffer.readBootstrap();
		BootSplitter bs = new BootSplitter();
		BootItem boot = bs.split(b);

		// 设置实例
		if (file != null) {
			boot.setFile(file);
		}

		BootLocation bootLocation = boot.getIcon();

		ImageIcon icon = null;
		if (bootLocation.getURI() != null) {
			byte[] bytes = buffer.getURI(bootLocation.getURI());
			icon = ImageUtil.scale(bytes, 16, 16);
		} else if (bootLocation.getJURI() != null) {
			byte[] bytes = buffer.getJURI(bootLocation.getJURI());
			icon = ImageUtil.scale(bytes, 16, 16);
		}
		
		if (boot.hasSubObject()) {
			BootMenu menu = new BootMenu(boot.getTitle());
			menu.setIcon(icon);
			menu.setToolTipText(boot.getToolTip());
//			menu.setBootItem(boot);
			// 生成子单元
			for (BootItem sub : boot.list()) {
				addItem(menu, sub, buffer);
			}
			menu.setAttachMenu(boot.getAttachMenu());
			
			return menu;
		} else {
			BootMenuItem item = new BootMenuItem(boot.getTitle());
			item.setIcon(icon);
			item.setToolTipText(boot.getToolTip());
			// 保存属性
//			item.setBootItem(boot);
			// 关联菜单
			item.setAttachMenu(boot.getAttachMenu());
			
			return item;
		}
	}
	
	/**
	 * 从配置参数中读取参数，生成菜单
	 * 
	 * @param file das/eas后缀格式的文件
	 * @return 返回JMenuItem子类实例
	 * @throws IOException
	 */
	public JMenuItem create(File file) throws IOException {
		BasketBuffer buffer = new BasketBuffer();
		// 加载软件包
		buffer.load(file);
	
		return create(buffer, file);
	}

	/**
	 * 从从配置参数中读取参数，生成菜单
	 * @param content 字节数组
	 * @return 返回菜单
	 * @throws IOException
	 */
	public JMenuItem create(byte[] content) throws IOException {
		BasketBuffer buffer = new BasketBuffer();
		// 加载软件包
		buffer.load(content);

		return create(buffer, null);
	}
	
	
//	/**
//	 * 从配置参数中读取参数，生成菜单
//	 * 
//	 * @param file das/eas后缀格式的文件
//	 * @return 返回JMenuItem子类实例
//	 * @throws IOException
//	 */
//	public JMenuItem create(File file) throws IOException {
//		BasketBuffer buffer = new BasketBuffer();
//		// 加载软件包
//		buffer.load(file);
//		byte[] b = buffer.readBootstrap();
//		BootSplitter bs = new BootSplitter();
//		BootItem boot = bs.split(b);
//		boot.setFile(file);
//
//		BootLocation bootLocation = boot.getIcon();
//
//		ImageIcon icon = null;
//		if (bootLocation.getURI() != null) {
//			byte[] bytes = buffer.getURI(bootLocation.getURI());
//			icon = ImageUtil.scale(bytes, 16, 16);
//		} else if (bootLocation.getJURI() != null) {
//			byte[] bytes = buffer.getJURI(bootLocation.getJURI());
//			icon = ImageUtil.scale(bytes, 16, 16);
//		}
//		
//		if (boot.hasSubObject()) {
//			BootMenu menu = new BootMenu(boot.getTitle());
//			menu.setIcon(icon);
//			menu.setToolTipText(boot.getToolTip());
//			menu.setBootItem(boot);
//			// 生成子单元
//			for (BootItem sub : boot.list()) {
//				addItem(menu, sub, buffer);
//			}
//			menu.setAttachMenu(boot.getAttachMenu());
//			
//			return menu;
//		} else {
//			BootMenuItem item = new BootMenuItem(boot.getTitle());
//			item.setIcon(icon);
//			item.setToolTipText(boot.getToolTip());
//			// 保存属性
//			item.setBootItem(boot);
//			// 关联菜单
//			item.setAttachMenu(boot.getAttachMenu());
//			
//			return item;
//		}
//	}
	
	private void addItem(BootMenu parent, BootItem boot, BasketBuffer buffer) throws IOException {
		// 属性
		BootLocation bootLocation = boot.getIcon();
		ImageIcon icon = null;
		if (bootLocation.getURI() != null) {
			byte[] bytes = buffer.getURI(bootLocation.getURI());
			icon = ImageUtil.scale(bytes, 16, 16);
		} else if (bootLocation.getJURI() != null) {
			byte[] bytes = buffer.getJURI(bootLocation.getJURI());
			icon = ImageUtil.scale(bytes, 16, 16);
		}
		
		// 判断有子对象
		if (boot.hasSubObject()) {
			BootMenu menu = new BootMenu(boot.getTitle());
			if (icon != null) {
				menu.setIcon(icon);
			}
//			menu.setBootItem(boot);
			// 生成子单元
			for (BootItem sub : boot.list()) {
				addItem(menu, sub, buffer);
			}
			parent.add(menu);
		} else {
			BootMenuItem item = new BootMenuItem(boot.getTitle());
			if (icon != null) {
				item.setIcon(icon);
			}
			// 保存属性
//			item.setBootItem(boot);

			parent.add(item);
		}
	}

}