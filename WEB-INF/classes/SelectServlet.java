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

    String choiceLabel = request.getParameter("choice");
    String sessionIdRaw = request.getParameter("session");
    String questionIdRaw = request.getParameter("question_id");

    if (choiceLabel == null || sessionIdRaw == null || questionIdRaw == null) {
        response.sendError(400, "Missing parameters");
        return;
    }

    try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?useSSL=false",
                "myuser", "xxxx")) {
        
        int sessionID = Integer.parseInt(sessionIdRaw);
        int questionID = Integer.parseInt(questionIdRaw);
        int userID = 1; 

        Integer choiceID = null;
        String cSql = "SELECT choice_id FROM choices WHERE question_id=? AND choice=?";
        try (PreparedStatement cStmt = conn.prepareStatement(cSql)) {
            cStmt.setInt(1, questionID);
            cStmt.setString(2, choiceLabel);
            ResultSet rs = cStmt.executeQuery();
            if (rs.next()) {
                choiceID = rs.getInt("choice_id");
            }
        }

        if (choiceID != null) {
            String iSql = "INSERT INTO responses (session_id, question_id, choice_id, user_id, submitted_at) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement insertStmt = conn.prepareStatement(iSql)) {
                insertStmt.setInt(1, sessionID);
                insertStmt.setInt(2, questionID);
                insertStmt.setInt(3, choiceID);
                insertStmt.setInt(4, userID);
                insertStmt.executeUpdate();
                
                System.out.println("Student answered " + choiceLabel + " for Question " + questionID);
                response.setStatus(200);
            }
        } else {
            response.sendError(404, "Choice not found for this question");
        }

    } catch (Exception e) {
        e.printStackTrace();
        response.sendError(500, e.getMessage());
    }
}
}