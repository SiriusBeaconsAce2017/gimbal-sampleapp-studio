package com.parking.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.com.parking.beans.LotAndDistance;
import com.gimbal.android.sample.R;

import java.util.ArrayList;

public class LotAdapter extends ArrayAdapter<LotAndDistance> {

    private Context context;
    private ArrayList<LotAndDistance> objects;

    public LotAdapter(Context context, int layoutResourceId, ArrayList<LotAndDistance> objects) {
        super(context, layoutResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.activity_lots, parent, false);

            LotAndDistance lotAndDistance = objects.get(position);

            if(lotAndDistance != null) {
                TextView lotID = (TextView) convertView.findViewById(R.id.lot_id);
                lotID.setText(lotAndDistance.getLot().getLotID());
                TextView lotName = (TextView) convertView.findViewById(R.id.lot_name);
                lotName.setText(lotAndDistance.getLot().getLotName());
                TextView lotDistance = (TextView) convertView.findViewById(R.id.lot_distance);
                lotDistance.setText(String.format("%s",lotAndDistance.getDistance()));
            }
        }
        return row;
    }
}
