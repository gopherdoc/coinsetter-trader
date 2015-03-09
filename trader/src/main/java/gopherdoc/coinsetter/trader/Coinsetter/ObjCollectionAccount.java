package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ObjCollectionAccount implements Parcelable {
    //Fields
    public ArrayList<ObjAccount> collection;

    //Constructors
    public ObjCollectionAccount(){
        collection = new ArrayList<ObjAccount>();
    }
    public ObjCollectionAccount(final JSONObject jObj) {
        collection = new ArrayList<ObjAccount>();
        try {
            JSONArray jArray = jObj.getJSONArray("accountList");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject mObj = jArray.getJSONObject(i);
                collection.add(new ObjAccount(mObj));
            }
        } catch (JSONException e) {
            Log.e("ObjCollectionAccount", "Error parsing JSON");
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
        b.putParcelableArrayList("accounts",collection);
        out.writeBundle(b);
    }
    public static final Creator<ObjCollectionAccount> CREATOR = new Creator<ObjCollectionAccount>() {
        @Override
        public ObjCollectionAccount createFromParcel(Parcel parcel) {
            return new ObjCollectionAccount(parcel);
        }

        @Override
        public ObjCollectionAccount[] newArray(int i) {
            return new ObjCollectionAccount[i];
        }
    };
    public ObjCollectionAccount(Parcel in) {
        Bundle b = in.readBundle(ObjAccount.class.getClassLoader());
        collection = b.getParcelableArrayList("accounts");
    }
}
