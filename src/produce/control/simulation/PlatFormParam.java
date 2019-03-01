package produce.control.simulation;

import java.io.IOException;

import com.eastsoft.fio.FileToRead;
import com.eastsoft.fio.FileToWrite;
import com.google.gson.Gson;

public class PlatFormParam {

	// 单例模式：静态变量 uniqueInstance 类的唯一实例
	private volatile static PlatFormParam uniqueInstance;
	public static PlatFormParam getInstance() {
		if (uniqueInstance == null) {
			synchronized (PlatFormParam.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					String str = FileToRead.readLocalFile1("arc\\PlatFormParam.json");
					uniqueInstance = new Gson().fromJson(str, PlatFormParam.class);
				}
			}
		}
		return uniqueInstance;
	}
	public static PlatFormParam getInstance(String str) {
		if (uniqueInstance == null) {
			synchronized (PlatFormParam.class) {
				if (uniqueInstance == null)
					uniqueInstance = new Gson().fromJson(str, PlatFormParam.class);
			}
		}
		return uniqueInstance;
	}

	private PlatFormParam() {
	}

	public synchronized String saveParam() {
		String str = getParamString();
		FileToWrite.writeLocalFile1("arc\\PlatFormParam.json", str);
		return str;
	}

	public String getParamString() {
		// xuky 2018.03.14 CaseList无需进行记录
//		uniqueInstance.CaseList = null;
//		uniqueInstance.recvDataMap = new HashMap<String, String>();
	return new Gson().toJson(uniqueInstance);
	}

	// xuky 2014.09.10 调整为需要接收传入的字符串参数
	public void refresh(String str) {
		uniqueInstance = null;
		getInstance(str);
	}


	public static void main(String[] args) throws IOException {
		PlatFormParam platformParam = new PlatFormParam();
		uniqueInstance = platformParam;
		platformParam.saveParam();
	}

	// 建议调整为枚举类型，进行数据选择
	private String PlatFormNO = "01";  // 台体的编号信息
	private String PlatFormCOM = "50";  // 台体的串口地址
	private String TerminalModel = "国网";  // 集中器模式(南网、国网)
	private String FSNum = "6";  // 遥信端子数量，含门节点
	private String ESAMCheckFlag = "1:需要";  // 1表示，需要进行ESAM的测试0:不需要","1:需要
	private String ESAMModel = "1:1376.1";  // ESAM类型(.1、2-698)
	private String RT_MeterAddr = "";  // 路由测试用-表地址（自动填充为6字节）
	private String RT_Protecol = "00:国网376.2";  // 路由测试用-协议类型( 00-国网376.2、01-广东16、02-福建扩展)
	private String RT_AddFalg = "00:不加表";  // 路由测试用-加表标志（00-不加表、01-加表）
	private String RT_JoinTime = "0";  // 路由测试用-组网时间(秒)
	private String RT_LeftTime = "0";  // 路由测试用-离网时间(秒)
	private String RT_ReadType = "00:AFN13_F1";  // 路由测试用-抄表方式(00-AFN13_F1、01-AFN02_F1)
	private String RT_AddType = "00:1376.2";  // 路由测试用-加表方式(00-1376.2、01-376.2)
	private String GPRS_IP = "000.000.000.000";  // GPRS检测用主站IP地址
	private String GPRS_Port = "0000";  // GPRS检测用主站端口地址
	private int RUNID = 0;  // 当前测试的ID信息
	private String RT_MeterAddr_NW = "";  // 路由测试用-表地址（自动填充为6字节） 南网用

	public String getTerminalModel() {
		return TerminalModel;
	}
	public void setTerminalModel(String terminalModel) {
		TerminalModel = terminalModel;
	}
	public String getFSNum() {
		return FSNum;
	}
	public void setFSNum(String fSNum) {
		FSNum = fSNum;
	}
	public String getESAMModel() {
		return ESAMModel;
	}
	public void setESAMModel(String eSAMModel) {
		ESAMModel = eSAMModel;
	}
	public String getRT_MeterAddr() {
		return RT_MeterAddr;
	}
	public void setRT_MeterAddr(String rT_MeterAddr) {
		RT_MeterAddr = rT_MeterAddr;
	}
	public String getRT_Protecol() {
		return RT_Protecol;
	}
	public void setRT_Protecol(String rT_Protecol) {
		RT_Protecol = rT_Protecol;
	}
	public String getRT_AddFalg() {
		return RT_AddFalg;
	}
	public void setRT_AddFalg(String rT_AddFalg) {
		RT_AddFalg = rT_AddFalg;
	}
	public String getRT_JoinTime() {
		return RT_JoinTime;
	}
	public void setRT_JoinTime(String rT_JoinTime) {
		RT_JoinTime = rT_JoinTime;
	}
	public String getRT_LeftTime() {
		return RT_LeftTime;
	}
	public void setRT_LeftTime(String rT_LeftTime) {
		RT_LeftTime = rT_LeftTime;
	}
	public String getRT_ReadType() {
		return RT_ReadType;
	}
	public void setRT_ReadType(String rT_ReadType) {
		RT_ReadType = rT_ReadType;
	}
	public String getRT_AddType() {
		return RT_AddType;
	}
	public void setRT_AddType(String rT_AddType) {
		RT_AddType = rT_AddType;
	}
	public String getGPRS_IP() {
		return GPRS_IP;
	}
	public void setGPRS_IP(String gPRS_IP) {
		GPRS_IP = gPRS_IP;
	}
	public String getGPRS_Port() {
		return GPRS_Port;
	}
	public void setGPRS_Port(String gPRS_Port) {
		GPRS_Port = gPRS_Port;
	}
	public String getPlatFormCOM() {
		return PlatFormCOM;
	}
	public void setPlatFormCOM(String platFormCOM) {
		PlatFormCOM = platFormCOM;
	}
	public String getPlatFormNO() {
		return PlatFormNO;
	}
	public void setPlatFormNO(String platFormNO) {
		PlatFormNO = platFormNO;
	}
	public String getESAMCheckFlag() {
		return ESAMCheckFlag;
	}
	public void setESAMCheckFlag(String eSAMCheckFlag) {
		ESAMCheckFlag = eSAMCheckFlag;
	}
	public int getRUNID() {
		return RUNID;
	}
	public void setRUNID(int rUNID) {
		RUNID = rUNID;
	}
	public String getRT_MeterAddr_NW() {
		return RT_MeterAddr_NW;
	}
	public void setRT_MeterAddr_NW(String rT_MeterAddr_NW) {
		RT_MeterAddr_NW = rT_MeterAddr_NW;
	}

}
