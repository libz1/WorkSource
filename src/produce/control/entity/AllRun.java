package produce.control.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import produce.control.comm.CommWithRecv;
import produce.control.comm.FramePlatform;
import produce.control.simulation.MeterRunInfo;
import produce.control.simulation.PlatFormUtil;
import produce.control.simulation.SimuRun;
import util.Util698;

public class AllRun {
	int maxNum = 32;
	Map<String, String> HaveMeters;
	Map<String, BaseCommLog> MeterResults;
	String COM, dataitem, data, sendData,expect;
	Map<String, String> Params;
	Map<String, String> MeterAddrs;
	public AllRun(Map<String, String> HaveMeters,String COM, Map<String, BaseCommLog> meterResults2,Map<String, String> Params,Map<String, String> MeterAddrs, String dataitem, String data, String sendData, String expect){
		this.HaveMeters = HaveMeters;
		this.COM = COM;
		this.MeterResults = meterResults2;
		this.Params = Params;
		this.MeterAddrs = MeterAddrs;
		this.data = data;
		this.dataitem = dataitem;
		this.sendData = sendData;
		this.expect = expect;
	}


	public Map<String, BaseCommLog> run(String fun ){
		Map<Integer, MeterRunInfo> runInfos =  new ConcurrentHashMap<Integer, MeterRunInfo>(); // ��λ�������

		int validNum = 0;
		for (int i = 1; i <= maxNum; i++) {
			String meterno = DataConvert.int2String(i);
			String flag = HaveMeters.get(meterno);
			if (flag!=null && flag.equals("1"))
				validNum++;
		}

		for (int i = 1; i <= maxNum; i++) {

			// 2�����������Ӹ�������
			MeterRunInfo meterRunInfo = new MeterRunInfo();
			String meterno = DataConvert.int2String(i);
			meterRunInfo.setMeterno(meterno);
			runInfos.put(i, meterRunInfo);
			String flag = HaveMeters.get(meterno);
			String param1 = "",meterAddr1="";
			if (Params!= null) param1 = Params.get(meterno);
			if (MeterAddrs!= null) meterAddr1 = MeterAddrs.get(meterno);
			String param = param1,meterAddr=meterAddr1;
//			SimuRun.checkTermail("04 96 96 03", "1F", "129.1.22.12:7000", "129.1.22.12");

			if (flag!=null && flag.equals("1"))
				new Thread(() -> {
					Object[] result = {false,null};
					Method m = null;
					try {
						m = (Method) this.getClass().getMethod(fun,
								new Class[] { Object[].class });
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
					try {
						Object[] args = {meterno,COM, data, dataitem,param,meterAddr,sendData,expect};
						result = (Object[]) m.invoke(this, new Object[] { args });
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}

//					Object[] result = singleDateTimeErrNum(meterRunInfo.getMeterno(),COM,meterRunInfo);
					MeterResults.put(meterno, (BaseCommLog) result[1]);
					// 3��ִ����ϣ����ø������ݵı�־��Ϥ
					meterRunInfo.setEndflag("1");
				}).start();
		}

		// 4�����������е��жϹ���
		Boolean allIsOK = false;
		MeterRunInfo info = null;
		int j = 0;
		while(!allIsOK){
			j = 0 ;
			for (int i = 1; i <= maxNum; i++) {
				info = runInfos.get(i);
				if (info.getEndflag().equals("1"))
					j++;
			}
			if (j == validNum)
				allIsOK = true;
			else
				Debug.sleep(1000);
		}
		return MeterResults;
	}


	public static Object[] singleDateTimeErrNum(Object[] obj) {
		String meter = (String)obj[0];
		String COM = (String)obj[1], expect = (String)obj[7];
		Object[] ret = {false,null};

		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		BaseCommLog tmp = null;
		if (expect == null || expect.equals("")){
			// ������01H+��ַ(A����Z) +����+11H(����)+(ͨ�����)+У��λ+����(17H)
			// FE FE FE FE FE 01 4C 07 11 34 45 17
			String sData = "";
			expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
			int max = 6;

			framePlatform.setCONTROL("11");
			framePlatform.setDATA("34");
			sData = framePlatform.getFrame();
			String msg = "��λ" + meter + "�ռ�ʱ��1-" + max + "������ͨ���л�";

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
		}
		else{
			expect = "get12V:substring(18,26);val(12);region(-0.2,0.2);";
			framePlatform.setCONTROL("B3");
			String sData = framePlatform.getFrame();
			String msg = "��λ" + meter + "ֱ����ѹ����ȡ";
			tmp = commWithRecv.deal_one("��" + msg + "��", "COM" + COM + ":9600:NONE", sData, expect);
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			ret[1] = tmp;
			if (!tmp.getResult().equals("OK"))
				ret[0] = true;

		}
		return ret;
	}

	public static Object[] singleFSRead(Object[] obj) {
		//{meterno,COM, data, dataitem,param,meterAddr};
		String dataitem = (String)obj[3], data= (String)obj[2], sendData = (String)obj[6];
		String param = (String)obj[4], meterAddr= (String)obj[5], expect = (String)obj[7];

		Object[] ret = {true,null};
		ret[1] = SimuRun.checkTermail(dataitem, data, param, meterAddr,sendData,expect);
		return ret;
	}

}
