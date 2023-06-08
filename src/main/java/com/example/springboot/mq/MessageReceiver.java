package com.example.springboot.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.config.SimpleJmsListenerEndpointId;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQConnectionFactory;

import java.util.List;

@Component
@EnableJms
public class MessageReceiver implements ApplicationRunner {

    private final JmsListenerEndpointRegistry registry;
    private final MQConnectionFactory connectionFactory;
    private final List<QueueConfiguration> queueConfigurations;

    @Autowired
    public MessageReceiver(JmsListenerEndpointRegistry registry, MQConnectionFactory connectionFactory,
                           List<QueueConfiguration> queueConfigurations) {
        this.registry = registry;
        this.connectionFactory = connectionFactory;
        this.queueConfigurations = queueConfigurations;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (queueConfigurations.isEmpty()) {
            System.out.println("Listening to 0 queues at the moment.");
        } else {
            System.out.println("Listening to the following queues:");
            for (QueueConfiguration queueConfig : queueConfigurations) {
                System.out.println(" - Queue Name: " + queueConfig.getQueueName());
                System.out.println("   Port Number: " + queueConfig.getPortNumber());
                System.out.println("   Queue Manager: " + queueConfig.getQueueManager());
                System.out.println("   Channel: " + queueConfig.getChannel());
                System.out.println("   Username: " + queueConfig.getUsername());
                System.out.println("   Password: " + queueConfig.getPassword());
                System.out.println("   Receive Timeout: " + queueConfig.getReceiveTimeout());
                System.out.println("   Receive Concurrency: " + queueConfig.getReceiveConcurrency());
                System.out.println("   SSL Enabled: " + queueConfig.isSslEnabled());
                System.out.println("   Additional Details: " + queueConfig.getAdditionalDetails());

                try {
                    // Create a JMS Queue object using the queue name
                    Queue queue = new MQQueue(queueConfig.getQueueName());

                    // Configure the properties for the queue
                    connectionFactory.setTransportType(1); // 1 for Client mode
                    connectionFactory.setQueueManager(queueConfig.getQueueManager());
                    connectionFactory.setChannel(queueConfig.getChannel());
                    connectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
                    connectionFactory.setStringProperty(WMQConstants.USERID, queueConfig.getUsername());
                    connectionFactory.setStringProperty(WMQConstants.PASSWORD, queueConfig.getPassword());
                    connectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
                    connectionFactory.setBooleanProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE_RESET, true);

                    if (queueConfig.isSslEnabled()) {
                        // Set SSL properties if enabled
                        connectionFactory.setBooleanProperty(WMQConstants.WMQ_SSL_ENABLED, true);
                        connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, queueConfig.getCipherSuite());
                        connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_PEER_NAME, queueConfig.getPeerName());
                        connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_KEYSTORE, queueConfig.getKeystore());
                        connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_KEYSTORE_PASSWORD, queueConfig.getKeystorePassword());
                    }

                    // Create a JmsListenerEndpoint for each queue and register it
                    SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
                    endpoint.setId(new SimpleJmsListenerEndpointId(queueConfig.getQueueName()));
                    endpoint.setDestination(queue);
                    endpoint.setMessageListener(message -> {
                        // Handle the received message asynchronously
                        System.out.println("Received message from " + queueConfig.getQueueName() + ": " + message);
                    });

                    SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
                    factory.setConnectionFactory(connectionFactory);

                    registry.registerListenerContainer(endpoint, factory);
                } catch (JMSException e) {
                    // Handle any exceptions
                    e.printStackTrace();
                }
            }
        }
    }

    @JmsListener(destination = "your-queue-name") // Placeholder JmsListener to satisfy annotation scanning
    public void dummyListener(String message) {
        // This method is not used; it is just a placeholder for annotation scanning
    }
}
