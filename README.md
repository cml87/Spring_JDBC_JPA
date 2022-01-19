# Spring JDBC JPA
In this project I will put my notes about Spring JDBC and JPA. It will include notes from the following courses: 
- <span style="color:aquamarine">Mastering Spring Framework Fundamentals</span>, by Matthew Speak. O'Reilly. **pluralsight**

# Spring JDBC

## Creating and configuring Data Sources
Data sources are used to create connections, or to retrieve connections (from a connection pool), from an existing data source. `DataSource` is an interface.

Before Spring, we used to get the connection with `java.sql.DriverManager.getConnection()`as:
```java
    Class.forName("org.h2.Driver");  // load the jdbc driver
    Connection conn = DriverManager.getConnection("jdbc:h2:˜/test", "sa", "sa");
```
It returned a new connection to the db whenever we called it. But we don't use the DriverManager directly, we use the DataSource ? In Spring, there is an implementation of the driver manager that includes the data source, `DriverManagerDataSource`, and works in the same way. It is not used in production, only in demo applications.

In a `DriverManagerDataSource` we have to set the driver class name. Depending on the db we are connecting to, we'll have one or another JDBC driver installed. In the code, we must use the fully qualified class name of the JDBC driver in our path.  

The JDBC driver will be a class implementing interface `java.sql.Driver`.
How to know if we have any JDBC driver in our class path? How to know the JDBC driver class?
1. Intellij, ^+shift+A, classes, "Driver". select `java.sql.Driver`. The methods in this interface will be called by the driver manager enclosed in the data source. ^F12 for methods list.
2. Look at the class hierarchy of this interface, ^H. We'll see the classes in our classpath implementing this interface! For example, `org.h2.Driver`, if we have included this dependency in the pom. That's what we need to use.

We know that service classes access repository classes to get access to data. Repository classes are also called DAO classes (Data Access Object) when they give access to a database. A Dao class will have injected a DataSource object. Here is the patter:
```java
@Configuration
@ComponentScan("com.example.jdbc")
public class AppConfig {

    @Bean
    public DataSource dataSource(){
        //SingleConnectionDataSource singleConnectionDataSource = new SingleConnectionDataSource();
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName("org.h2.Driver");
        driverManagerDataSource.setUrl("jdbc:h2:mem:mydb");
        driverManagerDataSource.setUsername("sa");
        driverManagerDataSource.setPassword("");
        return driverManagerDataSource;
    }
}
```
```java
@Repository
public class MyDao {

    private DataSource dataSource;

    @Autowired
    public MyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```
The example above uses an in memory H2 database with url `jdbc:h2:mem:mydb`. We can use a file based database instead with `jdbc:h2:file:~/Desktop/mydb`, so we can "see" the database in the file system. This way we can connect a sql client to the db and examine its state. Additional options can be passed as `jdbc:h2:file:~/Desktop/mydb;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE`.
- AUTO_SERVER=TRUE: enables the H2 process to additionally access the server accepting incoming connections from a sql client that we attach.
- DB_CLOSE_ON_EXIT=FALSE: keep the db file after the java application finish, so we can still connect to the db and examine it.  

jdbc:h2://<server>:<9092>/<db-name>

The database will be created when we get the connection. The connection will be created in turn in the exectute() method call explained below, from the jdbc template. 


Another type of data source is `SingleConnectionDataSource`. It is a subclass of `DriverManagerDataSource`. It will ensure that the same connection is returned when a connection is requested in a application, a singleton. Useful when we don't need more than one connection in our application, eg. a Swing desktop application accessing its db. 

## Connection pool
Data sources can have a connection pool, so whenever a new connection is requested, one from the pool is returned. This save time, compared to instantiating one, which is a relatively costly operation. In production environments we find different types of data sources having this feature, for example:  
- DBCP (DataBase Connection Pool) from Apache
- C3PO, default with Hibernate
- AS, bind to the JNDI registry: `JndiDataSourceLookup.getDataSource(string_jndi_ref, eg. jdbc/myDataSource)`

It is possible to instantiate one of this data sources in our Spring application. Let's use dbcp from Apache. Include the dependency in the pom:
```xml
        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>dbcp</artifactId>
            <version>6.0.53</version>
        </dependency>
```
The data source class to be instantiated is `org.apache.tomcat.dbcp.dbcp.BasicDataSource`, which extends `javax.sql.DataSource`. It will add, respect to the javax data source, some additional fields to configure the data source, such as the max. active connections, and the idle time for a connection (time after which, if a connection is not used it is returned to the pool). See documentation at https://commons.apache.org/proper/commons-dbcp/:
```java
@Configuration
@ComponentScan("com.example.jdbc")
public class AppConfig {

    @Bean
    public DataSource dataSource(){

        //SingleConnectionDataSource singleConnectionDataSource = new SingleConnectionDataSource();
        //DriverManagerDataSource dataSource = new DriverManagerDataSource();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:mydb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        // fields from Apache's BasicDataSource
        dataSource.setMaxActive(5);
        dataSource.setMaxIdle(30000);

        return dataSource;
    }
}
```

## JDBC template 
The JDBC template from Spring allows to easily launch queries against a database.


If we go into the execute() method we'll see it does all the boilerplate code we normally have to do with pure jdbc. This included getting the connection from the data source, closing it once we have finished using it, handle exceptions.

We'll connect to the db with the client Squirrel sql

47.19