package gopherdoc.coinsetter.trader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import gopherdoc.coinsetter.trader.Coinsetter.ObjAccount;

public class ListItemAccount extends LinearLayout {
    private TextView usdbalance;
    private TextView btcbalance;
    private TextView tradebtn;
    private TextView historybtn;
    private ObjAccount myAccount;
    private OnAccountItemSelectedListener listener;

    public ListItemAccount(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.lvitem_account, this);
        btcbalance = (TextView)findViewById(R.id.btcbalance);
        usdbalance = (TextView)findViewById(R.id.usdbalance);
        tradebtn = (TextView)findViewById(R.id.trade_btn);
        tradebtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.AccountItemSelectedTrade(myAccount);
            }
        });
        historybtn = (TextView)findViewById(R.id.history_btn);
        historybtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.AccountItemSelectedHistory(myAccount);
            }
        });

    }

    public void setAccount(PagerAccountsSection fragAcct, ObjAccount account){
        listener = fragAcct;
        myAccount = account;

    }

    public void setUSDBalance(String bal){
        usdbalance.setText(bal);
    }
    public void setBTCBalance(String bal){
        btcbalance.setText(bal);
    }
    public void updateBalances(ObjAccount acct){
        usdbalance.setText("$" + acct.USD.getBalanceString());
        btcbalance.setText("฿" + acct.BTC.getBalanceString());
    }

    public interface OnAccountItemSelectedListener {
        public void AccountItemSelectedTrade(ObjAccount acct);
        public void AccountItemSelectedHistory(ObjAccount acct); }
}
