package produce.deal;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.eastsoft.protocol.Frame645;
import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.entity.ProduceCase;
import produce.entity.ProduceCaseDaoImpl;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseResultDaoImpl;
import socket.DealSendBlockLock;
import socket.SendBlockData;
import socket.SendData;
import util.PublisherShowList;
import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

// xuky 2017.06.21 ִ�в�������
public class DealTestCase1 implements Observer {

	// xuky 2018.03.14 ���ִ��Ч�� ȥ�������չʾ����
	Boolean RUNFASTER = SoftParameter.getInstance().getRUNFASTER();

	private Boolean isEnd = false;
	// xuky 2017.09.04 isPreEnd ��ʾ���ͽ����� isEnd��������������
	private Boolean isPreEnd = false;
	private Boolean isGetMsg = false;
	int msgFlag = -4;

	private String ADDR = "";
	String OLDMSG = "";
	ProduceCaseResult produceCaseResult = null;

	// xuky 2018.01.24 �µĲ��д����־
	// ��������produceCaseList�����������comid=RT�����ݣ���ʾ������������
	// 0:ԭ��ģʽ(��·������);1:RTģʽ(��·������)
	// xuky 2018.02.06 2:ȫ������ģʽ
	int NEW_PARALLEL = 0;

	//  ���������л�������������޸Ĵ�������Ϣ
	private String devType = "1"; // 1.II��;2.������


	private IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult = null;
	private IBaseDao<ProduceCase> iBaseDao_ProduceCase1 = null;

	List<ProduceCase> produceCaseList = null;
	int currentRow = 0;
	int allNum = 0;
	int runID = 0;

	// ��������ʱ�������˶��󣬽�������ʱ���´˶�������
	ProduceCaseResult produceCaseResult_new1 = null;

	int UsingTime = 0;

	public DealTestCase1() {
		// 1���첽�������̣� ��3�� �ڹ��캯���У���Publisher����Ϣ�����ߣ�������ϵ
		PublisherUI.getInstance().addObserver(this);
		PublisherShowList.getInstance().addObserver(this);
		RUNFASTER = SoftParameter.getInstance().getRUNFASTER();
		iBaseDao_ProduceCase1 = new ProduceCaseDaoImpl();
		iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
		produceCaseResult = new ProduceCaseResult();
	}
	public void init() {
		// xuky 2018.03.14 ���ִ��Ч�� ȥ�������չʾ����


		isEnd = false;
		isPreEnd = false;
		isGetMsg = false;
		msgFlag = -4;
		ADDR = "";
		OLDMSG = "";

		// xuky 2018.01.24 �µĲ��д����־
		// ��������produceCaseList�����������comid=RT�����ݣ���ʾ������������
		// 0:ԭ��ģʽ(��·������);1:RTģʽ(��·������)
		// xuky 2018.02.06 2:ȫ������ģʽ
		NEW_PARALLEL = 0;

		//  ���������л�������������޸Ĵ�������Ϣ
		devType = "1"; // 1.II��;2.������


		produceCaseList = null;
		currentRow = 0;
		allNum = 0;
		runID = 0;

		// ��������ʱ�������˶��󣬽�������ʱ���´˶�������
		produceCaseResult_new1 = null;

		UsingTime = 0;

	}

	public String[] Start(String subid, int runID, String addr, int numOfAll) {

		String[] ret = {"ʧ��ԭ�򣺱�����֤δͨ��",devType};
		this.ADDR = addr;
		this.runID = runID;
		// ��ȡ�������������������У�
		// 1���ȴ���ʱ�󣬷��ͱ���
		// 2���ȴ���ʱ �ȴ�����������յ����ģ��������ݱȶԣ��жϽ��
		// 3������ȴ���ʱ����Ȼ�޽��ձ��ģ��������Դ������ж�η��ͣ�����ﵽ���������ж�Ϊ��ʱ

		// xuky 2018.03.14 Ϊ�����Ч�ʣ����ٴ����ݿ��ѯ����
		if (!SoftParameter.getInstance().getCaseListID().equals(subid +"-"+SoftParameter.getInstance().getPCID())){
			String where = "where subid='" + subid + "' and computer='" + SoftParameter.getInstance().getPCID() + "'";

			SoftParameter.getInstance().setCaseList(iBaseDao_ProduceCase1.retrieve(where, " order by caseno"));
		}
		produceCaseList = SoftParameter.getInstance().getCaseList();

//		Util698.log("xuky", "temp-retrieve.end", Debug.LOG_INFO);

		allNum = produceCaseList.size();

		// xuky 2018.01.25 ���ݲ��������е�portid=RT���ж��Ƿ�Ϊ�µĲ��в��Է�ʽ
		// ����·�ɽ��ж�ռ��������·������в��в���
		for (int i = 0; i < allNum; i++) {
			ProduceCase produceCase = produceCaseList.get(i);
			String port = "";
			if (produceCase.getNote() != null)
				port = produceCase.getNote();
			if (port.equals("portid=RT")) {
				NEW_PARALLEL = 1;
			}
			if (produceCase.getName().indexOf("ȫ����") >= 0) {
				NEW_PARALLEL = 2;
				break;
			}

		}
		for (int i = 0; i < allNum; i++) {
			ProduceCase produceCase = produceCaseList.get(i);

			String port = "";
			if (produceCase.getNote() != null)
				port = produceCase.getNote();

			if (port.toLowerCase().indexOf("udp") >= 0 || produceCase.getName().indexOf("�Լ�") >= 0)
				devType = "2";
			if (port.toLowerCase().indexOf("telnet") >= 0)
				devType = "2";

			if (port.indexOf("portid") >= 0)
				port = port.split("=")[1];

			// xuky 2018.01.24 ����µĲ��д������
			if (NEW_PARALLEL == 1 || NEW_PARALLEL == 2) {
				if (port.equals("RT")) {
					// RT�������������д��ں��޸Ĳ���
				} else {
					// �Է�RT���ݽ������´���
					// numOfAll ��ʾ���
					// portid = numOfAll portid=1 portid=2 portid=3...
					produceCase.setNote("portid=" + numOfAll);
				}
				// MT485 = MT485-numOfAll MT485-1 MT485-2 MT485-3...
				String send = produceCase.getSend().toUpperCase();
				if (send.indexOf("MT485") >= 0)
					produceCase.setSend(send.replaceAll("MT485", "MT485-" + numOfAll));
				String expect = produceCase.getExpect().toUpperCase();
				if (expect.indexOf("MT485") >= 0)
					produceCase.setExpect(expect.replaceAll("MT485", "MT485-" + numOfAll));
			} else {
				// xuky 2018.01.24 ԭ�ȵĴ������
				// portid=1 port2n-1
				// portid=2 port2n
				if (port.equals("1"))
					produceCase.setNote("portid=" + (2 * numOfAll - 1));
				if (port.equals("2"))
					produceCase.setNote("portid=" + (2 * numOfAll));
			}

		}

//		Util698.log("xuky", "temp-produceCaseList.deal.end", Debug.LOG_INFO);

		isEnd = false;
		isPreEnd = false;

		isGetMsg = true;
		currentRow = 0;
		// xuky 2017.07.27 ���ʧ�ܣ������û�Ҫ�󣬿��Լ�������
		String keep = SoftParameter.getInstance().getParamValByKey("KEEP");

		while (true) {

			if (isGetMsg) {
				// System.out.println("isGetMsg msgFlag="+msgFlag);

				// �ظ�ΪĬ�ϵ�״̬
				isGetMsg = false;
				// System.out.println(ADDR + " Start isGetMsg
				// msgFlag="+msgFlag+" currentRow"+currentRow);

				if (msgFlag == -1) {
//					PublisherUI.getInstance().deleteObserver(this);
					ret[0] = "ʧ��ԭ��δ�򿪴���";
					return ret;
				}
				if (msgFlag == -2) {
					if (!keep.equals("1")) {
						// xuky 2017.10.26 ��ִ�����̽������⴦�� ���˳�������ִ��
						if (currentRow > 0) {
							ProduceCase produceCase = produceCaseList.get(currentRow - 1);
							if (produceCase.getNote().toLowerCase().indexOf("udp-server") >= 0) {
								// ���˳�������ִ��
								ret[0] = "ʧ��ԭ�򣺱�����֤δͨ��";
								// xuky 2017.11.07 ���� ����ִ����һ��֮�󣬾��˳�����
								isPreEnd = true;
							} else {
								// xuky 2017.10.26 ���齫��������δ��뼯����������ֹ���ֲ�ͬ��ִ�е����
//								PublisherUI.getInstance().deleteObserver(this);
								ret[0] = "ʧ��ԭ�򣺱�����֤δͨ��";
								return ret;
							}
						} else{
//							PublisherUI.getInstance().deleteObserver(this);
							ret[0] = "ʧ��ԭ�򣺱�����֤δͨ��";
							return ret;
						}
					} else {
						ret[0] = "ʧ��ԭ�򣺱�����֤δͨ��";
						// ����ִ�к����
					}
				}
				if (msgFlag == -3) {
					if (!keep.equals("1")) {
						// �˴���ʾ��ʱ����ʾ�Ѿ����������Բ���
//						PublisherUI.getInstance().deleteObserver(this);
						ret[0] = "ʧ��ԭ�򣺳�ʱ�޻�Ӧ";
						return ret;
					} else
						// ����ִ�к����
						ret[0] = "ʧ��ԭ�򣺳�ʱ�޻�Ӧ";
				}
				if (msgFlag == -4) {
					// ����ִ�к����
					ret[0] = "ʧ��ԭ�򣺿����޲�������";
				}
				if (msgFlag == 0) {
					// ����ִ�к����
					ret[0] = "�ɹ�";
				}

				// xuky 2017.09.04 ��Ҫ�����һ�η��ص����ݽ��д���
				if (isEnd)
					break;

				// �ָ�ΪĬ�ϵĴ�����
				msgFlag = -4;

				try {
//					Util698.log("xuky", "temp-sendData", Debug.LOG_INFO);

					sendData();
				} catch (Exception e) {
					Util698.log(DealTestCase1.class.getName(), "Start Exception: sendData ERROR-" + e.getMessage(), Debug.LOG_INFO);
					// xuky 2018.�����쳣

			        Util698.log(DealTestCase1.class.getName(), "���¿��������Զ�ִ�в��Թ���", Debug.LOG_INFO);
			        Util698.ResetApp();
				}

				// xuky 2017.09.04 isPreEnd ��ʾ���ͽ����� isEnd��������������
				// ����ֱ�ӽ�������Ϊ��Ҫ�ȴ����һ�����͵Ľ����Ϣ
				currentRow++;
				if (currentRow >= allNum)
					isPreEnd = true;

			}

			// ���ݴ���
			// Debug.sleep(1000);
			// ��ѭ��֮��Ъ
			Debug.sleep(100);
		}

		// xuky 2018.04.24 ���ֹ��쳣��ʵ�������ݣ�����ret1 = "ͨ���쳣-��������Ϊ�� ��������" + p1.getCaseno()
		Debug.sleep(1000);

		// xuky 2017.09.04 �����Ͻ����ж��Ƿ���Գɹ� ��֤
		String[] s = { "DealOperate", "", "������֤��������Ե�..." };
		PublisherUI.getInstance().publish(s);

		String ret1 = "";
		List<ProduceCaseResult> produceCaseResultList = iBaseDao_ProduceCaseResult.retrieve("where runID=" + runID,
				"order by caseno");
		for (int i = 0; i < produceCaseResultList.size(); i++) {
			ProduceCaseResult p1 = produceCaseResultList.get(i);
			// for ( ProduceCaseResult p1: produceCaseResultList){
			// xuky 2017.11.29 ����Ϊ�ջ���""ʱ���ж�
			if (p1.getResult() == null || p1.getResult().trim().equals("")) {
				if (p1.getExpect() == null)
					continue;
				if (p1.getExpect().trim().equals(""))
					continue;
				ret1 = "ͨ���쳣-��������Ϊ�� ��������" + p1.getCaseno() + " ����ʱ��:"+p1.getRecvtime() +"��������:"+p1.getRecv();
				Util698.log(DealTestCase1.class.getName(), "sendData ERROR-" + ret1, Debug.LOG_INFO);
				break;
			}
			if (!p1.getResult().equals("�ɹ�")){
				if (ret1.equals("")) {
					ret1 = p1.getResult();
					break;
				}
			}
			else{
				// xuky 2018.04.23 ����������������ƵĲ�����������Ҫ��ȡ��ĳЩ��Ϣ�������ݴ洢
				if (p1.getName().indexOf("SAVEID") >= 0 ){
					String recv = p1.getRecv();
					String data = recv;
					try{
						// xuky 2018.10.27 �������صĴ��������Ĳ������ᵼ���м��FE��Ϣ�������滻
//						data = data.replaceAll("FE", "");
						data = Util698.trimFronStr(data,"FE");
//						while (data.substring(0, 2).equals("FE")){
//							data = data.substring(2);
//						}

						if (Util698.checkFrameType(data).equals("376.2")){
							if (data.substring(44,46).equals("68")){
								// xuky 2019.01.17 ����Э���оƬIDλ��
								data = data.substring(72,72+48);
								data = DataConvert.reverseString(DataConvert.HexStrReduce33H(data));
							}
							else{
								// xuky 2019.01.17 ����Э���оƬIDλ��
								data = data.substring(50,50+48);
								data = DataConvert.reverseString(data);
							}
						}
						else{
							Frame645 frame645 = new Frame645(data, "", "");
							data = frame645.getData();
							// xuky 2018.10.23 ��������Ҫ�󣬷����������ݣ���ΪоƬID��Ϣ
							data = data.substring(10);
						}
					}
					catch (Exception e){
						ret1 = "SAVEID�����쳣"+e.getMessage();
						Util698.log(DealTestCase1.class.getName(), "SAVEID ERROR-" + e.getMessage() +" data:"+recv, Debug.LOG_INFO);
					}
//					data = data.substring(32,42);
//					data = DataConvert.HexStrReduce33H(data);
//					data = DataConvert.asciiHex2String(data);

//					data = frame645.getData().substring(8);
//					data = DataConvert.asciiHex2String(data);

					ret[0] = ret[0] +"-"+data;
				}
			}
		}
		produceCaseResultList = null;

		if (!ret1.equals(""))
			if (!ret1.equals(ret)) {
				ret[0] = ret1;
			}

//		PublisherUI.getInstance().deleteObserver(this);
		return ret;
	}


	private void sendData() throws Exception {
		ProduceCase produceCase = produceCaseList.get(currentRow);
		String testMsg = "�豸" + ADDR + " ����:" + (currentRow + 1) + "/" + allNum + " ����:" + produceCase.getName();
//		Util698.log(DealTestCase1.class.getName(), testMsg, Debug.LOG_INFO);

		String[] s = { "DealOperate", "", testMsg };
		PublisherUI.getInstance().publish(s);

		produceCaseResult.init();
		produceCaseResult.setADDR(ADDR);
		produceCaseResult.setRunID(runID);

		produceCaseResult.setSubid(produceCase.getSubid());
		produceCaseResult.setName(produceCase.getName());
		// produceCaseResult.setSend(produceCase.getSend());

		String sendData = produceCase.getSend();
		String expect = produceCase.getExpect();
		if (produceCaseResult.getName().indexOf("����") >= 0)
			produceCaseResult.setSend(sendData);
		else {
			if (sendData.indexOf("&&") >= 0) {
				// xuky 2018.01.23 ��������г�����&&,��ʾ�������͡����ͺ��յ�ȷ�ϣ�������Ҫ�ȴ��ظ�����
				// Ŀǰ���������ظ��������ĵ����
				produceCaseResult.setWaitReply(true);
				produceCaseResult.setSend0(sendData);
				produceCaseResult.setExpect0(expect);
				// getSend0�Ƿ��ͣ������͵�ȷ��
				sendData = produceCaseResult.getSend0().split("&&")[0];
				expect = produceCaseResult.getSend0().split("&&")[1];
				// ʹ�÷������ݵĵ�2����Ϊ�����ظ�����
			}
			produceCaseResult.setSend(Util698.DealFrameWithParam(sendData, ADDR, produceCase.getProtocol()));
		}

		// produceCaseResult.setExpect(produceCase.getExpect());
		if (produceCaseResult.getName().indexOf("����") >= 0)
			expect = "ok";
		else
			expect = Util698.DealFrameWithParam(expect, ADDR, produceCase.getProtocol());

		// xuky 2017.07.20 ���ܶ�����и�ʽ���������ݿ���Ϊ68 [SEQ=6] 68 91 05 32 3A 33 37
		// [MT=1] ** 16
		// expect.replaceAll(" ", "");
		// expect = Util698.seprateString(expect, " ");
		produceCaseResult.setExpect(expect);

		produceCaseResult.setDelaytime(produceCase.getDelaytime());
		produceCaseResult.setWaittime(produceCase.getWaittime());
		produceCaseResult.setProtocol(produceCase.getProtocol());
		produceCaseResult.setRetrys(produceCase.getRetrys());
		produceCaseResult.setNote(produceCase.getNote());
		produceCaseResult.setAnalys(produceCase.getAnalys());
		produceCaseResult.setCaseno(produceCase.getCaseno());

		// javafxutil.f_alert_informationDialog("������ʾ", "���������б�����д�˿���Ϣ
		// "+produceCase.getName());
		String note = produceCase.getNote();
		if (note.indexOf("portid") >= 0) {
			String port = note.split("=")[1];
			produceCaseResult.setPort(port);
		} else {
			produceCaseResult.setPort(note.toLowerCase());
		}

//		xuky  ���ֹ�iBaseDao_ProduceCaseResult�����ظ������
		// ��ʱ��setRecvtime��������ݵ�ʱ�䣬������ʵ�ʽ������ݵ�ʱ�䣬��ʱ��result=""
		// �������sendTime�У��ᵼ���жϳ�ʱ����������
		// ������Խ����ˣ�result=""�����������������
		produceCaseResult.setRecvtime(DateTimeFun.getDateTimeSSS());
		produceCaseResult_new1 = iBaseDao_ProduceCaseResult.create(produceCaseResult);
//		Util698.log(DealTestCase1.class.getName(), "ProduceCaseResult.create��" + produceCaseResult_new1.getADDR()+"-"+produceCaseResult_new1.getName()+"-"+produceCaseResult_new1.getID(), Debug.LOG_INFO);

		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(produceCaseResult_new1, new ProduceCaseResult(), "");
		// xuky 2017.07.05 ���Ͳ��Թ�����Ϣ
		Object[] s2 = { "DealTestCase", "new", produceCaseResult_tmp };
		Util698.log(DealTestCase1.class.getName(), "DealTestCase.new " + produceCaseResult_tmp,Debug.LOG_INFO);

		PublisherShowList.getInstance().publish(s2);
		produceCaseResult_tmp = null;

		// System.out.println("DealTestCase newResult="+
		// produceCaseResult_new.getResult());

		// ���ͱ������� --begin--
		// sendFrame(produceCaseResult_new);

		// xuky 2017.10.12 telnet���͵����⴦��
		String portStr = produceCaseResult_new1.getPort();
		if (portStr.equals("telnet") || portStr.indexOf("client") >= 0 || portStr.indexOf("server") >= 0
				|| portStr.indexOf("dos") >= 0) {
//			Util698.log(DealTestCase1.class.getName(),
//					"��ӷ���������(telnet��) taskID:" + produceCaseResult_new1.getADDR() + "."
//							+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//					Debug.LOG_INFO);
			SendData.getInstance().push(produceCaseResult_new1);
			return;
		}

		// xuky 2018.01.24 ����µĲ��д������
		if (produceCaseResult_new1.getName().indexOf("����") >= 0) {
			// xuky 2018.01.25 ��Ϊ���ʵ��Ժ�ʱ�ϳ�����������û����õĵȴ�ʱ��С��6000����Ҫ����ΪĬ�ϵ���С6000
			int waittime = produceCaseResult_new1.getWaittime();
			if (waittime < 6000)
				produceCaseResult_new1.setWaittime(6000);

			produceCaseResult_new1.setWaittime(waittime);
//			Util698.log(
//					DealTestCase1.class.getName(), "�������(������) taskID:" + produceCaseResult_new1.getADDR() + "."
//							+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//					Debug.LOG_INFO);

			// xuky 2018.02.02 getSend=0ʱ�������������
			if (produceCaseResult_new1.getSend().equals("0"))
				SendData.getInstance().push(produceCaseResult_new1);
			else
				SendBlockData.getInstance().push(produceCaseResult_new1);
		} else if (NEW_PARALLEL == 1) {
			// xuky 2018.01.24 ����µĲ��д������
			// �����RT���ڣ�Ϊ��ռģʽ
			if (portStr.equals("RT")) {
//				if (!RUNFASTER)
//					Util698.log(DealTestCase1.class.getName(),
//						"�����������(RT��) taskID:" + produceCaseResult_new1.getADDR() + "."
//								+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//						Debug.LOG_INFO);

				// xuky 2018.04.24 ����豸��ʼ�ز�ͨ���ˣ��Ϳ��Խ�������豸
				DealSendBlockLock.getInstance().setAddr(produceCaseResult_new1.getADDR(),"��ʼ�ز�ͨ��");

				Util698.log(DealTestCase1.class.getName(),"�޸���ADDR(��ʼ�ز�ͨ��) ִ�����",Debug.LOG_INFO);

				SendBlockData.getInstance().push(produceCaseResult_new1);
			} else {
//				if (!RUNFASTER)
//					Util698.log(DealTestCase1.class.getName(),
//						"��ӷ���������(RT��) taskID:" + produceCaseResult_new1.getADDR() + "."
//								+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//						Debug.LOG_INFO);
				SendData.getInstance().push(produceCaseResult_new1);
			}
		} else if (NEW_PARALLEL == 2) {
			// xuky 2018.02.06 ����µĲ��д������
			// ȫ����ռ
//			Util698.log(
//					DealTestCase1.class.getName(), "�����������(RT��) taskID:" + produceCaseResult_new1.getADDR() + "."
//							+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//					Debug.LOG_INFO);
			SendBlockData.getInstance().push(produceCaseResult_new1);
		} else {
			// ĿǰII�ɵ���������������������˿ڣ������ز��˿��Ƕ�ռ�ŵ���ͬһʱ��ֻ����һ·��ͨ�ţ����͡��ȴ������ա��ȴ���ʱ����ʱ�ط���
			if (Util698.isNumber(portStr)) {
				int port = DataConvert.String2Int(portStr);
				if (port % 2 == 1) {
//					Util698.log(DealTestCase1.class.getName(),
//							"�����������(isNumber) taskID:" + produceCaseResult_new1.getADDR() + "."
//									+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//							Debug.LOG_INFO);
					SendBlockData.getInstance().push(produceCaseResult_new1);
				} else {
//					Util698.log(DealTestCase1.class.getName(),
//							"��ӷ���������(isNumber) taskID:" + produceCaseResult_new1.getADDR() + "."
//									+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//							Debug.LOG_INFO);
					SendData.getInstance().push(produceCaseResult_new1);
				}
			} else {
//				Util698.log(DealTestCase1.class.getName(),
//						"�����������(����Ϊ�ز��˿�) taskID:" + produceCaseResult_new1.getADDR() + "." + produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//						Debug.LOG_INFO);
				SendBlockData.getInstance().push(produceCaseResult_new1);
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Object[] s = (Object[]) arg;
		if (s[0].equals("DealTestCase")) {
			DealData(arg);
		}

	}

	// �Խ��յ������ݽ��д���
	// xuky 2017.08.25 ��������synchronized�ᵼ������
	// private synchronized void DealData(Object arg) {
	private synchronized void DealData(Object arg) {
		Object[] s = (Object[]) arg;
		Object object = s[2];
		ProduceCaseResult produceCaseResult = (ProduceCaseResult) object;

		if (produceCaseResult.getADDR().equals(ADDR)) {
			if (produceCaseResult.getResult() == null || produceCaseResult.getResult().equals(""))
				return;

			// // xuky 2018.01.23 ��Ϊ��Ҫ�ȴ���������ʱ��ִ�����´���
			if (produceCaseResult.getResult().equals("0��0��0")) {
				// ���ֳɹ�����Ҫ����ִ��
				return;
			}

			if (produceCaseResult.getResult().equals("0ʧ0��0")) {
				// ���ֳɹ�����Ҫ����ִ��
				return;
			}

			// System.out.println("DealData "+produceCaseResult.getADDR()+"."
			// +produceCaseResult.getCaseno()+"
			// Result="+produceCaseResult.getResult());
			String msg = produceCaseResult.getADDR() + "." + produceCaseResult.getCaseno();
			// System.out.println("DealData isGetMsg = true " + msg);
			if (OLDMSG.equals(msg)) {
				System.out.println("DealData OLDMSG.equals(msg) ");
				return;
			}
			OLDMSG = msg;
			isGetMsg = true;

			if (produceCaseResult.getResult().equals("δ�򿪴���")) {
				msgFlag = -1;
			}
			if (produceCaseResult.getResult().equals("ʧ��")) {
				msgFlag = -2;
			}
			if (produceCaseResult.getResult().equals("��ʱ")) {
				msgFlag = -3;
			}
			if (produceCaseResult.getResult().equals("�ɹ�")) {
				msgFlag = 0;
			}

			// xuky 2017.09.04 isPreEnd ��ʾ���ͽ����� isEnd��������������
			if (isPreEnd) {
				isEnd = true;
			}

		}

	}

	public static void main(String[] args) {

//		String str = "";
//
//		// HEXתΪASCII
//		str = "83 7F 76 80 64 69 69 66 92 7C 7C 5B A9 64 64 74 65 63 65 5C 92 64 6A 63 67 65 63 53 53 53 53 53";
//		System.out.println(str);
//		str = str.replaceAll(" ", "");
//		str = DataConvert.HexStrReduce33H(str);
//		str = DataConvert.asciiHex2String(str);
//		System.out.println(str);
//
//		// ASCIIתΪHEX
//		str = "PLCM1663_II(v11A202)_170420     ";
//		System.out.println(str);
//		str = DataConvert.string2ASCIIHexString(str, str.length());
//		str = newFrame645().formatData(str);
//		str = DataConvert.reverseString(str);
//		str = Util698.seprateString(str, " ");
//		System.out.println(str);

	}

}
