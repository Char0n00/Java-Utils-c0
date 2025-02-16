package c0.util.observer;

/**
 * Functional interface that allows to call a specific {@link Observer} method to notify of an event in the {@link Observable}
 */
@FunctionalInterface
public interface EventHandler<T> {
    void handle(T listener);
}
