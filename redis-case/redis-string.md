##**string类型**
###**incr**
####**用法**
***incr*** key，可以将key值原子自增1，并返回递增操作后key对应的新值。如果指定的key不存在，那么在执行incr操作之前，会先将它的值设定为0。
```
/*测试前，清除当前数据库所有key*/
127.0.0.1:6379> flushDB
OK
/*没有key*/
127.0.0.1:6379> keys *
(empty list or set) 
/*使用incr 一个不存在的key，有返回为1（如果指定的key不存在，那么在执行incr操作之前，会先将它的值设定为0，并返回自增后的值1）*/
127.0.0.1:6379> incr incrKey
(integer) 1
127.0.0.1:6379> get incrKey
"1"
/*自增1，返回增加后的值2*/
127.0.0.1:6379> incr incrKey
(integer) 2
127.0.0.1:6379> get incrKey
"2"
```
####**使用场景1 - 计数器**
例如：一个web应用，我们想记录每个用户每天访问这个网站的次数。就可以使用这个用户的id和当天日期拼接一个key，每访问一次只用incr对该key操作，从而获得该用户当天的访问网站次数。比如用户id为9eda3e419e6eadb99293f5c9105816c93a0ca760，今日是20161015，则可以使用incr 9eda3e419e6eadb99293f5c9105816c93a0ca76020161015作为统计该用户在2016-10-15当天的访问次数。
该场景的扩展：统计该用户在某个时间范围之内的访问次数，可以结合incr、expire来达到目标。
####**使用场景2 - 限制访问次数（一）**
假设我们有这样的需求：每个api接口，每秒每个ip的访问次数不能超过10次。
我们可以为ip:时间戳(到秒)设置key，以下使用伪码展示：
```
FUNCTION LIMIT_ACCESS_COUNT(ip)
currSecond = CURRENT_UNIX_TIME()
keyName = ip+":"+currSecond
currentCnt = GET(keyName)
IF currentCnt != NULL AND currentCnt > 10 THEN
    ERROR "一秒内访问次数过多"
ELSE
    MULTI
    	/*比如10.192.168.27在2016-10-15 15:20:19时访问次数不到10，一直自增*/
        INCR(keyName,1)  
        /*计数器每次递增的时候都设置了10秒的过期时间，这样在进入下一秒时，redis会自动删除前一秒的计数器。
		 *  键 10.192.168.27:2016-10-15 15:20:19将会在2016-10-15 15:20:29之后删除      
         */
        EXPIRE(keyName,10)  
    EXEC
    DO_JOB()
END
```
####**使用场景2 - 限制访问次数（二）**
前面例子是每个ip每一秒都生成一个key。在此例中，我们一个ip只会生成一个key，但是实际使用中需要注意竞态条件的出现。
具体思路是：从第一个请求开始设置过期时间为1秒。如果1秒内请求数超过了10个，那么会提示错误信息。到了下一秒，计数器会清零后重新开始计数。
```
FUNCTION LIMIT_ACCESS_COUNT(ip)
keyName = ip
currentCnt = GET(keyName)
IF currentCnt != NULL AND currentCnt > 10 THEN
    ERROR "一秒内访问次数过多"
ELSE
    MULTI
    	/*比如10.192.168.27在2016-10-15 15:20:19时访问次数不到10，一直自增*/
    	currentCnt = INCR(ip)
    	IF currentCnt == 1 THEN
        /*计数器每次递增的时候都设置了1秒的过期时间，只有在第一次访问时才设置超时时间为1秒
		 * 键 10.192.168.27:2016-10-15 15:20:19将会在2016-10-15 15:20:20之后删除      
         */
        EXPIRE(keyName,1)  
    	END
    EXEC
    DO_JOB()
END
```
***处理竞态条件*** ： 使用LUA脚本。
在前面的例子中，如果使用incr后，没有成功执行expire，会导致这个ip的key引起内存泄漏，知道下次有同一个ip发送相同请求过来。可以将可能发生竞态条件的逻辑放在LUA脚本中，再使用eval解决（要求REDIS2.6版本以上）
```
/*LUA脚本*/
local currentCnt
currentCnt = redis.call("incr",KEYS[1])
if tonumber(currentCnt) == 1 then
    redis.call("expire",KEYS[1],1)
end
```

###**getset**
***getset*** key value 会将key设置为key的值，但是返回的是key原来的值。如果key存在但是对应的value不是字符串，就返回错误。如果之前Key不存在将返回nil。
```
127.0.0.1:6379> flushDB
OK
127.0.0.1:6379> keys *
(empty list or set)
/*使用incr实现计数器自增，使用getset可以重置为0*/
127.0.0.1:6379> incr testKey
(integer) 1
127.0.0.1:6379> incr testKey
(integer) 2
127.0.0.1:6379> getset testKey 0
"2"
127.0.0.1:6379> get testKey
"0"

/*key不存在返回nil*/
127.0.0.1:6379> getset testKey2 0
(nil)
```



