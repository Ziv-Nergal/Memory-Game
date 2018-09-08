package online_utils;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import user_utils.User;

public class OnlineGameManager {

    public static DatabaseReference mGameRequestDatabaseRef;
    public static DatabaseReference mGameSessionDatabaseRef;

    private static String mCurrentState;

    public static void SetCurrentState(String currentState, String uid) {
        mGameSessionDatabaseRef.child(uid).child("status").setValue(currentState);
        mCurrentState = currentState;
    }

    public static void SetGameRequestListener(final GameRequestListener gameRequestListener, final String currentUserUid) {

        mCurrentState = GameRequestState.WAITING_FOR_APPROVAL;

        mGameSessionDatabaseRef.child(currentUserUid).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                switch (Objects.requireNonNull(dataSnapshot.getValue(String.class))) {
                    case GameRequestState.APPROVED:
                        mCurrentState = GameRequestState.APPROVED;
                        gameRequestListener.onApproved();
                        break;
                    case GameRequestState.DECLINED:
                        mCurrentState = GameRequestState.DECLINED;
                        gameRequestListener.onDeclined();
                        break;
                    case GameRequestState.NO_ANSWER:
                        mCurrentState = GameRequestState.NO_ANSWER;
                        gameRequestListener.onNoResponse();
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mCurrentState.equals(GameRequestState.WAITING_FOR_APPROVAL)) {
                    SetCurrentState(GameRequestState.NO_ANSWER, currentUserUid);
                }
                gameRequestListener.onNoResponse();
            }
        }, 20000);
    }

    public interface GameRequestListener{
        void onApproved();
        void onDeclined();
        void onNoResponse();
    }

    public static void AcceptGameRequest(final AcceptListener acceptListener, String uid){

        mGameSessionDatabaseRef.child(uid).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (Objects.equals(dataSnapshot.getValue(String.class), GameRequestState.WAITING_FOR_APPROVAL)) {
                    acceptListener.onAcceptOk();
                } else {
                    acceptListener.onTooLate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public interface  AcceptListener{
        void onAcceptOk();
        void onTooLate();
    }

    public static void SendGameRequest(FirebaseUser mCurrentUser, User mChosenFriend, int cardNumber, int cardResId){

        Map<String, Object> gameRequestMap = new HashMap<>();

        gameRequestMap.put("sender_id", mCurrentUser.getUid());
        gameRequestMap.put("sender_user_name", mCurrentUser.getDisplayName());
        gameRequestMap.put("card_number", String.valueOf(cardNumber));
        gameRequestMap.put("card_res_id", String.valueOf(cardResId));

        mGameRequestDatabaseRef.child(mChosenFriend.getUid()).push().setValue(gameRequestMap);

        Map<String, Object> gameSessionMap = new HashMap<>();

        gameSessionMap.put("status", GameRequestState.WAITING_FOR_APPROVAL);
        gameSessionMap.put("turn", mCurrentUser.getDisplayName());
        gameSessionMap.put("rival_id", mChosenFriend.getUid());
        gameSessionMap.put("rival_user_name", mChosenFriend.getName());
        gameSessionMap.put("ready_to_start", false);

        mGameSessionDatabaseRef.child(mCurrentUser.getUid()).updateChildren(gameSessionMap);
    }

    public interface GameRequestState {
        String APPROVED = "approved";
        String DECLINED = "declined";
        String NO_ANSWER = "no answer";
        String TERMINATED = "terminated";
        String IN_PROGRESS = "in progress";
        String WAITING_FOR_APPROVAL = "waiting for approval";
    }
}
