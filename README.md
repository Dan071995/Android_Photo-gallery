# About the application:
This application simulates the work of a photo gallery. 
+ When you click on the "Make Photo" button, a file is created in the external personal storage of the application, the camera opens and the picture taken is saved to the file created on the disk.
+ Information about this file (his uri and name) is recorded in the Room database.
+ RecyclerView displays this file on the screen.

When creating several files, all of them will be written to the database and displayed on the screen in the form of tiles. When the application is restarted, all files previously added to the database will be displayed on the screen.

When you click on a file, the photo opens in full screen. The option to DELETE the current file appears. When a file is deleted, data about it is deleted from the database + the file itself is deleted from the disk!

# Technology stack:
Coroutines / Flows / MVVM / Room / SQLLite / RecyclerView / ResultAPI

# Screenshots:
![image](https://user-images.githubusercontent.com/104363713/231105716-abe08d33-f7a9-4f71-8f66-95a9fafde515.png)
