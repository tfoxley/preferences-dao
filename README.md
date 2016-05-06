# PreferencesDao
Alternative implementations of PreferencesDao

###Relational DB (e.g. MySQL)
#####Create database tables (Financials DB)

InstitutionPreferences (SH_INST_PREF_T)
```sql
CREATE TABLE `sh_inst_pref_t` (
  `INST_ID` varchar(10) COLLATE utf8_bin NOT NULL DEFAULT '',
  `LOGO_URL` text COLLATE utf8_bin,
  `LNK_GRPS` mediumtext COLLATE utf8_bin,
  `MENU` text COLLATE utf8_bin,
  PRIMARY KEY (`INST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```
InstitutionPreferencesCache (SH_INST_PREF_CACHE_T)
```sql
CREATE TABLE `sh_inst_pref_cache_t` (
  `INST_ID` varchar(10) COLLATE utf8_bin NOT NULL DEFAULT '',
  `LOGO_URL` text COLLATE utf8_bin,
  `MENU` text COLLATE utf8_bin,
  `LNK_GRPS` mediumtext COLLATE utf8_bin,
  `CACHED_IND` varchar(1) COLLATE utf8_bin DEFAULT NULL,
  `PRNCPL_NM` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `CREATED_AT` datetime DEFAULT NULL,
  PRIMARY KEY (`INST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```
InstitutionPreferencesCacheLength (SH_INST_PREF_CACHE_LNGTH_T)
```sql
CREATE TABLE `sh_inst_pref_cache_lngth_t` (
  `INST_ID` varchar(10) COLLATE utf8_bin NOT NULL DEFAULT '',
  `CACHE_LNGTH` int(11) DEFAULT '0',
  PRIMARY KEY (`INST_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```
UserPreferences (SH_USER_PREF_T)
```sql
CREATE TABLE `sh_user_pref_t` (
  `PRNCPL_NM` varchar(100) COLLATE utf8_bin NOT NULL DEFAULT '',
  `PREFS` text COLLATE utf8_bin,
  PRIMARY KEY (`PRNCPL_NM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
```
#####Insert data
- Seed InstitutionPreferences table
  - Use the last file referenced in kfs-core/src/main/resources/org/kuali/core/documentstore/updates.json
  - The top level keys in that JSON file represent the columns. All of their values are inserted as strings into the database.

#####Config Required (spring-sys.xml):
- Replace existing preferencesDao and preferencesDao-parentBean beans with:

```xml
  <bean id="preferencesDao" parent="platformAwareDaoJdbc" class="org.kuali.kfs.sys.dataaccess.impl.PreferencesDaoJdbc"></bean>
  ```

#####Additional Class Required:
- /kfs-core/src/main/java/org/kuali/kfs/sys/dataaccess/impl
org.kuali.kfs.sys.dataaccess.impl.PreferencesDaoJdbc

#####Caching:
- With institution configuration all of the links and permissions have to be calculated based on the user. To prevent this calculation from occurring every time we also store a calculated version of all of the links for the given user in a “cache”.
- The cache length defaults to zero.
- To set cache length use the “Sidebar Menu Cache Configuration” navigation link.


###DynamoDB
#####Create tables:
- initializeTables method in PreferencesDaoDynamoDB.java
- use DynamoDB console to insert data from the last file referenced in kfs-core/src/main/resources/org/kuali/core/documentstore/updates.json

#####Properties Required:
- kfs.documentstore.client.class=org.kuali.kfs.sys.dataaccess.impl.DynamoDBClient
- kfs.documentstore.class=org.kuali.kfs.sys.dataaccess.impl.PreferencesDaoDynamoDB
- kfs.documentstore.host=127.0.0.1
- kfs.documentstore.port=8000
- kfs.documentstore.username=kfs
- kfs.documentstore.password=kfs

#####Config Required (spring-sys.xml):
- Replace existing preferencesDao and preferencesDao-parentBean beans with:
```xml
<bean id="documentstoreClient" parent="documentstoreClient-parentBean"/>
<bean id="documentstoreClient-parentBean" abstract="true" class="org.kuali.kfs.sys.dataaccess.impl.DynamoDBClient">
  <constructor-arg value="${kfs.documentstore.host}" type="java.lang.String" />
  <constructor-arg value="${kfs.documentstore.port}" type="int" />
  <constructor-arg value="${kfs.documentstore.username}" type="java.lang.String" />
  <constructor-arg value="${kfs.documentstore.password}" type="java.lang.String" />
</bean>

<bean id="preferencesDao" parent="preferencesDao-parentBean"/>
<bean id="preferencesDao-parentBean" abstract="true" class="org.kuali.kfs.sys.dataaccess.impl.PreferencesDaoDynamoDB">
  <property name="documentstoreClient" ref="documentstoreClient" />
</bean>
```

#####Additional Classes Required:
- /kfs-core/src/main/java/org/kuali/kfs/sys/dataaccess/impl
  - org.kuali.kfs.sys.dataaccess.impl.DynamoDBClient
  - org.kuali.kfs.sys.dataaccess.impl.PreferencesDaoDynamoDB

#####Additional Maven Dependency:
```xml
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-java-sdk-dynamodb</artifactId>
  <version>1.10.21</version>
</dependency>
```

#####Methods Not Yet Implemented:
- Class: PreferencesDaoDynamoDB
  - saveInstitutionPreferences
  - findInstitutionPreferencesCache
  - cacheInstitutionPreferences
  - setInstitutionPreferencesCacheLength
  - getInstitutionPreferencesCacheLength
  - saveUserPreferences(String principalName, Map<String, Object> preferences)
