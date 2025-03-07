package com.cb.SmartGroups.fragments.teacher.administration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.tablayouts.teacher.AdministrationFragmentTeacherAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * The fragment representing the Administration Tab of the teacher.
 *
 * @author Martín Mateos Sánchez and Adrián Carruana Martín
 */
public class Administration extends Fragment {
    AdministrationFragmentTeacherAdapter optionsAdapter;

    private ViewPager2 viewpager;
    private TabLayout tablayout;

    private String selectedCourse;
    private String selectedSubject;

    public Administration(String selectedCourse, String selectedSubject){
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_administration_teacher, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tablayout = view.findViewById(R.id.tabLayout);
        tablayout.setInlineLabel(false);
        viewpager = view.findViewById(R.id.fragment_container_teacher_administration);

        optionsAdapter = new AdministrationFragmentTeacherAdapter(this, selectedCourse, selectedSubject);
        viewpager.setAdapter(optionsAdapter);

        new TabLayoutMediator(tablayout, viewpager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Statistics");
                    tab.setIcon(R.drawable.ic_baseline_pie_chart_24);
                    break;
                case 1:
                    tab.setText("Petitions");
                    tab.setIcon(R.drawable.ic_baseline_notifications_none_24);
                    break;
                case 2:
                    tab.setText("Events");
                    tab.setIcon(R.drawable.ic_baseline_event_24);
                    break;
            }
        }).attach();
    }

}