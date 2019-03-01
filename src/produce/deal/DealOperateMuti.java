package produce.deal;

import java.util.List;

import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceLog;
import produce.entity.ProduceLog2MES;
import produce.entity.ProduceLog2MESDaoImpl;
import produce.entity.ProduceLogDaoImpl;
import util.ObjectPoolDealTestCase1;
import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

public class DealOperateMuti {

	Boolean addrToMES = true;

	private IBaseDao<ProduceLog> iBaseDao_ProduceLog = null;
	private IBaseDao<ProduceLog2MES> iBaseDao_ProduceLog2MES = null;
	private IBaseDao<DevInfo> iBaseDao_DevInfo = null;
	String ADDR = "", MSG = "", ERRADDR = "";
	String RESULT = "";
	ProduceLog produceLog = null;
	ProduceLog2MES produceLog2MES = null;
	DevInfo devInfo1 = null;

	int produceLogID = 0;
	String produceLogOptime = "";

	public String getERRADDR() {
		return ERRADDR;
	}

	public void setERRADDR(String erraddr) {
		ERRADDR = erraddr;
	}

	public DealOperateMuti() {
		iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		iBaseDao_ProduceLog2MES = new ProduceLog2MESDaoImpl();
		iBaseDao_DevInfo = new DevInfoDaoImpl();
		ERRADDR = SoftParameter.getInstance().getERRADDR();
		produceLog = new ProduceLog();
		produceLog2MES = new ProduceLog2MES();
		devInfo1 = new DevInfo();
	}

	public void Start(String addr,int numOfAll) {
		// numOfAll ��ʾ��ǰ��ִ�������Ϣ ���ֱ���1��2��3...
		init();
		Start(addr,numOfAll,0);
	}

	private void init() {
		ADDR = "";
		MSG = "";
		ERRADDR = "";
		RESULT = "";
	}

	public void Start(String addr,int numOfAll,int a) {

		// ADDR = getAddrByBarcode(barcode);

//		Util698.log("Ч�ʷ���", "���ݿ⽻��2 begin1" , Debug.LOG_INFO);
		ADDR = addr;
		// xuky 2018.04.13 �������룬��Ҫnew��ʹ��ԭ�ȵģ�ֻҪ����init����
		produceLog.init();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());
		produceLog.setOpTime(Util698.getDateTimeSSS_new());
		produceLog.setOperation("ɨ������(1)");

//		Util698.log("Ч�ʷ���", "���ݿ⽻��2 begin2" , Debug.LOG_INFO);
		final ProduceLog produceLog_new1 = iBaseDao_ProduceLog.create(produceLog);
		produceLogID = produceLog_new1.getID();
		produceLogOptime = produceLog_new1.getOpTime();
//		Util698.log("Ч�ʷ���", "���ݿ⽻��2 end1" , Debug.LOG_INFO);

		// xuky 2017.06.15 �������γ��򣬷ֱ���������߳���ִ�У����ȼ�����һ���ģ���������쳣�Ĵ���
		MSG = "�豸" + ADDR + " ��ʼ����...";
		String[] s = { "DealOperate", "", MSG };
		PublisherUI.getInstance().publish(s);
		Util698.log(DealOperateMuti.class.getName(), MSG, Debug.LOG_INFO);

//		Util698.log("Ч�ʷ���", "���ݿ⽻��2 end2" , Debug.LOG_INFO);


		BeginTest(produceLogID,numOfAll);

	}

	private void BeginTest(int ID,int numOfAll) {
		String devType = "1";
		{
			// xuky 2017.07.14 ˢ������
			// xuky 2018.03.14 Ϊ�����Ч�ʣ����Ĳ�����������Ч
//			SoftParameter.refreshDataFromDB(); // ���ݿ⽻��2

			// xuky 2017.06.15 ִ�о���ĺ�ʱ�ϳ�������
//			RESULT = DealTestCase.getInstance().Start(SoftParameter.getInstance().getParamValByKey("PLANID"), ID, ADDR);

			try{
				String planid = SoftParameter.getInstance().getParamValByKey("PLANID");
				// xuky 2017.11.07 ����planid�жϲ��Ե��豸����
				Boolean isPool = true;
				String[] rets = null;
				if (isPool){
					ObjectPoolDealTestCase1 objPool = ObjectPoolDealTestCase1.getInstance();
					DealTestCase1 obj = (DealTestCase1)objPool.getObject();
					obj.init();
					rets = obj.Start(planid, ID, ADDR,numOfAll );
					objPool.returnObject(obj);
				}
				else{
					DealTestCase1 DealTestCase1 = new DealTestCase1();
					rets = DealTestCase1.Start(planid, ID, ADDR,numOfAll );
				}
				RESULT = rets[0];
				devType = rets[1];

				// xuky 2017.10.09 ��Ҫ�ر�telnet��������
				if (devType == "2")
					TerminalTelnetSingle.getInstance("").destroy();

			}
			catch(Exception e){
				Util698.log(DealOperateMuti.class.getName(), "DealTestCase1().Start ERROR-"+e.getMessage(), Debug.LOG_INFO);
			}
		}
		if (RESULT.equals("���ڲ���"))
			return;

		// System.out.println("DealOperate["+2+"]");

		// xuky 2017.06.15 ִ����ɣ����ݵõ��Ľ�����з�֧����
		produceLog.init();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setOpTime(Util698.getDateTimeSSS_new());

		if (RESULT.indexOf("�ɹ�")>=0) {
//			playOkSound();
			MSG = "��2���豸" + ADDR + " ���Գɹ���";
			produceLog.setOperation("���Գɹ�(2)");
			ERRADDR = "";

			// xuky 2017.07.25 ��devlist�в�����Ϣ������ҵ���������޸�
			List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ADDR + "'", "");
			DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
			if (devInfo != null) {
				if (devType.equals("2"))
					devInfo.setType("������(2)");
				devInfo.setStatus("�������(1)");
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(Util698.getDateTimeSSS_new());
				iBaseDao_DevInfo.update(devInfo);
			} else {
				devInfo1.init();
				devInfo = devInfo1;
				// xuky 2017.11.07 ��Ҫ�������ͽ����豸��������
				if (devType.equals("2"))
					devInfo.setType("������(2)");
				devInfo.setAddr(ADDR);
				devInfo.setStatus("�������(1)");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(Util698.getDateTimeSSS_new());
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				// xuky 2018.06.26  ���Խ��Ϊ����
				iBaseDao_DevInfo.create(devInfo);
			}
			devInfo = null;
			devInfos = null;

		} else {
//			playErrSound();
			MSG = "�豸" + ADDR + " ����ʧ�ܣ� " + RESULT;
			produceLog.setOperation("����ʧ��(3)");
			produceLog.setOpResult(RESULT);
			ERRADDR = ADDR;

			// xuky 2017.08.31 �ڽ������������豸ʱ�����ִ������Ϊ�쳣
			setErr();

			// ��¼�쳣�豸��ַ��Ϣ��Ҫ���û��������ݴ���
		}
		// xuky 2018.04.17 ֱ������Ϊ�쳣��������������ݼ�¼
//		SoftParameter.getInstance().setERRADDR(ERRADDR);
//		SoftParameter.getInstance().saveParam();

		iBaseDao_ProduceLog.create(produceLog);
		String[] s = { "DealOperate", "", MSG };
		PublisherUI.getInstance().publish(s);
		Util698.log(DealOperateMuti.class.getName(), MSG, Debug.LOG_INFO);

		if (addrToMES){
			try {
				// xuky 2018.06.26 ��Ҫ��ӿڱ���������ݣ����ڽӿڴ�������
				produceLog2MES.init();
				produceLog2MES.setProducelogID(produceLogID);
				produceLog2MES.setAddr(produceLog.getAddr());
				produceLog2MES.setPriority("1");
				produceLog2MES.setOptime_b(produceLogOptime);
				produceLog2MES.setOptime_e(produceLog.getOpTime());
				iBaseDao_ProduceLog2MES.create(produceLog2MES);
			} catch (Exception e) {
				Util698.log(DealOperateMuti.class.getName(), "ERR:" + e.getMessage(), Debug.LOG_INFO);
			}
		}

	}

	public synchronized void setErr() {

		if (ERRADDR.equals(""))
			return;

		produceLog.init();
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setAddr(ERRADDR);
		String datetime = Util698.getDateTimeSSS_new();
		produceLog.setOpTime(datetime);
		produceLog.setOperation("�����豸�쳣(4)");
		iBaseDao_ProduceLog.create(produceLog);

		// �����豸�쳣��Ϣ��

		// xuky 2017.08.01 ��Ҫ��devInfos.barCode�б����豸����ʧ�ܵ���������
		// ��ȡid��Ϣ
		List result = iBaseDao_DevInfo
				.retrieveBySQL("select max(ID) from " + ProduceLog.class.getName() + " where addr = '" + ERRADDR
						+ "' and operation='ɨ������(1)' and workStation ='" + SoftParameter.getInstance().getPCID() + "'");
		int id = 0;
		for (Object o : result) {
			if (o != null)
				id = (Integer) o;
		}
		result = iBaseDao_DevInfo.retrieveBySQL("select name from " + ProduceCaseResult.class.getName()
				+ " where runID = " + id + " order by caseno");
		String name = "";
		for (Object o : result) {
			name = (String) o;
			break;
		}
		result = null;

		// xuky 2017.07.25 ���֮ǰ�Ѿ�����ˣ��Ͳ�Ҫ�������
		List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ERRADDR + "'", "");
		DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
		if (devInfo != null) {
			devInfo.setErrCode(name);
			devInfo.setErrcomputer(SoftParameter.getInstance().getPCID());
			devInfo.setErroperater(SoftParameter.getInstance().getUserManager().getUserid());
			devInfo.setErrdatetime(datetime);
			iBaseDao_DevInfo.update(devInfo);
		} else {
			devInfo1.init();
			devInfo = devInfo1;
			devInfo.setErrCode(name);
			devInfo.setAddr(ERRADDR);
			devInfo.setErrcomputer(SoftParameter.getInstance().getPCID());
			devInfo.setErroperater(SoftParameter.getInstance().getUserManager().getUserid());
			devInfo.setErrdatetime(datetime);
			// xuky 2018.06.26  ���Խ��Ϊ�쳣
			iBaseDao_DevInfo.create(devInfo);
		}
		devInfo = null;
		devInfos = null;
		ADDR = ERRADDR;
		ERRADDR = "";
		MSG = "�豸" + ADDR + " ����Ϊ�豸����״̬��";
		// xuky 2018.07.04 ���ⲿ����publish�����ﲻҪ������
//		String[] s = { "DealOperate", "", MSG };
//		PublisherUI.getInstance().publish(s);
		SoftParameter.getInstance().setERRADDR("");

	}

	public void playOkSound() {
		playSound("media\\3462.wav");
	}

	public void playErrSound() {
		playSound("media\\1822.wav");
	}

	public void playSound(String Filename) {
		// xuky 2018.04.13 ȥ������������
//		newThread(() -> play1(Filename)).start();
	}

	private void play1(String Filename) {
//		try {
//			// ����������һ��Ƶ�ļ�
//			InputStream in = newFileInputStream(Filename);// FIlename
//															// ������ص������ļ���(��game.wav��)
//			// ���������д���һ��AudioStream����
//			AudioStream as = newAudioStream(in);
//			AudioPlayer.player.start(as);// �þ�̬��Աplayer.start��������
//			// AudioPlayer.player.stop(as);//�ر����ֲ���
//			// ���Ҫʵ��ѭ�����ţ��������������ȡ������ġ�AudioPlayer.player.start(as);�����
//			/*
//			 * AudioData data = as.getData(); ContinuousAudioDataStream gg= new
//			 * ContinuousAudioDataStream (data); AudioPlayer.player.start(gg);//
//			 * Play audio.
//			 */
//			// ���Ҫ��һ�� URL ��Ϊ��������Դ(source)����������Ĵ�����ʾ�滻��������������������
//			/*
//			 * AudioStream as = new AudioStream (url.openStream());
//			 */
//		} catch (FileNotFoundException e) {
//			System.out.print("FileNotFoundException ");
//		} catch (IOException e) {
//			System.out.print("�д���!");
//		}
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
