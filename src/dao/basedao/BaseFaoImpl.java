package dao.basedao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.eastsoft.fio.FileToRead;
import com.eastsoft.fio.FileToWrite;
import com.google.gson.Gson;

// xuky 2017.02.23  CURD  Create Update Retrieve Delete  File operate
// xuky 2017.04.46 基于文件的CURD操作
// 参考http://blog.csdn.net/zbw18297786698/article/details/52003727
public class BaseFaoImpl<T> implements IBaseDao<T> {

	// 对象的集合，用于进行集合操作、集合到json文件的序列号与反序列化操作
	List<T> list;

	// 创建一个Class的对象来获取泛型的class，实现BaseDao的关键
	private Class<?> clz;
	public Class<?> getClz() {
		if (clz == null) {
			Type type =  this.getClass().getGenericSuperclass();
			clz = ((Class<?>) (((ParameterizedType) (type))
					.getActualTypeArguments()[0]));
			// 获取泛型的Class对象  ParameterizedType   getActualTypeArguments()[0]取得第一个
			clz = ((Class<?>) (((ParameterizedType) (this.getClass().getGenericSuperclass()))
					.getActualTypeArguments()[0]));
		}
		return clz;
	}

	private void getList() {
		// 1、根据泛型参数的信息，得到对应json文件信息
		String[] classname = getClz().getName().split("\\.");
		String fileName = "arc\\" + classname[classname.length - 1] + ".json";
		String str = new FileToRead().readLocalFile1(fileName);
		// xuky 2017.05.02数据异常防护
		if (str.equals(""))
			str = "[]";
		// 2、转为对象集合（难点部分） List<T> 类型，例如List<SoftParameter>   实现List<T>序列化的关键代码
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
		// 4、集合转为字符串，保存到文件中，格式为json
		String str = new Gson().toJson(list);
		FileToWrite.writeLocalFile1(fileName, str);
	}

	// 增加数据
	@Override
	public T create(T t) {
		getList();
		// 3.0、增加的对象设置其主键信息，后续更新、删除时需要使用此主键id信息
		((FaoBase) t).setID(getMaxID());
		// 3、集合中增加数据
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
			// 3.0、增加的对象设置其主键信息，后续更新、删除时需要使用此主键id信息
			// 3、集合中增加数据
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
