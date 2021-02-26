package rabbitmqconfig;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MQConfiguration {

    public final static String SENDING_QUEUE = "sendingQueue";
    public final static String EXCHANGE_NAME = "fanountExchange";

    public static Channel createQueue() {

        try {
            Connection conn = createConnection();
            Channel channel = conn.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.queueDeclare(SENDING_QUEUE, false, false, false, null);

            //channel.queueDeclare(RECEIVING_QUEUE, false, false, false, null);
            channel.queueBind(SENDING_QUEUE, EXCHANGE_NAME, "");

            return channel;
        } catch (Exception ex) {
            System.out.println("Rabbit MQ queue cannot be created");
            return null;
        }
    }

    public static Connection createConnection(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
// "guest"/"guest" by default, limited to localhost connections
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setVirtualHost("/");
            factory.setHost("localhost");

            factory.setPort(5672);
            System.out.println("bfr");
            Connection print= factory.newConnection();
            System.out.println("aftr"+print);
            return print ;
        }
        catch (Exception ex){
            System.out.println("Error in rabbitmq connection: "+ex.getMessage());
            return null;
        }
    }
}
