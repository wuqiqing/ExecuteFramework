package com.yiting.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Future ���첽����Ľ�� ���ṩmethods ���Լ�������Ƿ���ɡ��ȴ���ɡ�ȡ�ؽ���� ���������ʱֻ��ͨ��get��ȡ���
 * 
 * @author yiting
 * 
 * @param <V>
 */

public interface MFuture<V> {

	/**
	 * ��ͼȡ��ִ�����񣬵�����ִ����ϡ������Ѿ�ȡ�������߲��ܱ�ȡ��������ԭ��
	 * ִ�и÷����򶼻᷵��false��������û��ִ��ȴ�����˸������cancel��������ô�÷�������ִ��
	 * 
	 * <p>
	 * After this method returns, subsequent calls to {@link #isDone} will
	 * always return <tt>true</tt>. Subsequent calls to {@link #isCancelled}
	 * will always return <tt>true</tt> if this method returned <tt>true</tt>.
	 * 
	 * @param mayInterruptIfRunning
	 *            true���߳�ִ�и�����ʱ���Ա��ն�interrupt
	 * @return �������Ѿ�ִ����ִ�и÷����᷵��false������������true
	 */
	boolean cancel(boolean mayInterruptIfRunning);

	/**
	 * 
	 * @return true ����task��������ǰ��task��ֹ
	 */
	boolean isCancelled();

	/**
	 * 
	 * @return true ��������ɣ�����terminate��cancel��exception������������ɣ�
	 */
	boolean isDone();

	/**
	 * �÷���һֱ�ȴ�������������ȡ����
	 * 
	 * @return ���ؽ��
	 * @throws InterruptedException
	 *             �ж��쳣
	 * @throws ExecutionException
	 *             ִ�м����쳣
	 * @throws cancellationException
	 *             ȡ���쳣
	 */
	V get() throws InterruptedException, ExecutionException;

	/**
	 * ��get��������������뱾����ֻ�ȴ��̶���ʱ��
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 *             ��ʱ�쳣
	 */
	V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException;
}
