package produce.deal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceLog;
import produce.entity.ProduceLog2MES;
import produce.entity.ProduceLog2MESDaoImpl;
import produce.entity.ProduceLogDaoImpl;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

// xuky 2017.06.15 ����ģʽ�������������ݴ���
public class DealOperate {
	private volatile static DealOperate uniqueInstance;

	Boolean addrToMES = true;

	private IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
	private IBaseDao<ProduceLog2MES> iBaseDao_ProduceLog2MES = new ProduceLog2MESDaoImpl();
	private IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
	String ADDR = "", MSG = "", ERRADDR = "";
	String RESULT = "";
	int produceLogID = 0;
	String produceLogOptime = "";

	public String getERRADDR() {
		return ERRADDR;
	}

	public void setERRADDR(String erraddr) {
		ERRADDR = erraddr;
	}

	public static DealOperate getInstance() {
		if (uniqueInstance == null) {
			synchronized (DealOperate.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new DealOperate();
				}
			}
		}
		return uniqueInstance;
	}

	private DealOperate() {
		ERRADDR = SoftParameter.getInstance().getERRADDR();
	}


	public synchronized void Start(String addr) {


		// ADDR = getAddrByBarcode(barcode);
		ADDR = addr;
		ProduceLog produceLog = new ProduceLog();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());
		produceLog.setOpTime(DateTimeFun.getDateTimeSSS());
		produceLog.setOperation("ɨ������(1)");
		final ProduceLog produceLog_new = iBaseDao_ProduceLog.create(produceLog);
		produceLogID = produceLog_new.getID();
		produceLogOptime = produceLog_new.getOpTime();

		// xuky 2017.06.15 �������γ��򣬷ֱ���������߳���ִ�У����ȼ�����һ���ģ���������쳣�Ĵ���
		new Thread(() -> {
			MSG = "�豸" + ADDR + " ��ʼ����...";
			String[] s = { "DealOperate", "", MSG };
			PublisherUI.getInstance().publish(s);
			Util698.log(DealOperate.class.getName(), MSG, Debug.LOG_INFO);
		}).start();

		new Thread(() -> {
			BeginTest(produceLogID);
		}).start();

	}

	private void BeginTest(int ID) {
		String devType = "1";
		{
			// xuky 2017.07.14 ˢ������
			// xuky 2018.02.07 Ϊ��Լʱ�䣬����������ˢ��,����TerminalParameterController�ĳ�ʼ��������
			String planid = SoftParameter.getInstance().getParamValByKey("PLANID");

			// xuky 2017.11.07 ����planid�жϲ��Ե��豸����

			DealTestCase1 dealTestCase1 = new DealTestCase1();

//			Util698.log("xuky", "temp-dealTestCase1.Start", Debug.LOG_INFO);

			String[] rest = dealTestCase1.Start(planid, ID, ADDR,1 );
			RESULT = rest[0];
			devType = rest[1];
//			Util698.log(DealOperate.class.getName(), "RESULT-"+RESULT, Debug.LOG_INFO);

			// xuky 2017.10.09 ��Ҫ�ر�telnet��������
			TerminalTelnetSingle.getInstance("").destroy();

		}
		if (RESULT.equals("���ڲ���"))
			return;

		// System.out.println("DealOperate["+2+"]");

		// xuky 2017.06.15 ִ����ɣ����ݵõ��Ľ�����з�֧����
		ProduceLog produceLog = new ProduceLog();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setOpTime(DateTimeFun.getDateTimeSSS());

		if (RESULT.indexOf("�ɹ�")>=0) {
			playOkSound();
			MSG = "�豸" + ADDR + " ���Գɹ���";
			produceLog.setOperation("���Գɹ�(2)");
			ERRADDR = "";

			// xuky 2017.07.25 ��devlist�в�����Ϣ������ҵ���������޸�
			String[] s = { "DealOperate", "", "���ڴ洢�豸��Ϣ�����Ե�..." };
			PublisherUI.getInstance().publish(s);

			List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ADDR + "'", "");
			DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
			if (devInfo != null) {
				devInfo.setStatus("�������(1)");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(DateTimeFun.getDateTimeSSS());
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				String is_setid = SoftParameter.getInstance().getIS_SETID();
				if (is_setid.equals("��ģ��"))
					devInfo.setType("��ģ��(3)");
				if (is_setid.equals("·��"))
					devInfo.setType("·��(4)");
				iBaseDao_DevInfo.update(devInfo);
			} else {
				devInfo = new DevInfo();
				// xuky 2017.11.07 ��Ҫ�������ͽ����豸��������
				if (devType.equals("2"))
					devInfo.setType("������(2)");

				String is_setid = SoftParameter.getInstance().getIS_SETID();
				if (is_setid.equals("��ģ��"))
					devInfo.setType("��ģ��(3)");
				if (is_setid.equals("·��"))
					devInfo.setType("·��(4)");

				devInfo.setAddr(ADDR);
				devInfo.setStatus("�������(1)");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(DateTimeFun.getDateTimeSSS());
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				// xuky 2018.06.26  ���Խ��Ϊ����
				iBaseDao_DevInfo.create(devInfo);
			}

		} else {
			playErrSound();
			MSG = "�豸" + ADDR + " ����ʧ�ܣ� " + RESULT;
			produceLog.setOperation("����ʧ��(3)");
			produceLog.setOpResult(RESULT);
			ERRADDR = ADDR;

			// ��¼�쳣�豸��ַ��Ϣ��Ҫ���û��������ݴ���
		}

		// xuky 2017.08.29 �����ʱ��ȷ��������ʱ��������
		// ��Ŀǰ�����գ�system.out.println   logger�Ĳ������Ƕ��߳��еĲ���
//		Debug.sleep(500);

		iBaseDao_ProduceLog.create(produceLog);
		String op_status = "";
		// 2019.02.13 ��Ϊ�豸��Ϣ�ǵ�һ�ģ�֮ǰ��NG�ᱻ�����OK����
		if (RESULT.indexOf("�ɹ�")>=0)
			op_status = "OK";
		else
			op_status = "NG";

		if (addrToMES){
			try {
				// xuky 2018.06.26 ��Ҫ��ӿڱ���������ݣ����ڽӿڴ�������
				ProduceLog2MES produceLog2MES = new ProduceLog2MES();
				produceLog2MES.init();
				produceLog2MES.setProducelogID(produceLogID);
				produceLog2MES.setAddr(produceLog.getAddr());
				produceLog2MES.setPriority("1");
				produceLog2MES.setOptime_b(produceLogOptime);
				produceLog2MES.setOptime_e(produceLog.getOpTime());
				produceLog2MES.setOp_status(op_status);
				iBaseDao_ProduceLog2MES.create(produceLog2MES);
			} catch (Exception e) {
				Util698.log(DealOperate.class.getName(), "ERR:" + e.getMessage(), Debug.LOG_INFO);
			}
		}

		String[] s = { "DealOperate", "", MSG };
		PublisherUI.getInstance().publish(s);
		Util698.log(DealOperate.class.getName(), MSG, Debug.LOG_INFO);

		SoftParameter.getInstance().setERRADDR(ERRADDR);
		SoftParameter.getInstance().saveParam();


	}

	public synchronized void setErr() {

		if (ERRADDR.equals("")){
			String[] s = { "DealOperate", "", "���쳣�豸" };
			PublisherUI.getInstance().publish(s);
			return;
		}

		ProduceLog produceLog = new ProduceLog();
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setAddr(ERRADDR);
		String datetime = DateTimeFun.getDateTimeSSS();
		produceLog.setOpTime(datetime);
		produceLog.setOperation("�����豸�쳣(4)");
		iBaseDao_ProduceLog.create(produceLog);

		// �����豸�쳣��Ϣ��

		// xuky 2017.08.01 ��Ҫ��devInfos.barCode�б����豸����ʧ�ܵ���������
		// ��ȡid��Ϣ
		IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
//		List result = iBaseDao_DevInfo
//				.retrieveBySQL("select max(ID) from " + ProduceLog.class.getName() + " where addr = '" + ERRADDR
//						+ "' and operation='ɨ������(1)' and workStation ='" + SoftParameter.getInstance().getPCID() + "'");
		// xuky 2017.09.07 ��Ϊ����������һ̨�豸����ɨ�����������ȡ����workStation������
		List result = iBaseDao_DevInfo
		.retrieveBySQL("select max(ID) from " + ProduceLog.class.getName() + " where addr = '" + ERRADDR
				+ "' and operation='ɨ������(1)'" );
		int id = 0;
		for (Object o : result) {
			if (o != null)
				id = (Integer) o;
		}
		result = iBaseDao_DevInfo.retrieveBySQL("select name from " + ProduceCaseResult.class.getName()
				+ " where runID = " + id + " order by caseno desc");
		String name = "";
		for (Object o : result) {
			name = (String) o;
			break;
		}

		// xuky 2017.07.25 ���֮ǰ�Ѿ�����ˣ��Ͳ�Ҫ�������
		List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ERRADDR + "'", "");
		DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
		String is_setid = SoftParameter.getInstance().getIS_SETID();

		if (devInfo != null) {
			if (is_setid.equals("��ģ��"))
				devInfo.setType("��ģ��(3)");
			if (is_setid.equals("·��"))
				devInfo.setType("·��(4)");

			devInfo.setStatus("�豸����(2)");
			devInfo.setErrCode(name);
			devInfo.setErrcomputer(SoftParameter.getInstance().getPCID());
			devInfo.setErroperater(SoftParameter.getInstance().getUserManager().getUserid());
			devInfo.setErrdatetime(datetime);
			iBaseDao_DevInfo.update(devInfo);
		} else {
			devInfo = new DevInfo();
			if (is_setid.equals("��ģ��"))
				devInfo.setType("��ģ��(3)");
			if (is_setid.equals("·��"))
				devInfo.setType("·��(4)");
			devInfo.setErrCode(name);
			devInfo.setAddr(ERRADDR);
			devInfo.setErrcomputer(SoftParameter.getInstance().getPCID());
			devInfo.setErroperater(SoftParameter.getInstance().getUserManager().getUserid());
			devInfo.setErrdatetime(datetime);
			// xuky 2018.06.26  ���Խ��Ϊ�쳣
			iBaseDao_DevInfo.create(devInfo);
		}

		ADDR = ERRADDR;
		ERRADDR = "";
		MSG = "�豸" + ADDR + " ����Ϊ�豸����״̬��";
		String[] s = { "DealOperate", "", MSG };
		PublisherUI.getInstance().publish(s);

		SoftParameter.getInstance().setERRADDR("");
		SoftParameter.getInstance().saveParam();


	}

	public void playOkSound() {
		playSound("media\\3462.wav");
	}

	public void playErrSound() {
		playSound("media\\1822.wav");
	}

	public void playSound(String Filename) {
		new Thread(() -> play1(Filename)).start();
	}

	private void play1(String Filename) {
		try {
			// ����������һ��Ƶ�ļ�
			InputStream in = new FileInputStream(Filename);// FIlename
															// ������ص������ļ���(��game.wav��)
			// ���������д���һ��AudioStream����
			AudioStream as = new AudioStream(in);
			AudioPlayer.player.start(as);// �þ�̬��Աplayer.start��������
			// AudioPlayer.player.stop(as);//�ر����ֲ���
			// ���Ҫʵ��ѭ�����ţ��������������ȡ������ġ�AudioPlayer.player.start(as);�����
			/*
			 * AudioData data = as.getData(); ContinuousAudioDataStream gg= new
			 * ContinuousAudioDataStream (data); AudioPlayer.player.start(gg);//
			 * Play audio.
			 */
			// ���Ҫ��һ�� URL ��Ϊ��������Դ(source)����������Ĵ�����ʾ�滻��������������������
			/*
			 * AudioStream as = new AudioStream (url.openStream());
			 */
		} catch (FileNotFoundException e) {
			System.out.print("FileNotFoundException ");
		} catch (IOException e) {
			System.out.print("�д���!");
		}
	}

	public static void main(String[] arg) {
		String barcode = "03191ZC00000001703653816";

		System.out.println(barcode);
		System.out.println(Util698.getAddrByBarcode(barcode));

		barcode = "63012600017046444A";
		System.out.println(barcode);
		System.out.println(Util698.getAddrByBarcode(barcode));

		// System.out.println(new
		// DealOperate().getAddrByBarcode("001703653816"));

	}

}
