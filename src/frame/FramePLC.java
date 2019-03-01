package frame;

import com.eastsoft.util.DataConvert;

// 用于与PLC进行通信的协议类
public class FramePLC {
	String beginByte = "68";
	// 报文长度
	int LEN = 0;
	// 控制字对象 默认为 PLC发出的启动测试报文
	String ControlData = "";
	public String getControlData() {
		return ControlData;
	}

	String DATA = ""; // 数据域

	// 设备信息
	int DEVNUM = 6;  // 数量信息
	String[] DEVADDR = {"","","","","",""};   // 地址信息

	public String[] getDEVADDR() {
		return DEVADDR;
	}

	String[] DEVSTATUS = {"","","","","",""}; // 设备状态

	String NoCSDATA = "";

	// 验证码
	String CS = "";

	String endByte = "16";

	public FramePLC(String control, String data) {
		ControlData = control;
		DATA = data.replaceAll(" ", "");
		dealDATA();
	}

	public FramePLC(String frameDATA) {
		frameDATA = frameDATA.replaceAll(" ", "");
		ControlData = frameDATA.substring(8, 10);
		LEN = DataConvert.hexString2Int(frameDATA.substring(2, 4));
		DATA = frameDATA.substring(10, 10 + LEN * 2 - 2);
		dealDATA();
	}

	// 对数据域的数据进行更进一步的数据处理
	private void dealDATA() {
		String str = DATA;
		if (ControlData.equals("01")){
			for( int i = 0;i<6;i++ ){
				int addrLen = DataConvert.hexString2Int(str.substring(2, 4));
				if (addrLen == 0)
					DEVADDR[i] = "";
				else
					DEVADDR[i] = DataConvert.asciiHex2String(str.substring(4,addrLen*2+4));
				// xuky 2018.07.10
//				at produce.deal.TerminalParameterController.dealPLCData(TerminalParameterController.java:887)
//				Exception in thread "Thread-751" java.lang.NullPointerException
				if (DEVADDR[i] == null)
					DEVADDR[i] = "";
				str = str.substring(addrLen*2+4);
			}
		}
		if (ControlData.equals("02")){
			for( int i = 0;i<6;i++ ){
//				DEVSTATUS[i] = str.substring(2,4);
				// xuky 2018.05.04 防止意外数据出现
				if (str.length() > 4)
					str = str.substring(4);
			}
		}
	}

	public String getFrame() {
		String ret = "";
		getLEN();
		NoCSDATA = beginByte + DataConvert.int2HexString(LEN, 2) + DataConvert.int2HexString(LEN, 2) + beginByte;
		NoCSDATA = NoCSDATA + ControlData + DATA;
		getCS();
		ret = NoCSDATA + CS + endByte;
		return ret;
	}

	private int getLEN() {
		LEN = DATA.length() / 2 + 1;
		return LEN;
	}

	private String getCS() {
		CS = "00";
		String str = NoCSDATA;

		int num = str.length() / 2;
		String sTemp = "";
		String aStr = "";
		int aTemp = 0;
		for (int i = 0; i < num; i++) {
			sTemp = str.substring(i * 2, (i + 1) * 2);
			aTemp = aTemp + Integer.valueOf(sTemp, 16);
		}
		aStr = "0000" + Integer.toHexString(aTemp);
		CS = aStr.substring(aStr.length() - 2, aStr.length());
		CS = CS.toUpperCase();

		return CS;
	}

	public static void main(String[] args) {
		String control = "01";
//		String data = "01 06 201801130013" + "02 06 201801130014" + "03 06 201801130015" + "04 06 201801130017"
//				+ "05 06 20180113001A" + "06 06 000000000000";
//		String data = "010620180113001302062018011300140306201801130015040600000000000005060000000000000606000000000000";\
//		String data = "01 18 30333030315A43303030303030323137303036353836363602 18 30333030315A43303030303030323137303036353836363703 0004 0005 0006 00";
		String data = "01 06 303938303631 0206 303938303632 0306 303938303633 0406 303938303634 0500 0600";


		FramePLC framePLC = null;
		framePLC = new FramePLC(control, data);
		System.out.println(framePLC.getFrame());
//
//		control = "81";
//		data = "";
//		framePLC = new FramePLC(control, data);
//		System.out.println(framePLC.getFrame());
//
//		// framePLC = new FramePLC("68010168815316");
//		// System.out.println(framePLC.getFrame());
//
//		framePLC = new FramePLC(
//				"68313168010106201801130013020620180113001403062018011300150406201801130017050620180113001A06062018011300FFA016");
//		System.out.println(framePLC.getFrame());

//		control = "02";
//		data = "01 01" + "02 02" + "03 00" + "04 00" + "05 00" + "06 00";
//		framePLC = new FramePLC(control, data);
//		System.out.println(framePLC.getFrame());

	}

}
