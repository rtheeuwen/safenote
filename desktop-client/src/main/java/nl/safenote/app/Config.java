package nl.safenote.app;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import nl.safenote.utils.textsearch.TextSearchEngine;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

//todo refactor
public class Config {

    private DataSource dataSource(Properties properties){
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl(properties.getProperty("jdbcurl"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        dataSource.setMaximumPoolSize(1);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 15);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 1024);
        dataSource.addDataSourceProperty("useServerPrepStmts", true);
        return dataSource;
    }

    public Sql2o sql2o(){
        return new Sql2o(dataSource(properties()));
    }

    public Properties properties(){
        try(InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public Gson gson(){
        return new Gson();
    }

    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(5);
    }

    public TextSearchEngine textSearchEngine(){
        return new TextSearchEngine();
    }
}
