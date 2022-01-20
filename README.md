# Spring JDBC JPA
In this project I will put my notes about Spring JDBC and JPA. It will include notes from the following courses: 
- <span style="color:aquamarine">Mastering Spring Framework Fundamentals</span>, by Matthew Speak. O'Reilly. **pluralsight**

# Spring JDBC

## Creating and configuring Data Sources
Data sources are used to create connections, or to retrieve connections (from a connection pool), from an existing data source. In our code, they are represented by classes implementing interface `javax.sql.DataSource`, always.

Before Spring, we used to get the connection with `java.sql.DriverManager.getConnection()` as:
```java
    Class.forName("org.h2.Driver");  // load the jdbc driver
    Connection conn = DriverManager.getConnection("jdbc:h2:˜/test", "sa", "sa");
```
It returned a new connection to the db whenever we called it. But we don't use the DriverManager directly, we use the DataSource ? In Spring, there is an implementation of `javax.sql.DataSource` that includes the driver manager, `DriverManagerDataSource`, and works in the same way. It is not used in production, only in demo applications.

- `DriverManagerDataSource`: a class in Spring that defines a data source, together with the driver class needed for that specific jdbc vendor.

In a `DriverManagerDataSource` we have to set the driver class name. Depending on the db we are connecting to, we'll have one or another JDBC driver installed. In the code, we must use the fully qualified class name of the JDBC driver in our path.  

The JDBC driver will be a class implementing interface `java.sql.Driver`.
How to know if we have any JDBC driver in our class path? How to know the JDBC driver class?
1. Intellij, ^+shift+A, classes, "Driver". select `java.sql.Driver`. The methods in this interface will be called by the driver manager enclosed in the data source. ^F12 for methods list.
2. Look at the class hierarchy of this interface, ^H. We'll see the <u>classes in our classpath implementing this interface</u>! For example, `org.h2.Driver`, if we have included this dependency in the pom. That's what we need to use.

We know that service classes access repository classes to get access to data. Repository classes are also called DAO classes (Data Access Object) when they give access to a database. A Dao class will have injected a DataSource object. Here is the pattern:
```java
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan("com.example.jdbc")
public class AppConfig {

    @Bean
    public DataSource dataSource(){
        //SingleConnectionDataSource singleConnectionDataSource = new SingleConnectionDataSource();
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        
        // the driver class
        driverManagerDataSource.setDriverClassName("org.h2.Driver");
        
        // the data source definition: vendor specific db url, username, password
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
Another type of data source from Spring is `SingleConnectionDataSource`, which extends `DriverManagerDataSource`. It will ensure that the same connection is returned when a connection is requested in an application, a singleton. Useful when we don't need more than one connection in our application, eg. a Swing desktop application accessing its db. But why we need it when the data source is wrapped in a singleton Spring bean?

The connection url format will be vendor specific in general. The example above uses an in memory H2 database with url `jdbc:h2:mem:mydb`. We can use a file based database instead with `jdbc:h2:file:~/Desktop/mydb`, so we can "see" the database in the file system. This way we can connect a sql client to the db and examine its state. Additional options can be passed as `jdbc:h2:file:~/Desktop/mydb;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE`.
- AUTO_SERVER=TRUE: enables the H2 process to additionally access the server accepting incoming connections from a sql client that we attach ?
- DB_CLOSE_ON_EXIT=FALSE: keep the db file after the java application finish, so we can still connect to the db and examine it.  

There are different types of H2 databases (in memory, file based, standalone ?? ), with different connection urls. See the wizard of DBeaver.
jdbc:h2://<server>:<9092>/<db-name>

## Connection pool
Some data source implementations have a connection pool as a feature, so whenever a new connection is requested, one from the pool is returned. This save time, compared to instantiating one, which is a relatively costly operation. In production environments we find different types of data sources having this feature, for example:  
- DBCP (DataBase Connection Pool) from Apache
- C3PO, default with Hibernate
- AS, bind to the JNDI registry: `JndiDataSourceLookup.getDataSource(string_jndi_ref, eg. jdbc/myDataSource)`

Let's use as example `org.apache.tomcat.dbcp.dbcp.BasicDataSource` from Apache. The pom dependency is:
```xml
        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>dbcp</artifactId>
            <version>6.0.53</version>
        </dependency>
```
This data source implementation adds, respect to the javax `DataSource` interface, some additional configurations, such as the maximum active connections, and the idle time for a connection (time after which, if a connection is not used it is returned to the pool). See documentation at https://commons.apache.org/proper/commons-dbcp/:
```java
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

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
The JDBC template from Spring, `org.springframework.jdbc.core.JdbcTemplate`, allows working with JDBC but without all its boilerplate code. It is instantiated with a Data source, and will create and close connections automatically. Here is the config class with the data source bean and the Jdbc template using it:
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.example.jdbc")
public class AppConfig {

    @Bean
    public DataSource dataSource(){

        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        //DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        //dataSource.setUrl("jdbc:h2:file:mydb");
        dataSource.setUrl("jdbc:h2:file:~/Desktop/mydb;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }
}
```
And here is the Dao class the gets injected the data source and jdbc beans defined above:
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MyDao {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public MyDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void doQuery(){
        // This is used to see if the db is up and running only, no schema or tables
        //jdbcTemplate.execute("select 1 from dual");

        jdbcTemplate.update("insert into employee values (2,'Paul','HR')"); //update is used for update, insert or delete

        jdbcTemplate.query("select name, department from employee", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String name = rs.getString("name");
                String dept = rs.getString("department");
                System.out.println(String.format("Name = [%s], Dept = [%s]",name, dept));
            }
        });
    }
}
```
The `@PostConstruct` method will be called after all dependencies of the bean have been injected. We use it here just to show some usage examples of a jdbc template.

If we go into the `execute()` method we'll see it does all the boilerplate code we normally have to do with pure jdbc. This included getting the connection from the data source, closing it once we have finished using it, handle exceptions etc. This method will create the connection to the db specified in the data source, and when doing so, it will create the database itself, if it doesn't exist yet.

There are different methods a jdbc template supports to work with a db: `execute()`, `update()` and `query()`. 
- `execute()`: used for "select" sql queries ??
- `update()`: used for insert, update or delete sql queries.
- `query()`: used to read data from the database.  We need to supply the method to process the output result set from the database through an anonymous class. More specifically, we must pass a callback handler anonymous class?

The `doQuery()` method above could load the data read from the db into an `Employee` pojo, and return it to a method from the service layer. Remember, we are in a method of the repository layer that access the db. These methods are normally called by method of the service layer.



47.19