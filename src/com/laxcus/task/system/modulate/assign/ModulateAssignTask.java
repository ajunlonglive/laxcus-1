/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.modulate.assign;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.distribute.meta.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.seeker.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 数据优化的“ASSIGN”阶段 <br><br>
 * 
 * 它承接来自“SCAN/SIFT/SUBSIFT”阶段的数据，分析和整理它们，然后分配给“SIFT/RISE”阶段去处理。<br>
 * 
 * ASSIGN是SCAN/SIFT之间的桥梁。<br>
 * 
 * @author scott.liang
 * @version 1.1 1/9/2013
 * @since laxcus 1.0
 */
public class ModulateAssignTask extends AssignTask {

	/**
	 * 构造默认的数据优化ASSIGN阶段任务
	 */
	public ModulateAssignTask() {
		super();
	}

	private void writeScanAreas(List<ScanArea> areas) throws TaskException {
		Phase phase = super.getPhase();
		Phase scan = new Phase(PhaseTag.SCAN, phase.getSock());
		MetaTag tag = createMetaTag(scan, 0);

		ClassWriter writer = new ClassWriter();
		for (ScanArea e : areas) {
			writer.writeObject(e);
		}
		byte[] b = writer.effuse();
		write(tag, b, 0, b.length);
	}
	
	private List<ScanArea> readScanArea() throws TaskException {
		Phase phase = super.getPhase();
		Phase scan = new Phase(PhaseTag.SCAN, phase.getSock());
		MetaTag tag = createMetaTag(scan, 0);

		byte[] b = read(tag);
		ArrayList<ScanArea> array = new ArrayList<ScanArea>();
		ClassReader reader = new ClassReader(b);
		while (reader.hasLeft()) {
			ScanArea e = new ScanArea(reader);
			array.add(e);
		}
		return array;
	}

	private SiftObject assort(SiftObject object, List<ScanArea> array)
			throws TaskException {
		// 查找BUILD节点
		Phase phase = object.getPhase();
		NodeSet set = super.findSiftSites(phase);
		if (set == null || set.isEmpty()) {
			throw new AssignTaskException("cannot find build site by '%s'",
					phase);
		}

		Logger.debug(getIssuer(), this, "assort", "build site size is %d", set.size());

		// SIFT资源分派器
		SiftDispatcher dispatcher = new SiftDispatcher(phase);

		// 根据扫描结果数量和节点数量，分配SIFT阶段会话
		int size = array.size();
		for (int i = 0; i < size; i++) {
			Node node = set.next();
			SiftSession session = new SiftSession(phase, node);

			// 保存参数
			ScanArea area = array.get(i);
			for (ScanField e : area.list()) {
				if (e.getStubCount() > 0) {
					SiftHead head = new SiftHead(e.getFlag());
					head.addStubItems(e.getStubItems());
					session.add(head);
				}
			}

			// 有成员时，保存会话
			if (session.size() > 0) {
				dispatcher.addSession(session);
			}
		}

		dispatcher.setIssuer(getIssuer());
		object.setDispatcher(dispatcher);

		// 保存这批元数据
		writeScanAreas(array);

		Logger.debug(getIssuer(), this, "assort", "session size is %d", dispatcher.list().size());

		return object;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.assign.AssignTask#scan(com.laxcus.distribute.establish.SiftObject, java.io.File[])
	 */
	@Override
	public SiftObject scan(SiftObject sift, File[] files)
			throws TaskException {
		ArrayList<ScanArea> array = new ArrayList<ScanArea>();
		for (int i = 0; i < files.length; i++) {
			Logger.debug(getIssuer(), this, "scan", "%s length is %d", files[i], files[i].length());
			
			try {
				ClassReader reader = new ClassReader(files[i]);
				ScanArea area = new ScanArea(reader);
				array.add(area);
			} catch (IOException e) {
				throw new TaskException(e);
			}
		}
		return assort(sift, array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.assign.AssignTask#scan(com.laxcus.distribute.establish.SiftObject, byte[], int, int)
	 */
	@Override
	public SiftObject scan(SiftObject sift, byte[] b, int off, int len)
			throws TaskException {
		
		Logger.debug(getIssuer(), this, "scan", "length is %d", b.length);
		
		// 解析字节数组
		ArrayList<ScanArea> array = new ArrayList<ScanArea>();
		ClassReader reader = new ClassReader(b, off, len);
		while (reader.hasLeft()) {
			ScanArea area = new ScanArea(reader);
			array.add(area);
		}
		return assort(sift, array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.assign.AssignTask#sift(com.laxcus.distribute.establish.SiftObject, java.io.File[])
	 */
	@Override
	public SiftObject sift(SiftObject subsift, File[] files)
			throws TaskException {
		// MODULATE命令不需要调用这个接口，如果调用是错误
		throw new TaskException("illegal call");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.assign.AssignTask#sift(com.laxcus.distribute.establish.SiftObject, byte[], int, int)
	 */
	@Override
	public SiftObject sift(SiftObject subsift, byte[] b, int off, int len)
			throws TaskException {
		// 不调用
		throw new TaskException("illegal call");
	}
	
	/**
	 * 以数据块编号为键值，标注SCAN阶段数据
	 * @param areas - SCAN阶段数据区
	 * @return
	 */
	private Map<Long, ScanField> splitScanAreas(List<ScanArea> areas) {
		TreeMap<Long, ScanField> fields = new TreeMap<Long, ScanField>();
		for (ScanArea area : areas) {
			for (ScanField field : area.list()) {
				for (Long stub : field.getStubs()) {
					fields.put(stub, field);
				}
			}
		}
		return fields;
	}

	/**
	 * 以数据块编号为键值，标注SIFT阶段数据
	 * @param areas - SIFT阶段数据区
	 * @return
	 */
	private Map<Long, SiftField> splitSiftAreas(List<SiftArea> areas) {
		TreeMap<Long, SiftField> fields = new TreeMap<Long, SiftField>();

		for (SiftArea area : areas) {
			for (SiftField field : area.list()) {
				for (Long stub : field.getStubs()) {
					fields.put(stub, field);
				}
			}
		}

		return fields;
	}

	/**
	 * 查找一个最少使用的RISE SESSION。先检查UPDATE域，没有匹配检查DELETE域
	 * @param space - 数据表名
	 * @param sessions - RISE会话集合
	 * @return - RISE会话
	 */
	private RiseSession seekLeastSession(Space space, Map<Node, RiseSession> sessions) {
		Node hub = null;
		int count = 0;

		// 1. 找更新
		Iterator<Map.Entry<Node, RiseSession>> iterator = sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, RiseSession> entry = iterator.next();
			List<RiseNewHead> list = entry.getValue().getUpdateHeads();
			for (RiseNewHead head : list) {
				if (head.getSpace().compareTo(space) != 0) {
					continue;
				}
				if (count == 0 || count > head.getStubCount()) {
					count = head.getStubCount();
					hub = entry.getKey();
				}
			}
		}
		if (hub != null) {
			return sessions.get(hub);
		}
		
		//2. 找删除
		iterator = sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Node, RiseSession> entry = iterator.next();
			List<RiseOldHead> list = entry.getValue().getDeleteHeads();
			for (RiseOldHead head : list) {
				if (head.getSpace().compareTo(space) != 0) {
					continue;
				}
				if (count == 0 || count > head.getStubCount()) {
					count = head.getStubCount();
					hub = entry.getKey();
				}
			}
		}
		if (hub != null) {
			return sessions.get(hub);
		}
		
		return null;
	}

	/**
	 * 把更新会话中的每一个数据块拆解保存
	 * @param sessions
	 * @return
	 */
	private Map<StubFlag, RiseSession> splitUpdateSessions(Collection<RiseSession> sessions) {
		TreeMap<StubFlag, RiseSession> elements = new TreeMap<StubFlag, RiseSession>();
		for (RiseSession session : sessions) {
			for (RiseNewHead head : session.getUpdateHeads()) {
				Space space = head.getSpace();
				for (Long stub : head.getStubs()) {
					StubFlag flag = new StubFlag(space, stub);
					elements.put(flag, session);

					Logger.debug(getIssuer(), this, "splitUpdateSessions", "save %s to %s", flag, session.getRemote());
				}
			}
		}
		return elements;
	}

	/**
	 * 把删除会话中的每一个数据块拆解保存
	 * @param sessions
	 * @return
	 */
	private Map<StubFlag, RiseSession> splitDeleteSessions(Collection<RiseSession> sessions) {
		TreeMap<StubFlag, RiseSession> elements = new TreeMap<StubFlag, RiseSession>();
		for (RiseSession session : sessions) {
			for (RiseOldHead head : session.getDeleteHeads()) {
				Space space = head.getSpace();
				for (Long stub : head.getStubs()) {
					StubFlag flag = new StubFlag(space, stub);
					elements.put(flag, session);

					Logger.debug(getIssuer(), this, "splitDeleteSessions", "save %s to %s", flag, session.getRemote());
				}
			}
		}
		return elements;
	}

	private RiseObject feedback(RiseObject object,
			List<ScanArea> scanAreas, List<SiftArea> siftAreas)
			throws TaskException {
		
		// 保存元数据
		super.addSiftDocks(siftAreas);
		
		Siger issuer = super.getIssuer();
		Phase phase = object.getPhase();

		Map<Long, ScanField> scanFields = splitScanAreas(scanAreas);
		Map<Long, SiftField> siftFields = splitSiftAreas(siftAreas);

		// RISE会话
		TreeMap<Node, RiseSession> sessions = new TreeMap<Node, RiseSession>(); 

		//1. SIFT/SCAN并存的数据块，拿出SIFT.STUB，做为更新保存
		ArrayList<Long> keys = new ArrayList<Long>(siftFields.keySet());
		for (Long stub : keys) {
			// 查找SIFT.STUB在SCAN中的对应域
			ScanField field = scanFields.get(stub);
			if (field == null) {
				continue; // SCAN不存在，忽略它
			}

			EstablishFlag scanFlag = field.getFlag();
			// DATA.SCAN节点地址
			Node hub = scanFlag.getSource();

			RiseSession session = sessions.get(hub);
			if (session == null) {
				session = new RiseSession(phase, hub);
				sessions.put(session.getRemote(), session);
			}
			
			// 指向BUILD.SIFT的更新，加入队列
			SiftField siftField = siftFields.get(stub);
			EstablishFlag siftFlag = siftField.getFlag();
			RiseNewHead updateHead = session.findUpdateHead(siftFlag);
			if (updateHead == null) {
				updateHead = new RiseNewHead(siftFlag);
				session.addUpdateHead(updateHead);
			}

			// 拿出SIFT.STUB，做为更新保存
			StubItem item = siftField.findStubItem(stub);
			updateHead.addStubItem(item);

			Logger.debug(getIssuer(), this, "feedback", "add update %s to %s", item, siftFlag);
		}

		//2. SCAN有，SIFT没有，拿出SCAN.STUB，放入删除队列
		keys.clear();
		keys.addAll(scanFields.keySet());
		for (Long stub : keys) {
			SiftField field = siftFields.get(stub);
			// 如果不是空值，忽略它
			boolean empty = (field == null);
			if (!empty) {
				continue;
			}

			ScanField scanField = scanFields.get(stub);
			// SCAN标识
			EstablishFlag scanFlag = scanField.getFlag();
			// DATA.SCAN节点地址
			Node hub = scanFlag.getSource();

			RiseSession session = sessions.get(hub);
			if (session == null) {
				session = new RiseSession(phase, hub);
				sessions.put(session.getRemote(), session);
			}
			RiseOldHead deleteHead = session.findDeleteHead(scanFlag);
			if (deleteHead == null) {
				deleteHead = new RiseOldHead(scanFlag);
				session.addDeleteHead(deleteHead);
			}
			// 保存删除域
			deleteHead.addStub(stub);

			Logger.debug(getIssuer(), this, "feedback", "add delete %x to %s", stub, scanFlag);
		}

		// 3. SIFT有，SCAN没有，选择一个分配量最少的SESSION，拿出SIFT.STUB，做为更新保存
		keys.clear();
		keys.addAll(siftFields.keySet());
		for (Long stub : keys) {
			ScanField scanField = scanFields.get(stub); // DATA主机
			boolean empty = (scanField == null);
			if (!empty) {
				continue;
			}
			// 根据这个数据块对应的SIFT表，找到对应的SESSION表，选择数据块存量最少一个
			SiftField siftField = siftFields.get(stub);
			Space space = siftField.getSpace();
			// 查找一个分配量最少的RISE会话
			RiseSession session = seekLeastSession(space, sessions);
			if (session == null) {
//				Logger.error(this, "assort", "ignore %s at step 3", space);
//				return false; // 错误
				throw new TaskException("illegal session");
			}

			// 指向BUILD站点的更新，加入更新队列
			Node hub = siftField.getSource();
			EstablishFlag siftFlag = new EstablishFlag(space, hub);
			RiseNewHead head = session.findUpdateHead(siftFlag);
			if (head == null) {
				head = new RiseNewHead(siftFlag);
				session.addUpdateHead(head);
			}
			// 找到数据
			StubItem item = siftField.findStubItem(stub);
			head.addStubItem(item);

			Logger.debug(getIssuer(), this, "feedback", "distribute %s to %s", item, siftFlag);
		}

		Logger.debug(getIssuer(), this, "feedback", "session size is %d", sessions.size());

		// 取出SESSION中分配的更新数据块
		Map<StubFlag, RiseSession> updates = splitUpdateSessions(sessions.values());
		// 取出SESSION中分配的删除数据块
		Map<StubFlag, RiseSession> deletes = splitDeleteSessions(sessions.values());
		// SCAN阶段辅助接口
		ScanSeeker helper = super.getScanSeeker();

		//4. 在更新STUB基础上，找到从节点，分配给从块
		ArrayList<StubFlag> flags = new ArrayList<StubFlag>(updates.keySet());
		for(StubFlag stubFlag : flags) {
			// 根据数据块，找到对应的SCAN站点
			Space space = stubFlag.getSpace();
			long stub = stubFlag.getStub();

			// 构建标识和数据块属性，指向BUILD.SIFT
			EstablishFlag siftFlag = null;
			StubItem item = null;

			RiseSession rs = updates.get(stubFlag);
			List<RiseNewHead> list = rs.getUpdateHeads();
			for (RiseNewHead head : list) {
				if (head.getSpace().compareTo(space) != 0) {
					continue;
				}
				item = head.findStubItem(stub);
				if (item != null) {
					siftFlag = head.getFlag();
					break;
				}
			}
			if (item == null) {
				continue;
			}

			// 查询全部关联的站点
			NodeSet set = helper.findStubSites(getInvokerId(), space, stub);
			Logger.debug(getIssuer(), this, "feedback", "slave update sites:%d", (set == null ? -1 : set.size()));
			if (set == null) {
				continue;
			}

			for (Node node : set.list()) {
				// 只查DATA从站点
				boolean match = helper.isSlave(getInvokerId(), node);
				if (!match) {
					continue;
				}

				// 取RISE会话，没有建立一个
				RiseSession session = sessions.get(node);
				if (session == null) {
					session = new RiseSession(phase, node);
					sessions.put(session.getRemote(), session);
				}
				// 指向BUILD.SIFT的更新
				RiseNewHead head = session.findUpdateHead(siftFlag);
				if (head == null) {
					head = new RiseNewHead(siftFlag);
					session.addUpdateHead(head);
				}
				// 保存BUILD更新单元
				head.addStubItem(item);
			}
		}

		// 5. 在删除STUB基础上，找到从节点，保存被删除块
		flags.clear();
		flags.addAll(deletes.keySet());
		for(StubFlag sf : flags) {
			// 根据数据块，找到对应的SCAN站点
			Space space = sf.getSpace();
			long stub = sf.getStub();

			// 查找数据块编号存在
			RiseSession rs = deletes.get(sf);
			boolean found = false;
			List<RiseOldHead> list = rs.getDeleteHeads();
			for (RiseOldHead head : list) {
				if (head.getSpace().compareTo(space) != 0) {
					continue;
				}
				if(found = head.hasStub(stub)) {
					break;
				}
			}
			// 没有找到，过滤它
			if (!found) {
				continue;
			}

			// 查询全部关联的站点
			NodeSet set = helper.findStubSites(getInvokerId(), space, stub);
			Logger.debug(getIssuer(), this, "feedback", "slave delete sites:%d", (set == null ? -1 : set.size()));
			if (set == null) {
				continue;
			}

			for(Node node : set.list()) {
				// 只查DATA从站点
				boolean match = helper.isSlave(getInvokerId(), node);
				if (!match) {
					continue;
				}

				RiseSession session = sessions.get(node);
				if (session == null) {
					session = new RiseSession(phase, node);
					sessions.put(session.getRemote(), session);
				}

				// 指向DATA.SIFT的数据构建标识，和RISE删除
				EstablishFlag scanFlag = new EstablishFlag(space, node);
				RiseOldHead head = session.findDeleteHead(scanFlag);
				if (head == null) {
					head = new RiseOldHead(scanFlag);
					session.addDeleteHead(head);
				}
				head.addStub(stub);
			}
		}

		// 全部会话，放入RISE分派器
		RiseDispatcher dispatcher = new RiseDispatcher();
		dispatcher.addSessions(sessions.values());
		dispatcher.setIssuer(issuer);
		object.setDispatcher(dispatcher);
		
		Logger.debug(getIssuer(), this, "feedback", "rise session size is:%s", sessions.size());

		return object;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.assign.AssignTask#rise(com.laxcus.distribute.establish.RiseObject, java.io.File[])
	 */
	@Override
	public RiseObject rise(RiseObject rise, File[] files)
			throws TaskException {
		ArrayList<SiftArea> siftAreas = new ArrayList<SiftArea>();
		for (int i = 0; i < files.length; i++) {
			try {
				ClassReader reader = new ClassReader(files[i]);
				SiftArea area = new SiftArea(reader);
				siftAreas.add(area);
			} catch (IOException e) {
				throw new TaskException(e);
			}
		}

		// 读SCAN
		List<ScanArea> scanAreas = readScanArea();

		// 结合SCAN和SIFT产生的数据，分配给RISE阶段
		return feedback(rise, scanAreas, siftAreas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.assign.AssignTask#rise(com.laxcus.distribute.establish.RiseObject, byte[], int, int)
	 */
	@Override
	public RiseObject rise(RiseObject rise, byte[] b, int off, int len)
			throws TaskException {
		// 解析字节数组
		ArrayList<SiftArea> siftAreas = new ArrayList<SiftArea>();
		ClassReader reader = new ClassReader(b, off, len);
		while (reader.hasLeft()) {
			SiftArea area = new SiftArea(reader);
			siftAreas.add(area);
		}

		List<ScanArea> scanAreas = readScanArea();

		return feedback(rise, scanAreas, siftAreas);
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.establish.assign.AssignTask#allocate(byte[], int, int)
	//	 */
	//	@Override
	//	public boolean allocate(byte[] b, int off, int len) throws TaskException {
	//		ArrayList<EstablishScanArea> array = new ArrayList<EstablishScanArea>();
	//
	//		// 读取SCAN扫描结果
	//		ClassReader reader = new ClassReader(b, off, len);
	//		while (reader.getSeek() < reader.getEnd()) {
	//			EstablishScanArea area = new EstablishScanArea(reader);
	//			array.add(area);
	//		}
	//
	//		Logger.debug(getIssuer(), this, "allocate", "scan area size is %d", array.size());
	//
	//		// 查找BUILD节点
	//		Establish estab = super.getCommand();
	//		SiftObject object = estab.getSift();
	//		Phase phase = object.getPhase();
	//		NodeSet set = super.findSiftSites(phase);
	//		if (set == null || set.isEmpty()) {
	//			throw new AssignTaskException("cannot find build site by '%s'", phase);
	//		}
	//
	//		Logger.debug(getIssuer(), this, "allocate", "build site size is %d", set.size());
	//
	//		// 根据扫描结果数量和节点数量，分配SIFT阶段会话
	//		int size = array.size();
	//		for (int i = 0; i < size; i++) {
	//			Node node = set.next();
	//			SiftSession session = new SiftSession(phase, node);
	//
	//			// 保存参数
	//			EstablishScanArea area = array.get(i);
	//			for (EstablishScanField e : area.list()) {
	//				if (e.getStubCount () > 0) {
	//					EstablishSiftHead head = new EstablishSiftHead(e.getFlag());
	//					head.addStubItems(e.getStubItems());
	//					session.add(head);
	//				}
	//			}
	//
	//			// 有成员时，保存会话
	//			if (session.size() > 0) {
	//				object.addSession(session);
	//			}
	//		}
	//
	//		Logger.debug(getIssuer(), this, "allocate", "session size is %d", object.getSessions().size());
	//
	//		return true;
	//	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.establish.assign.AssignTask#allocate(java.util.List)
//	 */
//	@Override
//	public boolean allocate(List<EstablishScanArea> scanAreas) throws TaskException {
//		Logger.debug(getIssuer(), this, "allocate", "scan area size is %d", scanAreas.size());
//
//		// 查找BUILD节点
//		Establish estab = super.getCommand();
//		SiftObject object = estab.getSiftObject();
//		Phase phase = object.getPhase();
//		NodeSet set = super.findSiftSites(phase);
//		if (set == null || set.isEmpty()) {
//			throw new AssignTaskException("cannot find build site by '%s'",
//					phase);
//		}
//
//		Logger.debug(getIssuer(), this, "allocate", "build site size is %d", set.size());
//
//		// SIFT资源分派器
//		SiftDispatcher dispatcher = new SiftDispatcher(phase);
//
//		// 根据扫描结果数量和节点数量，分配SIFT阶段会话
//		int size = scanAreas.size();
//		for (int i = 0; i < size; i++) {
//			Node node = set.next();
//			SiftSession session = new SiftSession(phase, node);
//
//			// 保存参数
//			EstablishScanArea area = scanAreas.get(i);
//			for (EstablishScanField e : area.list()) {
//				if (e.getStubCount() > 0) {
//					EstablishSiftHead head = new EstablishSiftHead(e.getFlag());
//					head.addStubItems(e.getStubItems());
//					session.add(head);
//				}
//			}
//
//			// 有成员时，保存会话
//			if (session.size() > 0) {
//				//				object.addSession(session);
//				dispatcher.addSession(session);
//			}
//		}
//
//		object.setDispatcher(dispatcher);
//
//		Logger.debug(getIssuer(), this, "allocate", "session size is %d", dispatcher.list().size());
//
//		return true;
//	}

}