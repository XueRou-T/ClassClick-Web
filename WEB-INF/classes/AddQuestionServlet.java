import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/addquestion")
public class AddQuestionServlet extends HttpServlet {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
    }

    private String readHtml(String path) throws IOException {
        InputStream is = getServletContext().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }

    private int getNextQuestionNumber(int setId) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) + 1 AS next FROM question WHERE set_id=?"
            );
            ps.setInt(1, setId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("next");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private String getSetName(int setId) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT set_name FROM question_set WHERE set_id=?"
            );
            ps.setInt(1, setId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("set_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Set";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        int setId = Integer.parseInt(request.getParameter("setId"));
        int questionNumber = getNextQuestionNumber(setId);
        String setName = getSetName(setId);

        String html = readHtml("/addquestion.html");
        html = html.replace("<!-- SET_ID -->", String.valueOf(setId))
                   .replace("<!-- SET_NAME -->", setName)
                   .replace("<!-- QUESTION_NUMBER -->", String.valueOf(questionNumber));

        response.setContentType("text/html");
        response.getWriter().write(html);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int setId = Integer.parseInt(request.getParameter("setId"));
        String action = request.getParameter("action");

        String questionText = request.getParameter("questionText");
        String optionA = request.getParameter("optionA");
        String optionB = request.getParameter("optionB");
        String optionC = request.getParameter("optionC");
        String optionD = request.getParameter("optionD");
        String correct = request.getParameter("correctOption");

        if (questionText == null || questionText.trim().isEmpty() ||
            optionA == null || optionB == null || optionC == null || optionD == null ||
            correct == null || !correct.matches("[ABCD]")) {
            response.sendRedirect("addquestion?setId=" + setId + "&error=invalid");
            return;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            PreparedStatement psQ = conn.prepareStatement(
                "INSERT INTO question (text, set_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            psQ.setString(1, questionText.trim());
            psQ.setInt(2, setId);
            psQ.executeUpdate();
            ResultSet keys = psQ.getGeneratedKeys();
            int questionId = 0;
            if (keys.next()) questionId = keys.getInt(1);

            PreparedStatement psC = conn.prepareStatement(
                "INSERT INTO choices (choice, label, question_id, is_correct) VALUES (?, ?, ?, ?)"
            );
            String[] opts = {optionA.trim(), optionB.trim(), optionC.trim(), optionD.trim()};
            char[] letters = {'A','B','C','D'};
            for (int i = 0; i < 4; i++) {
                psC.setString(1, String.valueOf(letters[i]));
                psC.setString(2, opts[i]);
                psC.setInt(3, questionId);
                psC.setBoolean(4, letters[i] == correct.charAt(0));
                psC.addBatch();
            }
            psC.executeBatch();
            conn.commit();

            if ("addAnother".equals(action)) {
                response.sendRedirect("addquestion?setId=" + setId);
            } else {
                response.sendRedirect("reviewquestionset?setId=" + setId);
            }
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.sendError(500, "Database error saving question");
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }
}