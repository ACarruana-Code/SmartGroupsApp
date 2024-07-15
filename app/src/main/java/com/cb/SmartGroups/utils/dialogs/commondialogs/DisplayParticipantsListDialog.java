package com.cb.SmartGroups.utils.dialogs.commondialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.listviews.PendingParticipantsListAdapter;
import com.cb.SmartGroups.utils.firesoredatamodels.PetitionGroupParticipant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DisplayParticipantsListDialog extends DialogFragment {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    Context context;
    ArrayList<PetitionGroupParticipant> participantsList;

    public DisplayParticipantsListDialog(ArrayList<PetitionGroupParticipant> participantsList){
        this.participantsList = participantsList;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.utils_participantspetitionlist, null);

        ListView listView = view.findViewById(R.id.participantsList);

        PendingParticipantsListAdapter adapter = new PendingParticipantsListAdapter(context, participantsList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        builder.setView(view).setTitle("Participantes")
                .setPositiveButton("Vale", (dialogInterface, i) -> {
                    // Just closes the dialog
                });

        return builder.create();
    }

}
