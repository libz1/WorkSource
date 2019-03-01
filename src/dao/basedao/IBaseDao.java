package dao.basedao;

import java.util.List;

public interface IBaseDao<T> {

	public abstract T create(T t);
	public abstract void create(List<T> listData);

	public abstract T retrieve(int id);
	// ����where������ order by���򣬵õ����󼯺�  ����ֵΪ��������
	public abstract List<T> retrieve(String where, String order);
	public abstract List<T> retrieve(String where, String order, int limit0,int limit1);

	public abstract void update(T t);

	public abstract void delete(int id);
	public abstract void delete(String where);
	public abstract void delete(T t);

	public abstract List retrieveBySQL(String sql);

	public abstract List executeSQL(String sql);
}