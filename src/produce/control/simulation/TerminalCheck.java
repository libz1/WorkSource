package produce.control.simulation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.control.entity.BaseCommLog;
import produce.control.entity.TerminalResult;
import produce.control.entity.TerminalResultDaoImpl;
import produce.entity.ProduceLog;
import produce.entity.ProduceLogDaoImpl;
import util.Frame645Control;
import util.Publisher;
import util.SoftParameter;
import util.Util698;

public class TerminalCheck {
	// xuky 2019.02.22 �������ĵȴ�ʱ��Ϊ20��
	int TernailStarTime = 80 * 1000;

	// �������Ƿ�����˲��Եı�־
	Map<String, String> MeterFlags = new ConcurrentHashMap<String, String>(); // ��λ�Ƿ�������ϱ�־

	// �ظ���������ַΪ129.1.22.96ʱ����Ҫʹ�������Ϣ  ��Ҳ�Ǽ������Ĳ���ID��Ϣ
	Map<String, String> TerminalMACs = new ConcurrentHashMap<String, String>(); // ��λ������MAC��ַ��Ϣ

	// ���ն�ͨ��ʱ��Ҫʹ��  �Ƿ��У�����У����ͨ��
	Map<String, String> MetersEnd = new ConcurrentHashMap<String, String>(); // ��λͨ������������
	Map<String, String> MetersRJ45 = new ConcurrentHashMap<String, String>(); // ��λ��RJ45ͨ�����
	Map<String, String> HaveMeters = new ConcurrentHashMap<String, String>(); // ��λ�Ƿ��м�������Ϣ��
	Map<String, String> Params = new ConcurrentHashMap<String, String>(); // ��λͨ�Ų�����Ϣ��
	Map<String, String> MeterAddrs = new ConcurrentHashMap<String, String>(); // ��λ��������ַ��Ϣ;
	Map<String, BaseCommLog> MetersResult = new ConcurrentHashMap<String, BaseCommLog>(); // ��λ�Ƿ��м�������Ϣ��
	Map<String, String> MetersChannel = new ConcurrentHashMap<String, String>(); // ��λͨ�����
	Map<String, String> MetersChannelResults = new ConcurrentHashMap<String, String>(); // ��λͨ���������
	int beginNum = 1;
	int MeterNum = 32;
	String begin_time = Util698.getDateTimeSSS_new();

	public void AllCheck(String COM) {

		// ���Խ�����浽���ݿ�
		ProduceLog produceLog = new ProduceLog();
		produceLog.setAddr("");
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());
		produceLog.setOpTime(DateTimeFun.getDateTimeSSS());
		produceLog.setOperation("�����������Լ�");
		// xuky 2019.02.28  ��� ���̨��NO��¼
		produceLog.setStageno(PlatFormParam.getInstance().getPlatFormNO());
		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		ProduceLog produceLog_new = iBaseDao_ProduceLog.create(produceLog);
		PlatFormParam.getInstance().setRUNID(produceLog_new.getID());

		String model = PlatFormParam.getInstance().getTerminalModel();

		// 1���ӵ�   ��ȡ��ѹ�������ݣ�����У�������ӵ�
//		SimuRun.raisePower(TernailStarTime);
		// 2���޸�̨��ģʽ
		SimuRun.chaneMode(model, COM);

		//  3�����ͨ�������ȷ�ϼ������Ƿ���λ
		String type = "�ŵ����";
	    Util698.log(TerminalCheck.class.getName(), type+"��ʼ", Debug.LOG_INFO);

		new Thread(() -> {
			String[] s2 = { "result", "user data", "�ŵ���⿪ʼ", ""};
			Publisher.getInstance().publish(s2);
		}).start();

		for (int i = beginNum; i <= MeterNum; i++) {

			// 32��λ�����߳��в���ִ��
			MeterRunInfo meterRunInfo = new MeterRunInfo();
			meterRunInfo.setMeterno(DataConvert.int2String(i));
			new Thread(() -> {
				String meterno = meterRunInfo.getMeterno();
				MetersEnd.put(meterno,"");
				MetersRJ45.put(meterno,"");
				HaveMeters.put(meterno,"");
				Params.put(meterno,"");
				MeterAddrs.put(meterno,"");
//				MetersResult.put(meterno,"");
				MetersChannel.put(meterno,"");
				MetersChannelResults.put(meterno,"");
				TerminalMACs.put(meterno,"");

				String param = "", meterAddr = "";
				boolean is_test = false;
				if (is_test) {
					System.out.println(meterno);
					return;
				}
				String terminalMAC = ""; // ������ɺ���Ҫ��IP����ΪĬ�ϵ�129.1.22.96

				// ���ؽ����ͨ��IP��ͨ��port���ն�ͨ�ŵ�ַ�������ŵ��б��ŵ����ñ�־
				Object[] result = SimuRun.getUserfulChanel(meterno, COM, model);
				if (!result[0].equals("")) {
					param = result[0] + ":" + result[1];
					meterAddr = (String) result[2];
					HaveMeters.put(meterno, "1");
					MetersChannel.put(meterno, (String)result[3]);
					MetersChannelResults.put(meterno, (String)result[4]);
					Params.put(meterno, param);
					MeterAddrs.put(meterno, meterAddr);
				} else {
					HaveMeters.put(meterno, "0");
					MetersEnd.put(meterno, "1");
					return;
				}

				// �޸��ն˵�IP��ַ
//				String NetIP = "192.168.127.";
				String NetIP = "129.1.22.";
				// ��Σ���ǰ���ն˵�ַ��ͨ�Ų�������λ��Ϣ�����ݱ�λ��Ϣ���Զ��õ���Ҫ���ó�Ϊ���ն˵�ַ����
				// ���أ��µ�IP��ַ��ͬʱҲ���ն˵�ַ����MAC��ַ��Ϣ�������������
				result = SimuRun.setIP_MAC(meterAddr, param, meterno, NetIP);
				if (((BaseCommLog) result[2]).getResult().equals("OK")) {
					meterAddr = (String) result[0];
					if (param.indexOf("COM") < 0)
						param = meterAddr + ":7000"; // ���ԭ�ȵ���Чͨ���ŵ�����RJ45������Ҫ���е���
					terminalMAC = (String) result[1];
//					HaveMeters.put(meterno, "1");
					Params.put(meterno, param);
					MeterAddrs.put(meterno, meterAddr);
					TerminalMACs.put(meterno, terminalMAC);
					MetersRJ45.put(meterno, "1");
				} else {
					MetersRJ45.put(meterno, "0"); // �м������������޷����ü�������ַ��MAC
				}
				MetersEnd.put(meterno, "1");
				// ����HaveMeters��MeterChannels��MeterChannelResults�����ŵ����Խ��
			}).start();
		}


		// 4�����������е��жϹ���
		Boolean allIsOK = false;
		String tmp = "";
		int j = 0;
		while(!allIsOK){
			j = 0 ;
			for (int i = beginNum; i <= MeterNum; i++) {
				tmp = MetersEnd.get(DataConvert.int2String(i));
				if (tmp==null || tmp.equals(""))
					break;
				else
					j++;
			}
			if (j == MeterNum-beginNum+1)
				allIsOK = true;
			else
				Debug.sleep(1000);
		}
		IBaseDao<TerminalResult> iBaseDao_TerminalResult = new TerminalResultDaoImpl();

		for (int i = beginNum; i <= MeterNum; i++) {
			String meterno = DataConvert.int2String(i);

			// ���Խ�����浽���ݿ���
			TerminalResult terminalResult = new TerminalResult();
			String mac = TerminalMACs.get(meterno);
			if (mac.equals(""))
				mac = meterno;
			terminalResult.setDevID(mac);
			terminalResult.setName(type);
			terminalResult.setRecvtime(Util698.getDateTimeSSS_new());
			String channelResult = MetersChannelResults.get(meterno);
			String meterRJ45 = MetersRJ45.get(meterno);
			// xuky 2019.02.27 ͨ���жϱ�׼��Ҫ����
			if (channelResult.indexOf("0") <0 && meterRJ45.equals("1"))
				terminalResult.setResult("OK");
			else
				terminalResult.setResult("NG");

			terminalResult.setNote1("'"+MetersChannel.get(meterno)+"':"+channelResult+";RJ45:"+MetersRJ45.get(meterno));
			terminalResult.setRunID(produceLog_new.getID());
			TerminalResult TerminalResult_new = iBaseDao_TerminalResult.create(terminalResult);

		}
	    Util698.log(TerminalCheck.class.getName(), type+"���", Debug.LOG_INFO);

	    // ��̨�����
//	    String[] types1 = {"�ռ�ʱ������","12V����"};
	    String[] types1 = {"�ռ�ʱ������"};
	    Map<String, BaseCommLog> MeterResults = null;
	    Boolean is_skip1=false;
	    if (!is_skip1){
	    	for( String type1: types1 ){
			    Util698.log(TerminalCheck.class.getName(), type1+"��ʼ", Debug.LOG_INFO);
				// ���������ռ�ʱ������
			    MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
				SimuRun.allDateTimeErrNum_new(HaveMeters, MeterResults, COM,type1);
				for (int i = beginNum; i <= MeterNum; i++) {
					String meterno = DataConvert.int2String(i);
					BaseCommLog log = MeterResults.get(meterno);
					if (log != null)
						add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type1);
				}
			    Util698.log(TerminalCheck.class.getName(), type1+"���", Debug.LOG_INFO);
	    	}
	    }

	    // ̨���뼯������ϲ���
	    type = "ң��on����";
	    Boolean is_skip2=false;
	    if (!is_skip2){
		    Util698.log(TerminalCheck.class.getName(), type+"��ʼ", Debug.LOG_INFO);
			MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
			// ��������ң�Ų���
			SimuRun.allFS(COM,HaveMeters,MeterResults, Params, MeterAddrs,"on");
			for (int i = beginNum; i <= MeterNum; i++) {
				String meterno = DataConvert.int2String(i);
				BaseCommLog log = MeterResults.get(meterno);
				if (log != null)
					add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type);
			}
		    Util698.log(TerminalCheck.class.getName(), type+"���", Debug.LOG_INFO);

		    type = "ң��off����";
		    Util698.log(TerminalCheck.class.getName(), type+"��ʼ", Debug.LOG_INFO);
			MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
			// ��������ң�Ų���
			SimuRun.allFS(COM,HaveMeters,MeterResults, Params, MeterAddrs,"off");
			for (int i = beginNum; i <= MeterNum; i++) {
				String meterno = DataConvert.int2String(i);
				BaseCommLog log = MeterResults.get(meterno);
				if (log != null)
					add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type);
			}
		    Util698.log(TerminalCheck.class.getName(), type+"���", Debug.LOG_INFO);

	    }

	    //  GPRS��Ϣ��ζ�ȡ(IP PORT )��  112.6.118.246  9002 ����쳣���Ѿ������ˣ�����01
	    //  RT ���ڷ���02 ��Ϣ��·��ģ�����ģ�����ƥ�䣿��Ҫ��Ӧ������ʽ   �����������ƥ�� �������Զ��ж� �����������ַ
	    // ����USBδ���     ����USB��Ч
	    // ֱ��ģ���� ��ѹ�ܵ�����ȡ  ̨��֧��

	    // ����ǰ�Ƚ���ʱ�����á�Ȼ���ٽ���ʱִ��ʱ�Ӷ�ȡ

//	    String[] types = {"��������","USB����","��ز���","Һ������","GPRS����","ʱ������","ʱ�Ӳ���","ESAM����","GPRS����"};
	    // ������������
	    String[] types = {"��������","��ز���","Һ������","GPRS����","ʱ������","ʱ�Ӳ���","ESAM����"};
	    Boolean is_skip3=true;
	    if (!is_skip3){
	    	for( String type1: types ){
			    Util698.log(TerminalCheck.class.getName(), type1+"��ʼ", Debug.LOG_INFO);
			    MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
			    SimuRun.allButtons(COM,HaveMeters,MeterResults, Params, MeterAddrs,type1);
				for (int i = beginNum; i <= MeterNum; i++) {
					String meterno = DataConvert.int2String(i);
					BaseCommLog log = MeterResults.get(meterno);
					if (log != null)
						add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type1);
				}
			    Util698.log(TerminalCheck.class.getName(), type1+"���", Debug.LOG_INFO);
	    	}
	    }

	    // ·�ɲ��ԣ�������У��޷����������Կ�����ͨ�Ų���ͻ������£������������н���
	    type = "·�ɲ���";
	    Boolean is_skip4=true;
	    if (!is_skip4){
	    	String sendData = PlatFormParam.getInstance().getRT_MeterAddr();
	    	if (model.equals("����"))
	    		sendData = PlatFormParam.getInstance().getRT_MeterAddr_NW();
	    	sendData = "000000000000" + sendData;
	    	sendData = sendData.substring(sendData.length()-12);  // �õ����ַ��Ϣ
	    	sendData +=  "000000000000";  // ��ӳ��������Ϣ
			Frame645Control frame645 = null;
			for (int i = beginNum; i <= MeterNum; i++) {
				String meterno = DataConvert.int2String(i);
				String flag = HaveMeters.get(meterno);
				String param1 = "",meterAddr1="";
				if (flag!=null && flag.equals("1")){
					if (Params!= null) param1 = Params.get(meterno);
					if (MeterAddrs!= null) meterAddr1 = MeterAddrs.get(meterno);
					while (true){
						BaseCommLog tmp1 = SimuRun.checkTermail("04 96 96 09", "00", param1, meterAddr1,sendData,"");
						frame645 = new Frame645Control(tmp1.getRecv());
						String data = frame645.getData_data();
						if (!data.equals("02")){
							add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, tmp1,type);
							break;
						}
						else
							Debug.sleep(2000);
					}
				}

			}
		}

	    String end_time = Util698.getDateTimeSSS_new();

	    Util698.log(TerminalCheck.class.getName(), model+"ģʽ������ɣ�����"+(MeterNum-beginNum+1), Debug.LOG_INFO);
	    Util698.log(TerminalCheck.class.getName(), "��ʼʱ��"+begin_time +" ����ʱ��"+end_time+" ��ʱ"+Util698.getMilliSecondBetween_new(end_time, begin_time), Debug.LOG_INFO);

		// �����������û� �Բ���δ��ɵ���Ŀ������һ�ֲ���  ���ڲ���ͨ������Ŀ��Ϊ��Լʱ���������Ҫ�ٽ��в���

		// ��Ҫ�Զ����У���Ϊ������Ҫ�û�����һЩ�������̣�����β����ߡ��β�USB��������Һ�����ȣ�
		// ������Ȼ�����Զ�������δ�ϸ���Ŀ�Զ����¼��

		// �ָ���������IP��ַ

	}

	private void add2TermialResult(ProduceLog produceLog_new, IBaseDao<TerminalResult> iBaseDao_TerminalResult,
			String meterno, BaseCommLog log, String type) {
		TerminalResult terminalResult = new TerminalResult();
		String mac = TerminalMACs.get(meterno);
		if (mac.equals(""))
			mac = meterno;
		// xuky 2019.02.28 ��¼��λ��Ϣ
		String recv = log.getRecv();
		if (recv == null || recv.equals(""))
			log.setSpecialData("��ʱ");
		else{
			if (type.indexOf("ң��") >= 0 && log.getResult().equals("NG") ){
				Frame645Control frame645Control = new Frame645Control(recv);
				log.setSpecialData(frame645Control.getData_data());
			}
		}
		terminalResult.setMeterno(meterno);
		terminalResult.setDevID(mac);
		terminalResult.setName(type);
		terminalResult.setRecvtime(Util698.getDateTimeSSS_new());
		terminalResult.setResult(log.getResult());
		terminalResult.setRunID(produceLog_new.getID());
		terminalResult.setResultID(log.getID());
		terminalResult.setNote1(log.getSpecialData());
		terminalResult.setNote2(log.getSpecialRule());
		TerminalResult TerminalResult_new = iBaseDao_TerminalResult.create(terminalResult);
	}

	public static void main(String[] args) {
		TerminalCheck terminalCheck = new TerminalCheck();
		terminalCheck.AllCheck(PlatFormParam.getInstance().getPlatFormCOM());

	}

	/*
	 * 2019.02.22 ��Դ ��Դ�󣬵ȴ���Լ10-20�룬Ȼ���ٽ�������������ʱ��ֵ��Ҫ������ȷһЩ���ٿ���Щ�� ȷ������������������
	 *
	 * ̨��ģʽ����
	 *
	 * ���Զ�����λ״̬��������λ��ͨ���жϡ����н��С�����ȷ���豸�Ƿ���λ ���ݱ�λ�Զ��л�IP��ַ������MAC��ַ ��RJ45ͨ����������
	 *
	 * ������Ҫ�Ĳ��������ݱ�λ����� ��λ���豸���Զ����е�ѹ���� ��λ���豸���Զ����е�������
	 *
	 * �ռ�ʱ������ �Ա�̨���в��� �����н��С��ռ�ʱ������ �ȴ�ȫ��������� �Ա�̨�������壬�ظ���ԭʼ״̬
	 *
	 * ������Ŀ�Ĳ��� ң�š�12V��ֱ��ģ������ѹ���� GPRS��⡢ʱ�Ӽ�⡢���״̬ ·�ɼ�� ESAM��� ������USB��Һ��
	 * �����Զ���Ŀ���в��⡿
	 *
	 * �ȴ�������Ŀ������ɡ��ȴ����ƵĿ�����������
	 *
	 * ��չʾ���Խ���� ����ʾ���Խ�����Ϣ��
	 *
	 *
	 * ̨��ϵ�ǰ����Ҫ����IP��ַ���лز��� ��ȡIP��MAC��д��129.1.22.96��MAC ��ȡ��֤
	 *
	 * �������� ����ͨ�� RTͨ�� ����̨��ͨ��
	 *
	 * �������û��趨����Ϣ�� ң���м�· ����ģʽ���Ƿ���ESAM��������ģʽ ·�ɲ�����Ĭ��һ�����ַ����
	 *
	 *
	 *
	 * ��������Ŀ�� �������Ƿ��������޵�����Ӱ���ն˹��ܲ��ԣ����� ����б�Ҫ���ͽ��н�Դ����Դ�Ĳ���
	 */
}
