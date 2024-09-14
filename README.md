# PuzzleMaster

This project is an Android-based sliding puzzle game with various modes, including normal play, image-based play, and a "versus" mode where the player competes against an AI. It also features a login and registration system where users can manage their profile with a photo.

## Features

### 1. **Game Modes**
- **Quick Play (3x3 puzzle)**: The player can choose to use a solver (A* algorithm with Manhattan and Linear Conflict heuristics) to solve the puzzle.
  
- **Normal Play**: Play with different difficulties:
  - **Easy (2x2)**
  - **Medium (3x3)**
  - **Hard (4x4)**

  The 4x4 puzzle uses the Bidirectional A* algorithm, while the 2x2 and 3x3 puzzles use the standard A* algorithm.

- **Image-Based Puzzle**: Users can select an image from their gallery or take a picture and play the sliding puzzle with that image.

- **Versus Mode**: Compete against the AI solver. The AI uses the A* algorithm and completes the puzzle with varying speeds depending on the difficulty level:
  - **Easy**: Slow AI
  - **Medium**: Moderate AI
  - **Hard**: Fast AI

### 2. **User Login and Registration**
- Users can register with a username, email, password, and profile picture.
- Security questions are implemented for password recovery.
- Profile images can be selected from the gallery or taken with the camera.

### 3. **Game Statistics Dashboard**
- A dashboard with animated charts (powered by MPAndroidChart) that displays the player's performance:
  - **Moves per game**: Bar chart.
  - **Experience gained and lost**: Bar chart showing experience points.
  - **Game results (win/loss)**: Pie chart.
  - **Game difficulty distribution**: Pie chart showing the number of games played in each difficulty.

## Database Schema

The app uses SQLite for data persistence. The following schema represents the database structure:

```sql
-- Table: Usuarios
CREATE TABLE Usuarios (
    id integer NOT NULL PRIMARY KEY,
    nombre_usuario varchar(50) NOT NULL,
    correo varchar(50) NOT NULL,
    foto_perfil varchar(300) NOT NULL,
    pregunta_seguridad varchar(300) NOT NULL,
    respuesta_seguridad varchar(300) NOT NULL,
    fecha_creacion date NOT NULL,
    nivel integer NOT NULL,
    experiencia_acumulada integer NOT NULL
);

-- Table: Juegos
CREATE TABLE Juegos (
    cj integer NOT NULL PRIMARY KEY,
    dificultad varchar(50) NOT NULL,
    tipo_juego varchar(50) NOT NULL,
    cantidad_movimientos integer NOT NULL,
    resultado boolean NOT NULL,
    experiencia_ganada integer NOT NULL,
    experiencia_perdida integer NOT NULL,
    tiempo integer NOT NULL,
    fecha_juego date NOT NULL,
    isSolverUsed boolean NOT NULL,
    Usuarios_id integer NOT NULL,
    FOREIGN KEY (Usuarios_id) REFERENCES Usuarios (id)
);
```

![Database Diagram](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/puzzle_master_Physical_Export-2024-09-10_14-14.png)

### Game Modes & Algorithm Details
- **A* Algorithm**: Used in 2x2, 3x3, and 4x4 puzzles with Manhattan Distance and Linear Conflict heuristics.
- **Bidirectional A* (4x4 mode)**: In hard mode (4x4 puzzles), the Bidirectional A* algorithm is implemented for efficiency.

### Solver Details
- The **A* algorithm** with the **Manhattan Distance** and **Linear Conflict** heuristics efficiently solves the puzzles in **Quick Play**, **Normal Play**, and **Versus Mode**.
- **Bidirectional A*** is used in the **4x4 mode** for faster solving in large puzzles.

## Project Setup

### Prerequisites
- [Android Studio](https://developer.android.com/studio) installed.
- Android SDK (API level 24 or higher).
- Internet connection for downloading dependencies.

### How to Run the App

1. **Clone the repository**:
   ```bash
   git clone https://github.com/MijaelMendoza/PuzzleMaster.git
   cd PuzzleMaster  
   ```

2. **Open in Android Studio**:
   - Launch Android Studio and select "Open an existing Android Studio project".
   - Navigate to the cloned project folder and open it.

3. **Build the project**:
   - Wait for Android Studio to sync the project and download all required dependencies.

4. **Run the project**:
   - Connect an Android device or start an emulator.
   - Press the `Run` button in Android Studio or use the `Shift + F10` shortcut.

### Important Libraries Used

- **MPAndroidChart**: Used for displaying game statistics in bar charts and pie charts.
  ```gradle
  implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
  ```

- **Glide**: For handling image loading (profile pictures).
  ```gradle
  implementation 'com.github.bumptech.glide:glide:4.12.0'
  annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
  ```

## User Features

### Login and Registration

The app includes a login and registration system where users can upload profile pictures and set security questions for password recovery.

- **Registration Fields**:
  - Username
  - Email
  - Password
  - Profile Picture
  - Security Question and Answer

- **Login Fields**:
  - Email
  - Password

### Game Screenshots

| Registration Screen         | Login Screen              | Home Screen        |
|-----------------------------|---------------------------|-------------------------|
| ![Registration](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/register.png) | ![Login](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/login.png) | ![Home](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/home.png) |

| Quick Play Mode         | Images Mode              | Versus Mode        |
|-----------------------------|---------------------------|-------------------------|
| ![QuickPLay](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/quickplay.png) | ![Images](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/images.png) | ![Versus](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/versus.png) |

| Normal Mode         | Profile              | Player Statistics        |
|-----------------------------|---------------------------|-------------------------|
| ![NormalMode](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/normal.png) | ![Profile](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/profile.png) | ![Statistics](https://raw.githubusercontent.com/MijaelMendoza/PuzzleMaster/main/BD/statistics.png) |


## Statistics Dashboard

The app provides a detailed statistics dashboard with animated charts that display:

- **Moves per Game**: A bar chart showing the number of moves made in each game.
- **Experience (Gained vs Lost)**: Bar chart for experience points earned and lost in each game.
- **Game Results**: A pie chart showing the number of games won or lost.
- **Game Difficulty Distribution**: Pie chart showing how many games were played at each difficulty level.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

