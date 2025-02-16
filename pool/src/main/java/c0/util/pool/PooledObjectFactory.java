package c0.util.pool;

public interface PooledObjectFactory<T extends PooledObject<T>> {

    public interface Initializer<Obj, Init>{
        Obj init(Init constructor);
    }

    public interface Constructor{

    }

    public abstract class AbstractFactory<T extends PooledObject<T>> implements PooledObjectFactory<T>{

    }



}
