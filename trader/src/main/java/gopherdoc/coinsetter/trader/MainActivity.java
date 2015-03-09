package gopherdoc.coinsetter.trader;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import gopherdoc.coinsetter.trader.Coinsetter.ListFragmentAdapter;
import gopherdoc.coinsetter.trader.Coinsetter.ObjAPI;
import gopherdoc.coinsetter.trader.Coinsetter.ObjAccount;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionAccount;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionActiveOrderRecord;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionHistoryOrderRecord;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionNewsAlert;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionPriceAlert;
import gopherdoc.coinsetter.trader.Coinsetter.ObjExceptions;
import gopherdoc.coinsetter.trader.Coinsetter.ObjOrderRecord;
import gopherdoc.coinsetter.trader.Coinsetter.ObjPriceAlert;
import gopherdoc.coinsetter.trader.Coinsetter.ObjTicker;
import gopherdoc.coinsetter.trader.Coinsetter.ObjTradeBook;
import gopherdoc.coinsetter.trader.Coinsetter.ObjTradeBookOrderRecord;
import gopherdoc.coinsetter.trader.NavDrawer.NavDrawerItem;
import gopherdoc.coinsetter.trader.NavDrawer.NavDrawerListAdapter;
import gopherdoc.coinsetter.trader.security.KeyStore;
import gopherdoc.coinsetter.trader.security.KeyStoreJb43;
import gopherdoc.coinsetter.trader.security.KeyStoreKk;
import gopherdoc.coinsetter.trader.security.PRNGFixes;


public class MainActivity extends FragmentActivity implements
        PagerTradeSection.TradePagerListener,
        PagerAccountsSection.AccountsPagerListener,
        PagerPriceAlertSection.PriceAlertPagerListener,
        Handler.Callback{

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavDrawerListAdapter adapter;
    private ViewPager mViewPager;
    private PagerHistorySection pageHist;
    private PagerTradeSection pageTrade;
    private PagerAccountsSection pageAcct;
    private PagerPriceAlertSection pageAlert;
    private PagerNewsSection pageNews;
    private FragmentTickerSection fragTick;

    private ObjAPI API;
    private CustomHandlerThread myCustomHandlerThread;
    private Handler myHandler;
    private ArrayList<Toast> toasts;
    private boolean _doubleBackToExitPressedOnce = false;
    private Integer currPager;
    private CountDownTimer myCountDownTimer;
    private ArrayList depthchartdata;
    private LinearLayout mybar;
    private ImageView depthIcon;
    private static final boolean IS_JB43 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final boolean IS_JB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private static final boolean IS_KK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    public static final String OLD_UNLOCK_ACTION = "android.credentials.UNLOCK";

    public static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
    public static final String RESET_ACTION = "com.android.credentials.RESET";
    private KeyStore ks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        PRNGFixes.apply();
        if (IS_KK){
            ks = KeyStoreKk.getInstance();
        } else if (IS_JB43){
            ks = KeyStoreJb43.getInstance();
        } else {
            ks = KeyStore.getInstance();
        }

        //Initiating Fragments
        mViewPager = (ViewPager)findViewById(R.id.pager);
        fragTick = FragmentTickerSection.init();
        ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
        pageAcct = PagerAccountsSection.init();
        mFragments.add(pageAcct);
        pageTrade = PagerTradeSection.init();
        mFragments.add(pageTrade);
        pageHist = PagerHistorySection.init();
        mFragments.add(pageHist);
        pageAlert = PagerPriceAlertSection.init();
        mFragments.add(pageAlert);
        pageNews = PagerNewsSection.init();
        mFragments.add(pageNews);
        mViewPager.setAdapter(new ListFragmentAdapter(super.getSupportFragmentManager(), mFragments));
        currPager = 0;

        //Creating Nav Drawer
        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.list_slidermenu);
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();
        //Accounts
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0]));
        //Price Alert
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1]));
        //News
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2]));
        //Settings
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3]));
        //About
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4]));

        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

        //Enabling action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.app_name,
                R.string.app_name){
            public void onDrawerClosed(View view){
                invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView){
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        mybar = (LinearLayout) findViewById(R.id.myactionbartitle);
	    if (mybar != null) {
	        mybar.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                openNavDrawer();
	            }
        	});
	    }
        depthIcon = (ImageView)findViewById(R.id.depth_icon);
        if (depthIcon != null) {
		    depthIcon.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                launchDepthDialog();
	            }
   	     });
	    }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            //Stop handlers here
            myCustomHandlerThread.setCallback(null);
            myCustomHandlerThread.quit();
            myHandler.removeCallbacks(myCustomHandlerThread.fetchActiveOrders);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchAccountList);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchCompletedTrades);
            myHandler.removeCallbacks(myCustomHandlerThread.goHeartbeat);
            myHandler.removeCallbacks(myCustomHandlerThread.goLogin);
            myHandler.removeCallbacks(myCustomHandlerThread.goLogout);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchIP);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchNews);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchPriceAlertList);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchAccount);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchOrderBook);
            myHandler.removeCallbacks(myCustomHandlerThread.fetchTicker);
            myHandler.removeCallbacks(myCustomHandlerThread.refreshData);
            myHandler.removeCallbacksAndMessages(null);
            myCountDownTimer.cancel();
            myCustomHandlerThread = null;
            finish();

        } catch (NullPointerException ignored) {}
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }


    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item if not currently selected.
            boolean showView;
            int newposition = 0;
            if (adapter.getCurrent() == position && (currPager == 1 || currPager == 2)){
                showView = true;
            } else if (adapter.getCurrent() != position) {
                adapter.updateSelected(position);
                showView = true;
            } else { showView = false;}
            if (position >= 1) {
                newposition = position + 2;
            }
            displayView(newposition, showView);
        }
    }
    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position, boolean loadView) {
        // update the main content by replacing fragments
        if (loadView) {
            switch (position) {
                case 0:
                    //AccountList
                    mViewPager.setCurrentItem(0,true);
                    adapter.updateSelected(0);
                    currPager = 0;
                    depthIcon.setVisibility(View.INVISIBLE);
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                    pageAcct.updateAccounts(API.accounts);
                    break;
                case 1:
                    //Trade
                    mViewPager.setCurrentItem(1);
                    adapter.updateSelected(0);
                    currPager = 1;
                    depthIcon.setVisibility(View.VISIBLE);
                    myHandler.post(myCustomHandlerThread.fetchOrderBook);
                    myHandler.post(myCustomHandlerThread.fetchActiveOrders);
                    pageTrade.updateAccount(API.currentAcct);
                    pageTrade.tradeFee = Float.parseFloat(API.tradeFee);
                    // update selected item and title, then close the drawer
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                    mDrawerLayout.closeDrawer(mDrawerList);

                    break;
                case 2:
                    //History
                    mViewPager.setCurrentItem(2);
                    currPager = 2;
                    adapter.updateSelected(0);
                    depthIcon.setVisibility(View.INVISIBLE);
                    // update selected item and title, then close the drawer
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                    sendToast("Updating Trade History");
                    myHandler.post(myCustomHandlerThread.fetchCompletedTrades);
                    break;
                case 3:
                    //PriceAlert
                    mViewPager.setCurrentItem(3);
                    currPager = 3;
                    adapter.updateSelected(1);
                    depthIcon.setVisibility(View.INVISIBLE);
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                    sendToast("Fetching Current Alerts");
                    myHandler.post(myCustomHandlerThread.fetchPriceAlertList);
                    break;

                case 4:
                    //News
                    mViewPager.setCurrentItem(4);
                    currPager = 4;
                    adapter.updateSelected(2);
                    depthIcon.setVisibility(View.INVISIBLE);
                    mDrawerList.setItemChecked(position, true);
                    mDrawerList.setSelection(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                    sendToast("Fetching News");
                    myHandler.post(myCustomHandlerThread.fetchNews);
                    break;
                case 5:
                    //Settings
                    startActivity(new Intent(this, ActivitySettings.class));
                    finish();
                    break;
                case 6:
                    //About
                    startActivity(new Intent(this, ActivityAbout.class));
                    finish();
                    break;
                default:
                    break;
            }
        } else {
            mDrawerLayout.closeDrawer(mDrawerList);
        }


    }

    @Override
    public void onResumeFragments(){
        super.onResumeFragments();
        //Retrieve some info from savedprefs
        if (ks.state() == KeyStore.State.UNLOCKED){
            byte[] keyBytes = ks.get("hashkey");
            boolean success;
            SecretKey key;
            if (keyBytes != null){
                key = new SecretKeySpec(keyBytes, "AES");
                success = true;
            } else {
                try {
                    KeyGenerator kgen = KeyGenerator.getInstance("AES");
                    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                    byte[] keyStart = "startseed".getBytes();
                    sr.setSeed(keyStart);
                    kgen.init(128, sr);
                    key = kgen.generateKey();
                    success = ks.put("hashkey", key.getEncoded());
                } catch (NoSuchAlgorithmException ignored) {
                    success = false;
                    key = null;
                }
            }
            if (success && key != null) {
                API = new ObjAPI(this, key);
                API.loadPrefs(this, API.getKey());
                pageTrade.tradeFee = Float.parseFloat(API.tradeFee);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                fragTick = FragmentTickerSection.init();
                Bundle args = new Bundle();
                args.putString("TickerText", "");
                fragTick.setArguments(args);
                ft.replace(R.id.fragment_ticker, fragTick, "Ticker");
                ft.commit();
                currPager = 0;
                displayView(currPager, true);

                //Start the thread handlers
                myCustomHandlerThread = new CustomHandlerThread("MyHandlerThread");
                myCustomHandlerThread.start();
                myHandler = new Handler(myCustomHandlerThread.getLooper(), this);
                myCustomHandlerThread.setCallback(myHandler);
                myHandler.post(myCustomHandlerThread.refreshData);
                myCountDownTimer = new CountDownTimer(API.refreshInterval * 1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if (fragTick != null) {
                            fragTick.updateCounter(Long.toString(millisUntilFinished / 1000L));
                        }
                    }

                    public void onFinish() {
                    }
                };
            }
        } else {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    startActivity(new Intent(OLD_UNLOCK_ACTION));
                } else {
                    startActivity(new Intent(UNLOCK_ACTION));
                }
            } catch (ActivityNotFoundException e) {
                Log.e("Coinsetter", "No UNLOCK activity: " + e.getMessage(), e);
                Toast.makeText(this, "No keystore unlock activity found.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    @Override
    public void onStart(){
        super.onStart();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeToast();
    }

    @Override
    protected void onStop () {
        super.onStop();
        removeToast();
    }

    @Override
    public void onBackPressed() {
        if (currPager == 0){
            if (_doubleBackToExitPressedOnce) {
                super.onBackPressed();
                finish();
                return;
            }
            this._doubleBackToExitPressedOnce = true;
            sendToast("Press again to quit");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    _doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            adapter.updateSelected(0);
            displayView(0,true);
        }
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    //Data functions

    public void sendToast(final String message) {
        if (null == toasts) {
            toasts = new ArrayList<Toast>();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
                    toast.show();
                    toasts.add(toast);
                } catch (Exception e) {/* do nothing, just means that the activity doesn't exist anymore*/}
            }
        });
    }


    public void removeToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != toasts) {
                    for(Toast toast:toasts) {
                        toast.cancel();
                    }
                    toasts = null;
                }
            }
        });
    }

    //Network functions
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected();
    }

    //Listener methods

    public void openNavDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public void launchDepthDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        GraphView graph = new GraphView(this);
        if (depthchartdata != null && depthchartdata.size() == 5 ) {
            LineGraphSeries<DataPoint> bids = (LineGraphSeries<DataPoint>) depthchartdata.get(0);
            bids.setColor(Color.RED);
            bids.setBackgroundColor(Color.RED);
            bids.setDrawBackground(true);
            graph.addSeries(bids);

            LineGraphSeries<DataPoint> asks = (LineGraphSeries<DataPoint>) depthchartdata.get(1);
            asks.setColor(Color.GREEN);
            asks.setBackgroundColor(Color.GREEN);
            asks.setDrawBackground(true);
            graph.addSeries(asks);
            Float yMax = (Float)depthchartdata.get(2);
            Float leftbound = (Float)depthchartdata.get(3);
            Float rightbound = (Float)depthchartdata.get(4);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(1);
            nf.setMaximumFractionDigits(0);
            nf.setRoundingMode(RoundingMode.HALF_EVEN);
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
            graph.getGridLabelRenderer().setNumHorizontalLabels(4);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(leftbound);
            graph.getViewport().setMaxX(rightbound);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0.0);
            graph.getViewport().setMaxY(yMax);

            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            graph.setLayoutParams(ll);
            layout.addView(graph);
            alert.setTitle("Market Depth");
            alert.setView(layout);
            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alert.show();
        }
    }

    //TODO listener implementations
    @Override
    public void onOrderBookItemSelected(ObjTradeBookOrderRecord item) {
        String[] myString = item.getPrice().split(" ");
        Double doublePrice = Double.parseDouble(myString[0]);
        String stringPrice = BigDecimal.valueOf(doublePrice).toPlainString();
        pageTrade.setOrderPrice(stringPrice);
    }

    @Override
    public void onTradeListener(String tradeSide, Float price, Float amount) {
        Double dPrice = Double.parseDouble(Float.toString(price));
        DecimalFormat amtFormat = new DecimalFormat("####0.00");
        amtFormat.setRoundingMode(RoundingMode.FLOOR);
        String myPrice = BigDecimal.valueOf(dPrice).toPlainString();
        myCustomHandlerThread.addOrder(tradeSide, myPrice, amtFormat.format(amount));
    }
    @Override
    public void onAccountTradeSelected(ObjAccount item) {
        API.currentAcct = item;
        displayView(1,true);
    }
    public void onAccountHistorySelected(ObjAccount item){
        API.currentAcct = item;
        displayView(2,true);
    }

    @Override
    public void onPriceAlertItemCanceled(ObjPriceAlert alert) {
        myCustomHandlerThread.cancelPriceAlert(alert);
    }

    @Override
    public void onAddPriceAlertSelected(String condition, Integer price, String method) {
        myCustomHandlerThread.addPriceAlert(condition, price, method);
    }

    private class asyncFetchAccountList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchAccountList", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                API.accounts = new ObjCollectionAccount(jObj);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        pageAcct.updateAccounts(API.accounts);
                    }
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchAccountList", "Error parsing JSON balances");
            }
        }

    }

    private class asyncFetchAccount extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchAccount", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
             return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                API.currentAcct = new ObjAccount(jObj);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        pageTrade.updateAccount(API.currentAcct);
                        pageTrade.tradeFee = Float.parseFloat(API.tradeFee);
                    }
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchAccount", "Error parsing JSON balances");
            }
        }
    }
    private class asyncFetchIP extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Content-Type", "application/json");
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchIP", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                    API.currentIP = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                API.currentIP = "";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            API.currentIP = result;
            if (!API.currentIP.equals("")){
                try {
                    if (myHandler != null) {
                        myHandler.post(myCustomHandlerThread.goLogin);
                    }
                }catch (NullPointerException e) {}
            }
        }
    }
    private class asyncAddOrder extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String response = "";
            String url = params[0];
            String side = params[1];
            String price = params[2];
            String amount = params[3];
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params1 = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params1,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params1);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("coinsetter-client-session-id", API.sessionID);
            String httpData = "{\"customerUuid\":\"" + API.currentAcct.customerUUID +
                    "\",\"accountUuid\":\"" + API.currentAcct.accountUUID +
                    "\",\"symbol\":\"" + "BTCUSD" +
                    "\",\"side\":\"" + side +
                    "\",\"orderType\":\"" + "LIMIT" +
                    "\",\"requestedQuantity\": " + amount +
                    ",\"requestedPrice\": " + price +
                    ",\"routingMethod\": " + "1" +
                    " }";
            try {
                httpPost.setEntity(new StringEntity(httpData));
                HttpResponse execute = client.execute(httpPost);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("addOrder", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if (jObj.getString("requestStatus").equals("SUCCESS")){
                    String orderID = jObj.getString("uuid");
                    sendToast("Order added with OrderID: " + orderID);
                } else {
                    String message = jObj.getString("message");
                    Log.e("addOrder", message);
                    sendToast(message);
                }
            } catch (JSONException e) {
                Log.e("addOrder", "Error parsing JSON");
                sendToast("Error adding order");
            }
        }
    }
    private class asyncAddPriceAlert extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String response = "";
            String url = params[0];
            String condition = params[1];
            String price = params[2];
            String method = params[3];
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params1 = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params1,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params1);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("coinsetter-client-session-id", API.sessionID);
            String httpData = "{\"type\":\"" + method +
                    "\",\"condition\":\"" + condition +
                    "\",\"symbol\":\"" + "BTCUSD" +
                    "\",\"price\": " + price +" }";
            try {
                httpPost.setEntity(new StringEntity(httpData));
                HttpResponse execute = client.execute(httpPost);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("addPriceAlert", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if (jObj.getString("requestStatus").equals("SUCCESS")){
                    sendToast("Price alert added successully");
                } else {
                    String message = jObj.getString("message");
                    Log.e("addPriceAlert", message);
                    sendToast(message);
                }
            } catch (JSONException e) {
                Log.e("addPriceAlert", "Error parsing JSON");
                sendToast("Error adding price alert");
            }
        }
    }
    private class asyncFetchCompletedTrades extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("coinsetter-client-session-id",API.sessionID);
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchCompletedTrades", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("")) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    final ObjCollectionHistoryOrderRecord historyOrderRecord = new ObjCollectionHistoryOrderRecord(jObj);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            pageHist.updateHistory(historyOrderRecord);
                        }
                    };
                    runOnUiThread(runnable);
                } catch (JSONException e) {
                    Log.e("fetchCompletedTrades", "Error parsing JSONArray");
                    sendToast("Error processing completed trades");
                }
            }
        }
    }
    private class asyncFetchActiveOrders extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchActiveOrders", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                final ObjCollectionActiveOrderRecord activeOrderRecord = new ObjCollectionActiveOrderRecord(jObj);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pageTrade.updateActiveOrders(activeOrderRecord.collection);
                        }catch (NullPointerException ignored){}
                    }
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchActiveOrders", "Error parsing JSONArray");
            }
        }
    }
    private class asyncFetchNews extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchNews", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                final ObjCollectionNewsAlert newsAlerts = new ObjCollectionNewsAlert(jObj);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pageNews.updateNews(newsAlerts);
                        }catch (NullPointerException ignored){}
                    }
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchNews", "Error parsing JSONArray");
            }
        }
    }
    private class asyncFetchPriceAlertList extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchPriceAlertList", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                final ObjCollectionPriceAlert priceAlerts = new ObjCollectionPriceAlert(jObj);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pageAlert.updateAlerts(priceAlerts);
                        }catch (NullPointerException ignored){}
                    }
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchPriceAlertList", "Error parsing JSONArray");
            }
        }
    }
    private class asyncCancelOrder extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpDelete httpDelete = new HttpDelete(urls[0]);
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpDelete);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("cancelOrder", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if (jObj.getString("requestStatus").equals("SUCCESS")){
                    sendToast("Order cancelled successfully");
                } else {
                    sendToast("Error cancelling order");
                    String message = jObj.getString("message");
                    Log.e("cancelOrder", message);
                }
            } catch (JSONException e) {
                Log.e("cancelOrder", "Error parsing JSON");
                sendToast("Error cancelling order");
            }
        }
    }
    private class asyncCancelPriceAlert extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpDelete httpDelete = new HttpDelete(urls[0]);
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpDelete);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("cancelPriceAlert", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if (jObj.getString("status").equals("success")){
                    sendToast("Price alert cancelled successfully");
                } else {
                    sendToast("Error cancelling price alert");
                    String message = jObj.getString("status");
                    Log.e("cancelPriceAlert", message);
                }
            } catch (JSONException e) {
                Log.e("cancelPriceAlert", "Error parsing JSON");
                sendToast("Error cancelling price alert");
            }
        }
    }
    private class asyncFetchTicker extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchTicker", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            String response = result;
            try {
                JSONObject jObj = new JSONObject(response);
                final ObjTicker ticker = new ObjTicker(jObj);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        fragTick.updateTicker(ticker);
                    }
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchTicker", "Error parsing JSON");
            }
        }
    }
    private class asyncFetchOrderBook extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            httpGet.setHeader("Accept", "application/json");
            try {
                HttpResponse execute = client.execute(httpGet);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("fetchOrderBook", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                final ObjTradeBook orderBook = new ObjTradeBook(jObj.getJSONArray("topNBidAsks"));
                depthchartdata = orderBook.chart.buildChart();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {pageTrade.updateOrderBook(orderBook);}
                };
                runOnUiThread(runnable);
            } catch (JSONException e) {
                Log.e("fetchOrderBook", "Error parsing JSON");
            }
        }
    }
    private class asyncLogin extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String response = "";
            String url = params[0];
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params1 = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params1,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params1);
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            String httpData = "{\"username\":\"" + API.user +
                    "\",\"ipAddress\":\"" + API.currentIP +
                    "\",\"password\":\"" + API.pass +"\"}";
            try {
                httpPost.setEntity(new StringEntity(httpData));
                HttpResponse execute = client.execute(httpPost);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } else if (responseCode.msg.equals("Forbidden")) {
                    sendToast("Non-whitelisted IP: " + API.currentIP);
                    API.sessionID = "";
                } else if (responseCode.intcode == 400){
                    sendToast("Please check credentials");
                    API.sessionID = "";
                } else {

                    Log.e("Login", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", url);
                    API.sessionID = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                API.sessionID = "";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if (jObj.getString("requestStatus").equals("SUCCESS")){
                    API.sessionID = jObj.getString("uuid");
                    if (myHandler != null) {
                        myHandler.post(myCustomHandlerThread.fetchAccountList);
                    }
                } else {
                    API.sessionID = "";
                    String message = jObj.getString("message");
                    Log.e("Login error", message);
                }
            } catch (JSONException e) {
                API.sessionID = "";
                Log.e("Login", "Error parsing JSON");
            } catch (NullPointerException e) {}
        }
    }
    private class asyncLogout extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpPut httpPut = new HttpPut(urls[0]);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpPut);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("Logout", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if (jObj.getString("requestStatus").equals("SUCCESS")){
                    API.sessionID = "";
                } else {
                    String message = jObj.getString("message");
                    Log.e("Logout error", message);
                }
            } catch (JSONException e) {
                Log.e("Logout", "Error parsing JSON");
            }
        }
    }
    private class asyncHeartbeat extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            ObjExceptions responseCode;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
            HttpParams params = new BasicHttpParams();
            SingleClientConnManager mgr = new SingleClientConnManager(params,schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(mgr, params);
            HttpPut httpPut = new HttpPut(urls[0]);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("coinsetter-client-session-id", API.sessionID);
            try {
                HttpResponse execute = client.execute(httpPut);
                responseCode = new ObjExceptions(execute.getStatusLine().getStatusCode());
                if (responseCode.status.equals("OK")) {
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null){
                        response += s;
                    }
                } else {
                    Log.e("Heartbeat", responseCode.intcode + ": " + responseCode.msg);
                    Log.e("Error message", execute.getStatusLine().getReasonPhrase());
                    Log.e("URL", urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if(!jObj.getString("requestStatus").equals("SUCCESS")){
                    API.sessionID = "";
                }
            } catch (JSONException e) {
                Log.e("Heartbeat", "Error parsing JSON");
            }
        }
    }
    @Override
    public void onOrderCancelled(ObjOrderRecord order) {
        myCustomHandlerThread.cancelOrder(order);
    }

    public class CustomHandlerThread extends HandlerThread implements Handler.Callback{
        private Handler callback;

        public CustomHandlerThread(String name) {
            super(name);
        }


        public void setCallback(Handler cb){
            callback = cb;
        }

        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }

        final Runnable fetchAccountList = new Runnable() {
            @Override
            public void run() {
                final String url = API.getAccountList();
                asyncFetchAccountList task = new asyncFetchAccountList();
                task.execute(url);
            }
        };

        final Runnable fetchAccount = new Runnable() {
            @Override
            public void run() {
                final String url = API.getAccount(API.currentAcct.accountUUID);
                asyncFetchAccount task = new asyncFetchAccount();
                task.execute(url);
            }
        };

        public void addOrder( final String side,
                              final String price, final String amount){
            final Runnable runOrder = new Runnable() {
                @Override
                public void run() {
                    final String url = API.addOrder();
                    asyncAddOrder task = new asyncAddOrder();
                    task.execute(url, side, price, amount);
                }
            };
            myHandler.post(runOrder);
        }
        public void addPriceAlert(final String condition,
                                  final Integer price, final String method) {
            final Runnable runPriceAlert = new Runnable() {
                @Override
                public void run() {
                    final String url = API.addAlert();
                    asyncAddPriceAlert task = new asyncAddPriceAlert();
                    task.execute(url, condition, price.toString() , method);
                }
            };
            myHandler.post(runPriceAlert);
        }

        final Runnable fetchActiveOrders = new Runnable() {
            @Override
            public void run() {
                if (!API.sessionID.equals("")) {
                    final String url = API.getOpenOrders(API.currentAcct.accountUUID);
                    asyncFetchActiveOrders task = new asyncFetchActiveOrders();
                    task.execute(url);
                }
            }
        };

        final Runnable fetchCompletedTrades = new Runnable() {
            @Override
            public void run() {
                if (!API.sessionID.equals("")) {
                    final String url = API.getTradeHistory(API.currentAcct.accountUUID);
                    asyncFetchCompletedTrades task = new asyncFetchCompletedTrades();
                    task.execute(url);
                }
            }
        };

        public void cancelOrder(final ObjOrderRecord order){
            final Runnable runCancel = new Runnable() {
                @Override
                public void run() {
                    final String url = API.cancelOrder(order.orderUuid);
                    asyncCancelOrder task = new asyncCancelOrder();
                    task.execute(url);
                }
            };
            myHandler.post(runCancel);
        }
        public void cancelPriceAlert(final ObjPriceAlert alert) {
            final Runnable runPACancel = new Runnable(){
                @Override
                public void run() {
                    final String url = API.cancelAlert(alert.uuid);
                    asyncCancelPriceAlert task = new asyncCancelPriceAlert();
                    task.execute(url);
                }
            };
            myHandler.post(runPACancel);
        }
        public Runnable fetchTicker = new Runnable() {
            @Override
            public void run() {
                final String url = API.getTicker();
                asyncFetchTicker task = new asyncFetchTicker();
                task.execute(url);
            }
        };

        public Runnable fetchOrderBook = new Runnable() {
            @Override
            public void run() {
                final String url = API.getOrderBook(API.depthRange);
                asyncFetchOrderBook task = new asyncFetchOrderBook();
                task.execute(url);
            }
        };
        public Runnable fetchIP = new Runnable() {
            @Override
            public void run() {
                final String url = API.FETCHPUBLICIPURL;
                asyncFetchIP task = new asyncFetchIP();
                task.execute(url);
            }
        };
        public Runnable fetchNews = new Runnable() {
            @Override
            public void run() {
                final String url = API.getNews();
                asyncFetchNews task = new asyncFetchNews();
                task.execute(url);
            }
        };
        public Runnable fetchPriceAlertList = new Runnable() {
            @Override
            public void run() {
                final String url = API.getAlertList();
                asyncFetchPriceAlertList task = new asyncFetchPriceAlertList();
                task.execute(url);
            }
        };

        public Runnable goLogin = new Runnable() {
            @Override
            public void run() {
                final String url = API.getLogin();
                asyncLogin task = new asyncLogin();
                task.execute(url);
            }
        };
        public Runnable goLogout = new Runnable() {
            @Override
            public void run() {
                final String url = API.getLogout(API.sessionID);
                asyncLogout task = new asyncLogout();
                task.execute(url);
            }
        };
        public Runnable goHeartbeat = new Runnable() {
            @Override
            public void run() {
                final String url = API.getHeartbeat(API.sessionID);
                asyncLogout task = new asyncLogout();
                task.execute(url);
            }
        };


        final Runnable refreshData = new Runnable() {
            public void run() {
                if(!isNetworkAvailable(getApplicationContext())){
                    sendToast("No Internet connection");
                } else {
                    if (myCountDownTimer != null){ myCountDownTimer.cancel(); }
                    myHandler.post(fetchTicker);
                    if (API.sessionID.equals("")){
                        if (API.currentIP.equals("")){
                            myHandler.post(fetchIP);
                        } else {
                            myHandler.post(goLogin);
                        }
                    } else {
                        if (currPager == 0){
                            myHandler.post(fetchAccountList);
                        } else if (currPager == 1) {
                            myHandler.post(fetchAccount);
                            myHandler.post(fetchOrderBook);
                            myHandler.post(fetchActiveOrders);
                        } else if(currPager == 2) {
                            myHandler.post(fetchCompletedTrades);
                        } else if(currPager == 3) {
                            myHandler.post(fetchPriceAlertList);
                        } else if(currPager == 4){
                            myHandler.post(fetchNews);
                        }
                    }
                    if (myCountDownTimer != null) { myCountDownTimer.start(); }
                }
                myHandler.postDelayed(this, API.refreshInterval*1000L);
            }
        };



    }
}