/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing Strategy
     * 
     * Partition for Extract.getTimespan(tweets)
     *  tweets.size: 1, > 1
     *  include tweets having similar timestamps
     * 
     * Partition for Extract.getMentionedUsers(tweets) -> mentionedUsers
     *  tweets.size: 1, > 1
     *  mentions in tweets: 0, 1, > 1
     *  Include usernames with mixed cases(uppercase, lowercase, or mixedcase)
     *  Also include strings containing @ that are not valid mentions
     *  
     * Exhaustive Cartesian coverage of partitions
     * 
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much? alyssa@gmail.com", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "@rivest talk in 30 minutes #hype, @dave's attending? ", d2);
    private static final Tweet tweet3 = new Tweet(3, "davemat", "junit tests in @Rivest #java8@mikes #software_construction", d2);
    
    private static final Set<String> mentions = new HashSet<>(Arrays.asList("rivest","dave"));
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    //Tests for getTimespan()
    @Test
    // covers tweets.size() = 1
    public void testGetTimespan_OneTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));
        
        assertEquals("Expected start == end",
                timespan.getStart(), timespan.getEnd());
        assertEquals("Expected start == d1",
                timespan.getStart(), d1);
    }
    @Test
    // covers tweets.size > 1
    public void testGetTimespan_TwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet3, tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    @Test
    // covers tweets.size > 1
    //        tweets having the same timestamp
    public void testGetTimespan_SameTimestamp() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet3));
        
        assertEquals("Expected start == end",
                timespan.getStart(), timespan.getEnd());
        assertEquals("Expected start == d2",
                timespan.getStart(), d2);
    }
    
    // Tests for getMentionedUsers()
    @Test
    // covers tweets.size = 1, mentions = 0
    public void testGetMentionedUsers_NoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));   
        
        assertEquals("expected empty set", 
                Collections.emptySet(), mentionedUsers);
    }
    @Test
    // covers tweets.size = 1, mentions = 1
    public void testGetMentionedUsers_OneTweetOneMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet3));
        Iterator<String> users = mentionedUsers.iterator();
        
        assertNotEquals("Expected non-empty set", 
                Collections.emptySet(), mentionedUsers);
        assertEquals("Expected singleton set",
                1, mentionedUsers.size());
        while (users.hasNext()) {
            assertTrue("Expected correct username-mention extracted",
                    "Rivest".equalsIgnoreCase(users.next()));
        }
    }
    @Test
    // covers tweets.size > 1, mentions > 0,
    //       A mention in multiple tweets with different cases
    public void testGetMentionedUsers_MultipleTweetsMultipleMentions() {
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        Set<String> mentionedUsers = Extract.getMentionedUsers(tweets);
        boolean areValid = 
                mentionedUsers.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .containsAll(mentions);
        
        assertNotEquals("Expected non-empty set",
                Collections.emptySet(), mentionedUsers);
        assertEquals("Expected 2 mentions",
                2, mentionedUsers.size());
        assertTrue("Expected mentions to contain valid mentions",
                areValid);
        
    }
    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}
