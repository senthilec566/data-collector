## What is μicroHttp?
  Small light weight JVM based http(s) server build on top of Netty Engine

## How it works ?
   μicroHttp Server accepts events from multiple sources , validates , process and respond to clients
   

## Supported DataStores 
   It supports Druid , Kafka and Cassandra Store 

## Benchmarking Result (Kafka) :

    ./wrk -c 2000 -d 5m  -t 500  -s scripts/post_data.lua  -R 1000000 http://198.18.134.13:8009/
    Running 5m test @ http://198.18.134.13:8009/
	  500 threads and 2000 connections
		Thread Stats   Avg      Stdev     Max   +/- Stdev
		    Latency    25.97s    10.52s   45.19s    57.73%
		    Req/Sec   484.35     18.23   538.00     70.95%
		  14144814 requests in 1.00m, 1.25GB read
		Requests/sec: 236353.17
		Transfer/sec:     21.41MB

## Benchmark Comparison :

	https://www.linkedin.com/pulse/netty-high-performance-jvm-based-http-server-senthilkumar-k/

## How to Build this ?
    git clone ssh://git@git.source.akamai.com:7999/perfan/microhttp.git
    cd microhttp
    mvn clean install assembly:single

  You will find executable jar in target folder.

## Starting Server 

      java  -cp microHttp-0.0.1.jar com.akamai.perfan.datastream.sink.DataCollector -p 8009 -t kafka -conf /microhttp/kafka.properties &
      -p port  , t type ( kafka or cassandra ) -conf Kafka or Cassandra properties
      
      kafka.properties
     	bootstrap.servers=<<Servers>
		acks=1
		retries=1
		batch.size=16384
		linger.ms=0
		buffer.memory=33554432
		key.serializer=org.apache.kafka.common.serialization.LongSerializer
		value.serializer=org.apache.kafka.common.serialization.StringSerializer
		topic=<<topic> 

	  cassandra.properties
		hostname=localhost
		port=9042
		keyspace=datastream
		table=ds_logs
		
		recommendation :  set proper memory for this server
			nohup java -Xmx4g -Xms4g -XX:MetaspaceSize=96m -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50 -XX:MaxMetaspaceFreeRatio=80 -cp microHttp-0.0.1.jar com.akamai.perfan.datastream.sink.DataCollector -p 8009 -t kafka -conf /microhttp/kafka.properties
