import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.twitter._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import org.apache.spark.sql


import org.apache.spark.storage.StorageLevel
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File


object SentimentAnalysis {
 
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: TwitterPopularTags <consumer key> <consumer secret> " +
        "<access token> <access token secret> [<filters>]")
      System.exit(1)
    }

    StreamingExamples.setStreamingLogLevels()

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)
    val filters = args.takeRight(args.length - 4)

    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generat OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("Sentiments").setMaster("local[2]")
   // val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    val stream = TwitterUtils.createStream(ssc, None, filters)

  val tags = stream.flatMap { status => status.getHashtagEntities.map(_.getText)}

tags.countByValue()
   .foreachRDD { rdd =>
       val now = org.joda.time.DateTime.now()
       rdd
         .sortBy(_._2)
         .map(x => (x, now))
         .saveAsTextFile(s"~/twitter/$now")
     }
/*
val tweets = stream.filter {t =>
     val tags = t.getText.split(" ").filter(_.startsWith("#")).map(_.toLowerCase)
     tags.contains("#obama") && tags.contains("#NYC")
   }
*/
val tweets = stream.filter {t =>
     val tags = t.getText.split(" ").filter(_.startsWith("Trump")).map(_.toLowerCase)
     tags.exists { x => true }
}
   

val data = tweets.map { status =>
   val sentiment = SentimentAnalysisUtils.detectSentiment(status.getText)
  
   val tagss = status.getHashtagEntities.map(_.getText.toLowerCase)

  (status.getText, sentiment.toString, tagss.toString())
   
    
}    
data.print()
data.saveAsTextFiles("~/twitterss","20000") 

/*

val sqlContext= new org.apache.spark.sql.SQLContext(sc)
import sqlContext.implicits._

/*
//me
data.foreachRDD(rdd => { 
  rdd.map(_.split(",")).map(p
=> Person(p(0), p(1),p(2)))
.toDF().registerTempTable("sentiments")
})
//endme
*/

//comment
data.foreachRDD(rdd => { 
  rdd.toDF().registerTempTable("sentiments")
})


sqlContext.sql("select * from sentiments").show()
*/

/*
//sqlContext.createDataFrame(rdd)

val rdd: RDD[Row] = ???
val schema = StructType(
  (1 to rdd.first.size).map(i => StructField(s"_$i", StringType, false))
)

val df = sqlContext.createDataFrame(rdd, schema)
sqlContext.sql("select * from df").show()
*/

ssc.start()
    ssc.awaitTermination()

  }

}

