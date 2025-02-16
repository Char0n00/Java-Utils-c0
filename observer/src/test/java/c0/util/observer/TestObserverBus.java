package c0.util.observer;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import c0.util.observer.Notifier.NotifierWrapper;
import c0.util.observer.Observable.AbstractObservable;
import c0.util.observer.TestObserverBus.ObservableClass.NumberUpdateObserver;
import c0.util.observer.TestObserverBus.ObservableClass.StringListUpdateObserver;
import c0.util.observer.TestObserverBus.ObservableClass.StringUpdateObserver;

public class TestObserverBus {

    /**
     * Example of an observable class 
     */
    public class ObservableClass extends AbstractObservable{

        public interface NumberUpdateObserver extends Observer{
            void onEvent(int number);
        }

        public interface StringUpdateObserver extends Observer{
            void onEvent(String string);
        }

        public interface StringListUpdateObserver extends Observer{
            void addToList(String string, List<String> list);
        }

        // Example of setting up a bus. The bus makes managing notifiers more modular for each observable class
        private ObserverBus bus;

        public ObservableClass(){
            bus = new ObserverBus();
            bus.registerNotifier(StringUpdateObserver.class);
            bus.registerBlockingNotifier(NumberUpdateObserver.class);
            bus.registerNotifier(StringListUpdateObserver.class);
        }

        // This method comes from the {@link Observable} interface, to allow observer classes assign and remove themselves but nothing else
        @Override
        public <T extends Observer> NotifierWrapper<T> getNotifier(Class<T> eventType) {
            return new NotifierWrapper<>(bus.getNotifier(eventType));
        }

        // Example of getting a {@link StringUpdateObserver} and calling its onEventMethod
        public void notifyString(String string){
            bus.notify(StringUpdateObserver.class, listener -> listener.onEvent(string));
        }

        // Example of getting a {@link NumberUpdateObserver} and calling its onEvent method
        public void notifyNumber(int number) {
            bus.notify(NumberUpdateObserver.class, listener -> listener.onEvent(number));
        }

        public void notifyAddString(String string, List<String> list){
            bus.notify(StringListUpdateObserver.class, listener -> listener.addToList(string, list));
        }
    }

    public class ObserverClass implements NumberUpdateObserver, StringUpdateObserver, StringListUpdateObserver{

        public int number = 0;
        public String string;

        @Override
        public void onEvent(String string) {
            this.string = string;
        }

        @Override
        public void onEvent(int number) {
            this.number = number;        
        }

        @Override
        public void addToList(String string, List<String> list) {
            list.add(string + " " + number);
        }
    }

    private ObservableClass observable;

    @Before
    public void setup(){
        this.observable = new ObservableClass();
    }

    @Test
    public void testMainFunctionality(){
        ObserverClass observer = new ObserverClass();
        // Verify notifiers that have not registered any observers dont throw any null errors and dont perform any updates
        observable.notifyNumber(3);
        assertTrue(observer.number == 0);

        observable.notifyString("Example string");
        assertTrue(observer.string == null);

        observable.getNotifier(NumberUpdateObserver.class).registerObserver(observer);
        observable.getNotifier(StringUpdateObserver.class).registerObserver(observer);

        // Verify observer get notifier correctly
        observable.notifyNumber(3);
        assertTrue(observer.number == 3);

        observable.notifyString("Example string");
        assertTrue(observer.string.equals("Example string"));

        observable.getNotifier(NumberUpdateObserver.class).removeObserver(observer);
        observable.getNotifier(StringUpdateObserver.class).removeObserver(observer);

        observer = null;
    }

    @Test
    public void testObserverPriorityOrdering(){
        List<String> accumulator = new ArrayList<>();

        ObserverClass observer1 = new ObserverClass();
        observer1.number = 1;
        ObserverClass observer2 = new ObserverClass();
        observer2.number = 2;
        ObserverClass observer3 = new ObserverClass();
        observer3.number = 3;
        ObserverClass observer4 = new ObserverClass();
        observer4.number = 4;

        // Add observers with specified priority
        observable.getNotifier(StringListUpdateObserver.class).registerObserver(observer1, 3);
        observable.getNotifier(StringListUpdateObserver.class).registerObserver(observer3, 2);
        observable.getNotifier(StringListUpdateObserver.class).registerObserver(observer2, 2);
        observable.getNotifier(StringListUpdateObserver.class).registerObserver(observer4, 1);

        observable.notifyAddString("String", accumulator);

        // Verify observers were notified in correct order, with observers 3 and 2 being notified in the order they were added since they have the same priority
        assertTrue(accumulator.get(0).equals("String 1"));
        assertTrue(accumulator.get(1).equals("String 3"));
        assertTrue(accumulator.get(2).equals("String 2"));
        assertTrue(accumulator.get(3).equals("String 4"));

        observer1 = null;
        observer2 = null;
        observer3 = null;
        observer4 = null;
    }

    @After
    public void deconstruct(){
        observable = null;
        System.gc();
    }
}
