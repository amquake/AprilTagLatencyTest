// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import org.photonvision.PhotonCamera;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.WPIUtilJNI;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
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
    private volatile long revealTime = -1;
    // when our photon result last updated
    private volatile long updateTime = -1;
    // average latency
    private LinearFilter actualFilter = LinearFilter.movingAverage(10);
    private LinearFilter reportedFilter = LinearFilter.movingAverage(10);

    DataLog log;
    DoubleLogEntry logActual;
    DoubleLogEntry logReport;
    DoubleLogEntry logActualAvg;
    DoubleLogEntry logReportAvg;

    @Override
    public void robotInit() {
        instance = NetworkTableInstance.getDefault();
        instance.stopServer();
        instance.startClient("photonvision.local");
        
        camera = new PhotonCamera(instance, cameraName);
        
        SmartDashboard.putData(field);
        field.setRobotPose(kFieldLength/2.0, kFieldWidth/2.0, new Rotation2d());

        instance.addEntryListener(
            instance.getTable("photonvision").getSubTable(cameraName).getEntry("hasTarget"),
            (notif)->{
                if(updateTime < 0) updateTime = WPIUtilJNI.now();
            },
            EntryListenerFlags.kUpdate
        );
        timer.start();

        DataLogManager.logNetworkTables(false);
        log = DataLogManager.getLog();
        logActual = new DoubleLogEntry(log, "actualMs");
        logReport = new DoubleLogEntry(log, "reportedMs");
        logActualAvg = new DoubleLogEntry(log, "avgActualMs");
        logReportAvg = new DoubleLogEntry(log, "avgReportedMs");
    }
    
    @Override
    public void robotPeriodic() {
        PhotonPipelineResult result = camera.getLatestResult();
        if(result.hasTargets() && revealTime > 0) {
            // log values
            double actualLatency = (updateTime - revealTime)*1.0e-3;
            SmartDashboard.putNumber("LatencyMs", actualLatency);
            logActual.append(actualLatency);

            double reportedLatency = result.getLatencyMillis();
            SmartDashboard.putNumber("Reported LatencyMs", reportedLatency);
            logReport.append(reportedLatency);

            double actualAvgLatency = actualFilter.calculate(actualLatency);
            SmartDashboard.putNumber("Average LatencyMs", actualAvgLatency);
            logActualAvg.append(actualAvgLatency);
            
            double reportedAvgLatency = reportedFilter.calculate(reportedLatency);
            SmartDashboard.putNumber("Reported Average LatencyMs", reportedAvgLatency);
            logReportAvg.append(reportedAvgLatency);

            // restart cycle
            field.setRobotPose(kFieldLength/2.0, kFieldWidth/2.0, new Rotation2d());
            revealTime = -1;
            timer.reset();
        }

        // Reveal april tag
        if(timer.get() > 0.5){
            if(revealTime < 0) {
                revealTime = WPIUtilJNI.now();
                updateTime = -1;
            }
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
