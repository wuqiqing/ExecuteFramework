package com.yiting.concurrent;

import java.util.Collection;
import java.util.List;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * �̳�Mexecutor,��excutr���ṩ�˶��߳̽��й���Ĺ��ܣ�����shutdown shutdownnow submit invokall �ȹ�����
 * 
 * @author yiting
 * 
 */
public interface MExecutorService extends MExcutor {
	/**
	 * shutdown���ڹر��Ѿ��ύ���̣߳�ͬʱ��֯�µ�task���룬�䲻�ȴ��̹߳رա�
	 */
	void shutdown();

	/**
	 * ��ͼ�ر��������е�task����ֹ���еȴ��е�task��ͬʱ�������еȴ�tasks list. �����䲢���ȴ��������е�task����ر�
	 * 
	 * @return �ȴ�tasks list ͬʱ�䲢����֤�̶߳��ܱ��رգ���ͨ��interruptȡ��task����ʹ��һЩtask�޷��ر�
	 */
	List<Runnable> shutdownNow();

	/**
	 * 
	 * @return true��executor�ر�
	 */
	boolean isShutdown();

	/**
	 * 
	 * @return ture �����е�task����ֹ ���Ǹú����������shutdown����shutdownNow ���򷵻�ֵ������Ϊtrue��
	 */
	boolean isTerminated();

	/**
	 * �ú�����һֱ���� ֱ����shutdown���������taskִ����ϣ�����timeout��ʱ�����ߵ�ǰ�߳�thread �Ѿ� interruted��
	 * 
	 * @param timeout
	 *            �ȴ�ʱ��
	 * @param unit
	 *            ʱ�䵥λ �롢���롢...
	 * @return true ����timeoutʱ�䷶Χ��executor��ֹ
	 * @throws InterruptedException
	 *             ��waiting״̬ʱ �߳�interrupted
	 */
	boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException;

	/**
	 * �ύһ�����Ի�ȡ����ֵ��task��ͨ��MFuture��get����
	 * 
	 * @param task
	 * @return
	 * @throws RejectedExecutionException
	 *             if the task cannot be scheduled for execution
	 * @throws NullPointerException
	 *             if the task is null
	 */
	<T> MFuture<T> submit(MCallable<T> task);

	/**
	 * ��submit(MCallable<T> task)����������MCallable���Է��ؽ��
	 * ��Runnable���ܷ��ؽ�������Դ���result�������Ӷ����Ի�ȡ������
	 * 
	 * @param task
	 * @param result
	 * @return
	 */
	<T> MFuture<T> submit(Runnable task, T result);

	/**
	 * �÷����ύtask ���᷵�ؽ����MFuture.get���ص�null
	 * 
	 * @param task
	 * @return
	 */
	MFuture<?> submit(Runnable task);

	/**
	 * ִ������collection��������MFuture�б�list��ͨ��get���Ի�ȡÿ��collection�Ľ��
	 * 
	 * @param tasks
	 * @return
	 * @throws InterruptedException
	 */
	<T> List<MFuture<T>> invokeAll(Collection<? extends MCallable<T>> tasks)
			throws InterruptedException;

	/**
	 * �������һ������ ������ʱ������
	 * 
	 * @param tasks
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	<T> List<MFuture<T>> invokeAll(Collection<? extends MCallable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * ��������һ���Ѿ��ɹ������task��ֵ�����صĽ�����͸���collection�Ĳ���ȷ��
	 * 
	 * @param tasks
	 * @return
	 * @throws InterruptedException
	 */
	<T> T invokeAny(Collection<? extends MCallable<T>> tasks)
			throws InterruptedException,ExecutionException;
	
	/**
	 * �����˳�ʱʱ��
	 * @param tasks
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	<T> T invokeAny(Collection<? extends MCallable<T>> tasks,
            long timeout, TimeUnit unit)throws InterruptedException, ExecutionException, TimeoutException;

}
