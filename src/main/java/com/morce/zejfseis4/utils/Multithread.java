package com.morce.zejfseis4.utils;

public abstract class Multithread {
	
	private Thread[] buffer;

	public Multithread(int threads) {
		buffer = new Thread[threads];
		
	}

	public final void divide(long start, long end) {
		if (end - start < 0) {
			return;
		}
		int threads = (int) Math.min(end - start, buffer.length);
		for (int i = 0; i < threads; i++) {
			long a = (long) (start + (end - start) * (i / (double) (threads)));
			long b = (long) (start + (end - start) * ((i+1) / (double) (threads)));
			buffer[i] = new Thread() {
				@Override
				public void run() {
					Multithread.this.run(a, b);
				}
			};
		}
		for (int i = 0; i < threads; i++) {
			buffer[i].start();
		}
		for (int i = 0; i < threads; i++) {
			try {
				buffer[i].join();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public abstract void run(long start, long end);
	
	public static void main(String[] args) {
		new Multithread(5) {
			
			@Override
			public void run(long start, long end) {
				System.out.println(start+", "+end);
			}
		}.divide(10, 12);
	}

}
