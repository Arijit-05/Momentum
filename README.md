# Momentum

Momentum is a simple, beautiful habit-tracking app for Android. Build positive routines, track your progress, and stay motivated with streaks and reminders.

## Features

- **Habit Tracking:** Add, edit, and delete daily habits.
- **Calendar View:** Visualize your progress with a calendar.
- **Streak Widget:** Home screen widget to display your current streak.
- **Reminders:** Set daily reminders to keep you on track.
- **AI Assistant:** Get habit suggestions and motivation powered by AI (Mistral API).
- **Authentication:** Secure login and registration.
- **Customizable Settings:** Personalize your experience.
- **Offline Support:** Works without an internet connection.

## Screenshots

> _Add screenshots of your app here!_

## Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/momentum.git
   cd momentum
   ```

2. **Open in Android Studio:**
   - File > Open > Select the `Habits` folder.

3. **Set up Firebase:**
   - Add your `google-services.json` to `app/`.

4. **Build and run:**
   - Click the Run button in Android Studio or use:
     ```bash
     ./gradlew assembleDebug
     ```

## Technologies Used

- **Kotlin** (Android)
- **Firebase** (Authentication, Cloud, etc.)
- **Mistral API** (AI Assistant)
- **Material Design**
- **Lottie** (Animations)

## Project Structure

```
app/
  src/main/java/com/arijit/habits/
    ai/                # AI integration (Mistral)
    fragments/         # Login/Register UI
    models/            # Data models (Habit, CalendarDate)
    utils/             # Utilities (reminders, adapters, vibration, etc.)
    widgets/           # Home screen widgets
    MainActivity.kt    # Main app logic
    ...
  res/                 # Layouts, drawables, values, etc.
```

## Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## Credits

- App icon and graphics by [your name or source]
- AI powered by [Mistral](https://mistral.ai/)
- Built with ❤️ by [your name]

## License

[MIT](LICENSE) (or specify your license) 