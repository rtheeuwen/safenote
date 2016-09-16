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

public interface TextSearchService {

    List<Header> search(String args);
}

@Service
class TextSearchServiceImpl implements TextSearchService {

    private final NoteRepository noteRepository;
    private final CryptoService cryptoService;

    @Autowired
    public TextSearchServiceImpl(NoteRepository noteRepository, CryptoService cryptoService) {
        assert noteRepository !=null&&cryptoService!=null;
        this.noteRepository = noteRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public List<Header> search(String args) {
        return args.length()==0 ? noteRepository.findHeaders().stream().map(h -> {h.setHeader(cryptoService.decipherHeader(h.getHeader())); return h;}).collect(Collectors.toList())
        : noteRepository.findAllText().stream().map(note -> cryptoService.decipher(note)).map(note -> getResult(note, (args.split(" ")))).filter(result -> result!=null)
                .sorted((a, b) -> b.getScore() - a.getScore()).map(result -> new Header(result.getItem().getId(), result.getItem().getHeader())).collect(Collectors.toList());
    }

    private Result<Note> getResult(Note note, String[] args){
        Result<Note> result = new Result<>(note);
        String content = note.getContent().toLowerCase();
        Arrays.stream(args).forEachOrdered(arg -> result.incrementScore(StringUtils.countOccurrencesOf(content, arg.toLowerCase())));
        return result.getScore()!=0?result:null;
    }
}