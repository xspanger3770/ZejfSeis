
public class ThrTest {
	
	private static Object mutex;

	public static void main(String[] args) {
		mutex = new Object();
		Thread thread1 = new Thread() {
			@Override
			public void run() {
				synchronized (mutex) {
					mutex.notifyAll();
				}
				System.out.println("notified");
			}
		};
		Thread thread2 = new Thread() {
			@Override
			public void run() {
				System.out.println("waiting...");
				synchronized (mutex) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
