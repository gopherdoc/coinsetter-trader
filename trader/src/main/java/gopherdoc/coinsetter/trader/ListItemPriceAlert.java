package gopherdoc.coinsetter.trader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import gopherdoc.coinsetter.trader.Coinsetter.ObjPriceAlert;

public class ListItemPriceAlert extends LinearLayout {
    private Integer position;
    private TextView tv_info;
    private TextView tv_method;
    private TextView btn;
    private ObjPriceAlert alert;
    private OnCancelPriceAlertItemSelectedListener listener;

    public ListItemPriceAlert(final Context context) {
        super(context, null);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.lvitem_pricealert, this);
        tv_info = (TextView) findViewById(R.id.tv_pricealert_info);
        tv_method = (TextView) findViewById(R.id.tv_pricealert_methods);
        btn = (TextView) findViewById(R.id.btn_pricealert_cancel);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.CancelPriceAlertItemSelected(position, alert);
            }
        });
    }

    public interface OnCancelPriceAlertItemSelectedListener {
        public void CancelPriceAlertItemSelected(Integer position, ObjPriceAlert alert);
    }
    public void setAlert(PagerPriceAlertSection pageAlert, Integer position, ObjPriceAlert myAlert){
        listener = pageAlert;
        this.position = position;
        this.alert = myAlert;
        tv_info.setText(myAlert.printInfo());
        tv_method.setText(myAlert.printMethods());
    }
}
