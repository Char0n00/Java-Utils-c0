package c0.util.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 */
public class ObjectPool {

	// ----------------------------------------------------------

	static private ObjectPool instance;

	static protected ObjectPool instance() {
		if (instance == null) {
			instance = new ObjectPool();
		}
		return instance;
	}

	private ObjectPool() {
		daemmon = new Daemon();
	}

	// ----------------------------------------------------------

	protected Map<Class<? extends PooledObject<?>>, CappedBlockingQueue> poolMap = new ConcurrentHashMap<>();
	protected ExecutorService returnExecutor = Executors.newSingleThreadExecutor();

	protected Map<Class<? extends PooledObject<?>>, Integer> objectPoolSizeMap = new ConcurrentHashMap<>();
	protected Map<Class<? extends PooledObject<?>>, Integer> objectReferenceCountMap = new ConcurrentHashMap<>();

	protected int defaultObjectsPooledPerType = 5;

	private Daemon daemmon;

	static void startDaemon() {

	}

	/**
	 * Class handling management of long-running object pool tasks, such as dynamic
	 * pool
	 */
	public class Daemon {

		private ExecutorService executor = Executors.newFixedThreadPool(2);
		private boolean uiEnabled = false;

		private LinkedList<Map<Class<? extends PooledObject<?>>, Integer>> requestsPerObjectHistory = new LinkedList<>();
		private Map<Class<? extends PooledObject<?>>, Integer> requestsPerObject = new ConcurrentHashMap<>();

		public Daemon() {
			executor.submit(() -> {
				writeToHistoryThread();
			});
			executor.submit(() -> {
				resizeThread();
			});
		}

		private void resizeThread(){
			while(true){
				try{
					Thread.sleep(25000);
				} 
				catch (InterruptedException e){

				} 
				finally {

				}
			}
		}

		private void recalculateSizes(){
			for(int i = 0; i < 5; i++){
				Map<Class<? extends PooledObject<?>>, Integer> historyNode = requestsPerObjectHistory.get(i);
				int objectCount = 0;
				for(Map.Entry<Class<? extends PooledObject<?>>, Integer> entry : historyNode.entrySet()){
					entry.getValue();
				}
			}
		}

		private void writeToHistoryThread(){
			while(true){
				try{
					Thread.sleep(5000);
					writeRequestsToHistory();
				} 
				catch(InterruptedException e){

				} 
				finally {

				}

			}
		}

		private void writeRequestsToHistory() {
			requestsPerObjectHistory.push(requestsPerObject);
			requestsPerObject.clear();
		}

		protected void requested(Class<? extends PooledObject<?>> requestedType) {
			executor.submit(() -> {
				int requests = requestsPerObject.getOrDefault(requestedType, 0);
				if(requests == 0){
					requestsPerObject.put(requestedType, 1);
				} else {
					requestsPerObject.put(requestedType, requests++);
				}
			});
		}

	}

	/**
	 * Static method to set the pool size for a specific PooledObject class.
	 */
	static void setPoolSize(Class<? extends PooledObject<?>> clazz, int poolSize) {
		instance.objectPoolSizeMap.compute(clazz, (k, v) -> poolSize);
	}

	// ----------------------------------------------------------

	/**
	 * Static method to retrieve an object from the pool or create a new one.
	 */
	static <T extends PooledObject<T>> T get(Class<T> clazz, Object... params) {
		Queue<PooledObject<?>> queue = instance.poolMap.get(clazz);
		T obj;

		// Reinitialize a pooled object
		if (queue != null && (obj = clazz.cast(queue.poll())) != null) {
			obj.init();
			return obj;
		}

		// Create a new object
		try {
			Constructor<?> constr[] = clazz.getConstructors();

			// check for constructors matching the amount of parameters
			for (Constructor<?> constructor : constr) {
				Class<?>[] parameterTypes = constructor.getParameterTypes();
				if (parameterTypes.length == params.length) {
					// Before calling constructor, check if the argument types match too
					boolean allParametersMatch = true;
					for (int i = 0; i < parameterTypes.length; i++) {
						Class<?> parameterType = parameterTypes[i];
						Object arg = params[i];
						if (parameterType.isPrimitive()) {
							// Handle primitive types
							if (!getPrimitiveWrapperClass(parameterType)
									.isAssignableFrom(arg.getClass())) {
								allParametersMatch = false;
								break;
							}
						} else {
							// Handle reference types
							if (!parameterType.isAssignableFrom(arg.getClass())) {
								allParametersMatch = false;
								break;
							}
						}
					}
					// If all parameter types match, use the constructor to init a new object
					if (allParametersMatch) {
						try {
							@SuppressWarnings("unchecked")
							Constructor<T> typedConstructor = (Constructor<T>) constructor;
							instance().daemmon.requested(clazz); // Notify daemon to note type that was created
							return typedConstructor.newInstance(params);
						} catch (InstantiationException | IllegalAccessException
								| InvocationTargetException e) {
							e.printStackTrace();
						}

					}

				}
			}
			throw new RuntimeException("No constructor found for " + clazz.getName()
					+ " with the specified parameters");

		} catch (Exception e) {
			throw new RuntimeException("Failed to create pooled object", e);
		}
	}

	/**
	 * Helper function to get wrapper classes for primitives to help determine
	 * object constructor parameter types
	 * 
	 * @param primitiveType
	 * @return wrapper type
	 */
	private static Class<?> getPrimitiveWrapperClass(Class<?> primitiveType) {
		if (primitiveType == int.class) {
			return Integer.class;
		} else if (primitiveType == double.class) {
			return Double.class;
		} else if (primitiveType == float.class) {
			return Float.class;
		} else if (primitiveType == long.class) {
			return Long.class;
		} else if (primitiveType == short.class) {
			return Short.class;
		} else if (primitiveType == byte.class) {
			return Byte.class;
		} else if (primitiveType == boolean.class) {
			return Boolean.class;
		} else if (primitiveType == char.class) {
			return Character.class;
		} else {
			throw new IllegalArgumentException(
					"Unsupported primitive type: " + primitiveType.getName());
		}
	}

	// ----------------------------------------------------------

	private static void shrinkPoolSizes(int newMaxSize) {
		for (Map.Entry<Class<? extends PooledObject<?>>, CappedBlockingQueue> entry : instance.poolMap
				.entrySet()) {
			CappedBlockingQueue queue = entry.getValue();
			//queue.shrink(newMaxSize);
		}
	}

	private static void expandPoolSized(int newMaxSize) {
		for (Map.Entry<Class<? extends PooledObject<?>>, CappedBlockingQueue> entry : instance.poolMap
				.entrySet()) {
			CappedBlockingQueue queue = entry.getValue();
			//queue.shrink(newMaxSize);
		}
	}

	/**
	 * Purges all objects and object size mappings from the pools
	 */
	private static void purge() {
		instance.poolMap.clear();
		instance.objectPoolSizeMap.clear();
	}

}
