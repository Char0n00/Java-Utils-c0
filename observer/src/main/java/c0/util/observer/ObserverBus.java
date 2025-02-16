package c0.util.observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import c0.util.observer.Notifier.AbstractBlockingNotifier;
import c0.util.observer.Notifier.AbstractNotifier;

/**
 * Default implementation of an observer bus with utility methods to add/remove
 * notifiers and easily notify observers
 */
public class ObserverBus {
    private final Map<Class<?>, Notifier<?>> notifiers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Observer> Notifier<T> getNotifier(Class<T> clazz) {
        return (Notifier<T>) notifiers.get(clazz);
    }

    /**
     * Notifies passed type of observer of an event (if before calling this the observer type was register with {@link #registerNotifier(Class)} or {@link #registerBlockingNotifier(Class)})
     * @param <T>          type of the observer that will be notifier, will be
     *                     inferred after passing the observerType
     * @param observerType type of observer to notify
     * @param handler      expression that specifies which observer method to call
     *                     and what parameters to pass when handling notification
     */
    public <T extends Observer> void notify(Class<T> observerType, EventHandler<T> handler) {
        Notifier<T> notifier = getNotifier(observerType);
        if (notifier != null) {
            notifier.notifyObservers(handler);
        }
    }

    /**
     * Creates a new notifier within the bus for an event type. Automatically
     * prevents duplicate notifiers for the same observer type
     * 
     * @param <T>         observer type
     * @param observerKey custom observer type to register new notifier for
     */
    public <T extends Observer> void registerNotifier(Class<T> observerKey) {
        if (notifiers.get(observerKey) == null) {
            notifiers.put(observerKey, new AbstractNotifier<T>() {
            });
        }
    }

    /**
     * Creates a new blocking notifier within the bus for an event type.
     * Automatically prevents duplicate notifiers for the same observer type
     * 
     * @param <T>         observer type
     * @param observerKey custom observer type to register new notifier for
     */
    public <T extends Observer> void registerBlockingNotifier(Class<T> observerKey) {
        if (notifiers.get(observerKey) == null) {
            notifiers.put(observerKey, new AbstractBlockingNotifier<T>() {
            });
        }
    }

    /**
     * Deletes a stored notifier and so removed links to any stored observers for
     * that notifier
     * @param <T>         observer type of the notifier to remove
     * @param observerKey custom observer type to remove notifier for
     */
    public <T extends Observer> void removeNotifier(Class<T> observerKey) {
        notifiers.remove(observerKey);
    }

    /**
     * Clears stored notifiers
     */
    public void clearNotifiers() {
        this.notifiers.clear();
    }
}
