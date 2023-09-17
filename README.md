# Kafka streams using kstreams library

- Kafka streams used to build a Topology and Sub Topology of data transformations 
like aggregations, KStream, KTable, state of the data etc
# Kafka Console Producer, and Console Consumer 

-    Prereq: Docker, DockerCompose, IntelliJ, Java 8+, Here i use JDK 18/20
-    And or Any Local kafka broker running
-    Start your confluent kafka docker (see under resources)
-     [admin@fedora37 ~]$ docker-compose -f confluent-kafka.yml up 
-    Create a topic (see under resources/kafka-help.txt)
-    Receive data from input topic(dataIn4), transform , send to the output topic(dataOut)


