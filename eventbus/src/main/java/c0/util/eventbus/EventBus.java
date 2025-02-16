package c0.util.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import c0.util.eventbus.annotations.Subscribe;
import c0.util.eventbus.events.Event;

/**
 * The event bus, which registers subscribers for events and is used to notify
 * all stored subscribers of an event.
 */
public class EventBus {

	private final Map<Class<?>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

	private boolean checkIfEvent(Object object) {
		if (!(object instanceof Event)) {
			new IllegalArgumentException();
			return false;
		}
		return true;
	}

	public synchronized void register(Object listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (method.isAnnotationPresent(Subscribe.class)) {
				if (method.getParameterCount() != 1) {
					throw new IllegalArgumentException("Event subscriber " + method.getName()
							+ " must have exactly one parameter for the event that will be passed to it.");
				}

				// Get the event type through the method parameter
				Class<?> eventType = method.getParameterTypes()[0];
				Subscribe annotation = method.getAnnotation(Subscribe.class);
				int priority = annotation.priority();

				subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
						.add(new Subscriber(listener, method, priority));
			}
		}
	}

	public synchronized void unregister(Object listener) {
		subscribers.values().forEach(subscriberQueue -> subscriberQueue
				.removeIf(subscriber -> subscriber.getListener().equals(listener)));
	}

	public synchronized void post(Event event) {
		checkIfEvent(event);

		// Traverse the class hierarchy to find all subscribers for the event and its
		// supertypes
		Class<?> eventType = event.getClass();
		while (eventType != null) {
			List<Subscriber> subscribersForEvent = subscribers.get(eventType);
			if (subscribersForEvent != null) {
				subscribersForEvent.stream().sorted(Comparator.reverseOrder())
						.forEach(subscriber -> {
							try {
								subscriber.invoke(event);
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
			}

			// Checks for interfaces implemented by the current type
			for (Class<?> iface : eventType.getInterfaces()) {
				List<Subscriber> subscribersForInterface = subscribers.get(iface);
				if (subscribersForInterface != null) {
					subscribersForInterface.stream().sorted(Comparator.reverseOrder())
							.forEach(subscriber -> {
								try {
									subscriber.invoke(event);
								} catch (Exception e) {
									e.printStackTrace();
								}
							});
				}
			}

			// Move up the class hierarchy
			eventType = eventType.getSuperclass();
		}
	}

	/**
	 * Wrapper to allow to store method siganures as subscribers and order them by priority.
	 */
	private static class Subscriber implements Comparable<Subscriber> {
		private final Object listener;
		private final Method method;
		private final int priority;

		public Subscriber(Object listener, Method method, int priority) {
			this.listener = listener;
			this.method = method;
			this.priority = priority;
		}

		public Object getListener() {
			return listener;
		}

		public int getPriority() {
			return priority;
		}

		public void invoke(Object event) throws Exception {
			method.invoke(listener, event);
		}

		@Override
		public int compareTo(Subscriber other) {
			return Integer.compare(this.priority, other.priority);
		}
	}
}
