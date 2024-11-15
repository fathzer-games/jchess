package com.fathzer.games.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.fathzer.games.game.ItemPublisher.ItemListener;
import com.fathzer.games.util.exec.CustomThreadFactory;

class ItemPublisherTest {
	private static class MyListener<T> implements ItemListener<T> {
		private volatile boolean subscribed;
		private volatile boolean completed;
		private final List<T> items = new LinkedList<>();
		
		@Override
		public void onSubscribe(ItemPublisher<T> itemPublisher) {
			subscribed = true;
		}

		@Override
		public void onComplete(ItemPublisher<T> itemPublisher) {
			completed = true;
		}

		@Override
		public void accept(T t) {
			if (subscribed && !completed) {
				items.add(t);
			}
		}
	}
	
	private record TestData<T>(List<T> expected, List<MyListener<T>> listeners) {
		void assertExpected() {
			forEach(l -> org.junit.jupiter.api.Assertions.assertEquals(expected, l.items));
		}
		void forEach(Consumer<MyListener<T>> action) {
			listeners.forEach(action);
		}
		void addToExpected(Collection<T> items) {
			expected.addAll(items);
		}
		Callable<Boolean> listsHaveRightSize() {
			return () -> listeners.stream().allMatch(l -> l.items.size()==expected.size());
		}
	
	}

	@Test
	void test() {
		final ItemPublisher<Long> pub = new ItemPublisher<>(Executors.newFixedThreadPool(2, new CustomThreadFactory(()->"publishWorker", true)));
		assertFalse(pub.wasInterrupted());
		final Thread pubThread = new Thread(pub);
		final TestData<Long> data = new TestData<>(new LinkedList<>(), Arrays.asList(new MyListener<>(), new MyListener<>()));
		data.assertExpected();
		data.forEach(l -> assertTrue(!l.subscribed && !l.completed));
		data.forEach(l -> pub.subscribe(l));
		pubThread.start();
		data.forEach(l -> assertTrue(l.subscribed && l.items.isEmpty() && !l.completed));
		assertTrue(pub.submit(Arrays.asList(1L,2L)));
		data.addToExpected(Arrays.asList(1L, 2L));
		final Callable<Boolean> listsHaveRightSize = data.listsHaveRightSize();
		await().until(listsHaveRightSize);
		data.assertExpected();
		data.forEach(l -> assertFalse(l.completed));
		assertFalse(pub.pause(false));
		// Pause the publisher
		assertTrue(pub.pause(true));
		assertFalse(pub.pause(true));
		assertTrue(pub.submit(Arrays.asList(3L)) && pub.submit(Arrays.asList(4L)));
		// Nothing received despite event are published
		await().atLeast(100, TimeUnit.MILLISECONDS).and().atMost(150, TimeUnit.MILLISECONDS).until(listsHaveRightSize);
		data.assertExpected();
		// Restart the publisher
		assertTrue(pub.pause(false));
		// Items received during pause should now be received
		data.addToExpected(Arrays.asList(3L, 4L));
		await().until(listsHaveRightSize);
		data.assertExpected();
		// Pause again and publish an item
		assertTrue(pub.pause(true));
		assertTrue(pub.submit(Arrays.asList(5L)));
		// close the paused publisher
		pub.close();
		// listeners should receive nothing
		await().atLeast(100, TimeUnit.MILLISECONDS).and().atMost(150, TimeUnit.MILLISECONDS).until(listsHaveRightSize);
		data.assertExpected();
		data.forEach(l -> assertFalse(l.completed));
		// Publish one more item that should be ignored
		assertFalse(pub.submit(Arrays.asList(6L)));
		// Restart the publisher
		assertTrue(pub.pause(false));
		// Check 5 was received and not 6
		data.addToExpected(Collections.singleton(5L));
		await().until(listsHaveRightSize);
		data.assertExpected();
		// Check complete event was received
		data.forEach(l -> assertTrue(l.completed));
		// Check the publisher thread was ended
		assertFalse(pubThread.isAlive());
		// Not more subscribers are accepted
		final ItemListener<Long> newOne = new MyListener<>();
		assertThrows(IllegalStateException.class, () -> pub.subscribe(newOne));
	}

}
