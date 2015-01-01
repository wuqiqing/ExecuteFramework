package com.yiting.collection;
import java.util.*;
/**
 * 
 * @author youyou; write in 2014/5/28
 * ������ṩ��һЩ��Ҫ��ʵ��
 * ͨ�����ӵķ���Ҳ�ᱻ����
 */
public abstract class MAbstractQueue<E> extends MAbstractCollection<E> implements MQueue<E>{

	/**
	 * ʹ�����๹�캯��
	 */
	protected MAbstractQueue(){	
	}
	
	
	/**
	 * �ڲ�Υ�������������Ƶ�ǰ���£�����ָ����Ԫ��
	 * �������ɹ��ͷ���true,����ʧ���׳��쳣
	 * @param e �����Ԫ��
	 */
	public boolean add(E e){
		if(offer(e))
			return true;
		else
			throw new IllegalStateException("Queue full");
	}
	
	
	/**
	 * ��ȡ��ɾ�������е�ͷ
	 * @return ���ض��е�ͷ
	 * @throws �������Ϊ�գ����׳��쳣
	 */
	@Override
	public E remove() {
		E x=poll();
		if(x!=null){
			return x;
		}else {
			throw new NoSuchElementException();
		}
	}


	/**
	 * ��ȡ��������ɾ�������е�ͷԪ��
	 * @return ���ض��е�ͷԪ��
	 * @throws �������Ϊ�����׳��쳣
	 */
	@Override
	public E element() {
		E x = peek();
		if(x != null)
			return x;
		else 
			throw new NoSuchElementException();	
	}
	
	/**
	 * �Ӵ˶�����ɾ�����е�Ԫ��
	 * �������������֮�󣬶��л�Ϊ��
	 */
	public void clear(){
		while(poll() != null)
			;
	}
	
	/**
	 * ��ָ�����ϵ�����Ԫ�����ӵ��˶���
	 * ���⣬�ò�������Ϊ��δ����Ĳ���ʱ�����ָ���ļ��ϱ��޸ģ�ͬʱ����Ҳ�ڽ���
	 * ���ʵ�ֱ���ָ���ļ��ϣ��������������ص�ÿһ��Ԫ�����ӵ��˶��С�
	 */
	public boolean addAll(MCollection <? extends E> c){
		if(c == null)
			throw new NullPointerException();
		if(c == this)
			throw new IllegalArgumentException();
		boolean modified = false;
		for(E e : c)
			if(add(e))
				modified = true;
		return modified;
			
	}
	
	
}