package com.fathzer.games.game;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ItemPublisher<T> implements AutoCloseable, Runnable {
	
	public interface ItemListener<T> extends Consumer<T> {
		default void onSubscribe(ItemPublisher<T> itemPublisher) {}
		default void onComplete(ItemPublisher<T> itemPublisher) {}
	}
	
	private final Queue<T> items;
	private final List<ItemListener<T>> subscribers;
	private final ExecutorService executor;
	private final AtomicBoolean paused; 
	private final Semaphore pauseLock;
	private volatile boolean isClosed = false;
	private volatile boolean wasInterrupted;
	
	public ItemPublisher() {
		this(null);
	}
	
	public ItemPublisher(ExecutorService itemProcessor) {
		this.items = new LinkedList<>();
		this.subscribers = new LinkedList<>();
		this.executor = itemProcessor;
		this.paused = new AtomicBoolean();
		this.pauseLock = new Semaphore(1);
	}

	public void subscribe(ItemListener<T> subscriber) {
		if (isClosed) {
			throw new IllegalStateException();
		}
		synchronized (subscribers) {
			subscribers.add(subscriber);
		}
		subscriber.onSubscribe(this);
	}
	
	@Override
	public void run() {
		while (!isClosed || !items.isEmpty()) {
			try {
				T item = null;
				synchronized(items) {
					if (items.isEmpty()) {
						items.wait();
					}
					item = items.poll();
				}
				if (item!=null) {
					pauseLock.acquire();
					try {
						process(item);
					} finally {
						pauseLock.release();
					}
				}
			} catch (InterruptedException e) {
				doInterrupted(e);
			}
		}
		for (ItemListener<T> sub : subscribers) {
			sub.onComplete(this);
		}
	}

	private void doInterrupted(InterruptedException e) {
		wasInterrupted = true;
		// Exit gracefully
		this.isClosed = true;
		this.items.clear();
		close();
		Thread.currentThread().interrupt();
	}

	private void process(T item) throws InterruptedException {
		if (executor==null) {
			synchronized (subscribers) {
				for (Consumer<T> sub : subscribers) {
					sub.accept(item);
				}
			}
		} else {
			List<? extends Callable<Void>> callables;
			synchronized (subscribers) {
				callables = subscribers.stream().map(s -> new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						s.accept(item);
						return null;
					}}).toList();
			}
			executor.invokeAll(callables);
		}
	}
	
	public boolean submit(Collection<T> items) {
		if (isClosed) {
			return false;
		}
		synchronized (this.items) {
			this.items.addAll(items);
			this.items.notifyAll();
		}
		return true;
	}
	
	public boolean pause(boolean pause) {
		if (!this.paused.compareAndSet(!pause, pause)) {
			return false;
		}
		if (pause) {
			try {
				pauseLock.acquire();
			} catch (InterruptedException e) {
				doInterrupted(e);
			}
		} else {
			pauseLock.release();
		}
		return true;
	}

	@Override
	public void close() {
		this.isClosed = true;
		synchronized (items) {
			items.notifyAll();
		}
	}
	
	public boolean wasInterrupted() {
		return wasInterrupted;
	}
}