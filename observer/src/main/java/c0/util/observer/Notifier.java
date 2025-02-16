package c0.util.observer;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 */
public interface Notifier<T extends Observer> {

    /**
     * Register observer with notifier with a default priority 0
     * @param listener
     */
    void registerObserver(T observer);

    /**
     * Register observer with notifier with a specified priority. A higher priority int means an observer will be notifier first, lower - later
     * @param listener
     * @param priority
     */
    void registerObserver(T observer, int priority);

    /**
     * Removes observer from notifier
     * @param listener
     */
    void removeObserver(T observer);

    /**
     * Notifies observers stored in this notifier of an event, calling each observers handling method specified by the passed in handler
     * @param handler
     */
    void notifyObservers(EventHandler<T> handler);

    /**
     * Default, abstract implementation of a notifier
     */
    public abstract class AbstractNotifier<T extends Observer> implements Notifier<T> {

        private final Queue<ObserverWrapper<T>> observers = new PriorityQueue<>();

        private final int DEFAULT_PRIORITY = 0;

        /**
         * {@inheritdoc}
         */
        public void registerObserver(T observer) {
            observers.add(new ObserverWrapper<T>(observer, DEFAULT_PRIORITY));
        }

        /**
         * {@inheritdoc}
         */
        public void registerObserver(T observer, int priority) {
            observers.add(new ObserverWrapper<T>(observer, priority));
        }

        /**
         * {@inheritdoc}
         */
        public void removeObserver(T observer) {
            for (ObserverWrapper<T> wrapper : this.observers) {
                if (wrapper.getObserver().equals(observer)) {
                    this.observers.remove(wrapper);
                }
            }
        }

        /**
         * {@inheritdoc}
         */
        public void notifyObservers(EventHandler<T> handler) {
            for (ObserverWrapper<T> observer : observers) {
                handler.handle(observer.getObserver());
            }
        }
    }

    /**
     * Default, abstract implementation of a thread-safe blocking notifier
     */
    public abstract class AbstractBlockingNotifier<T extends Observer> implements Notifier<T> {
        private final Queue<ObserverWrapper<T>> observers = new PriorityBlockingQueue<>();
        private final Lock lock = new ReentrantLock();

        private static final int DEFAULT_PRIORITY = 0;

        public void registerObserver(T observer) {
            registerObserver(observer, DEFAULT_PRIORITY);
        }

        /**
         * {@inheritdoc}
         * This implementation acquires a {@link Lock} on the calling thread to prevent
         * concurrency issues
         */
        public void registerObserver(T observer, int priority) {
            lock.lock();
            try {
                observers.add(new ObserverWrapper<>(observer, priority));
            } finally {
                lock.unlock();
            }
        }

        /**
         * {@inheritdoc}
         * This implementation acquires a {@link Lock} on the calling thread to prevent
         * concurrency issues
         */
        public void removeObserver(T observer) {
            lock.lock();
            try {
                observers.removeIf(wrapper -> wrapper.getObserver().equals(observer));
            } finally {
                lock.unlock();
            }
        }

        /**
         * {@inheritdoc}
         * This implementation acquires a {@link Lock} on the calling thread to prevent
         * concurrency issues
         */
        @SuppressWarnings("unchecked")
        public void notifyObservers(EventHandler<T> handler) {
            ObserverWrapper<T>[] snapshot;
            lock.lock();
            try {
                snapshot = observers.toArray(new ObserverWrapper[0]);

                for (ObserverWrapper<T> observer : snapshot) {
                    handler.handle(observer.getObserver());
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
    * This class wraps observers stored by notifiers to allow for priority ordering without observers having to implement boilerplate code
    */
    public static class ObserverWrapper<T extends Observer> implements Comparable<ObserverWrapper<T>> {
        private final T observer;
        private final int priority;

        protected ObserverWrapper(T observer, int priority) {
            this.observer = observer;
            this.priority = priority;
        }

        protected T getObserver() {
            return observer;
        }

        @Override
        public int compareTo(ObserverWrapper<T> arg0) {
            return arg0.priority - priority;
        }
    }

    /**
     * A class that wraps {@link Notifier}s when returning them from an observable
     * class, in order to limit functionality to only allow {@link Observer}s to
     * register/remove themselves from {@link Notifier}s
     */
    public static class NotifierWrapper<T extends Observer> {

        private Notifier<T> wrappedNotifier;

        public NotifierWrapper(Notifier<T> notifier) {
            this.wrappedNotifier = notifier;
        }

        public void registerObserver(T observer) {
            this.wrappedNotifier.registerObserver(observer);
        }

        public void registerObserver(T observer, int priority) {
            this.wrappedNotifier.registerObserver(observer, priority);
        }

        public void removeObserver(T observer) {
            this.wrappedNotifier.removeObserver(observer);
        }

    }
}
