package safenote.client.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import safenote.client.model.Header;
import safenote.client.model.Note;
import safenote.client.model.Result;
import safenote.client.persistence.NoteRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface SearchService {

    List<Header> search(String args);
}

@Service
class SearchServiceImpl implements SearchService {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;

    @Autowired
    public SearchServiceImpl(NoteRepository noteRepository, CryptoService cryptoService) {
        this.noteRepository = noteRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public List<Header> search(String args) {
        return args.length()==0 ? noteRepository.findAll().stream().sorted((a, b) -> (int)(b.getCreated()-a.getCreated())).map(note -> new Header(note.getId(), cryptoService.decipher(note, true).getHeader())).collect(Collectors.toList())
        : noteRepository.findAll().stream().map(note -> cryptoService.decipher(note, false)).map(note -> getResult(note, (args.split(" ")))).filter(result -> result!=null).sorted((a, b) -> b.getScore() - a.getScore()).map(result -> new Header(result.getItem().getId(), result.getItem().getHeader())).collect(Collectors.toList());
    }

    private Result<Note> getResult(Note note, String[] args){
        Result<Note> result = new Result<>(note);
        String content = note.getContent().toLowerCase();
        Arrays.stream(args).forEachOrdered(arg -> result.incrementScore(StringUtils.countOccurrencesOf(content, arg.toLowerCase())));
        return result.getScore()!=0?result:null;
    }
}