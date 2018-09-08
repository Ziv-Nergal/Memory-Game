package card_view_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private final int size = eCards.values().length;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return CardFragment.newInstance(position % size);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }
}
