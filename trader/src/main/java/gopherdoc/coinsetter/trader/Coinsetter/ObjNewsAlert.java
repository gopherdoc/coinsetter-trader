package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ObjNewsAlert implements Parcelable{
    //Fields
    public String uuid;
    public String message;
    public String createDate;
    public String messageType;


    //Constructors
    public ObjNewsAlert(){
        this.uuid = "";
        this.message= "";
        this.createDate= "";
        this.messageType = "";

    }
    public ObjNewsAlert(JSONObject item){
        try {
            this.uuid = item.getString("uuid");
            this.message= item.getString("message");
            this.messageType= item.getString("messageType");
            this.createDate= item.getString("createDate");
        } catch (JSONException e) {
            Log.e("ObjNewsAlert", "Error parsing JSONObject");
        }
    }
    //Methods

    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(uuid);
        out.writeString(message);
        out.writeString(messageType);
        out.writeString(createDate);
    }
    public ObjNewsAlert(Parcel in){
        this.uuid = in.readString();
        this.message= in.readString();
        this.messageType= in.readString();
        this.createDate= in.readString();
    }
    public static final Parcelable.Creator<ObjNewsAlert> CREATOR = new Parcelable.Creator<ObjNewsAlert>() {
        @Override
        public ObjNewsAlert createFromParcel(Parcel parcel) {
            return new ObjNewsAlert(parcel);
        }

        @Override
        public ObjNewsAlert[] newArray(int i) {
            return new ObjNewsAlert[i];
        }
    };
}
