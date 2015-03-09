package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class ObjTradeBook implements Parcelable {
    //Fields
    public ArrayList<ObjTradeBookOrderRecord> bids;
    public ArrayList<ObjTradeBookOrderRecord> asks;
    private String priceCurrency = "USD";
    private String quantityCurrency = "BTC";
    public ObjTradeBookChart chart;

    public ObjTradeBook(){
        clearOrderBook();
    }

    //Constructors
    public ObjTradeBook(final JSONArray jArray){
        bids = new ArrayList<ObjTradeBookOrderRecord>();
        asks = new ArrayList<ObjTradeBookOrderRecord>();
        chart = new ObjTradeBookChart();
        try {
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject pair = jArray.getJSONObject(i);
                JSONObject bid = pair.getJSONObject("bid");
                JSONObject ask = pair.getJSONObject("ask");
                if (bid.getDouble("price") > 0 && bid.getDouble("size") > 0) {
                    addBid(new ObjTradeBookOrderRecord("buy", priceCurrency, quantityCurrency, bid));
                    chart.addBid(Float.parseFloat(Double.toString(bid.getDouble("price"))),
                            Float.parseFloat(Double.toString(bid.getDouble("size"))));
                }
                if (ask.getDouble("price") > 0 && ask.getDouble("size") > 0) {
                    addAsk(new ObjTradeBookOrderRecord("sell", priceCurrency, quantityCurrency, ask));
                    chart.addAsk(Float.parseFloat(Double.toString(ask.getDouble("price"))),
                            Float.parseFloat(Double.toString(ask.getDouble("size"))));
                }
            }
        } catch (JSONException e) {
            Log.e("ObjTradeBook","Error parsing JSON");
        }
    }

    //Methods
    public void clearOrderBook(){
        this.bids = new ArrayList<ObjTradeBookOrderRecord>();
        this.asks = new ArrayList<ObjTradeBookOrderRecord>();
        this.chart = new ObjTradeBookChart();
        this.priceCurrency = "";
        this.quantityCurrency = "";
    }



    public void addBid(ObjTradeBookOrderRecord item){
        this.bids.add(item);
    }
    public void addAsk(ObjTradeBookOrderRecord item) {
        this.asks.add(item);
    }


    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("bids",bids);
        bundle.putParcelableArrayList("asks",asks);
        bundle.putParcelable("chart",chart);
        bundle.putString("priceCurrency", priceCurrency);
        bundle.putString("quantityCurrency", quantityCurrency);
        out.writeBundle(bundle);

    }
    public ObjTradeBook(Parcel in) {
        Bundle b = in.readBundle(ObjTradeBookOrderRecord.class.getClassLoader());
        this.bids = b.getParcelableArrayList("bids");
        this.asks = b.getParcelableArrayList("asks");
        this.chart = b.getParcelable("chart");
        this.priceCurrency = b.getString("priceCurrency");
        this.quantityCurrency = b.getString("quantityCurrency");
    }
    public static final Creator<ObjTradeBook> CREATOR = new Creator<ObjTradeBook>() {
        @Override
        public ObjTradeBook createFromParcel(Parcel parcel) {
            return new ObjTradeBook(parcel);
        }

        @Override
        public ObjTradeBook[] newArray(int i) {
            return new ObjTradeBook[i];
        }
    };
}
