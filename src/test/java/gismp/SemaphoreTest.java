package gismp;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gismp.CrptApi.TimedSemaphore;

/**
 * These tests make sure that TimedSemaphore is
 * working properly. <br>
 * There are two tests, both having the same conditions,
 * but one of them is restricted to 3 permits,
 * while the other is allowed to acquire 10 permits.
 */
public class SemaphoreTest {
	/** Used for thread naming. */
	static int threadCount = 0;
	/** Each test has its own semaphore. */
	static TimedSemaphore semaphore;
	
	/**
	 * Starts a new Thread using the same template. <br>
	 * The thread waits for 1 seconds and shuts down.
	 * @return The new Thread, so that {@code join()}
	 * can be called on it.
	 */
	public static Thread startNewThread() {
		threadCount++;
		
		Runnable runnable = () -> {
			try {
				// Acquire lock
				semaphore.acquire();
				
				System.out.printf(
						"%s started.%n", 
						Thread.currentThread().getName()
						);
				
				Thread.sleep(1000);
				
				System.out.printf(
						"%s finished.%n", 
						Thread.currentThread().getName()
						);
			} catch (InterruptedException e) {
				System.out.printf(
						"%s interrupted.%n", 
						Thread.currentThread().getName()
						);
			}
			
		};
		String threadName = String.format("Thread-%d", threadCount);
		Thread thread = new Thread(runnable, threadName);
		thread.start();
		return thread;
	}

	@Test
	public void executionScheduledProperly() {
		// 3 permits per second
		semaphore = new TimedSemaphore(TimeUnit.SECONDS, 3);
		
		// Count the time passed
		long start = System.currentTimeMillis();
		long end = start;
		
		// Get the last thread and join it
		Thread lastThread = null;
		for (int i = 0; i < 10; i++) {
			lastThread = startNewThread();
		}
		
		try {
			lastThread.join();
		} catch (InterruptedException e) {
			System.err.println("Main thread was interrupted.");
		} finally {
			end = System.currentTimeMillis();
		}
		
		// (10 threads overall / 3 threads running in parallel) ~= 3 seconds
		// More than 3 second (the very best case) should pass
		int secondsPassed = (int) ((end - start) / 1000);
		Assertions.assertTrue(secondsPassed > 3);
	}

	@Test
	public void executionIsImmediate() {
		// 10 permits per second
		semaphore = new TimedSemaphore(TimeUnit.SECONDS, 10);
		
		// Count the time passed
		long start = System.currentTimeMillis();
		long end = start + 10;
		
		// Get the last thread and join it
		Thread lastThread = null;
		for (int i = 0; i < 10; i++) {
			lastThread = startNewThread();
		}
		
		try {
			lastThread.join();
		} catch (InterruptedException e) {
			System.err.println("Main thread was interrupted.");
		} finally {
			end = System.currentTimeMillis();
		}
		
		// Execution must be finished almost immediately
		int secondsPassed = (int) ((end - start) / 1000);
		Assertions.assertTrue(secondsPassed < 2);
	}
	
}
