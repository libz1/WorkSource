package produce.control.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.eastsoft.util.Debug;

import entity.SerialParam;
import produce.control.comm.CommWithRecv;
import produce.control.comm.FramePlatform;
import produce.control.entity.BaseCommLog;
import produce.entity.ProduceCase;
import util.Frame645Control;
import util.SoftVarSingleton;
import util.Util698;

public class MeterRunInfo {
	private String Meterno;
	private String Endflag = "0";
	private String result = ""; // 测试结果

	private List<ProduceCase> produceCases = new ArrayList<ProduceCase>();
	private int currStep = -1; // -1表示尚未开始执行
	private int numOfALL = 0;
	private Boolean continue_run = true;
	private String beginTime = ""; // 开始时间
	private String endTime = ""; // 结束时间
	private int usingTime = 0; // 测试耗时
	private Map<String, SerialParam> SerialMap;
	private String TerminalID_MAC = "";

	public MeterRunInfo(String Meterno, List<ProduceCase> produceCases, Map<String, SerialParam> serialMap){
		Meterno = "00"+Meterno;
		Meterno = Meterno.substring(Meterno.length()-2,Meterno.length());
		this.Meterno = Meterno;
		this.produceCases = produceCases;
		this.SerialMap = serialMap;
		numOfALL = produceCases.size();
		endTime = "";
		dealTerminalIP();
	}

	public MeterRunInfo() {
		Endflag = "0";
	}

	private void dealTerminalIP(){
		// 升源 - 逐个表位进行电压回路的断开处理  - 对单一表位进行电压回路的接入处理
		// 根据需要进行集中器自检程序更新
		// 调整集中器的IP地址和MAC地址。MAC地址作为对集中器的唯一标示进行管理
		// 比较麻烦的事情，如果生产流程需要进行最终版集中器程序的更新，则无法一次性的完成，因为目前的dn程序只是支持唯一的IP操作
//		15、电压回路
//		主机:01H+地址(A—Z) +长度+AAH(命令) +相别(H-30H,A-31H,B-32H,C-33H)+(30H/断开  31H/接入)+校验位+结束(17H)
//		   从机:01H+地址(A—Z +长度)+ 06H(肯定)/15H(否定) +校验位+结束(17H)
		// 需要台体进行验证
		// 1、单一表位控制是否有效，H-30H,A-31H,B-32H,C-33H，先H，然后再A\B\C
		// 2、广播地址FFH是否有效
		// 3、单表位  加电是否有效  先H，然后再A\B\C

		// xuky 2019.02.13 获取唯一的MAC地址
		Object[] ret =  PlatFormUtil.getMAC("201902", 1, "01");
		TerminalID_MAC = (String)ret[1];

	}

	private void dealData(ProduceCase produceCase){

		String caseName = produceCase.getName();
		String caseID = Meterno + "-"+produceCase.getCaseno();

        Util698.log(MeterRunInfo.class.getName(), "ID："+caseID +" 测试开始"+caseName, Debug.LOG_INFO);
		// 可进行通信的信道 PS2\485-1（TCP-1）
		// 485-1	129.1.22.201:10001-10032
		// PS2		129.1.22.202:10001-10032

		// 红外信道	129.1.22.205:10001-10032（TCP-2）

		// RJ45  单纯IP检测用  什么时候进行更新程序？ 更新程序时进行测试即可！
		// 129.1.22.96 -> 129.1.22.1-129.1.22.32:7000
		// 485-2	COM01-COM32  校表用  校表时测试即可
        String protocol = produceCase.getProtocol();
    	// xuky 2019.02.14 台体控制协议
        if (protocol.equals("platform")){
    		String port = produceCase.getNote();
    		port = port.substring(port.indexOf("=")+1);
    		SerialParam serialParam = SerialMap.get(port);
    		String tempData = produceCase.getSend();
    		// 组织需要发送的数据
    		FramePlatform framePlatform = new FramePlatform();
    		framePlatform.setADDR(Meterno);
    		framePlatform.setCONTROL(tempData.substring(31,33));
    		String len = tempData.substring(28,30);
    		if (len.equals("06"))
    			framePlatform.setDATA("");
    		else if (len.equals("07"))
    			framePlatform.setDATA(tempData.substring(34,36));
    		else if (len.equals("08"))
    			framePlatform.setDATA(tempData.substring(34,39));
    		else if (len.equals("09"))
    			framePlatform.setDATA(tempData.substring(34,42));
    		String sData = framePlatform.getFrame();
    		// 组织需要接收的数据
    		tempData = produceCase.getExpect();
    		framePlatform.setCONTROL(tempData.substring(31,33));
    		len = tempData.substring(28,30);
    		if (len.equals("06"))
    			framePlatform.setDATA("");
    		else if (len.equals("07"))
    			framePlatform.setDATA(tempData.substring(34,36));
    		else if (len.equals("08"))
    			framePlatform.setDATA(tempData.substring(34,39));
    		else if (len.equals("09"))
    			framePlatform.setDATA(tempData.substring(34,42));
    		String expect = framePlatform.getFrame();
    		// 与实际设备进行通信
    		CommWithRecv commWithRecv = new CommWithRecv();
//    		String result = commWithRecv.deal_one("TCP", "192.168.127.120:100"+Meterno, sData, expect);
//    		String tmp = serialParam.getCOMM()+":"+serialParam.getBaudRate();
//    		tmp = commWithRecv.deal_one("COM", tmp, sData, expect);
        }
    	// xuky 2019.02.14 645协议
        if (protocol.equals("") || protocol.indexOf("645")>=0){
//        	String chanel = "192.168.127.120:100"+Meterno;
        	String chanel = "129.1.22.202:100"+Meterno;
        	// 地址域为IP地址信息，可能需要进行动态调整
        	// 暂时使用默认的地址信息
    		String tempData = produceCase.getSend();
    		Frame645Control frame645 = new Frame645Control(tempData);
    		frame645.setAddr(Util698.StrIP2HEX("129.1.22.96"));
    		String sData = frame645.get645Frame();
    		String expect = produceCase.getExpect();
    		CommWithRecv commWithRecv = new CommWithRecv();
    		BaseCommLog tmp = commWithRecv.deal_one("TCP", chanel, sData, expect);
        }

        // 缺少过程记录程序段
        // 测试开始执行时新增记录，汇总信息和明细信息   可以现有的测试记录
        //

		// 模拟进行红外通信等的交互耗时过程
//		int sleep = (int)(Math.random()*1000);
//		if (caseName.indexOf("红外") >= 0)
//			sleep = sleep * 3;
//		caseID += " sleep="+sleep;
//		Debug.sleep(sleep);
        Util698.log(MeterRunInfo.class.getName(), "ID："+caseID +" 测试结束"+caseName, Debug.LOG_INFO);
	};

	// 已经提前设定了台体的工作模式、南网、国网
	public void run() {
		beginTime = Util698.getDateTimeSSS_new();
		// 根据produceCases数据执行具体的测试过程
		while( continue_run ){
			currStep ++;
			// 判断是否执行到了最末的测试用例
			if (currStep == numOfALL){
	            Util698.log(MeterRunInfo.class.getName(), "表位："+ Meterno+"测试完成", Debug.LOG_INFO);
	    		endTime = Util698.getDateTimeSSS_new();
	    		usingTime = Util698.getMilliSecondBetween_new(endTime, beginTime).intValue();
				break;
			}
			ProduceCase produceCase = produceCases.get(currStep);
			String caseName = produceCase.getName();
//			String caseID = Meterno+"-"+produceCase.getCaseno();

			String protocol = produceCase.getProtocol();
            Util698.log(MeterRunInfo.class.getName(), "表位："+ Meterno  +" 测试进度："+(currStep+1)+"/"+numOfALL+" 测试内容:"+caseName, Debug.LOG_INFO);

            Lock test_Lock = null;
			if (caseName.indexOf("红外") >= 0)
				test_Lock = SoftVarSingleton.getInstance().getInfraTest_Lock();
			if (caseName.indexOf("路由") >= 0 || caseName.indexOf("RT") >= 0)
				test_Lock = SoftVarSingleton.getInstance().getRTTest_Lock();
			if (protocol.indexOf("platform") >= 0)
				test_Lock = SoftVarSingleton.getInstance().getPlatformTest_Lock();


			if (test_Lock != null){
				test_Lock.lock();
		        try {
		            dealData(produceCase);
		        } finally {
		        	test_Lock.unlock();
		        }
			}
			else{
	            dealData(produceCase);
			}

		}
		// 1、数据只能发送到发送队列中，什么时候得到结果是不确定的，得到的也可能是无效的结果，可能需要重新发送
		// 发送数据，接收数据，无响应数据，都需要有标示，区分来源

		// QT 课程5-23:59
		// 2、不停的监听，监听到信息后，判断其中的特征码
		// 根据特征码，推送到相应的处理程序中，进行下一步的数据处理过程
		// 分工协作，任务单一，监听任务只负责接收数据和基本的判断，收到数据后（添加判断标示）立即转存到缓冲区
		// IP:port 表位信息
		// COM:Rate 表位信息
		// 功能与外观显示代码分离  M V C  M(模型、数据模型)与V(视图不直接交互)，通过C实现  ，降低维护成本

//		3、CommWithRecv 的deal_one已经被设计发送到接收数据是闭环的，所以不存在接收后找不到源头的问题
		//问题在于需要进行 资源独占的控制，如果一个资源正在被deal_one使用，其他的进程就应该等待至释放
		// RT通信比较特殊，是否可以采用这样的控制方式？调整为单一的RT测试过程，间隔时间稍长一些的，进行多次数据抄读
//			发前延时在每次发送时都需要严格执行的 ok 2019年春节前调整完成
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getUsingTime() {
		return usingTime;
	}

	public void setUsingTime(int usingTime) {
		this.usingTime = usingTime;
	}

	public String getMeterno() {
		return Meterno;
	}

	public void setMeterno(String meterno) {
		Meterno = meterno;
	}

	public String getEndflag() {
		return Endflag;
	}
	public void setEndflag(String endflag) {
		Endflag = endflag;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}


}
