package com.swrobotics.robot.subsystems;

import com.swrobotics.robot.Robot;
import com.swrobotics.robot.RobotContainer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class StatusLogging extends SubsystemBase {
    private int queuelen = 0;
   public HashMap<Integer, RobotStatus> Statuses;


   @Override
   public void periodic() {
       int worstsev = getWorstSeverity();


    }
   /* Adds an Error To the Error Log Data Structure, and returns an id value that the throwing subsystem can retain,
    so it can later resolve it.
    * */
   public int AddStatus(int severity) {
       int queuepos = queuelen;
       queuelen++;
       Statuses.put(queuepos, new RobotStatus(severity, queuepos));
       return queuepos;
   }


   // Resolves Error By Id, so that it will not cause issues with the light subsystem.
   public void ResolveStatus(int id) {
       Statuses.remove(id);
   }

   public int getWorstSeverity() {
       AtomicInteger worst = new AtomicInteger(10);

       Statuses.forEach((key, value) -> {
           int valsev = value.severity;
           if (valsev < worst.get()) {
               worst.set(valsev);
           }
       });

       return worst.get();

   }



}

// This could not exist but I added it so that I can refactor later
class RobotStatus {
    public int severity;
    public int id;

    public RobotStatus(int severity, int id) {
        this.id = id;
        this.severity = severity;
    }
}


