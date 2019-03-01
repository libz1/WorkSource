package socket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import entity.SerialParam;
import mina.JavaUDPClient;
import mina.MinaSerialServer;
import mina.MinaUDPServer;
import produce.deal.TerminalTelnetSingle;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseResultDaoImpl;
import util.Publisher;
import util.PublisherFrame;
import util.PublisherShowList;
import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

// ���������ݵ��߳�
public class DealSendData implements Observer {
	// xuky 2018.07.16 ����̳߳صķ�ʽ�����̵߳���
//	ExecutorService pool = Executors.newFixedThreadPool(15);
	// xuky 2018.08.02 ͨ�� ThreadPoolExecutor�ķ�ʽ
	// �ο� https://www.cnblogs.com/zedosu/p/6665306.html
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	private volatile static DealSendData uniqueInstance;

	private Boolean isRuning = true;
	public Boolean getIsRuning() {
		return isRuning;
	}
	public void setIsRuning(Boolean isRuning) {
		this.isRuning = isRuning;
	}

	// xuky 2018.03.14 ���ִ��Ч�� ȥ�������չʾ����
	Boolean RUNFASTER = SoftParameter.getInstance().getRUNFASTER();

	private Map<String, ProduceCaseResult> mapDealData = null;
	private IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult;
	private String returnResult = "";
	private String TERMINAL_IP = "192.168.1.96";

	// xuky 2017.10.12 ���ڻظ�������
	private String replyData = "";

	// xuky 2018.02.07 ���������Ŀ
	private String LockBeginTime = ""; // ��ʼ����ʱ��
	private long lockTime = 1000;   // ��Ҫ����ʱ�䳤
	private String LockADDR = ""; // �����������ƣ��������Ľ�������
	boolean plc_in_front = false;  // ����ز��������ں��棬��������������ж���


	// xuky 2018.01.22 ��MinaUDPServer.DealData�����У����ô�getReplyData��������жϵȲ���
	public String getReplyData() {
		return replyData;
	}

	public static DealSendData getInstance() {
		if (uniqueInstance == null) {
			synchronized (DealSendData.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new DealSendData();
				}
			}
		}
		return uniqueInstance;
	}
	// xuky 2018.02.07 �ж��Ƿ����������
	public synchronized boolean isLock(ProduceCaseResult p){

		// xuky 2018.03.14 ��������  ��Ч
		// xuky 2018.02.08 ����������Ч��һ��
		if (!plc_in_front) return false;

		boolean ret = false;
		String time = LockBeginTime;

		// �������ַ������Ӱ�� �����緢�����ز��󣬿���ֱ�ӷ��ͺ���
		if (p == null || p.getADDR().equals(LockADDR)){
//			Util698.log("xuky","�������жϣ�  ����������",Debug.LOG_INFO);
//			System.out.println("�������жϣ�  ����������");
			return ret;
		}
		if (p == null || p.getADDR().equals(LockADDR)){
			Util698.log("xuky","�������жϣ�  p.getADDR():"+p.getADDR() + " LockADDR:"+LockADDR,Debug.LOG_INFO);
			return ret;
		}

		if (!time.equals("")){
			String nowTime = Util698.getDateTimeSSS_new();
			// �ж��Ƿ�ʱ
			if ((Util698.getMilliSecondBetween_new(nowTime, time)) > lockTime){
				// ֻ����������н�������
				LockBeginTime = "";
				Util698.log("xuky","������  nowTime:"+nowTime + " LockBeginTime:"+time+" LockADDR:"+LockADDR,Debug.LOG_INFO);
			}
			else{
				ret = true;
				Util698.log("xuky","����������  nowTime:"+nowTime + " LockBeginTime:"+time+" LockADDR"+LockADDR,Debug.LOG_INFO);
			}
		}
		return ret;
	}

	private DealSendData() {
		Publisher.getInstance().addObserver(this);
		PublisherUI.getInstance().addObserver(this);
		PublisherFrame.getInstance().addObserver(this);
		iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
		mapDealData = new ConcurrentHashMap<String, ProduceCaseResult>();

		new Thread() {
			@Override
			public void run() {
				while (isRuning) {
					if (isLock(SendData.getInstance().getFirst()))
						continue;

					// xuky 2018.07.03 ���300�������ʱ�� ��ֹ�������µ��쳣���ݣ���1����result=Ϊ�գ���nameyҲ�Ǻ������������ģ�֮ǰ�Ĳ����������ݱ�������
//					2018-07-03 12:18:21:287 [ProduceCaseResult.update��000000098062-�����PLCM1643����(FE)[SAVEID]-3018487-DealData result=] socket.DealSendData
//					2018-07-03 12:18:21:295 [1run  �˿�:1 ִ�з���������-begin taskID:000000098061.30 �����PLCM1643����(FE)[SAVEID]] socket.DealSendData
//					2018-07-03 12:18:21:305 [ProduceCaseResult.create��000000098062-�����PLCM1643����(FE)[SAVEID]-3018496] deal.DealTestCase1
					Debug.sleep(300);
					// �ӷ��Ͷ�����ȡ�����ݲ�ִ�з��͹���
					ProduceCaseResult p = SendData.getInstance().pop();

					if (p != null) {
						if (!RUNFASTER)
							Util698.log(DealSendData.class.getName(),
								"1run "+" �˿�:"+p.getPort() +" ִ�з���������-begin taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName() ,
								Debug.LOG_INFO);
						mapDealData.put(p.getPort(), p);
						sendData_new(mapDealData, p);
					}

					checkData();
				}
			}
		}.start();

		ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
		long initialDelay = 0;
		long period = 1;
		// ÿ��1������ִ��һ��job1����2���ڵ�һ��ִ����ɺ�n��
		service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MINUTES);

	}
	class MyScheduledExecutor implements Runnable {
		@Override
		public void run() {
//			Util698.log(DealSendData.class.getName(),
//					"ScheduledExecutorService��ʽ ִ������ĳ�ʱ���  map.size=" + mapDealData.size(),
//					Debug.LOG_INFO);
			checkData();
		}
	}

	public synchronized void checkData(){

		if (!isRuning)
			return;

		// ����map��1�֡�   �ж��Ƿ���ڳ�ʱ�����
		Iterator<String> iter = mapDealData.keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			String addr = (String) iter.next();
			ProduceCaseResult p = mapDealData.get(addr);

			if (p == null)
				continue;

			String beginTime = p.getSendtime();
			// xuky 2017.11.29 ��Ϊǰ���sendData�����߳���ִ�еģ����Կ���ʵ��ִ��
			if (beginTime == null || beginTime.equals("")) {
				Debug.sleep(100);
				continue;
			}

			String nowTime = Util698.getDateTimeSSS_new();
			// �ж��Ƿ�ʱ
			long diff = Util698.getMilliSecondBetween_new(nowTime, beginTime);
			// xuky 2018.07.23 ������Ҫ�жϵĳ�ʱʱ��Ϊ�ȴ�ʱ��+��ǰ����ʱ��֮��
			if (diff > (long)(p.getWaittime()+p.getDelaytime())) {
				if (p.getSendtimes() > p.getRetrys()) {
					mapDealData.remove(p.getPort());
//					System.out.println("map.remove:"+p.getPort()+"map.size:"+mapDealData.size());

					// xuky 2018.04.24 ����豸ͨ�ų�ʱ�ˣ��Ϳ��Խ�������豸
					DealSendBlockLock.getInstance().removeAddr(p.getADDR(),"��ʱ");

					if (!RUNFASTER)
						Util698.log(DealSendData.class.getName(),
							"���������� ��ʱ�����Զ�� :" + p.getADDR() + "." + p.getCaseno(), Debug.LOG_INFO);
					p.setRecvtime(Util698.getDateTimeSSS_new());
					p.setResult("��ʱ");

					// xuky 2018.01.30 �����������
					// II������ʡ��ģʽ�󣬿��ܻ��л����ʣ������޷��ϱ�ȷ�ϱ���
					// Ϊ��ȷ�����̼���ִ�У�������������Ϊ�ɹ�
					if (p.getWaittime()==0){
						p.setResult("�ɹ�");
					}

					// ��ʱʱ��д������ 					xukyxuky
					iBaseDao_ProduceCaseResult.update(p);
//					Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-��ʱ", Debug.LOG_INFO);

					ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
					Object[] s21 = { "DealTestCase", "old", produceCaseResult_tmp,"��ʱ" };
					PublisherShowList.getInstance().publish(s21);
					produceCaseResult_tmp = null;
					p = null;
				} else {
					// xuky 2017.11.29 sendData��ִ�еĴ�����ܵ��³�ʱ������Ч
					// ����telnet��ִ�з�ʽ����Ϊû�����߳���ִ��
					Util698.log(DealSendData.class.getName(),
							"����������taskID:" + p.getADDR() + "." + p.getCaseno() +" ��ʱ�ط�"+i+" nowTime"+nowTime+" beginTime"+beginTime+" Waittime"+p.getWaittime()+" diff"+diff, Debug.LOG_INFO);
					sendData_new(mapDealData, p);
					// System.out.println("sendData(map,p) ");
//					Debug.sleep(300);
				}
			}
			i++;
			// xuky 2018.07.04 �����ʱ����ֹδ��ʱִ�з��ͳ���ʱ����ִ������Ĵ���
			// xuky 2018.07.06  ȡ������Ϊ�ȴ�ʱ�����
//			Debug.sleep(200);
		};


	}
	// xuky 2017.11.29 ���߳���ִ�У��Դ���������ʱ������Ч
	public synchronized void sendData_new(Map<String, ProduceCaseResult> map1, ProduceCaseResult p) {

		// xuky 2018.07.24 ��ǰ��ʱ����Щ��������߳���ִ�У��ô��ǣ��������������Ӧ�����ˣ������ǣ����̳߳���ִ�У������в�ȷ������ʱ
		// ���Կ��� �� 11:01:54:014��11:01:54,222 �бȽϴ����ʱ ���м�������������ͬ������
//		bad
//		2018-07-24 11:01:54,014 [1run  �˿�:RT ִ����������-begin taskID:000000098061.60 �ز��޵�ַ��485��] socket.DealSendBlockData
//		2018-07-24 11:01:54,090 [ListShow.testDetailList produce.entity.ProduceCaseResult@719a5446] deal.TerminalParameterController
//		2018-07-24 11:01:54,090 [xuky add Detail row:-1 p1�ز��޵�ַ��485��000000098062.60-����-����2018-07-24 11:01:53:987-���:] deal.TerminalParameterController
//		2018-07-24 11:01:54,222 [sendDataInThread sendTime=2018-07-24 11:01:54:014 Sendtimes=1] socket.DealSendData

//		good
//		2018-07-24 11:01:56,362 [1run  �˿�:RT ִ����������-begin taskID:000000098064.60 �ز��޵�ַ��485��] socket.DealSendBlockData
//		2018-07-24 11:01:56,362 [sendDataInThread sendTime=2018-07-24 11:01:56:362 Sendtimes=1] socket.DealSendData


		// xuky 2018.07.04 ����ִ�����´��룬��Ϊ�����ѭ���ܿ컹���жϴ�setSendtime��Ϣ
		// ��д����ʱ��
		// �������ʹ���
		String sendTime = Util698.getDateTimeSSS_new();
		p.setSendtime(sendTime);
//		Util698.log(DealSendData.class.getName(), "set sendTime��"+ sendTime +" " + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-sendDataInThread", Debug.LOG_INFO);
		p.setSendtimes(p.getSendtimes() + 1);

		// xuky 2018.07.18 ThreadPool
		pool.submit(new sendThread(map1, p));
	}

	public class sendThread extends Thread {
		Map<String, ProduceCaseResult> map1;
		ProduceCaseResult p;
    	public sendThread(Map<String, ProduceCaseResult> map1, ProduceCaseResult p) {
    		this.map1 = map1;
    		this.p = p;
    		super.setName("sendThread");
    	}
        @Override
        public void run() {
			Util698.log(DealSendData.class.getName(), "sendDataInThread "+p.getADDR()+"."+p.getCaseno()+" sendTime="+p.getSendtime()+" Sendtimes="+p.getSendtimes(), Debug.LOG_INFO);
			sendDataInThread(map1, p);
        }
    }



	public void sendDataInThread(Map<String, ProduceCaseResult> map1, ProduceCaseResult p) {


		// xuky 2017.08.24 ��ǰ��ʱ  ��������ʱ���Ժ�   ����������ǰ��ʱ��д��sendtime��ì�ܵģ�����Э����
		// ì�ܵĽ����  diff > (long)(p.getWaittime()+p.getDelaytime()
		Debug.sleep(p.getDelaytime());

		// ����ǰ�����ն�IP��ַ��Ϣ
		TERMINAL_IP = SoftParameter.getInstance().getTERMINAL_IP();

		// xuky 2018.07.06  ��ʱ�ж�������������ٽ��  ��ʱ�ĺ������������������õ�������
//		2018-07-06 10:46:45:354 [����������taskID:000000098063.30 ��ʱ�ط�0] socket.DealSendData
//		2018-07-06 10:46:45:462 [3recv �˿�:3 user data��1��:FFFEFEFEFE686380090000006891243433B337837F768064696866607C7C5BA96463656564655C60646B6369656C5353535353D216] socket.DealData
		if (map1.get(p.getPort()) == null){
			Util698.log(DealSendData.class.getName(), "sendDataInThread��ProduceCaseResult.update����ǰ����Ƿ������map1�У�" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-sendDataInThread", Debug.LOG_INFO);
			return;
		}

		// xuky 2018.07.03 ���˶δ����ִ�д�����ǰ�ᣬ��Ȼ��ʾ�Ĵ����Ǵ����
		iBaseDao_ProduceCaseResult.update(p);
//		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-sendDataInThread", Debug.LOG_INFO);

		// xuky 2018.02.07 �趨����ʱ��
		if (p.getName().indexOf("�ز�") >= 0){
			LockBeginTime = Util698.getDateTimeSSS_new();
			LockADDR = p.getADDR();
		}

		String port = p.getPort();
		if (p.getName().indexOf("����") >= 0) {

			deal_Rate(p, port);

		} else if (p.getProtocol().indexOf("698.45") >= 0) {
			deal_69845(p);

		} else if (p.getPort().indexOf("udp-client") >= 0) {
			deal_udpclient(p);

		} else if (p.getPort().indexOf("udp-server") >= 0) {
			deal_udpserver(p);

		} else if (p.getProtocol().equals("dos")) {
			deal_dos(map1, p, port);

			Util698.log(DealSendData.class.getName(),
					"ִ�з���������-end1 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
					Debug.LOG_INFO);

			return;

		} else if (p.getProtocol().equals("telnet")) {
			deal_telnet(p, port);
			Util698.log(DealSendData.class.getName(),
					"ִ�з���������-end2 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
					Debug.LOG_INFO);
			return;

		} else {

			// 4��ͨ�����ڷ�ʽ����ͨ��

			if (p.getName().indexOf("�ȴ�") >= 0) {
				// ���������� ����deal_udpserver�Ĵ������
				replyData = p.getSend();
//				System.out.println("replyData=" + replyData + " �ȴ��ϱ�����");
			} else {
				boolean ret_bool = false;

//				// xuky 2018.01.24 ����ʱ��Ҫ���·���ʱ������send0�����ݻظ����״εķ�������
				String send0 = p.getSend0();
				if (send0.indexOf("&&")>=0) {
					Util698.log(DealSendData.class.getName(), "&& ���ݴ���", Debug.LOG_INFO);
					p.setSend(Util698.DealFrameWithParam(send0.split("&&")[0], p.getADDR(), p.getProtocol()));
					p.setExpect(Util698.DealFrameWithParam(send0.split("&&")[1], p.getADDR(), p.getProtocol()));
					p.setWaitReply(true);
					iBaseDao_ProduceCaseResult.update(p);
					Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-&& ���ݴ���", Debug.LOG_INFO);
				}

				ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
				Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"sendDataInThread"+"-"+this };
				Util698.log(DealSendData.class.getName(), "DealTestCase.old(sendDataInThread) " + produceCaseResult_tmp+" "+produceCaseResult_tmp.getADDR()+"."+produceCaseResult_tmp.getCaseno(),Debug.LOG_INFO);
				PublisherShowList.getInstance().publish(s2);

				// xuky 2018.01.24 ��Ӵ��ڷ�����־
				if (!RUNFASTER)
					Util698.log(DealSendData.class.getName(), "2send �˿�:"+p.getPort()+ " Data:" + p.getSend(), Debug.LOG_INFO);

				try{
					ret_bool = SocketServerEast.sendData(p.getSend(), p.getPort());
				}
				catch(Exception e){
					ret_bool = false;
					Util698.log(DealSendData.class.getName(), "send Exception:"+e.getMessage(), Debug.LOG_INFO);
				}

				// ��Ϊ����ʧ�ܣ�����������ֱ�Ӵ�map1��ɾ�� ���������Ӧ���ڳ�ʱ���ǽ��յ����ݺ����ɾ��
				if (ret_bool == false) {

					map1.remove(p.getPort());
//					System.out.println("map.remove:"+p.getPort()+"map.size:"+mapDealData.size());

					// xuky 2018.04.24 ����豸�Ѿ��޷�ͨ���ˣ��ͽ�������豸
					DealSendBlockLock.getInstance().removeAddr(p.getADDR(),"����ʧ��");

					// xuky 2018.01.25 �ж������Ƿ�Ϊ������������ǣ���Ϊ�����Ѿ�ִ������˾�ȥ�޸�DealSendBlockData�Ķ�Ӧ״̬
					if (p.getIsBlockTask()){
						DealSendBlockData.getInstance().setISBUSY(false);
					}
					// System.out.println("map.remove2 "+
					// p.getADDR()+"."+p.getCaseno());

					String msg = "δ�򿪴���";
					p.setResult(msg);
					iBaseDao_ProduceCaseResult.update(p);
					Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-"+msg, Debug.LOG_INFO);

					// ��Ϊû�д򿪴��ڣ���ʱ��ɾ��p����

					// xuky 2017.07.05 ���Ͳ��Թ�����Ϣ
					ProduceCaseResult produceCaseResult_tmp3 = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
					Object[] s3 = { "DealTestCase", "old", produceCaseResult_tmp3,"δ�򿪴���" };
					PublisherShowList.getInstance().publish(s3);
					produceCaseResult_tmp = null;

					Util698.log(DealSendData.class.getName(),
							"ִ�з���������-end3 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
							Debug.LOG_INFO);
					p = null;
					// ����������˳����������
					return;
				}
			}
		}

		// xuky 2018.07.24 ��������������Щ���ᵽǰ�棬����Ŀǰֻ����Դ���ͨ�Ž����˵�������A������ʱ������
//		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
//		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"sendDataInThread"+"-"+this };
//		Util698.log(DealSendData.class.getName(), "DealTestCase.old(sendDataInThread) " + produceCaseResult_tmp,Debug.LOG_INFO);
//		PublisherShowList.getInstance().publish(s2);


		if (p.getName().indexOf("����") >= 0) {
			String[] s = { "recv frame", "user data", "ok- ", port };
			PublisherFrame.getInstance().publish(s);
		}
		if (p.getPort().indexOf("udp-client") >= 0) {
			String[] s = { "recv frame", "user data", returnResult, port };
			PublisherFrame.getInstance().publish(s);
		}

//		if (!RUNFASTER)
//			Util698.log(DealSendData.class.getName(),
//				"ִ�з���������-end4 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
//				Debug.LOG_INFO);

	}

	private void deal_telnet(ProduceCaseResult p, String port) {
		// 3��ͨ��telnetͨ��
		// Util698.log(DealSendData.class.getName(),
		// "TerminalTelnetSingle.getInstance", Debug.LOG_INFO);

		TerminalTelnetSingle terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP);
		String key = p.getSend(), val = p.getExpect();

		// xuky 2017.10.17
		// 1�������ֵģ���ʾ��Ҫֱ��ִ��linux����
		// 2����������ظ����ݵĸ�ʽ��xxx%����ʾ�ж��Ƿ������xxx����
		// 3��������ص����ݳ���С��100������ͷȥβ��������������������������ص���ʾ����Ϣ�������������м䲿������
		if (!Util698.isNumber(key)) {
			// 1�������ֵģ���ʾ��Ҫֱ��ִ��linux����
			String end = "[root@(none) /]#";

			if (key.indexOf("dn") == 0) {
				// xuky 2018.01.08 ���ó�ʱʱ��
				TerminalTelnetSingle.getInstance("").destroy();
				terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP, 0);
			} else if (!terminalTelnet.getREADSTR().endsWith(end)) {
				// �����ǰ����ʾ������ [root@(none) /]#����Ҫ��������
				TerminalTelnetSingle.getInstance("").destroy();
				terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP);
			}
			// Util698.log(DealSendData.class.getName(), "linux: finish
			// init", Debug.LOG_INFO);
			String ret = terminalTelnet.writeThenReadUtil(key, end);

			// xuky 2017.11.17 ����޷������նˣ��޷��������ݣ���Ҫ�������µĴ���
			if (ret == null || ret.equals(""))
				ret = " ";
			else
				// ��ͷȥβ��������������������������ص���ʾ����Ϣ�������������м䲿������
				ret = ret.substring(key.length() + 2, ret.length() - end.length() - 2);

			String result = ret;
			// 3��������ص����ݳ���С��100������ͷȥβ��������������������������ص���ʾ����Ϣ�������������м䲿������
			// if (ret.length() > 100)
			// result = " ";

			// 2����������ظ����ݵĸ�ʽ��xxx%����ʾ�ж��Ƿ������xxx����
			if (val.endsWith("%")) {
				val = val.substring(0, val.length() - 1);
				if (ret.indexOf(val) >= 0)
					returnResult = "ok-" + result;
				else
					returnResult = "err-" + result;
			} else {
				// 2����������ظ����ݵĸ�ʽ��%xxx����ʾ�ж��Ƿ�xxxΪ���������� �ж������Ƿ�ɹ������������ĳɹ�
				if (val.startsWith("%")) {
					val = val.substring(1, val.length());
					if (ret.endsWith(val)) {
						// xuky 2017.11.07 ��ֹ���������ǡ����ɹ���
						if (ret.endsWith("��" + val))
							returnResult = "err-" + result;
						else
							returnResult = "ok-" + result;
					} else
						returnResult = "err-" + result;
				} else {
					if (ret.equals(val))
						returnResult = "ok-" + result;
					else
						returnResult = "err-" + result;
				}
			}

			if (key.indexOf("dn") == 0) {
				TerminalTelnetSingle.getInstance("").destroy();
				// xuky 2018.01.08 �ָ�Ĭ�ϳ�ʱʱ��
				terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP);

			}

		} else {
			// ���ò���
			if (terminalTelnet.changeParam(key, val) == false) {
				// changeParam����false,��ʾtelnet�����쳣����ʱ������л�Ӧ���ȴ���ʱ����
				return;
			}
			// xuky 2017.10.09 �����������Ϊ�գ���ʾ������ظ�����
			if (val == null || val.equals("")) {
				returnResult = "ok- ";
			} else {
				// ��֤����
				String[] result = terminalTelnet.verify(key, val);
				if (result[1].equals("1"))
					returnResult = "ok-" + result[0];
				else
					returnResult = "err-" + result[0];
			}

		}

		// xuky 2017.11.29 �����﷢����Ϣ���Ա����������и�������
		// telentʱ�Ĵ������
		iBaseDao_ProduceCaseResult.update(p);
		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-telnet", Debug.LOG_INFO);

		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"deal_telnet" };
		PublisherShowList.getInstance().publish(s2);
		produceCaseResult_tmp = null;

		String[] s = { "recv frame", "user data", returnResult, port };
		PublisherFrame.getInstance().publish(s);

		return;
	}

	private void deal_dos(Map<String, ProduceCaseResult> map1, ProduceCaseResult p, String port) {
		// 3��ͨ��DOS ������ģʽͨ��
		try {
			String key = p.getSend(), val = p.getExpect();
			// cmd /c ��ʾִ����Ͻ��������
			String cmd = "cmd /c " + key;
			Runtime rt = Runtime.getRuntime(); // ��ȡ����ʱϵͳ
			Process proc = rt.exec(cmd); // ִ������
			InputStream stderr = proc.getInputStream(); // ��ȡ������
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = "", all_data = "";
			String ret = "", result = "";
			while ((line = br.readLine()) != null) {
				// ��ӡ������ִ�еĽ��
				// System.out.println(line);
				// xuky 2017.11.29 "\r\n"��Ҫ����ǰ�棬���ܷ��ں��棬�ᵼ��endwith�ж���Ч
				all_data += "\r\n" + line;
				if (SoftParameter.getInstance().getLOG_Level().equals("1"))
					Util698.log(DealSendData.class.getName(), "dos => " + line, Debug.LOG_INFO);
				val = p.getExpect();

				ret = all_data;
				result = all_data;
				// 2����������ظ����ݵĸ�ʽ��xxx%����ʾ�ж��Ƿ������xxx����
				if (val.endsWith("%")) {
					val = val.substring(0, val.length() - 1);
					if (ret.indexOf(val) >= 0)
						returnResult = "ok-" + result;
					else
						returnResult = "err-" + result;
				} else {
					// 2����������ظ����ݵĸ�ʽ��%xxx����ʾ�ж��Ƿ�xxxΪ���������� �ж������Ƿ�ɹ������������ĳɹ�
					if (val.startsWith("%")) {
						val = val.substring(1, val.length());
						if (ret.endsWith(val)) {
							// xuky 2017.11.07 ��ֹ���������ǡ����ɹ���
							if (ret.endsWith("��" + val))
								returnResult = "err-" + result;
							else
								returnResult = "ok-" + result;
						} else
							returnResult = "err-" + result;
					} else {
						if (ret.equals(val))
							returnResult = "ok-" + result;
						else
							returnResult = "err-" + result;
					}
				}

				// xuky 2017.11.29 ������ص������ж�ͨ�����������ѭ��ִ��
				if (returnResult.indexOf("ok-") >= 0)
					break;

				// xuky 2017.11.29 ��ʱ��map1���Ҳ���p.getPort()���������ѭ��ִ��
				if (map1.get(p.getPort()) == null)
					break;
			}

		} catch (Throwable t) {
			Util698.log(DealSendData.class.getName(), "deldosThrowable " + t.getMessage(), Debug.LOG_INFO);
		}
		// xuky 2017.12.07 �������������γ����ִ���Ⱥ���򣬳��ֹ���ʾ��ʵ�ʴ洢����������

		String[] s = { "recv frame", "user data", returnResult, port };
		PublisherFrame.getInstance().publish(s);

		// xuky 2017.11.29 �����﷢����Ϣ���Ա����������и�������
		iBaseDao_ProduceCaseResult.update(p);
		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-deal_dos", Debug.LOG_INFO);

		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"deal_dos" };
		PublisherShowList.getInstance().publish(s2);
		produceCaseResult_tmp = null;

		return;
	}

	private void deal_udpserver(ProduceCaseResult p) {
		// 7������UDP������ ���ȴ��ն��ϱ����Խ������

		// xuky 2017.10.12 ��Ҫ����������DealData
		DealData.getInstance();
		SerialParam s = new SerialParam();
		s.setCOMM(SoftParameter.getInstance().getUDPSVR_IP());
		s.setBaudRate(SoftParameter.getInstance().getUDPSVR_PORT());

		// 1���������¼��Ҫ���лظ�������
		replyData = p.getSend();
		// 2������UDP Server�������ݼ���
		new MinaUDPServer(s);
		// 3����UDP Server��ʹ��sendMessage(byteData)�ظ�����
		// 4����UDP
		// Server��ʹ��Publisher.getInstance().publish���յ������ݵ��������ر�MinaUDPServer
		// 5���ڱ������DealData�жԽ��յ������ݽ��д���
	}

	private void deal_udpclient(ProduceCaseResult p) {
		// 5����UDP�������������ݣ�Ŀǰ��������ն��Լ칤װ
		String data = p.getSend();
		String ret = new JavaUDPClient().sendAndRecv(SoftParameter.getInstance().getUDPCLIENT_IP(),
				SoftParameter.getInstance().getUDPCLIENT_PORT(), data);
		if (!ret.equals("")) {
			returnResult = ret;
		}
	}

	private void deal_69845(ProduceCaseResult p) {
		// 2�����ն˶�Ӧ�����ڷ�������
		// SocketServer.sendToOs(obj_addr, sendData, frameType, os);
		SocketServerEast.sendData(p.getSend());
	}

	private void deal_Rate(ProduceCaseResult p, String port) {
		// 1��������������
		// xuky 2017.09.04 �޸�ͨ����ͨ������
		String send = p.getSend();
		if (send == null || send.equals("") || send.equals("0"))
		{
			// xuky 2018.02.02  �������Ĳ���������portid=RT����������Ϊ�����ʡ�����������Ϊ��0��
			// ��ʾռλ����Ϊportid=RTʱ���������comid������̣�����ʵ�ʲ�ִ���κβ���
			returnResult = "ok- ";
			Debug.sleep(100);
			return;
		}
 		int rate = DataConvert.String2Int(send);

		// xuky 2018.01.26 ����Ϊ��·���ڽ��е���
//		PrefixMain.getInstance().closeSerial();  // �ر����д���
		List<MinaSerialServer> serialServers = PrefixMain.getInstance().getSerialServers();
		for (MinaSerialServer svr : serialServers) {
			if (svr.getSerialParam().getCOMID().equals(port)) {
				// xuky 2018.01.26 ��·���д�������
				// 1���Ч�� 2��ֹ������������ʹ��ʱ�����ڽ��е������������ͻ
				svr.disConnect();
				svr.getSerialParam().setBaudRate(rate);
				svr.init();
				break;
			}
		}
//		PrefixMain.getInstance().setSerial_open(true);
		returnResult = "ok- ";
	}

	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("recv frame") && s[1].equals("user data")) {
			// xuky 2018.07.03 �����߳���ִ�У������ת��Ч��
			// xuky 2018.07.03 �����߳���ִ�У���ʾЧ�����ã�����
//			new Thread(() -> {
				if (!replyData.equals("")) {
					replyData = replyData + "";
				}
				String frame = ((String[]) arg)[2];
//				Util698.log(DealSendData.class.getName(), "3.1 update ׼������DealData���������� :"+frame.substring(frame.length()-4,frame.length()), Debug.LOG_INFO);
				DealData(mapDealData, arg,"����������");
//			}).start();
		}
	}

	public void DealData(Map<String, ProduceCaseResult> map1, Object arg, String type) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String addr = s[3];

//		System.out.println("map.size:"+map1.size());
		ProduceCaseResult produceCaseResult_tmp = map1.get(addr);

		// xuky 2018.07.03 ������ֵ�produceCaseResult_tmp == null�����������������������Ĵ��붼��ִ�е�����
		if (produceCaseResult_tmp == null) {
			return;
		 }

		// xuky 2018.07.03 ��ֹ���ݱ�����
		ProduceCaseResult produceCaseResult_new = (ProduceCaseResult) Util698.objClone(produceCaseResult_tmp, new ProduceCaseResult(), "");

//		Util698.log(DealSendData.class.getName(), "3.2 DealData���й����У����� or �������� :"+frame.substring(frame.length()-4,frame.length()), Debug.LOG_INFO);

		if (!replyData.equals("")) {
			// xuky 2018.01.22 ��Ҫ�������ݻظ�
			// Ŀǰ��˵��Ϊ�����жϣ�ֱ��socket��ʽ���лظ�
			Util698.log(DealSendData.class.getName(), "SocketServer.sendData ���ϱ��Ļظ�:" + replyData, Debug.LOG_INFO);

			SocketServerEast.sendData(replyData, produceCaseResult_new.getPort());
			// ������ɺ����� replyDataΪ�գ���ֹ�ǳ����ٴν�����˴������
			replyData = "";
		}

//		Util698.log(DealSendData.class.getName(),
//				"�յ�����ظ�����("+type+")�����д���  taskID:" + produceCaseResult_new.getADDR() + "." + produceCaseResult_new.getCaseno()+" addr:" + addr+" frame:"+frame,
//				Debug.LOG_INFO);

		produceCaseResult_new.setRecvtime(Util698.getDateTimeSSS_new());
		// �ж��Ƿ�����Ԥ������ ���ò��Խ��
		if (frame.indexOf("ok") < 0 && frame.indexOf("err") < 0) {
			frame = frame.replaceAll(" ", "");
			frame = frame.replaceAll(",", "");
			frame = Util698.seprateString(frame, " ");
			String expect = produceCaseResult_new.getExpect();
			expect = Util698.DealFrameWithParam(expect, produceCaseResult_new.getADDR(),
					produceCaseResult_new.getProtocol());

			// xuky 2018.05.12 ֱ�ӽ���expect�����ݴ�������frame�����ݴ���
			String result = "";
//			String result = Util698.verify(frame, expect);
//			Util698.log(DealSendData.class.getName(), "Util698.verify-1 ���:" + result + " expect:"+expect +" frame:"+frame, Debug.LOG_INFO);
//
//			// xuky 2018.01.25 ������Ϊ���������������ݵ��´���
//			// ����ʹ�����һ���������½��бȽϣ���֤�����Ƿ���ȷ
//			if (result.equals("ʧ��")){
				expect = expect.replaceAll(" ", "");
				// xuky 2018.03.02 �޸�frame���ݣ���Ϊ���������������ݵ����������ܴ󣬵��´洢����
				frame = Util698.rightStr(frame.replaceAll(" ", ""),expect.length());
				result = Util698.verify(frame, expect);
				Util698.log(DealSendData.class.getName(), "4very �˿�:"+produceCaseResult_new.getPort()+" ���:" + result +"  recv:"+frame.substring(frame.length()-4,frame.length()) + "  expect:"+expect+" arg:"+arg, Debug.LOG_INFO);
//			}

			produceCaseResult_new.setResult(result);
			produceCaseResult_new.setRecv(frame);
		} else {
			String result_data = "";
			try {
				result_data = returnResult.split("-")[1];
				// xuky 2017.12.12 ��ĳЩ��������£�ping�����õ��Ľ���ǳ�������Ҫ�ض�һЩ
				if (result_data.length() > 252)
					result_data = result_data.substring(result_data.length() - 252, result_data.length());
			} catch (Exception e) {
				Util698.log(DealSendData.class.getName(), "DealData returnResult err" + e.getMessage(), Debug.LOG_INFO);
			}

			if (frame.indexOf("ok") >= 0) {
				produceCaseResult_new.setResult("�ɹ�");
				produceCaseResult_new.setRecv(result_data);
			} else {
				// xuky 2017.10.26
				// ���������õ�setResult("ʧ��")����DealTestCase1�����DealData�����л����ʹ�ã����б�־λ����
				produceCaseResult_new.setResult("ʧ��");
				produceCaseResult_new.setRecv(result_data);
			}
		}
		// ������ɾ��map������
		if (produceCaseResult_new.getWaitReply() == true) {
			// xuky 2018.01.23 ��Ϊ����Ҫ�ȴ��ظ����ģ����Բ���ɾ��map1������
			// replyData = produceCaseResult_new.getExpect0().split("&&")[1];
		} else {
			// xuky 2018.01.24 Ŀ����Ҫ��Ӵ˶δ��룬ʧ���˻�ϣ���������Բ���
			if (produceCaseResult_new.getResult().equals("ʧ��")) {
				if (produceCaseResult_new.getSendtimes() > produceCaseResult_new.getRetrys()) {
					map1.remove(produceCaseResult_new.getPort());
//					System.out.println("map.remove:"+produceCaseResult_new.getPort()+"map.size:"+mapDealData.size());

					// xuky 2018.04.24 ����豸ͨ�ų�ʱ�ˣ��Ϳ��Խ�������豸
					DealSendBlockLock.getInstance().removeAddr(produceCaseResult_new.getADDR(),"�ж�ʧ��");

					// xuky 2018.01.25 �ж������Ƿ�Ϊ������������ǣ���Ϊ�����Ѿ�ִ������˾�ȥ�޸�DealSendBlockData�Ķ�Ӧ״̬
					if (produceCaseResult_new.getIsBlockTask()){
						DealSendBlockData.getInstance().setISBUSY(false);
					}

				} else {
					// xuky 2018.01.24 Ŀ����Ҫ��Ӵ˶δ��룬ʧ���˻�ϣ���������Բ���
					produceCaseResult_new.setResult("0ʧ0��0");
				}
			} else {
				map1.remove(produceCaseResult_new.getPort());
//				System.out.println("map.remove:"+produceCaseResult_new.getPort()+"map.size:"+mapDealData.size());

				// xuky 2018.04.24 ����豸ͨ�ų�ʱ�ˣ��Ϳ��Խ�������豸
				// xuky 2018.05.04 ���ﲻ�ܽ�����ÿ���豸ֻ�Ǽ���һ�Σ�����ÿ����������ִ�гɹ��ͽ�������Ҫͳһ����
//				DealSendBlockLock.getInstance().removeAddr(produceCaseResult_new.getADDR());

				// xuky 2018.01.25 �ж������Ƿ�Ϊ������������ǣ���Ϊ�����Ѿ�ִ������˾�ȥ�޸�DealSendBlockData�Ķ�Ӧ״̬
				if (produceCaseResult_new.getIsBlockTask()){
					Util698.log(DealSendData.class.getName(),"5IsBlockTask ������setISBUSY(false)",Debug.LOG_INFO);
					DealSendBlockData.getInstance().setISBUSY(false);
				}
			}
		}

		// xuky 2018.01.23 ��Ҫ�ȴ��ظ����ݣ���������ݽ��д���
		if (produceCaseResult_new.getWaitReply() == true) {
			replyData = produceCaseResult_new.getExpect0().split("&&")[1];
			produceCaseResult_new.setWaitReply(false);
			String expect0 = produceCaseResult_new.getExpect0();
			// ע������1��0 �ȴ��ϱ� �����ϱ��Ľ��лظ�-��ֹ�����ϱ�
			produceCaseResult_new.setSend(Util698.DealFrameWithParam(expect0.split("&&")[1],
					produceCaseResult_new.getADDR(), produceCaseResult_new.getProtocol()));
			produceCaseResult_new.setExpect(Util698.DealFrameWithParam(expect0.split("&&")[0],
					produceCaseResult_new.getADDR(), produceCaseResult_new.getProtocol()));

			produceCaseResult_new.setResult("0��0��0");
		}

		// xuky 2018.07.03
		iBaseDao_ProduceCaseResult.update(produceCaseResult_new);
		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update��" + produceCaseResult_new.getADDR()+"-"+produceCaseResult_new.getName()+"-"+produceCaseResult_new.getID()+"-DealData result="+produceCaseResult_new.getResult(), Debug.LOG_INFO);

		produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(produceCaseResult_new, new ProduceCaseResult(), "");
		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"DealData"+"-"+this };
		Util698.log(DealSendData.class.getName(), "DealTestCase.old(DealData) " + produceCaseResult_tmp + " "+produceCaseResult_tmp.getADDR()+"."+produceCaseResult_tmp.getCaseno(),Debug.LOG_INFO);
		PublisherShowList.getInstance().publish(s2);
//		produceCaseResult_tmp = null;

		// System.out.println("DealTestCase old Result="+
		// produceCaseResult_new.getResult());

	}
}
