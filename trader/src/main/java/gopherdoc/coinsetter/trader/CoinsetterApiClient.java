package gopherdoc.coinsetter.trader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinsetterApiClient 
{
	static JSONParser json = new JSONParser();
	static Logger log = LoggerFactory.getLogger(CoinsetterApiClient.class);
	static Scanner input = new Scanner(System.in);
	static DefaultHttpClient http = new DefaultHttpClient();
	static String coinsetterUri;
	
	static String customerUuid;
	static String accountUuid;
	static String clientSessionUuid;
	static boolean loggedIn = false;
	
	static boolean isProduction = false;
	static boolean isStaging = false;
	
	static String operatingSystem = System.getProperty("os.name");
	
    public static void main( String[] args )
    {
    	printWelcome();
    	
    	String loginDecider = input.next();
    	if(loginDecider.equals("1")) {
			coinsetterUri = "https://api.coinsetter.com/v1";
			isProduction = true;
		}
		else if(loginDecider.equals("2")) {
			coinsetterUri = "https://staging-api.coinsetter.com/v1";
			isStaging = true;
		}
		else {
			System.out.println("Exiting.");
			System.exit(0);
		}
    	while (!loggedIn){
	    	System.out.print("\nEnter your Username: ");
	        String user = input.next();
	        System.out.print("\nEnter your Password: ");
	        String pass = input.next();
	        System.out.print("\nEnter your IP Address: ");
	        String ipAd = input.next();
	        login(user, pass, ipAd);
    	}
    	printMenu();
    	
    	while (loggedIn) {

    		System.out.println("\nType 0 to display the menu ");
    		
            int choice = 0;
            try {
            	choice = input.nextInt();
            } catch(Exception e){
            	input.nextLine();
            }
            
            switch(choice) {
            case 0:
            	printMenu();
            	break;
            case 1: 
            	getLastMarketData();
            	break;
            case 2: 
            	getTickerMarketData();
            	break;
            case 3: 
            	getDepthMarketData();
            	break;
            case 4:
            	getFullDepthMarketData();
            	break;
            case 5:
            	getQuoteMarketData();
            	break;
            case 6: 
            	getAccountList();
            	break;
            case 7: 
            	getAccount();
            	break;
            case 8: 
            	getHeartbeat();
            	break;
            case 9: 
            	getFinancialTransactionList();
            	break;
            case 10: 
            	getFinancialData();
            	break;
            case 11: 
            	addOrder();
            	break;
            case 12: 
            	listOpenOrders();
            	break;
            case 13: 
            	getOrder();
            	break;
            case 14: 
            	cancelOrderProcess();
            	break;
            case 15:
            	addPriceAlert();
            	break;
            case 16:
            	listPriceAlerts();
            	break;
            case 17:
            	cancelPriceAlert();
            	break;
            case 18:
            	listNewsAlerts();
            	break;
            case 19: 
            	ping();
            	break;
            case 20: 
            	logout();
            	System.exit(0);
            	break;
            default:
            	printMenu();
            	break;
            }
    	}
    	
    }
    
    static void login(String user, String pass, String IpAd) {
    	try {
    		//Create and send the http request
    		HttpPost httpRequest = new HttpPost(coinsetterUri + "/clientSession");
    		httpRequest.addHeader("Content-Type", "application/json");
    		String httpData = "{\"username\":\"" + user +"\",\"ipAddress\":\"" + IpAd + "\",\"password\":\"" + pass +"\"}";
    		httpRequest.setEntity(new StringEntity(httpData));

    		System.out.println("curl Request:\ncurl -X POST -H 'Content-Type: application/json' -d '" + "{\"username\":\"" + user +"\",\"ipAddress\":\"" + IpAd + "\",\"password\":\"" + "*HIDDEN-FOR-API-DEMO*" +"\"}" + "' " + coinsetterUri + "/clientSession");

    		HttpResponse response = http.execute(httpRequest);
    		
    		String responseContent  = printResponse(response);

    		if (response.getStatusLine().getStatusCode() == 200) {
    			loggedIn = true;
    			JSONObject loginJson = (JSONObject) json.parse(responseContent);
        		clientSessionUuid = (String) loginJson.get("uuid");
        		customerUuid = (String) loginJson.get("customerUuid");
        		
        		System.out.println("\nRetrieving your Account UUID...\n");
        		getAccountList();
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getLastMarketData() {
    	try {
        	//Create and send the http request
    		System.out.println("How many trades would you like to look back?");
    		while(!input.hasNextInt()) {
    			System.out.println("Please enter an integer");
    			input.nextLine();
    		}
    		
    		int lookback = input.nextInt();
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/marketdata/last?lookback=" + Integer.toString(lookback));
    		System.out.println("curl Request:\ncurl -X GET " + coinsetterUri + "/marketdata/last?lookback=" + Integer.toString(lookback));
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getTickerMarketData() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/marketdata/ticker");
    		System.out.println("curl Request:\ncurl -X GET " + coinsetterUri + "/marketdata/ticker");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getDepthMarketData() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/marketdata/depth");
    		System.out.println("curl Request:\ncurl -X GET " + coinsetterUri + "/marketdata/depth");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getFullDepthMarketData() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/marketdata/full_depth");
    		System.out.println("curl Request:\ncurl -X GET " + coinsetterUri + "/marketdata/full_depth");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getQuoteMarketData() {
    	try {
    		System.out.println("How many BTC would you like to find the price of? (Must be less than 150 and a multiple of 5)");
            while(!input.hasNextBigDecimal()) {
    			System.out.println("Please enter a valid input");
    			input.nextLine();
    		}
            
            int btcAmount = input.nextInt();
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/marketdata/quote?quantity=" + Integer.toString(btcAmount));
    		System.out.println("curl Request:\ncurl -X GET " + coinsetterUri + "/marketdata/quote?quantity=" + Integer.toString(btcAmount));
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getAccountList() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/customer/account");
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/customer/account");
    		HttpResponse response = http.execute(httpRequest);
    		
    		String responseContent = printResponse(response);
    		
    		//Set accountUuid
    		JSONObject accountJson = (JSONObject) json.parse(responseContent);
    		JSONArray accountList = (JSONArray) accountJson.get("accountList");
    		JSONObject account = (JSONObject) accountList.get(0);
    		accountUuid = (String) account.get("accountUuid");
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getAccount() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/customer/account/" + accountUuid);
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/customer/account/" + accountUuid);
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getHeartbeat() {
    	try {
	    	//Create and send the http request
			HttpPut httpRequest = new HttpPut(coinsetterUri + "/clientSession/" + clientSessionUuid +"?action=HEARTBEAT");
			httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X PUT -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/clientSession/" + clientSessionUuid +"?action=HEARTBEAT");
			HttpResponse response = http.execute(httpRequest);
			
			//Print the input stream the request returns as a string
			if(response.getStatusLine().getStatusCode() != 200){System.out.println("Request ERROR");}
    		System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
    		System.out.println("Response Phrase: " + response.getStatusLine().getReasonPhrase());
    		System.out.println("Response:");
			String responseContent = inputStreamtoString(response.getEntity().getContent());
			System.out.println(responseContent);
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    static void getFinancialTransactionList() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/customer/account/" + accountUuid + "/financialTransaction");
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/customer/account/" + accountUuid + "/financialTransaction");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getFinancialData() {
    	try {
    		System.out.println("Enter Financial Transaction UUID (Can be obtained with 'Get FinancialTransactionList')");
    		String financialTransactionUuid = input.next();
    		    		
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/financialTransaction/" + financialTransactionUuid);
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/financialTransaction/" + financialTransactionUuid);
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    /**
     * This api method has been deprecated
     */
    /*static void listPositions() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/customer/account/" + accountUuid + "/position");
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/customer/account/" + accountUuid + "/position");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }*/
    
    static void addOrder() {
    	try {
    		if(isProduction){
    			System.out.println(
    					"############################################################\n" +
    					"WARNING!!!! \n" +
    					"You are connected to the LIVE PRODUCTION SERVER!\n" +
    					"Any order that you place will be a LIVE order involving REAL MONEY!\n" +
    					"############################################################\n"
    			);
    		}
    		
    		String side=null;
    		String type=null;
    		int    routingMethod=1;
    		    		
    		System.out.print("\nType 1 to buy and 2 to sell: ");
    		input.nextLine();
    		boolean validInput=false; 
            while(!validInput)
            {
	            while(!input.hasNextInt()) {
	    			System.out.println("Please enter an integer");
	    			input.nextLine();
	    		}
	            int sideInt = input.nextInt();
	            if(sideInt==1) {
	            	side="BUY"; 
	            	validInput=true;
	            }
	            else if (sideInt==2) {
	            	side="SELL"; 
	            	validInput=true;
	            }
	            else {
	            	System.out.println("Please pick either 1 or 2"); 
	            	input.nextLine();
	            }
	        }

            System.out.print("\nType 1 to make a limit order and 2 to make a market order: ");
            input.nextLine();
            validInput=false; 
            while(!validInput)
            {
	            while(!input.hasNextInt()) {
	    			System.out.println("Please enter an integer");
	    			input.nextLine();
	    		}
	            int typeInt = input.nextInt();
	            if(typeInt==1) {
	            	type="LIMIT"; 
	            	validInput=true;
	            }
	            else if (typeInt==2) {
	            	type="MARKET"; 
	            	validInput=true;
	            }
	            else {
	            	System.out.println("Please pick either 1 or 2"); 
	            	input.nextLine();
	            }
	        }	

            
            System.out.print("\nType 1 to route the order to SMART or type 2 to route the order to COINSETTER: ");
            while(!input.hasNextInt()) {
    			System.out.println("Please enter an integer");
    			input.nextLine();
    		}
            int typeRoutingInt = input.nextInt();
            routingMethod = typeRoutingInt == 2 ? 2 : 1;
            
            
            BigDecimal price = null;
            
            if (type == "LIMIT") {
                System.out.print("\nEnter Price: ");
                while(!input.hasNextBigDecimal()) {
        			System.out.println("Please enter a valid input");
        			input.nextLine();
        		}
                price = input.nextBigDecimal();
            }
            
            System.out.print("\nEnter quantity: ");
            while(!input.hasNextBigDecimal()) {
    			System.out.println("Please enter a valid input");
    			input.nextLine();
    		}
            BigDecimal qty = input.nextBigDecimal();
    		
    		//Create and send the http request
    		HttpPost httpRequest = new HttpPost(coinsetterUri + "/order");
    		httpRequest.addHeader("Content-Type", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		String httpData = "{\"accountUuid\":\"" + accountUuid +"\",\"customerUuid\":\"" + customerUuid + "\",\"orderType\":\"" + type +"\",\"requestedQuantity\":" + qty + ",\"requestedPrice\":" + price + ",\"side\":\"" + side +"\",\"symbol\":\"BTCUSD\",\"routingMethod\":"+routingMethod+"}";
    		httpRequest.setEntity(new StringEntity(httpData));
    		System.out.println("curl Request:\ncurl -X POST -H 'Content-Type: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' -d '" + httpData + "' " + coinsetterUri + "/order");
    		HttpResponse response = http.execute(httpRequest);
    		
    		//Print the input stream the request returns as a string and get client session info for future calls
    		if(response.getStatusLine().getStatusCode() != 200){System.out.println("Request ERROR");}
    		System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
    		System.out.println("Response Phrase: " + response.getStatusLine().getReasonPhrase());
    		System.out.println("Response:");
    		String responseContent = inputStreamtoString(response.getEntity().getContent());
    		System.out.println(responseContent);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void listOpenOrders() {
    	try { 		
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/customer/account/" + accountUuid + "/order");
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/customer/account/" + accountUuid + "/order");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void getOrder() {
    	try {
    		System.out.println("Enter Order UUID (Can be obtained with 'List Open Orders')");
    		String orderUuid = input.next();
    		    		
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/order/" + orderUuid);
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/order/" + orderUuid);
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void cancelOrderProcess() {
    	try {
    		System.out.println("Enter Order UUID (Can be obtained with 'List Open Orders')");
    		String orderUuid = input.next();
    		    		
        	//Create and send the http request
    		HttpDelete httpRequest = new HttpDelete(coinsetterUri + "/order/" + orderUuid);
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X DELETE -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/order/" + orderUuid);
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void ping() {
    	try {
    		System.out.println("\nEnter the string, up to 12 characters, that you wish to ping: ");
    		String quote = input.next();
    		    		
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/ping/" + quote);
    		System.out.println("curl Request:\ncurl -X GET " + coinsetterUri + "/ping/" + quote);
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void addPriceAlert() {
    	try {
    		System.out.println("How would you like to be notified? Type 1 for Email, 2 for Text, 3 for Both.");
    		String type = null;
    		input.nextLine();
    		boolean validInput=false;
    		while(!validInput)
    		{
	    		while(!input.hasNextInt()) {
	    			System.out.println("Please enter an integer");
	    			input.nextLine();
	    		}
	            int typeInt = input.nextInt();	
	
	            if (typeInt == 1) {
	            	type = "EMAIL";
	            	validInput=true;
	            } else if (typeInt == 2) {
	            	type = "TEXT";
	            	validInput=true;
	            } else if (typeInt == 3) {
	            	type = "BOTH";
	            	validInput=true;
	            }
	            else {
	            	System.out.println("Please pick 1, 2 or 3");
	            	input.nextLine();
	            }
    		}
    		
            System.out.println("At what price would you like to be notified?");
            BigDecimal price=null;
            input.nextLine();
            validInput=false; 
            while(!validInput)
            {
	            while(!input.hasNextBigDecimal()) {
	    			System.out.println("Please enter a valid input");
	    			input.nextLine();
	    		}
	            price = input.nextBigDecimal();
	            if(price.compareTo(new BigDecimal(999999))<0&&price.compareTo(new BigDecimal(0))>0)//price must be between 0 and 999999
	            {
	            	validInput=true;
	            }
	            else
	            {
	            	System.out.println("Price must be between $0 and $999,999");
	            	input.nextLine();
	            }
            }
            
        	//Create and send the http request
    		HttpPost httpRequest = new HttpPost(coinsetterUri + "/pricealert");
    		httpRequest.addHeader("Content-Type", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		String httpData = "{\"type\":\"" + type +"\",\"condition\":\"CROSSES\",\"price\":" + price +",\"symbol\":\"BTCUSD\"}";
    		httpRequest.setEntity(new StringEntity(httpData));
    		System.out.println("curl Request:\ncurl -X POST -H 'Content-Type: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' -d '" + httpData + "' " + coinsetterUri + "/pricealert");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void listPriceAlerts() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/pricealert");
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/pricealert");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void cancelPriceAlert() {
    	try {
    		System.out.println("Enter Price Alert UUID (Can be obtained with 'List Price Alerts')");
    		String alertUuid = input.next();
    		    		
        	//Create and send the http request
    		HttpDelete httpRequest = new HttpDelete(coinsetterUri + "/pricealert/" + alertUuid);
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X DELETE -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/pricealert/" + alertUuid);
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void listNewsAlerts() {
    	try {
        	//Create and send the http request
    		HttpGet httpRequest = new HttpGet(coinsetterUri + "/newsalert");
    		httpRequest.addHeader("Accept", "application/json");
    		httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X GET -H 'Accept: application/json' -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/newsalert");
    		HttpResponse response = http.execute(httpRequest);
    		
    		printResponse(response);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    static void logout() {
    	try {
	    	//Create and send the http request
			HttpPut httpRequest = new HttpPut(coinsetterUri + "/clientSession/" + clientSessionUuid + "?action=LOGOUT");
			httpRequest.addHeader("coinsetter-client-session-id", clientSessionUuid);
    		System.out.println("curl Request:\ncurl -X PUT -H 'coinsetter-client-session-id:" + clientSessionUuid + "' " + coinsetterUri + "/clientSession/" + clientSessionUuid + "?action=LOGOUT");
			HttpResponse response = http.execute(httpRequest);
			
			printResponse(response);
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    private static String inputStreamtoString(InputStream stream) {
    	 
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
 
		String line;
 
		reader = new BufferedReader(new InputStreamReader(stream));
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
 
		return builder.toString();
	}
    
    private static void printWelcome() {
    	String environment = null;
    	
    	try {
    		if(operatingSystem.toLowerCase().indexOf("win") >= 0){ //windows os
    			environment = "Windows Environment " + operatingSystem;
    			Runtime.getRuntime().exec("cls");
    		} else {
    			environment = "Unix Environment " + operatingSystem;
    			String ESC = "\033[";
    			System.out.print(ESC + "2J"); 
    			Runtime.getRuntime().exec("clear"); //unix os
    		}
    	} catch(IOException e){
    		System.out.println(e.getMessage());
    	}
    	
    	System.out.println(
    			"############################################################\n" +
    			"Welcome, thank-you for using the Coinsetter API Demo.\n" +
    			"This demo uses Coinsetter's RESTful/JSON API.\n" +
    			"To begin, please select a server.\n" +
    			"\n" +
    			"Type 1 to login to our production server (LIVE TRADING!!)\n" +
    			"\n" +
    			"Type 2 to login to our staging server\n" + 
    			"\n" +
    			"Type anything else to quit. \n" +
    			"\n\n" + 
    			environment + "\n"+
    			"############################################################");
    }
    
    private static void printMenu() {
    	System.out.println(
  			   "\n 1. GET MarketData: Last.\n "
 				+ "2. GET MarketData: Ticker.\n "
 				+ "3. GET MarketData: Depth.\n "
 				+ "4. GET MarketData: Full Depth.\n "
 				+ "5. GET MarketData: Quote.\n "
 				+ "6. GET Account List.\n "
 				+ "7. GET Account.\n "
 				+ "8. PUT Heartbeat.\n "
 				+ "9. GET Financial Transaction List.\n "
 				+ "10. GET Financial Data.\n "
 				+ "11. POST Place a buy or sell order (MARKET or LIMIT).\n "
 				+ "12. GET List Open Orders.\n "
 				+ "13. GET Order.\n "
 				+ "14. DELETE Cancel an order.\n "
 				+ "15. POST PriceAlert: Add.\n "
 				+ "16. GET PriceAlert: List.\n "
 				+ "17. DELETE PriceAlert: Remove.\n "
 				+ "18. GET NewsAlert: List.\n "
 				+ "19. GET Ping the site\n "
 				+ "20. PUT QUIT Logout and exit.\n");
    }
    
    private static String printResponse(HttpResponse response){
    	String responseContent = null;
    	
    	try {
	    	//Print the input stream the request returns as a string
    		System.out.print("\n");
			if(response.getStatusLine().getStatusCode() != 200){System.out.println("Request ERROR");}
			System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
			System.out.println("Response Phrase: " + response.getStatusLine().getReasonPhrase());
			System.out.println("Response:");
			responseContent = inputStreamtoString(response.getEntity().getContent());
			System.out.println(responseContent);
			
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	
		return responseContent;
    }
}
