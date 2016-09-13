package nl.safenote.services;

import nl.safenote.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import nl.safenote.model.Header;
import nl.safenote.model.Note;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface SearchService {

    List<Header> search(String args);
}

@Service
class SearchServiceImpl implements SearchService {

    private final SafeNoteRepository safeNoteRepository;
    private final CryptoService cryptoService;

    @Autowired
    public SearchServiceImpl(SafeNoteRepository safeNoteRepository, CryptoService cryptoService) {
        assert safeNoteRepository !=null&&cryptoService!=null;
        this.safeNoteRepository = safeNoteRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public List<Header> search(String args) {
        return args.length()==0 ? safeNoteRepository.findAll().stream().sorted((a, b) -> (int)(b.getCreated()-a.getCreated())).map(note -> new Header(note.getId(), cryptoService.decipher(note, true).getHeader())).collect(Collectors.toList())
        : safeNoteRepository.findAll().stream().map(note -> cryptoService.decipher(note, false)).map(note -> getResult(note, (args.split(" ")))).filter(result -> result!=null).sorted((a, b) -> b.getScore() - a.getScore()).map(result -> new Header(result.getItem().getId(), result.getItem().getHeader())).collect(Collectors.toList());
    }

    private Result<Note> getResult(Note note, String[] args){
        Result<Note> result = new Result<>(note);
        String content = note.getContent().toLowerCase();
        Arrays.stream(args).forEachOrdered(arg -> result.incrementScore(StringUtils.countOccurrencesOf(content, arg.toLowerCase())));
        return result.getScore()!=0?result:null;
    }
}