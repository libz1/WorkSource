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
		// numOfAll 表示当前的执行序号信息 ，分别是1、2、3...
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

//		Util698.log("效率分析", "数据库交互2 begin1" , Debug.LOG_INFO);
		ADDR = addr;
		// xuky 2018.04.13 调整代码，不要new，使用原先的，只要进行init即可
		produceLog.init();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());
		produceLog.setOpTime(Util698.getDateTimeSSS_new());
		produceLog.setOperation("扫描条码(1)");

//		Util698.log("效率分析", "数据库交互2 begin2" , Debug.LOG_INFO);
		final ProduceLog produceLog_new1 = iBaseDao_ProduceLog.create(produceLog);
		produceLogID = produceLog_new1.getID();
		produceLogOptime = produceLog_new1.getOpTime();
//		Util698.log("效率分析", "数据库交互2 end1" , Debug.LOG_INFO);

		// xuky 2017.06.15 以下两段程序，分别放在两个线程中执行，优先级就是一样的，不会出现异常的次序
		MSG = "设备" + ADDR + " 开始测试...";
		String[] s = { "DealOperate", "", MSG };
		PublisherUI.getInstance().publish(s);
		Util698.log(DealOperateMuti.class.getName(), MSG, Debug.LOG_INFO);

//		Util698.log("效率分析", "数据库交互2 end2" , Debug.LOG_INFO);


		BeginTest(produceLogID,numOfAll);

	}

	private void BeginTest(int ID,int numOfAll) {
		String devType = "1";
		{
			// xuky 2017.07.14 刷新数据
			// xuky 2018.03.14 为了提高效率，更改参数后，重启生效
//			SoftParameter.refreshDataFromDB(); // 数据库交互2

			// xuky 2017.06.15 执行具体的耗时较长的任务
//			RESULT = DealTestCase.getInstance().Start(SoftParameter.getInstance().getParamValByKey("PLANID"), ID, ADDR);

			try{
				String planid = SoftParameter.getInstance().getParamValByKey("PLANID");
				// xuky 2017.11.07 根据planid判断测试的设备类型
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

				// xuky 2017.10.09 需要关闭telnet单例对象
				if (devType == "2")
					TerminalTelnetSingle.getInstance("").destroy();

			}
			catch(Exception e){
				Util698.log(DealOperateMuti.class.getName(), "DealTestCase1().Start ERROR-"+e.getMessage(), Debug.LOG_INFO);
			}
		}
		if (RESULT.equals("正在测试"))
			return;

		// System.out.println("DealOperate["+2+"]");

		// xuky 2017.06.15 执行完成，根据得到的结果进行分支操作
		produceLog.init();
		produceLog.setAddr(ADDR);
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());

		produceLog.setOpTime(Util698.getDateTimeSSS_new());

		if (RESULT.indexOf("成功")>=0) {
//			playOkSound();
			MSG = "【2】设备" + ADDR + " 测试成功！";
			produceLog.setOperation("测试成功(2)");
			ERRADDR = "";

			// xuky 2017.07.25 从devlist中查找信息，如果找到，则进行修改
			List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + ADDR + "'", "");
			DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
			if (devInfo != null) {
				if (devType.equals("2"))
					devInfo.setType("集中器(2)");
				devInfo.setStatus("测试完毕(1)");
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
				// xuky 2017.11.07 需要根据类型进行设备类型设置
				if (devType.equals("2"))
					devInfo.setType("集中器(2)");
				devInfo.setAddr(ADDR);
				devInfo.setStatus("测试完毕(1)");
				devInfo.setOkcomputer(SoftParameter.getInstance().getPCID());
				devInfo.setOkoperater(SoftParameter.getInstance().getUserManager().getUserid());
				devInfo.setOkdatetime(Util698.getDateTimeSSS_new());
				if (RESULT.indexOf("-") >= 0)
					devInfo.setBarCode(RESULT.split("-")[1]);
				else
					devInfo.setBarCode("");
				// xuky 2018.06.26  测试结果为正常
				iBaseDao_DevInfo.create(devInfo);
			}
			devInfo = null;
			devInfos = null;

		} else {
//			playErrSound();
			MSG = "设备" + ADDR + " 测试失败！ " + RESULT;
			produceLog.setOperation("测试失败(3)");
			produceLog.setOpResult(RESULT);
			ERRADDR = ADDR;

			// xuky 2017.08.31 在进行批量操作设备时，发现错误就视为异常
			setErr();

			// 记录异常设备地址信息，要求用户进行数据处理
		}
		// xuky 2018.04.17 直接设置为异常了吗，无需进行数据记录
//		SoftParameter.getInstance().setERRADDR(ERRADDR);
//		SoftParameter.getInstance().saveParam();

		iBaseDao_ProduceLog.create(produceLog);
		String[] s = { "DealOperate", "", MSG };
		PublisherUI.getInstance().publish(s);
		Util698.log(DealOperateMuti.class.getName(), MSG, Debug.LOG_INFO);

		if (addrToMES){
			try {
				// xuky 2018.06.26 需要向接口表中添加数据，用于接口传递数据
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
		produceLog.setOperation("设置设备异常(4)");
		iBaseDao_ProduceLog.create(produceLog);

		// 加入设备异常信息表

		// xuky 2017.08.01 需要在devInfos.barCode中保存设备测试失败的用例名称
		// 获取id信息
		List result = iBaseDao_DevInfo
				.retrieveBySQL("select max(ID) from " + ProduceLog.class.getName() + " where addr = '" + ERRADDR
						+ "' and operation='扫描条码(1)' and workStation ='" + SoftParameter.getInstance().getPCID() + "'");
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

		// xuky 2017.07.25 如果之前已经添加了，就不要再添加了
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
			// xuky 2018.06.26  测试结果为异常
			iBaseDao_DevInfo.create(devInfo);
		}
		devInfo = null;
		devInfos = null;
		ADDR = ERRADDR;
		ERRADDR = "";
		MSG = "设备" + ADDR + " 设置为设备故障状态！";
		// xuky 2018.07.04 在外部进行publish，这里不要进行了
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
		// xuky 2018.04.13 去掉此声音提醒
//		newThread(() -> play1(Filename)).start();
	}

	private void play1(String Filename) {
//		try {
//			// 用输入流打开一音频文件
//			InputStream in = newFileInputStream(Filename);// FIlename
//															// 是你加载的声音文件如(“game.wav”)
//			// 从输入流中创建一个AudioStream对象
//			AudioStream as = newAudioStream(in);
//			AudioPlayer.player.start(as);// 用静态成员player.start播放音乐
//			// AudioPlayer.player.stop(as);//关闭音乐播放
//			// 如果要实现循环播放，则用下面的三句取代上面的“AudioPlayer.player.start(as);”这句
//			/*
//			 * AudioData data = as.getData(); ContinuousAudioDataStream gg= new
//			 * ContinuousAudioDataStream (data); AudioPlayer.player.start(gg);//
//			 * Play audio.
//			 */
//			// 如果要用一个 URL 做为声音流的源(source)，则用下面的代码所示替换输入流来创建声音流：
//			/*
//			 * AudioStream as = new AudioStream (url.openStream());
//			 */
//		} catch (FileNotFoundException e) {
//			System.out.print("FileNotFoundException ");
//		} catch (IOException e) {
//			System.out.print("有错误!");
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
