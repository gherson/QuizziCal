package QuizziCal; /**
 * Created by George Herson on 3/31/2015.
 *
 * To only be asked questions you've gotten wrong, add "wrong" to the program arguments (in IDEA, edit the QuizziCal run
 * configuration available at top-right).  2015-04-20
 *
 * Useful SQL:
 * To pull latest scores:
 * select sum(correct), count(*), sum(correct)/count(*) as %
 * from response where userId=1 and testId=1 and trial=
 * (select max(trial) from response where userId=1 and testId=1)
 * 4/5/15
 * To check your errors:
 * SELECT r.* FROM question q, response r
 * WHERE r.qnId=q.id AND q.testId=?
 * AND r.correct=0 and r.trial= (select max(trial) from response)
 * 4/12/15
 *
 * Setup
 * 1/9/2017 instead of studying the below I just ran a MySQL dump, Quizdb20150414.sql, as sys.
 * See also section "Usage in a new project" in OneNote - Database tech - MySQL...
   Prompt user for multiple choice answers.  After user response, show if it was correct while keeping score, and timestamp of each reply. 3/31/15

 * To consider:
 * Increase difficulty by randomizing qn order.
 * Calculate partially correct answers (and make response.correct a real).
 * 4/12/15
 *
 */

public class QuizziCal {
    public static void main(String[] args) {
        CalledByQuizziCal cbq = new CalledByQuizziCal();
        cbq.run(args);
    }
}
