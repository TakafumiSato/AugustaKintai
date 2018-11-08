/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import data.KintaiData;
import data.UserData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Log;

/**
 *
 * @author 佐藤孝史
 */
public class DakokuBeanDataAccess {
    
    // ログ生成
    private static final Logger LOG = Log.getLog();
    
    
    public void getWorkPatternData(Connection connection, int workPtn_cd, KintaiData kintaiData) throws SQLException {
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            
            // userテーブルからデータを取得
            stmt = connection.prepareStatement("SELECT * FROM work_pattern WHERE ptn_cd = ?");
            stmt.setInt(1, workPtn_cd);
            rs = stmt.executeQuery();

            // パスワード一致していたらページ遷移
            if( rs.next() ) {
                
                kintaiData.setStart_default(rs.getTime("start_time"));
                kintaiData.setEnd_default(rs.getTime("end_time"));
                kintaiData.setRest_default(rs.getTime("rest_time"));
            }
        
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            throw new SQLException();
        } finally {
            
            // クローズ
            try {
                if (rs != null)
                    rs.close();
                rs = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "ResultSetクローズ失敗", ex);
                ex.printStackTrace();
            }
            
            try {
                if (stmt != null)
                    stmt.close();
                stmt = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Statementクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
    }
    
    public void getAttendanceData(Connection connection, int nowYearMonth, String user_id, int day, KintaiData data) throws SQLException {
    
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        
        try {
            
            // userテーブルからデータを取得
            stmt = connection.prepareStatement("SELECT * FROM attendance WHERE ym = ? AND user_id = ? AND day = ?");
            stmt.setInt(1, nowYearMonth);
            stmt.setString(2, user_id);
            stmt.setInt(3, day);
            rs = stmt.executeQuery();

            // 今まで登録されているデータを取得し設定
            if (rs.next()) {
                data.setData(
                                rs.getTime("start_time"), rs.getTime("end_time"), 
                                rs.getTime("rest_time"), rs.getTime("total_time"), rs.getTime("over_time"), 
                                rs.getTime("real_time"), rs.getInt("kbn_cd"), "",
                                rs.getInt("workptn_cd"), rs.getTime("late_time"), rs.getTime("leave_time"),
                                rs.getString("remarks"));
            }
        
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            ex.printStackTrace();
            throw new SQLException();
        } finally {
            
            // クローズ
            try {
                if (rs != null)
                    rs.close();
                rs = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "ResultSetクローズ失敗", ex);
                ex.printStackTrace();
            }
            
            try {
                if (stmt != null)
                    stmt.close();
                stmt = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Statementクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
    }
    
    public void setAttendanceData(Connection connection, KintaiData kintaiData, UserData userData) throws SQLException {
        
        PreparedStatement stmt = null;
        
        try {
            
            // attendanceテーブルに入力データをセット
            stmt = connection.prepareStatement("REPLACE INTO attendance (ym,user_id,day,start_time,end_time,rest_time,total_time,real_time,over_time,late_time,leave_time,remarks,kbn_cd,workptn_cd) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setInt(1, kintaiData.getYm());
            stmt.setString(2, userData.getId());
            stmt.setInt(3, kintaiData.getDay());
            stmt.setTime(4, kintaiData.getStart());
            stmt.setTime(5, kintaiData.getEnd());
            stmt.setTime(6, kintaiData.getRest());
            stmt.setTime(7, kintaiData.getTotal());
            stmt.setTime(8, kintaiData.getReal());
            stmt.setTime(9, kintaiData.getOver());
            stmt.setTime(10, kintaiData.getLate());
            stmt.setTime(11, kintaiData.getLeave());
            stmt.setString(12, kintaiData.getRemarks());
            stmt.setInt(13, kintaiData.getKbnCd());
            stmt.setInt(14, userData.getWorkptn_cd());
            
            stmt.executeQuery();
        
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            ex.printStackTrace();
            throw new SQLException();
        } finally {
            
            // クローズ
            
            try {
                if (stmt != null)
                    stmt.close();
                stmt = null;
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Statementクローズ失敗", ex);
                ex.printStackTrace();
            }
        }
    }
}
