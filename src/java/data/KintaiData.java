/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.sql.Time;

/**
 *
 * @author 佐藤孝史
 */
public class KintaiData {
    
    private int ym = 0;
    private int day = 0;
    private Time start = null;
    private Time end = null;
    private Time start_default = null;
    private Time end_default = null;
    private Time rest = null;
    private Time rest_default = null;
    private Time total = null;
    private Time over = null;
    private Time real = null;
    private int kbn_cd = 0;
    private String kbnName = null;
    private int workPtn_cd = 0;
    private Time late = null;
    private Time leave = null;
    private String remarks = null;
    
    //private boolean dbFlag = false;

    
    public KintaiData(int ym, int day, int kbn_cd, String kbnName) {

        this.ym = ym;
        this.day = day;
        this.start = null;
        this.end = null;
        this.start_default = null;
        this.end_default = null;
        this.total = null;
        this.rest_default = null;
        this.rest = null;
        this.over = null;
        this.real = null;
        this.kbn_cd = kbn_cd;
        this.kbnName = kbnName;
        this.workPtn_cd = 1;
        this.late = null;
        this.leave = null;
        this.remarks = null;
    }
    
    public void setData(Time start, Time end, Time rest, Time total, Time over, Time real, int kbn_cd, String kbnName, int workPtn_cd, Time late, Time leave, String remarks) {
        this.start = start;
        this.end = end;
        this.total = total;
        this.rest = rest;
        this.over = over;
        this.real = real;
        this.kbn_cd = kbn_cd;
        this.kbnName = kbnName;
        this.workPtn_cd = workPtn_cd;
        this.late = late;
        this.leave = leave;
        this.remarks = remarks;
    }
    
    public void setData() {
        this.start = null;
        this.end = null;
        this.total = null;
        this.rest = null;
        this.over = null;
        this.real = null;
        this.late = null;
        this.leave = null;
    }

    
    public int getYm() {
        return ym;
    }

    public void setYm(int ym) {
        this.ym = ym;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getKbnCd() {
        return kbn_cd;
    }

    public void setKbnCd(int kbn_cd) {
        this.kbn_cd = kbn_cd;
    }

    public String getKbnName() {
        return kbnName;
    }

    public void setKbnName(String kbnName) {
        this.kbnName = kbnName;
    }
    
    public Time getStart() {
        return start;
    }

    public void setStart(Time start) {
        this.start = start;
    }
    
    public Time getEnd() {
        return end;
    }

    public void setEnd(Time end) {
        this.end = end;
    }

    public Time getStart_default() {
        return start_default;
    }

    public void setStart_default(Time start_default) {
        this.start_default = start_default;
    }

    public Time getEnd_default() {
        return end_default;
    }

    public void setEnd_default(Time end_default) {
        this.end_default = end_default;
    }

    public Time getRest_default() {
        return rest_default;
    }

    public void setRest_default(Time rest_default) {
        this.rest_default = rest_default;
    }

    public Time getTotal() {
        return total;
    }

    public void setTotal(Time total) {
        this.total = total;
    }

    public Time getRest() {
        return rest;
    }

    public void setRest(Time rest) {
        this.rest = rest;
    }

    public Time getOver() {
        return over;
    }

    public void setOver(Time over) {
        this.over = over;
    }

    public Time getReal() {
        return real;
    }

    public void setReal(Time real) {
        this.real = real;
    }

    public int getWorkPtn_cd() {
        return workPtn_cd;
    }

    public void setWorkPtn_cd(int workPtn_cd) {
        this.workPtn_cd = workPtn_cd;
    }

    public Time getLate() {
        return late;
    }

    public void setLate(Time late) {
        this.late = late;
    }

    public Time getLeave() {
        return leave;
    }

    public void setLeave(Time leave) {
        this.leave = leave;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
