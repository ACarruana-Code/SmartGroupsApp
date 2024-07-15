package com.cb.SmartGroups.fragments.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.tablayouts.student.HomeFragmentStudentAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * The fragment representing the Home Tab of the student.
 *
 * @author Martín Mateos Sánchez and Adrián Carruana Martín
 */
public class Home extends Fragment {

    private HomeFragmentStudentAdapter optionsAdapter;

    private ViewPager2 viewpager;
    private TabLayout tablayout;

    private String selectedCourse;
    private String selectedSubject;


    public Home(String selectedCourse, String selectedSubject){
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_student, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tablayout = view.findViewById(R.id.tabLayout);
        tablayout.setInlineLabel(false);
        viewpager = view.findViewById(R.id.fragment_container_student_home);

        optionsAdapter = new HomeFragmentStudentAdapter(this, selectedCourse, selectedSubject);
        viewpager.setAdapter(optionsAdapter);

        new TabLayoutMediator(tablayout, viewpager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Class");
                    tab.setIcon(R.drawable.ic_baseline_group_24);
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