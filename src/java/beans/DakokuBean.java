/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import data.DakokuMessage;
import data.KbnData;
import data.KintaiData;
import data.UserData;
import database.DBController;
import database.DakokuBeanDataAccess;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.naming.NamingException;
import util.Log;
import util.MathKintai;
import util.MyCalendar;
import util.Utility;

/**
 *
 * @author 佐藤孝史
 */
@ManagedBean
@ViewScoped
public class DakokuBean {

    @ManagedProperty(value="#{userData}")
    private UserData userData;
    @ManagedProperty(value="#{kbnData}")
    private KbnData kbnData;
    @ManagedProperty(value="#{dakokuMessage}")
    private DakokuMessage dakokuMessage;
    
    private KintaiData kintaiData = null;
    
    private DakokuBeanDataAccess dakokuBeanDA = null;
    
    private MyCalendar myCalendar = null;
    
    // ログ生成
    private static final Logger LOG = Log.getLog();
    
    private boolean entry = false;
    private boolean entrySuccess = false;
    private boolean entryType = false;
    private Time entryTime = null;
    private String resultMessage = null;

    
    
    /************************ getter,setter ****************************/
    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public void setKbnData(KbnData kbnData) {
        this.kbnData = kbnData;
    }

    public void setDakokuMessage(DakokuMessage dakokuMessage) {
        this.dakokuMessage = dakokuMessage;
    }
    /*******************************************************************/
    
    /*
    init
    初期化
    */
    @PostConstruct
    public void init() {
        
        dakokuBeanDA = new DakokuBeanDataAccess();
        
        // 現在の時刻を保存
        myCalendar = new MyCalendar();
        entryTime = Time.valueOf(String.valueOf(myCalendar.get().get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(myCalendar.get().get(Calendar.MINUTE)) + ":00");
        
        // 出退勤ボタンのフラグをfalseに
        entry = false;
        
        
        try {
            // kintaiData初期化
            initKintaiData();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            ex.printStackTrace();
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, "Naming例外です", ex);
            ex.printStackTrace();
        }
        
        try {
            // 該当の勤怠データを取得
            readKintaiData();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            ex.printStackTrace();
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, "Naming例外です", ex);
            ex.printStackTrace();
        }
    }
    
    /*
    initKintaiData
    kintaiDataを初期化
    */
    private void initKintaiData() throws SQLException, NamingException {
        
        Connection connection = null;
        
        // 現在の年月日をkintaiDataにセット
        if (myCalendar.get().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || myCalendar.get().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            kintaiData = new KintaiData(Utility.unionYearMonth(myCalendar.get().get(Calendar.YEAR), myCalendar.get().get(Calendar.MONTH)+1), myCalendar.get().get(Calendar.DAY_OF_MONTH), kbnData.getKbnList().indexOf("休日"), "休日");
        else
            kintaiData = new KintaiData(Utility.unionYearMonth(myCalendar.get().get(Calendar.YEAR), myCalendar.get().get(Calendar.MONTH)+1), myCalendar.get().get(Calendar.DAY_OF_MONTH), 0, "");
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // 勤怠データの初期値をユーザーの勤務パターンに合わせる
            dakokuBeanDA.getWorkPatternData(connection, userData.getWorkptn_cd(), kintaiData);
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            ex.printStackTrace();
            throw new SQLException();
        } finally {
            try {
                if (connection != null)
                    connection.close();
                connection = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Connectionクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
    }
    
    /*
    readKintaiData
    データベースに登録される勤怠データを読込
    */
    private void readKintaiData() throws SQLException, NamingException {
        
        Connection connection = null;
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // 現在の年月、ユーザーID、日、でデータ検索
            dakokuBeanDA.getAttendanceData(connection, kintaiData.getYm(), this.userData.getId(), kintaiData.getDay(), kintaiData);
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            throw new SQLException();
        } finally {
            try {
                if (connection != null)
                    connection.close();
                connection = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Connectionクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
    }
    
    /*
    workStart
    出勤ボタンが押されたら、出勤時刻をデータベースに登録
    */
    public String workStartEntry() throws SQLException, NamingException {
        
        Connection connection = null;
        Time workStart = null;
        entrySuccess = false;
        entryType = false;
        entry = true;
        
        // すでに本日の出勤が打刻されている場合は以下を処理しない
        if (kintaiData.getStart() != null) {
            dakokuMessage.setResultMessage("本日はすでに出勤されています。", true);
            return null;
        }
        
        // 出勤時刻を調整
        workStart = adjustStartTime(entryTime);
        
        // 出勤時刻を設定
        kintaiData.setStart(workStart);
        kintaiData.setKbnCd(kbnData.getKbnList().indexOf("出勤"));
        kintaiData.setKbnName("出勤");
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // 出勤時刻を勤怠データに書込
            dakokuBeanDA.setAttendanceData(connection, kintaiData, userData);
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            throw new SQLException();
        } finally {
            try {
                if (connection != null)
                    connection.close();
                connection = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Connectionクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
        
        // 登録成功
        entrySuccess = true;
        LocalTime localTime = workStart.toLocalTime();
        dakokuMessage.setResultMessage(String.valueOf(localTime.getHour()) + ":" + String.valueOf(localTime.getMinute()) + "に出勤いたしました。", false);
        
        return "dakoku.xhtml?faces-redirect=true";
    }
    
    /*
    adjustStartTime
    出勤時刻を調整
    */
    private Time adjustStartTime(Time start) {
        
        // StringBuilderにセット
        StringBuilder sb = new StringBuilder(start.toString());
        // 出勤開始時刻の'分'をintとして抽出
        int mm = Integer.parseInt(sb.substring(3, 5));
        // 15で割って余り0なら修正なしで返す
        if (mm % 15 == 0)
            return Time.valueOf(sb.toString());
        // 分を15で割り整数部分の0～3の区分に分ける
        int caseNumber =mm / 15;
        
        // 上記で出した区分によって時間を繰り上げる
        switch (caseNumber) {
            // 01～14分なら15分に
            case 0:
                sb.replace(3, 5, "15");
                break;
            // 16～29分なら30分に
            case 1:
                sb.replace(3, 5, "30");
                break;
            // 31～44分なら45分に
            case 2:
                sb.replace(3, 5, "45");
                break;
            // 46～59分なら00分に
            case 3:
                sb.replace(3, 5, "00");
                // 時をひとつ繰り上げる
                sb.replace(0, 2, String.valueOf((Integer.parseInt(sb.substring(0, 2)) + 1)));
                break;
            default:
                break;
        }
        
        // 調整した出勤時刻を返す
        return Time.valueOf(sb.toString());
    }
    
    /*
    workStart
    退勤ボタンが押されたら、退勤時刻をデータベースに登録
    */
    public String workEndEntry() throws SQLException, NamingException {
        
        Connection connection = null;
        Time workEnd = null;
        entrySuccess = false;
        entryType = true;
        entry = true;
        
        // すでに本日の退勤が打刻されている場合は以下を処理しない
        if (kintaiData.getEnd() != null) {
            dakokuMessage.setResultMessage("本日はすでに退勤されています。", true);
            return null;
        }
        // 出勤が押される前に退勤は押せない
        if (kintaiData.getStart() == null) {
            dakokuMessage.setResultMessage("本日はまだ出勤されていません。", true);
            return null;
        }
        
        // 退勤時刻を調整
        workEnd = adjustEndTime(entryTime);
        
        // 退勤時刻設定
        kintaiData.setEnd(workEnd);
        kintaiData.setRest(Time.valueOf("01:00:00"));
        
        // 勤務計算
        kintaiData.setTotal(MathKintai.resultTotal(kintaiData.getStart(), kintaiData.getEnd(), kintaiData.getRest()));
        kintaiData.setOver(MathKintai.resultOver(kintaiData.getStart(), kintaiData.getEnd(), kintaiData.getRest()));
        kintaiData.setReal(MathKintai.resultReal(kintaiData.getStart(), kintaiData.getEnd(), kintaiData.getStart_default(), kintaiData.getEnd_default(), kintaiData.getRest(), kbnData.getKbnList().get(kintaiData.getKbnCd())));
        kintaiData.setLate(MathKintai.resultLate(kintaiData.getStart(), kintaiData.getStart_default()));
        kintaiData.setLeave(MathKintai.resultLeave(kintaiData.getEnd(), kintaiData.getEnd_default()));
        kintaiData.setRemarks("");
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // 入力値を勤怠データに書込
            dakokuBeanDA.setAttendanceData(connection, kintaiData, userData);
            
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            throw new SQLException();
        } finally {
            try {
                if (connection != null)
                    connection.close();
                connection = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Connectionクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
        
        // 登録成功
        entrySuccess = true;
        LocalTime localTime = workEnd.toLocalTime();
        dakokuMessage.setResultMessage(String.valueOf(localTime.getHour()) + ":" + String.valueOf(localTime.getMinute()) + "に退勤いたしました。", false);
        
        return "dakoku.xhtml?faces-redirect=true";
    }
    
    /*
    adjustStartTime
    出勤時刻を調整
    */
    private Time adjustEndTime(Time end) {
        
        // StringBuilderにセット
        StringBuilder sb = new StringBuilder(end.toString());
        // 出勤開始時刻の'分'をintとして抽出
        int mm = Integer.parseInt(sb.substring(3, 5));
        // 15で割って余り0なら修正なしで返す
        if (mm % 15 == 0)
            return Time.valueOf(sb.toString());
        // 分を15で割り整数部分の0～3の区分に分ける
        int caseNumber =mm / 15;
        
        // 上記で出した区分によって時間を繰り上げる
        switch (caseNumber) {
            // 01～14分なら15分に
            case 0:
                sb.replace(3, 5, "00");
                break;
            // 16～29分なら30分に
            case 1:
                sb.replace(3, 5, "15");
                break;
            // 31～44分なら45分に
            case 2:
                sb.replace(3, 5, "30");
                break;
            // 46～59分なら00分に
            case 3:
                sb.replace(3, 5, "45");
                break;
            default:
                break;
        }
        
        // 調整した出勤時刻を返す
        return Time.valueOf(sb.toString());
    }
    
    
    public String getViewEntryTime() {
        
        LocalTime localTime = entryTime.toLocalTime();
        
        return localTime.toString();
    }

    public boolean getViewResultMessageWarning() {
        
        return dakokuMessage.isWarning();
    }

    public String getViewResultMessage() {
        
        return dakokuMessage.getResultMessage();
    }
    
    
    public String goDakokuPage() {
        
        return "dakoku.xhtml?faces-redirect=true";
    }
    
    public String goKintaiPage() {
        
        return "kintai.xhtml?faces-redirect=true";
    }
}
