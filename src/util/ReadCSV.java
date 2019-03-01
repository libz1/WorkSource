package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadCSV {
	public static void  main(String[] arg){
		 List<String> list= readFileByLines("d:/0 20181217-08(GZ100000014)ɽ���繤�ӱ�HPLC����ģ�����Ʒ����ȷ����(V1.0).csv");
		 System.out.println(list.size());
	}

	public static List<String> readFileByLines(String fileName) {
		List<String> list = new ArrayList<String>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
//			if (sysprint)
//				System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
//			int line = 1;
			// һ�ζ���һ�У�ֱ������nullΪ�ļ�����
			while ((tempString = reader.readLine()) != null) {
				// ��ʾ�к�
//				if (sysprint)
//					System.out.println("line " + line + ": " + tempString);
				list.add(tempString);
//				line++;
			}
			reader.close();
		} catch (IOException e) {
//			print("�ļ�������" + fileName);
			// e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return list;
	}

}
