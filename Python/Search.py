#Import the necessary methods from tweepy library
from tweepy.streaming import StreamListener
from tweepy import OAuthHandler
from tweepy import Stream

#Variables that contains the user credentials to access Twitter API 
access_token = "823370125105446912-vsUZErkkBsbHaqtcDLgEBmsCvapZ0cI"
access_token_secret = "Tj9neSksnCtQGa6SdmVYSc0jDIW0udDgz204e1RVW4Oel"
consumer_key = "VWigcSM2QGpsB43I1iWiN24nj"
consumer_secret = "8RNjV3DqDrxxesVx3p5Vn5BtZGYOul5b3R6Ki6tZF0nsQaIcLV"


#This is a basic listener that just prints received tweets to stdout.
class StdOutListener(StreamListener):

    def on_data(self, data):
        print (data)
        return True

    def on_error(self, status):
        print (status)


if __name__ == '__main__':

    #This handles Twitter authetification and the connection to Twitter Streaming API
    l = StdOutListener()
    auth = OAuthHandler(consumer_key, consumer_secret)
    auth.set_access_token(access_token, access_token_secret)
    stream = Stream(auth, l)

    #This line filter Twitter Streams to capture data by the keywords: 'python', 'javascript', 'ruby'
    stream.filter(track=['python', 'javascript', 'ruby'])
