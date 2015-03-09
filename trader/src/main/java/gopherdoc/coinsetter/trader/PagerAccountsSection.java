package gopherdoc.coinsetter.trader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

import gopherdoc.coinsetter.trader.Coinsetter.ObjAccount;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionAccount;

public class PagerAccountsSection extends Fragment implements
        ListItemAccount.OnAccountItemSelectedListener{
    static PagerAccountsSection init() {return new PagerAccountsSection();}
    public PagerAccountsSection(){}
    private ArrayList<ObjAccount> accounts;
    private AccountsArrayAdapter mAdapter;
    private AccountsPagerListener listener;


    public interface AccountsPagerListener {
        public void onAccountTradeSelected(ObjAccount item);
        public void onAccountHistorySelected(ObjAccount item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager_account,null);
        assert rootView != null;
        Bundle args = getArguments();
        if (args != null) {
            ObjCollectionAccount pAccounts = args.getParcelable("AccountCollection");
            accounts = pAccounts.collection;
        } else {
            accounts = new ArrayList<ObjAccount>();
        }
        if (savedInstanceState != null){
            if (savedInstanceState.getParcelableArrayList("accounts") != null){
                accounts = savedInstanceState.getParcelableArrayList("accounts");
            }
        }
        ListView lv_balances = (ListView) rootView.findViewById(R.id.lv_accounts);
        mAdapter = new AccountsArrayAdapter(getActivity(),accounts,this);
        lv_balances.setAdapter(mAdapter);
        lv_balances.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    ObjAccount acct = mAdapter.getItem(i);
                    listener.onAccountTradeSelected(acct);
                }
            }
        });
        return rootView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("accounts",accounts);
    }
    private class AccountsArrayAdapter extends ArrayAdapter<ObjAccount> {
        private final Context context;
        private final ArrayList<ObjAccount> values;
        private PagerAccountsSection section;

        public AccountsArrayAdapter(Context context, ArrayList<ObjAccount> objects, PagerAccountsSection section) {
            super(context, R.layout.lvitem_account, objects);
            this.context = context;
            this.values = objects;
            this.section = section;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ListItemAccount rowView = new ListItemAccount(context);
            rowView.setAccount(section, values.get(position));
            if (values.get(position) != null){
                //todo - exclude closed/testing
                rowView.updateBalances(values.get(position));
            } else {rowView.updateBalances(new ObjAccount());}
            return rowView;
        }
    }
    public void updateAccounts(ObjCollectionAccount collection){
        if (mAdapter != null) {
            mAdapter.clear();
            accounts = collection.collection;
            for (ObjAccount account : accounts) {
                mAdapter.add(account);
            }
            mAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void AccountItemSelectedTrade(ObjAccount acct) {
        listener.onAccountTradeSelected(acct);
    }

    @Override
    public void AccountItemSelectedHistory(ObjAccount acct) { listener.onAccountHistorySelected(acct); }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AccountsPagerListener) {
            listener = (AccountsPagerListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement PagerTradeSection.TradePagerListener");
        }

    }
    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

}
