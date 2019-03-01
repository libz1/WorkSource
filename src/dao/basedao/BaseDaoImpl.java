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
// �ο�http://blog.csdn.net/zbw18297786698/article/details/52003727
public class BaseDaoImpl<T> implements IBaseDao<T> {

	// ���ϵ�ʱ������Springע����ʵ��

	private SessionFactory sessionFactory = SessionFactoryTone.getInstance().getSessionFactory();

	// ����һ��Class�Ķ�������ȡ���͵�class��ʵ��BaseDao�Ĺؼ�
	private Class<?> clz;

	public Class<?> getClz() {
		if (clz == null) {
			// ��ȡ���͵�Class����
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
		// ����Spring���ϵ�ʱ��������ķ���
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
			// ��ȡSession
			session = getSession();
			// ��������
			tx = session.beginTransaction();
			// ɾ���û�
			session.delete(t);
		} catch (Exception e) {
	        Util698.log(BaseDaoImpl.class.getName(), "delete Exception"+e.getMessage()+" id"+id, Debug.LOG_INFO);

			// ��������Ļع�
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			try{
				// ����������ύ
				tx.commit();
				// �ر�session
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
			// ��ȡSession
			session = getSession();
			// ��������
			tx = session.beginTransaction();
			// ɾ���û�
			session.delete(t);
		} catch (Exception e) {
			// ��������Ļع�
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			// ����������ύ
			tx.commit();
			// �ر�session
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
			// ��ȡSession
			session = getSession();
			// ��������
			tx = session.beginTransaction();
			// ���ز��ҵ��û�
			String sql = "delete from " + getClz().getName() + " " + where;
			System.out.println(" delete sql:" + sql);
			session.createQuery(sql).executeUpdate();
		} catch (Exception e) {
			// ��������Ļع�
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			// ����������ύ
			tx.commit();
			// �ر�session
			session.close();
		}

	}

	@Override
	public void create(List<T> listData) {
		Session session = null;
		Transaction tx = null;
		try {
			// ��ȡSession
			session = getSession();
			// ��������
			tx = session.beginTransaction();
			// ���ݼ��ϵĵı���
			for (T t : listData)
				session.save(t);
		} catch (Exception e) {
			// ��������Ļع�
			tx.rollback();
			throw new RuntimeException(e);
		} finally {
			// ����������ύ
			tx.commit();
			// �ر�session
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
