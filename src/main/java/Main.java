import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import java.math.BigDecimal;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;

import com.heroku.sdk.jdbc.DatabaseUrl;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

public class Main {
/*	
	private static BraintreeGateway gateway = new BraintreeGateway(
		Environment.PRODUCTION,
		"69ppkf6h8fqh9cxb",
		"svp64bn3p56344yj",
		"fa458ae542e48d150ed2d456d28f16b7");
*/
	private static BraintreeGateway gateway;


	public static void main(String[] args) {
	
		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");
		get("/hello", (request, response) ->{
	       	return "Hello World!";
	       });
	             		
		post("/postIOSToken" , (req, res) -> {
			String token = req.queryParams("token");
			String UDID = req.queryParams("UDID");
			String deviceToken = req.queryParams("deviceToken");
			
			System.out.println("Channel ID: " + token);
			System.out.println("UDID: " + UDID);
			System.out.println("Device Token: " + deviceToken);
			return token;
		});
	        
        		post("/checkout", (req, res) -> {
				
				String nonce = req.queryParams("payment_method_nonce");
				String email = req.queryParams("email");
				String amount = req.queryParams("amount");
				String devProd = req.queryParams("devProd");
				
				Double amountDouble = Double.valueOf(amount);
				
				if (devProd.equals("production")){
					gateway = new BraintreeGateway(
						Environment.PRODUCTION,
						"69ppkf6h8fqh9cxb",
						"svp64bn3p56344yj",
						"fa458ae542e48d150ed2d456d28f16b7"
						);	
				}
				
				if (devProd.equals("sandbox")){
					gateway = new BraintreeGateway(
					    Environment.SANDBOX,
				   	    "9j46c9m8t3mjfwwq",
					    "9fhk7sty57gz2fmx",
					    "edbf53fbe7189a0a7412e9e86b23575b"
						);	
				}
				
				System.out.println("-----------------------Purchase-----------------------");
				System.out.println("Nonce: " + nonce);
				System.out.println("Email: " + email);
				System.out.println("Amount: " + amountDouble);
				
				TransactionRequest request = new TransactionRequest()
				.amount(new BigDecimal(amount))
				.paymentMethodNonce(nonce)
				.customer()
				  .email(email)
				  .done()
				//.merchantAccountId("JobsME_marketplace")
				.options()
				  .submitForSettlement(true)
				  .done();
		   
				Result<Transaction> result = gateway.transaction().sale(request);
				
				String status = "";
			
				if (result.isSuccess() == true){
					Transaction transaction = result.getTarget();
					transaction.getStatus();
					System.out.println("***Payment Success --> Status: " + transaction.getStatus() + "***");
				}
		   
				if (result.isSuccess() == false)
				{
					System.out.println("***PAYMENT FAILED***");
					Transaction transaction = result.getTransaction();
					
					transaction.getProcessorResponseCode();
					// e.g. "2001"
					transaction.getProcessorResponseText();
					// e.g. "Insufficient Funds"
					System.out.println("Status: " + transaction.getStatus());
					System.out.println("Response Code: " + transaction.getProcessorResponseCode());
					System.out.println("Response Text: " + transaction.getProcessorResponseText());
				}
				
				System.out.println("-----------------------End of Purchase-----------------------");

				return result.isSuccess() + ": Payment Success!";
	   
			});	
        
        post("/sendPush", (req, res) -> {
        	
        	System.out.println("///////////////////////Message///////////////////////");
        	
			String to = req.queryParams("to");
			String os = req.queryParams("os");
			String title = req.queryParams("title");
			String message = req.queryParams("message");
			String type = req.queryParams("type");
			String braceletId = req.queryParams("braceletId");
			String devProd = req.queryParams("devProd");
			
			System.out.println("To: " + to);
			System.out.println("OS: " + os);
			System.out.println("Message: " + message);
	
			if (os.equals("android")){
				 try {
					  
					JSONObject messageDetails = new JSONObject();

					try {
						messageDetails.put("title",title);
						messageDetails.put("message",message);
						messageDetails.put("to", to);
						messageDetails.put("type", type);
						messageDetails.put("braceletId", braceletId);
					} catch (JSONException e){

					}
									  
						System.out.println("In Android Push Section");
			            // Prepare JSON containing the GCM message content. What to send and where to send.
			            JSONObject jGcmData = new JSONObject();
			            JSONArray regIds = new JSONArray();
			            JSONObject jsonMessage = new JSONObject();
			 
						regIds.put(to);
						jsonMessage.put("message", messageDetails);
			   
						jGcmData.put("registration_ids", regIds);
						jGcmData.put("data", jsonMessage);
						
			            // Create connection to send GCM Message request.
			            //URL url = new URL("https://android.googleapis.com/gcm/send");
						URL url = new URL("https://pushy.me/push?api_key=144f5ee08d5c0ead05247a144a916e9d035aec539fb4a9779beef8bb2ed79721");
			            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			            //conn.setRequestProperty("Authorization", "key=" + API_KEY);
			            conn.setRequestProperty("Content-Type", "application/json");
			            conn.setRequestMethod("POST");
			            conn.setDoOutput(true);

			            // Send GCM message content.
			            OutputStream outputStream = conn.getOutputStream();
			            outputStream.write(jGcmData.toString().getBytes());

			            // Read GCM response.
			            InputStream inputStream = conn.getInputStream();
			            String resp = IOUtils.toString(inputStream);
			            System.out.println(resp);
			       
			        } catch (IOException e) {
			           e.getMessage();
			            e.printStackTrace();
			        }
			}
			
			else if (os.equals("ios")){
				try {
					
					String tempJSON = "{\"audience\": {\"device_token\":\"EA9A6A7181A8584EA61A6028B7223F09E1E3DC3906B39ACE991CA18661900E61\"},\"notification\": {\"alert\": \"yo whats up@@\"},\"device_types\": [\"ios\"]}";
					
					System.out.println("In iOS Push Section");
					JSONObject jGcmData = new JSONObject();
					JSONObject notifications = new JSONObject();
					
					JSONObject iosChannel = new JSONObject();
					iosChannel.put("device_token",to);
					//iosChannel.put("ios_channel", to);
					
					notifications.put("alert", message);
				
					//jGcmData.put("audience", "all");
					jGcmData.put("audience", iosChannel);
					jGcmData.put("device_types", "all");
					jGcmData.put("notification", notifications);
					
					System.out.println(jGcmData.toString());
					
					URL url = new URL("https://go.urbanairship.com/api/push");
		            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		           
				    //Sandbox
		            //String userCredentials = "toDJ-fwhRj211DjZUWL80w:hooZT_vcStG4sWYYurKM5A";
					
					//Production
					String userCredentials = "eXJUniLXSk2Tm1RZq0qgMQ:8H3dKUwAT1iqKoW9uCvCWg";
					
		            String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
		            
		            //conn.setRequestProperty  ("Authorization", "Basic " + encoding);
		            conn.setRequestProperty  ("Authorization", basicAuth);
		   
		            conn.setRequestProperty("Accept", "application/vnd.urbanairship+json; version=3");
		            conn.setRequestProperty("Content-Type", "application/json");
		            conn.setRequestMethod("POST");
		            conn.setDoOutput(true);
		
		            // Send GCM message content.
		            OutputStream outputStream = conn.getOutputStream();
		            //outputStream.write(jGcmData.toString().getBytes());
					outputStream.write(jGcmData.toString().getBytes());
		
		            // Read GCM response.
		            InputStream inputStream = conn.getInputStream();
		            String resp = IOUtils.toString(inputStream);
		            System.out.println(resp);
		            
		        } catch (IOException e) {
		        	System.out.println("Error: " + e.getMessage());
					//System.out.println("Error Body: " + e.printStackTrace());
			
		        }
			}
			
     		System.out.println("///////////////////////End of Message///////////////////////");	       
	       	return "Attempt Made to Push Server";
		});

		post("/BraceletAddedEmail", (req, res) -> {
        	
        	String braceletId = req.queryParams("braceletId");
        
        	URL url = new URL("https://api.createsend.com/api/v3.1/transactional/smartemail/eed1daea-68ee-4242-bb90-c7ea6f4e7ffb/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            //JSONObject sendThis = new JSONObject("{\"To\": [\"Gurinder Bhangoo <G@JobsMEPlatform.com>\"]}");
            
            JSONObject finalObject = new JSONObject();
            JSONObject braceletId2 = new JSONObject();
            JSONArray sendTo = new JSONArray();
            
            
            sendTo.put("Gurinder Bhangoo <G@JobsMEPlatform.com>");
            finalObject.put("To", sendTo);
      
            braceletId2.put("braceletId", braceletId);
            finalObject.put("Data", braceletId2);
    
		    //Sandbox
            //String userCredentials = "toDJ-fwhRj211DjZUWL80w:hooZT_vcStG4sWYYurKM5A";
			
			//Production
			String userCredentials = "967f06c3e197cfc501e0441133ad82f99dd37c7e853bd8c0:.";
			
            String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
            
            //conn.setRequestProperty  ("Authorization", "Basic " + encoding);
            conn.setRequestProperty  ("Authorization", basicAuth);
   
          
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            //outputStream.write(jGcmData.toString().getBytes());
			outputStream.write(finalObject.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
       
        	
        	return "End of API Request";
        });
		
		post("/OrderMadeEmail", (req, res) -> {
        	
        	String orderAmount = req.queryParams("orderAmount");
        	
        	URL url = new URL("https://api.createsend.com/api/v3.1/transactional/smartemail/d9af158b-b37e-4069-acde-ef77afcbd09c/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            //JSONObject sendThis = new JSONObject("{\"To\": [\"Gurinder Bhangoo <G@JobsMEPlatform.com>\"]}");
            
            JSONObject finalObject = new JSONObject();
            JSONObject putOrder = new JSONObject();
            JSONArray sendTo = new JSONArray();
            
            
            sendTo.put("Gurinder Bhangoo <G@JobsMEPlatform.com>");
            finalObject.put("To", sendTo);
      
            putOrder.put("totalAmount", orderAmount);
            finalObject.put("Data", putOrder);
    
		    //Sandbox
            //String userCredentials = "toDJ-fwhRj211DjZUWL80w:hooZT_vcStG4sWYYurKM5A";
			
			//Production
			String userCredentials = "967f06c3e197cfc501e0441133ad82f99dd37c7e853bd8c0:.";
			
            String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
            
            //conn.setRequestProperty  ("Authorization", "Basic " + encoding);
            conn.setRequestProperty  ("Authorization", basicAuth);
   
          
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            //outputStream.write(jGcmData.toString().getBytes());
			outputStream.write(finalObject.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
       
        	
        	return "End of API Request";
        });
		
		
		
		
		
		
		
		
			
			
			
			
			
			
			
			
			
			
		get("/", (request, response) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");

			return new ModelAndView(attributes, "index.ftl");
		}, new FreeMarkerEngine());

		get("/db", (req, res) -> {
		  Connection connection = null;
		  Map<String, Object> attributes = new HashMap<>();
		  try {
			connection = DatabaseUrl.extract().getConnection();

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

			ArrayList<String> output = new ArrayList<String>();
			while (rs.next()) {
			  output.add( "Read from DB: " + rs.getTimestamp("tick"));
			}

			attributes.put("results", output);
			return new ModelAndView(attributes, "db.ftl");
		  } catch (Exception e) {
			attributes.put("message", "There was an error: " + e);
			return new ModelAndView(attributes, "error.ftl");
		  } finally {
			if (connection != null) try{connection.close();} catch(SQLException e){}
		  }
		}, new FreeMarkerEngine());
	}

}
