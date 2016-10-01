package nl.safenote.utils.textsearch;


import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


public class TextSearchEngine<T extends TextSearchable> {

    public List<T> search(List<T> haystack, String needle) {
        if(needle.length()==0)
            throw new IllegalArgumentException("Query must be provided");
        return haystack.parallelStream()
                .map(searchable -> this.getResult(searchable, (needle.split(" "))))
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getScore() - a.getScore())
                .map(SearchResult::getSearchable)
                .collect(Collectors.toList());
    }

    private SearchResult<T> getResult(T searchable, String[] args){
        SearchResult<T> searchResult = new SearchResult<>(searchable);
        String text = searchable.getText().toLowerCase(Locale.getDefault());
        for (String query : args) {
            int count = countNumberOfMatches(text, query.toLowerCase(Locale.getDefault()));
            if (count == 0)
                return null; //AND condition -> note will be filtered out
            else
                searchResult.incrementScore(count);
        }
        return searchResult;
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