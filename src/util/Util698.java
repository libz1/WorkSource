package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.eastsoft.protocol.Frame645;
import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import entity.Constant;
import frame.Frame698;
import gnu.io.CommPortIdentifier;
import javafx.application.Platform;
import javafx.base.javafxutil;
import javafx.collections.ObservableList;
import produce.control.entity.BaseCommLog;
import produce.control.simulation.PlatFormParam;
import produce.control.simulation.SimuRun;
import produce.entity.BarCodeAndAddr;
import socket.DealSendBlockData;
import socket.DealSendData;
import socket.PrefixMain;

/**
 * 698.45协议常用处理函数
 *
 * @author xuky
 * @version 2016.09.12
 */
public class Util698 {

	// 从BCD格式的日期时间数据得到常规
	static public String BCDDateTimeToNormal(String data) {
		String ret = "";
		// 07E0090D02001F130000
		// 07E0 09 0D 02 00 1F 13 0000
		// 07E1 04 0D 04 08 29 2400
		// 07E1 04 0E 05 09 08 28 0000
		if (data.length() == 20) {
			ret = DataConvert.hexString2String(data.substring(0, 4), 4) + "-"
					+ DataConvert.hexString2String(data.substring(4, 6), 2) + "-"
					+ DataConvert.hexString2String(data.substring(6, 8), 2) + " "
					// +DataConvert.hexString2String(data.substring(8,10),2)+":"
					// // 周次信息
					+ DataConvert.hexString2String(data.substring(10, 12), 2) + ":"
					+ DataConvert.hexString2String(data.substring(12, 14), 2) + ":"
					+ DataConvert.hexString2String(data.substring(14, 16), 2) + ":"
					+ DataConvert.hexString2String(data.substring(16), 3);
		}
		return ret;
	}

	// 从常规日期时间数据得到BCD格式
	static public String NormalToBCDDateTime(String data) {
		String ret = "";
		if (data.length() == 23) {
			// yyyy-mm-dd hh:mm:ss:sss
			// ret = data.replaceAll(" ","" );
			// ret = ret.replaceAll(":", "");
			// ret = ret.replaceAll("-", "");
			// String tmp = ret.substring(ret.length()-3,ret.length());
			// tmp = DataConvert.String2HexString(tmp,4);
			// ret = ret.substring(0, ret.length()-3)+ tmp;

			String tmp = getWeekOfDate(data);
			ret = DataConvert.String2HexString(data.substring(0, 4), 4)
					+ DataConvert.String2HexString(data.substring(5, 7), 2)
					+ DataConvert.String2HexString(data.substring(8, 10), 2) + DataConvert.String2HexString(tmp, 2)
					+ DataConvert.String2HexString(data.substring(11, 13), 2)
					+ DataConvert.String2HexString(data.substring(14, 16), 2)
					+ DataConvert.String2HexString(data.substring(17, 19), 2)
					+ DataConvert.String2HexString(data.substring(20), 4);
			// day_of_week：0表示周日，1~6分别表示周一到周六
		}
		return ret;
	}

	public static String getWeekOfDate(String dateStr) {
		// String[] weekDaysName = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
		// "星期六" };
		String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DateTimeFun.string2Date(dateStr, "yyyy-MM-dd HH:mm:SSS"));
		int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		return weekDaysCode[intWeek];
	}

	// 对字符串添加分隔符
	static public String seprateString(String data, String str) {
		// 添加分隔符前，先清理原先的分隔符数据
		data = data.replaceAll(" ", "");
		data = data.replaceAll(",", "");
		String ret = "";
		if (data.length() >= 2) {
			String tmp = data;
			while (tmp.length() >= 2) {
				ret += tmp.substring(0, 2) + str;
				tmp = tmp.substring(2);
			}
			ret += tmp;
		} else
			ret = data;
		return ret;
	}

	static int[] fcstab = { 0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf, 0x8c48, 0x9dc1, 0xaf5a,
			0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7, 0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
			0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876, 0x2102, 0x308b, 0x0210, 0x1399, 0x6726,
			0x76af, 0x4434, 0x55bd, 0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5, 0x3183, 0x200a,
			0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c, 0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd,
			0xc974, 0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb, 0xce4c, 0xdfc5, 0xed5e, 0xfcd7,
			0x8868, 0x99e1, 0xab7a, 0xbaf3, 0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a, 0xdecd,
			0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72, 0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab,
			0x0630, 0x17b9, 0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1, 0x7387, 0x620e, 0x5095,
			0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738, 0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
			0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7, 0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64,
			0x5fed, 0x6d76, 0x7cff, 0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036, 0x18c1, 0x0948,
			0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e, 0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c,
			0xd1b5, 0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd, 0xb58b, 0xa402, 0x9699, 0x8710,
			0xf3af, 0xe226, 0xd0bd, 0xc134, 0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c, 0xc60c,
			0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3, 0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9,
			0x2f72, 0x3efb, 0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232, 0x5ac5, 0x4b4c, 0x79d7,
			0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a, 0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
			0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9, 0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab,
			0xa022, 0x92b9, 0x8330, 0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78 };

	static private int compuCS(int fcs, byte[] frame, int len) {
		int i = 0, c, d;
		while (i < len) {
			c = (fcs ^ frame[i]) & 0xFF;
			d = fcstab[c];
			fcs = (fcs >> 8) ^ d;
			i++;
		}
		return fcs;
	}

	static public String getCS(String data) {
		data = data.replaceAll(",", "");
		data = data.replaceAll(" ", "");
		int fcs = 0xFFFF;
		byte[] frame = DataConvert.hexString2ByteArray(data);
		int len = frame.length;
		int cs = compuCS(fcs, frame, len);
		cs = cs ^ 0xFFFF;
		return DataConvert.int2HexString(cs, 4);
	}

	static public String getPercent(int minNum, int maxNum, int dec) {
		int num1 = minNum;
		int num2 = maxNum;
		// 参考http://blog.csdn.net/macwhirr123/article/details/7552806
		// 创建一个数值格式化对象
		NumberFormat numberFormat = NumberFormat.getInstance();
		// 设置精确到小数点后2位
		numberFormat.setMaximumFractionDigits(dec);
		String result = numberFormat.format((float) num1 / (float) num2 * 100);
		return result;
	}

	static public Boolean isEven(String data) {
		Boolean ret = false;
		int addrLen = data.length();
		if (addrLen / 2 * 2 == addrLen) {
			// 偶数长度
			ret = true;
		}
		return ret;
	}

	static public String checkFrameType(String msg) {
		String ret = "";
		msg = msg.replaceAll(" ", "");
		msg = msg.replaceAll(",", "");
		// 68开始 16结束
		msg = Util698.trimFronStr(msg, "FE");
		if (msg.substring(0, 2).equals("68") && msg.substring(msg.length() - 2, msg.length()).equals("16")) {
//			System.out.println("checkFrameType " + msg);
			// 距离第一个68后12位，又是一个68
			if (msg.length() > 14)
				if (msg.substring(14, 16).equals("68"))
					ret = "645";
			if (msg.length() > 8)
				if (msg.substring(6, 8).equals("68")){
					// xuky 2018.04.12 添加对于PLC协议的判断 与II采自动化产线的通信过程
					int len  = DataConvert.hexString2Int(msg.substring(2,4));
					if (msg.length()/2 == len + 6)
						ret = "PLC";
				}
		}
		if (msg.indexOf("AAAAAAAAAAAA") >= 0 && msg.indexOf("6817004345") >= 0) {
			ret = "APPLY_ADDR";
		}
		if (ret.equals("")){
			try{
				int pos1 = msg.indexOf("68");
				String str = msg.substring(pos1+2,pos1+4);
				int len1 = DataConvert.hexString2Int(str)*2;
				len1 = pos1 + len1;
				str = msg.substring(len1-2,len1);
				if (str.equals("16"))
					ret = "376.2";
			}
			catch(Exception e){
				Util698.log(Util698.class.getName(), "checkFrameType Exception:" + e.getMessage(), Debug.LOG_INFO);
			}
		}

		return ret;
	}

	// 删除指定目录下，文件名中包含某个数据的文件
	public static void deleteFiles(String sPath, String context) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			dirFile = null;
			return;
		}
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				String filename = files[i].getAbsolutePath();
				if (filename.indexOf(context) >= 0)
					deleteFile(filename);
			}
		}
		files = null;
		dirFile = null;
	}

	public static void deleteFile(String sPath) {
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
		}
		file = null;
	}

	// 获取属性类型(type)，属性名(name)，属性值(value)的map组成的list
	// 参考http://blog.csdn.net/linshutao/article/details/7693625
	public static List getFiledsInfo(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		List<Map> list = new ArrayList();
		Map<String, Object> infoMap = null;

		String type = "";
		for (int i = 0; i < fields.length; i++) {
			infoMap = new HashMap<String, Object>();
			type = fields[i].getType().toString();
			// xuky 2016.11.15
			if (type.toLowerCase().indexOf("list") < 0) {
				infoMap.put("type", fields[i].getType());
				infoMap.put("name", fields[i].getName());
				infoMap.put("value", getFieldValueByName(fields[i].getName(), o));
				list.add(infoMap);
			}
		}
		infoMap = null;
		fields = null;
		return list;
	}

	// 获取第一个属性信息
	public static Map<String, Object> getFirstFiledsInfo(Object o) {
		Field[] fields = o.getClass().getDeclaredFields();
		Map<String, Object> infoMap = new HashMap<String, Object>();
		infoMap.put("type", fields[0].getType());
		infoMap.put("name", fields[0].getName());
		infoMap.put("value", getFieldValueByName(fields[0].getName(), o));
		fields = null;
		return infoMap;
	}

	// 根据属性名称获取属性信息
	public static Map<String, Object> getFiledsInfoByName(Object o, String name) {
		Field[] fields = o.getClass().getDeclaredFields();
		Map<String, Object> infoMap = new HashMap<String, Object>();
		for (Field f : fields) {
			if (f.getName().toLowerCase().equals(name.toLowerCase())) {
				infoMap.put("type", f.getType());
				infoMap.put("name", f.getName());
				infoMap.put("value", getFieldValueByName(f.getName(), o));
				break;
			}
		}
		fields = null;
		return infoMap;
	}

	// 得到对象的属性内容
	public static Object getObjectAttr(Object o, String getter) {
		Object value = null;
		try {
			Method method = o.getClass().getMethod(getter, new Class[] {});
			value = method.invoke(o, new Object[] {});
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "getObjectAttr Exception:" + e.getMessage(), Debug.LOG_INFO);
			e.printStackTrace();
		}
		return value;
	}

	// 得到多个对象的属性内容，各个属性内容转为字符串，使用,进行分隔
	public static String getObjectAttrs(Object o, String getter) {
		// 有,分隔，表示有多个字段
		String ret = "";
		String[] tmp = getter.split(",");
		for (String str : tmp) {
			Object value = null;
			try {
				Method method = o.getClass().getMethod(str, new Class[] {});
				value = method.invoke(o, new Object[] {});
			} catch (Exception e) {
				Util698.log(Util698.class.getName(), "getObjectAttrs Exception:" + e.getMessage(), Debug.LOG_INFO);
				e.printStackTrace();
			}
			if (value == null)
				ret += ",";
			else {
				String type = value.getClass().toString();
				if (type.indexOf("String") >= 0)
					ret += (String) value + ",";

				if (type.toLowerCase().indexOf("int") >= 0)
					ret += DataConvert.int2String((int) value) + ",";
			}
		}

		return ret;
	}

	public static String getGetter(String attrName) {
		String firstLetter = attrName.substring(0, 1).toUpperCase();
		String getter = "get" + firstLetter + attrName.substring(1);
		return getter;
	}

	public static String getSetter(String attrName) {
		String firstLetter = attrName.substring(0, 1).toUpperCase();
		String getter = "set" + firstLetter + attrName.substring(1);
		return getter;
	}

	// 根据对象的属性名获取对象的属性值
	public static Object getFieldValueByName(String fieldName, Object o) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);

			Method method = o.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});

			// xuky 2017.03.10 用户录入的采集器地址有空格，导致后续处理异常，对此异常数据进行处理
			if (value != null) {
				String type = value.getClass().toString();
				if (type.indexOf("String") >= 0) {
					if (!type.equals("class [Ljava.lang.String;")) {
						String tmp = (String) value;
						tmp = tmp.trim();
						value = tmp;
					}
				}
			}
			return value;
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "getFieldValueByName Exception:" + e.getMessage(), Debug.LOG_INFO);
			e.printStackTrace();
			return null;
		}
	}

	public static Object[] getFieldValueTypeByName(String fieldName, Object o) {
		Object[] ret = new Object[2];
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);

			Method method = o.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			String type = "";
			// xuky 2017.03.10 用户录入的采集器地址有空格，导致后续处理异常，对此异常数据进行处理
			if (value != null) {
				type = value.getClass().toString();
				if (type.indexOf("String") >= 0) {
					if (!type.equals("class [Ljava.lang.String;")) {
						String tmp = (String) value;
						tmp = tmp.trim();
						value = tmp;
					}
				}
			}
			ret[0] = value;
			ret[1] = type;
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "getFieldValueTypeByName Exception:" + e.getMessage(), Debug.LOG_INFO);
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	// xuky 如果使用getFieldValueTypeByName 会因为数据为空，无法返回具体的属性类型
	public static String getFieldTypeByName(String fieldName, Object o) {
		List<Map> infoList = getFiledsInfo(o);
		String name, type;
		for (Map<String, Object> info : infoList) {
			if (info.get("name").toString().equals(fieldName))
				return info.get("type").toString();
		}
		return "";

	}

	// 根据对象的属性名设置对象的属性值
	// 需要有函数名称、函数参数（因为存在重载情况，通过参数类型进行函数判断）
	public static void setFieldValueByName(String fieldName, Object o, Object val) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "set" + firstLetter + fieldName.substring(1);

			Map<String, Object> info = getFiledsInfoByName(o, fieldName);
			Object valType = info.get("type");
			// xuku 2017.03.27 存在数据与对象属性不匹配的情况，需要进行数据处理
			String attr_type = ((Class<?>) valType).getName();
			String data_type = "";
			if (val != null) {
				data_type = val.getClass().getName();
				// 传入的参数类型为string，但是对象属性的类型为int，进行数据转换处理
				if (data_type.toLowerCase().indexOf("string") >= 0 && attr_type.equals("int")) {
					val = DataConvert.String2Int((String) val);
				}
				if (data_type.toLowerCase().indexOf("int") >= 0 && attr_type.equals("string")) {
					val = DataConvert.int2String((int) val);
				}
				Method method = o.getClass().getMethod(getter, new Class[] { (Class) valType });
				method.invoke(o, new Object[] { val });
			}
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "setFieldValueByName Exception:" + e.getMessage(), Debug.LOG_INFO);
			e.printStackTrace();
		}
	}

	// 判断两个对象是否相等 属性列表相同、属性类型相同、属性值相同
	public static Boolean objEquals(Object obj1, Object obj2, String excepColumns) {
		Boolean equas = false;

		// 如果有一个对象为空，另外一个不为空，则判断为不等
		if (obj1 == null && obj2 != null)
			return false;
		if (obj2 == null && obj1 != null)
			return false;

		excepColumns = "," + excepColumns.toLowerCase() + ",";
		List<Map> infoList = getFiledsInfo(obj1);
		String name, type;
		Object val1, val2;
		int attrNo = 0;
		equas = true;
		for (Map<String, Object> info : infoList) {
			name = info.get("name").toString();
			// 排除例外字段 例如id，meter.archiveType等
			if (excepColumns.indexOf("," + name.toLowerCase() + ",") < 0) {
				type = info.get("type").toString();
				val1 = Util698.getFieldValueByName(name, obj1);
				val2 = Util698.getFieldValueByName(name, obj2);

				// 需要处理数据为空的情况
				if (val1 == null && val2 != null)
					return false;
				if (val2 == null && val1 != null)
					return false;
				if (val1 != null && val2 != null)
					if (!val1.equals(val2)) {
						if ((val1.equals("00") && val2.equals("0")) || (val1.equals("0") && val2.equals("00"))) {
							// xuky 2017.03.10 出现00与0的比较
						} else
							return false;
					}
			}
			attrNo++;
		}
		return equas;
	}

	// 两个对象进行属性赋值 excepColumns为例外属性，例外属性不进行赋值操作
	public static Object objClone(Object sourceObj, Object targetObj, String excepColumns) {
		Boolean result = false;

		// 如果有一个对象为空，另外一个不为空，则无法继续操作 即在外部进行对象构建
		if (sourceObj == null && targetObj != null)
			return targetObj;
		if (targetObj == null && sourceObj != null)
			return targetObj;

		excepColumns = "," + excepColumns.toLowerCase() + ",";
		List<Map> sourceObjecAtrrList = getFiledsInfo(sourceObj);
		String name;
		Object val1;
		result = true;
		for (Map<String, Object> info : sourceObjecAtrrList) {
			name = info.get("name").toString();
			// 排除例外字段 例如id，meter.archiveType等
			if (excepColumns.indexOf("," + name.toLowerCase() + ",") < 0) {
				val1 = Util698.getFieldValueByName(name, sourceObj);
				Util698.setFieldValueByName(name, targetObj, val1);
			}
		}
		return targetObj;
	}

	// 对象属性转为数组内容
	public static Object[] obj2Array(Object obj) {
		List<Map> l = getFiledsInfo(obj);
		int attrNum = l.size();
		Object[] data = new Object[attrNum];

		int i = 0;
		for (Map<String, Object> info : l) {
			String name = info.get("name").toString();
			data[i] = getFieldValueByName(name, obj);
			i++;
		}
		return data;
	};

	// 将二维数组转为对象列表
	public static <T> List<T> array2ObjList(Object[][] data, NewObjAction newobjact) {
		List<T> retList = new ArrayList<T>();
		for (Object[] o : data) {

			// 此代码与实际对象关联紧密。需要调整
			// Attr d = new Attr();
			// setObjVal(d,o);

			// 将变化部分进行剥离 自定义对象中实现代码 通过回调函数，实现动态创建对象
			Object obj = newobjact.getNewObject();
			setObjVal(obj, o);

			// 将对象添加到列表中
			retList.add((T) obj);
		}
		return retList;
	}

	// 将数组转为字符串
	public static String array2String(String[] data) {
		String ret = "";
		for (String str : data) {
			if (str == null)
				str = "";
			ret = ret + str + ",";
		}
		ret = ret.substring(0,ret.length()-1);
		return ret;
	}

	public interface NewObjAction {
		Object getNewObject();
	}

	private static void setObjVal(Object obj, Object objVal[]) {
		List<Map> l = getFiledsInfo(obj);
		int i = 0;
		for (Map<String, Object> info : l) {
			String name = info.get("name").toString();
			// System.out.println("setObjVal=>"+name);
			if (objVal[i] == null)
				setFieldValueByName(name, obj, objVal[i]);
			else {
				// 数据类型可能是int，可能是Integer
				if (info.get("type").toString().toLowerCase().indexOf("int") >= 0)
					setFieldValueByName(name, obj, (int) objVal[i]);
				if (info.get("type").toString().indexOf("String") >= 0)
					setFieldValueByName(name, obj, (String) objVal[i]);
			}
			i++;
		}
	}

	public static String[] setArrayData(String[] data) {
		// xuky 2016.12.14 之前是自行编写的数组赋值代码，修改为使用系统提供的数组复制函数
		String[] ret = new String[data.length];
		System.arraycopy(data, 0, ret, 0, data.length);
		return ret;
	}

	public static Object[] setArrayData(Object[] data) {
		// xuky 2016.12.14 之前是自行编写的数组赋值代码，修改为使用系统提供的数组复制函数
		Object[] ret = new Object[data.length];
		System.arraycopy(data, 0, ret, 0, data.length);
		return ret;
	}

	public static Object[][] setArrayData(Object[][] data) {
		// xuky 2016.12.14 之前是自行编写的数组赋值代码，修改为使用系统提供的数组复制函数
		int row = data.length;
		Object[][] ret = new Object[row][data[0].length];
		for (int i = 0; i < row; i++) {
			ret[i] = setArrayData(data[i]);
		}
		return ret;
	}

	// 数组转为List
	public static List<Object> arrayToList(Object[] data) {
		return Arrays.asList(data);
	}

	// List转为数组
	public static Object[] listToArray(List<?> data) {
		int size = data.size();
		return data.toArray(new Object[size]);
	}

	// 判断添加的数据是否符合要求，如果不符合使用默认数据
	public static String getCodeData(String data, String type) {
		String ret = "";
		// 1、进行数据处理，可能失败，则直接使用原值
		try {
			data = DataConvert.int2String(DataConvert.hexString2Int(data));
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "getCodeData Exception:" + e.getMessage(), Debug.LOG_INFO);
			// 不进行打印输出 屏蔽此错误信息
			// e.printStackTrace();
		}

		// 2、整体进行匹配判断
		String[] array = (String[]) Util698.getFieldValueByName(type, new Constant());
		// 判断数据是否为数组的一个元素
		// 参考http://blog.csdn.net/maxracer/article/details/8439195
		List<String> tempList = Arrays.asList(array);
		if (tempList.contains(data))
			ret = data;
		else {
			// 3、数据两边加上左右括号，进行部分匹配判断
			data = "(" + data + ")";
			String findStr = "";
			for (String str : tempList) {
				if (str.indexOf(data) >= 0) {
					findStr = str;
					break;
				}
			}
			if (!findStr.equals(""))
				ret = findStr;
			else
				ret = Constant.getProtocolType()[0];
		}

		return ret;
	}

	// xuky 2018.05.10 参考 https://blog.csdn.net/e_wsq/article/details/70812637
	// 重新定位log4j.properties的位置，放在外面，用户可以自行调整
	public static void InitLog4jConfig() {
	    Properties props = null;
	    FileInputStream fis = null;
	    try {
	        // 从配置文件dbinfo.properties中读取配置信息
	        props = new Properties();
	        fis = new FileInputStream("arc\\log4j.properties");
	        props.load(fis);
	        PropertyConfigurator.configure(props);//装入log4j配置信息
	    } catch (Exception e) {
	    	Util698.log(Util698.class.getName(), "InitLog4jConfig Exception:" + e.getMessage(), Debug.LOG_INFO);
	        e.printStackTrace();
	    } finally {
	        if (fis != null)
	            try {
	                fis.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        fis = null;
	    }
	}

	public static void log(String className, String message, String level) {
		// log4j
		// 重点和难点在于log4j.properties的定义
		// log4j.properties记录了日志的存储方式、日志内容的格式、日志的存储选项等


		Logger log = Logger.getLogger(className);
		if (level.equals(Debug.LOG_DEBUG))
			log.debug(message);
		if (level.equals(Debug.LOG_INFO))
			log.info(message);
		if (level.equals(Debug.LOG_WARN))
			log.warn(message);
		if (level.equals(Debug.LOG_ERROR))
			log.error(message);
		if (level.equals(Debug.LOG_FATAL))
			log.fatal(message);
	}

	public static String leftPaddingZero(String str, int len) {
		// 判断str字符串是否为空或者null
		if (str != null && !"".equals(str)) {
			if (str.length() < len) {// 字符串长度小于指定长度，需要左填充
				// 1.使用字符串的格式化，先左填充空格
				String format = "%" + len + "s";
				String tempResult = String.format(format, str);

				// 2.使用String的replace函数将空格转换为指定字符即可
				String finalResult = tempResult.replace(" ", "0");

				return finalResult;
			} else {
				return str;
			}
		} else {
			return "左填充的字符串不能为空！";
		}

	}

	public static String Fill645CS(String str) {
		String CS = "00";
		str = str.replaceAll(" ", "");
		str = str.replaceAll(",", "");
		str = str.substring(0, str.length() - 4);
		int num = str.length() / 2;
		String sTemp = "";
		String aStr = "";
		int aTemp = 0;
		for (int i = 0; i < num; i++) {
			sTemp = str.substring(i * 2, (i + 1) * 2);
			aTemp = aTemp + Integer.valueOf(sTemp, 16);
		}
		aStr = "0000" + Integer.toHexString(aTemp);
		CS = aStr.substring(aStr.length() - 2, aStr.length());
		CS = CS.toUpperCase();

		return str + CS + "16";
	}

	// xuky 2017.07.12 解决迭代方式变量数据时，无法进行正常删除的情况
	// for (ProduceCaseResult produceCaseResult :list)
	public synchronized static void ListReMoveAll(ObservableList list) {
		if (list != null)
			if (list.size() > 0) {
				for (int i = list.size() - 1; i >= 0; i--)
					if (i < list.size()){
						list.remove(i);
					}
			}
	}

	public static void ListReMoveAll(List list) {
		if (list != null)
			if (list.size() > 0) {
				for (int i = list.size() - 1; i >= 0; i--){
					list.remove(i);
				}
			}
	}

	public static void ListReMoveAll(Map map) {
		Iterator<Entry<Object, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			it.remove();
			// Entry<Object, Object> itEntry = it.next();
			// Object itKey = itEntry.getKey();
			// Object itValue = itEntry.getValue();
			// System.out.println("key:" + itKey + " value:" + itValue);
			// //注意：可以使用这种遍历方式进行删除元素和修改元素
			// /*itEntry.setValue("ttt");
		}
	}

	public static Object getFirstObject(List list) {
		Object ret = null;
		if (list != null)
			if (list.size() > 0)
				ret = list.get(0);
		return ret;
	}

	public static Map<String, String> praseXml(String fileName)
			throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		// 定义工厂 API，使应用程序能够从 XML 文档获取生成 DOM 对象树的解析器。
		javax.xml.parsers.DocumentBuilderFactory dbf;
		// 获取 DocumentBuilderFactory 的新实例。
		dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		// 从 XML 获取一个 Document
		javax.xml.parsers.DocumentBuilder db;
		// 使用dbf当前配置的参数创建一个新的 DocumentBuilder 实例。
		db = dbf.newDocumentBuilder();
		// 定义org.w3c.dom.Document 接口
		org.w3c.dom.Document document;
		// 将给定输入源的内容解析为一个 XML 文档，并且返回一个新的 DOM Document 对象。
		document = db.parse(new FileInputStream(fileName));
		// Element 接口表示 XML 文档中的一个元素，根节点
		org.w3c.dom.Element root;
		// 允许直接访问文档的文档元素的子节点
		root = document.getDocumentElement();
		// 获得包含此节点的所有子节点的 NodeList
		org.w3c.dom.NodeList nodelist = root.getChildNodes();
		// 获得所有节点的名称与值
		for (int i = 0; i < nodelist.getLength(); i++) {
			String key = nodelist.item(i).getNodeName();
			String value = nodelist.item(i).getTextContent();
			map.put(key, value);
		}
		// 返回键值对
		return map;
	}

	public static boolean isHexNumber(String str) {
		// xuky 2017.08.03 严格的判断，完全10进制的数据不认为是16进制
		boolean flag = false;
		for (int i = 0; i < str.length(); i++) {
			char cc = str.charAt(i);
			if (cc == 'A' || cc == 'B' || cc == 'C' || cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a' || cc == 'b'
					|| cc == 'c' || cc == 'c' || cc == 'd' || cc == 'e' || cc == 'f') {
				flag = true;
			}
		}
		return flag;
	}

	public static Long hexString2Long(String data) {
		Long ret = Long.parseLong(data, 16);
		return ret;
	}

	public static String long2HexString(Long data, int charnum) {
		String ret = "";
		String temp = "";
		temp = Long.toHexString(data);
		for (int i = 1; i <= charnum; i++) {
			temp = "0" + temp;
		}
		ret = temp.substring(temp.length() - charnum, temp.length());
		ret = ret.toUpperCase();
		return ret;
	}

	// 根据入参处理用户填写的用例报文，得到最终的报文
	// xuky 2018.03.03 出现未正常解析数据的情况
	// 2018-03-03 10:13:03:288 [send 端口:1 Data:FE FE FE FE 68 AA AA AA AA AA AA 68 15 06 [SEQ=6] 12 16] socket.DealSendData
	public synchronized static String DealFrameWithParam(String send, String addr, String protocol) {
//		Util698.log("xuky", "DealFrameWithParam send:"+send+" addr:"+addr+" protocol:"+protocol, Debug.LOG_INFO);

		String ret = send;
		// xuky 2017.09.30 在某些情况下，可能无需返回数据
		if (send == null || send.equals(""))
			return "";
		if (send.startsWith("[误差"))
			return send;

//		if (send.equals("68 [SEQ=6] 68 91 0E 33 33 34 33 ** ** ** ** [MT485=6] ** 16")){
//			send = send + "";
//		}

		try {
			// 在telnet模式下直接就是[SEQ=8]内容
			if (send.substring(0, 1).equals("[")) {
				// [SEQ=4]
				String dealdata = send.split("\\[")[1].split("\\]")[0];
				ret = getDataByType(dealdata, "2", addr);

			} else if (send.length() < 4) {
				// 在telnet模式下，直接就是命令内容

			} else if (protocol.indexOf("376.2") >= 0) {
				ret = deal_3762(send, addr);

			} else if (protocol.indexOf("698.45") >= 0) {
				ret = deal_69845(send, addr);


			} else if (protocol == null || protocol.equals("") || protocol.indexOf("645") >= 0) {
				ret = deal_645(addr, ret);

			}
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "DealFrameWithParam Exception:" + e.getMessage(), Debug.LOG_INFO);
			e.printStackTrace();
		}
//		Util698.log("xuky", "DealFrameWithParam ret:"+ret, Debug.LOG_INFO);

		return ret;
	}

	private static String deal_3762(String send, String addr) {
		String ret;
		// xuky 2018.01.22 添加 376.2 协议
		// 需要重新CS  需要处理报文中可能包含的**信息
		send = send.replaceAll(" ", "");
		send = send.replaceAll(",", "");
		send = send.toUpperCase();
		// 682E0060111111111111[MT485=6]0207010202E8010080E8031068[MT485=6]68110433333433B716C116
		// 68[MT485=6]68110433333433B716
		// [MT485-1=6] [MT485-12=6]
		int pos1 = send.indexOf("68[MT485");
		// 其中包含了一个完整的645报文，需要对其进行处理
		ret = send;
		if (pos1 >= 0){
			int pos2 = send.indexOf("]",pos1);
			String str = send.substring(pos1,pos2+19);
			str = deal_645("",str);
			str = str + "";
			ret = send.substring(0,pos1)+str+send.substring(pos2+19);
		}
		ret = dela3762VAR(ret,addr);
		ret = ret.replaceAll("\\{", "\\[");
		ret = ret.replaceAll("\\}", "\\]");
		ret = deal3762LenCs(ret);
		return ret;
	}

	private static String dela3762VAR(String data,String addr){
		int pos = data.indexOf("[");
		while (pos >= 0) {

			String part1 = data.split("\\[")[0];
			// String part2 = ret.split("\\]")[1];
			// xuky 2017.07.07 如果有多个[]，需要进行特殊处理
			String part2 = data.substring(data.indexOf("]") + 1);
			String dealdata = data.split("\\[")[1].split("\\]")[0];

			// xuky 2017.11.01 用于区分数据在地址域还是数据域，是否加33H，是否倒序
			String flag = "0";
			// 对数据进行处理getDataByType
			dealdata = getDataByType(dealdata, flag, addr);
			data = part1 + dealdata + part2;

			// xuky 2017.07.07 1\以下代码有问题 2\是对期望报文的处理

			// xuky 2017.07.20 处理如下报文 68 [SEQ=6] 68 91 05 32 3A 33 37
			// [MT=1] **
			// 16
			int pos1 = data.indexOf("*");
			pos = data.indexOf("[");

			if (pos > pos1)
				if (data.indexOf("*") >= 0)
				{
					if (data.indexOf("[MT485") <= 0)
						return data;

				}

			// Frame645 frame645 = new Frame645(ret,"","");
			//
			// // xuky 2017.06.23 特殊情况 在Frame645(String frame, String
			// coll_param, String suffix) 未对Control=15的情况有效处理
			// if (frame645.getControl().equals("15") ||
			// frame645.getControl().equals("14")){
			// dealdata = DataConvert.reverseString(dealdata);
			// ret = part1 + dealdata + part2;
			// }
		}
		return data;

	}
	private static String deal3762LenCs(String data) {
		//680F004100005F000000030100A416
		String ret = "";
		String temp = "";
		String cs = "";
		String len1 ="";
		String len2 ="";
		String len ="";
		if (data.indexOf("[") < 0)
			return data;
		int lenth = 0;
		data = data.replaceAll("\\s", "");
//		p("data:" + data);
		Pattern p = Pattern.compile("68(\\[LEN3762=2\\])(.+)(\\[CS\\]16)");
		Matcher m = p.matcher(data);
		if (m.find()) {
			//CS是控制域加数据域的校验和
			temp = "68"+m.group(2)+m.group(3);
//			System.out.println("temp的值："+temp);
			lenth = (temp.length()-2+4)/2; //报文长度为见去[cs]的[和]再加上LEN3762占的4个
			if(lenth %256<=15)
				len1 = "0"+	Integer.toHexString(lenth %256);
			else
				len1 = Integer.toHexString(lenth %256);
			if(lenth /256<=15)
				len2 = "0"+	Integer.toHexString(lenth /256);
			else
				len2 = Integer.toHexString(lenth /256);
			len = len1 +len2;
			temp = "68"+len+temp.substring(2,temp.length());
//			System.out.println("报文是："+temp);
			cs = DataConvert.sumHEXMod256(temp.substring(6,temp.length()-6));
			ret = temp.replaceAll("\\[CS\\]", cs);
		} else {
			return "";
		}
		// 2、CS处理
		return ret.toUpperCase();
	}


	private static String deal_645(String addr, String data) {
		// 645协议报文
		// 处理前导符 FE 等
		String prefix = "";
		int posOfFirst68 = data.indexOf("68");
		if (posOfFirst68 != 0) {
			prefix = data.substring(0, posOfFirst68);
			data = data.substring(posOfFirst68);
		}

		// 第二个68的位置
		int posOfSecond68 = data.indexOf("68", 2);

		// 需要判断[SEQ=6]内容在数据的那一部分，
		// 645的 需要倒序
		// 如果是645的数据域需要加33H
		// 如果是645的地址域无需加33H
		int pos = data.indexOf("[");
		while (pos >= 0) {

			String part1 = data.split("\\[")[0];
			// String part2 = ret.split("\\]")[1];
			// xuky 2017.07.07 如果有多个[]，需要进行特殊处理
			String part2 = data.substring(data.indexOf("]") + 1);
			String dealdata = data.split("\\[")[1].split("\\]")[0];

			// xuky 2017.11.01 用于区分数据在地址域还是数据域，是否加33H，是否倒序
			String flag = "0";
			if (pos > posOfSecond68)
				flag = "1";
			// 对数据进行处理getDataByType
			dealdata = getDataByType(dealdata, flag, addr);
			data = part1 + dealdata + part2;

			// xuky 2017.07.07 1\以下代码有问题 2\是对期望报文的处理

			// xuky 2017.07.20 处理如下报文 68 [SEQ=6] 68 91 05 32 3A 33 37
			// [MT=1] **
			// 16
			int pos1 = data.indexOf("*");
			pos = data.indexOf("[");

			if (pos > pos1)
				if (data.indexOf("*") >= 0)
				{
					if (data.indexOf("[MT485") <= 0)
						return prefix + data;

				}

			// Frame645 frame645 = new Frame645(ret,"","");
			//
			// // xuky 2017.06.23 特殊情况 在Frame645(String frame, String
			// coll_param, String suffix) 未对Control=15的情况有效处理
			// if (frame645.getControl().equals("15") ||
			// frame645.getControl().equals("14")){
			// dealdata = DataConvert.reverseString(dealdata);
			// ret = part1 + dealdata + part2;
			// }
		}
		// 68 [MT485=6] 68 11 04 33 33 34 33 35 16
		// FE FE FE FE 68 AA AA AA AA AA AA 68 15 06 [SEQ=6] 12 16
		// xuky 2017.07.07 1\以下代码有问题 2\是对期望报文的处理
		if (data.indexOf("*") >= 0)
			return prefix + data;

		// xuky 2017.06.23 重新计算645报文的CS信息
		// Frame645 frame645 = new Frame645(ret,"","");
		data = Util698.Fill645CS(data);
		data = prefix + data;
		return data;
	}

	private static String deal_69845(String send, String addr) {
		String ret;
		// xuky 2017.09.25 添加 698.45 协议
		// 需要重新计算两个CS
		// 获取APDU部分，进行数据替换，然后更新终端地址，更新
		send = send.replaceAll(" ", "");
		send = send.replaceAll(",", "");
		String s_APDU = send.substring(24, send.length() - 6);
		Frame698 frame698 = new Frame698();
		// 1、设置终端地址 报文中的终端地址无需特别设置，一定是会进行替换的
		String addr1 = getDataByType("SEQ=4", "2", addr);
		frame698.getFrameAddr().setSAData(addr1);
		// 2、设置APDU信息
		frame698.getAPDU().init(s_APDU);
		// 4、得到具体报文内容
		ret = frame698.getFrame();
		return ret;
	}

	private static String getDataByType(String dealdata, String flag, String ADDR) {
		String ret = "";

		if (dealdata.indexOf("LEN3762") >= 0)
			return "{"+dealdata+"}";
		if (dealdata.indexOf("CS") >= 0)
			return "{"+dealdata+"}";

		String type = "";
		int len = 0;
		// xuky 2017.08.15 VERIFYVER的长度不确定
		if (dealdata.indexOf("=") >= 0) {
			type = dealdata.split("=")[0];
			len = DataConvert.String2Int(dealdata.split("=")[1]);
		}


		// VERIFYVER

		// 从外部参数表，获取此参数的内容信息
		if (type.equals("SEQ") || type.equals("SEQ_NO33")) {
			// xuky 2017.09.30 根据SEQ的长度信息来判断，如果SEQ=4表示是终端地址，需要专门进行数据处理
			if (len == 4) {
				// 取字符串的右边9位 4位区位码，5位10进制流水号
				String str = ADDR.substring(ADDR.length() - 9, ADDR.length());
				String part1 = str.substring(0, 4);
				String part2 = str.substring(4);
				part2 = DataConvert.String2HexString(part2, 4);
				ret = part1 + part2;
			} else {
				if (ADDR.length() > len * 2)
					ADDR = ADDR.substring(ADDR.length() - len * 2, ADDR.length());
				ret = SoftParameter.getInstance().getParamValByKey(type);
				if (type.equals("SEQ_NO33"))
					ret = ADDR;
				if (ret.equals("BARCODE"))
					ret = ADDR;
				ret = Util698.leftPaddingZero(ret, len * 2);

			}
		} else if (type.equals("APNNAME")) {
			ret = Util698.leftPaddingZero(ADDR, len * 2);

		} else if (type.equals("APNPWD")) {
			ret = Util698.leftPaddingZero(ADDR, len * 2);
//			ret = DataConvert.reverseString(ret);
			// xuky 2017.11.08  国网要求“APN初始密码为A的逆序”  不是字节级别的逆序，是整个字符串的逆序
			ret = Util698.reverseAsciiString(ret);


		} else if (type.equals("VERIFYVER")) {
			// 2017.08.15 ASCII 信息转为HEX
			ret = SoftParameter.getInstance().getParamValByKey(type);
			String str = String.format("%1$-" + len + "s", ret);
			str = DataConvert.string2ASCIIHexString(str, str.length());
			str = SoftVarSingleton.getInstance().getFrame645().formatData(str);
			str = DataConvert.reverseString(str);
			str = Util698.seprateString(str, " ");
			ret = str;
			flag = "2";
		} else {
			ret = SoftParameter.getInstance().getParamValByKey(type);
			if (ret.equals("----")) {
				String msg = "出现了未定义的变量：[" + dealdata + "]";
				Util698.log(Util698.class.getName(), "ERR:" + msg, Debug.LOG_INFO);
				javafxutil.f_alert_informationDialog("操作提示", msg);
				return "";
			}
			ret = Util698.leftPaddingZero(ret, len * 2);
		}

		// (0倒序) (1倒序且加33) (2正序)
		if (flag.equals("0"))
			ret = DataConvert.reverseString(ret);
		else if (flag.equals("1"))
			// xuky 2019.01.11 根据协议规定，芯片ID不倒序
			// xuky 2019.01.11 根据协议规定，22位的ID不倒序
			if (ret.length()==48 || ret.length()==22 ){
				// xuky 2019.01.19  如果类型为SEQ_NO33 表示可以不加33，只是倒序即可
				if (type.equals("SEQ_NO33")){
					ret = DataConvert.reverseString(ret);
				}
				else{
					// flag = 1 , +33处理  不倒序
					ret = SoftVarSingleton.getInstance().getFrame645().formatData(ret);
					ret = DataConvert.reverseString(ret);
				}
			}
			else
				// flag = 1 , 倒序后需要加33处理
				ret = SoftVarSingleton.getInstance().getFrame645().formatData(ret);
		else if (flag.equals("2"))
			// xuky 2017.08.15 ver信息无需做任何处理
			ret = ret;

		return ret;
	}
	public static String verify(String frame, String expect) {
		return verify(frame, expect, null);
	}

	// 判断接收到的报文是否与期望结果匹配
	public static String verify(String frame, String expect, BaseCommLog logInfo) {


		// 68 90 78 56 34 12 00 68 91 08 33 33 34 33 12 34 56 78 90 16
		// 68 907856341200 68 91 08 33 33 34 33 ** ** ** ** ** 16

		// 6890785634120068910833333433123456789016
		// 6890785634120068910833333433**********16

		// FEFEFEFE681234560617206895001216
		// FEFEFEFE68******061720689500**16

		// xuky 2017.11.13 进行时钟误差比较

		// xuky 2018.05.22 进行死循环递归情况处理


		String frame0 = frame, expect0 = expect;

		frame = frame.replaceAll(" ", "");
		expect = expect.replaceAll(" ", "");
		if (frame.equals(expect)) {
			return "成功";
		}
//		String recv = tmp.getRecv();
//		recv = recv.substring(20,26);
//		recv = DataConvert.asciiHex2String(recv);
//		expect = "get12V:substring(18,26);val(5);region(-0.2,0.2);";
		if (expect.startsWith("get12V:")){
			String tmp = expect, tmp1 = "";
			// 数据区间
			int pos1 = DataConvert.String2Int(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(",")));
			int pos2 = DataConvert.String2Int(tmp.substring(tmp.indexOf(",")+1,tmp.indexOf(")")));
			tmp = tmp.substring(tmp.indexOf(")")+1);
			// 基准值
			double val = Double.valueOf(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(")")));
			tmp = tmp.substring(tmp.indexOf(")")+1);
			// 上下浮动偏差
			double v1 = Double.valueOf(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(",")));
			double v2 = Double.valueOf(tmp.substring(tmp.indexOf(",")+1,tmp.indexOf(")")));
			double val1 = val * (1+v1);
			double val2 = val * (1+v2);
			if (frame.length() < pos2){
				logInfo.setSpecialData(frame);
				logInfo.setSpecialRule(expect);
				return "失败";
			}
			double data = Double.valueOf(DataConvert.asciiHex2String(frame.substring(pos1,pos2)))*0.75/100;
			// xuky 2019.02.25 输出解析得到的数据和规则
			if (logInfo != null){
				logInfo.setSpecialData(String.valueOf(data));
				logInfo.setSpecialRule(expect);
			}
			Util698.log(Util698.class.getName(), "verify-get12V data:" +data , Debug.LOG_INFO);
			if (data>=val1 && data<=val2)
				return "成功";
			else
				return "失败";
		}
//		"getTimeErr:substring(28,52);val(3000000);region(-0.5,0.5);";
		if (expect.startsWith("getTimeErr:")){
			String tmp = expect, tmp1 = "";
			// 数据区间
			int pos1 = DataConvert.String2Int(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(",")));
			int pos2 = DataConvert.String2Int(tmp.substring(tmp.indexOf(",")+1,tmp.indexOf(")")));
			tmp = tmp.substring(tmp.indexOf(")")+1);
			// 基准值
			double val = Double.valueOf(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(")")));
			tmp = tmp.substring(tmp.indexOf(")")+1);
			// 上下浮动偏差
			double v1 = Double.valueOf(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(",")));
			double v2 = Double.valueOf(tmp.substring(tmp.indexOf(",")+1,tmp.indexOf(")")));
//			差值 = (XXXXXXXXXXXX (12位标准表脉冲数)  -理论脉冲数(12位)[60*50000 ])/ 理论脉冲数(12位)* 86400
			if (frame.length() < pos2){
				logInfo.setSpecialData(frame);
				logInfo.setSpecialRule(expect +" ~~ data = (data - val)/val * 86400");
				return "失败";
			}
			double data = Double.valueOf(DataConvert.asciiHex2String(frame.substring(pos1,pos2)));
			data = (data - val)/val * 86400;
			// xuky 2019.02.25 输出解析得到的数据和规则
			if (logInfo != null){
				logInfo.setSpecialData(String.valueOf(data));
				logInfo.setSpecialRule(expect +" ~~ data = (data - val)/val * 86400");
			}
			Util698.log(Util698.class.getName(), "verify-getTimeErr data:" +data , Debug.LOG_INFO);
			if (data>=v1 && data<=v2)
				return "成功";
			else
				return "失败";
		}


//		"getDateTime:substring(28,40);val(now);region(-60000,60000);";
		if (expect.startsWith("getDateTime:")){
			String tmp = expect, tmp1 = "";
			// 数据区间
			int pos1 = DataConvert.String2Int(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(",")));
			int pos2 = DataConvert.String2Int(tmp.substring(tmp.indexOf(",")+1,tmp.indexOf(")")));
			tmp = tmp.substring(tmp.indexOf(")")+1);
			// 基准值
			String val = tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(")"));
			tmp = tmp.substring(tmp.indexOf(")")+1);
			// 上下浮动偏差
			double v1 = Double.valueOf(tmp.substring(tmp.indexOf("(")+1,tmp.indexOf(",")));
			double v2 = Double.valueOf(tmp.substring(tmp.indexOf(",")+1,tmp.indexOf(")")));
//			差值 = (XXXXXXXXXXXX (12位标准表脉冲数)  -理论脉冲数(12位)[60*50000 ])/ 理论脉冲数(12位)* 86400
			if (frame.length() < pos2){
				logInfo.setSpecialData(frame);
				logInfo.setSpecialRule(expect);
				return "失败";
			}
			String data = frame.substring(pos1,pos2);
			//17-11-13 14:29:45
			data = "20"+data.substring(0,2)+"-"+data.substring(2,4)+"-"+data.substring(4,6) +" "+
					data.substring(6,8)+":"+data.substring(8,10)+":"+data.substring(10)	+":000";
			double val_teminal = getMilliSecondBetween_new(data, DateTimeFun.getDateTimeSSS());

			if (logInfo != null){
				logInfo.setSpecialData(String.valueOf(data));
				logInfo.setSpecialRule(expect);
			}
			Util698.log(Util698.class.getName(), "verify-getDateTime data:" +data , Debug.LOG_INFO);


			if (val_teminal>=v1 && val_teminal<=v2)
				return "成功";
			else
				return "失败";

		}

		if (expect.startsWith("[误差")){
			//verify("17-11-13 14:29:45","[误差<5分钟]")  // 测试用例的格式为 [误差<nn分钟]
			Long val_teminal = Math.abs(getMilliSecondBetween_new("20"+frame+":000", DateTimeFun.getDateTimeSSS())/1000/60);
			String val_expect = expect.split("<")[1];
			val_expect = val_expect.substring(0,val_expect.length()-3); // 去掉分钟]以后的数据
			if (Long.parseLong(val_expect) > val_teminal)
				return "成功";
			else
				return "失败";
		}


		// xuky 2017.10.20 不能简单的判断 长度即结束
		if (!expect.endsWith("%"))
			if (frame.length() != expect.length()){
				// xuky 2018.05.16 出现比较特殊的数据 CS恰好是16，此时的数据接收程序会导致不合理的数据分隔
				// 发送的是6899092515013768910833333433333333331616
				// 收到的是68990925150137689108333334333333333316
				if (frame.length() == expect.length()-2){
					frame = frame+"16";
					return verify(frame,expect);
				}
				else
					return "失败";
			}

		int pos1 = 0;
		int pos2 = expect.indexOf("*");
		while (pos2 >= 0) {
			String part1_1 = frame.substring(pos1, pos2);
			String part1_2 = expect.substring(pos1, pos2);
			if (!part1_1.equals(part1_2)) {
				// xuky 2018.05.16 出现比较特殊的数据 CS恰好是16，此时的数据接收程序会导致不合理的数据分隔
				// 发送的是6899092515013768910833333433333333331616
				// 收到的是1668990925150137689108333334333333333316
				if (frame0.substring(0,2).equals("16")){
					frame = frame0.substring(2)+"16";
					return verify(frame,expect0);
				}
				else
					return "失败";
			}
			String str = expect.substring(pos2, pos2 + 1);
			int len = 0;
			while (str.equals("*")) {
				len++;
				str = expect.substring(pos2 + len, pos2 + 1 + len);
			}
			frame = frame.substring(pos2 + len);
			expect = expect.substring(pos2 + len);
			pos2 = expect.indexOf("*");
		}

		if (expect.indexOf("*") < 0) {
			if (expect.indexOf("%") < 0) {
				if (!frame.equals(expect))
					return "失败";
			} else {
				String d = expect.substring(0, expect.indexOf("%"));
				if (!frame.substring(0, d.length()).equals(d))
					return "失败";
			}
		}

		return "成功";
	}

	// 根据当前扫描得到的条码信息，从数据库中检索得到对应的条码信息
	public static String getAddrByBarcode(String barcode) {
		if (barcode.replaceAll(" ", "").equals("")) return "";
		// xuky 2018.03.14 测试前执行两次数据查询操作会影响执行的效率
//		if (true)
//			return barcode;

		// xyky 2017.07.04 针对长条码的处理过程去掉最后一位，保留前面的12位
		int lostBit = DataConvert.String2Int(SoftParameter.getInstance().getParamValByKey("DROPBIT"));
		if (lostBit != 0 && barcode.length() > 12) {
			// return barcode.substring(barcode.length() - 12 - lostBit,
			// barcode.length() - lostBit);
			// xuky 2017.08.07 最后一位是验证码，且需要对照
			barcode = barcode.substring(0, barcode.length() - lostBit);
		}

		if (barcode.length() < 12)
			barcode = Util698.leftPaddingZero(barcode, 12);
		String addr = barcode.substring(barcode.length() - 12);
		IBaseDao<BarCodeAndAddr> iBarCodeAndAddrDao = SoftVarSingleton.getInstance().getiBarCodeAndAddrDao();


		List<BarCodeAndAddr> list = iBarCodeAndAddrDao
				.retrieve("where longBarCodeBegin<='" + barcode + "' and longBarCodeEnd>='" + barcode + "'", "");
		BarCodeAndAddr barCodeAndAddr = null;
		if (list.size() > 0)
			barCodeAndAddr = list.get(0);
		if (barCodeAndAddr != null) {
			String data1 = barCodeAndAddr.getLongBarCodeBegin();
			String part = data1.substring(data1.length() - 12);
			// xuky 2017.08.02 判断设备地址是否为16进制数据
			Long d1 = Long.parseLong(part);
			Long d2 = Long.parseLong(barcode.substring(data1.length() - 12));
			if (Util698.isHexNumber(barCodeAndAddr.getAddrBegin())) {
				Long begin = Util698.hexString2Long(barCodeAndAddr.getAddrBegin().replaceAll(" ", ""));
				addr = Util698.long2HexString(begin + d2 - d1, 12);
			} else {
				Long begin = Long.parseLong(barCodeAndAddr.getAddrBegin());
				addr = String.valueOf(begin + d2 - d1);
				addr = Util698.leftPaddingZero(addr, 12);
			}
			list = null;
			return addr;
		}
		list = null;
		list = iBarCodeAndAddrDao
				.retrieve("where shortBarCodeBegin<='" + barcode + "' and shortBarCodeEnd>='" + barcode + "'", "");
		barCodeAndAddr = null;
		if (list.size() > 0)
			barCodeAndAddr = list.get(0);
		if (barCodeAndAddr != null) {
			String data1 = barCodeAndAddr.getShortBarCodeBegin();
			String part = data1.substring(data1.length() - 12);
			// xuky 2017.08.02 判断设备地址是否为16进制数据
			Long d1 = Long.parseLong(part);
			Long d2 = Long.parseLong(barcode.substring(data1.length() - 12));
			if (Util698.isHexNumber(barCodeAndAddr.getAddrBegin())) {
				Long begin = Util698.hexString2Long(barCodeAndAddr.getAddrBegin());
				addr = Util698.long2HexString(begin + d2 - d1, 12);
			} else {
				Long begin = Long.parseLong(barCodeAndAddr.getAddrBegin());
				addr = String.valueOf(begin + d2 - d1);
				addr = Util698.leftPaddingZero(addr, 12);
			}

		}
		list = null;

		return addr;
	}

	// 判断是否是整数数值
	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {

			return false;
		}
	}

	// 判断是否是整数数值
	public static boolean isNumber(String value) {
		return isInteger(value);
	}

	// 获取URL数据
	public static String getURLData(String urlString) throws Exception {
		try{
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection(); // 打开连接
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8")); // 获取输入流
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			// System.out.println(sb.toString());
			return sb.toString();
		}
		catch(Exception e){
			return "getURLData Exception:"+e.getMessage();
		}
	}

	// 获取URL 时间
	public static String getURLDateTime1() {
		String url = "http://api.k780.com:88/?app=life.time&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
		String ret = "";
		try {
			ret = getURLData(url);
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "getURLDateTime1 Exception:" + e.getMessage(), Debug.LOG_INFO);
			if (e.getMessage().equals("api.k780.com"))
				System.out.println("getURLDateTime err-> 无法连接" + url + "，请检查网络情况！");
			else {
				System.out.println("getURLDateTime err->" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (!ret.equals("")) {
			String[] str = ret.split("datetime_1\":\"");
			if (str != null || str.length >= 1) {
				ret = str[1];
				ret = ret.substring(0, ret.indexOf("\""));
			}
		}
		return ret;
	}

	public static String getURLDateTime() {
		String ret = "";
		String webUrl3 = "http://www.360.cn";// 淘宝
		ret = getWebsiteDatetime(webUrl3);
		if (ret.equals("")) {
			String webUrl5 = "http://www.taobao.com";// 360
			ret = getWebsiteDatetime(webUrl5);
		}
		return ret;
	}

	// http://blog.csdn.net/catoop/article/details/50076879
	// 只需要建立连接就可以了，不需要请求（request）与响应（response），所以比单独写接口效率要高
	private static String getWebsiteDatetime(String webUrl) {
		try {
			URLConnection uc = new URL(webUrl).openConnection();// 生成连接对象
			uc.connect();// 发出连接
			long ld = uc.getDate();// 读取网站日期时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);// 输出北京时间
			return sdf.format(new Date(ld));
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "getWebsiteDatetime Exception:"+e.getMessage(), Debug.LOG_INFO);
			if (e.getMessage().indexOf("www") >= 0 )
				System.out.println("getWebsiteDatetime err:无法连接服务器"+webUrl );
			else
				e.printStackTrace();
		}
		return "";
	}

	// 根据给定的时间修改本地PC时间信息
	public static Boolean setSystemDateTime(String datetime) {
		Boolean ret = true;
		if (datetime == null || datetime.equals("") || datetime.length() != "2017-11-06 10:11:14".length())
			return false;

		String osName = System.getProperty("os.name");
		String cmd = "";
		try {
			if (osName.matches("^(?i)Windows.*$")) {// Window 系统
				// 格式 HH:mm:ss
				String time = datetime.split(" ")[1];
				cmd = "  cmd /c time " + time;
				Process process = Runtime.getRuntime().exec(cmd);
				// 格式：yyyy-MM-dd
				String date = datetime.split(" ")[0];
				cmd = " cmd /c date " + date;
				Runtime.getRuntime().exec(cmd);
				Util698.log(Util698.class.getName(), "修改本地时钟"+datetime, Debug.LOG_INFO);
			}
		} catch (Exception e) {
			Util698.log(Util698.class.getName(), "setSystemDateTime Exception:"+e.getMessage(), Debug.LOG_INFO);
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	private static void testGet() {
		// System.out.println(getURLDateTime());
		// String webUrl1 = "http://www.bjtime.cn";//bjTime
		String webUrl3 = "http://www.taobao.com";// 淘宝
		String webUrl5 = "http://www.360.cn";// 360
		String webUrl6 = "http://www.beijing-time.org";// beijing-time
		String webUrl4 = "http://www.ntsc.ac.cn";// 中国科学院国家授时中心
		String webUrl2 = "http://www.baidu.com";// 百度
		// System.out.println(getWebsiteDatetime(webUrl1) + " [bjtime]");
		System.out.println(getWebsiteDatetime(webUrl3) + " [淘宝]");
		System.out.println(getWebsiteDatetime(webUrl5) + " [360安全卫士]");
		// System.out.println(getWebsiteDatetime(webUrl6) + " [beijing-time]");
		// System.out.println(getWebsiteDatetime(webUrl4) + " [中国科学院国家授时中心]");
		// System.out.println(getWebsiteDatetime(webUrl2) + " [百度]");

	}

	public static String reverseAsciiString(String str) {
		String sTemp = "";
		String aStr = "";
		int num = str.length() / 1;
		for (int i = 0; i < num; i++) {
			sTemp = str.substring(i * 1, (i + 1) * 1);
			aStr = sTemp + aStr;
		}
		return aStr;
	};

	// 返回字符串的右边几位  例如 rightStr("1234",2) --> "34"
	public static String rightStr(String str,int num){
		String ret = "";
		if (str == null)
			str = "";
		if (str.length() <= num)  // 如果长度太大，则返回全部字符串
			return str;
		ret = str.substring(str.length()-num);
		return ret;
	}


	private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

	static public String getDateTimeSSS_new() {
		// xuky 2018.06.06 出现过如下的错误
//		Exception in thread "Thread-62" java.lang.ArrayIndexOutOfBoundsException
//		at java.lang.System.arraycopy(Native Method)
//		at java.text.DigitList.set(Unknown Source)
//		at java.text.DecimalFormat.format(Unknown Source)
//		at java.text.DecimalFormat.format(Unknown Source)
//		at java.text.SimpleDateFormat.zeroPaddingNumber(Unknown Source)
//		at java.text.SimpleDateFormat.subFormat(Unknown Source)
//		at java.text.SimpleDateFormat.format(Unknown Source)
//		at java.text.SimpleDateFormat.format(Unknown Source)
//		at java.text.DateFormat.format(Unknown Source)
//		at util.Util698.getDateTimeSSS_new(Util698.java:1476)
//		at socket.DealSendData.sendDataInThread(DealSendData.java:215)
//		at socket.DealSendData.lambda$0(DealSendData.java:203)
//		at java.lang.Thread.run(Unknown Source)
		String dateStr = "";
		try{
			dateStr = format.format(LocalDateTime.now());
		}
		catch (Exception e){
			Util698.log(Util698.class.getName(), "getDateTimeSSS_new Exception:" + e.getMessage(), Debug.LOG_INFO);
			// xuky 2018.06.06 处理之前出现的异常问题
			Debug.sleep(100);
			getDateTimeSSS_new();
		}
		return dateStr;
	};

	static public Long getMilliSecondBetween_new(String aDatime1, String aDatime2) {
		Long diff = (long) 0;
		try{
			if (aDatime2.equals("") || aDatime2 == "")
				diff = (long)0;
			LocalDateTime d1 = LocalDateTime.parse(aDatime1, format);
			LocalDateTime d2 = LocalDateTime.parse(aDatime2, format);
			diff = Duration.between(d2,  d1).toMillis();
			// xuky 2018.08.03 考虑到前后两个值可能顺序颠倒，所以取值绝对值
			diff = Math.abs(diff);
		}
		catch(Exception e)
		{
			Util698.log(Util698.class.getName(), "getMilliSecondBetween_new Exception:"+e.getMessage(), Debug.LOG_INFO);
		}
		return diff;
	};

	// xuky 2018.04.26 对地址信息进行格式处理。如果不足XX位，则补齐，如果超出XX位，补齐偶数位信息
	static public String FormatAddr(String addr, int charLen){
		if (addr.length() < charLen){
			addr = DataConvert.fillWith0(addr, charLen-addr.length()+2);
		}
		if (addr.length()%2 == 1)
			addr = "0" + addr;
		return addr;
	}

	// 将txt格式的log文件，将其中的有效信息分解到excel文件中  时间+端口+类型+log内容
	static public void AnalyLog(){

	}

	static public void ResetApp(){
		Util698.log(Util698.class.getName(), "RestApp 重启前停止DealSendData & block", Debug.LOG_INFO);
		DealSendData.getInstance().setIsRuning(false);
		DealSendBlockData.getInstance().setIsRuning(false);

        try {
            // 关闭所有的串口
            PrefixMain.getInstance().closeSerial();
        } catch (Exception e) {
			Util698.log(Util698.class.getName(), "RestApp Exception:"+e.getMessage(), Debug.LOG_INFO);
        } // 执行命令
        finally{

        	try{
                // 启动执行新的程序
                String cmd = "ProduceWare.exe " + SoftParameter.getInstance().getUserManager().getUserid() + " "
                        + SoftParameter.getInstance().getUserManager().getUserpwd(); // 关闭软件后，自动重新执行
                Runtime rt = Runtime.getRuntime(); // 获取运行时系统
    			Util698.log(Util698.class.getName(), "RestApp rt.exec:"+cmd, Debug.LOG_INFO);
                rt.exec(cmd);
                // Process proc = rt.exec(cmd);

            	// xuky 2018.08.06 之前出现过异常  导致后续的代码没有继续执行  使用finally的方式进行尝试
    //java.lang.NoClassDefFoundError: UnsupportedCommOperationException
//            	...
//            	at org.apache.mina.core.session.AbstractIoSession.closeNow(AbstractIoSession.java:357)
//            	at mina.MinaSerialServer.disConnect(MinaSerialServer.java:95)
        		Util698.log(Util698.class.getName(), "RestApp 等待2000", Debug.LOG_INFO);
                Debug.sleep(2000);
        		Util698.log(Util698.class.getName(), "关闭软件4 sleep-2000", Debug.LOG_INFO);
                Platform.exit();
                System.exit(0);
        	}
        	catch (Exception e1){
    			Util698.log(Util698.class.getName(), "RestApp Exception1:"+e1.getMessage(), Debug.LOG_INFO);

        	}
        }

	}

	private Object[] resize(Object[] object_array,int max){
	    //将大小为N<MAX的栈移动到大小为MAX的数组中
		Object[] temp = new Object[max];
	    for(int i = 0;i < object_array.length;i++){
	        temp[i] = object_array[i];
	    }
	    return temp;
	}

	static public boolean isConnect(String pingAddr){

        boolean connect = false;
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec("ping " + pingAddr);
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line+"\r");
            }
            Util698.log(Util698.class.getName(), "ping "+pingAddr+" recv:"+sb, Debug.LOG_INFO);
            is.close();
            isr.close();
            br.close();

            if (null != sb && !sb.toString().equals("")) {
                String logString = "";
                if (sb.toString().indexOf("TTL") > 0) {
                    // 网络畅通
                    connect = true;
                } else {
                    // 网络不畅通
                    connect = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connect;
    }

	// xuky 2018.11.14 得到一个字符串中子串的个数
	public static int numOfStr(String data,String str){
		int ret = 0;
		String tmp = data, now = "";
		int pos = tmp.indexOf(str);
		while (pos >= 0){
			ret ++;
			if (pos == 0)
				now = "";
			else
				now = tmp.substring(0,pos);
			// 如果中间出现空数据，则不计算个数
			if (now.equals(""))
				ret --;
			tmp = tmp.substring(pos+1);
			pos = tmp.indexOf(str);
		}
		return ret;
	}

	public static byte[] String2ByteArray(String data){
		byte[] byteData = new byte[data.length() / 2];
		// 将16进制字符串转为Byte数组
		byteData = DataConvert.hexString2ByteArray(data);
		return byteData;
	}

	// xuky 2018.11.29 判断是否是一个完整的报文
	public static Boolean isCompleteFrame(String recvData, String comID){
		try{
			return isCompleteFrame1(recvData,comID );
		}
		catch(Exception e){
			Util698.log(Util698.class.getName(), "isCompleteFrame:" + e.getMessage(), Debug.LOG_INFO);
			Util698.log(Util698.class.getName(), "isCompleteFrame解析:" + recvData+"出现意外数据！", Debug.LOG_INFO);
			return false;
		}
	}

	public static Boolean isCompleteFrame1(String recvData, String comID){

		// xuky 2018.12.22 统一在这里进行数据保存，而不是在后面一个个的保存，之前的处理isCompleteFrame5和isCompleteFrame56的时候，没有进行保存，导致出现不易发现的错误
		SoftParameter.getInstance().getRecvDataMap().put(comID,recvData);

		// xuky 2019.01.09 盛迪台体升源、降源协议
		// 5个F9 开头，总长度为46字节
		//F9 F9 F9 F9 F9 B1 10 00 02 00 10 20 13 88 55 F0 55 F0 55 F0 00 00 27 10 00 00 27 10 00 00 27 10 00 00 00 00 00 00 2E E0 5D C0 00 00 FE C7
		if (recvData.startsWith("F9F9F9F9F9") && (recvData.length() == 92 || recvData.length() == 26) )
			return true;

		if (recvData.equals("B110000200107A35") || recvData.equals("B103021603B7FF")){
			return true;
		}
		if ((recvData.startsWith("4D") || recvData.startsWith("49") ||recvData.startsWith("55")) && recvData.endsWith("3B")){
			return true;
		}

		// xuky 2019.02.14 盛迪台体
		//01H+地址(A——Z)+ 长度+06H(肯定)/15H(否定)+校验位+结束(17H)
//		FEFEFEFEFE010106060617
		if (recvData.startsWith("FEFEFEFEFE01")){
//			&& recvData.length() == 92)
			// FEFEFEFEFE0143173030303036303030303030333030303030366F17
			// xuky 2019.02.28 收到FEFEFEFEFE014317，不能认为结束
			if (recvData.length() < 16)
				return false;
			int len  = DataConvert.hexString2Int(recvData.substring(14,16));
			len = len * 2 + 10;
			if (recvData.length() < len)
				return false;
			if (recvData.endsWith("17"))
				return true;
			else
				return false;
		}

		if (!recvData.substring(recvData.length() - 2).equals("16")) {
//			Util698.log(Util698.class.getName(), "isCompleteFrame1 return flase:" + recvData, Debug.LOG_INFO);
			return false;
		} else {
			// 首先进行698.45协议报文的判断
			if (recvData.length() >= 4){
				if (recvData.substring(0, 2).equals("68")){
					int len = DataConvert.hexString2Int(recvData.substring(2, 4));
					if (recvData.length() == len *2 + 4)
//						System.out.println("checkData => 698.45 :"+recvData);
						return true;
				}

			}

			// 68 17 00 43 45 AA AA AA AA AA AA 00 5B 4F 05 01 01 40 01 02 00 00 C6 07 16
			if (recvData.indexOf("AAAAAAAAAAAA") >= 0  && recvData.indexOf("6817004345") >= 0){
				Util698.log(Util698.class.getName(), "收到特殊协议:" + recvData, Debug.LOG_INFO);
				return true;
			}

			int pos1 = recvData.indexOf("68");
			int pos2 = recvData.indexOf("68", pos1+1);
//			System.out.println("68 pos ->" + (pos2-pos1) );

			// xuky 2017.07.19 出现了设备地址中有16的情况
			// 645报文中应该有两处68和最后的16
			if (pos1 < 0 || pos2 < 0  ){
				if (pos1>=0){
					// xuky 2018.12.11 需要进行376.2协议报文的判断
					// int pos1 = recvData.indexOf("68");
					String str = recvData.substring(pos1+2,pos1+4);
					int len1 = DataConvert.hexString2Int(str)*2;
					len1 = pos1 + len1;
					// xuky 2019.02.18 出现FEFE6816数据，导致执行异常
					if (recvData.length() < len1){
						Util698.log(Util698.class.getName(), "isCompleteFrame-L1 return flase:" + recvData, Debug.LOG_INFO);
						return false;
					}
					// xuky 2019.02.26 出现680000810116数据，导致执行异常
					if (len1 == 0){
						Util698.log(Util698.class.getName(), "isCompleteFrame-L2 return flase:" + recvData, Debug.LOG_INFO);
						return false;
					}
					str = recvData.substring(len1-2,len1);
					if (str.equals("16"))
						return true;
				}
				Util698.log(Util698.class.getName(), "isCompleteFrame2 return flase:" + recvData, Debug.LOG_INFO);
				return false;
			}

			// xuky 2017.09.20 出现了设备地址中有68的情况 000000611668
			// FE FE FE FE 68 68 16 61 00 00 00 68 95 00 11 16
			// FE FE FE FE 68 68 16
			if (pos2 - pos1 < 14 ){
				// 判断两个68之间的字符个数
				int pos3 = recvData.indexOf("68", pos2+1);
				if (pos3 < 0){
					Util698.log(Util698.class.getName(), "isCompleteFrame3 return flase:" + recvData, Debug.LOG_INFO);
					return false;
				}
				else{
					if (pos3 - pos1 < 14 ){
						int pos4 = recvData.indexOf("68", pos3+1);
						if (pos4 < 0){
							Util698.log(Util698.class.getName(), "isCompleteFrame4 return flase:" + recvData, Debug.LOG_INFO);
							return false;
						}
					}
				}
			}
			// xuky 2018.11.03 出现16H判断异常的情况
			if (pos2 - pos1 == 14 ){
				// xuky 2018.11.12 添加特殊自定义645报文的长度判断
				// 6899999999999968140054FFFFEE01000101020002011801029C01C1FB0245534131000001ACBF092E43B981B1E2480206112233445566030001011801029C01C1FB0245534131000001AD4E60A617726EA74BD1040001020A0102030405060708090A3316
				Boolean is_newFormat = false;
				if (recvData.startsWith("689999999999996814")){
					if (recvData.indexOf("FFFFEE01") >= 0)
						is_newFormat = true;
				}
				if (!is_newFormat){
					String str = recvData.substring(pos2+4,pos2+6);
					int len = DataConvert.hexString2Int(str)*2;
					len = len + 0;
					// xuky 2019.02.19 substring前进行必要的判断，以免出现异常数据
					if (recvData.length() < pos2+6){
						Util698.log(Util698.class.getName(), "isCompleteFrame-L2 return flase:" + recvData, Debug.LOG_INFO);
						return false;
					}
					str = recvData.substring(pos2+6);
					if (str.length() < len){
						Util698.log(Util698.class.getName(), "isCompleteFrame5 return flase:" + recvData, Debug.LOG_INFO);
						return false;
					}
					// 进行最后一个16的判断
					// 68056145520700689F1D878687634B3435CF34F42E357886746433333516472DB902DEE91B16
					// 68546045520700689F1D878687634B3435CF34F42E357886746433333516FB55E76F51BC7B9D3B7016
				}
				else{
					// xuky 2019.02.19 substring前进行必要的判断，以免出现异常数据
					if (recvData.length() < pos2+8){
						Util698.log(Util698.class.getName(), "isCompleteFrame-L2 return flase:" + recvData, Debug.LOG_INFO);
						return false;
					}

					String str = recvData.substring(pos2+4,pos2+8);
					int len = DataConvert.hexString2Int(str)*2;
					str = recvData.substring(pos2+8);
					len = len + 8+4;
					if (str.length() != len){
						Util698.log(Util698.class.getName(), "isCompleteFrame6 return flase:" + recvData, Debug.LOG_INFO);
						return false;
					}
				}
			}
		}
//		System.out.println("checkData => 645 :"+recvData);
		return true;
	}

	// xuky 2018.12.04 动态添加数据内容，添加为任意类型
	public static Object[] addArrayMaxPos(Object[] data,Object addData) {
		return addArrayMaxPos(data,addData,Object.class);
	}

	// xuky 2018.12.04 动态添加数据内容
	public static Object[] addArrayMaxPos(Object[] data,Object addData,Class componentType) {
		int length = 0;
		if (data != null)
			length= data.length;
		Object[] ret = (Object[]) Array.newInstance(componentType, length+1);
		if (data != null)
			System.arraycopy(data, 0, ret, 0, data.length);
		ret[length] =  addData;
		return ret;
	}


	// xuky 2018.12.11 去掉字符串前面的一些无用数据
	public static String trimFronStr(String data,String str){
		String data0 = data;
		data = data.replaceAll(" ", "");
		while (data.substring(0, 2).equals(str)){
			data = data.substring(2);
		}
		data0 = data0+"";
		return data;
	}

	public static String StrIP2HEX(String IP){
		String ret = "";
		ret += DataConvert.String2HexString(IP.split("\\.")[0],2);
		ret += DataConvert.String2HexString(IP.split("\\.")[1],2);
		ret += DataConvert.String2HexString(IP.split("\\.")[2],2);
		ret += DataConvert.String2HexString(IP.split("\\.")[3],2);
		return ret;
	}

	// xuky 2018.12.27 获取当前系统的所有串口名称
	public static List<String> getSrialNames(Boolean revers){
		CommPortIdentifier portId;
		List<String> serialNameList=new ArrayList<>();
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL){
				serialNameList.add(portId.getName());
			}
		}
		return serialNameList;
	}

	// 参考 https://blog.csdn.net/fulunyong/article/details/80118432
	public static String Double2String(double data){
		String ret = "0";
//		DecimalFormat decimalFormat = new DecimalFormat("###################");
		DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
	    ret = decimalFormat.format(data);
	    return ret;
	}

	// 验证MAC是否为本系统产生的 023A19020001
	public static boolean is_validMAC(String mac){
		boolean ret = false;
		if (mac.substring(0,2).equals("02")){
			int yy = DataConvert.String2Int(mac.substring(4,6));
			int mm = DataConvert.String2Int(mac.substring(6,8));
			if (yy>=18 && yy <=28)
				if (mm >=0 && mm <=12 )
					return true;
		}
		return ret;
	}

	public static void main(String[] arg) {

		// 验证MAC是否为本系统产生的 023A19020001
		System.out.println(is_validMAC("023A19020001"));

//		String sendData = Util698.getDateTimeSSS_new();
//		//yyyy-MM-dd HH:mm:ss:SSS
//		sendData = sendData.replaceAll("-", "");
//		sendData = sendData.replaceAll(":", "");
//		sendData = sendData.replaceAll(" ", "");
//		sendData = sendData.substring(2);
//		sendData = sendData.substring(0,sendData.length()-3);
//		sendData = "";
//
//		SimuRun.getTermianlFrame("129.1.22.11", "94", "", "00");
//
//		String IP = PlatFormParam.getInstance().getGPRS_IP();
//		String port = PlatFormParam.getInstance().getGPRS_Port();
//		sendData = DataConvert.int2HexString(DataConvert.String2Int(port), 4);
//		String sendData1 = Util698.StrIP2HEX(IP) + sendData;
//		sendData1 += "";
//		String sendData = Util698.getDateTimeSSS_new();
//		sendData = sendData.replaceAll("-", "");
//		sendData = sendData.replaceAll(":", "");
//		sendData = sendData.replaceAll(" ", "");
//		sendData = sendData.substring(2);
//		sendData = sendData.substring(0,sendData.length()-3);
//		String sData = SimuRun.getTermianlFrame("129.1.22.12", "14", "04 96 96 04", sendData);
//		Util698.verify(sData,"getDateTime:substring(28,40);val(now);region(-60000,60000);");

//		Util698.verify("FEFEFEFEFE014317","getTimeErr:substring(28,52);val(3000000);region(-0.5,0.5);");


//		String str = "FEFEFEFEFE0143173030303036303030303030333030303030366F17";
		String str = "680000C0A87F6068940E04969610C0A87F60023A190200019816";
		String mac = str.substring(36,48);
		mac += "";


		System.out.println(str);
		System.out.println(Util698.isCompleteFrame(str, "11"));

		str = "FEFEFEFEFE0143173030303036303030303030333030303030366F17";
		System.out.println(str);
		System.out.println(Util698.isCompleteFrame(str, "11"));

		str = "FEFEFEFEFE0143173030303036303030303030333030303030366F";
		System.out.println(str);
		System.out.println(Util698.isCompleteFrame(str, "11"));

	}

}
