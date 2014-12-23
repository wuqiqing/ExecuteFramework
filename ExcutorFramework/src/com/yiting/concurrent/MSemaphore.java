package com.yiting.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.yiting.concurrent.locks.MAbstractQueuedSynchronizer;
/**
 * �ź��������Ƶ�ǰ�����̵߳ĸ���
 * @author yiting
 *
 */

public class MSemaphore implements Serializable {
	private static final long serialVersionUID = -3287838633152854055L;
	private final Sync sync;

	abstract static class Sync extends MAbstractQueuedSynchronizer {
		private static final long serialVersionUID = 5913797436234496349L;

		Sync(int permits) {
			setState(permits);
		}

		final int getPermits() {
			return getState();
		}

		final int nonfairTryAcquireShared(int acquires) {
			for (;;) {
				int available = getState();
				int remaining = available - acquires;
				if (remaining < 0 || compareAndSetState(available, remaining)) {
					return remaining;
				}
			}
		}

		protected final boolean tryReleaseShared(int release) {
			for (;;) {
				int current = getState();
				int next = current + release;
				if (next < current) {
					throw new Error("Maximum permit count exceeded");
				}
				if (compareAndSetState(current, next)) {
					return true;
				}
			}
		}

		final void reducePermits(int reductions) {
			for (;;) {
				int current = getState();
				int next = current - reductions;
				if (next > current) {
					throw new Error("permit count underflow");
				}
				if (compareAndSetState(current, next)) {
					return;
				}
			}
		}

		final int drainPermits() {
			for (;;) {
				int current = getState();
				if (current == 0 || compareAndSetState(current, 0)) {
					return current;
				}
			}
		}

	}

	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = -5520691716509843296L;

		NonfairSync(int permits) {
			super(permits);
		}

		@Override
		protected int tryAcquireShared(int acquires) {
			return nonfairTryAcquireShared(acquires);
		}
	}

	static final class FairSync extends Sync {
		private static final long serialVersionUID = 7185227045079901224L;

		FairSync(int permits) {
			super(permits);
		}

		@Override
		protected int tryAcquireShared(int acquires) {
			for (;;) {
				if (hasQueuedPredecessors()) {
					return -1;
				}
				int available = getState();
				int remaining = available - acquires;
				if (remaining < 0 || compareAndSetState(available, remaining)) {
					return remaining; // ����ֵ����0 ���ǳɹ�
				}
			}
		}

	}

	public MSemaphore(int permits) {
		sync = new NonfairSync(permits);
	}

	public MSemaphore(int permits, boolean fair) {
		sync = fair ? new FairSync(permits) : new NonfairSync(permits);
	}

	public void acquire() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	public void acquireUninterruptibly() {
		sync.acquireShared(1);
	}

	public boolean tryAcquire() {
		return sync.nonfairTryAcquireShared(1) >= 0;
	}

	public boolean tryAcquire(long timeout, TimeUnit unit)
			throws InterruptedException {
		return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
	}

	public void release() {
		sync.releaseShared(1);
	}

	public void acquire(int permits) throws InterruptedException {
		if (permits < 0)
			throw new IllegalArgumentException();
		sync.acquireSharedInterruptibly(permits);
	}

	public void acquireUninterruptibly(int permits) {
		if (permits < 0)
			throw new IllegalArgumentException();
		sync.acquireShared(permits);
	}

	public boolean tryAcquire(int permits) {
		if (permits < 0)
			throw new IllegalArgumentException();
		return sync.nonfairTryAcquireShared(permits) >= 0;
	}

	public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (permits < 0)
			throw new IllegalArgumentException();
		return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
	}

	public void release(int permits) {
		if (permits < 0)
			throw new IllegalArgumentException();
		sync.releaseShared(permits);
	}

	public int availablePermits() {
		return sync.getPermits();
	}

	public int drainPermits() {
		return sync.drainPermits();
	}

	protected void reducePermits(int reduction) {
		if (reduction < 0)
			throw new IllegalArgumentException();
		sync.reducePermits(reduction);
	}

	public boolean isFair() {
		return sync instanceof FairSync;
	}

	public final boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	public final int getQueueLength() {
		return sync.getQueueLength();
	}

	protected Collection<Thread> getQueuedThreads() {
		return sync.getQueuedThreads();
	}

	public String toString() {
		return super.toString() + "[Permits = " + sync.getPermits() + "]";
	}
	

}