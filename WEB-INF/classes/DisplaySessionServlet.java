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
    
        String html = readHtml("/displaysession.html");
                html = html.replace("<!-- SESSION_ID -->", sessionId)
                           .replace("<!-- SET_ID -->", setId != null ? setId : "");

                response.setContentType("text/html");
                response.getWriter().write(html);
            }

    
    private String readHtml(String path) throws IOException {
        InputStream is = getServletContext().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }
}

