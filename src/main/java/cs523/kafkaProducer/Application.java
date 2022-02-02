package cs523.kafkaProducer;
import java.util.Properties;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class Application {

    public static KafkaProducer<String,String> producer=Application.createKafkaProducer();
    public static KafkaProducer<String,String> createKafkaProducer(){
        //creating kafka producer
    	//creating producer properties
        String bootstrapServers="127.0.0.1:9092";
        Properties properties= new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,    bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> first_producer = new KafkaProducer<String, String>(properties);
        return first_producer;

    }

    public static void streamFeed() {

        StatusListener listener = new StatusListener(){

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg) {
                System.out.println("Got a status deletion notice id:" + arg.getStatusId());
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onStatus(Status status) {
                System.out.println(status.getUser().getName() + " : " + status.getText());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }
        };
        
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        //specify the consumer key from the twitter app
        String consumerKey = "glwUfufMmiXnjBVucTRVjIbKe";
        //specify the consumerSecret key from the twitter app
        String consumerSecret = "YyFv1bdeZbngJCVPTFwkTWP6Bs7o22u6FJ0dFTkNx53R04myyi";
        //specify the token key from the twitter app
        String token = "2841712675-U9ecwsaEEUDsoT88J2MYwQ8s6IH2f0taraIbAi6";
        //specify the secret key from the twitter app
        String secret = "uHrHPgd432SrvYMj6i9n43nC8rbDuixtxnD59ZHMyI5lq";
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(token);
        cb.setOAuthAccessTokenSecret(secret);
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        twitterStream.addListener(listener);

        twitterStream.onStatus(status -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", status.getId());
            jsonObject.put("text", status.getText());
            jsonObject.put("username", status.getUser().getScreenName());
            jsonObject.put("timestamp", status.getCreatedAt().getTime());
            jsonObject.put("status", "tbd");
            Application.sendOutput(jsonObject);
        }).sample("en");

    }
    public static void main(String[] args) throws TwitterException {

        Application.streamFeed();
    }
    public static void sendOutput(JSONObject status){
       
    		Application.producer.send(new ProducerRecord<String,String>("tweets_topic", "", status.toString()), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e!=null){
                    System.out.println("Something went wrong "+e.getMessage());
                }
            }
        });
        System.out.println(status);
    }

}
