package goddmit;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SentimentAnalysisWithCount {
	DoccatModel model;
    static int positive = 0;
    static int negative = 0;

    public static void main(String[] args) throws IOException, TwitterException {
        String line = "";
        
		//Constructor
		SentimentAnalysisWithCount twitterCategorizer = new SentimentAnalysisWithCount();
        
		//Train Open NLP model
		twitterCategorizer.trainModel();
		
		// Twitter Keys and Access Token
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("VWigcSM2QGpsB43I1iWiN24nj")
                .setOAuthConsumerSecret("8RNjV3DqDrxxesVx3p5Vn5BtZGYOul5b3R6Ki6tZF0nsQaIcLV")
                .setOAuthAccessToken("823370125105446912-vsUZErkkBsbHaqtcDLgEBmsCvapZ0cI")
                .setOAuthAccessTokenSecret("Tj9neSksnCtQGa6SdmVYSc0jDIW0udDgz204e1RVW4Oel");
        
		//Build the object
		TwitterFactory tf = new TwitterFactory(cb.build());
        
		//Twitter instance is created for streaming tweets.
		Twitter twitter = tf.getInstance();
        
		//Search for Topic
		String search_topic=new String();
        System.out.println("Search: ");
        Scanner me=new Scanner(System.in);
        search_topic=me.nextLine();
        
		//Search in twitter using Query
		Query query = new Query(search_topic);
        QueryResult result = twitter.search(query);
        
		//Classify incoming tweets by reading tweet text from tweet data
		int result1 = 0;
        for (Status status : result.getTweets()) {
            result1 = twitterCategorizer.classifyNewTweet(status.getText());
            if (result1 == 1) {
                positive++;
            } else {
                negative++;
            }
        }

		//Write the count
        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\Agalya\\goddmit\\results.txt"));
        bw.write("Positive Tweets," + positive);
        bw.newLine();
        bw.write("Negative Tweets," + negative);
        bw.close();
        me.close();
    }

    public void trainModel() {
        InputStream dataIn = null;
		
		//Read training data from txt
        try {
            dataIn = new FileInputStream("C:\\Users\\Agalya\\goddmit\\tweets.txt");
            ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);
            
			// Specifies the minimum number of times a feature must be seen
            int cutoff = 3;
            int trainingIterations = 100;
            model = DocumentCategorizerME.train("en", sampleStream, cutoff,
                    trainingIterations);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int classifyNewTweet(String tweet) throws IOException {
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = myCategorizer.categorize(tweet);
        String category = myCategorizer.getBestCategory(outcomes);
		
		//Use Open NLP lib to categorize
        System.out.print("-----------------------------------------------------\nTWEET :" + tweet + " ===> ");
        if (category.equalsIgnoreCase("1")) {
            System.out.println(" POSITIVE ");
            return 1;
        } else {
            System.out.println(" NEGATIVE ");
            return 0;
        }

    }
}