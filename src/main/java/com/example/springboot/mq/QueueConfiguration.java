package com.example.springboot.mq;

public class QueueConfiguration {

    private String queueName;
    private int portNumber;
    private String additionalDetails;

    public QueueConfiguration() {
    }

    public QueueConfiguration(String queueName, int portNumber, String additionalDetails) {
        this.queueName = queueName;
        this.portNumber = portNumber;
        this.additionalDetails = additionalDetails;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(String additionalDetails) {
        this.additionalDetails = additionalDetails;
    }
}
