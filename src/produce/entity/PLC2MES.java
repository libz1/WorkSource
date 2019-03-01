package produce.entity;

import com.google.gson.Gson;

public class PLC2MES {
	String addr;
	String status;
	String errcode;
	String opdatetime;
	String operator;
	String devtype;
	String SAVEID;  // xuky 2018.10.23 ÃÌº”–æ∆¨ID–≈œ¢
	PLC2MES2[] ITEMS;
	public PLC2MES(){
		addr = "";
		status = "";
		errcode = "";
		opdatetime = "";
		operator = "";
		devtype = "";
	}

	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrcode() {
		return errcode;
	}
	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}
	public String getOpdatetime() {
		return opdatetime;
	}
	public void setOpdatetime(String opdatetime) {
		this.opdatetime = opdatetime;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getDevtype() {
		return devtype;
	}
	public void setDevtype(String devtype) {
		this.devtype = devtype;
	}
	public PLC2MES2[] getITEMS() {
		return ITEMS;
	}
	public void setITEMS(PLC2MES2[] iTEMS) {
		ITEMS = iTEMS;
	}


	public String getSAVEID() {
		return SAVEID;
	}

	public void setSAVEID(String sAVEID) {
		SAVEID = sAVEID;
	}

	public static void main(String[] args) {
		PLC2MES pLC2MES = new PLC2MES();
		pLC2MES.setAddr("1");
		pLC2MES.setITEMS(new PLC2MES2[2]);

		PLC2MES2 pLC2MES2_1 = new PLC2MES2();
		pLC2MES2_1.setID("101");
		pLC2MES.getITEMS()[0] = pLC2MES2_1;

		pLC2MES2_1.setITEMS(new PLC2MES3[2]);

		PLC2MES3 pLC2MES3_1 = new PLC2MES3();
		pLC2MES3_1.setID("123456");
		pLC2MES2_1.getITEMS()[0] = pLC2MES3_1;

		PLC2MES3 pLC2MES3_2 = new PLC2MES3();
		pLC2MES3_2.setID("654321");
		pLC2MES2_1.getITEMS()[1] = pLC2MES3_2;

		PLC2MES2 pLC2MES2_2 = new PLC2MES2();
		pLC2MES2_2.setID("102");
		pLC2MES.getITEMS()[1] = pLC2MES2_2;

		pLC2MES2_2.setITEMS(new PLC2MES3[1]);
		System.out.println(new Gson().toJson(pLC2MES));



	}


}
