package org.kuali.kfs.sys.dataaccess.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class DynamoDBClient {

    private AmazonDynamoDBClient dynamoDBClient;

    public DynamoDBClient(String host, int port, String username, String password) {
        AWSCredentials credentials = new BasicAWSCredentials(username, password);
        dynamoDBClient = new AmazonDynamoDBClient(credentials);
        dynamoDBClient.setEndpoint("http://" + host + ":" + port);
    }

    public AmazonDynamoDBClient getClient() {
        return dynamoDBClient;
    }
}
