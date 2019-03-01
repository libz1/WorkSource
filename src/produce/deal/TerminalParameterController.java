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
    private String IS_SETID = ""; // 模块或路由设置ID

	// xuky 2018.07.16 添加线程池的方式进行线程调用
//	ExecutorService pool = Executors.newFixedThreadPool(15);

	// xuky 2018.08.02 通过 ThreadPoolExecutor的方式
	// 参考 https://www.cnblogs.com/zedosu/p/6665306.html
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    // xuky 2018.04.24 测试模式下，自动循环执行
    // xuky 2018.07.09 调整IS_AUTO的含义，意思是重启后，自动运行的状态
    private Boolean IS_AUTO = false;
    private String RESET_TYPE = "RestCOMServicel";  //  RestApp\RestCOMServicel

    // 正在进行任务处理的标志位
    private Boolean ISBUSY = false;
    // 任务开始执行时间
    private String BUSY_B_time = "";
//    private int RestCheck = 150000;  // 2.5 分钟 = 2.5 * 60 *1000 毫秒   150000
    // 判断任务执行是否异常的时间，长时间处于ISBUSY状态
    // xuky 2018.10.08 调整为240秒，是4分钟的等待时间  因为双模设备测试效率太低导致此次调整
    private int RestCheck = 80000*3;  // 80秒 = 1 * 80 *1000 毫秒   240000  4分钟
    // 记录收到的启动报文，可能用于重新发送
    private String PLCFrame = "";

    private IBaseDao<DevInfo> iBaseDao_DevInfo = null;

    int MAX = 180; // 累计自动执行次数   到达次数后自动重新启动
//  int MAX = 2; // 累计自动执行次数   到达次数后自动重新启动

    String txt_showMsg_txt = "", txt_showMsg_txt0 = "";
    String input_rate_txt = "", input_rate_txt0 = "";
    String lable_result_txt = "", lable_result_txt0 = "";
    Paint lable_result_value = null;
	IBaseDao<BarCodesInfo> iBaseDao_BarCodesInfo = new BarCodesInfoDaoImpl();

    // xuky 2018.06.01 用于testDetailList的数据维护
    // Object[] = {type,data,rowNum}
    // type 是对象类型，可以对多种类型数据进行处理
    // rowNum = -1 表示add ,rowNum != -1 表示set

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

    // xuky 2018.06.01 调整为死等模式 无需调用者进行处理
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

    // xuky 2018.05.29 修改为在text中进行数据修改 两个线程进行控制，其他线程直接更新数据
    // 定时线程 间隔特定时间，进行数据比较和写入操作 如果数据有变化则写入



    int TestNum = 4;

    @FXML
    private TextField input_produceBarCode, input_comm, input_rate, input_produceBarCode1, input_produceBarCode2,
            input_produceBarCode3, input_produceBarCode4, input_produceBarCode5;

    String ADDR;

    // xuky 2018.03.14 提高执行效率 去掉界面的展示部分
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

    // 多个同时执行的启动时间
    String MULTI_BEGIN = "", MULTI_END = "", SAVE_MSG = "", MULTI_END0 = "";
    int OK_NUM = 0;
    String OP_NAME = SoftParameter.getInstance().getUserManager().getUserid();
    String PCID = SoftParameter.getInstance().getPCID();
    // // 多个同时执行的发出和接收数据量
    // int SEND_NUM = 0;
    // int RECV_NUM = 0;


    String addr = "";

    private Boolean CREATE_SUM = false; // 标记是否进行了测试汇总
    // 回复报文的重试次数
    int Send_repeateTime = 5, Send_repeateTime1 = 5;
    // 当前已经执行的次数信息
    int Send_time = 0, Send_time1 = 0;
    int send_interval = 1000; // 发送测试结果重试时间
    // 用来记录设备地址的no信息 使用LinkedHashMap，确保数据的次序
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
			if ((p.getName().indexOf("写") >=0 || p.getName().indexOf("设置") >=0) && p.getName().indexOf("ID") >=0)
				b1 = true;
			if ((p.getName().indexOf("读") >=0 || p.getName().indexOf("查询") >=0) && p.getName().indexOf("ID") >=0)
				b2 = true;
			if (p.getName().indexOf("表") >=0 )
				b3 = true;
			if (p.getName().indexOf("路由") >=0 )
				b3 = false;
		}
		if (b1 && b2){
			if (b3)
				IS_SETID = "表模块";
			else
				IS_SETID = "路由";
		}
		SoftParameter.getInstance().setIS_SETID(IS_SETID);


    	// xuky 2018.10.25 根据参数表中的定义设置整轮测试等待时间，如果超时需要重启软件 ，重新进行测试
    	String str = SoftParameter.getInstance().getTESTALL_TIME_OUT();
    	if (str != null && !str.equals("")){
    		int init = RestCheck;
    		// 如果用户设置的数据有问题，使用默认的设定数据
    		try{
        		RestCheck = DataConvert.String2Int(SoftParameter.getInstance().getTESTALL_TIME_OUT());
    		}
    		catch (Exception e){
    			RestCheck = init;
    		}

    	}

    	iBaseDao_DevInfo = new DevInfoDaoImpl();

//    	// xuky 2018.07.18 尝试在此启动接口处理线程
//    	PLC2MESThread.getInstance();

        Util698.log(TerminalParameterController.class.getName(), "PublisherUI.getInstance().addObserver(this)",Debug.LOG_INFO);
        PublisherUI.getInstance().addObserver(this);
        PublisherShowList.getInstance().addObserver(this);


        // JavaFX2 Stage窗口最大化
        // http://blog.csdn.net/alanzyy/article/details/18249107
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        parentStage.setX(bounds.getMinX());
        parentStage.setY(bounds.getMinY());
        parentStage.setWidth(bounds.getWidth());
        parentStage.setHeight(bounds.getHeight());


        // 参考 https://blog.csdn.net/chuan_yu_chuan/article/details/53395626
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        long initialDelay = 0;
        long period = 300;
        // 每隔n秒钟执行一次job1，第2次在第一次执行完成后n秒
//		service.scheduleWithFixedDelay(new MyScheduledExecutor_list(), initialDelay, period, TimeUnit.MILLISECONDS);
        service.scheduleWithFixedDelay(new MyScheduledExecutor_txt(), initialDelay, period, TimeUnit.MILLISECONDS);

        // 如下代码不能多次执行，因为pop是阻塞式的 ，不能使用MyScheduledExecutor_list
//		class MyScheduledExecutor_list implements Runnable {
        // 参考 https://www.cnblogs.com/dyllove98/archive/2013/06/23/3151268.html

    	// xuky 2018.08.02 添加线程的名称，便于出现问题时进行定位
        pool.submit(new ListShowThread());

        if (IS_AUTO){
            int maxRetry = 1000;
            int i = 0;
            Util698.log(TerminalParameterController.class.getName(), "验证Observer是否ok"+SoftParameter.getInstance().getObserverOK(),Debug.LOG_INFO);
            SoftParameter.getInstance().setObserverOK("");
            while (i<maxRetry){
            	// 发送报文，检查观察者模式是否生效，有效后再开启发送报文过程
                Util698.log(TerminalParameterController.class.getName(), "发送验证Observer的消息",Debug.LOG_INFO);
    			String[] s = { "recv frame", "user data", "13869881856", "" };
    			// 注意需要使用PublisherUI发送
    			PublisherUI.getInstance().publish(s);
                Debug.sleep(1000);
                String flag = SoftParameter.getInstance().getObserverOK();
                if (flag.equals("168")){
                    break;
                }
                i++;
            }

            // xuky 2018.07.09 添加判断，判断是否有客户端（PLC）进行了连接

            String send_data = SoftParameter.getInstance().getSENDPLC1();

            if (send_data == null || send_data.equals("")){

            }
            else{
                Util698.log(TerminalParameterController.class.getName(), "验证PLC连接 getRECVCLINET:"+SoftParameter.getInstance().getRECVCLINET(),Debug.LOG_INFO);
                // 在JFXMain中setRECVCLINET("");
                i = 0;
                while (i<maxRetry){
                    String flag = SoftParameter.getInstance().getRECVCLINET();
                    if (flag.equals("168")){
                        Util698.log(TerminalParameterController.class.getName(), "getRECVCLINET:"+flag +" and Send2PLC",Debug.LOG_INFO);
                        // xuky 2018.07.09 需要检查是否需要回复PLC测试结果信息
                        Send2PLC();
                        break;
                    }
                    i++;
                    Debug.sleep(200);
                }
            }

            // xuky 2018.07.11 重启后如果PLCFRAME有数据，则重新测试
            PLCFrame = SoftParameter.getInstance().getPLCFRAME();
            if (!PLCFrame.equals("")){
                Util698.log(TerminalParameterController.class.getName(), "重启后重新测试:"+PLCFrame,Debug.LOG_INFO);
            	dealPLCData(PLCFrame,false);
            }
            else{
                Util698.log(TerminalParameterController.class.getName(), "重启后PLCFRAME为空，无需测试",Debug.LOG_INFO);
            }

            SoftParameter.getInstance().setRECVCLINET("");
            SoftParameter.getInstance().saveParam();
        }

//		// http://www.it1352.com/543057.html
//		// 使用Platform.runLater(()不当时，依然会屏幕显示异常
//		Task task = new Task() {
//		    @Override
//		    protected Object call() throws Exception {
//		        while (true) {
//		            this.updateMessage(MessageCenter.getInstance().PLCSimulator_msg+ " 时间-"+DateTimeFun.getDateTimeSSS());
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
        		// 处理以下出现的特殊情况，测试过程异常中断，处于ISBUSY状态，此时无法继续测试了，所以重新启动软件
//        		2018-07-09 17:31:42:162 [xuky add Detail row:-1 p1载波特殊000000098063-] deal.TerminalParameterController
//        		2018-07-09 17:32:14:154 [ScheduledExecutorService方式 执行任务的超时检测  map.size=0] socket.DealSendData
//        		2018-07-09 17:33:14:159 [ScheduledExecutorService方式 执行任务的超时检测  map.size=0] socket.DealSendData
//        		2018-07-09 17:34:14:164 [ScheduledExecutorService方式 执行任务的超时检测  map.size=0] socket.DealSendData
//        		2018-07-09 17:34:59:426 [3recv 端口:/127.0.0.1:58642 user data【1】:68252568010106303938303631020630393830363203063039383036330406303938303634050006002E16] socket.DealData
//        		2018-07-09 17:34:59:476 [收到并处理报文：68252568010106303938303631020630393830363203063039383036330406303938303634050006002E16 framePLC.getControlData:01] deal.TerminalParameterController
//        		2018-07-09 17:34:59:568 [启动测试，进行数据回复，68 01 01 68 81 53 16] deal.TerminalParameterController
//        		2018-07-09 17:34:59:569 [sendData=>68 01 01 68 81 53 16] socket.SocketServerEast
//        		2018-07-09 17:34:59:570 [getBarCode_new ISBUSY return ] deal.TerminalParameterController

    			String nowTime = Util698.getDateTimeSSS_new();
    			long diff = Util698.getMilliSecondBetween_new(nowTime, BUSY_B_time);
    			if (diff > (long)RestCheck) {
    		        Util698.log(TerminalParameterController.class.getName(), "Exception 异常处理，ISBUSY时，开始时间"+BUSY_B_time+"现在"+nowTime+"时差超过"+RestCheck+" 故RestApp", Debug.LOG_INFO);

    				// 重启后，无需发送回复报文，等待重新测试即可
    	            SoftParameter.getInstance().setSENDPLC1("");
    	            SoftParameter.getInstance().setSENDPLC2("");
//    	            SoftParameter.getInstance().setPLCFRAME(PLCFrame);
    	            SoftParameter.getInstance().saveParam();
    	            // 但是重启后，最好重新进行测试，需要记录下收到的启动报文
    	            // 等待时间不可过长

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
        // 如下的testDetailList不能使用Platform.runLater，因为会导致次序混乱
        Object[] obj = pop();
        int row = (int) obj[2];
        Boolean notSum = true;
        // 测试用例详细信息
        if (obj[0].equals("testDetailList")) {
            ProduceCaseResult p1 = (ProduceCaseResult) obj[1];
            Util698.log(TerminalParameterController.class.getName(), "ListShow.testDetailList "+p1+" "+p1.getADDR()+"."+p1.getCaseno(), Debug.LOG_INFO);

            if (row >= 0){
                Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-b p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-发送"+p1.getSendtime()+"-接收"+p1.getRecvtime()+"-结果:"+p1.getResult(), Debug.LOG_INFO);
                for (int i = 0 ;i<testDetailList.size();i++){
                	ProduceCaseResult dataInList = testDetailList.get(i);
                    if (dataInList.getADDR().equals(p1.getADDR()) && dataInList.getCaseno()==p1.getCaseno() ) {
                    	if (!dataInList.getResult().equals("") && p1.getResult().equals("") ){
//                    	if (dataInList.getResult().equals("成功")){
                    		// xuky 2018.07.24 已经成功就不要覆盖了
                    		// 还有一种可能，已经超时或是失败了，结果被“空值”覆盖了
                            Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-stop p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-发送"+p1.getSendtime()+"-接收"+p1.getRecvtime()+"-row="+i+"-结果:"+p1.getResult(), Debug.LOG_INFO);
                            p1 = dataInList;
                            Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-stop dataInList"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-发送"+p1.getSendtime()+"-接收"+p1.getRecvtime()+"-row="+i+"-结果:"+p1.getResult(), Debug.LOG_INFO);
                    	}
                    	else{
                            testDetailList.set(i, p1);
                            Util698.log(TerminalParameterController.class.getName(), "xuky set Detail-e p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-发送"+p1.getSendtime()+"-接收"+p1.getRecvtime()+"-row="+i+"-结果:"+p1.getResult(), Debug.LOG_INFO);
                    	}
                        break;
                    }
                }
            }
            else if (row == -1){
//                Util698.log(TerminalParameterController.class.getName(), "xuky add Detail row:"+row+" p1"+p1.getName()+p1.getADDR()+"-"+p1.getSendtime(), Debug.LOG_INFO);
                Util698.log(TerminalParameterController.class.getName(), "xuky add Detail p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-发送"+p1.getSendtime()+"-接收"+p1.getRecvtime()+"-结果:"+p1.getResult(), Debug.LOG_INFO);
                testDetailList.add(p1);
            }
            else{
                testDetailList.remove(p1);
                Util698.log(TerminalParameterController.class.getName(), "xuky remove Detail p1"+p1.getName()+p1.getADDR()+"."+p1.getCaseno()+"-发送"+p1.getSendtime()+"-接收"+p1.getRecvtime()+"-结果:"+p1.getResult(), Debug.LOG_INFO);
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

        // 测试结果汇总信息
        if (obj[0].equals("observableList_record")) {
            if (row >= 0)
                observableList_record.set(row, (ProduceRecord) obj[1]);
            else if (row == -1) {
                // 新增数据
                ProduceRecord p1 = (ProduceRecord) obj[1];
                observableList_record.add(p1);
//                Util698.log(TerminalParameterController.class.getName(), "xuky add Sum row:"+row+" p1"+p1.getAddr(), Debug.LOG_INFO);

                ProduceRecord produceRecord1 = (ProduceRecord) obj[1];

                // 确保新增时不向数据库添加数据，只是在测试结束时添加数据
                // 无法解决失败时，经常出现的数据重复问题
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

                // 进行判断，判断是否完成
                Boolean reOperate = true;
                for (ProduceRecord produceRecord : observableList_record){
                    if (produceRecord.getEndTime().equals("")) {
                        // 表示执行未完成
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
                // 如果执行了remove，表示重新进行一轮测试
                notSum = true;
                ProduceRecord p1 = (ProduceRecord) obj[1];
//				Boolean removeResult = observableList_record.remove(p1);
                // 可能出现根据对象remove(p1)失败的情况
                for (ProduceRecord produceRecord : observableList_record){
                    if (produceRecord.getAddr().equals(p1.getAddr())) {
                        Boolean removeResult = observableList_record.remove(produceRecord);
//                        Util698.log(TerminalParameterController.class.getName(), "xuky remove Sum result:"+removeResult+" p1"+p1.getAddr(), Debug.LOG_INFO);
                        break;
                    }
                }
            }
        }

        // xuky 2018.06.01 因为pop是阻塞的，所以无需在这里进行sleep
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

    	// xuky 2018.08.02 添加线程的名称，便于出现问题时进行定位
        pool.submit(new RefreshDataThread());

        // 获得消息
        // xuky  2018.07.09 有个疑问，大部分的消息是Publisher传递的，这里的只是注册了UI如何收到
        // 对上个疑问的解释，参考DealData Line136   if (frameType.equals("PLC"))。。。  特殊处理，使用Publisher UI
//        Publisher.getInstance().addObserver(this);

        String subname = "", plandID = SoftParameter.getInstance().getParamValByKey("PLANID");
        if (plandID.equals("----")) {
            javafxutil.f_alert_informationDialog("操作提示", "请对软件运行参数进行数据维护！");
            return;
        }

        IBaseDao<ProduceCaseSub> iBaseDao_ProduceCaseSub = new ProduceCaseSubDaoImpl();
        // xuky 2017.07.28 注意，需要使用no进行关联
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

        // xuky 2017.07.26 客户反馈显示的方案信息错误
        String msg = "【plandID】" + plandID + " 【subname】" + subname;
        Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);

        txt_showTitle.setText("【方案】" + subname + "【操作人】" + SoftParameter.getInstance().getUserManager().getUsername());

        String[] detail_colNames = { "通信地址-单行", "名称", "结果", "发送次数", "发送-单行", "接收-单行", "期望-单行", "发送时间", "接收时间", "发前延时",
                "超时等待", "重试次数", "端口" };
        String detail_export_columns = "ADDR,name,result,sendtimes,send,recv,expect,sendtime,recvtime,delaytime,waittime,retrys,port";

        // 列表展示用字段信息 只是一部分内容
        String[] table_colNames = { "ID[0]", "地址[100]", "名称", "次数[40]", "结果[40]", "发送时间[170]", "接收时间[170]", "发送",
                "端口[40]", "发前延时[60]", "超时等待[60]", "重试次数[60]" };
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
        // 不显示CURD的按钮操作区域
        testDetail_crud.setButtonsVisible(false);

        // ------------------------------------------------------
        // String[] detail_colNames_param = { "名称", "key", "val", "类型", "说明",
        // "显示控制" };
        // detail_export_columns = "name,keyname,value,type,note1,note2";
        String[] detail_colNames_param = { "内容" };
        detail_export_columns = "value";
        String[] table_colNames_param = { "ID[0]", "名称", "内容[220]", "备注" };
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
        // object_crud_param.setExportFileName("软件运行参数导出数据.xls");

        object_crud_param.setButtonsVisible(false);
        // object_crud_param.setActionListener(actionListener);

        // xuky 2018.03.01 显示整体的测试进度信息
        String[] detail_colNames_begin_end = { "开始时间", "地址", "结束时间", "测试结果", "标志" };
        detail_export_columns = "opTime,addr,endTime,opResult,beginOpt";
        String[] table_colNames_begin_end = { "ID[0]", "地址[100]", "开始时间", "结束时间", "测试结果", "标志" };
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

        // 录入框默认获得焦点
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                input_produceBarCode.requestFocus();
            }
        });

        {
            DealSendData.getInstance();
            DealSendBlockData.getInstance();

            // xuky 2017.06.15 条码枪配套测试代码：不自动设置设备的状态信息
            input_produceBarCode.setOnAction((event) -> {
                if (input_comm.getText().equals("0"))
                    getBarCode(1);
                else
                    getBarCode();
            });

            input_produceBarCode1.setOnAction((event) -> {
                // 多个设备同时进行测试
                getBarCode(2);
            });

            input_produceBarCode2.setOnAction((event) -> {
                // 多个设备同时进行测试
                getBarCode(3);
            });

            input_produceBarCode3.setOnAction((event) -> {
                // 多个设备同时进行测试
                getBarCode(4);
            });
            input_produceBarCode4.setOnAction((event) -> {
                // 多个设备同时进行测试
                getBarCode(5);
            });
            input_produceBarCode5.setOnAction((event) -> {
                // 多个设备同时进行测试
                getBarCode(6);
            });
        }

        txt_showMsg.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        txt_showMsg.setTextFill(Color.web("#0076a3"));

        txt_showTitle.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 15));
        txt_showTitle.setTextFill(Color.web("#0076a3"));

        // lable_result.setText("测试通过");
        lable_result.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));

    }

    // xuky 2018.04.19 因为以下代码有关于界面操作的，所以必须放在Platform.runLater中
    public void getBarCode(int num) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getBarCode_new(num, 0);
            }
        });
    }

    public void getBarCode_new(int num, int flag) {


        // xuky 2018.03.06 如果正在执行就不要执行了
        if (ISBUSY){
            Util698.log(TerminalParameterController.class.getName(), "getBarCode_new ISBUSY return ", Debug.LOG_INFO);
            return;
        }

        lableResultSet("正在测试...", Color.web("#9ed048"));

        ISBUSY = true;
        BUSY_B_time = DateTimeFun.getDateTimeSSS();
        Util698.log(TerminalParameterController.class.getName(), "设置ISBUSY = true andBUSY_B_time="+BUSY_B_time, Debug.LOG_INFO);

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
        // xuky 2018.04.19 希望通过这样的方式释放内存
        object_crud_begin_end.refreshTableData();
        observableList_record = object_crud_begin_end.getData_objs();

        testDetail_crud.refreshTableData();
        testDetailList = testDetail_crud.getData_objs();

        object_crud_param.refreshTableData();
        observableList_param = object_crud_param.getData_objs();

        DealSendData.getInstance();
        DealSendBlockData.getInstance();

        // xuky 2018.03.23 用来记录上次的完成时间
        MULTI_END0 = MULTI_END;

        MULTI_BEGIN = Util698.getDateTimeSSS_new();
        OK_NUM = 0;
        // SEND_NUM = 0;
        // RECV_NUM = 0;

        addr = input_produceBarCode.getText();
        addr = Util698.getAddrByBarcode(addr);
        // Util698.log("效率分析", "数据库交互1 begin" , Debug.LOG_INFO);
        // Util698.log("效率分析", "数据库交互1 end" , Debug.LOG_INFO);
        addNewRecord(addr);
        // SEND_NUM++;

        // xuky 2018.04.24 初始化锁标志
        DealSendBlockLock.getInstance().init();

        TestBegin(addr, 1);

        if (num == 1)
            return;

        // xuky 2018.02.07 多路之前添加2.5秒的间隔， +1秒，因为还有其他耗时
        // 每一路的测试，首先执行的就是载波抄表，载波抄表完成2.5秒后，相互之间影响消失
        // 严格意义上讲，应该是前一路载波最后操作，延时2.5后启动 因为前面的载波操作可能是需要多次重试的

        // xuky 2018.02.08 经过试验，发现载波通信的影响不止2.5秒，更加的长时间
        // 修改为先进行红外通信，然后再进行载波通信 注意！！！ 需要修改plc_in_front = false
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
        addr = Util698.getAddrByBarcode(addr); // 数据库交互1
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
    	// xuky 2018.08.02 添加线程的名称，便于出现问题时进行定位
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
            // xuky 2018.04.13 对这里的代码进行处理，使用对象池技术，以期减少对内存的占用
            Boolean isPool = true;
            if (isPool) {
                ObjectPoolDealOperateMuti objPool = ObjectPoolDealOperateMuti.getInstance();
                DealOperateMuti obj = (DealOperateMuti) objPool.getObject();

                // xuky 2018.04.24 添加每个设备的锁 每个设置只加一次锁
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
        RECORD_ADD.setOpResult("测试中");
        // observableList_record.add(RECORD_ADD);

        Object[] obj = new Object[] { "observableList_record", RECORD_ADD, -1 };
        push(obj);

    }

    private synchronized void getBarCode() {

        observableList_record.clear();
        // Util698.ListReMoveAll(observableList_record);

        String barCode = input_produceBarCode.getText();
        if (barCode.equals("")) {
            Util698.log(TerminalParameterController.class.getName(), "扫描内容为空，故return" + barCode, Debug.LOG_INFO);
            return;
        }
        Util698.log(TerminalParameterController.class.getName(), "扫描条码-" + barCode, Debug.LOG_INFO);

        if (ISBUSY) {
            Util698.log(TerminalParameterController.class.getName(), "正在测试设备-" + ADDR + "，故return", Debug.LOG_INFO);

            lable_result.setText("正在测试...");
            lableResultSet("正在测试...", Color.web("#9ed048"));

            // input_produceBarCode.setText("正在测试...");
            // input_produceBarCode.selectAll();
            return;
        }

        ISBUSY = true;
        BUSY_B_time = DateTimeFun.getDateTimeSSS();
        Util698.log(TerminalParameterController.class.getName(), "设置ISBUSY = true and BUSY_B_time="+BUSY_B_time, Debug.LOG_INFO);
        // System.out.println(" isBusy = true1 ");

        try {
            if (barCode.equals(SoftParameter.getInstance().getParamValByKey("ERRBARCODE"))) {
                DealOperate.getInstance().setErr();
                ISBUSY = false;
                Util698.log(TerminalParameterController.class.getName(), "ERRBARCODE", Debug.LOG_INFO);
            } else if (barCode.equals(SoftParameter.getInstance().getParamValByKey("RATEBARCODE"))) {
                // xuky 2017.08.01 根据扫码信息，进行通信速率的切换
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
        // xuky 注意：update函数中建议添加try catch 函数，防止出现异常，导致后续的窗口接收不到相关消息
//        Platform.runLater(() -> {
            try {
                Object[] s = (Object[]) arg;
                if (s[0].equals("DealOperate")) {
                    // 显示执行进度信息
                    showDealOperateData(arg);
                }
                if (s[0].equals("DealTestCase")) {
                    // 显示执行的测试用例详情
                    showDealTestData(arg);
                }
                if (s[0].equals("recv frame")) {
                    // 显示执行的测试用例详情
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
                Util698.log(TerminalParameterController.class.getName(), "收到验证Observer报文", Debug.LOG_INFO);
                SoftParameter.getInstance().setObserverOK("168");
        		return;
        	}

            if (s.toUpperCase().endsWith("0D"))
                s = s.substring(0, s.length() - 2);
            FramePLC framePLC = new FramePLC(s);
            Util698.log(TerminalParameterController.class.getName(), "收到并处理报文：" + s +" framePLC.getControlData:"+framePLC.getControlData(), Debug.LOG_INFO);
            if (framePLC.getControlData().equals("01")) {

            	// xuky 2018.07.25 在测试前记录需要测试的报文内容。在测试完成时清空，如果中间出现异常需要重启，则继续按照报文内容进行测试
	            SoftParameter.getInstance().setPLCFRAME(s);
	            SoftParameter.getInstance().saveParam();

            	// 对收到的启动报文进行记录，因为可能需要重新测试
            	step = "2";
            	PLCFrame = s;
                // xuky 2018.06.01 测试前清空数据
                testDetailList.clear();

                step = "3";
                String[] addrs = framePLC.getDEVADDR();
                errcode = "91";
                // 显示的是条码信息，测试的是地址信息

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
                // 如果是发生异常后，重新启动，重新执行的测试，则无需进行回复PLC
                if (needReply){
                	step = "7";
                    String senddata = "68 01 01 68 81 53 16";
                    Util698.log(TerminalParameterController.class.getName(), "启动测试，进行数据回复，" + senddata, Debug.LOG_INFO);
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
                // 如果收到这样的数据就停止重发回复报文
                Send_time = Send_repeateTime;
            }
            if (framePLC.getControlData().equals("82")) {
                // 如果收到这样的数据就停止重发回复报文
                Send_time1 = Send_repeateTime1;
            }
            framePLC = null;
        } catch (Exception e) {
        	e.printStackTrace();
            Util698.log(TerminalParameterController.class.getName(), "dealPLCData Exception " + e.getMessage() +"step:"+ step,
                    Debug.LOG_INFO);
            Debug.sleep(1000);
            dealPLCData(s,needReply);
//            // 进行数据回复 xuky 2018.07.23 以下代码含义不清楚
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

        // xuky 2017.08.16 需要解析produceCaseResult中的数据
        ProduceCaseResult produceCaseResult = (ProduceCaseResult) object;

        String recv = produceCaseResult.getRecv();
        // 只在单项测试时，有此需要
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
                    if (produceParam.getName().equals("设备返回版本信息")) {
                        // xuky 2017.08.17 之前尝试进行增加和修改，无效，使用先删除后增加的方式更好
                        Object[] obj = new Object[] { "observableList_param", produceParam, -100 };
                        push(obj);
                        // observableList_param.remove(produceParam);
                        break;
                    }
                }
                ProduceParam needChange = new ProduceParam();
                needChange.setName("设备返回版本信息");
                needChange.setValue(str);

                Object[] obj = new Object[] { "observableList_param", needChange, -1 };
                push(obj);

            }
        }

        // 如下两个操作都是比较耗时的，建议进行压栈和出栈操作
        // testDetailList.add
        // testDetailList.set

        if (type.equals("new")) {
            // ProduceCaseResult produceCaseResult =
            // iBaseDao_ProduceCaseResult.retrieve(ID);
        	Util698.log(TerminalParameterController.class.getName(), "showDealTestData.new "+produceCaseResult, Debug.LOG_INFO);
            // testDetailList.add(produceCaseResult);
            Object[] obj = new Object[] { "testDetailList", produceCaseResult, -1 };
            push(obj);
            // xuky 2018.07.04 不要过早的释放这里的数据，会影响正常使用
//			produceCaseResult = null;

        }
        if (type.equals("old")) {
            // xuky 2017.08.25 因为可能有多个设备在同时进行测试
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
                flag = "未标记来源";
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
            // obj.restart(); // 因为是从pool中获取，可能已经执行完毕，所以restart
        }

        String addr = "", result = "";
        Boolean finished = false;
        Boolean needShow = true;
        if (msg1.indexOf("测试成功") >= 0) {
            addr = msg1.substring(msg1.indexOf("设备") + 2, msg1.indexOf("测试")).trim();
            finished = true;
            result = "测试成功";

//			Util698.log(TerminalParameterController.class.getName(), "测试结果:"+addr+result, Debug.LOG_INFO);

            OK_NUM++;
            if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
                // "设备000000098064 测试成功！"
                // "【2】设备000000098063 测试成功！"

                lableResultSet(result, Color.web("#0c8918"));

                // xuky 2017.07.25 测试成功，进行信息记录，防止被重复进行测试
                SoftParameter.getInstance().setOKADDR(ADDR);
                SoftParameter.getInstance().saveParam();
            }
        }
        if (msg1.indexOf("测试失败") >= 0) {
            // System.out.println(" TerminalParameterController 测试失败");

            finished = true;
            result = "测试失败";
            // System.out.println("xuky 2018.04.12-1 失败1");
            addr = msg1.substring(msg1.indexOf("设备") + 2, msg1.indexOf("测试")).trim();

//			Util698.log(TerminalParameterController.class.getName(), "测试结果:"+addr+result, Debug.LOG_INFO);

            if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
                lableResultSet(result, Color.web("#ff2121"));
            }

        }

        if (msg1.indexOf("设置为设备故障状态") >= 0) {
            needShow = false;
            // System.out.println(" TerminalParameterController 设置为设备故障状态");

            finished = true;
            result = "测试失败";
            // System.out.println("xuky 2018.04.12-1 故障1");

            addr = msg1.substring(msg1.indexOf("设备") + 2, msg1.indexOf("设置为设备故障状态")).trim();

//			Util698.log(TerminalParameterController.class.getName(), "测试结果:"+addr+result+"设置为设备故障状态",Debug.LOG_INFO);

            if (RUNFASTER == false || MULTI_BEGIN.equals("")) {
                lableResultSet(result, Color.web("#ff2121"));
            }

        }

        // 单项测试
        if (MULTI_BEGIN.equals("") && finished) {
            ISBUSY = false;
        }

        // xuky 2018.03.06 通过MULTI_BEGIN判断是否为多项同时测试
        if (!MULTI_BEGIN.equals("") && finished ) {
            if (!addr.equals("")) {
                // xuky 2018.03.02 添加结束信息
                // msg1 = "设备000000098064 测试成功！"
                // 获取地址信息，修改数据

                // 更新测试结果信息
                for (ProduceRecord produceRecord : observableList_record) {
                    if (produceRecord.getAddr().equals(addr)) {
                        if (!produceRecord.getEndTime().equals(""))
                            break;

                        // 删除数据前，先进行对象数据的备份
                        ProduceRecord newProduceRecord = new ProduceRecord();
                        newProduceRecord.init();
                        Util698.objClone(produceRecord, newProduceRecord, "");
                         Object[] obj = new
                         Object[]{"observableList_record",produceRecord,-100};
                         push(obj);

                        MULTI_END = Util698.getDateTimeSSS_new(); // 最后的完成时间
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
        Util698.log(TerminalParameterController.class.getName(), "测试执行次数："+current_time + " 软件重启上限次数"+MAX, Debug.LOG_INFO);

        input_rate_txt = DataConvert.int2String(current_time);

        Boolean rest = true;
        if (!rest) {
            current_time = 1;
            MAX = 2;
        }

        if (current_time <= MAX) {
            // 添加测试汇总数据
            AddSumData();
            // 组织需要向PLC软件回复的报文
            BuildReply4PLC();

            // 只有测试完毕，才设置此PLCFRAME信息为空，否则自动重启后，自动执行测试过程
            SoftParameter.getInstance().setPLCFRAME("");
            SoftParameter.getInstance().saveParam();
            Util698.log(TerminalParameterController.class.getName(), "测试完毕，设置PLCFRAME为空", Debug.LOG_INFO);
        }

        if (current_time > MAX) {
            Util698.log(TerminalParameterController.class.getName(), "current_time > MAX 是多次进入此段代码，此次无需处理",
                    Debug.LOG_INFO);
        }

        if (current_time == MAX) {
            // 重新启动测试程序
            // 如果需要重新启动，则不要启动前上报测试结果，只是记录即可
            // 启动以后再上报测试结果
        	if (RESET_TYPE.equals("RestApp")){
                Util698.log(TerminalParameterController.class.getName(), "current_time == MAX 准备RestApp",
                        Debug.LOG_INFO);
        		RestApp();
        	}
        	if (RESET_TYPE.equals("RestCOMServicel")){
        		current_time = 0;
        		RestCOMServicel();
        	}
        }

        // 需要调整执行的先后次序，首先进行重启串口操作，然后再进行数据回复
        if (RESET_TYPE.equals("RestApp") && current_time == MAX){
            // 如果是需要重新启动的情况，则不在此时进行回复，而是等到重新启动以后再发送回复数据
            Util698.log(TerminalParameterController.class.getName(), "current_time == MAX 不在此时Send2PLC",
                    Debug.LOG_INFO);
        }
        else
            Send2PLC();  // 向PLC软件回复测试结果信息


    }
    private void RestCOMServicel() {
        Util698.log(TerminalParameterController.class.getName(), "重启串口通信", Debug.LOG_INFO);
        PrefixMain.getInstance().closeSerial();
        PrefixMain.getInstance().openSerial();
    }

    private void RestApp() {
        Util698.log(TerminalParameterController.class.getName(), "重新开启，且自动执行测试过程", Debug.LOG_INFO);
        Util698.ResetApp();
    }

    private void BuildReply4PLC() {
        Send_time = 0;
        Send_time1 = 0;
        // xuky 2018.04.12 回复的数据需要根据实际情况进行变更

        // 根据xuky observableList_record 中的数据进行组织
        // xuky 2018.04.26 将需要发送的数据保存到NO_ADDR中
        String send_data = "";
        String datetime = DateTimeFun.getDateTime();
        datetime = datetime.replaceAll("-", "");
        datetime = datetime.replaceAll(":", "");
        datetime = datetime.replaceAll(" ", "");
        int i = 1;

        // xuky 2018.10.23 添加ID信息  测试用数据
//        NO_ADDR = new LinkedHashMap<String, Object>();
//        NO_ADDR.put(DataConvert.int2String(1), Util698.getAddrByBarcode("160000000001"));


        Iterator it = NO_ADDR.entrySet().iterator();
        while (it.hasNext()) {
            send_data = DataConvert.int2HexString(i, 2);
            Map.Entry entry = (Map.Entry) it.next();
            String addr1 = (String) entry.getValue(); // 地址信息
            // Object value = ; // 数据内容
            // 序号 标志 地址长度 地址数据 日期 数据长度 数据内容
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
                if (result1.indexOf("成功") >= 0)
                    send_data += "01"; // 01成功 00(后续数据的变长信息)
                                        // 00表示后面无数据
                else
                    send_data += "02"; // 02失败 00(后续数据的变长信息)
                                        // 00表示后面无数据
                addr1 = Util698.FormatAddr(addr1, 12);

                send_data += DataConvert.int2HexString(addr1.length() / 2, 2) + addr1 + datetime; // 地址需要补齐偶数位长度，最少为06
                // xuky 2018.10.23 添加ID信息
    			List<DevInfo> devInfos = iBaseDao_DevInfo.retrieve("where addr='" + addr1 + "'", "");
    			DevInfo devInfo = (DevInfo) Util698.getFirstObject(devInfos);
    			String SAVEID = "";
    			if (devInfo != null) {
    				SAVEID = devInfo.getBarCode();
    			}

                if (result1.indexOf("成功") >= 0)
                	if (SAVEID.equals(""))
                		send_data += "00"; // 目前的传递变长数据长度为0
                	else{
                		send_data += DataConvert.int2HexString(SAVEID.length() / 2, 2) + SAVEID;
                	}
                else
                    send_data += "00"; // 目前的传递变长数据长度为0
            }
            entry.setValue(send_data);

            i++;
        }
        i = 1;
        // xuky 2018.04.26 从NO_ADDR中豁获取数据长度信息
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
        Util698.log(TerminalParameterController.class.getName(), "BuildReply4PLC执行完成！", Debug.LOG_INFO);
    }

    private void Send2PLC() {
        String send_data;
        String send_data1;
        send_data = SoftParameter.getInstance().getSENDPLC1();
        send_data1 =  SoftParameter.getInstance().getSENDPLC2();

        if (send_data == null || send_data.equals(""))
            return;

            Util698.log(TerminalParameterController.class.getName(), "向PLC发送测试结果："+send_data+"-"+send_data1,Debug.LOG_INFO);

            while (true) {
                // 如果收到了回复报文，就会设置Send_time，从而停止重发
                if ( Send_time >= Send_repeateTime)
                    break;
                if (send_data.equals("680D0D68030100020003000400050006000216")){
                    Util698.log(TerminalParameterController.class.getName(), "SumAndSendOK不发送"+send_data, Debug.LOG_INFO);
                    break;
                }
                SocketServerEast.sendSocketData(send_data);
                Send_time++;
                Debug.sleep(send_interval); // 间隔1秒进行重试
            }
            while (true) {
                if (Send_time1 >= Send_repeateTime1)
                    break;
                if (send_data1.equals("680D0D68020100020003000400050006000116")){
                    Util698.log(TerminalParameterController.class.getName(), "SumAndSendOK不发送"+send_data1, Debug.LOG_INFO);
                    break;
                }
                SocketServerEast.sendSocketData(send_data1);
                Send_time1++;
                Debug.sleep(send_interval); // 间隔1秒进行重试
            }

            // 清空需要发送的数据
            SoftParameter.getInstance().setSENDPLC1("");
            SoftParameter.getInstance().setSENDPLC2("");
            SoftParameter.getInstance().saveParam();

    }

    private void AddSumData() {
        // xuky 2018.06.98 已经在线程中执行当前整个函数，就无需再线程执行了，会导致数据存储的不美观
//			new Thread(() -> {
            // 尚未使用对象池，改进对象回收利用情况
            int sleep_time = 8000;

            // xuky 2018.03.23 在数据库中插入一条汇总信息
            ProduceRecord newProduceRecord = new ProduceRecord();
            // ProduceRecord newProduceRecord = (ProduceRecord)
            // objPool2.getObject();
            newProduceRecord.init();
            newProduceRecord.setAddr("sumData-"+MAX+"-"+DataConvert.fillWith0(DataConvert.int2String(current_time), 4));

            newProduceRecord.setBeginOpt(MULTI_BEGIN);

            // 上次的完成时间为空，表明此次是首次执行
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
            Util698.log(TerminalParameterController.class.getName(), "测试成功数量："+OK_NUM + " 测试耗时："+usingTime, Debug.LOG_INFO);
            // 成功数量信息
            newProduceRecord.setOpResult(DataConvert.int2String(OK_NUM));
            newProduceRecord.setEndTime(MULTI_END);
            newProduceRecord.setWorkStation(PCID);
            newProduceRecord.setOpName(OP_NAME);
            CREATE_SUM = true;
            iBaseDao_ProduceRecord.create(newProduceRecord);
            Util698.log(TerminalParameterController.class.getName(), "iBaseDao_ProduceRecord.create执行完成！", Debug.LOG_INFO);
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
        // obj.restart(); // 因为是从pool中获取，可能已经执行完毕，所以restart
    }

    private void dealAddrCode() {
        // 1扫描条码信息 解析得到地址信息
        String barcode = input_produceBarCode.getText();

        if (!IS_SETID.equals("")){
        	// xuky 2019.01.26 如果IS_SETID数据不为空，表示可能是表模块或是路由模块的ID信息，其长度不是12位
    		List result = iBaseDao_BarCodesInfo.retrieveBySQL(
    				"select barcode from " + BarCodesInfo.class.getName() + " where barcode='" + barcode + "'");
    		if (result == null || result.size() == 0 ){
    			// 如果没有查询到。则不继续
                String msg = "条码信息"+barcode+"未找到，请重新扫描！";
                msg += "\n也可能是配置文件有误，请系统管理员进行排查！";
    			javafxutil.f_alert_informationDialog("操作提示", msg);
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
        Util698.log(TerminalParameterController.class.getName(), "设备地址-" + ADDR, Debug.LOG_INFO);
        // 正在处理测试结果为异常的设备，不能操作其他设备
        String errAddr = SoftParameter.getInstance().getERRADDR();
        if (!errAddr.equals(""))
            DealOperate.getInstance().setERRADDR(errAddr);
        String err = DealOperate.getInstance().getERRADDR();
        if (!err.equals("") && !err.equals(ADDR)) {
            String msg = "设备" + err + " 测试出现异常，请勿测试其他设备！";
            Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
            lable_result.setText("");
            txt_showMsg.setText(msg);
            ISBUSY = false;
            return;
        }

        // Util698.ListReMoveAll(dataListFrame);
        testDetailList.clear();

        // xuky 2017.07.25 如果连续扫描并解析得到了两个地址，第一个成功了，第二个就不要继续
        if (SoftParameter.getInstance().getOKADDR().equals(ADDR)) {
            String msg = "设备" + ADDR + "测试成功，无需再次测试！";
            Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
            lable_result.setText("");
            txt_showMsg.setText(msg);
            ISBUSY = false;

            return;
        }

        if (ADDR.length() > 12 && IS_SETID.equals("")) {
            String msg = "扫描对照后得到设备地址" + ADDR + "异常，请重新扫码！";
            Util698.log(TerminalParameterController.class.getName(), msg, Debug.LOG_INFO);
            lable_result.setText("");
            txt_showMsg.setText(msg);
            ISBUSY = false;
            return;
        }
        DealOperate.getInstance().Start(ADDR);

        addNewRecord(ADDR);

        lable_result.setTextFill(Color.web("#9ed048"));
        lable_result.setText("正在测试...");

        lableResultSet("正在测试...", Color.web("#9ed048"));


    }

    @FXML
    public void chanePortRateAction(ActionEvent event) {
        changePortRate();
    }

    // xuky 2017.08.01 修改串口通信速率
    private void changePortRate() {
        String comid = input_comm.getText();
        int rate = DataConvert.String2Int(input_rate.getText());

        // xuky 2017.08.11 首先关闭所有的串口
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
        input_changeresult.setText("串口ID" + comid + "对应串口修改波特率为" + rate);
        // 修改指定端口的通信速率，即时生效
    }

    @FXML
    public void openPortAction(ActionEvent event) {

        // xuky 2018.02.27 无需修改，只需开启即可
        // 开启通信服务器
        PrefixWindow.getInstance().showFrame("通信服务器", 120, 510, 800, 200);


    }

}
