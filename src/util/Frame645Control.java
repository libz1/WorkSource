package util;

import com.eastsoft.util.DataConvert;

/**
 * ���Э��645��������.
 * <p>
 * ���ĵĸ�����ɲ��� ���зֽ����ϣ��������ݺͽ���������Ҫʹ��
 *
 * @author xuky
 * @version 2013-10-27
 *
 */
public class Frame645Control {

	String Addr; // ��ַ�� ����������� ʹ��ʱ����
	String Control; // ������
	String DataLen; // ���ݳ���
	String Data; // ������ ����������� ʹ��ʱ����
	String Data_item; // ������ �е� ���ݱ�ʶ����
	String Data_data; // ������ �е� ���ݱ�ʶ��Ӧ����
	String CS; // ��֤��
	String Err; // �쳣��־
	String Coll; // �ɼ�����ַ
	String Coll_surffix; // �ɼ�����ַ��������ͷ��1��β��0

	public String getColl_surffix() {
		return Coll_surffix;
	}

	public void setColl_surffix(String coll_surffix) {
		Coll_surffix = coll_surffix;
	}

	public String getColl() {
		return Coll;
	}

	public void setColl(String coll) {
		Coll = coll;
	}

	// ���캯��������645���ģ��õ�����
	public Frame645Control(String frame, String coll_param, String suffix) {

		frame = frame.replaceAll(" ", "");

		// xuky 2014.09.29�����ִ���
		setControl(frame.substring(16, 18));

		// xuky 2014.09.26 �õ������ֵ�ÿһλ��Ϣ
		String ControlBin = DataConvert.hexString2BinString(Control, 8);
		// ���ݿ������ж��Ƿ��쳣
		Boolean abnormal = false;
		if (ControlBin.substring(1, 2).equals("1"))
			abnormal = true;

		// xuky 2014.09.29���ݳ��ȴ���
		DataLen = DataConvert.hexString2String(frame.substring(18, 20), 3);

		// �Ա�����ȷ�Խ����ж�
		if ((frame.substring(0, 2).equals("68") && frame.substring(
				frame.length() - 2, frame.length()).equals("16"))) {
			// 68 + 12λ���ַ + 68 + C + ���ݱ�ʶ��97���� 2�ֽ� ��07���� 4�ֽ� �� +
			// �������ݣ�����C�����ݱ�ʶ��ͬ����ͬ��

			// xuky 2014.09.19 ���ݲɼ���Э�飬���вɼ�����ر��Ĵ���
			if (coll_param.equals("")) {
				// ��������1
				Addr = DataConvert.reverseString(frame.substring(2, 14));
				Data = frame
						.substring(20, 20 + DataConvert.hexString2Int(frame
								.substring(18, 20)) * 2);
				Coll = "";
				// 878687833E34333333333334373A6376
//				Data_item = DataConvert.reverseString(DataConvert.HexStrReduce33H(Data.substring(0,8)));
//				Data_data = DataConvert.reverseString(DataConvert.HexStrReduce33H(Data.substring(8)));
				Data_item = Data.substring(0,8);
				Data_data = Data.substring(8);
			} else {
				// xuky 2014.09.29 ����ɼ��������쳣���ģ����쳣���ĵĳ���Ϊ1��������������������ģ��޵���ַ
				if (abnormal && DataLen.equals("001")) {
					// ������ʽͬ����������1��
					Addr = DataConvert.reverseString(frame.substring(2, 14));
					Data = frame.substring(20, 20 + DataConvert
							.hexString2Int(frame.substring(18, 20)) * 2);
				} else {
					Coll = DataConvert.reverseString(frame.substring(2, 14));
					if (suffix.equals("1")) {
						Addr = DataConvert.reverseString(frame.substring(
								frame.length() - 4 - 12, frame.length() - 4));
						// ��12���ַ��ǵ���ַ����ȡ��
						Data = frame.substring(20,
								20 + DataConvert.hexString2Int(frame.substring(
										18, 20)) * 2 - 12);
					} else {
						Addr = DataConvert.reverseString(frame.substring(20,
								20 + 12));
						// ǰ12���ַ��ǵ���ַ����ȡ��
						Data = frame.substring(20 + 12, 20 + DataConvert
								.hexString2Int(frame.substring(18, 20)) * 2);
					}
				}
			}

			String Data0 = Data;
			Data = DataConvert.HexStrReduce33H(Data);

			if (Control.equals("91")) {
				// �����ݵĻظ�
				Data_item = DataConvert.reverseString(Data.substring(0, 8));
				Data_data = DataConvert.reverseString(Data.substring(8,
						Data.length()));

			} else if (Control.equals("81")) {
				// �����ݵĻظ�
				Data_item = DataConvert.reverseString(Data.substring(0, 4));
				Data_data = DataConvert.reverseString(Data.substring(4,
						Data.length()));

			} else if (Control.equals("11")) {
				// ������
				Data_item = DataConvert.reverseString(Data);
				Data_data = "";
				Data = DataConvert.reverseString(Data);

			} else if (Control.equals("01")) {
				// ������
				Data_item = DataConvert.reverseString(Data);
				Data_data = "";
				Data = DataConvert.reverseString(Data);

			} else if (ControlBin.substring(1, 2).equals("1")) {
				// xuky 2014.09.26 ���ݿ����ֵ������ж��Ƿ��쳣����
				// �����ݵ��쳣�ظ�
				Data_item = "������";
				// Data = Data0;
				if (!Data0.equals("80"))
					Data_data = DataConvert.hexString2BinString(Data, 8);
				else
					Data_data = Data0;

			} else {

			}
		}
	}

	public Frame645Control() {
		init();
	}

	private void init(){
		Addr = "";
		Control = "";
		DataLen = "";
		Data = "";
		Data_item = "";
		Data_data = "";
		Err = "0";
		Coll = "";
	}

	public Frame645Control(String frame) {
		// �����Ľ���Ϊ������ɲ���
		//68000081011660681404049696051516
		init();
		Addr = frame.substring(2,14);
		Control = frame.substring(16,18);
		Data_item = frame.substring(20,28);
		int len = DataConvert.hexString2Int(frame.substring(18,20));
		DataLen = DataConvert.int2String(len);
		if (len > 4)
			Data_data = frame.substring(28,(len-4)*2+28);
	}

	public String get645FrameNoCS() {
		// �õ�����CS֮�����������
		String aStr, aAddr, aData, aColl;
		aAddr = formatAddr(Addr); // ���򣬲���Ϊ���ʵ�Ϊ��������33H
		aAddr = DataConvert.reverseString(aAddr);
		// �ɼ������ͱ���
		aColl = formatAddr(Coll);
		// xky 2014.09.19 �ɼ������ͱ��ģ�����ַ������ǰ�������ں�
		if (aColl.equals("")){
//			aData = formatData(Data_item) + formatData(Data_data);
			aData = Data_item + Data_data;
		}
//			aData = formatData(Data);
		else {
			// xuky 2014.09.28 ����Ĵ��벻�������
			// formatData(DataConvert.reverseString(aAddr))
			// ��ʾ��������ַ������λ�����������ȵ���Ȼ���33H���ٵ���
			if (Coll_surffix.equals("1")) {
				aData = formatData(Data)
						+ formatData(DataConvert.reverseString(aAddr));
			} else {
				aData = formatData(DataConvert.reverseString(aAddr))
						+ formatData(Data);
			}
		}
		aData = getDataLen(aData);
		// 68 02 00 00 00 00 00 68 01 08 34 33 33 33 33 33 43 C3 14 16

		// �ɼ������ͱ���
		if (Coll.equals(""))
			aStr = "68" + aAddr + "68" + Control + DataLen + aData;
		else
			aStr = "68" + aColl + "68" + Control + DataLen + aData;

		return aStr;
	};

	public String get645Frame() {
		String aStr;
		getCS();
		aStr = get645FrameNoCS() + CS + "16";
		return aStr;
	};

	/**
	 * ��ʽ����ַ�� �����䳤�� �̶�12λ��6���ֽ�
	 *
	 * @return <code>String</code>���ص������Ѿ�����
	 *
	 */
	public String formatAddr(String str) {
		// xuky 2015.01.14 �Կ����ݽ��д���
		if (str==null || str.equals("")){
			return "";
		}
		else{
			String aStr = "000000000000" + str;
			aStr = DataConvert.reverseString(aStr.substring(aStr.length() - 12,
					aStr.length()));
			return aStr;
		}
	};

	/**
	 * ��ʽ��������(��������+33H ����).
	 * <p>
	 * ��ʱ������д��������Ҫ�������롢�����ߴ��롢�������ݣ�
	 *
	 * @return <code>String</code>���ص�����
	 *
	 */
	public static String formatData(String str) {
		String sTemp = "";
		String aStr = "";
		int num = str.length() / 2;
		int temp = 0;
		for (int i = 0; i < num; i++) {
			sTemp = str.substring(i * 2, (i + 1) * 2);
			temp = Integer.valueOf(sTemp, 16) + 51;
			aStr = DataConvert.int2HexString(temp, 2) + aStr;
			// aStr = Integer.toHexString(temp) + aStr;

		}
		aStr = aStr.toUpperCase();
		return aStr;
	};

	public String getAddr() {
		return Addr;
	}

	public void setAddr(String addr) {
		addr = "000000000000"+addr;
		Addr = addr.substring(addr.length()-12,addr.length());
	}

	public String getControl() {
		return Control;
	}

	public String getControlAnalyse() {
		String ret = "";

		if (Control.equals("01"))
			ret = "645-97������";
		if (Control.equals("81"))
			ret = "645-97�����ݻظ�";
		if (Control.equals("C1"))
			ret = "645-97�����ݻظ��쳣";
		if (Control.equals("04"))
			ret = "645-97д����";
		if (Control.equals("84"))
			ret = "645-97д���ݻظ�";
		if (Control.equals("0A"))
			ret = "645-97д�豸��ַ";
		if (Control.equals("8A"))
			ret = "645-97д�豸��ַ�ظ�";

		if (Control.equals("11"))
			ret = "645-07������";
		if (Control.equals("91"))
			ret = "645-07�����ݻظ�";
		if (Control.equals("D1"))
			ret = "645-07�����ݻظ��쳣";
		if (Control.equals("14"))
			ret = "645-07д����";
		if (Control.equals("94"))
			ret = "645-07д���ݻظ�";
		if (Control.equals("13"))
			ret = "645-07��ͨ�ŵ�ַ\\MAC��ַ";
		if (Control.equals("93"))
			ret = "645-07��ͨ�ŵ�ַ\\MAC��ַ�ظ�";
		if (Control.equals("15"))
			ret = "645-07дͨ�ŵ�ַ\\MAC��ַ";
		if (Control.equals("95"))
			ret = "645-07дͨ�ŵ�ַ\\MAC��ַ�ظ�";

		if (Control.equals("08"))
			ret = " �㲥Уʱ";

		return ret;
	}

	public void setControl(String control) {
		Control = control;
		// ����Control��ͬʱ�������쳣��־
		String str = DataConvert.hexString2BinString(Control, 8);
		if (str.substring(1, 2).equals("1"))
			Err = "1";
		else
			Err = "0";
	}

	public String getDataLen(String aData) {
		if (Data_item.equals("")){
			// xuky 2019.02.27 1�������ⲿ�����Ĵ������壬2����ȷ��������Ϊ00
			return "00";
		}
		int num;
		if (Coll.equals(""))
			num = (Data_item.length()+Data_data.length()) / 2;
		else
			num = Data.length() / 2 + 6;

		DataLen = "00" + Integer.toHexString(num);
		DataLen = DataLen.substring(DataLen.length() - 2, DataLen.length());
		DataLen = DataLen.toUpperCase();
		return aData;
	}

	public void setDataLen(String dataLen) {
		DataLen = dataLen;
	}

	public String getData() {
		return Data;
	}

	public void setData(String data) {
		Data = data;
	}

	public String getErr() {
		return Err;
	}

	public void setErr(String err) {
		Err = err;
	}

	public String getCS() {
		CS = "00";
		String str = get645FrameNoCS();

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

	public void setCS(String cs) {
		CS = cs;
	}

	public String getData_item() {
		return Data_item;
	}

	public void setData_item(String data_item) {
		Data_item = data_item.replaceAll(" ","");
	}
	public String getData_data() {
		String ret = getData_data0();
		if (ret.equals(""))
			ret = Data_data;
		return ret;
	}

	public String getData_data0() {
		// xuky 2014.09.19 ����쳣�ж�
		try {
			String ControlBin = DataConvert.hexString2BinString(Control, 8);
			if (Control.equals("81") || Control.equals("91")) {
				if (Data_item.equals("00010000") || Data_item.equals("9010"))
					return Data_data.substring(0, 6) + "."
							+ Data_data.substring(6, 8);
				else if (Data_item.equals("04000101")
						|| Data_item.equals("C010"))
					return "20" + Data_data.substring(0, 2) + "-"
							+ Data_data.substring(2, 4) + "-"
							+ Data_data.substring(4, 6) + " ��"
							+ Data_data.substring(6, 8);
				else if (Data_item.equals("04000102")
						|| Data_item.equals("C011"))
					return Data_data.substring(0, 2) + ":"
							+ Data_data.substring(2, 4) + ":"
							+ Data_data.substring(4, 6);
				else if (Data_item.equals("0001FF00")
						|| Data_item.equals("0002FF00")
						|| Data_item.equals("901F") || Data_item.equals("902F")
						|| Data_item.equals("9A1F")
						|| Data_item.equals("0001FF01")
						|| Data_item.equals("0002FF01")
						|| Data_item.equals("05060101")
						|| Data_item.equals("05060201")) {
					String str = DataConvert.reverseString(Data_data);
					String s = "", ret = "";
					s = str.substring(0, 8);
					s = DataConvert.reverseString(s);
					ret = "�ܼ�0 " + s.substring(0, 6) + "." + s.substring(6, 8)
							+ "\r\n";
					if (str.length() >= 16) {
						s = str.substring(8, 16);
						s = DataConvert.reverseString(s);
						ret = ret + "����1 " + s.substring(0, 6) + "."
								+ s.substring(6, 8) + "\r\n";
						if (str.length() >= 24) {
							s = str.substring(16, 24);
							s = DataConvert.reverseString(s);
							ret = ret + "����2 " + s.substring(0, 6) + "."
									+ s.substring(6, 8) + "\r\n";
							if (str.length() >= 32) {
								s = str.substring(24, 32);
								s = DataConvert.reverseString(s);
								ret = ret + "����3 " + s.substring(0, 6) + "."
										+ s.substring(6, 8) + "\r\n";
								if (str.length() >= 40) {
									s = str.substring(32, 40);
									s = DataConvert.reverseString(s);
									ret = ret + "����4 " + s.substring(0, 6)
											+ "." + s.substring(6, 8);

								}
							}
						}
					}
					return ret;
				} else if (Data_item.equals("03300D01")) {
					String str = DataConvert.reverseString(Data_data);
					String ret = "��:"
							+ DataConvert.reverseString(str.substring(0, 12));
					ret = ret + "ֹ:"
							+ DataConvert.reverseString(str.substring(12, 24));
					return ret;
				} else
					return Data_data;
			} else if (ControlBin.substring(1, 2).equals("1")) {
				// ���� �������� ��ʱ������ ��ʱ������ ͨѶ���ʲ��ܸ��� �����/δ��Ȩ ���������� ��������
				String ret = Data_data;
				if (Data_data.equals("80")) {
					ret = "�����쳣-80";
				} else {
					if (Data_data.substring(0, 1).equals("1"))
						ret = "����ʧ�� " + ret;
					if (Data_data.substring(1, 2).equals("1"))
						ret = "�������� " + ret;
					if (Data_data.substring(2, 3).equals("1"))
						ret = "��ʱ������ " + ret;
					if (Data_data.substring(3, 4).equals("1"))
						ret = "��ʱ������ " + ret;
					if (Data_data.substring(4, 5).equals("1"))
						ret = "ͨѶ���ʲ��ܸ��� " + ret;
					if (Data_data.substring(5, 6).equals("1"))
						ret = "�����δ��Ȩ " + ret;
					if (Data_data.substring(6, 7).equals("1"))
						ret = "���������� " + ret;
					if (Data_data.substring(7, 8).equals("1"))
						ret = "�������� " + ret;

				}
				return ret;

			} else if (Control.equals("93")) {
				return Addr;
			}
			return "";

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

	public void setData_data(String data_data) {
		Data_data = data_data.replaceAll(" ","");
	}

	public static void main(String[] args) {
//		Frame645Control frame645 = new Frame645Control();
//		frame645.setAddr(Util698.StrIP2HEX("129.1.22.96"));
//		frame645.setControl("14");
//		frame645.setData_item("04 96 96 01");
//
//		frame645.setData_data(Util698.StrIP2HEX("129.1.22.1")+"02 03 04 05 06 CC");
//		System.out.println(frame645.get645Frame());

//		String str = "68000081011660681404049696051516";
//		String str = "6800008101166068940A04969605190114100220FB16";
		String str = "6800008101166068140A049696041901141000419916";

		System.out.println(str);
		Frame645Control frame645 = new Frame645Control(str);
		str = frame645.get645Frame();
		System.out.println(str);
	}

}
