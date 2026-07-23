import java.io.*;
import java.sql.*; // Added missing SQL imports
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/logoutservlet")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            Integer instructorId = (Integer) session.getAttribute("userId");

            if (instructorId != null) {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/clicker_db?useSSL=false&allowPublicKeyRetrieval=true",
                        "myuser", "xxxx")) {

                    String sql = "UPDATE sessions SET status='ended' WHERE instructor_id=? AND status='active'";
                    try (PreparedStatement update = conn.prepareStatement(sql)) {
                        update.setInt(1, instructorId);
                        update.executeUpdate();
                    }
                    
                } catch (SQLException e) {
                    e.printStackTrace(); 
                }
            }

            session.invalidate();
        }

        response.sendRedirect("login.html");
    }
}