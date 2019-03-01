package produce.deal;

import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DateTimeFun;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import frame.DealTypeCode;
import frame.FramePLC;
import javafx.application.Platform;
import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.base.javafxutil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import mina.MinaSerialServer;
import produce.entity.BarCodesInfo;
import produce.entity.BarCodesInfoDaoImpl;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.ProduceCase;
import produce.entity.ProduceCaseDaoImpl;
import produce.entity.ProduceCaseResult;
import produce.entity.ProduceCaseSub;
import produce.entity.ProduceCaseSubDaoImpl;
import produce.entity.ProduceParam;
import produce.entity.ProduceParamDaoImpl;
import produce.entity.ProduceRecord;
import produce.entity.ProduceRecordDaoImpl;
import produce.entity.TransImplObject;
import socket.DealSendBlockData;
import socket.DealSendBlockLock;
import socket.DealSendData;
import socket.PrefixMain;
import socket.SocketServerEast;
import ui.PrefixWindow;
import util.ObjectPoolDealOperateMuti;
import util.PublisherShowList;
import util.PublisherUI;
import util.SoftParameter;
import util.Util698;

public class TerminalParameterController extends BaseController implements Initializable, Observer {
    private String IS_SETID = ""; // ģ���·������ID

	// xuky 2018.07.16 ����̳߳صķ�ʽ�����̵߳���
//	ExecutorService pool = Executors.newFixedThreadPool(15);

	// xuky 2018.08.02 ͨ�� ThreadPoolExecutor�ķ�ʽ
	// �ο� https://www.cnblogs.com/zedosu/p/6665306.html
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    // xuky 2018.04.24 ����ģʽ�£��Զ�ѭ��ִ��
    // xuky 2018.07.09 ����IS_AUTO�ĺ��壬��˼���������Զ����е�״̬
    private Boolean IS_AUTO = false;
    private String RESET_TYPE = "RestCOMServicel";  //  RestApp\RestCOMServicel

    // ���ڽ���������ı�־λ
    private Boolean ISBUSY = false;
    // ����ʼִ��ʱ��
    private String BUSY_B_time = "";
//    private int RestCheck = 150000;  // 2.5 ���� = 2.5 * 60 *1000 ����   150000
    // �ж�����ִ���Ƿ��쳣��ʱ�䣬��ʱ�䴦��ISBUSY״̬
    // xuky 2018.10.08 ����Ϊ240�룬��4���ӵĵȴ�ʱ��  ��Ϊ˫ģ�豸����Ч��̫�͵��´˴ε���
    private int RestCheck = 80000*3;  // 80�� = 1 * 80 *1000 ����   240000  4����
    // ��¼�յ����������ģ������������·���
    private String PLCFrame = "";

    private IBaseDao<DevInfo> iBaseDao_DevInfo = null;

    int MAX = 180; // �ۼ��Զ�ִ�д���   ����������Զ���������
//  int MAX = 2; // �ۼ��Զ�ִ�д���   ����������Զ���������

    String txt_showMsg_txt = "", txt_showMsg_txt0 = "";
    String input_rate_txt = "", input_rate_txt0 = "";
    String lable_result_txt = "", lable_result_txt0 = "";
    Paint lable_result_value = null;
	IBaseDao<BarCodesInfo> iBaseDao_BarCodesInfo = new BarCodesInfoDaoImpl();

    // xuky 2018.06.01 ����testDetailList������ά��
    // Object[] = {type,data,rowNum}
    // type �Ƕ������ͣ����ԶԶ����������ݽ��д���
    // rowNum = -1 ��ʾadd ,rowNum != -1 ��ʾset

    Queue<Object[]> testDetailQueue = new LinkedList<Object[]>();


    Lock Queuelock = new ReentrantLock();
    Lock Listlock = new ReentrantLock();
    public void push(Object[] data) {
        Queuelock.lock();
        try {
            testDetailQueue.offer(data);
        } finally {
            Queuelock.unlock();
        }
    }

    // xuky 2018.06.01 ����Ϊ����ģʽ ��������߽��д���
    public Object[] pop() {
        Object[] obj = null;
        while (true) {
            Queuelock.lock();
            try {
                obj = testDetailQueue.poll();
                if (obj != null){
//					Util698.log(TerminalParameterController.class.getName(), "pop:"+obj, Debug.LOG_INFO);
                    return obj;
                }
            } finally {
                Queuelock.unlock();
            }
            Debug.sleep(50);
        }
    }

    // xuky 2018.05.29 �޸�Ϊ��text�н��������޸� �����߳̽��п��ƣ������߳�ֱ�Ӹ�������
    // ��ʱ�߳� ����ض�ʱ�䣬�������ݱȽϺ�д����� ��������б仯��д��



    int TestNum = 4;

    @FXML
    private TextField input_produceBarCode, input_comm, input_rate, input_produceBarCode1, input_produceBarCode2,
            input_produceBarCode3, input_produceBarCode4, input_produceBarCode5;

    String ADDR;

    // xuky 2018.03.14 ���ִ��Ч�� ȥ�������չʾ����
    Boolean RUNFASTER = SoftParameter.getInstance().getRUNFASTER();

    @FXML
    private Label txt_showMsg, lable_result, txt_showTitle, input_changeresult;

    @FXML
    BorderPane tableview_testDetail, tableview_param, tableview_begin_end;

    ObservableList<ProduceCaseResult> testDetailList = null;

    ObservableList<ProduceParam> observableList_param = null;

    ObservableList<ProduceRecord> observableList_record = null;
    private IBaseDao<ProduceRecord> iBaseDao_ProduceRecord = new ProduceRecordDaoImpl();

    ObjectCURD<ProduceCaseResult> testDetail_crud = null;
    ObjectCURD<ProduceParam> object_crud_param = null;
    ObjectCURD<ProduceRecord> object_crud_begin_end = null;

    // ���ͬʱִ�е�����ʱ��
    String MULTI_BEGIN = "", MULTI_END = "", SAVE_MSG = "", MULTI_END0 = "";
    int OK_NUM = 0;
    String OP_NAME = SoftParameter.getInstance().getUserManager().getUserid();
    String PCID = SoftParameter.getInstance().getPCID();
    // // ���ͬʱִ�еķ����ͽ���������
    // int SEND_NUM = 0;
    // int RECV_NUM = 0;


    String addr = "";

    private Boolean CREATE_SUM = false; // ����Ƿ�����˲��Ի���
    // �ظ����ĵ����Դ���
    int Send_repeateTime = 5, Send_repeateTime1 = 5;
    // ��ǰ�Ѿ�ִ�еĴ�����Ϣ
    int Send_time = 0, Send_time1 = 0;
    int send_interval = 1000; // ���Ͳ��Խ������ʱ��
    // ������¼�豸��ַ��no��Ϣ ʹ��LinkedHashMap��ȷ�����ݵĴ���
    Map<String, Object> NO_ADDR = new LinkedHashMap<String, Object>();


    public void setIS_AUTO(Boolean iS_AUTO) {
        IS_AUTO = iS_AUTO;
    }

    int current_time = 0;
    ProduceRecord RECORD_ADD = null;

    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            SoftParameter.getInstance().refreshDataFromDB();
            System.out.println(" ActionListener -> SoftParameter.refreshDataFromDB()");
        }
    };

    @Override
    public void init() {

    	String subid = SoftParameter.getInstance().getParamValByKey("PLANID");
		IBaseDao<ProduceCase> iBaseDao_ProduceCase1 = new ProduceCaseDaoImpl();
		String where = "where subid='" + subid + "' and computer='" + SoftParameter.getInstance().getPCID() + "'";
		SoftParameter.getInstance().setCaseList(iBaseDao_ProduceCase1.retrieve(where, " order by caseno"));
		boolean b1 = false,b2 = false, b3 = true;
		for(ProduceCase p :SoftParameter.getInstance().getCaseList() ){
			if ((p.getName().indexOf("д") >=0 || p.getName().indexOf("����") >=0) && p.getName().indexOf("ID") >=0)
				b1 = true;
			if ((p.getName().indexOf("��") >=0 || p.getName().indexOf("��ѯ") >=0) && p.getName().indexOf("ID") >=0)
				b2 = true;
			if (p.getName().indexOf("��") >=0 )
				b3 = true;
			if (p.getName().indexOf("·��") >=0 )
				b3 = false;
		}
		if (b1 && b2){
			if (b3)
				IS_SETID = "��ģ��";
			else
				IS_SETID = "·��";
		}
		SoftParameter.getInstance().setIS_SETID(IS_SETID);


    	// xuky 2018.10.25 ���ݲ������еĶ����������ֲ��Եȴ�ʱ�䣬�����ʱ��Ҫ������� �����½��в���
    	String str = SoftParameter.getInstance().getTESTALL_TIME_OUT();
    	if (str != null && !str.equals("")){
    		int init = RestCheck;
    		// ����û����õ����������⣬ʹ��Ĭ�ϵ��趨����
    		try{
        		RestCheck = DataConvert.String2Int(SoftParameter.getInstance().getTESTALL_TIME_OUT());
    		}
    		catch (Exception e){
    			RestCheck = init;
    		}

    	}

    	iBaseDao_DevInfo = new DevInfoDaoImpl();

//    	// xuky 2018.07.18 �����ڴ������ӿڴ����߳�
//    	PLC2MESThread.getInstance();

        Util698.log(TerminalParameterController.class.getName(), "PublisherUI.getInstance().addObserver(this)",Debug.LOG_INFO);
        PublisherUI.getInstance().addObserver(this);
        PublisherShowList.getInstance().addObserver(this);


        // JavaFX2 Stage�������
        // http://blog.csdn.net/alanzyy/article/details/18249107
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        parentStage.setX(bounds.getMinX());
        parentStage.setY(bounds.getMinY());
        parentStage.setWidth(bounds.getWidth());
        parentStage.setHeight(bounds.getHeight());


        // �ο� https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        long initialDelay = 0;
        long period = 300;
        // ÿ��n����ִ��һ��job1����2���ڵ�һ��ִ����ɺ�n��
//		service.scheduleWithFixedDelay(new MyScheduledExecutor_list(), initialDelay, period, TimeUnit.MILLISECONDS);
        service.scheduleWithFixedDelay(new MyScheduledExecutor_txt(), initialDelay, period, TimeUnit.MILLISECONDS);

        // ���´��벻�ܶ��ִ�У���Ϊpop������ʽ�� ������ʹ��MyScheduledExecutor_list
//		class MyScheduledExecutor_list implements Runnable {
        // �ο� https://www.cnblogs.com/dyllove98/archive/2013/06/23/3151268.html

    	// xuky 2018.08.02 ����̵߳����ƣ����ڳ�������ʱ���ж�λ
        pool.submit(new ListShowThread());

        if (IS_AUTO){
            int maxRetry = 1000;
            int i = 0;
            Util698.log(TerminalParameterController.class.getName(), "��֤Observer�Ƿ�ok"+SoftParameter.getInstance().getObserverOK(),Debug.LOG_INFO);
            SoftParameter.getInstance().setObserverOK("");
            while (i<maxRetry){
            	// ���ͱ��ģ����۲���ģʽ�Ƿ���Ч����Ч���ٿ������ͱ��Ĺ���
                Util698.log(TerminalParameterController.class.getName(), "������֤Observer����Ϣ",Debug.LOG_INFO);
    			String[] s = { "recv frame", "user data", "13869881856", "" };
    			// ע����Ҫʹ��PublisherUI����
    			PublisherUI.getInstance().publish(s);
                Debug.sleep(1000);
                String flag = SoftParameter.getInstance().getObserverOK();
                if (flag.equals("168")){
                    break;
                }
                i++;
            }

            // xuky 2018.07.09 ����жϣ��ж��Ƿ��пͻ��ˣ�PLC������������

            String send_data = SoftParameter.getInstance().getSENDPLC1();

            if (send_data == null || send_data.equals("")){

            }
            else{
                Util698.log(TerminalParameterController.class.getName(), "��֤PLC���� getRECVCLINET:"+SoftParameter.getInstance().getRECVCLINET(),Debug.LOG_INFO);
                // ��JFXMain��setRECVCLINET("");
                i = 0;
                while (i<maxRetry){
                    String flag = SoftParameter.getInstance().getRECVCLINET();
                    if (flag.equals("168")){
                        Util698.log(TerminalParameterController.class.getName(), "getRECVCLINET:"+flag +" and Send2PLC",Debug.LOG_INFO);
                        // xuky 2018.07.09 ��Ҫ����Ƿ���Ҫ�ظ�PLC���Խ����Ϣ
                        Send2PLC();
                        break;
                    }
                    i++;
                    Debug.sleep(200);
                }
            }

            // xuky 2018.07.11 ���������PLCFRAME�����ݣ������²���
            PLCFrame = SoftParameter.getInstance().getPLCFRAME();
            if (!PLCFrame.equals("")){
                Util698.log(TerminalParameterController.class.getName(), "���������²���:"+PLCFrame,Debug.LOG_INFO);
            	dealPLCData(PLCFrame,false);
            }
            else{
                Util698.log(TerminalParameterController.class.getName(), "������PLCFRAMEΪ�գ��������",Debug.LOG_INFO);
            }

            SoftParameter.getInstance().setRECVCLINET("");
            SoftParameter.getInstance().saveParam();
        }

//		// http://www.it1352.com/543057.html
//		// ʹ��Platform.runLater(()����ʱ����Ȼ����Ļ��ʾ�쳣
//		Task task = new Task() {
//		    @Override
//		    protected Object call() throws Exception {
//		        while (true) {
//		            this.updateMessage(MessageCenter.getInstance().PLCSimulator_msg+ " ʱ��-"+DateTimeFun.getDateTimeSSS());
//		            Debug.sleep(200);
//		        }
//		    }
//		};
//
//		Thread t = new Thread(task);
//		msg3.textProperty().bind(task.messageProperty());
//		t.start();

    }

    class MyScheduledExecutor_txt implements Runnable {
        @Override
        public void run() {
//			System.out.println("xuky debug MyScheduledExecutor_txt lable_result_txt="+lable_result_txt + " lable_result_txt0="+lable_result_txt0);

        	if (ISBUSY){
        		// �������³��ֵ�������������Թ����쳣�жϣ�����ISBUSY״̬����ʱ�޷����������ˣ����������������
//        		2018-07-09 17:31:42:162 [xuky add Detail row:-1 p1�ز�����000000098063-] deal.TerminalParameterController
//        		2018-07-09 17:32:14:154 [ScheduledExecutorService��ʽ ִ������ĳ�ʱ���  map.size=0] socket.DealSendData
//        		2018-07-09 17:33:14:159 [ScheduledExecutorService��ʽ ִ������ĳ�ʱ���  map.size=0] socket.DealSendData
//        		2018-07-09 17:34:14:164 [ScheduledExecutorService��ʽ ִ������ĳ�ʱ���  map.size=0] socket.DealSendData
//        		2018-07-09 17:34:59:426 [3recv �˿�:/127.0.0.1:58642 user data��1��:68252568010106303938303631020630393830363203063039383036330406303938303634050006002E16] socket.DealData
//        		2018-07-09 17:34:59:476 [�յ��������ģ�68252568010106303938303631020630393830363203063039383036330406303938303634050006002E16 framePLC.getControlData:01] deal.TerminalParameterController
//        		2018-07-09 17:34:59:568 [�������ԣ��������ݻظ���68 01 01 68 81 53 16] deal.TerminalParameterController
//        		2018-07-09 17:34:59:569 [sendData=>68 01 01 68 81 53 16] socket.SocketServerEast
//        		2018-07-09 17:34:59:570 [getBarCode_new ISBUSY return ] deal.TerminalParameterController

    			String nowTime = Util698.getDateTimeSSS_new();
    			long diff = Util698.getMilliSecondBetween_new(nowTime, BUSY_B_time);
    			if (diff > (long)RestCheck) {
    		        Util698.log(TerminalParameterController.class.getName(), "Exception �쳣����ISBUSYʱ����ʼʱ��"+BUSY_B_time+"����"+nowTime+"ʱ���"+RestCheck+" ��RestApp", Debug.LOG_INFO);

    				// ���������跢�ͻظ����ģ��ȴ����²��Լ���
    	            SoftParameter.getInstance().setSENDPLC1("");
    	            SoftParameter.getInstance().setSENDPLC2("");
//    	            SoftParameter.getInstance().setPLCFRAME(PLCFrame);
    	            SoftParameter.getInstance().saveParam();
    	            // ����������������½��в��ԣ���Ҫ��¼���յ�����������
    	            // �ȴ�ʱ�䲻�ɹ���

    				RestApp();
    				return;
    			}
        	}

            if (!txt_showMsg_txt.equals(txt_showMsg_txt0)) {
                Platform.runLater(() -> txt_showMsg.setText(txt_showMsg_txt));
                txt_showMsg_txt0 = txt_showMsg_txt;
            }
            if (!input_rate_txt.equals(input_rate_txt0)) {
                Platform.runLater(() -> input_rate.setText(input_rate_txt));
                input_rate_txt0 = input_rate_txt;
            }
            if (!lable_result_txt.equals(lable_result_txt0)) {
                Platform.runLater(() -> {
                    lable_result.setText(lable_result_txt);
                    lable_result.setTextFill(lable_result_value);
                });
                lable_result_txt0 = lable_result_txt;
            }
//			Debug.sleep(300);
        }
    }

    public class ListShowThread extends Thread {
    	public ListShowThread() {
    		super.setName("ListShowThread");
    	}
        @Override
        public void run() {
            while (true) {
                Listlock.lock();
                try {
                    ListShow();
                } finally {
                	Listlock.unlock();
                }

            }
        }
    }


    private void ListShow() {
        // Object[] = {type,data,rowNum}
        // ���µ�testDetailList����ʹ��Platform.runLater����Ϊ�ᵼ�´������
        Object[] obj = pop();
        int row = (int) obj[2];
        Boolean notSum = true;
        // ����������ϸ��Ϣ
        if (obj[0].equals("testDetailList")) {
            ProduceCaseResult p1 = (ProduceCaseResult) obj[1];
            Util698.log(TerminalParameterController.class.getName(), "ListShow.testDetailList "+p1+" "+p1.getADDR()+"."+p1.getCaseno(), Debug.LOG_INFO);

            if (row >= 0){
                Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-b p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-����"+p1.getSendtime()+"-����"+p1.getRecvtime()+"-���:"+p1.getResult(), Debug.LOG_INFO);
                for (int i = 0 ;i<testDetailList.size();i++){
                	ProduceCaseResult dataInList = testDetailList.get(i);
                    if (dataInList.getADDR().equals(p1.getADDR()) && dataInList.getCaseno()==p1.getCaseno() ) {
                    	if (!dataInList.getResult().equals("") && p1.getResult().equals("") ){
//                    	if (dataInList.getResult().equals("�ɹ�")){
                    		// xuky 2018.07.24 �Ѿ��ɹ��Ͳ�Ҫ������
                    		// ����һ�ֿ��ܣ��Ѿ���ʱ����ʧ���ˣ����������ֵ��������
                            Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-stop p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-����"+p1.getSendtime()+"-����"+p1.getRecvtime()+"-row="+i+"-���:"+p1.getResult(), Debug.LOG_INFO);
                            p1 = dataInList;
                            Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-stop dataInList"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-����"+p1.getSendtime()+"-����"+p1.getRecvtime()+"-row="+i+"-���:"+p1.getResult(), Debug.LOG_INFO);
                    	}
                    	else{
                            testDetailList.set(i, p1);
                            Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-e p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-����"+p1.getSendtime()+"-����"+p1.getRecvtime()+"-row="+i+"-���:"+p1.getResult(), Debug.LOG_INFO);
                    	}
                        break;
                    }
                }
            }
            else if (row == -1){
//                Util698.log(TerminalParameterController.class.getName(), "xuky add Detail row:"+row+" p1"+p1.getName()+p1.getADDR()+"-"+p1.getSendtime(), Debug.LOG_INFO);
                Util698.log(TerminalParameterController.class.getName(), "xuky add Detail p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-����"+p1.getSendtime()+"-����"+p1.getRecvtime()+"-���:"+p1.getResult(), Debug.LOG_INFO);
                testDetailList.add(p1);
            }
            else{
                testDetailList.remove(p1);
                Util698.log(TerminalParameterController.class.getName(), "xuky remove Detail p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-����"+p1.getSendtime()+"-����"+p1.getRecvtime()+"-���:"+p1.getResult(), Debug.LOG_INFO);
            }
        }
        if (obj[0].equals("observableList_param")) {
            if (row >= 0)
                observableList_param.set(row, (ProduceParam) obj[1]);
            else if (row == -1)
                observableList_param.add((ProduceParam) obj[1]);
            else
                observableList_param.remove((ProduceParam) obj[1]);
        }

        // ���Խ��������Ϣ
        if (obj[0].equals("observableList_record")) {
            if (row >= 0)
                observableList_record.set(row, (ProduceRecord) obj[1]);
            else if (row == -1) {
                // ��������
                ProduceRecord p1 = (ProduceRecord) obj[1];
                observableList_record.add(p1);
//                Util698.log(TerminalParameterController.class.getName(), "xuky add Sum row:"+row+" p1"+p1.getAddr(), Debug.LOG_INFO);

                ProduceRecord produceRecord1 = (ProduceRecord) obj[1];

                // ȷ������ʱ�������ݿ�������ݣ�ֻ���ڲ��Խ���ʱ�������
                // �޷����ʧ��ʱ���������ֵ������ظ�����
                if (!produceRecord1.getEndTime().equals("")){
                    Boolean isExitAndEnd = false;
                    for (ProduceRecord produceRecord : observableList_record){
                        if (produceRecord.getAddr().equals(produceRecord1.getAddr())) {
                            if (!produceRecord.getEndTime().equals("")){
                                isExitAndEnd = true;
                                break;
                            }
                        }
                    }
                    if (isExitAndEnd)
                        iBaseDao_ProduceRecord.create(produceRecord1);
                }

                // �����жϣ��ж��Ƿ����
                Boolean reOperate = true;
                for (ProduceRecord produceRecord : observableList_record){
                    if (produceRecord.getEndTime().equals("")) {
                        // ��ʾִ��δ���
                        reOperate = false;
                        break;
                    }
                }
                if (reOperate && notSum){
                    notSum = false;
                    Util698.log(TerminalParameterController.class.getName(), "6SumAndSendOK...", Debug.LOG_INFO);
                    SumAndSendOK();
                }
            } else{
                // ���ִ����remove����ʾ���½���һ�ֲ���
                notSum = true;
                ProduceRecord p1 = (ProduceRecord) obj[1];
//				Boolean removeResult = observableList_record.remove(p1);
                // ���ܳ��ָ��ݶ���remove(p1)ʧ�ܵ����
                for (ProduceRecord produceRecord : observableList_record){
                    if (produceRecord.getAddr().equals(p1.getAddr())) {
                        Boolean removeResult = observableList_record.remove(produceRecord);
//                        Util698.log(TerminalParameterController.class.getName(), "xuky remove Sum result:"+removeResult+" p1"+p1.getAddr(), Debug.LOG_INFO);
                        break;
                    }
                }
            }
        }

        // xuky 2018.06.01 ��Ϊpop�������ģ������������������sleep
        // Debug.sleep(300);
    }


    public class RefreshDataThread extends Thread {
    	public RefreshDataThread() {
    		super.setName("RefreshDataThread");
    	}
        @Override
        public void run() {
            SoftParameter.getInstance().refreshDataFromDB();
            DealSendData.getInstance();
            DealSendBlockData.getInstance();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    	// xuky 2018.08.02 ����̵߳����ƣ����ڳ�������ʱ���ж�λ
        pool.submit(new RefreshDataThread());

        // �����Ϣ
        // xuky  2018.07.09 �и����ʣ��󲿷ֵ���Ϣ��Publisher���ݵģ������ֻ��ע����UI����յ�
        // ���ϸ����ʵĽ��ͣ��ο�DealData Line136   if (frameType.equals("PLC"))������  ���⴦��ʹ��Publisher UI
//        Publisher.getInstance().addObserver(this);

        String subname = "", plandID = SoftParameter.getInstance().getParamValByKey("PLANID");
        if (plandID.equals("----")) {
            javafxutil.f_alert_informationDialog("������ʾ", "���������в�����������ά����");
            return;
        }

        IBaseDao<ProduceCaseSub> iBaseDao_ProduceCaseSub = new ProduceCaseSubDaoImpl();
        // xuky 2017.07.28 ע�⣬��Ҫʹ��no���й���
        List<ProduceCaseSub> produceCaseSubs = iBaseDao_ProduceCaseSub.retrieve(
                "where no='" + plandID + "'  and computer='" + SoftParameter.getInstance().getPCID() + "'", "");
        ProduceCaseSub produceCaseSub = null;
        if (produceCaseSubs != null)
            if (produceCaseSubs.size() > 0)
                produceCaseSub = produceCaseSubs.get(0);
        if (produceCaseSub != null){
            subname = produceCaseSub.getName();
            String note1 = produceCaseSub.getNote1();
            if (note1 == null) note1 = "";
            subname += note1;
        }

        // xuky 2017.07.26 �ͻ�������ʾ�ķ�����Ϣ����
        String msg = "��plandID��" + plandID + " ��subname��" + subname;
        Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);

        txt_showTitle.setText("��������" + subname + "�������ˡ�" + SoftParameter.getInstance().getUserManager().getUsername());

        String[] detail_colNames = { "ͨ�ŵ�ַ-����", "����", "���", "���ʹ���", "����-����", "����-����", "����-����", "����ʱ��", "����ʱ��", "��ǰ��ʱ",
                "��ʱ�ȴ�", "���Դ���", "�˿�" };
        String detail_export_columns = "ADDR,name,result,sendtimes,send,recv,expect,sendtime,recvtime,delaytime,waittime,retrys,port";

        // �б�չʾ���ֶ���Ϣ ֻ��һ��������
        String[] table_colNames = { "ID[0]", "��ַ[100]", "����", "����[40]", "���[40]", "����ʱ��[170]", "����ʱ��[170]", "����",
                "�˿�[40]", "��ǰ��ʱ[60]", "��ʱ�ȴ�[60]", "���Դ���[60]" };
        String table_columns = "ID,ADDR,name,sendtimes,result,sendtime,recvtime,send,port,delaytime,waittime,retrys";

        testDetail_crud = new ObjectCURD<ProduceCaseResult>(new ProduceCaseDaoImpl(),
                new TransImplObject(detail_export_columns, new ProduceCaseResult()), detail_colNames,
                detail_export_columns, table_colNames, table_columns, "getSubid,getName", " where subid='999'");
        // ScrollPane sp = new ScrollPane();
        // sp.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        // sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        // Label label1 = new Label();
        // sp.setContent(label1);
        // label1.setText("1"+"\r\n"+"2");
        // tableview_testDetail.setCenter(sp);

        tableview_testDetail.setCenter(testDetail_crud);
        testDetailList = testDetail_crud.getData_objs();
        // ����ʾCURD�İ�ť��������
        testDetail_crud.setButtonsVisible(false);

        // ------------------------------------------------------
        // String[] detail_colNames_param = { "����", "key", "val", "����", "˵��",
        // "��ʾ����" };
        // detail_export_columns = "name,keyname,value,type,note1,note2";
        String[] detail_colNames_param = { "����" };
        detail_export_columns = "value";
        String[] table_colNames_param = { "ID[0]", "����", "����[220]", "��ע" };
        table_columns = "ID,name,value,note1";

        object_crud_param = new ObjectCURD<ProduceParam>(new ProduceParamDaoImpl(),
                new TransImplObject(detail_export_columns, new ProduceParam()), detail_colNames_param,
                detail_export_columns, table_colNames_param, table_columns, "getKeyname",
                "where computer='" + SoftParameter.getInstance().getPCID() + "' and note2='1'", "order by type desc");
        tableview_param.setCenter(object_crud_param);

        observableList_param = object_crud_param.getData_objs();
        for (ProduceParam produceParam : observableList_param) {
            String note = produceParam.getNote1();
            if (!note.equals("")) {
                produceParam.setValue(DealTypeCode.getVal(note, produceParam.getValue()));
            }
        }

        // object_crud_param.setNewObject(new ProduceParam());
        // object_crud_param.setExportFileName("������в�����������.xls");

        object_crud_param.setButtonsVisible(false);
        // object_crud_param.setActionListener(actionListener);

        // xuky 2018.03.01 ��ʾ����Ĳ��Խ�����Ϣ
        String[] detail_colNames_begin_end = { "��ʼʱ��", "��ַ", "����ʱ��", "���Խ��", "��־" };
        detail_export_columns = "opTime,addr,endTime,opResult,beginOpt";
        String[] table_colNames_begin_end = { "ID[0]", "��ַ[100]", "��ʼʱ��", "����ʱ��", "���Խ��", "��־" };
        table_columns = "ID,addr,opTime,endTime,opResult,beginOpt";

        int colNumPerRow = 2;
        object_crud_begin_end = new ObjectCURD<ProduceRecord>(new ProduceRecordDaoImpl(),
                new TransImplObject(detail_export_columns, new ProduceRecord()), detail_colNames_begin_end,
                detail_export_columns, table_colNames_begin_end, table_columns, "getAddr,getOpTime", "where 1=2", "",
                colNumPerRow);
        tableview_begin_end.setCenter(object_crud_begin_end);

        observableList_record = object_crud_begin_end.getData_objs();

        object_crud_begin_end.setButtonsVisible(false);
        // object_crud_begin_end.setDetailVisible(false);

        // ------------------------------------------------------

        // ¼���Ĭ�ϻ�ý���
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                input_produceBarCode.requestFocus();
            }
        });

        {
            DealSendData.getInstance();
            DealSendBlockData.getInstance();

            // xuky 2017.06.15 ����ǹ���ײ��Դ��룺���Զ������豸��״̬��Ϣ
            input_produceBarCode.setOnAction((event) -> {
                if (input_comm.getText().equals("0"))
                    getBarCode(1);
                else
                    getBarCode();
            });

            input_produceBarCode1.setOnAction((event) -> {
                // ����豸ͬʱ���в���
                getBarCode(2);
            });

            input_produceBarCode2.setOnAction((event) -> {
                // ����豸ͬʱ���в���
                getBarCode(3);
            });

            input_produceBarCode3.setOnAction((event) -> {
                // ����豸ͬʱ���в���
                getBarCode(4);
            });
            input_produceBarCode4.setOnAction((event) -> {
                // ����豸ͬʱ���в���
                getBarCode(5);
            });
            input_produceBarCode5.setOnAction((event) -> {
                // ����豸ͬʱ���в���
                getBarCode(6);
            });
        }

        txt_showMsg.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        txt_showMsg.setTextFill(Color.web("#0076a3"));

        txt_showTitle.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 15));
        txt_showTitle.setTextFill(Color.web("#0076a3"));

        // lable_result.setText("����ͨ��");
        lable_result.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));

    }

    // xuky 2018.04.19 ��Ϊ���´����й��ڽ�������ģ����Ա������Platform.runLater��
    public void getBarCode(int num) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getBarCode_new(num, 0);
            }
        });
    }

    public void getBarCode_new(int num, int flag) {


        // xuky 2018.03.06 �������ִ�оͲ�Ҫִ����
        if (ISBUSY){
            Util698.log(TerminalParameterController.class.getName(), "getBarCode_new ISBUSY return ", Debug.LOG_INFO);
            return;
        }

        lableResultSet("���ڲ���...", Color.web("#9ed048"));

        ISBUSY = true;
        BUSY_B_time = DateTimeFun.getDateTimeSSS();
        Util698.log(TerminalParameterController.class.getName(), "����ISBUSY = true andBUSY_B_time="+BUSY_B_time, Debug.LOG_INFO);

        CREATE_SUM = false;

        // for (ProduceRecord p:observableList_record ){
        // objPool2.returnObject(p);
        // }
        // Util698.ListReMoveAll(observableList_record);
        // Util698.ListReMoveAll(dataListFrame);
        // observableList_record.clear();
        // dataListFrame.clear();

         Util698.ListReMoveAll(observableList_record);
         Util698.ListReMoveAll(testDetailList);
         Util698.ListReMoveAll(observableList_param);
        // xuky 2018.04.19 ϣ��ͨ�������ķ�ʽ�ͷ��ڴ�
        object_crud_begin_end.refreshTableData();
        observableList_record = object_crud_begin_end.getData_objs();

        testDetail_crud.refreshTableData();
        testDetailList = testDetail_crud.getData_objs();

        object_crud_param.refreshTableData();
        observableList_param = object_crud_param.getData_objs();

        DealSendData.getInstance();
        DealSendBlockData.getInstance();

        // xuky 2018.03.23 ������¼�ϴε����ʱ��
        MULTI_END0 = MULTI_END;

        MULTI_BEGIN = Util698.getDateTimeSSS_new();
        OK_NUM = 0;
        // SEND_NUM = 0;
        // RECV_NUM = 0;

        addr = input_produceBarCode.getText();
        addr = Util698.getAddrByBarcode(addr);
        // Util698.log("Ч�ʷ���", "���ݿ⽻��1 begin" , Debug.LOG_INFO);
        // Util698.log("Ч�ʷ���", "���ݿ⽻��1 end" , Debug.LOG_INFO);
        addNewRecord(addr);
        // SEND_NUM++;

        // xuky 2018.04.24 ��ʼ������־
        DealSendBlockLock.getInstance().init();

        TestBegin(addr, 1);

        if (num == 1)
            return;

        // xuky 2018.02.07 ��·֮ǰ���2.5��ļ���� +1�룬��Ϊ����������ʱ
        // ÿһ·�Ĳ��ԣ�����ִ�еľ����ز������ز��������2.5����໥֮��Ӱ����ʧ
        // �ϸ������Ͻ���Ӧ����ǰһ·�ز�����������ʱ2.5������ ��Ϊǰ����ز�������������Ҫ������Ե�

        // xuky 2018.02.08 �������飬�����ز�ͨ�ŵ�Ӱ�첻ֹ2.5�룬���ӵĳ�ʱ��
        // �޸�Ϊ�Ƚ��к���ͨ�ţ�Ȼ���ٽ����ز�ͨ�� ע�⣡���� ��Ҫ�޸�plc_in_front = false
        boolean plc_in_front = false;

        if (plc_in_front)
            Debug.sleep(3500);
        addr = input_produceBarCode1.getText();
        addr = Util698.getAddrByBarcode(addr);
        addNewRecord(addr);
        // SEND_NUM++;
        // new DealOperateMuti().Start(addr, 2);
        TestBegin(addr, 2);

        if (num == 2)
            return;

        if (plc_in_front)
            Debug.sleep(3500 * 2);
        addr = input_produceBarCode2.getText();
        addr = Util698.getAddrByBarcode(addr);
        addNewRecord(addr);
        // SEND_NUM++;
        // new DealOperateMuti().Start(addr, 3);
        TestBegin(addr, 3);

        if (num == 3)
            return;

        if (plc_in_front)
            Debug.sleep(3500 * 3);
        addr = input_produceBarCode3.getText();
        addr = Util698.getAddrByBarcode(addr);
        addNewRecord(addr);
        // SEND_NUM++;
        // new DealOperateMuti().Start(addr, 4);
        TestBegin(addr, 4);
        if (num == 4)
            return;

        if (plc_in_front)
            Debug.sleep(3500 * 3);
        addr = input_produceBarCode4.getText();
        addr = Util698.getAddrByBarcode(addr); // ���ݿ⽻��1
        addNewRecord(addr);
        // SEND_NUM++;
        // new DealOperateMuti().Start(addr, 5);
        TestBegin(addr, 5);
        if (num == 5)
            return;

        if (plc_in_front)
            Debug.sleep(3500 * 3);
        addr = input_produceBarCode5.getText();
        addr = Util698.getAddrByBarcode(addr);
        addNewRecord(addr);
        // SEND_NUM++;
        // new DealOperateMuti().Start(addr, 6);
        TestBegin(addr, 6);
    }

    private void TestBegin(String addr1, int numOfAll) {
    	// xuky 2018.08.02 ����̵߳����ƣ����ڳ�������ʱ���ж�λ
        pool.submit(new DealOperateMutiThread(addr1,numOfAll));
    }

    public class DealOperateMutiThread extends Thread {
    	String addr1;
    	int numOfAll;
    	public DealOperateMutiThread(String addr1,int numOfAll) {
    		this.addr1 = addr1;
    		this.numOfAll = numOfAll;
    		super.setName("DealOperateMutiThread"+addr1+"."+numOfAll);
    	}
        @Override
        public void run() {
            // xuky 2018.04.13 ������Ĵ�����д���ʹ�ö���ؼ��������ڼ��ٶ��ڴ��ռ��
            Boolean isPool = true;
            if (isPool) {
                ObjectPoolDealOperateMuti objPool = ObjectPoolDealOperateMuti.getInstance();
                DealOperateMuti obj = (DealOperateMuti) objPool.getObject();

                // xuky 2018.04.24 ���ÿ���豸���� ÿ������ֻ��һ����
                DealSendBlockLock.getInstance().addAddr(addr1);

                obj.Start(addr1, numOfAll);
                objPool.returnObject(obj);
            } else {
                new DealOperateMuti().Start(addr, 1);
            }
        }
    }


    private synchronized void addNewRecord(String addr) {

        // if (isPool){

        // RECORD_ADD = (ProduceRecord)objPool2.getObject();
        // }
        // else{
        RECORD_ADD = new ProduceRecord();
        // }
        RECORD_ADD.init();
        RECORD_ADD.setAddr(addr);
        RECORD_ADD.setOpTime(Util698.getDateTimeSSS_new());
        RECORD_ADD.setEndTime("");
        RECORD_ADD.setBeginOpt(MULTI_BEGIN);
        RECORD_ADD.setOpResult("������");
        // observableList_record.add(RECORD_ADD);

        Object[] obj = new Object[] { "observableList_record", RECORD_ADD, -1 };
        push(obj);

    }

    private synchronized void getBarCode() {

        observableList_record.clear();
        // Util698.ListReMoveAll(observableList_record);

        String barCode = input_produceBarCode.getText();
        if (barCode.equals("")) {
            Util698.log(TerminalParameterController.class.getName(), "ɨ������Ϊ�գ���return" + barCode, Debug.LOG_INFO);
            return;
        }
        Util698.log(TerminalParameterController.class.getName(), "ɨ������-" + barCode, Debug.LOG_INFO);

        if (ISBUSY) {
            Util698.log(TerminalParameterController.class.getName(), "���ڲ����豸-" + ADDR + "����return", Debug.LOG_INFO);

            lable_result.setText("���ڲ���...");
            lableResultSet("���ڲ���...", Color.web("#9ed048"));

            // input_produceBarCode.setText("���ڲ���...");
            // input_produceBarCode.selectAll();
            return;
        }

        ISBUSY = true;
        BUSY_B_time = DateTimeFun.getDateTimeSSS();
        Util698.log(TerminalParameterController.class.getName(), "����ISBUSY = true and BUSY_B_time="+BUSY_B_time, Debug.LOG_INFO);
        // System.out.println(" isBusy = true1 ");

        try {
            if (barCode.equals(SoftParameter.getInstance().getParamValByKey("ERRBARCODE"))) {
                DealOperate.getInstance().setErr();
                ISBUSY = false;
                Util698.log(TerminalParameterController.class.getName(), "ERRBARCODE", Debug.LOG_INFO);
            } else if (barCode.equals(SoftParameter.getInstance().getParamValByKey("RATEBARCODE"))) {
                // xuky 2017.08.01 ����ɨ����Ϣ������ͨ�����ʵ��л�
                changePortRate();
                Util698.log(TerminalParameterController.class.getName(), "changePortRate", Debug.LOG_INFO);
            } else
                dealAddrCode();
            input_produceBarCode.setText("");
        } catch (Exception e) {
            Util698.log(TerminalParameterController.class.getName(), "getBarCode Exception " + e.getMessage(),
                    Debug.LOG_INFO);
            ISBUSY = false;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // xuky ע�⣺update�����н������try catch ��������ֹ�����쳣�����º����Ĵ��ڽ��ղ��������Ϣ
//        Platform.runLater(() -> {
            try {
                Object[] s = (Object[]) arg;
                if (s[0].equals("DealOperate")) {
                    // ��ʾִ�н�����Ϣ
                    showDealOperateData(arg);
                }
                if (s[0].equals("DealTestCase")) {
                    // ��ʾִ�еĲ�����������
                    showDealTestData(arg);
                }
                if (s[0].equals("recv frame")) {
                    // ��ʾִ�еĲ�����������
                    // showDealTestData(arg);
                    // System.out.println("SocketServer 2018.04.12 recv=>" +
                    // arg);
                	if (s[4].equals("PLC"))
                    	dealPLCData((String) s[2]);
                	else
                		dealApplyADDR(s);
                    //
                }
            } catch (Exception e) {
    			Util698.log(TerminalParameterController.class.getName(), "update Exception:"+e.getMessage(), Debug.LOG_INFO);
            }
//        });
    }
    private void dealApplyADDR(Object[] s) {
//    	System.out.println(s[5]);
		String reply = "68 21 00 C3 05 13 30 04 22 17 20 10 7D A0 85 01 01 40 01 02 00 01 09 06 20 17 22 04 30 13 00 00 4E EB 16";
		SocketServerEast.sendData(reply, (String)s[3]);

	}

	private void dealPLCData(String s) {
    	dealPLCData(s,true);
    }

    private void dealPLCData(String s,Boolean needReply) {
        String errcode = "";
        String step = "1";
        try {

        	if (s.equals("13869881856")){
                Util698.log(TerminalParameterController.class.getName(), "�յ���֤Observer����", Debug.LOG_INFO);
                SoftParameter.getInstance().setObserverOK("168");
        		return;
        	}

            if (s.toUpperCase().endsWith("0D"))
                s = s.substring(0, s.length() - 2);
            FramePLC framePLC = new FramePLC(s);
            Util698.log(TerminalParameterController.class.getName(), "�յ��������ģ�" + s +" framePLC.getControlData:"+framePLC.getControlData(), Debug.LOG_INFO);
            if (framePLC.getControlData().equals("01")) {

            	// xuky 2018.07.25 �ڲ���ǰ��¼��Ҫ���Եı������ݡ��ڲ������ʱ��գ�����м�����쳣��Ҫ��������������ձ������ݽ��в���
	            SoftParameter.getInstance().setPLCFRAME(s);
	            SoftParameter.getInstance().saveParam();

            	// ���յ����������Ľ��м�¼����Ϊ������Ҫ���²���
            	step = "2";
            	PLCFrame = s;
                // xuky 2018.06.01 ����ǰ�������
                testDetailList.clear();

                step = "3";
                String[] addrs = framePLC.getDEVADDR();
                errcode = "91";
                // ��ʾ����������Ϣ�����Ե��ǵ�ַ��Ϣ

                step = "4";
                input_produceBarCode.setText(addrs[0]);
                input_produceBarCode1.setText(addrs[1]);
                input_produceBarCode2.setText(addrs[2]);
                input_produceBarCode3.setText(addrs[3]);
                input_produceBarCode4.setText(addrs[4]);
                input_produceBarCode5.setText(addrs[5]);
                NO_ADDR = null;
                step = "5";
                NO_ADDR = new LinkedHashMap<String, Object>();
                for (int i = 0; i < 6; i++) {
                    NO_ADDR.put(DataConvert.int2String(i + 1), Util698.getAddrByBarcode(addrs[i]));
                }
                step = "6";
                // ����Ƿ����쳣����������������ִ�еĲ��ԣ���������лظ�PLC
                if (needReply){
                	step = "7";
                    String senddata = "68 01 01 68 81 53 16";
                    Util698.log(TerminalParameterController.class.getName(), "�������ԣ��������ݻظ���" + senddata, Debug.LOG_INFO);
                    SocketServerEast.sendSocketData(senddata);
                }

                step = "8";
                if (!addrs[5].equals(""))
                    getBarCode(6);
                else if (!addrs[4].equals(""))
                    getBarCode(5);
                else if (!addrs[3].equals(""))
                    getBarCode(4);
                else if (!addrs[2].equals(""))
                    getBarCode(3);
                else if (!addrs[1].equals(""))
                    getBarCode(2);
                else if (!addrs[0].equals(""))
                    getBarCode(1);
            }
            step = "9";

            if (framePLC.getControlData().equals("83")) {
                // ����յ����������ݾ�ֹͣ�ط��ظ�����
                Send_time = Send_repeateTime;
            }
            if (framePLC.getControlData().equals("82")) {
                // ����յ����������ݾ�ֹͣ�ط��ظ�����
                Send_time1 = Send_repeateTime1;
            }
            framePLC = null;
        } catch (Exception e) {
        	e.printStackTrace();
            Util698.log(TerminalParameterController.class.getName(), "dealPLCData Exception " + e.getMessage() +"step:"+ step,
                    Debug.LOG_INFO);
            Debug.sleep(1000);
            dealPLCData(s,needReply);
//            // �������ݻظ� xuky 2018.07.23 ���´��뺬�岻���
//            if (errcode.equals("91")) {
//                String data = "01";
//                FramePLC framePLC = new FramePLC(errcode, data);
//                SocketServerEast.sendSocketData(framePLC.getFrame());
//                framePLC = null;
//            }
        }

    }

    private synchronized void showDealTestData(Object msg) {

        if (RUNFASTER)
            return;

        Object[] s = (Object[]) msg;
        String type = (String) s[1]; // new old
        // int ID = DataConvert.String2Int((String) s[2]);
        Object object = s[2];

        // xuky 2017.08.16 ��Ҫ����produceCaseResult�е�����
        ProduceCaseResult produceCaseResult = (ProduceCaseResult) object;

        String recv = produceCaseResult.getRecv();
        // ֻ�ڵ������ʱ���д���Ҫ
        if (MULTI_BEGIN.equals("") && recv.indexOf("68") >= 0) {
            recv = recv.substring(recv.indexOf("68"), recv.length());
            recv = recv.replaceAll(" ", "");
            // 680170010000006891243433B337837F768064696966927C7C5BA96464746563655C92646A6367656353535353531116
            if ((recv.length() > 28) && (recv.substring(20, 28).equals("3433B337"))) {
                // observableList_param

                String str = recv.substring(28, recv.length() - 4);
                str = DataConvert.HexStrReduce33H(str);
                str = DataConvert.asciiHex2String(str);
                for (ProduceParam produceParam : observableList_param) {
                    if (produceParam.getName().equals("�豸���ذ汾��Ϣ")) {
                        // xuky 2017.08.17 ֮ǰ���Խ������Ӻ��޸ģ���Ч��ʹ����ɾ�������ӵķ�ʽ����
                        Object[] obj = new Object[] { "observableList_param", produceParam, -100 };
                        push(obj);
                        // observableList_param.remove(produceParam);
                        break;
                    }
                }
                ProduceParam needChange = new ProduceParam();
                needChange.setName("�豸���ذ汾��Ϣ");
                needChange.setValue(str);

                Object[] obj = new Object[] { "observableList_param", needChange, -1 };
                push(obj);

            }
        }

        // ���������������ǱȽϺ�ʱ�ģ��������ѹջ�ͳ�ջ����
        // testDetailList.add
        // testDetailList.set

        if (type.equals("new")) {
            // ProduceCaseResult produceCaseResult =
            // iBaseDao_ProduceCaseResult.retrieve(ID);
        	Util698.log(TerminalParameterController.class.getName(), "showDealTestData.new "+produceCaseResult, Debug.LOG_INFO);
            // testDetailList.add(produceCaseResult);
            Object[] obj = new Object[] { "testDetailList", produceCaseResult, -1 };
            push(obj);
            // xuky 2018.07.04 ��Ҫ������ͷ���������ݣ���Ӱ������ʹ��
//			produceCaseResult = null;

        }
        if (type.equals("old")) {
            // xuky 2017.08.25 ��Ϊ�����ж���豸��ͬʱ���в���
//			for (int i = 0; i < testDetailList.size(); i++) {
//				ProduceCaseResult p = testDetailList.get(i);
//				if (p.getADDR().equals(produceCaseResult.getADDR()) && p.getCaseno() == produceCaseResult.getCaseno()) {
//					// testDetailList.set(i, produceCaseResult);
//					Object[] obj = new Object[] { "testDetailList", produceCaseResult, i };
//					push(obj);
//					break;
//				}
//			}
            Object[] obj = new Object[] { "testDetailList", produceCaseResult, 123 };
        	Util698.log(TerminalParameterController.class.getName(), "showDealTestData.old "+produceCaseResult+" "+produceCaseResult.getADDR()+"."+produceCaseResult.getCaseno(), Debug.LOG_INFO);
            push(obj);
            String flag = "";
            if (s.length >= 4)
                flag = (String) s[3];
            else
                flag = "δ�����Դ";
//            Util698.log(TerminalParameterController.class.getName(), "xuky push Detail "+produceCaseResult.getName()+produceCaseResult.getADDR()+"-"+produceCaseResult.getSendtime()+"-"+flag, Debug.LOG_INFO);
//			produceCaseResult = null;
        }
    }

    private synchronized void showDealOperateData(Object msg) {

        String[] s = (String[]) msg;
        String msg1 = s[2];

        if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
            // txt_showMsg.setText(msg1);

            txt_showMsg_txt = msg1;
            // ObjectPoolDrawUIService objPool =
            // ObjectPoolDrawUIService.getInstance();
            // DrawUIService obj = (DrawUIService)objPool.getObject();
            // obj.init(new Object[]{msg1}, new EventHandler<WorkerStateEvent>()
            // {
            // @Override
            // public void handle(WorkerStateEvent t) {
            // txt_showMsg.setText((String)
            // ((Object[])t.getSource().getValue())[0]);
            // objPool.returnObject(obj);
            // }
            // });
            // obj.restart(); // ��Ϊ�Ǵ�pool�л�ȡ�������Ѿ�ִ����ϣ�����restart
        }

        String addr = "", result = "";
        Boolean finished = false;
        Boolean needShow = true;
        if (msg1.indexOf("���Գɹ�") >= 0) {
            addr = msg1.substring(msg1.indexOf("�豸") + 2, msg1.indexOf("����")).trim();
            finished = true;
            result = "���Գɹ�";

//			Util698.log(TerminalParameterController.class.getName(), "���Խ��:"+addr+result, Debug.LOG_INFO);

            OK_NUM++;
            if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
                // "�豸000000098064 ���Գɹ���"
                // "��2���豸000000098063 ���Գɹ���"

                lableResultSet(result, Color.web("#0c8918"));

                // xuky 2017.07.25 ���Գɹ���������Ϣ��¼����ֹ���ظ����в���
                SoftParameter.getInstance().setOKADDR(ADDR);
                SoftParameter.getInstance().saveParam();
            }
        }
        if (msg1.indexOf("����ʧ��") >= 0) {
            // System.out.println(" TerminalParameterController ����ʧ��");

            finished = true;
            result = "����ʧ��";
            // System.out.println("xuky 2018.04.12-1 ʧ��1");
            addr = msg1.substring(msg1.indexOf("�豸") + 2, msg1.indexOf("����")).trim();

//			Util698.log(TerminalParameterController.class.getName(), "���Խ��:"+addr+result, Debug.LOG_INFO);

            if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
                lableResultSet(result, Color.web("#ff2121"));
            }

        }

        if (msg1.indexOf("����Ϊ�豸����״̬") >= 0) {
            needShow = false;
            // System.out.println(" TerminalParameterController ����Ϊ�豸����״̬");

            finished = true;
            result = "����ʧ��";
            // System.out.println("xuky 2018.04.12-1 ����1");

            addr = msg1.substring(msg1.indexOf("�豸") + 2, msg1.indexOf("����Ϊ�豸����״̬")).trim();

//			Util698.log(TerminalParameterController.class.getName(), "���Խ��:"+addr+result+"����Ϊ�豸����״̬",Debug.LOG_INFO);

            if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
                lableResultSet(result, Color.web("#ff2121"));
            }

        }

        // �������
        if (MULTI_BEGIN.equals("") && finished) {
            ISBUSY = false;
        }

        // xuky 2018.03.06 ͨ��MULTI_BEGIN�ж��Ƿ�Ϊ����ͬʱ����
        if (!MULTI_BEGIN.equals("") && finished ) {
            if (!addr.equals("")) {
                // xuky 2018.03.02 ��ӽ�����Ϣ
                // msg1 = "�豸000000098064 ���Գɹ���"
                // ��ȡ��ַ��Ϣ���޸�����

                // ���²��Խ����Ϣ
                for (ProduceRecord produceRecord : observableList_record) {
                    if (produceRecord.getAddr().equals(addr)) {
                        if (!produceRecord.getEndTime().equals(""))
                            break;

                        // ɾ������ǰ���Ƚ��ж������ݵı���
                        ProduceRecord newProduceRecord = new ProduceRecord();
                        newProduceRecord.init();
                        Util698.objClone(produceRecord, newProduceRecord, "");
                         Object[] obj = new
                         Object[]{"observableList_record",produceRecord,-100};
                         push(obj);

                        MULTI_END = Util698.getDateTimeSSS_new(); // �������ʱ��
                        newProduceRecord.setEndTime(MULTI_END);
                        newProduceRecord.setOpResult(result);
                        newProduceRecord.setWorkStation(PCID);
                        newProduceRecord.setOpName(OP_NAME);
                        newProduceRecord
                                .setOpUsingTime(Util698.getMilliSecondBetween_new(MULTI_END, newProduceRecord.getOpTime()));

                         Object[] obj1 = new
                         Object[]{"observableList_record",newProduceRecord,-1};
                         push(obj1);
                        break;
                    }
                }
            }


        }

        // System.out.println("DealOperate["+1+"]");
    }

    private void SumAndSendOK() {
        ISBUSY = false;
        CREATE_SUM = true;

        current_time++;
        Util698.log(TerminalParameterController.class.getName(), "����ִ�д�����"+current_time + " ����������޴���"+MAX, Debug.LOG_INFO);

        input_rate_txt = DataConvert.int2String(current_time);

        Boolean rest = true;
        if (!rest) {
            current_time = 1;
            MAX = 2;
        }

        if (current_time <= MAX) {
            // ��Ӳ��Ի�������
            AddSumData();
            // ��֯��Ҫ��PLC����ظ��ı���
            BuildReply4PLC();

            // ֻ�в�����ϣ������ô�PLCFRAME��ϢΪ�գ������Զ��������Զ�ִ�в��Թ���
            SoftParameter.getInstance().setPLCFRAME("");
            SoftParameter.getInstance().saveParam();
            Util698.log(TerminalParameterController.class.getName(), "������ϣ�����PLCFRAMEΪ��", Debug.LOG_INFO);
        }

        if (current_time > MAX) {
            Util698.log(TerminalParameterController.class.getName(), "current_time > MAX �Ƕ�ν���˶δ��룬�˴����账��",
                    Debug.LOG_INFO);
        }

        if (current_time == MAX) {
            // �����������Գ���
            // �����Ҫ������������Ҫ����ǰ�ϱ����Խ����ֻ�Ǽ�¼����
            // �����Ժ����ϱ����Խ��
        	if (RESET_TYPE.equals("RestApp")){
                Util698.log(TerminalParameterController.class.getName(), "current_time == MAX ׼��RestApp",
                        Debug.LOG_INFO);
        		RestApp();
        	}
        	if (RESET_TYPE.equals("RestCOMServicel")){
        		current_time = 0;
        		RestCOMServicel();
        	}
        }

        // ��Ҫ����ִ�е��Ⱥ�������Ƚ����������ڲ�����Ȼ���ٽ������ݻظ�
        if (RESET_TYPE.equals("RestApp") && current_time == MAX){
            // �������Ҫ������������������ڴ�ʱ���лظ������ǵȵ����������Ժ��ٷ��ͻظ�����
            Util698.log(TerminalParameterController.class.getName(), "current_time == MAX ���ڴ�ʱSend2PLC",
                    Debug.LOG_INFO);
        }
        else
            Send2PLC();  // ��PLC����ظ����Խ����Ϣ


    }
    private void RestCOMServicel() {
        Util698.log(TerminalParameterController.class.getName(), "��������ͨ��", Debug.LOG_INFO);
        PrefixMain.getInstance().closeSerial();
        PrefixMain.getInstance().openSerial();
    }

    private void RestApp() {
        Util698.log(TerminalParameterController.class.getName(), "���¿��������Զ�ִ�в��Թ���", Debug.LOG_INFO);
        Util698.ResetApp();
    }

    private void BuildReply4PLC() {
        Send_time = 0;
        Send_time1 = 0;
        // xuky 2018.04.12 �ظ���������Ҫ����ʵ��������б��

        // ����xuky observableList_record �е����ݽ�����֯
        // xuky 2018.04.26 ����Ҫ���͵����ݱ��浽NO_ADDR��
        String send_data = "";
        String datetime = DateTimeFun.getDateTime();
        datetime = datetime.replaceAll("-", "");
        datetime = datetime.replaceAll(":", "");
        datetime = datetime.replaceAll(" ", "");
        int i = 1;

        // xuky 2018.10.23 ���ID��Ϣ  ����������
//        NO_ADDR = new LinkedHashMap<String, Object>();
//        NO_ADDR.put(DataConvert.int2String(1), Util698.getAddrByBarcode("160000000001"));


        Iterator it = NO_ADDR.entrySet().iterator();
        while (it.hasNext()) {
            send_data = DataConvert.int2HexString(i, 2);
            Map.Entry entry = (Map.Entry) it.next();
            String addr1 = (String) entry.getValue(); // ��ַ��Ϣ
            // Object value = ; // ��������
            // ��� ��־ ��ַ���� ��ַ���� ���� ���ݳ��� ��������
            // 01 01 06 201801130013 2018.04.25 05:56:00 00
            String result1 = "";
            for (ProduceRecord produceRecord : observableList_record) {
                if (produceRecord.getAddr().equals(addr1)) {
                    result1 = produceRecord.getOpResult();
                    break;
                }
            }
            if (result1.equals("")) {
                send_data += "00";
            } else {
                if (result1.indexOf("�ɹ�") >= 0)
                    send_data += "01"; // 01�ɹ� 00(�������ݵı䳤��Ϣ)
                                        // 00��ʾ����������
                else
                    send_data += "02"; // 02ʧ�� 00(�������ݵı䳤��Ϣ)
                                        // 00��ʾ����������
                addr1 = Util698.FormatAddr(addr1, 12);

                send_data += DataConvert.int2HexString(addr1.length() / 2, 2) + addr1 + datetime; // ��ַ��Ҫ����ż��λ���ȣ�����Ϊ06
                // xuky 2018.10.23 ���ID��Ϣ
    			List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + addr1 + "'", "");
    			DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
    			String SAVEID = "";
    			if (devInfo != null) {
    				SAVEID = devInfo.getBarCode();
    			}

                if (result1.indexOf("�ɹ�") >= 0)
                	if (SAVEID.equals(""))
                		send_data += "00"; // Ŀǰ�Ĵ��ݱ䳤���ݳ���Ϊ0
                	else{
                		send_data += DataConvert.int2HexString(SAVEID.length() / 2, 2) + SAVEID;
                	}
                else
                    send_data += "00"; // Ŀǰ�Ĵ��ݱ䳤���ݳ���Ϊ0
            }
            entry.setValue(send_data);

            i++;
        }
        i = 1;
        // xuky 2018.04.26 ��NO_ADDR�л��ȡ���ݳ�����Ϣ
        send_data = "";
        it = NO_ADDR.entrySet().iterator();
        String send_data1 = "";
        while (it.hasNext()) {
            send_data += DataConvert.int2HexString(i, 2);
            Map.Entry entry = (Map.Entry) it.next();
            send_data1 += (String) entry.getValue();
            send_data += DataConvert.int2HexString(((String) entry.getValue()).length() / 2 - 2, 2);
            i++;
        }
        FramePLC framePLC = new FramePLC("03", send_data);
        send_data = framePLC.getFrame();
        framePLC = new FramePLC("02", send_data1);
        send_data1 = framePLC.getFrame();
        framePLC = null;

        SoftParameter.getInstance().setSENDPLC1(send_data);
        SoftParameter.getInstance().setSENDPLC2(send_data1);
        SoftParameter.getInstance().saveParam();
        Util698.log(TerminalParameterController.class.getName(), "BuildReply4PLCִ����ɣ�", Debug.LOG_INFO);
    }

    private void Send2PLC() {
        String send_data;
        String send_data1;
        send_data = SoftParameter.getInstance().getSENDPLC1();
        send_data1 =  SoftParameter.getInstance().getSENDPLC2();

        if (send_data == null || send_data.equals(""))
            return;

            Util698.log(TerminalParameterController.class.getName(), "��PLC���Ͳ��Խ����"+send_data+"-"+send_data1,Debug.LOG_INFO);

            while (true) {
                // ����յ��˻ظ����ģ��ͻ�����Send_time���Ӷ�ֹͣ�ط�
                if ( Send_time >= Send_repeateTime)
                    break;
                if (send_data.equals("680D0D68030100020003000400050006000216")){
                    Util698.log(TerminalParameterController.class.getName(), "SumAndSendOK������"+send_data, Debug.LOG_INFO);
                    break;
                }
                SocketServerEast.sendSocketData(send_data);
                Send_time++;
                Debug.sleep(send_interval); // ���1���������
            }
            while (true) {
                if (Send_time1 >= Send_repeateTime1)
                    break;
                if (send_data1.equals("680D0D68020100020003000400050006000116")){
                    Util698.log(TerminalParameterController.class.getName(), "SumAndSendOK������"+send_data1, Debug.LOG_INFO);
                    break;
                }
                SocketServerEast.sendSocketData(send_data1);
                Send_time1++;
                Debug.sleep(send_interval); // ���1���������
            }

            // �����Ҫ���͵�����
            SoftParameter.getInstance().setSENDPLC1("");
            SoftParameter.getInstance().setSENDPLC2("");
            SoftParameter.getInstance().saveParam();

    }

    private void AddSumData() {
        // xuky 2018.06.98 �Ѿ����߳���ִ�е�ǰ�������������������߳�ִ���ˣ��ᵼ�����ݴ洢�Ĳ�����
//			new Thread(() -> {
            // ��δʹ�ö���أ��Ľ���������������
            int sleep_time = 8000;

            // xuky 2018.03.23 �����ݿ��в���һ��������Ϣ
            ProduceRecord newProduceRecord = new ProduceRecord();
            // ProduceRecord newProduceRecord = (ProduceRecord)
            // objPool2.getObject();
            newProduceRecord.init();
            newProduceRecord.setAddr("sumData-"+MAX+"-"+DataConvert.fillWith0(DataConvert.int2String(current_time), 4));

            newProduceRecord.setBeginOpt(MULTI_BEGIN);

            // �ϴε����ʱ��Ϊ�գ������˴����״�ִ��
            Long usingTime = null;
            if (MULTI_END0.equals("")) {
                usingTime = Util698.getMilliSecondBetween_new(MULTI_END, MULTI_BEGIN);
                newProduceRecord.setOpTime(MULTI_BEGIN);
                newProduceRecord.setOpUsingTime(usingTime);
            } else {
                usingTime = Util698.getMilliSecondBetween_new(MULTI_END, MULTI_END0);
                newProduceRecord.setOpTime(MULTI_END0);
                newProduceRecord.setOpUsingTime(usingTime);
            }
            Util698.log(TerminalParameterController.class.getName(), "���Գɹ�������"+OK_NUM + " ���Ժ�ʱ��"+usingTime, Debug.LOG_INFO);
            // �ɹ�������Ϣ
            newProduceRecord.setOpResult(DataConvert.int2String(OK_NUM));
            newProduceRecord.setEndTime(MULTI_END);
            newProduceRecord.setWorkStation(PCID);
            newProduceRecord.setOpName(OP_NAME);
            CREATE_SUM = true;
            iBaseDao_ProduceRecord.create(newProduceRecord);
            Util698.log(TerminalParameterController.class.getName(), "iBaseDao_ProduceRecord.createִ����ɣ�", Debug.LOG_INFO);
            newProduceRecord = null;
    }

    private void lableResultSet(String result, Paint value) {
        // lable_result.setTextFill(Color.web("#0c8918"));
        // lable_result.setText(result);

        lable_result_txt = result;
        lable_result_value = value;
        // ObjectPoolDrawUIService objPool =
        // ObjectPoolDrawUIService.getInstance();
        // DrawUIService obj = (DrawUIService)objPool.getObject();
        // obj.init(new Object[]{result}, new EventHandler<WorkerStateEvent>() {
        // @Override
        // public void handle(WorkerStateEvent t) {
        // lable_result.setText((String)
        // ((Object[])t.getSource().getValue())[0]);
        // lable_result.setTextFill(value);
        // objPool.returnObject(obj);
        // }
        // });
        // obj.restart(); // ��Ϊ�Ǵ�pool�л�ȡ�������Ѿ�ִ����ϣ�����restart
    }

    private void dealAddrCode() {
        // 1ɨ��������Ϣ �����õ���ַ��Ϣ
        String barcode = input_produceBarCode.getText();

        if (!IS_SETID.equals("")){
        	// xuky 2019.01.26 ���IS_SETID���ݲ�Ϊ�գ���ʾ�����Ǳ�ģ�����·��ģ���ID��Ϣ���䳤�Ȳ���12λ
    		List result = iBaseDao_BarCodesInfo.retrieveBySQL(
    				"select barcode from " + BarCodesInfo.class.getName() + " where barcode='" + barcode + "'");
    		if (result == null || result.size() == 0 ){
    			// ���û�в�ѯ�����򲻼���
                String msg = "������Ϣ"+barcode+"δ�ҵ���������ɨ�裡";
                msg += "\nҲ�����������ļ�������ϵͳ����Ա�����Ų飡";
    			javafxutil.f_alert_informationDialog("������ʾ", msg);
                Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
                lable_result.setText("");
                txt_showMsg.setText(msg);
                ISBUSY = false;
                return;
    		}
        	ADDR = barcode;
        }
        else
        	ADDR = Util698.getAddrByBarcode(barcode);
        Util698.log(TerminalParameterController.class.getName(), "�豸��ַ-" + ADDR, Debug.LOG_INFO);
        // ���ڴ�����Խ��Ϊ�쳣���豸�����ܲ��������豸
        String errAddr = SoftParameter.getInstance().getERRADDR();
        if (!errAddr.equals(""))
            DealOperate.getInstance().setERRADDR(errAddr);
        String err = DealOperate.getInstance().getERRADDR();
        if (!err.equals("") && !err.equals(ADDR)) {
            String msg = "�豸" + err + " ���Գ����쳣��������������豸��";
            Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
            lable_result.setText("");
            txt_showMsg.setText(msg);
            ISBUSY = false;
            return;
        }

        // Util698.ListReMoveAll(dataListFrame);
        testDetailList.clear();

        // xuky 2017.07.25 �������ɨ�貢�����õ���������ַ����һ���ɹ��ˣ��ڶ����Ͳ�Ҫ����
        if (SoftParameter.getInstance().getOKADDR().equals(ADDR)) {
            String msg = "�豸" + ADDR + "���Գɹ��������ٴβ��ԣ�";
            Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
            lable_result.setText("");
            txt_showMsg.setText(msg);
            ISBUSY = false;

            return;
        }

        if (ADDR.length() > 12 && IS_SETID.equals("")) {
            String msg = "ɨ����պ�õ��豸��ַ" + ADDR + "�쳣��������ɨ�룡";
            Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
            lable_result.setText("");
            txt_showMsg.setText(msg);
            ISBUSY = false;
            return;
        }
        DealOperate.getInstance().Start(ADDR);

        addNewRecord(ADDR);

        lable_result.setTextFill(Color.web("#9ed048"));
        lable_result.setText("���ڲ���...");

        lableResultSet("���ڲ���...", Color.web("#9ed048"));


    }

    @FXML
    public void chanePortRateAction(ActionEvent event) {
        changePortRate();
    }

    // xuky 2017.08.01 �޸Ĵ���ͨ������
    private void changePortRate() {
        String comid = input_comm.getText();
        int rate = DataConvert.String2Int(input_rate.getText());

        // xuky 2017.08.11 ���ȹر����еĴ���
        // PrefixMain.getInstance().closeSerial();
        List<MinaSerialServer> serialServers = PrefixMain.getInstance().getSerialServers();
        for (MinaSerialServer svr : serialServers) {
            if (svr.getSerialParam().getCOMID().equals(comid)) {
                svr.getSerialParam().setBaudRate(rate);
                svr.disConnect();
                svr.getSerialParam().setBaudRate(rate);
                svr.init();
                break;
            }
        }
        // PrefixMain.getInstance().setSerial_open(true);

        if (rate == 600)
            input_rate.setText("1200");
        else if (rate == 1200)
            input_rate.setText("600");
        input_changeresult.setText("����ID" + comid + "��Ӧ�����޸Ĳ�����Ϊ" + rate);
        // �޸�ָ���˿ڵ�ͨ�����ʣ���ʱ��Ч
    }

    @FXML
    public void openPortAction(ActionEvent event) {

        // xuky 2018.02.27 �����޸ģ�ֻ�迪������
        // ����ͨ�ŷ�����
        PrefixWindow.getInstance().showFrame("ͨ�ŷ�����", 120, 510, 800, 200);


    }

}
