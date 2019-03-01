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
		String msg = "验证集中器数据项" + dataitem + " 应为" + data;
		// 6800008101160C6894000816
		// xuky 2019.02.27 sendData为空，则多数为期望确保报文
		// data不为空，表示需要收到带数据标示的数据报文
		if (!sendData.equals("") && data.equals("") )
			dataitem = "";
		String expect = "";
		if (inputExpect == null || inputExpect.equals(""))
			expect = getTermianlFrame(terminlIP, "94", dataitem, data);
		else
			expect = inputExpect;
		result = commWithRecv.deal_one("【" + msg + "】", param, sData, expect, 5000);
		return result;
	}

	private static String getTermianlFrame(String terminalIP, String type) {
		if (type.indexOf("读") >= 0 && type.indexOf("时钟") >= 0) {
			return getTermianlFrame(terminalIP, "14", "04 96 96 05", "");
		}
		if (type.indexOf("读") >= 0 && type.indexOf("IP") >= 0) {
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
		// 15、电压回路
		// 主机:01H+地址(A—Z) +长度+AAH(命令) +相别(H-30H,A-31H,B-32H,C-33H)+(30H/断开
		// 31H/接入)+校验位+结束(17H)
		// 从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定) +校验位+结束(17H)
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
			// 多个表位循环
			for (int i = 1; i <= 32; i++) {
				// 多个相位循环
				String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(DataConvert.int2String(i))
						+ " 06 06 06 17";
				framePlatform.setADDR(DataConvert.int2String(i));
				framePlatform.setCONTROL("AA");
				framePlatform.setDATA("30" + con);
				sData = framePlatform.getFrame();
				String msg = "表位" + i + "电压" + type;
				tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
				Util698.log(SimuRun.class.getName(), msg + "结果:" + tmp, Debug.LOG_INFO);
			}
		} else {
			// 广播方式 多个相位循环
			framePlatform.setADDR("FF");
			framePlatform.setCONTROL("AA");
			framePlatform.setDATA("30" + con);
			sData = framePlatform.getFrame();
			String msg = "广播所有表位电压" + type + "，无回复";
			// 广播模式下，发送3次，确保执行的正确性
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		}

	}

	public static boolean singelMeterVoltageOn(String meter, String COM) {
		// 15、电压回路
		// 主机:01H+地址(A—Z) +长度+AAH(命令) +相别(H-30H,A-31H,B-32H,C-33H)+(30H/断开
		// 31H/接入)+校验位+结束(17H)
		// 从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定) +校验位+结束(17H)

		// 切换所有电压
		allMeterVoltageControl(COM, "off");

		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();

		// 单表位 接入电压
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("AA");
		framePlatform.setDATA("3031");
		sData = framePlatform.getFrame();
		String msg = "表位" + meter + "电压接入";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	public static void allMeterCurrentOff(String meter, String COM) {
		// 14、 电流回路复位/断开/闭合
		// 主机:01H+地址(A—Z) +长度+A8H(命令) +(30H/断开 31H/接入 32H/复位)+校验位+结束(17H)

		// 短接所有电流
		String sData = "";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();

		// 短接所有电流
		framePlatform.setADDR("FF");
		framePlatform.setCONTROL("A8");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		String msg = "广播所有表位电流断开,无回复";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

	}

	public static void singelMeterCurrentOn(String meter, String COM) {
		// 14、 电流回路复位/断开/闭合
		// 主机:01H+地址(A—Z) +长度+A8H(命令) +(30H/断开 31H/接入 32H/复位)+校验位+结束(17H)

		// 短接所有电流
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();

		// 短接所有电流
		framePlatform.setADDR("FF");
		framePlatform.setCONTROL("A8");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		String msg = "广播所有表位电流接入,无回复";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "", 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

		// 单表位 断开电流
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("A8");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "电流断开";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

	}

	public Boolean raisePower() {
		return raisePower(0);
	}

	// 升源
	public static Boolean raisePower(int sleepTime) {
		String COM = PlatFormParam.getInstance().getPlatFormCOM();
		CommWithRecv commWithRecv = new CommWithRecv();
		int maxstep = 5;
		String param = "COM" + COM + ":9600:NONE";
		String sData = "F9 F9 F9 F9 F9 B1 03 00 00 00 01 9E 3A";
		String expect = "B1 03 02 16 03 B7 FF";
		BaseCommLog tmp = commWithRecv.deal_one("【升源1-" + maxstep + "】", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "4D 53 30 0D";
		expect = "4D 53 41 43 4B 3B";
		tmp = commWithRecv.deal_one("【升源2-" + maxstep + "】", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "49 42 30 36 0D";
		expect = "49 42 41 43 4B 3B";
		tmp = commWithRecv.deal_one("【升源3-" + maxstep + "】", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "55 42 31 0D";
		expect = "55 42 41 43 4B 3B";
		tmp = commWithRecv.deal_one("【升源4-" + maxstep + "】", param, sData, expect);
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
		tmp = commWithRecv.deal_one("【升源5-" + maxstep + "】", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;

		// xuky 2019.02.22 根据设定的参数，进行等待，等待集中器等设备加载完成
		if (sleepTime != 0){
		    Util698.log(SimuRun.class.getName(), "sleep"+sleepTime+"等待集中器初始化完成", Debug.LOG_INFO);
			Debug.sleep(sleepTime);
		}
		return true;
	}

	public static Boolean PowerOff(String COM) {
		CommWithRecv commWithRecv = new CommWithRecv();
		String param = "COM" + COM + ":9600:NONE";
		String sData = "FE FE FE FE FE 01 41 07 33 31 64 17";
		String expect = "FE FE FE FE FE 01 41 06 06 06 17";
		BaseCommLog tmp = commWithRecv.deal_one("【降源1-2】", param, sData, expect);
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
		tmp = commWithRecv.deal_one("【降源2-2】", param, sData, expect);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	public static boolean chaneMode(String model, String COM) {
		String sData = "";
		if (model.indexOf("国网") >= 0 || model.indexOf("1") >= 0)
			sData = "FE FE FE FE FE 01 41 07 4B 32 7D 17";
		if (model.indexOf("南网") >= 0 || model.indexOf("2") >= 0)
			sData = "FE FE FE FE FE 01 41 07 4B 31 7C 17";
		String expect = "FE FE FE FE FE 01 41 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		// 台体通信的参数为9600-无校验 NONE
		BaseCommLog tmp = commWithRecv.deal_one("【" + model + "模式】", "COM" + COM + ":9600:NONE", sData, expect);
		if (tmp.getResult().equals("OK"))
			return true;
		else
			return false;
	}

	public static boolean setFSFlag(String meter, String COM, String FSFlag) {
		return setFSFlag(meter, COM, FSFlag, 0);
	}

	public static boolean setFSFlag(String meter, String COM, String FSFlag, int num) {
		// 主机:01H+地址(A—Z) +长度+B4H(命令) +(30H/短接 31H/外加电压 32/脉冲输出)+校验位+结束(17H)
		String sData = "", msg = "";
		BaseCommLog tmp = null;
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		if (meter.equals("FF"))
			expect = "";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		if (num == 0) { // 首次运行时需要执行
			framePlatform.setCONTROL("B4");
			framePlatform.setDATA("30");
			sData = framePlatform.getFrame();
			msg = "表位" + meter + "设置遥信方式短接";
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect,500);
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			if (!tmp.getResult().equals("OK"))
				return false;
		}

		// 主机:01H+地址(A—Z) +长度+A2H(命令)+ 遥信状态字(1字节)+校验位+结束(17H)
		framePlatform.setCONTROL("A2");
		framePlatform.setDATA(DataConvert.binStr2HexString(FSFlag, 2));
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "遥信输出" + FSFlag;
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect,500);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;

	}

	public static boolean read12V(String meter, String COM) {
		// 01H+地址(A—Z) +长度+B2H(命令) +(30H/开始 31H/停止)+校验位+结束(17H)
		// 单表位 接入电压
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		BaseCommLog tmp = null;
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("B2");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		String msg = "表位" + meter + "直流电压检测开始";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// Debug.sleep(2000);
		expect = "get12V:substring(18,26);val(12);region(-0.2,0.2);";
		framePlatform.setCONTROL("B3");
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "直流电压检测读取";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// FE FE FE FE FE 01 42 0A 30 30 36 33 38 01 17 638 = 478.5
		// 需要对读取的电压数据进行进一步的处理

		expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		framePlatform.setCONTROL("B2");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "直流电压检测停止";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	private void singleCase() {
		int max = 1;
		String caseNO = "96";
		// 多表位测试记录存储的List
		List<MeterRunInfo> meterRunInfos = new CopyOnWriteArrayList<MeterRunInfo>();
		// 提前提取出来的测试用例信息
		List<ProduceCase> produceCases = new ProduceCaseDaoImpl().retrieve(
				"where subid='" + caseNO + "' and computer='" + SoftParameter.getInstance().getPCID() + "'",
				" order by caseno");
		// 串口信息
		Map<String, SerialParam> serialMap = new ConcurrentHashMap<String, SerialParam>();
		List<SerialParam> result = new SerialParamFaoImpl().retrieve("", "");
		for (SerialParam sp : result)
			serialMap.put(sp.getCOMID(), sp);

		String bTime = Util698.getDateTimeSSS_new();
		Util698.log(SimuRun.class.getName(), "表位数量：" + max + " 测试开始", Debug.LOG_INFO);

		// 逐个表位的创建测试实例对象
		for (int i = 1; i <= max; i++)
			meterRunInfos.add(new MeterRunInfo(DataConvert.int2String(i), produceCases, serialMap));

		// 在线程启动测试实例对象的测试过程
		for (MeterRunInfo meterinfo : meterRunInfos) {
			new Thread(() -> {
				meterinfo.run();
			}).start();
		}

		// 判断测试过程是否执行结束
		Boolean endFlag = true;
		while (true) {
			endFlag = true;
			for (MeterRunInfo meterinfo : meterRunInfos) {
				// System.out.println(meterinfo.getEndTime());
				if (meterinfo.getEndTime().equals("")) {
					// 表示还有数据 没有处理完
					endFlag = false;
					// System.out.println(meterinfo.getMeterno());
					break;
				}
			}
			if (endFlag == false) {
				Debug.sleep(500);
			} else {
				// 如果所有的meterinfo的getEndTime都有数据了，在表示任务全部完成
				String eTime = Util698.getDateTimeSSS_new();
				int usingTime = Util698.getMilliSecondBetween_new(eTime, bTime).intValue();
				Util698.log(SimuRun.class.getName(),
						"表位数量：" + max + " 测试结束 测试耗时:" + usingTime + " 平均耗时:" + (usingTime / max), Debug.LOG_INFO);
				break;
			}
		}

	}

	public static void outPut(String meter, String COM, String val, String type) {

		String data_type = "", data_val = "";
		if (type.equals("电压")) {
			data_type = "31";
			// 3.5V
			data_val = "00 A6 A6";
		} else {
			data_type = "30";
			// 18.5mA
			data_val = "00 39 39";
		}

		// 主机:01H+地址(A—Z) +长度+B6H(命令) +(31H/电压 30H/电流)+校验位+结束(17H)
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR(meter);
		framePlatform.setCONTROL("B6");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		String msg = "表位" + meter + "设置" + type + "输出";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

		// 设置PWM占空比
		// 主机:01H+地址(A—Z) +长度+B0H(命令) +高位1+高位+低位+校验位+结束(17H)
		framePlatform.setCONTROL("B0");
		framePlatform.setDATA(data_val);
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "设置" + type + val;
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);

		// 主机:01H+地址(A—Z) +长度+B1H(命令) +(30H/开始 31H/停止)+校验位+结束(17H)
		framePlatform.setCONTROL("B1");
		framePlatform.setDATA("30");
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "输出" + type + "开始";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
	}

	public static void stopOutPut(String meter, String COM) {
		// 主机:01H+地址(A—Z) +长度+B1H(命令) +(30H/开始 31H/停止)+校验位+结束(17H)
		String sData = "";
		String expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		CommWithRecv commWithRecv = new CommWithRecv();
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setCONTROL("B1");
		framePlatform.setDATA("31");
		sData = framePlatform.getFrame();
		String msg = "表位" + meter + "输出结束";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
	}
	// 批量进行日记时误差读取  因为其中有对整个台体的操作。必须集中进行
	public static boolean allDateTimeErrNum_new(Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults, String COM, String type) {

		if (type.indexOf("日记时")>=0){
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
			String msg = "表位" + meter + "直流电压检测开始";
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "");
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "");

			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			if (!tmp.getResult().equals("OK"))
				return false;
			AllRun allRun = new AllRun(HaveMeters,COM,MeterResults,null,null,null,null,null,"get12V:substring(18,26);val(12);region(-0.2,0.2);");
			Map<String, BaseCommLog> result= allRun.run("singleDateTimeErrNum");

			framePlatform.setCONTROL("B2");
			framePlatform.setDATA("31");
			sData = framePlatform.getFrame();
			msg = "表位" + meter + "直流电压检测停止";
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "");
			tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, "");
			Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
			if (!tmp.getResult().equals("OK"))
				return false;
			return true;
		}
		return true;
	}
	public static boolean allButtons(String COM, Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults,Map<String, String> Params,Map<String, String> MeterAddrs, String type) {
		String  dataitem = "04 96 96 0A", data = "00", sendData = "", expect= "";
		if (type.indexOf("按键") >= 0){
			dataitem = "04 96 96 0A";
			data = "00";
		}
		if (type.indexOf("USB") >= 0){
			dataitem = "04 96 96 0B";
			data = "00";
		}
		if (type.indexOf("电池") >= 0){
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
		if (type.indexOf("液晶") >= 0){
			dataitem = "04 96 96 12";
			data = "00";
		}

		if (type.indexOf("GPRS") >= 0 && type.indexOf("测试") >= 0){
			dataitem = "04 96 96 13";
			data = "00";
		}

		if (type.indexOf("GPRS") >= 0 && type.indexOf("设置") >= 0){
			dataitem = "04 96 96 06";
			data = "";
			String IP = PlatFormParam.getInstance().getGPRS_IP();
			String port = PlatFormParam.getInstance().getGPRS_Port();
			sendData = Util698.StrIP2HEX(IP)+DataConvert.int2HexString(DataConvert.String2Int(port), 4);
		}

		if (type.indexOf("时钟") >= 0 && type.indexOf("设置") >= 0){
			dataitem = "04 96 96 04";
			data = "";
			sendData = Util698.getDateTimeSSS_new();
			sendData = sendData.replaceAll("-", "");
			sendData = sendData.replaceAll(":", "");
			sendData = sendData.replaceAll(" ", "");
			sendData = sendData.substring(2);
			sendData = sendData.substring(0,sendData.length()-3);
		}
		if (type.indexOf("时钟") >= 0 && type.indexOf("测试") >= 0){
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
	// 批量进行日记时误差读取  因为其中有对整个台体的操作。必须集中进行
	public static boolean allDateTimeErrNum(int maxNum,Map<String, String> HaveMeters, Map<String, BaseCommLog> MeterResults, String COM) {
		if (!dateTimeErrBegin(COM))
			return false;

		// 1、用于整体性运行判断的队列
		Map<Integer, MeterRunInfo> runInfos =  new ConcurrentHashMap<Integer, MeterRunInfo>(); // 表位运行情况
		for (int i = 1; i <= maxNum; i++) {

			// 2、向队列中添加个体数据
			MeterRunInfo meterRunInfo = new MeterRunInfo();
			String meterno = DataConvert.int2String(i);
			meterRunInfo.setMeterno(meterno);
			runInfos.put(i, meterRunInfo);

			if (HaveMeters.get(meterno).equals("1"))
				new Thread(() -> {
					Object[] result = singleDateTimeErrNum(meterRunInfo.getMeterno(),COM);
					if ((boolean)result[0])
						MeterResults.put(meterno, (BaseCommLog) result[1]);
					// 3、执行完毕，设置个体数据的标志熟悉
					meterRunInfo.setEndflag("1");
				}).start();
			else
				// 3、执行完毕，设置个体数据的标志熟悉
				meterRunInfo.setEndflag("1");
		}

		// 4、整体性运行的判断过程
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
		// 标准时钟仪切换 切上  针对台体的操作
		CommWithRecv commWithRecv = new CommWithRecv();
		String sData = "FE FE FE FE FE 01 A9 03 06 09 17 0D";// 切上
		String expect = "FEFEFEFEFE01A906060617";
		String msg = "脉冲通道切上";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		return true;
	}

	public static boolean dateTimeErrEnd(String COM) {
		if (Debug_NoPLAT)
			return true;
		// 标准时钟仪切换 切下  针对台体的操作
		CommWithRecv commWithRecv = new CommWithRecv();
		String sData = "FE FE FE FE FE 01 A9 03 07 0A 17 0D"; // 切下
		String expect = "FEFEFEFEFE01A906060617";
		String msg = "脉冲通道切下";
		BaseCommLog tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "FE FE FE FE FE 01 FF 06 0C 0C 17";
		expect = "";
		msg = "广播总清命令";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		return true;
	}

	public static Object[] singleDateTimeErrNum(String meter, String COM) {
		Object[] ret = {false,null};
		// 主机：01H+地址(A——Z) +长度+11H(命令)+(通道编号)+校验位+结束(17H)
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
		String msg = "表位" + meter + "日记时误差【1-" + max + "】脉冲通道切换";
		BaseCommLog tmp = null;

		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return ret;


		// FE FE FE FE FE 01 4C 18 27 30 30 30 30 36 30 30 30 30 30 30 33 30 30
		// 30 30 30 30 90 17
		// FE FE FE FE FE 01 4C 18 27 【30 30 30 30 36 30】 【30 30 30 30 30 33 30
		// 30 30 30 30 30】 90 17
		// 进入长时间误差测试状态—秒脉冲
		// 主机：01H + 地址(A-Z) +长度+ 27H(命令)+圈数或脉冲数(6位)[60]
		// +理论脉冲数(12位)[60*50000 – HEX ] +通道号(35H)+校验位+结束(17H)
		// 主机：01H+地址(A——Z) +长度+28H(命令)+校验位+结束(17H)
		// 完全的使用台体控制协议进行操作 ： 间隔一定的时间，以后进行此数据的读取
		framePlatform.setCONTROL("27");
		framePlatform.setDATA("30 30 30 30 36 30 30 30 30 30 30 33 30 30 30 30 30 30");
		expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "日记时误差【3-" + max + "】开始测试时钟误差";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return ret;

		// ??? 自动发送FE FE FE FE FE 01 A9 03 06 09 17 0D？？？

		// FE FE FE FE FE 01 4C 06 28 28 17
		// 主机：01H+地址(A——Z) +长度+28H(命令)+校验位+结束(17H)

		Debug.sleep(61 * 1000); // 需要等待60秒

		framePlatform.setCONTROL("28");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
		expect = "getTimeErr:substring(28,52);val(3000000);region(-0.5,0.5);";
		msg = "表位" + meter + "日记时误差【4-" + max + "】读取时钟误差";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		ret[1] = tmp;
		if (tmp.getResult().equals("OK"))
			ret[0] = true;
		return ret;
	}

	// !!!注意日记时误差的部分操作是针对整个台体的
	// 最好在最后的时候统一执行
	public static boolean dateTimeErrNum(String meter, String COM) {
		// 主机：01H+地址(A——Z) +长度+11H(命令)+(通道编号)+校验位+结束(17H)
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
		String msg = "表位" + meter + "日记时误差【1-" + max + "】脉冲通道切换";
		BaseCommLog tmp = null;

		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// 标准时钟仪切换 切上 没有针对某个表位的，是全部的？
		sData = "FE FE FE FE FE 01 A9 03 06 09 17 0D";// 切上
		expect = "FEFEFEFEFE01A906060617";
		msg = "表位" + meter + "日记时误差【2-" + max + "】脉冲通道切上";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// FE FE FE FE FE 01 4C 18 27 30 30 30 30 36 30 30 30 30 30 30 33 30 30
		// 30 30 30 30 90 17
		// FE FE FE FE FE 01 4C 18 27 【30 30 30 30 36 30】 【30 30 30 30 30 33 30
		// 30 30 30 30 30】 90 17
		// 进入长时间误差测试状态—秒脉冲
		// 主机：01H + 地址(A-Z) +长度+ 27H(命令)+圈数或脉冲数(6位)[60]
		// +理论脉冲数(12位)[60*50000 – HEX ] +通道号(35H)+校验位+结束(17H)
		// 主机：01H+地址(A——Z) +长度+28H(命令)+校验位+结束(17H)
		// 完全的使用台体控制协议进行操作 ： 间隔一定的时间，以后进行此数据的读取
		framePlatform.setCONTROL("27");
		framePlatform.setDATA("30 30 30 30 36 30 30 30 30 30 30 33 30 30 30 30 30 30");
		expect = "FE FE FE FE FE 01 " + PlatFormUtil.getMetrNo(meter) + " 06 06 06 17";
		sData = framePlatform.getFrame();
		msg = "表位" + meter + "日记时误差【3-" + max + "】开始测试时钟误差";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		// ??? 自动发送FE FE FE FE FE 01 A9 03 06 09 17 0D？？？

		// FE FE FE FE FE 01 4C 06 28 28 17
		// 主机：01H+地址(A——Z) +长度+28H(命令)+校验位+结束(17H)
		Debug.sleep(61 * 1000); // 需要等待60秒
		framePlatform.setCONTROL("28");
		framePlatform.setDATA("");
		sData = framePlatform.getFrame();
		expect = "getTimeErr:substring(28,52);val(3000000);region(-0.5,0.5);";
		msg = "表位" + meter + "日记时误差【4-" + max + "】读取时钟误差";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		sData = "FE FE FE FE FE 01 A9 03 07 0A 17 0D";
		expect = "FEFEFEFEFE01A906060617";
		msg = "表位" + meter + "日记时误差【5-" + max + "】脉冲通道切下";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;
		sData = "FE FE FE FE FE 01 FF 06 0C 0C 17";
		expect = "";
		msg = "广播日记时误差【6-" + max + "】总清命令";
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		tmp = commWithRecv.deal_one("【" + msg + "】", "COM" + COM + ":9600:NONE", sData, expect, 1000);
		Util698.log(SimuRun.class.getName(), msg, Debug.LOG_INFO);
		if (!tmp.getResult().equals("OK"))
			return false;

		return true;
		//
	}

	// 国网或南网各种集中器通信通道检测
	// 返回结果：通信IP、通信port、终端通信地址、待检信道列表、信道可用标志
	public static Object[] getUserfulChanel(String meterno, String COM, String model) {
		String[] channels = null; // 需要验证信道个数，其先后次序也是信道选用的优先级
		Object[] ret = { "", 0, "", channels, "" }; // IP、port、terminalIP、信道列表、信道可用信息
		// 通过RJ45信道，可以获取终端IP地址信息，组织终端交互报文时需要
		// 但是优先级为从PS2开始
		if (model.equals("国网")) {
//			String[] gwType = { "PS2", "485-1", "RJ45-1", "INFRA" };
			// xuky 2019.02.26 不能使用129.1.22.96进行验证，会导致混乱
			String[] gwType = { "PS2", "485-1", "INFRA" };
			channels = gwType;
		}
		if (model.equals("南网")) {
//			String[] nwType = { "PS2", "485-1", "485-3", "RJ45-1", "INFRA" };
			// xuky 2019.02.26 不能使用129.1.22.96进行验证，会导致混乱
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
		ret[4] = ""; // 累加类型，所以需要提前清空
		for (String type : channels) {
			o = PlatFormUtil.getIPParam(meterno, type);
			IP = (String) o[0];
			port = (int) o[1];
			msg = "表位" + meterno + "-" + type + "-信道检测" + IP + ":" + port;
//			sData = getTermianlFrame(terminalIP, "读时钟"); // 注意必须使用terminalIP！！！
//			expect = "68************68940A04969605**************16";
			sData = getTermianlFrame(terminalIP, "读IP和MAC"); // 注意必须使用terminalIP！！！
//			680000C0A87F6068940E04969610C0A87F60023A190200019816
			expect = "68************68940A04969605**************16";
			expect = "68************68940E04969610**************0200019816";
			expect = "68************68940E04969610**********************16";
			result = commWithRecv.deal_one("【" + msg + "】", IP + ":" + port, sData, expect, 2500);
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



	// 国网或南网各种集中器通信通道检测
	// 返回结果：通信IP、通信port、终端通信地址、待检信道列表、信道可用标志
	public static Object[] allChanel(String meterno, String COM, String model) {
		String[] typesRJ45 = { "RJ45-0", "RJ45-1", "RJ45-2" }; // 可能的RJ45信道
		String[] channels = null; // 需要验证信道个数，其先后次序也是信道选用的优先级
		Object[] ret = { "", 0, "129.1.22.96", channels, "" }; // IP、port、terminalIP、信道列表、信道可用信息
		// 通过RJ45信道，可以获取终端IP地址信息，组织终端交互报文时需要
		// 但是优先级为从PS2开始
		if (model.equals("国网")) {
			String[] gwType = { "PS2", "485-1", "RJ45", "INFRA" };
			channels = gwType;
		}
		if (model.equals("南网")) {
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
		// 首先进行RJ45通道的判断，也是为了获取集中器可能的地址信息
		for (String rj45type : typesRJ45) {
			o = PlatFormUtil.getIPParam(meterno, rj45type);
			IP = (String) o[0];
			port = (int) o[1];
			msg = "表位" + meterno + "-" + rj45type + "-终端地址获取" + IP + ":" + port;
			sData = getTermianlFrame(IP, "读时钟");
			expect = "68************68940A04969605**************16";
			result = commWithRecv.deal_one("【" + msg + "】", IP + ":7000", sData, expect, 3000);
			if (result.getResult().equals("OK")) {
				terminalIP = IP;
				ret[2] = IP;
				RJ45Port = port;
				break;
			}
		}
		for (String type : channels) {
			if (type.equals("RJ45")) {
				// 根据前面的信息直接判断
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
				msg = "表位" + meterno + "-" + type + "-信道检测" + IP + ":" + port;
				sData = getTermianlFrame(terminalIP, "读时钟"); // 注意必须使用terminalIP！！！
				expect = "68************68940A04969605**************16";
				result = commWithRecv.deal_one("【" + msg + "】", IP + ":" + port, sData, expect, 3000);
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

	// 国网集中器全台体检测流程
	public void allPlatForm(String meter, String model) {
		// 测试项目 国网 单表位、单集中器台体相关功能测试
		// 升源、切换为特定模式（国网、南网）、单表位加电压、单表位加电压、信道检测及获取
		// 遥信端子，逐位检查（先闭合、后打开） 先控制台体、再读取集中器数据
		// 00000-11111 = 0 - 31 ，总共32种组合，逐个进行验证，对应的集中器报文应该是？？？
		String PlatFormCOM = PlatFormParam.getInstance().getPlatFormCOM();
		if (raisePower()) {
			if (chaneMode(model, PlatFormCOM))
				if (singelMeterVoltageOn(meter, PlatFormCOM)) {
					Debug.sleep(15000);
					// 返回结果：通信IP、通信port、终端通信地址、待检信道列表、信道可用标志
					Object[] param = allChanel(meter, PlatFormCOM, model);
					String IP_port = param[0] + ":" + param[1];
					String terminalIP = (String) param[2];
					if (model.equals("国网")) {
						// 遥信验证，国网为5路遥信端子
						for (int i = 0; i < 32; i++)
							if (setFSFlag(meter, PlatFormCOM, DataConvert.IntToBinString(i, 8)))
								checkTermail("04 96 96 03", DataConvert.int2HexString(i, 2), IP_port, terminalIP,"","");
					}
					if (model.equals("南网")) {
						// 遥信验证，南网为5路遥信端子
						String FSFlag = "";
						int FSNum = 5;
						for (int i = 0; i < 64; i++) {
							FSFlag = DataConvert.IntToBinString(i, 8);
							// xuky 2019.02.22 如果只有5路端子，则第5路相关的数据不进行验证
							if (FSNum == 5 && FSFlag.substring(3, 4).equals("1"))
								continue;
							if (setFSFlag(meter, PlatFormCOM, FSFlag)) {
								// 注意表台协议中的高位(6)对应集中器报文中的低位(1)
								FSFlag = PlatFormUtil.nwFSConvert(FSFlag);
								FSFlag = DataConvert.binStr2HexString(FSFlag, 2);
								checkTermail("04 96 96 03", FSFlag, param[0] + ":" + param[1], (String) param[2],"","");
							}
						}
						read12V(meter, PlatFormCOM);
						// 12V读取
						// 暂停-设置直流模拟量电压，读取集中器数据
						// 暂停-设置直流模拟量电流，读取集中器数据
					}
				}
		}
	}

	// 入参：当前的终端地址，通信参数、表位信息（根据表位信息，自动得到需要设置成为的终端地址）、
	// 返回：新的IP地址（同时也是终端地址）、MAC地址信息、操作结果对象
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
		String msg = "修改集中器IP及MAC 原：" + param;
//		String expect = getTermianlFrame(terminlIP, "94", dataitem, "");
		// xuky 2019.02.25 修改IP后，无法得到回复的报文
		String expect = "";
		result = commWithRecv.deal_one("【" + msg + "】", param, sData, expect, 1000);
		// xuky 2019.02.25 修改IP后，通过读取IP的方式，验证IP地址是否OK
//		param = NetIP+meterno + ":" + param.split(":")[1];
		sData = getTermianlFrame(newTermialAddr, "读IP和MAC"); // 注意必须使用terminalIP！！！
		expect = "68************68940E04969610"+data+"**16";
		msg = "验证集中器IP及MAC 现：" + param + " MAC：" + mac[1];
		// xuky 2019.02.26 需要增加延时，确保成功
		Debug.sleep(1000);
		param =  NetIP+meterno +":7000";
		result = commWithRecv.deal_one("【" + msg + "】", param, sData, expect, 2000);
		Object[] ret = { NetIP+meterno,mac[1],result };
		return ret;
	}

	public static void main(String[] args) {

	    SimuRun.setFSFlag("FF", "50", "11111111",0);
	    SimuRun.checkTermail("04 96 96 03", "1F", "129.1.22.12:7000", "129.1.22.12","","");
	    SimuRun.setFSFlag("FF", "50", "00000000",1);
	    SimuRun.checkTermail("04 96 96 03", "00", "129.1.22.12:7000", "129.1.22.12","","");

		// 1、升源
		// simuRun.raisePower();
		// 2、切换模式（2:南网配变/集中器；1:国网专变/集中器）
		// simuRun.chaneMode("32");
		// 3、进行单项测试用例的执行
		// simuRun.singleCase();

		// simuRun.single645Frame();
		// 4、单个表位加电
		// simuRun.singelMeterOn("01");
		// // 5、降源
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
		// 2019-02-21 17:19:42:002 [【表位18遥信输出00010000】测试结果 result=OK]
		// comm.CommWithRecv
		// 2019-02-21 17:19:42:002 [表位18遥信输出00010000] simulation.SimuRun
		// 2019-02-21 17:19:42:009 [129.1.22.205:10002 nowTimes:1 allTimes:1]
		// comm.CommWithRecv
		// 2019-02-21 17:19:42:009 [129.1.22.205:10002
		// SendData:68000081011660681404049696031316] comm.CommWithRecv
		// 2019-02-21 17:19:42:458 [129.1.22.205:10002
		// RecvComplete:6800008101166068940504969603009416]
		// comm.CommWithRecv$ServerHandlerByte
		// 2019-02-21 17:19:42:464 [【验证集中器数据项04 96 96 03 应为20】测试结果 result=NG]
		// comm.CommWithRecv
		// 2019-02-21 17:19:42:464 [【验证集中器数据项04 96 96 03 应为20】测试结果
		// expect=680000810116606894050496960320B416] comm.CommWithRecv
		// 2019-02-21 17:19:42:464 [【验证集中器数据项04 96 96 03 应为20】测试结果
		// recv=6800008101166068940504969603009416] comm.CommWithRecv

	}

}
