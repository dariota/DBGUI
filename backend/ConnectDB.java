package backend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

//Interacts with Amazon's DynamoDB
public class ConnectDB {
	
	private String user = Run.user;
	private static String errSource = "CDB";
	private AmazonDynamoDBClient client = null;
	
	/**
	 * Connects to database, authing with given keys.
	 * @param accessKey
	 * accessKey for Amazon DynamoDB
	 * @param secretKey
	 * secretKey for Amazon DynamoDB
	 */
	public void initialiseConnection(String accessKey, String secretKey) {
		BasicAWSCredentials bawsc = new BasicAWSCredentials(accessKey, secretKey);
		client = new AmazonDynamoDBClient(bawsc);
		Region r = Region.getRegion(Regions.US_WEST_2);
		client.setRegion(r);
	}
  
	/**
	 * @param table
	 * Takes a String, table name (mammal, reptile etc.)
	 * @param keyValuePairs
	 * ArrayList of ValueHolder, containing all the fields to be submitted, with no null or empty values
	 * @return
	 * Always returns true, will crash with exception if fails.
	 */
	public void insertInto(String table, ArrayList<ValueHolder> keyValuePairs) {
		HashMap<String, AttributeValue> toInsert = new HashMap<>();
		
		for (ValueHolder vh : keyValuePairs) {
			toInsert.put(vh.key(), vh.attribute());
		}
		
		String lastID = latestID(table);
		AttributeValue ID = new AttributeValue();
		ID.setS(lastID);
		toInsert.put("Animal ID", ID);
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		AttributeValue dateAV = new AttributeValue();
		dateAV.setS(dateFormat.format(date));
		toInsert.put("Last Updated", dateAV);
		AttributeValue userAV = new AttributeValue();
		userAV.setS(user);
		toInsert.put("Author", userAV);
		try {
			client.putItem(table, toInsert);
			incrementID(table, Integer.parseInt(lastID)); //simulates primary key auto-increment
		} catch (Exception e) {
			ErrorHandler.error(e, errSource);
		}
	}
	
	/**
	 * Updates the given table's current "next id" to the value given.
	 * @param table
	 * The table to update
	 * @param id
	 * The id which the next item inserted in the given table will take
	 */
	public void incrementID(String table, int id) {
		HashMap<String, AttributeValue> keyValuePair = new HashMap<>();
		AttributeValue animalID = new AttributeValue();
		animalID.setS("-1");
		keyValuePair.put("Animal ID", animalID);
		AttributeValue currID = new AttributeValue();
		currID.setS(Integer.toString(id + 1));
		keyValuePair.put("CurrID", currID);
		client.putItem(table, keyValuePair);
	}
	
	/**
	 * Updates an existing entry with the information given
	 * @param table
	 * Takes a String, table name (mammal, reptile etc.)
	 * @param keyValuePairs
	 * ArrayList of ValueHolder, containing all the fields to be updated
	 * @param ID
	 * Numeric ID in String form of animal to change
	 * @return
	 * Always returns true, will crash with exception if fails.
	 */
	public void update(String table, ArrayList<ValueHolder> keyValuePairs, String ID) {
		HashMap<String, AttributeValue> animalID = new HashMap<>();
		AttributeValue idValue = new AttributeValue();
		idValue.setS(ID);
		animalID.put("Animal ID", idValue);
		HashMap<String, AttributeValueUpdate> toUpdate = new HashMap<>();
		for (ValueHolder vh : keyValuePairs) {
			if (!vh.attribute().getS().equals("")) { //some fields may be empty, causing errors
				AttributeValueUpdate AVU = new AttributeValueUpdate();
				AVU.setValue(vh.attribute());
				toUpdate.put(vh.key(), AVU);
			}
		}
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		AttributeValueUpdate dateAVU = new AttributeValueUpdate();
		AttributeValue dateValue = new AttributeValue();
		dateValue.setS(dateFormat.format(date));
		dateAVU.setValue(dateValue);
		toUpdate.put("Last Updated", dateAVU);
		AttributeValueUpdate userAVU = new AttributeValueUpdate();
		AttributeValue userAV = new AttributeValue();
		userAV.setS(user);
		userAVU.setValue(userAV);
		toUpdate.put("Author", userAVU);
		client.updateItem(table, animalID, toUpdate);
	}
	
	/**
	 * Searches for a given term under given conditions, returning information of all the animals found that match it.
	 * @param table
	 * Table to search in
	 * @param column
	 * Column in which to search for a match
	 * @param similar
	 * Boolean describing whether to search for equality or just containing the given string
	 * @return
	 * ArrayList of Map of String to AttributeValue, describing every animal found matching
	 */
	public ArrayList<Map<String, AttributeValue>> search(String searchTerm, String column, 
															boolean similar, String table) { 
		ScanRequest criteria = new ScanRequest(table); //constructs scan query, retrieving all columns
		criteria.setAttributesToGet(Arrays.columnsAsArrayList());
		ArrayList<String> col = new ArrayList<>();
		col.add("Animal ID");
		Condition matchCondition = new Condition();
		ArrayList<AttributeValue> searchBy = new ArrayList<>();
		AttributeValue av = new AttributeValue(searchTerm);
		searchBy.add(av);
		matchCondition.setAttributeValueList(searchBy);
		//Matching depends on whether user specifies exact matches
		matchCondition.setComparisonOperator(similar ? ComparisonOperator.CONTAINS 
														: ComparisonOperator.EQ);
		criteria.addScanFilterEntry(column, matchCondition);
		
		ScanResult searchResult = client.scan(criteria);
		ArrayList<Map<String, AttributeValue>> results = (ArrayList<Map<String, AttributeValue>>) searchResult.getItems();
		Map<String, AttributeValue> resultSet = searchResult.getLastEvaluatedKey();
		//Amazon limits max results returned in one search
		while (resultSet != null) {
			for (Entry<String, AttributeValue> e : resultSet.entrySet()) {
				criteria.addExclusiveStartKeyEntry(e.getKey(), e.getValue());
			}
			searchResult = client.scan(criteria);
			results.addAll(searchResult.getItems());
			resultSet = searchResult.getLastEvaluatedKey();
		}
		
		results = sortSearchResults(results, table);
		
		return results;
	}
	
	//Groups search results by subtypes
	private ArrayList<Map<String, AttributeValue>> sortSearchResults(ArrayList<Map<String, AttributeValue>> results, String table) {
		int type = 1;
		for (int i = 0; i < Arrays.onlyValidDBTypes().length; i++) {
			if (table.equals(Arrays.onlyValidDBTypes()[i])) {
				type += i;
				break;
			}
		}
		String[] subtypes = Arrays.getSubtypes(type);
		
		ArrayList<Map<String, AttributeValue>> sorted = new ArrayList<>();
		for (String s : subtypes) {
			for (int i = 0; i < results.size(); i++) {
				Map<String, AttributeValue> msav = results.get(i);
				try {
				if (msav.get("Subtype").getS().equals(s)) {
					sorted.add(msav);
					results.remove(i);
					i--;
				}
				} catch (NullPointerException e) {}
			}
		}
		
		if (results.size() > 0) {
			for (Map<String, AttributeValue> msav : results) sorted.add(msav);
		}
		
		return sorted;
	}
	
	/**
	 * Returns every animal in the given tables
	 * @param tables
	 * Array of Strings of the tables to fetch all animals from
	 * @return
	 * Animal info grouped by source table
	 */
	public ArrayList<SimpleEntry<String, ArrayList<Map<String, AttributeValue>>>> displayAll(String[] tables) {
		ArrayList<SimpleEntry<String, ArrayList<Map<String, AttributeValue>>>> animalsByTable = new ArrayList<>();
		for (String table : tables) {
			ScanRequest criteria = new ScanRequest(table);
			criteria.setAttributesToGet(Arrays.columnsAsArrayList());
			
			Condition matchCondition = new Condition();
			ArrayList<AttributeValue> alav = new ArrayList<>();
			AttributeValue av = new AttributeValue("-1");
			alav.add(av);
			matchCondition.setAttributeValueList(alav);
			matchCondition.setComparisonOperator(ComparisonOperator.NE);
			//Returns every entry except the one keeping track of the next ID to use 
			criteria.addScanFilterEntry("Animal ID", matchCondition);
			
			ScanResult searchResult = client.scan(criteria);
			ArrayList<Map<String, AttributeValue>> results = 
										(ArrayList<Map<String, AttributeValue>>) searchResult.getItems();
			Map<String, AttributeValue> resultSet = searchResult.getLastEvaluatedKey();
			while (resultSet != null) {
				for (Entry<String, AttributeValue> e : resultSet.entrySet()) {
					criteria.addExclusiveStartKeyEntry(e.getKey(), e.getValue());
				}
				searchResult = client.scan(criteria);
				results.addAll(searchResult.getItems());
				resultSet = searchResult.getLastEvaluatedKey();
			}
			animalsByTable.add(new SimpleEntry<>(table, sortSearchResults(results, table)));
		}
		return animalsByTable;
	}
	
	/**
	 * Returns the ID the next item inserted in a given table should take
	 * @param table
	 * Table to fetch the ID for
	 * @return
	 * ID to be used
	 */
	public String latestID(String table) {
		HashMap<String, AttributeValue> idFetcher = new HashMap<>();
		AttributeValue idFetchVal = new AttributeValue();
		idFetchVal.setS("-1");
		idFetcher.put("Animal ID", idFetchVal);
		GetItemResult gir = client.getItem(table, idFetcher);
		String id = gir.getItem().get("CurrID").getS();
		return id;
	}

	/**
	 * Self explanatory
	 * @return
	 * Boolean describing readiness.
	 */
	public boolean isReady() {
		return client != null;
	}

	/**
	 * Safely closes the connection
	 */
	public void close() {
		client.shutdown();
	}

}
