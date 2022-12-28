package com.swrobotics.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.HashMap;


public class StatusLogging extends SubsystemBase {
    private int queuelen = 0;
   public HashMap<Integer, RobotError> Statuses;

   /* Adds an Error To the Error Log Data Structure, and returns an id value that the throwing subsystem can retain,
    so it can later resolve it.
    * */
   public int AddError(int severity) {
       int queuepos = queuelen;
       queuelen++;
       Errors.put(queuepos, new RobotError(severity, queuepos));
       return queuepos;
   }


   // Resolves Error By Id, so that it will not cause issues with the light subsystem.
   public void ResolveError(int id) {
       Errors.remove(id);
   }




}

// This could not exist but I added it so that I can refactor later
class RobotError {
    public int severity;
    public int id;

    public RobotError(int severity, int id) {
        this.id = id;
        this.severity = severity;
    }
}


