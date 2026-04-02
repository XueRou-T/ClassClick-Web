import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/createquestionset")
public class CreateQuestionSetServlet extends HttpServlet {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"instructor".equals(session.getAttribute("usertype"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        String setName = request.getParameter("setName");
        if (setName == null || setName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Set name is required");
            return;
        }

        int setId = 0;
        try (Connection conn = getConnection()) {
            PreparedStatement check = conn.prepareStatement(
                "SELECT set_id FROM question_set WHERE set_name = ?"
            );
            check.setString(1, setName.trim());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Set name already exists");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO question_set (set_name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, setName.trim());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) setId = keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error creating question set");
            return;
        }

        if (setId == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Failed to create question set");
            return;
        }

        // Success: return the new setId
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("SUCCESS:" + setId);
    }
}