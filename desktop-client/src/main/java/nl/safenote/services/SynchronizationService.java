package nl.safenote.services;


import com.google.gson.Gson;
import nl.safenote.model.*;
import org.javalite.http.Get;
import org.javalite.http.Http;
import org.javalite.http.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public interface SynchronizationService {

    boolean enlist(PublicKey publicKey);
    void send(Note note);
    void delete(Note note);
    boolean synchronize();
}

@Service @Deprecated //TODO PLX FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!
class SynchronizationServiceImpl implements SynchronizationService {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;

    private final String remoteHostUri;
    private volatile String userId;
    private String publicKey;
    private volatile long serverTimeOffset = 0;

    private final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private static final String contentType = "Content-Type";
    private static final String accept = "Accept";
    private static final String applicationJson = "application/json";

    @Autowired
    public SynchronizationServiceImpl(Environment environment, NoteRepository noteRepository, CryptoService cryptoService) {
        assert(environment!=null&&noteRepository!=null&&cryptoService!=null);
        this.noteRepository = noteRepository;
        this.remoteHostUri = "http://"+environment.getProperty("remotehostname")+":"+environment.getProperty("port")+"/"+environment.getProperty("contextroot")+"/";
        this.cryptoService = cryptoService;

    }

    @Override
    public boolean enlist(PublicKey publicKey) {
        this.publicKey = DatatypeConverter.printBase64Binary(publicKey.getEncoded());
        return synchronize();
    }

    @Override
    public boolean synchronize(){
        return synchronize(0);
    }
    private boolean synchronize(int stackDepth) {
        if(stackDepth>2)
            throw new RuntimeException("synchronization failed");
        if(this.serverTimeOffset==0) {
            try {
                System.out.println("servertime");
                this.serverTimeOffset = getTime() - System.currentTimeMillis();
                System.out.println("done servertime");
                if (this.serverTimeOffset == 0)
                    this.serverTimeOffset--;
                return synchronize(++stackDepth);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.userId == null) {
                System.out.println("userid");
                this.userId = getUserId(this.publicKey);
                System.out.println("done userid");
                return synchronize(++stackDepth);
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
                    notes.entrySet().stream().filter(e -> idsOfNotesToSend.contains(e.getKey())).map(e -> {Note n = e.getValue(); n.setEncrypted(true); return n;}).forEachOrdered(this::send);
                    while (!newNotes.isDone()) {
                        Thread.sleep(10L);
                    }

                    newNotes.get().stream().filter(n -> n.getHash().equals(cryptoService.checksum(n))).sorted((a, b) -> (int)(b.getCreated() - a.getCreated())).map(n -> {n.setEncrypted(true); return n;}).forEachOrdered(noteRepository::create);
                    deletedNotes.stream().filter(notes::containsKey).forEachOrdered(noteRepository::delete);
                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
    }

    public void send(Note note){
        executorService.execute(() -> {
            System.out.println("send");
            httpPost(remoteHostUri, jsonMessage(note))
                    .doConnect().dispose();
            System.out.println("done sending");
        });
    }

    public void delete(Note note){
        executorService.execute(() -> {
            System.out.println("delete");
            httpPost(remoteHostUri + "delete", jsonMessage(note))
                    .doConnect().dispose();
            System.out.println("done delete");
        });
    }

    private Future<NoteList> getNotes(List<String> ids){
        return executorService.submit(() -> {
            System.out.println("getnotes");
            String json = httpPost(remoteHostUri + "notes", jsonMessage(ids)).text();
            System.out.println("done notes");
            return gson.fromJson(json, NoteList.class);
        });
    }

    private Map<String, String> getChecksums(){

        String json = httpPost(remoteHostUri + "checksums", jsonMessage()).text();
        return gson.fromJson(json, StringMap.class);
    }

    private List<String> getDeleted(){
        String json = httpPost(remoteHostUri + "deleted", jsonMessage()).text();
        return gson.fromJson(json, StringList.class);
    }

    private long getTime(){
        String json = httpGet(remoteHostUri + "time").text();
        return Long.valueOf(json);
    }

    private String getUserId(String publicKey){
        String json = gson.toJson(new PublicKeyWrapper(publicKey));
        return httpPost(remoteHostUri + "enlist", json).text();
    }

    private long expirationTime(){
        if(serverTimeOffset==0) throw new RuntimeException("Did not connect to server yet.");
        return System.currentTimeMillis()+5000+serverTimeOffset;
    }

    private String jsonMessage(Object... input){
        Message message;
        switch (input.length){
            case 0: message = new Message(expirationTime());
                break;
            case 1: message = new Message(input[0], expirationTime());
                break;
            default: throw new IllegalArgumentException();
        }
        return gson.toJson(cryptoService.sign(message, this.userId));
    }

    private static Post httpPost(String uri, String body){
        return Http.post(uri, body).header(contentType, applicationJson).header(accept, applicationJson);
    }

    private static Get httpGet(String uri){
        return Http.get(uri, 1000, 1000).header(contentType, applicationJson).header(accept, applicationJson);
    }

    private List<String> getUniqueInX(Map<String, String> x, Map<String, String> y){
        return x.entrySet().stream().filter((e) -> !(y.containsKey(e.getKey())&&y.containsValue(e.getValue()))).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private class StringList extends ArrayList<String>{}

    private class StringMap extends HashMap<String, String>{}

}
