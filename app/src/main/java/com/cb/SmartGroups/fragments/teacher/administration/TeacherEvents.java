package com.cb.SmartGroups.fragments.teacher.administration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.recyclerviews.EventsContainerAdapter;
import com.cb.SmartGroups.utils.cards.EventCard;
import com.cb.SmartGroups.utils.cards.EventContainerCard;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.EventCardDocument;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class TeacherEvents extends Fragment {

    String selectedCourse;
    String selectedSubject;

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    private ArrayList<EventContainerCard> eventContainerList;
    private EventsContainerAdapter adapter;

    private HashMap<String, ArrayList<EventCard>> eventContainerMap;
    HashMap<String, Boolean> hasDocumentsMap;

    private TextView warningMessage;

    public TeacherEvents(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;


        eventContainerList = new ArrayList<EventContainerCard>();
        adapter = new EventsContainerAdapter(eventContainerList);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        eventContainerMap = new HashMap<String, ArrayList<EventCard>>();
        hasDocumentsMap = new HashMap<String, Boolean>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_events, container, false);


        RecyclerView eventsContainer = view.findViewById(R.id.eventsContainer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        eventsContainer.setAdapter(adapter);
        eventsContainer.setLayoutManager(layoutManager);

        warningMessage = view.findViewById(R.id.warningMessage);

        fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CollectiveGroupDocument collectiveGroupDocument = documentSnapshot.toObject(CollectiveGroupDocument.class);
                        String groupName = collectiveGroupDocument.getName();

                        ArrayList<EventCard> eventsList = new ArrayList<EventCard>();
                        eventContainerMap.put(groupName, eventsList);

                        documentSnapshot
                                .getReference()
                                .collection("TeachersEvents")
                                .addSnapshotListener((chatDocumentsSnapshots, error2) -> {

                                    if (error2 != null) {
                                        return;
                                    } else if (chatDocumentsSnapshots == null) {
                                        return;
                                    }

                                    eventsList.clear();

                                    hasDocumentsMap.put(groupName, !chatDocumentsSnapshots.isEmpty());

                                    for (DocumentSnapshot documentSnapshot1 : chatDocumentsSnapshots) {
                                        EventCardDocument eventCardDocument = documentSnapshot1.toObject(EventCardDocument.class);

                                        EventCard eventCard = new EventCard(eventCardDocument.getEventTile(), eventCardDocument.getEventDescription(), eventCardDocument.getEventPlace(), eventCardDocument.getEventDate() ,documentSnapshot1,  eventCardDocument.getSenderID(), eventCardDocument.getSentByTeacher());
                                        eventsList.add(eventCard);
                                    }

                                    changeList();
                                });
                    }
                });

        return view;
    }

    private void changeList() {
        eventContainerList.clear();
        for (String key : eventContainerMap.keySet()) {
            ArrayList<EventCard> interactivitiesList = eventContainerMap.get(key);
            Boolean hasDocuments = hasDocumentsMap.get(key);
            if (interactivitiesList != null && hasDocuments != null) {
                if (hasDocuments) {
                    eventContainerList.add(new EventContainerCard(key, interactivitiesList));
                }
            }
        }

        if (eventContainerList.isEmpty()) {
            warningMessage.setVisibility(View.VISIBLE);
        } else {
            warningMessage.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }
}
