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
                String usertype = rs.getString("usertype");

                if ("instructor".equals(usertype)) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", rs.getString("username"));
                    session.setAttribute("displayname", rs.getString("display_name"));
                    session.setAttribute("usertype", usertype);
                    session.setAttribute("userId", rs.getInt("user_id"));

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("SUCCESS");
                } else if ("student".equals(usertype)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Students cannot log in here.");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Wrong Username or Password. Please try again.");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("login.html?msg=failed");
        }
    }
}