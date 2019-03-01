package produce.control.comm;

import com.eastsoft.util.DataConvert;

import produce.control.simulation.PlatFormUtil;
import util.Util698;

public class FramePlatform {
	String PREFIX = "FE FE FE FE FE",BEGIN = "01", END = "17";
	int FRAMELEN = 0;
	String ADDR = "", CS = "", CONTROL = "",DATA = "";
	public FramePlatform(){

	}
	public FramePlatform(String frame){

	}

	public void init(){
		ADDR = "";
		CS = "";
		CONTROL = "";
		DATA = "";
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


	public int getFRAMELEN() {
		return FRAMELEN;
	}


	public void setFRAMELEN(int fRAMELEN) {
		FRAMELEN = fRAMELEN;
	}


	public String getADDR() {
		return ADDR;
	}


	public void setADDR(String aDDR) {
		ADDR = PlatFormUtil.getMetrNo(aDDR);
	}


	public String getCONTROL() {
		return CONTROL;
	}


	public void setCONTROL(String cONTROL) {
		CONTROL = cONTROL;
	}


	public String getDATA() {
		return DATA;
	}

	public void setDATA(String dATA) {
		// xuky 2019.02.14 �滻���ܳ��ֵĿո���Ϣ
		DATA = dATA.replaceAll(" ", "");
	}

	public void setCS(String cS) {
		CS = cS;
	}

	public String getCS() {
		return CS;
	}
	public int BuildFrameLen(){
		FRAMELEN = 6 + DATA.length() /2 ;
		return FRAMELEN;
	}

	public String BuildCS(){
		BuildFrameLen();

		String str = BuildFrameNoCS();

		int num = str.length() / 2;
		String sTemp = "";
		String aStr = "";
		int aTemp = 0;
		for (int i = 0; i < num; i++) {
			sTemp = str.substring(i * 2, (i + 1) * 2);
			aTemp = aTemp + Integer.valueOf(sTemp, 16);
		}
		aStr = DataConvert.int2HexString(aTemp,2);
		CS = aStr.substring(aStr.length() - 2, aStr.length());
		CS = CS.toUpperCase();

		return CS;
	}

	private String BuildFrameNoCS() {
		String ret = "";
		ret = CONTROL + DATA;
		return ret.replaceAll(" ", "");
	}
	public String getFrame(){
		String ret = "";
		BuildCS();
		ret = PREFIX + BEGIN + ADDR + DataConvert.int2HexString(FRAMELEN,2) + BuildFrameNoCS() + CS + END;
		return Util698.seprateString(ret.replace(" ", ""), " ");

	}

	public static void main(String[] arg){
		// �л�̨��Ϊ����ģʽ
//		������01H+��ַ(A����Z) +����+4BH(����)+30H/31H/32H+У��λ+����(17H)
//		�ӻ���01H+��ַ(A����Z) +����+06H/15H+У��λ+����(17H)
//		   30H: ��������  31H:�������/������  32H:����ר��/������
//		��ַʹ�ù̶���41
		FramePlatform framePlatform = new FramePlatform();
		framePlatform.setADDR("41");
		framePlatform.setCONTROL("4B");
		framePlatform.setDATA("30");
		System.out.println(framePlatform.getFrame());
	}
}
