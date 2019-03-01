package frame;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.eastsoft.util.DataConvert;

public class DealTypeCode {

	public static String getVal(String data,String code){
		String ret = "";
		try{
			if (code.indexOf("-") >= 0)
				code = code.split("-")[0];

			ret = getValByComma(data,code);
			if (!ret.equals(""))
				return ret;
			ret = getValBySemicolon(data,code);
			if (!ret.equals(""))
				return ret;
			ret = getValByComma1(data,code);
		}
		catch(Exception e){
			e.printStackTrace();
			return "";
		}
		// 去掉ret左右的特殊字符 回车、换行、制表符等
		String regex = "[^a-zA-Z_\u4e00-\u9fa5]";
		ret = ret.replaceAll(regex, "");
		return ret;
	}

	// 0："分"状态；1："合"状态
	public static String getValBySemicolon(String data,String code){
		final String[] retList = {""};
		if (code.indexOf(",") >=0 ) return retList[0];
		if (code.equals("") ) return retList[0];
		final String endCode = DataConvert.int2String(DataConvert.hexString2Int(code));
		data = data.replaceAll("；", ";");
		data = data.replaceAll("：", ":");
		String[] array = data.split(";");
		List<String> list = Arrays.asList(array);
		final Optional<String> opt_found =
				list.stream()
				.filter(str -> str.indexOf(":")>0)
				.filter(str -> str.split(":")[0].equals(endCode))
				.findFirst();
		opt_found.ifPresent(name -> retList[0] = name.split(":")[1]);
		return retList[0];
	}

	//无电能表[0]NULL，全部用户地址[1]NULL，一组用户类型[2]SEQUENCEOFunsigned，一组用户地址[3]SEQUENCEOFTSA，一组配置序号[4]SEQUENCEOFlong-unsigned，一组用户类型区间[5]SEQUENCEOFRegion，一组用户地址区间[6]SEQUENCEOFRegion，一组配置序号区间[7]SEQUENCEOFRegion
	public static String getValByComma1(String data,String code){
		final String[] retList = {""};
		if ((code.indexOf(",")) >= 0) return retList[0];
		if (code.equals("") ) return retList[0];

		data = data.replaceAll("，", ",");
		data = data.replaceAll("（", "(");
		data = data.replaceAll("）", ")");
		data = data.replaceAll(" ", "");

		final String endCode = DataConvert.int2String(DataConvert.hexString2Int(code));
		data = data.replaceAll("，", ",");
		data = data.replaceAll(" ", "");
		String[] array = data.split(",");
		List<String> list = Arrays.asList(array);
		// 进行index判断的时候，不能添加"\\转义符" ；进行split操作的时候，需要添加"\\转义符"
		// 首先进行判断是否存在[和]，然后再进行split操作
		final Optional<String> opt_found =
				list.stream()
				.filter(str -> str.indexOf("[")>0)
				.filter(str -> str.indexOf("]")>0)
				.filter(str -> str.split("\\[")[1].split("\\]")[0].equals(endCode))
				.findFirst();
		// lambda的代码库块中如果需要操作外部的变量，必须是final类型的，所以定义了final String[] retList
		opt_found.ifPresent(name -> retList[0] = name.split("\\[")[0]);
		return retList[0];
	}


	//未知(0)，DL/T645-1997（1），DL/T645-2007（2），DL/T698.45（3），CJ/T188-2004（4）
	// 采集当前数据[0] NULL，采集上第N次[1] unsigned，按冻结时标采集[2] NULL，按时标间隔采集[3] TI
	public static String getValByComma(String data,String code){
		final String[] retList = {""};
		// code="1,日" 无需进行数据继续解析
		if (code.equals("") || code.indexOf(",")>=0){
			code = code +"";
			return retList[0];
		}
		if (code.equals("FF") )
			code = code+ "";

//		code = DataConvert.int2String(DataConvert.hexString2Int(code));
		final String endCode = DataConvert.int2String(DataConvert.hexString2Int(code));
		data = data.replaceAll("，", ",");
		data = data.replaceAll("（", "(");
		data = data.replaceAll("）", ")");
		data = data.replaceAll(" ", "");
		data = data.replaceAll("\\n", "");
		String[] array = data.split(",");
		List<String> list = Arrays.asList(array);
		final Optional<String> opt_found =
				list.stream()
				.filter(str -> str.indexOf("(")>0)
				.filter(str -> str.indexOf(")")>0)
				.filter(str -> str.split("\\(")[1].split("\\)")[0].equals(endCode))
				.findFirst();
		opt_found.ifPresent(name -> retList[0] = name.split("\\(")[0]);
		return retList[0];
	}



}
