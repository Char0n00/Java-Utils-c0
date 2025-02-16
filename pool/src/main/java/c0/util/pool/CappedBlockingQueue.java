package c0.util.pool;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A custom extention of a {@link ConcurrentLinkedQueue} for an object pool, which has a mainautomatically nullifies objects which are offered to the queue 
 */
public class CappedBlockingQueue extends ConcurrentLinkedQueue<PooledObject<?>>{

	private int maxSize;

	public CappedBlockingQueue(int maxSize) {
        this.maxSize = maxSize;
    }

	@Override
	public boolean offer(PooledObject<?> item) {
		if(size() > maxSize){
			item = null;
			return false;
		} else {
			return super.offer(item);
		}
		
	}

	public void updateSize(int newSize){
		if(newSize < maxSize){
			shrink(newSize);
		} else if(newSize > maxSize){
			expand(newSize);
		}
	}

	private void expand(int newSize){
		if(newSize < 1 || newSize <= maxSize){
			return;
		}
		else{
			maxSize = newSize;
		}
	}

	private void shrink(int newSize){
		if(newSize < 1) {
			return;
		}
		while(size() > newSize) {
            poll();
        }
		this.maxSize = newSize;
	}

}