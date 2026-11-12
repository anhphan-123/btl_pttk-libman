package libman.web;

import libman.config.DatabaseConfig8;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/health")
public class HealthCheckServlet8 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (Connection connection = DatabaseConfig8.getConnection()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"UP\",\"database\":\""
                    + escapeJson(connection.getCatalog()) + "\"}");
        } catch (SQLException exception) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"status\":\"DOWN\",\"message\":\"Không thể kết nối MySQL.\"}");
        }
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
