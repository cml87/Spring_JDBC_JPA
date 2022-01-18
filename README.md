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

Another type of data source is `SingleConnectionDataSource`. It is a subclass of `DriverManagerDataSource`. It will ensure that the same connection is returned when a connection is requested in a application, a singleton. Useful when we don't need more than one connection in our application, eg. a Swing desktop application accessing its db. 

##

