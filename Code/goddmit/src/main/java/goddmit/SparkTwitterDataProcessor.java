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

import com.google.common.collect.Iterables;

public class SparkTwitterDataProcessor {
	public static void main(String[] args) {
		
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
		final JavaPairDStream<String, String> userTweetsStream = 
								enTweetsDStream.mapToPair(
									(status) -> new Tuple2<String, String>(status.getUser().getScreenName(), status.getText())
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
					System.out.println("#"+t._1() + "," + t._2());
				}
				
			});
		});
						
		// Triggers the start of processing
		streamingContext.start();
		
		// Keeps the processing live by halting here unless terminated manually
		streamingContext.awaitTermination();
		
	}
}