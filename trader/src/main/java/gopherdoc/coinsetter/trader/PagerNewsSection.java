package gopherdoc.coinsetter.trader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionHistoryOrderRecord;
import gopherdoc.coinsetter.trader.Coinsetter.ObjCollectionNewsAlert;
import gopherdoc.coinsetter.trader.Coinsetter.ObjNewsAlert;
import gopherdoc.coinsetter.trader.Coinsetter.ObjOrderRecord;


public class PagerNewsSection extends Fragment {
    public static PagerNewsSection init() {
        return new PagerNewsSection();
    }
    private ArrayList<ObjNewsAlert> collection;
    private MyAdapter myAdapter;
    private ListView lv;
    public PagerNewsSection(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager_news, null);
        assert rootView != null;
        lv = (ListView) rootView.findViewById(R.id.lv_news);
        Bundle args = getArguments();
        if (args != null) {
            ObjCollectionNewsAlert newsAlerts = args.getParcelable("newsList");
            collection = newsAlerts.collection;
        } else {
            collection = new ArrayList<ObjNewsAlert>();
        }
        if (savedInstanceState != null){
            if (savedInstanceState.getParcelableArrayList("news") != null){
                collection = savedInstanceState.getParcelableArrayList("news");
            }
        }
        myAdapter = new MyAdapter(getActivity(), collection);
        lv.setAdapter(myAdapter);
        return rootView;
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("news",collection);
    }

    private class MyAdapter extends ArrayAdapter<ObjNewsAlert> {
        private final Context context;
        private final ArrayList<ObjNewsAlert> news;

        public MyAdapter(Context context, ArrayList<ObjNewsAlert> news) {
            super(context, R.layout.lvitem_news, news);
            this.context = context;
            this.news = news;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemNews rowView = new ListItemNews(context);
            rowView.setNews(news.get(position));
            return rowView;
        }
    }
    public void updateNews(ObjCollectionNewsAlert newNews){
        if (myAdapter != null) {
            collection = newNews.collection;
            myAdapter.clear();
            for (ObjNewsAlert article : newNews.collection) {
                myAdapter.add(article);
            }
            myAdapter.notifyDataSetChanged();
        }
    }
    public void clearHistory(){
        updateNews(new ObjCollectionNewsAlert());
    }

}
