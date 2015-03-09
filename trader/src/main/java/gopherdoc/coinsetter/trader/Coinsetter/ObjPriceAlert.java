package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ObjPriceAlert implements Parcelable{
    //Fields
    public String uuid;
    public String customerUuid;
    public String type;
    public String condition;
    public String price;
    public String symbol;
    public String createDate;


    //Constructors
    public ObjPriceAlert(){
        this.uuid = "";
        this.customerUuid= "";
        this.type= "";
        this.condition= "";
        this.price= "";
        this.symbol= "";
        this.createDate= "";

    }
    public ObjPriceAlert(JSONObject item){
        try {
            this.uuid = item.getString("uuid");
            this.type= item.getString("type");
            this.condition= item.getString("condition");
            this.price= myFloatString(Float.parseFloat(Double.toString(item.getDouble("price"))));
            this.createDate= item.getString("createDate");
        } catch (JSONException e) {
            Log.e("ObjPriceAlert", "Error parsing JSONObject");
        }
    }
    //Methods
    public String printInfo(){
        if (this.condition != null && this.price != null) {
            return "Alert when " + this.condition.toLowerCase() + " " + this.price;
        } else {
            return "";
        }
    }
    public String printMethods(){
        if (this.type != null) {
            if (this.type.equals("BOTH")) {
                return "Method: E-mail and SMS";
            } else if (this.type.equals("EMAIL")) {
                return "Method: E-mail";
            } else {
                return "Method: SMS";
            }
        } else {
            return "";
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
        out.writeString(uuid);
        out.writeString(customerUuid);
        out.writeString(type);
        out.writeString(condition);
        out.writeString(price);
        out.writeString(symbol);
        out.writeString(createDate);
    }
    public ObjPriceAlert(Parcel in){
        this.uuid = in.readString();
        this.customerUuid= in.readString();
        this.type= in.readString();
        this.condition= in.readString();
        this.price= in.readString();
        this.symbol= in.readString();
        this.createDate= in.readString();
    }
    public static final Parcelable.Creator<ObjPriceAlert> CREATOR = new Parcelable.Creator<ObjPriceAlert>() {
        @Override
        public ObjPriceAlert createFromParcel(Parcel parcel) {
            return new ObjPriceAlert(parcel);
        }

        @Override
        public ObjPriceAlert[] newArray(int i) {
            return new ObjPriceAlert[i];
        }
    };
}

