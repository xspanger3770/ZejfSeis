import java.util.concurrent.Semaphore;

public class ThrTest2 {
	
	private static Semaphore semaphore;

	public static void main(String[] args) {
		semaphore = new Semaphore(0);
		Thread thread1 = new Thread() {
			@Override
			public void run() {
				semaphore.release();
				System.out.println("notified");
			}
		};
		Thread thread2 = new Thread() {
			@Override
			public void run() {
				System.out.println("waiting...");
				try {
					semaphore.acquire(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("wait done");
			}
		};
		
		thread1.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		thread2.start();
	}

}
