# Game of Life: Remastered
My OOP university project, **Game of Life: Remastered** is a reimagined version of the game of *Life* originally invented by John Horton Conway.
For this project I mainly aim to expand the original ruleset and add new types of units, while still having the game behave **exactly as the original**, as long as only the original Cells are on the board.

The game supports all desktop systems (i hope).

### How To play
- Drag the mouse inside the window to move the view around the board
- Click on an empty square to place a live unit
- Click on a populated square to kill that unit
- Middle mouse button clears the board
- Right mouse button computes a turn
- Press space for autoplay (1/4 of a second per turn)

Right now the game runs on a hardcoded 200 rows by 300 columns grid, and the only unit type currently available is called Cell, which behaves like the original *Life* cells.

### How to build
Open the project using NetBeans and click the run button. I still don't know how to make it run normally as a JAR, so there are no releases at the moment.
You need to at least compile for Java 8 and the have org.json library installed.
