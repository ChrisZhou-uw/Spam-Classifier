// Chris Zhou
// 05/29/2025
// This class represents a classification tree that predicts labels for
// text-based data. It allows users to train the classification model,
// classify the new input, save and load model to and from file, and evaluate
// the prediction accuracy.
import java.io.*;
import java.util.*;

public class Classifier {
    private ClassifierNode root;

    // This class represents the nodes of the tree and can represent both
    // features and labels.
    private static class ClassifierNode {
        public final TextBlock data;
        public final String label;
        public final String feature;
        public final double threshold;
        public ClassifierNode left;
        public ClassifierNode right;

        // Behavior:
        //   - This constructor initializes a new node representing classification label
        // Parameters:
        //   - data: The TextBlock that represents a piece of text data that can be classified
        //   - label: The String that represents the classification of data
        // Returns:
        //   - None
        public ClassifierNode(TextBlock data, String label) {
            this.data = data;
            this.label = label;
            this.feature = null;
            this.threshold = 0.0;
        }

        // Behavior:
        //   - This constructor initializes a new node representing a decision point
        // Parameters:
        //   - feature: The String that represents important aspects of dataset
        //   - threshold: The double that represents the numeric value when comparing
        //     a feature against at any decision point within classifier.
        //   - left: The ClassifierNode to follow if the current input
        //     is less than the threshold
        //   - right: The ClassifierNode to follow if the current input
        //     is greater than or equal to the threshold
        // Returns:
        //   - None
        public ClassifierNode(String feature, double threshold,
                ClassifierNode left, ClassifierNode right) {
            this.data = null;
            this.label = null;
            this.feature = feature;
            this.threshold = threshold;
            this.left = left;
            this.right = right;
        }
    }

    // Behavior:
    //   - This constructor initializes a new Classifier by loading the Classifier
    //     from a file.
    // Parameters:
    //   - input: The Scanner that is connected to the file.
    // Returns:
    //   - None
    // Exceptions:
    //   - If the given input is null, an IllegalArgumentException is thrown.
    //   - If the tree is still empty after processing input, an IllegalStateException
    //     is thrown.
    public Classifier(Scanner input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        root = readTree(input);
        if (root == null) {
            throw new IllegalStateException();
        }
     }

    // Behavior:
    //   - This method helps initialize a new Classifier by loading the Classifier
    //     from a file.
    // Parameters:
    //   - input: The Scanner that is connected to the file.
    // Returns:
    //   - ClassifierNode: The top of the initialized Classifier tree
    private ClassifierNode readTree(Scanner input) {
        if (!input.hasNextLine()) {
            return null;
        }
        String nextLine = input.nextLine();
        if (nextLine.contains("Feature: ")) {
            String feature = nextLine.substring("Feature: ".length());
            double threshold = Double.parseDouble(
                    input.nextLine().substring("Threshold: ".length()));
            ClassifierNode left = readTree(input);
            ClassifierNode right = readTree(input);
            return new ClassifierNode(feature, threshold, left, right);
        } else {
            return new ClassifierNode(null, nextLine);
        }
    }

    // Behavior:
    //   - This constructor initializes a new Classifier by training on given data and labels
    // Parameters:
    //   - data: The list of TextBlock objects to classify.
    //   - labels: The list of expected labels for each TextBlock object.
    // Returns:
    //   - None
    // Exceptions:
    //   - If data or labels is null, data and labels are not the same size,
    //     or data or labels is empty, an IllegalArgumentException is thrown.
    public Classifier(List<TextBlock> data, List<String> labels) {
        if (data == null || labels == null || data.size() != labels.size() ||
                data.isEmpty() || labels.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < data.size(); i++) {
            root = insert(root, data.get(i), labels.get(i));
        }
    }

    // Behavior:
    //   - This method helps initialize a new Classifier by training on given data and labels
    // Parameters:
    //   - node: The ClassifierNode that represents current node of the tree
    //   - data: The TextBlock that represents a piece of text data that can be classified
    //   - labels: The String that represents the classification of data
    // Returns:
    //   - ClassifierNode: The top of the initialized Classifier tree after training
    private ClassifierNode insert(ClassifierNode node, TextBlock data, String label) {
        if (node == null) {
            return new ClassifierNode(data, label);
        }
        if (node.label != null) {
            if (node.label.equals(label)) {
                return node;
            }
            String diff = data.findBiggestDifference(node.data);
            double middlePoint = midpoint(data.get(diff), node.data.get(diff));
            if (data.get(diff) < node.data.get(diff)) {
                return new ClassifierNode(diff, middlePoint,
                        new ClassifierNode(data, label), node);
            } else {
                return new ClassifierNode(diff, middlePoint, node,
                        new ClassifierNode(data, label));
            }
        } else {
            if (data.get(node.feature) < node.threshold) {
                node.left = insert(node.left, data, label);
            } else {
                node.right = insert(node.right, data, label);
            }
            return node;
        }
    }

    // Behavior:
    //   - This method classifies a piece of data and predicts the appropriate label
    // Parameters:
    //   - input: The TextBlock that represents a piece of text data that can be classified
    // Returns:
    //   - String: The predicted label of the given input
    // Exceptions:
    //   - If the input is null, an IllegalArgumentException is thrown.
    public String classify(TextBlock input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        return classify(root, input);
    }

    // Behavior:
    //   - This method helps classifies a piece of data and predicts the appropriate label
    // Parameters:
    //   - node: The ClassifierNode that represent the current node in the tree
    //   - input: The TextBlock that represents a piece of text data that can be classified
    // Returns:
    //   - String: The predicted label of the given input
    private String classify(ClassifierNode node, TextBlock input) {
        if (node.label != null) {
            return node.label;
        }
        double value = input.get(node.feature);
        if (value < node.threshold) {
            return classify(node.left, input);
        } else {
            return classify(node.right, input);
        }
    }

    // Behavior:
    //   - This method saves the current classifier to the given output.
    // Parameters:
    //   - output: The PrintStream to save current classifier to.
    // Returns:
    //   - None
    // Exceptions:
    //   - If the output is null, an IllegalArgumentException is thrown.
    public void save(PrintStream output) {
        if (output == null) {
            throw new IllegalArgumentException();
        }
        save(root, output);
    }

    // Behavior:
    //   - This method helps save the current classifier to the given output.
    // Parameters:
    //   - node: The ClassifierNode that represents the node to be saved.
    //   - output: The PrintStream to save current classifier to.
    // Returns:
    //   - None
    private void save(ClassifierNode node, PrintStream output) {
        if (node.label != null) {
            output.println(node.label);
        } else {
            output.println("Feature: " + node.feature);
            output.println("Threshold: " + node.threshold);
            save(node.left, output);
            save(node.right, output);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // PROVIDED METHODS - **DO NOT MODIFY ANYTHING BELOW THIS LINE!** //
    ////////////////////////////////////////////////////////////////////

    // Helper method to calcualte the midpoint of two provided doubles.
    private static double midpoint(double one, double two) {
        return Math.min(one, two) + (Math.abs(one - two) / 2.0);
    }    

    // Behavior: Calculates the accuracy of this model on provided Lists of 
    //           testing 'data' and corresponding 'labels'. The label for a 
    //           datapoint at an index within 'data' should be found at the 
    //           same index within 'labels'.
    // Exceptions: IllegalArgumentException if the number of datapoints doesn't match the number 
    //             of provided labels
    // Returns: a map storing the classification accuracy for each of the encountered labels when
    //          classifying
    // Parameters: data - the list of TextBlock objects to classify. Should be non-null.
    //             labels - the list of expected labels for each TextBlock object. 
    //             Should be non-null.
    public Map<String, Double> calculateAccuracy(List<TextBlock> data, List<String> labels) {
        // Check to make sure the lists have the same size (each datapoint has an expected label)
        if (data.size() != labels.size()) {
            throw new IllegalArgumentException(
                    String.format("Length of provided data [%d] doesn't match provided labels [%d]",
                                  data.size(), labels.size()));
        }
        
        // Create our total and correct maps for average calculation
        Map<String, Integer> labelToTotal = new HashMap<>();
        Map<String, Double> labelToCorrect = new HashMap<>();
        labelToTotal.put("Overall", 0);
        labelToCorrect.put("Overall", 0.0);
        
        for (int i = 0; i < data.size(); i++) {
            String result = classify(data.get(i));
            String label = labels.get(i);

            // Increment totals depending on resultant label
            labelToTotal.put(label, labelToTotal.getOrDefault(label, 0) + 1);
            labelToTotal.put("Overall", labelToTotal.get("Overall") + 1);
            if (result.equals(label)) {
                labelToCorrect.put(result, labelToCorrect.getOrDefault(result, 0.0) + 1);
                labelToCorrect.put("Overall", labelToCorrect.get("Overall") + 1);
            }
        }

        // Turn totals into accuracy percentage
        for (String label : labelToCorrect.keySet()) {
            labelToCorrect.put(label, labelToCorrect.get(label) / labelToTotal.get(label));
        }
        return labelToCorrect;
    }
}
