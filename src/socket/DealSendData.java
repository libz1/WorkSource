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

// 处理发送数据的线程
public class DealSendData implements Observer {
	// xuky 2018.07.16 添加线程池的方式进行线程调用
//	ExecutorService pool = Executors.newFixedThreadPool(15);
	// xuky 2018.08.02 通过 ThreadPoolExecutor的方式
	// 参考 https://www.cnblogs.com/zedosu/p/6665306.html
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	private volatile static DealSendData uniqueInstance;

	private Boolean isRuning = true;
	public Boolean getIsRuning() {
		return isRuning;
	}
	public void setIsRuning(Boolean isRuning) {
		this.isRuning = isRuning;
	}

	// xuky 2018.03.14 提高执行效率 去掉界面的展示部分
	Boolean RUNFASTER = SoftParameter.getInstance().getRUNFASTER();

	private Map<String, ProduceCaseResult> mapDealData = null;
	private IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult;
	private String returnResult = "";
	private String TERMINAL_IP = "192.168.1.96";

	// xuky 2017.10.12 用于回复的数据
	private String replyData = "";

	// xuky 2018.02.07 添加锁定项目
	private String LockBeginTime = ""; // 开始锁定时间
	private long lockTime = 1000;   // 需要锁定时间长
	private String LockADDR = ""; // 对自身处理不限制，对其他的进行限制
	boolean plc_in_front = false;  // 如果载波操作都在后面，则无需进行锁定判断了


	// xuky 2018.01.22 在MinaUDPServer.DealData函数中，调用此getReplyData进行相关判断等操作
	public String getReplyData() {
		return replyData;
	}

	public static DealSendData getInstance() {
		if (uniqueInstance == null) {
			synchronized (DealSendData.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new DealSendData();
				}
			}
		}
		return uniqueInstance;
	}
	// xuky 2018.02.07 判断是否有锁定情况
	public synchronized boolean isLock(ProduceCaseResult p){

		// xuky 2018.03.14 加锁尝试  无效
		// xuky 2018.02.08 发现锁定后效果一般
		if (!plc_in_front) return false;

		boolean ret = false;
		String time = LockBeginTime;

		// 对自身地址操作不影响 ，例如发送完载波后，可以直接发送红外
		if (p == null || p.getADDR().equals(LockADDR)){
//			Util698.log("xuky","跳过锁判断！  队列无数据",Debug.LOG_INFO);
//			System.out.println("跳过锁判断！  队列无数据");
			return ret;
		}
		if (p == null || p.getADDR().equals(LockADDR)){
			Util698.log("xuky","跳过锁判断！  p.getADDR():"+p.getADDR() + " LockADDR:"+LockADDR,Debug.LOG_INFO);
			return ret;
		}

		if (!time.equals("")){
			String nowTime = Util698.getDateTimeSSS_new();
			// 判断是否超时
			if ((Util698.getMilliSecondBetween_new(nowTime, time)) > lockTime){
				// 只能在这里进行解锁处理
				LockBeginTime = "";
				Util698.log("xuky","解锁！  nowTime:"+nowTime + " LockBeginTime:"+time+" LockADDR:"+LockADDR,Debug.LOG_INFO);
			}
			else{
				ret = true;
				Util698.log("xuky","保持锁定！  nowTime:"+nowTime + " LockBeginTime:"+time+" LockADDR"+LockADDR,Debug.LOG_INFO);
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

					// xuky 2018.07.03 添加300毫秒的延时， 防止出现如下的异常数据，第1条的result=为空，且namey也是后续测试用例的，之前的测试用例数据被覆盖了
//					2018-07-03 12:18:21:287 [ProduceCaseResult.update：000000098062-红外读PLCM1643程序(FE)[SAVEID]-3018487-DealData result=] socket.DealSendData
//					2018-07-03 12:18:21:295 [1run  端口:1 执行非阻塞任务-begin taskID:000000098061.30 红外读PLCM1643程序(FE)[SAVEID]] socket.DealSendData
//					2018-07-03 12:18:21:305 [ProduceCaseResult.create：000000098062-红外读PLCM1643程序(FE)[SAVEID]-3018496] deal.DealTestCase1
					Debug.sleep(300);
					// 从发送队列中取出数据并执行发送过程
					ProduceCaseResult p = SendData.getInstance().pop();

					if (p != null) {
						if (!RUNFASTER)
							Util698.log(DealSendData.class.getName(),
								"1run "+" 端口:"+p.getPort() +" 执行非阻塞任务-begin taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName() ,
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
		// 每隔1分钟钟执行一次job1，第2次在第一次执行完成后n秒
		service.scheduleWithFixedDelay(new MyScheduledExecutor(), initialDelay, period, TimeUnit.MINUTES);

	}
	class MyScheduledExecutor implements Runnable {
		@Override
		public void run() {
//			Util698.log(DealSendData.class.getName(),
//					"ScheduledExecutorService方式 执行任务的超时检测  map.size=" + mapDealData.size(),
//					Debug.LOG_INFO);
			checkData();
		}
	}

	public synchronized void checkData(){

		if (!isRuning)
			return;

		// 遍历map【1轮】   判断是否存在超时的情况
		Iterator<String> iter = mapDealData.keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			String addr = (String) iter.next();
			ProduceCaseResult p = mapDealData.get(addr);

			if (p == null)
				continue;

			String beginTime = p.getSendtime();
			// xuky 2017.11.29 因为前面的sendData是在线程中执行的，所以可能实际执行
			if (beginTime == null || beginTime.equals("")) {
				Debug.sleep(100);
				continue;
			}

			String nowTime = Util698.getDateTimeSSS_new();
			// 判断是否超时
			long diff = Util698.getMilliSecondBetween_new(nowTime, beginTime);
			// xuky 2018.07.23 调整需要判断的超时时间为等待时间+发前掩饰时间之和
			if (diff > (long)(p.getWaittime()+p.getDelaytime())) {
				if (p.getSendtimes() > p.getRetrys()) {
					mapDealData.remove(p.getPort());
//					System.out.println("map.remove:"+p.getPort()+"map.size:"+mapDealData.size());

					// xuky 2018.04.24 如果设备通信超时了，就可以解锁这个设备
					DealSendBlockLock.getInstance().removeAddr(p.getADDR(),"超时");

					if (!RUNFASTER)
						Util698.log(DealSendData.class.getName(),
							"非阻塞任务 超时且重试多次 :" + p.getADDR() + "." + p.getCaseno(), Debug.LOG_INFO);
					p.setRecvtime(Util698.getDateTimeSSS_new());
					p.setResult("超时");

					// xuky 2018.01.30 特殊情况处理
					// II采设置省份模式后，可能会切换速率，导致无法上报确认报文
					// 为了确保流程继续执行，所以这里设置为成功
					if (p.getWaittime()==0){
						p.setResult("成功");
					}

					// 超时时，写入数据 					xukyxuky
					iBaseDao_ProduceCaseResult.update(p);
//					Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-超时", Debug.LOG_INFO);

					ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
					Object[] s21 = { "DealTestCase", "old", produceCaseResult_tmp,"超时" };
					PublisherShowList.getInstance().publish(s21);
					produceCaseResult_tmp = null;
					p = null;
				} else {
					// xuky 2017.11.29 sendData中执行的代码可能导致超时控制无效
					// 例如telnet的执行方式，因为没有在线程中执行
					Util698.log(DealSendData.class.getName(),
							"非阻塞任务taskID:" + p.getADDR() + "." + p.getCaseno() +" 超时重发"+i+" nowTime"+nowTime+" beginTime"+beginTime+" Waittime"+p.getWaittime()+" diff"+diff, Debug.LOG_INFO);
					sendData_new(mapDealData, p);
					// System.out.println("sendData(map,p) ");
//					Debug.sleep(300);
				}
			}
			i++;
			// xuky 2018.07.04 添加延时，防止未及时执行发送程序时，就执行这里的代码
			// xuky 2018.07.06  取消，因为等待时间过长
//			Debug.sleep(200);
		};


	}
	// xuky 2017.11.29 在线程中执行，以此来期望超时控制有效
	public synchronized void sendData_new(Map<String, ProduceCaseResult> map1, ProduceCaseResult p) {

		// xuky 2018.07.24 以前的时候这些代码放在线程中执行，好处是，对其他请求的响应提速了，坏处是，在线程池中执行，可能有不确定的延时
		// 可以看到 从 11:01:54:014到11:01:54,222 有比较大的延时 因中间有其他操作在同步进行
//		bad
//		2018-07-24 11:01:54,014 [1run  端口:RT 执行阻塞任务-begin taskID:000000098061.60 载波无地址抄485表] socket.DealSendBlockData
//		2018-07-24 11:01:54,090 [ListShow.testDetailList produce.entity.ProduceCaseResult@719a5446] deal.TerminalParameterController
//		2018-07-24 11:01:54,090 [xuky add Detail row:-1 p1载波无地址抄485表000000098062.60-发送-接收2018-07-24 11:01:53:987-结果:] deal.TerminalParameterController
//		2018-07-24 11:01:54,222 [sendDataInThread sendTime=2018-07-24 11:01:54:014 Sendtimes=1] socket.DealSendData

//		good
//		2018-07-24 11:01:56,362 [1run  端口:RT 执行阻塞任务-begin taskID:000000098064.60 载波无地址抄485表] socket.DealSendBlockData
//		2018-07-24 11:01:56,362 [sendDataInThread sendTime=2018-07-24 11:01:56:362 Sendtimes=1] socket.DealSendData


		// xuky 2018.07.04 优先执行如下代码，因为后面的循环很快还会判断此setSendtime信息
		// 填写发送时间
		// 递增发送次数
		String sendTime = Util698.getDateTimeSSS_new();
		p.setSendtime(sendTime);
//		Util698.log(DealSendData.class.getName(), "set sendTime："+ sendTime +" " + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-sendDataInThread", Debug.LOG_INFO);
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


		// xuky 2017.08.24 发前延时  放在启动时间以后   ！！！【发前延时与写入sendtime是矛盾的，不易协调】
		// 矛盾的解决：  diff > (long)(p.getWaittime()+p.getDelaytime()
		Debug.sleep(p.getDelaytime());

		// 操作前更新终端IP地址信息
		TERMINAL_IP = SoftParameter.getInstance().getTERMINAL_IP();

		// xuky 2018.07.06  超时判断与接收数据在临界点  超时的后续操作覆盖了正常得到的数据
//		2018-07-06 10:46:45:354 [非阻塞任务taskID:000000098063.30 超时重发0] socket.DealSendData
//		2018-07-06 10:46:45:462 [3recv 端口:3 user data【1】:FFFEFEFEFE686380090000006891243433B337837F768064696866607C7C5BA96463656564655C60646B6369656C5353535353D216] socket.DealData
		if (map1.get(p.getPort()) == null){
			Util698.log(DealSendData.class.getName(), "sendDataInThread的ProduceCaseResult.update操作前检查是否存在于map1中：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-sendDataInThread", Debug.LOG_INFO);
			return;
		}

		// xuky 2018.07.03 将此段代码的执行次序往前提，不然显示的次序是错误的
		iBaseDao_ProduceCaseResult.update(p);
//		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-sendDataInThread", Debug.LOG_INFO);

		// xuky 2018.02.07 设定锁定时间
		if (p.getName().indexOf("载波") >= 0){
			LockBeginTime = Util698.getDateTimeSSS_new();
			LockADDR = p.getADDR();
		}

		String port = p.getPort();
		if (p.getName().indexOf("速率") >= 0) {

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
					"执行非阻塞任务-end1 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
					Debug.LOG_INFO);

			return;

		} else if (p.getProtocol().equals("telnet")) {
			deal_telnet(p, port);
			Util698.log(DealSendData.class.getName(),
					"执行非阻塞任务-end2 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
					Debug.LOG_INFO);
			return;

		} else {

			// 4、通过串口方式进行通信

			if (p.getName().indexOf("等待") >= 0) {
				// 不发送数据 类似deal_udpserver的处理过程
				replyData = p.getSend();
//				System.out.println("replyData=" + replyData + " 等待上报数据");
			} else {
				boolean ret_bool = false;

//				// xuky 2018.01.24 当超时需要重新发送时，根据send0的数据回复到首次的发送内容
				String send0 = p.getSend0();
				if (send0.indexOf("&&")>=0) {
					Util698.log(DealSendData.class.getName(), "&& 数据处理", Debug.LOG_INFO);
					p.setSend(Util698.DealFrameWithParam(send0.split("&&")[0], p.getADDR(), p.getProtocol()));
					p.setExpect(Util698.DealFrameWithParam(send0.split("&&")[1], p.getADDR(), p.getProtocol()));
					p.setWaitReply(true);
					iBaseDao_ProduceCaseResult.update(p);
					Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-&& 数据处理", Debug.LOG_INFO);
				}

				ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
				Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"sendDataInThread"+"-"+this };
				Util698.log(DealSendData.class.getName(), "DealTestCase.old(sendDataInThread) " + produceCaseResult_tmp+" "+produceCaseResult_tmp.getADDR()+"."+produceCaseResult_tmp.getCaseno(),Debug.LOG_INFO);
				PublisherShowList.getInstance().publish(s2);

				// xuky 2018.01.24 添加串口发送日志
				if (!RUNFASTER)
					Util698.log(DealSendData.class.getName(), "2send 端口:"+p.getPort()+ " Data:" + p.getSend(), Debug.LOG_INFO);

				try{
					ret_bool = SocketServerEast.sendData(p.getSend(), p.getPort());
				}
				catch(Exception e){
					ret_bool = false;
					Util698.log(DealSendData.class.getName(), "send Exception:"+e.getMessage(), Debug.LOG_INFO);
				}

				// 因为发送失败，所以在这里直接从map1中删除 ，正常情况应该在超时或是接收到数据后进行删除
				if (ret_bool == false) {

					map1.remove(p.getPort());
//					System.out.println("map.remove:"+p.getPort()+"map.size:"+mapDealData.size());

					// xuky 2018.04.24 如果设备已经无法通信了，就解锁这个设备
					DealSendBlockLock.getInstance().removeAddr(p.getADDR(),"发送失败");

					// xuky 2018.01.25 判断任务是否为阻塞任务，如果是，因为这里已经执行完毕了就去修改DealSendBlockData的对应状态
					if (p.getIsBlockTask()){
						DealSendBlockData.getInstance().setISBUSY(false);
					}
					// System.out.println("map.remove2 "+
					// p.getADDR()+"."+p.getCaseno());

					String msg = "未打开串口";
					p.setResult(msg);
					iBaseDao_ProduceCaseResult.update(p);
					Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-"+msg, Debug.LOG_INFO);

					// 因为没有打开串口，及时的删除p对象

					// xuky 2017.07.05 发送测试过程信息
					ProduceCaseResult produceCaseResult_tmp3 = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
					Object[] s3 = { "DealTestCase", "old", produceCaseResult_tmp3,"未打开串口" };
					PublisherShowList.getInstance().publish(s3);
					produceCaseResult_tmp = null;

					Util698.log(DealSendData.class.getName(),
							"执行非阻塞任务-end3 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
							Debug.LOG_INFO);
					p = null;
					// 无需继续，退出这里的流程
					return;
				}
			}
		}

		// xuky 2018.07.24 放在这里运行有些晚，提到前面，但是目前只是针对串口通信进行了调整，其A他的暂时不考虑
//		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
//		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"sendDataInThread"+"-"+this };
//		Util698.log(DealSendData.class.getName(), "DealTestCase.old(sendDataInThread) " + produceCaseResult_tmp,Debug.LOG_INFO);
//		PublisherShowList.getInstance().publish(s2);


		if (p.getName().indexOf("速率") >= 0) {
			String[] s = { "recv frame", "user data", "ok- ", port };
			PublisherFrame.getInstance().publish(s);
		}
		if (p.getPort().indexOf("udp-client") >= 0) {
			String[] s = { "recv frame", "user data", returnResult, port };
			PublisherFrame.getInstance().publish(s);
		}

//		if (!RUNFASTER)
//			Util698.log(DealSendData.class.getName(),
//				"执行非阻塞任务-end4 taskID:" + p.getADDR() + "." + p.getCaseno() + " " + p.getName(),
//				Debug.LOG_INFO);

	}

	private void deal_telnet(ProduceCaseResult p, String port) {
		// 3、通过telnet通信
		// Util698.log(DealSendData.class.getName(),
		// "TerminalTelnetSingle.getInstance", Debug.LOG_INFO);

		TerminalTelnetSingle terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP);
		String key = p.getSend(), val = p.getExpect();

		// xuky 2017.10.17
		// 1、非数字的，表示需要直接执行linux命令
		// 2、如果期望回复数据的格式是xxx%，表示判断是否包含此xxx数据
		// 3、如果返回的数据长度小于100，则掐头去尾（不保留发出的命令、不保留返回的提示符信息），仅仅保留中间部分数据
		if (!Util698.isNumber(key)) {
			// 1、非数字的，表示需要直接执行linux命令
			String end = "[root@(none) /]#";

			if (key.indexOf("dn") == 0) {
				// xuky 2018.01.08 设置超时时间
				TerminalTelnetSingle.getInstance("").destroy();
				terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP, 0);
			} else if (!terminalTelnet.getREADSTR().endsWith(end)) {
				// 如果当前的提示符不是 [root@(none) /]#，需要重新连接
				TerminalTelnetSingle.getInstance("").destroy();
				terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP);
			}
			// Util698.log(DealSendData.class.getName(), "linux: finish
			// init", Debug.LOG_INFO);
			String ret = terminalTelnet.writeThenReadUtil(key, end);

			// xuky 2017.11.17 如果无法连接终端，无法返回数据，需要进行如下的处理
			if (ret == null || ret.equals(""))
				ret = " ";
			else
				// 掐头去尾（不保留发出的命令、不保留返回的提示符信息），仅仅保留中间部分数据
				ret = ret.substring(key.length() + 2, ret.length() - end.length() - 2);

			String result = ret;
			// 3、如果返回的数据长度小于100，则掐头去尾（不保留发出的命令、不保留返回的提示符信息），仅仅保留中间部分数据
			// if (ret.length() > 100)
			// result = " ";

			// 2、如果期望回复数据的格式是xxx%，表示判断是否包含此xxx数据
			if (val.endsWith("%")) {
				val = val.substring(0, val.length() - 1);
				if (ret.indexOf(val) >= 0)
					returnResult = "ok-" + result;
				else
					returnResult = "err-" + result;
			} else {
				// 2、如果期望回复数据的格式是%xxx，表示判断是否xxx为结束的数据 判断升级是否成功，必须是最后的成功
				if (val.startsWith("%")) {
					val = val.substring(1, val.length());
					if (ret.endsWith(val)) {
						// xuky 2017.11.07 防止最后的字样是“不成功”
						if (ret.endsWith("不" + val))
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
				// xuky 2018.01.08 恢复默认超时时间
				terminalTelnet = TerminalTelnetSingle.getInstance(TERMINAL_IP);

			}

		} else {
			// 设置参数
			if (terminalTelnet.changeParam(key, val) == false) {
				// changeParam返回false,表示telnet操作异常，此时无需进行回应，等待超时即可
				return;
			}
			// xuky 2017.10.09 如果期望数据为空，表示无需检查回复数据
			if (val == null || val.equals("")) {
				returnResult = "ok- ";
			} else {
				// 验证参数
				String[] result = terminalTelnet.verify(key, val);
				if (result[1].equals("1"))
					returnResult = "ok-" + result[0];
				else
					returnResult = "err-" + result[0];
			}

		}

		// xuky 2017.11.29 在这里发送消息，以便后续程序进行跟进处理
		// telent时的处理过程
		iBaseDao_ProduceCaseResult.update(p);
		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-telnet", Debug.LOG_INFO);

		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"deal_telnet" };
		PublisherShowList.getInstance().publish(s2);
		produceCaseResult_tmp = null;

		String[] s = { "recv frame", "user data", returnResult, port };
		PublisherFrame.getInstance().publish(s);

		return;
	}

	private void deal_dos(Map<String, ProduceCaseResult> map1, ProduceCaseResult p, String port) {
		// 3、通过DOS 命令行模式通信
		try {
			String key = p.getSend(), val = p.getExpect();
			// cmd /c 表示执行完毕结束命令窗口
			String cmd = "cmd /c " + key;
			Runtime rt = Runtime.getRuntime(); // 获取运行时系统
			Process proc = rt.exec(cmd); // 执行命令
			InputStream stderr = proc.getInputStream(); // 获取输入流
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = "", all_data = "";
			String ret = "", result = "";
			while ((line = br.readLine()) != null) {
				// 打印出命令执行的结果
				// System.out.println(line);
				// xuky 2017.11.29 "\r\n"需要放在前面，不能放在后面，会导致endwith判断无效
				all_data += "\r\n" + line;
				if (SoftParameter.getInstance().getLOG_Level().equals("1"))
					Util698.log(DealSendData.class.getName(), "dos => " + line, Debug.LOG_INFO);
				val = p.getExpect();

				ret = all_data;
				result = all_data;
				// 2、如果期望回复数据的格式是xxx%，表示判断是否包含此xxx数据
				if (val.endsWith("%")) {
					val = val.substring(0, val.length() - 1);
					if (ret.indexOf(val) >= 0)
						returnResult = "ok-" + result;
					else
						returnResult = "err-" + result;
				} else {
					// 2、如果期望回复数据的格式是%xxx，表示判断是否xxx为结束的数据 判断升级是否成功，必须是最后的成功
					if (val.startsWith("%")) {
						val = val.substring(1, val.length());
						if (ret.endsWith(val)) {
							// xuky 2017.11.07 防止最后的字样是“不成功”
							if (ret.endsWith("不" + val))
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

				// xuky 2017.11.29 如果返回的数据判断通过，无需继续循环执行
				if (returnResult.indexOf("ok-") >= 0)
					break;

				// xuky 2017.11.29 超时后，map1中找不到p.getPort()，无需继续循环执行
				if (map1.get(p.getPort()) == null)
					break;
			}

		} catch (Throwable t) {
			Util698.log(DealSendData.class.getName(), "deldosThrowable " + t.getMessage(), Debug.LOG_INFO);
		}
		// xuky 2017.12.07 调整以下两两段程序的执行先后次序，出现过显示与实际存储不相符的情况

		String[] s = { "recv frame", "user data", returnResult, port };
		PublisherFrame.getInstance().publish(s);

		// xuky 2017.11.29 在这里发送消息，以便后续程序进行跟进处理
		iBaseDao_ProduceCaseResult.update(p);
		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + p.getADDR()+"-"+p.getName()+"-"+p.getID()+"-deal_dos", Debug.LOG_INFO);

		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(p, new ProduceCaseResult(), "");
		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"deal_dos" };
		PublisherShowList.getInstance().publish(s2);
		produceCaseResult_tmp = null;

		return;
	}

	private void deal_udpserver(ProduceCaseResult p) {
		// 7、开启UDP服务器 ，等待终端上报测试结果数据

		// xuky 2017.10.12 需要在这里启动DealData
		DealData.getInstance();
		SerialParam s = new SerialParam();
		s.setCOMM(SoftParameter.getInstance().getUDPSVR_IP());
		s.setBaudRate(SoftParameter.getInstance().getUDPSVR_PORT());

		// 1、在这里记录需要进行回复的数据
		replyData = p.getSend();
		// 2、启动UDP Server进行数据监听
		new MinaUDPServer(s);
		// 3、在UDP Server中使用sendMessage(byteData)回复数据
		// 4、在UDP
		// Server中使用Publisher.getInstance().publish将收到的数据弹出，并关闭MinaUDPServer
		// 5、在本对象的DealData中对接收到的数据进行处理
	}

	private void deal_udpclient(ProduceCaseResult p) {
		// 5、向UDP服务器发送数据，目前的情况是终端自检工装
		String data = p.getSend();
		String ret = new JavaUDPClient().sendAndRecv(SoftParameter.getInstance().getUDPCLIENT_IP(),
				SoftParameter.getInstance().getUDPCLIENT_PORT(), data);
		if (!ret.equals("")) {
			returnResult = ret;
		}
	}

	private void deal_69845(ProduceCaseResult p) {
		// 2、向终端对应的网口发送数据
		// SocketServer.sendToOs(obj_addr, sendData, frameType, os);
		SocketServerEast.sendData(p.getSend());
	}

	private void deal_Rate(ProduceCaseResult p, String port) {
		// 1、调整串口速率
		// xuky 2017.09.04 修改通道的通信速率
		String send = p.getSend();
		if (send == null || send.equals("") || send.equals("0"))
		{
			// xuky 2018.02.02  添加特殊的测试用例，portid=RT，用例名称为“速率”，发送内容为“0”
			// 表示占位，因为portid=RT时，是特殊的comid计算过程，但是实际不执行任何操作
			returnResult = "ok- ";
			Debug.sleep(100);
			return;
		}
 		int rate = DataConvert.String2Int(send);

		// xuky 2018.01.26 调整为单路串口进行调整
//		PrefixMain.getInstance().closeSerial();  // 关闭所有串口
		List<MinaSerialServer> serialServers = PrefixMain.getInstance().getSerialServers();
		for (MinaSerialServer svr : serialServers) {
			if (svr.getSerialParam().getCOMID().equals(port)) {
				// xuky 2018.01.26 逐路进行串口重启
				// 1提高效率 2防止其他串口正在使用时整串口进行调整，会与其冲突
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
			// xuky 2018.07.03 放在线程中执行，提高运转的效率
			// xuky 2018.07.03 放在线程中执行，显示效果不好！！！
//			new Thread(() -> {
				if (!replyData.equals("")) {
					replyData = replyData + "";
				}
				String frame = ((String[]) arg)[2];
//				Util698.log(DealSendData.class.getName(), "3.1 update 准备运行DealData（非阻塞） :"+frame.substring(frame.length()-4,frame.length()), Debug.LOG_INFO);
				DealData(mapDealData, arg,"非阻塞处理");
//			}).start();
		}
	}

	public void DealData(Map<String, ProduceCaseResult> map1, Object arg, String type) {
		String[] s = (String[]) arg;
		String frame = s[2];
		String addr = s[3];

//		System.out.println("map.size:"+map1.size());
		ProduceCaseResult produceCaseResult_tmp = map1.get(addr);

		// xuky 2018.07.03 这里出现的produceCaseResult_tmp == null的情况，就是阻塞与非阻塞的代码都会执行到这里
		if (produceCaseResult_tmp == null) {
			return;
		 }

		// xuky 2018.07.03 防止数据被覆盖
		ProduceCaseResult produceCaseResult_new = (ProduceCaseResult) Util698.objClone(produceCaseResult_tmp, new ProduceCaseResult(), "");

//		Util698.log(DealSendData.class.getName(), "3.2 DealData运行过程中（阻塞 or 非阻塞） :"+frame.substring(frame.length()-4,frame.length()), Debug.LOG_INFO);

		if (!replyData.equals("")) {
			// xuky 2018.01.22 需要进行数据回复
			// 目前来说，为减少判断，直接socket方式进行回复
			Util698.log(DealSendData.class.getName(), "SocketServer.sendData 对上报的回复:" + replyData, Debug.LOG_INFO);

			SocketServerEast.sendData(replyData, produceCaseResult_new.getPort());
			// 发送完成后，设置 replyData为空，防止非常的再次进进入此处理过程
			replyData = "";
		}

//		Util698.log(DealSendData.class.getName(),
//				"收到任务回复数据("+type+")，进行处理  taskID:" + produceCaseResult_new.getADDR() + "." + produceCaseResult_new.getCaseno()+" addr:" + addr+" frame:"+frame,
//				Debug.LOG_INFO);

		produceCaseResult_new.setRecvtime(Util698.getDateTimeSSS_new());
		// 判断是否满足预期条件 设置测试结果
		if (frame.indexOf("ok") < 0 && frame.indexOf("err") < 0) {
			frame = frame.replaceAll(" ", "");
			frame = frame.replaceAll(",", "");
			frame = Util698.seprateString(frame, " ");
			String expect = produceCaseResult_new.getExpect();
			expect = Util698.DealFrameWithParam(expect, produceCaseResult_new.getADDR(),
					produceCaseResult_new.getProtocol());

			// xuky 2018.05.12 直接进行expect的数据处理，进行frame的数据处理
			String result = "";
//			String result = Util698.verify(frame, expect);
//			Util698.log(DealSendData.class.getName(), "Util698.verify-1 结果:" + result + " expect:"+expect +" frame:"+frame, Debug.LOG_INFO);
//
//			// xuky 2018.01.25 可能因为包含了其他的数据导致错误
//			// 所以使用最后一段数据重新进行比较，验证数据是否正确
//			if (result.equals("失败")){
				expect = expect.replaceAll(" ", "");
				// xuky 2018.03.02 修改frame数据，因为包含了其他的数据导致数据量很大，导致存储错误
				frame = Util698.rightStr(frame.replaceAll(" ", ""),expect.length());
				result = Util698.verify(frame, expect);
				Util698.log(DealSendData.class.getName(), "4very 端口:"+produceCaseResult_new.getPort()+" 结果:" + result +"  recv:"+frame.substring(frame.length()-4,frame.length()) + "  expect:"+expect+" arg:"+arg, Debug.LOG_INFO);
//			}

			produceCaseResult_new.setResult(result);
			produceCaseResult_new.setRecv(frame);
		} else {
			String result_data = "";
			try {
				result_data = returnResult.split("-")[1];
				// xuky 2017.12.12 在某些特殊情况下（ping），得到的结果非常长，需要截短一些
				if (result_data.length() > 252)
					result_data = result_data.substring(result_data.length() - 252, result_data.length());
			} catch (Exception e) {
				Util698.log(DealSendData.class.getName(), "DealData returnResult err" + e.getMessage(), Debug.LOG_INFO);
			}

			if (frame.indexOf("ok") >= 0) {
				produceCaseResult_new.setResult("成功");
				produceCaseResult_new.setRecv(result_data);
			} else {
				// xuky 2017.10.26
				// 在这里设置的setResult("失败")，在DealTestCase1对象的DealData函数中会进行使用，进行标志位设置
				produceCaseResult_new.setResult("失败");
				produceCaseResult_new.setRecv(result_data);
			}
		}
		// 在这里删除map的数据
		if (produceCaseResult_new.getWaitReply() == true) {
			// xuky 2018.01.23 因为还需要等待回复报文，所以不能删除map1的数据
			// replyData = produceCaseResult_new.getExpect0().split("&&")[1];
		} else {
			// xuky 2018.01.24 目的是要添加此段代码，失败了还希望继续重试操作
			if (produceCaseResult_new.getResult().equals("失败")) {
				if (produceCaseResult_new.getSendtimes() > produceCaseResult_new.getRetrys()) {
					map1.remove(produceCaseResult_new.getPort());
//					System.out.println("map.remove:"+produceCaseResult_new.getPort()+"map.size:"+mapDealData.size());

					// xuky 2018.04.24 如果设备通信超时了，就可以解锁这个设备
					DealSendBlockLock.getInstance().removeAddr(produceCaseResult_new.getADDR(),"判断失败");

					// xuky 2018.01.25 判断任务是否为阻塞任务，如果是，因为这里已经执行完毕了就去修改DealSendBlockData的对应状态
					if (produceCaseResult_new.getIsBlockTask()){
						DealSendBlockData.getInstance().setISBUSY(false);
					}

				} else {
					// xuky 2018.01.24 目的是要添加此段代码，失败了还希望继续重试操作
					produceCaseResult_new.setResult("0失0败0");
				}
			} else {
				map1.remove(produceCaseResult_new.getPort());
//				System.out.println("map.remove:"+produceCaseResult_new.getPort()+"map.size:"+mapDealData.size());

				// xuky 2018.04.24 如果设备通信超时了，就可以解锁这个设备
				// xuky 2018.05.04 这里不能解锁，每个设备只是加锁一次，不能每个测试用例执行成功就解锁，需要统一解锁
//				DealSendBlockLock.getInstance().removeAddr(produceCaseResult_new.getADDR());

				// xuky 2018.01.25 判断任务是否为阻塞任务，如果是，因为这里已经执行完毕了就去修改DealSendBlockData的对应状态
				if (produceCaseResult_new.getIsBlockTask()){
					Util698.log(DealSendData.class.getName(),"5IsBlockTask ，设置setISBUSY(false)",Debug.LOG_INFO);
					DealSendBlockData.getInstance().setISBUSY(false);
				}
			}
		}

		// xuky 2018.01.23 需要等待回复数据，对相关数据进行处理
		if (produceCaseResult_new.getWaitReply() == true) {
			replyData = produceCaseResult_new.getExpect0().split("&&")[1];
			produceCaseResult_new.setWaitReply(false);
			String expect0 = produceCaseResult_new.getExpect0();
			// 注意是先1后0 等待上报 ，对上报的进行回复-防止不断上报
			produceCaseResult_new.setSend(Util698.DealFrameWithParam(expect0.split("&&")[1],
					produceCaseResult_new.getADDR(), produceCaseResult_new.getProtocol()));
			produceCaseResult_new.setExpect(Util698.DealFrameWithParam(expect0.split("&&")[0],
					produceCaseResult_new.getADDR(), produceCaseResult_new.getProtocol()));

			produceCaseResult_new.setResult("0成0功0");
		}

		// xuky 2018.07.03
		iBaseDao_ProduceCaseResult.update(produceCaseResult_new);
		Util698.log(DealSendData.class.getName(), "ProduceCaseResult.update：" + produceCaseResult_new.getADDR()+"-"+produceCaseResult_new.getName()+"-"+produceCaseResult_new.getID()+"-DealData result="+produceCaseResult_new.getResult(), Debug.LOG_INFO);

		produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(produceCaseResult_new, new ProduceCaseResult(), "");
		Object[] s2 = { "DealTestCase", "old", produceCaseResult_tmp,"DealData"+"-"+this };
		Util698.log(DealSendData.class.getName(), "DealTestCase.old(DealData) " + produceCaseResult_tmp + " "+produceCaseResult_tmp.getADDR()+"."+produceCaseResult_tmp.getCaseno(),Debug.LOG_INFO);
		PublisherShowList.getInstance().publish(s2);
//		produceCaseResult_tmp = null;

		// System.out.println("DealTestCase old Result="+
		// produceCaseResult_new.getResult());

	}
}
