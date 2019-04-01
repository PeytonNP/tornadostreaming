package edu.purdue.cs.tornado.tutorial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.storm.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.purdue.cs.tornado.SpatioTextualToplogyBuilder;
import edu.purdue.cs.tornado.helper.PartitionsHelper;
import edu.purdue.cs.tornado.helper.SpatioTextualConstants;
import edu.purdue.cs.tornado.loadbalance.Cell;
import edu.purdue.cs.tornado.spouts.KafkaSpout;


public class TornadoUITopology {
	private static final Logger LOGGER = LoggerFactory.getLogger(TornadoUITopology.class);
	// Edit path files as needed
	private static final String PROJECT_DIR = System.getProperty("user.dir");	
	private static final String DATASOURCES_DIR = System.getProperty("user.dir") + "/datasources/";
	private static final String SAMPLE_TWEETS_FILE_PATH = DATASOURCES_DIR + "sample_tweets.csv";
	private static final String TWEETS_FILE_PATH = DATASOURCES_DIR + "twitterdata.csv";
	private static final String TEMP_FILE_PATH = DATASOURCES_DIR + "temp.csv";
	
	public static String javaArgs = "-Xmx3g -Xms3g"; // -Dcom.sun.management.jmxremote  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -javaagent:/home/staticdata/CustomAgent%ID%.jar ";
	public static String tweetsSource = "Tweets";
	public static String querySource  = "querySource";
	public static String topologyName = "TornadoUI";
	
	
	private static KafkaConsumer<String, String> consumer; //static ConsumerConnector consumer;  
	private static Producer<String, String> producer;
	private static String zookeeper;
	private static String topic;
	private static ArrayList<String> subscriptionTopics = new ArrayList<String>();
			
	public static void main(String[] args) {
		System.out.println("Project Directory : " + PROJECT_DIR);
		System.out.println("Datasources Directory : " + DATASOURCES_DIR);
		
		final Properties properties = new Properties();
		try {
			LOGGER.info("**********************Tornado UI Topology************************");
			LOGGER.info("**********************Reading toplogy config******************");
			properties.load(new FileInputStream(SpatioTextualConstants.CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			// Should not occur. If it does, we can't continue. So exiting the program!
			LOGGER.error(ioException.getMessage(), ioException);
			System.exit(1);
		}
	
		
		setupConsumer();
		setupProducer();
		//consumeQueriesFromUI();
		sendQueryToProducer();
	}
	
	/* FROM TORNADOTWEETCOUNTEXAMPLE.JAVA
	 * Find and load the properties available in the given file path
	 * 
	 * @param filepath the location of properties to be loaded
	 * @return Properties object with the loaded properties at the specified file path
	 */
	static Properties loadProperties(String filepath) {
		//Create and load a new properties object
		final Properties properties = new Properties();
		try {
			LOGGER.info("******************************************************************");
			LOGGER.info("**********************Reading toplogy config******************");
			properties.load(new FileInputStream(filepath));
		} catch (final IOException ioException) {
			//Should not occur. If it does, we can't continue. So we're exiting the program!
			LOGGER.error(ioException.getMessage(), ioException);
			System.exit(1);
		}
		
		return properties;
	}

	
	public static void setupConsumer() {
		consumer = new KafkaConsumer<>(createConsumerConfigProps(zookeeper, topic));
		subscriptionTopics.add("queries");
		subscriptionTopics.add("output");
		consumer.subscribe(subscriptionTopics);	// consume from topics: queries, output, etc
	}
	
	public static void setupProducer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producer = new KafkaProducer<>(props);
	}
	
	
	
	public static void readSampleTweetFiles(String filename) {
		
	}
	
	public static void consumeQueriesFromUI() {
		// Print the records that are consumed from the topics denoted above
		int consumerCount = 0;
	     //while (consumerCount == 0) {
	         ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
	         for (ConsumerRecord<String, String> record : records) {
	             System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
	             consumerCount++;
	         }
	     //}
	}
	
	public static void processUIQueries() {
		
	}
	
	public static void sendQueryToProducer() {
		
		// Test text to send to producer
		for (int i = 0; i < 10; i++) {
			String topic = "output";
			String key = "key";
			String value = "TornadoUITopology Test value = " + Integer.toString(i + 1);
			producer.send(new ProducerRecord<String, String>(topic, key, value));
		}
		
		producer.close();
	}
	
	/**
	 * 3/30/2019 - Slightly modified function similar to createConsumerConfig(String, String)
	 * 
	 * @param a_zookeeper
	 * @param a_groupId
	 * @return Properties object 
	 */
	public static Properties createConsumerConfigProps(String a_zookeeper, String a_groupId) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "queries");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
	}
}
