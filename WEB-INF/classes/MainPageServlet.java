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
            "myuser", "xxxx"
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.html");
            return;
        }

        String displayName = (String) session.getAttribute("displayname");

        StringBuilder setsHtml = new StringBuilder();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT set_id, set_name FROM question_set ORDER BY set_id"
            );
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int setId = rs.getInt("set_id");
                String setName = rs.getString("set_name");

                setsHtml.append("<div class='set-card' ")
                        .append("onclick=\"location.href='editquestions?set_id=")
                        .append(setId).append("'\">")
                        .append(setName)
                        .append("</div>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Always show + button
        setsHtml.append("<button onclick=\"location.href='createset.html'\">+</button>");

        String html = readHtml("/mainpage.html");
        html = html.replace("<!-- DISPLAY_NAME -->", displayName)
                   .replace("<!-- SETS -->", setsHtml.toString());

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