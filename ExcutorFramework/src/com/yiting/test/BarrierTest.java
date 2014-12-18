package com.yiting.test;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

import com.yiting.concurrent.MCyclicBarrier;

public class BarrierTest {
	public static class ComponentThread implements Runnable {
		MCyclicBarrier barrier;
		int ID;
		int[] array;

		public ComponentThread(MCyclicBarrier barrier, int iD, int[] array) {
			this.barrier = barrier;
			ID = iD;
			this.array = array;
		}

		@Override
		public void run() {
			try {
				array[ID] = new Random().nextInt(100);
				System.out.println("Component " + ID + " generates: "
						+ array[ID]);
				// ������ȴ�Barrier��
				System.out.println("Component " + ID + " sleep...");
				barrier.await();
				System.out.println("Component " + ID + " awaked...");
				// �������������еĵ�ǰֵ�ͺ���ֵ
				int result = array[ID] + array[ID + 1];
				System.out.println("Component " + ID + " result: " + result);
			} catch (Exception ex) {
			}
		}

	}
	
	@Test
	 public  void testCyclicBarrier() {  
	        final int[] array = new int[4];  
	        MCyclicBarrier barrier = new MCyclicBarrier(3, new Runnable() {  
	            // �������̶߳�����Barrierʱִ��  
	            public void run() {  
	                System.out.println("testCyclicBarrier run...");  
	                array[2] = array[0] + array[1];  
	            }  
	        });  
	  
	        // �����߳�  
	        new Thread(new ComponentThread(barrier, 0, array)).start();  
	        new Thread(new ComponentThread(barrier, 1, array)).start();  
	        new Thread(new ComponentThread(barrier, 2, array)).start(); 
	    } 
	
	
	
	public static void main(String[] args){
		(new BarrierTest()).testCyclicBarrier();
	}
}
