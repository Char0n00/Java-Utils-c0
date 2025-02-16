package c0.util.observer;

import c0.util.observer.Notifier.NotifierWrapper;

/**
 * This interface specifies classes that notify observers of events
 */
public interface Observable {

    /**
     * Getter for a notifier for a specific observer sub-type, wrapped in a
     * {@link NotifierWrapper}. This allows to manage registering/removing observers
     * for an observable class outside of this class with access to only required
     * methods
     * 
     * @param <T>       The observer sub-type, should be left as <T extends
     *                  Observer>
     * @param eventType {@link Class} of the observer for which to get the notifier
     *                  that used as the key
     * @return {@link NotifierWrapper} that wraps the requested notifier
     */
    <T extends Observer> NotifierWrapper<T> getNotifier(Class<T> eventType);

    /**
     * An abstract observable implementation that will initialize a {@code final} {@link ObserverBus} and provide a default implementation for retrieving a wrapped notifier
     */
    abstract public class AbstractObservable implements Observable{

        protected ObserverBus bus;

        protected AbstractObservable(){
            bus = new ObserverBus();
        }

        @Override
        public <T extends Observer> NotifierWrapper<T> getNotifier(Class<T> eventType) {
            return new NotifierWrapper<>(bus.getNotifier(eventType));
        }

    }

}
