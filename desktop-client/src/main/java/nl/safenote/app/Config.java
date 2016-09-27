package nl.safenote.app;


import com.zaxxer.hikari.HikariDataSource;
import nl.safenote.controllers.AuthenticationController;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import nl.safenote.controllers.NoteController;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.services.SynchronizationService;
import nl.safenote.services.SearchService;
import org.sql2o.Sql2o;

import javax.sql.DataSource;


@Configuration
@EnableAsync
@PropertySource(value = {"classpath:application.properties"})
@ComponentScan(basePackageClasses = {View.class, NoteController.class, AuthenticationController.class, NoteRepository.class, SynchronizationService.class, CryptoService.class, SearchService.class})
public class Config {

    @Bean
    public DataSource dataSource(){
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:~/.safenote/database");
        dataSource.setUsername("safenote");
        dataSource.setPassword("safenote");
        dataSource.setMaximumPoolSize(1);
        dataSource.addDataSourceProperty("cachePrepStmts", true);
        dataSource.addDataSourceProperty("prepStmtCacheSize", 15);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 1024);
        dataSource.addDataSourceProperty("useServerPrepStmts", true);
        return dataSource;
    }

    @Bean
    public Sql2o sql2o(){
        return new Sql2o(dataSource());
    }

}
