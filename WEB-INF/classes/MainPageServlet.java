import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/mainpageservlet")
public class MainPageServlet extends HttpServlet {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"instructor".equals(session.getAttribute("usertype"))) {
            response.sendRedirect("login.html");
            return;
        }

        String displayName = (String) session.getAttribute("displayname");
        if (displayName == null) {
            response.sendRedirect("login.html");
            return;
        }

        int instructorId = (int) session.getAttribute("userId");
        String sessionId = "";
        StringBuilder setsHtml = new StringBuilder();

        try (Connection conn = getConnection()) {
            // Get active session for this instructor
            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT session_id FROM sessions WHERE instructor_id=? AND status='active' ORDER BY start_time DESC LIMIT 1");
            ps1.setInt(1, instructorId);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                sessionId = String.valueOf(rs1.getInt("session_id"));
            }

            // Get question sets
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT set_id, set_name FROM question_set ORDER BY set_id");
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                int sid = rs2.getInt("set_id");
                String sname = rs2.getString("set_name");

                setsHtml.append("<div class='set-card'>")
                        .append("<form method='get' action='displaysession'>")
                        .append("<input type='hidden' name='setId' value='").append(sid).append("'>")
                        .append("<button type='submit'>").append(sname).append("</button>")
                        .append("</form>")
                        .append("</div>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String html = readHtml("/mainpage.html");
        html = html.replace("<!-- DISPLAY_NAME -->", displayName != null ? displayName : "")
                   .replace("<!-- SESSION_ID -->", sessionId != null ? sessionId : "")
                   .replace("<!-- QUESTION_SETS -->", setsHtml.toString());

        response.setContentType("text/html");
        response.getWriter().write(html);
    }

    private String readHtml(String path) throws IOException {
        InputStream is = getServletContext().getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Template not found: " + path);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }
}