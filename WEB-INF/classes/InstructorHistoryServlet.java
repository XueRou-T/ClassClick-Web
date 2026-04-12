import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/instructorhistory")
public class InstructorHistoryServlet extends HttpServlet {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Integer instructorId = (Integer) session.getAttribute("userId");

        StringBuilder setsHtml = new StringBuilder();

        try (Connection conn = getConnection()) {
            // Query all sets 
            PreparedStatement psSets = conn.prepareStatement(
                "SELECT DISTINCT qs.set_id, qs.set_name " +
                "FROM sessions s JOIN question_set qs ON s.set_id = qs.set_id " +
                "WHERE s.instructor_id=? ORDER BY qs.set_name"
            );
            psSets.setInt(1, instructorId);
            ResultSet rsSets = psSets.executeQuery();

            while (rsSets.next()) {
                int setId = rsSets.getInt("set_id");
                String setName = rsSets.getString("set_name");

                // Query sessions for this set
                StringBuilder sessionHtml = new StringBuilder();
                PreparedStatement psSessions = conn.prepareStatement(
                    "SELECT session_id FROM sessions WHERE instructor_id=? AND set_id=? ORDER BY start_time DESC"
                );
                psSessions.setInt(1, instructorId);
                psSessions.setInt(2, setId);
                ResultSet rsSessions = psSessions.executeQuery();

                while (rsSessions.next()) {
                    int sessionId = rsSessions.getInt("session_id");

                    sessionHtml.append("<div class='session-card'>")
                        .append("<p><strong>Session ").append(sessionId).append("</strong></p>")
                        .append("<form method='get' action='statistics'>")
                        .append("<input type='hidden' name='sessionId' value='").append(sessionId).append("'>")
                        .append("<input type='hidden' name='setId' value='").append(setId).append("'>")
                        .append("<input type='hidden' name='qIndex' value='0'>")
                        .append("<input type='hidden' name='origin' value='history'>") //pass origin flag
                        .append("<button type='submit' class='stat-btn'>View Statistics</button>")
                        .append("</form> </div>");
                }

                setsHtml.append("<div class='history-container- set-card'>")
                        .append("<h3>").append(setName).append("</h3>")
                        .append(sessionHtml.toString())
                        .append("</div>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error loading history");
            return;
        }

        String html = readHtml("/instructorhistory.html")
            .replace("<!-- SET_CARDS -->", setsHtml.toString());

        resp.setContentType("text/html");
        resp.getWriter().write(html);
    }

    private String readHtml(String path) throws IOException {
        InputStream is = getServletContext().getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Template not found: " + path);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}