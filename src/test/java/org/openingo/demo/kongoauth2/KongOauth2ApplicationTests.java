package org.openingo.demo.kongoauth2;

import lombok.SneakyThrows;
import org.openingo.jdkits.validate.ValidateKit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

//@SpringBootTest
class KongOauth2ApplicationTests {


	private static Map m = new HashMap<>();

 ReentrantLock lock = new ReentrantLock(true);

	@SneakyThrows
	public void ok() {
		System.out.println("starting" + Thread.currentThread().getName());
//		synchronized (KongOauth2ApplicationTests.class) {
		lock.lock();

		if (ValidateKit.isNotEmpty(m)) {
			m.put("not", "not");
		} else {
			m.put("em", "em");
		}

			lock.unlock();
//		}
//		TimeUnit.SECONDS.sleep(3);

		System.out.println(m);
		System.out.println("ending" + Thread.currentThread().getName());
	}

//	@Test
	void contextLoads() {
	}

	public static void main(String[] args) {

		ExecutorService executorService = Executors.newFixedThreadPool(5);
		for (int i = 0; i < 50; i++) {
			executorService.submit(new KongOauth2ApplicationTests()::ok);
		}
		executorService.shutdown();
	}

}
