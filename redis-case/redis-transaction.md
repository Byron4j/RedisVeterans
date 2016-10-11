##**Redis 中的事务**
使用MULTI与EXEC之间之间执行多个操作来处理一个事务:
```
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379> SADD key element
QUEUED
127.0.0.1:6379> LPUSH helper_key x
QUEUED
127.0.0.1:6379> EXEC
1) (integer) 1
2) (integer) 1
```
**MULTI开启事务(开启成功返回OK)，MULTI与EXEC之间的多个操作仅仅是暂时缓存起来(我们可以看到返回为QUEUED)，直到执行EXEC时，提交事务。**

##**Redis 超时(TTL):数据在限定时间内存活**
Redis可以expire对键设置生命周期TTL：
```
/*先存入数据到list中*/
127.0.0.1:6379> lpush ttlTestList ttl
(integer) 1
/*再设置超时时间（为5秒失效）*/
127.0.0.1:6379> expire ttlTestList 5
(integer) 1
```
5秒后获取元素为空：
```
127.0.0.1:6379> lpop ttlTestList
(nil)
```
/*我们看到超时时间一过，ttlTestList键被删除了*/
127.0.0.1:6379> keys *
1) "zset"
2) "set"
3) "helper_key"
4) "mylist"
5) "user"
6) "mykey"
7) "relList"

上面的例子使用了**EXPIRE**来设置超时时间(也可以**再次调用这个命令来改变超时时间**，使用**PERSIST命令去除超时时间** )。
使用**ttl**:
```
127.0.0.1:6379> lpush ttlTestList ttl
(integer) 1
/*设置超时时间为10秒*/
127.0.0.1:6379> expire ttlTestList 10
(integer) 1
/*使用ttl查看剩余存活时间*/
127.0.0.1:6379> ttl ttlTestList
(integer) 5
```

##**KEYS 命令**
***keys pattern*** 可以查看所在数据库中符合pattern匹配模式的键，pattern通配符：
| 符号 | 含义 |
| ------------- |:-------------:| 
| ? | 匹配一个字符 | 
| * | 匹配任意字符（包括0个字符） | 
| [] | 匹配括号里的任意字符，也可以使用“-”表示范围，如a[b-d]可以匹配ab、ac、ad |
| \x | 可以匹配转义字符。如匹配?，就使用\? |
```
127.0.0.1:6379[1]> keys list?
1) "list1"
```
**注意：**keys会遍历数据库的所有键，如果键数量较大，不建议在生产使用.


##**EXISTS 命令**
***exists*** 用于判断某个键是否存在，存在返回1，否则返回0.
```
127.0.0.1:6379[15]> keys *
1) "testKey"
127.0.0.1:6379[15]> exists testKey
(integer) 1
127.0.0.1:6379[15]> exists testKey2
(integer) 0
```


##**DEL 命令**
***del*** 用于删除一个或多个键，del key [key2...],返回值是删除的键的个数：
```
127.0.0.1:6379[15]> del testKey
(integer) 1
/*因为之前已经删除testKey键了，再使用del操作时，实际上并没有删除任何键，于是返回0*/
127.0.0.1:6379[15]> del testKey
(integer) 0
```

##**SELECT 命令**
Redis 的数据库默认存在编号为0-15的共计16个数据库实例，可以通过设置databases参数修改这个默认设置。我们可以使用***SELECT***命令选择使用指定的数据库：
```
/*选择使用数据库1*/
127.0.0.1:6379[1]> select 1
OK
/*在数据库1中设置一个键*/
127.0.0.1:6379[1]> lpush list1 ele1
(integer) 1
127.0.0.1:6379[1]> keys *
1) "list1"
```
切换数据库0，在数据库0中是看不到数据库1中的键的：
```
/*切换到数据库0*/
127.0.0.1:6379[1]> select 0
OK
/*数据库0中无键*/
127.0.0.1:6379> keys *
(empty list or set)
```

##**FLUSHALL 命令**
***flushall*** 命令会清除所有数据库实例的键值数据(默认配置的话数据库0-15会全删除)。恨他，就用flushall来对付他吧。

##**TYPE 命令**
***type*** 命令用来获取键的类型，返回值可能是 **string(字符串类型)**，**list(列表类型)**，**set(集合类型)**，**hash(散列类型)**，**zset(有序集合类型)**