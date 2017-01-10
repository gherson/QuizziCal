package QuizziCal;
import org.apache.commons.lang3.text.WordUtils;
import query.MySQLAccess;
import java.sql.ResultSet;
import java.util.Scanner;

/**
 * Created by George Herson on 4/5/2015.
 */
public class CalledByQuizziCal {

    private int testId = 2; // todo Move this to args
    private int userId = 1;
    MySQLAccess msa = new MySQLAccess();

    protected void run(String[] args) {
        int trial = 1;
        int qnNumber = 0;
        try {

            // Determine the trial #.
            ResultSet rs = msa.execute("SELECT max(trial) AS max FROM response"+
                    " WHERE testId="+ this.testId +" AND userId="+ userId);
            if (rs!=null &&
                    rs.next() &&
                    rs.getString("max")!=null) {
                trial = 1 + rs.getInt("max");
            }

            // The arguments to the program may modify the Select.
            String selectionOptions = this.processArgs(args, trial);

            // Pull the questions and their choices.
            rs = msa.execute("SELECT q.id, q.txt qn, c.txt choice, c.correct"+
                    " FROM question q, choice c"+
                    " WHERE c.qnId=q.id AND q.testId="+ this.testId + selectionOptions +
                    " ORDER BY q.id, RAND()");

            // Declare and initialize.
            String qnText = "";
            int choiceNumber = 0; // (1, 2, 3, 4 or 5)
            String qnId = "";
            Scanner userInput = new Scanner(System.in);
            int correctAnswer = 0;
            boolean correct = false;
            boolean openQuestion = false;
            boolean rsNext;

            // For each question and choice.
            while (rs!=null && ((rsNext=rs.next()) || openQuestion)) {

                if (!rsNext || // Get answer to last qn and we're done.
                        !qnId.equals(rs.getString("id"))) { // Pulled a new question.

                    if (choiceNumber!=0) { // A question was just asked, with choices presented.
                        // Take user input for that prior question.
                        // todo use singular prompt when only 1 answer exists
                        System.out.println("Please enter the sum of your selected Choices: ");
                        //               *******************
                        int userChoice = userInput.nextInt(); // Only an int is accepted.
                        //               *******************
                        if (userChoice == correctAnswer) {
                            correct = true;
                            System.out.println("Correct");
                        } else {
                            System.out.println("No. The correct response: "+ correctAnswer);
                        }
                        msa.execute("INSERT INTO response (userId,testId,trial,qnId,response,correct) "+
                                "VALUES ("+ userId +","+ testId +","+ trial +","+ qnId +","+ userChoice +","+
                                (correct ? "1":"0") +")");
                        correct = openQuestion = false;
                        if (!rsNext) {
                            System.out.println("Score: "+ this.score(trial,qnNumber) +"%");
                            break; // THE END.
                        }
                    }

                    // Present new question.
                    qnId = rs.getString("id");
                    qnText = rs.getString("qn");
                    // Print e.g. "Question 2 (qnId 123): What is a bit?"
                    System.out.println("\nQuestion "+ ++qnNumber +" (qnId "+ qnId +"):\n"+
                            WordUtils.wrap(qnText,80));
                    openQuestion = true;
                    correctAnswer = 0; // Reset answer.
                    choiceNumber = 1; // Reset # of choice
                }

                // Present new choice and add it to answer if it is a correct choice.
                String choiceText = rs.getString("choice");
                Double choiceIndicator = Math.pow(2, choiceNumber - 1); // 1, 2, 4, 8 or 16
                System.out.println("Choice " + choiceIndicator.intValue() +": "+
                        WordUtils.wrap(choiceText, 70));
                if (rs.getString("correct").equals("1")) {
                    correctAnswer += choiceIndicator.intValue();
                }
                choiceNumber++; // Increment # of choice
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private float score(int trial, int totalQns) {
        int numberCorrect = 0;
        try {
            ResultSet rs = this.msa.execute("SELECT count(*) AS count FROM response" +
                    " WHERE testId="+ this.testId +" AND userId="+ this.userId +
                    " AND trial="+ trial +" AND correct=1");
            rs.next();
            numberCorrect = rs.getInt("count");

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return 100.0F * numberCorrect / totalQns;
    }

    private String processArgs(String[] args, int trial) {
        String returnStr = "";
        if (args.length>0) {
            try {
                for (int i = 0; i < args.length; i++) {

                    if (args[i].equalsIgnoreCase("wrong")) { // Only failed qns are desired:
                        if (trial > 1) { // Gather and return failed question IDs.
                            ResultSet rs = this.msa.execute("SELECT q.id"+
                                    " FROM question q, response r"+
                                    " WHERE r.qnId=q.id AND q.testId="+ this.testId +
                                    " AND r.correct=0"); //AND r.trial="+ (this.trial - 1));

                            returnStr = " AND q.id IN (";
                            String tmp = ""; // This will hold the q.id's
                            while (rs!=null && rs.next()) {
                                tmp += ","+ rs.getInt("id");
                            }
                            // E.g., " AND q.id IN (123,321) "
                            returnStr += tmp.substring(1) +") "; // Trim leading comma, add paren.
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        return returnStr;
    }
}
