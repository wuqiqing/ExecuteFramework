package com.yiting.concurrent.locks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.yiting.concurrent.locks.MCondition;

import sun.misc.*;

public abstract class MAbstractQueuedSynchronizer extends
		MAbstarctOwnableSynchronizer implements Serializable {

	private static final long serialVersionUID = 4572261883510245093L;

	protected MAbstractQueuedSynchronizer() {
	}

	/**
	 * the queue
	 * 
	 * @author yiting
	 * 
	 */
	static final class Node {
		static final Node SHARED = new Node();
		static final Node EXCLUSIVE = null;

		/*
		 * ״̬�ֶΣ�ֻ��ȡ�����ֵ�� SIGNAL(-1)�� ������ĺ���ǣ���ܿ��ǣ������ģ�ͨ��park�������Ե�ǰ���
		 * ����unpark���ĺ�̣������ͷŻ�ȡ��ʱ��Ϊ�˱��⾺����acquire��������
		 * ���ȱ���������Ҫһ���źţ�Ȼ���ٴγ���ԭ����acquire�����ʧ���˾�������
		 * 
		 * CANCELLED(1)�� ���������ڳ�ʱ���ж��ѱ�ȡ�������Ӳ��뿪����״̬�������ǣ� ����״̬���̴߳Ӳ��ٴ�������
		 * 
		 * CONDITION(-2)�� �����㵱ǰ��һ�����������ϡ�������������sync���еĽ�㣬 ֱ����ת�ƣ�����ʱ������״̬������Ϊ0.
		 * ���ֵ�������ʹ���������ֶε�ʹ��û�й�ϵ�������Ǽ򻯽ṹ��
		 * 
		 * PROPAGATE(-3)�� releaseSharedӦ�ô��ݸ�������㡣������doReleaseShared������
		 * ��������ͷ��㣩��ȷ�����ݼ�������ʹ���������и��档
		 * 
		 * 0�� �������κ�ֵ��
		 * 
		 * ֵ����֯Ϊ���ֵ����Լ�ʹ�á��Ǹ�ֵ��ʾ��㲻��Ҫ�źš��������󲿷ִ��벻��Ҫ ����ض���ֵ��ֻ��Ҫ(���)���š�
		 */

		static final int CANCELLED = 1;
		/** waitStatus value to indicate successor's thread needs unparking */
		static final int SIGNAL = -1;
		/** waitStatus value to indicate thread is waiting on condition */
		static final int CONDITION = -2;
		/**
		 * waitStatus value to indicate the next acquireShared should
		 * unconditionally propagate
		 */
		static final int PROPAGATE = -3;

		volatile int waitStatus;
		volatile Node prev;
		volatile Node next;
		volatile Thread thread;
		Node nextWaiter;

		final boolean isShared() {
			return nextWaiter == SHARED;
		}

		final Node predecessor() throws NullPointerException {
			Node p = prev;
			if (p == null) {
				throw new NullPointerException();
			} else {
				return p;
			}
		}

		Node() { // Used to establish initial head or SHARED marker
		}

		Node(Thread thread, Node mode) { // Used by addWaiter
			this.nextWaiter = mode;
			this.thread = thread;
		}

		Node(Thread thread, int waitStatus) { // Used by Condition
			this.waitStatus = waitStatus;
			this.thread = thread;
		}

	} // end the node

	private transient volatile Node head;
	private transient volatile Node tail;
	private volatile int state;

	protected final int getState() {
		return this.state;
	}

	protected final void setState(int newState) {
		this.state = newState;
	}

	/**
	 * cas set the state
	 * 
	 * @param expect
	 * @param update
	 * @return
	 */
	protected final boolean compareAndSetState(int expect, int update) {
		return unsafe.compareAndSwapInt(this, stateOffest, expect, update);
	}

	/**
	 * ��Ӳ���
	 * 
	 * @param node
	 * @return
	 */
	private Node enq(final Node node) {
		for (;;) {
			Node t = tail;
			if (t == null) {
				if (compareAndSetHead(new Node())) {
					tail = head;
				}
			} else {
				node.prev = t;

				// ����û����ȡ��tail����˼��Ϊʲô��
				/**
				 * ����Ľ��ͣ�AQS�б��������tail�ڵ㣬ֻ�и�tail�ڵ����CAS�����ɹ����������next�ڵ㣬
				 * ������֤�˲�������ȷ�� head-> node-> node ->node tail------------��
				 * ����node���ƶ� tail��ָ��Ҳ������ƶ�
				 */
				if (compareAndSetTail(t, node)) {
					t.next = node;
					return t;
				}
			}
		}

	}

	/**
	 * @param mode
	 *            Node.EXCLUSIVE for exclusive, Node.SHARED for shared
	 * @return the new node
	 */
	private Node addWaiter(Node mode) {
		Node node = new Node(Thread.currentThread(), mode);
		Node pred = tail;
		if (pred != null) {
			node.prev = pred;
			if (compareAndSetTail(pred, node)) {
				pred.next = node;
				return node;
			}
		}
		enq(node);
		return node;
	}

	/**
	 * set head of queue. Called only by acquire methods.
	 * 
	 * @param node
	 */
	private void setHead(Node node) {
		head = node;
		node.thread = null;
		node.prev = null;
	}

	/**
	 * @param node
	 */
	private void unparkSucessor(Node node) {
		int ws = node.waitStatus;
		if (ws < 0) {
			compareAndSetWaitStatus(node, ws, 0);
		}

		Node s = node.next;
		if (s == null || s.waitStatus > 0) {
			s = null;
			// �Ӻ���ǰ���ҵ���һ���ڵ�
			for (Node t = tail; tail != null && t != node; t = t.prev) {
				if (t.waitStatus <= 0) {
					s = t;
				}
			}
		}

		if (s != null) {
			LockSupport.unpark(s.thread);
		}

	}

	/**
	 * ����ͷ����״̬
	 */
	private void doReleaseShared() {
		for (;;) {
			Node h = head;
			if (h != null && h != tail) {
				int ws = h.waitStatus;
				if (ws == Node.SIGNAL) {
					if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) {
						continue;
					}
					unparkSucessor(h); // ����ͷ�������һ���ڵ�
				} else if (ws == 0
						&& !compareAndSetWaitStatus(h, 0, Node.PROPAGATE)) {
					continue;
				}
			}
			if (h == head) { // ��h=headʱ˵��headû�иı䣬��ʱ��������ѭ��
				break;
			}
		}
	}

	private void setHeadAndPropagate(Node node, int propagate) {
		Node h = head; // ���ּ�¼�ķ�ʽ�������ڱȽ�head�Ƿ��иı�
		setHead(node);
		if (propagate > 0 || h == null || h.waitStatus < 0) {
			Node s = node.next;
			if (s == null || s.isShared()) {
				doReleaseShared();
			}
		}
	}

	private void cancelAcquire(Node node) {
		if (node == null) {
			return;
		}
		node.thread = null;
		Node pred = node.prev;
		while (pred.waitStatus > 0) {
			node.prev = pred = pred.prev;
		}
		Node predNext = pred.next;
		node.waitStatus = Node.CANCELLED;
		// If we are the tail, remove ourselves.
		if (node == tail && compareAndSetTail(node, pred)) {
			compareAndSetNext(pred, predNext, null); // ��node�ڵ��е�nextָ��null
		} else {
			int ws;
			if (pred != head
					&& ((ws = pred.waitStatus) == Node.SIGNAL || (ws < 0 && compareAndSetWaitStatus(
							pred, ws, Node.SIGNAL))) && pred.thread != null) {
				Node next = node.next;
				if (next != null && next.waitStatus <= 0) {
					compareAndSetNext(pred, predNext, next);
				}
			} else {
				unparkSucessor(node);
			}

			node.next = node; // help GC
		}
	}

	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
		int ws = pred.waitStatus;
		if (ws == Node.SIGNAL) {
			return true;
		}
		if (ws > 0) {
			do {
				node.prev = pred = pred.prev;
			} while (pred.waitStatus > 0);
			pred.next = node;
		} else {
			/*
			 * waitStatus must be 0 or PROPAGATE. Indicate that we need a
			 * signal, but don't park yet. Caller will need to retry to make
			 * sure it cannot acquire before parking.
			 */
			compareAndSetWaitStatus(node, ws, Node.SIGNAL);
		}
		return false;
	}

	public static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	private final boolean parkAndCheckInterrupt() {
		LockSupport.park(this);
		return Thread.interrupted();
	}

	final boolean acquireQueued(final Node node, int arg) {
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null;// help GC;
					failed = false;
					return interrupted;
				}
				if (shouldParkAfterFailedAcquire(p, node)) {
					parkAndCheckInterrupt();
					interrupted = true;
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	/**
	 * Acquires in exclusive interruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireInterruptibly(int arg) throws InterruptedException {
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return;
				}
				if (shouldParkAfterFailedAcquire(p, node)
						&& parkAndCheckInterrupt())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in exclusive timed mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 * @param nanosTimeout
	 *            max wait time
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireNanos(int arg, long nanosTimeout)
			throws InterruptedException {
		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return true;
				}
				if (nanosTimeout <= 0)
					return false;
				if (shouldParkAfterFailedAcquire(p, node)
						&& nanosTimeout > spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared uninterruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireShared(int arg) {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						if (interrupted)
							selfInterrupt();
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(p, node)
						&& parkAndCheckInterrupt())
					interrupted = true;
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared interruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireSharedInterruptibly(int arg)
			throws InterruptedException {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(p, node)
						&& parkAndCheckInterrupt())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared timed mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 * @param nanosTimeout
	 *            max wait time
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
			throws InterruptedException {

		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						failed = false;
						return true;
					}
				}
				if (nanosTimeout <= 0)
					return false;
				if (shouldParkAfterFailedAcquire(p, node)
						&& nanosTimeout > spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	protected boolean tryAcquire(int arg) {
		throw new UnsupportedOperationException();
	}

	protected boolean tryRelease(int arg) {
		throw new UnsupportedOperationException();
	}

	protected int tryAcquireShared(int arg) {
		throw new UnsupportedOperationException();
	}

	protected boolean tryReleaseShared(int arg) {
		throw new UnsupportedOperationException();
	}

	protected boolean isHeldExclusively() {
		throw new UnsupportedOperationException();
	}

	public final void acquire(int arg) {
		if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
			selfInterrupt();
		}
	}

	public final void acquireInterruptibly(int arg) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		if (!tryAcquire(arg)) {
			doAcquireInterruptibly(arg);
		}
	}

	public final boolean tryAcquireNanos(int arg, long nanosTimeout)
			throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
	}

	public final boolean release(int arg) {
		if (tryRelease(arg)) {
			Node h = head;
			if (h != null && h.waitStatus != 0) {
				unparkSucessor(h);
			}
			return true;
		}
		return false;
	}

	public final void acquireShared(int arg) {
		if (tryAcquireShared(arg) < 0)
			doAcquireShared(arg);
	}

	public final void acquireSharedInterruptibly(int arg)
			throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		if (tryAcquireShared(arg) < 0)
			doAcquireSharedInterruptibly(arg);
	}

	public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
			throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquireShared(arg) >= 0
				|| doAcquireSharedNanos(arg, nanosTimeout);
	}

	public final boolean releaseShared(int arg) {
		if (tryReleaseShared(arg)) {
			doReleaseShared();
			return true;
		}
		return false;
	}

	public final boolean hasQueuedThreads() {
		return head != tail;
	}

	public final boolean hasContended() {
		return head != null;
	}

	public final Thread getFirstQueuedThread() {
		// handle only fast path, else relay
		return (head == tail) ? null : fullGetFirstQueuedThread();
	}

	private Thread fullGetFirstQueuedThread() {
		Node h, s;
		Thread st;
		if (((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null)
				|| ((h = head) != null && (s = h.next) != null
						&& s.prev == head && (st = s.thread) != null)) {
			return st;
		}

		Node t = tail;
		Thread firstThread = null;
		while (t != null && t != head) {
			Thread tt = t.thread;
			if (tt != null) {
				firstThread = tt;
			}
			t = t.prev;
		}

		return firstThread;

	}

	public final boolean isQueued(Thread thread) {
		if (thread == null)
			throw new NullPointerException();
		for (Node p = tail; p != null; p = p.prev)
			if (p.thread == thread)
				return true;
		return false;
	}

	final boolean apparentlyFirstQueuedIsExclusive() {
		Node h, s;
		return (h = head) != null && (s = h.next) != null && !s.isShared()
				&& s.thread != null;
	}

	public final boolean hasQueuedPredecessors() {
		Node t = tail;
		Node h = head;
		Node s;
		return h != t
				&& ((s = h.next) == null || s.thread != Thread.currentThread());
	}

	/**
	 * estimate the length that's wait to acquire CPU
	 * 
	 * @return
	 */
	public final int getQueueLength() {
		int n = 0;
		for (Node p = tail; p != null; p = p.prev) {
			if (p.thread != null) {
				++n;
			}
		}
		return n;
	}

	/**
	 * estimate the queued threads ,because the thread is dynamically.
	 * 
	 * @return
	 */
	public final Collection<Thread> getQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			Thread t = p.thread;
			if (t != null) {
				list.add(t);
			}
		}
		return list;
	}

	public final Collection<Thread> getExclusiveThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (!p.isShared()) {
				Thread t = p.thread;
				if (t != null) {
					list.add(t);
				}
			}
		}
		return list;
	}

	final boolean isOnSyncQueue(Node node) {
		if (node.waitStatus == Node.CONDITION || node.prev == null) {
			return false;
		}
		if (node.next != null) {// If has successor, it must be on queue
			return true;
		}
		return findNodeFromTail(node);
	}

	private boolean findNodeFromTail(Node node) {
		Node t = tail;
		for (;;) {
			if (t == node) {
				return true;
			}
			if (t == null) {
				return false;
			}
			t = t.prev;
		}
	}

	/**
	 * Transfers a node from a condition queue onto sync queue. Returns true if
	 * successful.
	 * 
	 * @param node
	 *            the node
	 * @return true if successfully transferred (else the node was cancelled
	 *         before signal).
	 */
	final boolean transferForSignal(Node node) {
		if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			return false;
		}
		Node p = enq(node);
		int ws = p.waitStatus;
		if (ws > 0 || compareAndSetWaitStatus(p, ws, Node.SIGNAL)) {
			LockSupport.unpark(node.thread);
		}
		return true;
	}

	/**
	 * Transfers node, if necessary, to sync queue after a cancelled wait.
	 * Returns true if thread was cancelled before being signalled.
	 * 
	 * @param current
	 *            the waiting thread
	 * @param node
	 *            its node
	 * @return true if cancelled before the node was signalled
	 */
	final boolean transferAfterCancelledWait(Node node) {
		if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			enq(node);
			return true;
		}
		/*
		 * If we lost out to a signal(), then we can't proceed until it finishes
		 * its enq(). Cancelling during an incomplete transfer is both rare and
		 * transient, so just spin.
		 */
		while (!isOnSyncQueue(node))
			Thread.yield();
		return false;
	}

	final int fullyRelease(Node node) {
		boolean failed = true;
		try {
			int saveState = getState();
			if (release(saveState)) {
				failed = false;
				return saveState;
			} else {
				throw new IllegalMonitorStateException();
			}
		} finally {
			if (failed) {
				node.waitStatus = Node.CANCELLED;
			}
		}
	}

	public final Collection<Thread> getExclusiveQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (!p.isShared()) {
				Thread t = p.thread;
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}

	public final Collection<Thread> getSharedQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (p.isShared()) {
				Thread t = p.thread;
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}

	public String toString() {
		int s = getState();
		String q = hasQueuedThreads() ? "non" : "";
		return super.toString() + "[State = " + s + ", " + q + "empty queue]";
	}

	public final boolean owns(ConditionObject condition) {
		if (condition == null)
			throw new NullPointerException();
		return condition.isOwnedBy(this);
	}

	public final boolean hasWaiters(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.hasWaiters();
	}

	public final int getWaitQueueLength(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitQueueLength();
	}

	public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitingThreads();
	}

	public class ConditionObject implements MCondition, Serializable {
		private static final long serialVersionUID = -3088849475955141529L;
		/** first node of condition queue */
		private transient Node firstWaiter;
		/** last node of condition queue */
		private transient Node lastWaiter;

		public ConditionObject() {
		}

		private Node addConditionWaiter() {
			Node t = lastWaiter;
			if (t != null && t.waitStatus != Node.CONDITION) {
				unlinkCancelledWaiters();
				t = lastWaiter;
			}
			Node node = new Node(Thread.currentThread(), Node.CONDITION);
			if (t == null) {
				firstWaiter = node;
			} else {
				t.nextWaiter = node;
			}
			return node;
		}

		private void doSignal(Node first) {
			do {
				if ((firstWaiter = first.nextWaiter) == null) {
					lastWaiter = null;
				}
				first.nextWaiter = null;
			} while (!transferForSignal(first) && (first = firstWaiter) != null);
		}

		private void doSignalAll(Node first) {
			lastWaiter = firstWaiter = null;
			do {
				Node next = first.nextWaiter;
				first.nextWaiter = null;
				transferForSignal(first);
				first = next;
			} while (first != null);
		}

		/**
		 * Unlinks cancelled waiter nodes from condition queue. Called only
		 * while holding lock. This is called when cancellation occurred during
		 * condition wait, and upon insertion of a new waiter when lastWaiter is
		 * seen to have been cancelled. This method is needed to avoid garbage
		 * retention in the absence of signals. So even though it may require a
		 * full traversal, it comes into play only when timeouts or
		 * cancellations occur in the absence of signals. It traverses all nodes
		 * rather than stopping at a particular target to unlink all pointers to
		 * garbage nodes without requiring many re-traversals during
		 * cancellation storms.
		 */
		private void unlinkCancelledWaiters() {
			Node t = firstWaiter;
			Node trail = null;
			while (t != null) {
				Node next = t.nextWaiter;
				if (t.waitStatus != Node.CONDITION) { // ɾ��t�ڵ�
					t.nextWaiter = null;
					if (trail == null) {
						firstWaiter = next;
					} else {
						trail.nextWaiter = next;
					}
					if (next == null) {
						lastWaiter = trail;
					}
				} else {
					trail = t;
				}
				t = next;
			}
		}

		@Override
		public final void signal() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			Node first = firstWaiter;
			if (first != null) {
				doSignal(first);
			}
		}

		@Override
		public void signalAll() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			Node first = firstWaiter;
			if (first != null) {
				doSignalAll(first);
			}

		}

		@Override
		public void await() throws InterruptedException {
			// TODO Auto-generated method stub

		}

		@Override
		public void awaitUninterruptly() {
			Node node = addConditionWaiter();
			int saveState = fullyRelease(node);
			boolean interrupted = false;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if (Thread.interrupted()) {
					interrupted = true;
				}
			}
			if (acquireQueued(node, saveState) || interrupted) {
				selfInterrupt();
			}
		}

		private static final int REINTERRUPT = 1;
		private static final int THROW_IE = -1;

		private int checkInterruptWhileWaiting(Node node) {
			return Thread.interrupted() ? (transferAfterCancelledWait(node) ? REINTERRUPT
					: THROW_IE)
					: 0;
		}

		private void reportInterruptAfterWait(int interruptMode)
				throws InterruptedException {
			if (interruptMode == THROW_IE) {
				throw new InterruptedException();
			} else if (interruptMode == REINTERRUPT) {
				selfInterrupt();
			}
		}

		@Override
		public long awaitNanos(long nanosTimeout) throws InterruptedException {
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			long lastTime = System.nanoTime();
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (nanosTimeout <= 0L) {
					transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkNanos(this, nanosTimeout);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;

				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return nanosTimeout - (System.nanoTime() - lastTime);
		}

		@Override
		public boolean await(long time, TimeUnit unit)
				throws InterruptedException {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
					break;
				}
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
				interruptMode = REINTERRUPT;
			}
			if (node.nextWaiter != null) {
				unlinkCancelledWaiters();
			}
			if (interruptMode != 0) {
				reportInterruptAfterWait(interruptMode);
			}
			return false;
		}

		@Override
		public boolean awaitUntil(Date deadline) throws InterruptedException {
			if (deadline == null)
				throw new NullPointerException();
			long abstime = deadline.getTime();
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			boolean timedout = false;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (System.currentTimeMillis() > abstime) {
					timedout = transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkUntil(this, abstime);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return !timedout;
		}

		final boolean isOwnedBy(MAbstractQueuedSynchronizer sync) {
			return sync == MAbstractQueuedSynchronizer.this;
		}

		protected final boolean hasWaiters() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION) {
					return true;
				}
			}
			return false;
		}

		protected final int getWaitQueueLength() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			int n = 0;
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION)
					++n;
			}
			return n;
		}

		protected final Collection<Thread> getWaitingThreads() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			ArrayList<Thread> list = new ArrayList<Thread>();
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION) {
					Thread t = w.thread;
					if (t != null)
						list.add(t);
				}
			}
			return list;
		}

	}

	static final long spinForTimeoutThreshold = 1000L;

	private static final Unsafe unsafe = Unsafe.getUnsafe();
	private static final long stateOffest;
	private static final long headOffset;
	private static final long tailOffset;
	private static final long waitStatusOffset;
	private static final long nextOffset;

	static {
		try {
			stateOffest = unsafe
					.objectFieldOffset(MAbstractQueuedSynchronizer.class
							.getDeclaredField("state"));
			headOffset = unsafe
					.objectFieldOffset(MAbstractQueuedSynchronizer.class
							.getDeclaredField("head"));
			tailOffset = unsafe
					.objectFieldOffset(MAbstractQueuedSynchronizer.class
							.getDeclaredField("tail"));
			waitStatusOffset = unsafe.objectFieldOffset(Node.class
					.getDeclaredField("waitStatus"));
			nextOffset = unsafe.objectFieldOffset(Node.class
					.getDeclaredField("next"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/**
	 * cas head field.used only by enq
	 * 
	 * @param update
	 * @return
	 */
	private final boolean compareAndSetHead(Node update) {
		return unsafe.compareAndSwapObject(this, headOffset, null, update);
	}

	/**
	 * cas tail field .Used only by enq
	 * 
	 * @param expect
	 * @param update
	 * @return
	 */
	private final boolean compareAndSetTail(Node expect, Node update) {
		return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
	}

	/**
	 * cas waitstatus field of a node
	 * 
	 * @param node
	 * @param expect
	 * @param update
	 * @return
	 */
	private static final boolean compareAndSetWaitStatus(Node node, int expect,
			int update) {
		return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
	}

	/**
	 * cas next filed of a node
	 * 
	 * @param node
	 * @param expect
	 * @param update
	 * @return
	 */
	private static final boolean compareAndSetNext(Node node, Node expect,
			Node update) {
		return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
	}

}
