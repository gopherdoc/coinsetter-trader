package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ObjCollectionPriceAlert implements Parcelable{
    //Fields
    public ArrayList<ObjPriceAlert> collection;

    //Constructors
    public ObjCollectionPriceAlert(){
        collection = new ArrayList<ObjPriceAlert>();
    }
    public ObjCollectionPriceAlert(final JSONObject jObj) {
        collection = new ArrayList<ObjPriceAlert>();
        try {
            JSONArray jArray = jObj.getJSONArray("PriceAlerts");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject mObj = jArray.getJSONObject(i);
                collection.add(new ObjPriceAlert(mObj));
            }
        } catch (JSONException e) {
            Log.e("ObjCollectionPriceAlert", "Error parsing JSON");
        }
    }

    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        Bundle b = new Bundle();
        b.putParcelableArrayList("pricealerts",collection);
        out.writeBundle(b);
    }
    public static final Parcelable.Creator<ObjCollectionPriceAlert> CREATOR = new Parcelable.Creator<ObjCollectionPriceAlert>() {
        @Override
        public ObjCollectionPriceAlert createFromParcel(Parcel parcel) {
            return new ObjCollectionPriceAlert(parcel);
        }

        @Override
        public ObjCollectionPriceAlert[] newArray(int i) {
            return new ObjCollectionPriceAlert[i];
        }
    };
    public ObjCollectionPriceAlert(Parcel in) {
        Bundle b = in.readBundle(ObjPriceAlert.class.getClassLoader());
        collection = b.getParcelableArrayList("pricealerts");
    }
}

