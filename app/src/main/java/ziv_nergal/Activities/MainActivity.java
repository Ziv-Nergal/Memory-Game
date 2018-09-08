package ziv_nergal.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import br.com.simplepass.loading_button_lib.interfaces.OnAnimationEndListener;
import card_view_pager.PagerAdapter;
import card_view_pager.ZoomOutPageTransformer;
import online_utils.OnlineGameManager;
import online_utils.ProfileImageManager;
import sound_and_graphics.MusicManager;
import sound_and_graphics.SoundFxManager;
import user_utils.User;
import user_utils.UserAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity{

    private ViewPager mCardViewPager;

    private List<User> mUserList = new ArrayList<>();
    private UserAdapter mUserAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUsersDatabaseRef;

    private User mChosenFriend;

    public static CircleImageView mProfilePic;

    private Handler mDelayHandler = new Handler();

    private BroadcastReceiver mGameRequestReceiver;

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this.getApplicationContext())
                .registerReceiver(mGameRequestReceiver, new IntentFilter("GameRequestAction"));

        MusicManager.Play(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mGameRequestReceiver);

        if(MusicManager.getMusicState() != MusicManager.eMusicState.BETWEEN_ACTIVITIES){
            MusicManager.Pause();
        }else {
            MusicManager.setMusicState(MusicManager.eMusicState.PLAYING);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }else {

            initializeFireBaseData();
            initUserData();
            setCardViewPager();
            listenToGameRequests();

            MusicManager.InitMediaPlayer(this, MusicManager.eSongTypes.MAIN_SONG);
            SoundFxManager.InitManager(this);

            /* Means that game was opened through game request notification! */
            if(getIntent().getStringExtra("friend_id") != null){
                createGameRequestDialog(getIntent());
            }
        }
    }

    private void initializeFireBaseData() {

        mCurrentUser = mAuth.getCurrentUser();

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        OnlineGameManager.mGameRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Game Requests");
        OnlineGameManager.mGameSessionDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Game Sessions");

        mUsersDatabaseRef.child(mCurrentUser.getUid()).child("online").setValue(true);
        mUsersDatabaseRef.child(mCurrentUser.getUid()).child("online").onDisconnect().setValue(false);
    }

    private void initUserData() {

        mProfilePic = findViewById(R.id.main_menu_profile_image_view);

        if(mCurrentUser.getPhotoUrl() != null) {

            File file = new File(URI.create(mCurrentUser.getPhotoUrl().toString()).getPath());

            if (file.exists()) {
                mProfilePic.setImageURI(mCurrentUser.getPhotoUrl());
            } else {
                ProfileImageManager.Download(mUsersDatabaseRef, mCurrentUser.getUid(), mProfilePic);
            }
        }

        ((TextView)findViewById(R.id.main_menu_user_name_text_view)).setText(mCurrentUser.getDisplayName());
    }

    private void setCardViewPager() {

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());

        mCardViewPager = findViewById(R.id.card_pager);
        mCardViewPager.setAdapter(adapter);
        mCardViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mCardViewPager.setCurrentItem(new Random().nextInt(getResources().getInteger(R.integer.num_of_themes)) + 1000);

        View.OnClickListener onArrowsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

                int tab = mCardViewPager.getCurrentItem();

                if (tab > 0) {
                    if(view.getTag().toString().equals("left")) {
                        tab++;
                    }else{
                        tab--;
                    }
                    mCardViewPager.setCurrentItem(tab);
                } else if (tab == 0) {
                    mCardViewPager.setCurrentItem(tab);
                }
            }
        };

        findViewById(R.id.swipe_left_image_btn).setOnClickListener(onArrowsClickListener);
        findViewById(R.id.swipe_right_image_btn).setOnClickListener(onArrowsClickListener);
    }

    private void listenToGameRequests() {
        mGameRequestReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                createGameRequestDialog(intent);
            }
        };
    }

    private void createGameRequestDialog(Intent intent) {

        final String friendUserId = intent.getStringExtra("friend_id");
        final String friendUserName = intent.getStringExtra("friend_user_name");
        final String cardNumber = intent.getStringExtra("card_number");
        final String cardResId = intent.getStringExtra("card_res_id");

        final View dialogCustomView = getLayoutInflater().inflate(R.layout.game_request_dialog, null);

        final Dialog dialog = getCustomDialog(dialogCustomView);

        ((TextView)dialogCustomView.findViewById(R.id.game_request_dialog_user_name_tv)).setText(friendUserName);

        ProfileImageManager.Download(mUsersDatabaseRef, friendUserId, dialogCustomView.findViewById(R.id.game_request_dialog_user_image));

        dialogCustomView.findViewById(R.id.game_request_dialog_accept_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

                OnlineGameManager.AcceptGameRequest(new OnlineGameManager.AcceptListener() {
                    @Override
                    public void onAcceptOk() {
                        OnlineGameManager.SetCurrentState(OnlineGameManager.GameRequestState.APPROVED, friendUserId);
                        startDualGame(friendUserId, friendUserName, cardNumber, cardResId, false);
                        dialog.cancel();
                    }

                    @Override
                    public void onTooLate() {
                        Toast.makeText(MainActivity.this, "Your Friend has cancelled the request!",
                                Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                }, friendUserId);
            }
        });

        dialogCustomView.findViewById(R.id.game_request_dialog_decline_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                OnlineGameManager.SetCurrentState(OnlineGameManager.GameRequestState.DECLINED, friendUserId);
            }
        });

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 15000);

        dialog.setCancelable(false);
        dialog.show();

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.GAME_REQUEST_FX, this);
    }

    public void OpenProfileClickBtn(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, this);
        MusicManager.setMusicState(MusicManager.eMusicState.BETWEEN_ACTIVITIES);

        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(MainActivity.this,
                        mProfilePic,
                        ViewCompat.getTransitionName(mProfilePic));
        startActivity(profileIntent, options.toBundle());
    }

    public void SinglePlayerBtnClick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

        int cardTypeNumber = mCardViewPager.getCurrentItem() % getResources().getInteger(R.integer.num_of_themes);
        int cardResId = getResources().obtainTypedArray(R.array.cards_array).getResourceId(cardTypeNumber, -1);

        Intent singleGameIntent = new Intent(MainActivity.this, SingleGameActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("card_id's_array", getCardIdsArray(cardTypeNumber, getResources()));
        singleGameIntent.putExtras(bundle);
        singleGameIntent.putExtra("card_res_id", cardResId);
        singleGameIntent.putExtra("song_number", cardTypeNumber);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(MainActivity.this,
                        mProfilePic, ViewCompat.getTransitionName(mProfilePic));
        startActivity(singleGameIntent, options.toBundle());
    }

    public void ChallengeFriendBtnClick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

        final View dialogView = getLayoutInflater().inflate(R.layout.who_is_online_dialog, null);

        dialogView.findViewById(R.id.online_dialog_layout).setVisibility(View.INVISIBLE);

        Dialog onlineFriendsDialog = createOnlineFriendsDialog(dialogView);

        loadOnlineUsers();
        creteUsersRecyclerView(dialogView);

        onlineFriendsDialog.show();

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogView.findViewById(R.id.online_dialog_progress_bar).setVisibility(View.GONE);
                dialogView.findViewById(R.id.online_dialog_layout).setVisibility(VISIBLE);
            }
        }, 3000);
    }

    private Dialog createOnlineFriendsDialog(View dialogView) {

        final Dialog dialog = getCustomDialog(dialogView);

        dialogView.findViewById(R.id.online_dialog_send_friend_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

                if(mChosenFriend != null) {

                    final CircularProgressButton loadingBtn = (CircularProgressButton)view;
                    loadingBtn.startAnimation();

                    final int cardNumber = mCardViewPager.getCurrentItem() % getResources().getInteger(R.integer.num_of_themes);
                    final int cardResId = getResources().obtainTypedArray(R.array.cards_array).getResourceId(cardNumber, -1);

                    OnlineGameManager.SendGameRequest(mCurrentUser, mChosenFriend, cardNumber, cardResId);

                    OnlineGameManager.SetGameRequestListener(new OnlineGameManager.GameRequestListener() {
                        @Override
                        public void onApproved() {
                            updateBtnFinishLoading(loadingBtn, true, "");
                            OnlineGameManager.SetCurrentState(OnlineGameManager.GameRequestState.IN_PROGRESS, mCurrentUser.getUid());
                            startDualGame(mChosenFriend.getUid(), mChosenFriend.getName(),
                                    String.valueOf(cardNumber), String.valueOf(cardResId), true);
                        }

                        @Override
                        public void onDeclined() {
                            updateBtnFinishLoading(loadingBtn, false, "User has declined your request");
                        }

                        @Override
                        public void onNoResponse() {
                            updateBtnFinishLoading(loadingBtn, false, "User not responding");
                        }
                    }, mCurrentUser.getUid());

                }else{
                    Toast.makeText(MainActivity.this, "You have to choose a friend to play with!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogView.findViewById(R.id.online_dialog_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                OnlineGameManager.SetCurrentState(OnlineGameManager.GameRequestState.TERMINATED, mCurrentUser.getUid());
                mChosenFriend = null;
            }
        });

        return dialog;
    }

    private Dialog getCustomDialog(View dialogView) {
        final Dialog dialog = new Dialog(this);

        if(dialog.getWindow() != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(dialogView);
            dialog.getWindow().setLayout((6 * getResources().getDisplayMetrics().widthPixels) / 7, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        }

        return dialog;
    }

    private void loadOnlineUsers() {

        mUsersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUserList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if(!Objects.equals(snapshot.child("email").getValue(String.class), mCurrentUser.getEmail())) {

                        User user = new User();

                        if (snapshot.child("photoUrl").getValue() != null) {
                            user.setImage(snapshot.child("photoUrl").getValue(String.class));
                        }

                        user.setUserId(snapshot.getKey());
                        user.setName(snapshot.child("display_name").getValue(String.class));
                        user.setEmail(snapshot.child("email").getValue(String.class));

                        mUserList.add(user);
                    }
                }

                mUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void creteUsersRecyclerView(View dialogView) {

        mUserAdapter = new UserAdapter(mUserList);

        RecyclerView usersRecyclerView;

        usersRecyclerView = dialogView.findViewById(R.id.users_list);
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        usersRecyclerView.setAdapter(mUserAdapter);

        mUserAdapter.setListener(new UserAdapter.UserClickListener() {
            @Override
            public void onClickListener(int position) {
                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                mChosenFriend = mUserList.get(position);
            }
        });
    }

    private void startDualGame(String friendId, String friendName, String cardNumber, String cardResId, boolean startFirst) {

        final Intent dualGameIntent = new Intent(MainActivity.this, TwoPlayersActivity.class);

        dualGameIntent.putExtra("friend_id", friendId);
        dualGameIntent.putExtra("friend_user_name", friendName);
        dualGameIntent.putExtra("card_number", cardNumber);
        dualGameIntent.putExtra("card_res_id", cardResId);

        if(startFirst) {
            dualGameIntent.putExtra("starts_first", "true");
        }

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(dualGameIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, 1000);
    }

    private void updateBtnFinishLoading(final CircularProgressButton btn, final boolean success, final String msg) {

        if(success){
            btn.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(getResources(), R.drawable.check_icon));
        }else{
            btn.doneLoadingAnimation(Color.TRANSPARENT, BitmapFactory.decodeResource(getResources(), R.drawable.error_icon));
        }

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.revertAnimation(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        if(!success){

                            String buttonText = msg + "... " + getString(R.string.try_again) + "?";

                            btn.setText(buttonText);
                            btn.setTypeface(Typeface.createFromAsset(getAssets(), "jua_regular.ttf"));
                            btn.setWidth(ViewPager.LayoutParams.WRAP_CONTENT);
                            btn.setHeight(ViewPager.LayoutParams.WRAP_CONTENT);
                        }
                    }
                });
            }
        }, 1000);
    }

    public void SettingsBtnClick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
        MusicManager.setMusicState(MusicManager.eMusicState.BETWEEN_ACTIVITIES);

        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static Integer[] getCardIdsArray(int cardNumber, Resources resources) {

        Integer[] cardIdsArray = new Integer[resources.getInteger(R.integer.num_of_cards)];

        TypedArray cards;

        switch (cardNumber){

            case 0: cards = resources.obtainTypedArray(R.array.animal_array); break;
            case 1: cards = resources.obtainTypedArray(R.array.spongebob_array); break;
            case 2: cards = resources.obtainTypedArray(R.array.family_guy_array); break;
            case 3: cards = resources.obtainTypedArray(R.array.rick_morty_array); break;
            case 4: cards = resources.obtainTypedArray(R.array.simpsons_array); break;
            case 5: cards = resources.obtainTypedArray(R.array.southpark_array); break;
            case 6: cards = resources.obtainTypedArray(R.array.bojack_horseman_array); break;

            default: cards = resources.obtainTypedArray(R.array.animal_array);
        }

        for(int i = 0; i < cardIdsArray.length; i++){
            cardIdsArray[i] = cards.getResourceId(i, -1);
        }

        cards.recycle();

        return cardIdsArray;
    }

    public void SignOutBtnClick(View view) {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.logout_dialog_msg)
                .setPositiveButton(R.string.positive_dialog_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mAuth.signOut();
                        mUsersDatabaseRef.child(mCurrentUser.getUid()).child("online").setValue(false);
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                })
                .setNegativeButton(R.string.negative_dialog_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setCancelable(false).create().show();
    }
}