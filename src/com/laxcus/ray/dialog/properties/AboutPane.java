/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.properties;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.ray.panel.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;

/**
 * 关于面板
 * 
 * @author scott.liang
 * @version 1.0 7/31/2021
 * @since laxcus 1.0
 */
class AboutPane extends RayPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	public AboutPane(){
		super();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

	}
	
	private String getOS() {
		if (isLinux()) {
			String name = System.getProperty("os.name");
			String version = LinuxEffector.getInstance().getVersion();
			if (version != null) {
				name = String.format("%s/%s", name, version);
			}
			return name;
		} else {
			return System.getProperty("os.name") + "/"
					+ System.getProperty("os.version");
		}
	}

	private String getCPU() {
		if (isLinux()) {
			return LinuxEffector.getInstance().getCPUName();
		} else {
			return System.getProperty("os.arch");
		}
	}
	
	private String getDisk() {
		long max = 0, used = 0;
		if (isLinux()) {
			max = LinuxDevice.getInstance().getSysMaxDisk();
			used = LinuxDevice.getInstance().getSysUsedDisk();
		} else if (isWindows()) {
			max = WindowsDevice.getInstance().getSysMaxDisk();
			used = WindowsDevice.getInstance().getSysUsedDisk();
		}
		
		String s1 = String.format("%s %s <br>", UIManager.getString("PropertiesDialog.allCapacityText"),
				ConfigParser.splitCapacity(max));
		String s2 = String.format("%s %s <br>", UIManager.getString("PropertiesDialog.usedCapacityText"),
				ConfigParser.splitCapacity(used));
		String s3 = String.format("%s %s ", UIManager.getString("PropertiesDialog.leftCapacityText"),
				ConfigParser.splitCapacity(max - used));
		return s1 + s2+s3;
	}
	
	private String getMemory() {
		long max = 0, used = 0;
		if (isLinux()) {
			// 系统内存
			max = LinuxDevice.getInstance().getSysMaxMemory();
			used = LinuxDevice.getInstance().getSysUsedMemory();
		} else if (isWindows()) {
			max = WindowsDevice.getInstance().getSysMaxMemory();
			used = WindowsDevice.getInstance().getSysUsedMemory();
		}
		String s1 = String.format("%s %s <br>", UIManager.getString("PropertiesDialog.allCapacityText"),
				ConfigParser.splitCapacity(max));
		String s2 = String.format("%s %s <br>", UIManager.getString("PropertiesDialog.usedCapacityText"),
				ConfigParser.splitCapacity(used));
		String s3 = String.format("%s %s ", UIManager.getString("PropertiesDialog.leftCapacityText"),
				ConfigParser.splitCapacity(max - used));
		return s1 + s2+s3;
	}
	
	private String getVMMemory() {
		Runtime rt = Runtime.getRuntime();
		// JVM可分配最大内存
		long maxMemory = rt.maxMemory();
		// totalMemory：JVM已分配内存，freeMemory：是totalMemory中未使用部分
		// 实际使用的内存 = JVM已分配内存 - JVM已分配内存中未使用部分
		long usedMemory = (rt.totalMemory() - rt.freeMemory());

		String s1 = String.format("%s %s <br>",
				UIManager.getString("PropertiesDialog.allCapacityText"),
				ConfigParser.splitCapacity(maxMemory));
		String s2 = String.format("%s %s <br>",
				UIManager.getString("PropertiesDialog.usedCapacityText"),
				ConfigParser.splitCapacity(usedMemory));
		String s3 = String.format("%s %s ", UIManager.getString("PropertiesDialog.leftCapacityText"),
				ConfigParser.splitCapacity(maxMemory - usedMemory));
		
		return s1 + s2 + s3;
	}

	public void init() {
		Icon logo = UIManager.getIcon("PropertiesDialog.laxcusImageLogo");
		
		JLabel left = new JLabel(logo);
		left.setHorizontalAlignment(SwingConstants.LEFT);
		left.setVerticalAlignment(SwingConstants.TOP);
		
		String os = String.format("<b>%s</b> <br> %s <br><br>",
				UIManager.getString("PropertiesDialog.aboutOSTitle"), getOS());
		String cpu = String.format("<b>%s</b> <br> %s <br><br>",
				UIManager.getString("PropertiesDialog.aboutCPUTitle"), getCPU());
		String memory = String.format("<b>%s</b> <br> %s <br><br>", UIManager
				.getString("PropertiesDialog.aboutPhysicalMemoryTitle"), getMemory());
		String disk = String.format("<b>%s</b> <br> %s <br><br>",
				UIManager.getString("PropertiesDialog.aboutDiskTitle"),
				getDisk());
		String vmemory = String.format("<b>%s</b> <br> %s <br><br>",
				UIManager.getString("PropertiesDialog.aboutVMeoryTitle"),
				getVMMemory());
		String version = String.format("<b>%s</b> <br> %s <br><br>",
				UIManager.getString("PropertiesDialog.aboutVersionTitle"),
				UIManager.getString("PropertiesDialog.aboutVersionText")); 
		String producer = String.format("<b>%s</b> <br> %s <br><br>",
				UIManager.getString("PropertiesDialog.aboutProducerTitle"),
				UIManager.getString("PropertiesDialog.aboutProducerText")); 
		
		String content = String.format("<html><body> %s </body></html>", os + cpu + memory
				+ disk + vmemory + version + producer);

		JLabel right = new JLabel(content);
		right.setVerticalAlignment(SwingConstants.TOP);
		right.setHorizontalAlignment(SwingConstants.LEFT);
		right.setBorder(new EmptyBorder(4, 0, 0, 0));
		
		// 位置
		setLayout(new BorderLayout(10, 0));
		setBorder(new EmptyBorder(10, 4, 4, 4));
		add(left, BorderLayout.WEST);
		add(right, BorderLayout.CENTER);
	}
	
	@Override
	public void updateUI() {
		super.updateUI();

		FontKit.updateDefaultFonts(this, false);
	}

}