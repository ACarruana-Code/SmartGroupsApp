package com.cb.SmartGroups.fragments.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.recyclerviews.files.student.GroupContainerCardListAdapter;

import com.cb.SmartGroups.utils.cards.files.FileCard;
import com.cb.SmartGroups.utils.cards.files.FilesContainerCard;
import com.cb.SmartGroups.utils.cards.files.student.GroupContainerCard;

import com.cb.SmartGroups.utils.firesoredatamodels.Group;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;

import com.cb.SmartGroups.utils.firesoredatamodels.StorageFile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Files extends Fragment {

    // Firestore
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    private String selectedCourse;
    private String selectedSubject;

    ArrayList<GroupContainerCard> groupsList;
    GroupContainerCardListAdapter groupsContainerAdapter;

    TextView noFiles;

    public Files(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        groupsList = new ArrayList<GroupContainerCard>();
        groupsContainerAdapter = new GroupContainerCardListAdapter(groupsList, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_files_groupalfiles, container, false);

        noFiles = view.findViewById(R.id.noFiles);
        RecyclerView filesContainer = view.findViewById(R.id.groupalFilesContainer);

        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(getContext());

        filesContainer.setAdapter(groupsContainerAdapter);
        filesContainer.setLayoutManager(coursesLayoutManager);

        CollectionReference collectionRef = fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups");

        collectionRef
                .whereArrayContains("allParticipantsIDs", fAuth.getUid())
                .addSnapshotListener((collectiveGroupsDocumentsSnapshots, error1) -> {

                    if (error1 != null) {
                        return;
                    } else if (collectiveGroupsDocumentsSnapshots == null) {
                        return;
                    }

                    groupsList.clear();
                    for (DocumentSnapshot collectiveGroupDocumentSnapshot : collectiveGroupsDocumentsSnapshots) {
                        CollectiveGroupDocument collectiveGroupDocument = collectiveGroupDocumentSnapshot.toObject(CollectiveGroupDocument.class);

                        String collectiveGroupName = collectiveGroupDocument.getName();
                        ArrayList<FilesContainerCard> filesContainerList = new ArrayList<FilesContainerCard>();

                        for (Group group : collectiveGroupDocument.getGroups()) {
                            ArrayList<FileCard> filesList = new ArrayList<FileCard>();
                            String groupName = group.getName();
                            String storageCollection;

                            if (group.getHasTeacher()) {
                                storageCollection = "StorageWithTeacher";
                            } else {
                                storageCollection = "StorageWithoutTeacher";
                            }

                            collectionRef
                                    .document(collectiveGroupDocumentSnapshot.getId())
                                    .collection(storageCollection)
                                    .addSnapshotListener((storageFileDocumentSnapshots, error2) -> {

                                        if (error2 != null) {
                                            return;
                                        } else if (storageFileDocumentSnapshots == null) {
                                            return;
                                        }

                                        // TODO: When the teacher deletes the group, the files dont dissapear automatically
                                        for (DocumentSnapshot storageFileDoc : storageFileDocumentSnapshots) {
                                            StorageFile storageFile = storageFileDoc.toObject(StorageFile.class);
                                            FileCard fileCard = new FileCard(
                                                    storageFile.getFileName(),
                                                    storageFile.getUploaderName(),
                                                    storageFile.getUploadedDate(),
                                                    storageFile.getDownloadLink());
                                            filesList.add(fileCard);
                                        }

                                        if (!filesList.isEmpty()) {
                                            if (filesContainerList.isEmpty()) {
                                                groupsList.add(new GroupContainerCard("Archivos del " + collectiveGroupName, filesContainerList));
                                            }
                                            filesContainerList.add(new FilesContainerCard(groupName, filesList));
                                            listChanged();
                                        }

                                    });
                        }
                    }
                });

        return view;
    }

    private void listChanged() {
        groupsContainerAdapter.notifyDataSetChanged();
        if (groupsList.isEmpty()) {
            noFiles.setVisibility(View.VISIBLE);
        } else {
            noFiles.setVisibility(View.GONE);
        }
    }

}
