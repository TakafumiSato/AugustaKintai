/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import data.EditInputTable;
import data.KbnData;
import data.KintaiData;
import data.KintaiKey;
import data.UserData;
import database.DBController;
import database.EditBeanDataAccess;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import util.Log;
import util.MathKintai;
import util.Utility;


/**
 *
 * @author 佐藤孝史
 */
@ManagedBean
@ViewScoped
public class EditBean {
    
    @ManagedProperty(value="#{kintaiKey}")
    private KintaiKey kintaiKey;
    @ManagedProperty(value="#{userData}")
    private UserData userData;
    @ManagedProperty(value="#{kbnData}")
    private KbnData kbnData;
    @ManagedProperty(value="#{editInputTable}")
    private EditInputTable editInputTable;
    
    // データベースへのアクセスクラス
    private EditBeanDataAccess editBeanDA = null;
    
    // 勤怠データ
    private KintaiData kintaiData = null;
    
    // 入力ロックフラグ
    private boolean disabled = false;
    // 編集画面遷移時に持っている勤怠データの区分コード
    private int baseKbnCd = 0;
    // 有休フラグ
    private double yukyuAddDay = 0;
    
    // ログ生成
    private static final Logger LOG = Log.getLog();
    
    

    /**
     * Creates a new instance of EditBean
     */
    public EditBean() {
    }
    
    /*
    init
    初期化
    */
    @PostConstruct
    public void init() {
        editBeanDA = new EditBeanDataAccess();
        
        try {
            // 勤怠データ初期化
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
    勤怠データの初期化
    */
    private void initKintaiData() throws SQLException, NamingException {
        
        // 勤怠データを初期化
        kintaiData = new KintaiData(kintaiKey.getYm(), kintaiKey.getDay(), 0, kbnData.getKbnList().get(0));
        
        Connection connection = null;
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // 勤怠データの初期値をユーザーの勤務パターンに合わせる
            editBeanDA.getWorkPatternData(connection, userData.getWorkptn_cd(), kintaiData);
            kintaiData.setKbnCd(kbnData.getKbnList().indexOf("出勤"));
            kintaiData.setKbnName("出勤");
            
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
    勤怠データの読込
    */
    private void readKintaiData() throws SQLException, NamingException {
        
        Connection connection = null;
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // キーが一致する勤怠データを読込
            editBeanDA.getAttendanceData(connection, kintaiKey.getYm(), kintaiKey.getUserId(), kintaiKey.getDay(), kintaiData);
            
            baseKbnCd = kintaiData.getKbnCd();
            
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
    entry
    戻り値：String
    入力された値をデータベースに登録し、勤怠画面へとページ遷移
    */
    public String entry() throws SQLException, NamingException {
        
        Connection connection = null;
        
        try {
            // データベース接続
            connection = DBController.open();
            
            // 入力値を勤怠データに書込
            kintaiDataDisabled();
            kintaiDataCalculation();
            
            editBeanDA.setYukyuData(connection, Utility.unionYearMonth(kintaiData.getYm(), kintaiData.getDay()), userData, kbnData.getKbnList().get(kintaiData.getKbnCd()));
            
            editBeanDA.setAttendanceData(connection, kintaiData, userData);
            
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
        
        return "kintai.xhtml?faces-redirect=true";
    }
    
    /*
    kintaiDataCalculation
    総労働時間、残業時間、実労働時間、遅刻、早退を算出
    */
    private void kintaiDataCalculation() {
        
        String kbnName = kbnData.getKbnList().get(kintaiData.getKbnCd());
        
        // すべての入力値が0になる場合（夏季休暇、冬季休暇、代休、欠勤）
        if (kbnName.equals("夏季休暇") ||
                kbnName.equals("冬季休暇") ||
                kbnName.equals("代休") ||
                kbnName.equals("欠勤") ||
                kbnName.equals("休日"))
            kintaiData.setData();
        
        // 区分が休日の場合nullを設定
//        if (kbnName.equals("休日")) {
//            kintaiData.setData();
//        }
        
        // nullチェック
        if (kintaiData.getStart() != null &&
                kintaiData.getEnd() != null &&
                kintaiData.getRest() != null) {
            
            // 総労働時間算出
            kintaiData.setTotal(MathKintai.resultTotal(kintaiData.getStart(), kintaiData.getEnd(), kintaiData.getRest()));
            // 残業時間算出
            if (!kbnName.equals("休日出勤"))
                kintaiData.setOver(MathKintai.resultOver(kintaiData.getStart(), kintaiData.getEnd(), kintaiData.getRest()));
            else
                kintaiData.setOver(kintaiData.getTotal());
            // 実労働時間算出
            kintaiData.setReal(MathKintai.resultReal(kintaiData.getStart(), kintaiData.getEnd(), kintaiData.getStart_default(), kintaiData.getEnd_default(), kintaiData.getRest(), kbnData.getKbnList().get(kintaiData.getKbnCd())));
        }
        
        // nullチェック
        if (kintaiData.getStart() != null &&
                kintaiData.getStart_default()!= null)
        {
            if (kbnData.getKbnList().get(kintaiData.getKbnCd()).equals("午前有休"))
                kintaiData.setStart_default(Time.valueOf(kintaiData.getStart_default().toLocalTime().plusHours(4).toString()+":00"));
            // 遅刻算出
            kintaiData.setLate(MathKintai.resultLate(kintaiData.getStart(), kintaiData.getStart_default()));
        }
        
        // nullチェック
        if (kintaiData.getEnd() != null &&
                kintaiData.getEnd_default()!= null)
        {
            if (kbnData.getKbnList().get(kintaiData.getKbnCd()).equals("午後有休"))
                kintaiData.setEnd_default(Time.valueOf(kintaiData.getEnd_default().toLocalTime().minusHours(4).toString()+":00"));
            // 早退算出
            kintaiData.setLeave(MathKintai.resultLeave(kintaiData.getEnd(), kintaiData.getEnd_default()));
        }
    }
    
    /*
    kintaiDataDisabled
    入力ロックの場合の値設定
    */
    private void kintaiDataDisabled() {
        
        // 入力ロックフラグ
        if (disabled) {
            
            // 出退勤、休憩、残業、遅刻早退を0に
            kintaiData.setStart(kintaiData.getStart_default());
            kintaiData.setEnd(kintaiData.getEnd_default());
            kintaiData.setRest(new Time(Time.valueOf("01:00:00").getTime()));
            
            // 有休でない場合は総労働、実労働も0に
            if (!kbnData.getKbnList().get(kintaiData.getKbnCd()).equals("有休")){
                kintaiData.setStart(null);
                kintaiData.setEnd(null);
                kintaiData.setTotal(new Time(Time.valueOf("00:00:00").getTime()));
                kintaiData.setRest(new Time(Time.valueOf("00:00:00").getTime()));
                kintaiData.setOver(new Time(Time.valueOf("00:00:00").getTime()));
                kintaiData.setReal(new Time(Time.valueOf("00:00:00").getTime()));
                kintaiData.setLate(new Time(Time.valueOf("00:00:00").getTime()));
                kintaiData.setLeave(new Time(Time.valueOf("00:00:00").getTime()));
            }
        }
    }
    
    /*
    goKintaiPage
    戻り値：String
    勤怠ページ画面遷移
    */
    public String goKintaiPage() {
        return "kintai.xhtml?faces-redirect=true";
    }
    

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public void setKbnData(KbnData kbnData) {
        this.kbnData = kbnData;
    }
    
    public void setKintaiKey(KintaiKey kintaiKey) {
        this.kintaiKey = kintaiKey;
    }

    public KintaiData getKintaiData() {
        return kintaiData;
    }

    public void setKintaiData(KintaiData kintaiData) {
        this.kintaiData = kintaiData;
    }
    
    
    /********************** Viewが参照するメソッド ********************/
    public void setViewKbn(int kbnCd) {
        
        // 有休の加算減算
        yukyuAddDay = MathKintai.resultSumYukyu(kbnData, baseKbnCd, kbnCd);
        
        kintaiData.setKbnCd(kbnCd);
    }
    
    public void setViewStart(String start) {
        kintaiData.setStart(Time.valueOf(start));
    }
    
    public void setViewEnd(String end) {
        kintaiData.setEnd(Time.valueOf(end));
    }
    
    public void setViewRest(String rest) {
        kintaiData.setRest(Time.valueOf(rest));
    }
    
    public void setViewRemarks(String remarks) {
        kintaiData.setRemarks(remarks);
    }
    
    public int getViewYM() {
        return kintaiData.getYm();
    }
    
    public int getViewDay() {
        return kintaiData.getDay();
    }
    
    public int getViewKbn() {
        return kintaiData.getKbnCd();
    }
    
    public String getViewStart() {
        
        if (kintaiData.getStart() != null)
            return kintaiData.getStart().toString();
        else
            return kintaiData.getStart_default().toString();
    }
    
    public String getViewEnd() {
        
        if (kintaiData.getEnd() != null)
            return kintaiData.getEnd().toString();
        else
            return kintaiData.getEnd_default().toString();
    }
    
    public String getViewRest() {
        
        if (kintaiData.getRest() != null)
            return kintaiData.getRest().toString();
        else
            return new Time(Time.valueOf("01:00:00").getTime()).toString();
    }
    
    public String getViewRemarks() {
        
        if (kintaiData.getRemarks() != null)
            return kintaiData.getRemarks();
        else
            return "";
    }
    
    public ArrayList<SelectItem> getViewKbnTable() {
        
        return editInputTable.getKbnTable();
    }
    
    public ArrayList<SelectItem> getViewStartTable() {
        
        ArrayList<SelectItem> itemList = new ArrayList<SelectItem>();
        String sTime =  null;
        
        // 退勤時間がnullか nullならデフォルト時間を指定
        if (kintaiData.getEnd() != null)
            sTime =  kintaiData.getEnd().toLocalTime().toString() + ":00";
        else
            sTime = kintaiData.getEnd_default().toLocalTime().toString() + ":00";
        
        for (SelectItem item: editInputTable.getTimeTable()) {
            
            // テーブルの値と退勤時間が一致していれば抜ける（出勤の入力は退勤時間を超えない）
            if (item.getValue().equals(sTime)) {
                break;
            }
            
            // アイテムリストに追加
            itemList.add(new SelectItem(item.getValue(), item.getLabel()));
        }
        
        return itemList;
    }
    
    public ArrayList<SelectItem> getViewEndTable() {
        
        ArrayList<SelectItem> itemList = new ArrayList<SelectItem>();
        LocalTime startLocalTime = null;
        
        // 出勤時間がnullか nullならデフォルト時間を指定
        if (kintaiData.getStart() != null)
            startLocalTime = kintaiData.getStart().toLocalTime();
        else
            startLocalTime = kintaiData.getStart_default().toLocalTime();
        
        for (SelectItem item: editInputTable.getTimeTable()) {
            
            Time time = Time.valueOf(item.getValue().toString());
            
            // 出勤時間より後を設定（退勤の入力は出勤時間を必ず超える）
            if (time.toLocalTime().compareTo(startLocalTime) > 0) {
                itemList.add(new SelectItem(item.getValue(), item.getLabel()));
            }
        }
        
        return itemList;
    }
    
    public ArrayList<SelectItem> getViewRestTable() {
        
        ArrayList<SelectItem> itemList = new ArrayList<SelectItem>();
        
        LocalTime startLocalTime = null;
        LocalTime endLocalTime = null;
        LocalTime restLocalTime = null;
        
        // 出退勤時間がnullか nullならデフォルト時間を設定
        if (kintaiData.getStart() != null && kintaiData.getEnd() != null) {
            startLocalTime = kintaiData.getStart().toLocalTime();
            endLocalTime = kintaiData.getEnd().toLocalTime();
        } else {
            startLocalTime = kintaiData.getStart_default().toLocalTime();
            endLocalTime = kintaiData.getEnd_default().toLocalTime();
        }
        
        // 退勤時間から出勤時間を引いた値が設定可能休憩時間
        restLocalTime = endLocalTime.minusHours(startLocalTime.getHour());
        restLocalTime = restLocalTime.minusMinutes(startLocalTime.getMinute());
        
        for (SelectItem item: editInputTable.getTimeTable()) {
            
            Time time = Time.valueOf(item.getValue().toString());
            
            // 設定された休憩時間までのリスト作成
            if (time.toLocalTime().compareTo(restLocalTime) < 0) {
                itemList.add(new SelectItem(item.getValue(), item.getLabel()));
            } else {
                break;
            }
        }
        
        return itemList;
    }
    
    public EditInputTable getEditInputTable() {
        
        return editInputTable;
    }
    
    public void setEditInputTable(EditInputTable editInputTable) {
        
        this.editInputTable = editInputTable;
    }
    

    public boolean isDisabled() {
        
        String kbnName = kbnData.getKbnList().get(kintaiData.getKbnCd());
        
        // 区分をチェックして入力が必要な項目かを返す
        if (kbnName.equals("夏季休暇") ||
                kbnName.equals("冬季休暇") ||
                kbnName.equals("有休") ||
                kbnName.equals("代休") ||
                kbnName.equals("欠勤") ||
                kbnName.equals("休日")) {
            disabled = true;
        } else {
            disabled = false;
        }
        
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
}
