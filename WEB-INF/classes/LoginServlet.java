import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/loginservlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx");
        ) {

            // Check if username and password is correct
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("username", rs.getString("username"));
                session.setAttribute("displayname", rs.getString("display_name"));
                session.setAttribute("usertype", rs.getString("usertype"));
                session.setAttribute("userId", rs.getInt("user_id"));

                // Redirect to main page
                response.sendRedirect("mainpageservlet");

            } else {
                response.sendRedirect("login.html?msg=failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("login.html?msg=failed");
        }
    }
}