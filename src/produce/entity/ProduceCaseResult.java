package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import util.SoftParameter;

@Entity
@Table(name="producecaseresult")
public class ProduceCaseResult {
    int ID;
    String computer;  // 电脑识别码
    String subid;  //分类
    String name;  // 测试用例名称
    String send;  // 发送报文
    String recv;  // 接收报文
    int delaytime;  // 执行前延时时间
    int waittime;  // 执行等待时间
    String protocol;  //协议类型
    int retrys; // 重试次数
    String expect;  // 验证报文
    String note; // "批处理"标志   交互端口标志（一次测试过程中，可能需要与多路端口进行通信  串口、网口）
    String analys; // 报文解析
    int caseno; // 用例的先后次序   用于确定执行时的先后次序

    int sendtimes; // 发送次数
    String sendtime; // 发送时间 记录最后一次发送时间
    String recvtime; // 接收时间
    String result = ""; // 判断结果
    int runID ;  // 执行记录ID   ProduceLog.ID operation="扫描条码(1)" 扫描条码后得到设备地址即可进行测试执行
    String port ; // 实际执行的端口信息
    String ADDR ; // 设备地址，表结构中没有的数据项目 借助@Transient实现

    // xuky  2018.01.23 用来记录原先的报文，适用于发送后等待回复的测试用例
    String send0 = "";  // 发送报文-原
    String expect0  = "";  // 验证报文-原
    Boolean waitReply = false; // 等待上报回复
    Boolean isBlockTask = false; // 当前用例是否为阻塞任务

    public void init(){
    	computer = SoftParameter.getInstance().getPCID();  // 电脑识别码
    	subid = "";  //分类
    	name = "";  // 测试用例名称
    	send = "";  // 发送报文
    	recv = "";  // 接收报文
    	delaytime = 0;  // 执行前延时时间
    	waittime = 0;  // 执行等待时间
    	protocol = "";  //协议类型
    	retrys = 0; // 重试次数
    	expect = "";  // 验证报文
    	note = ""; // "批处理"标志   交互端口标志（一次测试过程中，可能需要与多路端口进行通信  串口、网口）
    	analys = ""; // 报文解析
    	caseno = 0; // 用例的先后次序   用于确定执行时的先后次序

    	sendtimes = 0; // 发送次数
    	sendtime = ""; // 发送时间 记录最后一次发送时间
    	recvtime = ""; // 接收时间
    	result = "" ; // 判断结果
    	runID  = 0;  // 执行记录ID   ProduceLog.ID operation="扫描条码(1)" 扫描条码后得到设备地址即可进行测试执行
    	port  = ""; // 实际执行的端口信息
    	ADDR  = ""; // 设备地址，表结构中没有的数据项目 借助@Transient实现

    	    // xuky  2018.01.23 用来记录原先的报文，适用于发送后等待回复的测试用例
    	send0 = "";  // 发送报文-原
    	expect0  = "";  // 验证报文-原
    	waitReply = false; // 等待上报回复
    	isBlockTask = false; // 当前用例是否为阻塞任务
    }

	@Transient
	public Boolean getIsBlockTask() {
		return isBlockTask;
	}

	public void setIsBlockTask(Boolean isBlockTask) {
		this.isBlockTask = isBlockTask;
	}

	@Transient
    public Boolean getWaitReply() {
		return waitReply;
	}

	public void setWaitReply(Boolean waitReply) {
		this.waitReply = waitReply;
	}

	@Transient
	public String getSend0() {
		return send0;
	}

	public void setSend0(String send0) {
		this.send0 = send0;
	}

	@Transient
	public String getExpect0() {
		return expect0;
	}

	public void setExpect0(String expect0) {
		this.expect0 = expect0;
	}


	public ProduceCaseResult(){
    	sendtimes = 0;
    	recv = "";
//    	computer = Debug.getHdSerialInfo();
    	computer = SoftParameter.getInstance().getPCID();

//    	result = "";
    }

	@Transient
	public String getADDR() {
		return ADDR;
	}

	public void setADDR(String aDDR) {
		ADDR = aDDR;
	}



	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getSubid() {
		return subid;
	}
	public void setSubid(String subid) {
		this.subid = subid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSend() {
		return send;
	}
	public void setSend(String send) {
		this.send = send;
	}
	public String getRecv() {
		return recv;
	}
	public void setRecv(String recv) {
		this.recv = recv;
	}
	public int getDelaytime() {
		return delaytime;
	}
	public void setDelaytime(int delaytime) {
		this.delaytime = delaytime;
	}
	public int getWaittime() {
		return waittime;
	}
	public void setWaittime(int waittime) {
		this.waittime = waittime;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getRetrys() {
		return retrys;
	}
	public void setRetrys(int retrys) {
		this.retrys = retrys;
	}
	public String getExpect() {
		return expect;
	}
	public void setExpect(String expect) {
		this.expect = expect;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getAnalys() {
		return analys;
	}
	public void setAnalys(String analys) {
		this.analys = analys;
	}
	public int getSendtimes() {
		return sendtimes;
	}
	public void setSendtimes(int sendtimes) {
		this.sendtimes = sendtimes;
	}
	public String getSendtime() {
		return sendtime;
	}
	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}
	public String getRecvtime() {
		return recvtime;
	}
	public void setRecvtime(String recvtime) {
		this.recvtime = recvtime;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public int getRunID() {
		return runID;
	}
	public void setRunID(int runID) {
		this.runID = runID;
	} //运行记录ID
	public int getCaseno() {
		return caseno;
	}
	public void setCaseno(int caseno) {
		this.caseno = caseno;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	public String getComputer() {
		return computer;
	}

	public void setComputer(String computer) {
		this.computer = computer;
	}


}
