package nova.agent.common.util;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Producer and consumer used in download and install progress
 * 
 * @author gaotao1987@gmail.com
 * 
 */
public class ProducerAndConsumer {
	private static int CAPACITY = 100;
	private LinkedList<String> queue = new LinkedList<String>();

	private static Lock lock = new ReentrantLock();

	private static Condition notEmpty = lock.newCondition();
	private static Condition notFull = lock.newCondition();

	public void write(String value) {
		lock.lock();
		try {
			while (queue.size() == CAPACITY) {
				notFull.await();
			}

			queue.offer(value);
			notEmpty.signal();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("finally")
	public String read() {
		String value = null;
		lock.lock();
		try {
			while (queue.isEmpty()) {
				notEmpty.await();
			}

			value = queue.remove();
			notFull.signal();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			lock.unlock();
			return value;
		}
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
