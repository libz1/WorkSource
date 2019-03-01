package util;

import static com.eastsoft.util.Debug.print;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;


/**
 * ��ȡ�ļ���.
 *
 * @author xuky
 * @version 2013.10.24
 */
public class FileToRead {

	/**
	 * ���ֽ�Ϊ��λ��ȡ�ļ�.
	 * <p>
	 * �����ڶ��������ļ�����ͼƬ��������Ӱ����ļ�
	 *
	 * @param fileName
	 *            <code>String</code>�ļ���
	 */
	public static void readFileByBytes(String fileName) {

		InputStream in = null;

		// ���ֽ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���ֽ�
		try {
			System.out.println("���ֽ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���ֽڣ�");

			// ��fileΪ�������죨������FileInputStream
			File file = new File(fileName);
			in = new FileInputStream(file);

			int tempbyte;
			while ((tempbyte = in.read()) != -1) {
				System.out.write(tempbyte);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// ���ֽ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�����ֽ�
		try {
			System.out.println("���ֽ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�����ֽڣ�");
			byte[] tempbytes = new byte[100];
			int byteread = 0;

			// ���ļ������ַ�����Ϊ�������죨������FileInputStream
			in = new FileInputStream(fileName);

			FileToRead.showAvailableBytes(in);
			// �������ֽڵ��ֽ������У�bytereadΪһ�ζ�����ֽ���
			while ((byteread = in.read(tempbytes)) != -1) {
				System.out.write(tempbytes, 0, byteread);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * ���ַ�Ϊ��λ��ȡ�ļ�.
	 * <p>
	 * �����ڶ��ı������ֵ����͵��ļ�
	 *
	 * @param fileName
	 *            <code>String</code>�ļ���
	 */
	public static void readFileByChars(String fileName) {
		File file = new File(fileName);
		Reader reader = null;

		// һ�ζ�һ���ַ�
		try {
			System.out.println("���ַ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���ֽڣ�");

			reader = new InputStreamReader(new FileInputStream(file));
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				// ����windows�£�rn�������ַ���һ��ʱ����ʾһ�����С�
				// ������������ַ��ֿ���ʾʱ���ỻ�����С�
				// ��ˣ����ε�r����������n�����򣬽������ܶ���С�
				if (((char) tempchar) != 'r') {
					System.out.print((char) tempchar);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// һ�ζ�����ַ�
		try {
			System.out.println("���ַ�Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�����ֽڣ�");

			char[] tempchars = new char[30];
			int charread = 0;
			reader = new InputStreamReader(new FileInputStream(fileName));
			// �������ַ����ַ������У�charreadΪһ�ζ�ȡ�ַ���
			while ((charread = reader.read(tempchars)) != -1) {
				// ͬ�����ε�r����ʾ
				if ((charread == tempchars.length)
						&& (tempchars[tempchars.length - 1] != 'r')) {
					System.out.print(tempchars);
				} else {
					for (int i = 0; i < charread; i++) {
						if (tempchars[i] == 'r') {
							continue;
						} else {
							System.out.print(tempchars[i]);
						}
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * ����Ϊ��λ��ȡ�ļ�.
	 * <p>
	 * �����ڶ������еĸ�ʽ���ļ�
	 *
	 * @param fileName
	 *            <code>String</code>�ļ���
	 * @return <code>String</code>�ļ�����
	 */
	public static String readFileByLines(String fileName, Boolean sysprint, String separator) {
		String sRet = "";
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
				sRet = sRet + tempString +separator;
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
		return sRet;
	}

	/**
	 * ��ȡ����Ŀ¼�µ��ļ�.
	 * <p>
	 * ·����ϢΪ��ִ�г�������Ŀ¼�µ��ļ�(Ҳ��������Ŀ¼�µ��ļ�)
	 *
	 * @param fileName
	 *            <code>String</code>�ļ���
	 */
	public static String readLocalFile1(String fileName) {
		String str = "";
		try{
			fileName = System.getProperty("user.dir") + "\\" + fileName;
			str = FileToRead.readFileByLines(fileName, false, "");
		}
		catch (Exception e) {
//			SharedPreferences preferences = getSharedPreferences("PLCDebug", 0);
//			str = preferences.getString("SoftParameter", null);
//			if (str == null)
//				str = "";

		}
		return str;
	}

	/**
	 * ��ȡ����Ŀ¼�µ��ļ�.
	 * <p>
	 * ·����ϢΪ��ִ�г�������Ŀ¼�µ��ļ�(Ҳ��������Ŀ¼�µ��ļ�)
	 *
	 * @param fileName
	 *            <code>String</code>�ļ���
	 */
	public String readFile(String fileName) {
		String str = "";
		try{
			fileName = System.getProperty("user.dir") + "\\" + fileName;
			str = FileToRead.readFileByLines(fileName, false, "");
		}
		catch (Exception e) {
//			SharedPreferences preferences = getSharedPreferences("PLCDebug", 0);
//			str = preferences.getString("SoftParameter", null);
//			if (str == null)
//				str = "";

		}
		return str;
	}



	/**
	 * �����ȡ�ļ�����
	 *
	 * @param fileName
	 *            �ļ���
	 */
	public static void readFileByRandomAccess(String fileName) {
		RandomAccessFile randomFile = null;
		try {
			System.out.println("�����ȡһ���ļ����ݣ�");
			// ��һ����������ļ�������ֻ����ʽ
			randomFile = new RandomAccessFile(fileName, "r");
			// �ļ����ȣ��ֽ���
			long fileLength = randomFile.length();
			// ���ļ�����ʼλ��
			int beginIndex = (fileLength > 4) ? 4 : 0;
			// �����ļ��Ŀ�ʼλ���Ƶ�beginIndexλ�á�
			randomFile.seek(beginIndex);
			byte[] bytes = new byte[10];
			int byteread = 0;
			// һ�ζ�10���ֽڣ�����ļ����ݲ���10���ֽڣ����ʣ�µ��ֽڡ�
			// ��һ�ζ�ȡ���ֽ�������byteread
			while ((byteread = randomFile.read(bytes)) != -1) {
				System.out.write(bytes, 0, byteread);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (randomFile != null) {
				try {
					randomFile.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * ��ʾ�������л�ʣ���ֽ���
	 *
	 * @param in
	 */
	private static void showAvailableBytes(InputStream in) {
		try {
			System.out.println("��ǰ�ֽ��������е��ֽ���Ϊ:" + in.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String fileName = "C:/temp/newTemp.txt";
		// ReadFromFile.readFileByBytes(fileName);
		// ReadFromFile.readFileByChars(fileName);
		String str = FileToRead.readFileByLines(fileName, false,"");
		print(str);
		// ReadFromFile.readFileByRandomAccess(fileName);
	}

}
