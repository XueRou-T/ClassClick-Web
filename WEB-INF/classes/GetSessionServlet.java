import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/getsession")
public class GetSessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String sessionIdRaw = request.getParameter("session_id");

        if (sessionIdRaw == null || sessionIdRaw.isEmpty()) {
            out.print("no_session_param");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx")) {

            int sessionID = Integer.parseInt(sessionIdRaw);

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM sessions WHERE session_id=?");
            stmt.setInt(1, sessionID);

            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                out.print(status); 
            } else {
                out.print("No session found");
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            out.print("error");
        }
    }
}