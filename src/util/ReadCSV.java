package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadCSV {
	public static void  main(String[] arg){
		 List<String> list= readFileByLines("d:/0 20181217-08(GZ100000014)山东电工河北HPLC单相模块类产品生产确认项(V1.0).csv");
		 System.out.println(list.size());
	}

	public static List<String> readFileByLines(String fileName) {
		List<String> list = new ArrayList<String>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
//			if (sysprint)
//				System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
//			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
//				if (sysprint)
//					System.out.println("line " + line + ": " + tempString);
				list.add(tempString);
//				line++;
			}
			reader.close();
		} catch (IOException e) {
//			print("文件不存在" + fileName);
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
