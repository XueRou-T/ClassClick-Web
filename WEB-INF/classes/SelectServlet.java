import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/select")
public class SelectServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.html");
            return;
        }

        String choice = request.getParameter("choice");

        Integer sessionID = (Integer) session.getAttribute("sessionid");
        Integer userID = (Integer) session.getAttribute("user_id");

        Integer questionID = null;
        Integer choiceID = null;

        try (
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx");
        ) {

            // 1. Get current question ID
            PreparedStatement getCurrentQuestionStmt = conn.prepareStatement(
                "SELECT current_question_id FROM sessions WHERE session_id=?");
            getCurrentQuestionStmt.setInt(1, sessionID);

            ResultSet rs1 = getCurrentQuestionStmt.executeQuery();
            if (rs1.next()) {
                questionID = rs1.getInt("current_question_id");
            }

            // 2. Get choice ID based on label (A/B/C/D)
            PreparedStatement getChoicesStmt = conn.prepareStatement(
                "SELECT choice_id FROM choices WHERE question_id=? AND label=?");
            getChoicesStmt.setInt(1, questionID);
            getChoicesStmt.setString(2, choice);

            ResultSet rs2 = getChoicesStmt.executeQuery();
            if (rs2.next()) {
                choiceID = rs2.getInt("choice_id");
            }

            // 3. Insert response
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO response (session_id, question_id, choice_id, user_id, submitted_at) VALUES (?, ?, ?, ?, NOW())");

            insertStmt.setInt(1, sessionID);
            insertStmt.setInt(2, questionID);
            insertStmt.setInt(3, choiceID);
            insertStmt.setInt(4, userID);

            insertStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}