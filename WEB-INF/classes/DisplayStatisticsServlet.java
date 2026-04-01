import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/statistics")
public class DisplayStatisticsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {


        HttpSession session = request.getSession(false);
        Integer sessionId = (session != null) ? (Integer) session.getAttribute("sessionid") : null;
        
        if (session == null || session.getAttribute("currentquestion") == null) {
            response.getWriter().write("No active question.");
            return;
        }

        int questionId = (Integer) session.getAttribute("currentquestion");


        String questionText = "";
        StringBuilder html = new StringBuilder();

        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "myuser", "xxxx");

            PreparedStatement q = conn.prepareStatement(
                "SELECT text FROM question WHERE question_id=?");
            q.setInt(1, questionId);

            ResultSet qrs = q.executeQuery();
            if (qrs.next()) {
                questionText = qrs.getString("text");
            }

           PreparedStatement c = conn.prepareStatement(
                "SELECT c.label, c.text, COUNT(r.choice_id) AS total " +
                "FROM choices c " +
                "LEFT JOIN response r ON c.choice_id = r.choice_id AND r.session_id=? " +
                "WHERE c.question_id=? " +
                "GROUP BY c.choice_id " +
                "ORDER BY c.label"
            );

            if (sessionId == null) sessionId = 0;

            c.setInt(1, sessionId);
            c.setInt(2, questionId);

            ResultSet crs = c.executeQuery();

            while (crs.next()) {
                String choice = crs.getString("label"); // A B C D
                String label = crs.getString("text");
                int total = crs.getInt("total");

                labels.add(choice);
                counts.add(total);

                html.append("<p>")
                    .append(choice).append(": ")
                    .append(label)
                    .append(" (").append(total).append(" votes)")
                    .append("</p>");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        String labelsStr = "[\"" + String.join("\",\"", labels) + "\"]";
        String countsStr = counts.toString();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><head>");
        out.println("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");
        out.println("</head><body>");

        out.println("<h2>" + questionText + "</h2>");
        out.println(html.toString());

        out.println("<canvas id='chart'></canvas>");

        out.println("<script>");
        out.println("const labels = " + labelsStr + ";");
        out.println("const data = " + countsStr + ";");

        out.println("new Chart(document.getElementById('chart'), {");
        out.println("type: 'bar',");
        out.println("data: { labels: labels, datasets: [{ label: 'Votes', data: data }] },");
        out.println("options: { scales: { y: { beginAtZero: true } } }");
        out.println("});");

        out.println("</script>");
        out.println("</body></html>");
    }
}