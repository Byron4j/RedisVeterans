package org.byron4j.redis_case.pool;

import redis.clients.jedis.JedisPubSub;

/**
 *	@author  	Byron.Y.Y
 *  @optDate 	2016年10月17日
 *  This class is for ...
 */
public class SubPubTest {

	public static void main(String[] args) {
		JedisUtil.subscribe(new JedisPubSub() {
		}, "foo");
	}
}
