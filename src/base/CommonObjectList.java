package base;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eastsoft.fio.ReadWriteExcel;
import com.eastsoft.util.DataConvert;
import com.eastsoft.util.DebugSwing;
import com.google.gson.Gson;

import util.DB;
import util.Util698;
import util.Util698.NewObjAction;

/**
 * ���������޹صļ��ϼ�����
 * @author xuky
 * @version 2016.10.18
 */
public class CommonObjectList<T> {

	// ��Ӧ�����ݿ����
	String tableName;

	// ������
	String idColName;

	// ��Ҫ�ֶ�
	String codeColName;

	// ��������Ԫ�ض�����ⲿ�¼�
	NewObjAction newobj_act;

	// �����ʵ�����Ķ���
	Object object;

	// �洢���ݵļ���
	private List objList = new ArrayList<T>();

	// �����������ֶ���Ϣ���ֶ���ʾ��Ϣ���ֶ�����
	String[] exportColmunNames;
	String exportColmuns;
	String exportFileName;


	// ������
	private CommonObjectList(){

	}

	// ���캯���У���Ҫ���봴��������¼�
	public CommonObjectList(NewObjAction newobj_act,String tableName,String idColName,String codeColName) {
		this.newobj_act = newobj_act;
		this.tableName = tableName;
		this.idColName = idColName;
		this.codeColName = codeColName;
	}

	public CommonObjectList(NewObjAction newobj_act,String tableName,String codeColName,String where,String order) {
		init(newobj_act,tableName,codeColName,where,order);
	}

	public CommonObjectList(NewObjAction newobj_act,String tableName,String codeColName) {
		init(newobj_act,tableName,codeColName,"","");
	}

	// ������Ԫ��תΪ���϶�������
	public void setDataFromArray(Object[][] data){
		objList = Util698.array2ObjList(data,newobj_act );
	}

	private void init(NewObjAction newobj_act,String tableName,String codeColName,String where,String order){
		this.newobj_act = newobj_act;
		this.tableName = tableName;
		this.codeColName = codeColName;
		object = newobj_act.getNewObject();
		// ��ȡ������׸�������Ϣ����Ϊ�����ֶ���Ϣ
		Map<String, Object> fileld = Util698.getFirstFiledsInfo(object);
		this.idColName = fileld.get("name").toString();
		Object[][] data = DB.getInstance().getDataList(tableName,where,order,object);
		setDataFromArray(data);
	}


	// ���ַ���תΪ���ϣ���Ҫ����������Ϣ
	public void converFormString(String s,Type type) {
		if (s == null || s.equals(""))
			objList = new ArrayList<T>();
		objList = new Gson().fromJson(s,type);
	}

	// ������תΪ�ַ���
	public String converToString() {
		return new Gson().toJson(objList);
	}

	public List<T> getList() {
		return objList;
	}

	// ��������id����ȡһ������
	public Object getOne(int id) {
		return getByCode(id,getIDString());
	}

	// �����ֶ���Ϣ����ȡһ������
	public Object getByCode(int code,String getter) {
		Object object = null;
		try {
			for (Object o : objList){
	            Method method = o.getClass().getMethod(getter, new Class[] {});
	            Object value = method.invoke(o, new Object[] {});
	            if ((int)value == code){
				    object = o;
				    break;
	            }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	// �����ֶ���Ϣ����ȡһ������
	// ������Ҫ���������ֶι����Ƚϵ�����
	public Object getByCode(String code,String getter) {
		Object object = null;
		try {
			for (Object o : objList){
				String value = (String) Util698.getObjectAttrs(o,getter);
				if (value.equals(code)){
				    object = o;
				    break;
				}
//	            Method method = o.getClass().getMethod(getter, new Class[] {});
//	            Object value = method.invoke(o, new Object[] {});
//	            if (  ((String)value).equals(code) ){
//				    object = o;
//				    break;
//	            }

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	// id������Ϣ����������ֵ���͵ģ���Ȼmaxȡֵ����ִ���
	public int getUseableID() {
		int tempID = DB.getInstance().getMaxId(tableName,idColName);
		return tempID;
	}

	public void addAll() {
//		for( int i = objList.size()-1;i>=0;i-- ){
//			Object o = objList.get(i);
//			addWithParam(o,false,false);
//		}
		DB.getInstance().addAllData(tableName,objList);
	}

	public void addAll(List objList) {
//		for( int i = objList.size()-1;i>=0;i-- ){
//			Object o = objList.get(i);
//			addWithParam(o,false,false);
//		}
		DB.getInstance().addAllData(tableName,objList);
	}

	public String add(Object o) {
		return addWithParam(o,true,false);
	}

	public String add(Object[] o,int num) {
		return addWithParam(o,true,false,num);
	}

	public String add(Object o,Boolean noDBOp) {
		return addWithParam(o,true,noDBOp);
	}

	public String addWithParam(Object[] o_array, Boolean addtoList,Boolean noDBOp,int num) {
		String ret = "";
		if (addtoList ){
			for( int i=0;i<num;i++){
				Object o = o_array[i];
				if (!noDBOp){
					// �����ظ������ݣ����Ǹ���������Ϣ���ң��Ǹ�����Ҫ�ֶ���Ϣ����
					// ���ܴ��ڶ���ֶε������������ͳͳתΪ�ַ��������ƥ�����
					String getter = getCodeColString();
					String value = (String) Util698.getObjectAttrs(o,getter);
			        Object tmp = null ;
			       	tmp = getByCode(value,getter);
					if (tmp != null){
						// �����˹ؼ������ظ������⣬�����������ǲ�������ӵģ�Ӧ����ʾ�û��������޸�
						ret = "�ؼ������ظ�������! �ؼ������ֶ���:"+getter +"�ؼ���������:"+value ;
						DebugSwing.showMsg(ret);
						return ret;
					}
				}
				objList.add((T) o);
			}
		}
		//Object[] data = Util698.obj2Array(tmp);
		if (!noDBOp)
			DB.getInstance().addData(tableName,o_array,num);

		return ret;
	}

	// Boolean addtoList ��ʾ�Ƿ���Ҫ��ӵ�list������
	public String addWithParam(Object o, Boolean addtoList,Boolean noDBOp) {
		String ret = "";
		if (addtoList ){
			if (!noDBOp){
				// �����ظ������ݣ����Ǹ���������Ϣ���ң��Ǹ�����Ҫ�ֶ���Ϣ����
				// ���ܴ��ڶ���ֶε������������ͳͳתΪ�ַ��������ƥ�����
				String getter = getCodeColString();
				String value = (String) Util698.getObjectAttrs(o,getter);
		        Object tmp = null ;
		       	tmp = getByCode(value,getter);
				if (tmp != null){
					// �����˹ؼ������ظ������⣬�����������ǲ�������ӵģ�Ӧ����ʾ�û��������޸�
					ret = "�ؼ������ظ�������! �ؼ������ֶ���:"+getter +"�ؼ���������:"+value ;
					DebugSwing.showMsg(ret);
					return ret;
				}
			}
			objList.add((T) o);
		}
		//Object[] data = Util698.obj2Array(tmp);
		if (!noDBOp)
			DB.getInstance().addData(tableName,o);
		return ret;
	}

	// ɾ���������
	public void deleteByCode(String code,String getter,Boolean noDBOp) {
		int[] ids = new int[2000];
		Object[] objects = new Object[2000];
		int num = 0;

		Object object = null;
		try {
//			for (Object o : objList){
			// ��Ϊ�漰��ɾ�����ݣ�������Ҫ����Լ��Ͻ��д���
			for (int i=objList.size()-1;i>=0;i--){
				Object o = objList.get(i);
				String value = (String) Util698.getObjectAttrs(o,getter);
				if (value.equals(code)){
					object = o;
					objList.remove(o);
					if (!noDBOp){
						int id = (int) Util698.getObjectAttr(object,getIDString());
						ids[num] = id;
						objects[num] = object;
						num ++;
					}
				}
			}

			if (!noDBOp){
				DB.getInstance().deleteData(ids,tableName,idColName,objects,num);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void delete(int ID) {
		Object deleteObject = null;
		String getter =  getIDString();
		for (Object o : objList){
	        Object value = Util698.getObjectAttr(o,getter);
			if (value == null)
				value = 0;
			if ((value).equals(ID)) {
				deleteObject = o;
				objList.remove(o);
				break;
			}
		}
		DB.getInstance().deleteData(ID,tableName,idColName,deleteObject);
	}

	public void deleteAll(int[] ids) {
		DB.getInstance().deleteAllData(ids,tableName,idColName);
	}

	public void deleteAll() {
//		System.out.println("deleteAll=>"+DateTimeFun.getDateTimeSSS() +" begin");

//		for( int i = objList.size()-1;i>=0;i-- ){
//			Object o = objList.get(i);
//			objList.remove(o);
//			Map<String, Object> info = Util698.getFirstFiledsInfo(o);
//			int ID = (int) Util698.getFieldValueByName(info.get("name").toString(), o);
//			System.out.println("deleteAll=>"+DateTimeFun.getDateTimeSSS() +" listOPT end");
//			DB.getInstance().deleteData(ID,tableName,idColName);
//			System.out.println("deleteAll=>"+DateTimeFun.getDateTimeSSS() +" DBOPT end");
//		}

		// xuky 2016.11.07 ��������SQL�����͵������У���ʱ����
		int[] ids = new int[objList.size()];
		for( int i = objList.size()-1;i>=0;i-- ){
			Object o = objList.get(i);
			objList.remove(o);
			Map<String, Object> info = Util698.getFirstFiledsInfo(o);
			int ID = (int) Util698.getFieldValueByName(info.get("name").toString(), o);
			ids[i] = ID;
//			System.out.println("deleteAll=>"+DateTimeFun.getDateTimeSSS() +" listOPT end");
//			DB.getInstance().deleteData(ID,tableName,idColName);
//			System.out.println("deleteAll=>"+DateTimeFun.getDateTimeSSS() +" DBOPT end");
		}
		DB.getInstance().deleteAllData(ids,tableName,idColName);

	}

	private String getIDString(){
        String firstLetter = idColName.substring(0, 1).toUpperCase();
        String getter = "get" + firstLetter + idColName.substring(1);
        return getter;
	}

	//
	private String getCodeColString(){
		String firstLetter = "", getter = "";

		if (codeColName.indexOf(",") < 0)
			codeColName += ",";

			// ��,�ָ�����ʾ�ж���ֶ�
			String[] tmp = codeColName.split(",");
			for ( String str: tmp){
				firstLetter = str.substring(0, 1).toUpperCase();
				getter += "get" + firstLetter + str.substring(1)+",";
			}
//		}
//		else{
//	        firstLetter = codeColName.substring(0, 1).toUpperCase();
//	        getter = "get" + firstLetter + codeColName.substring(1);
//		}
        return getter;
	}

	public void update(Object object) {
        String getter = getIDString();
        Object value2 = Util698.getObjectAttr(object,getter);
        Object value1 = null;

		for (int i= 0;i<objList.size() ;i++ ){
			Object o = objList.get(i);
	        value1 = Util698.getObjectAttr(o,getter);
			if (value1.equals(value2)) {
				objList.set(i, object);
				break;
			}
		}

		// xuky 2016.11.11 �������remove��add�������ᵼ��objList�Ĵ���仯�����ⲿ���б���ʱ����ִ���ConcurrentModificationException
//		for (Object o : objList){
//	        value1 = Util698.getObjectAttr(o,getter);
//			if (value1.equals(value2)) {
//				objList.remove(o);
//				objList.add((T) object);
//				break;
//			}
//		}
		DB.getInstance().updateData(tableName,object);
	}

	public int size() {
		return objList.size();
	}


	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public void setExportColmunNames(String[] exportColmunNames) {
		this.exportColmunNames = exportColmunNames;
	}
	public void setExportColmunNames(String exportColmunNames) {
		this.exportColmunNames = exportColmunNames.split(",");
	}
	public String[] getExportColmunNames() {
		return exportColmunNames;
	}


	public void setExportColmuns(String exportColmuns) {
		this.exportColmuns = exportColmuns+",";
	}

	public String[][] getStringArray() {
		String[][] data = null;
		int aRow = objList.size();
		if (aRow != 0) {
			Object object = objList.get(0);
			int aCol = exportColmunNames.length;
			data = new String[aRow + 1][aCol];

			String[] colmuns = exportColmuns.split(",");
			// ��һ���Ƕ���������Ϣ
			for (int j = 0; j < aCol; j++){
				String col_name = exportColmunNames[j];
				if (col_name.indexOf(";") >=0)
					col_name = col_name.split(";")[0];
				data[0][j] = col_name;
			}

			for (int i = 0; i < aRow; i++) {
				for (int j = 0; j < aCol; j++) {

					// �õ���ȡ�������Եķ�������
					String getter = Util698.getGetter(colmuns[j]);

					object = objList.get(i);
					try {
						// ��̬ ���������ػ�ȡ�������Եĺ���

						// xuky 2017.03.21 ��Ӷ����ֶ����Ե��ж�
						Object val = Util698.getObjectAttr(object, getter);
						if (val == null){
							// xuky ��Ӷ��ڿ�ֵ�Ĵ���
							data[i + 1][j] = "";
						}
						else{
							String type = val.getClass().getTypeName();
							if (type.toLowerCase().indexOf("int")>=0)
								data[i + 1][j] = DataConvert.int2String((int)val);
							else
								data[i + 1][j] = (String) val;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return data;
	}

	public void converFormStringArray(String[][] data) {
		// �����ⲿ��String Array���ݣ��õ������б�
		String[] colmuns = exportColmuns.split(",");

		if (data == null || data.length == 0)
			return;
		else {
			int aRow = data.length;
			int aCol = data[0].length;
			int id = getUseableID();
			for (int i = 0; i < aRow; i++) {
				Object object = newobj_act.getNewObject();

				// ����ID��Ϣ
				Map<String, Object> infoMap = Util698.getFirstFiledsInfo(object);
				Util698.setFieldValueByName(infoMap.get("name").toString(),object,id);

				// ����ʹ��getUseableID����Ϊ��ʱ���ݻ�û����ӵ����ݿ���
				id++;

				for (int j = 0; j < aCol; j++) {
					String setter = Util698.getSetter(colmuns[j]);

					try {
						// ע�� new Class[] {String.class} ���е�String.class��ʾ
						// fun(String.class);
						Util698.setFieldValueByName(colmuns[j], object, data[i][j]);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// meter.
				}
				objList.add(object);
			}
		}
	}

	public void export2Excel() {
		// 123 ����excel
		// ����ǰ�������ݽ���һ�����ã����ղ���������չʾ
		// setTableData(TerminalID);

		String filePath = DebugSwing.directorChoose();
		if (!filePath.equals("")) {
			String fileName = filePath + exportFileName;

			// ��������ת��Ϊ�ַ�������
			String[][] data = getStringArray();

			// ���ַ�������ת��Ϊexcel�ļ�
			String ret = ReadWriteExcel.stringArray2Excel(data, fileName);

			// ֮ǰ���ϵĺ�����������е����ݵ���Ϊexcel�ļ�
			// String ret = ReadWriteUtil.table2Excel(colNames,
			// defaultModel,fileName);

			// ���ݷ��ؽ���������û�����
			if (ret.equals(""))
				DebugSwing.showMsg("���ݵ�����\"" + fileName + "\"�ɹ���");
			else
				DebugSwing.showMsg("���ݵ���ʧ�ܣ�" + ret);
		}

	}

	public void importFromExcel() {
		String fileName = DebugSwing.fileChoose();
		String[][] data = null;
		if (!fileName.equals("")) {
			data = ReadWriteExcel.excel2StringArray(fileName);
			deleteAll();
			converFormStringArray(data);
			addAll();
		}
//		DebugSwing.showMsg("�������ݳɹ���");
	}



	public static void main(String[] args) {

	}



}
