# Memory-Game

This is a classic memory card game with a design of famous cartoons.
Each theme has it's own TV show characters and music.

![screenshot_20180909-112009_memory game](https://user-images.githubusercontent.com/42380097/45262748-a7fd8700-b425-11e8-9147-7dc2b347aaf3.jpg)
![screenshot_20180909-112110_memory game](https://user-images.githubusercontent.com/42380097/45262743-9320f380-b425-11e8-877d-054a83e42477.jpg)
![screenshot_20180909-112043_memory game](https://user-images.githubusercontent.com/42380097/45262758-bba8ed80-b425-11e8-88b5-db6ea34f463d.jpg)
![screenshot_20180909-160812_memory game](https://user-images.githubusercontent.com/42380097/45264848-a21a9c80-b44b-11e8-941c-ff4f22fc0641.jpg)

# Features

- Single Player mode
- Two player mode
- 7 Themes - Family Guy, The Simpsons, Rick&Morty, Bojack Horseman, Southpark, Spongebob, and an animals theme.
- Sounds effects when flipping a card, tapping buttons, winning, losing and more.
- Vibration when playing with a friend.
- Option to change game time (single game only).
- Settings menu that lets the user change music, sound, and vibration preferences. 
- signing up a user profile and uploading a photo

# Code

The main tool I used for this app is Google's Firebase for registering new users to the app, Saving their info,
And for the real-time online game session.
Also, I used Google's FCM (Firebase Cloud Messaging) with Node.js in order to send push notifications of game requests between devices when the app is in the background or in the foreground.
