package nl.safenote.utils.textsearch;

class SearchResult<T> {

	private final T searchable;
	private int score;

	SearchResult(T searchable) {
		this.searchable = searchable;
		this.score = 0;
	}

	void incrementScore(int incr) {
		this.score += incr;
	}

	int getScore() {
		return score;
	}

	T getSearchable() {
		return this.searchable;
	}
}
