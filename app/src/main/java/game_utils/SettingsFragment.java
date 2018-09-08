package game_utils;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import sound_and_graphics.MusicManager;
import ziv_nergal.Activities.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SwitchPreference musicSwitch = (SwitchPreference) getPreferenceManager()
                .findPreference("pref_key_music");

        if (musicSwitch != null) {
            musicSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if(!((boolean) newValue)){
                        MusicManager.Pause();
                    }else{
                        if(MusicManager.getMusicState().equals(MusicManager.eMusicState.PAUSED)) {
                            MusicManager.Resume();
                        }else{
                            MusicManager.InitMediaPlayer(getActivity(), MusicManager.eSongTypes.MAIN_SONG);
                        }
                    }

                    return true;
                }
            });
        }
    }
}
