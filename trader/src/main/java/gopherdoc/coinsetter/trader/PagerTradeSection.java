package gopherdoc.coinsetter.trader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import gopherdoc.coinsetter.trader.Coinsetter.ObjAccount;
import gopherdoc.coinsetter.trader.Coinsetter.ObjOrderRecord;
import gopherdoc.coinsetter.trader.Coinsetter.ObjTradeBook;
import gopherdoc.coinsetter.trader.Coinsetter.ObjTradeBookOrderRecord;


public class PagerTradeSection extends Fragment implements ListItemActiveorder.OnCancelItemSelectedListener{
    private PagerTradeSection myPTS;

    //Trade Section
    private TextView basebal;
    private TextView quotebal;
    private EditText myorderamount;
    private EditText myorderprice;
    private ObjAccount acct;
    public Float tradeFee;

    //ActiveOrder Section
    private TextView aOrders;
    private ArrayList<ObjOrderRecord> activeOrders;
    private ObjOrderRecord cancelSelected;
    private ActiveOrdersArrayAdapter activeOrderAdapter;
    private ActiveOrdersArrayAdapter copyAdapter;

    //Orderbook section
    private TradePagerListener listener;
    private ObjTradeBookOrderRecord obSelected;
    private OrderBookArrayAdapter askadapter;
    private OrderBookArrayAdapter bidadapter;
    private ArrayList<ObjTradeBookOrderRecord> asks;
    private ArrayList<ObjTradeBookOrderRecord> bids;
    private SecurePreferences mSecurePrefs;

    public interface TradePagerListener {
        public void onOrderBookItemSelected(ObjTradeBookOrderRecord item);
        public void onTradeListener(String tradeSide, Float price, Float amount);
        public void onOrderCancelled(ObjOrderRecord order);
    }

    static PagerTradeSection init() {
        return new PagerTradeSection();
    }

    public PagerTradeSection(){
        activeOrders = new ArrayList<ObjOrderRecord>();
        asks = new ArrayList<ObjTradeBookOrderRecord>();
        bids = new ArrayList<ObjTradeBookOrderRecord>();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myPTS = this;
        final View rootView = inflater.inflate(R.layout.pager_trade, null);
        assert rootView != null;
        activeOrderAdapter = new ActiveOrdersArrayAdapter(
                getActivity(),activeOrders,this);
        basebal = (TextView) rootView.findViewById(R.id.basebal);
        basebal.setText("Loading");
        basebal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxSellAmount();
            }
        });
        quotebal = (TextView) rootView.findViewById(R.id.quotebal);
        quotebal.setText("Loading");
        quotebal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxBuyAmount();
            }
        });
        myorderamount = (EditText) rootView.findViewById(R.id.myorder_amount);
        myorderamount.setText("");
        myorderprice = (EditText) rootView.findViewById(R.id.myorder_price);
        myorderprice.setText("");
        if (savedInstanceState != null){
            if (savedInstanceState.getString("balquote") != null){
                quotebal.setText(savedInstanceState.getString("balquote"));
            }
            if (savedInstanceState.getString("balbase") != null){
                basebal.setText(savedInstanceState.getString("balbase"));
            }
            if (savedInstanceState != null){
                if (savedInstanceState.getParcelableArrayList("activeorders") != null){
                    activeOrders = savedInstanceState.getParcelableArrayList("activeorders");
                    activeOrderAdapter.notifyDataSetChanged();
                }
            }
            if (savedInstanceState != null){
                if (savedInstanceState.getParcelable("tradebook") != null){
                    ObjTradeBook objTradeBook = savedInstanceState.getParcelable("tradebook");
                    asks = objTradeBook.asks;
                    bids = objTradeBook.bids;
                }
            }
        }
        TextView btnbuy = (TextView) rootView.findViewById(R.id.btn_buy);
        btnbuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!getAmount().equals("") && !getPrice().equals("") && isFloat(getAmount()) && isFloat(getPrice())) {
                        tradeConfirm(rootView.getContext(), 0, Float.parseFloat(getPrice()), Float.parseFloat(getAmount()));
                    } else {
                        Toast.makeText(getActivity(), "Invalid order", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e){
                    Toast.makeText(getActivity(), "Invalid order", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TextView btnsell = (TextView) rootView.findViewById(R.id.btn_sell);
        btnsell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!getAmount().equals("") && !getPrice().equals("")&& isFloat(getAmount()) && isFloat(getPrice())) {
                        tradeConfirm(rootView.getContext(), 1, Float.parseFloat(getPrice()), Float.parseFloat(getAmount()));
                    } else {
                        Toast.makeText(getActivity(), "Invalid order", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e){
                    Toast.makeText(getActivity(), "Invalid order", Toast.LENGTH_SHORT).show();
                }
            }
        });
        aOrders = (TextView)rootView.findViewById(R.id.btn_activeorders);
        disableAOBtn();
        aOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("Open Order(s)");
                ListView itemList = new ListView(getActivity());
                ArrayList<ObjOrderRecord> orders = activeOrderAdapter.orders;
                copyAdapter = new ActiveOrdersArrayAdapter(getActivity(),orders,myPTS);
                itemList.setAdapter(copyAdapter);
                builder.setView(itemList);
                builder.setNeutralButton("Dismiss",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                final Dialog dialog = builder.create();
                dialog.show();
            }
        });
        ListView lv_asks = (ListView) rootView.findViewById(R.id.lv_asks);
        ListView lv_bids = (ListView) rootView.findViewById(R.id.lv_bids);
        askadapter = new OrderBookArrayAdapter(getActivity(), asks);
        lv_asks.setAdapter(askadapter);
        lv_asks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                obSelected = askadapter.getItem(i);
                updateTrader();
            }
        });
        bidadapter = new OrderBookArrayAdapter(getActivity(), bids);
        lv_bids.setAdapter(bidadapter);
        lv_bids.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                obSelected = bidadapter.getItem(i);
                updateTrader();
            }
        });

        return rootView;
    }

    public void disableAOBtn() {
        if (aOrders != null) {
            aOrders.setEnabled(false);
            aOrders.setBackgroundResource(R.drawable.grayback);
            aOrders.setText("");
        }
    }
    public void enableAOBtn(){
        if (aOrders != null) {
            aOrders.setEnabled(true);
            aOrders.setBackgroundResource(R.drawable.cancelback);
            aOrders.setText("");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof TradePagerListener) {
            listener = (TradePagerListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement PagerTradeSection.TradePagerListener");
        }

    }
    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        String balquote = quotebal.getText().toString();
        String balbase = basebal.getText().toString();
        outState.putString("balquote",balquote);
        outState.putString("balbase", balbase);
        outState.putParcelableArrayList("activeorders",activeOrders);
        ObjTradeBook ob = new ObjTradeBook();
        for (ObjTradeBookOrderRecord bid : bids){ob.addBid(bid);}
        for (ObjTradeBookOrderRecord ask : asks){ob.addAsk(ask);}
        outState.putParcelable("tradebook",ob);
    }
    @Override
    public void CancelItemSelected(Integer pos, ObjOrderRecord orderRecord) {
        cancelSelected = orderRecord;
        cancelConfirm(getActivity(), pos);
    }

    //Adapters
    private class ActiveOrdersArrayAdapter extends ArrayAdapter<ObjOrderRecord> {
        private PagerTradeSection listener;
        private final Context context;
        private final ArrayList<ObjOrderRecord> orders;

        public ActiveOrdersArrayAdapter(Context context, ArrayList<ObjOrderRecord> objects, PagerTradeSection section) {
            super(context, R.layout.lvitem_activeorder, objects);
            this.listener = section;
            this.context = context;
            this.orders = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemActiveorder rowView = new ListItemActiveorder(context);
            rowView.newOrder(listener,orders.get(position), position);
            return rowView;
        }
        public void removeAt(int position){
            orders.remove(position);
        }
    }
    private class OrderBookArrayAdapter extends ArrayAdapter<ObjTradeBookOrderRecord> {
        private final Context context;
        private final ArrayList<ObjTradeBookOrderRecord> values;

        public OrderBookArrayAdapter(Context context, ArrayList<ObjTradeBookOrderRecord> objects) {
            super(context, R.layout.lvitem_order, objects);
            this.context = context;
            this.values = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ListItemOrderBook rowView = new ListItemOrderBook(context);
            rowView.setPrice(values.get(position).getPrice());
            rowView.setAmount(values.get(position).getAmount());
            rowView.setQuote(values.get(position).getQuoteAmount());

            return rowView;
        }
    }


    //Setters
    public void setOrderAmount(String amt){
        if(myorderamount != null){
            myorderamount.setText(amt);
        }
    }
    public void setOrderPrice(String price){
        if( myorderprice != null){
            myorderprice.setText(price.split(" ")[0]);
        }
    }
    public void setBasebal(String bal){if (basebal != null) {basebal.setText(bal);}}
    public void setQuotebal(String bal){ if (quotebal != null) {quotebal.setText(bal);} }

    //Getters
    public String getPrice(){ return (myorderprice.getText() != null) ? myorderprice.getText().toString() : ""; }
    public String getAmount() { return (myorderamount.getText() != null) ? myorderamount.getText().toString() : ""; }
    public String getBasebal(){ return (basebal.getText() != null) ? basebal.getText().toString() : "";}
    public String getQuotebal(){ return (quotebal.getText() != null) ? quotebal.getText().toString() : "";}


    //Trade Section Methods
    public String myFloatString2(Float flt) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(2);
        return df.format(flt);
    }
    public void maxSellAmount(){
        if (basebal != null) {
            if (!getBasebal().equals("Loading")) {
                String[] maxamt = getBasebal().split(" ");
                Float max = Float.parseFloat(maxamt[0]);
                Float maxvol = (max - 0.001F)/(1.0F + tradeFee);
                setOrderAmount(myFloatString2(maxvol));
            }
        }
    }
    public void maxBuyAmount(){
        if (quotebal != null) {
            if (!getPrice().equals("") && !getQuotebal().equals("Loading")) {
                Float myPrice = Float.parseFloat(getPrice());
                Float quotebal = Float.parseFloat(getQuotebal().split(" ")[0]);
                Float maxvol = (quotebal - 0.01F)/(myPrice * (1.0F + tradeFee));
                setOrderAmount(myFloatString2(maxvol));
            }
        }
    }

    public boolean isFloat(String str) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    public String myFloatString(Float flt) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(5);
        return df.format(flt);
    }
    public void tradeConfirm(Context con, final Integer side,final Float price,final Float amount) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(con);
        myAlertDialog.setTitle("Confirm Trade");
        String base = amount.toString() + " BTC";
        String quote = price.toString() + " USD";
        String total = myFloatString(price * amount) + " USD";
        String commission = "";
        if (side == 0) {
            commission = myFloatString(price * amount * tradeFee) + " USD";
        } else {
            commission = myFloatString(amount * tradeFee) +  " BTC";
        }

        myAlertDialog.setMessage(((side == 0) ? "Buy " : "Sell ") + base
                + " at " + quote + " for " + total
                + "\nCommission: " + commission);
        myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (side == 0) {
                    Float myPrice = Float.parseFloat(getPrice());
                    Float quotebal = Float.parseFloat(getQuotebal().split(" ")[0]);
                    Float maxBuyAmount = (quotebal - 0.01F) / (myPrice * (1.0F + tradeFee));
                    if (amount <= maxBuyAmount) {
                        listener.onTradeListener("buy", price, amount);
                        setOrderAmount("");
                        setOrderPrice("");
                    } else {
                        Toast mt = Toast.makeText(getActivity(), "Please check buy amount", Toast.LENGTH_SHORT);
                        mt.show();
                    }
                } else {
                    String[] maxamt = getBasebal().split(" ");
                    Float max = Float.parseFloat(maxamt[0]);
                    Float maxSell = (max - 0.001F) / (1.0F + tradeFee);
                    if (amount <= maxSell) {
                        listener.onTradeListener("sell", price, amount);
                        setOrderAmount("");
                        setOrderPrice("");
                    } else {
                        Toast mt = Toast.makeText(getActivity(), "Please check sell amount", Toast.LENGTH_SHORT);
                        mt.show();
                    }
                }
            }
        });
        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }});
        myAlertDialog.show();
    }

    //Active Order Section Methods
    public void updateActiveOrders(ArrayList<ObjOrderRecord> ao){
        if (ao != null && activeOrderAdapter != null) {
            activeOrderAdapter.clear();
            for (ObjOrderRecord order : ao) {
                activeOrderAdapter.add(order);
            }
            activeOrderAdapter.notifyDataSetChanged();
            Integer size = ao.size();
            if (size == 0) {
                disableAOBtn();
                aOrders.setText("No Open Orders");
            } else if (size == 1) {
                enableAOBtn();
                aOrders.setText("1 Open Order");

            } else {
                enableAOBtn();
                aOrders.setText(size + " Open Orders");
            }

        }
    }

    public void cancelConfirm(Context con, final Integer pos) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(con);
        myAlertDialog.setTitle("Cancel order?");
        String orderstring = cancelSelected.getOpenOrderString();
        myAlertDialog.setMessage(orderstring);
        myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                listener.onOrderCancelled(cancelSelected);
                copyAdapter.removeAt(pos);
                copyAdapter.notifyDataSetChanged();
            }
        });
        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }});
        AlertDialog alert = myAlertDialog.create();
        alert.show();
    }

    public void clearActiveOrders(){updateActiveOrders(new ArrayList<ObjOrderRecord>());}


    //OrderBook Methods
    public void clearOrderBook(){updateOrderBook(new ObjTradeBook());}
    public void updateTrader() {
        if (this.obSelected != null) {listener.onOrderBookItemSelected(this.obSelected);}
    }
    public void updateOrderBook(ObjTradeBook orderBook){
        if (askadapter != null) {
            askadapter.clear();
            asks = orderBook.asks;
            for (ObjTradeBookOrderRecord ask : asks) {
                askadapter.add(ask);
            }
            askadapter.notifyDataSetChanged();
        }
        if (bidadapter != null) {
            bidadapter.clear();
            bids = orderBook.bids;
            for (ObjTradeBookOrderRecord bid : bids) {
                bidadapter.add(bid);
            }
            bidadapter.notifyDataSetChanged();
        }
    }
    public void updateAccount(ObjAccount acct){
        this.acct = acct;
        //TODO calculate balances
        Float basebalance = acct.BTC.balance;
        Float quotebalance = acct.USD.balance;
        for (int i=0;i< activeOrders.size(); i++){
            ObjOrderRecord item = activeOrders.get(i);
            if (item.tradeside.equals("BUY")){
                quotebalance -= item.requestedPrice * item.quantityOpen * (1.0F + tradeFee);
            } else {
                basebalance -= item.quantityOpen * (1.0F + tradeFee);
            }
        }

        setBasebal(myFloatString(basebalance) + " BTC");
        setQuotebal(myFloatString(quotebalance) + " USD");


    }
}
