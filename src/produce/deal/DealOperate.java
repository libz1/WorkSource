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

// xuky 2017.06.15 单例模式，加锁进行数据处理
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
					// 双重检查加锁
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
		produceLog.setOperation("扫描条码(1)");
		final ProduceLog produceLog_new = iBaseDao_ProduceLog.create(produceLog);
		produceLogID = produceLog_new.getID();
		produceLogOptime = produceLog_new.getOpTime();

		// xuky 2017.06.15 以下两段程序，分别放在两个线程中执行，优先级就是一样的，不会出现异常的次序
		new Thread(() -> {
			MSG = "设备" + ADDR + " 开始测试...";
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
			// xuky 2017.07.14 刷新数据
			// xuky 2018.02.07 为节约时间，不进行数据刷新,放在TerminalParameterController的初始化代码中
			String planid = SoftParameter.getInstance().getParamValByKey("PLANID");

			// xuky 2017.11.07 根据planid判断测试的设备类型

			DealTestCase1 dealTestCase1 = new DealTestCase1();

//			Util698.log("xuky", "temp-dealTestCase1.Start", Debug.LOG_INFO);

			String[] rest = dealTestCase1.Start(planid, ID, ADDR,1 );
			RESULT = rest[0];
			devType = rest[1];
//			Util698.log(DealOperate.class.getName(), "RESULT-"+RESULT, Debug.LOG_INFO);

			// xuky 2017.10.09 需要关闭telnet单例对象
			TerminalTelnetSingle.getInstance("").destroy();

		}
		if (RESULT.equals("正在测试"))
			return;

		// System.out.println("DealOperate["+2+"]");

		// xuky 2017.06.15 执行完成，根据得到的结果进行分支操作
		ProduceLog produceLog = new ProduceLog();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setOpTime(DateTimeFun.getDateTimeSSS());

		if (RESULT.indexOf("成功")>=0) {
			playOkSound();
			MSG = "设备" + ADDR + " 测试成功！";
			produceLog.setOperation("测试成功(2)");
			ERRADDR = "";

			// xuky 2017.07.25 从devlist中查找信息，如果找到，则进行修改
			String[] s = { "DealOperate", "", "正在存储设备信息，请稍等..." };
			PublisherUI.getInstance().publish(s);

			List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ADDR + "'", "");
			DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
			if (devInfo != null) {
				devInfo.setStatus("测试完毕(1)");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(DateTimeFun.getDateTimeSSS());
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				String is_setid = SoftParameter.getInstance().getIS_SETID();
				if (is_setid.equals("表模块"))
					devInfo.setType("表模块(3)");
				if (is_setid.equals("路由"))
					devInfo.setType("路由(4)");
				iBaseDao_DevInfo.update(devInfo);
			} else {
				devInfo = new DevInfo();
				// xuky 2017.11.07 需要根据类型进行设备类型设置
				if (devType.equals("2"))
					devInfo.setType("集中器(2)");

				String is_setid = SoftParameter.getInstance().getIS_SETID();
				if (is_setid.equals("表模块"))
					devInfo.setType("表模块(3)");
				if (is_setid.equals("路由"))
					devInfo.setType("路由(4)");

				devInfo.setAddr(ADDR);
				devInfo.setStatus("测试完毕(1)");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(DateTimeFun.getDateTimeSSS());
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				// xuky 2018.06.26  测试结果为正常
				iBaseDao_DevInfo.create(devInfo);
			}

		} else {
			playErrSound();
			MSG = "设备" + ADDR + " 测试失败！ " + RESULT;
			produceLog.setOperation("测试失败(3)");
			produceLog.setOpResult(RESULT);
			ERRADDR = ADDR;

			// 记录异常设备地址信息，要求用户进行数据处理
		}

		// xuky 2017.08.29 添加延时，确保操作的时序正常，
		// 就目前的掌握，system.out.println   logger的操作都是多线程中的操作
//		Debug.sleep(500);

		iBaseDao_ProduceLog.create(produceLog);
		String op_status = "";
		// 2019.02.13 因为设备信息是单一的，之前的NG会被后面的OK覆盖
		if (RESULT.indexOf("成功")>=0)
			op_status = "OK";
		else
			op_status = "NG";

		if (addrToMES){
			try {
				// xuky 2018.06.26 需要向接口表中添加数据，用于接口传递数据
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
			String[] s = { "DealOperate", "", "无异常设备" };
			PublisherUI.getInstance().publish(s);
			return;
		}

		ProduceLog produceLog = new ProduceLog();
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setAddr(ERRADDR);
		String datetime = DateTimeFun.getDateTimeSSS();
		produceLog.setOpTime(datetime);
		produceLog.setOperation("设置设备异常(4)");
		iBaseDao_ProduceLog.create(produceLog);

		// 加入设备异常信息表

		// xuky 2017.08.01 需要在devInfos.barCode中保存设备测试失败的用例名称
		// 获取id信息
		IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
//		List result = iBaseDao_DevInfo
//				.retrieveBySQL("select max(ID) from " + ProduceLog.class.getName() + " where addr = '" + ERRADDR
//						+ "' and operation='扫描条码(1)' and workStation ='" + SoftParameter.getInstance().getPCID() + "'");
		// xuky 2017.09.07 因为可能在另外一台设备进行扫描操作，所以取消对workStation的限制
		List result = iBaseDao_DevInfo
		.retrieveBySQL("select max(ID) from " + ProduceLog.class.getName() + " where addr = '" + ERRADDR
				+ "' and operation='扫描条码(1)'" );
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

		// xuky 2017.07.25 如果之前已经添加了，就不要再添加了
		List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ERRADDR + "'", "");
		DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
		String is_setid = SoftParameter.getInstance().getIS_SETID();

		if (devInfo != null) {
			if (is_setid.equals("表模块"))
				devInfo.setType("表模块(3)");
			if (is_setid.equals("路由"))
				devInfo.setType("路由(4)");

			devInfo.setStatus("设备故障(2)");
			devInfo.setErrCode(name);
			devInfo.setErrcomputer(SoftParameter.getInstance().getPCID());
			devInfo.setErroperater(SoftParameter.getInstance().getUserManager().getUserid());
			devInfo.setErrdatetime(datetime);
			iBaseDao_DevInfo.update(devInfo);
		} else {
			devInfo = new DevInfo();
			if (is_setid.equals("表模块"))
				devInfo.setType("表模块(3)");
			if (is_setid.equals("路由"))
				devInfo.setType("路由(4)");
			devInfo.setErrCode(name);
			devInfo.setAddr(ERRADDR);
			devInfo.setErrcomputer(SoftParameter.getInstance().getPCID());
			devInfo.setErroperater(SoftParameter.getInstance().getUserManager().getUserid());
			devInfo.setErrdatetime(datetime);
			// xuky 2018.06.26  测试结果为异常
			iBaseDao_DevInfo.create(devInfo);
		}

		ADDR = ERRADDR;
		ERRADDR = "";
		MSG = "设备" + ADDR + " 设置为设备故障状态！";
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
			// 用输入流打开一音频文件
			InputStream in = new FileInputStream(Filename);// FIlename
															// 是你加载的声音文件如(“game.wav”)
			// 从输入流中创建一个AudioStream对象
			AudioStream as = new AudioStream(in);
			AudioPlayer.player.start(as);// 用静态成员player.start播放音乐
			// AudioPlayer.player.stop(as);//关闭音乐播放
			// 如果要实现循环播放，则用下面的三句取代上面的“AudioPlayer.player.start(as);”这句
			/*
			 * AudioData data = as.getData(); ContinuousAudioDataStream gg= new
			 * ContinuousAudioDataStream (data); AudioPlayer.player.start(gg);//
			 * Play audio.
			 */
			// 如果要用一个 URL 做为声音流的源(source)，则用下面的代码所示替换输入流来创建声音流：
			/*
			 * AudioStream as = new AudioStream (url.openStream());
			 */
		} catch (FileNotFoundException e) {
			System.out.print("FileNotFoundException ");
		} catch (IOException e) {
			System.out.print("有错误!");
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
