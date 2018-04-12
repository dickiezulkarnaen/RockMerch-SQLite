package com.example.dickiez.rockmerch;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    List<TshirtModel> list;
    CustomAdapter adapter = null;
    ImageView imageView;
    public static DataHelper helper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        helper = new DataHelper(this, "DB_TSHIRT.sqlite", null, 1);

        helper.queryData("CREATE TABLE IF NOT EXISTS TB_TSHIRT(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR, PRICE VARCHAR, IMAGE BLOB)");

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab) ;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.list_view);
        list = new ArrayList<>();
        adapter = new CustomAdapter(this, R.layout.item_list, list);
        listView.setAdapter(adapter);

        Cursor cursor = helper.getData("SELECT * FROM TB_TSHIRT");
        list.clear();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String price = cursor.getString(2);
            byte[] image = cursor.getBlob(3);

            list.add(new TshirtModel(id, name, price, image));
        }
        adapter.notifyDataSetChanged();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {

                CharSequence[] items = {"Update", "Delete"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(ListActivity.this);

                dialog.setTitle("Choose an action");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {

                            Cursor c = helper.getData("SELECT ID FROM TB_TSHIRT");
                            ArrayList<Integer> id = new ArrayList<Integer>();
                            while (c.moveToNext()){
                                id.add(c.getInt(0));
                            }

                            showDialogUpdate(ListActivity.this, id.get(position));


                        } else {
                            Cursor c = helper.getData("SELECT ID FROM TB_TSHIRT");
                            ArrayList<Integer> id = new ArrayList<Integer>();
                            while (c.moveToNext()){
                                id.add(c.getInt(0));
                            }
                            showDialogDelete(id.get(position));
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    private void showDialogUpdate(Activity activity, final int position){

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.activity_update);
        dialog.setTitle("Update");

        imageView = (ImageView) dialog.findViewById(R.id.img_update);
        final EditText edtName = (EditText) dialog.findViewById(R.id.txt_name_update);
        final EditText edtPrice = (EditText) dialog.findViewById(R.id.txt_price_update);
        Button btnUpdate = (Button) dialog.findViewById(R.id.btn_update);

        TshirtModel tshirt = list.get(position);
        edtName.setText(tshirt.getName());

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);

        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(ListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        999);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    helper.updateData(
                            edtName.getText().toString().trim(),
                            edtPrice.getText().toString().trim(),
                            AddActivity.imageViewToByte(imageView), position
                    );
                    updateList();
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Update successfully!",Toast.LENGTH_SHORT).show();
                }
                catch (Exception error) {
                    Log.e("Can't Update", error.getMessage());
                }
            }
        });
    }

    private void showDialogDelete(final int id){
        final AlertDialog.Builder dialogDelete = new AlertDialog.Builder(ListActivity.this);

        dialogDelete.setTitle("Warning!");
        dialogDelete.setMessage("Are you sure you want to delete this item?");
        dialogDelete.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    helper.deleteData(id);
                    Toast.makeText(getApplicationContext(), "Delete successfully!",Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Log.e("Can't Delete", e.getMessage());
                }
                updateList();
            }
        });

        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.show();
    }

    private void updateList(){
        Cursor cursor = helper.getData("SELECT * FROM TB_TSHIRT");
        list.clear();
        while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String price = cursor.getString(2);
                byte[] image = cursor.getBlob(3);
                list.add(new TshirtModel(id, name, price, image));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 999){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 999);
            }
            else {
                Toast.makeText(getApplicationContext(), "Access Denied!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 999 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
