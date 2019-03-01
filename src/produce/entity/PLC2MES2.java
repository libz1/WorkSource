package produce.entity;

public class PLC2MES2 {
    String ID;
    String result;
    String optime;
    String operator;
    PLC2MES3[] ITEMS;
    public PLC2MES2(){
    	ID = "";
    	result = "";
    	optime = "";
    	operator = "";
    }

	public String getID() {
		return ID;
	}
	public void setID(String iD) {
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
	public PLC2MES3[] getITEMS() {
		return ITEMS;
	}
	public void setITEMS(PLC2MES3[] iTEMS) {
		ITEMS = iTEMS;
	}

}
