'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendGameRequest = functions.database.ref('/Game Requests/{user_id}/{notification_id}').onWrite((data, context) => {

    const user_id = context.params.user_id;
    const notification_id = context.params.notification_id;

    if (data.after.val()) { // Avoid sending notifications when deleting nodes directly from firebase.

        console.log(user_id, ' has a game request');

        var sender = admin.database().ref(`/Game Requests/${user_id}/${notification_id}`).once('value');

        return sender.then(senderResult => {

            const senderId = senderResult.val().sender_id;
            const senderUserName = senderResult.val().sender_user_name;
            const cardNumber = senderResult.val().card_number;
            const cardResId = senderResult.val().card_res_id;

            const currentUserDeviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once(`value`);

            return currentUserDeviceToken.then(tokenIdResult => {

                console.log('You Have A New Game Request From: ', senderUserName);

                const tokenId = tokenIdResult.val();

                var payload = {
                    notification: {
                        title: "New Game Request",
                        body: `${senderUserName} Wants to challenge you!`,
                        icon: "default",
                        sound: "game_request_sound_effect",
                        tag: `${senderUserName}`,
                        click_action: "ziv_nergal.Activities_TARGET_NOTIFICATION"
                    },
                    data: {
                        friend_id: senderId,
                        friend_user_name: senderUserName,
                        card_number: cardNumber,
                        card_res_id: cardResId
                    }
                };

                return admin.messaging().sendToDevice(tokenId, payload).then(response => {
                    return console.log('Notification sent successfuly!');
                });
            });
        });
    }
});