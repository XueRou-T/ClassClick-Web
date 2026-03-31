import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/questions")
public class QuestionServlet extends HttpServlet {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        int setId = Integer.parseInt(request.getParameter("set_id"));

        StringBuilder questionsHtml = new StringBuilder();
        boolean hasQuestions = false;

        try (Connection conn = getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                "SELECT question_id, text FROM question WHERE set_id = ?"
            );
            ps.setInt(1, setId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                hasQuestions = true;

                int qid = rs.getInt("question_id");
                String qText = rs.getString("text");

                // Get choices
                PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT choice, label FROM choices WHERE question_id = ?"
                );
                ps2.setInt(1, qid);
                ResultSet rs2 = ps2.executeQuery();

                String A="", B="", C="", D="";

                while (rs2.next()) {
                    String ch = rs2.getString("choice");
                    String label = rs2.getString("label");

                    switch (ch) {
                        case "A": A = label; break;
                        case "B": B = label; break;
                        case "C": C = label; break;
                        case "D": D = label; break;
                    }
                }

                questionsHtml.append(buildBlock(qText, A, B, C, D));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If no questions show 1 empty
        if (!hasQuestions) {
            questionsHtml.append(buildBlock("", "", "", "", ""));
        }

        String html = readHtml("/questions.html");
        html = html.replace("<!-- QUESTIONS -->", questionsHtml.toString())
                   .replace("SET_ID", String.valueOf(setId));

        response.setContentType("text/html");
        response.getWriter().write(html);
    }

    private String buildBlock(String q, String A, String B, String C, String D) {
        return "<div class='question-block'>"
                + "<input name='question[]' value='" + safe(q) + "' placeholder='Question'/>"
                + "<input name='A[]' value='" + safe(A) + "' placeholder='A'/>"
                + "<input name='B[]' value='" + safe(B) + "' placeholder='B'/>"
                + "<input name='C[]' value='" + safe(C) + "' placeholder='C'/>"
                + "<input name='D[]' value='" + safe(D) + "' placeholder='D'/>"
                + "</div>";
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("'", "&#39;");
    }

    private String readHtml(String path) throws IOException {
        InputStream is = getServletContext().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }
}