package online_utils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import ziv_nergal.Activities.R;

public class ProfileImageManager {

    public static void Upload(File image, final FirebaseUser user) {

        user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(Uri.fromFile(image)).build());

        final StorageReference imageStorageRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child("Profile Images")
                .child(user.getUid() + ".jpg");

        UploadTask uploadTask = imageStorageRef.putFile(Uri.fromFile(image));

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                imageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Users")
                                .child(user.getUid());
                        userRef.child("photoUrl").setValue(uri.toString());
                    }
                });
            }
        });
    }

    public static void Download(DatabaseReference databaseReference, String uid, final View view){

        databaseReference.child(uid).child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != "default") {
                    Picasso.get()
                            .load(dataSnapshot.getValue(String.class)).placeholder(R.drawable.blank_profile)
                            .into((CircleImageView) view);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
