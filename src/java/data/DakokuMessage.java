/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author 佐藤孝史
 */
@ManagedBean
@SessionScoped
public class DakokuMessage {
    private boolean warning = false;
    private String resultMessage = null;

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage, boolean warning) {
        this.resultMessage = resultMessage;
        this.warning = warning;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }
    
}
