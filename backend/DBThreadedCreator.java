package backend;

//Allows for multiple connections to the database, and prevents some lock-up
public class DBThreadedCreator implements Runnable {
	
	private ConnectDB db;
	
	@Override
	public void run() {
		db = new ConnectDB();
		db.initialiseConnection(Run.accessKey, Run.secretKey);
	}
	
	public ConnectDB getAccess() {
		return db.isReady() ? db : null;
	}

}