/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author 佐藤孝史
 */
public class Utility {
    
    public static Date conversionSQLDateToDate(java.sql.Date date) {
        Date d = new Date();
        d.setTime(date.getTime());
        
        return d;
    }
    
    public static String conversionDateToString(Date date) {
        // DateからStringに変換
        String text = null;
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        
        text = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        
        c = null;
        
        return text;
    }
    
    public static String conversionDayOfWeek(int ym, int day) {
        
        String dayOfWeek = null;
        Calendar c = new GregorianCalendar(ym/100,(ym%100)-1,day);
        
        int dow = c.get(Calendar.DAY_OF_WEEK);
        
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
        
        c = null;
        
        return dayOfWeek;
    }
    
    public static int union(int x, int y) {
        String s = String.valueOf(x) + String.valueOf(y);
        
        return Integer.parseInt(s);
    }
}
