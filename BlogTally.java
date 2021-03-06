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
	final static int LIKES_PER_KEY_USE = 50;
	
	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in);
		
		System.out.println("Enter blog URL:");
		String APIURLblog = in.nextLine();
		System.out.println("Enter API key");
		String APIkey = in.nextLine();
		System.out.println("Enter an estimate for how many Likes need to be tallied");
		int numberOfLikes = in.nextInt();
		in.nextLine();
		int keyUseLimit = (numberOfLikes / LIKES_PER_KEY_USE) + 1; //To prevent runaway usage of the API key
		
		Map<String, Integer> blogTally = new HashMap<String, Integer>();
		
		String foundTime = FIRST_TIMESTAMP_MINUS_ONE;
		int keyUses = 0;
		int likesAnalyzed = 0;
		//int likesAnalyzedBulk = 0;
		boolean thereAreMoreLikes = true;
		
		System.out.println("Tallying . . .");
	    
	    while(thereAreMoreLikes && (keyUses < keyUseLimit))
	    {   
	    	String enteredURL = API_URL_BEFORE_BLOG
	    					  + APIURLblog
	    					  + API_URL_BEFORE_TIME
	    					  + foundTime
	    					  + API_URL_BEFORE_KEY
	    					  + APIkey;
	    	
			keyUses++;
			JSONArray likedPostsArray;
			try
			{
				likedPostsArray = retrieveLikesArray(enteredURL);
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
	    	//likesAnalyzedBulk += sourceNameArrayList.size();
	    	
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
	    
	    in.close();
	}
	
	/**
	  * Retrieves an array of JSON data, each element of which gives
	  * information about a Like displayed by the Tumblr API.
	  * @param enteredURL The URL of the JSON data.
	  * @return An array of data respective to a list of Likes from the page.
	  */
	private static JSONArray retrieveLikesArray(String enteredURL) throws Exception
	{
    	JSONArray likedPostsArray;
    	
	    URL enteredURLObject = new URL(enteredURL);
	    JSONParser parser = new JSONParser();
	    Object obj = parser.parse(new BufferedReader(
	            new InputStreamReader(enteredURLObject.openStream())));
	    
	    JSONObject root = (JSONObject) obj;
	    JSONObject response = (JSONObject) root.get("response");
	    likedPostsArray = (JSONArray) response.get("liked_posts");
	    
	    //System.out.printf("The number of Likes in this URL is %d\n", likedPostsArray.size());

        return likedPostsArray;
	}
	
	/**
	  * Interprets the array of JSON data to return an ArrayList of original
	  * artist names.
	  * @param likedPostsArray An array of data respective to a list of Likes
	  * from a Tumblr API page.
	  * @return An ArrayList of artist names.
	  */
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
	  
	/**
	  * Finds the timestamp for the most recent Like whose information is
	  * displayed on a Tumblr API page.
	  * @param likedPostsArray A JSON array of Likes information.
	  * @return The numerical String for the timestamp of the most recent
	  * Like displayed.
	  */
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
	 
	/**
	  *	Prints a hashmap of blog names and tally counts in order from greatest
	  * tally to smallest tally.
	  * @param tally The hashmap of blog name keys and tally values.
	  */ 
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