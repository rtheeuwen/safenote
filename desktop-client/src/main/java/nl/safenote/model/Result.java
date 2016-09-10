package nl.safenote.model;

public final class Result<T> {

    private final T item;
    private int score;

    public Result(T item) {
        this.item = item;
        this.score = 0;
    }

    public void incrementScore(int incr){
        this.score += incr;
    }

    public T getItem() {
        return item;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
