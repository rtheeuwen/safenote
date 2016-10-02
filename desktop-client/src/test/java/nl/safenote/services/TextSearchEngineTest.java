package nl.safenote.services;


import nl.safenote.utils.textsearch.Text;
import nl.safenote.utils.textsearch.TextSearchEngine;
import nl.safenote.utils.textsearch.TextSearchable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TextSearchEngineTest {

	private List<Quote> quotes;
	private TextSearchEngine<Quote> textSearchEngine;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void init() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {

		quotes = new ArrayList<>(5);

		Quote quote1 = new Quote();
		quote1.author = "Joshua Bloch";
		quote1.book = "Effective java";
		quote1.content = "Always override hashCode when you override equals";
		quotes.add(quote1);

		Quote quote2 = new Quote();
		quote2.author = "Leibniz";
		quote2.book = "New essays on Human Understanding";
		quote2.content = "If equals be substituted for equals, the equality remains.";
		quotes.add(quote2);

		Quote quote3 = new Quote();
		quote3.author = "Umberto Eco";
		quote3.book = "This is not the end of the book";
		quote3.content = "The book is like the wheel - once invented, it cannot be bettered.";
		quotes.add(quote3);

		Quote quote4 = new Quote();
		quote4.author = "George Orwell";
		quote4.book = "Animal farm";
		quote4.content = "All animals are equal, but some animals are more equal than others";
		quotes.add(quote4);

		Quote quote5 = new Quote();
		quote5.author = "George Orwell";
		quote5.book = "1984";
		quote5.content = "The best books... are those that tell you what you know already.";
		quotes.add(quote5);

		Class<?> service = Class.forName("nl.safenote.utils.textsearch.TextSearchEngineImpl");
		Constructor<?> constructor = service.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		this.textSearchEngine = (TextSearchEngine<Quote>) constructor.newInstance();
	}

	@Test
	public void cannotSearchForEmptyString() {
		exception.expect(NullPointerException.class);
		textSearchEngine.search(quotes, "", TextSearchEngine.Condition.AND);
	}

	@Test
	public void andWorksCorrectly() {
		List<Quote> results = textSearchEngine.search(quotes, "book wheel", TextSearchEngine.Condition.AND);
		assertTrue(results.size() == 1);
		assertEquals("Umberto Eco", results.get(0).author);
	}

	@Test
	public void orWorksCorrectly() {
		List<Quote> results = textSearchEngine.search(quotes, "book wheel", TextSearchEngine.Condition.OR);
		assertTrue(results.size() == 2);
		assertTrue(results.contains(quotes.get(2)));
		assertTrue(results.contains(quotes.get(4)));
	}

	@Test
	public void andIsDefaultCondition() {
		List<Quote> def = textSearchEngine.search(quotes, "book wheel");
		List<Quote> and = textSearchEngine.search(quotes, "book wheel", TextSearchEngine.Condition.AND);
		List<Quote> or = textSearchEngine.search(quotes, "book wheel", TextSearchEngine.Condition.OR);

		assertEquals(def, and);
		assertNotEquals(def, or);
		assertNotEquals(and, or);
	}

	@Test
	public void ensureRankingIsCorrect() {
		List<Quote> results = textSearchEngine.search(quotes, "equal");
		assertEquals(results.get(0), quotes.get(1));
		assertEquals(results.get(1), quotes.get(3));
		assertEquals(results.get(2), quotes.get(0));
	}

}

class Quote implements TextSearchable {

	@Text(weight = 2)
	String author;

	@Text(weight = 3)
	String book;

	@Text(weight = 1)
	String content;
}