import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/statistics")
public class DisplayStatisticsServlet extends HttpServlet {

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
        String chartJs = "";

        HttpSession session = request.getSession(false);
        Integer sessionId = (session != null) ? (Integer) session.getAttribute("sessionid") : null;

        try (Connection conn = getConnection()) {
            // Get all question IDs for this set
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

                // Get question text
                PreparedStatement qps = conn.prepareStatement("SELECT text FROM question WHERE question_id=?");
                qps.setInt(1, questionId);
                ResultSet qrs = qps.executeQuery();
                if (qrs.next()) questionText = qrs.getString("text");

                // Get choices + counts from responses
                PreparedStatement cps = conn.prepareStatement(
                    "SELECT c.choice, c.label, c.is_correct, COUNT(r.response_id) AS total " +
                    "FROM choices c " +
                    "LEFT JOIN responses r ON c.choice_id = r.choice_id AND r.session_id=? AND r.question_id=? " +
                    "WHERE c.question_id=? " +
                    "GROUP BY c.choice_id ORDER BY c.choice"
                );
                cps.setInt(1, sessionId != null ? sessionId : 0);
                cps.setInt(2, questionId);
                cps.setInt(3, questionId);
                ResultSet crs = cps.executeQuery();

                List<String> labels = new ArrayList<>();
                List<Integer> counts = new ArrayList<>();

                while (crs.next()) {
                    String label = crs.getString("choice") + ": " + crs.getString("label");
                    int count = crs.getInt("total");
                    boolean correct = crs.getBoolean("is_correct");

                    optionsHtml.append("<li class='choice-item")
                               .append(correct ? " correct" : "")
                               .append("'>")
                               .append(label).append(" — ")
                               .append(count).append(" students</li>");

                    labels.add(label);
                    counts.add(count);
                }

                chartJs = "<canvas id='chart'></canvas>\n" +
                          "<script>\n" +
                          "const labels = " + toJsArray(labels) + ";\n" +
                          "const counts = " + counts.toString() + ";\n" +
                          "new Chart(document.getElementById('chart'), {\n" +
                          "  type: 'bar',\n" +
                          "  data: { labels: labels, datasets: [{ label: 'Votes', data: counts, " + "backgroundColor: ['#007bff','#28a745','#ffc107','#dc3545'] }] },\n" +
                          "  options: { scales: { y: { beginAtZero: true } } }\n" +
                          "});\n" +
                          "</script>\n";
            } else {
                questionText = "No questions found for this set.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int totalQuestions = questionIds.size();
        int prevIndex = qIndex - 1;
        int nextIndex = qIndex + 1;
        String prevDisabled = (qIndex <= 0) ? "disabled" : "";
        boolean isLast = (qIndex >= totalQuestions - 1);

        String html = readHtml("/statistics.html");
        html = html.replace("<!-- QUESTION_TEXT -->", questionText)
                   .replace("<!-- OPTIONS -->", optionsHtml.toString())
                   .replace("<!-- CHART_DATA -->", chartJs)
                   .replace("<!-- SET_ID -->", setId)
                   .replace("<!-- QINDEX -->", String.valueOf(qIndex + 1))
                   .replace("<!-- TOTAL -->", String.valueOf(totalQuestions))
                   .replace("<!-- PREV -->", String.valueOf(prevIndex))
                   .replace("<!-- NEXT -->", String.valueOf(nextIndex))
                   .replace("<!-- PREV_DISABLED -->", prevDisabled)
                   .replace("<!-- NEXT_DISABLED -->", isLast ? "disabled" : "")
                   .replace("<!-- RETURN_BUTTON -->", isLast ? "<form method='get' action='mainpageservlet' style='display:inline;'>" + "<button type='submit' class='nav-btn'>Return</button></form>" : "");

        response.setContentType("text/html");
        response.getWriter().write(html);
    }

    private String toJsArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
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