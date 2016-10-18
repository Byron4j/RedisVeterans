##**Redis配置相关的命令**
相关命令包括服务配置、读写数据到磁盘、获取服务器信息、同步复制数据、异步保存数据到磁盘等涉及redis整个服务的操作。示例Redis版本V2.8.2。


-  FILE命令
	-  SAVE 
	-  BGSAVE
	-  LASTSAVE

-  CLIENT命令
	-  CLIENT LIST
	-  CLIENT GETNAME
	-  CLIENT SETNAME
	-  CLIENT KILL ip:port
	-  CLIENT PAUSE timeout-- since V2.9.50

-  CONFIG命令
	-  CONFIG GET args
	-  CONFIG RESETSTAT

-  DEBUG命令
	-  DEBUG OBJECT key
	-  DEBUG SEGFAULT -- 模拟一个错误

-  LOG命令
	-  SLOWLOG ```[GET|RESET|LEN] [args]```

-  INFO命令
	-  INFO ```[section]```
	-  TIME
	-  DBSIZE
	-  KEYS pattern
	
-  SERVER
	-  SHUTDOWN
	-  MONITOR
	-  SLAVEOF host port
	-  SYNC
	-  FLUSHDB
	-  FLUSHALL

###**FILE命令**	
####**save**
**<code>SAVE</code>** 命令执行一个同步操作，以RDB文件的方式保存所有数据的快照 很少在生产环境直接使用SAVE 命令，因为它会阻塞所有的客户端的请求，可以使用**<code>BGSAVE</code>** 命令代替。如果在BGSAVE命令的保存数据的子进程发生错误的时,用 SAVE命令保存最新的数据是最后的手段。命令成功返回OK。
```
127.0.0.1:6379> save
OK
```
在客户端连接中执行save命令后，在redis根目录下生成了rdb文件dump.rdb。

####**bgsave、lastsave**
**<code>BGSAVE</code>** 命令执行异步操作，后台保存DB数据，不会堵塞客户端请求。会立即返回 OK 状态码。 Redis forks, 父进程继续提供服务以供客户端调用，子进程将DB数据保存到磁盘然后退出。如果操作成功，可以通过客户端命令**<code>LASTSAVE</code>** 来检查操作结果。LASTSAVE 命令执行成功时返回UNIX时间戳。客户端执行 BGSAVE 命令时，可以通过每N秒发送一个 LASTSAVE 命令来查看BGSAVE 命令执行的结果，由 LASTSAVE 返回结果的变化可以判断执行结果。
```
127.0.0.1:6379> bgsave
Background saving started
127.0.0.1:6379> lastsave
(integer) 1476685203
```

###**CLIENT命令**
####**client list**
**<code>CLIENT LIST</code>** 命令显示当前所有客户端连接信息。
```
127.0.0.1:6379> client list
id=3 addr=127.0.0.1:58578 fd=10 name= age=1553 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client
id=4 addr=127.0.0.1:58580 fd=11 name= age=1551 idle=5 flags=O db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=56 oll=0 omem=0 events=rw cmd=monitor
id=159 addr=127.0.0.1:59737 fd=14 name= age=5 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=10 oll=0 omem=0 events=rw cmd=replconf
```

####**client getname**
**<code>CLIENT GETNAME</code>** 命令显示当前客户端连接的名称，没有设置则返回nil。
```
127.0.0.1:6379> client getname
(nil)
```

####**client setname**
**<code>CLIENT SETNAME</code>** 命令设置当前客户端连接的名称，成功返回OK。
```
127.0.0.1:6379> client setname 'conn1'
OK
(3.69s)
127.0.0.1:6379> client getname
"conn1"
(1.84s)
```

####**client kill ip:port**
**<code>CLIENT KILL ip:port</code>** 命令关闭指定ip和端口的连接。2.8.12开始改为这种格式:```CLIENT KILL <filter> <value> ... ... <filter> <value>```,可以查看[Redis中文网](http://www.redis.cn/commands/client-kill.html),获得更多信息。
```
127.0.0.1:6379> client list
id=3 addr=127.0.0.1:58578 fd=10 name=conn1 age=2232 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client
id=4 addr=127.0.0.1:58580 fd=11 name= age=2230 idle=5 flags=O db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=56 oll=0 omem=0 events=rw cmd=monitor
id=274 addr=127.0.0.1:60194 fd=14 name= age=5 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=10 oll=0 omem=0 events=rw cmd=replconf
(3.90s)
127.0.0.1:6379> client kill 127.0.0.1:58578
OK
```

####**client pause timeout**
**<code>CLIENT PAUSE timeout</code>** 命令使当前客户端连接暂停挂起指定的timeout毫秒。**V2.9.5** 才有的命令。

###**CONFIG命令**
####**CONFIG GET args**
**<code>CONFIG GET args</code>** 命令获取指定参数args名的值。
```
127.0.0.1:6379> config get port
1) "port"
2) "6379"
```

####**CONFIG RESETSTAT**
**<code>config resetstat</code>** 命令重置INFO命令统计里面的一些计算器。被重置的数据如下:
```
Keyspace hits
Keyspace misses
Number of commands processed
Number of connections received
Number of expired keys
```
操作成功则返回OK：
```
127.0.0.1:6379> config resetstat
OK
```


###**DEBUG命令**
####**DEBUG OBJECT key **
**<code>DEBUG OBJECT key</code>** 命令获取一个key的debug信息。
在测试该命令的之前，笔者使用了```slaveof 127.0.0.1 6379```将当前作为从服务器，则添加key时，会提示错误（slave不允许写操作），需要关闭服务之后重新启动，使用role命令返回master即可。
**debug object key** 会返回一些信息：key值所在位置、引用次数、编码、序列化长度、LRU算法
```
127.0.0.1:6379> debug object testKey
Value at:00007FF405231A70 refcount:1 encoding:raw serializedlength:5 lru:0 lru_seconds_idle:363238
```


####**DEBUG SEGFAULT**
**<code>debug segfault</code>** 命令模拟服务崩溃指令，导致服务关闭。

###**LOG命令**
####**slowlog**
**<code>SLOWLOG subcommond [args]</code>** 命令处理redis慢查询。**subcommond** 可选的包括：**get**、**reset**、**len**
```
//提示slowlog的参数不正确
127.0.0.1:6379> slowlog
(error) ERR wrong number of arguments for 'slowlog' command

/*提示slowlog的子命令只能是get、reset、len其中的一个。*/
127.0.0.1:6379> slowlog slowthan 1
(error) ERR Unknown SLOWLOG subcommand or wrong # of args. Try GET, RESET, LEN.

//显示是慢查询的命令，只查看一条。可以使用slowlog get N 查看N条慢查询命令。
127.0.0.1:6379> slowlog get 1
1) 1) (integer) 8
   2) (integer) 1476686813
   3) (integer) 15174
   4) 1) "slowlog"
      2) "slowthan"
      3) "1"
      
//使用slowlog len 可以查看慢查询的总记录条数
127.0.0.1:6379> slowlog len
(integer) 9
```

**redis.conf** 文件可以查看关于慢查询的相关配置。你可以通过两个参数配置redis慢查询：

-  slowlog-log-slower-than 10000 : 单位微s，默认是10ms=10000；超过10ms则认为是慢查询，会记录到慢查询日志中。

-  slowlog-max-len 128 : 指定慢查询的记录数长度，最多记录128条。





###**INFO命令**
####**info**
***INFO [section]*** 获取服务器详细信息，如果指定section如CPU，则展示指定section的配置信息。

-  不指定section，获取全部信息:
```
127.0.0.1:6379> info
# Server
redis_version:2.8.2400
redis_git_sha1:00000000
redis_git_dirty:0
redis_build_id:5b5b8d41b2049c5
redis_mode:standalone
os:Windows
arch_bits:64
multiplexing_api:winsock_IOCP
process_id:8032
run_id:05cf71acbc3dda05c64cca17fb5016ed5631ebf0
tcp_port:6379
uptime_in_seconds:174786
uptime_in_days:2
hz:10
lru_clock:292884
config_file:

# Clients
connected_clients:4
client_longest_output_list:0
client_biggest_input_buf:0
blocked_clients:0

# Memory
used_memory:1005328
used_memory_human:981.77K
used_memory_rss:971784
used_memory_peak:15353928
used_memory_peak_human:14.64M
used_memory_lua:36864
mem_fragmentation_ratio:0.97
mem_allocator:dlmalloc-2.8

# Persistence
loading:0
rdb_changes_since_last_save:0
rdb_bgsave_in_progress:0
rdb_last_save_time:1476685203
rdb_last_bgsave_status:ok
rdb_last_bgsave_time_sec:1
rdb_current_bgsave_time_sec:-1
aof_enabled:0
aof_rewrite_in_progress:0
aof_rewrite_scheduled:0
aof_last_rewrite_time_sec:-1
aof_current_rewrite_time_sec:-1
aof_last_bgrewrite_status:ok
aof_last_write_status:ok

# Stats
total_connections_received:7
total_commands_processed:59
instantaneous_ops_per_sec:0
total_net_input_bytes:1935
total_net_output_bytes:0
instantaneous_input_kbps:0.00
instantaneous_output_kbps:0.00
rejected_connections:0
sync_full:0
sync_partial_ok:0
sync_partial_err:0
expired_keys:0
evicted_keys:0
keyspace_hits:8
keyspace_misses:1
pubsub_channels:2
pubsub_patterns:0
latest_fork_usec:85610

# Replication
role:master
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0

# CPU
used_cpu_sys:0.81
used_cpu_user:0.41
used_cpu_sys_children:0.00
used_cpu_user_children:0.00

# Keyspace
db0:keys=4,expires=0,avg_ttl=0
```

-  指定section:
```
127.0.0.1:6379> info cpu
# CPU
used_cpu_sys:0.81
used_cpu_user:0.41
used_cpu_sys_children:0.00
used_cpu_user_children:0.00
```


####**time**
**<code>TIME</code>** 命令获取服务器当前时间，返回当前Unix时间戳和这一秒已经过去的微秒数2个值。
-  UNIX时间戳（单位：秒）
-  微秒
```
127.0.0.1:6379> time
1) "1476685454"
2) "587363"
...
127.0.0.1:6379> time
1) "1476685598"
2) "244845"
```


####**dbsize**
***dbsize*** 获取当前数据库中keys的数量。
```
127.0.0.1:6379> dbsize
(integer) 4
```

####**keys pattern**
***keys pattern*** 返回匹配pattern模式的key。
```
127.0.0.1:6379> keys *
1) "testKey"
127.0.0.1:6379> lpush listKey 'val1'
(integer) 1
127.0.0.1:6379> keys *
1) "testKey"
2) "listKey"
127.0.0.1:6379> keys t*
1) "testKey"
```

###**SERVER命令**
####**SHUTDOWN**
***shutdown*** 会关闭当前连接的Redis服务器。

####**MONITOR**
***monitor*** 是一个调试命令，返回服务器处理的每一个命令，它能帮助我们了解在数据库上发生了什么操作，可以通过redis-cli和telnet命令使用。
```
/*客户端1：监听*/
127.0.0.1:6379> monitor
OK

/*客户端2*/
127.0.0.1:6379> keys *
1) "testKey"
2) "listKey"
127.0.0.1:6379> client getname
(nil)

/*客户端2执行命令时，可以看到监听客户端1的输出信息：*/
1476759059.765371 [0 127.0.0.1:58776] "keys" "*"
1476759066.213351 [0 127.0.0.1:58776] "client" "getname"
```

####**SLAVEOF host port**
***slaveof host port*** 会指定当前服务器作为指定host的slave。
```
> slaveof 127.0.0.1 6379
> OK
```

####**SYNC**
***sync*** 用于复制的内部命令。
 
####**FLUSHDB**
***flushdb*** 命令会清除当前数据库实例的所有key。

####**FLUSHALL**
***flushall*** 命令会清除当前服务器所有数据库实例的所有key。