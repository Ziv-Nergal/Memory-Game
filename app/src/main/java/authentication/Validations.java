package authentication;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Validations {

    public static void ValidateLoginValues(String email, String password) throws EmptyValueException{

        if(email.isEmpty()){
            throw new EmptyValueException("Email");
        }else if (password.isEmpty()){
            throw new EmptyValueException("Password");
        }
    }

    public static void ValidateRegistrationValues(String name, String email, String userName, String password,
                                                  String confirmPassword) throws EmptyValueException{

        if(name.isEmpty()){
            throw new EmptyValueException("Name");
        }else if (email.isEmpty()){
            throw new EmptyValueException("Email");
        }else if (userName.isEmpty()){
            throw new EmptyValueException("User name");
        }else if (password.isEmpty()){
            throw new EmptyValueException("Password");
        }else if (confirmPassword.isEmpty()){
            throw new EmptyValueException("Confirm password");
        }
    }

    public static void ValidateMatchingPasswords(String pass, String confirmPass) throws PassDoNotMatchException{
        if(!pass.equals(confirmPass)){
            throw new PassDoNotMatchException();
        }
    }

    public static void ShowErrorMessage(Context context, Exception exception){

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {

            String errorCode = ((FirebaseAuthInvalidCredentialsException) exception).getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_WEAK_PASSWORD":
                    Toast.makeText(context, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (exception instanceof FirebaseAuthInvalidUserException) {

            String errorCode =
                    ((FirebaseAuthInvalidUserException) exception).getErrorCode();

            switch (errorCode) {
                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(context, "No matching account found", Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_USER_DISABLED":
                    Toast.makeText(context, "User account has been disabled", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }else if (exception instanceof FirebaseAuthUserCollisionException){

            Toast.makeText(context, "Email is already in use by another account", Toast.LENGTH_SHORT).show();
        }
    }

    public static class EmptyValueException extends Exception {
        EmptyValueException(String value) {
            super(value + " is empty!");
        }
    }

    public static class PassDoNotMatchException extends Exception {
        PassDoNotMatchException() {
            super("Passwords do not match! try again...");
        }
    }
}
