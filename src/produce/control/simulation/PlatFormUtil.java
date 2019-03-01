package produce.control.simulation;

import com.eastsoft.util.DataConvert;
import dao.basedao.IBaseDao;
import produce.control.entity.BaseCommLog;

public class PlatFormUtil {


	// 因台体南网模式时，第一路设定为门节点，实际是第6路
	public static String nwFSConvert(String flag){
		String FSFlag = flag;
		// 左边两位不动  第3位移到最后面
		String FSFlag1 = FSFlag.substring(0,2)+FSFlag.substring(3)+FSFlag.substring(2,3);
		return FSFlag1;
	}
	public static String nwFSConvert1(String flag){
		String FSFlag = flag;
		String FSFlag1 = FSFlag.substring(0,3)+FSFlag.substring(3)+FSFlag.substring(2,3);
		return FSFlag1;
	}

	public static Object[] getIPParam(String meterno, String type){
		// 3、表位对应关系算法  表位1--16，表位17--32
		// 485-1
		//		n<=16 129.1.22.201:10000+n
		//		n>=17 129.1.22.202:10000+n-16
		// 485-3
		//		129.1.22.203:10000+n
		// PS2
		//		n<=16 129.1.22.204:10000+n
		//		n>=17 129.1.22.205:10000+n-16
		// INFRA
		//		n<=16 129.1.22.204:10000+n+16
		//		n>=17 129.1.22.205:10000+n
		int no = DataConvert.String2Int(meterno);
		Object[] ret = {"",0};
		if (type.equals("RJ45-0")){
			String tmp = "00"+meterno;
			tmp = tmp.substring(tmp.length()-2);
			ret[0] = "129.1.22."+tmp;
			ret[1] = 7000;
		}
		if (type.equals("RJ45-1")){
			ret[0] = "129.1.22.96";
			ret[1] = 7000;
		}
		if (type.equals("RJ45-2")){
			ret[0] = "192.168.127.96";
			ret[1] = 7000;
		}
		if (type.equals("485-1")){
			if (no <= 16){
				ret[0] = "129.1.22.201";
				ret[1] = 10000+no;
			}
			else{
				ret[0] = "129.1.22.202";
				ret[1] = 10000+no-16;
			}
		}
		if (type.equals("485-3")){
			ret[0] = "129.1.22.203";
			ret[1] = 10000+no;
		}
		if (type.equals("PS2")){
			if (no <= 16){
				ret[0] = "129.1.22.204";
				ret[1] = 10000+no;
			}
			else{
				ret[0] = "129.1.22.205";
				ret[1] = 10000+no-16;
			}
		}
		if (type.equals("INFRA")){
			if (no <= 16){
				ret[0] = "129.1.22.204";
				ret[1] = 10000+no+16;
			}
			else{
				ret[0] = "129.1.22.205";
				ret[1] = 10000+no;
			}
		}
		return ret;
	}


	public static String getPWM(String val, String type){
		String ret = "";
		if (type.indexOf("电压") >= 0 || type.indexOf("1") >= 0){

		}
		if (type.indexOf("电流") >= 0 || type.indexOf("1") >= 0){

		}
		return ret;
	}


	public static String getMetrNo(String meter){
		String ret = "";
		if (meter.equals("FF"))
			ret = "FF";
		else{
			int meterno = DataConvert.String2Int(meter)+65;
			ret = DataConvert.int2HexString(meterno, 2);
		}
		return ret;
	}

	public static Object[] getMAC(String datetime, int meter_i, String PlatStageNo) {
		IBaseDao<TerminalTestNo> iBaseDao_TeminalTestNo  = new TerminalTestNoDaoImpl();

		// 使用设备的MAC地址作为设备的唯一标示 测试前计算产生MAC地址信息，以此MAC地址信息进行唯一标识
		// MAC 02:ID:YY:MM:tmpsno/256:tmpsno%256
		// [ID]FF：高位的F表台号（1-16），低位的F表位号（0-15） 一般都是16表位的台体
		// xuky 2019.02.25 调整 111(0-7对应8个台体) 11111(0-31对应1-32表位)
		// [tmpsno] tmpsno 这个 是测试的序号 是单表位的测试序号，FFFF，前面是取整，后面是取余
		// xuky 2018.08.24 因为序号为每个表台+表位、每个月的流水号，所以需要记录到数据库中进行保存
		// 一个表台使用一个软件，多个表位共用一个软件

		Object[] ret = new Object[2];
		datetime = datetime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
		// xuky 2019.02.25  注意台体编号是从0-31的 ，如果是1-32，无法表示32
		String MeterNo = DataConvert.int2String(meter_i-1);
		int TestNo = ((TerminalTestNoDaoImpl) iBaseDao_TeminalTestNo).getNo(PlatStageNo, MeterNo, datetime.substring(2, 6)); // 当前表位的流水号
		String id1 = DataConvert.IntToBinString(DataConvert.String2Int(PlatStageNo)); // 得到32位的二进制数据
				id1 = id1.substring(32-3);
		String id2 = DataConvert.IntToBinString(DataConvert.String2Int(MeterNo));
				id2 = id2.substring(32-5);
		String id = DataConvert.binStr2HexString(id1 + id2, 2);
		String tmpsno = DataConvert.int2HexString(TestNo / 256, 2) + DataConvert.int2HexString(TestNo % 256, 2);
		String mac = "02" + id + datetime.substring(2, 6) + tmpsno;
		ret[0] = TestNo;
		ret[1] = mac;
		return ret;
	}

	public static void main(String[] args) {
//		PlatFormUtil.getPWM("1", "电压");
//		for( int i = 0; i<32;i++  ){
//			System.out.println(i);
//			String FSFlag = DataConvert.IntToBinString(i, 8);
//			String FSFlag1 = PlatFormUtil.nwFSConvert(FSFlag);
//			System.out.println(FSFlag+"\t"+FSFlag1  );
//		}

		int num = DataConvert.String2Int(PlatFormParam.getInstance().getFSNum());
		String bintxt = "";
		for( int i=1;i<=num;i++ )
			bintxt += "1";
		String data = DataConvert.binStr2HexString(bintxt, 2);
		data += "";

	}

}
