package produce.deal;

import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import dao.SerialParamFaoImpl;
import dao.basedao.IBaseDao;
import entity.SerialParam;
import entity.TransImplSerialParam;
import javafx.application.Platform;
import javafx.base.BaseController;
import javafx.base.ObjectCURD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import produce.entity.DevInfo;
import produce.entity.DevInfoDaoImpl;
import produce.entity.ProduceCaseSub;
import produce.entity.ProduceParam;
import produce.entity.ProduceParamDaoImpl;
import produce.entity.TransImplObject;
import util.Publisher;
import util.SoftParameter;

import com.eastsoft.util.DataConvert;


public class TerminalCURDController extends BaseController implements Initializable,Observer {

    @FXML
    BorderPane object_crud_panel, object_crud_panel_com, programtask_crud_panel_com, object_crud_panel_param;

    @FXML
    TextField txt_Prefix_ip, txt_Prefix_port, txt_Prefix_port_New, txt_all_timeout, txt_workID, txt_startqcode;

    @FXML
    TextField txt_UDPCLIENT_IP,txt_UDPCLIENT_PORT,txt_UDPSVR_IP,txt_UDPSVR_PORT, txt_TERMINAL_IP, txt_LOG_Level;

    @FXML
	ComboBox<String> cb_pcid;

    ObjectCURD object_crud, object_crud_com, task_crud, object_crud_param;

    private SoftParameter softParam;

    ActionListener actionListener = new ActionListener() {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e) {
			SoftParameter.getInstance().refreshDataFromDB();
			System.out.println(" ActionListener -> SoftParameter.refreshDataFromDB()");
		}
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Publisher.getInstance().addObserver(this);

        // 因为需要调整鼠标的外观，需要使用parentScene数据，
        // table_init0();
        // 在TerminalCURD中setParentStage之后，调用table_init0

        // ------------------------------------------------
        // 明细信息用字段信息 内容较为全面


        String[] detail_colNames_com = { "串口ID","串口号", "终端地址", "波特率", "校验方式;code:PARITY" };
        String detail_export_columns = "COMID,COMM,terminal,baudRate,parity";
        String[] table_colNames_com = { "ID[0]","串口ID", "串口号", "终端地址", "波特率", "校验方式" };
        String table_columns = "ID,COMID,COMM,terminal,baudRate,parity";

        object_crud_com = new ObjectCURD<SerialParam>(new SerialParamFaoImpl(), new TransImplSerialParam(),
                detail_colNames_com, detail_export_columns, table_colNames_com, table_columns, "getCOMM");
        object_crud_panel_com.setCenter(object_crud_com);

        // ------------------------------------------------

        String[] detail_colNames_param = { "名称","key", "val", "说明", "显示控制", "顺序" };
        detail_export_columns = "name,keyname,value,note1,note2,type";
        String[] table_colNames_param = { "ID[0]","PCID","名称","key", "val", "说明", "显示控制", "顺序" };
        table_columns = "ID,computer,name,keyname,value,note1,note2,type";

        object_crud_param = new ObjectCURD<ProduceParam>(new ProduceParamDaoImpl(), new TransImplObject(detail_export_columns, new ProduceParam()),
                detail_colNames_param, detail_export_columns, table_colNames_param, table_columns, "getKeyname","where computer='"+SoftParameter.getInstance().getPCID()+"'","");
        object_crud_panel_param.setCenter(object_crud_param);

        object_crud_param.setNewObject(new ProduceParam());
        object_crud_param.setExportFileName("软件运行参数导出数据.xls");

        object_crud_param.setActionListener(actionListener);

        // ------------------------------------------------


        softParam = SoftParameter.getInstance();
        showData();

		// ----列出所有的PCID信息----
		IBaseDao<DevInfo> iBaseDao_DevInfo = new DevInfoDaoImpl();
		List result = iBaseDao_DevInfo
				.retrieveBySQL("select computer from " + ProduceCaseSub.class.getName() + " group by computer ");
		for (Object o : result)
			cb_pcid.getItems().add((String) o);
		cb_pcid.setValue(SoftParameter.getInstance().getPCID());

		// xuky 2017.08.16 lambda方式进行下拉控件选择数据事件
		cb_pcid.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			object_crud_param.setWHERE("where computer='" + newValue + "'");
		});

    }



    private void showData() {



        txt_Prefix_ip.setText(softParam.getPrefix_ip());
        String str = DataConvert.int2String(softParam.getPrefix_port());
        txt_Prefix_port.setText(str);

        str = DataConvert.int2String(softParam.getPrefix_port_new());
        txt_Prefix_port_New.setText(str);

        txt_all_timeout.setText(softParam.getTESTALL_TIME_OUT());
        txt_workID.setText(softParam.getWORKID());
        txt_startqcode.setText(softParam.getSTARTQCODE());

        txt_UDPCLIENT_IP.setText(softParam.getUDPCLIENT_IP());
        str = DataConvert.int2String(softParam.getUDPCLIENT_PORT());
        txt_UDPCLIENT_PORT.setText(str);



        txt_UDPSVR_IP.setText(softParam.getUDPSVR_IP());
        str = DataConvert.int2String(softParam.getUDPSVR_PORT());
        txt_UDPSVR_PORT.setText(str);


        txt_TERMINAL_IP.setText(softParam.getTERMINAL_IP());

        txt_LOG_Level.setText(softParam.getLOG_Level());
//        txt_SendTerminal.setText(softParam.getSendTerminal());
//        txt_IsProxyModel.setText(softParam.getIsProxyModel());
//        txt_All_timeout.setText(DataConvert.int2String(softParam.getAll_timeout()));
//        txt_Single_timeout.setText(DataConvert.int2String(softParam.getSingle_timeout()));
//        txt_Targets.setText(softParam.getTargets());
//
//        txt_svr_priority.setText(softParam.getSvr_priority());
//        txt_svr_no.setText(softParam.getSvr_no());

//        txt_errbarcode.setText(softParam.getErrBarCode());
//        txt_mt485.setText(softParam.getMT485());
//        txt_seq.setText(softParam.getSEQ());
//        txt_subid.setText(softParam.getCURRENTSUBID());

//        txt_lostbit.setText(softParam.getLOSTBIT());
//        txt_verifymt.setText(softParam.getVERIFYMT());
//        txt_mt.setText(softParam.getMT());
    }

    @FXML
    public void saveDataAction(ActionEvent event) {

        softParam.setPrefix_ip(txt_Prefix_ip.getText());
        softParam.setPrefix_port(DataConvert.String2Int(txt_Prefix_port.getText()));

        softParam.setPrefix_port_new(DataConvert.String2Int(txt_Prefix_port_New.getText()));

        softParam.setTESTALL_TIME_OUT(txt_all_timeout.getText());
        softParam.setWORKID(txt_workID.getText());
        softParam.setSTARTQCODE(txt_startqcode.getText());


        softParam.setUDPCLIENT_IP(txt_UDPCLIENT_IP.getText());
        softParam.setUDPCLIENT_PORT(DataConvert.String2Int(txt_UDPCLIENT_PORT.getText()));
        softParam.setUDPSVR_IP(txt_UDPSVR_IP.getText());
        softParam.setUDPSVR_PORT(DataConvert.String2Int(txt_UDPSVR_PORT.getText()));

        softParam.setTERMINAL_IP(txt_TERMINAL_IP.getText());

        softParam.setLOG_Level(txt_LOG_Level.getText());
//        softParam.setSendTerminal(txt_SendTerminal.getText());
//        softParam.setIsProxyModel(txt_IsProxyModel.getText());
//        softParam.setAll_timeout(DataConvert.String2Int(txt_All_timeout.getText()));
//        softParam.setSingle_timeout(DataConvert.String2Int(txt_Single_timeout.getText()));
//        softParam.setTargets(txt_Targets.getText());
//        softParam.setSvr_priority(txt_svr_priority.getText());
//        softParam.setSvr_no(txt_svr_no.getText());

//        softParam.setErrBarCode(txt_errbarcode.getText());
//        softParam.setMT485(txt_mt485.getText());
//        softParam.setSEQ(txt_seq.getText());
//        softParam.setCURRENTSUBID(txt_subid.getText());
//        softParam.setLOSTBIT(txt_lostbit.getText());
//        softParam.setVERIFYMT(txt_verifymt.getText());
//        softParam.setMT(txt_mt.getText());


        softParam.saveParam();
    }

    @Override
    public void update(Observable o, Object arg) {
        Object[] s = (Object[]) arg;
        if (s[0].equals("jobListChaned")){
            refreshList();
        }
    }
    private synchronized void refreshList(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                task_crud.refreshTableData();
            }
        });
    }

}
