import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/displayquestion")
public class DisplayQuestionServlet extends HttpServlet {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String setId = request.getParameter("setId");
        int qIndex = (request.getParameter("qIndex") == null) ? 0 : Integer.parseInt(request.getParameter("qIndex"));

        List<Integer> questionIds = new ArrayList<>();
        String questionText = "";
        StringBuilder optionsHtml = new StringBuilder();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT question_id FROM question WHERE set_id=? ORDER BY question_id"
            );
            ps.setInt(1, Integer.parseInt(setId));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                questionIds.add(rs.getInt("question_id"));
            }

            if (!questionIds.isEmpty()) {
                if (qIndex < 0) qIndex = 0;
                if (qIndex >= questionIds.size()) qIndex = questionIds.size() - 1;

                int questionId = questionIds.get(qIndex);

                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.setAttribute("currentquestion", questionId);
                    Integer sessionId = (Integer) session.getAttribute("sessionid");

                    if (sessionId != null) {
                        // Add question set
                        if (qIndex == 0) {
                            PreparedStatement startQuiz = conn.prepareStatement(
                                "UPDATE sessions SET set_id=?, status='active', start_time=NOW() WHERE session_id=?"
                            );
                            startQuiz.setInt(1, Integer.parseInt(setId));
                            startQuiz.setInt(2, sessionId);
                            startQuiz.executeUpdate();
                        }

                        // Update current question
                        PreparedStatement updateSession = conn.prepareStatement(
                            "UPDATE sessions SET current_question_id=? WHERE session_id=?"
                        );
                        updateSession.setInt(1, questionId);
                        updateSession.setInt(2, sessionId);
                        updateSession.executeUpdate();

                        // End session
                        String finish = request.getParameter("finish");
                        if ("true".equals(finish)) {
                            PreparedStatement endSession = conn.prepareStatement(
                                "UPDATE sessions SET status='ended', end_time=NOW() WHERE session_id=?"
                            );
                            endSession.setInt(1, sessionId);
                            endSession.executeUpdate();

                            // Redirect with origin flag
                            response.sendRedirect("statistics?setId=" + setId + "&sessionId=" + sessionId + "&qIndex=0&origin=mainpage");
                            return;
                        }
                    }
                }

                // Get question text
                PreparedStatement qps = conn.prepareStatement("SELECT text FROM question WHERE question_id=?");
                qps.setInt(1, questionId);
                ResultSet qrs = qps.executeQuery();
                if (qrs.next()) questionText = qrs.getString("text");

                // Get choices
                PreparedStatement cps = conn.prepareStatement(
                    "SELECT choice, label FROM choices WHERE question_id=? ORDER BY choice"
                );
                cps.setInt(1, questionId);
                ResultSet crs = cps.executeQuery();
                while (crs.next()) {
                    optionsHtml.append("<div class='option-card'>")
                               .append("<span class='option-letter'>").append(crs.getString("choice")).append("</span>")
                               .append("<span class='option-label'>").append(crs.getString("label")).append("</span>")
                               .append("</div>");
                }
            } else {
                questionText = "No questions found for this set.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Navigation
        int totalQuestions = questionIds.size();
        int prevIndex = qIndex - 1;
        int nextIndex = qIndex + 1;
        String prevDisabled = (qIndex <= 0) ? "disabled" : "";
        boolean isLast = (qIndex >= totalQuestions - 1);

        String html = readHtml("/displayquestion.html");
        html = html.replace("<!-- QUESTION_TEXT -->", questionText)
                   .replace("<!-- OPTIONS -->", optionsHtml.toString())
                   .replace("<!-- SET_ID -->", setId)
                   .replace("<!-- QINDEX -->", String.valueOf(qIndex + 1))
                   .replace("<!-- TOTAL -->", String.valueOf(totalQuestions))
                   .replace("<!-- PREV -->", String.valueOf(prevIndex))
                   .replace("<!-- NEXT -->", String.valueOf(nextIndex))
                   .replace("<!-- PREV_DISABLED -->", prevDisabled)
                   .replace("<!-- NEXT_DISABLED -->", isLast ? "disabled" : "")
                   .replace("<!-- FINISH_BUTTON -->", isLast ? "<form method='get' action='displayquestion' style='display:inline;' id='finishForm'>" +
                        "<input type='hidden' name='setId' value='" + setId + "'>" +
                        "<input type='hidden' name='qIndex' value='" + nextIndex + "'>" +
                        "<input type='hidden' name='finish' value='true'>" +
                        "<button type='submit' class='finish-btn'>Finish & Show Statistics</button>" +
                        "</form>"
                      : "");


        response.setContentType("text/html");
        response.getWriter().write(html);
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