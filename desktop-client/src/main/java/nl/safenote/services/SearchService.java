package nl.safenote.services;

import nl.safenote.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nl.safenote.model.Header;
import nl.safenote.model.Note;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
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
        assert noteRepository !=null&&cryptoService!=null;
        this.noteRepository = noteRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public List<Header> search(String args) {
        if(args==null||args.length()==0)
            throw new IllegalArgumentException("Query must be provided");
        return noteRepository.findAllTextNotes().parallelStream()
                .map(cryptoService::decipher)
                .map(note -> this.getResult(note, (args.split(" "))))
                .filter(result -> result!=null)
                .sorted((a, b) -> b.getScore() - a.getScore())
                .map(result -> new Header(result.getItem().getId(), result.getItem().getHeader()))
                .collect(Collectors.toList());
    }

    private Result<Note> getResult(Note note, String[] args){
        Result<Note> result = new Result<>(note);
        String text = note.getContent().toLowerCase(Locale.getDefault());
        for (String query : args) {
            int count = StringUtils.countOccurrencesOf(text, query.toLowerCase(Locale.getDefault()));
            if (count == 0)
                return null; //AND condition -> note will be filtered out
            else
                result.incrementScore(count);
        }
        return result;
    }
}