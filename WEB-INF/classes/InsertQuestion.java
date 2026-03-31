import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/insertquestionservlet")
public class SignUpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        int set_id = request.getInt("setid");
        String question = request.getParameter("question");
        String choice_A = request.getParameter("password");
        String choice_B = request.getParameter("email");
        String choice_C = request.getParameter("displayname");
        String choice_D = request.getParameter("usertype");

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = username;
        }
        
        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx");
        ) {

             PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO question (text, set_id) VALUES (?, ?)");
            insertStmt.setString(1, question);
            insertStmt.setInt(2, set_id);
            insertStmt.executeUpdate();

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