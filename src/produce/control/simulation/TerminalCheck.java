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
	// xuky 2019.02.22 集中器的等待时间为20秒
	int TernailStarTime = 80 * 1000;

	// 集中器是否完成了测试的标志
	Map<String, String> MeterFlags = new ConcurrentHashMap<String, String>(); // 表位是否运行完毕标志

	// 回复集中器地址为129.1.22.96时，需要使用这个信息  这也是集中器的测试ID信息
	Map<String, String> TerminalMACs = new ConcurrentHashMap<String, String>(); // 表位集中器MAC地址信息

	// 与终端通信时需要使用  是否有，如果有，如何通信
	Map<String, String> MetersEnd = new ConcurrentHashMap<String, String>(); // 表位通道测试完成情况
	Map<String, String> MetersRJ45 = new ConcurrentHashMap<String, String>(); // 表位的RJ45通道情况
	Map<String, String> HaveMeters = new ConcurrentHashMap<String, String>(); // 表位是否有集中器信息表
	Map<String, String> Params = new ConcurrentHashMap<String, String>(); // 表位通信参数信息表
	Map<String, String> MeterAddrs = new ConcurrentHashMap<String, String>(); // 表位集中器地址信息;
	Map<String, BaseCommLog> MetersResult = new ConcurrentHashMap<String, BaseCommLog>(); // 表位是否有集中器信息表
	Map<String, String> MetersChannel = new ConcurrentHashMap<String, String>(); // 表位通道情况
	Map<String, String> MetersChannelResults = new ConcurrentHashMap<String, String>(); // 表位通道测试情况
	int beginNum = 1;
	int MeterNum = 32;
	String begin_time = Util698.getDateTimeSSS_new();

	public void AllCheck(String COM) {

		// 测试结果保存到数据库
		ProduceLog produceLog = new ProduceLog();
		produceLog.setAddr("");
		produceLog.setOpName(SoftParameter.getInstance().getUserManager().getUserid());
		produceLog.setOpTime(DateTimeFun.getDateTimeSSS());
		produceLog.setOperation("集中器整机自检");
		// xuky 2019.02.28  添加 检测台体NO记录
		produceLog.setStageno(PlatFormParam.getInstance().getPlatFormNO());
		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		ProduceLog produceLog_new = iBaseDao_ProduceLog.create(produceLog);
		PlatFormParam.getInstance().setRUNID(produceLog_new.getID());

		String model = PlatFormParam.getInstance().getTerminalModel();

		// 1、加电   读取电压电流数据，如果有，则无需加电
//		SimuRun.raisePower(TernailStarTime);
		// 2、修改台体模式
		SimuRun.chaneMode(model, COM);

		//  3、检查通信情况，确认集中器是否在位
		String type = "信道检测";
	    Util698.log(TerminalCheck.class.getName(), type+"开始", Debug.LOG_INFO);

		new Thread(() -> {
			String[] s2 = { "result", "user data", "信道检测开始", ""};
			Publisher.getInstance().publish(s2);
		}).start();

		for (int i = beginNum; i <= MeterNum; i++) {

			// 32表位放在线程中并行执行
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
				String terminalMAC = ""; // 测试完成后，需要将IP设置为默认的129.1.22.96

				// 返回结果：通信IP、通信port、终端通信地址、待检信道列表、信道可用标志
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

				// 修改终端的IP地址
//				String NetIP = "192.168.127.";
				String NetIP = "129.1.22.";
				// 入参：当前的终端地址，通信参数、表位信息（根据表位信息，自动得到需要设置成为的终端地址）、
				// 返回：新的IP地址（同时也是终端地址）、MAC地址信息、操作结果对象
				result = SimuRun.setIP_MAC(meterAddr, param, meterno, NetIP);
				if (((BaseCommLog) result[2]).getResult().equals("OK")) {
					meterAddr = (String) result[0];
					if (param.indexOf("COM") < 0)
						param = meterAddr + ":7000"; // 如果原先的有效通信信道就是RJ45，则需要进行调整
					terminalMAC = (String) result[1];
//					HaveMeters.put(meterno, "1");
					Params.put(meterno, param);
					MeterAddrs.put(meterno, meterAddr);
					TerminalMACs.put(meterno, terminalMAC);
					MetersRJ45.put(meterno, "1");
				} else {
					MetersRJ45.put(meterno, "0"); // 有集中器，但是无法设置集中器地址和MAC
				}
				MetersEnd.put(meterno, "1");
				// 根据HaveMeters、MeterChannels、MeterChannelResults保存信道测试结果
			}).start();
		}


		// 4、整体性运行的判断过程
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

			// 测试结果保存到数据库中
			TerminalResult terminalResult = new TerminalResult();
			String mac = TerminalMACs.get(meterno);
			if (mac.equals(""))
				mac = meterno;
			terminalResult.setDevID(mac);
			terminalResult.setName(type);
			terminalResult.setRecvtime(Util698.getDateTimeSSS_new());
			String channelResult = MetersChannelResults.get(meterno);
			String meterRJ45 = MetersRJ45.get(meterno);
			// xuky 2019.02.27 通道判断标准需要调整
			if (channelResult.indexOf("0") <0 && meterRJ45.equals("1"))
				terminalResult.setResult("OK");
			else
				terminalResult.setResult("NG");

			terminalResult.setNote1("'"+MetersChannel.get(meterno)+"':"+channelResult+";RJ45:"+MetersRJ45.get(meterno));
			terminalResult.setRunID(produceLog_new.getID());
			TerminalResult TerminalResult_new = iBaseDao_TerminalResult.create(terminalResult);

		}
	    Util698.log(TerminalCheck.class.getName(), type+"完成", Debug.LOG_INFO);

	    // 单台体操作
//	    String[] types1 = {"日记时误差测试","12V测试"};
	    String[] types1 = {"日记时误差测试"};
	    Map<String, BaseCommLog> MeterResults = null;
	    Boolean is_skip1=false;
	    if (!is_skip1){
	    	for( String type1: types1 ){
			    Util698.log(TerminalCheck.class.getName(), type1+"开始", Debug.LOG_INFO);
				// 批量进行日记时误差测试
			    MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
				SimuRun.allDateTimeErrNum_new(HaveMeters, MeterResults, COM,type1);
				for (int i = beginNum; i <= MeterNum; i++) {
					String meterno = DataConvert.int2String(i);
					BaseCommLog log = MeterResults.get(meterno);
					if (log != null)
						add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type1);
				}
			    Util698.log(TerminalCheck.class.getName(), type1+"完成", Debug.LOG_INFO);
	    	}
	    }

	    // 台体与集中器配合操作
	    type = "遥信on测试";
	    Boolean is_skip2=false;
	    if (!is_skip2){
		    Util698.log(TerminalCheck.class.getName(), type+"开始", Debug.LOG_INFO);
			MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
			// 批量进行遥信测试
			SimuRun.allFS(COM,HaveMeters,MeterResults, Params, MeterAddrs,"on");
			for (int i = beginNum; i <= MeterNum; i++) {
				String meterno = DataConvert.int2String(i);
				BaseCommLog log = MeterResults.get(meterno);
				if (log != null)
					add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type);
			}
		    Util698.log(TerminalCheck.class.getName(), type+"完成", Debug.LOG_INFO);

		    type = "遥信off测试";
		    Util698.log(TerminalCheck.class.getName(), type+"开始", Debug.LOG_INFO);
			MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
			// 批量进行遥信测试
			SimuRun.allFS(COM,HaveMeters,MeterResults, Params, MeterAddrs,"off");
			for (int i = beginNum; i <= MeterNum; i++) {
				String meterno = DataConvert.int2String(i);
				BaseCommLog log = MeterResults.get(meterno);
				if (log != null)
					add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type);
			}
		    Util698.log(TerminalCheck.class.getName(), type+"完成", Debug.LOG_INFO);

	    }

	    //  GPRS信息如何读取(IP PORT )？  112.6.118.246  9002 检测异常，已经连接了，但是01
	    //  RT 长期返回02 信息？路由模块与表模块必须匹配？需要对应参数格式   南网与国网不匹配 ，建议自动判断 添加两个电表地址
	    // 国网USB未检查     南网USB无效
	    // 直流模拟量 电压＼电流读取  台表不支持

	    // 测试前先进行时钟设置、然后再结束时执行时钟读取

//	    String[] types = {"按键测试","USB测试","电池测试","液晶测试","GPRS设置","时钟设置","时钟测试","ESAM测试","GPRS测试"};
	    // 单集中器操作
	    String[] types = {"按键测试","电池测试","液晶测试","GPRS设置","时钟设置","时钟测试","ESAM测试"};
	    Boolean is_skip3=true;
	    if (!is_skip3){
	    	for( String type1: types ){
			    Util698.log(TerminalCheck.class.getName(), type1+"开始", Debug.LOG_INFO);
			    MeterResults = new ConcurrentHashMap<String, BaseCommLog>();
			    SimuRun.allButtons(COM,HaveMeters,MeterResults, Params, MeterAddrs,type1);
				for (int i = beginNum; i <= MeterNum; i++) {
					String meterno = DataConvert.int2String(i);
					BaseCommLog log = MeterResults.get(meterno);
					if (log != null)
						add2TermialResult(produceLog_new, iBaseDao_TerminalResult, meterno, log,type1);
				}
			    Util698.log(TerminalCheck.class.getName(), type1+"完成", Debug.LOG_INFO);
	    	}
	    }

	    // 路由测试，逐个进行，无法批量，可以考虑在通信不冲突的情况下，与其他任务并行进行
	    type = "路由测试";
	    Boolean is_skip4=true;
	    if (!is_skip4){
	    	String sendData = PlatFormParam.getInstance().getRT_MeterAddr();
	    	if (model.equals("南网"))
	    		sendData = PlatFormParam.getInstance().getRT_MeterAddr_NW();
	    	sendData = "000000000000" + sendData;
	    	sendData = sendData.substring(sendData.length()-12);  // 得到表地址信息
	    	sendData +=  "000000000000";  // 添加抄表参数信息
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

	    Util698.log(TerminalCheck.class.getName(), model+"模式测试完成！数量"+(MeterNum-beginNum+1), Debug.LOG_INFO);
	    Util698.log(TerminalCheck.class.getName(), "开始时间"+begin_time +" 结束时间"+end_time+" 耗时"+Util698.getMilliSecondBetween_new(end_time, begin_time), Debug.LOG_INFO);

		// ！！！允许用户 对测试未完成的项目，继续一轮测试  对于测试通过的项目，为节约时间起见，不要再进行测试

		// 不要自动进行，因为可能需要用户进行一些调整过程，例如拔插网线、拔插USB、按键、液晶检测等，
		// ！！！然后再自动对所有未合格项目自动重新检测

		// 恢复集中器的IP地址

	}

	private void add2TermialResult(ProduceLog produceLog_new, IBaseDao<TerminalResult> iBaseDao_TerminalResult,
			String meterno, BaseCommLog log, String type) {
		TerminalResult terminalResult = new TerminalResult();
		String mac = TerminalMACs.get(meterno);
		if (mac.equals(""))
			mac = meterno;
		// xuky 2019.02.28 记录表位信息
		String recv = log.getRecv();
		if (recv == null || recv.equals(""))
			log.setSpecialData("超时");
		else{
			if (type.indexOf("遥信") >= 0 && log.getResult().equals("NG") ){
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
	 * 2019.02.22 升源 升源后，等待大约10-20秒，然后再进行其他操作（时间值需要尽量精确一些，再宽容些） 确保集中器程序加载完成
	 *
	 * 台体模式设置
	 *
	 * 【自动检测表位状态】各个表位的通信判断【并行进行】可以确定设备是否在位 根据表位自动切换IP地址，设置MAC地址 非RJ45通道，都可以
	 *
	 * 可能需要的操作：根据表位情况， 表位有设备，自动进行电压接入 表位无设备，自动进行电流接入
	 *
	 * 日记时误差测试 对表台进行操作 【并行进行】日记时误差操作 等待全部操作完成 对表台进行总清，回复到原始状态
	 *
	 * 其他项目的测试 遥信、12V、直流模拟量电压电流 GPRS检测、时钟检测、电池状态 路由检测 ESAM检测 按键、USB、液晶
	 * 【可以对项目进行补测】
	 *
	 * 等待所有项目测试完成【等待机制的开发！！！】
	 *
	 * 【展示测试结果】 【提示测试进度信息】
	 *
	 *
	 * 台体断电前，需要进行IP地址的切回操作 读取IP和MAC，写入129.1.22.96和MAC 读取验证
	 *
	 * 【加锁】 红外通信 RT通信 串口台体通信
	 *
	 * 【需用用户设定的信息】 遥信有几路 国网模式（是否检测ESAM）、南网模式 路由参数，默认一个表地址即可
	 *
	 *
	 *
	 * 【待定项目】 检查电流是否正常，无电流不影响终端功能测试！！！ 如果有必要，就进行降源再升源的操作
	 */
}
