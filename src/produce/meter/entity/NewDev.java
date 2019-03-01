package produce.meter.entity;

import com.google.gson.Gson;

public class NewDev {
	String QRCode;   //二维码信息
  	String status;   // 状态 OK NG
	String errcode;
	String opdatetime;
	String operator;
	String devtype;
	NewDev3[] IDDATA;  // 对应数据信息
	NewDev2[] ITEMS;  // 测试记录信息
	public NewDev(){
		QRCode = "";
		status = "";
		errcode = "";
		opdatetime = "";
		operator = "";
		devtype = "";
		ITEMS = new NewDev2[0];
		IDDATA = new NewDev3[0];
	}

	public String getQRCode() {
		return QRCode;
	}
	public void setQRCode(String QRCode) {
		this.QRCode = QRCode;
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
	public NewDev2[] getITEMS() {
		return ITEMS;
	}
	public void setITEMS(NewDev2[] iTEMS) {
		ITEMS = iTEMS;
	}



	public NewDev3[] getIDDATA() {
		return IDDATA;
	}

	public void setIDDATA(NewDev3[] iDDATA) {
		IDDATA = iDDATA;
	}

	public static void main(String[] args) {

		String data = "";

		NewDev newDev = new NewDev();
		newDev.setQRCode("123456789");
		newDev.setStatus("OK");
		newDev.setOpdatetime("2018.11.09 14:00:01");
		newDev.setOperator("admin");
		newDev.setDevtype("模块或路由");

		newDev.setIDDATA(new NewDev3[2]);
		NewDev3 NewDev3_1 = new NewDev3();
		NewDev3_1.setID("1");
		NewDev3_1.setType("1");
		NewDev3_1.setData("102030405060708090");
		newDev.getIDDATA()[0] = NewDev3_1;
		NewDev3 NewDev3_2 = new NewDev3();
		NewDev3_2.setID("2");
		NewDev3_2.setType("2");
		NewDev3_2.setData("112131415161718191");
		newDev.getIDDATA()[1] = NewDev3_2;

		newDev.setITEMS(new NewDev2[1]);
		NewDev2 NewDev2_1 = new NewDev2();
		NewDev2_1.setID(1);
		NewDev2_1.setOperator("admin");
		NewDev2_1.setOptime("2018.11.09 14:00:01");
		NewDev2_1.setIDDATA(newDev.getIDDATA());
		NewDev2_1.getIDDATA()[0] = NewDev3_1;

		newDev.getITEMS()[0] = NewDev2_1;

		System.out.println(new Gson().toJson(newDev));

	}


}
