package com.cb.SmartGroups.utils.cards.interactivity.teachercards;


import com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments.InputTextCardDocument;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class InputTextCardParent extends InteractivityCard {

    private final ArrayList<InputTextCardChild> inputTextCardChildList;
    private final DocumentSnapshot documentSnapshot;

    public InputTextCardParent(DocumentSnapshot documentSnapshot) {
        super(documentSnapshot.toObject(InputTextCardDocument.class).getTitle());
        this.inputTextCardChildList = new ArrayList<InputTextCardParent.InputTextCardChild>();
        this.documentSnapshot = documentSnapshot;

        populateChildsList();
    }

    private void populateChildsList() {

        for (InputTextCardDocument.InputTextCardStudentData studentData : getInputTextCardDocument().getStudentsData()) {
            if (studentData.getResponse() != null) {
                InputTextCardParent.InputTextCardChild childInputTextCard =
                        new InputTextCardParent.InputTextCardChild(
                                studentData.getStudentID(),
                                studentData.getResponse(),
                                studentData.getHasMarkSet(),
                                getInputTextCardDocument(),
                                documentSnapshot.getReference()
                        );
                inputTextCardChildList.add(childInputTextCard);
            }
        }

    }

    public ArrayList<InputTextCardChild> getInputTextCardChildList() {
        return inputTextCardChildList;
    }

    public DocumentSnapshot getDocumentSnapshot() {
        return documentSnapshot;
    }

    public InputTextCardDocument getInputTextCardDocument() {
        return documentSnapshot.toObject(InputTextCardDocument.class);
    }

    public boolean getHasTeacherVisibility() {
        return getInputTextCardDocument().getHasTeacherVisibility();
    }

    public static class InputTextCardChild {

        private String studentID;
        private String response;
        private boolean hasMarkSet;
        private InputTextCardDocument inputTextCardDocument;
        private DocumentReference documentReference;

        public InputTextCardChild() {

        }

        public InputTextCardChild(String studentID, String response, boolean hasMarkSet, InputTextCardDocument inputTextCardDocument, DocumentReference documentReference) {
            this.studentID = studentID;
            this.response = response;
            this.hasMarkSet = hasMarkSet;
            this.inputTextCardDocument = inputTextCardDocument;
            this.documentReference = documentReference;
        }

        public String getStudentID() {
            return studentID;
        }

        public String getResponse() {
            return response;
        }

        public boolean getHasMarkSet() {
            return hasMarkSet;
        }

        public InputTextCardDocument getInputTextCardDocument() {
            return inputTextCardDocument;
        }

        public DocumentReference getDocumentReference() {
            return documentReference;
        }
    }

}
