import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

//To read URL files
import java.net.URL;
import java.io.*;

//JSON stuff
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *  Tallies up the original artist of each of a Tumblr blog's Likes.
 *  @author Orion Guan
 *  @version February 20th, 2017
 */
public class BlogTally
{
	final static String FIRST_TIMESTAMP_MINUS_ONE = "1167631200"; //Beginning of 2007, central time
	final static String API_URL_BEFORE_BLOG = "http://api.tumblr.com/v2/blog/";
	final static String API_URL_BEFORE_TIME = "/likes?limit=50&after=";
	final static String API_URL_BEFORE_KEY = "&api_key=";
	
	public static void main(String[] args)
	{
		final String API_URL_BLOG = "sleepycoaster.tumblr.com";
		final String API_KEY = "2BxgovaoE4o7AL5kOjH6z5br5km7OEX7bwRpzDoQYEtZOi5SBX";
		final int KEY_USE_LIMIT = 40; //To prevent runaway usage of the API key
		
		//Scanner in = new Scanner(System.in); //Let's not implement any user choice yet.
		Map<String, Integer> blogTally = new HashMap<String, Integer>();
		
		String foundTime = FIRST_TIMESTAMP_MINUS_ONE;
		int keyUses = 0;
		int likesAnalyzed = 0;
		int likesAnalyzedBulk = 0;
		boolean thereAreMoreLikes = true;
		
		System.out.println("Tallying . . .");
	    
	    while(thereAreMoreLikes && (keyUses < KEY_USE_LIMIT))
	    {   
	    	String enteredURL = API_URL_BEFORE_BLOG
	    					  + API_URL_BLOG
	    					  + API_URL_BEFORE_TIME
	    					  + foundTime
	    					  + API_URL_BEFORE_KEY
	    					  + API_KEY;
	    	keyUses++; //The retrieval of Likes from a single API call not only returns an array of Likes information but
	    	//also information on whether or not there are more Likes to be found in subsequent calls. Therefore,
	    	//the next few lines can't be done in a separate method yet.
	    	JSONArray likedPostsArray;
	        try
	        {
	    	    URL enteredURLObject = new URL(enteredURL);
	    	    JSONParser parser = new JSONParser();
	    	    Object obj = parser.parse(new BufferedReader(
	    	            new InputStreamReader(enteredURLObject.openStream())));
	    	    
	    	    JSONObject root = (JSONObject) obj;
	    	    JSONObject response = (JSONObject) root.get("response");
	    	    likedPostsArray = (JSONArray) response.get("liked_posts");
	    	    
	    	    //System.out.printf("The number of Likes in this URL is %d\n", likedPostsArray.size());
	      	}
	        catch (Exception e)
	        {
	        	e.printStackTrace();
	        	likedPostsArray = null;
	        	System.out.println("An exception has been caught. Setting thereAreMoreLikes to false.");
	        	thereAreMoreLikes = false;
	      	}
	        
	    	ArrayList<String> sourceNameArrayList = getLikeSources(likedPostsArray);
	    	if (sourceNameArrayList.size() != likedPostsArray.size())
	    	{
	    		System.out.printf("MISMATCH %d", likedPostsArray.size() - sourceNameArrayList.size());
	    	}	
	    	likesAnalyzedBulk += sourceNameArrayList.size();
	    	
	    	if (sourceNameArrayList.size() != 0)
	    	{
	    		//System.out.printf("~~~The 50 likes after timestamp " + foundTime + "~~\n");
	    		//System.out.printf("This use of the key gave us %d Likes\n", sourceNameArrayList.size());
			    for (int i = (sourceNameArrayList.size() - 1); i >= 0; i--)
			    {
			    	String sourceName = sourceNameArrayList.get(i);
			    	
			    	if (blogTally.containsKey(sourceName))
			    	{
			    		blogTally.put(sourceName, blogTally.get(sourceName) + 1);
			    	}
			    	else
			    	{
			    		blogTally.put(sourceName, 1);
			    	}
			        //System.out.println(sourceName);
			    	likesAnalyzed++;
			    }
	    	}
	    	else
	    	{
	    		//System.out.println("No more Likes!");
	    		thereAreMoreLikes = false;
	    	}
	    	foundTime = grabFoundTime(likedPostsArray);
	    	//System.out.println("The most recent timestamp here is " + foundTime);
	    }
	    
	    System.out.println("~~~RESULTS~~~");
	    printOrdered(blogTally);
	    System.out.println("~~~");
	    System.out.printf("%d likes were analyzed\n", likesAnalyzed);
	    //System.out.printf("%d is the total size of all analyzed arrays", likesAnalyzedBulk);
	    System.out.printf("API key was used %d times\n", keyUses);
	    
	    //in.close();
	}
	  
	private static ArrayList<String> getLikeSources(JSONArray likedPostsArray)
	{
		ArrayList<String> sourceList = new ArrayList<>();
	    
	    for (int i = 0; i < likedPostsArray.size(); i++)
	    {
	    	JSONObject like = (JSONObject) likedPostsArray.get(i);
	    
		    String likeSourceTitle = (String) like.get("source_title");
		    if (likeSourceTitle != null)
		    {
		    	sourceList.add(likeSourceTitle);
		    }
		    else
		    {
		    	JSONArray likeTrail = (JSONArray) like.get("trail");
		    	if (likeTrail.size() != 0)
		    	{
		    		JSONObject likeTrailBody = (JSONObject) likeTrail.get(0);
		    		JSONObject likeTrailBlog = (JSONObject) likeTrailBody.get("blog");
		    		String likeTrailName = (String) likeTrailBlog.get("name");
		    		sourceList.add(likeTrailName);
		    	}
		    	else
		    	{
		    		String blogName = (String) like.get("blog_name");
		    		sourceList.add(blogName);
		    	}
		    }
		}
	    
	    return sourceList;
	}
	  
	private static String grabFoundTime(JSONArray likedPostsArray)
	{
		if (likedPostsArray.size() > 0)
		{
			JSONObject mostRecentLike = (JSONObject) likedPostsArray.get(0);
			String mostRecentTimestamp = Long.toString((Long) mostRecentLike.get("liked_timestamp"));
			return mostRecentTimestamp;
		}
		else
		{
			return null;
		}
	}
	  
	private static void printOrdered(Map<String, Integer> tally)
	{
		while (tally.size() > 0)
		{
			String maxEntry = null;
			for (String name : tally.keySet())
			{
				if (maxEntry == null || tally.get(name) > tally.get(maxEntry))
				{
					maxEntry = name;
				}
			}
			System.out.printf("%d " + maxEntry + "\n", tally.get(maxEntry));
			tally.remove(maxEntry);
		}
	}
}