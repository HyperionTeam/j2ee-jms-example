package bertrand.jms.queue;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class QLender implements MessageListener {

	private QueueConnection connection;
	private QueueSession session;
	private Queue requestQ;
	
	public QLender(String queuecf, String requestQueue) throws Exception {
		Context ctx = new InitialContext();
		QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup(queuecf);
		connection = factory.createQueueConnection();
		session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		requestQ = (Queue) ctx.lookup(requestQueue);
		connection.start();
		
		QueueReceiver receiver = session.createReceiver(requestQ);
		receiver.setMessageListener(this);
		System.out.println("Waiting for loan requests...");
	}
	
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		try {
			boolean accepted = false;
			MapMessage msg = (MapMessage)message;
			double salary = msg.getDouble("Salary");
			double loanAmt = msg.getDouble("LoanAmount");
			
			if(loanAmt < 200000) {
				accepted = (salary / loanAmt) > .25;
			}else {
				accepted = (salary / loanAmt) > .33;
			}
			System.out.println("Percent = " + (salary/loanAmt) + ", loan is " + (accepted ? "Accepted!" : "Declined"));
			TextMessage tmsg = session.createTextMessage();
			tmsg.setText(accepted?"Accepted!" : "Declined");
			tmsg.setJMSCorrelationID(message.getJMSMessageID());
			
			QueueSender sender = session.createSender((Queue)message.getJMSReplyTo());
			sender.send(tmsg);
			System.out.println("\nWaiting for loan requests...");
		}catch(Exception e) {
			
		}
	}
	
	private void exit() {
		try {
			connection.close();
		}catch(Exception e) {
		}
		System.exit(0);
	}
	
	public static void main(String[] args) throws Exception {
		String queuecf = "QueueCF";
		String requestQueue = "LoanRequestq";
		
		QLender lender = new QLender(queuecf, requestQueue);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("QLender application started");
		System.out.println("Press enter to quit application");
		reader.readLine();
		lender.exit();
	}
	
}
