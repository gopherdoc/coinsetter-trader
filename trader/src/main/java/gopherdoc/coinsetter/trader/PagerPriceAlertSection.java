package gopherdoc.coinsetter.trader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionPriceAlert;
import gopherdoc.coinsetter.trader.Coinsetter.ObjPriceAlert;


public class PagerPriceAlertSection extends Fragment implements ListItemPriceAlert.OnCancelPriceAlertItemSelectedListener{
    public static PagerPriceAlertSection init() {
        return new PagerPriceAlertSection();
    }
    private PriceAlertPagerListener listener;
    private ArrayList<ObjPriceAlert> collection;
    private MyAdapter myAdapter;
    private ListView lv;
    private TextView tv;
    public interface PriceAlertPagerListener {
        public void onPriceAlertItemCanceled(ObjPriceAlert alert);
        public void onAddPriceAlertSelected(String condition, Integer price, String method);
    }

    public PagerPriceAlertSection(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager_pricealert, null);
        assert rootView != null;
        tv = (TextView) rootView.findViewById(R.id.btn_addpricealert);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_pricealert);
                dialog.setTitle("Set Price Alert");
                TextView cancelbtn = (TextView)dialog.findViewById(R.id.dialog_pricealert_cancel);
                cancelbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                TextView okbtn = (TextView) dialog.findViewById(R.id.dialog_pricealert_ok);
                okbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String condition;
                        RadioGroup radioGroup = (RadioGroup)dialog.findViewById(R.id.radgroup_pricealert_condition);
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_lt:
                                condition = "LESS";
                                break;
                            case R.id.radio_gt:
                                condition = "GREATER";
                                break;
                            case R.id.radio_crosses:
                            default:
                                condition = "CROSSES";
                                break;
                        }
                        EditText priceText = (EditText)dialog.findViewById(R.id.edittext_pricealert_target);
                        Integer price = Integer.parseInt(priceText.getText().toString());
                        CheckBox cbEmail = (CheckBox)dialog.findViewById(R.id.checkBox_pricealert_email);
                        boolean isEmail = cbEmail.isChecked();
                        CheckBox cbSMS = (CheckBox)dialog.findViewById(R.id.checkBox_pricealert_sms);
                        boolean isSMS = cbSMS.isChecked();
                        String method;
                        if (isEmail && isSMS){
                            method = "BOTH";
                            listener.onAddPriceAlertSelected(condition, price, method);
                            dialog.dismiss();
                        } else if (isEmail){
                            method = "EMAIL";
                            listener.onAddPriceAlertSelected(condition, price, method);
                            dialog.dismiss();
                        } else  if (isSMS) {
                            method = "TEXT";
                            listener.onAddPriceAlertSelected(condition, price, method);
                            dialog.dismiss();
                        } else {
                            Toast error = Toast.makeText(getActivity(),"Please select a notification method", Toast.LENGTH_SHORT);
                            error.show();
                        }
                    }
                });
                dialog.show();
            }
        });
        lv = (ListView) rootView.findViewById(R.id.lv_pricealerts);
        Bundle args = getArguments();
        if (args != null) {
            ObjCollectionPriceAlert myAlerts = args.getParcelable("PriceAlerts");
            collection = myAlerts.collection;
        } else {
            collection = new ArrayList<ObjPriceAlert>();
        }
        if (savedInstanceState != null){
            if (savedInstanceState.getParcelableArrayList("alerts") != null){
                collection = savedInstanceState.getParcelableArrayList("alerts");
            }
        }
        myAdapter = new MyAdapter(getActivity(), collection, this);
        lv.setAdapter(myAdapter);
        return rootView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("alerts",collection);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PriceAlertPagerListener) {
            listener = (PriceAlertPagerListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement PagerPriceAlertSection.PriceAlertPagerListener");
        }

    }
    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

    @Override
    public void CancelPriceAlertItemSelected(Integer pos, ObjPriceAlert alert) {
        cancelConfirm(alert, pos);
    }

    public void cancelConfirm(final ObjPriceAlert myAlert, final Integer myPos){
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle("Cancel alert?");
        String orderstring = myAlert.printInfo();
        myAlertDialog.setMessage(orderstring);
        myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                listener.onPriceAlertItemCanceled(myAlert);
            }
        });
        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }});
        AlertDialog alert = myAlertDialog.create();
        alert.show();
    }

    private class MyAdapter extends ArrayAdapter<ObjPriceAlert> {
        private final Context context;
        private final ArrayList<ObjPriceAlert> alerts;
        private PagerPriceAlertSection section;

        public MyAdapter(Context context, ArrayList<ObjPriceAlert> alerts, PagerPriceAlertSection section) {
            super(context, R.layout.lvitem_pricealert, alerts);
            this.context = context;
            this.alerts = alerts;
            this.section = section;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemPriceAlert rowView = new ListItemPriceAlert(context);
            rowView.setAlert(section, position, alerts.get(position));
            return rowView;
        }
        public void removeAt(int position){
            alerts.remove(position);
        }
    }
    public void updateAlerts(ObjCollectionPriceAlert alertCollection){
        if (myAdapter != null) {
            collection = alertCollection.collection;
            myAdapter.clear();
            for (ObjPriceAlert alert : alertCollection.collection) {
                myAdapter.add(alert);
            }
            myAdapter.notifyDataSetChanged();
        }
    }
    public void clearAlerts(){
        updateAlerts(new ObjCollectionPriceAlert());
    }

}
