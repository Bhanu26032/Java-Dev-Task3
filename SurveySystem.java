import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SurveySystem {
    static class Survey {
        int id;
        String title;
        User creator;
        List<Question> questions;
        Timestamp creationDate;
        boolean published;

        Survey(String title, User creator) {
            this.title = title;
            this.creator = creator;
            this.questions = new ArrayList<>();
            this.creationDate = new Timestamp(System.currentTimeMillis());
            this.published = false;
        }

        void addQuestion(Question question) {
            questions.add(question);
        }

        void removeQuestion(Question question) {
            questions.remove(question);
        }

        List<Question> getQuestions() {
            return questions;
        }

        User getCreator() {
            return creator;
        }

        Timestamp getCreationDate() {
            return creationDate;
        }

        void publish() {
            published = true;
        }

        void close() {
            published = false;
        }
    }

    static class Question {
        int id;
        String text;
        String type;
        List<String> options;
        boolean required;

        Question(String text, String type, boolean required) {
            this.text = text;
            this.type = type;
            this.required = required;
            this.options = new ArrayList<>();
        }

        void addOption(String option) {
            options.add(option);
        }

        List<String> getOptions() {
            return options;
        }

        boolean isRequired() {
            return required;
        }
    }

    static class Response {
        int id;
        User respondent;
        Survey survey;
        Map<Question, String> answers;

        Response(User respondent, Survey survey) {
            this.respondent = respondent;
            this.survey = survey;
            this.answers = new HashMap<>();
        }

        User getRespondent() {
            return respondent;
        }

        Survey getSurvey() {
            return survey;
        }

        Map<Question, String> getAnswers() {
            return answers;
        }

        void submitAnswer(Question question, String answer) {
            answers.put(question, answer);
        }
    }

    static class User {
        int id;
        String username;
        String password;  // In practice, should be hashed or encrypted
        String email;
        String role;

        User(String username, String password, String email, String role) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.role = role;
        }

        String getUsername() {
            return username;
        }

        String getEmail() {
            return email;
        }

        String getRole() {
            return role;
        }

        void changePassword(String newPassword) {
            this.password = newPassword;
        }
    }

    // Database connectivity setup
    static class DatabaseConnection {
        static Connection connect() throws SQLException {
            // Update the connection URL, username, and password for Oracle
            String url = "jdbc:oracle:thin:@localhost:1521:orcl";
            String username = "system"; // Replace with your Oracle username
            String password = "admin@123";

            return DriverManager.getConnection(url, username, password);
        }
    }

    // Survey creation UI
    static class SurveyCreationUI extends JFrame {
        private JTextField titleField;
        private JButton createButton;

        public SurveyCreationUI(User creator) {
            setTitle("Create Survey");
            setLayout(null);

            titleField = new JTextField();
            titleField.setBounds(10, 10, 200, 25);
            add(titleField);

            createButton = new JButton("Create Survey");
            createButton.setBounds(10, 45, 150, 25);
            createButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createSurvey(creator);
                }
            });
            add(createButton);

            setSize(300, 120);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void createSurvey(User creator) {
            String title = titleField.getText();

            if (!title.isEmpty()) {
                Survey survey = new Survey(title, creator);

                // Dummy questions for demonstration
                Question question1 = new Question("What is your age?", "Text", true);
                Question question2 = new Question("Select your favorite color:", "Multiple Choice", true);
                question2.addOption("Red");
                question2.addOption("Blue");
                question2.addOption("Green");

                survey.addQuestion(question1);
                survey.addQuestion(question2);

                // Save the survey to the database (dummy method)
                saveSurveyToDatabase(survey);

                JOptionPane.showMessageDialog(this, "Survey created successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a title for the survey.");
            }
        }

        private void saveSurveyToDatabase(Survey survey) {
            try (Connection connection = DatabaseConnection.connect()) {
                // Save survey details (dummy method)
                String surveySql = "INSERT INTO surveys (title, creator_id, creation_date) VALUES (?, ?, ?)";
                try (PreparedStatement surveyStatement = connection.prepareStatement(surveySql, Statement.RETURN_GENERATED_KEYS)) {
                    surveyStatement.setString(1, survey.title);
                    surveyStatement.setInt(2, survey.creator.id);
                    surveyStatement.setTimestamp(3, survey.creationDate);
                    surveyStatement.executeUpdate();

                    // Get the generated survey ID
                    ResultSet generatedKeys = surveyStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        survey.id = generatedKeys.getInt(1);

                        // Save survey questions (dummy method)
                        String questionSql = "INSERT INTO questions (survey_id, text, type, required) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement questionStatement = connection.prepareStatement(questionSql, Statement.RETURN_GENERATED_KEYS)) {
                            for (Question question : survey.questions) {
                                questionStatement.setInt(1, survey.id);
                                questionStatement.setString(2, question.text);
                                questionStatement.setString(3, question.type);
                                questionStatement.setBoolean(4, question.required);
                                questionStatement.executeUpdate();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving survey to the database.");
            }
        }
    }

    public static void main(String[] args) {
        // Dummy user for demonstration
        User user = new User("admin", "admin123", "admin@example.com", "admin");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SurveyCreationUI(user);
            }
        });
    }
}
