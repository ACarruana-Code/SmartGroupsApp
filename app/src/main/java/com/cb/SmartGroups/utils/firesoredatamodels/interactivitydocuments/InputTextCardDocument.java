package com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments;

import com.cb.SmartGroups.utils.customdatamodels.InteractivityCardType;

import java.util.ArrayList;

public class InputTextCardDocument {

    private String title;
    private boolean hasToBeEvaluated;
    private boolean hasGroupalActivity;
    private boolean hasTeacherVisibility;

    private ArrayList<InputTextCardStudentData> studentsData;

    public InputTextCardDocument() {

    }

    public InputTextCardDocument(String title, boolean hasToBeEvaluated, boolean hasGroupalActivity, ArrayList<String> studentsIDs) {
        this.title = title;

        this.hasToBeEvaluated = hasToBeEvaluated;
        ArrayList<InputTextCardStudentData> studentsData = new ArrayList<InputTextCardStudentData>();


        for (String studentID : studentsIDs) {
            studentsData.add(new InputTextCardStudentData(studentID));
        }

        this.hasTeacherVisibility = true;
        this.hasGroupalActivity = hasGroupalActivity;
        this.studentsData = studentsData;
    }

    public String getTitle() {
        return title;
    }

    public boolean getHasToBeEvaluated() {
        return hasToBeEvaluated;
    }

    public boolean getHasTeacherVisibility() {
        return hasTeacherVisibility;
    }

    public boolean getHasGroupalActivity() {
        return hasGroupalActivity;
    }

    public int getCardType() {
        return InteractivityCardType.TYPE_INPUTTEXT;
    }

    public ArrayList<InputTextCardStudentData> getStudentsData() {
        return studentsData;
    }


    public static class InputTextCardStudentData {
        private String studentID;
        private float mark;
        private boolean hasMarkSet;
        private String response;

        public InputTextCardStudentData() {

        }

        public InputTextCardStudentData(String studentID) {
            this.studentID = studentID;
            this.hasMarkSet = false;
        }

        public String getStudentID() {
            return studentID;
        }

        public float getMark() {
            return mark;
        }

        public boolean getHasMarkSet() {
            return hasMarkSet;
        }

        public String getResponse() {
            return response;
        }

        public void setStudentID(String studentID) {
            this.studentID = studentID;
        }

        public void setMark(float mark) {
            this.mark = mark;
        }

        public void setHasMarkSet(boolean hasMarkSet) {
            this.hasMarkSet = hasMarkSet;
        }

        public void setResponse(String response) {
            this.response = response;
        }

    }

}
