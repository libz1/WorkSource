package produce.entity;

public class PLC2MES3 {
	String ID;
	String name;
	String result;
	String sendtimes;
	String send;
    String recv;
    String expect;
    String sendtime;
    String recvtime;
    String port;

    public PLC2MES3(){
    	ID = "";
    	name = "";
    	result = "";
    	sendtimes = "";
    	send = "";
    	recv = "";
    	expect = "";
    	sendtime = "";
    	recvtime = "";
    	port = "";

    }


	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getSendtimes() {
		return sendtimes;
	}
	public void setSendtimes(String sendtimes) {
		this.sendtimes = sendtimes;
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
	public String getExpect() {
		return expect;
	}
	public void setExpect(String expect) {
		this.expect = expect;
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
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

}
