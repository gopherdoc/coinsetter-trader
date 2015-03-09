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


public class ObjCollectionHistoryOrderRecord implements Parcelable {
    //Fields
    public ArrayList<ObjOrderRecord> collection;

    //Constructors
    public ObjCollectionHistoryOrderRecord(){
        collection = new ArrayList<ObjOrderRecord>();
    }
    public ObjCollectionHistoryOrderRecord(final JSONObject jObj) {
        collection = new ArrayList<ObjOrderRecord>();
        try {
            JSONArray jArray = jObj.getJSONArray("orderList");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject mObj = jArray.getJSONObject(i);
                collection.add(new ObjOrderRecord(mObj));
            }
        } catch (JSONException e) {
            Log.e("ObjCollectionHistoryOrderRecord", "Error parsing JSON");
        }
        Collections.sort(collection);
    }

    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        Bundle b = new Bundle();
        b.putParcelableArrayList("historyorders",collection);
        out.writeBundle(b);
    }
    public static final Creator<ObjCollectionHistoryOrderRecord> CREATOR = new Creator<ObjCollectionHistoryOrderRecord>() {
        @Override
        public ObjCollectionHistoryOrderRecord createFromParcel(Parcel parcel) {
            return new ObjCollectionHistoryOrderRecord(parcel);
        }

        @Override
        public ObjCollectionHistoryOrderRecord[] newArray(int i) {
            return new ObjCollectionHistoryOrderRecord[i];
        }
    };
    public ObjCollectionHistoryOrderRecord(Parcel in) {
        Bundle b = in.readBundle(ObjOrderRecord.class.getClassLoader());
        collection = b.getParcelableArrayList("historyorders");
    }
}
