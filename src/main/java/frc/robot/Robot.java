// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.ArrayList;
import java.util.List;

import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
* The VM is configured to automatically run this class, and to call the functions corresponding to
* each mode, as described in the TimedRobot documentation. If you change the name of this class or
* the package after creating this project, you must also update the build.gradle file in the
* project.
*/
public class Robot extends TimedRobot {

    private final double kFieldLength = Units.inchesToMeters(8.5);
    private final double kFieldWidth = Units.inchesToMeters(11);
    private final Field2d field = new Field2d();

    private final String cameraName = "mmal_service_16.1";
    private PhotonCamera camera;
    private NetworkTableInstance instance;

    private final Timer timer = new Timer();
    // when we revealed the tag
    private double revealTime = -1;
    // when our photon result last updated
    private double updateTime = -1;
    // average latency
    private LinearFilter actualFilter = LinearFilter.movingAverage(5);
    private LinearFilter reportedFilter = LinearFilter.movingAverage(5);

    @Override
    public void robotInit() {
        instance = NetworkTableInstance.getDefault();
        instance.stopServer();
        instance.startClient("photonvision.local");
        
        camera = new PhotonCamera(instance, cameraName);
        
        SmartDashboard.putData(field);
        field.setRobotPose(kFieldLength/2.0, kFieldWidth/2.0, new Rotation2d());

        instance.addEntryListener(
            instance.getTable("photonvision").getSubTable(cameraName).getEntry("rawBytes"),
            (notif)->{
                updateTime = Timer.getFPGATimestamp();
            },
            EntryListenerFlags.kUpdate
        );
        timer.start();
    }
    
    @Override
    public void robotPeriodic() {
        PhotonPipelineResult result = camera.getLatestResult();
        if(result.hasTargets() && revealTime > 0) {
            double latency = (updateTime - revealTime)*1000;
            SmartDashboard.putNumber("LatencyMs", latency);
            SmartDashboard.putNumber("Reported LatencyMs", result.getLatencyMillis());
            SmartDashboard.putNumber("Average LatencyMs", actualFilter.calculate(latency));
            SmartDashboard.putNumber("Reported Average LatencyMs", reportedFilter.calculate(result.getLatencyMillis()));

            // restart cycle
            field.setRobotPose(kFieldLength/2.0, kFieldWidth/2.0, new Rotation2d());
            revealTime = -1;
            timer.reset();
        }

        // Reveal april tag
        if(timer.get() > 1){
            if(revealTime < 0) revealTime = Timer.getFPGATimestamp();
            field.setRobotPose(-100, -100, new Rotation2d());
        }
    }
    
    @Override
    public void autonomousInit() {}
    
    @Override
    public void autonomousPeriodic() {}
    
    @Override
    public void teleopInit() {}
    
    @Override
    public void teleopPeriodic() {}
    
    @Override
    public void disabledInit() {}
    
    @Override
    public void disabledPeriodic() {}
    
    @Override
    public void testInit() {}
    
    @Override
    public void testPeriodic() {}
    
    @Override
    public void simulationInit() {}
    
    @Override
    public void simulationPeriodic() {}
}
