package c0.util.eventbus.events;

import c0.util.objectpool.PooledObject;

public interface Event extends PooledObject<Event> {

	/**
	 * Helper interface to group records which define event subtypes to be grouped
	 * together by
	 */
	interface Type {
		String getName();
	}

	Type getType();

	String getTimestamp();

}
