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
		// 返回信息为对处理过程的文字记录
		String[] ret = {"",""};
		Util698.log(MeterDataAnly.class.getName(), "向数据库中保存测试记录及读取到的ID信息...", Debug.LOG_INFO);

		// 掐头去尾得到数据域内容
		String data = frame.substring(30);
		data = data.substring(0,data.length()-4);


		String tmp = data;
		String errCode, devType, dataType, IDData, opTime, status ;
		int result_id = 0, dataNum, dataLen, p_end, info_id=0, analyDataNum = 1;
		opTime = Util698.getDateTimeSSS_new();
		devType = tmp.substring(0,2);
		tmp = tmp.substring(2);
		Boolean is_qrcode_null = false;  // 标记是否读取到了二维码信息
		MeterResult meterResult = null;
		MeterInfo meterInfo = null;



		for( String QRCode :QRCodes  ){


			is_qrcode_null = false;
			if (QRCode.indexOf("null-") >= 0){
				// 表示存在读取不到二维码的情况，对此类数据需要特殊处理
				is_qrcode_null = true;
				ret[0] += "序号"+analyDataNum+"设备二维码为:空"+"";
			}
			else
				ret[0] += "序号"+analyDataNum+"设备二维码为:"+QRCode+"";

			if (!is_qrcode_null){
				// 根据QRCode 从meterinfo表中查找数据
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

			// xuky 2018.11.15 与孙发东约定，如果设备没有秒，则工装上报的异常代码是01，无论表模块还是路由模块

			if (is_qrcode_null && !errCode.equals("01"))
				ret[1] += "异常:序号"+analyDataNum+"设备无二维码，但是工装可以进行测试，请检查二维码信息！\n";

			if (is_qrcode_null && errCode.equals("01"))
				ret[1] += "异常:序号"+analyDataNum+"设备无二维码，工装也检测不到，请确认测试设备是否存在！\n";

			if (errCode.equals(""))
				ret[0] += "	测试结果为:" + status + "\n";
			else
				ret[0] += "	测试结果为:" + status + ",错误码为:"+errCode+ "\n";
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


			// 获取主表ID信息，进行获取的ID信息保存操作
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
					ret[0] += "	"+dataType+"类型-ID数据为:" + IDData + "\n";

					if (!is_qrcode_null){
						MeterInfoID meterInfoID = new MeterInfoID();
						meterInfoID.setResultid(result_id);
						meterInfoID.setInfoid(info_id);  // 需要新增meterinfo信息
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
		// 将收到的二维码信息和报文进行数据记录，因为当没有二维码信息时，将会无法存储相关数据
		MeterLog meterLog = new MeterLog();
		meterLog.setQRCodes(Util698.array2String(QRCodes));
		meterLog.setFrame(frame);
		meterLog.setOptime(Util698.getDateTimeSSS_new());
		meterLog.setAnalydata(ret[0]);
		iBaseDao_MeterLog.create(meterLog);

		Util698.log(MeterDataAnly.class.getName(), "向数据库中保存测试记录及读取到的ID信息...完成", Debug.LOG_INFO);

		return ret;
	}


	public static void main(String[] args) {
//		String[]  QRCodes = {"1230001","1230002","1230003","1230004"};

		//type	测试模块类型：0-表模块，1-路由模块
 		String data = "00"+"0101";
			data = data + "0200"+"02"+"0118"+"01029C01C1FB0245534131000001ACBF092E43B981B1E248" + "0206"+"112233445566";
			data = data + "0300"+"01"+"0118"+"01029C01C1FB0245534131000001AD4E60A617726EA74BD1";
			data = data + "0400"+"01"+"020A"+"0102030405060708090A";

		// 根据扫描到的二维码信息，和读取到的设备信息，进行数据库存储
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


		// 序号(1Byte)+设备类型(1Byte)+errcode(1Byte)+数据个数(1Byte)+数据类型(1Byte)+数据长度L(1Byte)+数据内容(L Byte)
		// 设备类型  1表模块  2路由模块  由此进行errcode的对照
		// errCode = "00"表示无错误    其他请提供
//		01 errCode
//		02 errCode 数据个数n
//			数据1类型  数据1长度 数据1内容
//			数据n类型  数据n长度 数据n内容
		// 数据类型：1表示芯片ID 2表示资产编号
//		03 errCode
//		04 errCode

	}


	//
	private String barCode2ErrCode(String code, String devType){
		return code;
	}

}
