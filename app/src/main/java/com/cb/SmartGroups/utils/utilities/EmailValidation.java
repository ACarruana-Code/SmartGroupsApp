package com.cb.SmartGroups.utils.utilities;

/**
 * Class which single method is used to check if the format of the email given as input is valid.
 * It never checks if the email exists.
 *
 * @author Martín Mateos Sánchez and Adrián Carruana Martín
 */
public class EmailValidation {

    public static boolean isInvalidEmail(CharSequence target) {
        if (target == null) {
            return true;
        } else {
            return !android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
