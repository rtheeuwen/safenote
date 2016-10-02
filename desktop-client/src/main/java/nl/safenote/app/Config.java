package nl.safenote.app;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class Config {

	DataSource dataSource(Properties properties) {
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

	Properties properties() {
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties")) {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties;
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	ExecutorService executorService() {
		return Executors.newFixedThreadPool(5);
	}

}
