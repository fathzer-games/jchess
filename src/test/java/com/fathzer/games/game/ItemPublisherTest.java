package com.fathzer.games.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import com.fathzer.games.game.ItemPublisher.ItemListener;
import com.fathzer.games.util.exec.CustomThreadFactory;

class ItemPublisherTest {
	private static class MyListener implements ItemListener<Long> {
		private volatile boolean subscribed;
		private volatile boolean completed;
		private final List<Long> items = new LinkedList<>();
		
		@Override
		public void onSubscribe(ItemPublisher<Long> itemPublisher) {
			subscribed = true;
		}

		@Override
		public void onComplete(ItemPublisher<Long> itemPublisher) {
			completed = true;
		}

		@Override
		public void accept(Long t) {
			if (subscribed && !completed) {
				items.add(t);
			}
		}
	}

	@Test
	void test() throws InterruptedException {
		final ItemPublisher<Long> pub = new ItemPublisher<>(Executors.newFixedThreadPool(2, new CustomThreadFactory(()->"publishWorker", true)));
		assertFalse(pub.wasInterrupted());
		final Thread pubThread = new Thread(pub);
		final MyListener listener = new MyListener();
		final MyListener other = new MyListener();
		assertFalse(listener.subscribed);
		assertFalse(listener.completed);
		assertTrue(listener.items.isEmpty());
		pub.subscribe(listener);
		pub.subscribe(other);
		pubThread.start();
		assertTrue(listener.subscribed);
		assertFalse(listener.completed);
		assertTrue(listener.items.isEmpty());
		assertTrue(pub.submit(Arrays.asList(1L,2L)));
		final List<Long> expected = new LinkedList<>();
		Thread.sleep(100);
		assertFalse(listener.completed);
		expected.addAll(Arrays.asList(1L, 2L));
		assertEquals(expected, listener.items);
		assertEquals(expected, other.items);
		assertFalse(pub.pause(false));
		// Pause the publisher
		assertTrue(pub.pause(true));
		assertFalse(pub.pause(true));
		assertTrue(pub.submit(Arrays.asList(3L)));
		assertTrue(pub.submit(Arrays.asList(4L)));
		Thread.sleep(100);
		// Nothing received despite event are published
		assertEquals(expected, listener.items);
		assertEquals(expected, other.items);
		// Restart the publisher
		assertTrue(pub.pause(false));
		Thread.sleep(100);
		// Items received during pause should now be received
		expected.addAll(Arrays.asList(3L, 4L));
		assertEquals(expected, listener.items);
		assertEquals(expected, other.items);
		// Pause again and publish an item
		assertTrue(pub.pause(true));
		assertTrue(pub.submit(Arrays.asList(5L)));
		// close the paused publisher
		pub.close();
		Thread.sleep(100);
		// listeners should receive nothing
		assertEquals(expected, listener.items);
		assertEquals(expected, other.items);
		assertFalse(listener.completed);
		assertFalse(other.completed);
		// Publish one more item that should be ignored
		assertFalse(pub.submit(Arrays.asList(6L)));
		expected.add(5L);
		// Restart the publisher
		assertTrue(pub.pause(false));
		Thread.sleep(100);
		// Check 5 was received and not 6
		assertEquals(expected, listener.items);
		assertEquals(expected, other.items);
		// Check complete event was received
		assertTrue(listener.completed);
		assertTrue(other.completed);
		// Check the publisher thread was ended
		assertFalse(pubThread.isAlive());
	}

}
