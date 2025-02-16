package c0.util.pool;


public interface ReferenceCounted<T> {

    T retain();

    T release();

}
