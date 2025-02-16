package c0.util.pool;

@FunctionalInterface
public interface Initializable<T> {
    T init();
}