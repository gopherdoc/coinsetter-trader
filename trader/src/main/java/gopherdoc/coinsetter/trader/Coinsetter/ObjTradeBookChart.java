package gopherdoc.coinsetter.trader.Coinsetter;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ObjTradeBookChart implements Parcelable {
    private ArrayList<Float> x1;
    private ArrayList<Float> x2;
    private ArrayList<Float> y;
    private ArrayList<Float> z;
    private Float zSum;
    private Float ySum;
    private LineGraphSeries<DataPoint> bidsSeries;
    private LineGraphSeries<DataPoint> asksSeries;
    private ArrayList data;


    public ObjTradeBookChart(){
        clearChart();
    }
    public void clearChart(){
        x1 = new ArrayList<Float>();
        x2 = new ArrayList<Float>();
        y = new ArrayList<Float>();
        z = new ArrayList<Float>();
        ySum = 0F;
        zSum = 0F;
    }

    public void addBid(Float price, Float volume){
        ySum += volume;
        if (x1.size() > 0 && x1.get(x1.size() - 1).equals(price)){
            y.set(x1.size()-1,ySum);
        } else {
            x1.add(price);
            y.add(ySum);
        }
    }

    public void addAsk(Float price, Float volume){
        zSum += volume;
        if(x2.size() > 0 && x2.get(x2.size() - 1).equals(price)){
            z.set(x2.size()-1,zSum);
        } else {
            x2.add(price);
            z.add(zSum);
        }
    }
    public ArrayList buildChart(){
        ArrayList<DataPoint> asks = new ArrayList<DataPoint>();
        ArrayList<DataPoint> bids = new ArrayList<DataPoint>();

        for (int i = 0; i < x1.size(); i++) {
            bids.add(new DataPoint(x1.get(i), y.get(i)));

        }
        DataPoint[] bidsArray = bids.toArray(new DataPoint[bids.size()]);
        bidsSeries = new LineGraphSeries<DataPoint>(bidsArray);
        for (int i = 0; i < x2.size(); i++) {
            asks.add(new DataPoint(x2.get(i), z.get(i)));
        }
        DataPoint[] asksArray = asks.toArray(new DataPoint[asks.size()]);
        asksSeries = new LineGraphSeries<DataPoint>(asksArray);
        data = new ArrayList<LineGraphSeries<DataPoint>>();
        data.add(bidsSeries);
        data.add(asksSeries);
        if (y.get(y.size()-1) > z.get(z.size()-1)){
            data.add(y.get(y.size()-1)); //Ymax
        } else {
            data.add(z.get(z.size()-1)); //Ymax
        }
        data.add(x1.get(x1.size()-1)); //leftbound
        data.add(x2.get(x2.size()-1)); //rightbound
        return data;
    }

    private float[] toArray(List<Float> in){
        float[] result = new float[in.size()];
        for(int i=0; i< result.length; i++){
            result[i] = in.get(i);
        }
        return result;
    }
    private List<Float> toList(float[] in){
        List<Float> result = new ArrayList<Float>(in.length);
        for(float f: in){
            result.add(f);
        }
        return result;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putFloatArray("x1",toArray(x1));
        bundle.putFloatArray("x2",toArray(x2));
        bundle.putFloatArray("y",toArray(y));
        bundle.putFloatArray("z",toArray(z));
        out.writeBundle(bundle);
    }
    public ObjTradeBookChart(Parcel in){
        Bundle b = in.readBundle();
        x1 = (ArrayList<Float>)toList(b.getFloatArray("x1"));
        x2 = (ArrayList<Float>)toList(b.getFloatArray("x2"));
        y = (ArrayList<Float>)toList(b.getFloatArray("y"));
        z = (ArrayList<Float>)toList(b.getFloatArray("z"));
    }
    public static final Creator<ObjTradeBookChart> CREATOR = new Creator<ObjTradeBookChart>() {
        @Override
        public ObjTradeBookChart createFromParcel(Parcel parcel) {
            return new ObjTradeBookChart(parcel);
        }

        @Override
        public ObjTradeBookChart[] newArray(int size) {
            return new ObjTradeBookChart[size];
        }
    };
}
