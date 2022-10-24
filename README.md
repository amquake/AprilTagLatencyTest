# AprilTagLatencyTest

* Update photonvision in dashboard to act as NT server
* Update project to your appropriate photonlib.json version
* Update `cameraName` in the project
* Simulate robot code GUI
* Display field2d widget (NetworkTables tab in top bar -> SmartDashboard -> Field)
* Change field2d size to 8.5 by 11 inches
* Update field2d background image with included png
* Change Robot Pose line width (right click Field widget title) to 9999
* Point camera at monitor

## Datalog
* Close simulation
* Click WPILib icon in VSCode (or do `Ctrl + P`)
* WPILib: Start Tool -> DataLogTool
* Open file -> latest `.wpilog` file (if nothing shows up after, do it again)
* Unselect `messages` and `systemTime`
* Export to CSV
