# Modern Dino Runner Game

A modern implementation of the classic Chrome Dino game with enhanced features, beautiful graphics, and engaging gameplay mechanics.

## Enhanced Features

- Smooth sprite animations for all characters
- Parallax scrolling backgrounds with three layers
- Multiple character types (Dino, Robot, Ninja) with unique animations
- Various obstacles (cactus, rocks, flying birds)
- Power-up system with different effects:
  - Shield (temporary invincibility)
  - Speed boost
  - Double points
- Dynamic difficulty scaling
- Particle effects
- Mobile-friendly touch controls
- Day/Night theme toggle
- Modern UI with score tracking
- Background music and sound effects

## Setup

1. Clone this repository or download the files
2. Create the following directory structure:
```
dino-game/
├── assets/
│   ├── audio/
│   │   ├── background-music.mp3
│   │   └── jump.mp3
│   └── images/
│       ├── characters/
│       │   ├── dino-run.png
│       │   ├── dino-jump.png
│       │   ├── dino-duck.png
│       │   ├── robot-run.png
│       │   ├── robot-jump.png
│       │   ├── robot-duck.png
│       │   ├── ninja-run.png
│       │   ├── ninja-jump.png
│       │   └── ninja-duck.png
│       ├── obstacles/
│       │   ├── cactus.png
│       │   ├── rock.png
│       │   └── bird.png
│       ├── powerups/
│       │   ├── shield.png
│       │   ├── speed.png
│       │   └── double-points.png
│       └── background/
│           ├── background-far.png
│           ├── background-mid.png
│           └── background-near.png
├── css/
│   └── style.css
├── js/
│   └── game.js
├── index.html
└── README.md
```

3. Add sprite sheets for characters:
   - Each character (dino, robot, ninja) needs three sprite sheets:
     - Running animation (6 frames)
     - Jumping animation (2 frames)
     - Ducking animation (2 frames)
   - Recommended size: 50x60 pixels per frame

4. Add obstacle sprites:
   - Cactus: 30x40 pixels
   - Rock: 40x30 pixels
   - Bird: 40x40 pixels with 2 animation frames

5. Add power-up sprites:
   - Shield: 30x30 pixels
   - Speed: 30x30 pixels
   - Double Points: 30x30 pixels

6. Add background layers:
   - Far background: Full width, parallax scrolling
   - Middle background: Full width, medium parallax scrolling
   - Near background: Full width, fast parallax scrolling

7. Add audio files:
   - `background-music.mp3`: Upbeat background music
   - `jump.mp3`: Quick jump sound effect

8. Open `index.html` in a modern web browser to play

## How to Play

- Press SPACE or tap upper screen to jump
- Press DOWN ARROW or tap lower screen to duck
- Collect power-ups for special abilities:
  - Shield: Temporary invincibility
  - Speed: Move faster
  - Double Points: Score multiplier
- Avoid obstacles:
  - Cacti: Jump over them
  - Rocks: Jump or duck
  - Birds: Time your jumps carefully
- Try to achieve the highest score

## Controls

- **SPACE / Upper Screen Tap**: Jump
- **DOWN ARROW / Lower Screen Tap**: Duck
- **Start Button**: Begin game
- **Theme Toggle**: Switch between day/night modes
- **Character Selection**: Choose your character

## Technical Details

- Built with vanilla JavaScript
- Uses HTML5 Canvas for rendering
- Sprite-based animations
- Parallax scrolling backgrounds
- Collision detection system
- Power-up management
- Mobile-responsive design
- Local storage for high score persistence

## Browser Support

Tested and working on:
- Chrome
- Firefox
- Safari
- Edge

## License

Feel free to use and modify this code for your own projects! 