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
					// ˫�ؼ�����
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
		//xuky 2018.10.25 ʹ���û�����Ĺ�������ID
		workstationSN = SoftParameter.getInstance().getWORKID();


        // ��Ҫ���Ƕ���ͻ��ˣ�����һ�����ݿⶼ���нӿ����ݴ�������
        // 1��ͬһ�����ݿ�����пͻ���ͳһʹ��һ����������ID���ڴ�������ʱȷ��
		// 2�������������ID��ϢΪ�գ����ʾ��ǰ�ͻ����������нӿڴ������
		if (workstationSN.equals("") || workstationSN == null){
			Util698.log(Meter2MESThread.class.getName(), "workstationSN ��ϢΪ�գ��������ӿڴ����߳�", Debug.LOG_INFO);
			return;
		}

		Util698.log(Meter2MESThread.class.getName(), "����(��ģ��)�ӿڴ����߳�...", Debug.LOG_INFO);

		IBaseDao<MeterInfo> iBaseDao_MeterInfo = new MeterInfoDaoImpl();
		IBaseDao<MeterLog2MES> iBaseDao_MeterLog2MES = new MeterLog2MESDaoImpl();
		IBaseDao<MeterResult> iBaseDao_MeterResult = new MeterResultDaoImpl();
		IBaseDao<MeterInfoID> iBaseDao_MeterInfoID = new MeterInfoIDDaoImpl();
//		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		String flag = "1",jsonStr = "";
		while (true) {
			try {
				// xuky 2018,11.08 ����˲�ѯ����������һ��ֻ����ȡ10�����ݣ����������ݺ�������ȡ����
				List<MeterLog2MES> meterLog2MES_list = iBaseDao_MeterLog2MES.retrieve(" where status!='1'"," order by status asc ,optime_b desc ",0,10);
				int size = 0;
				if (meterLog2MES_list != null)
					size = meterLog2MES_list.size();
//				Util698.log(Meter2MESThread.class.getName(), "iBaseDao_Meter2MES.retrieveBySQL OK! size:"+size, Debug.LOG_INFO);
				flag = "2";
				for (Object o : meterLog2MES_list) {
                    // һ��ֻ��ִ��һ��addr������
					NewDev newDev_single = new NewDev();
					if (o != null){
						MeterLog2MES meterLog2MES = (MeterLog2MES)o;
						String addr = meterLog2MES.getAddr();
						String status = meterLog2MES.getStatus();
						int meterLog2MESID = meterLog2MES.getID();
						int resultID =  meterLog2MES.getResultID();
						Util698.log(Meter2MESThread.class.getName(), "meterLog2MES�� addr=" +addr +" status="+status+" ID="+meterLog2MESID, Debug.LOG_INFO);

						// 1����1�㣬���ݵ�ַ��Ϣ��ѯmeterinfo
						List<MeterInfo> meterinfo_list = iBaseDao_MeterInfo.retrieve("where QRCode='"+addr+"'","");
						size = 0;
						if (meterinfo_list != null)
							size = meterinfo_list.size();
						Util698.log(Meter2MESThread.class.getName(), "iBaseDao_MeterInfo.retrieve OK! addr=" +addr +" size="+size, Debug.LOG_INFO);

						if (size == 0){
							// xuky 2018.11.08 ���iBaseDao_Devinfoû�����ݣ��򲻽��нӿ����ݴ��ݣ���Ϊ���ֶ�ģʽ�£���Ҫ��Ϊ�趨���Խ��
							Util698.log(Meter2MESThread.class.getName(), "addr=" +addr +" status="+status +" size="+size + " iBaseDao_Devinfo�޼�¼������", Debug.LOG_INFO);
							continue;
						}

						for (Object o1 : meterinfo_list) {
							if (o1 != null){
								MeterInfo meterinfo = (MeterInfo) o1;
								// �жϲ��Խ��
								newDev_single.setErrcode(meterinfo.getErrCode());
								newDev_single.setOpdatetime(meterinfo.getOpdatetime());
								newDev_single.setOperator(meterinfo.getOperater());
								newDev_single.setStatus(meterinfo.getStatus());
								newDev_single.setQRCode(meterinfo.getQRCode());
								newDev_single.setDevtype(meterinfo.getType());

								// ��ӻ�ȡ��ID��Ϣ
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

						// 1����2�㣬��֯���Թ�������
						List<MeterResult> meterresult_list = iBaseDao_MeterResult.retrieve("where id="+resultID,"");
						size = 0;
						if (meterresult_list != null)
							size = meterresult_list.size();
						Util698.log(Meter2MESThread.class.getName(), "iBaseDao_MeterResult.retrieve OK! id=" +resultID +" size="+size, Debug.LOG_INFO);

						if (size == 0){
							// xuky 2018.11.08 ���iBaseDao_Devinfoû�����ݣ��򲻽��нӿ����ݴ��ݣ���Ϊ���ֶ�ģʽ�£���Ҫ��Ϊ�趨���Խ��
							Util698.log(Meter2MESThread.class.getName(), "id=" +resultID +" status="+status +" size="+size + " iBaseDao_MeterResult�޼�¼������", Debug.LOG_INFO);
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


						// ��֯ΪwebServices��ʽ�������ݴ���
//						Util698.log(PLC2MESThread.class.getName(), "��ʼnew Gson().toJson...", Debug.LOG_INFO);
						jsonStr = new Gson().toJson(newDev_single);
						Util698.log(Meter2MESThread.class.getName(), "MES�������ݷ���\r"+jsonStr, Debug.LOG_INFO);

						// ����webservices�ӿڽ������ݴ���
						String webReturn = "";
						webReturn = UploadTestWORKData(jsonStr);
						Util698.log(Meter2MESThread.class.getName(), "MES�������ݽ���\r"+webReturn, Debug.LOG_INFO);

						String url = "http://10.1.200.16:8088/MesFrameWork.asmx?op=Save_Route_Tool_Test";
						String pingAddr = "10.1.200.16";
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 ֱ�ӷ��ʽӿڵ�ַ�����Ƿ�ͨ
							String url_data = Util698.getURLData(url);
							Util698.log(Meter2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 ֱ��ping�ӿڵ�ַ�����Ƿ�ͨ
							Util698.isConnect(pingAddr);

							// xuky 2018.11.08 ������ִ�������Ҫ�ȴ�һ��ʱ����ٴ�ִ��
							Debug.sleep(1000 * 60 * 5);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(Meter2MESThread.class.getName(), "MES�������ݽ���\r"+webReturn, Debug.LOG_INFO);
						}
						// xuky 2018.11.08 ����2��
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 ������ִ�������Ҫ�ȴ�һ��ʱ����ٴ�ִ��
							Debug.sleep(1000 * 60 * 3);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(Meter2MESThread.class.getName(), "MES�������ݽ���\r"+webReturn, Debug.LOG_INFO);
						}
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 ֱ�ӷ��ʽӿڵ�ַ�����Ƿ�ͨ
							String url_data = Util698.getURLData(url);
							Util698.log(Meter2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 ֱ��ping�ӿڵ�ַ�����Ƿ�ͨ
							Util698.isConnect(pingAddr);
						}

						if (webReturn.length() > 200)
							webReturn = webReturn.substring(0,200);

						status = "1";
						if (webReturn.indexOf("OK")<0){
							// ����
							if (webReturn.indexOf("����") >= 0 || webReturn.indexOf("�ظ�") >= 0 )
								status = "1";
							else
								status = "2";
						}

//						//  �޸�producelog2MES���е��ϴ����
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
				// ���ִ������ӳ��ȴ�ʱ�䣬��Ϊ������ʼ���޷���ͨ��״̬ 20���Ӻ�����
				// xuky 2018.10.29 ����Ϊ5���Ӻ����³���
				Debug.sleep(1000 * 60 * 1);
				}
			}

	}
	// MES�����ӿ�����http://10.1.200.16:8088/MesFrameWork.asmx?op=UploadTestWORKData
	// SOAPAction: "http://device.service.moresoft.com/UploadTestWORKData"
	public String UploadTestWORKData(String data) {
		try {
			String endpoint = "http://10.1.200.16:8088/MesFrameWork.asmx";
			String Namespace = "http://device.service.moresoft.com/";
			String funName = "Save_Route_Tool_Test";
			// ����һ������(service)����(call)
			Call call = (Call) new Service().createCall();
			// ����service����URL
			call.setTargetEndpointAddress(new java.net.URL(endpoint));
			// ���ú�������
			call.setOperationName(new QName(Namespace, funName));
			// ��Ӳ���
			call.addParameter(new QName(Namespace, "strjson"),
					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
//			call.addParameter(new QName(Namespace, "workstationSN"),
//					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
			call.setUseSOAPAction(true);
			// ���ز���������
			call.setReturnType(org.apache.axis.encoding.XMLType.SOAP_STRING);
			// ��ǰ���setOperationName��Щ�ظ��������Ǳ���ģ�
			call.setSOAPActionURI(Namespace+funName);
			// �����Ϣ
//			String msg = "{\"addr\":\"01\",\"status\":\"210\",\"ITEMS\":[{\"ID\":\"1\",\"name\":\"1\",\"result\":\"10-23s-00\",\"subid\":\"123232\",\"caseno\":\"1\"}]}";
			// ����������Ϊһ������
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
