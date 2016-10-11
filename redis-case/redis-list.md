##**Redis-list学习笔记**
Redis中使用<code>keys *</code>可以查看所有存在的键。 
####**lpush、rpush**
***lpush***可以向指定的list左边（头部）添加新元素,并返回添加的元素个数
***rpush***可以向指定的list右边（尾部）添加新元素,并返回添加的元素个数
```
127.0.0.1:6379> lpush relList oyy
(integer) 1
127.0.0.1:6379> keys *
1) "zset"
2) "set"
3) "mylist"
4) "user"
5) "mykey"
6) "relList"
```
也可以一次性向list中存入多个值:
```
127.0.0.1:6379> rpush relList zyy yiyi yoyo ynyn
(integer) 5
```
####**lrange**
***lrange***可以获取list中指定索引范围内的元素，它有两个参数start、end分别表示开始、结束索引位置，负数表示从尾部数起，如-1表示倒数第一个元素的位置。获取全部元素：
```
127.0.0.1:6379> lrange relList 0 -1
1) "oyy"
2) "zyy"
3) "yiyi"
4) "yoyo"
5) "ynyn"
```

####**lpop、rpop**
与插入相对的，有取出操作，pop操作会从list中删除元素并同时返回删除的值。可以在左边(lpop)或右边(rpop)操作。

####**lindex、llen**
***lindex***可以获取指定索引位置的元素：
```
127.0.0.1:6379> lindex relList 0
"oyy"
```

***llen***可以获得list的元素个数：
```
127.0.0.1:6379> llen relList
(integer) 5
```

***获取不存在的list的元素、获取空list的元素返回NULL(nil)***
```
127.0.0.1:6379> keys *
1) "zset"
2) "set"
3) "mylist"
4) "user"
5) "mykey"
6) "relList"
127.0.0.1:6379> lpop popList
(nil)
```

####**ltrim**
***ltrim***可以将指定的list截取成为指定索引范围的子集：
```
127.0.0.1:6379> lrange relList 0 -1
1) "oyy"
2) "zyy"
3) "yiyi"
4) "yoyo"
5) "ynyn"
127.0.0.1:6379> ltrim relList 0 2
OK
127.0.0.1:6379> lrange relList 0 -1
1) "oyy"
2) "zyy"
3) "yiyi"
```

###**list的阻塞行为**
**可以使用list实现生产-消费者模式**，如果使用lpush、rpop在队列头部插入元素、队列尾部取出元素。当元素取完时，返回空，就需要使用轮询机制来获取下一次的元素，这会**加大内存消耗，增加redis的访问压力、增加消费端的cpu时间，而很多访问都是无用的**。为此，Redis为**获取list元素**提供了阻塞式访问**blpop、brpop**命令。消费者可以在获取数据时指定超时时间，指定时间范围内获得数据立即返回，超时未获得数据则返回空，超时参数设置为0则表示一直阻塞。
***blpop*** 当blpop调用时，如果给定 key 内至少有一个非空列表，那么弹出遇到的第一个非空列表的头元素，并和被弹出元素所属的列表的名字 key 一起，组成结果返回给调用者。如果所有给定 key 都不存在或包含空列表，那么 BLPOP 命令将阻塞连接， 直到有另一个客户端对给定的这些 key 的任意一个执行 LPUSH 或 RPUSH 命令为止。
一旦有新的数据出现在其中一个列表里，那么这个命令会解除阻塞状态，并且返回 key 和弹出的元素值。
**BLPOP key [key ...] timeout**
```
/*client1: thisList不存在，使用blpop一直阻塞*/
127.0.0.1:6379> lpop thisList
(nil)
127.0.0.1:6379> blpop thisList 0

```
再开启一个客户端，使用lpush命令插入一个元素：
```
/*client2*/
127.0.0.1:6379> lpush thisList oyy
(integer) 1
```
第一个客户端马上获得数据并返回:
```
/*client1: 会将key也一同返回*/
127.0.0.1:6379> blpop thisList 0
1) "thisList"
2) "oyy"
```
***timeout 参数表示的是一个指定阻塞的最大秒数的整型值。当 timeout 为 0 是表示阻塞时间无限制。*** 

