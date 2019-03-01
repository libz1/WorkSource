package produce.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import util.SoftParameter;



@Entity
@Table(name="producecase")
public class ProduceCase {
    int ID;
    String computer;  // 电脑识别码
    String subid;  //分类  所属测试方案
    String name;  // 测试用例名称
    String send;  // 发送报文
    int delaytime;  // 执行前延时时间
    int waittime;  // 执行等待时间
    String protocol;  //协议类型
    int retrys; // 重试次数
    String expect;  // 验证报文
    String note; // "批处理"标志   交互端口标志（一次测试过程中，可能需要与多路端口进行通信  串口、网口）
    String analys; // 报文解析
    int caseno; // 用例的先后次序   用于确定执行时的先后次序

    public ProduceCase(){
//    	computer = Debug.getHdSerialInfo();
//    	import util.SoftParameter;
    	computer = SoftParameter.getInstance().getPCID();
    	waittime = 2500;
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
	public int getCaseno() {
		return caseno;
	}
	public void setCaseno(int caseno) {
		this.caseno = caseno;
	}
	public String getComputer() {
		return computer;
	}
	public void setComputer(String computer) {
		this.computer = computer;
	}




}
