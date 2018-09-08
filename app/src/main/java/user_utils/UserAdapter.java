package user_utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ziv_nergal.Activities.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private List<User> mUsersList;
    private UserClickListener mListener;
    private int mSelectedPosition = RecyclerView.NO_POSITION;

    public interface UserClickListener{
        void onClickListener(int position);
    }

    public void setListener(UserClickListener mListener) {
        this.mListener = mListener;
    }

    public UserAdapter(List<User> mUsersList) {
        this.mUsersList = mUsersList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.user_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder userViewHolder, int position) {

        userViewHolder.userName.setTextColor(mSelectedPosition == position ?
                userViewHolder.userName.getResources().getColor(R.color.green) : Color.BLACK);

        User user = mUsersList.get(position);

        if(user.getImage() != null){

            Picasso.get()
                    .load(user.getImage()).placeholder(R.drawable.blank_profile)
                    .into(userViewHolder.userImage);
        }else{
            userViewHolder.userImage.setImageResource(R.drawable.blank_profile);
        }

        userViewHolder.userName.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView userName;

        UserViewHolder(View itemView) {
            super(itemView);

            this.userImage = itemView.findViewById(R.id.user_cell_image);
            this.userName = itemView.findViewById(R.id.user_cell_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClickListener(getAdapterPosition());

                    notifyItemChanged(mSelectedPosition);
                    mSelectedPosition = getAdapterPosition();
                    notifyItemChanged(mSelectedPosition);
                }
            });
        }
    }
}
