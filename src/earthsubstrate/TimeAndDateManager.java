/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package earthsubstrate;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Christopher Brett
 */
public class TimeAndDateManager {
    
    protected static Calendar getCurrentTime(){
        Calendar currentTime = Calendar.getInstance();
        return currentTime; 
    }
    
    protected static String getFormattedTimeString(Calendar time){
        SimpleDateFormat parser = new SimpleDateFormat("h:mma MMM d, yyyy");
        return parser.format(time.getTime()).replaceAll("[.]", "");
    }
    
    protected static void printDateAndTime(){
        Calendar currentTime = Calendar.getInstance();
        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
        String date = parser.format(currentTime.getTime());
        System.out.println(date);
    }
}
