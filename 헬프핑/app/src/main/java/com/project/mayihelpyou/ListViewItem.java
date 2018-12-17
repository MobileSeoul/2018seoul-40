package com.project.mayihelpyou;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewItem extends BaseAdapter {
    private Drawable iconDrawable ;
    private String titleStr ;
    private String descStr ;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }

    LayoutInflater inflater = null;
    private ArrayList<HashMap<String,String>> m_oData = null;
Context context;
    public ListViewItem(Context context, ArrayList<HashMap<String,String>> _oData)
    {
        m_oData = _oData;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return m_oData.size();
    }

    @Override
    public Object getItem(int position)
    {
        return m_oData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    boolean tmp = false;
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        /*
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.simpletext, parent, false);
        }*/
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.simpletext,null);
        }

        tmp = true;
        for(HashMap<String,String> hashMap : m_oData)
            if(Integer.valueOf(hashMap.get("id")) < defaultValue.RESTROOM_DISABLE_START_INDEX) tmp = false;

        ImageView imageView = convertView.findViewById(R.id.imageView1);
        TextView oTextTitle = (TextView) convertView.findViewById(R.id.textView1);
        TextView oTextDate = (TextView) convertView.findViewById(R.id.textView2);

        oTextTitle.setText(String.format("%.22s%s",m_oData.get(position).get("name"),m_oData.get(position).get("name").length() > 22 ? "..." : ""));
        oTextDate.setText(m_oData.get(position).get("description"));
        if(m_oData.get(position).get("what_is_it").equals("화장실"))
            if(tmp) {
                if (Integer.valueOf(m_oData.get(position).get("id")) >= defaultValue.RESTROOM_DISABLE_START_INDEX)
                    imageView.setImageResource(R.drawable.disabled);
                else
                    imageView.setImageResource(R.drawable.toilet);
            }else{
                imageView.setImageResource(R.drawable.toilet);
            }
        else if(m_oData.get(position).get("what_is_it").equals("흡연부스"))
            imageView.setImageResource(R.drawable.smoking_area);
        else imageView.setImageResource(R.drawable.unknown);
        return convertView;
    }
}