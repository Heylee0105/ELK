package elasticsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class INT008 {
	


    public final static String HOST = "10.140.8.212";
    public final static int PORT = 9300;
    public final static String START = "2018-05-01T15:59:59.999Z";
    public final static String END = "2018-05-15T15:59:59.999Z";

    TransportClient client;

    /**
     * 
     * 
     */
    private void init() throws UnknownHostException{
        // on startup
//    	TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-2")); 
//    	Settings settings = Settings.builder().put("cluster.name", "name").build();
        client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new TransportAddress(InetAddress.getByName(INT008.HOST),INT008.PORT));
        System.out.println("Elasticsearch connect info: " + client.nodeName());
    }
    
    private void close() {
        // on shutdown
        client.close();
    }

    public static void main(String[] args) throws IOException {
    	
        INT008 es = new INT008();
        es.init();
        
        // CEST +02:00 中欧夏令时
        QueryBuilder qbTime = QueryBuilders.rangeQuery("@timestamp").from(INT008.START).to(INT008.END).includeLower(false).includeUpper(false);
        
        
        // Agency request received for Agency 
        // IATA EasyPay (MSTS).
        // IATA EasyPay (EDENRED).
        
		//  *******EDENRED Picked up form Queue*******
		//  "Received message on Inbound AMQP" 
		//  "Routing to PUT-Agency-Edenred"
		//  "Routing to POST-Agency-Edenred" 
        
		// *******MSTS Picked up form Queue*******
		// "Received mesage:" None
		// "Routing to PUT-Agency-MSTS" 
		// "Routing to POST-Agency-MSTS" 
        
        QueryBuilder qb008Receieved = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.matchPhraseQuery("message", "Agency request received for Agency"));
        
        QueryBuilder qb008RMEandBSPLinkReceieved = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.matchPhraseQuery("message", "Agency request received for Agency"))
        		.must(QueryBuilders.boolQuery()
        				.should(QueryBuilders.matchPhraseQuery("message", "\"EventType\":\"Create\""))
        				.should(QueryBuilders.matchPhraseQuery("message", "\"EventType\":\"Update\"")));
        
        QueryBuilder qb008MSTSReceieved = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.matchPhraseQuery("message", "Agency request received for Agency"))
        		.must(QueryBuilders.matchPhraseQuery("message", "\"EventType\":\"IATA EasyPay (MSTS).\""));
        
        QueryBuilder qb008EDENREDReceieved = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.matchPhraseQuery("message", "Agency request received for Agency"))
        		.must(QueryBuilders.matchPhraseQuery("message", "\"EventType\":\"IATA EasyPay (EDENRED).\""));
        
        
        QueryBuilder qbEDENREDPicked = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.matchPhraseQuery("message", "Received message on Inbound AMQP"));
        
        QueryBuilder qbMSTSPicked = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.boolQuery()
        				.should(QueryBuilders.matchPhraseQuery("message", "Routing to PUT-Agency-MSTS"))
        				.should(QueryBuilders.matchPhraseQuery("message", "Routing to POST-Agency-MSTS")));
        
        QueryBuilder qbFailed = QueryBuilders.boolQuery()
        		.must(qbTime)
        		.must(QueryBuilders.matchPhraseQuery("message", "Received message on Inbound AMQP"));
        

        SearchResponse response4INT008 = es.client.prepareSearch("newgen*")
        		.setTypes("log")
        		.setQuery(qb008Receieved)
        		.setSearchType(SearchType.DEFAULT)
        		.setScroll(TimeValue.timeValueMinutes(7))
        		.setSize(10)
        		.addSort("@timestamp", SortOrder.DESC)
        		.get();
        
        SearchResponse response4INT008RMEandBSPLink = es.client.prepareSearch("newgen*")
        		.setTypes("log")
        		.setQuery(qb008RMEandBSPLinkReceieved)
        		.setSearchType(SearchType.DEFAULT)
        		.setScroll(TimeValue.timeValueMinutes(7))
        		.setSize(10)
        		.addSort("@timestamp", SortOrder.DESC)
        		.get();
        
        SearchResponse response4INT008MSTS = es.client.prepareSearch("newgen*")
        		.setTypes("log")
        		.setQuery(qb008MSTSReceieved)
        		.setSearchType(SearchType.DEFAULT)
        		.setScroll(TimeValue.timeValueMinutes(7))
        		.setSize(10)
        		.addSort("@timestamp", SortOrder.DESC)
        		.get();
        
        SearchResponse response4INT008EDENRED = es.client.prepareSearch("newgen*")
        		.setTypes("log")
        		.setQuery(qb008EDENREDReceieved)
        		.setSearchType(SearchType.DEFAULT)
        		.setScroll(TimeValue.timeValueMinutes(7))
        		.setSize(10)
        		.addSort("@timestamp", SortOrder.DESC)
        		.get();
        
        SearchResponse response4INT008EDENREDSucess = es.client.prepareSearch("newgen*")
        		.setTypes("log")
        		.setQuery(qbEDENREDPicked)
        		.setSearchType(SearchType.DEFAULT)
        		.setScroll(TimeValue.timeValueMinutes(7))
        		.setSize(10)
        		.addSort("@timestamp", SortOrder.DESC)
        		.get();
        
        SearchResponse response4INT008MSTSSuccess = es.client.prepareSearch("newgen*")
        		.setTypes("log")
        		.setQuery(qbMSTSPicked)
        		.setSearchType(SearchType.DEFAULT)
        		.setScroll(TimeValue.timeValueMinutes(7))
        		.setSize(10)
        		.addSort("@timestamp", SortOrder.DESC)
        		.get();
        
        
        SearchHits hits4INT008 = response4INT008.getHits();
        SearchHits hits4INT008RMEandBSPLink = response4INT008RMEandBSPLink.getHits();
        SearchHits hits4INT008MSTS = response4INT008MSTS.getHits();
        SearchHits hits4INT008EDENRED = response4INT008EDENRED.getHits();
        SearchHits hits4INT008EDENREDSuccess = response4INT008EDENRED.getHits();
        SearchHits hits4INT008MSTSSucess = response4INT008EDENRED.getHits();
        
        System.out.println("INT008 Received : " + hits4INT008.totalHits);
        System.out.println("INT008 MSTS Received : " + hits4INT008MSTS.totalHits);
        System.out.println("INT008 EDENRED Received : " + hits4INT008EDENRED.totalHits);
        System.out.println("INT008 RME & BSPLink Received : " + hits4INT008RMEandBSPLink.totalHits);
        System.out.println("**********************************");
        System.out.println("INT008 MSTS Sucessed : " + hits4INT008MSTSSucess.totalHits);
        System.out.println("INT008 EDENRED Sucessed : " + hits4INT008EDENREDSuccess.totalHits);

        
        /*
        // shades * size
        int pageNum008 = (int)hits4INT008.totalHits / (1 * 10);
                
        // Get IDs from each hit
        String regx = "([a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})";
        Pattern pattern = Pattern.compile(regx);
        Matcher match = null;
        List<String> ids = new ArrayList<String>();
        
        // get unknown failed ids
		long startTime = System.currentTimeMillis();
        for(int i = 0; i <= pageNumUpdate; i++) {
        	
//        	System.out.println("------------------Page: " + i + "   Reason Unknown ---------------------");
	        for(SearchHit hit : response4BalanceUpdate.getHits()) {
//        		System.out.println(hit.getSourceAsString());
	        	match = pattern.matcher(hit.getSourceAsString());
	        	// query log by ID
	        	if(match.find()) {
//    				System.out.println("ID: " + match.group(1));    			
//    				QueryBuilder qbID = QueryBuilders.matchPhraseQuery("message", match.group(1));
	    			
	    			// Query log by ID and is not known reason 
	    			QueryBuilder qbIDUnknow = QueryBuilders.boolQuery()
	    	        		.must(QueryBuilders.matchPhraseQuery("message", match.group(1)))
	    	        		.must(QueryBuilders.boolQuery()
	    	        				.should(QueryBuilders.matchPhraseQuery("message", "SFDC System API Response: HTTP Response Status: 202"))
	    	        				.should(QueryBuilders.matchPhraseQuery("message", "SFDC System API Response: HTTP Response Status: 404")))
	    	        		.must(qbTime);
	    			
	    			SearchResponse responseUpadateKnown = es.client.prepareSearch("newgen*")
	    	        		.setTypes("log")
	    	        		.setQuery(qbIDUnknow)
	    	        		.get();
	    			
	    			SearchHits hits4IDUnknown = responseUpadateKnown.getHits();
	    			// if don't match known
	    			if (hits4IDUnknown.getTotalHits() == 0)
	    				ids.add(match.group(1));  	
	    		}
	        }
	        response4BalanceUpdate = es.client.prepareSearchScroll(response4BalanceUpdate.getScrollId()).setScroll(new TimeValue(20000)).get();
        }
        
        long endTime = System.currentTimeMillis();
        
        
		File file = null;
		File file404 = null;
		FileWriter fw = null;
		Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dirName = sdf.format(now) + " " + now.getTime();
        String dirName404 = "404";
        String path = "c:/Users/wangjm@iata.org/Desktop/";
        
        file = new File(path + dirName);
        file404 = new File(path + dirName + "/" + dirName404);
        file.mkdirs();
        file404.mkdirs();
        
        // Query and Write Unknow Failed
        for (String id : ids) {
        	QueryBuilder qbIDUnknowFailed = QueryBuilders.boolQuery()
	        		.must(QueryBuilders.matchPhraseQuery("message", id))
	        		.must(qbTime);
        	// 对应的ID 日志不会很多 不用分页
        	SearchResponse response4BalanceUpdateUnknownFailed = es.client.prepareSearch("newgen*")
             		.setTypes("log")
             		.setQuery(qbIDUnknowFailed)
             		.setSize(100)
             		.addSort("@timestamp", SortOrder.DESC)
             		.get();
        	 
        	 SearchHits hits4BalanceUpdateUnknownFailed = response4BalanceUpdateUnknownFailed.getHits();
        	 
        	 file = new File(path + dirName + "/", id + ".txt");
        	 try {
				fw = new FileWriter(file);
				for(SearchHit hit : hits4BalanceUpdateUnknownFailed) {
	 				System.out.println(hit.getSourceAsString());	 				
	 				fw.write(hit.getSourceAsString());
	 				fw.write("\r\n");
	 			}  
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fw.close();
			}
        	 
         	System.out.println("---------------------------------Unknown Failed--------------------------------------------"); 	 
        }
        
        
        System.out.println("Balance Update Hits Count: " + hits4BalanceUpdate.totalHits);
        System.out.println("SUCCESS Count: " + hits4BalanceUpdateSuccess.totalHits);
        System.out.println("Total Failed Count: " + (ids.size() + ids404.size()));
        System.out.println("Unknow Failed Count: " + ids.size());
        System.out.println("404 Failed Count: " + ids404.size());
        
        es.close();
        
        System.out.println("Query "+ hits4BalanceUpdate.totalHits + " Run Time: " + (endTime - startTime) / 1000 + "s");
        */
    }

}
