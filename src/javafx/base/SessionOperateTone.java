package javafx.base;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.eastsoft.util.Debug;

import util.Util698;

// ��������������ݿ����
public class SessionOperateTone {

	private volatile static SessionOperateTone uniqueInstance;

	public static SessionOperateTone getInstance() {
		if (uniqueInstance == null) {
			synchronized (SessionOperateTone.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new SessionOperateTone();
				}
			}
		}
		return uniqueInstance;
	}

	private SessionFactory sessionFactory = SessionFactoryTone.getInstance().getSessionFactory();

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

	private SessionOperateTone() {
	}

	public synchronized List operateDB(String type, Object data) {
		return operateDB(type, data, "","","",null);
	}

	public synchronized List operateDB(String type, int ID, Class<?> clz) {
		return operateDB(type, ID, "","","",clz);
	}

	public List<Object> operateDB(String string, Object object, String name, String where, String order, Class<?> clz) {
		return operateDB(string, object, name, where, order, clz,0,0);
	}


	public synchronized List operateDB(String type, Object data,String className,String where, String order, Class<?> clz,int limit0, int limit1) {
		List<Object> list = new ArrayList<Object>();
		Session session = null;
		Transaction tx = null;
		try {
			// ��ȡSession
			session = getSession();
			// ��������

			if  (type.equals("create") || type.equals("update"))
				tx = session.beginTransaction();
			// �û��ı���
			if (type.equals("create")){
				session.save(data);
				list.add(data);
			}
			else if (type.equals("update"))
				session.update(data);
			else if (type.equals("retrieve")){
				String sql = "from " + className + " " + where + " " + order;
				// xuky 2018.11.09 ͨ�����������п���
				if (limit0 == 0 && limit1 == 0)
					list = session.createQuery(sql).list();
				else
					list = session.createQuery(sql).setFirstResult(limit0).setMaxResults(limit1).list();
				// xuky 2018.11.08 ����ʹ��createSQLQuery����˵����ʹ��ԭ��SQL������MySQL��limit
				// ������Ч����createSQLQueryҲ�Ǳ�������
//				String sql = "select id from " + className + " " + where + " " + order;
//				list = session.createSQLQuery(sql).list();
			}
			else if (type.equals("retrieveByID")){
				list.add(session.get(clz, (int)data));
			}
			else if (type.equals("retrieveBySQL")){
				String sql = (String)data;
				list = session.createQuery(sql).list();
//				list = session.createSQLQuery(sql).list();
			}
			else if (type.equals("executeSQL")){
				String sql = (String)data;
				int ret = session.createQuery(sql).executeUpdate();
				list.add(ret);
			}

		} catch (Exception e) {
	        Util698.log(SessionOperateTone.class.getName(), "operateDB Exception"+e.getMessage()+" type"+type, Debug.LOG_INFO);

			// ��������Ļع�
			if  (type.equals("create") || type.equals("update"))
				tx.rollback();
			throw new RuntimeException(e);
		} finally {
			try {
				// ����������ύ
				if  (type.equals("create") || type.equals("update"))
					tx.commit();
				// �ر�session
				session.close();
			} catch (Exception e) {
		        Util698.log(SessionOperateTone.class.getName(), "operateDB commit Exception"+e.getMessage()+" type"+type, Debug.LOG_INFO);
			}
		}
		return list;

	}

}
