package util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eastsoft.fio.FileToRead;
import com.eastsoft.fio.FileToWrite;
import com.eastsoft.util.Debug;
import com.google.gson.Gson;

import dao.basedao.IBaseDao;
import produce.entity.ProduceCase;
import produce.entity.ProduceParam;
import produce.entity.ProduceParamDaoImpl;
import produce.entity.UserManager;

/**
 * �������.
 * <p>
 *
 * @author xuky
 * @version 2016.09.18
 */
public class SoftParameter {

	// xuky 2018.03.14 ���ִ��Ч�� ȥ�������չʾ����
	Boolean RUNFASTER = false;

	// xuky 2018.12.11 II�ɳ�����������ñ�ģ�鼰·��ID�Ĺ��ܣ���ɨ��õ������뱣��ԭ����
	// ��DevInfo���н�������
	String IS_SETID = "";

	static List<ProduceParam> produceParamList = null;

	// �ϰ��������
	Map<String, String> recvDataMap = new HashMap<String, String>();
	Map<String, String> CompleteDataMap = new HashMap<String, String>();

	String CaseListID = "";
	List<ProduceCase> CaseList = null;

	private String PCID;

	// �������͵�ѡ�����
	private String DBTYPE = "netdb"; // netdb2-cfg_PostgreSQL;netdb-cfg_MySql;else-cfg_SQLite
//	private String DBTYPE = "filedb";

	// ǰ�û������˿�
	private String prefix_ip = "";
	private int prefix_port = 20001;
	private int prefix_port_new = 12345;

	private String UDPCLIENT_IP = "129.1.22.95";
	private int UDPCLIENT_PORT = 9000;

	private String UDPSVR_IP = "129.1.22.90";
	private int UDPSVR_PORT = 9000;

	private String TERMINAL_IP = "129.1.22.96";

	private String LOG_Level = "0";  // �Ƿ��¼��ϸ��־  0����¼�������ճ����� 1��¼�����ڵ��ԣ�

	private String WAIT_TIME = "2500";  // ����ʱ���ز�ͨ�ŵ�Ӱ�쵽���ͺ��2.5���2��

//	private String USER_CONTROL_WAIT = "80000";  // ���в���ʱ���������Թ��̵ĵȴ�����  Ĭ��Ϊ80��

	// xuky 2018.10.25
	private String TESTALL_TIME_OUT = "240000";  // ���ֲ��Եĳ�ʱʱ�� ����������ʱ�䣬�������������  240000 = 80000*3 = 2.4����
	private String WORKID = "Y51-����";  //��������ID

	private String STARTQCODE = "";  // ������ ���յ����������Ժ���Ϊ�Զ���ʼ����ģ��ɨ��

	// Ĭ�ϵ�ͨ���ն˵�ַ
	private String sendTerminal = "000000000002";

	// �Ƿ�Ϊ����ģʽ 0 ��ʾ�Ǵ���ģʽ��1��ʾ����ģʽ
	private String isProxyModel = "0";

	// ������������ʱʱ�� ��λΪ�� 3����
	private int all_timeout = 60 * 3;
	// ������������ʱʱ�� ��λΪ�� 1����
	private int single_timeout = 60 * 1;
	// Ŀ���������ַ�����ʱ��ʹ�ö��Ž��зָ��� ProxyGetRequestRecordʱ��ֻȡ�õ�һ��
	private String targets = "";

	// �������ȼ�
	private String svr_priority = "0";

	// �������
	private String svr_no = "0";

	// ��ǰ���ڽ�������ı���
	private String sendFrame = "";
	private String recvFrame = "";
	private String APDUData = "";

	// ���ıȽ�
	private String compareFrame1 = "";
	private String compareFrame2 = "";

	private UserManager userManager = null;

	// private String ErrBarCode = "";
	private String ERRADDR = ""; // �쳣��ַ
	private String OKADDR = ""; // ������ַ
	// private String CURRENTSUBID = ""; // ��ǰ���Է���
	// private String LOSTBIT = "0"; // ����ĩβ

	private Boolean SERIAL_FINISHED = false;


	private String SENDPLC1 = ""; // ��Ҫ��PLC�ظ�������1
	private String SENDPLC2 = ""; // ��Ҫ��PLC�ظ�������2
	private String RECVCLINET = ""; // �Ƿ��пͻ��˽���������
	private String ObserverOK = ""; // �Ƿ�۲���ģʽ��Ч
	private String PLCFRAME = ""; // �յ���PLC��������


	// ����ģʽ����̬���� uniqueInstance ���Ψһʵ��
	private volatile static SoftParameter uniqueInstance;

	public static SoftParameter getInstance(String str) {
		if (uniqueInstance == null) {
			synchronized (SoftParameter.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new Gson().fromJson(str, SoftParameter.class);
				}
			}
		}
		return uniqueInstance;
	}

	private SoftParameter() {
		// LOSTBIT = "0";

		// serialList = SerialList.getInstance();

		// SerialParam serialParam = new SerialParam();
		// serialParam.setCOMM("COM6");
		// serialList.add(serialParam);
		//
		// serialParam = new SerialParam();
		// serialParam.setCOMM("COM8");
		// serialList.add(serialParam);
	}

	public static SoftParameter getInstance() {
		if (uniqueInstance == null) {
			synchronized (SoftParameter.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					String str = new FileToRead().readLocalFile1("arc\\SoftParameter.json");
					uniqueInstance = new Gson().fromJson(str, SoftParameter.class);
					// xuky 2018.05.10 ʹ���ⲿ�������ļ�
					Util698.InitLog4jConfig();
					uniqueInstance.PCID = Debug.getHdSerialInfo();
					uniqueInstance.CaseListID = "";
					// xuky 2017.07.24 Ϊ����߼��س�����ٶȣ��˶δ������ MainPanel��
//					new Thread(() -> {
//						refreshDataFromDB();
//					}).start();
				}
			}
		}
		return uniqueInstance;
	}



	public int getPrefix_port_new() {
		return prefix_port_new;
	}

	public void setPrefix_port_new(int prefix_port_new) {
		this.prefix_port_new = prefix_port_new;
	}

	// xuky 2017.03.02 ȷ���̰߳�ȫ
	public synchronized String saveParam() {
		String str = getParamString();
		FileToWrite.writeLocalFile1("arc\\SoftParameter.json", str);
		return str;
	}

	public String getParamString() {
		// xuky 2018.03.14 CaseList������м�¼
		uniqueInstance.CaseList = null;
		uniqueInstance.recvDataMap = new HashMap<String, String>();
		uniqueInstance.CompleteDataMap = new HashMap<String, String>();
		return new Gson().toJson(uniqueInstance);
	}

	// xuky 2014.09.10 ����Ϊ��Ҫ���մ�����ַ�������
	public void refresh(String str) {
		uniqueInstance = null;
		getInstance(str);
	}

	public void init() {
		String str = new Gson().toJson(this);
		FileToWrite.writeLocalFile1("arc\\SoftParameter.json", str);
	}

	public int getPrefix_port() {
		return prefix_port;
	}

	public void setPrefix_port(int prefix_port) {
		this.prefix_port = prefix_port;
	}

	public String getSendTerminal() {
		return sendTerminal;
	}

	public synchronized void setSendTerminal(String sendTerminal) {
		this.sendTerminal = sendTerminal;
	}

	public int getAll_timeout() {
		return all_timeout;
	}

	public void setAll_timeout(int all_timeout) {
		this.all_timeout = all_timeout;
	}

	public int getSingle_timeout() {
		return single_timeout;
	}

	public void setSingle_timeout(int single_timeout) {
		this.single_timeout = single_timeout;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public String getIsProxyModel() {
		return isProxyModel;
	}

	public void setIsProxyModel(String isProxyModel) {
		this.isProxyModel = isProxyModel;
	}

	public String getSendFrame() {
		return sendFrame;
	}

	public void setSendFrame(String sendFrame) {
		this.sendFrame = sendFrame;
	}

	public String getRecvFrame() {
		return recvFrame;
	}

	public void setRecvFrame(String recvFrame) {
		this.recvFrame = recvFrame;
	}

	public String getAPDUData() {
		return APDUData;
	}

	public void setAPDUData(String aPDUData) {
		APDUData = aPDUData;
	}

	public String getPrefix_ip() {
		return prefix_ip;
	}

	public void setPrefix_ip(String prefix_ip) {
		this.prefix_ip = prefix_ip;
	}

	public String getCompareFrame1() {
		return compareFrame1;
	}

	public void setCompareFrame1(String compareFrame1) {
		this.compareFrame1 = compareFrame1;
	}

	public String getCompareFrame2() {
		return compareFrame2;
	}

	public void setCompareFrame2(String compareFrame2) {
		this.compareFrame2 = compareFrame2;
	}

	public String getSvr_priority() {
		return svr_priority;
	}

	public void setSvr_priority(String svr_priority) {
		this.svr_priority = svr_priority;
	}

	public String getSvr_no() {
		return svr_no;
	}

	public void setSvr_no(String svr_no) {
		this.svr_no = svr_no;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public String getERRADDR() {
		return ERRADDR;
	}

	public void setERRADDR(String eRRADDR) {
		ERRADDR = eRRADDR;
	}


	public String getPCID() {
		return PCID;
	}

	public void setPCID(String pCID) {
		PCID = pCID;
	}

	public String getDBTYPE() {
		return DBTYPE;
	}

	public void setDBTYPE(String dBTYPE) {
		DBTYPE = dBTYPE;
	}



	public void refreshDataFromDB() {
//		System.out.println("refreshDataFromDB begin");

		IBaseDao<ProduceParam> iBaseDao_ProduceParam = new ProduceParamDaoImpl();
		produceParamList = iBaseDao_ProduceParam.retrieve("where computer='" + SoftParameter.getInstance().getPCID() + "'", "");
//		System.out.println("refreshDataFromDB end");
	}

	// xuky 2017.07.14 �޸�Ϊ�л����л�ȡ����
	public String getParamValByKey(String key) {
		String ret = "----";
		if (produceParamList != null)
			for (ProduceParam produceParam : produceParamList) {
				if (produceParam.getKeyname().equals(key)) {
					ret = produceParam.getValue();
					break;
				}
			}
		return ret;
	}


	public String getOKADDR() {
		if (OKADDR == null)
			OKADDR = "";
		return OKADDR;
	}

	public void setOKADDR(String oKADDR) {
		OKADDR = oKADDR;
	}


	public Map<String, String> getCompleteDataMap() {
		return CompleteDataMap;
	}

	public void setCompleteDataMap(Map<String, String> completeDataMap) {
		CompleteDataMap = completeDataMap;
	}

	public Map<String, String> getRecvDataMap() {
		return recvDataMap;
	}

	public void setRecvDataMap(Map<String, String> recvDataMap) {
		this.recvDataMap = recvDataMap;
	}


	public String getUDPCLIENT_IP() {
		return UDPCLIENT_IP;
	}

	public void setUDPCLIENT_IP(String uDPCLIENT_IP) {
		UDPCLIENT_IP = uDPCLIENT_IP;
	}

	public int getUDPCLIENT_PORT() {
		return UDPCLIENT_PORT;
	}

	public void setUDPCLIENT_PORT(int uDPCLIENT_PORT) {
		UDPCLIENT_PORT = uDPCLIENT_PORT;
	}

	public String getUDPSVR_IP() {
		return UDPSVR_IP;
	}

	public void setUDPSVR_IP(String uDPSVR_IP) {
		UDPSVR_IP = uDPSVR_IP;
	}

	public int getUDPSVR_PORT() {
		return UDPSVR_PORT;
	}

	public void setUDPSVR_PORT(int uDPSVR_PORT) {
		UDPSVR_PORT = uDPSVR_PORT;
	}


	public String getTERMINAL_IP() {
		return TERMINAL_IP;
	}

	public void setTERMINAL_IP(String tERMINAL_IP) {
		TERMINAL_IP = tERMINAL_IP;
	}



	public String getLOG_Level() {
		return LOG_Level;
	}

	public void setLOG_Level(String lOG_Level) {
		LOG_Level = lOG_Level;
	}


	public String getWAIT_TIME() {
		return WAIT_TIME;
	}

	public void setWAIT_TIME(String wAIT_TIME) {
		WAIT_TIME = wAIT_TIME;
	}



	public Boolean getSERIAL_FINISHED() {
		return SERIAL_FINISHED;
	}

	public void setSERIAL_FINISHED(Boolean sERIAL_FINISHED) {
		SERIAL_FINISHED = sERIAL_FINISHED;
	}

	public Boolean getRUNFASTER() {
		return RUNFASTER;
	}

	public void setRUNFASTER(Boolean rUNFASTER) {
		RUNFASTER = rUNFASTER;
	}

	public List<ProduceCase> getCaseList() {
		return CaseList;
	}

	public void setCaseList(List<ProduceCase> caseList) {
		CaseList = caseList;
	}

	public void setCaseListID(String caseListID) {
		CaseListID = caseListID;
	}
	public String getCaseListID() {
		// TODO Auto-generated method stub
		return CaseListID;
	}

	public String getSENDPLC1() {
		return SENDPLC1;
	}

	public void setSENDPLC1(String sENDPLC1) {
		SENDPLC1 = sENDPLC1;
	}

	public String getSENDPLC2() {
		return SENDPLC2;
	}

	public void setSENDPLC2(String sENDPLC2) {
		SENDPLC2 = sENDPLC2;
	}


	public String getRECVCLINET() {
		return RECVCLINET;
	}

	public void setRECVCLINET(String rECVCLINET) {
		RECVCLINET = rECVCLINET;
	}

	public String getObserverOK() {
		return ObserverOK;
	}

	public void setObserverOK(String observerOK) {
		ObserverOK = observerOK;
	}

	public String getPLCFRAME() {
		return PLCFRAME;
	}

	public void setPLCFRAME(String pLCFRAME) {
		PLCFRAME = pLCFRAME;
	}



	public String getTESTALL_TIME_OUT() {
		return TESTALL_TIME_OUT;
	}

	public void setTESTALL_TIME_OUT(String tESTALL_TIME_OUT) {
		TESTALL_TIME_OUT = tESTALL_TIME_OUT;
	}

	public String getWORKID() {
		return WORKID;
	}

	public void setWORKID(String wORKID) {
		WORKID = wORKID;
	}


	public String getSTARTQCODE() {
		return STARTQCODE;
	}

	public void setSTARTQCODE(String sTARTQCODE) {
		STARTQCODE = sTARTQCODE;
	}

	public String getIS_SETID() {
		return IS_SETID;
	}

	public void setIS_SETID(String iS_SETID) {
		IS_SETID = iS_SETID;
	}

	public static void main(String[] args) throws IOException {
		SoftParameter softParameter = new SoftParameter();
		uniqueInstance = softParameter;
		softParameter.saveParam();
		// softParameter.init();
	}


}
