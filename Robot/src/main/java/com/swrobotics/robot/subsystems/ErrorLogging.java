package com.swrobotics.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.HashMap;


public class ErrorLogging extends SubsystemBase {
    private int queuelen = 0;
   public HashMap<Integer, RobotError> Errors;

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
