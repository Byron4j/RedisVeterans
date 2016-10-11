package org.byron4j.redis_case.pool;

import java.util.Date;

import org.byron4j.redis_case.App;

/**
 *	@author  	Byron.Y.Y
 *  @optDate 	2016年10月11日
 *  This class is for ...
 */
public class Test3 {

	
	
	public static void main(String[] args) throws Exception{
		System.out.println(App.sdf.format(new Date()) + " - " + JedisUtil.setValue("key1", "val1"));
		System.out.println(App.sdf.format(new Date()) + " - " + JedisUtil.getValue("key1"));
		
		System.out.println(App.sdf.format(new Date()) + " - " + JedisUtil.lpush("listKey1", "listVal1"));
		System.out.println(App.sdf.format(new Date()) + " - " + "Test3结束操作.");
	}
}
