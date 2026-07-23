import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/updatequestion")
public class UpdateQuestionServlet extends HttpServlet {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int qid = Integer.parseInt(req.getParameter("questionId"));
        int setId = Integer.parseInt(req.getParameter("setId"));
        String text = req.getParameter("questionText");
        String optionA = req.getParameter("optionA");
        String optionB = req.getParameter("optionB");
        String optionC = req.getParameter("optionC");
        String optionD = req.getParameter("optionD");
        String correct = req.getParameter("correctOption");

        try (Connection conn = getConnection()) {
            PreparedStatement psQ = conn.prepareStatement("UPDATE question SET text=? WHERE question_id=?");
            psQ.setString(1, text);
            psQ.setInt(2, qid);
            psQ.executeUpdate();

            PreparedStatement psC = conn.prepareStatement(
                "UPDATE choices SET label=?, is_correct=? WHERE question_id=? AND choice=?"
            );
            updateChoice(psC, optionA, qid, "A", "A".equals(correct));
            updateChoice(psC, optionB, qid, "B", "B".equals(correct));
            updateChoice(psC, optionC, qid, "C", "C".equals(correct));
            updateChoice(psC, optionD, qid, "D", "D".equals(correct));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error updating question");
            return;
        }
        resp.sendRedirect("reviewquestionset?setId=" + setId);
    }

    private void updateChoice(PreparedStatement ps, String label, int qid, String choice, boolean correct) throws SQLException {
        ps.setString(1, label);
        ps.setBoolean(2, correct);
        ps.setInt(3, qid);
        ps.setString(4, choice);
        ps.executeUpdate();
    }
}