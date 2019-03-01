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
		// ȥ��ret���ҵ������ַ� �س������С��Ʊ����
		String regex = "[^a-zA-Z_\u4e00-\u9fa5]";
		ret = ret.replaceAll(regex, "");
		return ret;
	}

	// 0��"��"״̬��1��"��"״̬
	public static String getValBySemicolon(String data,String code){
		final String[] retList = {""};
		if (code.indexOf(",") >=0 ) return retList[0];
		if (code.equals("") ) return retList[0];
		final String endCode = DataConvert.int2String(DataConvert.hexString2Int(code));
		data = data.replaceAll("��", ";");
		data = data.replaceAll("��", ":");
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

	//�޵��ܱ�[0]NULL��ȫ���û���ַ[1]NULL��һ���û�����[2]SEQUENCEOFunsigned��һ���û���ַ[3]SEQUENCEOFTSA��һ���������[4]SEQUENCEOFlong-unsigned��һ���û���������[5]SEQUENCEOFRegion��һ���û���ַ����[6]SEQUENCEOFRegion��һ�������������[7]SEQUENCEOFRegion
	public static String getValByComma1(String data,String code){
		final String[] retList = {""};
		if ((code.indexOf(",")) >= 0) return retList[0];
		if (code.equals("") ) return retList[0];

		data = data.replaceAll("��", ",");
		data = data.replaceAll("��", "(");
		data = data.replaceAll("��", ")");
		data = data.replaceAll(" ", "");

		final String endCode = DataConvert.int2String(DataConvert.hexString2Int(code));
		data = data.replaceAll("��", ",");
		data = data.replaceAll(" ", "");
		String[] array = data.split(",");
		List<String> list = Arrays.asList(array);
		// ����index�жϵ�ʱ�򣬲������"\\ת���" ������split������ʱ����Ҫ���"\\ת���"
		// ���Ƚ����ж��Ƿ����[��]��Ȼ���ٽ���split����
		final Optional<String> opt_found =
				list.stream()
				.filter(str -> str.indexOf("[")>0)
				.filter(str -> str.indexOf("]")>0)
				.filter(str -> str.split("\\[")[1].split("\\]")[0].equals(endCode))
				.findFirst();
		// lambda�Ĵ������������Ҫ�����ⲿ�ı�����������final���͵ģ����Զ�����final String[] retList
		opt_found.ifPresent(name -> retList[0] = name.split("\\[")[0]);
		return retList[0];
	}


	//δ֪(0)��DL/T645-1997��1����DL/T645-2007��2����DL/T698.45��3����CJ/T188-2004��4��
	// �ɼ���ǰ����[0] NULL���ɼ��ϵ�N��[1] unsigned��������ʱ��ɼ�[2] NULL����ʱ�����ɼ�[3] TI
	public static String getValByComma(String data,String code){
		final String[] retList = {""};
		// code="1,��" ����������ݼ�������
		if (code.equals("") || code.indexOf(",")>=0){
			code = code +"";
			return retList[0];
		}
		if (code.equals("FF") )
			code = code+ "";

//		code = DataConvert.int2String(DataConvert.hexString2Int(code));
		final String endCode = DataConvert.int2String(DataConvert.hexString2Int(code));
		data = data.replaceAll("��", ",");
		data = data.replaceAll("��", "(");
		data = data.replaceAll("��", ")");
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
