package nl.safenote.app;


import nl.safenote.controllers.AuthenticationController;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import nl.safenote.controllers.NoteController;
import nl.safenote.services.CryptoService;
import nl.safenote.services.NoteRepository;
import nl.safenote.services.SynchronizationService;
import nl.safenote.services.SearchService;
import org.sql2o.Sql2o;


@Configuration
@EnableAsync
@PropertySource(value = {"classpath:application.properties"})
@ComponentScan(basePackageClasses = {View.class, NoteController.class, AuthenticationController.class, NoteRepository.class, SynchronizationService.class, CryptoService.class, SearchService.class})
public class Config {

    @Bean
    public Sql2o sql2o(){
        return new Sql2o("jdbc:h2:~/.safenote/database", "safenote", "safenote");
    }

}
