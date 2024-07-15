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
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.Group;
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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NextStepPattern extends DialogFragment {

    private String selectedCourse;
    private String selectedSubject;

    private Context context;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    public NextStepPattern(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.utils_dialogs_nextsteppattern, null);

        builder.setTitle("Group Administrator")
                .setView(view)
                .setNegativeButton("Cancel", (dialog, i) -> {
                    AdministrateGroupsDialog admin = new AdministrateGroupsDialog(selectedCourse, selectedSubject);
                    admin.show(getParentFragmentManager(), "dialog");
                })
                .setPositiveButton("Ok", (dialog, i) -> {
                    CollectionReference groupCollRef = fStore
                            .collection("CoursesOrganization")
                            .document(selectedCourse)
                            .collection("Subjects")
                            .document(selectedSubject)
                            .collection("CollectiveGroups");

                    groupCollRef
                            .whereArrayContains("allParticipantsIDs", fAuth.getUid())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    deleteDocuments(document.getReference());
                                }

                                DocumentReference subjectRef = fStore
                                        .collection("CoursesOrganization")
                                        .document(selectedCourse)
                                        .collection("Subjects")
                                        .document(selectedSubject);

                                subjectRef.get()
                                        .addOnSuccessListener(documentSnapshot -> {

                                            Subject subject = documentSnapshot.toObject(Subject.class);
                                            ArrayList<String> studentIDs = subject.getStudentIDs();

                                            int studentsPerGroup = 4;
                                            int numGroups = studentIDs.size() / studentsPerGroup;
                                            int remainder = studentIDs.size() % studentsPerGroup;

                                            createGroupsBatchPattern(subjectRef, studentsPerGroup, numGroups, remainder, studentIDs, "Jigsaw");
                                        });
                            });
                });

        return builder.create();
    }

    private void createGroupsBatchPattern(DocumentReference subjectRef, int studentsPerGroup, int numGroups, int remainder, ArrayList<String> studentIDs, String pattern) {
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

            boolean isChecked = true;

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

    private void deleteDocuments(DocumentReference groupRef) {
        groupRef.collection("ChatRoomWithTeacher").get().addOnSuccessListener(queryDocumentSnapshots1 -> {
            for (DocumentSnapshot document : queryDocumentSnapshots1) {
                document.getReference().delete();
            }

            groupRef.collection("StorageWithTeacher").get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                for (DocumentSnapshot document : queryDocumentSnapshots2) {
                    document.getReference().delete();
                }
                groupRef.collection("ChatRoomWithoutTeacher").get().addOnSuccessListener(queryDocumentSnapshots3 -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots3) {
                        document.getReference().delete();
                    }

                    groupRef.collection("StorageWithoutTeacher").get().addOnSuccessListener(queryDocumentSnapshots4 -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots4) {
                            document.getReference().delete();
                        }
                        groupRef.collection("InteractivityCards").get().addOnSuccessListener(queryDocumentSnapshots5 -> {
                            for (DocumentSnapshot document : queryDocumentSnapshots5) {
                                document.getReference().delete();
                            }
                            groupRef.delete();
                        });
                    });
                });
            });
        });
    }
}
