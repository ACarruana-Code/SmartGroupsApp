package com.cb.SmartGroups.utils.dialogs.teacherdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.utils.firesoredatamodels.Group;
import com.cb.SmartGroups.utils.restmodel.Subject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollaborativePatternSelectDialog extends DialogFragment {

    // Selected course and subject
    private String selectedCourse;
    private String selectedSubject;

    // Bach
    int studentsPerGroup;
    int numGroups;
    int remainder;
    ArrayList<String> studentIDs;

    // Radiogroup
    private RadioGroup radioGroup;
    private int checkedRadioButtonId;

    private int inputNumber;

    // Views to be shown
    private TextView modeTitle;
    private EditText numberInput;
    private CheckBox checkBox;
    private View separator;
    private TextView errorMessageView;

    // Firestore
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    // Context
    Context context;

    // Custom error message
    String customErrorMessage;

    // Reference to the subject of the selected course
    DocumentReference subjectRef;

    // Pattern
    String pattern;

    public CollaborativePatternSelectDialog(String selectedCourse, String selectedSubject, int studentsPerGroup, int numGroups, int remainder, ArrayList<String> studentIDs) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
        this.studentsPerGroup = studentsPerGroup;
        this.numGroups = numGroups;
        this.remainder = remainder;
        this.studentIDs = studentIDs;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        subjectRef = fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject);
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_dialogs_collaborativepatternselectdialog, null);

        // Views
        radioGroup = view.findViewById(R.id.radioGroup);

        separator = view.findViewById(R.id.separator);
        separator.setVisibility(View.GONE);

        errorMessageView = view.findViewById(R.id.errorMessage);
        errorMessageView.setVisibility(View.GONE);

        // RadioGroup configuration
        checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        // Inicializamos asi porque no recibimos el patron
        pattern = getString(R.string.Pyramid);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            checkedRadioButtonId = checkedId;
            if (checkedId == R.id.radio_button_1) {
                pattern = null;
            } else if (checkedId == R.id.radio_button_2) {
                pattern = getString(R.string.Jigsaw);
            } else if (checkedId == R.id.radioButton2) {
                pattern = getString(R.string.Pyramid);
            } else if (checkedId == R.id.radioButton3) {
                pattern = getString(R.string.Think_Pair_Share);
            }
        });

        builder.setView(view)
                .setTitle("Group creation menu")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Just closes the dialog
                }).setPositiveButton("Create groups", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(view1 -> {
                createGroupsBatch(studentsPerGroup, numGroups, remainder, studentIDs, pattern);
                dialog.dismiss();
            });
        });

        return dialog;
    }

    private void createGroupsBatch(int studentsPerGroup, int numGroups, int remainder, ArrayList<String> studentIDs, String pattern) {
        CollectionReference groupsCollRef;
        ArrayList<List<String>> subLists = new ArrayList<List<String>>();

        if (numGroups == studentIDs.size()) { // We want to create individual chats of all the students
            groupsCollRef = subjectRef.collection("IndividualGroups");
        } else {
            groupsCollRef = subjectRef.collection("CollectiveGroups");
        }

        for (int i = 0; i < numGroups; i++) {
            List<String> subList = studentIDs.subList(i * studentsPerGroup, i * studentsPerGroup + studentsPerGroup);
            subLists.add(subList);
        }

        if (remainder != 0) {

            boolean isChecked;
            SELECTED_MODE mode = getSelectedMode();

            if (mode == SELECTED_MODE.NUMBER_OF_GROUPS) {
                isChecked = true;
            } else {
                isChecked = checkBox.isChecked();
            }

            List<String> lastList;
            if (isChecked) { // Add the remainder students to a group greater than the specified group
                lastList = new ArrayList<String>(subLists.get(subLists.size() - 1));
                subLists.remove(subLists.size() - 1);

                for (int i = 0; i < remainder; i++) {
                    lastList.add(studentIDs.get(studentIDs.size() - i - 1));
                }

            } else { // Add the remainder students to a separate group
                lastList = new ArrayList<String>();

                for (int i = 0; i < remainder; i++) {
                    lastList.add(studentIDs.get(studentIDs.size() - i - 1));
                }

            }
            subLists.add(lastList);

        }

        groupsCollRef
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int maxIdentifier = Group.getMaxGroupIdentifier(queryDocumentSnapshots);

                    // Create the groups
                    for (int i = 0; i < subLists.size(); i++) {
                        Group.createGroup(groupsCollRef, selectedCourse, selectedSubject, subLists.get(i), maxIdentifier + 1 + i, context, null, null, pattern);
                    }

                });
    }

    private enum COMPLETE_OP_CODE {
        NO_ERROR,
        NO_OPTION_SELECTED,
        NO_INPUT_NUMBER,
        INPUT_NUMBER_NEGATIVE,
        INPUT_NUMBER_ZERO,
        ZERO_GROUPS,
        MORE_GROUPS_THAN_STUDENTS
    }

    private enum SELECTED_MODE {
        STUDENTS_PER_GROUP, NUMBER_OF_GROUPS, NO_MODE_SELECTED
    }

    private String getErrorMessage(COMPLETE_OP_CODE completeCode) {
        String errorMessage = null;
        SELECTED_MODE mode = getSelectedMode();

        if (completeCode == COMPLETE_OP_CODE.NO_ERROR) {
            errorMessage = "Everything is fine";
        } else if (completeCode == COMPLETE_OP_CODE.NO_OPTION_SELECTED) {
            errorMessage = "No group creation mode selected";
        } else if (completeCode == COMPLETE_OP_CODE.NO_INPUT_NUMBER) {
            errorMessage = "No number has been entered. Please enter a valid number";
        } else if (completeCode == COMPLETE_OP_CODE.INPUT_NUMBER_NEGATIVE) {
            errorMessage = "You cannot enter a negative number. Enter a number greater than 0";
        } else if (completeCode == COMPLETE_OP_CODE.INPUT_NUMBER_ZERO) {
            if (mode == SELECTED_MODE.STUDENTS_PER_GROUP) {
                errorMessage = "You cannot create groups of 0 students. Enter a number greater than 0";
            } else if (mode == SELECTED_MODE.NUMBER_OF_GROUPS) {
                errorMessage = "You have to create at least one group";
            }
        } else if (completeCode == COMPLETE_OP_CODE.ZERO_GROUPS) {
            errorMessage = "The desired number of students per group exceeds the number of students in the course. Choose a smaller number";
        } else if (completeCode == COMPLETE_OP_CODE.MORE_GROUPS_THAN_STUDENTS) {
            errorMessage = "You cannot create more groups than there are students in the course";
        } else {
            errorMessage = "Unknown error";
        }

        return errorMessage;
    }

    private void displayError(COMPLETE_OP_CODE completeCode) {
        String errorMessage = getErrorMessage(completeCode);
        if (errorMessage != null) {
            separator.setVisibility(View.VISIBLE);
            errorMessageView.setText(errorMessage);
            errorMessageView.setVisibility(View.VISIBLE);
        }
    }

    private SELECTED_MODE getSelectedMode() {
        SELECTED_MODE mode;
        if (checkedRadioButtonId == R.id.radio_button_1) {
            mode = SELECTED_MODE.STUDENTS_PER_GROUP;
        } else if (checkedRadioButtonId == R.id.radio_button_2) {
            mode = SELECTED_MODE.NUMBER_OF_GROUPS;
        } else {
            mode = SELECTED_MODE.NO_MODE_SELECTED;
        }
        return mode;
    }


}
