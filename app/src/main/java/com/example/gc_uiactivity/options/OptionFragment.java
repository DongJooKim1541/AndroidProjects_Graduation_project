package com.example.gc_uiactivity.options;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.home.HomeFragment;

import java.util.ArrayList;

public class OptionFragment  extends Fragment {
    ArrayList<String> items,items2;
    OptionAdapter Adapter;
    OptionSwitchAdapter optionSwitchAdapter;
    ListView listView,listView2;
    ChoiceProblemFragment choiceProblemFragment;
    ProfileImageUploadFragment profileImageUploadFragment;
    HomeFragment homeFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.optionfragment,container,false);


        choiceProblemFragment=new ChoiceProblemFragment();
        profileImageUploadFragment=new ProfileImageUploadFragment();
        homeFragment=new HomeFragment();

        listView=v.findViewById(R.id.lv_choice_option);
        items=new ArrayList<String>();
        items.add("문제 선택");
        items.add("프로필 사진 업로드");

        Adapter=new OptionAdapter(getActivity(),R.layout.list_form,items,listView);

        listView.setAdapter(Adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item=parent.getAdapter().getItem(position).toString();
                if(item.equals("문제 선택")){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,choiceProblemFragment).commit();
                }
                else if(item.equals("프로필 사진 업로드")){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,profileImageUploadFragment).commit();
                }
            }
        });


        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,homeFragment).commit();

                    return true;
                }
                else{
                    return false;
                }
            }
        });

        listView2=v.findViewById(R.id.lv_switch_option);
        items2=new ArrayList<String>();
        items2.add("퀴즈 잠금화면 사용 여부");
        optionSwitchAdapter=new OptionSwitchAdapter(getActivity(),R.layout.list_switch_form,items2,listView);

        listView2.setAdapter(optionSwitchAdapter);
        listView2.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        optionSwitchAdapter.notifyDataSetChanged();

        return v;
    }
}