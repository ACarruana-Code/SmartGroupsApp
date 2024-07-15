package com.cb.SmartGroups.utils.cards.interactivity.teachercards;

import com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments.MultichoiceCardDocument;
import com.google.firebase.firestore.DocumentSnapshot;

public class MultichoiceCard extends InteractivityCard {

    private DocumentSnapshot multichoiceCardDocumentSnapshot;

    public MultichoiceCard() {

    }

    public MultichoiceCard(DocumentSnapshot multichoiceCardDocumentSnapshot) {
        super(multichoiceCardDocumentSnapshot.toObject(MultichoiceCardDocument.class).getTitle());
        this.multichoiceCardDocumentSnapshot = multichoiceCardDocumentSnapshot;
    }

    public DocumentSnapshot getMultichoiceCardDocumentSnapshot() {
        return multichoiceCardDocumentSnapshot;
    }

    public MultichoiceCardDocument getMultichoiceCardDocument() {
        return multichoiceCardDocumentSnapshot.toObject(MultichoiceCardDocument.class);
    }

    public boolean getHasTeacherVisibility(){
        return getMultichoiceCardDocument().getHasTeacherVisibility();
    }

}
