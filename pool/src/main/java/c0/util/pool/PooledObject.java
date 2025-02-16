package c0.util.pool;

/**
 * An interface for pooled objects with default implementations for managing
 * their lifetime and accessing the object pool
 */
public interface PooledObject<T extends PooledObject<T>> extends Initializable<T>, ReferenceCounted<T>{

    ObjectPool objectPool = ObjectPool.instance();
    
    @SuppressWarnings("unchecked")
    default T retain() {
        Class<T> returnedObjectClazz = (Class<T>) this.getClass();

        int referenceCount = objectPool.objectReferenceCountMap.getOrDefault(returnedObjectClazz, 0);

        if (referenceCount == 0) {
            objectPool.objectReferenceCountMap.put(returnedObjectClazz, 1);
        } else {
            objectPool.objectReferenceCountMap.put(returnedObjectClazz, ++referenceCount);
        }

        return (T)this;
    }

    @SuppressWarnings("unchecked")
    default T release() {
        
        Class<T> returnedObjectClazz = (Class<T>) this.getClass();
        int referenceCount = objectPool.objectReferenceCountMap.get(returnedObjectClazz);

        // remove a reference
        objectPool.objectReferenceCountMap.put(returnedObjectClazz, --referenceCount);

        if (referenceCount <= 0) {
            this.returnToPool();
        }
        return (T)this;
    }

    /**
     * Default method to return the object to the pool.
     */
    default void returnToPool() {
        objectPool.returnExecutor.submit(() -> {
            @SuppressWarnings("unchecked")
            Class<T> returnedObjectClazz = (Class<T>) this.getClass();
            this.reset();
            objectPool.poolMap.computeIfAbsent(returnedObjectClazz,
                    k -> new CappedBlockingQueue(
                            objectPool.objectPoolSizeMap.getOrDefault(returnedObjectClazz,
                                    objectPool.defaultObjectsPooledPerType)))
                    .offer(this);
        });
    }

    /**
     * Method to reset the object's state when reused.
     */
    abstract public void reset();

    /**
     * Method to initialize the object when retrieved from the pool. Parameters can
     * vary depending on the object's needs.
     */
    //abstract public void initialize(Object... params);

}
