package com.cb.SmartGroups.utils.dialogs.commondialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.expandablelistviews.CourseExpandableListAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectDisplayedCourse extends DialogFragment implements CourseExpandableListAdapter.OnChildClick {

    private HashMap<String, ArrayList<String>> detail;
    private onSelectedCourse onSelectedCourse;

    public SelectDisplayedCourse(HashMap<String, ArrayList<String>> detail){
        this.detail = detail;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            onSelectedCourse = (onSelectedCourse) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSelectedCourse");
        }
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_dialogs_selectdisplayedcourse, null);

        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);

        CourseExpandableListAdapter adapter = new CourseExpandableListAdapter(detail, this);
        expandableListView.setAdapter(adapter);

        builder.setView(view).setTitle("Select the course/subject you want to display information about");

        return builder.create();
    }

    // Get selected course and selected subject
    @Override
    public void onClick(String selectedCourse, String selectedSubject) {
        onSelectedCourse.onSelectedCourseChange(selectedCourse, selectedSubject);
    }

    public interface onSelectedCourse {
        void onSelectedCourseChange(String selectedCourse, String selectedSubject);
    }

}
