package entity;

import com.eastsoft.util.DataConvert;

import base.CommonObjectList;
import util.Util698;
import util.Util698.NewObjAction;

/**
 * 电表类.
 *
 * @author xuky
 * @version 2013.10.24
 */
public class Meter implements Comparable<Meter> {

	private int ID;    // 主键信息
	private String archiveType; // 档案类型
	private String flag; // 删除标志
	private String optTime; // 信息操作时间

	private String terminalID; // 终端ID
	private String meterCode; // 电表通信地址    // 重要字段
	private String measureNo; // 测量点号   配置序号
	private String collectCode; // 采集器地址
	private String protocolType; // 电表协议  规约类型

	private String meterType; // 电表类型 01:单相电子表，02 机械表,03:简易多功能表,08:多功能总表
	private String usrMeterType; // 用户表、总表标志

	private String port; // 端口号  端口
	private String type1; // 大类号、小类号  用户类型
	private String type2; // 小类号  接线方式
	private String fee; // 费率号  费率个数

	private String addr; // 电表所在位置
	private String gisInfo; // 电表地理位置经纬度坐标
	private String note; // 备注信息

	private String portRate; // 波特率
	private String pwd;// 通信密码
	private String ratedVoltage;// 额定电压
	private String ratedCurrent;// 额定电流
	private String assetsNo;// 资产号
	private String PT;// PT
	private String CT;// CT

	// exportAttributes.length就是需要导出数据的个数

	public Meter() {
		protocolType = "DL/T698.45(3)";
		port = "载波/微功率(F2090201)";
		type1 = "1";
		type2 = "单相(1)";
		meterType = "单相电子表";
		usrMeterType = "分";
		fee = "4";
		portRate = "2400bps(3)";
		PT = "1";
		CT = "1";
		pwd = "000000000000";
		terminalID = "1";
		archiveType = "1";
		ratedVoltage = "0";
		ratedCurrent = "0";
		collectCode = "000000000000";
	}

	public void setProtocolType(String data) {
		protocolType = Util698.getCodeData(data,"protocolType");
	}

	public void setPort(String data) {
		port = Util698.getCodeData(data,"port");
	}

	public void setType2(String data) {
		type2 = Util698.getCodeData(data,"type2");
	}

	public void setPortRate(String data) {
		portRate = Util698.getCodeData(data,"portRate");;
	}

	public void setType1(String type1) {
		this.type1 = type1;
	}

	public void setMeterType(String meterType) {
		this.meterType = meterType;
	}

	public void setUsrMeterType(String usrMeterType) {
		this.usrMeterType = usrMeterType;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}

	public String getTerminalID() {
		return terminalID;
	}

	public void setTerminalID(String terminalID) {
		this.terminalID = terminalID;
	}

	public String getMeterCode() {
		return meterCode;
	}

	public void setMeterCode(String meterCode) {
		// 对电表地址进行格式化处理，一定是12位，前面填充0
//		this.meterCode = DataConvert.fillWith0(meterCode, 12);
		// xuky 2017.03.09 根据用户录入的格式
		this.meterCode = meterCode;
	}

	public String getMeasureNo() {
		return measureNo;
	}

	public void setMeasureNo(String measureNo) {
		// 对测量点号进行格式化处理，一定是4位，前面填充0
		this.measureNo = DataConvert.fillWith0(measureNo, 4);
	}

	public String getCollectCode() {
		return collectCode;
	}

	public void setCollectCode(String collectCode) {
//		this.collectCode = DataConvert.fillWith0(collectCode, 12);
		this.collectCode = collectCode;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getGisInfo() {
		return gisInfo;
	}

	public void setGisInfo(String gisInfo) {
		this.gisInfo = gisInfo;
	}

	public String getArchiveType() {
		return archiveType;
	}

	public void setArchiveType(String archiveType) {
		this.archiveType = archiveType;
	}

	public String getOptTime() {
		return optTime;
	}

	public void setOptTime(String optTime) {
		this.optTime = optTime;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getUsrMeterType() {
		return usrMeterType;
	}

	public String getMeterType() {
		return meterType;
	}

	@Override
	public int compareTo(Meter o) {
		// 参考http://www.cnblogs.com/wentiertong/archive/2011/03/07/1973698.html
		// Java中List的排序
		Integer i, j;
		i = DataConvert.String2Int(this.getMeasureNo());
		j = DataConvert.String2Int(o.getMeasureNo());
		return i.compareTo(j);
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getPort() {
		return port;
	}

	public String getType1() {
		return type1;
	}

	public String getType2() {
		return type2;
	}

	public String getFee() {
		return fee;
	}

	public String getPortRate() {
		return portRate;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getRatedVoltage() {
		return ratedVoltage;
	}

	public void setRatedVoltage(String ratedVoltage) {
		this.ratedVoltage = ratedVoltage;
	}

	public String getRatedCurrent() {
		return ratedCurrent;
	}

	public void setRatedCurrent(String ratedCurrent) {
		this.ratedCurrent = ratedCurrent;
	}

	public String getAssetsNo() {
		return assetsNo;
	}

	public void setAssetsNo(String assetsNo) {
		this.assetsNo = assetsNo;
	}

	public String getPT() {
		return PT;
	}

	public void setPT(String pT) {
		PT = pT;
	}

	public String getCT() {
		return CT;
	}

	public void setCT(String cT) {
		CT = cT;
	}

	static public CommonObjectList<Meter> getList(String where, String order){
		// ------------meterList的初始化过程---------begin-------------
		NewObjAction newobj_act = new NewObjAction() {
			public Object getNewObject() {
				return new Meter();
			}
		};
//		CommonObjectList<Meter> meterList;
		// 指定0创建 对象、1表名、2主键、3where条件、4排序
		CommonObjectList<Meter> retList = new CommonObjectList<Meter>(newobj_act, "meter", "meterCode", where,order);
		// 指定导出的字段内容
		String export_columns = "measureNo,meterCode,portRate,protocolType,port,pwd,fee,type1,type2,ratedVoltage,ratedCurrent,collectCode,assetsNo,PT,CT";
		retList.setExportColmuns(export_columns);
		// 指定导出的字段标题
		String[] colNames_info1 = { "配置序号", "通信地址", "波特率;code:PortRate", "规约类型;code:ProtocolType", "端口;code:Port",
				"通信密码", "费率个数", "用户类型", "接线方式;code:Type2", "额定电压", "额定电流", "采集器地址", "资产号", "PT", "CT" };
		retList.setExportColmunNames(colNames_info1);
		// 指定导出的文件名称
		retList.setExportFileName("电表档案.xls");
		// ------------meterList的初始化过程---------end-------------
		return retList;
	}

	public static void main(String[] args) {
//		String s = "123456";
//
//		System.out.println(DataConvert.reverseString(DataConvert.reverseString(s)));
		 Meter meter = new  Meter();
		 String data = "3";
		 meter.setProtocolType(data);
		 System.out.println(meter.getProtocolType());
		 data = "DL/T645-2007(2)";
		 meter.setProtocolType(data);
		 System.out.println(meter.getProtocolType());
	}

}
