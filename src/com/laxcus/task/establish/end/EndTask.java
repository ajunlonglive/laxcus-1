/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.end;

import java.io.*;

import com.laxcus.access.stub.index.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.local.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.display.show.*;

/**
 * ESTABLISH.END阶段任务。<br><br>
 * 
 * ESTABLISH.END阶段任务被部署和运行在FRONT站点上，是数据构建的最后一环。
 * 它承接ESTABLISH.RISE阶段任务，解释和显示RISE阶段生成的字节数组数据，显示在FRONT站点的屏幕上。<br>
 * 
 * ESTABLISH.END阶段任务的功能和作用，和CONDUCT.PUT阶段任务大体一致。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/12/2012
 * @since laxcus 1.0
 */
public abstract class EndTask extends LocalTask {

	/** END阶段资源代理 **/
	private EndTrustor trustor;

	/**
	 * 构造默认的数据构建END阶段任务实例
	 */
	protected EndTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeTask#destroy()
	 */
	@Override
	public void destroy() {
		trustor = null;
		super.destroy();
	}

	/**
	 * 设置END阶段资源代理
	 * @param e EndTrustor实例
	 */
	protected void setEndTrustor(EndTrustor e) {
		super.setTailTrustor(e);
		this.trustor = e;
	}

	/**
	 * 返回END阶段资源代理
	 * @return EndTrustor实例
	 */
	protected EndTrustor getEndTrustor() {
		return this.trustor;
	}
	
	/**
	 * 显示RISE标题
	 */
	protected void defaultDisplayTitle() {
		ShowTitleCell source = new ShowTitleCell(0, "Site");
		ShowTitleCell stub = new ShowTitleCell(1, "Stub");
		ShowTitleCell length = new ShowTitleCell(2, "Length");
		ShowTitleCell rank = new ShowTitleCell(3, "Rank");
		ShowTitleCell status = new ShowTitleCell(4, "Status");

		ShowTitle title = new ShowTitle();
		title.add(source);
		title.add(stub);
		title.add(length);
		title.add(rank);
		title.add(status);

		// 显示标题
		getDisplay().setShowTitle(title);
	}

	/**
	 * 显示RISE成员
	 * @param area RiseArea实例
	 */
	protected void defaultDisplayArea(RiseArea area) {
		for(RiseNewField field : area.getNewFields()) {
			Node node =	field.getSource();
			for(StubItem item : field.getStubItems()) {
				ShowItem content = new ShowItem();

				ShowStringCell s1 = new ShowStringCell(0, node.toString());
				ShowStringCell s2 = new ShowStringCell(1, String.format("%X", item.getStub()));
				ShowLongCell s3 = new ShowLongCell(2, item.getLength());
				ShowShortCell s4 = new ShowShortCell(3, item.getRank());
				ShowShortCell s5 = new ShowShortCell(4, item.getStatus());

				content.add(s1);
				content.add(s2);
				content.add(s3);
				content.add(s4);
				content.add(s5);

				getDisplay().addShowItem(content);
			}
		}
	}

	/**
	 * 从内存中解析RISE数据信息，在窗口界面显示出来
	 * @param b 字节数组
	 * @param off 有效数据开始下标
	 * @param len 有效数据长度
	 * @return 返回被解析的数据长度
	 * @throws TaskException
	 */
	protected long defaultDisplay(byte[] b, int off, int len) throws TaskException {
		defaultDisplayTitle();

		ClassReader reader = new ClassReader(b, off, len);
		while (reader.hasLeft()) {
			RiseArea area = new RiseArea(reader);
			defaultDisplayArea(area);
		}
		return reader.getUsed();
	}

	/**
	 * 从文件中解析RISE数据信息，在窗口界面显示出来
	 * @param files 文件集合
	 * @return 返回全部被解析的数据长度
	 * @throws TaskException
	 */
	protected long defaultDisplay(File[] files) throws TaskException {
		defaultDisplayTitle();
		
		long size = 0;
		for (File file : files) {
			try {
				ClassReader reader = new ClassReader(file);
				while (reader.hasLeft()) {
					RiseArea area = new RiseArea(reader);
					defaultDisplayArea(area);
				}
				size += reader.getUsed();
			} catch (IOException e) {
				throw new EndTaskException(e);
			} catch (Throwable e) {
				throw new EndTaskException(e);
			}
		}
		// 返回解析的文件长度
		return size;
	}

}