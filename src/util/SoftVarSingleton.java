package util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.eastsoft.protocol.Frame645;

import dao.basedao.IBaseDao;
import produce.entity.BarCodeAndAddr;
import produce.entity.BarCodeAndAddrDaoImpl;

/**
 * ������Ч�ʱ���.
 * <p>
 *
 * @author xuky
 * @version 2018.04.16
 */
public class SoftVarSingleton {

	// xuky 2019.02.12 ����ͨ�ż����������λ���ͬʱ�������Ӱ�졣���⴮�ţ�
    Lock InfraTest_Lock = new ReentrantLock();
	public Lock getInfraTest_Lock() {
		return InfraTest_Lock;
	}
	// xuky 2019.02.12 RTͨ�ż����������λ���ͬʱ�������Ӱ�죬�ز����ţ�
    Lock RTTest_Lock = new ReentrantLock();
	public Lock getRTTest_Lock() {
		return RTTest_Lock;
	}
	// xuky 2019.02.12 ����̨��ͨ�ż�����ʹ��Ψһ�Ĵ��ڽ���ͨ�ţ�
    Lock PlatformTest_Lock = new ReentrantLock();
	public Lock getPlatformTest_Lock() {
		return PlatformTest_Lock;
	}


	private IBaseDao<BarCodeAndAddr> iBarCodeAndAddrDao = null;
	private Frame645 frame645 = null;

	public Frame645 getFrame645() {
		return frame645;
	}

	public IBaseDao<BarCodeAndAddr> getiBarCodeAndAddrDao() {
		return iBarCodeAndAddrDao;
	}

	// ����ģʽ����̬���� uniqueInstance ���Ψһʵ��
	private volatile static SoftVarSingleton uniqueInstance;

	public static SoftVarSingleton getInstance() {
		if (uniqueInstance == null) {
			synchronized (SoftVarSingleton.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new SoftVarSingleton();
				}
			}
		}
		return uniqueInstance;
	}

	private SoftVarSingleton() {
		iBarCodeAndAddrDao = new BarCodeAndAddrDaoImpl();
		frame645 = new Frame645();
	}



}
