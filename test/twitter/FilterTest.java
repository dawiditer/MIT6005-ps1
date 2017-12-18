/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     * 
     * Partitions for writtenBy(tweets, author) -> tweetsByAuthor
     *   tweets.size: 1, > 1
     *   tweets written by author: 0, 1, > 1
     * 
     * Partitions for inTimespan(tweets, timespan) -> tweetsWithinTimespan
     *   tweets.size: 1, > 1
     *   number of tweets within timespan: 0, 1, > 1 
     * 
     * Partitions for containing(tweets, words) -> tweetsContainingWords
     *   tweets.size: 1, > 1
     *   words.size: 1, > 1
     *   number of tweets having at least one word in words: 0, 1, > 1
     *   Include tweets having the words in different cases 
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);;
    private static final Tweet tweet3 = new Tweet(3, "alyssa", "{retweet @bbitdiddle} rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // Tests for writtenBy()
    @Test
    // covers tweets.size = 1,
    //        tweets by author = 0
    public void testWrittenBy_SingleTweetNotAuthor() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet2), "alyssa");
        
        assertEquals("Expected no tweet by author",
                Collections.emptyList(), writtenBy);
    }
    @Test
    // covers tweets.size > 1,
    //        tweets by author = 1
    public void testWrittenBy_MultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }
    @Test
    // covers tweets.size > 1,
    //        tweets by author > 1
    public void testWrittenBy_MultipleTweetsMultipleResults() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3), "alyssa");
        
        assertEquals("expected doubleton list", 2, writtenBy.size());
        assertTrue("expected list to contain tweets by author", 
                writtenBy.containsAll(Arrays.asList(tweet3, tweet1)));
    }
    
    // Tests for inTimespan()
    @Test
    // covers tweets.size > 1,
    //        no tweets within timespan
    public void testInTimespan_NoResult() {
        Instant testStart = Instant.parse("2017-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2017-02-17T12:00:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(
                Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertEquals("expected no tweet within timespan", 
                Collections.emptyList(), inTimespan);
    }
    @Test
    // covers tweets.size > 1
    //        1 tweet within timestamp
    public void testInTimespan_MultipleTweetsOneResult() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T10:30:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(
                Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertNotEquals("expected a tweet within timespan", 
                Collections.emptyList(), inTimespan);
        assertEquals("expected 1 tweet within timespan", 
                1, inTimespan.size());
        assertTrue("expected correct tweet within timespan", inTimespan.contains(tweet1));
    }
    @Test
    // covers tweets.size > 1,
    //        more than 1 tweet within timespan 
    public void testInTimespan_MultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(
                Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertNotEquals("expected non-empty list", 
                Collections.emptyList(), inTimespan);
        assertTrue("expected list to contain tweets", 
                inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }
    
    // Tests for containing()
    @Test
    // covers tweets.size = 1
    //        words.size = 1
    //        no tweet has the word
    public void testContaining_NoResult() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1), Arrays.asList("Obama"));
        
        assertEquals("Expected no tweet with the word", 
                Collections.emptyList(), containing);
    }
    @Test
    // covers tweets.size > 1
    //        words.size > 1
    //        one tweet has at least one word
    public void testContaining_OneResult() {
        List<Tweet> containing = Filter.containing(
                Arrays.asList(tweet1, tweet2), Arrays.asList("REASONABLE","doubt"));
        
        assertNotEquals("Expected a tweet with a word in the list", 
                Collections.emptyList(), containing);
        assertTrue("Expected tweet containing word to be in list", containing.contains(tweet1));
    }
    @Test
    // covers tweets.size > 1
    //        words.size = 1
    //        more than 1 tweet containing atleast one word in the list 
    public void testContaining_MultipleResults() {
        List<Tweet> containing = Filter.containing(
                Arrays.asList(tweet1, tweet2, tweet3), Arrays.asList("talk"));
        
        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2, tweet3)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
        assertEquals("expected same order", 2, containing.indexOf(tweet3));
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}
