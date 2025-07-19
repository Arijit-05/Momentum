# Momentum 🔥

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

<table>
  <tr>
      <td><img src="https://github.com/user-attachments/assets/42594889-e533-494e-80ff-d08c403980d0" width="200"></td>
    <td><img src="https://github.com/user-attachments/assets/1acfcbc5-3031-4f0f-8cc9-3ee7399b1db9" width="200"></td>
    <td><img src="https://github.com/user-attachments/assets/11dba4f3-7665-4fd7-a1f9-00854b75146b" width="200"></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/8590fec3-1086-47be-8549-118be8dc8828" width="200"></td>
    <td><img src="https://github.com/user-attachments/assets/6f140309-54f0-4c9b-b9ad-803021a7a644" width="200"></td>
    <td><img src="https://github.com/user-attachments/assets/796214ad-85af-4e93-ac91-4fe87e0daee1" width="200"></td>
    
  </tr>
</table>

---
## More features will be added to Momentum+ soon

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

## Built with ❤️ by Arijit

## License

[MIT](LICENSE)
