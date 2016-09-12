package nl.safenote.services;


import nl.safenote.model.Note;
import nl.safenote.model.PublicKeyWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import nl.safenote.model.Message;
import nl.safenote.model.NoteList;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public interface SynchronizationService {

    boolean enlist(String publicKey);
    void send(Note note);
    void delete(Note note);
    boolean synchronize();
}

@Service
class SynchronizationServiceImpl implements SynchronizationService {

    private final RestTemplate restTemplate;

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;

    private final String remoteHostUri;
    private String userId;
    private String publicKey;
    private long serverTimeOffset = 0;

    @Autowired
    public SynchronizationServiceImpl(Environment environment, NoteRepository noteRepository, CryptoService cryptoService) {
        this.noteRepository = noteRepository;
        this.remoteHostUri = "http://"+environment.getProperty("remotehostname")+":"+environment.getProperty("port")+"/"+environment.getProperty("contextroot")+"/";
        this.cryptoService = cryptoService;
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(1000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(1000);
        this.restTemplate = new RestTemplate(httpRequestFactory);
        List<HttpMessageConverter<?>> gson = new ArrayList<>(1);
        gson.add(new GsonHttpMessageConverter());
        restTemplate.setMessageConverters(gson);
    }

    @Override
    public boolean enlist(String publicKey) {
        this.publicKey = publicKey;
        return synchronize();
    }

    @Override
    public boolean synchronize() {
        if(this.serverTimeOffset==0) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<Long> responseEntity = restTemplate.exchange(remoteHostUri + "time", HttpMethod.GET, entity, Long.class);
                this.serverTimeOffset = responseEntity.getBody();
                if (this.serverTimeOffset == 0)
                    this.serverTimeOffset--;
                return synchronize();
            } catch (RestClientException e) {
                return false;
            }
        } else {
            if (this.userId == null) {
                this.userId = getSupplied(() -> restTemplate.postForObject(remoteHostUri + "enlist", new PublicKeyWrapper(this.publicKey), String.class));
                return synchronize();
            } else {
                try {
                    List<Note> notesList = noteRepository.findAll();
                    Map<String, Note> notes = notesList.stream().collect(Collectors.toMap(Note::getId, n -> n));
                    Map<String, String> localChecksums = notesList.stream().collect(Collectors.toMap(Note::getId, Note::getHash));
                    Map<String, String> remoteChecksums = getChecksums();
                    List<String> deletedNotes = getDeleted();

                    List<String> idsOfNotesToSend;
                    if (remoteChecksums!=null&&!remoteChecksums.isEmpty()) {
                        idsOfNotesToSend = getUniqueInX(localChecksums, remoteChecksums);
                    } else {
                        idsOfNotesToSend = notesList.stream().map(Note::getId).collect(Collectors.toList());
                    }

                    List<String> idsOfNotesToGet;
                    if (localChecksums!=null&&!localChecksums.isEmpty()) {
                        idsOfNotesToGet = getUniqueInX(remoteChecksums, localChecksums);
                    } else {
                        idsOfNotesToGet = remoteChecksums.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
                    }

                    Future<NoteList> newNotes = getNotes(idsOfNotesToGet);
                    notes.entrySet().stream().filter(e -> idsOfNotesToSend.contains(e.getKey())).forEachOrdered(e -> send(e.getValue()));
                    while (!newNotes.isDone()) {
                        Thread.sleep(10L);
                    }

                    newNotes.get().stream().filter(n -> n.getHash().equals(cryptoService.checksum(n))).forEachOrdered(noteRepository::create);
                    deletedNotes.stream().filter(notes::containsKey).forEachOrdered(noteRepository::delete);
                    return true;

                } catch (Exception e) {
                    return false;
                }
            }
        }
    }

    @Async
    @Override
    public void send(Note note) {
        consume(note, n -> restTemplate.put(remoteHostUri, cryptoService.sign(new Message<>(n, getExpires()), this.userId), Message.class));
    }

    @Async
    @Override
    public void delete(Note note) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        consume(note, (n -> restTemplate.exchange(remoteHostUri, HttpMethod.DELETE, new HttpEntity<>(cryptoService.sign(new Message<>(n, getExpires()), this.userId), headers), String.class)));
    }

    @Async
    private Future<NoteList> getNotes(List<String> ids){
        return applyFunction(ids, list -> new AsyncResult<>(restTemplate.postForObject(remoteHostUri+"notes", cryptoService.sign(new Message<>(list, getExpires()), this.userId), NoteList.class)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getChecksums(){
        return getSupplied(() -> restTemplate.postForObject(remoteHostUri+"checksums", cryptoService.sign(new Message<>(getExpires()), this.userId), HashMap.class));
    }

    @SuppressWarnings("unchecked")
    private List<String> getDeleted(){
        return getSupplied(() -> restTemplate.postForObject(remoteHostUri+"deleted", cryptoService.sign(new Message<>(getExpires()), this.userId),ArrayList.class));
    }


    private long getExpires(){
        if(serverTimeOffset==0) throw new RuntimeException("Did not connect to server yet.");
        return System.currentTimeMillis()+5000+serverTimeOffset;
    }

    private List<String> getUniqueInX(Map<String, String> x, Map<String, String> y){
        return x.entrySet().stream().filter((e) -> !(y.containsKey(e.getKey())&&y.containsValue(e.getValue()))).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Convenience method for wrapping consumer in boilerplate code.
     * @param t
     * @param consumer
     * @param <T>
     */
    private <T> void consume(T t, Consumer<T> consumer){
        if(this.serverTimeOffset!=0&&this.userId!=null) {
            try {
                consumer.accept(t);
            } catch (RestClientException e) {}
        } else {
            synchronize();
        }
    }

    /**
     * Convenience method for wrapping function in boilerplate code.
     * @param t
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    private <T, R> R applyFunction(T t, Function<T, R> function){
        try{
            return function.apply(t);
        } catch (RestClientException e){
            return null;
        }
    }

    /**
     * Convenience method for wrapping supplier in boilerplate code.
     * @param supplier
     * @param <T>
     * @return
     */
    private <T> T getSupplied(Supplier<T> supplier){
        try{
            return supplier.get();
        } catch (RestClientException e){
            return null;
        }
    }
}
