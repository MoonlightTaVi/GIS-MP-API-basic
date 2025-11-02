package gismp;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
	private final TimedSemaphore timedSemaphore;

	public CrptApi(TimeUnit timeUnit, int requestLimit) {
		timedSemaphore = new TimedSemaphore(timeUnit, requestLimit);
	}
	
	
	/**
	 * A special version of Java Semaphore. <br>
	 * This kind of Semaphore has only {@code acquire()} method,
	 * that reduces the number of available permits, and blocks
	 * the caller-Thread (scheduling the execution for later)
	 * if no permits available. <br>
	 * The permits number is automatically replenished on a timed basis,
	 * using the daemon-thread for this purpose.
	 */
	public static class TimedSemaphore implements Runnable {
		final TimeUnit timeUnit;
		final int requestLimit;
		
		private final Semaphore semaphore;
		private final Thread releaseThread;
		
		/**
		 * @see TimedSemaphore
		 * @param timeUnit Specifies a TimeUnit for 
		 * releasing a Semaphore permit. <br>
		 * The {@code timeout} value 
		 * of the Unit (for Thread sleeping) will always be set to 1,
		 * so only the TimeUnit itself (seconds, minutes, etc.) matters.
		 * @param requestLimit Maximum number of available
		 * request during the mentioned TimeUnit.
		 */
		public TimedSemaphore(TimeUnit timeUnit, int requestLimit) {
			this.timeUnit = timeUnit;
			this.requestLimit = requestLimit;
			semaphore = new Semaphore(requestLimit, true);
			releaseThread = new Thread(this);
			releaseThread.setDaemon(true);
			releaseThread.start();
		}
		
		/**
		 * Acquires a permit from this semaphore,
		 * locking the Thread and scheduling it for a later
		 * execution if no permits available. <br>
		 * The permits are replenished automatically on a timed basis.
		 * @throws InterruptedException If the current Thread was interrupted
		 * while waiting for a permit.
		 */
		public void acquire() throws InterruptedException {
			semaphore.acquire();
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					// Release semaphore on a timed basis
					timeUnit.sleep(1);
					
					int currentPermits = semaphore.availablePermits();
					if (currentPermits < requestLimit) {
						semaphore.release();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
}
