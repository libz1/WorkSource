package produce.control.simulation;

// ģ���̨
public class SLPlatform {
	// �������socket�˿ڣ��������ݻظ�

	// �����յ��ı��ģ��õ����Իظ��ı���
	public static String getReply(String recv){
		String ret ="";
		if (recv.startsWith("F9F9F9F9F9") && recv.length() == 92){
			// ̨����Դ����Դ���Ʊ���
			ret = recv;
		}
		if (recv.startsWith("FEFEFEFEFE01")){
			// ̨��-�ն˲�����ؿ��Ʊ���
			ret = "FEFEFEFEFE01"+"";
		}
		return ret;
	}
}
