package com.yiting.concurrent;

/**
 * ��дrunnable�ӿ� ���ṩһ��ʵ��future���ܣ��ܷ��ؽ����runnable�ӿڣ�
 * @author yiting
 *
 * @param <V>
 */

public interface MRunnableFuture<V> extends Runnable , MFuture<V> {
	
	public void run();
}
