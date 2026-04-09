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
            conn.setAutoCommit(false);

            // Delete the question
            PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE question_id=?");
            ps.setInt(1, qid);
            ps.executeUpdate();

            // Check if any questions remain in the set
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM question WHERE set_id=?");
            check.setInt(1, setId);
            ResultSet rs = check.executeQuery();
            int remaining = 0;
            if (rs.next()) remaining = rs.getInt(1);

            if (remaining == 0) {
                PreparedStatement delSet = conn.prepareStatement("DELETE FROM question_set WHERE set_id=?");
                delSet.setInt(1, setId);
                delSet.executeUpdate();

                conn.commit();
                // Redirect back to main page since set no longer exists
                resp.sendRedirect("mainpageservlet");
                return;
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error deleting question");
            return;
        }

        resp.sendRedirect("reviewquestionset?setId=" + setId);
    }
}