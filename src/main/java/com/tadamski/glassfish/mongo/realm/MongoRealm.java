package com.tadamski.glassfish.mongo.realm;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * @author tmszdmsk
 */
public class MongoRealm extends AppservRealm {

    //properties
    public static final String MONGO_HOSTNAME = "mongo.hostname";
    public static final String MONGO_PORT = "mongo.port";
    public static final String MONGO_DB_NAME = "mongo.db.name";
    public static final String MONGO_COLLECTION_NAME = "mongo.collection.name";
    public static final String LOGIN_PROPERTY = "login.property";
    public static final String SALT_PROPERTY = "salt.property";
    public static final String PASSWORD_PROPERTY = "password.property";
    public static final String GROUPS_PROPERTY = "groups.property";
    public static final String HASH_FUNCTION = "hash.function";
    public static final String JAAS_CONTEXT = "jaas-context";

    public static final String AUTH_TYPE = "MongoAuth";
    private DBCollection collection;
    private String hostname;
    private Integer port;
    private String dbName;
    private String collectionName;
    private String loginProperty;
    private String saltProperty;
    private String passwordProperty;
    private String groupsProperty;
    private String hashFunction;

    @Override
    protected void init(Properties properties) throws BadRealmException, NoSuchRealmException {
        hostname = properties.getProperty(MONGO_HOSTNAME, "localhost");
        port = Integer.valueOf(properties.getProperty(MONGO_PORT, "27017"));
        dbName = properties.getProperty(MONGO_DB_NAME, "users");
        collectionName = properties.getProperty(MONGO_COLLECTION_NAME, "users");
        loginProperty = properties.getProperty(LOGIN_PROPERTY, "login");
        saltProperty = properties.getProperty(SALT_PROPERTY, "salt");
        passwordProperty = properties.getProperty(PASSWORD_PROPERTY, "password");
        groupsProperty = properties.getProperty(GROUPS_PROPERTY, "groups");
        //SUPPORTED: MD2, MD5, SHA-1, SHA-256, SHA-384, and SHA-512
        hashFunction = properties.getProperty(GROUPS_PROPERTY, "SHA-512");
        
        setProperty(MONGO_HOSTNAME, hostname);
        setProperty(MONGO_PORT, port.toString());
        setProperty(MONGO_DB_NAME, dbName);
        setProperty(MONGO_COLLECTION_NAME, collectionName);
        setProperty(LOGIN_PROPERTY, loginProperty);
        setProperty(SALT_PROPERTY, saltProperty);
        setProperty(PASSWORD_PROPERTY, passwordProperty);
        setProperty(GROUPS_PROPERTY, groupsProperty);
        setProperty(HASH_FUNCTION, hashFunction);
        try {
            collection = new MongoClient(hostname, port).getDB(dbName).getCollection(collectionName);
            String propJaasContext = properties.getProperty(JAAS_CONTEXT);
            if (propJaasContext != null) {
                setProperty(JAAS_CONTEXT, propJaasContext);
            }
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    public DBCollection getMongoCollection() {
        return collection;
    }

    @Override
    public Enumeration getGroupNames(String login) throws InvalidOperationException, NoSuchUserException {
        DBObject query = QueryBuilder.start(loginProperty).is(login).get();
        DBObject userObject = collection.findOne(query);
        if (userObject == null) {
            throw new NoSuchUserException(String.format("User with login property(%s)==%s not found", loginProperty, login));
        }
        BasicDBList groupsList = (BasicDBList) userObject.get(groupsProperty);

        return new Vector<>(groupsList).elements();
    }

    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

}
