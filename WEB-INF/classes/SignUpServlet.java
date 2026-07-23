import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/signupservlet")
public class SignUpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String displayName = request.getParameter("displayname");
        String usertype = request.getParameter("usertype");

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = username;
        }
        
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx");
        ) {

            // Check if username exists
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Username already exists
                response.sendRedirect("signup.html?msg=failed");
                return;
            }

            // Insert new user
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO users (username, password, display_name, usertype) VALUES (?, ?, ?, ?)");
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, displayName);
            insertStmt.setString(4, usertype); 

            insertStmt.executeUpdate();

            response.sendRedirect("login.html?msg=success");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("signup.html?msg=failed");
        }
    }
}