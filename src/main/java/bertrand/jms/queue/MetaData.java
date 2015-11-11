package bertrand.jms.queue;

import static java.lang.System.out;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

public class MetaData {

	public static void main(String[] args) throws Exception {
		Context ctx = new InitialContext();
		QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("QueueCF");
		QueueConnection connection = factory.createQueueConnection();
		ConnectionMetaData metaData = connection.getMetaData();
		out.println("JMS Version: " + metaData.getJMSMajorVersion());
		out.println("JMS Provider: " + metaData.getJMSProviderName());
		out.println("JMS Provider Version: " + metaData.getProviderVersion());
		out.println("JMSX Properties Supported: ");
		Enumeration e = metaData.getJMSXPropertyNames();
		while(e.hasMoreElements()) {
			out.println("    " + e.nextElement());
		}
	}
}
