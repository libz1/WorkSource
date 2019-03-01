package entity;

import javax.comm.SerialPort;

import dao.basedao.FaoBase;

/**
 * 串口通信参数类
 * @author xuky
 */
public class SerialParam extends FaoBase{

	String COMID;   // 用于测试端口选择使用，串口号
	String COMM;   // 串口号
	String terminal;  // 串口对应终端地址
	int baudRate;  // 波特率
	int dataBit;  // 数据位
	int stopBit;  // 停止位
	String parity;   // 校验方式
	int receiveTimeout;  // 通信超时时间
	public SerialParam(String COMM,int baudRate){
		this.COMM = COMM;
		this.baudRate = baudRate;
		dataBit = SerialPort.DATABITS_8;
		stopBit = SerialPort.STOPBITS_2;
		parity = "偶EVEN(2)";
		receiveTimeout = 30;
	}
	public SerialParam(String COMM,int baudRate, String parity){
		this.COMM = COMM;
		this.baudRate = baudRate;
		dataBit = SerialPort.DATABITS_8;
		this.stopBit = SerialPort.STOPBITS_2;
		this.parity = parity;
		receiveTimeout = 30;
	}

	public SerialParam() {
		COMM = "";
		terminal = "";
		baudRate = 9600;  // 应该是枚举类型  1200、2400、9600、115200
		dataBit = SerialPort.DATABITS_8;
		stopBit = SerialPort.STOPBITS_2;
		parity = "偶EVEN(2)";
		receiveTimeout = 30;
	}


	public String getCOMM() {
		return COMM;
	}

	public void setCOMM(String comm) {
		COMM = comm;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getDataBit() {
		return dataBit;
	}

	public void setDataBit(int dataBit) {
		this.dataBit = dataBit;
	}

	public int getStopBit() {
		return stopBit;
	}

	public void setStopBit(int stopBit) {
		this.stopBit = stopBit;
	}

	public String getParity() {
		return parity;
	}

	public void setParity(String parity) {
		this.parity = parity;
	}

	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(int receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}



	public String getCOMID() {
		return COMID;
	}


	public void setCOMID(String cOMID) {
		COMID = cOMID;
	}


	public static void main(String[] args) {

	}

}
