/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     * 
     * Partitions for guessFollowsGraph(tweets) -> followsGraph
     *    tweets.size: 0, 1, > 1
     *    authors: 1, > 1
     *    mentions: 0, 1, > 1
     *    Include tweets where a mentioned user is also an author, and 
     *    a tweet where an author mentions herself
     *  
     * Partitions for influencers(followsGraph) -> influencers
     *    followsGraph.size: 0, 1, > 1
     */
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "mike", "explosions anyone? explosions? anyone need explosions?", d1);
    private static final Tweet tweet2 = new Tweet(2, "jane", "if @Mike beats @Andy, @Mike has to face me in the hitman challenge", d2);
    private static final Tweet tweet3 = new Tweet(3, "andy", "come on @jane, do an @andy, if @mike beats you he wins", d2);
    private static final Tweet tweet4 = new Tweet(4, "mike", "Finally won one! SUCK IT @ANDY", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // Tests for guessFollowsGraph()
    @Test
    // covers tweets.size = 0
    public void testGuessFollowsGraph_Empty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }
    @Test
    // covers tweets.size = 1
    //        authors = 1
    //        mentions = 0
    public void testGuessFollowsGraph_NoMention() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1));
        
        if (followsGraph.size() > 0){
            assertEquals("Expected singleton map", 1, followsGraph.size());
            
            for (String user: followsGraph.keySet()) {
              assertTrue("Expected correct user in map", user.equalsIgnoreCase("mike"));
              assertEquals("Expected user to have no followers", 
                      Collections.emptySet(), followsGraph.get(user));
            }
        }    
    }
    @Test
    // covers tweets.size = 1
    //        authors = 1
    //        mentions = 1
    public void testGuessFollowsGraph_OneMention() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet4));
        Set<String> followees = followsGraph.keySet();
        String followeesString = followees.toString().toLowerCase();
        
        assertNotEquals("Expected map to have at least one user", 
                Collections.emptySet(), followsGraph);
        assertTrue("Expected map to have no more than two users", 
                followsGraph.size() <= 2);
        assertTrue("Expected user mentioned to be in map", followeesString.contains("andy"));
        
        if (followees.size() == 2) {
            assertTrue("Expected follower in map", followeesString.contains("mike"));
        }
        for (String user: followees) {
          Set<String> followers = followsGraph.get(user);
          
          assertFalse("Expected user not to follow self", followers.contains(user));
          
          if (user.equalsIgnoreCase("andy")) {
              assertEquals("Expected user to have one follower", 1, followers.size());
              assertEquals("Expected correct follower", 
                      followers.toString().toLowerCase().contains("mike"));
          } else if (user.equalsIgnoreCase("mike")) {
              assertTrue("Expected andy to have at most one follower", 
                      followers.size() <= 1);
          } else {
              fail("Incorrect usernames in map");
          }
        }
    }
    @Test
    // covers tweets.size > 1
    //        authors > 1
    //        mentions > 1
    public void testGuessFollowsGraph_MultipleMentions() {
        Map<String, Set<String>> followsGraph = 
                SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1,tweet2,tweet3,tweet4));
        
        assertNotEquals("Expected map to have at least one user", 
                Collections.emptySet(), followsGraph);
        assertTrue("Expected map to have no more than three users", 
                followsGraph.size() <= 3);
        for (String user: followsGraph.keySet()) {
            Set<String> followers = followsGraph.get(user);
            
            assertFalse("Expected user not to follow self", followers.contains(user));
            
            if (user.equalsIgnoreCase("mike")) {
                assertEquals("Expected user to have 2 followers", 2, followers.size());
            } else if (user.equalsIgnoreCase("jane")) {
                assertTrue("Expected user to have at least 1 follower", 1 <= followers.size());
                assertTrue("Expected andy to be jane's follower",
                        followers.toString().toLowerCase().contains("andy"));
                assertTrue("Expected user to have at most 2 followers", followers.size() <= 2);
            } else if (user.equalsIgnoreCase("andy")) {
                assertEquals("Expected user to have 2 followers", 2, followers.size());
            } else {
                fail("Incorrect usernames in map");
            }
        }
    }
    
    // Tests for influencers()
    @Test
    // covers empty graph
    public void testInfluencers_Empty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertEquals("expected empty list", Collections.emptySet(), influencers);
    }
    @Test
    // covers followsGraph.size 1, > 1
    public void testInfluencers_OneFollowee() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet4));
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertNotEquals("Expected non-empty list", Collections.emptyList(), influencers);
        assertTrue("Expected at least one followee", 1 <= influencers.size());
        assertTrue("Expected list to have no more than 2 followees", influencers.size() <= 2);
        assertTrue("Expected topmost influencer to have more followers", 
                influencers.get(0).equalsIgnoreCase("andy"));
    }
    @Test
    // covers followsGraph.size > 1
    public void testInfluencers_MultipleFollowers() {
        Map<String, Set<String>> followsGraph = 
                SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet4, tweet3));
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertNotEquals("Expected non-empty list", Collections.emptyList(), influencers);
        assertEquals("Expected three followees", 3, influencers.size());
        assertTrue("Expected topmost influencer to have the most followers", 
                influencers.get(0).equalsIgnoreCase("andy"));
        assertTrue("Expected lowest influencer to have the least number of followers", 
                influencers.get(2).equalsIgnoreCase("jane"));
    }
    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

}
