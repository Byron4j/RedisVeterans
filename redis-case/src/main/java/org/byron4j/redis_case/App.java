package org.byron4j.redis_case;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import redis.clients.jedis.Jedis;

/**
 * Hello world!
 *
 */
public class App 
{
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void main( String[] args ) throws Throwable
    {
    	Jedis jedis = new Jedis("localhost");
    	
    	/**
    	 * 
    	 */
    	System.out.println(sdf.format(new Date()) + " - " + "清空当前数据库实例全部键：" + jedis.flushDB());
    	
    	/**
    	 * 对字符串类型操作
    	 */
    	System.out.println(sdf.format(new Date()) + " - " +"字符串类型set的返回：" + jedis.set("foo", "bar"));
    	String value = jedis.get("foo");
    	System.out.println( sdf.format(new Date()) + " - " +"使用get获取字符串键值：" + value);
    	
    	/**
    	 * 对列表类型操作
    	 * 		-- 将val1放到对头，再将val2放到对头，这时候val1到了队尾，rpop获取队尾元素得到val1.
    	 */
    	System.out.println(sdf.format(new Date()) + " - " +"lpush往列表头插入一个元素：" + jedis.lpush("code_list1", "val1"));
    	System.out.println(sdf.format(new Date()) + " - " +"lpush往列表头插入一个元素：" + jedis.lpush("code_list1", "val2"));
    	System.out.println(sdf.format(new Date()) + " - " +"lpush往列表尾取出一个元素：" + jedis.rpop("code_list1"));
    	
    	
    	/**
    	 * 设置超时
    	 */
    	System.out.println(sdf.format(new Date()) + " - " +"lpush往列表头插入一个元素：" + jedis.lpush("ttl_list1", "val1"));
    	System.out.println(sdf.format(new Date()) + " - " +"设置超时时间10秒：" + jedis.expire("ttl_list1", 2));
    	System.out.println(sdf.format(new Date()) + " - " +"停顿3秒后...");
    	Thread.sleep(1000);
    	System.out.println(sdf.format(new Date()) + " - " +"查看剩余存活时间：" + jedis.ttl("ttl_list1"));
    	System.out.println(sdf.format(new Date()) + " - " +"查看数据库中的键:" + jedis.keys("*"));
    	System.out.println(sdf.format(new Date()) + " - " +"再停顿7秒...");
    	Thread.sleep(1000);
    	System.out.println(sdf.format(new Date()) + " - " +"查看数据库中的键:" + jedis.keys("*"));
    	
    	
    	String script = "local food=redis.pcall('set','str','food');"
                + "redis.call('get','str');";
    	
    	// 3.执行脚本
    	jedis.eval(script, 1, "100");
    	System.out.println(jedis.get("LOCK:110033:201611290101"));
    	Long lockResult = jedis.setnx("LOCK:110033:201611290101", "201611290101");
    	System.out.println("lockResult:" + lockResult);
    	System.out.println(jedis.setnx("LOCK:110033:201611290101", "201611290102"));
    	System.out.println(jedis.get("LOCK:110033:201611290101"));
    	jedis.del("LOCK:110033:201611290101");
    	
    	System.out.println(jedis.get("ss"));
    }
}
