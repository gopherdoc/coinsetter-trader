package gopherdoc.coinsetter.trader;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import gopherdoc.coinsetter.trader.Coinsetter.ObjNewsAlert;

public class ListItemNews extends LinearLayout {
    private final TextView date;
    private final TextView type;
    private final TextView message;

    //Constructor
    public ListItemNews(Context context) {
        super(context, null);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.lvitem_news, this);
        type = (TextView)findViewById(R.id.news_type);
        date = (TextView)findViewById(R.id.news_date);
        message = (TextView)findViewById(R.id.news_message);
        message.setMovementMethod(LinkMovementMethod.getInstance());
    }
    public void setNews(ObjNewsAlert news){
        type.setText(news.messageType);
        date.setText(news.createDate);
        message.setText(Html.fromHtml(news.message));
    }
}
