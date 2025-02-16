package c0.util.pool;

public class PooledObjExample implements PooledObject<PooledObjExample>, PooledObjectFactory.Constructor{

    private String name;
    private int id;

    public PooledObjExample(String name, int id){
        this.name = name;
        this.id = id;
    }

    @Override
    public PooledObjExample init(){
        return this;
    }

    @Override
    public void reset() {
        
    }

    public PooledObjExample initialize(String name, int id){
        this.name = name;
        this.id = id;
        return this;
    }
    
}
