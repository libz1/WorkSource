package produce.meter;

import java.util.List;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.Debug;

import dao.basedao.IBaseDao;
import produce.meter.entity.MeterInfo;
import produce.meter.entity.MeterInfoDaoImpl;
import produce.meter.entity.MeterInfoID;
import produce.meter.entity.MeterInfoIDDaoImpl;
import produce.meter.entity.MeterLog;
import produce.meter.entity.MeterLog2MES;
import produce.meter.entity.MeterLog2MESDaoImpl;
import produce.meter.entity.MeterLogDaoImpl;
import produce.meter.entity.MeterResult;
import produce.meter.entity.MeterResultDaoImpl;
import util.SoftParameter;
import util.Util698;

public class MeterDataAnly {


	private IBaseDao<MeterInfo> iBaseDao_MeterInfo = new MeterInfoDaoImpl();
	private IBaseDao<MeterResult> iBaseDao_MeterResult = new MeterResultDaoImpl();
	private IBaseDao<MeterInfoID> iBaseDao_MeterInfoID = new MeterInfoIDDaoImpl();
	private IBaseDao<MeterLog2MES> iBaseDao_MeterLog2MES = new MeterLog2MESDaoImpl();
	private IBaseDao<MeterLog> iBaseDao_MeterLog = new MeterLogDaoImpl();


	public String[] saveMeterInfo(String[]  QRCodes,String frame, String operater){
		// ������ϢΪ�Դ�����̵����ּ�¼
		String[] ret = {"",""};
		Util698.log(MeterDataAnly.class.getName(), "�����ݿ��б�����Լ�¼����ȡ����ID��Ϣ...", Debug.LOG_INFO);

		// ��ͷȥβ�õ�����������
		String data = frame.substring(30);
		data = data.substring(0,data.length()-4);


		String tmp = data;
		String errCode, devType, dataType, IDData, opTime, status ;
		int result_id = 0, dataNum, dataLen, p_end, info_id=0, analyDataNum = 1;
		opTime = Util698.getDateTimeSSS_new();
		devType = tmp.substring(0,2);
		tmp = tmp.substring(2);
		Boolean is_qrcode_null = false;  // ����Ƿ��ȡ���˶�ά����Ϣ
		MeterResult meterResult = null;
		MeterInfo meterInfo = null;



		for( String QRCode :QRCodes  ){


			is_qrcode_null = false;
			if (QRCode.indexOf("null-") >= 0){
				// ��ʾ���ڶ�ȡ������ά���������Դ���������Ҫ���⴦��
				is_qrcode_null = true;
				ret[0] += "���"+analyDataNum+"�豸��ά��Ϊ:��"+"";
			}
			else
				ret[0] += "���"+analyDataNum+"�豸��ά��Ϊ:"+QRCode+"";

			if (!is_qrcode_null){
				// ����QRCode ��meterinfo���в�������
				List<MeterInfo> meterInfos = iBaseDao_MeterInfo.retrieve("where QRCode='" + QRCode + "'", "");
				meterInfo = (MeterInfo) Util698.getFirstObject(meterInfos);
				if (meterInfo != null) {
					info_id = meterInfo.getID();
				}
				else{
					meterInfo = new MeterInfo();
					meterInfo = iBaseDao_MeterInfo.create(meterInfo);
					info_id = meterInfo.getID();
				}

				meterResult = new MeterResult();
				meterResult.setQRCode(QRCode);
				meterResult.setOperator(SoftParameter.getInstance().getUserManager().getUserid());
				meterResult.setOptime(opTime);

				meterResult.setType(devType);
			}

			errCode = tmp.substring(2,4);
			if (errCode.equals("00")){
				errCode = "";
				status = "OK";
			}
			else {
				errCode = barCode2ErrCode(errCode,devType);
				status = "NG";
			}

			// xuky 2018.11.15 ���﷢��Լ��������豸û���룬��װ�ϱ����쳣������01�����۱�ģ�黹��·��ģ��

			if (is_qrcode_null && !errCode.equals("01"))
				ret[1] += "�쳣:���"+analyDataNum+"�豸�޶�ά�룬���ǹ�װ���Խ��в��ԣ������ά����Ϣ��\n";

			if (is_qrcode_null && errCode.equals("01"))
				ret[1] += "�쳣:���"+analyDataNum+"�豸�޶�ά�룬��װҲ��ⲻ������ȷ�ϲ����豸�Ƿ���ڣ�\n";

			if (errCode.equals(""))
				ret[0] += "	���Խ��Ϊ:" + status + "\n";
			else
				ret[0] += "	���Խ��Ϊ:" + status + ",������Ϊ:"+errCode+ "\n";
			if (!is_qrcode_null){
				meterResult.setResult(status);
				meterResult.setErrCode(errCode);
				meterResult = iBaseDao_MeterResult.create(meterResult);

				MeterLog2MES merterLog2MES = new MeterLog2MES();
				merterLog2MES.setAddr(QRCode);
				merterLog2MES.setOptime_b(opTime);
				merterLog2MES.setOptime_e("");
				merterLog2MES.setPriority("1");
				merterLog2MES.setResultID(meterResult.getID());
				merterLog2MES.setStatus("0");
				merterLog2MES.setTranstime("");
				merterLog2MES.setWebinfo("");
				iBaseDao_MeterLog2MES.create(merterLog2MES);

				meterInfo.setOpdatetime(opTime);
				meterInfo.setQRCode(QRCode);
				meterInfo.setOperater(operater);
				meterInfo.setType(devType);
				meterInfo.setStatus(status);
				meterInfo.setErrCode(errCode);
				iBaseDao_MeterInfo.update(meterInfo);
			}


			// ��ȡ����ID��Ϣ�����л�ȡ��ID��Ϣ�������
			if (errCode.equals("")){
				if (!is_qrcode_null){
					result_id = meterResult.getID();
				}
				dataNum = DataConvert.hexString2Int(tmp.substring(4,6));
				tmp = tmp.substring(6);
				for ( int i = 0 ;i< dataNum ;i++){
					dataType = tmp.substring(0,2);
					dataLen = DataConvert.hexString2Int(tmp.substring(2,4));
					p_end = 4+dataLen*2;
					IDData = tmp.substring(4,p_end);
					ret[0] += "	"+dataType+"����-ID����Ϊ:" + IDData + "\n";

					if (!is_qrcode_null){
						MeterInfoID meterInfoID = new MeterInfoID();
						meterInfoID.setResultid(result_id);
						meterInfoID.setInfoid(info_id);  // ��Ҫ����meterinfo��Ϣ
						meterInfoID.setQRCode(QRCode);
						meterInfoID.setType(dataType);
						meterInfoID.setData(IDData);
						meterInfoID.setOptime(opTime);
						iBaseDao_MeterInfoID.create(meterInfoID);
					}

					tmp = tmp.substring(p_end);
				}
			}
			else{
				tmp = tmp.substring(4);
			}
			analyDataNum ++;
		}
		// ���յ��Ķ�ά����Ϣ�ͱ��Ľ������ݼ�¼����Ϊ��û�ж�ά����Ϣʱ�������޷��洢�������
		MeterLog meterLog = new MeterLog();
		meterLog.setQRCodes(Util698.array2String(QRCodes));
		meterLog.setFrame(frame);
		meterLog.setOptime(Util698.getDateTimeSSS_new());
		meterLog.setAnalydata(ret[0]);
		iBaseDao_MeterLog.create(meterLog);

		Util698.log(MeterDataAnly.class.getName(), "�����ݿ��б�����Լ�¼����ȡ����ID��Ϣ...���", Debug.LOG_INFO);

		return ret;
	}


	public static void main(String[] args) {
//		String[]  QRCodes = {"1230001","1230002","1230003","1230004"};

		//type	����ģ�����ͣ�0-��ģ�飬1-·��ģ��
 		String data = "00"+"0101";
			data = data + "0200"+"02"+"0118"+"01029C01C1FB0245534131000001ACBF092E43B981B1E248" + "0206"+"112233445566";
			data = data + "0300"+"01"+"0118"+"01029C01C1FB0245534131000001AD4E60A617726EA74BD1";
			data = data + "0400"+"01"+"020A"+"0102030405060708090A";

		// ����ɨ�赽�Ķ�ά����Ϣ���Ͷ�ȡ�����豸��Ϣ���������ݿ�洢
//		new MeterDataAnly().saveMeterInfo(QRCodes,data,SoftParameter.getInstance().getUserManager().getUserid());

		Frame645Meter frame645 = new Frame645Meter();
		frame645.setAddr("999999999999");
		frame645.setControl("14");
		frame645.setData("FFFFEE01"+data);
		System.out.println(frame645.get645Frame());

		frame645 = new Frame645Meter();
		frame645.setAddr("999999999999");
		frame645.setControl("94");
		frame645.setData("");
		System.out.println(frame645.get645Frame());


		// ���(1Byte)+�豸����(1Byte)+errcode(1Byte)+���ݸ���(1Byte)+��������(1Byte)+���ݳ���L(1Byte)+��������(L Byte)
		// �豸����  1��ģ��  2·��ģ��  �ɴ˽���errcode�Ķ���
		// errCode = "00"��ʾ�޴���    �������ṩ
//		01 errCode
//		02 errCode ���ݸ���n
//			����1����  ����1���� ����1����
//			����n����  ����n���� ����n����
		// �������ͣ�1��ʾоƬID 2��ʾ�ʲ����
//		03 errCode
//		04 errCode

	}


	//
	private String barCode2ErrCode(String code, String devType){
		return code;
	}

}
