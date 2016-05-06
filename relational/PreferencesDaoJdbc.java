package org.kuali.kfs.sys.dataaccess.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kuali.kfs.sys.dataaccess.PreferencesDao;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreferencesDaoJdbc extends PlatformAwareDaoBaseJdbc implements PreferencesDao {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PreferencesDaoJdbc.class);
    public static final String INSTITUTION_ID_VALUE = "1232413535";
    public static final String LOGO_URL = "logoUrl";
    public static final String MENU = "menu";
    public static final String LINK_GROUPS = "linkGroups";
    public static final String PRINCIPAL_NAME = "principalName";
    public static final String CREATED_AT = "createdAt";
    public static final String CACHED = "cached";
    public static final String PREFERENCES = "preferences";
    public static final String INSTITUTION_ID = "institutionId";
    public static final String INSTITUTION_ID_KEY = "INST_ID";
    public static final String LOGO_URL_KEY = "LOGO_URL";
    public static final String CACHED_IND_KEY = "CACHED_IND";
    public static final String PRINCIPAL_NAME_KEY = "PRNCPL_NM";
    public static final String CREATED_AT_KEY = "CREATED_AT";
    public static final String MENU_KEY = "MENU";
    public static final String LINK_GROUPS_KEY = "LNK_GRPS";
    public static final String PREFERENCES_KEY = "PREFS";
    public static final String ONE = "1";


    @Override
    public Map<String, Object> findInstitutionPreferences() {
        LOG.debug("findInstitutionPreferences() started");

        String sqlString = "SELECT * FROM SH_INST_PREF_T WHERE INST_ID = ?";
        Map<String, Object> preferences = getSimpleJdbcTemplate().queryForMap(sqlString, INSTITUTION_ID_VALUE);
        preferences = deserializeInstitutionPreferences(preferences);
        return preferences;
    }

    @Override
    public void saveInstitutionPreferences(String institutionId, Map<String, Object> preferences) {
        LOG.debug("saveInstitutionPreferences() started");

        String sqlString = "UPDATE SH_INST_PREF_T SET LOGO_URL = ?, MENU = ?, LNK_GRPS = ? WHERE INST_ID = ?";
        Map<String, Object> serializedPreferences = serializeInstitutionPreferences(preferences);
        getSimpleJdbcTemplate().update(sqlString, serializedPreferences.get(LOGO_URL), serializedPreferences.get(MENU),
                serializedPreferences.get(LINK_GROUPS), INSTITUTION_ID_VALUE);
    }

    @Override
    public Map<String, Object> findInstitutionPreferencesCache(String principalName) {
        LOG.debug("findInstitutionPreferencesCache() started");

        String sqlString = "SELECT * FROM SH_INST_PREF_CACHE_T WHERE INST_ID = ? AND PRNCPL_NM = ?";
        try {
            Map<String, Object> preferences = getSimpleJdbcTemplate().queryForMap(sqlString, INSTITUTION_ID_VALUE, principalName);
            preferences = deserializeInstitutionPreferences(preferences);
            if (isCacheExpired((Timestamp)preferences.get("createdAt"))) {
                preferences = findInstitutionPreferences();
                cacheInstitutionPreferences(principalName, preferences);
            }
            return preferences;
        } catch (DataAccessException dae) {
            LOG.debug("InstitutionPreferences not found in cache.", dae);
        }
        return null;
    }

    @Override
    public void cacheInstitutionPreferences(String principalName, Map<String, Object> preferences) {
        LOG.debug("cacheInstitutionPreferences() started");

        String sqlString = "UPDATE SH_INST_PREF_CACHE_T SET LOGO_URL = ?, MENU = ?, LNK_GRPS = ?, CACHED_IND = ?, PRNCPL_NM = ?, CREATED_AT = ? WHERE INST_ID = ? AND PRNCPL_NM = ?";
        Map<String, Object> serializedPreferences = serializeInstitutionPreferences(preferences);
        serializedPreferences.put(PRINCIPAL_NAME, principalName);
        serializedPreferences.put(CREATED_AT, new Date());
        serializedPreferences.put(CACHED, true);

        int success = getSimpleJdbcTemplate().update(sqlString, serializedPreferences.get(LOGO_URL), serializedPreferences.get(MENU),
                serializedPreferences.get(LINK_GROUPS), serializedPreferences.get(CACHED), serializedPreferences.get(PRINCIPAL_NAME),
                serializedPreferences.get(CREATED_AT), INSTITUTION_ID_VALUE, principalName);

        if (success == 0) {
            sqlString = "INSERT INTO SH_INST_PREF_CACHE_T (INST_ID, LOGO_URL, MENU, LNK_GRPS, CACHED_IND, PRNCPL_NM, CREATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?)";
            getSimpleJdbcTemplate().update(sqlString, INSTITUTION_ID_VALUE, serializedPreferences.get(LOGO_URL), serializedPreferences.get(MENU),
                    serializedPreferences.get(LINK_GROUPS), serializedPreferences.get(CACHED), serializedPreferences.get(PRINCIPAL_NAME),
                    serializedPreferences.get(CREATED_AT));
        }
    }

    @Override
    public void setInstitutionPreferencesCacheLength(int seconds) {
        LOG.debug("setInstitutionPreferencesCacheLength() started");

        String sqlString = "UPDATE SH_INST_PREF_CACHE_LNGTH_T SET CACHE_LNGTH = ? WHERE INST_ID = ?";
        int success = getSimpleJdbcTemplate().update(sqlString, seconds, INSTITUTION_ID_VALUE);

        if (success == 0) {
            sqlString = "INSERT INTO SH_INST_PREF_CACHE_LNGTH_T (INST_ID, CACHE_LNGTH) VALUES (?, ?)";
            getSimpleJdbcTemplate().update(sqlString, INSTITUTION_ID_VALUE, seconds);
        }
    }

    @Override
    public int getInstitutionPreferencesCacheLength() {
        LOG.debug("getInstitutionPreferencesCacheLength() started");

        try {
            String sqlString = "SELECT CACHE_LNGTH FROM SH_INST_PREF_CACHE_LNGTH_T WHERE INST_ID = ?";
            int cacheLength = getSimpleJdbcTemplate().queryForInt(sqlString, INSTITUTION_ID_VALUE);
            return cacheLength;
        } catch (EmptyResultDataAccessException e) {
            LOG.debug("InstitutionPreferencesCacheLength not found.", e);
        }
        return 0;
    }

    @Override
    public Map<String, Object> getUserPreferences(String principalName) {
        LOG.debug("getUserPreferences() started");

        try {
            String sqlString = "SELECT * FROM SH_USER_PREF_T WHERE PRNCPL_NM = ?";
            Map<String, Object> preferences = getSimpleJdbcTemplate().queryForMap(sqlString, principalName);
            preferences = deserializeUserPreferences(preferences);
            return (Map<String, Object>)preferences.get(PREFERENCES);
        } catch (EmptyResultDataAccessException e) {
            LOG.debug("UserPreferences not found for " + principalName, e);
        }
        return null;
    }

    @Override
    public void saveUserPreferences(String principalName, String preferences) {
        LOG.debug("saveUserPreferences() started");

        String sqlString = "UPDATE SH_USER_PREF_T SET PREFS = ? WHERE PRNCPL_NM = ?";
        int success = getSimpleJdbcTemplate().update(sqlString, preferences, principalName);
        if (success == 0) {
            sqlString = "INSERT INTO SH_USER_PREF_T (PRNCPL_NM, PREFS) VALUES (?, ?)";
            getSimpleJdbcTemplate().update(sqlString, principalName, preferences);
        }
    }

    @Override
    public void saveUserPreferences(String principalName, Map<String, Object> preferences) {
        LOG.debug("saveUserPreferences() started");

        String sqlString = "UPDATE SH_USER_PREF_T SET PREFS = ? WHERE PRNCPL_NM = ?";
        String preferencesString = serializeUserPreferences(preferences);
        int success = getSimpleJdbcTemplate().update(sqlString, preferencesString, principalName);
        if (success == 0) {
            sqlString = "INSERT INTO SH_USER_PREF_T (PRNCPL_NM, PREFS) VALUES (?, ?)";
            getSimpleJdbcTemplate().update(sqlString, principalName, preferencesString);
        }
    }

    private Map<String, Object> deserializeInstitutionPreferences(Map<String, Object> preferences) {
        Map<String, Object> deserializedPreferences = new ConcurrentHashMap<>();
        deserializedPreferences.put(INSTITUTION_ID, preferences.get(INSTITUTION_ID_KEY));
        deserializedPreferences.put(LOGO_URL, preferences.get(LOGO_URL_KEY));

        // Handle fields from cached InstitutionPreferences
        if (preferences.containsKey(CACHED_IND_KEY)) {
            deserializedPreferences.put(CACHED, ONE.equals(preferences.get(CACHED_IND_KEY)));
        }
        if (preferences.containsKey(PRINCIPAL_NAME_KEY)) {
            deserializedPreferences.put(PRINCIPAL_NAME, preferences.get(PRINCIPAL_NAME_KEY));
        }
        if (preferences.containsKey(CREATED_AT_KEY)) {
            deserializedPreferences.put(CREATED_AT, preferences.get(CREATED_AT_KEY));
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> menuList = mapper.readValue((String)preferences.get(MENU_KEY), List.class);
            deserializedPreferences.put(MENU, menuList);

            List<Map<String, Object>> linkGroups = mapper.readValue((String)preferences.get(LINK_GROUPS_KEY), List.class);
            deserializedPreferences.put(LINK_GROUPS, linkGroups);
        } catch (IOException e) {
            LOG.error("Failed to deserialize InstitutionPreferences JSON from database.", e);
        }
        return deserializedPreferences;
    }

    private Map<String, Object> serializeInstitutionPreferences(Map<String, Object> preferences) {
        Map<String, Object> serializedPreferences = new ConcurrentHashMap<>();
        serializedPreferences.put(INSTITUTION_ID, preferences.get(INSTITUTION_ID));
        serializedPreferences.put(LOGO_URL, preferences.get(LOGO_URL));

        ObjectMapper mapper = new ObjectMapper();
        try {
            String menuString = mapper.writeValueAsString(preferences.get(MENU));
            serializedPreferences.put(MENU, menuString);

            String linkGroupsString = mapper.writeValueAsString(preferences.get(LINK_GROUPS));
            serializedPreferences.put(LINK_GROUPS, linkGroupsString);
        } catch (IOException e) {
            LOG.error("Failed to serialize InstitutionPreferences JSON for database.", e);
        }
        return serializedPreferences;
    }

    private Map<String, Object> deserializeUserPreferences(Map<String, Object> preferences) {
        Map<String, Object> deserializedPreferences = new ConcurrentHashMap<>();
        if (preferences.containsKey(PRINCIPAL_NAME_KEY)) {
            deserializedPreferences.put(PRINCIPAL_NAME, preferences.get(PRINCIPAL_NAME_KEY));
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> prefs = mapper.readValue((String)preferences.get(PREFERENCES_KEY), Map.class);
            deserializedPreferences.put(PREFERENCES, prefs);
        } catch (IOException e) {
            LOG.error("Failed to deserialize UserPreferences JSON from database.", e);
        }
        return deserializedPreferences;
    }

    private String serializeUserPreferences(Map<String, Object> preferences) {
        String prefsString = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            prefsString = mapper.writeValueAsString(preferences);
        } catch (IOException e) {
            LOG.error("Failed to serialize UserPreferences JSON for database.", e);
        }
        return prefsString;
    }

    private boolean isCacheExpired(Timestamp createdAt) {
        int cacheLength = getInstitutionPreferencesCacheLength() * 1000;
        return new Date().getTime() > createdAt.getTime() + cacheLength;
    }
}
