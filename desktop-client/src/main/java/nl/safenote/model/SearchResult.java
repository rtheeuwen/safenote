package nl.safenote.model;

public class SearchResult{

    private final String identifier;
    private final String title;
    private int score;

    public SearchResult(Searchable searchable) {
        this.identifier = searchable.getIdentifier();
        this.title = searchable.getTitle();
        this.score = 0;
    }

    public void incrementScore(int incr){
        this.score += incr;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public int getScore() {
        return score;
    }

}
