
/**
 * @ # this class Iterable write in 2014/5/13
 */


package com.yiting.collection;

import javax.tools.JavaCompiler;

/**
 * iterable�����ݴ���������������ʵ�ָýӿڵ���ӵ�б����Ĺ��ܣ��Ӷ�ʹ��һ������ӵ�� foreach�Ĺ���
 * for-eachѭ���������κ�ʵ����Iterable�ӿڵĶ���һ������
 * @author yiting
 *
 * @param <T> ����������Ԫ�ص�����
 */
public interface MIterable<T> extends java.lang.Iterable<T> {
	
	/**
	 * ͨ���÷�����ȡһ������Iterator�ı����Ӷ���ͨ���ñ����ӶԼ��Ͻ��б���
	 * @return  iteraor����
	 */
	MIterator<T> iterator();
}
