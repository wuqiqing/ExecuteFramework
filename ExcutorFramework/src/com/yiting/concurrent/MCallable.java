package com.yiting.concurrent;

/**
 * �ýӿ��е�����runnable�ӿڣ�����runnable�ӿ��޷����ؽ������ruannal���� ���ؼ�����
 * 
 * @author yiting
 * 
 * @param <V>
 */
public interface MCallable<V> {

	/**
	 * 
	 * @return computed result
	 * @exception ����޷�����һ�����ʱ throw exception
	 */
	V call() throws Exception;
}
