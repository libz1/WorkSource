package produce.meter;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;
import com.google.gson.Gson;

import dao.basedao.IBaseDao;
import produce.meter.entity.MeterInfo;
import produce.meter.entity.MeterInfoDaoImpl;
import produce.meter.entity.MeterInfoID;
import produce.meter.entity.MeterInfoIDDaoImpl;
import produce.meter.entity.MeterLog2MES;
import produce.meter.entity.MeterLog2MESDaoImpl;
import produce.meter.entity.MeterResult;
import produce.meter.entity.MeterResultDaoImpl;
import produce.meter.entity.NewDev;
import produce.meter.entity.NewDev2;
import produce.meter.entity.NewDev3;
import util.SoftParameter;
import util.Util698;

public class Meter2MESThread {

	private String workstationSN = "123456";
	private volatile static Meter2MESThread uniqueInstance;

	public static Meter2MESThread getInstance() {
		if (uniqueInstance == null) {
			synchronized (Meter2MESThread.class) {
				if (uniqueInstance == null) {
					// 双重检查加锁
					uniqueInstance = new Meter2MESThread();
				}
			}
		}
		return uniqueInstance;
	}

	private Meter2MESThread() {
		new Thread(() -> init()).start();
	}


	private void init() {
		//xuky 2018.10.25 使用用户输入的工作中心ID
		workstationSN = SoftParameter.getInstance().getWORKID();


        // 需要考虑多个客户端，连接一个数据库都进行接口数据传输的情况
        // 1、同一个数据库的所有客户端统一使用一个工作中心ID，在传输数据时确定
		// 2、如果工作中心ID信息为空，则表示当前客户端无需运行接口传输程序
		if (workstationSN.equals("") || workstationSN == null){
			Util698.log(Meter2MESThread.class.getName(), "workstationSN 信息为空，不启动接口传输线程", Debug.LOG_INFO);
			return;
		}

		Util698.log(Meter2MESThread.class.getName(), "启动(表模块)接口传输线程...", Debug.LOG_INFO);

		IBaseDao<MeterInfo> iBaseDao_MeterInfo = new MeterInfoDaoImpl();
		IBaseDao<MeterLog2MES> iBaseDao_MeterLog2MES = new MeterLog2MESDaoImpl();
		IBaseDao<MeterResult> iBaseDao_MeterResult = new MeterResultDaoImpl();
		IBaseDao<MeterInfoID> iBaseDao_MeterInfoID = new MeterInfoIDDaoImpl();
//		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		String flag = "1",jsonStr = "";
		while (true) {
			try {
				// xuky 2018,11.08 添加了查询限制条件，一次只是提取10条数据，处理完数据后，重新提取数据
				List<MeterLog2MES> meterLog2MES_list = iBaseDao_MeterLog2MES.retrieve(" where status!='1'"," order by status asc ,optime_b desc ",0,10);
				int size = 0;
				if (meterLog2MES_list != null)
					size = meterLog2MES_list.size();
//				Util698.log(Meter2MESThread.class.getName(), "iBaseDao_Meter2MES.retrieveBySQL OK! size:"+size, Debug.LOG_INFO);
				flag = "2";
				for (Object o : meterLog2MES_list) {
                    // 一次只是执行一个addr的数据
					NewDev newDev_single = new NewDev();
					if (o != null){
						MeterLog2MES meterLog2MES = (MeterLog2MES)o;
						String addr = meterLog2MES.getAddr();
						String status = meterLog2MES.getStatus();
						int meterLog2MESID = meterLog2MES.getID();
						int resultID =  meterLog2MES.getResultID();
						Util698.log(Meter2MESThread.class.getName(), "meterLog2MES： addr=" +addr +" status="+status+" ID="+meterLog2MESID, Debug.LOG_INFO);

						// 1、第1层，根据地址信息查询meterinfo
						List<MeterInfo> meterinfo_list = iBaseDao_MeterInfo.retrieve("where QRCode='"+addr+"'","");
						size = 0;
						if (meterinfo_list != null)
							size = meterinfo_list.size();
						Util698.log(Meter2MESThread.class.getName(), "iBaseDao_MeterInfo.retrieve OK! addr=" +addr +" size="+size, Debug.LOG_INFO);

						if (size == 0){
							// xuky 2018.11.08 如果iBaseDao_Devinfo没有数据，则不进行接口数据传递，因为在手动模式下，需要人为设定测试结果
							Util698.log(Meter2MESThread.class.getName(), "addr=" +addr +" status="+status +" size="+size + " iBaseDao_Devinfo无记录，跳过", Debug.LOG_INFO);
							continue;
						}

						for (Object o1 : meterinfo_list) {
							if (o1 != null){
								MeterInfo meterinfo = (MeterInfo) o1;
								// 判断测试结果
								newDev_single.setErrcode(meterinfo.getErrCode());
								newDev_single.setOpdatetime(meterinfo.getOpdatetime());
								newDev_single.setOperator(meterinfo.getOperater());
								newDev_single.setStatus(meterinfo.getStatus());
								newDev_single.setQRCode(meterinfo.getQRCode());
								newDev_single.setDevtype(meterinfo.getType());

								// 添加获取的ID信息
								int infoid = meterinfo.getID();
								List<MeterInfoID> meterinfoid_list = iBaseDao_MeterInfoID.retrieve("where infoid="+infoid+" and optime = (select max(optime) from "+MeterInfoID.class.getName()+" where infoid="+infoid+")","");
								NewDev3[] dev3_array = new NewDev3[meterinfoid_list.size()];
								int i = 0;
								for (MeterInfoID o2 : meterinfoid_list) {
									dev3_array[i] = new NewDev3();
									dev3_array[i].setType(o2.getType());
									dev3_array[i].setData(o2.getData());
									i++;
								}
								newDev_single.setIDDATA(dev3_array);
							}
						}

						// 1、第2层，组织测试过程数据
						List<MeterResult> meterresult_list = iBaseDao_MeterResult.retrieve("where id="+resultID,"");
						size = 0;
						if (meterresult_list != null)
							size = meterresult_list.size();
						Util698.log(Meter2MESThread.class.getName(), "iBaseDao_MeterResult.retrieve OK! id=" +resultID +" size="+size, Debug.LOG_INFO);

						if (size == 0){
							// xuky 2018.11.08 如果iBaseDao_Devinfo没有数据，则不进行接口数据传递，因为在手动模式下，需要人为设定测试结果
							Util698.log(Meter2MESThread.class.getName(), "id=" +resultID +" status="+status +" size="+size + " iBaseDao_MeterResult无记录，跳过", Debug.LOG_INFO);
							continue;
						}

						NewDev2[] dev2_array = new NewDev2[meterresult_list.size()];
						int i = 0;
						for (MeterResult o1 : meterresult_list) {
							dev2_array[i] = new NewDev2();
							dev2_array[i].setID(o1.getID());
							dev2_array[i].setOperator(o1.getOperator());
							dev2_array[i].setOptime(o1.getOptime());
							dev2_array[i].setResult(o1.getResult());
							dev2_array[i].setResult(o1.getResult());
							int resultid = o1.getID();
							List<MeterInfoID> meterinfoid_list = iBaseDao_MeterInfoID.retrieve("where resultid="+resultid,"");
							NewDev3[] dev3_array = new NewDev3[meterinfoid_list.size()];
							int j = 0;
							for (MeterInfoID o2 : meterinfoid_list) {
								dev3_array[j] = new NewDev3();
								dev3_array[j].setType(o2.getType());
								dev3_array[j].setData(o2.getData());
								j++;
							}
							dev2_array[i].setIDDATA(dev3_array);
							i++;
						}
						newDev_single.setITEMS(dev2_array);


						// 组织为webServices格式进行数据传递
//						Util698.log(PLC2MESThread.class.getName(), "开始new Gson().toJson...", Debug.LOG_INFO);
						jsonStr = new Gson().toJson(newDev_single);
						Util698.log(Meter2MESThread.class.getName(), "MES交互数据发出\r"+jsonStr, Debug.LOG_INFO);

						// 调用webservices接口进行数据传递
						String webReturn = "";
						webReturn = UploadTestWORKData(jsonStr);
						Util698.log(Meter2MESThread.class.getName(), "MES交互数据接收\r"+webReturn, Debug.LOG_INFO);

						String url = "http://10.1.200.16:8088/MesFrameWork.asmx?op=Save_Route_Tool_Test";
						String pingAddr = "10.1.200.16";
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 直接访问接口地址，看是否畅通
							String url_data = Util698.getURLData(url);
							Util698.log(Meter2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 直接ping接口地址，看是否畅通
							Util698.isConnect(pingAddr);

							// xuky 2018.11.08 如果出现错误，则需要等待一段时间后，再次执行
							Debug.sleep(1000 * 60 * 5);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(Meter2MESThread.class.getName(), "MES交互数据接收\r"+webReturn, Debug.LOG_INFO);
						}
						// xuky 2018.11.08 重试2次
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 如果出现错误，则需要等待一段时间后，再次执行
							Debug.sleep(1000 * 60 * 3);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(Meter2MESThread.class.getName(), "MES交互数据接收\r"+webReturn, Debug.LOG_INFO);
						}
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 直接访问接口地址，看是否畅通
							String url_data = Util698.getURLData(url);
							Util698.log(Meter2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 直接ping接口地址，看是否畅通
							Util698.isConnect(pingAddr);
						}

						if (webReturn.length() > 200)
							webReturn = webReturn.substring(0,200);

						status = "1";
						if (webReturn.indexOf("OK")<0){
							// 对于
							if (webReturn.indexOf("存在") >= 0 || webReturn.indexOf("重复") >= 0 )
								status = "1";
							else
								status = "2";
						}

//						//  修改producelog2MES表中的上传标记
						meterLog2MES.setStatus(status);
						meterLog2MES.setWebinfo(webReturn);
						meterLog2MES.setTranstime(DateTimeFun.getDateTimeSSS());
						iBaseDao_MeterLog2MES.update(meterLog2MES);
//							}
//						}

					}

				}

				Debug.sleep(1000);
			}
			catch (Exception e){
				Util698.log(Meter2MESThread.class.getName(), "init Exception:"+e.getMessage(), Debug.LOG_INFO);
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
			String endpoint = "http://10.1.200.16:8088/MesFrameWork.asmx";
			String Namespace = "http://device.service.moresoft.com/";
			String funName = "Save_Route_Tool_Test";
			// 创建一个服务(service)调用(call)
			Call call = (Call) new Service().createCall();
			// 设置service所在URL
			call.setTargetEndpointAddress(new java.net.URL(endpoint));
			// 设置函数名称
			call.setOperationName(new QName(Namespace, funName));
			// 添加参数
			call.addParameter(new QName(Namespace, "strjson"),
					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
//			call.addParameter(new QName(Namespace, "workstationSN"),
//					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
			call.setUseSOAPAction(true);
			// 返回参数的类型
			call.setReturnType(org.apache.axis.encoding.XMLType.SOAP_STRING);
			// 与前面的setOperationName有些重复，但是是必须的！
			call.setSOAPActionURI(Namespace+funName);
			// 入参信息
//			String msg = "{\"addr\":\"01\",\"status\":\"210\",\"ITEMS\":[{\"ID\":\"1\",\"name\":\"1\",\"result\":\"10-23s-00\",\"subid\":\"123232\",\"caseno\":\"1\"}]}";
			// 多个参数打包为一个数组
			String ret = (String) call.invoke(new Object[] { data });
			return ret;

		} catch (Exception e) {
			Util698.log(Meter2MESThread.class.getName(), "UploadTestWORKData Exception:"+e.getMessage(), Debug.LOG_INFO);
			return "UploadTestWORKData Exception:"+e.getMessage();
		}
	}

	public static void main(String[] args) {
		Meter2MESThread.getInstance();
	}

}
