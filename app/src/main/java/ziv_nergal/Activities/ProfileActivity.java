package ziv_nergal.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import sound_and_graphics.MusicManager;
import online_utils.ProfileImageManager;
import sound_and_graphics.SoundFxManager;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mCurrentUserDatabaseRef;
    private FirebaseUser mCurrentUser;

    private CircleImageView mProfilePic;

    private LinearLayout.LayoutParams mLayoutParams;

    @Override
    public void onBackPressed() {
        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
        MusicManager.setMusicState(MusicManager.eMusicState.BETWEEN_ACTIVITIES);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MusicManager.Play(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(MusicManager.getMusicState() != MusicManager.eMusicState.BETWEEN_ACTIVITIES){
            MusicManager.Pause();
        }else {
            MusicManager.setMusicState(MusicManager.eMusicState.PLAYING);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        if(mAuth.getCurrentUser() != null){
            mCurrentUser = mAuth.getCurrentUser();
        }

        mCurrentUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        loadUserDetails();

        mLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
    }

    private void loadUserDetails(){

        mProfilePic = findViewById(R.id.profile_user_image);

        if(mCurrentUser.getPhotoUrl() != null) {

            File file = new File(URI.create(mCurrentUser.getPhotoUrl().toString()).getPath());

            if (file.exists()) {
                mProfilePic.setImageURI(mCurrentUser.getPhotoUrl());
            } else {
                ProfileImageManager.Download(FirebaseDatabase.getInstance().getReference().child("Users"), mCurrentUser.getUid(), mProfilePic);
            }
        }

        TextView displayNameTV = findViewById(R.id.profile_display_name);
        displayNameTV.setText(mCurrentUser.getDisplayName());

        TextView email = findViewById(R.id.profile_email);
        email.setText(mCurrentUser.getEmail());

        final TextView nameTV = findViewById(R.id.profile_name);

        mCurrentUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameTV.setText(dataSnapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void ChangeNameBtnCLick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

        final EditText inputEditText = new EditText(this);
        inputEditText.setLayoutParams(mLayoutParams);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.change_name).setView(inputEditText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newName = inputEditText.getText().toString().trim();

                        if(!newName.isEmpty()){
                            mCurrentUserDatabaseRef.child("name").setValue(newName);
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    public void ChangeDisplayNameBtnClick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

        final EditText inputEditText = new EditText(this);
        inputEditText.setLayoutParams(mLayoutParams);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.new_display_name).setView(inputEditText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final String newDisplayName = inputEditText.getText().toString().trim();

                        if(!newDisplayName.isEmpty()){

                            mCurrentUserDatabaseRef.child("display_name").setValue(newDisplayName);

                            mCurrentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newDisplayName).build())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    ((TextView)findViewById(R.id.profile_display_name)).setText(newDisplayName);
                                }
                            });

                            dialogInterface.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    public void ChangeProfilePicBtnClick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1)
                .start(ProfileActivity.this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                this.mProfilePic.setImageURI(result.getUri());
                MainActivity.mProfilePic.setImageURI(result.getUri());

                try {

                    File userImageFile = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToFile(new File(result.getUri().getPath()));

                    ProfileImageManager.Upload(userImageFile, mCurrentUser);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void ProfilePicBtnClick(View view) {
        onBackPressed();
    }

    public void BackBtnClick(View view) {
        onBackPressed();
    }
}