package com.cb.SmartGroups.fragments.teacher;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.recyclerviews.interactivity.teacher.GroupsInteractivityCardsContainerAdapter;
import com.cb.SmartGroups.utils.cards.interactivity.teachercards.InteractivityCardsContainer;
import com.cb.SmartGroups.utils.cards.interactivity.teachercards.InputTextCardParent;
import com.cb.SmartGroups.utils.cards.interactivity.teachercards.InteractivityCard;
import com.cb.SmartGroups.utils.cards.interactivity.teachercards.MultichoiceCard;
import com.cb.SmartGroups.utils.customdatamodels.InteractivityCardType;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.CreateEventDialog;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.CreateInputTextCardDialog;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.CreateMultichoiceCardDialog;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments.InputTextCardDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments.MultichoiceCardDocument;
import com.cb.SmartGroups.utils.utilities.ButtonAnimator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class Interactivity extends Fragment {

    // Firestore
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    private String selectedCourse;
    private String selectedSubject;

    // Buttons
    private FloatingActionButton createInteractivityCard;
    private FloatingActionButton createInputTextCard;
    private FloatingActionButton createMultichoiceCard;
    private FloatingActionButton createReminderCard;

    private ArrayList<InteractivityCardsContainer> interactivityContainerList;
    private GroupsInteractivityCardsContainerAdapter adapter;

    private HashMap<String, ArrayList<InteractivityCard>> interactivityListsMap;
    private HashMap<String, Boolean> hasDocumentsMap;

    private HashMap<String, QuerySnapshot> allInteractivityDocumentsSnapshotsMap;
    private HashMap<String, HashMap<String, Double>> statisticsMap;

    private TextView explicativeError;

    public Interactivity(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        interactivityListsMap = new HashMap<String, ArrayList<InteractivityCard>>();
        hasDocumentsMap = new HashMap<String, Boolean>();
        allInteractivityDocumentsSnapshotsMap = new HashMap<String, QuerySnapshot>();
        statisticsMap = new HashMap<String, HashMap<String, Double>>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_interactivity, container, false);

        explicativeError = view.findViewById(R.id.explicativeError);
        explicativeError.setVisibility(View.GONE);

        // Buttons
        createInteractivityCard = view.findViewById(R.id.createInteractivityCard);
        createInputTextCard = view.findViewById(R.id.createInputTextCard);
        createMultichoiceCard = view.findViewById(R.id.createMultichoiceCard);
        createReminderCard = view.findViewById(R.id.createReminderCard);

        // InteractivityCardsContainer
        RecyclerView interactivityCardsContainerRecyclerView = view.findViewById(R.id.interactivityCardsContainer);
        interactivityContainerList = new ArrayList<InteractivityCardsContainer>();
        adapter = new GroupsInteractivityCardsContainerAdapter(interactivityContainerList, getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        interactivityCardsContainerRecyclerView.setAdapter(adapter);
        interactivityCardsContainerRecyclerView.setLayoutManager(layoutManager);

        // Buttons configuration
        ArrayList<FloatingActionButton> buttons = new ArrayList<FloatingActionButton>();
        buttons.add(createInputTextCard);
        buttons.add(createMultichoiceCard);
        buttons.add(createReminderCard);

        ButtonAnimator buttonAnimator = new ButtonAnimator(getContext(), createInteractivityCard, buttons);

        createInteractivityCard.setOnClickListener(v -> buttonAnimator.onButtonClicked());

        createInputTextCard.setOnClickListener(v -> {
            CreateInputTextCardDialog dialog = new CreateInputTextCardDialog(selectedCourse, selectedSubject);
            dialog.show(getParentFragmentManager(), "dialog");
        });

        createMultichoiceCard.setOnClickListener(v -> {
            CreateMultichoiceCardDialog dialog = new CreateMultichoiceCardDialog(selectedCourse, selectedSubject);
            dialog.show(getParentFragmentManager(), "dialog");
        });

        createReminderCard.setOnClickListener(v -> {
            CreateEventDialog dialog = new CreateEventDialog(selectedCourse, selectedSubject);
            dialog.show(getParentFragmentManager(), "dialog");
        });

        fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups")
                .get()
                .addOnSuccessListener(collectiveGroupsDocumentSnapshots -> {

                    if (collectiveGroupsDocumentSnapshots.isEmpty()) {
                        explicativeError.setText(R.string.noGroupsInteractivity);
                        explicativeError.setVisibility(View.VISIBLE);
                        hideButtons();
                    } else {
                        showButtons();
                        explicativeError.setVisibility(View.GONE);
                        for (DocumentSnapshot documentSnapshot : collectiveGroupsDocumentSnapshots) {
                            String groupName = documentSnapshot.toObject(CollectiveGroupDocument.class).getName();

                            ArrayList<InteractivityCard> interactivityCardsList = new ArrayList<InteractivityCard>();
                            interactivityListsMap.put(groupName, interactivityCardsList);

                            documentSnapshot
                                    .getReference()
                                    .collection("InteractivityCards")
                                    .addSnapshotListener((interactivityCardsDocumentSnapshots, error1) -> {

                                        if (error1 != null) {
                                            return;
                                        } else if (interactivityCardsDocumentSnapshots == null) {
                                            return;
                                        }

                                        statisticsMap.put(groupName, getCardStatistics(interactivityCardsDocumentSnapshots));

                                        interactivityCardsList.clear();

                                        hasDocumentsMap.put(groupName, !interactivityCardsDocumentSnapshots.isEmpty());
                                        allInteractivityDocumentsSnapshotsMap.put(groupName, interactivityCardsDocumentSnapshots);

                                        for (DocumentSnapshot interactivityCardDocumentSnapshot : interactivityCardsDocumentSnapshots) {

                                            Long cardType = interactivityCardDocumentSnapshot.getLong("cardType");

                                            if (cardType != null) {
                                                switch (cardType.intValue()) {
                                                    case InteractivityCardType.TYPE_INPUTTEXT:
                                                        InputTextCardParent inputTextCardParent = new InputTextCardParent(interactivityCardDocumentSnapshot);

                                                        if (inputTextCardParent.getHasTeacherVisibility()) {
                                                            interactivityCardsList.add(inputTextCardParent);
                                                        }

                                                        break;
                                                    case InteractivityCardType.TYPE_CHOICES:

                                                        MultichoiceCard multichoiceCard = new MultichoiceCard(interactivityCardDocumentSnapshot);

                                                        if (multichoiceCard.getHasTeacherVisibility()) {
                                                            interactivityCardsList.add(multichoiceCard);
                                                        }

                                                        break;
                                                    case InteractivityCardType.TYPE_REMINDER:
                                                        break;
                                                }
                                            }
                                        }
                                        changeList();
                                    });
                        }
                    }
                });

        return view;
    }

    private void hideButtons() {
        createInteractivityCard.setVisibility(View.GONE);
        createInputTextCard.setVisibility(View.GONE);
        createMultichoiceCard.setVisibility(View.GONE);
        createReminderCard.setVisibility(View.GONE);
    }

    private void showButtons() {
        createInteractivityCard.setVisibility(View.VISIBLE);
    }

    private void changeList() {
        interactivityContainerList.clear();
        for (String key : interactivityListsMap.keySet()) {
            ArrayList<InteractivityCard> interactivitiesList = interactivityListsMap.get(key);
            Boolean hasDocuments = hasDocumentsMap.get(key);
            QuerySnapshot allInteractivityDocumentsSnapshots = allInteractivityDocumentsSnapshotsMap.get(key);
            HashMap<String, Double> statistics = statisticsMap.get(key);

            if (interactivitiesList != null && hasDocuments != null && allInteractivityDocumentsSnapshots != null && statistics != null) {
                if (hasDocuments) {
                    interactivityContainerList.add(new InteractivityCardsContainer("Activities with " + key, interactivitiesList, allInteractivityDocumentsSnapshots, statistics));
                }
            }
        }

        if (interactivityContainerList.isEmpty()) {
            explicativeError.setText(R.string.noInteractivitiesCreated);
            explicativeError.setVisibility(View.VISIBLE);
        } else {
            explicativeError.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private HashMap<String, Double> getCardStatistics(QuerySnapshot interactivityCardsDocumentSnapshots) {
        HashMap<String, Double> statistics = new HashMap<String, Double>();

        // InputText
        double evaluableGroupalInputCards = 0;
        double groupalMarkInputText = 0;

        double evaluableIndividualStudents = 0;
        double individualMarkInputText = 0;

        // Multichoice
        double evaluableGroupalMultichoiceCards = 0;
        double totalGroupalPoints = 0;

        double evaluableIndividuals = 0;
        double evaluableIndividualMarks = 0;

        for (DocumentSnapshot interactivityCardsDocumentSnapshot : interactivityCardsDocumentSnapshots) {
            Long cardType = interactivityCardsDocumentSnapshot.getLong("cardType");

            if (cardType != null) {
                if (cardType == InteractivityCardType.TYPE_INPUTTEXT) {
                    InputTextCardDocument inputTextCardDocument = interactivityCardsDocumentSnapshot.toObject(InputTextCardDocument.class);

                    if (inputTextCardDocument.getHasToBeEvaluated()) {
                        if (inputTextCardDocument.getHasGroupalActivity()) {
                            InputTextCardDocument.InputTextCardStudentData groupData = inputTextCardDocument.getStudentsData().get(0);
                            if (groupData.getHasMarkSet()) {
                                evaluableGroupalInputCards++;
                                groupalMarkInputText += groupData.getMark();
                            }
                        } else {
                            for (InputTextCardDocument.InputTextCardStudentData studentData : inputTextCardDocument.getStudentsData()) {
                                if (studentData.getHasMarkSet()) {
                                    evaluableIndividualStudents++;
                                    individualMarkInputText += studentData.getMark();
                                }
                            }
                        }
                    }

                } else if (cardType == InteractivityCardType.TYPE_CHOICES) {
                    MultichoiceCardDocument multichoiceCardDocument = interactivityCardsDocumentSnapshot.toObject(MultichoiceCardDocument.class);

                    if (multichoiceCardDocument.getHasToBeEvaluated()) {
                        if (multichoiceCardDocument.getHasGroupalActivity()) {
                            MultichoiceCardDocument.MultichoiceCardStudentData groupData = multichoiceCardDocument.getStudentsData().get(0);
                            if (groupData.getQuestionRespondedIdentifier() != -1) {
                                evaluableGroupalMultichoiceCards++;
                                totalGroupalPoints += groupData.getMark();
                            }
                        } else {
                            for (MultichoiceCardDocument.MultichoiceCardStudentData studentData : multichoiceCardDocument.getStudentsData()) {
                                if (studentData.getQuestionRespondedIdentifier() != -1) {
                                    evaluableIndividuals++;
                                    evaluableIndividualMarks += studentData.getMark();
                                }
                            }
                        }
                    }

                }
            }
        }

        // InputText Statistics
        // Groupal InputText mark
        statistics.put("Groupal InputText Mark", groupalMarkInputText);
        statistics.put("Groupal InputText Cards", evaluableGroupalInputCards);

        // Individual InputText mark
        statistics.put("Individual InputText Mark", individualMarkInputText);
        statistics.put("Individual Evaluable Students", evaluableIndividualStudents);

        // Multichoice Statistics
        // Groupal Multichoice mark
        statistics.put("Groupal Multichoice Cards", evaluableGroupalMultichoiceCards);
        statistics.put("Groupal Multichoice Mark", totalGroupalPoints);

        // Individual Multichoice mark
        statistics.put("Individial Multichoice Evaluable", evaluableIndividuals);
        statistics.put("Individual Mulichoice Mark", evaluableIndividualMarks);

        return statistics;
    }

}