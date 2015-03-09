package gopherdoc.coinsetter.trader.Coinsetter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;

import gopherdoc.coinsetter.trader.QR.Contents;
import gopherdoc.coinsetter.trader.QR.QRCodeEncoder;

public class ObjBalanceCoin implements Parcelable {
    public String name;
    public String symbol;
    public Float balance;


    //Constructors
    public ObjBalanceCoin(String name, String sym){
        this.name = name;
        this.symbol = sym.toUpperCase();
        this.balance = 0.0F;

    }
    public ObjBalanceCoin(String address){
        this.name = "Bitcoin";
        this.symbol = "BTC";
        this.balance = 0.0F;
    }
    public ObjBalanceCoin(){
        this.name = "Bitcoin";
        this.symbol = "BTC";
        this.balance = 0.0F;
    }

    //Getters
    public String getBalanceString() {
        DecimalFormat df = new DecimalFormat("#.#");
        int decimals = 4;
        df.setMaximumFractionDigits(decimals);
        return df.format(balance);
    }

    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(name);
        out.writeString(symbol);
        out.writeFloat(balance);

    }
    public ObjBalanceCoin(Parcel in){
        name = in.readString();
        symbol = in.readString();
        balance = in.readFloat();
    }

    public static final Creator<ObjBalanceCoin> CREATOR = new Creator<ObjBalanceCoin>() {
        @Override
        public ObjBalanceCoin createFromParcel(Parcel parcel) {
            return new ObjBalanceCoin(parcel);
        }

        @Override
        public ObjBalanceCoin[] newArray(int i) {
            return new ObjBalanceCoin[i];
        }
    };

}
