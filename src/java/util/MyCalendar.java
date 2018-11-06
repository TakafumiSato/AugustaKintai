/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author 佐藤孝史
 */
@ManagedBean
@SessionScoped
public class MyCalendar {
    
    Calendar calendar = null;
    
    public MyCalendar() {
        
        calendar = new GregorianCalendar();
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        calendar.setTimeZone(tz);
    }
    
    public Calendar get() {
        
        return calendar;
    }
}
