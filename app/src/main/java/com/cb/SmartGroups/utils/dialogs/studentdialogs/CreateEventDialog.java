package com.cb.SmartGroups.utils.dialogs.studentdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.listviews.SelectGroupsItemAdapter;
import com.cb.SmartGroups.utils.customdatamodels.SelectGroupItem;
import com.cb.SmartGroups.utils.firesoredatamodels.CollectiveGroupDocument;
import com.cb.SmartGroups.utils.firesoredatamodels.EventCardDocument;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class CreateEventDialog extends DialogFragment {

    // Selected course and subject
    private String selectedCourse;
    private String selectedSubject;

    // Firestore
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    private ArrayList<SelectGroupItem> groupItems;
    private SelectGroupsItemAdapter adapter;

    private Context context;

    private FloatingActionButton selectDate;

    private String selectedDate;

    public CreateEventDialog(String selectedCourse, String selectedSubject) {
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        groupItems = new ArrayList<SelectGroupItem>();
        adapter = new SelectGroupsItemAdapter(context, groupItems);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_dialogs_createventdialog, null);

        ListView groupList = view.findViewById(R.id.groupList);
        groupList.setAdapter(adapter);

        // Collection reference
        CollectionReference collectiveGroupsCollRef = fStore
                .collection("CoursesOrganization")
                .document(selectedCourse)
                .collection("Subjects")
                .document(selectedSubject)
                .collection("CollectiveGroups");

        collectiveGroupsCollRef
                .whereEqualTo("spokerID", fAuth.getUid())
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

        TextInputLayout eventTitleLayout = view.findViewById(R.id.eventTitleLayout);
        TextInputLayout eventDescriptionLayout = view.findViewById(R.id.eventDescriptionLayout);
        TextInputLayout eventPlaceLayout = view.findViewById(R.id.eventPlaceLayout);

        TextInputEditText eventTitle = view.findViewById(R.id.eventTitle);
        TextInputEditText eventDescription = view.findViewById(R.id.eventDescription);
        TextInputEditText eventPlace = view.findViewById(R.id.eventPlace);
        selectDate = view.findViewById(R.id.selectDate);

        selectDate.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
            MaterialDatePicker<Long> picker = datePickerBuilder.build();
            picker.show(getParentFragmentManager(), picker.toString());
            picker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date();
                date.setTime(selection);
                DateFormat df = new SimpleDateFormat("dd-MM-yy");
                df.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
                selectedDate = df.format(date);
            });
        });

        builder.setView(view)
                .setTitle("Create event")
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Just closes the dialog
                }).setPositiveButton("Create event", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(view1 -> {
                String eventTitleText = eventTitle.getText().toString();
                String eventDescriptionText = eventDescription.getText().toString();
                String eventPlaceText = eventPlace.getText().toString();

                if (eventTitleText.isEmpty() || eventDescriptionText.isEmpty() || eventPlaceText.isEmpty() || selectedDate == null) {
                    Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show();
                } else {

                    ArrayList<String> selectedGroupsIDs = new ArrayList<String>();

                    for (SelectGroupItem item : groupItems) {
                        if (item.isSelected()) {
                            selectedGroupsIDs.add(item.getGroupID());
                        }
                    }

                    collectiveGroupsCollRef
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if (selectedGroupsIDs.contains(documentSnapshot.getId())) {
                                        EventCardDocument eventCardDocument = new EventCardDocument(eventTitleText, eventDescriptionText, eventPlaceText, selectedDate, false, fAuth.getUid());
                                        documentSnapshot.getReference().collection("StudentEvents").add(eventCardDocument);
                                    }
                                }
                            });

                    dialog.dismiss();
                }

            });
        });

        return dialog;
    }
}
