package javafx.base;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;
import com.google.gson.Gson;

import dao.basedao.IBaseDao;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.PLC2MES;
import produce.entity.PLC2MES2;
import produce.entity.PLC2MES3;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseResultDaoImpl;
import produce.entity.ProduceLog;
import produce.entity.ProduceLog2MES;
import produce.entity.ProduceLog2MESDaoImpl;
import produce.entity.ProduceLogDaoImpl;
import util.SoftParameter;
import util.Util698;

public class PLC2MESThread {

	private String workstationSN = "123456";
	String endpoint = "http://10.1.200.16:8087/MesFrameWork.asmx";
	String Namespace = "http://device.service.moresoft.com/";
	String funName = "UploadTestWORKData";

	private volatile static PLC2MESThread uniqueInstance;

	public static PLC2MESThread getInstance() {
		if (uniqueInstance == null) {
			synchronized (PLC2MESThread.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new PLC2MESThread();
				}
			}
		}
		return uniqueInstance;
	}

	private PLC2MESThread() {
		new Thread(() -> init()).start();
	}


	private void init() {
		//xuky 2018.10.25 使用用户输入的工作中心ID
		workstationSN = SoftParameter.getInstance().getWORKID();


        // 需要考虑多个客户端，连接一个数据库都进行接口数据传输的情况
        // 1、同一个数据库的所有客户端统一使用一个工作中心ID，在传输数据时确定
		// 2、如果工作中心ID信息为空，则表示当前客户端无需运行接口传输程序
		if (workstationSN.equals("") || workstationSN == null){
			Util698.log(PLC2MESThread.class.getName(), "workstationSN 信息为空，不启动接口传输线程", Debug.LOG_INFO);
			return;
		}
		Util698.log(PLC2MESThread.class.getName(), "启动接口传输线程...", Debug.LOG_INFO);

		// xuky 2018.06.26 传输时，如果存在未传输的，同一个addr的多条数据，可以一次性同时传输
		// 逐条读取未传输的数据（status="0"），在一个事务中进行数据操作，防止出现数据的变化
		// 读一条数据，根据其addr，读取相关的一条或多条数据(addr="XXX" and status="0")，根据这些数据组织为一个webservices的数据集，进行通信，根通信结果，设置status和transtime
		IBaseDao<DevInfo> iBaseDao_Devinfo = new DevInfoDaoImpl();
		IBaseDao<ProduceLog2MES> iBaseDao_ProduceLog2MES = new ProduceLog2MESDaoImpl();
		IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		String flag = "1",jsonStr = "";
		Boolean is_single = true; // 一次只是进行一项数据的传递
		while (true) {
			try {
				// xuky 2018.10.23 调整查询数据的判断条件，只要是不成功的就需要不断的发送  查询数据的优先级为 status=0表示从未传输的（优先级更高），status=2表示传输失败的（优先级次之） 然后再重新传输时间最新的

				// xuky 2018,11.08 添加了查询限制条件，一次只是提取30条数据，处理完数据后，重新提取数据
				int numLimit = 30;
				List<ProduceLog2MES> produceLog2MES_list = iBaseDao_ProduceLog2MES.retrieve(" where status!='1'"," order by status asc ,optime_b desc ",0,numLimit);
//				List<ProduceLog2MES> produceLog2MES_list = iBaseDao_ProduceLog2MES.retrieve(" where ID = 17819"," order by status asc ,optime_b desc");
				int size = 0;
				if (produceLog2MES_list != null)
					size = produceLog2MES_list.size();
//				Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceLog2MES.retrieveBySQL OK! size:"+size, Debug.LOG_INFO);
				flag = "2";
				for (Object o : produceLog2MES_list) {
                    // 一次只是执行一个addr的数据
					PLC2MES pLC2MES_single = new PLC2MES();
					if (o != null){
						ProduceLog2MES produceLog2MES = (ProduceLog2MES)o;
						String addr = produceLog2MES.getAddr();
						String status = produceLog2MES.getStatus();
						int produceLog2MESID = produceLog2MES.getID();
						Util698.log(PLC2MESThread.class.getName(), "produceLog2MES： addr=" +addr +" status="+status+" ID="+produceLog2MESID, Debug.LOG_INFO);

						// 1、第1层，根据地址信息查询devinfo
						List<DevInfo> devinfo_list = iBaseDao_Devinfo.retrieve("where addr='"+addr+"'","");
						size = 0;
						if (devinfo_list != null)
							size = devinfo_list.size();
						Util698.log(PLC2MESThread.class.getName(), "iBaseDao_Devinfo.retrieve OK! addr=" +addr +" size="+size, Debug.LOG_INFO);

						if (size == 0){
							// xuky 2018.11.08 如果iBaseDao_Devinfo没有数据，则不进行接口数据传递，因为在手动模式下，需要人为设定测试结果
							Util698.log(PLC2MESThread.class.getName(), "addr=" +addr +" status="+status +" size="+size + " iBaseDao_Devinfo无记录，跳过", Debug.LOG_INFO);
							{
								// xuky 2019.02.18 需要判断数据的时间 ，如果是很早以前的，可能是废数据，不要继续传输
								//
							}
							continue;
						}

						for (Object o1 : devinfo_list) {
							if (o1 != null){
								DevInfo devinfo = (DevInfo) o1;
								// 判断测试结果
								if (devinfo.getErrdatetime() == null)
									devinfo.setErrdatetime("");
								if (devinfo.getOkdatetime() == null)
									devinfo.setOkdatetime("");

								if (devinfo.getErrdatetime().compareTo(devinfo.getOkdatetime())>=0){
									pLC2MES_single.setErrcode(barCode2ErrCode(devinfo.getBarCode()));  //将测试用例名称转为不良代码
									pLC2MES_single.setOpdatetime(devinfo.getErrdatetime());
									pLC2MES_single.setOperator(devinfo.getErroperater());
									pLC2MES_single.setStatus("NG");
//									Util698.log(PLC2MESThread.class.getName(), "NG", Debug.LOG_INFO);
								}
								else{
									pLC2MES_single.setOpdatetime(devinfo.getOkdatetime());
									pLC2MES_single.setOperator(devinfo.getOkoperater());
									// xuky 2018.10.23 添加芯片ID信息
									pLC2MES_single.setSAVEID(devinfo.getBarCode());
									pLC2MES_single.setStatus("OK");
//									Util698.log(PLC2MESThread.class.getName(), "OK", Debug.LOG_INFO);
								}
								pLC2MES_single.setAddr(addr);

								// xuky 2019.01.15 根据类型具体调整
								if (devinfo.getType().indexOf("1") >=0)
									pLC2MES_single.setDevtype("II型采集器");
								else if (devinfo.getType().indexOf("3") >=0)
									pLC2MES_single.setDevtype("表模块");
								else if (devinfo.getType().indexOf("2") >=0)
									pLC2MES_single.setDevtype("集中器");
								else if (devinfo.getType().indexOf("4") >=0)
									pLC2MES_single.setDevtype("路由");
							}
						}
//						jsonStr = new Gson().toJson(pLC2MES);
//						Util698.log(PLC2MESThread.class.getName(), "jsonStr1:"+jsonStr, Debug.LOG_INFO);

						// 2、第2层，根据地址信息查询ProduceLog  多次测试结果放在一起
						String where = "where status!='1' and addr = '"+addr+"'";
						// 根据情况，进行单个数据项的数据传输，以便进行ID重复的判断
						if (is_single)
							where = "where ID ="+produceLog2MESID;
						List<ProduceLog2MES> producelog2MES_list = iBaseDao_ProduceLog2MES.retrieve(where ,"order by optime_b desc");
//						Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceLog2MES.retrieve OK!" , Debug.LOG_INFO);
//						Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceLog2MES.retrieve OK! size:"+ producelog2MES_list.size(), Debug.LOG_INFO);
						PLC2MES2[] pLC2MES2_array = new PLC2MES2[producelog2MES_list.size()];
						pLC2MES_single.setITEMS(pLC2MES2_array);
						int i = 0;
						for (Object o1 : producelog2MES_list) {
							if (o1 != null){
								ProduceLog2MES p = (ProduceLog2MES) o1;
								List<ProduceLog> producelog_list = iBaseDao_ProduceLog.retrieve("where id="+p.getProducelogID(),"");
//								Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceLog.retrieve OK! " , Debug.LOG_INFO);
//								Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceLog.retrieve OK! size:"+producelog_list.size() , Debug.LOG_INFO);
								for (Object o2 : producelog_list) {
									if (o2 != null){
										ProduceLog producelog = (ProduceLog) o2;
										pLC2MES2_array[i] = new PLC2MES2();
										pLC2MES2_array[i].setID(DataConvert.int2String(producelog.getID()));
										pLC2MES2_array[i].setOperator(producelog.getOpName());
										pLC2MES2_array[i].setOptime(producelog.getOpTime());
										pLC2MES2_array[i].setResult(producelog.getOpResult());
									}
								}
								// 3、第3层信息，根据logid，获取测试明细信息
								List<ProduceCaseResult> producecaseresult = iBaseDao_ProduceCaseResult.retrieve("where runid="+p.getProducelogID(),"");
//								Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceCaseResult.retrieve OK!" , Debug.LOG_INFO);
//								Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceCaseResult.retrieve OK! size:"+producecaseresult.size() , Debug.LOG_INFO);
								size = 0;
								if (producecaseresult != null)
									size = producecaseresult.size();
								PLC2MES3[] pLC2MES3_array = new PLC2MES3[size];
								if (pLC2MES2_array[i] == null)
									pLC2MES2_array[i] = new PLC2MES2();
								pLC2MES2_array[i].setITEMS(pLC2MES3_array);
								int j = 0;
								for (Object o2 : producecaseresult) {
									if (o2 != null){
										ProduceCaseResult produceCaseResult = (ProduceCaseResult) o2;
										pLC2MES3_array[j] = new PLC2MES3();
										pLC2MES3_array[j].setID(DataConvert.int2String(produceCaseResult.getID()));
										pLC2MES3_array[j].setExpect(produceCaseResult.getExpect());
										pLC2MES3_array[j].setName(produceCaseResult.getName());
										pLC2MES3_array[j].setPort(produceCaseResult.getPort());
										pLC2MES3_array[j].setRecv(produceCaseResult.getRecv());
										pLC2MES3_array[j].setRecvtime(produceCaseResult.getRecvtime());
										pLC2MES3_array[j].setResult(produceCaseResult.getResult());
										pLC2MES3_array[j].setSend(produceCaseResult.getSend());
										pLC2MES3_array[j].setSendtime(produceCaseResult.getSendtime());
										pLC2MES3_array[j].setSendtimes(DataConvert.int2String(produceCaseResult.getSendtimes()));
										j++;
									}
								}
								i++;
							}
						}

						// 组织为webServices格式进行数据传递
//						Util698.log(PLC2MESThread.class.getName(), "开始new Gson().toJson...", Debug.LOG_INFO);
						jsonStr = new Gson().toJson(pLC2MES_single);
						Util698.log(PLC2MESThread.class.getName(), "MES交互数据发出\r"+jsonStr, Debug.LOG_INFO);

						// 调用webservices接口进行数据传递
						String webReturn = "";
						String url = "http://10.1.200.16:8087/MesNewWebService.asmx?op=UploadTestWORKData";
						String pingAddr = "10.1.200.16";
						endpoint = "http://10.1.200.16:8087/MesNewWebService.asmx";
						Namespace = "http://tempuri.org/";
						funName = "UploadTestWORKData";

						webReturn = UploadTestWORKData(jsonStr);
						Util698.log(PLC2MESThread.class.getName(), "MES交互数据接收\r"+webReturn, Debug.LOG_INFO);


						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 直接访问接口地址，看是否畅通
							String url_data = Util698.getURLData(url);
							Util698.log(PLC2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 直接ping接口地址，看是否畅通
							Util698.isConnect(pingAddr);

							// xuky 2018.11.08 如果出现错误，则需要等待一段时间后，再次执行
							Debug.sleep(1000 * 60 * 5);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(PLC2MESThread.class.getName(), "MES交互数据接收\r"+webReturn, Debug.LOG_INFO);
						}
						// xuky 2018.11.08 重试2次
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 如果出现错误，则需要等待一段时间后，再次执行
							Debug.sleep(1000 * 60 * 3);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(PLC2MESThread.class.getName(), "MES交互数据接收\r"+webReturn, Debug.LOG_INFO);
						}
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 直接访问接口地址，看是否畅通
							String url_data = Util698.getURLData(url);
							Util698.log(PLC2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 直接ping接口地址，看是否畅通
							Util698.isConnect(pingAddr);
						}

						if (webReturn.length() > 200)
							webReturn = webReturn.substring(0,200);

						status = "1";
						if (!webReturn.equals("OK")){
							// 如果返回的信息是记录已经存在
							if (webReturn.indexOf("NG:II采测试数据结果：该ID") >= 0 && webReturn.indexOf("已存在记录") >= 0 )
								status = "1";
							else
								status = "2";
							// xuky 2019.02.13 检查接口表的数据结果，发现此类数据。进行处理
							if (webReturn.indexOf("NG:该ADDR已存在为OK的记录") >= 0 )
								status = "1";
							else
								status = "2";
						}

						//  修改producelog2MES表中的上传标记
						for (Object o1 : producelog2MES_list) {
							if (o1 != null){
								ProduceLog2MES p = (ProduceLog2MES) o1;
								p.setStatus(status);
								p.setWebinfo(webReturn);
								p.setTranstime(DateTimeFun.getDateTimeSSS());
								iBaseDao_ProduceLog2MES.update(p);
							}
						}

					}
					// xuky 2018.06.28 通过以下代码跳过其他数  因为这里是一次包含多个操作
					if (!is_single){
						// xuky 2018.10.28 重新查询新的数据
						break;  // break 是跳出for循环
					}
				}

				Debug.sleep(1000);
			}
			catch (Exception e){
				Util698.log(PLC2MESThread.class.getName(), "init Exception:"+e.getMessage(), Debug.LOG_INFO);
				// 出现错误则延长等待时间，因为可能是始终无法联通的状态 20分钟后再试
				// xuky 2018.10.29 调整为5分钟后重新尝试
				Debug.sleep(1000 * 60 * 1);
				}
			}

	}
	// MES给定接口链接http://10.1.200.16:8088/MesFrameWork.asmx?op=UploadTestWORKData
	// SOAPAction: "http://device.service.moresoft.com/UploadTestWORKData"
	public String UploadTestWORKData(String data) {
		try {
			// 创建一个服务(service)调用(call)
			Call call = (Call) new Service().createCall();
			// 设置service所在URL
			call.setTargetEndpointAddress(new java.net.URL(endpoint));
			// 设置函数名称
			call.setOperationName(new QName(Namespace, funName));
			// 添加参数
			call.addParameter(new QName(Namespace, "msg"),
					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName(Namespace, "workstationSN"),
					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
			call.setUseSOAPAction(true);
			// 返回参数的类型
			call.setReturnType(org.apache.axis.encoding.XMLType.SOAP_STRING);
			// 与前面的setOperationName有些重复，但是是必须的！
			call.setSOAPActionURI(Namespace+funName);
			// 入参信息
//			String msg = "{\"addr\":\"01\",\"status\":\"210\",\"ITEMS\":[{\"ID\":\"1\",\"name\":\"1\",\"result\":\"10-23s-00\",\"subid\":\"123232\",\"caseno\":\"1\"}]}";
			// 多个参数打包为一个数组
			String ret = (String) call.invoke(new Object[] { data, workstationSN });
			return ret;

		} catch (Exception e) {
			Util698.log(PLC2MESThread.class.getName(), "UploadTestWORKData Exception:"+e.getMessage(), Debug.LOG_INFO);
			return "UploadTestWORKData Exception:"+e.getMessage();
		}
	}

	private String barCode2ErrCode(String code){
		String ret = "GN04";  // GN04 不上电
		if (code == null) return ret;

		if (code.indexOf("地址") >= 0 ){
			if (code.indexOf("设置") >= 0 || code.indexOf("写") >= 0 )
				ret = "GN04";  // GN02 红外不通   与宋工讨论后，认为不上电更有可能
			if (code.indexOf("读") >= 0 )
				ret = "CS03";  // CS03 地址错误
		}
		if (code.indexOf("程序") >= 0 )
			ret = "CS01";  // CS01 软件错误
		if (code.indexOf("省份") >= 0 || code.indexOf("模式") >= 0 )
			ret = "CS02";  // CS02 模式错误
		if (code.indexOf("485") >= 0 )
			ret = "GN03";  // GN03 485不通
		if (code.indexOf("载波") >= 0 )
			ret = "GN01";  // GN01 载波不通  放在最后面的优先级最高
		return ret;
	}

	public static void main(String[] args) {
		PLC2MESThread.getInstance();
	}

}
