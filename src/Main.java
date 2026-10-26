import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class Main extends JFrame {

    // UI Components
    private JTextArea textArea;
    private JButton selectTextFileButton;
    private JButton saveButton;
    private JLabel statusLabel;

    // Data Structures
    private final Set<String> stopWords = new TreeSet<>();
    private final Map<String, Integer> tagMap = new TreeMap<>();
    private File selectedTextFile;

    public Main() {
        setTitle("Computational Linguistics - Tag Extractor");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Layout setup
        setLayout(new BorderLayout());

        // Top Panel for Buttons
        JPanel topPanel = new JPanel();
        JButton loadStopWordsButton = new JButton("1. Load Stop Words");
        selectTextFileButton = new JButton("2. Select Text File & Extract");
        saveButton = new JButton("3. Save Tags to File");

        saveButton.setEnabled(false); // Disabled until extraction happens
        selectTextFileButton.setEnabled(false); // Disabled until stop words are loaded

        topPanel.add(loadStopWordsButton);
        topPanel.add(selectTextFileButton);
        topPanel.add(saveButton);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel for Text Area & Scroll Pane
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Status Bar
        statusLabel = new JLabel(" Status: Please load a stop words file to begin.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.SOUTH);

        // Event Listeners
        loadStopWordsButton.addActionListener(_ -> loadStopWords());
        selectTextFileButton.addActionListener(_ -> selectAndExtractTextFile());
        saveButton.addActionListener(_ -> saveOutputFile());
    }

    private void loadStopWords() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File stopWordsFile = chooser.getSelectedFile();
            try {
                List<String> lines = Files.readAllLines(stopWordsFile.toPath());
                stopWords.clear();
                for (String line : lines) {
                    String trimmed = line.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        stopWords.add(trimmed);
                    }
                }
                statusLabel.setText(" Status: Loaded " + stopWords.size() + " stop words from " + stopWordsFile.getName());
                textArea.append("--- Stop Words Loaded Successfully (" + stopWords.size() + " words) ---\n");
                selectTextFileButton.setEnabled(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading stop words file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectAndExtractTextFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedTextFile = chooser.getSelectedFile();
            tagMap.clear();
            textArea.setText("");

            textArea.append("Extracting tags from file: " + selectedTextFile.getAbsolutePath() + "\n\n");
            textArea.append(String.format("%-25s | %s\n", "TAG / WORD", "FREQUENCY"));
            textArea.append("-----------------------------------------\n");

            try {
                List<String> lines = Files.readAllLines(selectedTextFile.toPath());
                for (String line : lines) {
                    // Split line into words using whitespace
                    String[] rawWords = line.split("\\s+");
                    for (String rawWord : rawWords) {
                        // Clean: remove non-letters, convert to lowercase
                        String cleanedWord = rawWord.replaceAll("[^a-zA-Z]", "").toLowerCase();

                        // Validate: check if valid and NOT a stop word
                        if (!cleanedWord.isEmpty() && !stopWords.contains(cleanedWord)) {
                            tagMap.put(cleanedWord, tagMap.getOrDefault(cleanedWord, 0) + 1);
                        }
                    }
                }

                // Display results in JTextArea
                for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                    textArea.append(String.format("%-25s | %d\n", entry.getKey(), entry.getValue()));
                }

                statusLabel.setText(" Status: Extraction complete. Unique tags found: " + tagMap.size());
                saveButton.setEnabled(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error processing text file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveOutputFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Extracted Tags");
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outFile = chooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
                writer.println("Extracted Tags from: " + selectedTextFile.getName());
                writer.println("Total Unique Tags: " + tagMap.size());
                writer.println("-----------------------------------------");
                writer.println(String.format("%-25s | %s", "TAG / WORD", "FREQUENCY"));
                writer.println("-----------------------------------------");

                for (Map.Entry<String, Integer> entry : tagMap.entrySet()) {
                    writer.println(String.format("%-25s | %d", entry.getKey(), entry.getValue()));
                }

                statusLabel.setText(" Status: Successfully saved to " + outFile.getName());
                JOptionPane.showMessageDialog(this, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static void main() {
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}