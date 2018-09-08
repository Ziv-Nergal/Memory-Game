package ziv_nergal.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.podcopic.animationlib.library.AnimationType;
import com.podcopic.animationlib.library.StartSmartAnimation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import online_utils.OnlineGameManager;
import sound_and_graphics.GraphicsManager;
import sound_and_graphics.MusicManager;
import sound_and_graphics.SoundFxManager;

public class TwoPlayersActivity extends AppCompatActivity implements View.OnClickListener {

    private View mMainView;
    private View mAnimationView;
    private AnimationDrawable mAnimationDrawable;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mCurrentUser;

    private DatabaseReference mUsersDatabaseRef;
    private DatabaseReference mGameSessionDatabaseRef;
    private DatabaseReference mCurrentTurnDatabaseRef;

    private String mCurrentPlayerName;
    private String mFriendPlayerName;
    private TextView mCurrentPlayerPointsTV;
    private TextView mFriendPointsTV;
    private TextView mTurnWaitTV;

    private String mFriendUid;

    private Integer[] mCardsIdsArray;
    private int mCardResId;

    private TableLayout mCardsTableLayout;

    private ArrayList<View> mCardViewsArray;

    private ImageView mTurnArrow;
    private ImageView mFirstCard;
    private ImageView mSecondCard;

    private int mNumOfClicks;
    private int mCardsLeft;

    private int mCurrentPlayerPoints = 0;
    private int mFriendPoints = 0;

    private Handler mDelayHandler = new Handler();

    private boolean mIsOkToPlay = false;
    private boolean mExitToMainMenuPressed = false;
    private boolean mIsGameOver = false;

    private Vibrator mVibrator;
    private boolean mIsOkToVibrate;

    @Override
    public void onBackPressed() {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.leave_game_dialog_message)
                .setPositiveButton(R.string.positive_dialog_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                        exitToMainMenu();
                    }
                })
                .setNegativeButton(R.string.negative_dialog_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                        dialogInterface.dismiss();
                    }
                }).setCancelable(false).create().show();
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
        setContentView(R.layout.activity_two_players);

        if(mAuth.getCurrentUser() != null) {
            mCurrentUser = mAuth.getCurrentUser();
        }else{
            supportFinishAfterTransition();
        }

        startLoadingSplashScreenAnimation();

        initGameData();
        initUsersData();
        initCardViewsArray();

        setTurnChangeListener();
        setFriendMovesListener();
        setQuittersListener();

        listenForGameReady();
    }

    private void listenForGameReady() {

        mGameSessionDatabaseRef.child("ready_to_start").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((boolean) dataSnapshot.getValue()){
                    mAnimationDrawable.stop();
                    mAnimationView.setVisibility(View.GONE);
                    mMainView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void startLoadingSplashScreenAnimation() {

        mMainView = findViewById(R.id.dual_game_main_layout);
        mAnimationView = findViewById(R.id.dual_game_splash_screen);

        mAnimationView.setVisibility(View.VISIBLE);
        mMainView.setVisibility(View.INVISIBLE);

        mAnimationDrawable = (AnimationDrawable) mAnimationView.getBackground();

        mAnimationDrawable.setEnterFadeDuration(1500);
        mAnimationDrawable.setExitFadeDuration(1500);

        mAnimationDrawable.start();
    }

    public void initGameData() {

        mTurnArrow = findViewById(R.id.dual_game_turn_image_view);
        mCardsTableLayout = findViewById(R.id.dual_game_table_layout);
        mCurrentPlayerPointsTV = findViewById(R.id.dual_game_player1_points);
        mFriendPointsTV = findViewById(R.id.dual_game_player2_points);
        mTurnWaitTV = findViewById(R.id.dual_game_waiting_for_friend_turn_tv);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mIsOkToVibrate = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("pref_key_vibration", true);

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mGameSessionDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Game Sessions");
        mFriendUid = getIntent().getStringExtra("friend_id");
        mCardResId = Integer.parseInt(getIntent().getStringExtra("card_res_id"));
        mCardsLeft = getResources().getInteger(R.integer.num_of_cards);

        initCardIdsArray();

        if(getIntent().getStringExtra("starts_first") != null){ /* See who goes first (who sent the game request) */
            mGameSessionDatabaseRef = mGameSessionDatabaseRef.child(mCurrentUser.getUid());
            uploadCardArrangementToDatabase();
            mTurnArrow.setImageResource(R.drawable.ic_left_arrow_green_24dp);
        } else {
            mGameSessionDatabaseRef = mGameSessionDatabaseRef.child(mFriendUid);
            downloadCardArrangementFromDatabase();
            mTurnArrow.setImageResource(R.drawable.ic_right_arrow_red_24dp);
        }

        MusicManager.InitMediaPlayer(this, MusicManager
                .eSongTypes
                .values()[Integer.parseInt(getIntent().getStringExtra("card_number")) + 1]);

        GraphicsManager.SetCardsTableLayout(this, mCardsTableLayout, mCardResId);
        GraphicsManager.SetBackGround(mCardResId, findViewById(R.id.dual_game_main_layout));
    }

    private void initUsersData() {

        TextView currentPlayerTV = findViewById(R.id.dual_game_player1_name_tv);
        TextView friendPlayerTV = findViewById(R.id.dual_game_player2_name_tv);

        currentPlayerTV.setText(mCurrentUser.getDisplayName());
        friendPlayerTV.setText(getIntent().getStringExtra("friend_user_name"));

        mCurrentPlayerName = currentPlayerTV.getText().toString();
        mFriendPlayerName = friendPlayerTV.getText().toString();

        ((CircleImageView)findViewById(R.id.dual_game_player1_image)).setImageURI(mCurrentUser.getPhotoUrl());

        mUsersDatabaseRef.child(mFriendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Picasso.get()
                        .load(dataSnapshot.child("photoUrl").getValue(String.class))
                        .placeholder(R.drawable.blank_profile)
                        .into((CircleImageView)findViewById(R.id.dual_game_player2_image));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void initCardViewsArray() {

        mCardViewsArray = new ArrayList<>();

        for(int i = 0; i < mCardsTableLayout.getChildCount(); i++) {
            View view = mCardsTableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                for(int j = 0; j < ((TableRow) view).getChildCount(); j++){
                    View card = ((TableRow) view).getChildAt(j);
                    mCardViewsArray.add(card);
                }
            }
        }
    }

    private void initCardIdsArray() {

        int cardNumber = Integer.parseInt(getIntent().getStringExtra("card_number"));

        Integer[] cardIdsArray = MainActivity.getCardIdsArray(cardNumber, getResources());

        Collections.shuffle(Arrays.asList(cardIdsArray));

        mCardsIdsArray = cardIdsArray;
    }

    private void uploadCardArrangementToDatabase() {

        Map<String, String> cardsMap = new HashMap<>();

        for (int i = 0; i < mCardsIdsArray.length; i++) {
            cardsMap.put(String.valueOf(i), String.valueOf(mCardsIdsArray[i]));
        }

        mGameSessionDatabaseRef.child("cards_arrangement").setValue(cardsMap);
    }

    private void downloadCardArrangementFromDatabase() {

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGameSessionDatabaseRef.child("cards_arrangement").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        int i = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mCardsIdsArray[i++] = Integer.valueOf(snapshot.getValue(String.class));
                        }

                        mGameSessionDatabaseRef.child("ready_to_start").setValue(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        }, 6000); // To make sure cards were uploaded
    }

    private void setTurnChangeListener() {

        mCurrentTurnDatabaseRef = mGameSessionDatabaseRef.child("turn");

        mCurrentTurnDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                String currentTurn = dataSnapshot.getValue(String.class);

                if (currentTurn != null) {

                    StartSmartAnimation.startAnimation(mTurnArrow, AnimationType.FlipInY, 800, 0, true);

                    if(currentTurn.equals(mCurrentPlayerName)) {

                        StartSmartAnimation.startAnimation(mTurnWaitTV, AnimationType.FadeOut, 500, 0, true);
                        mTurnArrow.setImageResource(R.drawable.ic_left_arrow_green_24dp);
                        mIsOkToPlay = true;

                    }else {

                        StartSmartAnimation.startAnimation(mTurnWaitTV, AnimationType.FadeIn, 500, 0, true);
                        mTurnArrow.setImageResource(R.drawable.ic_right_arrow_red_24dp);
                        mIsOkToPlay = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setFriendMovesListener(){

        mGameSessionDatabaseRef.child("current_move").setValue(null);

        mGameSessionDatabaseRef.child("current_move").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {

                    String playerName = dataSnapshot.child("player_name").getValue(String.class);

                    if (playerName != null && !playerName.equals(mCurrentPlayerName)) {

                        final int firstCard = Integer.valueOf(dataSnapshot.child("first_card").getValue(String.class));
                        final int secondCard = Integer.valueOf(dataSnapshot.child("second_card").getValue(String.class));
                        final boolean match = (boolean) dataSnapshot.child("match").getValue();

                        showFriendMove(firstCard, secondCard, match);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setQuittersListener() {

        mGameSessionDatabaseRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(Objects.equals(dataSnapshot.getValue(String.class), OnlineGameManager.GameRequestState.TERMINATED)){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(TwoPlayersActivity.this);
                    builder.setMessage("Your friend left the game,\nWhat a looser!\nYou win!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    exitToMainMenu();
                                }
                            }).setCancelable(false).create();

                    mDelayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!mExitToMainMenuPressed && !mIsGameOver) {
                                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.WIN_FX, getApplicationContext());
                                builder.show();
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mUsersDatabaseRef.child(mFriendUid).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(boolean) dataSnapshot.getValue()){
                    mGameSessionDatabaseRef.child("status").setValue("terminated");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override // Card clicks
    public void onClick(View view) {

        if(mIsOkToPlay) {

            ImageView cardImageView = (ImageView) view;
            int cardNumber = Integer.parseInt(cardImageView.getTag().toString());

            GraphicsManager.RevealCard(cardImageView, mCardsIdsArray[cardNumber]);
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CARD_FLIP_FX, this);

            if (mNumOfClicks++ % 2 == 0) {
                mFirstCard = cardImageView;
            } else {
                mSecondCard = cardImageView;

                mIsOkToPlay = false;

                final boolean isMatch = checkForMatch();

                uploadMoveToDatabase(isMatch);

                mDelayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(isMatch){
                            mIsOkToPlay = true;

                            mCardsLeft -= 2;
                            updatePoints();

                            GraphicsManager.ConcealCards(mFirstCard, mSecondCard);
                            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.MATCH_FX, getApplicationContext());
                        }else {
                            mCurrentTurnDatabaseRef.setValue(mFriendPlayerName);
                            GraphicsManager.UnRevealCards(mFirstCard, mSecondCard, mCardResId);
                        }
                    }
                }, 1100);
            }
        } else {
            StartSmartAnimation.startAnimation(mTurnWaitTV, AnimationType.Shake, 400, 0, false);
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.ERROR_FX, this);
        }
    }

    private void uploadMoveToDatabase(boolean isMatch) {

        Map<String, Object> moveMap = new HashMap<>();

        moveMap.put("player_name", mCurrentPlayerName);
        moveMap.put("first_card", mFirstCard.getTag().toString());
        moveMap.put("second_card", mSecondCard.getTag().toString());
        moveMap.put("match", isMatch);

        mGameSessionDatabaseRef.child("current_move").updateChildren(moveMap);
    }

    private boolean checkForMatch() {

        boolean isMatch = false;

        Bitmap cardBitmap1 = ((BitmapDrawable) mFirstCard.getDrawable()).getBitmap();
        Bitmap cardBitmap2 = ((BitmapDrawable) mSecondCard.getDrawable()).getBitmap();

        if(cardBitmap1 == cardBitmap2) {
            isMatch = true;
        }

        return isMatch;
    }

    private void showFriendMove(int firstCard, int secondCard, final boolean match) {

        mIsOkToPlay = false;

        final ImageView firstCardImageView = (ImageView) mCardViewsArray.get(firstCard);
        final ImageView secondCardImageView = (ImageView) mCardViewsArray.get(secondCard);

        GraphicsManager.RevealCard(firstCardImageView, mCardsIdsArray[firstCard]);
        GraphicsManager.RevealCard(secondCardImageView, mCardsIdsArray[secondCard]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mVibrator != null && mIsOkToVibrate) {
                mVibrator.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }else{
            //deprecated in API 26
            if (mVibrator != null && mIsOkToVibrate) {
                mVibrator.vibrate(100);
            }
        }

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(match){

                    mCardsLeft -= 2;

                    GraphicsManager.ConcealCards(firstCardImageView, secondCardImageView);
                    SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.MATCH_FX, getApplicationContext());
                    updatePoints();
                }else {
                    GraphicsManager.UnRevealCards(firstCardImageView, secondCardImageView, mCardResId);
                    mIsOkToPlay = true;
                }
            }
        }, 1100);
    }

    private void updatePoints() {

        if(mIsOkToPlay){
            mCurrentPlayerPointsTV.setText(String.valueOf(++mCurrentPlayerPoints));
        }else{
            mFriendPointsTV.setText(String.valueOf(++mFriendPoints));
        }

        if(mCardsLeft == 0){
            gameOver();
        }
    }

    private void gameOver() {

        mIsGameOver = true;

        String msg;

        if(mCurrentPlayerPoints == mFriendPoints){
            msg = getString(R.string.tie);
        }else if(mCurrentPlayerPoints > mFriendPoints){
            msg = "Congratulations!\nYou won!";
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.WIN_FX, this);
        }else {
            msg = "You lost!\nBetter luck next time...";
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.LOSE_FX, this);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                exitToMainMenu();
            }
        }).create().show();
    }

    public void LeaveGameBtnClick(View view) {
        onBackPressed();
    }

    private void exitToMainMenu() {
        mExitToMainMenuPressed = true;
        mGameSessionDatabaseRef.child("status").setValue("terminated");
        MusicManager.InitMediaPlayer(getApplication(), MusicManager.eSongTypes.MAIN_SONG);
        MusicManager.setMusicState(MusicManager.eMusicState.BETWEEN_ACTIVITIES);
        startActivity(new Intent(TwoPlayersActivity.this, MainActivity.class));
        finish();
    }
}