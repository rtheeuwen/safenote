package nl.safenote.utils.textsearch;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides full text search. Takes a list of Searchable objects and sorts it according to relevance.
 * Relevance is a multiple of the number of times the query keywords appear in searchable fields (marked with @Text),
 * and the weight factor of the respective field. When the AND condition is passed as a parameter Searchables must
 * contain all specified query keywords in order to show up in the result List. When the OR condition is used all
 * Searchables will show up in the result List.
 *
 * @param <T> Searchable
 */
public interface TextSearchEngine<T extends TextSearchable> {

	enum Condition {
		AND, OR
	}

	List<T> search(List<T> haystack, String needle);
	List<T> search(List<T> haystack, String needle, Condition condition);

}

class TextSearchEngineImpl<T extends TextSearchable> implements TextSearchEngine<T> {

	private Map<Field, Integer> fields;

	@Override
	public List<T> search(List<T> haystack, String needle) {
		return this.search(haystack, needle, Condition.AND);
	}

	@Override
	public List<T> search(List<T> haystack, String needle, Condition condition) {
		if (needle.length() == 0 || haystack == null || needle == null || condition == null)
			throw new NullPointerException();

		if (this.fields == null)
			startEngine(haystack);

		return haystack.parallelStream()
				.map(searchable -> this.getResult(searchable, (needle.split(" ")), condition))
				.filter(Objects::nonNull)
				.sorted((a, b) -> b.getScore() - a.getScore())
				.map(SearchResult::getSearchable)
				.collect(Collectors.toList());
	}

	private SearchResult<T> getResult(T searchable, String[] args, Condition condition) {

		try {
			SearchResult<T> searchResult = new SearchResult<>(searchable);

			for (String arg : args) {

				int score = 0;
				for (Map.Entry<Field, Integer> field : fields.entrySet()) {
					String text = field.getKey().get(searchable).toString();
					int weight = field.getValue();
					score += weight * countNumberOfMatches(text, arg);
				}

				if (condition.equals(Condition.AND) && score == 0)
					return null;
				else
					searchResult.incrementScore(score);
			}

			return condition==Condition.OR?searchResult.getScore()==0?null:searchResult:searchResult;

		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	private void startEngine(List<T> haystack) {
		Field[] fields = haystack.get(0).getClass().getDeclaredFields();
		this.fields = Arrays.stream(fields).filter(field -> field.isAnnotationPresent(Text.class))
				.map(field -> {
					field.setAccessible(true);
					return field;
				})
				.collect(Collectors.toMap(f -> f, f -> f.getAnnotation(Text.class).weight()));
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