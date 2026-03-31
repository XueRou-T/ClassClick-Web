import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/displaysession")
public class DisplaySessionServlet extends HttpServlet {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String setId = request.getParameter("setId");
        HttpSession session = request.getSession(false);
        if (session == null || !"instructor".equals(session.getAttribute("usertype"))) {
            response.sendRedirect("login.html");
            return;
        }

        int instructorId = (int) session.getAttribute("userId");
        String sessionId = "";

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT session_id FROM sessions WHERE instructor_id=? AND status='active' ORDER BY start_time DESC LIMIT 1");
            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sessionId = String.valueOf(rs.getInt("session_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Inline HTML output
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Session Start</title>");
        out.println("<link rel=\"stylesheet\" href=\"styles.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Session ID</h2>");
        out.println("<div class='session-id'>" + sessionId + "</div>");
        out.println("<form method='get' action='displayquestion'>");
        out.println("<input type='hidden' name='setId' value='" + setId + "'>");
        out.println("<button type='submit' class='start-btn'>Start Quiz</button>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
}