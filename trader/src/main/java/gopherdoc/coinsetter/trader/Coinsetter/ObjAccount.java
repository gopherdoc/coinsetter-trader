package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjAccount implements Parcelable{
    public String accountUUID;
    public String customerUUID;
    public String accountNum;
    public String accountName;
    public String accountDesc;
    public String accountClass;
    public String activeStatus;
    public ObjBalanceCoin BTC;
    public ObjBalanceCoin USD;

    public ObjAccount(){
        accountUUID = "";
        customerUUID = "";
        accountNum = "";
        accountName = "";
        accountDesc = "";
        accountClass = "";
        activeStatus = "";
        BTC = new ObjBalanceCoin("Bitcoin", "BTC");
        USD = new ObjBalanceCoin("Dollars", "USD");
    }
    public ObjAccount(JSONObject jObj) {
        accountUUID = "";
        customerUUID = "";
        accountNum = "";
        accountName = "";
        accountDesc = "";
        accountClass = "";
        activeStatus = "";
        BTC = new ObjBalanceCoin("Bitcoin", "BTC");
        USD = new ObjBalanceCoin("Dollars", "USD");
        try {
            accountUUID = jObj.getString("accountUuid");
            customerUUID = jObj.getString("customerUuid");
            accountNum = jObj.getString("accountNumber");
            accountName = jObj.getString("name");
            accountDesc = jObj.getString("description");
            accountClass = jObj.getString("accountClass");
            activeStatus = jObj.getString("activeStatus");
            BTC.balance = (float)jObj.getDouble("btcBalance");
            USD.balance = (float)jObj.getDouble("usdBalance");
        } catch (JSONException e) {
            Log.e("ObjAccount", "Error parsing JSON");
        }
    }


    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeParcelable(BTC,i);
        out.writeParcelable(USD,i);
        out.writeString(accountUUID);
        out.writeString(customerUUID);
        out.writeString(accountNum);
        out.writeString(accountName);
        out.writeString(accountDesc);
        out.writeString(accountClass);
        out.writeString(activeStatus);
    }
    public ObjAccount(Parcel in){
        this.BTC = in.readParcelable(ObjBalanceCoin.class.getClassLoader());
        this.USD = in.readParcelable(ObjBalanceCoin.class.getClassLoader());
        this.accountUUID = in.readString();
        this.customerUUID = in.readString();
        this.accountNum = in.readString();
        this.accountNum = in.readString();
        this.accountDesc = in.readString();
        this.accountClass = in.readString();
        this.activeStatus = in.readString();
    }

    public static final Parcelable.Creator<ObjAccount> CREATOR = new Parcelable.Creator<ObjAccount>() {
        @Override
        public ObjAccount createFromParcel(Parcel parcel) {
            return new ObjAccount(parcel);
        }

        @Override
        public ObjAccount[] newArray(int i) {
            return new ObjAccount[i];
        }
    };
}
