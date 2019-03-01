package frame;

import java.lang.reflect.Method;

import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DebugSwing;

import entity.Constant;
import util.DB;
import util.TreeNode;
import util.Util698;


public class AnalyData {

	private static Boolean SHOW_MSG = true;

	TreeNode node = null;
	String desc = "";
	Object[][] analyMsgList = null;
	int currentMsgLine = -1;

	public AnalyData( Object[][] analyMsgList,int currentMsgLine  ){
		this.analyMsgList = analyMsgList;
		this.currentMsgLine = currentMsgLine;
	}

	public AnalyData( ){
	}
	public String[] dealDATA(String data, TreeNode n_node ) {
		return dealDATA(data, n_node, true);
	}

	// ��������DATA
	public String[] dealDATA(String data, TreeNode n_node, Boolean withMsg) {
		String ret[] = { "", "", "" };

		if (data.equals("")) return ret;
		// �ӱ����еõ��������ͣ��ӻ������ͱ����ҵ��������͵Ķ�����Ϣ
		int type = DataConvert.hexString2Int(data.substring(0, 2));
		String tmp = DataConvert.BinStr2String(DataConvert.IntToBinString(type),2);
		// �õ����͵�������Ϣ
		String typeName = DB.getInstance().getTypeInfo(tmp)[0];

		// ����
		String key = typeName+"("+type+"-0x"+data.substring(0, 2)+")";

		// xuky 2017.03.18 ����������������н���
		// 68 8D 01 C3 05 29 00 03 00 00 00 00 22 5A 85 01 00 60 14 02 00 01 01 06 02 06 11 01 12 00 3E 02 02 11 02 00 01 01 5B 01 50 04 02 00 02 00 20 02 00 00 10 02 00 5C 01 16 04 02 06 11 02 12 03 C0 02 02 11 03 54 01 00 0F 01 01 5B 01 50 02 02 00 0A 00 10 02 01 00 20 02 01 00 50 02 01 00 60 02 01 00 70 02 01 00 80 02 01 20 00 02 00 20 01 02 00 20 04 02 00 20 0A 02 00 5C 05 01 02 11 60 11 6F 16 06 02 06 11 03 12 03 C0 02 02 11 03 54 01 00 3C 01 01 5B 01 50 02 02 00 04 00 10 02 01 00 20 02 01 20 00 02 00 20 01 02 00 5C 05 01 02 11 10 11 5F 16 06 02 06 11 04 12 00 01 02 02 11 00 00 01 10 5B 00 00 10 02 00 5B 00 00 20 02 00 5B 00 00 30 02 00 5B 00 00 40 02 00 5B 00 20 00 02 00 5B 00 20 01 02 00 5B 00 20 04 02 00 5B 00 20 05 02 00 5B 00 00 50 02 00 5B 00 00 60 02 00 5B 00 00 70 02 00 5B 00 00 80 02 00 5B 00 10 10 02 00 5B 00 10 20 02 00 5B 00 20 0A 02 00 5B 00 20 06 02 00 5C 05 01 02 11 60 11 6F 16 01 02 06 11 05 12 00 3E 02 02 11 02 00 01 01 5B 01 50 04 02 00 08 00 20 02 00 00 50 02 00 00 60 02 00 00 70 02 00 00 80 02 00 10 10 02 00 10 20 02 00 00 10 02 00 5C 05 01 02 11 60 11 6F 16 04 02 06 11 06 12 00 1F 02 02 11 00 00 01 02 5B 00 20 14 02 00 5B 00 40 00 02 00 5C 01 16 02 00 00 53 6F 16
		// unsigned(17-0x11)���洢ʱ��ѡ��=>96-0x60 -
		String codeVal = "";
		if (withMsg && SHOW_MSG ){
			String msg[] = dealMsg();
			key = key + msg[0];
			codeVal = msg[1];
		}

		TreeNode treenode = new TreeNode(key, "");
		n_node.addChild(treenode);

		// �����������͵�����غ������д���
		String[] s = dealDataWithType(type, data.substring(2), treenode);
//		if (s[2].equals("1")){
//			if (!codeVal.equals("")){
//				// ��Ҫ���м�ֵ�Զ���ת��
//				String val = getVal( codeVal,s[0] );
//				treenode.setValue(s[0]+"-"+val);
//			}
//			else
//				treenode.setValue(s[0]);
//		}

		// xuky 2016.12.05 ������С�if (s[2].equals("1"))�����ж�
		if (!codeVal.equals("")){
			// ��Ҫ���м�ֵ�Զ���ת��
			String val = DealTypeCode.getVal( codeVal,s[0] );
			treenode.setValue(s[0]+"-"+val);
		}
		else
			treenode.setValue(s[0]);

		ret[1] = s[1];

		// xuky 2016.12.16 ���ؽ�����Ϣ
		ret[2] = DataConvert.int2String(currentMsgLine);
		return ret;
	}

	private String[] dealMsg() {
		String ret[] = {"",""};
		// xuky 2016.12.13 Ŀǰ��������������⣬�����ڲ�����������ͣ
//		if (true)
//			return ret;
		if (analyMsgList == null || analyMsgList.length == 0 || currentMsgLine >= analyMsgList.length )
			return ret;

		String msg = "";
		if (currentMsgLine >=0)
			msg = (String) analyMsgList[currentMsgLine][0];
		if (msg.equals("��¼��ѡ��"))
			msg = msg+"";
//		System.out.println("analyMsgList=> dealMsg(b4) " +currentMsgLine + " " + msg );

		if (currentMsgLine ==-1)
			return ret;
		if (analyMsgList == null)
			return ret;
		try{
			String type = (String) analyMsgList[currentMsgLine][1];
//			System.out.println("dealMsg=> "+analyMsgList[currentMsgLine][0]);
//			System.out.println("dealMsg=> "+analyMsgList[currentMsgLine][3]);

			String codeVal = "";
			if (analyMsgList[currentMsgLine][3] != null){
				try{
					codeVal = (String)analyMsgList[currentMsgLine][3];
				}
				catch(Exception e){
//					e.printStackTrace();
					int n =  (int)analyMsgList[currentMsgLine][3];
					codeVal = DataConvert.int2String(n);
				}
			}

			if (type.equals("enum"))
				ret[1] = codeVal;

			// xuky 2016.12.01 ���ַ�enum���͵����ݣ�Ҳ���ڼ�ֵ�Ե������������������Ԫ ״̬ST����0�����֡�״̬��1�����ϡ�״̬��
			if (codeVal.indexOf(";")>=0 || codeVal.indexOf("��")>=0)
				ret[1] = codeVal;

			// xuky 2016.12.02 ���ַ�enum���͵����ݣ�Ҳ���ڼ�ֵ�Ե�����������޵��ܱ�[0]NULL��ȫ���û���ַ[1]NULL��һ���û�����[2]SEQUENCEOFunsigned��һ���û���ַ[3]SEQUENCEOFTSA��һ���������[4]SEQUENCEOFlong-unsigned��һ���û���������[5]SEQUENCEOFRegion��һ���û���ַ����[6]SEQUENCEOFRegion��һ�������������[7]SEQUENCEOFRegion��
			if (codeVal.indexOf(",")>=0 || codeVal.indexOf("��")>=0)
				ret[1] = codeVal;

			ret[0] = "��"+(String) analyMsgList[currentMsgLine][0]+"��";

			if (ret[0].equals("����¼ѡ��������RSD��")){
				int a = 0;
				a = a+1;
			}

			if  (((String) analyMsgList[currentMsgLine][0]).equals("����")){
				ret = ret;
			}
			if (analyMsgList[currentMsgLine][1].equals("array")){
				// ��Ҫ�����еĵ�ǰ�������г�ʼ����ֵ��
				analyMsgList[currentMsgLine][3] = 0;
			}

//			System.out.println("dealMsg=>"+ret);
			currentMsgLine++;

//			// ���ݵ���ĩβ�ˣ���Ҫ���е���
//			if (currentMsgLine == analyMsgList.length){
//				String array_idx = (String) analyMsgList[currentMsgLine-1][5];
//				String nextStr = array_idx.substring(0, 7);
//				int nextLine = getNextNode(nextStr);
//				if (nextLine != -1)
//					currentMsgLine = nextLine;
//				else{
////					DebugSwing.showMsg("���ݷ���ָ���쳣����Ҫ������Ա���е���");
//				}
//			}

		}
		catch(Exception e){
			e.printStackTrace();
			return ret;
		}

		if (currentMsgLine >=0)
			if (currentMsgLine < analyMsgList.length)
				if (analyMsgList[currentMsgLine][0] != null)
					msg = (String) analyMsgList[currentMsgLine][0];
//		System.out.println("analyMsgList=> dealMsg(after) " +currentMsgLine + " " + msg);

		return ret ;
	}

	// ���÷��似�������ö�̬����
	public String[] dealDataWithType(int type, String data, TreeNode n) {
		String ret[] = { "", "","" };

		String tmp = DataConvert.BinStr2String(DataConvert.IntToBinString(type),2);
		String typeNum = DB.getInstance().getTypeInfo(tmp)[1];

		// xuky 2016.11.23 ʹ���Զ���ĳ������type28�������ݵĽ���
		if (type == 28)
			typeNum = "0";

		if (typeNum == null || typeNum.equals("0")){
			// ��������ݿ���δ���ã���Ҫ���б�д����������д���
			try {
				// xuky 2016.12.16 ���������ݼ��������ȷ������µ�AnalyData��
				AnalyData analyData_instance = new AnalyData( analyMsgList,currentMsgLine );

				String fun = "dealType"	+ DataConvert
						.BinStr2String(DataConvert.IntToBinString(type), 2);
//				System.out.println("!! run =>" + fun);
				Method m = (Method) analyData_instance.getClass().getMethod(fun,
						new Class[] { String.class, TreeNode.class });
				String[] result = (String[]) m.invoke(analyData_instance, new Object[] { data, n });
				ret[0] = result[0];
				ret[1] = result[1];
				// xuky 2016.12.16
				if (result.length == 3)
					if (!result[2].equals(""))
						currentMsgLine = DataConvert.String2Int(result[2]);
			} catch (Exception e) {
				System.out.println("dealDataWithType_new=>"+e.getMessage());
				String str = DataConvert.int2HexString(type, 2) + data;
				str = Util698.seprateString(str , " ");
				String msg = "��������=>" + type + "���޽������룬��ά�����������ִ��";
				System.out.println("���ڴ�������=>" + str);
				System.out.println(msg);
				DebugSwing.showMsg(msg);
			}
		}
		else{
			// ��������ݿ��п����ҵ�����ָ�������ݳ��ȣ���ֱ��ȡ������
			int len = DataConvert.String2Int(typeNum)*2;
			//node = new TreeNode("��DB��"+t[0], data.substring(0, len));
			//n.addChield(node);
			ret[0] = data.substring(0,len);
			ret[1] = data.substring(len);
			ret[2] = "1";
			// (2-0x02)
			if (type == 5 || type == 6 || (type >= 15 && type <= 18) || type == 20 || type == 21 )
				ret[0] = DataConvert.int2String(DataConvert.hexString2Int(ret[0]))+"-0x"+Util698.seprateString(ret[0]," ");
		}
		return ret;
	}

	public String[] dealWithNum(String data, TreeNode n, String fun) {
		return dealWithNum(data, n, fun, true);
	}

	private void setMsgNum(int num){
//		[0]name
//		[1]type
//		[2]������
//		[3]��ǰ����
//		[4]id
//		[5]array_index
		if (currentMsgLine ==-1)
			return ;
		if (analyMsgList == null)
			return ;
		try{
			// ǰһ��Ԫ��  ���������͵����ݽ���ѭ��������ֵ
			analyMsgList[currentMsgLine-1][2] = num;
		}
		catch(Exception e){
			return;
		}

	}

	// ���ո�������ѭ���ķ������
	public String[] dealWithNum(String data, TreeNode n, String fun, Boolean isArrary) {
		String ret[] = { "", "", "" };
		String[] s = { "", "" };

		// �õ���Ҫѭ��ִ�еĴ�����Ϣ
		int num = DataConvert.hexString2Int(data.substring(0, 2));

		setMsgNum(num);

		// ��tree����ʾѭ��������Ϣ
		n.setValue("��"+num+"��");

		String restData = data.substring(2);
		Method method = null;
		try {
			// �������ָ������
			AnalyData analyData_instance = new AnalyData( analyMsgList,currentMsgLine );
			method = (Method) analyData_instance.getClass().getMethod(fun,
					new Class[] { String.class, TreeNode.class });
			for (int i = 0; i < num; i++) {

				TreeNode tn = new TreeNode("���", i+1);
				n.addChild(tn);

//				System.out.println("dealWithNum=> "+fun + " "+(i+1));

				s = (String[]) method.invoke(analyData_instance, new Object[] {
						restData, tn });
				restData = s[1];

				// xuky 2016.12.16 ��������Ϣ�ڴ˽���ʾ����ͬ��
				if (s.length == 3){
					ret[2] = s[2];
					if (!s[2].equals(""))
						currentMsgLine = DataConvert.String2Int(s[2]);
				}


				// �ڴ���Ҫ����msgָ��λ�ã������ҵ������һ��arrary��seq ��������
				// �������ǣ��޸���������Ϣ��������������

				// �������������͵����ݽ��д��� ����ָ��λ�ã���Ϊ��Ҫ��ν���ѭ��
				if (isArrary){
					dealLineOfArrary();
					// xuky 2016.12.19 ���н�����Ϣ����
					analyData_instance.currentMsgLine = currentMsgLine;
				}

			}
			if (num == 0 && isArrary){
				dealLineOfArrary();

				// xuky 2016.12.16 ��������Ϣ�ڴ˽���ʾ����ͬ��
				ret[2] = DataConvert.int2String(currentMsgLine);
			}

			// xuky 2017.03.13
			// 68 99 00 C3 05 11 11 11 11 11 11 00 FA 39 85 01 00 60 12 02 00 01 01 02 02 0C 11 01 54 03 00 01 16 01 11 01 1C 07 E0 09 0C 00 02 00 1C 08 33 09 09 09 09 09 54 00 00 00 16 02 16 01 12 00 00 12 00 00 02 02 16 00 01 02 02 04 11 00 11 00 11 0C 11 3B 02 04 11 0D 11 00 11 17 11 3B 02 0C 11 02 54 03 00 01 16 01 11 02 1C 07 E0 09 0C 00 02 00 1C 08 33 09 09 09 09 09 54 00 00 00 16 02 16 01 12 00 00 12 00 00 02 02 16 00 01 01 02 04 11 00 11 00 11 17 11 3B 00 00 08 89 16
			if (isArrary && currentMsgLine == 1 && ((String)analyMsgList[0][0]).indexOf("����")>=0 ){
				// xuky 2016.12.16 ��������Ϣ�ڴ˽���ʾ����ͬ��
				ret[2] = DataConvert.int2String(currentMsgLine);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		ret[0] = DataConvert.int2String(num);
		ret[1] = restData;
		return ret;
	}

	private void dealLineOfArrary(){

//		System.out.println("analyMsgList=> dealLineOfArrary(b4) " +currentMsgLine );

		for(int i= currentMsgLine-1;i>=0;i--){
			if (((String)analyMsgList[i][1]).equals("array")){

				// ��Ҫ�����жϣ������ǰarrary�������ﵽ�����ޣ�����Ҫ��������  ͬ������һ������������ˣ������жϴ���
				// ������Ҫ��¼�ܵ�ѭ����������ǰ��ִ�д���
				if (analyMsgList[i][3] == null || analyMsgList[i][3].equals("") )
					analyMsgList[i][3] = 1;
				else
					analyMsgList[i][3] = (int)analyMsgList[i][3] +1;

				int a = 0,b = 0;

				try{
					a = (int)analyMsgList[i][3];
					b = (int)analyMsgList[i][2];
				}
				catch(Exception e){
					try{
						b = DataConvert.String2Int((String)analyMsgList[i][2]);
					}
					catch(Exception e1){
						e1.printStackTrace();
					}
				}

				if ( a >= b ){
					String array_idx = (String)analyMsgList[i][5];

					// xuky 2017.03.03 ԭ�ȵĴ������������⣬����ֱ����������������˳�
					// 68 64 00 43 05 11 11 11 11 11 11 00 92 86 07 01 00 60 14 7F 00 01 02 02 06 11 02 12 00 FA 02 02 11 01 11 01 01 01 5B 01 50 04 02 00 03 20 21 02 00 00 10 02 00 00 20 02 00 5C 01 16 04 02 06 11 01 12 01 00 02 02 11 00 11 00 01 03 5B 00 00 10 02 00 5B 00 20 00 02 00 5B 00 20 01 02 00 5C 01 16 02 00 CB AF 16
					if (a > b && array_idx.equals("null_000_001_004"))
					{
						if (b==0)
						{
							// xuky 2017.04.07  �������� ����
							// 68 AD 01 C3 05 29 00 03 00 00 00 00 A8 B8 85 01 00 60 14 02 00 01 01 07 02 06 11 01 12 00 3E 02 02 11 02 00 01 01 5B 01 50 04 02 00 02 00 20 02 00 00 10 02 00 5C 01 16 04 02 06 11 02 12 03 C0 02 02 11 03 54 01 00 0F 01 01 5B 01 50 02 02 00 0A 00 10 02 01 00 20 02 01 00 50 02 01 00 60 02 01 00 70 02 01 00 80 02 01 20 00 02 00 20 01 02 00 20 04 02 00 20 0A 02 00 5C 05 01 02 11 60 11 6F 16 06 02 06 11 03 12 03 C0 02 02 11 03 54 01 00 3C 01 01 5B 01 50 02 02 00 04 00 10 02 01 00 20 02 01 20 00 02 00 20 01 02 00 5C 05 01 02 11 10 11 5F 16 06 02 06 11 04 12 00 01 02 02 11 00 00 01 10 5B 00 00 10 02 00 5B 00 00 20 02 00 5B 00 00 30 02 00 5B 00 00 40 02 00 5B 00 20 00 02 00 5B 00 20 01 02 00 5B 00 20 04 02 00 5B 00 20 05 02 00 5B 00 00 50 02 00 5B 00 00 60 02 00 5B 00 00 70 02 00 5B 00 00 80 02 00 5B 00 10 10 02 00 5B 00 10 20 02 00 5B 00 20 0A 02 00 5B 00 20 06 02 00 5C 05 01 02 11 60 11 6F 16 01 02 06 11 05 12 00 3E 02 02 11 02 00 01 01 5B 01 50 04 02 00 08 00 20 02 00 00 50 02 00 00 60 02 00 00 70 02 00 00 80 02 00 10 10 02 00 10 20 02 00 00 10 02 00 5C 05 01 02 11 60 11 6F 16 04 02 06 11 06 12 00 1F 02 02 11 00 00 01 02 5B 00 20 14 02 00 5B 00 40 00 02 00 5C 01 16 02 02 06 11 07 12 00 7B 02 02 11 03 54 01 00 0F 01 00 5C 05 02 00 11 00 11 03 02 11 05 11 08 16 03 00 00 8E BE 16
							// ����������Ԫ�ص����
							currentMsgLine = currentMsgLine;
						}
						else{
							currentMsgLine = 1;
							return;
						}
					}
					// xuky 2017.03.03 ԭ�ȵĴ������������⣬����ֱ����������������˳�
					//68 99 00 C3 05 11 11 11 11 11 11 00 FA 39 85 01 00 60 12 02 00 01 01 02 02 0C 11 01 54 03 00 01 16 01 11 01 1C 07 E0 09 0C 00 02 00 1C 08 33 09 09 09 09 09 54 00 00 00 16 02 16 01 12 00 00 12 00 00 02 02 16 00 01 02 02 04 11 00 11 00 11 0C 11 3B 02 04 11 0D 11 00 11 17 11 3B 02 0C 11 02 54 03 00 01 16 01 11 02 1C 07 E0 09 0C 00 02 00 1C 08 33 09 09 09 09 09 54 00 00 00 16 02 16 01 12 00 00 12 00 00 02 02 16 00 01 01 02 04 11 00 11 00 11 17 11 3B 00 00 08 89 16
					if (a == b && array_idx.equals("null_000_001_012_002"))
					{
						currentMsgLine = 1;
						return;
					}
					// ������һ���ڵ� null_000_001_002
					int next = DataConvert.String2Int(array_idx.substring(array_idx.length()-3))+1;
					String nextStr = array_idx.substring(0, array_idx.length()-3)+DataConvert.hexString2String(DataConvert.int2HexString(next, 4), 3);
					int nextLine = getNodeByArrayidx(nextStr);
					if (nextLine == -1){
						nextStr = array_idx.substring(0, array_idx.length()-4);
						nextLine = getNodeByArrayidx(nextStr);
						currentMsgLine = nextLine;
					}
					else
						currentMsgLine = nextLine;
				}
//				else if (a > b){
//					// ������  ��OMD(�ϱ�������)=>60 1C 7F 00�������ݽ�������
//					// ��Ҫ����һ���ڵ� ��һ��������struture ����Ҫ��������
//					// ��һ�����������飬��Ҫ�ж������Ƿ������ﵽҪ���ǣ�����Ҫ��������
//					String array_idx = (String)analyMsgList[i][5];
//					String nextStr = array_idx.substring(0, array_idx.length()-4);
//					int nextLine = getNodeByArrayidx(nextStr);
//					if (analyMsgList[nextLine][1].equals("structure")){
//						nextStr = nextStr.substring(0, nextStr.length()-4);
//						nextLine = getNodeByArrayidx(nextStr);
//						if (nextLine != -1)
//							currentMsgLine = nextLine+1;
//					}
//				}
				else{
					// �ص��������� ����������ڵ�ĺ�����һ���ڵ�
					currentMsgLine = i+1;
				}
				break;
			}
		}
//		System.out.println("analyMsgList=> dealLineOfArrary(after) " +currentMsgLine );

		if (currentMsgLine == 1){
			currentMsgLine = 1;
		}

	}

	public int getNodeByArrayidx(String array_idx){
		int i=0;
		for (Object[] o:analyMsgList){
			if (((String)o[5]).equals(array_idx))
				return i;
			i++;
		}
		return -1;
	}

	// �������   ��ν������ݴ���  01�����һ����һ���ṹ�� ����ֵݹ���õ����
	public String[] dealType01(String data, TreeNode n) {
		return dealWithNum(data, n, "dealDATA",true);
	}
	// �ṹ�����  �ṹ���һ���Ƕ����������
	public String[] dealType02(String data, TreeNode n) {
		return dealWithNum(data, n, "dealDATA",false);
	}

	public String[] dealSeqOfResultNormal(String data, TreeNode n) {
		return dealWithNum(data, n, "dealResultNormal");
	}


	public String[] dealType96(String data, TreeNode n) {
		return dealWithNum(data, n, "dealCSD");
	}

	// RSD
	public String[] dealType90(String data, TreeNode n) {
		return dealRSD(data, n);
	}


	// bit-string
	public String[] dealType04(String data, TreeNode n) {
		String ret[] = { "", "" };
		// bit-string = 04 08 53  04���� 8bit ,������53
		int bitSize = DataConvert.hexString2Int(data.substring(0, 2));
		int byteSize = bitSize/8;
		String str = data.substring(2,2+byteSize*2);
		str = DataConvert.hexString2BinString(str, bitSize);
		node = new TreeNode("bit-strig(SIZE("+bitSize+"))", str);
		n.addChild(node);
		ret[1] = data.substring(2+byteSize*2);
		return ret;
	}

	public String[] dealType95(String data, TreeNode n) {
		String ret[] = { "", "" };
		// 68 27 00 43 05 08 00 00 00 00 00 10 D0 24 06 01 00 F2 0B 02 00 01 01 02 02 0A 04 31 32 33 34 5F 03 02 08 01 00 00 AC 80 16
		// COMDCB  = 5F X1 X2 X3 X4 X5 , 5F ������
		// 03 02 08 01 00 5�ֽ� ������
		String str = data.substring(0,10);
		node = new TreeNode("COMDCB", str);
		n.addChild(node);

		TreeNode node1 = new TreeNode("������", Constant.getVal(DataConvert.hexString2Int(str.substring(0,2)),"PortRate"));
		node.addChild(node1);

		node1 = new TreeNode("У��λ", Constant.getVal(DataConvert.hexString2Int(str.substring(2,4)),"PARITY"));
		node.addChild(node1);

		node1 = new TreeNode("����λ", DataConvert.hexString2Int(str.substring(4,6)));
		node.addChild(node1);

		node1 = new TreeNode("ֹͣλ", DataConvert.hexString2Int(str.substring(6,8)));
		node.addChild(node1);

		node1 = new TreeNode("����", Constant.getVal(DataConvert.hexString2Int(str.substring(8)),"FlowControl"));
		node.addChild(node1);

		ret[1] = data.substring(10);
		return ret;
	}

	// visible-string
	public String[] dealType10(String data, TreeNode n) {
		String ret[] = { "", "" };
		// visible-string = 0A 05 43 4D 4E 45 54   0A���� 05���� 43 4D 4E 45 54 = CMNET
		int byteSize = DataConvert.hexString2Int(data.substring(0, 2));
		String str = data.substring(2,2+byteSize*2);
		str = DataConvert.asciiHex2String(str);
		node = new TreeNode("visible-string", str);
		n.addChild(node);
		ret[1] = data.substring(2+byteSize*2);
		return ret;
	}

	public String[] dealType85(String data, TreeNode n) {
		return dealType09(data, n);
	}

	public String[] dealType91(String data, TreeNode n) {
		return dealCSD(data, n);
	}

	public String[] dealType92(String data, TreeNode n) {
		return dealMS(data, n);
	}

	public String[] dealType28(String data, TreeNode n) {
		String ret[] = { "", "" };
		// DataTimeBCD[28] = [7Byte]  07 E0 09 10 17 3B 3B
		String tmp = data.substring(0, 14);
		String t = DataConvert.hexString2String(tmp.substring(0, 4), 4);
		t += "-" + DataConvert.hexString2String(tmp.substring(4, 6), 2);
		t += "-" + DataConvert.hexString2String(tmp.substring(6, 8), 2);
		t += " " + DataConvert.hexString2String(tmp.substring(8, 10), 2);
		t += ":" + DataConvert.hexString2String(tmp.substring(10, 12), 2);
		t += ":" + DataConvert.hexString2String(tmp.substring(12), 2);
		node = new TreeNode("DataTimeBCD",t+"-0x"+Util698.seprateString(tmp, " "));
		n.addChild(node);
		ret[1] = data.substring(14);
		return ret;
	}

	// xuky 2017.06.07 �󲿷ֵĴ�����������1C���ݱ�ʶ��
	public String[] dealType28(String data, TreeNode n, String type) {
		return dealType28(data, n, type, false);
	}

	public String[] dealType28(String data, TreeNode n, String type,Boolean withHead) {
		String ret[] = { "", "" };
		if (withHead)
			data = data.substring(2,data.length());
		// DataTimeBCD[28] = [7Byte]  07 E0 09 10 17 3B 3B
		String tmp = data.substring(0, 14);
		String t = DataConvert.hexString2String(tmp.substring(0, 4), 4);
		t += "-" + DataConvert.hexString2String(tmp.substring(4, 6), 2);
		t += "-" + DataConvert.hexString2String(tmp.substring(6, 8), 2);
		t += " " + DataConvert.hexString2String(tmp.substring(8, 10), 2);
		t += ":" + DataConvert.hexString2String(tmp.substring(10, 12), 2);
		t += ":" + DataConvert.hexString2String(tmp.substring(12), 2);
		node = new TreeNode(type+"DataTimeBCD",t+"-0x"+Util698.seprateString(tmp, " "));
		n.addChild(node);
		ret[1] = data.substring(14);
		return ret;
	}

	public String dealTreeNode(TreeNode n) {
		String ret = n.getChildren().get(0).getValue();
		return ret;
	}

	public String[] dealType17(String data, TreeNode n) {
		String ret[] = { "", "" };
		// unsigned[17] = [1Byte]

		TreeNode node = new TreeNode("unsigned", data.substring(0, 2));
		n.addChild(node);

		ret[1] = data.substring(2);
		return ret;
	}

	public String[] dealType22(String data, TreeNode n) {
		String ret[] = { "", "" };
		// enum[16] = [1Byte]

		TreeNode node = new TreeNode("enum", data.substring(0, 2));
		n.addChild(node);

		ret[1] = data.substring(2);
		return ret;
	}

	public String[] dealType18(String data, TreeNode n) {
		String ret[] = { "", "" };
		// long-unsigned[18] = [2Byte]

		TreeNode node = new TreeNode("", data.substring(0, 4));
		n.addChild(node);

		ret[1] = data.substring(4);
		return ret;
	}



	/*
	public String[] dealType15(String data, TreeNode n) {
		String ret[] = { "", "" };
		// integer[15] = [1Byte]

		TreeNode node = new TreeNode("integer", data.substring(0, 2));
		n.addChield(node);

		ret[1] = data.substring(2);
		return ret;
	}





	public String[] dealType05(String data, TreeNode n) {
		String ret[] = { "", "" };
		// double-long[18] = [4Byte]
		int byteLen = 4;
		TreeNode node = new TreeNode("long-unsigned", data.substring(0,
				byteLen * 2));
		n.addChield(node);

		ret[1] = data.substring(byteLen * 2);
		return ret;
	}

	public String[] dealType06(String data, TreeNode n) {
		String ret[] = { "", "" };
		// double-long[18] = [4Byte]
		int byteLen = 4;
		TreeNode node = new TreeNode("double-long-unsigned", data.substring(0,
				byteLen * 2));
		n.addChield(node);

		ret[1] = data.substring(byteLen * 2);
		return ret;
	}
	*/

	public String[] dealType84(String data, TreeNode n) {
		String ret[] = { "", "" };

		// [84] = [3Byte ]
		// 54 03 00 01
		String unit = "";
		unit = data.substring(0, 2);
		if (unit.equals("00"))
			unit = "��";
		if (unit.equals("01"))
			unit = "��";
		if (unit.equals("02"))
			unit = "ʱ";
		if (unit.equals("03"))
			unit = "��";
		if (unit.equals("04"))
			unit = "��";
		if (unit.equals("05"))
			unit = "��";
		int len = 3;
		int val = DataConvert.hexString2Int(data.substring(2, len * 2));

//		TreeNode node = new TreeNode("����"+len+"Byte", data.substring(0, len * 2));
		ret[0] = val+","+unit+"-"+DataConvert.hexString2Int(data.substring(0, 2))
				+","+val+"-0x"+data.substring(0, len * 2);
//		TreeNode node = new TreeNode(data.substring(0, len * 2),DataConvert.hexString2Int(data.substring(0, 2))
//				+","+val+"->"+val+","+unit);
//		TreeNode node = new TreeNode(data.substring(0, len * 2),DataConvert.hexString2Int(data.substring(0, 2))
//		+","+val+"->"+val+","+unit);
//		n.addChild(node);

//		��λ  ENUMERATED
//		   {		       ��      ��0����		       ��      ��1����		       ʱ      ��2����		       ��      ��3����		       ��      ��4����
//		       ��      ��5��		    }��
//		    ���ֵ  long-unsigned

//		node = new TreeNode("����", );
//		n.addChild(node);

		ret[1] = data.substring(len * 2);
		return ret;
	}


	public String[] dealType09(String data, TreeNode n) {
		String ret[] = { "", "" };

		// [09] = [1Byte datalen=n][ nByte data ]

		int len = DataConvert.hexString2Int(data.substring(0, 2));
		String tmp = data.substring(2, 2 + len * 2);
		if ( n.getKey().indexOf("IP��ַ") >= 0)
		{
			String str = "";
			str += DataConvert.hexString2String(tmp.substring(0,2),3);
			str += "."+DataConvert.hexString2String(tmp.substring(2,4),3);
			str += "."+DataConvert.hexString2String(tmp.substring(4,6),3);
			str += "."+DataConvert.hexString2String(tmp.substring(6,8),3);
			ret[0] = str;
//			n.setValue(str);
		}
		else{
			TreeNode node = new TreeNode("octet-string ����"+len+"Byte",tmp );
			n.addChild(node);
		}

//		TreeNode node1 = new TreeNode("����", );
//		node.addChild(node1);

		ret[1] = data.substring(2 + len * 2);
		return ret;
	}

	public String[] dealDAR(String data, TreeNode t) {
		String ret[] = { "", "" };
		int tmp = DataConvert.hexString2Int(data.substring(0, 2));
		String type = DataConvert.int2String(tmp);
		node = new TreeNode("DAR("+DB.getInstance().getDARInfo(type)+")",tmp );
		t.addChild(node);
		ret[1] = data.substring(2);
		return ret;
	}

	public String[] dealOAD(String data, TreeNode t) {
		String ret[] = { "", "" };
		ret[0] = data.substring(0, 8);
		ret[1] = data.substring(8);
		String info=DB.getInstance().getOADInfo(data.substring(0, 4));

		String OAD = ret[0];
		String attr_name = DB.getInstance().getOADData(OAD,true);

		// ���ڽ��и�������������
		analyMsgList =  FrameNew.getFuncParasWithRoot(ret[0],null,"Attr");
		currentMsgLine = 0;

//		System.out.println("analyMsgList=> get " + OAD);

		node = new TreeNode("OAD("+info+" "+ attr_name +")", ret[0]);
		t.addChild(node);
		return ret;
	}


	public String[] dealType81(String data, TreeNode t) {
		return dealOAD(data, t);
	}

	// ���������ret[0]�У����µĲ�����ret[1]
	// ��������OMD
	public String[] dealOMD(String data,TreeNode t) {
		String ret[] = { "", "" };
		ret[0] = data.substring(0, 8);
		ret[1] = data.substring(8);

		String info=DB.getInstance().getOADInfo(data.substring(0, 4));

		String OAD = ret[0];

		String method_name = DB.getInstance().getOADData(OAD,false);

		// ���ڽ��и�������������
		analyMsgList =  FrameNew.getFuncParasWithRoot(ret[0],null,"Func");
		currentMsgLine = 0;

//		System.out.println("analyMsgList=> get " + OAD );

		node = new TreeNode("OMD("+info+" "+ method_name +")", ret[0]);

		t.addChild(node);

		return ret;
	}

	public String[] dealSeqOfOAD(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOAD");
	}

	public String[] dealRecord(String data, TreeNode t) {

		String ret[] = { "", "" };

		String[] s = dealOAD(data, t);
		s = dealRSD(s[1], t);

		node = new TreeNode("SEQUENCE OF CSD(RCSD)","");
		t.addChild(node);
		s = dealSeqOfCSD(s[1], node);

		ret[1] = s[1];
		return ret;
	}

	public String[] dealRSD(String data, TreeNode t) {
		String ret[] = { "", "" };
		String s[] = { "", "" };

		int type = DataConvert.hexString2Int(data.substring(0, 2));
		TreeNode rsd_node = new TreeNode("RSD����", type);
		t.addChild(rsd_node);
		String restData = data.substring(2);
		switch (type) {
		case 0:
			s[1] = restData;
			break;
		case 1:
			s = dealOADWithData(restData, rsd_node);
			break;
		case 2:
			// Selector2
			s = dealRSDSelector2(restData, rsd_node);
			break;
		case 3:
			// SEQUENCE OF Selector2
			s = dealSeqOfRSDSelector2(restData, rsd_node);
			break;
		case 4:
			// �ɼ��洢ʱ��
			s = dealType28(restData, rsd_node, "�ɼ�����ʱ��", true);
			// ���ܱ����� MS
			s = dealMS(s[1], rsd_node);
			break;
		case 5:
			// �ɼ��洢ʱ��
			s = dealType28(restData, rsd_node, "�ɼ��洢ʱ��", true);
			// ���ܱ����� MS
			s = dealMS(s[1], rsd_node);
			break;
		case 6:
			// �ɼ��洢ʱ��
			s = dealType28(restData, rsd_node, "�ɼ�����ʱ����ʼֵ", true);
			s = dealType28(s[1], rsd_node, "�ɼ�����ʱ�����ֵ", true);
			//  ʱ����            TI
			s = dealType84(s[1], rsd_node);
			// ���ܱ����� MS
			s = dealMS(s[1], rsd_node);
			break;
		case 7:
			s = dealType28(restData, rsd_node, "�ɼ��洢ʱ����ʼֵ", true);
			s = dealType28(s[1], rsd_node, "�ɼ��洢ʱ�����ֵ", true);
			//  ʱ����            TI
			s = dealType84(s[1], rsd_node);
			// ���ܱ����� MS
			s = dealMS(s[1], rsd_node);
			break;
		case 8:
			s = dealType28(restData, rsd_node, "�ɼ��ɹ�ʱ����ʼֵ", true);
			s = dealType28(s[1], rsd_node, "�ɼ��ɹ�ʱ�����ֵ", true);
			//  ʱ����            TI
			s = dealType84(s[1], rsd_node);
			// ���ܱ����� MS
			s = dealMS(s[1], rsd_node);
			break;
		case 9:
			s = dealType17(restData, rsd_node);
			break;
		case 10:
			s = dealType17(restData, rsd_node);
			// ���ܱ����� MS
			s = dealMS(s[1], rsd_node);
			break;
		default:
			break;
		}

		ret[1] = s[1];
		return ret;
	}

	// ���� RSD�������� ��Selector2
	public String[] dealRSDSelector2(String data, TreeNode n) {
		String ret[] = { "", "" };
		String[] s = dealOADWithData(data, n);
		s = dealDATA(s[1], n);
		s = dealDATA(s[1], n);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfRSDSelector2(String data, TreeNode n) {
		return dealWithNum(data, n, "dealRSDSelector2");
	}



	public String[] dealMS(String data, TreeNode t) {
		String ret[] = { "", "" };
		String s[] = { "", "" };

		ret[0] = data.substring(0, 2);
		int type = DataConvert.hexString2Int(data.substring(0, 2));
		String msg = "";
		if (type == 0)
			msg = type + "-�޵��ܱ�";
		if (type == 1)
			msg = type + "-ȫ���û���ַ";
		if (type == 2)
			msg = type + "-һ���û�����";
		if (type == 3)
			msg = type + "-һ���û���ַ";
		if (type == 4)
			msg = type + "-һ���������";
		if (type == 5)
			msg = type + "-һ���û��������� ";
		if (type == 6)
			msg = type + "-һ���û���ַ����";
		if (type == 7)
			msg = type + "-һ�������������";

		TreeNode n = new TreeNode("MS����", msg);
		t.addChild(n);
		TreeNode treenode = null;
		switch (type) {
		case 0:
			// null xuky 2016.08.15 ���ݱ��ľ�������Ϊnull�����ݣ�ռ��1byte��00 null
			s[1] = data.substring(2);

			// 2Byte�Ժ�  nullΪ������
			//s[1] = data.substring(4);
			break;
		case 1:
			//s[1] = data.substring(4);
			s[1] = data.substring(2);
			break;
		case 2:
			// SEQUENCE OF unsigned
			treenode = new TreeNode("SEQUENCE OF unsigned","");
			n.addChild(treenode);
			s = dealSeqOfUnsigned(data.substring(2), treenode);
			break;
		case 3:
			// SEQUENCE OF TSA
			// TSA��=octet-string(SIZE(2��17)) ��5.1.4.2.1����
			treenode = new TreeNode("SEQUENCE OF TSA","");
			n.addChild(treenode);
			s = dealSeqOfTSA(data.substring(2), treenode);
			break;
		case 4:
			// SEQUENCE OF unsigned
			treenode = new TreeNode("SEQUENCE OF long-unsigned","");
			n.addChild(treenode);
			s = dealSeqOfLongunsigned(data.substring(2), treenode);
			break;
		case 5:
			// SEQUENCE OF TSA
			// TSA��=octet-string(SIZE(2��17)) ��5.1.4.2.1����
			treenode = new TreeNode("SEQUENCE OF Region","");
			n.addChild(treenode);
			s = dealSeqOfRegion(data.substring(2), treenode);
			break;
		default:
			ret = ret ;
			break;
		}

		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfUnsigned(String data, TreeNode n) {
		return dealWithNum(data, n, "dealUnsigned");
	}
	public String[] dealSeqOfLongunsigned(String data, TreeNode n) {
		return dealWithNum(data, n, "dealLongunsigned");
	}
	public String[] dealLongunsigned(String data, TreeNode t) {
		String ret[] = { "", "" };
		node = new TreeNode("long-unsigned", DataConvert.hexString2Int(data.substring(0, 4))+"-0x"+data.substring(0, 4));
		t.addChild(node);
		ret[1] = data.substring(4);
		return ret;
	}

	public String[] dealUnsigned(String data, TreeNode t) {
		String ret[] = { "", "" };
		node = new TreeNode("Unsigned", DataConvert.hexString2Int(data.substring(0, 2))+"-0x"+data.substring(0, 2));
		t.addChild(node);
		ret[1] = data.substring(2);
		return ret;
	}

	public String[] dealSeqOfRegion(String data, TreeNode n) {
		return dealWithNum(data, n, "dealRegion");
	}

	public String[] dealRegion(String data, TreeNode t) {
		String ret[] = { "", "" };

		String unit =  data.substring(0, 2);
		if (unit.equals("00"))
			unit = unit+"-ǰ�պ�";
		if (unit.equals("01"))
			unit = unit+"-ǰ�����";
		if (unit.equals("02"))
			unit = unit+"-ǰ�պ��";
		if (unit.equals("03"))
			unit = unit+"-ǰ����";
		node = new TreeNode("Region", unit);
		t.addChild(node);

		// ����������DATA
		String tmp = data.substring(2);
		String[] s = dealDATA(tmp, node, false);
		s = dealDATA(s[1], node, false);
		ret = s;

		return ret;
	}

	public String[] dealSeqOfTSA(String data, TreeNode n) {
		return dealWithNum(data, n, "dealTSA");
	}

	public String[] dealTSA(String data, TreeNode t) {
		String ret[] = { "", "" };

		// 06 04 00 00 00 01 21
		int len = DataConvert.hexString2Int(data.substring(2, 4)) + 1;

		node = new TreeNode("TSA", data.substring(4, 2 + 2 + len * 2) +"-0x"+data.substring(0, 2 + 2 + len * 2));
		t.addChild(node);

		ret[1] = data.substring(2 + 2 + len * 2);
		return ret;
	}

	// RCSD
	public String[] dealSeqOfCSD(String data, TreeNode n) {
		return dealWithNum(data, n, "dealCSD");
	}

	public String[] dealCSD(String data, TreeNode t) {
		return new AnalyData().newDealCSD(data, t);
	}


	public String[] newDealCSD(String data, TreeNode t) {
		String ret[] = { "", "" };
		String s[] = { "", "" };

		int type = DataConvert.hexString2Int(data.substring(0, 2));
		TreeNode n = new TreeNode("CSD����", type);
		t.addChild(n);
		String restData = data.substring(2);

		if (type == 0) {
			// OAD
			s = dealOAD(restData, n);
		} else {
			// ROAD
			s = dealROAD(restData, n);
		}

		ret[1] = s[1];
		return ret;
	}

	public String[] dealROAD(String data, TreeNode t) {
		String ret[] = { "", "" };

		String s[] = dealOAD(data, t);

		node = new TreeNode("SEQUENCE OF OAD","");
		t.addChild(node);
		s = dealSeqOfOAD(s[1], node);

		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfOADWithDAR(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOADWithDAR");
	}

	public String[] dealOADWithDAR(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealOAD(data, t);
		s = dealDAR(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfResultRecord(String data, TreeNode n) {
		return dealWithNum(data, n, "dealResultRecord");
	}

	// ���������ret[0]�У����µĲ�����ret[1]
	public String[] dealFollowReport(String data, TreeNode t) {
		String ret[] = { "", "" };
		if (data.equals("")) return ret;
		String flag = data.substring(0, 2);
		if (flag.equals("00")) {
			node = new TreeNode("�����ϱ�", flag);
			t.addChild(node);
			ret[1] = data.substring(2);
		} else {

			String flag1 = data.substring(2, 4);
			String[] s = {"",""};
			if (flag1.equals("01")){
				node = new TreeNode("�����ϱ�(SEQUENCE OF A-ResultNormal)", "");
				t.addChild(node);
				s = dealSeqOfResultNormal(data, node);
			}
			else{
				node = new TreeNode("�����ϱ�(SEQUENCE OF A-ResultRecord)", "");
				t.addChild(node);
				s = dealSeqOfResultRecord(data, node);
			}
			ret[1] = s[1];
		}

		return ret;
	}

	// ��������ResultNormal RON ResultOfNormal
	public String[] dealResultNormal(String data, TreeNode t) {

		String ret[] = { "", "" };

		String[] s = dealOAD(data, t);

		s = dealResult(s[1], t);

		//int result = DataConvert.hexString2Int(s[1].substring(0, 2));

		ret[1] = s[1];
		return ret;
	}

	public String[] dealResult(String data, TreeNode t) {
		String ret[] = { "", "" };
		String type = DataConvert.String2HexString(data.substring(0, 2), 2);
		String msg = "DAR";
		if (type.equals("01"))
			msg = "DATA";
		node = new TreeNode("Get-Result("+msg+")",type );
		t.addChild(node);

		if (type.equals("00")) {
			ret = dealDAR(data.substring(2), node);
		} else
			// ��Ҫ����ǰ���OAD����struct���͵Ľ���
			ret = dealDATA(data.substring(2), node);

//		ret[0] = type;
//		ret[1] = data.substring(2);

		return ret;
	}

	public String[] dealTransResult(String data, TreeNode t) {
		String ret[] = { "", "" };
		String type = DataConvert.String2HexString(data.substring(0, 2), 2);
		String msg = "DAR";
		if (type.equals("01"))
			msg = "DATA";
		node = new TreeNode("Get-Result("+msg+")",type );
		t.addChild(node);

		if (type.equals("00")) {
			ret = dealDAR(data.substring(2), node);
		} else{
//			node = new TreeNode("octet-string",data.substring(2) );
//			t.addChild(node);
//			ret[0] = "";
//			ret[1] = "";
			ret = dealType09(data.substring(2), node);
		}

//		ret[0] = type;
//		ret[1] = data.substring(2);

		return ret;
	}

	// ���������ret[0]�У����µĲ�����ret[1]
	// �������OAD,���OADʹ��";"�ָ�
	public String[] dealSeqOfOADWithData(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOADWithData");
	}

	public String[] dealOADWithData(String data, TreeNode t) {
		String ret[] = { "", "" };

		String[] s = dealOAD(data, t);

		// dealDATA ׼����Ϊ�ӽڵ㣬������Ҫ�����ڵ���Ϊ��������
		s = dealDATA(s[1], t);
		ret[1] = s[1];
		return ret;
	}



	public String[] dealTimeTag(String data, TreeNode t) {
		String ret[] = { "", "" };

		// TimeTag OPTIONAL =
		// 00 + �޺���
		// 01 + ����ʱ�꣨DateTimeBCD[28]-14�ֽڣ�+����������ʱʱ�䣨TI[84]��
		// TI[84] = ENUMERATED-1 �ֽ� + long-unsigned[18]-2�ֽ�
		// {16����λ��������Unsigned16��}

		// TimeTag = OPTIONAL([28][84])
		// [84] = [ENUMERATED][18]

		// OPTIONAL = 00|01...

		// OPTIONAL

		if (data.equals("")) return ret;
		String flag = data.substring(0, 2);
		if (flag.equals("00")) {
			node = new TreeNode("ʱ���ǩ", flag);
			t.addChild(node);
			ret[1] = data.substring(2);
		} else {
			// ����ʱ�� DateTimeBCD�� 14
			// ����������ʱʱ�� TI 3
			node = new TreeNode("ʱ���ǩ", data.substring(2, 2 + 17 * 2));
			t.addChild(node);
			ret[1] = data.substring(2 + 17 * 2);
		}

		return ret;
	}

	public String[] dealType00(String data, TreeNode n) {
		String ret[] = { "", "" };
		ret[1] = data;
		return ret;
	}

	public String[] dealSeqOfOMDWithDARAndDataAndRead(String data,TreeNode n) {
		return dealWithNum(data, n, "dealOMDWithDARAndDataAndRead");
	}


	public String[] dealOMDWithDARAndDataAndRead(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealOMD(data,t);
		s = dealDAR(s[1], t);
		s = dealDATA(s[1], t);
		s = dealResultNormal(s[1], t);
		ret[1] = s[1];
		return ret;
	}


	public String[] dealSeqOfOMDWithDARAndData(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOMDWithDARAndData");
	}

	public String[] dealOMDWithDARAndData(String data, TreeNode t) {
		String ret[] = { "", "" };

		String[] s = dealOMD(data,t);

		s = dealDAR(s[1], t);
		// xuky 2016.08.16 ���������⣬Data �Ķ�����Data  OPTIONAL  ��һ�㶨�岻ͬ
		s = dealDATA(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfOMDWithDataAndRead(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOMDWithDataAndRead");
	}

	public String[] dealOMDWithDataAndRead(String data, TreeNode t) {
		String ret[] = { "", "" };

		String[] s = dealOMDWithData(data, t);

		s = dealOAD(s[1], t);

		node = new TreeNode("��ʱ��ȡʱ��", s[1].substring(0, 2));
		t.addChild(node);

		ret[1] = s[1].substring(2);
		return ret;
	}

	public String[] dealOMDWithData(String data, TreeNode t) {
		String ret[] = { "", "" };

		String[] s = dealOMD(data,t);

		s = dealDATA(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfOMDWithData(String data, TreeNode t) {
		String ret[] = { "", "" };
		int num = DataConvert.hexString2Int(data.substring(0, 2));
		TreeNode numNode = new TreeNode("����", num);
		t.addChild(numNode);

		String restData = data.substring(2);
		for (int i = 0; i < num; i++) {
			String[] s = dealOMDWithData(restData, numNode);
			ret[0] += s[0] + ";";
			restData = s[1];
		}
		ret[0] = ret[0].substring(0, ret[0].length() - 1);
		ret[1] = restData;
		return ret;
	}

	public String[] dealSeqOfOADWithDARAndData(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOADWithDARAndData");
	}

	public String[] dealOADWithDARAndData(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealOADWithDAR(data, t);
		s = dealResultNormal(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	// ���������ret[0]�У����µĲ�����ret[1]
	// �������OAD,���OADʹ��";"�ָ�
	public String[] dealSeqOfOADWithDataAndRead(String data, TreeNode n) {
		return dealWithNum(data, n, "dealOADWithDataAndRead");
	}

	public String[] dealOADWithDataAndRead(String data, TreeNode t) {
		String ret[] = { "", "" };

		String[] s = dealOADWithData(data, t);

		s = dealOAD(s[1], t);

		node = new TreeNode("��ʱ��ȡʱ��", s[1].substring(0, 2));
		t.addChild(node);

		ret[1] = s[1].substring(2);
		return ret;
	}

	// ��������Result
	public String[] dealResultRecord(String data, TreeNode t) {

		String ret[] = { "", "" };

		String[] s = dealOAD(data, t);

		// RCSD
		node = new TreeNode("SEQUENCE OF CSD(RCSD)","");
		t.addChild(node);
		s = dealSeqOfCSD(s[1], node);

		int result = DataConvert.hexString2Int(s[1].substring(0, 2));
		if (result == 0) {
			s = dealDAR(s[1].substring(2), t);
		} else {
			// �����ݽ���ʱ����Ҫ�ο�ǰ��dealSeqOfCSD�õ��ĸ���  numInOne
			// 6.3.8.2.3��GetResponseRecord��������
			node = new TreeNode("SEQUENCE OF A-RecordRow","");
			t.addChild(node);
			s = dealSeqOfRecordRow(DataConvert.String2Int(s[0]),
					s[1].substring(2), node);
		}

		ret[1] = s[1];
		return ret;
	}


	// ����ǰ��ĸ�����Ϣ���ڴ˽��ж��������Ŀ��ѭ������
	// 6.3.8.2.3��GetResponseRecord��������
	public String[] dealSeqOfRecordRow(int numInOne, String data,TreeNode t1) {
		String ret[] = { "", "" };
		String s[] = { "", "" };



		int num = DataConvert.hexString2Int(data.substring(0, 2));
		TreeNode t = new TreeNode("RecordRow����", num);
		t1.addChild(t);
		String restData = data.substring(2);

		if (restData.substring(0,2).equals("02")){
			// xuky 2016.12.20 ���н������ݽ��ȵĵ���
			// 68 42 00 C3 05 08 00 00 00 00 00 10 4C C4 85 03 0F 60 34 02 00 01 00 60 35 02 00 01 01 02 08 11 01 16 02 1C 07 E0 09 0C 00 00 00 1C 07 E0 09 0C 00 00 10 12 00 02 12 00 02 12 00 02 12 00 02 00 00 83 AF 16
			// ��ȡ�ɼ�����������   ʵ�����ݶ�Ӧ���ǽṹ����������飻���������ݶ�Ӧ��������
			currentMsgLine ++;
		}

		for (int i = 0; i < num; i++) {

			TreeNode n = new TreeNode("CSD����", numInOne);
			t.addChild(n);

			// // xuky 2017.06.08 ����������ݺ���ƥ��
			SHOW_MSG = false;
			for (int j = 0; j < numInOne; j++) {
//				s = dealDATA(restData, n);
				// xuky 2017.06.08 ����������ݺ���ƥ��
				s = dealDATA(restData, n );
				restData = s[1];
			}
			SHOW_MSG = true;
		}

		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfRecord(String data, TreeNode n) {
		return dealWithNum(data, n, "dealRecord");
	}

	public String getAPDUType(int choiseFlag) {
		String data = "1:Ԥ��������;129:Ԥ������Ӧ;2:����Ӧ����������;3:�Ͽ�Ӧ����������;"+
				"5:��ȡ����;6:��������;7:��������;8:�ϱ�Ӧ��;"+
				"9:��������;130:����Ӧ��������Ӧ;131:�Ͽ�Ӧ��������Ӧ;132:�Ͽ�Ӧ������֪ͨ;"+
				"133:��ȡ��Ӧ;134:������Ӧ;135:������Ӧ;136:�ϱ�֪ͨ;"+
				"137:������Ӧ;16:��ȫ����;144:��ȫ��Ӧ";
		String f = DataConvert.int2String(choiseFlag);
		String ret = "";
		for( String s:data.split(";")){
			if (s.split(":")[0].equals(f)){
				ret = s.split(":")[1];
				break;
			}
		}
		return ret;
	}

	public String getAPDUType1(int choiseFlag,int choiseFlag1) {
		String data = "5_1:��ȡһ��������������;5_2:��ȡ���ɸ�������������;5_3:��ȡһ����¼�Ͷ�����������;5_4:��ȡ���ɸ���¼�Ͷ�����������;5_5:��ȡ��֡��Ӧ����һ�����ݿ�����;"+
					"133_1:��ȡһ���������Ե���Ӧ;133_2:��ȡ���ɸ��������Ե���Ӧ;133_3:��ȡһ����¼�Ͷ������Ե���Ӧ;133_4:��ȡ���ɸ���¼�Ͷ������Ե���Ӧ;133_5:��֡��Ӧһ�����ݿ�;"+
					"9_1:������ȡ���ɸ������������ɸ�������������;9_2:������ȡһ����������һ����¼�Ͷ�����������;9_3:�����������ɸ������������ɸ�������������;9_4:�������ú��ȡ���ɸ������������ɸ�������������;9_5:�����������ɸ������������ɸ����󷽷�����;"+
					"9_6:�����������ȡ���ɸ������������ɸ����󷽷�����������;9_7:����͸��ת����������;";
		String f = DataConvert.int2String(choiseFlag)+"_"+DataConvert.int2String(choiseFlag1);
		String ret = "";
		for( String s:data.split(";")){
			if (s.split(":")[0].equals(f)){
				ret = s.split(":")[1];
				break;
			}
		}
		return ret;
	}

	public String getAPDUData(Object[][] data_attr,int seq, Boolean isRecord){
		String ret = "";
		String dataType="",datavalue="",dataname="";
		for(Object[] s:data_attr){

			if (s[0] == null)
				break;

			// seq != -1��ʾ�����ж�
			if (seq != -1)
				// �ж��Ƿ������seq
				if ((int)s[8] != seq)
						continue;

//			System.out.println("getAPDUData => dataname-"+ s[0] + " dataType-"+s[1]+" datavalue-"+s[2]);
			dataname  = (String)s[0];
			dataType = (String)s[1];
			datavalue = (String)s[2];

			if (dataname.indexOf("�ɼ�����")>=0){
				dataname = dataname +"";
			}

			// xuky 2016.09.02 �����CSD�е�OAD��Ϣ������ǰ��û�����ͱ�ʶ0x51��
			// xuky 2016.09.13 ���д��벻������ ������ͨ�ɼ�����  ��������
			//if (dataname.indexOf("CSD") >= 0){
			//	isRecord = true;
			//}
			// xuky 2016.09.13 ���Ӷ�dataType.indexOf("NULL")���жϣ�����ΪNULLʱ��������д����
			//System.out.println("getAPDUData=> dataname "+dataname);

			if (dataType.indexOf("NULL") <0 && (datavalue==null || datavalue.equals(""))){
				if (dataType.equals("enum") || dataType.toLowerCase().indexOf("unsigned")>=0 )
					datavalue = "0";
				else if (dataType.toLowerCase().indexOf("string")>=0)
					datavalue = "";
				else{
					String msg = "����д����������=>"+ dataname.replaceAll("��", "") ;
					DebugSwing.showMsg(msg);
					System.out.println(msg);
					return "";
					}
			}

			// ������ϢΪdatatypeid,bytenum
			String[] type = DB.getInstance().getBasicDataTypeInfo(dataType);

			if (type[0].equals("")){
				//�ɼ���ǰ����[0] NULL���ɼ��ϵ�N��[1] unsigned��������ʱ��ɼ�[2] NULL����ʱ�����ɼ�[3] TI
				//NULL����дNULL��0���ϵ�N�Σ���дN��ֵ���磺��1����д1��TI��Ӣ�İ�Ƕ��Ÿ�����15�����磺1,15
				// NULL 1�ֽ� 00;N 2�ֽ� 0x11+1�ֽ�;TI 4�ֽ�0x54+ENU+2�ֽ�
				if (dataType.equals("Data")){
					if (datavalue.toUpperCase().indexOf("NULL")>=0 || datavalue.toUpperCase().indexOf("-")>=0 || datavalue.equals("") || datavalue.equals("00") ){
						ret += "00";
						ret += ",";
					}
					else{
						if (datavalue.indexOf(",")>=0){
							// TI
							ret += "54"+"0"+datavalue.split(",")[0]+DataConvert.String2HexString(datavalue.split(",")[1], 4);
							ret += ",";
						}
						else{
							// ֱ�Ӿ��Ǳ�������
							datavalue = datavalue.replaceAll(" ", "");
							ret += Util698.seprateString(datavalue," ");
							ret += ",";
						}
					}
				}
				else if (dataType.indexOf("CHOICE")>=0){
					if (dataname.indexOf("CSD")>=0){
						if (isRecord)
							ret += DataConvert.String2HexString(datavalue, 2);
						else
							ret += "5B"+DataConvert.String2HexString(datavalue, 2);
						ret += ",";
					}
					else if (dataname.indexOf("MS")>=0){
						if (isRecord)
							ret += DataConvert.String2HexString(datavalue, 2);
						else
							ret += "5C"+DataConvert.String2HexString(datavalue, 2);
						ret += ",";
					}
					else if (dataname.indexOf("RSD")>=0){
						// �Ƚ����⣬������û�ж�Ӧ��RSD����
						ret += DataConvert.String2HexString(datavalue, 2);
						ret += ",";
					}
					else{
						String msg = "1δ�ҵ����Ͷ��壨���֪������Ա��=>"+dataType + " dataname=>"+ dataname ;
						DebugSwing.showMsg(msg);
						System.out.println(msg);
						return "";
						//System.exit(0);
					}
				}
				else if (dataType.equals("SEQUENCE")){
					//�޶�����  ��������
				}
				else if (dataType.equals("*NULL")){
					// xuky 2016.09.13 ��ͨ�ɼ������� ������֯�õ��ı���Ϊ������ ����ע�����´���
					// ������
					// xuky 2016.09.14 ��ͨ�ɼ������� ������֯�õ��ı���Ϊ������   ��������ݣ���Ҫ����dealMS����
					// �����ͨ������
					// NULLҲ������
					// xuky 2016.10.13 ���ľ� ���� ��ͨ�ɼ������� ������֯�õ��ı���Ϊ������   ����dealMS����
					// ������
					//ret += "00";
					//ret += ",";
				}
				else if (dataType.equals("NULL")){
					ret += "00";
					ret += ",";
				}
				else if (dataType.equals("SEQUENCE OF")){
					ret += DataConvert.String2HexString(datavalue, 2);
					ret += ",";
				}
				else if (dataType.indexOf("TSA")>=0){
					// xuky 2016.08.28 ��������ʱ����
					if (isRecord){
						if (!Util698.isEven(datavalue))
							datavalue = "0"+datavalue;
						int size = datavalue.length()/2;
						ret += DataConvert.int2HexString(size, 2)+datavalue;
						ret += ",";
					}
					else{
						ret += "55"+ getTSA(datavalue);
						ret += ",";
					}
				}
				else if (dataType.toLowerCase().equals("bit-string(size(8))")){
					// xuky 2016.08.28 ��������ʱ����
					ret += "0408"+DataConvert.binStr2HexString(datavalue,2);
					ret += ",";
				}
				else if (dataType.indexOf("-")>=0){
					// xuky 2016.09.13�����ݣ���վλ����
				}
				else if (dataType.indexOf("date_time_s")>=0){
					//2016-08-08 08:08:08
					// 1C 07 E0 08 08 08 08 08
					ret += "1C"+DataConvert.String2HexString(datavalue.substring(0, 4), 4);
					ret += DataConvert.String2HexString(datavalue.substring(5, 7), 2);
					ret += DataConvert.String2HexString(datavalue.substring(8, 10), 2);
					ret += DataConvert.String2HexString(datavalue.substring(11, 13), 2);
					ret += DataConvert.String2HexString(datavalue.substring(14, 16), 2);
					ret += DataConvert.String2HexString(datavalue.substring(17, 19), 2);
					ret += ",";
				}
				else if (dataType.indexOf("COMDCB")>=0){
					// xuky  2017.06.09 ���䳤��
					//3,2,8,1,0
					String[] array = datavalue.split(",");
					ret += "5F ";
					for(String str: array){
						ret += DataConvert.String2HexString(str, 2) +" ";
					}
					ret += ",";
				}
				else if (dataType.indexOf("*Region_3")>=0){
					//[0,3)  ǰ�� ��
					String tmp = datavalue.substring(0,1);
					tmp = datavalue.substring(datavalue.length()-1);
					if (datavalue.substring(0,1).equals("[") && datavalue.substring(datavalue.length()-1).equals(")"))
						ret += "00";
					if (datavalue.substring(0,1).equals("(") && datavalue.substring(datavalue.length()-1).equals("]"))
						ret += "01";
					if (datavalue.substring(0,1).equals("[") && datavalue.substring(datavalue.length()-1).equals("]"))
						ret += "02";
					if (datavalue.substring(0,1).equals("(") && datavalue.substring(datavalue.length()-1).equals(")"))
						ret += "03";
					tmp = datavalue.substring(1,datavalue.length()-1);
					ret += " 11"+DataConvert.String2HexString(tmp.split(",")[0],2)+" 11"+DataConvert.String2HexString(tmp.split(",")[1],2);
					ret += ",";
				}
				else{
					// δ�ҵ����Ͷ��� ��ֱ���˳�
					String msg = "2δ�ҵ����Ͷ��壨���֪������Ա��=>"+dataType;
					DebugSwing.showMsg(msg);
					System.out.println(msg);
				}
			}
			else{
				int bytenum = -1;
				if (type[1]!=null)
					bytenum = DataConvert.String2Int(type[1]);
				if (bytenum ==-1){
					//bytenumΪ��ʱ����Ҫ���н�������
					if ("array,structure".indexOf(dataType)>=0){
						// ��������+��������
						if (isRecord && dataType.equals("structure")) {
							// ��Ϊ����ڵ㣬����������
						}
						else{
							ret += DataConvert.String2HexString(type[0], 2);
							ret += DataConvert.String2HexString(datavalue, 2);
							ret += ",";
						}
					}
					else if (dataType.equals("octet-string")){
						int dataLen = 0;
						if (datavalue.indexOf("\u002E") >0){
							// 129.0.0.1
							dataLen = 4;
							String[] str = datavalue.split("\\.");
							datavalue = "";
							for(String t:str)
								datavalue += DataConvert.String2HexString(t, 2);
						}
						else{
							if (!Util698.isEven(datavalue))
								datavalue = "0" + datavalue;
							dataLen = datavalue.length() /2 ;
						}
						ret += "09" + DataConvert.int2HexString(dataLen, 2)+datavalue;
						ret += ",";
					}
					else if (dataType.indexOf("visible-string")>=0){
						int dataLen = datavalue.length();
						datavalue = DataConvert.string2ASCIIHexString(datavalue, dataLen);
						ret += "0A" + DataConvert.int2HexString(dataLen, 2)+datavalue;
						ret += ",";
					}
					else if (dataType.indexOf("TI")>=0){

						if (!isRecord)
							ret += "54";

						if ((datavalue.indexOf(",")) >=0){
							ret += "0"+datavalue.split(",")[0];
							ret += DataConvert.String2HexString(datavalue.split(",")[1],4);
						}
						else
							ret += datavalue;

						ret += ",";
					}
					else if (dataType.indexOf("COMDCB")>=0){
						//3,2,8,1,0
						String[] array = datavalue.split(",");
						ret += "5F ";
						for(String str: array){
							ret += DataConvert.String2HexString(str, 2) +" ";
						}
						ret += ",";
					}
					else if (dataType.indexOf("TSA")>=0){
						//55 07 05 00 00 00 00 00 01
						ret += "55 ";
						if (!Util698.isEven(datavalue))
							datavalue = "0"+datavalue;
						int size = datavalue.length()/2+1;
//						ret += DataConvert.int2HexString(size, 2)+DataConvert.int2HexString(size-2, 2)+datavalue;
						// xuky 2017.03.09
						ret += getTSA(datavalue);
						ret += ",";

					}
					else{
						String msg = "��Ҫ���ж���������������֪������Ա��=>"+dataType;
						DebugSwing.showMsg(msg);
						System.out.println(msg);
					}
				}
				else{
					if (isRecord){
						if (dataType.toLowerCase().indexOf("bcd")>=0 || dataType.indexOf("OAD")>=0 ){
							if (dataType.equals("datetimeBCD")){
								ret += dealDatetimeBCD(datavalue);
								ret += ",";
//								if ((datavalue.indexOf(":")) >=0){
//									// yyyy-mm-dd hh:mm:ss ��ʽת�� 123
//									String temp = DataConvert.String2HexString(datavalue.substring(0, 4),4);
//									temp += DataConvert.String2HexString(datavalue.substring(5, 7),2);
//									temp += DataConvert.String2HexString(datavalue.substring(8, 10),2);
//									temp += DataConvert.String2HexString(datavalue.substring(11, 13),2);
//									temp += DataConvert.String2HexString(datavalue.substring(14, 16),2);
//									temp += DataConvert.String2HexString(datavalue.substring(17, 19),2);
//									ret += temp;
//									ret += ",";
//								}
//								else{
//									// ������ֱ�����б༭�õ�����
//									ret += datavalue;
//									ret += ",";
//								}
							}
							else{
								ret += datavalue;
								ret += ",";
							}
						}
						else{
							ret += DataConvert.String2HexString(datavalue,bytenum*2 );
							ret += ",";
						}
					}
					else{

						// ����ǰ��Ҫ�������ݱ�ʶ

						// 2016.09.13 ����Ϊ��*OAD���жϣ���51���ݱ�ʶ
						if (dataType.indexOf("*OAD")<0)
							ret += DataConvert.String2HexString(type[0], 2);

						if (dataType.toLowerCase().indexOf("bcd")>=0 || dataType.indexOf("OAD")>=0){
							ret += datavalue;
							ret += ",";
						}
						else{
							ret += DataConvert.String2HexString(datavalue,bytenum*2 );
							ret += ",";
						}
					}
				}
			}
			//System.out.println("ret=>"+ret);

		}
		// ȥ������һ��,
		if (ret.length() > 0)
			if (ret.substring(ret.length()-1, ret.length()).equals(","))
				ret = ret.substring(0, ret.length()-1);

		return ret;
	}

	public String[] dealProxyRead(String data, TreeNode t) {
		String ret[] = { "", "" };
//		һ��Ŀ���������ַ         TSA��
//		����һ���������ĳ�ʱʱ��   long-unsigned��
//		���ɸ���������������       SEQUENCE OF OAD
		String[] s = dealTSA(data, t);
		node = new TreeNode("����һ���������ĳ�ʱʱ��", DataConvert.hexString2Int(s[1].substring(0,4)));
		t.addChild(node);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOAD(s[1].substring(4), node);
		ret[1] = s[1];
		return ret;
	}

	public String getTSA(String datavalue){
		String ret = "";
		if (!Util698.isEven(datavalue))
			datavalue = datavalue + "F";
		int size = datavalue.length()/2;
		ret = DataConvert.int2HexString(size+1, 2)+DataConvert.int2HexString(size-1, 2)+datavalue;
		return ret;
	}

	public String[] dealProxyReadResp(String data, TreeNode t) {
		String ret[] = { "", "" };
//		һ��Ŀ���������ַ         TSA��
//		���ɸ���������������������  SEQUENCE OF (OAD+Get-Result)

		String[] s = dealTSA(data, t);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�\
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfResultNormal(s[1], node);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxyRequest(String data, TreeNode t) {
		String ret[] = { "", "" };
//		һ��Ŀ���������ַ         TSA��
//		����һ���������ĳ�ʱʱ��   long-unsigned��
//		���ɸ���������������       SEQUENCE OF OAD Data
		String[] s = dealTSA(data, t);
		node = new TreeNode("����һ���������ĳ�ʱʱ��", DataConvert.hexString2Int(s[1].substring(0,4)));
		t.addChild(node);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOADWithData(s[1].substring(4), node);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxySetAndRead(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealTSA(data, t);
		node = new TreeNode("����һ���������ĳ�ʱʱ��", DataConvert.hexString2Int(s[1].substring(0,4)));
		t.addChild(node);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOADWithDataAndRead(s[1].substring(4), node);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxyOMD(String data, TreeNode t) {
		String ret[] = { "", "" };
		//һ��Ŀ���������ַ
		String[] s = dealTSA(data, t);

		node = new TreeNode("����һ���������ĳ�ʱʱ��", DataConvert.hexString2Int(s[1].substring(0,4)));
		t.addChild(node);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOMDWithData(s[1].substring(4), node);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxyOMDAndRead(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealTSA(data, t);
		node = new TreeNode("����һ���������ĳ�ʱʱ��", DataConvert.hexString2Int(s[1].substring(0,4)));
		t.addChild(node);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOMDWithDataAndRead(s[1].substring(4), node);
		ret[1] = s[1];
		return ret;
	}


	public String[] dealProxySetResp(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealTSA(data, t);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOADWithDAR(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxySetAndReadResp(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealTSA(data, t);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOADWithDARAndData(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxyOMDResp(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealTSA(data, t);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOMDWithDARAndData(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealProxyOMDAndReadResp(String data, TreeNode t) {
		String ret[] = { "", "" };
		String[] s = dealTSA(data, t);

		// xuky 2016.11.08 ������������һ��dealSeqOfXXX��������������������һ�����ڵ�
		node = new TreeNode("SEQUENCE OF ","");
		t.addChild(node);

		s = dealSeqOfOMDWithDARAndDataAndRead(s[1], t);
		ret[1] = s[1];
		return ret;
	}

	public String[] dealSeqOfProxyRead(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyRead");
	}

	public String[] dealSeqOfProxyReadResp(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyReadResp");
	}

	public String[] dealSeqOfProxyRequest(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyRequest");
	}

	public String[] dealSeqOfProxySetAndRead(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxySetAndRead");
	}
	public String[] dealSeqOfProxyOMD(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyOMD");
	}

	public String[] dealSeqOfProxyOMDAndRead(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyOMDAndRead");
	}

	public String[] dealSeqOfProxySetResp(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxySetResp");
	}

	public String[] dealSeqOfProxySetAndReadResp(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxySetAndReadResp");
	}

	public String[] dealSeqOfProxyOMDResp(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyOMDResp");
	}

	public String[] dealSeqOfProxyOMDAndReadResp(String data, TreeNode n) {
		return dealWithNum(data, n, "dealProxyOMDAndReadResp");
	}

	static public String dealDatetimeBCD(String datavalue) {
		String ret = "";
		if ((datavalue.indexOf(":")) >=0){
			// yyyy-mm-dd hh:mm:ss ��ʽת��
			String temp = DataConvert.String2HexString(datavalue.substring(0, 4),4);
			temp += DataConvert.String2HexString(datavalue.substring(5, 7),2);
			temp += DataConvert.String2HexString(datavalue.substring(8, 10),2);
			temp += DataConvert.String2HexString(datavalue.substring(11, 13),2);
			temp += DataConvert.String2HexString(datavalue.substring(14, 16),2);
			temp += DataConvert.String2HexString(datavalue.substring(17, 19),2);
			ret += temp;
		}
		else{
			// ������ֱ�����б༭�õ�����
			ret += datavalue;
		}
		return ret;
	}


	public void main(String[] args) {

	}

}