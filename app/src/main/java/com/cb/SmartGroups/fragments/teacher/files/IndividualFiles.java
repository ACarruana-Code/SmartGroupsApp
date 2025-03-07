package com.cb.SmartGroups.fragments.teacher.files;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.recyclerviews.files.FilesContainerCardAdapter;
import com.cb.SmartGroups.utils.cards.files.FileCard;
import com.cb.SmartGroups.utils.cards.files.FilesContainerCard;
import com.cb.SmartGroups.utils.firesoredatamodels.StorageFile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class IndividualFiles extends Fragment {

    // Firestore
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    private String selectedCourse;
    private String selectedSubject;

    ArrayList<FilesContainerCard> groupsList;
    FilesContainerCardAdapter filesContainersAdapter;

    TextView noFiles;

    public IndividualFiles(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        groupsList = new ArrayList<FilesContainerCard>();
        filesContainersAdapter = new FilesContainerCardAdapter(groupsList, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_files_groupalfiles, container, false);

        noFiles = view.findViewById(R.id.noFiles);
        noFiles.setText(R.string.noIndividualFiles);
        RecyclerView filesContainer = view.findViewById(R.id.groupalFilesContainer);

        LinearLayoutManager coursesLayoutManager = new LinearLayoutManager(getContext());

        filesContainer.setAdapter(filesContainersAdapter);
        filesContainer.setLayoutManager(coursesLayoutManager);

        fStore.collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("IndividualGroups")
                .whereArrayContains("allParticipantsIDs", fAuth.getUid())
                .addSnapshotListener((queryDocumentsSnapshots, error) -> {

                    if (error != null) {
                        return;
                    } else if (queryDocumentsSnapshots == null) {
                        return;
                    }
                    groupsList.clear();
                    for (DocumentSnapshot document : queryDocumentsSnapshots){
                        String name = (String) document.get("name");
                        document
                                .getReference()
                                .collection("StorageWithTeacher")
                                .addSnapshotListener((queryDocumentsSnapshots1, error1) -> {

                            if (error1 != null) {
                                return;
                            } else if (queryDocumentsSnapshots1 == null) {
                                return;
                            }

                            if (!queryDocumentsSnapshots1.isEmpty()) {
                                ArrayList<FileCard> filesList = new ArrayList<FileCard>();

                                for (DocumentSnapshot doc : queryDocumentsSnapshots1) {
                                    StorageFile storageRef = doc.toObject(StorageFile.class);

                                    filesList.add(new FileCard(
                                            storageRef.getFileName(),
                                            storageRef.getUploaderName(),
                                            storageRef.getUploadedDate(),
                                            storageRef.getDownloadLink()
                                    ));
                                }
                                groupsList.add(new FilesContainerCard(name, filesList));
                                listChanged();
                            }
                        });
                    }
                });

        return view;
    }

    private void listChanged() {
        filesContainersAdapter.notifyDataSetChanged();

        if (groupsList.isEmpty()) {
            noFiles.setVisibility(View.VISIBLE);
        } else {
            noFiles.setVisibility(View.GONE);
        }
    }

}
