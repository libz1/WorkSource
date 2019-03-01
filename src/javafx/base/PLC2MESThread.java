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
					// ˫�ؼ�����
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
		//xuky 2018.10.25 ʹ���û�����Ĺ�������ID
		workstationSN = SoftParameter.getInstance().getWORKID();


        // ��Ҫ���Ƕ���ͻ��ˣ�����һ�����ݿⶼ���нӿ����ݴ�������
        // 1��ͬһ�����ݿ�����пͻ���ͳһʹ��һ����������ID���ڴ�������ʱȷ��
		// 2�������������ID��ϢΪ�գ����ʾ��ǰ�ͻ����������нӿڴ������
		if (workstationSN.equals("") || workstationSN == null){
			Util698.log(PLC2MESThread.class.getName(), "workstationSN ��ϢΪ�գ��������ӿڴ����߳�", Debug.LOG_INFO);
			return;
		}
		Util698.log(PLC2MESThread.class.getName(), "�����ӿڴ����߳�...", Debug.LOG_INFO);

		// xuky 2018.06.26 ����ʱ���������δ����ģ�ͬһ��addr�Ķ������ݣ�����һ����ͬʱ����
		// ������ȡδ��������ݣ�status="0"������һ�������н������ݲ�������ֹ�������ݵı仯
		// ��һ�����ݣ�������addr����ȡ��ص�һ�����������(addr="XXX" and status="0")��������Щ������֯Ϊһ��webservices�����ݼ�������ͨ�ţ���ͨ�Ž��������status��transtime
		IBaseDao<DevInfo> iBaseDao_Devinfo = new DevInfoDaoImpl();
		IBaseDao<ProduceLog2MES> iBaseDao_ProduceLog2MES = new ProduceLog2MESDaoImpl();
		IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
		IBaseDao<ProduceLog> iBaseDao_ProduceLog = new ProduceLogDaoImpl();
		String flag = "1",jsonStr = "";
		Boolean is_single = true; // һ��ֻ�ǽ���һ�����ݵĴ���
		while (true) {
			try {
				// xuky 2018.10.23 ������ѯ���ݵ��ж�������ֻҪ�ǲ��ɹ��ľ���Ҫ���ϵķ���  ��ѯ���ݵ����ȼ�Ϊ status=0��ʾ��δ����ģ����ȼ����ߣ���status=2��ʾ����ʧ�ܵģ����ȼ���֮�� Ȼ�������´���ʱ�����µ�

				// xuky 2018,11.08 ����˲�ѯ����������һ��ֻ����ȡ30�����ݣ����������ݺ�������ȡ����
				int numLimit = 30;
				List<ProduceLog2MES> produceLog2MES_list = iBaseDao_ProduceLog2MES.retrieve(" where status!='1'"," order by status asc ,optime_b desc ",0,numLimit);
//				List<ProduceLog2MES> produceLog2MES_list = iBaseDao_ProduceLog2MES.retrieve(" where ID = 17819"," order by status asc ,optime_b desc");
				int size = 0;
				if (produceLog2MES_list != null)
					size = produceLog2MES_list.size();
//				Util698.log(PLC2MESThread.class.getName(), "iBaseDao_ProduceLog2MES.retrieveBySQL OK! size:"+size, Debug.LOG_INFO);
				flag = "2";
				for (Object o : produceLog2MES_list) {
                    // һ��ֻ��ִ��һ��addr������
					PLC2MES pLC2MES_single = new PLC2MES();
					if (o != null){
						ProduceLog2MES produceLog2MES = (ProduceLog2MES)o;
						String addr = produceLog2MES.getAddr();
						String status = produceLog2MES.getStatus();
						int produceLog2MESID = produceLog2MES.getID();
						Util698.log(PLC2MESThread.class.getName(), "produceLog2MES�� addr=" +addr +" status="+status+" ID="+produceLog2MESID, Debug.LOG_INFO);

						// 1����1�㣬���ݵ�ַ��Ϣ��ѯdevinfo
						List<DevInfo> devinfo_list = iBaseDao_Devinfo.retrieve("where addr='"+addr+"'","");
						size = 0;
						if (devinfo_list != null)
							size = devinfo_list.size();
						Util698.log(PLC2MESThread.class.getName(), "iBaseDao_Devinfo.retrieve OK! addr=" +addr +" size="+size, Debug.LOG_INFO);

						if (size == 0){
							// xuky 2018.11.08 ���iBaseDao_Devinfoû�����ݣ��򲻽��нӿ����ݴ��ݣ���Ϊ���ֶ�ģʽ�£���Ҫ��Ϊ�趨���Խ��
							Util698.log(PLC2MESThread.class.getName(), "addr=" +addr +" status="+status +" size="+size + " iBaseDao_Devinfo�޼�¼������", Debug.LOG_INFO);
							{
								// xuky 2019.02.18 ��Ҫ�ж����ݵ�ʱ�� ������Ǻ�����ǰ�ģ������Ƿ����ݣ���Ҫ��������
								//
							}
							continue;
						}

						for (Object o1 : devinfo_list) {
							if (o1 != null){
								DevInfo devinfo = (DevInfo) o1;
								// �жϲ��Խ��
								if (devinfo.getErrdatetime() == null)
									devinfo.setErrdatetime("");
								if (devinfo.getOkdatetime() == null)
									devinfo.setOkdatetime("");

								if (devinfo.getErrdatetime().compareTo(devinfo.getOkdatetime())>=0){
									pLC2MES_single.setErrcode(barCode2ErrCode(devinfo.getBarCode()));  //��������������תΪ��������
									pLC2MES_single.setOpdatetime(devinfo.getErrdatetime());
									pLC2MES_single.setOperator(devinfo.getErroperater());
									pLC2MES_single.setStatus("NG");
//									Util698.log(PLC2MESThread.class.getName(), "NG", Debug.LOG_INFO);
								}
								else{
									pLC2MES_single.setOpdatetime(devinfo.getOkdatetime());
									pLC2MES_single.setOperator(devinfo.getOkoperater());
									// xuky 2018.10.23 ���оƬID��Ϣ
									pLC2MES_single.setSAVEID(devinfo.getBarCode());
									pLC2MES_single.setStatus("OK");
//									Util698.log(PLC2MESThread.class.getName(), "OK", Debug.LOG_INFO);
								}
								pLC2MES_single.setAddr(addr);

								// xuky 2019.01.15 �������;������
								if (devinfo.getType().indexOf("1") >=0)
									pLC2MES_single.setDevtype("II�Ͳɼ���");
								else if (devinfo.getType().indexOf("3") >=0)
									pLC2MES_single.setDevtype("��ģ��");
								else if (devinfo.getType().indexOf("2") >=0)
									pLC2MES_single.setDevtype("������");
								else if (devinfo.getType().indexOf("4") >=0)
									pLC2MES_single.setDevtype("·��");
							}
						}
//						jsonStr = new Gson().toJson(pLC2MES);
//						Util698.log(PLC2MESThread.class.getName(), "jsonStr1:"+jsonStr, Debug.LOG_INFO);

						// 2����2�㣬���ݵ�ַ��Ϣ��ѯProduceLog  ��β��Խ������һ��
						String where = "where status!='1' and addr = '"+addr+"'";
						// ������������е�������������ݴ��䣬�Ա����ID�ظ����ж�
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
								// 3����3����Ϣ������logid����ȡ������ϸ��Ϣ
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

						// ��֯ΪwebServices��ʽ�������ݴ���
//						Util698.log(PLC2MESThread.class.getName(), "��ʼnew Gson().toJson...", Debug.LOG_INFO);
						jsonStr = new Gson().toJson(pLC2MES_single);
						Util698.log(PLC2MESThread.class.getName(), "MES�������ݷ���\r"+jsonStr, Debug.LOG_INFO);

						// ����webservices�ӿڽ������ݴ���
						String webReturn = "";
						String url = "http://10.1.200.16:8087/MesNewWebService.asmx?op=UploadTestWORKData";
						String pingAddr = "10.1.200.16";
						endpoint = "http://10.1.200.16:8087/MesNewWebService.asmx";
						Namespace = "http://tempuri.org/";
						funName = "UploadTestWORKData";

						webReturn = UploadTestWORKData(jsonStr);
						Util698.log(PLC2MESThread.class.getName(), "MES�������ݽ���\r"+webReturn, Debug.LOG_INFO);


						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 ֱ�ӷ��ʽӿڵ�ַ�����Ƿ�ͨ
							String url_data = Util698.getURLData(url);
							Util698.log(PLC2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 ֱ��ping�ӿڵ�ַ�����Ƿ�ͨ
							Util698.isConnect(pingAddr);

							// xuky 2018.11.08 ������ִ�������Ҫ�ȴ�һ��ʱ����ٴ�ִ��
							Debug.sleep(1000 * 60 * 5);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(PLC2MESThread.class.getName(), "MES�������ݽ���\r"+webReturn, Debug.LOG_INFO);
						}
						// xuky 2018.11.08 ����2��
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 ������ִ�������Ҫ�ȴ�һ��ʱ����ٴ�ִ��
							Debug.sleep(1000 * 60 * 3);
							webReturn = UploadTestWORKData(jsonStr);
							Util698.log(PLC2MESThread.class.getName(), "MES�������ݽ���\r"+webReturn, Debug.LOG_INFO);
						}
						if (webReturn.indexOf("UploadTestWORKData") >= 0){
							// xuky 2018.11.08 ֱ�ӷ��ʽӿڵ�ַ�����Ƿ�ͨ
							String url_data = Util698.getURLData(url);
							Util698.log(PLC2MESThread.class.getName(), "getURLData url="+url +" recv="+url_data, Debug.LOG_INFO);
							// xuky 2018.11.08 ֱ��ping�ӿڵ�ַ�����Ƿ�ͨ
							Util698.isConnect(pingAddr);
						}

						if (webReturn.length() > 200)
							webReturn = webReturn.substring(0,200);

						status = "1";
						if (!webReturn.equals("OK")){
							// ������ص���Ϣ�Ǽ�¼�Ѿ�����
							if (webReturn.indexOf("NG:II�ɲ������ݽ������ID") >= 0 && webReturn.indexOf("�Ѵ��ڼ�¼") >= 0 )
								status = "1";
							else
								status = "2";
							// xuky 2019.02.13 ���ӿڱ�����ݽ�������ִ������ݡ����д���
							if (webReturn.indexOf("NG:��ADDR�Ѵ���ΪOK�ļ�¼") >= 0 )
								status = "1";
							else
								status = "2";
						}

						//  �޸�producelog2MES���е��ϴ����
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
					// xuky 2018.06.28 ͨ�����´�������������  ��Ϊ������һ�ΰ����������
					if (!is_single){
						// xuky 2018.10.28 ���²�ѯ�µ�����
						break;  // break ������forѭ��
					}
				}

				Debug.sleep(1000);
			}
			catch (Exception e){
				Util698.log(PLC2MESThread.class.getName(), "init Exception:"+e.getMessage(), Debug.LOG_INFO);
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
			// ����һ������(service)����(call)
			Call call = (Call) new Service().createCall();
			// ����service����URL
			call.setTargetEndpointAddress(new java.net.URL(endpoint));
			// ���ú�������
			call.setOperationName(new QName(Namespace, funName));
			// ��Ӳ���
			call.addParameter(new QName(Namespace, "msg"),
					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName(Namespace, "workstationSN"),
					org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
			call.setUseSOAPAction(true);
			// ���ز���������
			call.setReturnType(org.apache.axis.encoding.XMLType.SOAP_STRING);
			// ��ǰ���setOperationName��Щ�ظ��������Ǳ���ģ�
			call.setSOAPActionURI(Namespace+funName);
			// �����Ϣ
//			String msg = "{\"addr\":\"01\",\"status\":\"210\",\"ITEMS\":[{\"ID\":\"1\",\"name\":\"1\",\"result\":\"10-23s-00\",\"subid\":\"123232\",\"caseno\":\"1\"}]}";
			// ����������Ϊһ������
			String ret = (String) call.invoke(new Object[] { data, workstationSN });
			return ret;

		} catch (Exception e) {
			Util698.log(PLC2MESThread.class.getName(), "UploadTestWORKData Exception:"+e.getMessage(), Debug.LOG_INFO);
			return "UploadTestWORKData Exception:"+e.getMessage();
		}
	}

	private String barCode2ErrCode(String code){
		String ret = "GN04";  // GN04 ���ϵ�
		if (code == null) return ret;

		if (code.indexOf("��ַ") >= 0 ){
			if (code.indexOf("����") >= 0 || code.indexOf("д") >= 0 )
				ret = "GN04";  // GN02 ���ⲻͨ   ���ι����ۺ���Ϊ���ϵ���п���
			if (code.indexOf("��") >= 0 )
				ret = "CS03";  // CS03 ��ַ����
		}
		if (code.indexOf("����") >= 0 )
			ret = "CS01";  // CS01 �������
		if (code.indexOf("ʡ��") >= 0 || code.indexOf("ģʽ") >= 0 )
			ret = "CS02";  // CS02 ģʽ����
		if (code.indexOf("485") >= 0 )
			ret = "GN03";  // GN03 485��ͨ
		if (code.indexOf("�ز�") >= 0 )
			ret = "GN01";  // GN01 �ز���ͨ  �������������ȼ����
		return ret;
	}

	public static void main(String[] args) {
		PLC2MESThread.getInstance();
	}

}
