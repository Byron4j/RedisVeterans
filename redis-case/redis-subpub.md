##**redis publish/subscribe(发布/订阅)模式**
***publish/subscribe*** 是一种消息接收模式，一个消息发布者，可以有很多消息消费者（订阅）接收消息.
更多详细关于发布/订阅模式的讲解，可以参考笔者的译文:[Rabbirmq JAVA编程（三） Publish/Subscribe(发布/订阅)](http://blog.csdn.net/zixiao217/article/details/52732924)
现在走一遍整个订阅/发布流程：
更多精彩请阅读 东陆之滇的csdn博客：http://blog.csdn.net/zixiao217
###订阅者客户端**subscriber1**：第一个客户端订阅了来自两个通道(key)——foo、bar的消息，会等待发布者发布消息。
```
/*19:00订阅消息*/
127.0.0.1:6379> subscribe foo bar
Reading messages... (press Ctrl-C to quit)
1) "subscribe"
2) "foo"
3) (integer) 1
1) "subscribe"
2) "bar"
3) (integer) 2
```

###消息发布者客户端**publisher**，19:05开始发布消息：
```
/*这是发布消息的客户端，开始发布消息，目前一共有一个订阅者 —— subscriber1*/
127.0.0.1:6379> publish foo haha
(integer) 1
127.0.0.1:6379> publish foo hehe
(integer) 1
127.0.0.1:6379> publish foo yeah
(integer) 1
127.0.0.1:6379> publish foo nice
(integer) 1
127.0.0.1:6379> publish foo gg
(integer) 1
127.0.0.1:6379> publish foo DK
(integer) 1
127.0.0.1:6379> publish foo SA
(integer) 1
127.0.0.1:6379> publish foo DOTA
(integer) 1
127.0.0.1:6379> publish foo 11GAME
(integer) 1
127.0.0.1:6379> publish foo SOLO
(integer) 1
```

###**subscriber1**在19:05开始接收**publisher**发布的消息:
```
/*subscriber1在19:05开始接收到消息*/
1) "message"
2) "foo"
3) "haha"
1) "message"
2) "foo"
3) "hehe"
1) "message"
2) "foo"
3) "yeah"
1) "message"
2) "foo"
3) "nice"
1) "message"
2) "foo"
3) "gg"
1) "message"
2) "foo"
3) "DK"
1) "message"
2) "foo"
3) "SA"
1) "message"
2) "foo"
3) "DOTA"
1) "message"
2) "foo"
3) "11GAME"
1) "message"
2) "foo"
3) "SOLO"
```

###19:10又有一个客户端**subscriber2**订阅了foo的消息
```
127.0.0.1:6379> subscribe foo
Reading messages... (press Ctrl-C to quit)
1) "subscribe"
2) "foo"
3) (integer) 1
```


###19:30发布者**publisher**发布消息，发现已经有2个订阅者了：
```
127.0.0.1:6379> publish foo SK
(integer) 2  //2个订阅者
```

###19:30订阅者客户端**subscriber1**接收到消息：
```
1) "message"
2) "foo"
3) "SK"
```
###19:30订阅者客户端**subscriber2**接收到消息：
1) "message"
2) "foo"
3) "SK"

