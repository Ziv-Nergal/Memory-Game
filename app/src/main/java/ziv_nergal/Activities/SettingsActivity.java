package ziv_nergal.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import sound_and_graphics.MusicManager;
import game_utils.SettingsFragment;
import sound_and_graphics.SoundFxManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        SoundFxManager.PlaySoundFx(SoundFxManager.eSoundEffect.CLICK_FX, getApplicationContext());
        MusicManager.setMusicState(MusicManager.eMusicState.BETWEEN_ACTIVITIES);
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
