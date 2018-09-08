package sound_and_graphics;

import android.content.Context;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

import ziv_nergal.Activities.R;

public class MusicManager {

    private static MediaPlayer mMediaPlayer;

    private static eMusicState mMusicState;

    private static int mCurrentSongId;

    private static float mLeftVolume = 0.5f;
    private static float mRightVolume = 0.5f;

    public static void InitMediaPlayer(Context context, eSongTypes songType){

        if(mMediaPlayer != null){
            mMediaPlayer.release();
        }

        getSongResId(songType);
        mMediaPlayer = MediaPlayer.create(context, mCurrentSongId);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setVolume(mLeftVolume, mRightVolume);
    }

    public static void Play(Context context){
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_music", true)){
            mMediaPlayer.start();

            if(mMusicState != eMusicState.BETWEEN_ACTIVITIES) {
                mMusicState = eMusicState.PLAYING;
            }
        }
    }

    public static void Pause(){
        if(mMusicState == eMusicState.PLAYING){
            mMediaPlayer.pause();
            mMusicState = eMusicState.PAUSED;
        }
    }

    public static void Resume(){
        mMediaPlayer.start();
        mMusicState = eMusicState.PLAYING;
    }

    public static void SetVolume(float volume){
        mLeftVolume = volume;
        mRightVolume = volume;

        if(getMusicState().equals(eMusicState.PLAYING)){
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    public static eMusicState getMusicState() {
        return mMusicState;
    }

    public static void setMusicState(eMusicState mMusicState) {
        MusicManager.mMusicState = mMusicState;
    }

    private static void getSongResId(eSongTypes songType) {

        switch (songType){
            default:
            case MAIN_SONG: mCurrentSongId = R.raw.main_song; break;
            case ANIMALS_SONG: mCurrentSongId = R.raw.animals_song; break;
            case SPONGEBOB: mCurrentSongId = R.raw.spongebob_song; break;
            case FAMILY_GUY: mCurrentSongId = R.raw.family_guy_song; break;
            case RICK_MORTY: mCurrentSongId = R.raw.rick_morty_song; break;
            case SIMPSONS: mCurrentSongId = R.raw.simpsons_song; break;
            case SOUTHPARK: mCurrentSongId = R.raw.southpark_song; break;
            case BOJACK_HORSEMAN: mCurrentSongId = R.raw.bojack_horseman_song; break;
        }
    }

    public enum eSongTypes{
        MAIN_SONG,
        ANIMALS_SONG,
        SPONGEBOB,
        FAMILY_GUY,
        RICK_MORTY,
        SIMPSONS,
        SOUTHPARK,
        BOJACK_HORSEMAN
    }

    public enum eMusicState{
        PLAYING,
        PAUSED,
        BETWEEN_ACTIVITIES,
    }
}
