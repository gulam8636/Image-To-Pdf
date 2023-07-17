package com.ali.pdf.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ali.pdf.R;

import java.util.ArrayList;

public class Gridview_Adapter extends BaseAdapter {
    ArrayList<Uri> uriArrayList;
public Gridview_Adapter(){

}
    public Gridview_Adapter(ArrayList<Uri> uriArrayList) {
        this.uriArrayList = uriArrayList;
    }

    @Override
    public int getCount() {
        return uriArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_adapter,parent,false);

            ImageView imageView = convertView.findViewById(R.id.Grid_imageview);


            imageView.setImageURI(uriArrayList.get(position));
        }
        return convertView;
    }
}
