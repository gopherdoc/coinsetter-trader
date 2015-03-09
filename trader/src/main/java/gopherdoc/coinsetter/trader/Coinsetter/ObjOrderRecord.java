package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class ObjOrderRecord implements Parcelable, Comparable<ObjOrderRecord> {
    //Fields
    public String orderUuid;
    private String accountUuid;
    private String orderNum;
    private String stage;
    private String symbol;
    public String tradeside;
    public Float requestedQuantity;
    public Float requestedPrice;
    public Float quantityOpen;
    public Float quantityFilled;
    public ObjTimestamp opendate;

    //Constructors

    public ObjOrderRecord(JSONObject item){
        try {
            this.orderUuid = item.getString("uuid");
            this.accountUuid = item.getString("accountUuid");
            this.orderNum = item.getString("orderNumber");
            this.stage = item.getString("stage");
            this.symbol = item.getString("symbol");
            this.tradeside = item.getString("side");
            this.requestedPrice = Float.parseFloat(Double.toString(item.getDouble("requestedPrice")));
            this.requestedQuantity = Float.parseFloat(Double.toString(item.getDouble("requestedQuantity")));
            this.quantityOpen = Float.parseFloat(Double.toString(item.getDouble("openQuantity")));
            this.quantityFilled = Float.parseFloat(Double.toString(item.getDouble("filledQuantity")));
            this.opendate = new ObjTimestamp(Integer.parseInt(Long.toString(item.getLong("createDate")/1000L)));
        } catch (JSONException e) {
            Log.e("ObjOrderRecord", "Error parsing JSON");
        }
    }
    public ObjOrderRecord(){
        this.orderUuid = "";
        this.accountUuid = "";
        this.orderNum = "";
        this.stage = "";
        this.symbol = "";
        this.tradeside = "";
        this.requestedPrice = 0.0F;
        this.requestedQuantity = 0.0F;
        this.quantityOpen = 0.0F;
        this.quantityFilled = 0.0F;
        long stamp = System.currentTimeMillis()/1000L;
        this.opendate = new ObjTimestamp(Integer.parseInt(Long.toString(stamp)));
    }
    public ObjOrderRecord clone(){
        ObjOrderRecord clone = new ObjOrderRecord();
        clone.orderUuid = this.orderUuid;
        clone.accountUuid = this.accountUuid;
        clone.orderNum = this.orderNum;
        clone.stage = this.stage;
        clone.symbol = this.symbol;
        clone.tradeside = this.tradeside;
        clone.requestedPrice = this.requestedPrice;
        clone.requestedQuantity = this.requestedQuantity;
        clone.quantityOpen = this.quantityOpen;
        clone.quantityFilled = this.quantityFilled;
        clone.opendate = this.opendate;
        return clone;
    }

    //Methods
    public String getPrice() {
        return (myFloatString(this.requestedPrice) + " USD");
    }
    public String getVolumeOpen() {
        return (myFloatString(this.quantityOpen) + " BTC");
    }
    public String getVolumeFilled() {
        return (myFloatString(this.quantityFilled) + " BTC");
    }
    public String getQuoteAmountFilled(){
        Float amount = this.requestedPrice * this.quantityFilled;
        return ("(" + myFloatString(amount) + " USD)");
    }
    public String getQuoteAmountOpen(){
        Float amount = this.requestedPrice * this.quantityOpen;
        return ("(" + myFloatString(amount) + " USD)");
    }
    public String myFloatString(Float flt) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(5);
        return df.format(flt);
    }

    public String getOpenOrderString() {
        StringBuilder text = new StringBuilder();
        text.append(getActiveOrderPosition());
        text.append(": ");
        text.append(getVolumeOpen());
        text.append(" at ");
        text.append(getPrice());
        text.append("\n");
        String quoteamount = myFloatString(this.quantityOpen * this.requestedPrice) + " USD";
        quoteamount = quoteamount.replace("(","");
        quoteamount = quoteamount.replace(")","");
        text.append("Total: ");
        text.append(quoteamount);
        return text.toString();
    }
    public String getFilledOrderString() {
        StringBuilder text = new StringBuilder();
        text.append(getActiveOrderPosition());
        text.append(": ");
        text.append(getVolumeFilled());
        text.append(" at ");
        text.append(getPrice());
        text.append("\n");
        String quoteamount = myFloatString(this.quantityFilled * this.requestedPrice) + " USD";
        quoteamount = quoteamount.replace("(","");
        quoteamount = quoteamount.replace(")","");
        text.append("Total: ");
        text.append(quoteamount);
        return text.toString();
    }

    public String getTimestamp(){
        if (opendate != null){
            return opendate.getStampstring();
        } else {
            return "";
        }
    }
    public String getPosition() {
        if (tradeside.equals("BUY")) {return "Bought";}
        else {return "Sold";}
    }
    public String getActiveOrderPosition(){
        if (tradeside.equals("BUY")) {return "Buy";}
        else {return "Sell";}
    }

    //Parcelable
    private ObjOrderRecord(Parcel in) {
        this.orderUuid = in.readString();
        this.accountUuid = in.readString();
        this.orderNum = in.readString();
        this.stage = in.readString();
        this.symbol = in.readString();
        this.tradeside = in.readString();
        this.requestedPrice = in.readFloat();
        this.requestedQuantity = in.readFloat();
        this.quantityOpen = in.readFloat();
        this.quantityFilled = in.readFloat();
        this.opendate = in.readParcelable(ObjTimestamp.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(orderUuid);
        parcel.writeString(accountUuid);
        parcel.writeString(orderNum);
        parcel.writeString(stage);
        parcel.writeString(symbol);
        parcel.writeString(tradeside);
        parcel.writeFloat(requestedPrice);
        parcel.writeFloat(requestedQuantity);
        parcel.writeFloat(quantityOpen);
        parcel.writeFloat(quantityFilled);
        parcel.writeParcelable(opendate,i);
    }
    public static final Creator<ObjOrderRecord> CREATOR = new Creator<ObjOrderRecord>(){
        @Override
        public ObjOrderRecord createFromParcel(Parcel parcel) {
            return new ObjOrderRecord(parcel);
        }

        @Override
        public ObjOrderRecord[] newArray(int i) {
            return new ObjOrderRecord[i];
        }
    };
    //Comparable
    @Override
    public int compareTo(ObjOrderRecord t) {
        return this.opendate.compareTo(t.opendate);
    }
}
