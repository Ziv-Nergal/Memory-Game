package sound_and_graphics;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import ziv_nergal.Activities.R;

public class SoundFxManager{

    private static final int MAX_STREAMS = 4;

    private static SoundPool mFxPlayer = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 100);

    private static int mClickFx;
    private static int mWinFx;
    private static int mLoseFx;
    private static int mCardFlipFx;
    private static int mMatchFx;
    private static int mGameRequestFx;
    private static int mErrorSoundFx;

    public static void InitManager(final Context context) {

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                mClickFx = mFxPlayer.load(context, R.raw.button_press_sound_effect, 1);
                mWinFx = mFxPlayer.load(context, R.raw.win_sound_effect, 1);
                mLoseFx = mFxPlayer.load(context, R.raw.lose_sound_effect, 1);
                mCardFlipFx = mFxPlayer.load(context, R.raw.card_flip_sound_effect, 1);
                mMatchFx = mFxPlayer.load(context, R.raw.match_sound_effect, 1);
                mGameRequestFx = mFxPlayer.load(context, R.raw.game_request_sound_effect, 1);
                mErrorSoundFx = mFxPlayer.load(context, R.raw.error_sound_effect, 1);

                return null;
            }
        }.execute();
    }

    public static void PlaySoundFx(eSoundEffect fx, Context context){

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_sound_fx", true)){
            switch (fx) {
                case CLICK_FX: mFxPlayer.play(mClickFx, 1, 1, 0, 0, 1);
                    break;
                case WIN_FX: mFxPlayer.play(mWinFx, 1, 1, 0, 0, 1);
                    break;
                case LOSE_FX: mFxPlayer.play(mLoseFx, 1, 1, 0, 0, 1);
                    break;
                case CARD_FLIP_FX: mFxPlayer.play(mCardFlipFx, 1, 1, 0, 0, 1);
                    break;
                case MATCH_FX: mFxPlayer.play(mMatchFx, 1, 1, 0, 0, 1);
                    break;
                case GAME_REQUEST_FX: mFxPlayer.play(mGameRequestFx, 1, 1, 0, 0, 1);
                    break;
                case ERROR_FX: mFxPlayer.play(mErrorSoundFx, 1, 1, 0, 0, 1);
                    break;
            }
        }
    }

    public enum eSoundEffect{
        CLICK_FX,
        WIN_FX,
        LOSE_FX,
        CARD_FLIP_FX,
        MATCH_FX,
        GAME_REQUEST_FX,
        ERROR_FX
    }
}
