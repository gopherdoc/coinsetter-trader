package gopherdoc.coinsetter.trader.Coinsetter;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import javax.crypto.SecretKey;

import gopherdoc.coinsetter.trader.R;
import gopherdoc.coinsetter.trader.SecurePreferences;


public class ObjAPI implements Parcelable {
    //Private Fields
    public String user;
    public String pass;
    public String sessionID;
    public String tradeFee;
    public ObjCollectionAccount accounts;
    public int refreshInterval;
    public int depthRange;
    public ObjAccount currentAcct;
    private SecretKey key;
    public String currentIP;

    //TODO Change here for sandboxing
    //private final static String BASEURL = "https://staging-api.coinsetter.com/v1/";
    public final static String BASEURL = "https://api.coinsetter.com/v1/";
    public final static String FETCHPUBLICIPURL = "http://icanhazip.com";
    private SecurePreferences mSecurePrefs;

    //Constructors
    public ObjAPI(Context con, SecretKey secKey){
        accounts = new ObjCollectionAccount();
        currentAcct = new ObjAccount();
        sessionID = "";
        currentIP = "";
        user = "";
        pass = "";
        refreshInterval = 10;
        depthRange = 10;
        key = secKey;
        mSecurePrefs = new SecurePreferences(con,con.getString(R.string.preference_file_key), secKey.toString(), true);
    }

    //Getters
    public SecretKey getKey() {return key;}
    public String getTimestampMillis(){
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        String time = "" + timestamp + "000";
        return time.trim();
    }
    public String getTimestamp(){
        int timestamp = (int) (System.currentTimeMillis() / 1000L);
        String time = "" + timestamp;
        return time.trim();
    }

    //URL Getters
    public String getTicker() {
        return this.BASEURL + "marketdata/ticker";
    }
    public String getOrderBook(int depth) {
        return this.BASEURL + "marketdata/depth?depth=" + depth + "&format=PAIRED";
    }
    public String getRecentTrades(int lastn){
        return this.BASEURL + "marketdata/last?lookback=" + lastn;
    }
    public String getAccount(String acctid){
        return this.BASEURL + "customer/account/" + acctid;
    }
    public String getAccountList() {
        return this.BASEURL + "customer/account";
    }
    public String getHeartbeat(String sessid) {
        return this.BASEURL + "clientSession/" + sessid + "?action=HEARTBEAT";
    }
    public String getLogin() {
        return this.BASEURL + "clientSession";
    }
    public String getLogout(String sessid) {
        return this.BASEURL + "clientSession/" + sessid + "?action=LOGOUT";
    }
    public String getOpenOrders(String uuid){
        return this.BASEURL + "customer/account/" + uuid + "/order?view=OPEN";
    }
    public String getTradeHistory(String uuid){
        return this.BASEURL + "customer/account/" + uuid + "/order?view=FILL";
    }
    public String addOrder(){
        return this.BASEURL + "order";
    }
    public String cancelOrder(String order_id){
        return this.BASEURL + "order/" + order_id;
    }
    public String getAlertList(){
        return this.BASEURL + "pricealert";
    }
    public String addAlert(){
        return this.BASEURL + "pricealert";
    }
    public String cancelAlert(String uuid){
        return this.BASEURL + "pricealert/" + uuid;
    }
    public String getNews(){
        return this.BASEURL + "newsalert";
    }

    //Methods
    public static Boolean validateKeys(Context con, SecretKey key) {
        SecurePreferences mSecurePrefs = new SecurePreferences(con,con.getString(R.string.preference_file_key), key.toString(), true);
        String primary = mSecurePrefs.getString(con.getString(R.string.prefusername));
        String secret = mSecurePrefs.getString(con.getString(R.string.prefpassword));
        if (primary != null && !primary.equals("") && secret != null && !secret.equals("")){
            return true;
        } else {
            return false;
        }
    }
    public static void initPrefs(final Context con, SecretKey key){
        SecurePreferences mSecurePrefs = new SecurePreferences(con,con.getString(R.string.preference_file_key), key.toString(), true);

        //API keys
        if (!mSecurePrefs.containsKey(con.getString(R.string.prefusername))){
            mSecurePrefs.put(con.getString(R.string.prefusername), "");}
        if (!mSecurePrefs.containsKey(con.getString(R.string.prefpassword))){
            mSecurePrefs.put(con.getString(R.string.prefpassword), "");}
        if (!mSecurePrefs.containsKey("prefSessionID")){
            mSecurePrefs.put("prefSessionID", "");}
        if (!mSecurePrefs.containsKey("refreshInterval")){
            mSecurePrefs.put("refreshInterval","10");}
        if (!mSecurePrefs.containsKey("depthRange")){
            mSecurePrefs.put("depthRange","10");}
        //TODO Update here with trade fee changes
        mSecurePrefs.put("tradeFeeOffset", "0.0025");


    }
    public void loadPrefs(final Context con, SecretKey key){
        SecurePreferences mSecurePrefs = new SecurePreferences(con,con.getString(R.string.preference_file_key), key.toString(), true);
        this.user = mSecurePrefs.getString(con.getString(R.string.prefusername));
        this.pass = mSecurePrefs.getString(con.getString(R.string.prefpassword));
        this.refreshInterval = Integer.parseInt(mSecurePrefs.getString("refreshInterval"));
        this.depthRange = Integer.parseInt(mSecurePrefs.getString("depthRange"));
        this.sessionID = mSecurePrefs.getString("prefSessionID");
        this.tradeFee = mSecurePrefs.getString("tradeFeeOffset");
    }

    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }
    public static final Creator<ObjAPI> CREATOR = new Creator<ObjAPI>() {
        @Override
        public ObjAPI createFromParcel(Parcel parcel) {
            return new ObjAPI(parcel);
        }

        @Override
        public ObjAPI[] newArray(int i) {
            return new ObjAPI[i];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(user);
        out.writeString(user);
        out.writeInt(refreshInterval);
        out.writeInt(depthRange);
        out.writeString(sessionID);
        out.writeString(tradeFee);
        out.writeParcelable(accounts,i);

    }
    private ObjAPI(Parcel in){
        this.user = in.readString();
        this.user = in.readString();
        this.refreshInterval = in.readInt();
        this.depthRange = in.readInt();
        this.sessionID = in.readString();
        this.tradeFee = in.readString();
        this.accounts = in.readParcelable(ObjCollectionAccount.class.getClassLoader());
    }
}
