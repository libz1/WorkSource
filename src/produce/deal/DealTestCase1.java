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

// xuky 2017.06.21 执行测试用例
public class DealTestCase1 implements Observer {

	// xuky 2018.03.14 提高执行效率 去掉界面的展示部分
	Boolean RUNFASTER = SoftParameter.getInstance().getRUNFASTER();

	private Boolean isEnd = false;
	// xuky 2017.09.04 isPreEnd 表示发送结束了 isEnd才是真正结束了
	private Boolean isPreEnd = false;
	private Boolean isGetMsg = false;
	int msgFlag = -4;

	private String ADDR = "";
	String OLDMSG = "";
	ProduceCaseResult produceCaseResult = null;

	// xuky 2018.01.24 新的并行处理标志
	// 遍历整个produceCaseList，如果出现了comid=RT的数据，表示这是特殊用例
	// 0:原有模式(多路抄控器);1:RT模式(单路抄控器)
	// xuky 2018.02.06 2:全部阻塞模式
	int NEW_PARALLEL = 0;

	//  后续代码中会根据用例自行修改此类型信息
	private String devType = "1"; // 1.II采;2.集中器


	private IBaseDao<ProduceCaseResult> iBaseDao_ProduceCaseResult = null;
	private IBaseDao<ProduceCase> iBaseDao_ProduceCase1 = null;

	List<ProduceCase> produceCaseList = null;
	int currentRow = 0;
	int allNum = 0;
	int runID = 0;

	// 发送数据时，创建此对象，接收数据时更新此对象数据
	ProduceCaseResult produceCaseResult_new1 = null;

	int UsingTime = 0;

	public DealTestCase1() {
		// 1、异步交互过程， （3） 在构造函数中，与Publisher（消息发布者）建立联系
		PublisherUI.getInstance().addObserver(this);
		PublisherShowList.getInstance().addObserver(this);
		RUNFASTER = SoftParameter.getInstance().getRUNFASTER();
		iBaseDao_ProduceCase1 = new ProduceCaseDaoImpl();
		iBaseDao_ProduceCaseResult = new ProduceCaseResultDaoImpl();
		produceCaseResult = new ProduceCaseResult();
	}
	public void init() {
		// xuky 2018.03.14 提高执行效率 去掉界面的展示部分


		isEnd = false;
		isPreEnd = false;
		isGetMsg = false;
		msgFlag = -4;
		ADDR = "";
		OLDMSG = "";

		// xuky 2018.01.24 新的并行处理标志
		// 遍历整个produceCaseList，如果出现了comid=RT的数据，表示这是特殊用例
		// 0:原有模式(多路抄控器);1:RT模式(单路抄控器)
		// xuky 2018.02.06 2:全部阻塞模式
		NEW_PARALLEL = 0;

		//  后续代码中会根据用例自行修改此类型信息
		devType = "1"; // 1.II采;2.集中器


		produceCaseList = null;
		currentRow = 0;
		allNum = 0;
		runID = 0;

		// 发送数据时，创建此对象，接收数据时更新此对象数据
		produceCaseResult_new1 = null;

		UsingTime = 0;

	}

	public String[] Start(String subid, int runID, String addr, int numOfAll) {

		String[] ret = {"失败原因：报文验证未通过",devType};
		this.ADDR = addr;
		this.runID = runID;
		// 读取多个测试用例，逐个进行：
		// 1、等待延时后，发送报文
		// 2、等待超时 等待过程中如果收到报文，进行数据比对，判断结果
		// 3、如果等待超时后，依然无接收报文，根据重试次数进行多次发送，如果达到最大次数，判定为超时

		// xuky 2018.03.14 为了提高效率，减少从数据库查询数据
		if (!SoftParameter.getInstance().getCaseListID().equals(subid +"-"+SoftParameter.getInstance().getPCID())){
			String where = "where subid='" + subid + "' and computer='" + SoftParameter.getInstance().getPCID() + "'";

			SoftParameter.getInstance().setCaseList(iBaseDao_ProduceCase1.retrieve(where, " order by caseno"));
		}
		produceCaseList = SoftParameter.getInstance().getCaseList();

//		Util698.log("xuky", "temp-retrieve.end", Debug.LOG_INFO);

		allNum = produceCaseList.size();

		// xuky 2018.01.25 根据测试用例中的portid=RT来判断是否为新的并行测试方式
		// 即单路由进行独占操作，多路红外进行并行操作
		for (int i = 0; i < allNum; i++) {
			ProduceCase produceCase = produceCaseList.get(i);
			String port = "";
			if (produceCase.getNote() != null)
				port = produceCase.getNote();
			if (port.equals("portid=RT")) {
				NEW_PARALLEL = 1;
			}
			if (produceCase.getName().indexOf("全阻塞") >= 0) {
				NEW_PARALLEL = 2;
				break;
			}

		}
		for (int i = 0; i < allNum; i++) {
			ProduceCase produceCase = produceCaseList.get(i);

			String port = "";
			if (produceCase.getNote() != null)
				port = produceCase.getNote();

			if (port.toLowerCase().indexOf("udp") >= 0 || produceCase.getName().indexOf("自检") >= 0)
				devType = "2";
			if (port.toLowerCase().indexOf("telnet") >= 0)
				devType = "2";

			if (port.indexOf("portid") >= 0)
				port = port.split("=")[1];

			// xuky 2018.01.24 添加新的并行处理规则
			if (NEW_PARALLEL == 1 || NEW_PARALLEL == 2) {
				if (port.equals("RT")) {
					// RT类的数据无需进行串口号修改操作
				} else {
					// 对非RT数据进行如下处理
					// numOfAll 表示序号
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
				// xuky 2018.01.24 原先的处理过程
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
		// xuky 2017.07.27 如果失败，根据用户要求，可以继续测试
		String keep = SoftParameter.getInstance().getParamValByKey("KEEP");

		while (true) {

			if (isGetMsg) {
				// System.out.println("isGetMsg msgFlag="+msgFlag);

				// 回复为默认的状态
				isGetMsg = false;
				// System.out.println(ADDR + " Start isGetMsg
				// msgFlag="+msgFlag+" currentRow"+currentRow);

				if (msgFlag == -1) {
//					PublisherUI.getInstance().deleteObserver(this);
					ret[0] = "失败原因：未打开串口";
					return ret;
				}
				if (msgFlag == -2) {
					if (!keep.equals("1")) {
						// xuky 2017.10.26 对执行流程进行特殊处理 不退出，继续执行
						if (currentRow > 0) {
							ProduceCase produceCase = produceCaseList.get(currentRow - 1);
							if (produceCase.getNote().toLowerCase().indexOf("udp-server") >= 0) {
								// 不退出，继续执行
								ret[0] = "失败原因：报文验证未通过";
								// xuky 2017.11.07 补充 继续执行下一个之后，就退出运行
								isPreEnd = true;
							} else {
								// xuky 2017.10.26 建议将下面的三段代码集合起来，防止出现不同步执行的情况
//								PublisherUI.getInstance().deleteObserver(this);
								ret[0] = "失败原因：报文验证未通过";
								return ret;
							}
						} else{
//							PublisherUI.getInstance().deleteObserver(this);
							ret[0] = "失败原因：报文验证未通过";
							return ret;
						}
					} else {
						ret[0] = "失败原因：报文验证未通过";
						// 继续执行后面的
					}
				}
				if (msgFlag == -3) {
					if (!keep.equals("1")) {
						// 此处显示超时，表示已经进行了重试操作
//						PublisherUI.getInstance().deleteObserver(this);
						ret[0] = "失败原因：超时无回应";
						return ret;
					} else
						// 继续执行后面的
						ret[0] = "失败原因：超时无回应";
				}
				if (msgFlag == -4) {
					// 继续执行后面的
					ret[0] = "失败原因：可能无测试用例";
				}
				if (msgFlag == 0) {
					// 继续执行后面的
					ret[0] = "成功";
				}

				// xuky 2017.09.04 需要对最后一次返回的数据进行处理
				if (isEnd)
					break;

				// 恢复为默认的错误码
				msgFlag = -4;

				try {
//					Util698.log("xuky", "temp-sendData", Debug.LOG_INFO);

					sendData();
				} catch (Exception e) {
					Util698.log(DealTestCase1.class.getName(), "Start Exception: sendData ERROR-" + e.getMessage(), Debug.LOG_INFO);
					// xuky 2018.出现异常

			        Util698.log(DealTestCase1.class.getName(), "重新开启，且自动执行测试过程", Debug.LOG_INFO);
			        Util698.ResetApp();
				}

				// xuky 2017.09.04 isPreEnd 表示发送结束了 isEnd才是真正结束了
				// 不能直接结束，因为需要等待最后一个发送的结果信息
				currentRow++;
				if (currentRow >= allNum)
					isPreEnd = true;

			}

			// 数据处理
			// Debug.sleep(1000);
			// 死循环之间歇
			Debug.sleep(100);
		}

		// xuky 2018.04.24 出现过异常，实际有数据，但是ret1 = "通信异常-接收数据为空 用例次序：" + p1.getCaseno()
		Debug.sleep(1000);

		// xuky 2017.09.04 更加严谨的判断是否测试成功 验证
		String[] s = { "DealOperate", "", "正在验证结果，请稍等..." };
		PublisherUI.getInstance().publish(s);

		String ret1 = "";
		List<ProduceCaseResult> produceCaseResultList = iBaseDao_ProduceCaseResult.retrieve("where runID=" + runID,
				"order by caseno");
		for (int i = 0; i < produceCaseResultList.size(); i++) {
			ProduceCaseResult p1 = produceCaseResultList.get(i);
			// for ( ProduceCaseResult p1: produceCaseResultList){
			// xuky 2017.11.29 完善为空或是""时的判断
			if (p1.getResult() == null || p1.getResult().trim().equals("")) {
				if (p1.getExpect() == null)
					continue;
				if (p1.getExpect().trim().equals(""))
					continue;
				ret1 = "通信异常-接收数据为空 用例次序：" + p1.getCaseno() + " 接收时间:"+p1.getRecvtime() +"接收数据:"+p1.getRecv();
				Util698.log(DealTestCase1.class.getName(), "sendData ERROR-" + ret1, Debug.LOG_INFO);
				break;
			}
			if (!p1.getResult().equals("成功")){
				if (ret1.equals("")) {
					ret1 = p1.getResult();
					break;
				}
			}
			else{
				// xuky 2018.04.23 如果出现了特殊名称的测试用例，需要提取出某些信息进行数据存储
				if (p1.getName().indexOf("SAVEID") >= 0 ){
					String recv = p1.getRecv();
					String data = recv;
					try{
						// xuky 2018.10.27 出现严重的错误，这样的操作将会导致中间的FE信息被错误替换
//						data = data.replaceAll("FE", "");
						data = Util698.trimFronStr(data,"FE");
//						while (data.substring(0, 2).equals("FE")){
//							data = data.substring(2);
//						}

						if (Util698.checkFrameType(data).equals("376.2")){
							if (data.substring(44,46).equals("68")){
								// xuky 2019.01.17 福建协议的芯片ID位置
								data = data.substring(72,72+48);
								data = DataConvert.reverseString(DataConvert.HexStrReduce33H(data));
							}
							else{
								// xuky 2019.01.17 国网协议的芯片ID位置
								data = data.substring(50,50+48);
								data = DataConvert.reverseString(data);
							}
						}
						else{
							Frame645 frame645 = new Frame645(data, "", "");
							data = frame645.getData();
							// xuky 2018.10.23 根据聂的要求，返回所有数据，作为芯片ID信息
							data = data.substring(10);
						}
					}
					catch (Exception e){
						ret1 = "SAVEID处理异常"+e.getMessage();
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
		String testMsg = "设备" + ADDR + " 进度:" + (currentRow + 1) + "/" + allNum + " 内容:" + produceCase.getName();
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
		if (produceCaseResult.getName().indexOf("速率") >= 0)
			produceCaseResult.setSend(sendData);
		else {
			if (sendData.indexOf("&&") >= 0) {
				// xuky 2018.01.23 如果报文中出现了&&,表示特殊类型。发送后，收到确认，但是需要等待回复数据
				// 目前仅仅处理，回复单条报文的情况
				produceCaseResult.setWaitReply(true);
				produceCaseResult.setSend0(sendData);
				produceCaseResult.setExpect0(expect);
				// getSend0是发送，及发送的确认
				sendData = produceCaseResult.getSend0().split("&&")[0];
				expect = produceCaseResult.getSend0().split("&&")[1];
				// 使用发送数据的第2段作为期望回复报文
			}
			produceCaseResult.setSend(Util698.DealFrameWithParam(sendData, ADDR, produceCase.getProtocol()));
		}

		// produceCaseResult.setExpect(produceCase.getExpect());
		if (produceCaseResult.getName().indexOf("速率") >= 0)
			expect = "ok";
		else
			expect = Util698.DealFrameWithParam(expect, ADDR, produceCase.getProtocol());

		// xuky 2017.07.20 不能对其进行格式化处理，数据可能为68 [SEQ=6] 68 91 05 32 3A 33 37
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

		// javafxutil.f_alert_informationDialog("操作提示", "测试用例中必须填写端口信息
		// "+produceCase.getName());
		String note = produceCase.getNote();
		if (note.indexOf("portid") >= 0) {
			String port = note.split("=")[1];
			produceCaseResult.setPort(port);
		} else {
			produceCaseResult.setPort(note.toLowerCase());
		}

//		xuky  出现过iBaseDao_ProduceCaseResult数据重复的情况
		// 此时的setRecvtime是添加数据的时间，而不是实际接收数据的时间，此时的result=""
		// 如果放在sendTime中，会导致判断超时代码有问题
		// 如果测试结束了，result=""，则数据是有问题的
		produceCaseResult.setRecvtime(DateTimeFun.getDateTimeSSS());
		produceCaseResult_new1 = iBaseDao_ProduceCaseResult.create(produceCaseResult);
//		Util698.log(DealTestCase1.class.getName(), "ProduceCaseResult.create：" + produceCaseResult_new1.getADDR()+"-"+produceCaseResult_new1.getName()+"-"+produceCaseResult_new1.getID(), Debug.LOG_INFO);

		ProduceCaseResult produceCaseResult_tmp = (ProduceCaseResult) Util698.objClone(produceCaseResult_new1, new ProduceCaseResult(), "");
		// xuky 2017.07.05 发送测试过程信息
		Object[] s2 = { "DealTestCase", "new", produceCaseResult_tmp };
		Util698.log(DealTestCase1.class.getName(), "DealTestCase.new " + produceCaseResult_tmp,Debug.LOG_INFO);

		PublisherShowList.getInstance().publish(s2);
		produceCaseResult_tmp = null;

		// System.out.println("DealTestCase newResult="+
		// produceCaseResult_new.getResult());

		// 发送报文数据 --begin--
		// sendFrame(produceCaseResult_new);

		// xuky 2017.10.12 telnet类型的特殊处理
		String portStr = produceCaseResult_new1.getPort();
		if (portStr.equals("telnet") || portStr.indexOf("client") >= 0 || portStr.indexOf("server") >= 0
				|| portStr.indexOf("dos") >= 0) {
//			Util698.log(DealTestCase1.class.getName(),
//					"添加非阻塞任务(telnet类) taskID:" + produceCaseResult_new1.getADDR() + "."
//							+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//					Debug.LOG_INFO);
			SendData.getInstance().push(produceCaseResult_new1);
			return;
		}

		// xuky 2018.01.24 添加新的并行处理规则
		if (produceCaseResult_new1.getName().indexOf("速率") >= 0) {
			// xuky 2018.01.25 因为速率调试耗时较长，所以如果用户设置的等待时间小于6000，需要设置为默认的最小6000
			int waittime = produceCaseResult_new1.getWaittime();
			if (waittime < 6000)
				produceCaseResult_new1.setWaittime(6000);

			produceCaseResult_new1.setWaittime(waittime);
//			Util698.log(
//					DealTestCase1.class.getName(), "添加任务(速率类) taskID:" + produceCaseResult_new1.getADDR() + "."
//							+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//					Debug.LOG_INFO);

			// xuky 2018.02.02 getSend=0时，是特殊的数据
			if (produceCaseResult_new1.getSend().equals("0"))
				SendData.getInstance().push(produceCaseResult_new1);
			else
				SendBlockData.getInstance().push(produceCaseResult_new1);
		} else if (NEW_PARALLEL == 1) {
			// xuky 2018.01.24 添加新的并行处理规则
			// 如果是RT串口，为独占模式
			if (portStr.equals("RT")) {
//				if (!RUNFASTER)
//					Util698.log(DealTestCase1.class.getName(),
//						"添加阻塞任务(RT类) taskID:" + produceCaseResult_new1.getADDR() + "."
//								+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//						Debug.LOG_INFO);

				// xuky 2018.04.24 如果设备开始载波通信了，就可以解锁这个设备
				DealSendBlockLock.getInstance().setAddr(produceCaseResult_new1.getADDR(),"开始载波通信");

				Util698.log(DealTestCase1.class.getName(),"修改锁ADDR(开始载波通信) 执行完成",Debug.LOG_INFO);

				SendBlockData.getInstance().push(produceCaseResult_new1);
			} else {
//				if (!RUNFASTER)
//					Util698.log(DealTestCase1.class.getName(),
//						"添加非阻塞任务(RT类) taskID:" + produceCaseResult_new1.getADDR() + "."
//								+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//						Debug.LOG_INFO);
				SendData.getInstance().push(produceCaseResult_new1);
			}
		} else if (NEW_PARALLEL == 2) {
			// xuky 2018.02.06 添加新的并行处理规则
			// 全部独占
//			Util698.log(
//					DealTestCase1.class.getName(), "添加阻塞任务(RT类) taskID:" + produceCaseResult_new1.getADDR() + "."
//							+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//					Debug.LOG_INFO);
			SendBlockData.getInstance().push(produceCaseResult_new1);
		} else {
			// 目前II采的配置来看，如果是奇数端口，就是载波端口是独占信道，同一时刻只能是一路在通信（发送、等待、接收、等待超时、超时重发）
			if (Util698.isNumber(portStr)) {
				int port = DataConvert.String2Int(portStr);
				if (port % 2 == 1) {
//					Util698.log(DealTestCase1.class.getName(),
//							"添加阻塞任务(isNumber) taskID:" + produceCaseResult_new1.getADDR() + "."
//									+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//							Debug.LOG_INFO);
					SendBlockData.getInstance().push(produceCaseResult_new1);
				} else {
//					Util698.log(DealTestCase1.class.getName(),
//							"添加非阻塞任务(isNumber) taskID:" + produceCaseResult_new1.getADDR() + "."
//									+ produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
//							Debug.LOG_INFO);
					SendData.getInstance().push(produceCaseResult_new1);
				}
			} else {
//				Util698.log(DealTestCase1.class.getName(),
//						"添加阻塞任务(奇数为载波端口) taskID:" + produceCaseResult_new1.getADDR() + "." + produceCaseResult_new1.getCaseno() + " " + produceCaseResult_new1.getName(),
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

	// 对接收到的数据进行处理
	// xuky 2017.08.25 如果添加了synchronized会导致死锁
	// private synchronized void DealData(Object arg) {
	private synchronized void DealData(Object arg) {
		Object[] s = (Object[]) arg;
		Object object = s[2];
		ProduceCaseResult produceCaseResult = (ProduceCaseResult) object;

		if (produceCaseResult.getADDR().equals(ADDR)) {
			if (produceCaseResult.getResult() == null || produceCaseResult.getResult().equals(""))
				return;

			// // xuky 2018.01.23 因为需要等待，所以暂时不执行以下代码
			if (produceCaseResult.getResult().equals("0成0功0")) {
				// 部分成功，还要继续执行
				return;
			}

			if (produceCaseResult.getResult().equals("0失0败0")) {
				// 部分成功，还要继续执行
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

			if (produceCaseResult.getResult().equals("未打开串口")) {
				msgFlag = -1;
			}
			if (produceCaseResult.getResult().equals("失败")) {
				msgFlag = -2;
			}
			if (produceCaseResult.getResult().equals("超时")) {
				msgFlag = -3;
			}
			if (produceCaseResult.getResult().equals("成功")) {
				msgFlag = 0;
			}

			// xuky 2017.09.04 isPreEnd 表示发送结束了 isEnd才是真正结束了
			if (isPreEnd) {
				isEnd = true;
			}

		}

	}

	public static void main(String[] args) {

//		String str = "";
//
//		// HEX转为ASCII
//		str = "83 7F 76 80 64 69 69 66 92 7C 7C 5B A9 64 64 74 65 63 65 5C 92 64 6A 63 67 65 63 53 53 53 53 53";
//		System.out.println(str);
//		str = str.replaceAll(" ", "");
//		str = DataConvert.HexStrReduce33H(str);
//		str = DataConvert.asciiHex2String(str);
//		System.out.println(str);
//
//		// ASCII转为HEX
//		str = "PLCM1663_II(v11A202)_170420     ";
//		System.out.println(str);
//		str = DataConvert.string2ASCIIHexString(str, str.length());
//		str = newFrame645().formatData(str);
//		str = DataConvert.reverseString(str);
//		str = Util698.seprateString(str, " ");
//		System.out.println(str);

	}

}
