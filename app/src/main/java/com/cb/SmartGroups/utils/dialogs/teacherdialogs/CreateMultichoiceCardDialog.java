package com.cb.SmartGroups.utils.dialogs.teacherdialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;

import com.cb.SmartGroups.adapters.listviews.SelectGroupsItemAdapter;
import com.cb.SmartGroups.utils.customdatamodels.SelectGroupItem;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;

import com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments.MultichoiceCardDocument;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateMultichoiceCardDialog extends DialogFragment {

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    private final String selectedCourse;
    private final String selectedSubject;

    ListView groupList;

    private TextInputLayout inputCardNameLayout;
    private TextInputEditText inputCardName;

    private TextInputLayout inputQuestionTitleLayout;
    private TextInputEditText inputQuestionTitle;

    private FloatingActionButton addOption;
    private CheckBox questionIsEvaluable;
    private CheckBox groupalQuestion;

    private LinearLayout questionsTitleContainer;
    private RadioGroup questionsRadioGroup;

    TextView textView5;
    TextView errorMessage;

    private ArrayList<SelectGroupItem> groupItems;
    private SelectGroupsItemAdapter adapter;

    private Context context;

    public CreateMultichoiceCardDialog(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        groupItems = new ArrayList<SelectGroupItem>();
        adapter = new SelectGroupsItemAdapter(context, groupItems);
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.utils_dialogs_createmultichoicecarddialog, null);

        groupList = view.findViewById(R.id.groupList);
        inputCardNameLayout = view.findViewById(R.id.inputCardNameLayout);
        inputCardName = view.findViewById(R.id.inputCardName);
        inputQuestionTitleLayout = view.findViewById(R.id.inputQuestionTitleLayout);
        inputQuestionTitle = view.findViewById(R.id.inputQuestionTitle);
        addOption = view.findViewById(R.id.addOption);
        textView5 = view.findViewById(R.id.textView5);
        textView5.setVisibility(View.GONE);

        groupList.setAdapter(adapter);


        questionIsEvaluable = view.findViewById(R.id.questionIsEvaluable);
        questionIsEvaluable.setChecked(false);

        groupalQuestion = view.findViewById(R.id.groupalQuestion);
        groupalQuestion.setChecked(false);

        questionsTitleContainer = view.findViewById(R.id.questionsTitleContainer);
        questionsRadioGroup = view.findViewById(R.id.questionsRadioGroup);
        questionsRadioGroup.setVisibility(View.GONE);

        errorMessage = view.findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.GONE);

        HashMap<String, String> groupMap = new HashMap<String, String>();

        // Collection reference
        CollectionReference collectiveGroupsCollRef = fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups");

        collectiveGroupsCollRef
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CollectiveGroupDocument groupDocument = documentSnapshot.toObject(CollectiveGroupDocument.class);
                        String groupName = groupDocument.getName();
                        String groupID = documentSnapshot.getId();

                        SelectGroupItem groupItem = new SelectGroupItem(groupName, groupID);
                        groupItems.add(groupItem);
                    }
                    adapter.notifyDataSetChanged();
                });

        questionsRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> errorMessage.setVisibility(View.GONE));

        questionIsEvaluable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            errorMessage.setVisibility(View.GONE);
            if (isChecked) {
                questionsRadioGroup.setVisibility(View.VISIBLE);
                questionsTitleContainer.setVisibility(View.GONE);
            } else {
                questionsRadioGroup.setVisibility(View.GONE);
                questionsTitleContainer.setVisibility(View.VISIBLE);
            }

        });

        addOption.setOnClickListener(v -> {
            if (inputQuestionTitle.getText().toString().isEmpty()) {
                inputQuestionTitleLayout.setErrorEnabled(true);
                inputQuestionTitleLayout.setError("The title of the option cannot be empty");
            } else {
                inputQuestionTitleLayout.setErrorEnabled(false);
                if (questionsRadioGroup.getChildCount() >= 5 || questionsTitleContainer.getChildCount() >= 5) {
                    inputQuestionTitleLayout.setErrorEnabled(true);
                    inputQuestionTitleLayout.setError("You can add no more than 5 options");
                } else if (inputQuestionTitle.getText().toString().length() > 100) {
                    inputQuestionTitleLayout.setErrorEnabled(true);
                    inputQuestionTitleLayout.setError("Too long a sentence");
                } else {

                    textView5.setVisibility(View.VISIBLE);
                    errorMessage.setVisibility(View.GONE);
                    inputQuestionTitleLayout.setErrorEnabled(false);
                    String newOption = inputQuestionTitle.getText().toString();

                    RadioButton button = new RadioButton(questionsRadioGroup.getContext());
                    button.setText(newOption);
                    questionsRadioGroup.addView(button);

                    TextView textView = new TextView(questionsTitleContainer.getContext());
                    textView.setText(newOption);
                    textView.setTextColor(Color.rgb(0, 0, 0));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(8, 8, 8, 8);
                    textView.setLayoutParams(params);

                    questionsTitleContainer.addView(textView);
                    inputQuestionTitle.getText().clear();
                }
            }
        });

        builder.setTitle("Create new multi-response type activity")
                .setView(view)
                .setNegativeButton("Cancel", (dialog, i) -> {
                    // Just closes the dialog
                })
                .setPositiveButton("Create", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(view1 -> {
                String cardTitle = inputCardName.getText().toString();
                inputCardNameLayout.setErrorEnabled(false);

                boolean isGroupalQuestion = groupalQuestion.isChecked();
                boolean isQuestionEvaluable = questionIsEvaluable.isChecked();

                // Check for errors
                if (cardTitle.isEmpty()) {
                    inputCardNameLayout.setErrorEnabled(true);
                    inputCardNameLayout.setError("The title of the question cannot be empty");
                } else if (cardTitle.length() > 100) {
                    inputCardNameLayout.setErrorEnabled(true);
                    inputCardNameLayout.setError("Title too long");
                } else if (questionsRadioGroup.getChildCount() < 2 || questionsTitleContainer.getChildCount() < 2) {
                    errorMessage.setText(R.string.debes_agregar_al_menos_dos_opciones);
                    errorMessage.setVisibility(View.VISIBLE);
                } else {

                    int checkedRadioButtonID = questionsRadioGroup.getCheckedRadioButtonId();

                    if (checkedRadioButtonID == -1 && isQuestionEvaluable) {
                        errorMessage.setText(R.string.noSelected);
                        errorMessage.setVisibility(View.VISIBLE);
                    } else { // Create evaluable multichoicecardactivity

                        ArrayList<String> selectedGroupsIDs = new ArrayList<String>();

                        for (SelectGroupItem item : groupItems) {
                            if (item.isSelected()) {
                                selectedGroupsIDs.add(item.getGroupID());
                            }
                        }

                        if (selectedGroupsIDs.isEmpty()) {
                            Toast.makeText(context, "Select at least one group", Toast.LENGTH_SHORT).show();
                        } else {
                            collectiveGroupsCollRef
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            if (selectedGroupsIDs.contains(documentSnapshot.getId())) {
                                                CollectiveGroupDocument groupDocument = documentSnapshot.toObject(CollectiveGroupDocument.class);

                                                ArrayList<MultichoiceCardDocument.Question> questionsList = new ArrayList<MultichoiceCardDocument.Question>();
                                                ArrayList<String> studentsIDs;

                                                for (int i = 0; i < questionsRadioGroup.getChildCount(); i++) {
                                                    RadioButton button = (RadioButton) questionsRadioGroup.getChildAt(i);
                                                    MultichoiceCardDocument.Question newQuestion = new MultichoiceCardDocument.Question(button.getText().toString(), i);
                                                    if (isQuestionEvaluable) {
                                                        newQuestion.setHasCorrectAnswer(button.getId() == checkedRadioButtonID);
                                                    } else {
                                                        newQuestion.setHasCorrectAnswer(false);
                                                    }
                                                    questionsList.add(newQuestion);
                                                }

                                                if (isGroupalQuestion) {
                                                    studentsIDs = new ArrayList<String>();
                                                    studentsIDs.add(groupDocument.getSpokerID());
                                                } else {
                                                    groupDocument.getAllParticipantsIDs().remove(fAuth.getUid());
                                                    studentsIDs = groupDocument.getAllParticipantsIDs();
                                                }

                                                MultichoiceCardDocument multichoiceCardDocument = new MultichoiceCardDocument(cardTitle, isQuestionEvaluable, isGroupalQuestion, questionsList, studentsIDs);

                                                documentSnapshot.getReference().collection("InteractivityCards").add(multichoiceCardDocument);

                                            }
                                        }
                                    });

                            dialog.dismiss();
                        }
                    }
                }
            });
        });


        return dialog;
    }

}
