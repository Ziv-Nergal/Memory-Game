package ziv_nergal.Activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.podcopic.animationlib.library.AnimationType;
import com.podcopic.animationlib.library.StartSmartAnimation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import sound_and_graphics.GraphicsManager;
import sound_and_graphics.MusicManager;
import sound_and_graphics.SoundFxManager;
import de.hdodenhof.circleimageview.CircleImageView;

public class SingleGameActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mCurrentUser;

    private TableLayout mCardsTableLayout;

    private Integer[] mCardsIdsArray;
    private int mCardResId;

    private ImageView mFirstCard;
    private ImageView mSecondCard;

    private int mNumOfClicks;
    private int mCardsLeft;

    private GameTimer mGameTimer;
    private long mGameTimeInMillis;

    private Handler mDelayHandler = new Handler();

    public static boolean mIsOkToPlay;

    @Override
    public void onBackPressed() {

        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, this);

        if(mGameTimer.IsRunning()) {
            mGameTimer.pause();
        }

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

                        if(mGameTimer.IsPaused()) {
                            mGameTimer.start();
                        }
                        dialogInterface.dismiss();
                    }
                }).setCancelable(false).create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mGameTimer.isPaused){
            mGameTimer.start();
        }

        MusicManager.Play(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mGameTimer.isRunning){
            mGameTimer.pause();
        }

        if(MusicManager.getMusicState() != MusicManager.eMusicState.BETWEEN_ACTIVITIES){
            MusicManager.Pause();
        }else {
            MusicManager.setMusicState(MusicManager.eMusicState.PLAYING);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_game);

        if(mAuth.getCurrentUser() != null){
            mCurrentUser = mAuth.getCurrentUser();
        }else{
            supportFinishAfterTransition();
        }

        initGameData();
        startNewGame();
    }

    private void initGameData(){

        mCardResId = getIntent().getIntExtra("card_res_id", -1);

        if(getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mCardsIdsArray = (Integer[]) bundle.getSerializable("card_id's_array");
        }

        mGameTimeInMillis = Long.parseLong(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("pref_key_single_player_time_limit", "60000"));

        ((CircleImageView)findViewById(R.id.single_game_player_image)).setImageURI(mCurrentUser.getPhotoUrl());
        ((TextView)findViewById(R.id.single_game_player_name_tv)).setText(mCurrentUser.getDisplayName());

        GraphicsManager.SetBackGround(mCardResId, findViewById(R.id.single_game_main_layout));

        MusicManager.InitMediaPlayer(this, MusicManager
                .eSongTypes
                .values()[getIntent().getIntExtra("song_number", 1) + 1]);

        mCardsTableLayout = findViewById(R.id.single_game_table_layout);
        GraphicsManager.SetCardsTableLayout(this, mCardsTableLayout , mCardResId);
    }

    private void startNewGame() {

        mIsOkToPlay = false;

        TextView readySetGoTV = findViewById(R.id.single_game_ready_set_go_tv);
        final LinearLayout timerLayout = findViewById(R.id.single_game_timer_layout);

        mFirstCard = null;
        mSecondCard = null;
        mCardsLeft  = getResources().getInteger(R.integer.num_of_cards);
        mNumOfClicks = 0;

        Collections.shuffle(Arrays.asList(mCardsIdsArray));

        mGameTimer = new GameTimer(mGameTimeInMillis, (TextView)findViewById(R.id.single_game_time_text_view));

        timerLayout.setVisibility(View.INVISIBLE);

        GraphicsManager.StartReadySetGoAnimation(readySetGoTV, new GraphicsManager.FinishRsgListener() {
            @Override
            public void onFinish() {
                mGameTimer.start();
                mIsOkToPlay = true;

                timerLayout.setVisibility(View.VISIBLE);
                StartSmartAnimation.startAnimation(timerLayout, AnimationType.BounceInLeft,
                        800, 0, false);
            }
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

                mDelayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForMatch();
                    }
                }, 1100);
            }
        } else {
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.ERROR_FX, this);
        }
    }

    private void checkForMatch() {

        Bitmap cardBitmap1 = ((BitmapDrawable) mFirstCard.getDrawable()).getBitmap();
        Bitmap cardBitmap2 = ((BitmapDrawable) mSecondCard.getDrawable()).getBitmap();

        if(cardBitmap1 == cardBitmap2){

            GraphicsManager.ConcealCards(mFirstCard, mSecondCard);
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.MATCH_FX, this);

            mCardsLeft -= 2;

            if(mCardsLeft == 0 && mGameTimer.IsRunning()){
                gameOver(true);
            }
        }else{
            GraphicsManager.UnRevealCards(mFirstCard, mSecondCard, mCardResId);
        }

        mIsOkToPlay = true;
    }

    private void gameOver(boolean win){

        mGameTimer.pause();

        String dialogTitleStr;

        if(win){
            dialogTitleStr = getString(R.string.game_over_dialog_win_title);
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.WIN_FX, this);
        }else{
            dialogTitleStr = getString(R.string.game_over_dialog_lose_title);
            SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.LOSE_FX, this);
        }

        AlertDialog.Builder gameOverDialog = new AlertDialog.Builder(this);

        gameOverDialog.setTitle(dialogTitleStr)
                .setMessage(R.string.game_over_dialog_msg)
                .setPositiveButton(R.string.game_over_dialog_pos_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                        GraphicsManager.ResetCards(mCardsTableLayout, mCardResId);
                        startNewGame();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.game_over_dialog_neg_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
                        exitToMainMenu();
                    }
                }).setCancelable(false).create().show();
    }

    public void LeaveGameBtnClick(View view) {
        onBackPressed();
    }

    private void exitToMainMenu() {
        MusicManager.InitMediaPlayer(getApplication(), MusicManager.eSongTypes.MAIN_SONG);
        MusicManager.setMusicState(MusicManager.eMusicState.BETWEEN_ACTIVITIES);
        mGameTimer.destroy();
        supportFinishAfterTransition();
    }

    public class GameTimer {

        private CountDownTimer countDownTimer;

        private long millisLeft;

        private TextView timeLeftTV;

        private boolean isRunning;
        private boolean isPaused;
        private boolean isDestroyed = false;

        private GameTimer(long millisLeft, TextView timeLeftTV) {
            this.millisLeft = millisLeft + 1000;
            this.timeLeftTV = timeLeftTV;
        }

        public void start(){

            if(!isDestroyed) {
                isPaused = false;
                isRunning = true;

                countDownTimer = new CountDownTimer(millisLeft, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        millisLeft = millisUntilFinished;
                        updateCountDownText();
                    }

                    @Override
                    public void onFinish() {
                        millisLeft = 0;
                        updateCountDownText();
                        isRunning = false;
                        gameOver(false);
                    }
                }.start();
            }
        }

        private void pause() {
            countDownTimer.cancel();
            isPaused = true;
        }

        private void updateCountDownText() {

            int minutes = (int) (millisLeft / 1000) / 60;
            int seconds = (int) (millisLeft / 1000) % 60;

            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

            timeLeftTV.setText(timeLeftFormatted);
        }

        private boolean IsRunning() {
            return isRunning;
        }

        private boolean IsPaused() {
            return isPaused;
        }

        private void destroy() {
            if(isRunning){
                countDownTimer.cancel();
            }

            isDestroyed = true;
        }
    }
}