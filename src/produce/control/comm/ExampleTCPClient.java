package produce.control.comm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.eastsoft.util.Debug;

import util.Util698;

public class ExampleTCPClient {
	String SendData(String data) {
		String ret = "";
		String modifiedSentence;// �ӷ������õ������͵��û���׼���
		Socket clientSocket;
		try {
			clientSocket = new Socket("127.0.0.1", 8008);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());// �������ӵ���׼����
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));// �����׽��ֵ�������
			Util698.log(ExampleTCPClient.class.getName(), "Send to Svr:" + data, Debug.LOG_INFO);

			outToServer.writeBytes(data + '\n'); // outToServer����������
			modifiedSentence = inFromServer.readLine();// �����������ʱ�����ǽ���modifiedSentence�ַ�����
			ret = modifiedSentence;
			Util698.log(ExampleTCPClient.class.getName(), "Recv From Svr:" + modifiedSentence, Debug.LOG_INFO);
			outToServer.close();
			clientSocket.close();// �ر��׽��֣�tcp����Ҳ��֮�ر�
		} catch (Exception e) {
			e.printStackTrace();
		} // clientSocketΪ������׽���
		return ret;
	}

	public static String[] getUsableInfo(){
		String[] ret = {"",""};
		ExampleTCPClient exampleTCPClient = new ExampleTCPClient();
		String result = "";
		List<String> names = Util698.getSrialNames(true);
		String[] SModels = {"HS5100","HS5300","HS5320"};
		Collections.reverse(names);
		Boolean break1 = false;
		for( String name:names ){
			if (break1)
				break;
			name = name.substring(3);
			for( String SModel: SModels ){
				exampleTCPClient.SendData("[Dll_Port_Close]");
				result = exampleTCPClient.SendData("[ReadMeter]type="+SModel+";port="+name+";");
				if (!result.equals("[]")){
					ret[0] = SModel;
					ret[1] = name;
					break1 = true;
					break;
				}
			}
		}
		return ret;
	}

	public static void main(String args[]) throws Exception {
//		// �Զ��жϴ������ͺͱ�׼������
//		System.out.println(Util698.getDateTimeSSS_new());
//		String[] ret = getUsableInfo();
//		String type = ret[0];
//		String port = ret[1];
//		System.out.println("find:"+type+"-"+port);
//		System.out.println(Util698.getDateTimeSSS_new());
		ExampleTCPClient exampleTCPClient =new ExampleTCPClient();
		// ��Դ
//		exampleTCPClient.SendData("[on]type="+type+";port="+port+";");

		// �����������������̨���ͨ��
		// 1������DLL���������Ŷӣ���Ϊֻ��һ��̨�壬ִ����DLL���ã�Ҫ�ȴ��ظ��󣬲��ܽ�����һ����
		// 2���ز�ͨ���Ŷӣ��������ģ�һ����������һ���ǲ�ѯ�����������Ҫ��β�ѯ�����  ���ν���ʵ��һ�����
		// 3������ͨ���Ŷ�  ��ռ
		// 4��̨���ÿ����λ��Ҫִ�в��Թ��̶��У����32�����У�����ǰ�������н������ݽ������Լ�Ҳ��ͨ��RJ�뼯�����ĸ����˿ڽ���ͨ��

		// ���ʹ��ڱ��ģ��л�����ģʽ ����Ϊ������������ģʽ  Ҳ���Գ��Է���һ��DLL���ƺ�������̨�����ģʽ�л�
		// 0�������� 1������� 2 ����ר�� meter=65 �㲥
//		exampleTCPClient.SendData("[ChangeMeterInterface]meter=65;port=46;Flag=0;");

////		String sData = "";
////		sData = "6800008101166068140E04669601810116010203040506CC9416";
////		new CommWithRecv().deal_one("�޸�IP"+"-485_1","COM11:2400",sData);

		//
////		// ����ң�ſ��Ʒ�ʽΪ�̽�
//		exampleTCPClient.SendData("[SetFCAddVolt]meter=12;port=46;Flag=0;");
////		// ��ѯң�ſ��Ʒ�ʽΪ ��= 0
//		exampleTCPClient.SendData("[GetFCAddVolt]meter=12;port=46;");


////		// ң��ǰ6· ��·  PChar
//		exampleTCPClient.SendData("[SetFSState]meter=12;port=46;Flag=11000000;");
////		// ��ѯң��״̬  ��= 11111000
//		exampleTCPClient.SendData("[GetFSState]meter=12;port=46;");
//
//		// ң��ǰ6· ��·
//		exampleTCPClient.SendData("[SetFSState]meter=12;port=46;Flag=00000000;");
//		// ��ѯң��״̬  ��= 00000000
//		exampleTCPClient.SendData("[GetFSState]meter=12;port=46;");

//		20��ֱ����ѹ��⿪ʼ/ֹͣ
//		����:01H+��ַ(A��Z) +����+B2H(����) +(30H/��ʼ  31H/ֹͣ)+У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
//		21��ֱ����ѹ����ȡ
//		����:01H+��ַ(A��Z) +����+B3H(����) +У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)  ???
//		 12V ֱ����ѹ��⿪ʼ
		exampleTCPClient.SendData("[SetCheckDCState]meter=12;port=46;Flag=0;");
		// ֱ����ѹ��ȡ
		exampleTCPClient.SendData("[GetDCValue]meter=12;port=46;");
//		// ֱ����ѹ������
//		exampleTCPClient.SendData("[SetCheckDCState]meter=12;port=46;Flag=1;");
//
////		1·ֱ����ѹ��0-5V�������1·ֱ��������4-20mA��
//
//		24��ֱ��ģ�������ѡ�� ------��2013-01-07���� ��
//		����:01H+��ַ(A��Z) +����+B6H(����) +(31H/��ѹ  30H/����)+У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
//		25��ֱ��ģ����������� ------��2013-01-07���� ��
//		����:01H+��ַ(A��Z) +����+B7H(����) +У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 31H(��ѹ)/30H(����)+У��λ+����(17H)
//		18������PWMռ�ձ�
//		����:01H+��ַ(A��Z) +����+B0H(����) +��λ1+��λ+��λ+У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
//
//		19��PWM ���/ֹͣ ����
//		����:01H+��ַ(A��Z) +����+B1H(����) +(30H/��ʼ  31H/ֹͣ)+У��λ+����(17H)
//		   �ӻ�:01H+��ַ(A��Z +����)+ 06H(�϶�)/15H(��)+У��λ+����(17H)
//		//ֱ��ģ�������ѡ��  1/��ѹ
		exampleTCPClient.SendData("[SetDCOutType]meter=12;port=46;Flag=1;");
		exampleTCPClient.SendData("[GetDCOutType]meter=12;port=46;");
		// ���  4.88V��ѹ
		exampleTCPClient.SendData("[SetPWMPara]meter=12;port=46;Flag=0;Double=4.88;");
		// ��ʼ
		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=0;");
//		// ����
//		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=1;");
//

//		//ֱ��ģ�������ѡ�� 0/����
//		exampleTCPClient.SendData("[SetDCOutType]meter=12;port=46;Flag=0;");
//		exampleTCPClient.SendData("[GetDCOutType]meter=12;port=46;");
//		// ���  18.81mA����
//		exampleTCPClient.SendData("[SetPWMPara]meter=12;port=46;Flag=1;Double=18.81;");
//		// ��ʼ
//		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=0;");
//		// ����
//		exampleTCPClient.SendData("[SetPWMState]meter=12;port=46;Flag=1;");
//
//		// �ϵ�
//		exampleTCPClient.SendData("[off]port=46;");
//		exampleTCPClient.SendData("[off]port=46;");
	}

}
