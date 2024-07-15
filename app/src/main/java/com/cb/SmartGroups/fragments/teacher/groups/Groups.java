package com.cb.SmartGroups.fragments.teacher.groups;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.tablayouts.GroupsFragmentAdapter;

import com.cb.SmartGroups.utils.dialogs.teacherdialogs.CreateGroupDialog;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.AdministrateGroupsDialog;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.CreateAutomaticDialog;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.NextStepPattern;
import com.cb.SmartGroups.utils.utilities.ButtonAnimator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * The fragment representing the Groups/Chat Tab of the teacher.
 *
 * @author Martín Mateos Sánchez and Adrián Carruana Martín
 */
public class Groups extends Fragment {

    // Firebase
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    // Views
    private TabLayout tablayout;
    private ViewPager2 viewpager;

    // Animator for the buttons
    ButtonAnimator buttonAnimator;
    FloatingActionButton createGroup;
    FloatingActionButton administrateGroups;
    FloatingActionButton createAutomaticGroup;
    FloatingActionButton createManualGroup;

    // Adapter
    private GroupsFragmentAdapter optionsAdapter;

    private int userType;

    private String selectedCourse;
    private String selectedSubject;

    public Groups(){

    }

    public Groups(int userType, String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
        this.userType = userType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        createGroup = view.findViewById(R.id.createGroup);
        ArrayList<FloatingActionButton> buttons = new ArrayList<FloatingActionButton>();

        administrateGroups = view.findViewById(R.id.administrateGroups);
        createAutomaticGroup = view.findViewById(R.id.createAutomaticGroup);
        createManualGroup = view.findViewById(R.id.createManualGroup);

        buttons.add(administrateGroups);
        buttons.add(createAutomaticGroup);
        buttons.add(createManualGroup);

        buttonAnimator = new ButtonAnimator(getContext(), createGroup, buttons);

        createGroup.setOnClickListener(v -> {
            buttonAnimator.onButtonClicked();
        });

        administrateGroups.setOnClickListener(v -> {
            fStore
                    .collection("CoursesOrganization")
                    .document(selectedCourse)
                    .collection("Subjects")
                    .document(selectedSubject)
                    .collection("CollectiveGroups")
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if(queryDocumentSnapshots.size() < 2){
                            Toast.makeText(getContext(), "There must be at least two groups created to use this option", Toast.LENGTH_LONG).show();
                        } else {
                            //AdministrateGroupsDialog dialog = new AdministrateGroupsDialog(selectedCourse, selectedSubject);
                            NextStepPattern dialog = new NextStepPattern(selectedCourse, selectedSubject);
                            dialog.show(getParentFragmentManager(), "dialog");
                        }
                    });
        });

        createAutomaticGroup.setOnClickListener(v -> {
            CreateAutomaticDialog dialog = new CreateAutomaticDialog(selectedCourse, selectedSubject);
            dialog.show(getParentFragmentManager(), "dialog");
        });

        createManualGroup.setOnClickListener(v -> {
            CreateGroupDialog dialog = new CreateGroupDialog(selectedCourse, selectedSubject);
            dialog.show(getParentFragmentManager(), "dialog");
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tablayout = view.findViewById(R.id.tabLayout);
        viewpager = view.findViewById(R.id.fragmentContainer);

        tablayout.setInlineLabel(false);

        optionsAdapter = new GroupsFragmentAdapter(this, userType, selectedCourse, selectedSubject);
        viewpager.setAdapter(optionsAdapter);

        new TabLayoutMediator(tablayout, viewpager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Groups");
                    tab.setIcon(R.drawable.ic_group);
                    break;
                case 1:
                    tab.setText("Individuals");
                    tab.setIcon(R.drawable.ic_baseline_person_24);
                    break;
            }
        }).attach();
    }

}