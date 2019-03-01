package produce.meter.entity;

public class NewDev2 {
    int ID;
    String result;
    String optime;
    String operator;
    String errcode;
    NewDev3[] IDDATA;
    public NewDev2(){
    	result = "";
    	optime = "";
    	operator = "";
    	IDDATA = new NewDev3[0];
    }

	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getOptime() {
		return optime;
	}
	public void setOptime(String optime) {
		this.optime = optime;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public NewDev3[] getIDDATA() {
		return IDDATA;
	}
	public void setIDDATA(NewDev3[] iTEMS) {
		IDDATA = iTEMS;
	}

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

}
