package com.yiting.collection;
/**
 * 
 * @wirte by youyou 2014/5/15 in the evening
 * #############��ʾ �в����ĵط�
 * 
 */
/**
 * ˫���С�˫�˶���
 * @param e
 */
public interface Deque<E> extends Queue<E> {

	/**
	 
	 * ��˫�˶��е�ǰ�����Ԫ�ء�
	 * @param elemҪ�����Ԫ��
	 *  @throw �����Ϊ�������������ƶ����ܲ���Ԫ�أ� ���׳�illegalStateException
	 * @throw ��������ض���Ԫ����ֹ�䱻���뵽˫�˶����У����׳�classCastException
	 * @throw �����������Ԫ���ǿ�ֵ�Ҵ�˫�˶��в������Ԫ�أ����׳�NullPointerException
	 * @throw ����������Ԫ�ص�һЩ����ʹ�䲻�ܲ��뵽���˫�˶����У�������illegalArgumentException
	 **/
		
	public void addFirst(E e);	
	
	/**
	 * �ڲ���������������ǰ���£������˫���еĶ�β����һ���ض���Ԫ��
	 * ��ʹ��һ�����������Ƶ�˫���У���ͨ�����õ�ʹ��link �����offerLast����//
	 * @param elemҪ�����Ԫ��
	 *
	 */
	public void addLast(E e);
	
	
	/**
	 * ��˫�˶��еĶ�ͷ�����ض���Ԫ��
	 * @param e Ҫ�����Ԫ��
	* @return ���Ԫ�سɹ����뵽���˫�˶����У��ͷ���true,����false
	 */
	public boolean offerFirst(E e);
	
	
	/**
	 * ��˫�˶��еĶ�β�����ض���Ԫ��
	 * @param e Ҫ�����Ԫ��
	* @return ���Ԫ�سɹ����뵽���˫�˶����У��ͷ���true,����false
	 */
	public boolean offerLast(E e);
	
	
	/**
	 * ��ȡ��ɾ�����˫�˶����еĵ�һ��Ԫ��
	 * public boolean offerLast(E e){
	 * 	addLast(e);
	 *  return true;
	 * }
	 * @return �������˫�˶��е�ͷ
	 * @throw �������Ϊ�գ����׳�noSuchElementException
	 */	
	public E removeFirst();
	
	/**
	 * ��ȡ��ɾ�����˫�˶��е����һ��Ԫ��
	 * ############@return the tail of this deque##########�����ô����ȽϺã�
	 * @return �������˫�˶��е�β
	 * @throw �������Ϊ�գ����׳�noSuchElementException
	 */
	
	public E removeLast();	
	
	/**
	 * ��ȡ��ɾ�����˫�˶��еĵ�һ��Ԫ��
	 * @return �������˫�˶��е�ͷ����������˶���Ϊ�գ�����NULL
	 */
	
	public E pollFirst();
	
	/**
	 * ��ȡ��ɾ�����˫�˶��е����һ��Ԫ��
	 * @return �������˫�˶��е�β����������˶���Ϊ�գ�����NULL
	 */
	public E pollLast();
	
	/**
	 * ��ȡ������ɾ������˫�˶��еĵ�һ��Ԫ�أ�
	 * @return �������˫�˶��е�ͷ
	 * @throw �������Ϊ�գ����׳�noSuchElementException
	 */
	
	public E getFirst();
	
	
	/**
	 * ��ȡ������ɾ������˫�˶��е����һ��Ԫ�أ�
	 * @return �������˫�˶��е�β
	 * @throw �������Ϊ�գ����׳�noSuchElementException
	 */
	
	public E getLast();
	
	
	/**
	 * ��ȡ������ɾ������˫�˶��еĵ�һ��Ԫ�أ�
	 * @return �������˫�˶��е�ͷ
	 * �������Ϊ�գ��򷵻�Null
	 */
	public E peekFirst();
	
	
	/**
	 * ��ȡ������ɾ������˫�˶��е����һ��Ԫ�أ�
	 * @return �������˫�˶��е�β
	 * �������Ϊ�գ��򷵻�Null
	 */
	
	public E peekLast();
	
	/**
	 * ɾ����һ�γ����ڴ���˫�˶����е�ָ��Ԫ��
	 * �����������в��������Ԫ�أ���ô�Ͳ����ı�
	 * @Param oָ���Ķ���Ԫ�أ������˫�˶����д�ɾ��������������
	 * @return ���һ��Ԫ�ر�ɾ����������õĽ�����򷵻�true
	 */
	
	public boolean removeFirstOccurrence(Object o);
	
	
	
	/**
	 * ɾ�����һ�γ����ڴ���˫�˶����е�ָ��Ԫ��
	 * �����������в��������Ԫ�أ���ô�Ͳ����ı�
	 * @Param oָ���Ķ���Ԫ�أ������˫�˶����д�ɾ��������������
	 * @return ���һ��Ԫ�ر�ɾ����������õĽ�����򷵻�true
	 */
	public boolean removeLastOccurrence(Object o);
	
	/**
	 *��ȡ��������ɾ����������е�ͷԪ��
	 *�������Ϊ�գ��򷵻�null
	 */
	
	public E peek();
	
	/**
	 * ��һ��Ԫ��ѹ��ջ�������仰˵���������˫�˶��е�ͷ����
	 * ��������ȼ���addFirst����
	 */
	
	public void push(E e);
	
	
	/**
	 * ��ջ�е���һ��Ԫ�أ������仰˵��ɾ��������������еĵ�һ��Ԫ�أ�
	 * ��������ȼ���removeFirsth����
	 * @return ����������е�ǰ��Ԫ�أ�ջ��Ԫ�أ�
	 */
	public E pop();
	

}
