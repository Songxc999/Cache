package com.sxc.cachetest.cache;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Songxc
 * @Date: 2019/6/6 10:55
 * @Version 1.0
 */
public class Cache {

	private final static HashMap<String, Entity> map = new HashMap<>();

	private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


	public synchronized static void put(String key, Object data) {
		Cache.put(key, data, 0);

	}

	public synchronized static void put(String key, Object data, long expire) {
		Cache.remove(key);
		if (expire > 0) {
			Future future = executor.schedule(() -> {
				synchronized (Cache.class) {
					map.remove(key);
				}
			}, expire, TimeUnit.SECONDS);
			map.put(key, new Entity(data, future));
		} else {

			map.put(key, new Entity(data, null));
		}


	}

	public synchronized static Object get(String key) {
		Entity entity = map.get(key);
		return entity == null ? null : entity.getValue();

	}

	public synchronized static <T> T get(String key, Class<T> clazz) {
		//强制类型转换
		return clazz.cast(Cache.get(key));

	}

	public synchronized static Object remove(String key) {
		Entity entity = map.remove(key);
		if (entity == null) return null;
		//清除原键值对定时器
		Future future = entity.getFuture();
		if (future != null) future.cancel(true);
		return entity.getValue();
	}

	public synchronized static int size() {
		return map.size();
	}


	private static class Entity {

		private Object value;

		private Future future;

		public Entity(Object value, Future future) {
			this.value = value;
			this.future = future;
		}

		public Object getValue() {
			return value;
		}

		public Future getFuture() {
			return future;
		}

	}


}

