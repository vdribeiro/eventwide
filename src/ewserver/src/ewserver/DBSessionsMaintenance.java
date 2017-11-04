/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ewserver;

import java.util.TimerTask;

/**
 *
 * @author Diogo
 */
public class DBSessionsMaintenance extends TimerTask {

    @Override
    public void run() {
        EWServer.dbm.sessions.deleteOldSessions();
    }
    
}
