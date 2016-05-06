package org.kuali.kfs.sys.dataaccess.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
import org.kuali.kfs.sys.dataaccess.PreferencesDao;

import java.util.Map;

public class PreferencesDaoDynamoDB implements PreferencesDao {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PreferencesDaoDynamoDB.class);

    public static final String INSTITUTION_PREFERENCES = "InstitutionPreferences";
    public static final String INSTITUTION_ID_KEY = "institutionId";
    public static final String USER_PREFERENCES = "UserPreferences";
    public static final String PRINCIPAL_NAME_KEY = "principalName";

    private DynamoDBClient documentstoreClient;

    @Override
    public Map<String, Object> findInstitutionPreferences() {
        DynamoDB dynamoDB = new DynamoDB(documentstoreClient.getClient());
        Table table = dynamoDB.getTable(INSTITUTION_PREFERENCES);

        Item item = table.getItem(INSTITUTION_ID_KEY, "1232413535");
        return item == null ? null : item.asMap();
    }

    @Override
    public void saveInstitutionPreferences(String institutionId, Map<String, Object> preferences) {

    }

    @Override
    public Map<String, Object> findInstitutionPreferencesCache(String principalName) {
        return null;
    }

    @Override
    public void cacheInstitutionPreferences(String principalName, Map<String, Object> institutionPreferences) {

    }

    @Override
    public void setInstitutionPreferencesCacheLength(int seconds) {

    }

    @Override
    public int getInstitutionPreferencesCacheLength() {
        return 0;
    }

    @Override
    public Map<String, Object> getUserPreferences(String principalName) {
        DynamoDB dynamoDB = new DynamoDB(documentstoreClient.getClient());
        Table table = dynamoDB.getTable(USER_PREFERENCES);

        Item item = table.getItem(PRINCIPAL_NAME_KEY, principalName);
        return item == null ? null : item.asMap();
    }

    @Override
    public void saveUserPreferences(String principalName, String preferences) {
        Item item = Item.fromJSON(preferences);
        item = item.withString(PRINCIPAL_NAME_KEY, principalName);

        DynamoDB dynamoDB = new DynamoDB(documentstoreClient.getClient());
        Table table = dynamoDB.getTable(USER_PREFERENCES);
        PutItemOutcome outcome = table.putItem(item);
        LOG.debug("Saved user preferences: " + outcome);
    }

    @Override
    public void saveUserPreferences(String principalName, Map<String, Object> preferences) {

    }

    public void setDocumentstoreClient(DynamoDBClient documentstoreClient) {
        this.documentstoreClient = documentstoreClient;
    }

    private void initializeTables() {
        // Create InstitutionPreferences table if it does not exist yet
        if (Tables.doesTableExist(documentstoreClient.getClient(), INSTITUTION_PREFERENCES)) {
            LOG.debug("Table " + INSTITUTION_PREFERENCES + " is already ACTIVE");
        } else {
            CreateTableRequest createTableRequest = new CreateTableRequest()
                    .withTableName(INSTITUTION_PREFERENCES)
                    .withKeySchema(new KeySchemaElement().withAttributeName(INSTITUTION_ID_KEY).withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName(INSTITUTION_ID_KEY).withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
            TableDescription createdTableDescription = documentstoreClient.getClient().createTable(createTableRequest).getTableDescription();
            LOG.debug("Created Table: " + createdTableDescription);

            LOG.debug("Waiting for " + INSTITUTION_PREFERENCES + " to become ACTIVE...");
            try {
                Tables.awaitTableToBecomeActive(documentstoreClient.getClient(), INSTITUTION_PREFERENCES);
            } catch (InterruptedException e) {
                LOG.error(INSTITUTION_PREFERENCES + " table failed to become ACTIVE", e);
            }
        }

        // Create UserPreferences table if it does not exist yet
        if (Tables.doesTableExist(documentstoreClient.getClient(), USER_PREFERENCES)) {
            LOG.debug("Table " + USER_PREFERENCES + " is already ACTIVE");
        } else {
            CreateTableRequest createTableRequest = new CreateTableRequest()
                    .withTableName(USER_PREFERENCES)
                    .withKeySchema(new KeySchemaElement().withAttributeName(PRINCIPAL_NAME_KEY).withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName(PRINCIPAL_NAME_KEY).withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
            TableDescription createdTableDescription = documentstoreClient.getClient().createTable(createTableRequest).getTableDescription();
            LOG.debug("Created Table: " + createdTableDescription);

            LOG.debug("Waiting for " + USER_PREFERENCES + " to become ACTIVE...");
            try {
                Tables.awaitTableToBecomeActive(documentstoreClient.getClient(), USER_PREFERENCES);
            } catch (InterruptedException e) {
                LOG.error(USER_PREFERENCES + " table failed to become ACTIVE", e);
            }
        }
    }
}
