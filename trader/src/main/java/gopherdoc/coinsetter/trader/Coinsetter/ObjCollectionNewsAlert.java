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

public class ObjCollectionNewsAlert implements Parcelable{
    //Fields
    public ArrayList<ObjNewsAlert> collection;

    //Constructors
    public ObjCollectionNewsAlert(){
        collection = new ArrayList<ObjNewsAlert>();
    }
    public ObjCollectionNewsAlert(final JSONObject jObj) {
        collection = new ArrayList<ObjNewsAlert>();
        try {
            JSONArray jArray = jObj.getJSONArray("MessageList");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject mObj = jArray.getJSONObject(i);
                collection.add(new ObjNewsAlert(mObj));
            }
        } catch (JSONException e) {
            Log.e("ObjCollectionNewsAlert", "Error parsing JSON");
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
        b.putParcelableArrayList("newsalerts",collection);
        out.writeBundle(b);
    }
    public static final Parcelable.Creator<ObjCollectionNewsAlert> CREATOR = new Parcelable.Creator<ObjCollectionNewsAlert>() {
        @Override
        public ObjCollectionNewsAlert createFromParcel(Parcel parcel) {
            return new ObjCollectionNewsAlert(parcel);
        }

        @Override
        public ObjCollectionNewsAlert[] newArray(int i) {
            return new ObjCollectionNewsAlert[i];
        }
    };
    public ObjCollectionNewsAlert(Parcel in) {
        Bundle b = in.readBundle(ObjNewsAlert.class.getClassLoader());
        collection = b.getParcelableArrayList("newsalerts");
    }
}


