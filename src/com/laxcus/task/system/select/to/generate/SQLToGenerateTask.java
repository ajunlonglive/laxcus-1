/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.generate;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.util.*;

/**
 * “GENERATE”模式的SQL操作。<br>
 * 所有基于“GENERATE”模式的SQL操作从这里派生。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2013
 * @since laxcus 1.0
 */
public abstract class SQLToGenerateTask extends ToGenerateTask {

	/**
	 * 建立SQLToGenerateTask实例
	 */
	protected SQLToGenerateTask() {
		super();
	}

	/**
	 * 解析数据和分割保存
	 * @param sheet - 列属性排序表
	 * @param sector - 数据分割
	 * @param b - 字节数组
	 * @param off - 开始下标
	 * @param len - 有效数据长度
	 * @return - 被解析的数据长度
	 * @throws TaskException
	 */
	protected int splitTo(Sheet sheet, ColumnSector sector, byte[] b, int off,
			int len) throws TaskException {
		// 解析数据
		RowCracker cracker = new RowCracker(sheet);
		int size = cracker.split(b, off, len);

		// 解析长度必须一致
		if (size != len) {
			throw new ToTaskException("split error! %d != %d", size, len);
		}

		// 输出全部
		List<Row> result = cracker.flush();

		// 数据分片和写入磁盘
		super.splitWriteTo(sector, result);

		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToGenerateTask#divide()
	 */
	@Override
	public long divide() throws TaskException {
		CastSelect post = (CastSelect) getSession().getCommand();
		Select select = post.getSelect();
		Space space = select.getSpace();
		List<Long> stubs = post.getStubs();

		// 命令来源站点地址，一定是CALL站点
		Node hub = getCommand().getSourceSite();

		// 获得数据块所在地址
		List<StubEntry> sites = getToTrustor().findStubSites(getInvokerId(), hub, space, stubs);

		// 执行SELECT命令
		for (StubEntry entry : sites) {
			hub = entry.getNode();
			for (long stub : entry.list()) {
				byte[] b = super.getToTrustor().select(getInvokerId(), hub, select, stub);
				// 忽略空值
				if (Laxkit.isEmpty(b)) {
					continue;
				}
				// 调用子类，去分割数据
				divide(b, 0, b.length);
			}
		}

		// 返回元数据
		return assemble();

		////		Node hub = this.getCommandSite();
		//
		//		// 去CALL站点查询数据块分布位置，并且等待结果。然后向不同节点发出SELECT操作指定
		//		ChoiceStubSite cmd = new ChoiceStubSite();
		//		cmd.addAll(stubs);
		//
		//		FindStubSiteHook hook = new FindStubSiteHook();
		//		ShiftFindStubSite shift = new ShiftFindStubSite(hub, cmd, hook);
		//
		//		boolean success = WorkCommandPool.getInstance().press(shift);
		//		if (!success) {
		//			Logger.error(this, "fireSelect", "cannot accept");
		//			return -1L;
		//		}
		//		// 钩子等待
		//		hook.await();
		//
		//		FindStubSiteProduct product = hook.getStubSiteProduct();
		//		// 出错
		//		if (product == null) {
		//			Logger.error(this, "fireSelect", "cannot be find stub site");
		//			return -1L;
		//		}
		//		// 数据块数目不匹配
		//		int count = product.getStubSize();
		//		if (count != stubs.size()) {
		//			Logger.error(this, "fireSelect", "cannot be match:%d,%d", count, stubs.size());
		//			return -1l;
		//		}
		//
		//		List<StubEntry> sites = product.list();
		//		Select select = post.getSelect();
		//
		//		// 根据数据块编号，生成POST命令
		//		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		//		for (StubEntry entry : sites) {
		//			CastSelect cell = new CastSelect(select, entry.list());
		//			CommandItem item = new CommandItem(entry.getNode(), cell);
		//			array.add(item);
		//		}
		//
		//		return 0;
	}

	/**
	 * 数据汇总操作 <br> 
	 * “assemble”方法在“divide/evaluate”方法之后执行，是对一次数据计算的数据汇总。它介于divide/evaluate和effuse/flushTo之间，只能调用一次。<br> 
	 * ToTask.assemble与FromTask.assemble的功能需求一致。<br> 
	 * 
	 * @return - 返回待输出数据的字节数组长度（如果有子级迭代任务，返回的是FluxArea字节数组长度，否则是实体数据长度）。
	 * @throws TaskException
	 */
	public long assemble() throws TaskException {
		byte[] b = effuse();
		return b.length;
	}
	
	/**
	 * 数据分割，允许在一次处理过程中任意多次调用。<br>
	 * 这个接口与FromTask.divide一样，由用户实现。
	 * 按照需要的规则对数据进行处理，将字节数组流解释成需要的数据，
	 * 分割的标准是以任务编号(taskId)+模值(mod)为键值，将结果写入本地磁盘。<br>
	 * 
	 * @param b - 与元信息对应的数据实体，以字节数组输入
	 * @param off - 数据实体的字节数组开始位置
	 * @param len - 数据实体的有效数据长度
	 * @throws TaskException
	 */
	public abstract void divide(byte[] b, int off, int len) throws TaskException;

}