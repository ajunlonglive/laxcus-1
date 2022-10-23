/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.effect;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.log.client.*;
import com.laxcus.top.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/**
 * 主键生成器。<br>
 * 所有数据库表主键编号产生器(从0开始分配)
 * 
 * @author scott.liang
 * @version 1.2 12/3/2009
 * @since laxcus 1.0
 */
public class KeyIterator extends MutexHandler implements Classable {

	/** 主键生成器 **/
	private static KeyIterator selfHandle = new KeyIterator();

	private final static String DISKFILE = "pid.conf";
	
	/** 数据库表名 -> 主键当前已分配编号(从0开始) **/
	private Map<Space, Number> mapKey = new TreeMap<Space, Number>();

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();

		Iterator<Map.Entry<Space, Number>> iterator = mapKey.entrySet().iterator();
		writer.writeInt(mapKey.size());
		while (iterator.hasNext()) {
			Map.Entry<Space, Number> entry = iterator.next();
			writer.writeObject(entry.getKey());
			Number num = entry.getValue();
			if (num.getClass() == java.lang.Short.class){
				writer.write(ColumnType.SHORT);
				writer.writeShort(num.shortValue());
			}
			else if (num.getClass() == java.lang.Integer.class) {
				writer.write(ColumnType.INTEGER);
				writer.writeInt(num.intValue());
			}
			else if (num.getClass() == java.lang.Long.class) {
				writer.write(ColumnType.LONG);
				writer.writeLong(num.longValue());
			}
			else if (num.getClass() == java.lang.Float.class) {
				writer.write(ColumnType.FLOAT);
				writer.writeFloat(num.floatValue());
			}
			else if (num.getClass() == java.lang.Double.class) {
				writer.write(ColumnType.DOUBLE);
				writer.writeDouble(num.doubleValue());
			}
		}

		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space space = new Space(reader);
			byte family = reader.read();
			switch (family) {
			case ColumnType.SHORT:
				mapKey.put(space, reader.readShort());
				break;
			case ColumnType.INTEGER:
				mapKey.put(space, reader.readInt());
				break;
			case ColumnType.LONG:
				mapKey.put(space, reader.readLong());
				break;
			case ColumnType.FLOAT:
				mapKey.put(space, reader.readFloat());
				break;
			case ColumnType.DOUBLE:
				mapKey.put(space, reader.readDouble());
				break;
			}
		}
		return reader.getSeek() - scale;
	}

	/**
	 *
	 */
	private KeyIterator() {
		super();
	}
	
	/**
	 * 返回主键生成器的静态实例。
	 * @return
	 */
	public static KeyIterator getInstance() {
		return KeyIterator.selfHandle;
	}
	
	public boolean isEmpty() {
		return mapKey.isEmpty();
	}

	public int size() {
		return mapKey.size();
	}

	public void clear() {
		mapKey.clear();
	}
	
	/**
	 * @param space
	 * @param value
	 * @return
	 */
	public boolean set(Space space, Number value) {
		super.lockSingle();
		try {
			if (mapKey.containsKey(space)) {
				return false;
			}
			return mapKey.put(space, value) == null;
		} catch (Throwable t) {
			Logger.fatal(t);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * get keys
	 * @param space
	 * @param count
	 * @return
	 */
	public Number[] pull(Space space, int count) {
		ArrayList<Number> array = new ArrayList<Number>();

		super.lockSingle();
		try {
			if (!mapKey.containsKey(space)) {
				return null;
			}
			Number digit = mapKey.get(space);
			if (digit.getClass() == Short.class) {
				// 分配指定数量的主键编号
				short value = digit.shortValue();
				for (int i = 0; i < count; i++) {
					array.add(new Short(value++));
				}
				// 更新为新的参数
				mapKey.put(space, new Short(value));
			} else if (digit.getClass() == Integer.class) {
				int value = digit.intValue(); // ((Integer) digit).intValue();
				for (int i = 0; i < count; i++) {
					array.add(new Integer(value++));
				}
				mapKey.put(space, new Integer(value));
			} else if (digit.getClass() == Long.class) {
				long value = digit.longValue(); // ((Long) digit).longValue();
				for (int i = 0; i < count; i++) {
					array.add(new Long(value++));
				}
				mapKey.put(space, new Long(value));
			} else if (digit.getClass() == Float.class) {
				float value = digit.floatValue(); // ((Float) digit).floatValue();
				for (int i = 0; i < count; i++) {
					array.add(new Float(value++));
				}
				mapKey.put(space, new Float(value));
			} else if (digit.getClass() == Double.class) {
				double value = digit.doubleValue(); // ((Double) digit).doubleValue();
				for (int i = 0; i < count; i++) {
					array.add(new Double(value++));
				}
				mapKey.put(space, new Double(value));
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}

		
		Number[] s = new Number[array.size()];
		return array.toArray(s);
	}
	
//	private String buildType(Number num) {
//		String type = "";
//		if (num.getClass() == Short.class) type = "SHORT";
//		else if (num.getClass() == Integer.class) type = "INT";
//		else if (num.getClass() == Long.class) type = "LONG";
//		else if (num.getClass() == Float.class) type = "FLOAT";
//		else if (num.getClass() == Double.class) type = "DOUBLE";
//		return element("type", type);
//	}
//	
//	private String buildValue(Number num) {
//		String value = "";
//		if (num.getClass() == Short.class) value = String.valueOf(num.shortValue());
//		else if (num.getClass() == Integer.class) value = String.valueOf(num.intValue());
//		else if (num.getClass() == Long.class) value = String.valueOf(num.longValue());
//		else if (num.getClass() == Float.class) value = String.valueOf(num.floatValue());
//		else if (num.getClass() == Double.class) value = String.valueOf(num.doubleValue());
//		return element("value", value);
//	}
//
//	/**
//	 * build to 
//	 * @return
//	 */
//	public byte[] buildXML() {
//		StringBuilder buff = new StringBuilder(10240);
//		for (Space space : mapKey.keySet()) {
//			String s1 = element("schema", space.getSchema());
//			String s2 = element("table", space.getTable());
//			String s3 = element("space", s1 + s2);
//			Number num = mapKey.get(space);
//			String type = buildType(num);
//			String value = buildValue(num);
//			String key = element("key", s3 + type + value);
//			buff.append(key);
//		}
//		String body = XML.element("application", buff.toString());
//		return toUTF8(Effect.xmlHead + body);
//	}
//	
//	/**
//	 * parse data
//	 * @param bytes
//	 * @return
//	 */
//	public boolean parseXML(byte[] bytes) {
//		XMLocal xml = new XMLocal();
//		Document doc = xml.loadXMLSource(bytes);
//		if(doc == null) {
//			return false;
//		}
//		NodeList list = doc.getElementsByTagName("key");
//		int len = list.getLength();
//		for (int i = 0; i < len; i++) {
//			Element elem = (Element) list.item(i);
//			String type = xml.getValue(elem, "type");
//			String value = xml.getValue(elem, "value");
//			String schema = xml.getValue(elem, "schema");
//			String table = xml.getValue(elem, "table");
//			// split value
//			Number num = null;
//			if ("SHORT".equalsIgnoreCase(type)) num = Short.valueOf(value);
//			else if ("INT".equalsIgnoreCase(type)) num = Integer.valueOf(value);
//			else if ("LONG".equalsIgnoreCase(type)) num = Long.valueOf(value);
//			else if ("FLOAT".equalsIgnoreCase(type)) num = Float.valueOf(value);
//			else if ("DOUBLE".equalsIgnoreCase(type)) num = Double.valueOf(value);
//			// save
//			Space space = new Space(schema, table);
//			this.set(space, num);
//		}
//		return true;
//	}


	public boolean load() {
		File file = TopLauncher.getInstance().createResourceFile(DISKFILE);
		// 允许数据字典不存在
		if (!file.exists()) {
			return true;
		}
		byte[] b = TopLauncher.getInstance().readFile(file);

		ClassReader reader = new ClassReader(b);
		int end = this.resolve(reader);
		return end == reader.getLength();
	}
	
	public boolean flush() {
		File file = TopLauncher.getInstance().createResourceFile(DISKFILE);
		ClassWriter writer = new ClassWriter(1024000);
		this.build(writer);
		byte[] b = writer.effuse();
		return TopLauncher.getInstance().flushFile(file, b);
	}
}