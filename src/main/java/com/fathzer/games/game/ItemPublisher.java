package com.fathzer.games.game;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ItemPublisher<T> implements AutoCloseable, Runnable {
	
	public interface ItemListener<T> extends Consumer<T> {
		void onSubscribe(ItemPublisher<T> itemPublisher);
		void onComplete(ItemPublisher<T> itemPublisher);
	}
	
	private final Queue<T> items = new LinkedList<>();
	private final List<ItemListener<T>> subscribers = new LinkedList<>();
	private boolean isClosed = false;
	private boolean wasInterrupted;
	private ExecutorService executor = null;

	/**
	 * @return the executor
	 */
	public ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * @param executor the executor to set
	 */
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
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
					process(item);
				}
			} catch (InterruptedException e) {
				// Exit gracefully
				this.isClosed = true;
				this.items.clear();
				close();
				Thread.currentThread().interrupt();
			}
		}
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
	
	public void submit(Collection<T> items) {
		if (isClosed) {
			throw new IllegalStateException();
		}
		synchronized (this.items) {
			this.items.addAll(items);
			this.items.notifyAll();
		}
	}

	@Override
	public void close() {
		this.isClosed = true;
		synchronized (subscribers) {
			for (ItemListener<T> sub : subscribers) {
				sub.onComplete(this);
			}
		}
		synchronized (items) {
			items.notifyAll();
		}
	}
	
	public boolean wasInterrupted() {
		return wasInterrupted;
	}
}