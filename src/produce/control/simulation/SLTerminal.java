package produce.control.simulation;

import java.util.concurrent.ConcurrentHashMap;

import produce.control.comm.CommParam;
import produce.control.comm.RJ45Param;
import util.Frame645Control;
import util.Util698;

//模拟集中器
public class SLTerminal {
	private volatile static SLTerminal uniqueInstance;
	// 表位与遥信状态对照表
	private ConcurrentHashMap<String, String> meterFS = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> meterDC = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> meterAC = new ConcurrentHashMap<>();
	public static SLTerminal getInstance() {
		if (uniqueInstance == null) {
			synchronized (SLTerminal.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new SLTerminal();
				}
			}
		}
		return uniqueInstance;
	}


	public ConcurrentHashMap<String, String> getMeterFS() {
		return meterFS;
	}
	public ConcurrentHashMap<String, String> getMeterDC() {
		return meterDC;
	}
	public ConcurrentHashMap<String, String> getMeterAC() {
		return meterAC;
	}


	// 监听串口，进行数据回复
	private SLTerminal() {
		CommParam commParam = new CommParam();
		// 开启32个
		for( int i=1;i<=32;i++){
			RJ45Param rJ45Param = new RJ45Param("192.168.127.120", 10000+i);
			commParam.setType("2");
			commParam.setRJ45Param(rJ45Param);
			CommServer tcpServer = new CommServer(commParam);
		}
	}

	// 根据收到的报文，得到需要回复的报文
	public String getReply(String recv, String addr_str){
		// 通过addr_str判断表位信息，进行相关操作
		String ret ="",data = "";
		Frame645Control frame645 = new Frame645Control(recv);
		if (frame645.getControl().equals("14")){
			// 调整控制字为94
			frame645.setControl("94");
			String dataitem = frame645.getData_item();
			if (dataitem.equals("04969603")){
				// 需要提前对模拟设备进行数据回复设置
				data = meterFS.get(addr_str);
				frame645.setData_data(data);
			}
			else if (dataitem.equals("04969605")){
				String time = Util698.getDateTimeSSS_new();
				time = time.substring(0,time.length()-3);
				time = time.substring(2);
				time = time.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				frame645.setData_data(time);
			}
			else if (dataitem.equals("04969607")){
				data = meterDC.get(addr_str);
				frame645.setData_data(data);
			}
			else if (dataitem.equals("04969608")){
				data = meterAC.get(addr_str);
				frame645.setData_data(data);
			}
			else if (dataitem.equals("0496960A") || dataitem.equals("0496960B") || dataitem.equals("0496960C")||dataitem.equals("0496960D") ){
				frame645.setData_data("00");
			}
			else{
				// 默认回复确认报文
				frame645.setData_data("00");
			}
		}
		ret = frame645.get645Frame();
		return ret;
	}

	public static void main(String[] args) {
	}

}
