package produce.control.simulation;

import java.io.IOException;

import com.eastsoft.fio.FileToRead;
import com.eastsoft.fio.FileToWrite;
import com.google.gson.Gson;

public class PlatFormParam {

	// ����ģʽ����̬���� uniqueInstance ���Ψһʵ��
	private volatile static PlatFormParam uniqueInstance;
	public static PlatFormParam getInstance() {
		if (uniqueInstance == null) {
			synchronized (PlatFormParam.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
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
		// xuky 2018.03.14 CaseList������м�¼
//		uniqueInstance.CaseList = null;
//		uniqueInstance.recvDataMap = new HashMap<String, String>();
	return new Gson().toJson(uniqueInstance);
	}

	// xuky 2014.09.10 ����Ϊ��Ҫ���մ�����ַ�������
	public void refresh(String str) {
		uniqueInstance = null;
		getInstance(str);
	}


	public static void main(String[] args) throws IOException {
		PlatFormParam platformParam = new PlatFormParam();
		uniqueInstance = platformParam;
		platformParam.saveParam();
	}

	// �������Ϊö�����ͣ���������ѡ��
	private String PlatFormNO = "01";  // ̨��ı����Ϣ
	private String PlatFormCOM = "50";  // ̨��Ĵ��ڵ�ַ
	private String TerminalModel = "����";  // ������ģʽ(����������)
	private String FSNum = "6";  // ң�Ŷ������������Žڵ�
	private String ESAMCheckFlag = "1:��Ҫ";  // 1��ʾ����Ҫ����ESAM�Ĳ���0:����Ҫ","1:��Ҫ
	private String ESAMModel = "1:1376.1";  // ESAM����(.1��2-698)
	private String RT_MeterAddr = "";  // ·�ɲ�����-���ַ���Զ����Ϊ6�ֽڣ�
	private String RT_Protecol = "00:����376.2";  // ·�ɲ�����-Э������( 00-����376.2��01-�㶫16��02-������չ)
	private String RT_AddFalg = "00:���ӱ�";  // ·�ɲ�����-�ӱ��־��00-���ӱ�01-�ӱ�
	private String RT_JoinTime = "0";  // ·�ɲ�����-����ʱ��(��)
	private String RT_LeftTime = "0";  // ·�ɲ�����-����ʱ��(��)
	private String RT_ReadType = "00:AFN13_F1";  // ·�ɲ�����-����ʽ(00-AFN13_F1��01-AFN02_F1)
	private String RT_AddType = "00:1376.2";  // ·�ɲ�����-�ӱ�ʽ(00-1376.2��01-376.2)
	private String GPRS_IP = "000.000.000.000";  // GPRS�������վIP��ַ
	private String GPRS_Port = "0000";  // GPRS�������վ�˿ڵ�ַ
	private int RUNID = 0;  // ��ǰ���Ե�ID��Ϣ
	private String RT_MeterAddr_NW = "";  // ·�ɲ�����-���ַ���Զ����Ϊ6�ֽڣ� ������

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
