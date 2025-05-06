import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class QuizApp extends JFrame {
    private JTextField nameField, rollField;
    private JButton startButton, nextButton;
    private JPanel questionPanel;
    private JTextArea questionArea;
    private JLabel statusLabel;
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup group;

    private java.util.List<String> questions = new ArrayList<>();
    private java.util.List<String[]> choices = new ArrayList<>();
    private java.util.List<Character> answers = new ArrayList<>();
    private java.util.List<Character> userAnswers = new ArrayList<>();

    private int current = 0;
    private String name;
    private int roll;

    public QuizApp() {
        setTitle("Quiz Application");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Start Screen
        JPanel startPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        startPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        nameField = new JTextField();
        rollField = new JTextField();
        startButton = new JButton("Start Quiz");

        startPanel.add(new JLabel("Enter Your Name:"));
        startPanel.add(nameField);
        startPanel.add(new JLabel("Enter Your Roll Number:"));
        startPanel.add(rollField);
        startPanel.add(new JLabel());
        startPanel.add(startButton);

        add(startPanel);
        setVisible(true);

        startButton.addActionListener(e -> startQuiz());
    }

    private void startQuiz() {
        name = nameField.getText().trim();
        try {
            roll = Integer.parseInt(rollField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid roll number.");
            return;
        }

        loadQuestions();
        loadAnswers();

        if (questions.isEmpty() || answers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Questions or Answers file is empty or missing!");
            return;
        }

        // Remove start inputs and show first question
        getContentPane().removeAll();
        current = 0;
        showQuestion();
    }

    private void loadQuestions() {
        questions.clear(); choices.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("QUESTIONS.txt"))) {
            String q;
            while ((q = br.readLine()) != null) {
                questions.add(q);
                String[] opts = new String[4];
                for (int i = 0; i < 4; i++) {
                    opts[i] = br.readLine();
                }
                choices.add(opts);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + e.getMessage());
        }
    }

    private void loadAnswers() {
        answers.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("ANSWERS.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                answers.add(line.trim().charAt(0));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading answers: " + e.getMessage());
        }
    }

    private void showQuestion() {
        if (current >= questions.size()) {
            calculateScore();
            return;
        }

        questionPanel = new JPanel(new BorderLayout(10, 10));
        questionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Use JTextArea for wrapping long questions
        questionArea = new JTextArea((current + 1) + ") " + questions.get(current));
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);
        questionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        questionPanel.add(new JScrollPane(questionArea), BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton(choices.get(current)[i]);
            group.add(options[i]);
            optionsPanel.add(options[i]);
        }
        questionPanel.add(optionsPanel, BorderLayout.CENTER);

        nextButton = new JButton(current < questions.size() - 1 ? "Next" : "Finish");
        nextButton.addActionListener(e -> {
            char selected = ' ';
            for (int i = 0; i < 4; i++) {
                if (options[i].isSelected()) {
                    selected = (char)('A' + i);
                }
            }
            if (selected == ' ') {
                JOptionPane.showMessageDialog(this, "Please select an option.");
                return;
            }
            userAnswers.add(selected);
            current++;
            getContentPane().removeAll();
            showQuestion();
        });

        questionPanel.add(nextButton, BorderLayout.SOUTH);
        add(questionPanel);
        revalidate();
        repaint();
    }

    private void calculateScore() {
        int score = 0;
        for (int i = 0; i < answers.size(); i++) {
            if (userAnswers.get(i) == answers.get(i)) score++;
        }

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        StringBuilder sb = new StringBuilder();
        sb.append("Roll No: ").append(roll).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Marks: ").append(score).append(" / ").append(answers.size()).append("\n\n");
        for (int i = 0; i < questions.size(); i++) {
            sb.append((i+1) + ") " + questions.get(i) + "\n");
            sb.append("You Entered: ").append(userAnswers.get(i)).append("\n");
            sb.append("Correct Answer: ").append(answers.get(i)).append("\n");
            sb.append("--------------------------------------------------\n");
        }
        resultArea.setText(sb.toString());

        getContentPane().removeAll();
        add(new JScrollPane(resultArea));
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApp::new);
    }
}
