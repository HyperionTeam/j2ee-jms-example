package bertrand.jms.queue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.MapMessage;
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
import javax.naming.NamingException;

public class QBorrower {
	
	private QueueConnection connection;
	private QueueSession session;
	private Queue responseQ;
	private Queue requestQ;

	public QBorrower(String queuecf, String requestQueue, String responseQueue) throws Exception {
		Context ctx = new InitialContext();
		QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup(queuecf);
		connection = factory.createQueueConnection();
		session = connection.createQueueSession(false,  Session.AUTO_ACKNOWLEDGE);
		requestQ = (Queue) ctx.lookup(requestQueue);
		responseQ = (Queue) ctx.lookup(responseQueue);
		connection.start();
	}
	
	private void sendLoanRequest(double salary, double loanAmt) throws Exception {
		MapMessage msg = session.createMapMessage();
		msg.setDouble("Salary", salary);
		msg.setDouble("LoanAmount", loanAmt);
		msg.setJMSReplyTo(responseQ);
		
		msg.setJMSExpiration(new Date().getTime() + 30000);
		QueueSender sender = session.createSender(requestQ);
		sender.send(msg);
		
		String filter = "JMSCorrelationID = '" + msg.getJMSMessageID() + "'";
		QueueReceiver receiver = session.createReceiver(responseQ, filter);
//		QueueReceiver receiver = session.createReceiver(responseQ);
		TextMessage tmsg = (TextMessage) receiver.receive(30000);
		if(null == tmsg) {
			System.out.println("Lender not responding");
		}else {
			System.out.println("Loan request was " + tmsg.getText() + " " + tmsg.getJMSMessageID());
		}
		receiver.close();
	}
	
	private void exit() {
		try {
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void main(String[] args) throws Exception {
		String queuecf = "QueueCF";
		String requestQueue = "LoanRequestq";
		String responseQueue = "LoanResponseq";
		
		QBorrower borrower = new QBorrower(queuecf, requestQueue, responseQueue);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("QBorrower Application Started");
		System.out.println("Press enter to quit application");
		System.out.println("Enter: Salary, Loan_Amount");
		
		while(true) {
			System.out.println(">");
			
			String loanRequest = reader.readLine();
			if(null == loanRequest || loanRequest.trim().length() == 0) {
				borrower.exit();
			}
			
			StringTokenizer tokenizer = new StringTokenizer(loanRequest, ",");
			double salary = Double.valueOf(tokenizer.nextToken().trim()).doubleValue();
			double loanAmt = Double.valueOf(tokenizer.nextToken().trim()).doubleValue();
			borrower.sendLoanRequest(salary, loanAmt);
		}
	}
	
}
