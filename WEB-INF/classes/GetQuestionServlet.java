import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/getquestion")
public class GetQuestionServlet extends HttpServlet {

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
                "SELECT current_question_id, status FROM sessions WHERE session_id=?");
            stmt.setInt(1, sessionID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                 String status = rs.getString("status");
                if ("active".equalsIgnoreCase(status)) 
                {
                    int questionID = rs.getInt("current_question_id");
                    out.print(questionID); 
                }
                else
                    out.println("ended");
            } else {
                out.print("no_session_in_db");
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            out.print("error");
        }
    }
}