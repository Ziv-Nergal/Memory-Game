package card_view_pager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ziv_nergal.Activities.R;

public class CardFragment extends Fragment {

    public static CardFragment newInstance(int num) {
        CardFragment cardFragment = new CardFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("card",num);
        cardFragment.setArguments(bundle);
        return cardFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.card_fragment,container,false);

        assert getArguments() != null;
        eCards card = eCards.values()[getArguments().getInt("card")];

        ImageView imageView = view.findViewById(R.id.card_fragment_image_view);
        TextView textView = view.findViewById(R.id.fragment_text_view);

        switch (card) {
            case CARD1:
                imageView.setImageResource(R.drawable.card1);
                textView.setText(R.string.animals);
                break;
            case CARD2:
                imageView.setImageResource(R.drawable.card2);
                textView.setText(R.string.spongebob);
                break;
            case CARD3:
                imageView.setImageResource(R.drawable.card3);
                textView.setText(R.string.family_guy);
                break;
            case CARD4:
                imageView.setImageResource(R.drawable.card4);
                textView.setText(R.string.rick_morty);
                break;
            case CARD5:
                imageView.setImageResource(R.drawable.card5);
                textView.setText(R.string.the_simpsons);
                break;
            case CARD6:
                imageView.setImageResource(R.drawable.card6);
                textView.setText(R.string.southpark);
                break;
            case CARD7:
                imageView.setImageResource(R.drawable.card7);
                textView.setText(R.string.bojack_horseman);
                break;
        }

        return view;
    }
}
