/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.skin.*;

/**
 * 修改软件图标窗口
 * 
 * @author scott.liang
 * @version 1.0 4/1/2013
 * @since laxcus 1.0
 */
public class TerminalExchangeIconDialog extends TerminalCommonFontDialog implements ActionListener {

	private static final long serialVersionUID = 8510834707725274446L;

	/** 属性，保存到UIManager的KEY值 **/
	private final static String OPEN_KEY = TerminalExchangeIconDialog.class.getSimpleName() + "_OPEN";

	private JLabel image = new JLabel();

	private JButton cmdSelect = new JButton();

	private JLabel path = new JLabel();

	private JButton cmdCancel = new JButton();

	private JButton cmdOK = new JButton();

	/** 选中的磁盘文件 **/
	private File selectFile;

	/** 新的软件名称 **/
	private byte[] stream;

	/**
	 * 构造修改软件图标窗口，指定上级窗口和模式
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public TerminalExchangeIconDialog(Frame parent, boolean modal) throws HeadlessException {
		super(parent, modal);
	}

	public void setStream(byte[] b) {
		stream = b;
	}

	/**
	 * 返回新的图标数据流
	 * @return 字节数组
	 */
	public byte[] getStream() {
		return stream;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		addThread(new ClickThread(e));
	}

	class ClickThread extends SwingEvent {
		ActionEvent event;

		ClickThread(ActionEvent e) {
			super();
			event = e;
		}

		public void process() {
			if (event.getSource() == cmdCancel) {
				stream = null;
				dispose();
			} else if(event.getSource() == cmdOK) {
				boolean success = saveStream();
				if(success) {
					dispose();
				}
			} else if(event.getSource() == cmdSelect) {
				chooseFile(); // 选择文件
			}
		}
	}

	/**
	 * 保存名称
	 * @return 成功返回真，否则假
	 */
	private boolean saveStream() {
		if (selectFile == null) {
			return false;
		}
		// 转换成压缩的字节流
		try {
			byte[] b = convert(selectFile, 32, 32, "png");
			if (b != null) {
				stream = b;
				return true;
			}
		} catch (IOException e) {

		}

		stream = null;

		return false;
	}

	/**
	 * 压缩转换图标
	 * @param source
	 * @param compressWidth
	 * @param compressHeight
	 * @param type
	 * @return
	 * @throws IOException
	 */
	private byte[] convert(File source, int compressWidth, int compressHeight, String type)  throws IOException {
		FileInputStream in = new FileInputStream(source);
		BufferedImage img = ImageIO.read(in);

		// 平滑缩小图象
		Image compressImage = img.getScaledInstance(compressWidth, compressHeight, BufferedImage.SCALE_SMOOTH);
		// 生成一个新图像
		BufferedImage buff = new BufferedImage(compressWidth, compressHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D gra = buff.createGraphics();
		buff = gra.getDeviceConfiguration().createCompatibleImage(compressWidth, compressHeight, Transparency.TRANSLUCENT);
		buff.getGraphics().drawImage(compressImage, 0, 0, null);

		// 输出
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(buff, type, out);
		out.flush();

		// 读出字节
		byte[] b = out.toByteArray();

		// 关闭！
		out.close();
		in.close();

		return b;
	}
	
	/** 选择的文件类型选项 **/
	private static String selectType;

	/**
	 * 选择图像文件
	 */
	private void chooseFile() {
		String title = findCaption("Dialog/ExchangeIcon/open-chooser/title/title");
		String buttonText = findCaption("Dialog/ExchangeIcon/open-chooser/choose/title");

		// JPEG文件
		String ds_jpeg = findCaption("Dialog/ExchangeIcon/open-chooser/jpeg/description/title");
		String jpeg = findCaption("Dialog/ExchangeIcon/open-chooser/jpeg/extension/title");
		// GIF文件
		String ds_gif = findCaption("Dialog/ExchangeIcon/open-chooser/gif/description/title");
		String gif = findCaption("Dialog/ExchangeIcon/open-chooser/gif/extension/title");
		// PNG文件
		String ds_png = findCaption("Dialog/ExchangeIcon/open-chooser/png/description/title");
		String png = findCaption("Dialog/ExchangeIcon/open-chooser/png/extension/title");

		DiskFileFilter f1 = new DiskFileFilter(ds_jpeg, jpeg);
		DiskFileFilter f2 = new DiskFileFilter(ds_gif, gif);
		DiskFileFilter f3 = new DiskFileFilter(ds_png, png);

		// 显示窗口
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(f3);
		chooser.addChoosableFileFilter(f2);
		chooser.addChoosableFileFilter(f1);

		// 找到选项
		chooseFileFilter(chooser, selectType);

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setApproveButtonText(buttonText);
		chooser.setApproveButtonToolTipText(buttonText);

		// 文件！
		if (selectFile != null) {
			chooser.setCurrentDirectory(selectFile.getParentFile());
		}
		// 没有定义，从系统中取
		else {
			Object memory = UITools.getProperity(OPEN_KEY);
			if (memory != null && memory.getClass() == String.class) {
				File file = new File((String) memory);
				if (file.exists() && file.isFile()) {
					chooser.setCurrentDirectory(file.getParentFile());
				}
			}
		}

		int val = chooser.showOpenDialog(this);
		// 显示窗口
		if (val != JFileChooser.APPROVE_OPTION) {
			return;
		}

		selectType = saveFileFileter(chooser);

		File file = chooser.getSelectedFile();
		boolean success = (file.exists() && file.isFile());
		if (success) {
			addThread(new ReadThread(file));
		}
	}

	class ReadThread extends SwingEvent {
		File file;

		ReadThread(File e) {
			super();
			file = e;
		}

		public void process() {
			readImage(file);
		}
	}

	/**
	 * 输入新的图像文件
	 * @param file
	 */
	private void readImage(File file) {
		// 转换图标
		byte[] b = null;
		try {
			b = convert(file, 32, 32, "png");
		} catch (IOException e) {

		}
		if (b == null) {
			return;
		}

		// 保存文件名
		selectFile = file;
		String filename = file.toString();
		UITools.putProperity(OPEN_KEY, filename);

		// 显示路径
		path.setText(filename);
		path.setToolTipText(filename);

		// 显示图标
		ImageIcon icon = createIcon(b);
		image.setIcon(icon);
	}

	/**
	 * 生成图标
	 * @param b
	 * @return
	 */
	private ImageIcon createIcon(byte[] b) {
		ImageIcon icon = new ImageIcon(b);
		Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}

	/**
	 * 控件
	 */
	private void initControls() {
		image.setHorizontalAlignment(SwingConstants.CENTER);
		image.setVerticalAlignment(SwingConstants.CENTER);
		Dimension d = new Dimension(68, 68);
		// 图标窗口最小位置和边框
		image.setMinimumSize(d);
		image.setPreferredSize(d);
		image.setMaximumSize(d);

		image.setBorder(UITools.createTitledBorder()); // 空边框
		if (stream != null) {
			ImageIcon icon = createIcon(stream);
			image.setIcon(icon);
		}

		String text = findCaption("Dialog/ExchangeIcon/buttons/select/title");
		cmdSelect.setText(text);
		cmdSelect.setMnemonic('S');
		cmdSelect.addActionListener(this);

		text = findCaption("Dialog/ExchangeIcon/buttons/okay/title");
		cmdOK.setText(text);
		cmdOK.setMnemonic('O');
		cmdOK.addActionListener(this);

		text = findCaption("Dialog/ExchangeIcon/buttons/cancel/title");
		cmdCancel.setText(text);
		cmdCancel.setMnemonic('C');
		cmdCancel.addActionListener(this);
	}

	/**
	 * 构造布局
	 * @return
	 */
	private JPanel initPane() {
		initControls();

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout(4, 0));
		north.add(cmdSelect, BorderLayout.WEST);
		north.add(path, BorderLayout.CENTER);

		JPanel sub = new JPanel();
		sub.setLayout(new BorderLayout(0, 0));
		sub.add(north, BorderLayout.NORTH);
		sub.add(new JPanel(), BorderLayout.CENTER);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(4, 0));
		center.setBorder(new EmptyBorder(6, 8, 0, 8));
		center.add(sub, BorderLayout.CENTER);
		center.add(image, BorderLayout.EAST);

		JPanel but = new JPanel();
		but.setLayout(new GridLayout(1, 2, 5, 0));
		but.add(cmdOK);
		but.add(cmdCancel);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout(0, 5));
		bottom.add(new JSeparator(), BorderLayout.NORTH);
		bottom.add(new JPanel(), BorderLayout.CENTER);
		bottom.add(but, BorderLayout.EAST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 2));
		setRootBorder(panel);
		panel.add(center, BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * 窗口尺寸！
	 * 
	 * @return
	 */
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 438;
		int height = 146;
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * 显示窗口
	 */
	public void showDialog() {
		// 面板
		setContentPane(initPane());

		// 窗口位置
		Rectangle rect =getBound();
		setBounds(rect);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setMinimumSize(new Dimension(300, 180));
		setAlwaysOnTop(true);

		// 标题
		String title = findCaption("Dialog/ExchangeIcon/title");
		setTitle(title);

		// 显示！
		setVisible(true);
	}

}