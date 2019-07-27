package goddmit;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;

import scala.Tuple2;

import twitter4j.Status;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.common.collect.Iterables;

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

public class sparkit {

    DoccatModel model;
    static int positive = 0;
    static int negative = 0;

    public static void main(String[] args) throws IOException, TwitterException {

        String line = "";

        //Constructor
        sparkit twitterCategorizer = new sparkit();

        //Train Open NLP model
        twitterCategorizer.trainModel();

        // Prepare the spark configuration by setting application name and master(only) node "local"
        final SparkConf sparkConf = new SparkConf().setAppName("Twitter Data Processing").setMaster("local[10]");

        // Create Streaming context using spark configuration and duration for which messages will be batched and fed to Spark Core
        final JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Duration.apply(10000));

        // Prepare configuration for Twitter authentication and authorization
        final Configuration conf = new ConfigurationBuilder().setDebugEnabled(false)
                .setOAuthConsumerKey("VWigcSM2QGpsB43I1iWiN24nj")
                .setOAuthConsumerSecret("8RNjV3DqDrxxesVx3p5Vn5BtZGYOul5b3R6Ki6tZF0nsQaIcLV")
                .setOAuthAccessToken("823370125105446912-vsUZErkkBsbHaqtcDLgEBmsCvapZ0cI")
                .setOAuthAccessTokenSecret("Tj9neSksnCtQGa6SdmVYSc0jDIW0udDgz204e1RVW4Oel")
                .build();

        // Create Twitter authorization object by passing prepared configuration containing consumer and access keys and tokens
        final Authorization twitterAuth = new OAuthAuthorization(conf);

        // Create a data stream using streaming context and Twitter authorization
        final JavaReceiverInputDStream<Status> inputDStream = TwitterUtils.createStream(streamingContext, twitterAuth, new String[]{});

        // Create a new stream by filtering the non english tweets from earlier streams
        final JavaDStream<Status> enTweetsDStream = inputDStream.filter((status) -> "en".equalsIgnoreCase(status.getLang()));

        // Convert stream to pair stream with key as user screen name and value as tweet text
        final JavaPairDStream<String, String> userTweetsStream
                = enTweetsDStream.mapToPair(
                        (status) -> new Tuple2<String, String>(status.getUser().getScreenName().startsWith("#"), status.getText())
                );

        // Group the tweets for each user
        final JavaPairDStream<String, Iterable<String>> tweetsReducedByUser = userTweetsStream.groupByKey();

        // Create a new pair stream by replacing iterable of tweets in older pair stream to number of tweets
        final JavaPairDStream<String, Integer> tweetsMappedByUser = tweetsReducedByUser.mapToPair(
                userTweets -> new Tuple2<String, Integer>(userTweets._1, Iterables.size(userTweets._2))
        );

		// Iterate over the stream's RDDs and print each element on console
		tweetsMappedByUser.foreachRDD((VoidFunction<JavaPairRDD<String, Integer>>)pairRDD -> {
         pairRDD.foreach(new VoidFunction<Tuple2<String,Integer>>() {

         @Override
         public void call(Tuple2<String, Integer> t) throws Exception {
         System.out.println(t._1() + "," + t._2());
         }
				
         });
         });
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
        String search_topic = new String();
        System.out.println("Search: ");
        Scanner me = new Scanner(System.in);
        search_topic = me.nextLine();

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

        // Triggers the start of processing. Nothing happens if streaming context is not started
        streamingContext.start();

        // Keeps the processing live by halting here unless terminated manually
        streamingContext.awaitTermination();

        bw.close();
        me.close();
    }
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
