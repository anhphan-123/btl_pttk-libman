package libman.config;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class JdbcCleanupListener8 implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        AbandonedConnectionCleanupThread.checkedShutdown();
        ClassLoader applicationClassLoader = getClass().getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == applicationClassLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException exception) {
                    event.getServletContext().log("Không thể hủy đăng ký JDBC driver.", exception);
                }
            }
        }
    }
}
