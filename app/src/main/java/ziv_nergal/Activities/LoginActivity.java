package ziv_nergal.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import authentication.Validations;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import br.com.simplepass.loading_button_lib.interfaces.OnAnimationEndListener;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import online_utils.ProfileImageManager;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUserDatabaseRef;

    private CircleImageView mRegUserImage;

    private File mUserImageFile;

    private Handler delayHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void LoginBtnClick(View view) {

        try {
            final String emailStr = ((EditText)findViewById(R.id.login_email_et)).getText().toString().trim();
            final String passStr = ((EditText)findViewById(R.id.login_password_et)).getText().toString().trim();

            final CircularProgressButton loginBtn = (CircularProgressButton) view;

            Validations.ValidateLoginValues(emailStr, passStr);

            loginBtn.startAnimation();

            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mAuth.signInWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        updateBtnFinishLoading(loginBtn, true);

                                        setUsersDeviceToken();

                                        delayHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                finish();
                                            }
                                        }, 1000);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    updateBtnFinishLoading(loginBtn, false);
                                    Validations.ShowErrorMessage(LoginActivity.this, e);
                                }
                            });
                }
            }, 1500);
        } catch (Validations.EmptyValueException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void CreateNewUserBtnClick(View view) {

        View dialogView = getLayoutInflater().inflate(R.layout.registration_dialog, null);

        final Dialog regDialog = createRegisterDialog(dialogView);

        final EditText nameET = dialogView.findViewById(R.id.reg_name);
        final EditText emailET = dialogView.findViewById(R.id.reg_email);
        final EditText userNameET = dialogView.findViewById(R.id.reg_user_name);
        final EditText passET = dialogView.findViewById(R.id.reg_pass);
        final EditText confirmPassET = dialogView.findViewById(R.id.reg_confirm_pass);

        mRegUserImage = dialogView.findViewById(R.id.reg_user_image);

        setRegEditTextChangeListeners(new EditText[]{nameET, emailET, userNameET, passET, confirmPassET});

        final CircularProgressButton doneBtn = dialogView.findViewById(R.id.reg_done_btn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    final String nameStr = nameET.getText().toString().trim();
                    final String emailStr = emailET.getText().toString().trim();
                    final String userNameStr = userNameET.getText().toString().trim();
                    final String passStr = passET.getText().toString().trim();
                    final String confirmPassStr = confirmPassET.getText().toString().trim();

                    Validations.ValidateRegistrationValues(nameStr, emailStr, userNameStr, passStr, confirmPassStr);
                    Validations.ValidateMatchingPasswords(passStr, confirmPassStr);

                    doneBtn.startAnimation();

                    mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        insertNewUserToDataBase(nameStr, emailStr, userNameStr, doneBtn);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    updateBtnFinishLoading(doneBtn, false);
                                    Validations.ShowErrorMessage(LoginActivity.this, e);
                                }
                            });
                }catch (Validations.EmptyValueException | Validations.PassDoNotMatchException ex) {
                    Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogView.findViewById(R.id.online_dialog_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regDialog.cancel();
            }
        });

        dialogView.findViewById(R.id.reg_take_photo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1,1)
                        .start(LoginActivity.this);
            }
        });

        regDialog.show();
    }

    private Dialog createRegisterDialog(View dialogView) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setContentView(dialogView);
        dialog.getWindow().setLayout(
                (6 * getResources().getDisplayMetrics().widthPixels)/7,
                (4 * getResources().getDisplayMetrics().heightPixels)/5);

        return dialog;
    }

    private void insertNewUserToDataBase(String nameStr, String emailStr, String userNameStr, final CircularProgressButton doneBtn) {

        if(mAuth.getCurrentUser() != null) {

            final FirebaseUser currentUser = mAuth.getCurrentUser();

            currentUser.updateEmail(emailStr);
            currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userNameStr).build());

            Map<String, Object> userDetailsMap = new HashMap<>();

            userDetailsMap.put("name", nameStr);
            userDetailsMap.put("email", emailStr);
            userDetailsMap.put("display_name", userNameStr);
            userDetailsMap.put("photoUrl", "default");

            mUserDatabaseRef = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("Users")
                    .child(currentUser.getUid());

            mUserDatabaseRef.setValue(userDetailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mUserDatabaseRef.child("online").setValue(true);

                                setUsersDeviceToken();

                                if (mUserImageFile != null) {
                                    ProfileImageManager.Upload(mUserImageFile, currentUser);
                                }

                                updateBtnFinishLoading(doneBtn, true);

                                delayHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }
                                }, 1000);
                            }
                        }
                    });
        }
    }

    private void setUsersDeviceToken() {

        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        DatabaseReference currentUserDbRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid());

        currentUserDbRef.child("device_token").setValue(deviceToken);
    }

    private void setRegEditTextChangeListeners(EditText[] editTexts) {

        for (final EditText et : editTexts){

            et.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    String text = et.getText().toString().trim();

                    boolean isValid = false;

                    switch (et.getTag().toString()) {

                        case "email":
                            if (!TextUtils.isEmpty(text) && android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                                isValid = true;
                            }

                            break;

                        case "pass":
                        case "confirm_pass":
                            if (text.length() >= 6) {
                                isValid = true;
                            }

                            break;

                        default:
                            if (!text.isEmpty()) {
                                isValid = true;
                            }

                            break;
                    }

                    if(isValid) {
                        et.setCompoundDrawablesWithIntrinsicBounds(et.getCompoundDrawables()[0],
                                null,
                                getResources().getDrawable(R.drawable.ic_check_green_24dp),
                                null);
                    }else{
                        et.setCompoundDrawablesWithIntrinsicBounds(et.getCompoundDrawables()[0],
                                null,
                                null,
                                null);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }
    }

    private void updateBtnFinishLoading(final CircularProgressButton btn, final boolean success) {

        if(success){
            btn.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(getResources(), R.drawable.check_icon));
        }else{
            btn.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(getResources(), R.drawable.error_icon));
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.revertAnimation(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        if(!success){
                            btn.setText(R.string.try_again);
                            btn.setTypeface(Typeface.createFromAsset(getAssets(), "jua_regular.ttf"));
                        }
                    }
                });
            }
        }, 1500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mRegUserImage.setImageURI(result.getUri());

                try {

                    mUserImageFile = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToFile(new File(result.getUri().getPath()));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
