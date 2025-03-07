package com.cb.SmartGroups.utils.dialogs.studentdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.listviews.SelectParticipantsListAdapter;
import com.cb.SmartGroups.utils.customdatamodels.SelectParticipantItem;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.PetitionRequest;
import com.cb.SmartGroups.utils.firesoredatamodels.PetitionUser;
import com.cb.SmartGroups.utils.restmodel.Subject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RequestGroupDialog extends DialogFragment {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private Context context;

    private ListView participantsListView;


    private ArrayList<SelectParticipantItem> participantsList;
    private SelectParticipantsListAdapter participantsAdapter;

    private final String selectedCourse;
    private final String selectedSubject;

    public RequestGroupDialog(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        participantsList = new ArrayList<SelectParticipantItem>();
        participantsAdapter = new SelectParticipantsListAdapter(getContext(), participantsList);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_dialogs_requestgroupdialog, null);

        // List of participants
        participantsListView = view.findViewById(R.id.participantsListView);
        participantsListView.setAdapter(participantsAdapter);

        populateParticipants();

        builder.setView(view).setTitle("Request to create a group")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Just closes the dialog
                })
                .setPositiveButton("Create", (dialogInterface, i) -> {

                    ArrayList<PetitionUser> petitionUsersList = new ArrayList<PetitionUser>();
                    ArrayList<String> petitionUsersIds = new ArrayList<String>();

                    for (SelectParticipantItem item : participantsList) {
                        if (item.isSelected()) {
                            String participantId = item.getParticipantId();
                            petitionUsersList.add(new PetitionUser(participantId, item.getParticipantName(), PetitionUser.STATUS_PENDING));
                            petitionUsersIds.add(participantId);
                        }
                    }

                    if (petitionUsersList.size() == 0) {
                        Toast.makeText(context, "You must add at least one other member to the group", Toast.LENGTH_SHORT).show();
                    } else {

                        fStore
                                .collection("Students")
                                .document(fAuth.getUid())
                                .get()
                                .addOnSuccessListener(requesterDocument -> {
                                    String requesterID = requesterDocument.getId();
                                    String requesterName = (String) requesterDocument.get("FullName");

                                    petitionUsersList.add(new PetitionUser(
                                            requesterID,
                                            requesterName,
                                            PetitionUser.STATUS_ACCEPTED)
                                    );
                                    petitionUsersIds.add(requesterID);

                                    DocumentReference subjectDocRef = fStore
                                            .collection("CoursesOrganization")
                                            .document(selectedCourse)
                                            .collection("Subjects")
                                            .document(selectedSubject);

                                    CollectionReference petitionsCollRef = subjectDocRef
                                            .collection("Petitions");

                                    subjectDocRef // Check if the group that we are trying to make already exists
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                Subject subject = documentSnapshot.toObject(Subject.class);
                                                String teacherID = subject.getTeacherID();

                                                ArrayList<String> allParticipantsIDs = new ArrayList<String>(petitionUsersIds);
                                                allParticipantsIDs.add(teacherID);

                                                subjectDocRef
                                                        .collection("CollectiveGroups")
                                                        .get()
                                                        .addOnSuccessListener(groupDocuments -> {

                                                            boolean groupExists = false;

                                                            for (DocumentSnapshot groupDoc : groupDocuments) {
                                                                CollectiveGroupDocument collectiveGroupDocument = groupDoc.toObject(CollectiveGroupDocument.class);
                                                                if (collectiveGroupDocument.getAllParticipantsIDs().containsAll(allParticipantsIDs) && collectiveGroupDocument.getAllParticipantsIDs().size() == allParticipantsIDs.size()) {
                                                                    groupExists = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (groupExists) {
                                                                Toast.makeText(context, "You are already in a group just like the one you are trying to apply to create with this petition", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                petitionsCollRef
                                                                        .get()
                                                                        .addOnSuccessListener(petitionDocuments -> {

                                                                            int numOfPetitions = 0;
                                                                            boolean petitionExists = false;

                                                                            for (DocumentSnapshot petitionDoc : petitionDocuments) {
                                                                                PetitionRequest petitionRequest = petitionDoc.toObject(PetitionRequest.class);
                                                                                if (petitionRequest.getPetitionUsersIds().containsAll(petitionUsersIds)) {
                                                                                    petitionExists = true;
                                                                                    break;
                                                                                } else if (petitionRequest.getRequesterId().equals(fAuth.getUid())) {
                                                                                    numOfPetitions = numOfPetitions + 1;
                                                                                }
                                                                            }
                                                                            if (petitionExists) {
                                                                                Toast.makeText(context, "You have already created a petition like this one", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                if (numOfPetitions >= 3) {
                                                                                    Toast.makeText(context, "You may not make more than three group creation requests. " +
                                                                                            "Wait for the teacher to accept or reject the requests you already have", Toast.LENGTH_LONG).show();
                                                                                } else {

                                                                                    PetitionRequest newPetition = new PetitionRequest(
                                                                                            requesterID,
                                                                                            requesterName,
                                                                                            teacherID,
                                                                                            petitionUsersIds,
                                                                                            petitionUsersList
                                                                                    );

                                                                                    petitionsCollRef.add(newPetition);

                                                                                }
                                                                            }
                                                                        });
                                                            }

                                                        });

                                            });

                                });

                    }
                });

        return builder.create();
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
                                        participantsList.add(new SelectParticipantItem((String) document.get("FullName"), document.getId()));
                                    }
                                }

                                Collections.sort(participantsList, new Comparator<SelectParticipantItem>() {
                                    @Override
                                    public int compare(SelectParticipantItem selectParticipantItem1, SelectParticipantItem selectParticipantItem2) {
                                        String participantName1 = selectParticipantItem1.getParticipantName();
                                        String participantName2 = selectParticipantItem2.getParticipantName();

                                        return extractInt(participantName1) - extractInt(participantName2);
                                    }

                                    int extractInt(String s) {
                                        String num = s.replaceAll("\\D", "");
                                        return num.isEmpty() ? 0 : Integer.parseInt(num);
                                    }

                                });

                                participantsAdapter.notifyDataSetChanged();
                            });

                });
    }

}
