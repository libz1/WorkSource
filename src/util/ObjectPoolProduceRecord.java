package util;
import java.util.Enumeration;
import java.util.Vector;

import produce.entity.ProduceRecord;

// ����� xuky 2018.03.22
// �ο�http://blog.csdn.net/shimiso/article/details/9814917
// ��������Bug������Ϊ����ĵ�������
public class ObjectPoolProduceRecord {
	private volatile static ObjectPoolProduceRecord uniqueInstance;

	Boolean PRINT_MSG = false;
	private int numObjects = 6; // ����صĴ�С
	private int maxObjects = 50; // ��������Ĵ�С
	private Vector<PooledObject> OBJECTS = null; // ��Ŷ�����ж��������( PooledObject����)

	public static ObjectPoolProduceRecord getInstance() {
		if (uniqueInstance == null) {
			synchronized (ObjectPoolProduceRecord.class) {
				if (uniqueInstance == null) {
					// ˫�ؼ�����
					uniqueInstance = new ObjectPoolProduceRecord();
				}
			}
		}
		return uniqueInstance;
	}

	/**
	 * �ڲ�ʹ�õ����ڱ��������ж�����ࡣ ��������������Ա��һ���Ƕ�����һ����ָʾ�˶����Ƿ�����ʹ�õı�־ ��
	 */
	class PooledObject {
		Object objection = null;// ����
		boolean busy = false; // �˶����Ƿ�����ʹ�õı�־��Ĭ��û������ʹ��
		// ���캯��������һ�� Object ����һ�� PooledObject ����

		public PooledObject(Object objection) {
			this.objection = objection;
		}

		// ���ش˶����еĶ���
		public Object getObject() {
			return objection;
		}

		// ���ô˶���ģ�����
		public void setObject(Object objection) {
			this.objection = objection;
		}

		// ��ö�������Ƿ�æ
		public boolean isBusy() {
			return busy;
		}

		// ���ö���Ķ�������æ
		public void setBusy(boolean busy) {
			this.busy = busy;
		}
	}

	private ObjectPoolProduceRecord() {
		createPool();
	}

	/*** ����һ������� ***/
	private synchronized void createPool() {
		// ȷ�������û�д�������������ˣ������������� objects ����Ϊ��
		if (OBJECTS != null) {
			return; // ��������������򷵻�
		}

		// ���������������� , ��ʼʱ�� 0 ��Ԫ��
		OBJECTS = new Vector<PooledObject>();

		// ���� numObjects �����õ�ֵ��ѭ������ָ����Ŀ�Ķ���
		for (int x = 0; x < numObjects; x++) {
			// xuky 2018.03.22 ԭʼ�����е�if�ж�������
			// if ((OBJECTS.size() == 0) && (OBJECTS.size() < maxObjects)) {
			Object obj = new ProduceRecord();
			OBJECTS.addElement(new PooledObject(obj));
			// }
		}
	}

	public synchronized Object getObject() {
		// ȷ������ؼ�������
		if (OBJECTS == null) {
			return null; // ����ػ�û�������򷵻� null
		}

		Object conn = getFreeObject(); // ���һ�����õĶ���

		// ���Ŀǰû�п���ʹ�õĶ��󣬼����еĶ�����ʹ����
		while (conn == null) {
			wait(250);
			conn = getFreeObject(); // �������ԣ�ֱ����ÿ��õĶ������
			// getFreeObject() ���ص�Ϊ null�����������һ�������Ҳ���ɻ�ÿ��ö���
		}

		return conn;// ���ػ�õĿ��õĶ���
	}

	/**
	 * �������Ӷ���ض��� objects �з���һ�����õĵĶ������ ��ǰû�п��õĶ����򴴽��������󣬲����������С�
	 * ������������еĶ�����ʹ���У��򷵻� null
	 */
	private Object getFreeObject() {

		// �Ӷ�����л��һ�����õĶ���
		Object obj = findFreeObject();

		if (obj == null) {

			// createObjects(incrementalObjects); // ���Ŀǰ�������û�п��õĶ��󣬴���һЩ����
			// xuky ����һЩ���󣬷��ڶ������
			addObjToPool();

			// ���´ӳ��в����Ƿ��п��ö���
			obj = findFreeObject();

			// �������������Ի�ò������õĶ����򷵻� null
			if (obj == null) {
				return null;
			}
		}

		return obj;
	}

	private void addObjToPool() {
		if (OBJECTS == null)
			createPool();

		if (PRINT_MSG)
			System.out.println("addObjToPool");

		if (OBJECTS.size() < maxObjects) {
			Object obj = new ProduceRecord();
			OBJECTS.addElement(new PooledObject(obj));
		}

	}

	/**
	 * ���Ҷ���������еĶ��󣬲���һ�����õĶ��� ���û�п��õĶ��󣬷��� null
	 */
	private Object findFreeObject() {

		Object obj = null;
		PooledObject pObj = null;

		// ��ö�������������еĶ���
		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		// �������еĶ��󣬿��Ƿ��п��õĶ���
		int i = 1;
		while (enumerate.hasMoreElements()) {
			pObj = (PooledObject) enumerate.nextElement();

			if (PRINT_MSG)
				System.out.println("findFreeObject ����(" + i + ") " + pObj);
			i++;

			// ����˶���æ���������Ķ��󲢰�����Ϊæ
			if (!pObj.isBusy()) {
				if (PRINT_MSG)
					System.out.println("PooledObject is free" + pObj);
				obj = pObj.getObject();
				pObj.setBusy(true);
				// xuky 2018.03.22 ����ҵ��˾��˳����ҵ�ѭ��
				break;
			}
		}

		return obj;// �����ҵ����Ŀ��ö���
	}

	/**
	 * �˺�������һ�����󵽶�����У����Ѵ˶�����Ϊ���С� ����ʹ�ö���ػ�õĶ����Ӧ�ڲ�ʹ�ô˶���ʱ��������
	 */

	public void returnObject(Object obj) {

		// ȷ������ش��ڣ��������û�д����������ڣ���ֱ�ӷ���
		if (OBJECTS == null) {
			return;
		}

		PooledObject pObj = null;

		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		// ����������е����ж����ҵ����Ҫ���صĶ������
		while (enumerate.hasMoreElements()) {
			pObj = (PooledObject) enumerate.nextElement();

			// ���ҵ�������е�Ҫ���صĶ������
			if (obj == pObj.getObject()) {
				if (PRINT_MSG)
					System.out.println("�ͷŶ��� "+pObj);

				// �ҵ��� , ���ô˶���Ϊ����״̬
				pObj.setBusy(false);
				break;
			}
		}
	}

	public void setNewObject(Object obj, Object newObj) {

		// ȷ������ش��ڣ��������û�д����������ڣ���ֱ�ӷ���
		if (OBJECTS == null) {
			return;
		}

		PooledObject pObj = null;

		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		// ����������е����ж����ҵ����Ҫ���صĶ������
		while (enumerate.hasMoreElements()) {
			pObj = (PooledObject) enumerate.nextElement();

			// ���ҵ�������е�Ҫ���صĶ������
			if (obj == pObj.getObject()) {

				// �ҵ��� , ���ô˶���Ϊ����״̬
				pObj.setObject(newObj);
				break;
			}
		}
	}

	/**
	 * �رն���������еĶ��󣬲���ն���ء�
	 */
	public synchronized void closeObjectPool() {

		// ȷ������ش��ڣ���������ڣ�����
		if (OBJECTS == null) {
			return;
		}

		PooledObject pObj = null;

		Enumeration<PooledObject> enumerate = OBJECTS.elements();

		int i = 1;
		while (enumerate.hasMoreElements()) {

			pObj = (PooledObject) enumerate.nextElement();

			if (PRINT_MSG)
				System.out.println("removeElement(" + i + ")" + pObj);

			i++;

			// ���æ���� 5 ��
			if (pObj.isBusy()) {
				// wait(5000); // �� 5 ��
				wait(1000); // xuky �� 1 ��
			}

			// �Ӷ����������ɾ����

			OBJECTS.removeElement(pObj);

			// xuky 2018.03.22 �����������Ĵ��룬�������ʱ����ʲ���һЩ�ڵ�
			// һ��ɾ���󣬽��ᵼ��˳��(����)��Ϣ����
			enumerate = OBJECTS.elements();
		}

		// �ö����Ϊ��
		OBJECTS = null;
	}

	/**
	 * ʹ����ȴ������ĺ�����
	 */
	private void wait(int mSeconds) {
		try {

			if (PRINT_MSG)
				System.out.println("sleep " + mSeconds);

			Thread.sleep(mSeconds);
		} catch (InterruptedException e) {
		}
	}
}
