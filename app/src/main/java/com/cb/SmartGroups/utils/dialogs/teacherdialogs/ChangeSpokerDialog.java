package com.cb.SmartGroups.utils.dialogs.teacherdialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ChangeSpokerDialog extends DialogFragment {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private Context context;

    private RadioGroup participantsContainer;

    private final String selectedCourse;
    private final String selectedSubject;

    private String groupID;

    private HashMap<Integer, String> studentsIDsMap;
    private HashMap<Integer, String> studentsNamesMap;

    private String spokerID;

    public ChangeSpokerDialog(String selectedCourse, String selectedSubject, String groupID, String spokerID) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
        this.groupID = groupID;
        this.spokerID = spokerID;
        this.studentsIDsMap = new HashMap<Integer, String>();
        this.studentsNamesMap = new HashMap<Integer, String>();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_dialogs_changespokerdialog, null);

        // List of participants
        participantsContainer = view.findViewById(R.id.participantsContainer);
        populateParticipants();

        builder.setView(view).setTitle("Change spokesperson")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Just closes the dialog
                })
                .setPositiveButton("Change", null);


        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(view1 -> {
                int checkedButtonID = participantsContainer.getCheckedRadioButtonId();
                if (checkedButtonID == -1) {
                    Toast.makeText(context, "Select a new spokesperson", Toast.LENGTH_LONG).show();
                } else {
                    String newSpokerID = studentsIDsMap.get(checkedButtonID);
                    String newSpokerName = studentsNamesMap.get(checkedButtonID);

                    DocumentReference grouDocRef = fStore
                            .collection("CoursesOrganization")
                            .document(selectedCourse)
                            .collection("Subjects")
                            .document(selectedSubject)
                            .collection("CollectiveGroups")
                            .document(groupID);

                    grouDocRef
                            .update("spokerID", newSpokerID)
                            .addOnSuccessListener(unused -> {
                                grouDocRef.update("spokerName", newSpokerName);
                            });

                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }

    private void populateParticipants() {
        fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups")
                .document(groupID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    CollectiveGroupDocument groupDocument = documentSnapshot.toObject(CollectiveGroupDocument.class);
                    ArrayList<String> studentsIDs = groupDocument.getAllParticipantsIDs();
                    studentsIDs.remove(fAuth.getUid());
                    studentsIDs.remove(spokerID);

                    fStore
                            .collection("Students")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                int buttonID = 0;

                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    if (studentsIDs.contains(document.getId())) {
                                        String studentName = (String) document.get("FullName");
                                        studentsIDsMap.put(buttonID, document.getId());
                                        studentsNamesMap.put(buttonID, studentName);

                                        RadioButton button = new RadioButton(participantsContainer.getContext());
                                        button.setText(studentName);

                                        int textColor = Color.parseColor("#1976d2");
                                        button.setButtonTintList(ColorStateList.valueOf(textColor));

                                        button.setId(buttonID);
                                        participantsContainer.addView(button);
                                        buttonID++;
                                    }
                                }

                            });

                });
    }

}
