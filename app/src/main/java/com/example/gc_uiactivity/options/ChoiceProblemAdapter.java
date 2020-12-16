package com.example.gc_uiactivity.options;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gc_uiactivity.R;

import java.util.ArrayList;

public class ChoiceProblemAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> arr;
    private ListView myList;

    //생성자
    public ChoiceProblemAdapter(Context context, int resource, ArrayList<String> arr, ListView myList){
        super(context,resource,arr);

        this.context=context;
        this.arr=arr;
        this.myList=myList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //인플레이션 작업 준비
        LayoutInflater linf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //시스템상에 있는 인플레이터를 가져다가 객체로 만드는 작업.

        //인플레이션 작업
        convertView=linf.inflate(R.layout.list_form,null);

        String str=arr.get(position);

        //객체 받기
        TextView txt_form = (TextView) convertView.findViewById(R.id.list_form);

        txt_form.setText(str);

        return convertView;
    }
}
