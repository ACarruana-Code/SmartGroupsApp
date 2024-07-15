package com.cb.SmartGroups.utils.dialogs.teacherdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.listviews.SelectParticipantsWithSpokerAdapter;
import com.cb.SmartGroups.utils.customdatamodels.SelectParticipantItemWithSpoker;
import com.cb.SmartGroups.utils.firesoredatamodels.Group;
import com.cb.SmartGroups.utils.restmodel.Subject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CreateGroupDialog extends DialogFragment {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private Context context;

    private ListView participantsListView;

    private ArrayList<SelectParticipantItemWithSpoker> participantsList;
    private SelectParticipantsWithSpokerAdapter participantsAdapter;

    private final String selectedCourse;
    private final String selectedSubject;

    private CheckBox selectAll;

    public CreateGroupDialog(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        participantsList = new ArrayList<SelectParticipantItemWithSpoker>();
        participantsAdapter = new SelectParticipantsWithSpokerAdapter(getContext(), participantsList);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_dialogs_creategroupdialog, null);
        selectAll = view.findViewById(R.id.selectAll);

        selectAll.setOnClickListener(view12 -> {
            boolean selected = selectAll.isChecked();
            if (!selected) {
                for (SelectParticipantItemWithSpoker participant : participantsList) {
                    participant.setSelected(false);
                }
            } else {
                for (SelectParticipantItemWithSpoker participant : participantsList) {
                    participant.setSelected(true);
                }
            }
            participantsAdapter.notifyDataSetChanged();
        });

        // List of participants
        participantsListView = view.findViewById(R.id.participantsList);
        participantsListView.setAdapter(participantsAdapter);

        populateParticipants();

        builder.setView(view).setTitle("Create a single group")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Just closes the dialog
                })
                .setPositiveButton("Create", null);


        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(view1 -> {

                ArrayList<String> studentsIDs = participantsAdapter.getParticipantsIDs();

                if (participantsAdapter.getNumberSelected() > 1 && participantsAdapter.getSpokerID() == null) {
                    Toast.makeText(context, "Select the team spokesperson", Toast.LENGTH_SHORT).show();
                } else {
                    DocumentReference subjectRef = fStore
                            .collection("CoursesOrganization")
                            .document(selectedCourse)
                            .collection("Subjects")
                            .document(selectedSubject);

                    CollectionReference collectionRef;

                    if (studentsIDs.size() == 1) {
                        collectionRef = subjectRef.collection("IndividualGroups");
                    } else {
                        collectionRef = subjectRef.collection("CollectiveGroups");
                    }

                    collectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                        int maxIdentifier = Group.getMaxGroupIdentifier(queryDocumentSnapshots);
                        Group.createGroup(collectionRef, selectedCourse, selectedSubject, studentsIDs, maxIdentifier + 1, context, null, participantsAdapter.getSpokerID(), null);
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
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Subject subject = documentSnapshot.toObject(Subject.class);
                    ArrayList<String> studentsIDs = subject.getStudentIDs();
                    studentsIDs.remove(fAuth.getUid());

                    fStore
                            .collection("Students")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    if (studentsIDs.contains(document.getId())) {
                                        participantsList.add(new SelectParticipantItemWithSpoker((String) document.get("FullName"), document.getId()));
                                    }
                                }

                                participantsAdapter.notifyDataSetChanged();
                            });

                });
    }

}
