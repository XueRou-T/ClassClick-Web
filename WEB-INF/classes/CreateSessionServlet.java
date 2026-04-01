import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/createsession")
public class CreateSessionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || !"instructor".equals(session.getAttribute("usertype"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int instructorId = (int) session.getAttribute("userId");

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?useSSL=false",
                "myuser", "xxxx")) {

            conn.setAutoCommit(false); 

            PreparedStatement update = conn.prepareStatement(
                "UPDATE sessions SET status='ended' WHERE instructor_id=? AND status='active'"
            );
            update.setInt(1, instructorId);
            update.executeUpdate();

            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO sessions (instructor_id, status, start_time) VALUES (?, 'active', NOW())",
                Statement.RETURN_GENERATED_KEYS
            );

            insert.setInt(1, instructorId);
            insert.executeUpdate();

            ResultSet rs = insert.getGeneratedKeys();

            if (rs.next()) {
                int sessionId = rs.getInt(1);
                session.setAttribute("sessionid", sessionId);
            }

            conn.commit(); 

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}