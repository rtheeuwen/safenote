package nl.safenote.services;

import nl.safenote.model.Result;
import nl.safenote.model.Header;
import nl.safenote.model.Note;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public interface SearchService {

    List<Header> search(String args);
}

class SearchServiceImpl implements SearchService {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;

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
                .map(result -> new Header(result.getItem().getId(), cryptoService.decipher(result.getItem().getHeader())))
                .collect(Collectors.toList());
    }

    private Result<Note> getResult(Note note, String[] args){
        Result<Note> result = new Result<>(note);
        String text = note.getContent().toLowerCase(Locale.getDefault());
        for (String query : args) {
            int count = countNumberOfMatches(text, query.toLowerCase(Locale.getDefault()));
            if (count == 0)
                return null; //AND condition -> note will be filtered out
            else
                result.incrementScore(count);
        }
        return result;
    }

    private static int countNumberOfMatches(String text, String query) {
        if (text == null || query == null || text.length() == 0 || query.length() == 0) {
            return 0;
        }
        int count = 0;
        int from = 0;
        int index;
        while ((index = text.indexOf(query, from)) != -1) {
            ++count;
            from = index + query.length();
        }
        return count;
    }
}