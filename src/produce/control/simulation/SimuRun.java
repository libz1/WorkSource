package produce.control.simulation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import dao.SerialParamFaoImpl;
import entity.SerialParam;
import produce.control.comm.CommWithRecv;
import produce.control.comm.FramePlatform;
import produce.control.comm.FramePlatformOn;
import produce.control.entity.AllRun;
import produce.control.entity.BaseCommLog;
import produce.entity.ProduceCase;
import produce.entity.ProduceCaseDaoImpl;
import util.Frame645Control;
import util.SoftParameter;
import util.Util698;

public class SimuRun {
	static Boolean Debug_NoPLAT = false;

	public SimuRun(String PlatFormCOM) {
	}

	public static BaseCommLog checkTermail(String dataitem, String data, String param, String terminlIP, String sendData, String inputExpect) {
		BaseCommLog result = null;
		CommWithRecv commWithRecv = new CommWithRecv();
		String sData = getTermianlFrame(terminlIP, "14", dataitem, sendData);
		String msg = "��֤������������" + dataitem + " ӦΪ" + data;
		// 6800008101160C6894000816
		// xuky 2019.02.27 sendDataΪ�գ������Ϊ����ȷ������
		// data��Ϊ�գ���ʾ��Ҫ�յ������ݱ�ʾ�����ݱ���
		if (!sendData.equals("") && data.equals("") )
			dataitem = "";
		String expect = "";
		if (inputExpect == null || inputExpect.equals(""))
			expect = getTermianlFrame(terminlIP, "94", dataitem, data);
		else
			expect = inputExpect;
		result = commWithRecv.deal_one("��" + msg + "��", param, sData, expect, 5000);
		return result;
	}

	private static String getTermianlFrame(String terminalIP, String type) {
		if (type.indexOf("��") >= 0 && type.indexOf("ʱ��") >= 0) {
			return getTermianlFrame(terminalIP, "14", "04 96 96 05", "");
		}
		if (type.indexOf("��") >= 0 && type.indexOf("IP") >= 0) {
			return getTermianlFrame(terminalIP, "14", "04 96 96 10", "");
		}
		return "";
	}

	public static String getTermianlFrame(String termianlIP, String contrl, String dataitem, String data) {
		String ret = "";
		Frame645Control frame645 = new Frame645Control();
		frame645.setAddr(Util698.StrIP2HEX(termianlIP));
		frame645.setControl(contrl);
		frame645.setData_item(dataitem);
		frame645.setData_data(data);
		ret = frame645.get645Frame();
		return ret;
	}

	private void singlePlatFormFrame(String meterno) {
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meterno);
		framePlatform.setCONTROL("A2");
		framePlatform.setDATA("3F");
		String sData = framePlatform.getFrame();
		System.out.println(sData);
	}

	public static void allMeterVoltageControl(String COM, String type) {
		// 15����ѹ��·
		// ����:01H+��ַ(A��Z) +����+AAH(����) +���(H-30H,A-31H,B-32H,C-33H)+(30H/�Ͽ�
		// 31H/����)+У��λ+����(17H)
		// �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��) +У��λ+����(17H)
		String con = "";
		if (type.equals("on"))
			con = "31";
		else
			con = "30";
		String sData = "";
		BaseCommLog tmp = null;
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		// Boolean broadCast = false;
		Boolean broadCast = true;
		if (!broadCast) {
			// �����λѭ��
			for (int i = 1; i <= 32; i++) {
				// �����λѭ��
				String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(DataConvert.int2String(i))
						+ " 06 06 06 17";
				framePlatform.setADDR(DataConvert.int2String(i));
				framePlatform.setCONTROL("AA");
				framePlatform.setDATA("30" + con);
				sData = framePlatform.getFrame();
				String msg = "��λ" + i + "��ѹ" + type;
				tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
				Util698.log(SimuRun.class.getName(), msg + "���:" + tmp, Debug.LOG_INFO);
			}
		} else {
			// �㲥��ʽ �����λѭ��
			framePlatform.setADDR("FF");
			framePlatform.setCONTROL("AA");
			framePlatform.setDATA("30" + con);
			sData = framePlatform.getFrame();
			String msg = "�㲥���б�λ��ѹ" + type + "���޻ظ�";
			// �㲥ģʽ�£�����3�Σ�ȷ��ִ�е���ȷ��
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		}

	}

	public static boolean singelMeterVoltageOn(String meter, String COM) {
		// 15����ѹ��·
		// ����:01H+��ַ(A��Z) +����+AAH(����) +���(H-30H,A-31H,B-32H,C-33H)+(30H/�Ͽ�
		// 31H/����)+У��λ+����(17H)
		// �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��) +У��λ+����(17H)

		// �л����е�ѹ
		allMeterVoltageControl(COM, "off");

		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();

		// ����λ �����ѹ
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("AA");
		framePlatform.setDATA("3031");
		sData = framePlatform.getFrame();
		String msg = "��λ" + meter + "��ѹ����";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	public static void allMeterCurrentOff(String meter, String COM) {
		// 14�� ������·��λ/�Ͽ�/�պ�
		// ����:01H+��ַ(A��Z) +����+A8H(����) +(30H/�Ͽ� 31H/���� 32H/��λ)+У��λ+����(17H)

		// �̽����е���
		String sData = "";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();

		// �̽����е���
		framePlatform.setADDR("FF");
		framePlatform.setCONTROL("A8");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		String msg = "�㲥���б�λ�����Ͽ�,�޻ظ�";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

	}

	public static void singelMeterCurrentOn(String meter, String COM) {
		// 14�� ������·��λ/�Ͽ�/�պ�
		// ����:01H+��ַ(A��Z) +����+A8H(����) +(30H/�Ͽ� 31H/���� 32H/��λ)+У��λ+����(17H)

		// �̽����е���
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();

		// �̽����е���
		framePlatform.setADDR("FF");
		framePlatform.setCONTROL("A8");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		String msg = "�㲥���б�λ��������,�޻ظ�";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "", 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

		// ����λ �Ͽ�����
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("A8");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "�����Ͽ�";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

	}

	public Boolean raisePower() {
		return raisePower(0);
	}

	// ��Դ
	public static Boolean raisePower(int sleepTime) {
		String COM = PlatFormParam.getInstance().getPlatFormCOM();
		CommWithRecv commWithRecv = new CommWithRecv();
		int maxstep = 5;
		String param = "COM" + COM + ":9600:NONE";
		String sData = "F9 F9 F9 F9 F9 B1 03 00 00 00 01 9E 3A";
		String expect = "B1 03 02 16 03 B7 FF";
		BaseCommLog tmp = commWithRecv.deal_one("����Դ1-" + maxstep + "��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "4D 53 30 0D";
		expect = "4D 53 41 43 4B 3B";
		tmp = commWithRecv.deal_one("����Դ2-" + maxstep + "��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "49 42 30 36 0D";
		expect = "49 42 41 43 4B 3B";
		tmp = commWithRecv.deal_one("����Դ3-" + maxstep + "��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "55 42 31 0D";
		expect = "55 42 41 43 4B 3B";
		tmp = commWithRecv.deal_one("����Դ4-" + maxstep + "��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		FramePlatformOn framePlatform = new FramePlatformOn();
		framePlatform.setRated_Freq(50.00);
		framePlatform.setRated_Volt_A(220.00);
		framePlatform.setRated_Volt_B(220.00);
		framePlatform.setRated_Volt_C(220.00);
		framePlatform.setRated_Curr_A(1.0000);
		framePlatform.setRated_Curr_B(1.0000);
		framePlatform.setRated_Curr_C(1.0000);
		sData = framePlatform.getFrame();
		expect = "B1 10 00 02 00 10 7A 35";
		tmp = commWithRecv.deal_one("����Դ5-" + maxstep + "��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		// xuky 2019.02.22 �����趨�Ĳ��������еȴ����ȴ����������豸�������
		if (sleepTime != 0){
		    Util698.log(SimuRun.class.getName(), "sleep"+sleepTime+"�ȴ���������ʼ�����", Debug.LOG_INFO);
			Debug.sleep(sleepTime);
		}
		return true;
	}

	public static Boolean PowerOff(String COM) {
		CommWithRecv commWithRecv = new CommWithRecv();
		String param = "COM" + COM + ":9600:NONE";
		String sData = "FE FE FE FE FE 01 41 07 33 31 64 17";
		String expect = "FE FE FE FE FE 01 41 06 06 06 17";
		BaseCommLog tmp = commWithRecv.deal_one("����Դ1-2��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		FramePlatformOn framePlatform = new FramePlatformOn();
		framePlatform.setRated_Freq(0.00);
		framePlatform.setRated_Volt_A(0.00);
		framePlatform.setRated_Volt_B(0.00);
		framePlatform.setRated_Volt_C(0.00);
		framePlatform.setRated_Curr_A(0.0000);
		framePlatform.setRated_Curr_B(0.0000);
		framePlatform.setRated_Curr_C(0.0000);
		sData = framePlatform.getFrame();
		expect = "B1 10 00 02 00 10 7A 35";
		tmp = commWithRecv.deal_one("����Դ2-2��", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	public static boolean chaneMode(String model, String COM) {
		String sData = "";
		if (model.indexOf("����") >= 0 || model.indexOf("1") >= 0)
			sData = "FE FE FE FE FE 01 41 07 4B 32 7D 17";
		if (model.indexOf("����") >= 0 || model.indexOf("2") >= 0)
			sData = "FE FE FE FE FE 01 41 07 4B 31 7C 17";
		String expect = "FE FE FE FE FE 01 41 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		// ̨��ͨ�ŵĲ���Ϊ9600-��У�� NONE
		BaseCommLog tmp = commWithRecv.deal_one("��" + model + "ģʽ��", "COM" + COM + ":9600:NONE", sData, expect);
		if (tmp.getResult().equals("OK"))
			return true;
		else
			return false;
	}

	public static boolean setFSFlag(String meter, String COM, String FSFlag) {
		return setFSFlag(meter, COM, FSFlag, 0);
	}

	public static boolean setFSFlag(String meter, String COM, String FSFlag, int num) {
		// ����:01H+��ַ(A��Z) +����+B4H(����) +(30H/�̽� 31H/��ӵ�ѹ 32/�������)+У��λ+����(17H)
		String sData = "", msg = "";
		BaseCommLog tmp = null;
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		if (meter.equals("FF"))
			expect = "";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		if (num == 0) { // �״�����ʱ��Ҫִ��
			framePlatform.setCONTROL("B4");
			framePlatform.setDATA("30");
			sData = framePlatform.getFrame();
			msg = "��λ" + meter + "����ң�ŷ�ʽ�̽�";
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect,500);
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			if (!tmp.getResult().equals("OK"))
				return false;
		}

		// ����:01H+��ַ(A��Z) +����+A2H(����)+ ң��״̬��(1�ֽ�)+У��λ+����(17H)
		framePlatform.setCONTROL("A2");
		framePlatform.setDATA(DataConvert.binStr2HexString(FSFlag, 2));
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "ң�����" + FSFlag;
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect,500);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;

	}

	public static boolean read12V(String meter, String COM) {
		// 01H+��ַ(A��Z) +����+B2H(����) +(30H/��ʼ 31H/ֹͣ)+У��λ+����(17H)
		// ����λ �����ѹ
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		BaseCommLog tmp = null;
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("B2");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		String msg = "��λ" + meter + "ֱ����ѹ��⿪ʼ";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// Debug.sleep(2000);
		expect = "get12V:substring(18,26);val(12);region(-0.2,0.2);";
		framePlatform.setCONTROL("B3");
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "ֱ����ѹ����ȡ";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// FE FE FE FE FE 01 42 0A 30 30 36 33 38 01 17 638 = 478.5
		// ��Ҫ�Զ�ȡ�ĵ�ѹ���ݽ��н�һ���Ĵ���

		expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		framePlatform.setCONTROL("B2");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "ֱ����ѹ���ֹͣ";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	private void singleCase() {
		int max = 1;
		String caseNO = "96";
		// ���λ���Լ�¼�洢��List
		List<MeterRunInfo> meterRunInfos = new CopyOnWriteArrayList<MeterRunInfo>();
		// ��ǰ��ȡ�����Ĳ���������Ϣ
		List<ProduceCase> produceCases = new ProduceCaseDaoImpl().retrieve(
				"where subid='" + caseNO + "' and computer='" + SoftParameter.getInstance().getPCID() + "'",
				" order by caseno");
		// ������Ϣ
		Map<String, SerialParam> serialMap = new ConcurrentHashMap<String, SerialParam>();
		List<SerialParam> result = new SerialParamFaoImpl().retrieve("", "");
		for (SerialParam sp : result)
			serialMap.put(sp.getCOMID(), sp);

		String bTime = Util698.getDateTimeSSS_new();
		Util698.log(SimuRun.class.getName(), "��λ������" + max + " ���Կ�ʼ", Debug.LOG_INFO);

		// �����λ�Ĵ�������ʵ������
		for (int i = 1; i <= max; i++)
			meterRunInfos.add(new MeterRunInfo(DataConvert.int2String(i), produceCases, serialMap));

		// ���߳���������ʵ������Ĳ��Թ���
		for (MeterRunInfo meterinfo : meterRunInfos) {
			new Thread(() -> {
				meterinfo.run();
			}).start();
		}

		// �жϲ��Թ����Ƿ�ִ�н���
		Boolean endFlag = true;
		while (true) {
			endFlag = true;
			for (MeterRunInfo meterinfo : meterRunInfos) {
				// System.out.println(meterinfo.getEndTime());
				if (meterinfo.getEndTime().equals("")) {
					// ��ʾ�������� û�д�����
					endFlag = false;
					// System.out.println(meterinfo.getMeterno());
					break;
				}
			}
			if (endFlag == false) {
				Debug.sleep(500);
			} else {
				// ������е�meterinfo��getEndTime���������ˣ��ڱ�ʾ����ȫ�����
				String eTime = Util698.getDateTimeSSS_new();
				int usingTime = Util698.getMilliSecondBetween_new(eTime, bTime).intValue();
				Util698.log(SimuRun.class.getName(),
						"��λ������" + max + " ���Խ��� ���Ժ�ʱ:" + usingTime + " ƽ����ʱ:" + (usingTime / max), Debug.LOG_INFO);
				break;
			}
		}

	}

	public static void outPut(String meter, String COM, String val, String type) {

		String data_type = "", data_val = "";
		if (type.equals("��ѹ")) {
			data_type = "31";
			// 3.5V
			data_val = "00 A6 A6";
		} else {
			data_type = "30";
			// 18.5mA
			data_val = "00 39 39";
		}

		// ����:01H+��ַ(A��Z) +����+B6H(����) +(31H/��ѹ 30H/����)+У��λ+����(17H)
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("B6");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		String msg = "��λ" + meter + "����" + type + "���";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

		// ����PWMռ�ձ�
		// ����:01H+��ַ(A��Z) +����+B0H(����) +��λ1+��λ+��λ+У��λ+����(17H)
		framePlatform.setCONTROL("B0");
		framePlatform.setDATA(data_val);
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "����" + type + val;
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

		// ����:01H+��ַ(A��Z) +����+B1H(����) +(30H/��ʼ 31H/ֹͣ)+У��λ+����(17H)
		framePlatform.setCONTROL("B1");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "���" + type + "��ʼ";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
	}

	public static void stopOutPut(String meter, String COM) {
		// ����:01H+��ַ(A��Z) +����+B1H(����) +(30H/��ʼ 31H/ֹͣ)+У��λ+����(17H)
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setCONTROL("B1");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		String msg = "��λ" + meter + "�������";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
	}
	// ���������ռ�ʱ����ȡ  ��Ϊ�����ж�����̨��Ĳ��������뼯�н���
	public static boolean allDateTimeErrNum_new(Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults, String COM, String type) {

		if (type.indexOf("�ռ�ʱ")>=0){
			if (!dateTimeErrBegin(COM))
				return false;
			AllRun allRun = new AllRun(HaveMeters,COM,MeterResults,null,null,null,null,null,null);
			Map<String, BaseCommLog> result= allRun.run("singleDateTimeErrNum");
			if (!dateTimeErrEnd(COM))
				return false;
		}
		if (type.indexOf("12V")>=0){
			String sData = "";
			CommWithRecv commWithRecv = new CommWithRecv();
			FramePlatform framePlatform = new FramePlatform();
			BaseCommLog tmp = null;
			String meter = "FF";
			framePlatform.setADDR(meter);
			framePlatform.setCONTROL("B2");
			framePlatform.setDATA("30");
			sData = framePlatform.getFrame();
			String msg = "��λ" + meter + "ֱ����ѹ��⿪ʼ";
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "");
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "");

			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			if (!tmp.getResult().equals("OK"))
				return false;
			AllRun allRun = new AllRun(HaveMeters,COM,MeterResults,null,null,null,null,null,"get12V:substring(18,26);val(12);region(-0.2,0.2);");
			Map<String, BaseCommLog> result= allRun.run("singleDateTimeErrNum");

			framePlatform.setCONTROL("B2");
			framePlatform.setDATA("31");
			sData = framePlatform.getFrame();
			msg = "��λ" + meter + "ֱ����ѹ���ֹͣ";
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "");
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, "");
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			if (!tmp.getResult().equals("OK"))
				return false;
			return true;
		}
		return true;
	}
	public static boolean allButtons(String COM, Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults,Map<String, String> Params,Map<String, String> MeterAddrs, String type) {
		String  dataitem = "04 96 96 0A", data = "00", sendData = "", expect= "";
		if (type.indexOf("����") >= 0){
			dataitem = "04 96 96 0A";
			data = "00";
		}
		if (type.indexOf("USB") >= 0){
			dataitem = "04 96 96 0B";
			data = "00";
		}
		if (type.indexOf("���") >= 0){
			dataitem = "04 96 96 0C";
			data = "00";
		}
		if (type.indexOf("ESAM") >= 0){
			dataitem = "04 96 96 0D";
			String ESAMModel = PlatFormParam.getInstance().getESAMModel();
			if (ESAMModel.indexOf("698") >= 0)
				sendData = "02";
			else
				sendData = "01";
			data = "00";
		}
		if (type.indexOf("Һ��") >= 0){
			dataitem = "04 96 96 12";
			data = "00";
		}

		if (type.indexOf("GPRS") >= 0 && type.indexOf("����") >= 0){
			dataitem = "04 96 96 13";
			data = "00";
		}

		if (type.indexOf("GPRS") >= 0 && type.indexOf("����") >= 0){
			dataitem = "04 96 96 06";
			data = "";
			String IP = PlatFormParam.getInstance().getGPRS_IP();
			String port = PlatFormParam.getInstance().getGPRS_Port();
			sendData = Util698.StrIP2HEX(IP)+DataConvert.int2HexString(DataConvert.String2Int(port), 4);
		}

		if (type.indexOf("ʱ��") >= 0 && type.indexOf("����") >= 0){
			dataitem = "04 96 96 04";
			data = "";
			sendData = Util698.getDateTimeSSS_new();
			sendData = sendData.replaceAll("-", "");
			sendData = sendData.replaceAll(":", "");
			sendData = sendData.replaceAll(" ", "");
			sendData = sendData.substring(2);
			sendData = sendData.substring(0,sendData.length()-3);
		}
		if (type.indexOf("ʱ��") >= 0 && type.indexOf("����") >= 0){
			dataitem = "04 96 96 05";
			data = "";
			expect = "getDateTime:substring(28,40);val(now);region(-60000,60000);";
		}


		AllRun allRun = new AllRun(HaveMeters,COM,MeterResults,Params,MeterAddrs,dataitem,data,sendData,expect);
		Map<String, BaseCommLog> result= allRun.run("singleFSRead");
		return true;
	}


	public static boolean allFS(String COM, Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults,Map<String, String> Params,Map<String, String> MeterAddrs,String type) {
		String  dataitem = "04 96 96 03", data = "";
		if (type.equals("on")){
			int num = DataConvert.String2Int(PlatFormParam.getInstance().getFSNum());
			String bintxt = "";
			for( int i=1;i<=num;i++ )
				bintxt += "1";
			data = DataConvert.binStr2HexString(bintxt, 2);
			SimuRun.setFSFlag("FF", COM, "11111111",0);
		}
		else{
			data = "00";
		    SimuRun.setFSFlag("FF", COM,  "00000000",1);
		}
		Debug.sleep(1000);
		AllRun allRun = new AllRun(HaveMeters,COM,MeterResults,Params,MeterAddrs,dataitem,data,"","");
		Map<String, BaseCommLog> result= allRun.run("singleFSRead");

//	    SimuRun.checkTermail("04 96 96 03", "00", "129.1.22.12:7000", "129.1.22.12");

		return true;
	}
	// ���������ռ�ʱ����ȡ  ��Ϊ�����ж�����̨��Ĳ��������뼯�н���
	public static boolean allDateTimeErrNum(int maxNum,Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults, String COM) {
		if (!dateTimeErrBegin(COM))
			return false;

		// 1�����������������жϵĶ���
		Map<Integer, MeterRunInfo> runInfos =  new ConcurrentHashMap<Integer, MeterRunInfo>(); // ��λ�������
		for (int i = 1; i <= maxNum; i++) {

			// 2�����������Ӹ�������
			MeterRunInfo meterRunInfo = new MeterRunInfo();
			String meterno = DataConvert.int2String(i);
			meterRunInfo.setMeterno(meterno);
			runInfos.put(i, meterRunInfo);

			if (HaveMeters.get(meterno).equals("1"))
				new Thread(() -> {
					Object[] result = singleDateTimeErrNum(meterRunInfo.getMeterno(),COM);
					if ((boolean)result[0])
						MeterResults.put(meterno, (BaseCommLog) result[1]);
					// 3��ִ����ϣ����ø������ݵı�־��Ϥ
					meterRunInfo.setEndflag("1");
				}).start();
			else
				// 3��ִ����ϣ����ø������ݵı�־��Ϥ
				meterRunInfo.setEndflag("1");
		}

		// 4�����������е��жϹ���
		Boolean allIsOK = false;
		MeterRunInfo info = null;
		int j = 0;
		while(!allIsOK){
			j = 0 ;
			for (int i = 1; i <= maxNum; i++) {
				info = runInfos.get(i);
				if (info.getEndflag().equals("0"))
					break;
				if (info.getEndflag().equals("1"))
					j++;
			}
			if (j == maxNum)
				allIsOK = true;
			else
				Debug.sleep(1000);
		}

		if (!dateTimeErrEnd(COM))
			return false;
		return true;
	}

	public static boolean dateTimeErrBegin(String COM) {
		if (Debug_NoPLAT)
			return true;
		// ��׼ʱ�����л� ����  ���̨��Ĳ���
		CommWithRecv commWithRecv = new CommWithRecv();
		String sData = "FE FE FE FE FE 01 A9 03 06 09 17 0D";// ����
		String expect = "FEFEFEFEFE01A906060617";
		String msg = "����ͨ������";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	public static boolean dateTimeErrEnd(String COM) {
		if (Debug_NoPLAT)
			return true;
		// ��׼ʱ�����л� ����  ���̨��Ĳ���
		CommWithRecv commWithRecv = new CommWithRecv();
		String sData = "FE FE FE FE FE 01 A9 03 07 0A 17 0D"; // ����
		String expect = "FEFEFEFEFE01A906060617";
		String msg = "����ͨ������";
		BaseCommLog tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "FE FE FE FE FE 01 FF 06 0C 0C 17";
		expect = "";
		msg = "�㲥��������";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		return true;
	}

	public static Object[] singleDateTimeErrNum(String meter, String COM) {
		Object[] ret = {false,null};
		// ������01H+��ַ(A����Z) +����+11H(����)+(ͨ�����)+У��λ+����(17H)
		// FE FE FE FE FE 01 4C 07 11 34 45 17
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		int max = 6;

		framePlatform.setCONTROL("11");
		framePlatform.setDATA("34");
		sData = framePlatform.getFrame();
		String msg = "��λ" + meter + "�ռ�ʱ��1-" + max + "������ͨ���л�";
		BaseCommLog tmp = null;

		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return ret;


		// FE FE FE FE FE 01 4C 18 27 30 30 30 30 36 30 30 30 30 30 30 33 30 30
		// 30 30 30 30 90 17
		// FE FE FE FE FE 01 4C 18 27 ��30 30 30 30 36 30�� ��30 30 30 30 30 33 30
		// 30 30 30 30 30�� 90 17
		// ���볤ʱ��������״̬��������
		// ������01H + ��ַ(A-Z) +����+ 27H(����)+Ȧ����������(6λ)[60]
		// +����������(12λ)[60*50000 �C HEX ] +ͨ����(35H)+У��λ+����(17H)
		// ������01H+��ַ(A����Z) +����+28H(����)+У��λ+����(17H)
		// ��ȫ��ʹ��̨�����Э����в��� �� ���һ����ʱ�䣬�Ժ���д����ݵĶ�ȡ
		framePlatform.setCONTROL("27");
		framePlatform.setDATA("30 30 30 30 36 30 30 30 30 30 30 33 30 30 30 30 30 30");
		expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "�ռ�ʱ��3-" + max + "����ʼ����ʱ�����";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return ret;

		// ??? �Զ�����FE FE FE FE FE 01 A9 03 06 09 17 0D������

		// FE FE FE FE FE 01 4C 06 28 28 17
		// ������01H+��ַ(A����Z) +����+28H(����)+У��λ+����(17H)

		Debug.sleep(61 * 1000); // ��Ҫ�ȴ�60��

		framePlatform.setCONTROL("28");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
		expect = "getTimeErr:substring(28,52);val(3000000);region(-0.5,0.5);";
		msg = "��λ" + meter + "�ռ�ʱ��4-" + max + "����ȡʱ�����";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		ret[1] = tmp;
		if (tmp.getResult().equals("OK"))
			ret[0] = true;
		return ret;
	}

	// !!!ע���ռ�ʱ���Ĳ��ֲ������������̨���
	// ���������ʱ��ͳһִ��
	public static boolean dateTimeErrNum(String meter, String COM) {
		// ������01H+��ַ(A����Z) +����+11H(����)+(ͨ�����)+У��λ+����(17H)
		// FE FE FE FE FE 01 4C 07 11 34 45 17
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		int max = 6;

		framePlatform.setCONTROL("11");
		framePlatform.setDATA("34");
		sData = framePlatform.getFrame();
		String msg = "��λ" + meter + "�ռ�ʱ��1-" + max + "������ͨ���л�";
		BaseCommLog tmp = null;

		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// ��׼ʱ�����л� ���� û�����ĳ����λ�ģ���ȫ���ģ�
		sData = "FE FE FE FE FE 01 A9 03 06 09 17 0D";// ����
		expect = "FEFEFEFEFE01A906060617";
		msg = "��λ" + meter + "�ռ�ʱ��2-" + max + "������ͨ������";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// FE FE FE FE FE 01 4C 18 27 30 30 30 30 36 30 30 30 30 30 30 33 30 30
		// 30 30 30 30 90 17
		// FE FE FE FE FE 01 4C 18 27 ��30 30 30 30 36 30�� ��30 30 30 30 30 33 30
		// 30 30 30 30 30�� 90 17
		// ���볤ʱ��������״̬��������
		// ������01H + ��ַ(A-Z) +����+ 27H(����)+Ȧ����������(6λ)[60]
		// +����������(12λ)[60*50000 �C HEX ] +ͨ����(35H)+У��λ+����(17H)
		// ������01H+��ַ(A����Z) +����+28H(����)+У��λ+����(17H)
		// ��ȫ��ʹ��̨�����Э����в��� �� ���һ����ʱ�䣬�Ժ���д����ݵĶ�ȡ
		framePlatform.setCONTROL("27");
		framePlatform.setDATA("30 30 30 30 36 30 30 30 30 30 30 33 30 30 30 30 30 30");
		expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		sData = framePlatform.getFrame();
		msg = "��λ" + meter + "�ռ�ʱ��3-" + max + "����ʼ����ʱ�����";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// ??? �Զ�����FE FE FE FE FE 01 A9 03 06 09 17 0D������

		// FE FE FE FE FE 01 4C 06 28 28 17
		// ������01H+��ַ(A����Z) +����+28H(����)+У��λ+����(17H)
		Debug.sleep(61 * 1000); // ��Ҫ�ȴ�60��
		framePlatform.setCONTROL("28");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
		expect = "getTimeErr:substring(28,52);val(3000000);region(-0.5,0.5);";
		msg = "��λ" + meter + "�ռ�ʱ��4-" + max + "����ȡʱ�����";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "FE FE FE FE FE 01 A9 03 07 0A 17 0D";
		expect = "FEFEFEFEFE01A906060617";
		msg = "��λ" + meter + "�ռ�ʱ��5-" + max + "������ͨ������";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		sData = "FE FE FE FE FE 01 FF 06 0C 0C 17";
		expect = "";
		msg = "�㲥�ռ�ʱ��6-" + max + "����������";
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		return true;
		//
	}

	// �������������ּ�����ͨ��ͨ�����
	// ���ؽ����ͨ��IP��ͨ��port���ն�ͨ�ŵ�ַ�������ŵ��б��ŵ����ñ�־
	public static Object[] getUserfulChanel(String meterno, String COM, String model) {
		String[] channels = null; // ��Ҫ��֤�ŵ����������Ⱥ����Ҳ���ŵ�ѡ�õ����ȼ�
		Object[] ret = { "", 0, "", channels, "" }; // IP��port��terminalIP���ŵ��б��ŵ�������Ϣ
		// ͨ��RJ45�ŵ������Ի�ȡ�ն�IP��ַ��Ϣ����֯�ն˽�������ʱ��Ҫ
		// �������ȼ�Ϊ��PS2��ʼ
		if (model.equals("����")) {
//			String[] gwType = { "PS2", "485-1", "RJ45-1", "INFRA" };
			// xuky 2019.02.26 ����ʹ��129.1.22.96������֤���ᵼ�»���
			String[] gwType = { "PS2", "485-1", "INFRA" };
			channels = gwType;
		}
		if (model.equals("����")) {
//			String[] nwType = { "PS2", "485-1", "485-3", "RJ45-1", "INFRA" };
			// xuky 2019.02.26 ����ʹ��129.1.22.96������֤���ᵼ�»���
			String[] nwType = { "PS2", "485-1", "485-3", "INFRA" };
			channels = nwType;
		}
		ret[3] = Util698.array2String(channels);

		ret = checkChannel(meterno, channels, ret, "129.1.22.96");
		if (ret[0].equals(""))
			ret = checkChannel(meterno, channels, ret, "129.1.22."+meterno);
		return ret;
	}

	private static Object[] checkChannel(String meterno, String[] channels, Object[] ret, String terminalIP) {
		String msg = "";
		CommWithRecv commWithRecv = new CommWithRecv();
		String IP = "",  sData, expect;
		int port = 0;
		Object[] o = null;
		BaseCommLog result = null;
		ret[4] = ""; // �ۼ����ͣ�������Ҫ��ǰ���
		for (String type : channels) {
			o = PlatFormUtil.getIPParam(meterno, type);
			IP = (String) o[0];
			port = (int) o[1];
			msg = "��λ" + meterno + "-" + type + "-�ŵ����" + IP + ":" + port;
//			sData = getTermianlFrame(terminalIP, "��ʱ��"); // ע�����ʹ��terminalIP������
//			expect = "68************68940A04969605**************16";
			sData = getTermianlFrame(terminalIP, "��IP��MAC"); // ע�����ʹ��terminalIP������
//			680000C0A87F6068940E04969610C0A87F60023A190200019816
			expect = "68************68940A04969605**************16";
			expect = "68************68940E04969610**************0200019816";
			expect = "68************68940E04969610**********************16";
			result = commWithRecv.deal_one("��" + msg + "��", IP + ":" + port, sData, expect, 2500);
			if (result.getResult().equals("OK")) {
				if (ret[0].equals("")) {
					ret[0] = IP;
					ret[1] = port;
					ret[2] = terminalIP;
				}
				ret[4] += "1";
			} else
				ret[4] += "0";
		}
		return ret;
	}



	// �������������ּ�����ͨ��ͨ�����
	// ���ؽ����ͨ��IP��ͨ��port���ն�ͨ�ŵ�ַ�������ŵ��б��ŵ����ñ�־
	public static Object[] allChanel(String meterno, String COM, String model) {
		String[] typesRJ45 = { "RJ45-0", "RJ45-1", "RJ45-2" }; // ���ܵ�RJ45�ŵ�
		String[] channels = null; // ��Ҫ��֤�ŵ����������Ⱥ����Ҳ���ŵ�ѡ�õ����ȼ�
		Object[] ret = { "", 0, "129.1.22.96", channels, "" }; // IP��port��terminalIP���ŵ��б��ŵ�������Ϣ
		// ͨ��RJ45�ŵ������Ի�ȡ�ն�IP��ַ��Ϣ����֯�ն˽�������ʱ��Ҫ
		// �������ȼ�Ϊ��PS2��ʼ
		if (model.equals("����")) {
			String[] gwType = { "PS2", "485-1", "RJ45", "INFRA" };
			channels = gwType;
		}
		if (model.equals("����")) {
			String[] nwType = { "PS2", "485-1", "485-3", "RJ45", "INFRA" };
			channels = nwType;
		}
		ret[3] = channels;

		String msg = "";
		CommWithRecv commWithRecv = new CommWithRecv();
		String IP = "", terminalIP = "129.1.22.96", sData, expect;
		int port = 0, RJ45Port = -1;
		Object[] o = null;
		BaseCommLog result = null;
		// ���Ƚ���RJ45ͨ�����жϣ�Ҳ��Ϊ�˻�ȡ���������ܵĵ�ַ��Ϣ
		for (String rj45type : typesRJ45) {
			o = PlatFormUtil.getIPParam(meterno, rj45type);
			IP = (String) o[0];
			port = (int) o[1];
			msg = "��λ" + meterno + "-" + rj45type + "-�ն˵�ַ��ȡ" + IP + ":" + port;
			sData = getTermianlFrame(IP, "��ʱ��");
			expect = "68************68940A04969605**************16";
			result = commWithRecv.deal_one("��" + msg + "��", IP + ":7000", sData, expect, 3000);
			if (result.getResult().equals("OK")) {
				terminalIP = IP;
				ret[2] = IP;
				RJ45Port = port;
				break;
			}
		}
		for (String type : channels) {
			if (type.equals("RJ45")) {
				// ����ǰ�����Ϣֱ���ж�
				if (RJ45Port != -1) {
					if (ret[0].equals("")) {
						ret[0] = terminalIP;
						ret[1] = RJ45Port;
					}
					ret[4] += "1";
				} else
					ret[4] += "0";
			} else {
				o = PlatFormUtil.getIPParam(meterno, type);
				IP = (String) o[0];
				port = (int) o[1];
				msg = "��λ" + meterno + "-" + type + "-�ŵ����" + IP + ":" + port;
				sData = getTermianlFrame(terminalIP, "��ʱ��"); // ע�����ʹ��terminalIP������
				expect = "68************68940A04969605**************16";
				result = commWithRecv.deal_one("��" + msg + "��", IP + ":" + port, sData, expect, 3000);
				if (result.getResult().equals("OK")) {
					if (ret[0].equals("")) {
						ret[0] = IP;
						ret[1] = port;
					}
					ret[4] += "1";
				} else
					ret[4] += "0";
			}
		}
		return ret;
	}

	// ����������ǫ̈̄��������
	public void allPlatForm(String meter, String model) {
		// ������Ŀ ���� ����λ����������̨����ع��ܲ���
		// ��Դ���л�Ϊ�ض�ģʽ��������������������λ�ӵ�ѹ������λ�ӵ�ѹ���ŵ���⼰��ȡ
		// ң�Ŷ��ӣ���λ��飨�ȱպϡ���򿪣� �ȿ���̨�塢�ٶ�ȡ����������
		// 00000-11111 = 0 - 31 ���ܹ�32����ϣ����������֤����Ӧ�ļ���������Ӧ���ǣ�����
		String PlatFormCOM = PlatFormParam.getInstance().getPlatFormCOM();
		if (raisePower()) {
			if (chaneMode(model, PlatFormCOM))
				if (singelMeterVoltageOn(meter, PlatFormCOM)) {
					Debug.sleep(15000);
					// ���ؽ����ͨ��IP��ͨ��port���ն�ͨ�ŵ�ַ�������ŵ��б��ŵ����ñ�־
					Object[] param = allChanel(meter, PlatFormCOM, model);
					String IP_port = param[0] + ":" + param[1];
					String terminalIP = (String) param[2];
					if (model.equals("����")) {
						// ң����֤������Ϊ5·ң�Ŷ���
						for (int i = 0; i < 32; i++)
							if (setFSFlag(meter, PlatFormCOM, DataConvert.IntToBinString(i, 8)))
								checkTermail("04 96 96 03", DataConvert.int2HexString(i, 2), IP_port, terminalIP,"","");
					}
					if (model.equals("����")) {
						// ң����֤������Ϊ5·ң�Ŷ���
						String FSFlag = "";
						int FSNum = 5;
						for (int i = 0; i < 64; i++) {
							FSFlag = DataConvert.IntToBinString(i, 8);
							// xuky 2019.02.22 ���ֻ��5·���ӣ����5·��ص����ݲ�������֤
							if (FSNum == 5 && FSFlag.substring(3, 4).equals("1"))
								continue;
							if (setFSFlag(meter, PlatFormCOM, FSFlag)) {
								// ע���̨Э���еĸ�λ(6)��Ӧ�����������еĵ�λ(1)
								FSFlag = PlatFormUtil.nwFSConvert(FSFlag);
								FSFlag = DataConvert.binStr2HexString(FSFlag, 2);
								checkTermail("04 96 96 03", FSFlag, param[0] + ":" + param[1], (String) param[2],"","");
							}
						}
						read12V(meter, PlatFormCOM);
						// 12V��ȡ
						// ��ͣ-����ֱ��ģ������ѹ����ȡ����������
						// ��ͣ-����ֱ��ģ������������ȡ����������
					}
				}
		}
	}

	// ��Σ���ǰ���ն˵�ַ��ͨ�Ų�������λ��Ϣ�����ݱ�λ��Ϣ���Զ��õ���Ҫ���ó�Ϊ���ն˵�ַ����
	// ���أ��µ�IP��ַ��ͬʱҲ���ն˵�ַ����MAC��ַ��Ϣ�������������
	public static Object[] setIP_MAC(String terminlIP, String param, String meterno, String NetIP) {
		BaseCommLog result = null;
		CommWithRecv commWithRecv = new CommWithRecv();
		String dataitem = "04 96 96 01";
		String newTermialAddr = NetIP+meterno;
		meterno = "00"+meterno;
		meterno = meterno.substring(meterno.length()-2,meterno.length());
		String data = Util698.StrIP2HEX(newTermialAddr);
		Object[] mac = PlatFormUtil.getMAC(Util698.getDateTimeSSS_new(), DataConvert.String2Int(meterno), PlatFormParam.getInstance().getPlatFormNO());
		data += mac[1];
		String sData = getTermianlFrame(terminlIP, "14", dataitem, data);
		String msg = "�޸ļ�����IP��MAC ԭ��" + param;
//		String expect = getTermianlFrame(terminlIP, "94", dataitem, "");
		// xuky 2019.02.25 �޸�IP���޷��õ��ظ��ı���
		String expect = "";
		result = commWithRecv.deal_one("��" + msg + "��", param, sData, expect, 1000);
		// xuky 2019.02.25 �޸�IP��ͨ����ȡIP�ķ�ʽ����֤IP��ַ�Ƿ�OK
//		param = NetIP+meterno + ":" + param.split(":")[1];
		sData = getTermianlFrame(newTermialAddr, "��IP��MAC"); // ע�����ʹ��terminalIP������
		expect = "68************68940E04969610"+data+"**16";
		msg = "��֤������IP��MAC �֣�" + param + " MAC��" + mac[1];
		// xuky 2019.02.26 ��Ҫ������ʱ��ȷ���ɹ�
		Debug.sleep(1000);
		param =  NetIP+meterno +":7000";
		result = commWithRecv.deal_one("��" + msg + "��", param, sData, expect, 2000);
		Object[] ret = { NetIP+meterno,mac[1],result };
		return ret;
	}

	public static void main(String[] args) {

	    SimuRun.setFSFlag("FF", "50", "11111111",0);
	    SimuRun.checkTermail("04 96 96 03", "1F", "129.1.22.12:7000", "129.1.22.12","","");
	    SimuRun.setFSFlag("FF", "50", "00000000",1);
	    SimuRun.checkTermail("04 96 96 03", "00", "129.1.22.12:7000", "129.1.22.12","","");

		// 1����Դ
		// simuRun.raisePower();
		// 2���л�ģʽ��2:�������/��������1:����ר��/��������
		// simuRun.chaneMode("32");
		// 3�����е������������ִ��
		// simuRun.singleCase();

		// simuRun.single645Frame();
		// 4��������λ�ӵ�
		// simuRun.singelMeterOn("01");
		// // 5����Դ
		// simuRun.PowerOff();
	}

	{
		// 16
		// 2019-02-21 17:19:41:827 [COM50:9600 nowTimes:1 allTimes:1]
		// comm.CommWithRecv
		// 2019-02-21 17:19:41:827 [COM50:9600
		// SendData:FEFEFEFEFE015307A210B217] comm.CommWithRecv
		// 2019-02-21 17:19:41:868 [COM50:9600
		// RecvComplete:FEFEFEFEFE015306060617]
		// comm.CommWithRecv$ServerHandlerByte
		// 2019-02-21 17:19:42:002 [����λ18ң�����00010000�����Խ�� result=OK]
		// comm.CommWithRecv
		// 2019-02-21 17:19:42:002 [��λ18ң�����00010000] simulation.SimuRun
		// 2019-02-21 17:19:42:009 [129.1.22.205:10002 nowTimes:1 allTimes:1]
		// comm.CommWithRecv
		// 2019-02-21 17:19:42:009 [129.1.22.205:10002
		// SendData:68000081011660681404049696031316] comm.CommWithRecv
		// 2019-02-21 17:19:42:458 [129.1.22.205:10002
		// RecvComplete:6800008101166068940504969603009416]
		// comm.CommWithRecv$ServerHandlerByte
		// 2019-02-21 17:19:42:464 [����֤������������04 96 96 03 ӦΪ20�����Խ�� result=NG]
		// comm.CommWithRecv
		// 2019-02-21 17:19:42:464 [����֤������������04 96 96 03 ӦΪ20�����Խ��
		// expect=680000810116606894050496960320B416] comm.CommWithRecv
		// 2019-02-21 17:19:42:464 [����֤������������04 96 96 03 ӦΪ20�����Խ��
		// recv=6800008101166068940504969603009416] comm.CommWithRecv

	}

}
