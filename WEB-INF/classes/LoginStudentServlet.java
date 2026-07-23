import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/loginstudent")
public class LoginStudentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");

        if (username == null || username.isEmpty()) {
            out.print("no username input");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx")) {

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

           if (rs.next()) {
                String password = rs.getString("password");
                int userId = rs.getInt("user_id"); 
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(userId + "," + password); 
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                out.print("no_username_found");
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            out.print("error");
        }
    }
}