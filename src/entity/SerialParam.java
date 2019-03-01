package entity;

import javax.comm.SerialPort;

import dao.basedao.FaoBase;

/**
 * ����ͨ�Ų�����
 * @author xuky
 */
public class SerialParam extends FaoBase{

	String COMID;   // ���ڲ��Զ˿�ѡ��ʹ�ã����ں�
	String COMM;   // ���ں�
	String terminal;  // ���ڶ�Ӧ�ն˵�ַ
	int baudRate;  // ������
	int dataBit;  // ����λ
	int stopBit;  // ֹͣλ
	String parity;   // У�鷽ʽ
	int receiveTimeout;  // ͨ�ų�ʱʱ��
	public SerialParam(String COMM,int baudRate){
		this.COMM = COMM;
		this.baudRate = baudRate;
		dataBit = SerialPort.DATABITS_8;
		stopBit = SerialPort.STOPBITS_2;
		parity = "żEVEN(2)";
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
		baudRate = 9600;  // Ӧ����ö������  1200��2400��9600��115200
		dataBit = SerialPort.DATABITS_8;
		stopBit = SerialPort.STOPBITS_2;
		parity = "żEVEN(2)";
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
