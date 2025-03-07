package com.cb.SmartGroups.utils.dialogs.teacherdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.listviews.SelectParticipantsListAdapter;
import com.cb.SmartGroups.utils.customdatamodels.SelectParticipantItem;
import com.cb.SmartGroups.utils.firesoredatamodels.Group;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.GroupParticipant;
import com.cb.SmartGroups.utils.restmodel.Subject;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class AdministrateGroupsDialog extends DialogFragment {

    private TextView textView1, textView2, textView3;

    private View separator;
    private TextView errorMessage;

    private Spinner groupSpinner1, groupSpinner2;

    private MaterialButton interchangeButton;

    private ListView participantsGroup1ListView, participantsGroup2ListView;
    private ArrayList<SelectParticipantItem> participantsGroup1List, participantsGroup2List;
    private SelectParticipantsListAdapter group1ParticipantsAdapter, group2ParticipantsAdapter;

    private String selectedCourse;
    private String selectedSubject;

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    private Context context;

    // Reference to CollectiveGroups collection
    CollectionReference groupCollRef;

    // Reference to the current subject
    DocumentReference subjectDocRef;

    public AdministrateGroupsDialog(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        participantsGroup1List = new ArrayList<SelectParticipantItem>();
        participantsGroup2List = new ArrayList<SelectParticipantItem>();


    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        this.context = context;

        group1ParticipantsAdapter = new SelectParticipantsListAdapter(context, participantsGroup1List);
        group2ParticipantsAdapter = new SelectParticipantsListAdapter(context, participantsGroup2List);
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.utils_dialogs_administrategroupsdialog, null);

        textView1 = view.findViewById(R.id.textView1);
        textView2 = view.findViewById(R.id.textView2);
        textView3 = view.findViewById(R.id.textView3);

        groupSpinner1 = view.findViewById(R.id.groupSpinner1);
        groupSpinner2 = view.findViewById(R.id.groupSpinner2);

        separator = view.findViewById(R.id.separator);
        errorMessage = view.findViewById(R.id.errorMessage);

        participantsGroup1ListView = view.findViewById(R.id.participantsGroup1List);
        participantsGroup2ListView = view.findViewById(R.id.participantsGroup2List);

        participantsGroup1ListView.setAdapter(group1ParticipantsAdapter);
        participantsGroup2ListView.setAdapter(group2ParticipantsAdapter);

        interchangeButton = view.findViewById(R.id.interchangeButton);

        groupCollRef = fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups");

        subjectDocRef = fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject);

        ArrayList<String> groupSpinner1Names = new ArrayList<String>();
        ArrayList<String> groupSpinner2Names = new ArrayList<String>();

        HashMap<String, String> group1SpinnerIDs = new HashMap<String, String>();
        HashMap<String, String> group2SpinnerIDs = new HashMap<String, String>();

        // Group 1 adapter
        ArrayAdapter<String> groups1ListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, groupSpinner1Names);
        groups1ListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner1.setAdapter(groups1ListAdapter);

        // Group 2 spinner
        ArrayAdapter<String> groups2ListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, groupSpinner2Names);
        groups2ListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner2.setAdapter(groups2ListAdapter);

        // Groups names
        groupCollRef
                .whereArrayContains("allParticipantsIDs", fAuth.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CollectiveGroupDocument group = document.toObject(CollectiveGroupDocument.class);
                        groupSpinner1Names.add(group.getName());
                        groupSpinner2Names.add(group.getName());

                        group1SpinnerIDs.put(group.getName(), document.getId());
                        group2SpinnerIDs.put(group.getName(), document.getId());
                    }
                    groupSpinner1.setSelection(0);
                    groupSpinner2.setSelection(1);
                    groups1ListAdapter.notifyDataSetChanged();
                    groups2ListAdapter.notifyDataSetChanged();
                });

        // Group 1 spinner listener
        groupSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGroupName = parent.getItemAtPosition(position).toString();

                if (selectedGroupName.equals((String) groupSpinner2.getSelectedItem())) {
                    showError();
                } else {
                    dismissError();
                }

                groupCollRef
                        .document(group1SpinnerIDs.get(groupSpinner1.getSelectedItem()))
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            updateListView(documentSnapshot, participantsGroup1List, group1ParticipantsAdapter);
                        });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Group 2 spinner listener
        groupSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGroupName = parent.getItemAtPosition(position).toString();

                if (selectedGroupName.equals((String) groupSpinner1.getSelectedItem())) {
                    showError();
                } else {
                    dismissError();
                }

                groupCollRef
                        .document(group2SpinnerIDs.get(groupSpinner2.getSelectedItem()))
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            updateListView(documentSnapshot, participantsGroup2List, group2ParticipantsAdapter);
                        });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        interchangeButton.setOnClickListener(v -> {

            ArrayList<SelectParticipantItem> updatedList1 = getUpdatedList(participantsGroup1List, participantsGroup2List);
            ArrayList<SelectParticipantItem> updatedList2 = getUpdatedList(participantsGroup2List, participantsGroup1List);

            if (updatedList1.size() < 2 || updatedList2.size() < 2) { // TODO: Check for more errors
                Toast.makeText(context, "Groups must have at least 2 people", Toast.LENGTH_LONG).show();
            } else {

                boolean duplicateStudentList1 = duplicateStudent(updatedList1);
                boolean duplicateStudentList2 = duplicateStudent(updatedList2);

                if (duplicateStudentList1 || duplicateStudentList2) {
                    Toast.makeText(context, "You cannot include a student in a group in which he/she is already included", Toast.LENGTH_LONG).show();
                } else {
                    participantsGroup1List.clear();
                    participantsGroup1List.addAll(updatedList1);

                    participantsGroup2List.clear();
                    participantsGroup2List.addAll(updatedList2);

                    group1ParticipantsAdapter.notifyDataSetChanged();
                    group2ParticipantsAdapter.notifyDataSetChanged();
                }

            }
        });

        builder.setTitle("Group Administrator")
                .setView(view)
                .setNegativeButton("Cancel", (dialog, i) -> {
                    // Just closes the dialog
                })
                .setPositiveButton("Ok", (dialog, i) -> {
                    String group1Name = (String) groupSpinner1.getSelectedItem();
                    String group2Name = (String) groupSpinner2.getSelectedItem();

                    String group1ID = group1SpinnerIDs.get(group1Name);
                    String group2ID = group2SpinnerIDs.get(group2Name);

                    // Update first group selected
                    groupCollRef
                            .document(group1ID)
                            .get()
                            .addOnSuccessListener(group1DocumentSnapshot -> {
                                updateGroup(group1DocumentSnapshot, participantsGroup1List);
                            });

                    // Update second group selected
                    groupCollRef
                            .document(group2ID)
                            .get()
                            .addOnSuccessListener(group2DocumentSnapshot -> {
                                updateGroup(group2DocumentSnapshot, participantsGroup2List);
                            });
                });

        return builder.create();
    }

    private void updateGroup(DocumentSnapshot groupDocumentSnapshot, ArrayList<SelectParticipantItem> participantsGroupList) {

        subjectDocRef
                .get()
                .addOnSuccessListener(subjectDocument -> {
                    Subject subject = subjectDocument.toObject(Subject.class);
                    String teacherID = subject.getTeacherID();

                    fStore
                            .collection("Teachers")
                            .document(teacherID)
                            .get()
                            .addOnSuccessListener(teacherDataDocument -> {
                                String teacherName = (String) teacherDataDocument.get("FullName");

                                // Update groups
                                CollectiveGroupDocument collectiveGroupDocument = groupDocumentSnapshot.toObject(CollectiveGroupDocument.class);
                                ArrayList<Group> updatedGroups = new ArrayList<Group>();

                                for (Group group : collectiveGroupDocument.getGroups()) {
                                    Group updatedGroup = new Group();

                                    updatedGroup.setName(group.getName());
                                    updatedGroup.setCourseName(group.getCourseName());
                                    updatedGroup.setSubjectName(group.getSubjectName());
                                    updatedGroup.setHasTeacher(group.getHasTeacher());

                                    ArrayList<String> participantsIDs = new ArrayList<String>();
                                    ArrayList<GroupParticipant> participants = new ArrayList<GroupParticipant>();

                                    for (SelectParticipantItem item : participantsGroupList) {
                                        participantsIDs.add(item.getParticipantId());
                                        participants.add(new GroupParticipant(item.getParticipantName(), item.getParticipantId()));
                                    }

                                    updatedGroup.setParticipantsIds(participantsIDs);
                                    updatedGroup.setParticipants(participants);
                                    updatedGroup.setCollectionId(group.getCollectionId());

                                    updatedGroups.add(updatedGroup);
                                }

                                ArrayList<String> updatedAllParticipantsIDs = null;

                                for (Group group : updatedGroups) {
                                    if (group.getHasTeacher()) {
                                        group.getParticipants().add(new GroupParticipant(teacherName, teacherID));
                                        group.getParticipantsIds().add(teacherID);
                                        updatedAllParticipantsIDs = group.getParticipantsIds();
                                    }
                                }

                                String spokerID = getSpokerID(participantsGroupList);

                                if (spokerID != null && updatedAllParticipantsIDs != null) {
                                    ArrayList<String> newAllParticipantsIDs = new ArrayList<String>(updatedAllParticipantsIDs);
                                    fStore
                                            .collection("Students")
                                            .document(spokerID)
                                            .get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                String spokerName = (String) documentSnapshot.get("FullName");

                                                groupDocumentSnapshot.getReference().update("groups", updatedGroups);
                                                groupDocumentSnapshot.getReference().update("allParticipantsIDs", newAllParticipantsIDs);
                                                groupDocumentSnapshot.getReference().update("spokerID", spokerID);
                                                groupDocumentSnapshot.getReference().update("spokerName", spokerName);

                                            });
                                }
                            });
                });

    }

    private void updateListView(DocumentSnapshot documentSnapshot, ArrayList<SelectParticipantItem> participantsGroupList, SelectParticipantsListAdapter groupParticipantsAdapter) {
        participantsGroupList.clear();
        CollectiveGroupDocument collectiveGroupDocument = documentSnapshot.toObject(CollectiveGroupDocument.class);
        ArrayList<Group> groupsList = collectiveGroupDocument.getGroups();
        String spokerID = collectiveGroupDocument.getSpokerID();

        for (Group group : groupsList) {
            if (!group.getHasTeacher()) {
                ArrayList<GroupParticipant> participants = group.getParticipants();

                for (GroupParticipant participant : participants) {
                    SelectParticipantItem participantItem = new SelectParticipantItem(participant.getParticipantFullName(), participant.getParticipantId());
                    if (participant.getParticipantId().equals(spokerID)) {
                        participantItem.setSpoker(true);
                    }
                    participantsGroupList.add(participantItem);
                }

                Collections.sort(participantsGroupList, new Comparator<SelectParticipantItem>() {
                    @Override
                    public int compare(SelectParticipantItem item1, SelectParticipantItem item2) {
                        String name1 = item1.getParticipantName();
                        String name2 = item2.getParticipantName();

                        return extractInt(name1) - extractInt(name2);
                    }

                    int extractInt(String s) {
                        String num = s.replaceAll("\\D", "");
                        return num.isEmpty() ? 0 : Integer.parseInt(num);
                    }

                });

                groupParticipantsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void dismissError() {
        separator.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);
        textView3.setVisibility(View.VISIBLE);
        participantsGroup1ListView.setVisibility(View.VISIBLE);
        interchangeButton.setVisibility(View.VISIBLE);
        participantsGroup2ListView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        separator.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
        textView3.setVisibility(View.GONE);
        participantsGroup1ListView.setVisibility(View.GONE);
        interchangeButton.setVisibility(View.GONE);
        participantsGroup2ListView.setVisibility(View.GONE);
    }

    private String getSpokerID(ArrayList<SelectParticipantItem> participantsList) {
        String spokerID = null;

        for (SelectParticipantItem item : participantsList) {
            if (item.isSpoker()) {
                spokerID = item.getParticipantId();
                break;
            }
        }

        return spokerID;
    }

    private ArrayList<SelectParticipantItem> getUpdatedList(ArrayList<SelectParticipantItem> list1, ArrayList<SelectParticipantItem> list2) {
        ArrayList<SelectParticipantItem> updatedList = new ArrayList<SelectParticipantItem>();

        String spokerID = null;
        for (SelectParticipantItem item : list1) {
            if (item.isSpoker()) {
                spokerID = item.getParticipantId();
            }
        }

        if (spokerID != null) { // Can't be null because there's always a spoker, technically

            for (SelectParticipantItem selectedParticipantList1 : list1) { // Add non selected items from own list
                if (!selectedParticipantList1.isSelected()) {
                    SelectParticipantItem newItem = new SelectParticipantItem(selectedParticipantList1.getParticipantName(), selectedParticipantList1.getParticipantId());
                    updatedList.add(newItem);
                }
            }

            for (SelectParticipantItem selectedParticipantList2 : list2) { // Add selected items from external list
                if (selectedParticipantList2.isSelected()) {
                    SelectParticipantItem newItem = new SelectParticipantItem(selectedParticipantList2.getParticipantName(), selectedParticipantList2.getParticipantId());
                    updatedList.add(newItem);
                }
            }

            SelectParticipantItem oldSpoker = null;

            for (SelectParticipantItem item : updatedList) {
                if (item.getParticipantId().equals(spokerID)) {
                    oldSpoker = item;
                    break;
                }
            }

            if (oldSpoker != null) {
                oldSpoker.setSpoker(true);
            } else {
                int randomNum = ThreadLocalRandom.current().nextInt(updatedList.size());
                updatedList.get(randomNum).setSpoker(true);
            }

        }

        return updatedList;
    }

    private boolean duplicateStudent(ArrayList<SelectParticipantItem> updatedList) {
        boolean duplicated = false;

        ArrayList<String> updatedStudentsIDs = new ArrayList<String>();
        HashMap<String, Integer> counterMap = new HashMap<String, Integer>();

        for (SelectParticipantItem item : updatedList) {
            String participantId = item.getParticipantId();

            updatedStudentsIDs.add(participantId);
            counterMap.put(participantId, 0);
        }

        for (String studentID : updatedStudentsIDs) {
            Integer counter = counterMap.get(studentID);
            if (counter != null) {
                counter++;
                counterMap.put(studentID, counter);
            }
        }

        for (String entry : counterMap.keySet()) {
            Integer totalNumber = counterMap.get(entry);
            if (totalNumber != null) {
                if (totalNumber > 1) {
                    duplicated = true;
                    break;
                }
            }
        }

        return duplicated;
    }

}
