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
import java.util.List;

@Component
@EnableJms
public class MessageReceiver implements ApplicationRunner {

    private final JmsListenerEndpointRegistry registry;
    private final ConnectionFactory connectionFactory;
    private final List<QueueConfiguration> queueConfigurations;

    @Autowired
    public MessageReceiver(JmsListenerEndpointRegistry registry, ConnectionFactory connectionFactory,
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
                System.out.println("   Additional Details: " + queueConfig.getAdditionalDetails());

                // Create a JmsListenerEndpoint for each queue and register it
                SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
                endpoint.setId(new SimpleJmsListenerEndpointId(queueConfig.getQueueName()));
                endpoint.setDestination(queueConfig.getQueueName());
                endpoint.setMessageListener(message -> {
                    // Handle the received message asynchronously
                    System.out.println("Received message from " + queueConfig.getQueueName() + ": " + message);
                });

                SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
                factory.setConnectionFactory(connectionFactory);

                registry.registerListenerContainer(endpoint, factory);
            }
        }
    }

    @JmsListener(destination = "your-queue-name") // Placeholder JmsListener to satisfy annotation scanning
    public void dummyListener(String message) {
        // This method is not used; it is just a placeholder for annotation scanning
    }
}
