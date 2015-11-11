package bertrand.jms.demo1;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;

public class Chat implements MessageListener {
	
	private TopicSession pubSession;
	private TopicPublisher publisher;
	private TopicConnection connection;
	private String username;
	
	public Chat(String topicFactory, String topicName, String username) throws Exception {
		InitialContext ctx = new InitialContext();
		TopicConnectionFactory connectionFactory = (TopicConnectionFactory) ctx.lookup(topicFactory);
		TopicConnection connection = connectionFactory.createTopicConnection();
		TopicSession pubSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSession subSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic chatTopic = (Topic) ctx.lookup(topicName);
		
		TopicPublisher publisher = pubSession.createPublisher(chatTopic);
		TopicSubscriber subscriber = subSession.createSubscriber(chatTopic, null, true);
		
		subscriber.setMessageListener(this);
		
		this.connection = connection;
		this.pubSession = pubSession;
		this.publisher = publisher;
		this.username = username;
		
		connection.start();
	}

	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		try {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			System.out.println(text);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void writeMessage(String text) throws JMSException {
		TextMessage message = pubSession.createTextMessage();
		message.setText(username + ": " + text);
		publisher.publish(message);
	}
	
	public void close() throws JMSException {
		connection.close();
	}
	
	public static void main(String[] args) throws Exception {
//		if(args.length != 3) {
//			System.out.println("Factory, Topic, or username missing!");
//		}
//		Chat chat = new Chat(args[0], args[1], args[2]);
		Chat chat = new Chat("TopicCF", "topic1", "Bertrand");
		BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			String s = commandLine.readLine();
			if(s.equalsIgnoreCase("exit")) {
				chat.close();
				break;
			}else {
				chat.writeMessage(s);
			}
		}
	}

}
