package sound_and_graphics;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.podcopic.animationlib.library.AnimationType;
import com.podcopic.animationlib.library.StartSmartAnimation;

import ziv_nergal.Activities.R;

public class GraphicsManager {

    private static final long ANIM_DURATION = 500;
    private static final long ANIM_DELAY = 0;

    private static Handler mDelayHandler = new Handler();

    private static int mReadySetGoCounter;

    public static void SetCardsTableLayout(Context context, TableLayout tableLayout, int cardResId) {

        for(int i = 0; i < tableLayout.getChildCount(); i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                for(int j = 0; j < ((TableRow) view).getChildCount(); j++){

                    View card = ((TableRow) view).getChildAt(j);
                    ((ImageView) card).setImageResource(cardResId);
                    card.setVisibility(View.VISIBLE);
                    card.setOnClickListener((View.OnClickListener) context);
                }
            }
        }
    }

    public static void RevealCard(ImageView cardImageView, int destCardId){

        StartSmartAnimation.startAnimation(cardImageView,
                AnimationType.FlipInY, ANIM_DURATION, ANIM_DELAY, false);

        cardImageView.setImageResource(destCardId);
        cardImageView.setClickable(false);
    }

    public static void UnRevealCards(ImageView card1, ImageView card2, int destCardId){

        StartSmartAnimation.startAnimation(card1,
                AnimationType.FlipInY, ANIM_DURATION, ANIM_DELAY, false);
        card1.setImageResource(destCardId);
        card1.setClickable(true);

        StartSmartAnimation.startAnimation(card2,
                AnimationType.FlipInY, ANIM_DURATION, ANIM_DELAY, false);
        card2.setImageResource(destCardId);
        card2.setClickable(true);
    }

    public static void ConcealCards(final ImageView card1, final ImageView card2){

        StartSmartAnimation.startAnimation(card1,
                AnimationType.FadeOut, ANIM_DURATION, ANIM_DELAY, false);

        StartSmartAnimation.startAnimation(card2,
                AnimationType.FadeOut, ANIM_DURATION, ANIM_DELAY, false);

        mDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                card1.setVisibility(View.INVISIBLE);
                card2.setVisibility(View.INVISIBLE);
            }
        }, ANIM_DURATION);
    }

    public static void ResetCards(TableLayout tableLayout, int cardResId){

        for(int i = 0; i < tableLayout.getChildCount(); i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                for(int j = 0; j < ((TableRow) view).getChildCount(); j++){

                    View card = ((TableRow) view).getChildAt(j);

                    ((ImageView) card).setImageResource(cardResId);

                    if(card.getVisibility() == View.INVISIBLE) {
                        card.setVisibility(View.VISIBLE);
                        StartSmartAnimation.startAnimation(card,
                                AnimationType.FlipInY, ANIM_DURATION * 2, ANIM_DELAY, false);
                        ((ImageView) card).setImageResource(cardResId);
                        card.setClickable(true);
                    }
                }
            }
        }
    }

    public static void SetBackGround(int themeId, View backGroundLayout) {

        switch (themeId) {

            case R.drawable.card1:
                backGroundLayout.setBackgroundResource(R.drawable.main_background);
                break;
            case R.drawable.card2:
                backGroundLayout.setBackgroundResource(R.drawable.spongebob_background);
                break;
            case R.drawable.card3:
                backGroundLayout.setBackgroundResource(R.drawable.family_guy_background);
                break;
            case R.drawable.card4:
                backGroundLayout.setBackgroundResource(R.drawable.rick_morty_background);
                break;
            case R.drawable.card5:
                backGroundLayout.setBackgroundResource(R.drawable.simpsons_background);
                break;
            case R.drawable.card6:
                backGroundLayout.setBackgroundResource(R.drawable.southpark_background);
                break;
            case R.drawable.card7:
                backGroundLayout.setBackgroundResource(R.drawable.bojack_horseman_background);
                break;
        }
    }

    public interface FinishRsgListener {
        void onFinish();
    }

    public static void StartReadySetGoAnimation(final TextView rsgTextView, final FinishRsgListener onFinishListener){

        mReadySetGoCounter = 0;

        rsgTextView.setVisibility(View.VISIBLE);

        new CountDownTimer(3000, 900) {
            @Override
            public void onTick(long timeInMillis) {

                if(mReadySetGoCounter <= 2) {
                    StartSmartAnimation.startAnimation(rsgTextView, AnimationType.BounceIn,
                            1000, 0, true);
                }

                mDelayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (mReadySetGoCounter++){
                            case 0:
                                rsgTextView.setText(R.string.ready);
                                rsgTextView.setTextColor(Color.RED);
                                break;
                            case 1:
                                rsgTextView.setText(R.string.set);
                                rsgTextView.setTextColor(Color.YELLOW);
                                break;
                            case 2:
                                rsgTextView.setText(R.string.go);
                                rsgTextView.setTextColor(Color.BLUE);
                                break;
                        }
                    }
                }, 100);
            }

            @Override
            public void onFinish() {

                rsgTextView.setVisibility(View.INVISIBLE);
                onFinishListener.onFinish();
            }
        }.start();
    }

    public static void StartReadySetGoAnimation(final TextView rsgTextView){

        mReadySetGoCounter = 0;

        rsgTextView.setTextSize(30);

        new CountDownTimer(3000, 900) {
            @Override
            public void onTick(long timeInMillis) {

                switch (mReadySetGoCounter++){
                    case 0:
                        StartSmartAnimation.startAnimation(rsgTextView, AnimationType.BounceIn,
                                900, 0, true);
                        rsgTextView.setText(R.string.ready);
                        rsgTextView.setTextColor(Color.RED);
                        break;
                    case 1:
                        StartSmartAnimation.startAnimation(rsgTextView, AnimationType.BounceIn,
                                900, 0, true);
                        rsgTextView.setText(R.string.set);
                        rsgTextView.setTextColor(Color.YELLOW);
                        break;
                    case 2:
                        StartSmartAnimation.startAnimation(rsgTextView, AnimationType.BounceIn,
                                1000, 0, true);
                        rsgTextView.setText(R.string.go);
                        rsgTextView.setTextColor(Color.BLUE);
                        break;
                }
            }

            @Override
            public void onFinish() {

                mDelayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rsgTextView.setTextSize(18);
                        rsgTextView.setText(R.string.waiting_for_your_friends_move);
                        rsgTextView.setTextColor(Color.RED);
                        StartSmartAnimation.startAnimation(rsgTextView, AnimationType.FadeOut, 1, 0, true);
                    }
                }, 400);
            }
        }.start();
    }
}
