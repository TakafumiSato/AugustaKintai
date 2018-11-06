/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author 佐藤孝史
 */
public class Utility {
    
    public static Date conversionSQLDateToDate(java.sql.Date sqlDate) {
        java.util.Date utilDate = new java.util.Date();
        utilDate.setTime(sqlDate.getTime());
        
        return utilDate;
    }
    
    public static String conversionDateToString(java.util.Date date) {
        // DateからStringに変換
        String text = null;
        MyCalendar myCalendar = new MyCalendar();
        myCalendar.get().setTime(date);
        
        text = String.valueOf(myCalendar.get().get(Calendar.DAY_OF_MONTH));
        
        return text;
    }
    
    public static String conversionDayOfWeek(int ym, int day) {
        
        MyCalendar myCalendar = new MyCalendar();
        
        String dayOfWeek = null;
        if (String.valueOf(ym).length() > 5) {
            
            myCalendar.get().set(ym/100,(ym%100)-1,day);
        } else {
            
            myCalendar.get().set(ym/10,(ym%10)-1,day);
        }
        
        int dow = myCalendar.get().get(Calendar.DAY_OF_WEEK);
        
        switch(dow) {
            case Calendar.MONDAY:
                dayOfWeek = "月";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "火";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "水";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "木";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "金";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "土";
                break;
            case Calendar.SUNDAY:
                dayOfWeek = "日";
                break;
        }
        
        return dayOfWeek;
    }
    
    public static int unionInt(int x, int y) {
        String s = String.valueOf(x) + String.valueOf(y);
        
        return Integer.parseInt(s);
    }
    
    public static int unionYearMonth(int y, int m) {
        
        String s = null;
        
        if (m < 10)
            s = String.valueOf(y) + "0" + String.valueOf(m);
        else
            s = String.valueOf(y) + String.valueOf(m);
        
        return Integer.parseInt(s);
    }
    
    public static int unionYearMonthDay(int y, int m, int d) {
        
        String s = null;
        
        if (m < 10)
            s = String.valueOf(y) + "0" + String.valueOf(m);
        else
            s = String.valueOf(y) + String.valueOf(m);
        
        if (d < 10)
            s += "0" + String.valueOf(d);
        else
            s += String.valueOf(d);
        
        return Integer.parseInt(s);
    }
}
