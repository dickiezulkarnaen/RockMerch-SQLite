package com.example.dickiez.rockmerch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dickiez on 4/11/2018.
 */

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private  int layout;
    private List<TshirtModel> tshirtList;

    public CustomAdapter(Context context, int layout, List<TshirtModel> tshirtList) {
        this.context = context;
        this.layout = layout;
        this.tshirtList = tshirtList;
    }

    @Override
    public int getCount() {
        return tshirtList.size();
    }

    @Override
    public Object getItem(int position) {
        return tshirtList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView txtName, txtPrice;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);

            holder.txtName = (TextView) row.findViewById(R.id.txt_name_list);
            holder.txtPrice = (TextView) row.findViewById(R.id.txt_price_list);
            holder.imageView = (ImageView) row.findViewById(R.id.img_list);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }

        TshirtModel listTshirt = tshirtList.get(position);

        holder.txtName.setText(listTshirt.getName());
        holder.txtPrice.setText(listTshirt.getPrice());

        byte[] image = listTshirt.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        holder.imageView.setImageBitmap(bitmap);

        return row;
    }
}
