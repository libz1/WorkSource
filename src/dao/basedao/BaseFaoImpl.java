package dao.basedao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.eastsoft.fio.FileToRead;
import com.eastsoft.fio.FileToWrite;
import com.google.gson.Gson;

// xuky 2017.02.23  CURD  Create Update Retrieve Delete  File operate
// xuky 2017.04.46 �����ļ���CURD����
// �ο�http://blog.csdn.net/zbw18297786698/article/details/52003727
public class BaseFaoImpl<T> implements IBaseDao<T> {

	// ����ļ��ϣ����ڽ��м��ϲ��������ϵ�json�ļ������к��뷴���л�����
	List<T> list;

	// ����һ��Class�Ķ�������ȡ���͵�class��ʵ��BaseDao�Ĺؼ�
	private Class<?> clz;
	public Class<?> getClz() {
		if (clz == null) {
			Type type =  this.getClass().getGenericSuperclass();
			clz = ((Class<?>) (((ParameterizedType) (type))
					.getActualTypeArguments()[0]));
			// ��ȡ���͵�Class����  ParameterizedType   getActualTypeArguments()[0]ȡ�õ�һ��
			clz = ((Class<?>) (((ParameterizedType) (this.getClass().getGenericSuperclass()))
					.getActualTypeArguments()[0]));
		}
		return clz;
	}

	private void getList() {
		// 1�����ݷ��Ͳ�������Ϣ���õ���Ӧjson�ļ���Ϣ
		String[] classname = getClz().getName().split("\\.");
		String fileName = "arc\\" + classname[classname.length - 1] + ".json";
		String str = new FileToRead().readLocalFile1(fileName);
		// xuky 2017.05.02�����쳣����
		if (str.equals(""))
			str = "[]";
		// 2��תΪ���󼯺ϣ��ѵ㲿�֣� List<T> ���ͣ�����List<SoftParameter>   ʵ��List<T>���л��Ĺؼ�����
		Type listType = new ParameterizedTypeImpl(List.class, new Class[] { clz });
		list = new Gson().fromJson(str, listType);
	}

	private int getMaxID() {
		int maxID = 0;
		for (T data : list) {
			int id = ((FaoBase) data).getID();
			if (id >= maxID)
				maxID = id+1;
		}
		return maxID;
	}

	private void saveList() {
		String[] classname = getClz().getName().split("\\.");
		String fileName = "arc\\" + classname[classname.length - 1] + ".json";
		// 4������תΪ�ַ��������浽�ļ��У���ʽΪjson
		String str = new Gson().toJson(list);
		FileToWrite.writeLocalFile1(fileName, str);
	}

	// ��������
	@Override
	public T create(T t) {
		getList();
		// 3.0�����ӵĶ���������������Ϣ���������¡�ɾ��ʱ��Ҫʹ�ô�����id��Ϣ
		((FaoBase) t).setID(getMaxID());
		// 3����������������
		list.add((T) t);
		saveList();
		return t;
	}

	@Override
	public void update(T t) {
		getList();
		int id = ((FaoBase) t).getID();
		int index = 0;
		for (T data : list) {
			if (((FaoBase) data).getID() == id) {
				list.set(index, (T) t);
				break;
			}
			index++;
		}
		saveList();
	}

	@Override
	public void delete(int id) {
		getList();
		int index = 0;
		for (T data : list) {
			if (((FaoBase) data).getID() == id) {
				list.remove(index);
				break;
			}
			index++;
		}
		saveList();
	}

	@Override
	public void delete(T t) {
		int id = ((FaoBase) t).getID();
		delete(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T retrieve(int id) {
		getList();
		for (T data : list) {
			if (((FaoBase) data).getID() == id) {
				return data;
			}
		}
		return null;
	}

	@Override
	public List<T> retrieve(String where, String order) {
		getList();
		return list;
	}

	@Override
	public void delete(String where) {
	}

	@Override
	public void create(List<T> listData) {
		getList();
		int maxID = getMaxID();
		for (T t : listData) {
			// 3.0�����ӵĶ���������������Ϣ���������¡�ɾ��ʱ��Ҫʹ�ô�����id��Ϣ
			// 3����������������
			((FaoBase) t).setID(getMaxID());
			list.add((T) t);
			maxID ++;
		}
		saveList();
	}

	@Override
	public List retrieveBySQL(String sql) {
		return null;
	}

	@Override
	public List executeSQL(String sql) {
		return null;
	}

	@Override
	public List<T> retrieve(String where, String order, int limit0, int limit1) {
		return null;
	}

}
