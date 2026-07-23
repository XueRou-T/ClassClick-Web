import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/reviewquestionset")
public class ReviewQuestionSetServlet extends HttpServlet {
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/clicker_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
            "myuser", "xxxx"
        );
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
        HttpSession session = request.getSession(false);
        if (session == null || !"instructor".equals(session.getAttribute("usertype"))) {
            response.sendRedirect("login.html");
            return;
        }

        int setId = Integer.parseInt(request.getParameter("setId"));
        String setName = getSetName(setId);
        StringBuilder overview = new StringBuilder();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT q.question_id, q.text, c.choice, c.label, c.is_correct " +
                "FROM question q JOIN choices c ON q.question_id = c.question_id " +
                "WHERE q.set_id=? ORDER BY q.question_id, c.choice"
            );
            ps.setInt(1, setId);
            ResultSet rs = ps.executeQuery();

            int currentQ = -1;
            int questionNumber = 0;
            StringBuilder choices = new StringBuilder();
            String questionText = "";
            int correctChoiceIndex = -1;
            String[] optionLabels = new String[4];

            while (rs.next()) {
                int qid = rs.getInt("question_id");
                if (qid != currentQ) {
                    if (currentQ != -1) {
                        overview.append(choices.toString())
                                .append("</ul></div>")
                                .append(buildEditForm(currentQ, setId, questionText, optionLabels, correctChoiceIndex))
                                .append(buildDeleteForm(currentQ, setId))
                                .append("</div>");
                        choices.setLength(0);
                        optionLabels = new String[4];
                        correctChoiceIndex = -1;
                    }
                    questionNumber++;
                    questionText = rs.getString("text");
                    overview.append("<div class='review-question' id='q-").append(qid).append("'>")
                            .append("<div class='review-question-header'>")
                            .append("<h3>Question ").append(questionNumber).append("</h3>")
                            .append("<a class='edit-link' onclick='toggleEditForm(").append(qid).append(")'>Edit</a>")
                            .append("</div>")
                            .append("<div class='card-body'>")
                            .append("<p class='question-text'><strong>Q:</strong> ").append(questionText).append("</p>")
                            .append("<ul class='choices-list'>");
                    currentQ = qid;
                }
                String choice = rs.getString("choice");
                String label = rs.getString("label");
                boolean isCorrect = rs.getBoolean("is_correct");

                int idx = choice.charAt(0) - 'A';
                optionLabels[idx] = label;
                if (isCorrect) correctChoiceIndex = idx;

                choices.append("<li class='choice-item")
                       .append(isCorrect ? " correct" : "")
                       .append("'>").append(choice).append(": ")
                       .append(label).append("</li>");
            }
            if (currentQ != -1) {
                overview.append(choices.toString())
                        .append("</ul></div>")
                        .append(buildEditForm(currentQ, setId, questionText, optionLabels, correctChoiceIndex))
                        .append(buildDeleteForm(currentQ, setId))
                        .append("</div>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html");
        response.getWriter().write(
            "<!DOCTYPE html><html><head><title>Review Question Set</title>"
          + "<link rel='stylesheet' href='styles.css'>"
          + "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css'>"
          + "<script>"
          + "function toggleEditForm(qid){var f=document.getElementById('editForm-'+qid);f.style.display=(f.style.display==='none'?'block':'none');}"
          + "</script>"
          + "</head><body>"
          + "<header class='main-header'><div class='logo'>Clicker</div>"
          + "<div class='header-right'><a href='mainpageservlet'><i class='fa-solid fa-house'></i></a>"
          + "<a href='logoutservlet'><i class='fa-solid fa-right-from-bracket'></i></a></div></header>"
          + "<div class='questions-overview'>"
          + "<h2>Review: " + setName + "</h2>"
          + overview.toString()
          + "<form method='get' action='mainpageservlet'><button type='submit' class='primary-btn'>Confirm & Return</button></form>"
          + "</div>"
          + "</body></html>"
        );
    }

    private String buildEditForm(int qid, int setId, String questionText, String[] options, int correctIndex) {
        StringBuilder form = new StringBuilder();
        form.append("<form id='editForm-").append(qid).append("' method='post' action='updatequestion' ")
            .append("style='display:none;' class='edit-form'>")
            .append("<h4>Edit Question</h4>")
            .append("<input type='hidden' name='questionId' value='").append(qid).append("'>")
            .append("<input type='hidden' name='setId' value='").append(setId).append("'>")

            .append("<div class='form-group'>")
            .append("<label for='questionText-").append(qid).append("'>Question Text</label>")
            .append("<textarea id='questionText-").append(qid).append("' name='questionText' rows='3'>")
            .append(questionText).append("</textarea>")
            .append("</div>");

        char[] letters = {'A','B','C','D'};
        for (int i = 0; i < 4; i++) {
            form.append("<div class='form-group'>")
                .append("<label for='option").append(letters[i]).append("-").append(qid).append("'>Option ")
                .append(letters[i]).append("</label>")
                .append("<input type='text' id='option").append(letters[i]).append("-").append(qid)
                .append("' name='option").append(letters[i]).append("' value='")
                .append(options[i] != null ? options[i] : "").append("'>")
                .append("</div>");
        }

        form.append("<div class='form-group'>")
            .append("<label for='correctOption-").append(qid).append("'>Correct Answer</label>")
            .append("<select id='correctOption-").append(qid).append("' name='correctOption'>");
        for (int i = 0; i < 4; i++) {
            form.append("<option value='").append(letters[i]).append("'")
                .append(i == correctIndex ? " selected" : "")
                .append(">").append(letters[i]).append("</option>");
        }
        form.append("</select></div>");

        form.append("<div class='form-actions'>")
            .append("<button type='submit' class='primary-btn'>Save</button>")
            .append("</div>")
            .append("</form>");
        return form.toString();
    }

    private String buildDeleteForm(int qid, int setId) {
        return "<form method='post' action='deletequestion' style='display:inline;'>"
             + "<input type='hidden' name='questionId' value='" + qid + "'>"
             + "<input type='hidden' name='setId' value='" + setId + "'>"
             + "<button type='submit' class='delete-btn'>🗑 Delete</button>"
             + "</form>";
    }
}