/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;

import com.laxcus.application.factory.*;
import com.laxcus.application.manage.*;
import com.laxcus.container.*;
import com.laxcus.front.desktop.dialog.start.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.register.*;
import com.laxcus.util.*;
import com.laxcus.util.event.*;
import com.laxcus.util.sound.*;

/**
 * 桌面应用启动器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2022
 * @since laxcus 1.0
 */
final class DesktopStarter {

	/** 桌面实例 **/
	private PlatformDesktop desktop;

	/** 应用后缀 -> 启动键 **/
	private TreeMap<String, StartKey> startKeys;

	/**
	 * 
	 */
	public DesktopStarter(PlatformDesktop e, TreeMap<String, StartKey> c) {
		super();
		desktop = e;
		startKeys = c;
	}

	/**
	 * 找到注册表中的WKey定义，关联的写入在DesktopStartDialog
	 * @param type 类型
	 * @return 返回注册的WKey实例，没有是空指针
	 */
	private WKey findWKey(String type) {
		String path = String.format("StartTypes/%s", type);
		String value = RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, path);
		// 有效，解析它
		if (WKey.validate(value)) {
			return WKey.translate(value);
		}
		return null;
	}

	/**
	 * 取出链接
	 * @param o
	 * @return
	 */
	private String[] getLinks(Object o) {
		String[] links = null;
		if (o.getClass() == SRL[].class) {
			SRL[] srls = (SRL[]) o;
			links = new String[srls.length];
			for (int i = 0; i < srls.length; i++) {
				links[i] = srls[i].toString();
			}
		} else if (o.getClass() == File[].class) {
			File[] files = (File[]) o;
			links = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				links[i] = Laxkit.canonical(files[i]);
			}
		} else if (o.getClass() == SRL.class) {
			SRL srl = (SRL) o;
			links = new String[1];
			links[0] = srl.toString();
		} else if (o.getClass() == File.class) {
			File file = (File) o;
			links = new String[1];
			links[0] = Laxkit.canonical(file);
		}
		return links;
	}

	private WKey doWKey(String link) {
		// 取出关联类型
		int last = link.lastIndexOf(".");
		if (last == -1) {
			// 出错
			String title = UIManager.getString("DesktopWindow.StartFileUnknowTitle");
			String content = UIManager.getString("DesktopWindow.StartFileUnknowContent");
			content = String.format(content, link);
			MessageBox.showWarning(desktop, title, content); //"出错", "这不是一个合适的启动类型");
			return null;
		}
		String suffix = link.substring(last + 1);
		suffix = suffix.toLowerCase().trim();

		// 找到WKEY
		WKey key = findWKey(suffix);

		// 找到匹配的
		if (key == null) {
			StartKey startKey = startKeys.get(suffix);
			if (startKey == null) {
				// 合并全部，输出!
				startKey = new StartKey(suffix);
				for (StartKey st : startKeys.values()) {
					startKey.addAll(st.list()); // 显示全部匹配的
				}
				DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
				StartToken token = dlg.showDialog(desktop);
				if (token != null) {
					key = token.getKey();
				}
			} else {
				if (startKey.size() > 1) {
					DesktopStartDialog dlg = new DesktopStartDialog(startKey, link);
					StartToken token = dlg.showDialog(desktop);
					if (token != null) {
						key = token.getKey();
					}
				} else {
					key = startKey.get(0).getKey();
				}
			}
		}
		// 解析没有定义，忽略退出
		return key;
	}

	/**
	 * 根据文件名启动对应的应用软件，打开它
	 * @param o
	 */
	public void run(Object o) {
		// 取出运行链
		String[] links = getLinks(o);
		if (links == null) {
			return;
		}

		TreeMap<WKey, Starter> starts = new TreeMap<WKey, Starter>();

		for (int i = 0; i < links.length; i++) {
			String link = links[i];
			WKey key = doWKey(link);
			if (key == null) {
				continue;
			}
			Starter element = starts.get(key);
			if (element == null) {
				element = new Starter(key);
				starts.put(element.getKey(), element);
			}
			element.add(link);
		}

		Iterator<Map.Entry<WKey, Starter>> iterator  = starts.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<WKey, Starter> entry = iterator.next();
			Starter starter = entry.getValue();
			start(starter);
		}
	}


	//	class RunApplicationThread extends SwingWorker<Integer, Object> {
	//		WKey key;
	//		String[] links;
	//
	//		RunApplicationThread(WKey k, String[] args) {
	//			super();
	//			key = k;
	//			links = args;
	//		}
	//
	//		/* (non-Javadoc)
	//		 * @see javax.swing.SwingWorker#doInBackground()
	//		 */
	//		@Override
	//		protected Integer doInBackground() throws Exception {
	//			int ret = -1;
	//			try {
	//				if (links != null) {
	//					ret = ApplicationStarter.start(key, links);
	//				} else {
	//					ret = ApplicationStarter.start(key);
	//				}
	//			} catch (SecurityException e) {
	//				Logger.error(e);
	//			} catch (IllegalArgumentException e) {
	//				Logger.error(e);
	//			} catch (IOException e) {
	//				Logger.error(e);
	//			} catch (InstantiationException e) {
	//				Logger.error(e);
	//			} catch (IllegalAccessException e) {
	//				Logger.error(e);
	//			} catch (NoSuchMethodException e) {
	//				Logger.error(e);
	//			} catch (InvocationTargetException e) {
	//				Logger.error(e);
	//			} catch(Throwable e) {
	//				Logger.fatal(e);
	//			}
	//			return new Integer(ret);
	//		}
	//		
	//		protected void done() {
	//			Integer ret = null;
	//			try {
	//				ret = get();
	//			} catch (Exception e) {
	//				Logger.error(e);
	//			}
	//			
	//			boolean failed = (ret == null || ret.intValue() != 0);
	//			if (!failed) {
	//				String title = UIManager.getString("ApplicationStart.startFaultTitle");
	//				String content = UIManager.getString("ApplicationStart.startFaultContent");
	//				
	//				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
	//				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
	//			}
	//		}
	//	}

	class RunApplicationThread extends SwingEvent {
		WKey key;
		String[] links;

		public RunApplicationThread(WKey e, String[] args) {
			super();
			key = e;
			links = args;
		}

		@Override
		public void process() {
			int ret = -1;
			try {
				if (links != null) {
					ret = ApplicationStarter.start(key, links);
				} else {
					ret = ApplicationStarter.start(key);
				}
			} catch (SecurityException e) {
				Logger.error(e);
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			} catch (IOException e) {
				Logger.error(e);
			} catch (InstantiationException e) {
				Logger.error(e);
			} catch (IllegalAccessException e) {
				Logger.error(e);
			} catch (NoSuchMethodException e) {
				Logger.error(e);
			} catch (InvocationTargetException e) {
				Logger.error(e);
			} catch(Throwable e) {
				Logger.fatal(e);
			}
			
			// 弹出错误
			if (ret != 0) {
				String title = UIManager.getString("ApplicationStart.startFaultTitle");
				String content = UIManager.getString("ApplicationStart.startFaultContent");
				PlatformKit.getPlatformDesktop().playSound(SoundTag.ERROR);
				MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
			}
		}
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	private void showFault(String title, String content) {
		desktop.playSound(SoundTag.ERROR);
		MessageBox.showFault(desktop, title, content);
	}

	/**
	 * 启动
	 * @param starter
	 */
	private void start(Starter starter) {
		WKey key = starter.getKey();

		// 判断存在
		if (!RTManager.getInstance().hasProgram(key)) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			showFault(title, content);
			return;
		}

		String[] links = starter.toArray();

		// 启动线程
		addThread(new RunApplicationThread(key, links));

		//		RunApplicationThread rt = new RunApplicationThread(key, links);
		//		rt.execute();

		//		int ret = -1;
		//		try {
		//			ret = ApplicationStarter.start(key, links);
		//		} catch (SecurityException e) {
		//			Logger.error(e);
		//		} catch (IllegalArgumentException e) {
		//			Logger.error(e);
		//		} catch (IOException e) {
		//			Logger.error(e);
		//		} catch (InstantiationException e) {
		//			Logger.error(e);
		//		} catch (IllegalAccessException e) {
		//			Logger.error(e);
		//		} catch (NoSuchMethodException e) {
		//			Logger.error(e);
		//		} catch (InvocationTargetException e) {
		//			Logger.error(e);
		//		} catch(Throwable e) {
		//			Logger.fatal(e);
		//		}
		//		if (ret != 0) {
		//			String title = UIManager.getString("ApplicationStart.startFaultTitle");
		//			String content = UIManager.getString("ApplicationStart.startFaultContent");
		//			// 弹出错误
		//			showFault(title, content); // "出错", "不能正确启动应用");
		//		}
	}

	/**
	 * 根据WKey，打开一个应用软件
	 * @param key
	 */
	public void open(WKey key) {
		// 不存在时...
		if (!RTManager.getInstance().hasProgram(key)) {
			String title = UIManager.getString("ApplicationStart.notfoundTitle");
			String content = UIManager.getString("ApplicationStart.notfoundContent");
			desktop.playSound(SoundTag.ERROR);
			MessageBox.showFault(PlatformKit.getPlatformDesktop(), title, content);
			return;
		}

		addThread(new RunApplicationThread(key, null));

		//		// 用线程处理
		//		RunApplicationThread rt = new RunApplicationThread(key, null);
		//		rt.execute();

		//		int ret = -1;
		//		try {
		//			ret = ApplicationStarter.start(key);
		//		} catch (SecurityException e) {
		//			Logger.error(e);
		//		} catch (IllegalArgumentException e) {
		//			Logger.error(e);
		//		} catch (IOException e) {
		//			Logger.error(e);
		//		} catch (InstantiationException e) {
		//			Logger.error(e);
		//		} catch (IllegalAccessException e) {
		//			Logger.error(e);
		//		} catch (NoSuchMethodException e) {
		//			Logger.error(e);
		//		} catch (InvocationTargetException e) {
		//			Logger.error(e);
		//		} catch(Throwable e) {
		//			Logger.fatal(e);
		//		}
		//		if (ret != 0) {
		//			String title = UIManager.getString("ApplicationStart.startFaultTitle");
		//			String content = UIManager.getString("ApplicationStart.startFaultContent");
		//			// 弹出错误
		//			desktop.playSound(SoundTag.ERROR);
		//			MessageBox.showFault(desktop, title, content); // "出错", "不能正确启动应用");
		//		}
	}

}