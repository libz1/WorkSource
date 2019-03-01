package produce.control.comm;

import com.eastsoft.util.DataConvert;

import util.Util698;

public class FramePlatformOn {
	double Rated_Freq, Rated_Volt_A, Rated_Volt_B, Rated_Volt_C;
	double Rated_Curr_A, Rated_Curr_B, Rated_Curr_C;

	String PREFIX = "F9 F9 F9 F9 F9", BEGIN = "B1", END = "";
	String CS = "", DATA = "";


	public FramePlatformOn() {

	}

	public FramePlatformOn(String frame) {

	}

	public void init() {
		CS = "";
		DATA = "";
	}

	public double getRated_Freq() {
		return Rated_Freq;
	}

	public void setRated_Freq(double rated_Freq) {
		Rated_Freq = rated_Freq;
	}

	public double getRated_Volt_A() {
		return Rated_Volt_A;
	}

	public void setRated_Volt_A(double rated_Volt_A) {
		Rated_Volt_A = rated_Volt_A;
	}

	public double getRated_Volt_B() {
		return Rated_Volt_B;
	}

	public void setRated_Volt_B(double rated_Volt_B) {
		Rated_Volt_B = rated_Volt_B;
	}

	public double getRated_Volt_C() {
		return Rated_Volt_C;
	}

	public void setRated_Volt_C(double rated_Volt_C) {
		Rated_Volt_C = rated_Volt_C;
	}

	public double getRated_Curr_A() {
		return Rated_Curr_A;
	}

	public void setRated_Curr_A(double rated_Curr_A) {
		Rated_Curr_A = rated_Curr_A;
	}

	public double getRated_Curr_B() {
		return Rated_Curr_B;
	}

	public void setRated_Curr_B(double rated_Curr_B) {
		Rated_Curr_B = rated_Curr_B;
	}

	public double getRated_Curr_C() {
		return Rated_Curr_C;
	}

	public void setRated_Curr_C(double rated_Curr_C) {
		Rated_Curr_C = rated_Curr_C;
	}

	public String getPREFIX() {
		return PREFIX;
	}

	public void setPREFIX(String pREFIX) {
		PREFIX = pREFIX;
	}

	public String getBEGIN() {
		return BEGIN;
	}

	public void setBEGIN(String bEGIN) {
		BEGIN = bEGIN;
	}

	public String getEND() {
		return END;
	}

	public void setEND(String eND) {
		END = eND;
	}

	public String getDATA() {
		return DATA;
	}

	public void setDATA(String dATA) {
		DATA = dATA;
	}

	public void setCS(String cS) {
		CS = cS;
	}

	public String getCS() {
		return CS;
	}

	/**
	 * 计算CRC16校验码
	 *
	 * @param bytes
	 *            字节数组
	 * @return {@link String} 校验码
	 * @since 1.0
	 */
	public static String getCRC(byte[] bytes) {
		String ret = "";
		int CRC = 0x0000ffff;
		int POLYNOMIAL = 0x0000a001;
		int i, j;
		for (i = 0; i < bytes.length; i++) {
			CRC ^= ((int) bytes[i] & 0x000000ff);
			for (j = 0; j < 8; j++) {
				if ((CRC & 0x00000001) != 0) {
					CRC >>= 1;
					CRC ^= POLYNOMIAL;
				} else {
					CRC >>= 1;
				}
			}
		}
		ret =  Integer.toHexString(CRC);
		//交换高低位
        return (ret.substring(2, 4) + ret.substring(0, 2)).toUpperCase();
	}


	public String BuildCS() {

		String str = BuildFrameNoCS();
		byte[] str_byte = Util698.String2ByteArray(str);
		CS = getCRC(str_byte);

		return CS;
	}

	private String BuildFrameNoCS() {
		String ret = "";
		// "10 00 02 00 10 20 13 88 55 F0 55 F0 55 F0 00 00 27 10 00 00 27 10 00 00 27 10 00 00 00 00 00 00 2E E0 5D C0 00 00");
//		1.13 88: 转成10进制=5000     表示50.00HZ
		DATA = "10 00 02 00 10 20 "
				+ DataConvert.String2HexString(Util698.Double2String(Rated_Freq * 100),4);
//		2.55 F0 55 F0 55 F0  转成10进制=22000，22000，22000  表示A/B/C三相都升220.00V
		DATA +=  DataConvert.String2HexString(Util698.Double2String(Rated_Volt_A * 100),4);
		DATA +=  DataConvert.String2HexString(Util698.Double2String(Rated_Volt_B * 100),4);
		DATA +=  DataConvert.String2HexString(Util698.Double2String(Rated_Volt_C * 100),4);
//		3. 00 00 27 10 00 00 27 10 00 00 27 10  转成10进制=10000,10000,10000 表示A/B/C三相电流都是1.0000A
		DATA +=  DataConvert.String2HexString(Util698.Double2String(Rated_Curr_A * 10000),8);
		DATA +=  DataConvert.String2HexString(Util698.Double2String(Rated_Curr_B * 10000),8);
		DATA +=  DataConvert.String2HexString(Util698.Double2String(Rated_Curr_C * 10000),8);

		DATA += "00 00 00 00 00 00 2E E0 5D C0 00 00";
		ret = BEGIN + DATA;
		return ret.replaceAll(" ", "");
	}

	public String getFrame() {
		String ret = "";
		BuildCS();
		ret = PREFIX + BuildFrameNoCS() + CS + END;
		return Util698.seprateString(ret.replace(" ", ""), " ");

	}

	public static void main(String[] arg) {
		// 切换台体为南网模式
		// 主机：01H+地址(A——Z) +长度+4BH(命令)+30H/31H/32H+校验位+结束(17H)
		// 从机：01H+地址(A——Z) +长度+06H/15H+校验位+结束(17H)
		// 30H: 南网公变 31H:南网配变/集中器 32H:国网专变/集中器
		// 地址使用固定的41
		FramePlatformOn framePlatform = new FramePlatformOn();
		framePlatform.setRated_Freq(50.00);
		framePlatform.setRated_Volt_A(220.00);
		framePlatform.setRated_Volt_B(220.00);
		framePlatform.setRated_Volt_C(220.00);
		framePlatform.setRated_Curr_A(1.0000);
		framePlatform.setRated_Curr_B(1.0000);
		framePlatform.setRated_Curr_C(1.0000);
		System.out.println(framePlatform.getFrame());

		framePlatform.setRated_Freq(50.00);
		framePlatform.setRated_Volt_A(0.00);
		framePlatform.setRated_Volt_B(0.00);
		framePlatform.setRated_Volt_C(0.00);
		framePlatform.setRated_Curr_A(0.0000);
		framePlatform.setRated_Curr_B(0.0000);
		framePlatform.setRated_Curr_C(0.0000);
		System.out.println(framePlatform.getFrame());

		framePlatform.setRated_Freq(50.00);
		framePlatform.setRated_Volt_A(220.00);
		framePlatform.setRated_Volt_B(0.00);
		framePlatform.setRated_Volt_C(0.00);
		framePlatform.setRated_Curr_A(1.0000);
		framePlatform.setRated_Curr_B(0.0000);
		framePlatform.setRated_Curr_C(0.0000);
		System.out.println(framePlatform.getFrame());
//		String str = BuildFrameNoCS();
//		str = "01";
//		byte[] str_byte = Util698.String2ByteArray(str);
//		System.out.println(str +" CS:"+getCRC(str_byte));
//		str = BuildFrameNoCS();
//		str_byte = Util698.String2ByteArray(str);
//		System.out.println(str +" CS:"+getCRC(str_byte));
//		str = "01 02 7E 80";
//		str_byte = Util698.String2ByteArray(str);
//		System.out.println(str +" CS:"+getCRC(str_byte));
//		str = "01 02 7E 80 80 18 ";
//		str_byte = Util698.String2ByteArray(str);
//		System.out.println(str +" CS:"+getCRC(str_byte));

	}
}
