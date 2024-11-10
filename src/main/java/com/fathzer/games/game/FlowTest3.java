package com.fathzer.games.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntPredicate;

import com.fathzer.games.game.ItemPublisher.ItemListener;
import com.fathzer.games.util.exec.CustomThreadFactory;

public class FlowTest3 {
	private static final class MySubscriber implements ItemListener<Integer> {
		private static AtomicLong counter = new AtomicLong();
		
		private final long id;
		private final IntPredicate continuePredicate;
		private boolean completed;
		private ItemPublisher<Integer> publisher;
		
		private MySubscriber(IntPredicate continuePredicate) {
			super();
			this.continuePredicate = continuePredicate;
			this.id = counter.incrementAndGet();
		}
		
		private void subscribe(ItemPublisher<Integer> publisher) {
			publisher.subscribe(this);
		}
		
		@Override
		public void onSubscribe(ItemPublisher<Integer> publisher) {
			System.out.println("onSubscribed on "+getId());
			this.publisher = publisher;
		}
		
		private String getId() {
			return id+" ("+Thread.currentThread()+")";
		}

		@Override
		public void accept(Integer item) {
			System.out.println("onNext on "+getId()+" with "+item);
			if (item==10) {
				new RuntimeException("Let see the stack trace").printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			completed = item>=10;
			if (!completed & continuePredicate.test(item)) {
				System.out.println("onNext on "+getId()+" publishes "+(item+1));
				publisher.submit(Collections.singleton(item+1));
			}
		}
		
		public boolean isCompleted() {
			return completed;
		}

		@Override
		public void onComplete(ItemPublisher<Integer> publisher) {
			System.out.println("onComplete on "+getId());
//			this.completed = true;
		}
	}

	public static void main(String[] args) {
		final MySubscriber sub1 = new MySubscriber(i->i!=0&&i%2==1);
		final MySubscriber sub2 = new MySubscriber(i->i!=0&&i%2==0);
		try (final ItemPublisher<Integer> publisher = new ItemPublisher<>()) {
			publisher.setExecutor(Executors.newFixedThreadPool(4, new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("Item publisher thread"), true)));
			sub1.subscribe(publisher);
			sub2.subscribe(publisher);
			new Thread(publisher).start();
			publisher.submit(Arrays.asList(0, 1));
			while (!(sub1.isCompleted() && sub2.isCompleted())) {
				System.out.println(Thread.currentThread()+" is waiting for subscriber to complete");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		System.out.println("End of program");
	}

}
