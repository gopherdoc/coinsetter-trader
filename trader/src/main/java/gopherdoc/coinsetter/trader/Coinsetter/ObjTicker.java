package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;


public class ObjTicker implements Parcelable {
    //Fields
    public Float bid;
    public Float ask;
    public Float lasttradeprice;

    //Constructors
    public ObjTicker(){
        this.bid = 0.0F;
        this.ask = 0.0F;
        this.lasttradeprice = 0.0F;

    }
    public ObjTicker(JSONObject item){
        try {
            JSONObject bidObject = item.getJSONObject("bid");
            JSONObject askObject = item.getJSONObject("ask");
            JSONObject lastObject = item.getJSONObject("last");
            this.bid = Float.parseFloat(bidObject.getString("price"));
            this.ask = Float.parseFloat(askObject.getString("price"));
            this.lasttradeprice = Float.parseFloat(lastObject.getString("price"));
        } catch (JSONException e) {
            Log.e("ObjTicker", "Error parsing JSONObject");
        }
    }
    //Methods
    public String print(){
        if (this.bid == null || this.ask == null || this.lasttradeprice == null){
            return "";
        } else {
            return "Bid: " + myFloatString(this.bid) +
                    " Ask: " + myFloatString(this.ask) + "\n" +
                    " Last: " + myFloatString(this.lasttradeprice);
        }
    }
    public String myFloatString(Float myFloat) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(5);
        return df.format(myFloat);
    }

    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeFloat(bid);
        out.writeFloat(ask);
        out.writeFloat(lasttradeprice);
    }
    public ObjTicker(Parcel in){
        this.bid = in.readFloat();
        this.ask = in.readFloat();
        this.lasttradeprice = in.readFloat();
    }
    public static final Creator<ObjTicker> CREATOR = new Creator<ObjTicker>() {
        @Override
        public ObjTicker createFromParcel(Parcel parcel) {
            return new ObjTicker(parcel);
        }

        @Override
        public ObjTicker[] newArray(int i) {
            return new ObjTicker[i];
        }
    };
}
