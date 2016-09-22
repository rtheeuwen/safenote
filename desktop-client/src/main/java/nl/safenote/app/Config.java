package nl.safenote.app;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import nl.safenote.api.AuthenticationController;
import org.springframework.context.annotation.*;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import nl.safenote.api.NoteController;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.services.SynchronizationService;
import nl.safenote.services.SearchService;


import java.security.SecureRandom;
import java.util.Properties;

@Configuration
@EnableAsync
@EnableTransactionManagement
@PropertySource(value = {"classpath:application.properties"})
@ComponentScan(basePackageClasses = {View.class, NoteController.class, AuthenticationController.class, NoteRepository.class, SynchronizationService.class, CryptoService.class, SearchService.class})
public class Config {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setPackagesToScan("nl.safenote.model");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        factoryBean.setJpaProperties(properties);
        return factoryBean;
    }

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:~/.safenote/database");
        dataSource.setUsername( "safenote" );
        dataSource.setPassword( "safenote" );
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public SecureRandom secureRandom(){
        return new SecureRandom();
    }

}
