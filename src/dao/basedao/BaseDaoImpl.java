package dao.basedao;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.eastsoft.util.Debug;

import javafx.base.SessionFactoryTone;
import javafx.base.SessionOperateTone;
import util.Util698;

// xuky 2017.02.23  CURD  Create Update Retrieve Delete  DB operate
// 参考http://blog.csdn.net/zbw18297786698/article/details/52003727
public class BaseDaoImpl<T> implements IBaseDao<T> {

	// 整合的时候，利用Spring注解来实现

	private SessionFactory sessionFactory = SessionFactoryTone.getInstance().getSessionFactory();

	// 创建一个Class的对象来获取泛型的class，实现BaseDao的关键
	private Class<?> clz;

	public Class<?> getClz() {
		if (clz == null) {
			// 获取泛型的Class对象
			clz = ((Class<?>) (((ParameterizedType) (this.getClass().getGenericSuperclass()))
					.getActualTypeArguments()[0]));
		}
		return clz;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession() {
		// 在于Spring整合的时候，用下面的方法
		// return sessionFactory.getCurrentSession();
		return sessionFactory.openSession();
	}

	@Override
	public T create(T t) {
		List<Object> list = SessionOperateTone.getInstance().operateDB("create", t);
		Object obj = list.get(0);
		list = null;
		return (T) obj;
	}

	@Override
	public void update(T t) {
		SessionOperateTone.getInstance().operateDB("update", t);
	}

	@Override
	public void delete(int id) {
		Session session = null;
		Transaction tx = null;
		T t = this.retrieve(id);
		if (t == null)
			return;
		try {
			// 获取Session
			session = getSession();
			// 开启事务
			tx = session.beginTransaction();
			// 删除用户
			session.delete(t);
		} catch (Exception e) {
	        Util698.log(BaseDaoImpl.class.getName(), "delete Exception"+e.getMessage()+" id"+id, Debug.LOG_INFO);

			// 进行事务的回滚
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			try{
				// 进行事务的提交
				tx.commit();
				// 关闭session
				session.close();
			}
			catch(Exception e){
		        Util698.log(BaseDaoImpl.class.getName(), "delete commit Exception"+e.getMessage()+" id"+id, Debug.LOG_INFO);
			}
		}

	}

	@Override
	public void delete(T t) {
		Session session = null;
		Transaction tx = null;
		if (t == null)
			return;
		try {
			// 获取Session
			session = getSession();
			// 开启事务
			tx = session.beginTransaction();
			// 删除用户
			session.delete(t);
		} catch (Exception e) {
			// 进行事务的回滚
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			// 进行事务的提交
			tx.commit();
			// 关闭session
			session.close();
		}

	}

	@Override
	public T retrieve(int id) {
//		List<Object> list = SessionOperateTone.getInstance().operateDB("retrieveByID", id);
		List<Object> list = SessionOperateTone.getInstance().operateDB("retrieveByID", id, getClz());
		Object obj = list.get(0);
		list = null;
		return (T) obj;
	}

	@Override
	public List<T> retrieve(String where, String order) {
		List<Object> list = SessionOperateTone.getInstance().operateDB("retrieve", null, getClz().getName(), where, order,getClz());
		return (List<T>) list;
	}

	@Override
	public List<T> retrieve(String where, String order, int limit0, int limit1) {
		List<Object> list = SessionOperateTone.getInstance().operateDB("retrieve", null, getClz().getName(), where, order,getClz(),limit0,limit1);
		return (List<T>) list;
	}

	@Override
	public void delete(String where) {
		Session session = null;
		Transaction tx = null;
		try {
			// 获取Session
			session = getSession();
			// 开启事务
			tx = session.beginTransaction();
			// 返回查找的用户
			String sql = "delete from " + getClz().getName() + " " + where;
			System.out.println(" delete sql:" + sql);
			session.createQuery(sql).executeUpdate();
		} catch (Exception e) {
			// 进行事务的回滚
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			// 进行事务的提交
			tx.commit();
			// 关闭session
			session.close();
		}

	}

	@Override
	public void create(List<T> listData) {
		Session session = null;
		Transaction tx = null;
		try {
			// 获取Session
			session = getSession();
			// 开启事务
			tx = session.beginTransaction();
			// 数据集合的的保存
			for (T t : listData)
				session.save(t);
		} catch (Exception e) {
			// 进行事务的回滚
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			// 进行事务的提交
			tx.commit();
			// 关闭session
			session.close();
		}
	}

	@Override
	public List retrieveBySQL(String sql) {
		List<Object> list = SessionOperateTone.getInstance().operateDB("retrieveBySQL", sql);
		return list;
	}

	@Override
	public List executeSQL(String sql) {
		List<Object> list = SessionOperateTone.getInstance().operateDB("executeSQL", sql);
		return list;
	}


}
