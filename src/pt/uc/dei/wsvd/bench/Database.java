/** ****************************************************************************
 *
 * Department of Informatics Engineering
 * Faculty of Sciences and Technology
 * University of Coimbra
 *
 *
 * Nuno Manuel dos Santos Antunes
 *
 *
 *******************************************************************************
 * Last changed on : $Date: 2011-10-27 18:37:26 +0100 (qui, 27 Out 2011) $
 * Last changed by : $Author: nmsa $
 ***************************************************************************** */
package pt.uc.dei.wsvd.bench;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a simple
 *
 * @since r334
 * @version $Revision: 1436 $
 *
 * @author $Author: nmsa $
 */
public class Database {

    private static final Logger logger = Logger.getLogger(Database.class.getName());
    public static final int CIVS_DATABASE_MAX_ROWS = 1000;
    private static final int DATABASE_CONNECTION_POOL_SIZE = 10;
    private static final int DATABASE_CONNECTION_MAX_USAGE = 100;
    //
    private static final String server = "soa-sut-db.dei.uc.pt";
    private static final int port = 1521;
    private static final String sid = "orcl";
    private static final String userName = "wsdbench";
    private static final String passwd = "Samsung";
    private static final String driverName = "oracle.jdbc.driver.OracleDriver";
    private static final String url = "jdbc:oracle:thin:@" + server + ":" + port + ":" + sid;
    //
    private final static BlockingQueue<Connection> pooll;
    private final static ConcurrentHashMap<Connection, AtomicInteger> usage;

    static {
        logger.info("Setting UP Database ");
        pooll = new ArrayBlockingQueue<Connection>(DATABASE_CONNECTION_POOL_SIZE + 10);
        usage = new ConcurrentHashMap<Connection, AtomicInteger>(DATABASE_CONNECTION_POOL_SIZE + 10);
        boolean succes = false;
        try {
            Class.forName(driverName);
            for (; pooll.size() < DATABASE_CONNECTION_POOL_SIZE;) {
                Connection c = null;
                try {
                    c = DriverManager.getConnection(url, userName, passwd);
                    succes = true;
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"Nao consegue criar conn:  {} ", e);
                }
                if (c != null) {
                    try {
                        usage.put(c, new AtomicInteger(0));
                        pooll.put(c);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Cannot init Database:  {} ", e);
        }
        if (succes) {
            logger.info("Database UP!");
        } else {
            logger.info("Cannot set database UP!");
        }
    }

    public static Connection pickConnection() {
        while (true) {
            try {
                Connection con = pooll.take();
                if (con != null) {
                    if (!con.isClosed()) {
                        return con;
                    } else {
                        usage.remove(con);
                        Connection c = DriverManager.getConnection(url, userName, passwd);
                        usage.put(c, new AtomicInteger(0));
                        return c;
                    }
                }
            } catch (InterruptedException e) {
            } catch (SQLException se) {
            }
        }
    }

    public static void relaseConnection(Connection con) {
        if (con != null) {
            try {
                AtomicInteger get = usage.get(con);
                if (get == null) {
                    get = new AtomicInteger(0);
                }
                if (get.getAndIncrement() < DATABASE_CONNECTION_MAX_USAGE) {
                    try {
                        con.rollback();
                    } catch (SQLException e) {
                        logger.info("con.rollback()...." + e);
                    }
                    pooll.put(con);
                } else {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        logger.log(Level.SEVERE,"ex = {}", ex);
                    }
                    usage.remove(con);
                    Connection c = null;
                    try {
                        c = DriverManager.getConnection(url, userName, passwd);
                    } catch (SQLException e) {
                        logger.info("Nao consegue criar conn....");
                        logger.log(Level.SEVERE,"e = {}", e);
                    }
                    pooll.put(c);
                    usage.put(c, new AtomicInteger(0));
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE,"e = {}", e);
            }
        }
    }

    public static Statement createStatement(final Connection con) throws SQLException {
        Statement st = con.createStatement();
        st.setMaxRows(CIVS_DATABASE_MAX_ROWS);
        return st;
    }
}
