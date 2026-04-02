import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/deletequestion")
public class DeleteQuestionServlet extends HttpServlet {
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
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE question_id=?");
            ps.setInt(1, qid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error deleting question");
            return;
        }
        resp.sendRedirect("reviewquestionset?setId=" + setId);
    }
}