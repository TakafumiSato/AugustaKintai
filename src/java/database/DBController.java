package database;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import util.Log;

/**
 *
 * @author 佐藤孝史
 * 
 * データベースへのアクセスを管理
 */
public class DBController {
    
    private static Connection connection = null;
    private static final Logger LOG = Log.getLog();
    
    //private static Log log = new Log(DBController.class.getName(),"test.log");
    
    
    /*
    データベースをオープン
    */
    public static Connection open() throws SQLException, NamingException {
        
        try {
            LOG.log(Level.INFO, "DB接続");
                try{
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(DBController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                connection = DriverManager.getConnection("jdbc:mysql://augustakintai.cr8sxdwzy9ux.ap-northeast-1.rds.amazonaws.com:3306/augusta_kintai?useUnicode=true&characterEncoding=utf8", "dbuser", "password");
                //connection = DriverManager.getConnection("jdbc:mysql://augusta-kintai.cr8sxdwzy9ux.ap-northeast-1.rds.amazonaws.com:3306/augusta_kintai?useUnicode=true&characterEncoding=utf8", "admin", "augusta1234");
                //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/augustaKintai?useUnicode=true&characterEncoding=utf8", "TakafumiSato", "1234567");
                
        } catch (SQLException ex) {
            
            try {
                if (connection != null)
                    connection.close();
                connection = null;
            } catch(SQLException ex2) {
                ex2.printStackTrace();
                LOG.log(Level.SEVERE, "SQL例外です", ex2);
                throw new SQLException();
            }
            
            ex.printStackTrace();
            LOG.log(Level.SEVERE, "SQL例外です", ex);
            throw new SQLException();
        }
        
        return connection;
    }
}
