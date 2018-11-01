
import java.util.ArrayList;
import java.util.List;

import java.io.*;

// StringTokenizer
import java.util.*;

// represents files and directory pathnames 
// in an abstract manner
import java.io.File;

// reads data from files as streams of characters
import java.io.FileReader;

// reads text efficiently from character-input
// stream buffers 
import java.io.BufferedReader;

// for writing data to files
import java.io.PrintWriter;

// signals that an input/output (I/O) exception 
// of some kind has occurred
import java.io.IOException;


public class StableV2 {

    // returns the index of a given element in 1D array
    private static int findIndex (int[] anArray, int anElement){
        int index = -1;
        for (int row = 0; row < anArray.length; row++){
            if (anArray[row] == anElement){
                index = row;
                break;
            }
        }
        return index;
    }


    // print matched pairs -- stable match solution
    private static void printMatchedPairs (boolean[][] matchedPairs, PrintWriter matchWriter){
        matchWriter.println("\nThe matched pairs are: \n");
        for (int row = 1; row < matchedPairs.length; row++)
            for (int col = 1; col < matchedPairs[row].length; col++)
                if (matchedPairs[row][col])
                    matchWriter.println("\n\t[" + row + ", " + col + "]\n");
        matchWriter.close();
    }


    // is the girl engaged?
    private static boolean girlEngaged (boolean[][] matchedPairs, int girl){
        boolean engaged = false;
        for (int row = 1; row < matchedPairs.length; row++)
            if (matchedPairs[row][girl])
                engaged = true;
        
        return engaged; 
    }

    // who is the boy that the girl is engaged to?
    private static int girlEngagedToWhom (boolean[][] matchedPairs, int girl){
        int boy = 0;
        // int boy;
        for (int row = 1; row < matchedPairs.length; row++)
            if (matchedPairs[row][girl])
                boy = row;
        
        return boy; 
    }


    // does the girl prefer current engagement (curEngagedBoy) over new proposal (newProposalBoy)?
    private static boolean prefersB1ToB2 (int[] girlPreferences, int curEngagedBoy, int newProposalBoy){
        int curEngagedBoyIndex = 1;
        int newProposalBoyIndex = 1;
        for (int index = 1; index < girlPreferences.length; index++){
            if (girlPreferences[index] == curEngagedBoy)
                curEngagedBoyIndex = index;
            if (girlPreferences[index] == newProposalBoy)
                newProposalBoyIndex = index;
        }

        if (curEngagedBoyIndex < newProposalBoyIndex)
            return true;
        else
            return false;
    }


    // did the boy make proposals to all the girls?
    private static boolean proposalsExhausted(boolean[][] proposed, int boy) {
        // number of proposals made by the boy
        int proposalCount = 0;

        // count how many proposals have been made by the boy
        for (int col = 1; col < proposed[boy].length; col++)
            if (proposed[boy][col] == true)
                proposalCount++;

        if (proposalCount == proposed[boy].length)
            return true;
        else
            return false;
    }

    // print elements of 1D array (skips element at index 0)
    // to achieve 1-based indexing
    // this method is needed for testing purpose only
    private static void print1DArray (int[] anArray){
        System.out.println();
        for (int index = 1; index < anArray.length; index++)
            System.out.print(anArray[index] + " ");
    }


    // return the max element in an integer list
    // private static int getMaxElement (ArrayList<Integer> aList){
    //     int maxElement = aList.get(0);
    //     for(int index = 1; index < aList.size(); index++)
    //         if (aList.get(index) > maxElement)
    //             maxElement = aList.get(index);

    //     return maxElement;
    // }


	public static void main(String[] args){
        // a queue to manage boys preferences
        LinkedQueue boysPrefQueue = new LinkedQueue();

        // a stack to hold unmatched boys
        LinkedStack unmatchedBoysStack = new LinkedStack();

        // a 2D array to manage girls preferences
        int[][] girlsPrefArray;

        // number of boys = preferences queue size
        int numOfBoys = 0;

        // 2D boolean array to indicate engaged pairs,
        // and who is free
        boolean[][] matchedPairs;

        // for tracking proposals made by boys to girls
        boolean[][] proposalsMade;

        // a writer for writing stable matches to an output file
        PrintWriter stableMatchWriter = null;

        // a buffered reader for efficiently reading from a file that has matches. 
        // these matches need to be validated for stability
        BufferedReader stabilityValBr = null;

        // Boolean variable to indicate whether stable matching 
        // is to be found (when set to true) or a given matching 
        // needs to be tested for stability (when set to false)
        Boolean findStable = true;

        // did the user provide correct number of command line arguments?
        // if not, print a message and exit
        if (args.length != 4){
            System.err.println("\nNumber of command line arguments must be 4");
            System.err.println("You have given " + args.length + " command line arguments");
            System.err.println("Incorrect usage. Program terminated");
            System.err.println("Correct usage: java Stable <find|check> <boys-pref-file> <girls-pref-file> <matching-file-name|doubtful-file-name> ");
            System.exit(1);
        }

        // determine the task: finding a stable match 
        // or testing if a match is stable
        if (new String("find").equals(args[0])) {
            findStable = true;
            System.out.println("\nThe task is to find a stable match\n");
        }
        else if (new String("check").equals(args[0])) {
            findStable = false;
            System.out.println("The task is to check whether a match is stable\n");
        }
        else {
            System.out.println("\nThe first command line argument must be either 'find' or 'check'\n");
            System.out.println("Program terminated...");
            System.exit(1);
        }

        // extract boys preference file name from command line arguments
        String boysPrefFileName = args[1];
        System.out.println("Boys preference file name: " + boysPrefFileName);

        // extract girls preference file name from command line arguments
        String girlsPrefFileName = args[2];
        System.out.println("Girls preference file name: " + girlsPrefFileName);

        // buffered reader for efficiently reading from boys preference file
        BufferedReader boysPrefBr = null;

        // buffered reader for efficiently reading from girls preference file
        BufferedReader girlsPrefBr = null;

        // buffered reader for efficiently reading from the file
        // that contains a given stable match
        BufferedReader stableMatchBr = null;

        // file which contains a given stable matching for validation
        String stabilityValFileName = null;

        if (findStable) {
            // extract from command line arguments the name of the file
            // for writing computed stable matches
            String stableMatchFileName = args[3];
            System.out.println("The name of the file for writing computed stable matches: " + stableMatchFileName);

            try {
            stableMatchWriter = new PrintWriter(stableMatchFileName, "UTF-8");
            System.out.println(stableMatchFileName + " file successfully opened for writing");
            }
            catch (IOException ex){
                System.err.println("Unable to open " + stableMatchFileName + " file for writing");
                System.err.println("Program terminated...\n");
                System.exit(1);
            }
        }
        else { // validate stability
            // extract from command line arguments the name of the file
            // that has matches, which need to be validated for stability
            stabilityValFileName = args[3];
            System.out.println("The name of the file which contains a stable matching to be validated: " + stabilityValFileName);

            // read matches for validation
            try {
                // get a BufferedReader object, which encapsulates
                // access to stabilityValFileName disk file
                stabilityValBr = new BufferedReader(new FileReader(stabilityValFileName));
                System.out.println(stabilityValFileName + " file successfully opened for reading");
            }
            catch (IOException ex) {
                System.err.println(stabilityValFileName + " file not found. Program terminated.\n");
                System.exit(1);
            }
        }

        // read boys preferences
        try {
            // get a BufferedReader object, which encapsulates
            // access to boysPrefFileName disk file
            boysPrefBr = new BufferedReader(new FileReader(boysPrefFileName));
            System.out.println(boysPrefFileName + " file successfully opened for reading");

            // a line of text read from a file
            String line;

            // read boys preferences
            while ((line = boysPrefBr.readLine()) != null) {
                System.out.println(line);

                StringTokenizer st = new StringTokenizer(line, " ");

                // holds preferences of a boy
                ArrayList<Integer> al = new ArrayList<Integer>(st.countTokens());

                // add preferences of a boy to an array list
                while (st.hasMoreTokens()){
                    al.add(Integer.parseInt(st.nextToken()));
                }

                System.out.println(al);

                // add preference list of a boy to the queue
                boysPrefQueue.enQueue(al);

                // increment boys queue size
                numOfBoys++;
            }

            // close boys preferences file
            boysPrefBr.close();

            System.out.println("numOfBoys: " + numOfBoys);

            // this while loop for test purpose only
            // while (!boysPrefQueue.isEmpty()) {
            //     // get the element at the front of the queue
            //     // without removing it from the queue
            //     ArrayList frontElement = boysPrefQueue.getFront();

            //     // print the element
            //     System.out.println("\n" + frontElement + " is at the front of the queue.");
                
            //     // now remove the front element from the queue
            //     frontElement = boysPrefQueue.deQueue();

            //     // print a message that the element has been removed from the queue
            //     System.out.println(frontElement + " is removed from the queue.");
            // }

            // queue should be empty now
            // System.out.println("\nisEmpty() returns " + boysPrefQueue.isEmpty());


            // initially, all boys are unmatched
            // add them to the stack
            for (int index = 1; index <= numOfBoys; index++){
                unmatchedBoysStack.push(index);
            }

            // int topEle;
            // while (!unmatchedBoysStack.isEmpty()) {
            //     // now remove the top element from the stack
            //     topEle = unmatchedBoysStack.pop();
            //     // print message that the element has been removed
            //     System.out.println(topEle + " is removed from the stack.");
            // }

            // // stack should be empty now
            // System.out.println("\nisEmpty() returns " + unmatchedBoysStack.isEmpty());

        }
        catch (IOException ex) {
            System.err.println(boysPrefFileName + " file not found. Program terminated.\n");
            System.exit(1);
        }

        // read girls' preferences
        // number of girls is same as the number of boys
        // create a 2D array to hold girls preferences
        // array size is numOfBoys+1 because of 1-based indexing
        girlsPrefArray = new int[numOfBoys+1][numOfBoys+1];
        // System.out.println("\nNumber of rows in girlsPrefArray: " + girlsPrefArray.length);

        try {
            // get a BufferedReader object, which encapsulates
            // access to girlsPrefFileName disk file
            girlsPrefBr = new BufferedReader(new FileReader(girlsPrefFileName));
            System.out.println(girlsPrefFileName + " file successfully opened for reading");

            // a line of text read from a file
            String line;

            // to index into 2D array rows
            int i = 1;

            while ((line = girlsPrefBr.readLine()) != null) {
                System.out.println(line);
                StringTokenizer st = new StringTokenizer(line, " ");

                // to index into 2D array columns
                int j = 1;

                // add preferences of a girl to a 2D array
                while (st.hasMoreTokens()){
                    girlsPrefArray[i][j] = Integer.parseInt(st.nextToken());
                    // next preference
                    j++;
                }

                // get preferences of the next girl
                i++;
            }

            // close girls preferences file
            girlsPrefBr.close();

            // verify that girls' preferences are loaded 
            // correctly into the 2D array

            // System.out.println("\nGirls preferences are:");
            // for (int row = 1; row <= numOfBoys; row++){
            //     for (int col = 1; col <= numOfBoys; col++){
            //         System.out.print(girlsPrefArray[row][col] + " ");
            //     }
            //     // next row
            //     System.out.println();
            // }
            
        } // try
        catch (IOException ex) {
            System.err.println("\n" + girlsPrefFileName + " file not found. Program terminated...\n");
            System.exit(1);
        }

        

        // Gale-Shapley algorithm

        // matchedPairs[][] 2D array will be used for both finding
        // a stable match and validating a given stable matching
        // for finding a stable match task, initially, there are
        // no matched pairs, and all girls are unmatched
        matchedPairs = new boolean[numOfBoys+1][numOfBoys+1];
        for (int row = 1; row <= numOfBoys; row++)
            for (int col = 1; col <= numOfBoys; col++)
                matchedPairs[row][col] = false;

        // for validating a given stable matching, matchedPairs
        // will be used to store the given stable matching 

        // task case: finding a stable match
        if (findStable) {
            // create a 2D array for tracking proposals made by boys
            proposalsMade = new boolean[numOfBoys+1][numOfBoys+1];

            // initially, none of the boys made proposals
            for (int row = 1; row < proposalsMade.length; row++)
                for (int col = 1; col < proposalsMade[row].length; col++)
                    proposalsMade[row][col] = false;

            // System.out.println("\nUnmatched boys in the stack:");

            int proposingBoy;
            // as long as there are boys who are free
            while (!unmatchedBoysStack.isEmpty()){
                proposingBoy = unmatchedBoysStack.pop();

                // System.out.println("Proposing boy: " + proposingBoy);

                // if this boy has not yet proposed to every girl
                if (!proposalsExhausted(proposalsMade, proposingBoy)) {
                    // find the highest-ranked girl in the boy's preference
                    // list to whom the boy has not yet proposed
                    // accomplished in multiple steps

                    // get the girl's preference list for the boy from the queue
                    ArrayList boyPreferences = boysPrefQueue.nthElement(proposingBoy);
                    System.out.println("\nBoy's preferences: " + boyPreferences);


                    // highest-ranked girl is at index location 1, unless
                    // she has been proposed to by the boy earlier
                    int highestRankedGirl = (int)boyPreferences.get(0);
                    // System.out.println("\n highestRankedGirl: " + highestRankedGirl);
                    boolean found = false;
                    int index = 0;
                    while (!found && index < boyPreferences.size()) {
                        // propose to the highest-ranked girl
                        if (!proposalsMade[proposingBoy][highestRankedGirl])
                            found = true;
                        else { // look for the next highest-ranked girl
                            index++;
                            highestRankedGirl = (int)boyPreferences.get(index);
                        }
                    }

                    // note the fact that proposalBoy proposed to highestRankedGirl
                    proposalsMade[proposingBoy][highestRankedGirl] = true;

                    // print the highest-ranked girl
                    // System.out.println("\n Boy " + proposingBoy + " highest-ranked girl is: " + highestRankedGirl);

                    // if highestRankedGirl is free, the boy and highestRankedGirl get engaged
                    if (!girlEngaged(matchedPairs, highestRankedGirl)) {
                        matchedPairs[proposingBoy][highestRankedGirl] = true;
                        // printMatchedPairs(matchedPairs);
                    }
                    else { // the girl is currently engaged to currentBoy
                        int curEngagedBoy = girlEngagedToWhom(matchedPairs, highestRankedGirl);
                        // System.out.println("\nGirl " + highestRankedGirl + " is currently engaged to boy " + currentBoy);
                        
                        // does the girl prefer her current engagement over new proposal?
                        // System.out.println("\nGirls preferences are:");
                        // for (int row = 0; row < numOfBoys; row++){
                        //     for (int col = 0; col < numOfBoys; col++){
                        //         System.out.print(girlsPrefArray[row][col] + " ");
                        //     }
                        //     // next row
                        //     System.out.println();
                        //  }

                        int[] girlPrefs = girlsPrefArray[highestRankedGirl];
                        if (prefersB1ToB2(girlPrefs, curEngagedBoy, proposingBoy)) {
                            // System.out.println("\n Girl " + highestRankedGirl + " prefers boy " + curEngagedBoy + " over boy " + proposalBoy);
                            // the boy making the proposal remains free as the proposal is not accepted
                            unmatchedBoysStack.push(proposingBoy);
                        }
                        else { // highestRankedGirl prefers the new proposal over current engagement
                            // System.out.println("\n Girl " + highestRankedGirl + " does not prefer boy " + curEngagedBoy + " over boy " + proposalBoy);
                            // highestRankedGirl breaks current engagement, and enter
                            // into a new engagement with the proposalBoy
                            matchedPairs[proposingBoy][highestRankedGirl] = true;
                            // curEngagedBoy becomes free
                            matchedPairs[curEngagedBoy][highestRankedGirl] = false;
                            unmatchedBoysStack.push(curEngagedBoy);
                        }
                    }
                } // if (!proposalsExhausted(proposalsMade, boy))
            } // while (!unmatchedBoysStack.isEmpty()){

            // print stable matchings to a disk file
            printMatchedPairs(matchedPairs, stableMatchWriter);
        } // if (findStable)
        else { // task case: verifying whether a given match is stable
            // load the stable match file into matchedPairs boolean array
            
            // to read a line of text from a file
            String line;

            int aBoy;
            int aGirl;

            // read stable match pairings
            try {
                while ((line = stabilityValBr.readLine()) != null) {
                    System.out.println(line);
                    StringTokenizer st = new StringTokenizer(line, " ");
                    aBoy = Integer.parseInt(st.nextToken());
                    aGirl = Integer.parseInt(st.nextToken());

                    // System.out.println("\nboy: " + aBoy + " girl: " + aGirl);
                    matchedPairs[aBoy][aGirl] = true;
                }

                // close the  matching validation file
                stabilityValBr.close();
            }
            catch (IOException ex) {
                System.err.println("\n" + stabilityValFileName + " file not found. Program terminated...\n");
                System.exit(1);
            }

            // print matchedPairs array values
            // System.out.println("\nPrinting the given stable matching ...");
            // for (int row = 1; row < matchedPairs.length; row++)
            //     for (int col = 1; col < matchedPairs[row].length; col++)
            //         if (matchedPairs[row][col])
            //             System.out.println("\n(" + row + ", " + col + ")");

            // test for *matching property*: a boy is matched with at most
            // one girl, and vice versa. it is not required that every boy
            // and every girl are matched

            int matchCount = 0;
            boolean matchViolation = false;
            // verify whether each boy is matched with only one girl
            outerloop1:
            for (int row = 1; row < matchedPairs.length; row++){
                matchCount = 0;
                for (int col = 1; col < matchedPairs[row].length; col++){
                    if (matchedPairs[row][col]) {
                        matchCount++;
                        if (matchCount > 1) {
                            matchViolation = true;
                            break outerloop1;
                        }
                    }
                }
            }

            if (matchViolation){
                System.out.println("\nMatching property violated. More than one match assigned to a boy.");
                System.exit(1);
            }
            // else {
            //     System.out.println("\nMatching property for boys is satisfied");
            // }

            matchViolation = false;
            // verify whether each girl is matched with only one boy
            outerloop2:
            for (int col = 1; col < matchedPairs[0].length; col++){
                matchCount = 0;
                for (int row = 1; row < matchedPairs.length; row++){
                    if (matchedPairs[row][col]) {
                        matchCount++;
                        if (matchCount > 1) {
                            matchViolation = true;
                            break outerloop2;
                        }
                    }
                }
            }

            if (matchViolation){
                System.out.println("\nMatching property violated. More than one match assigned to a girl.");
                 System.exit(1);
            }
            // else {
            //     System.out.println("\nMatching property for girls is satisfied");
            // }

            // test for *perfectness property*: each boy and each girl
            // appears in exactly one pair in the matching set

            // verify that every boy has only one match
            boolean boyTest = true;
            for (int row = 1; row < matchedPairs.length; row++){
                matchCount = 0;
                for (int col = 1; col < matchedPairs[row].length; col++){
                    if (matchedPairs[row][col])
                        matchCount++;
                }
                if (matchCount != 1)
                    boyTest = false;
            }

            // verify that every girl has only one match
            boolean girlTest = true;
            for (int col = 1; col < matchedPairs[0].length; col++){
                matchCount = 0;
                for (int row = 1; row < matchedPairs.length; row++){
                    if (matchedPairs[row][col])
                        matchCount++;
                }
                if (matchCount != 1)
                    girlTest = false;
            }

            if (!boyTest || !girlTest) {
                System.out.println("\nPerfectness property violated");
                System.exit(1);
            }
            // else {
            //     System.out.println("\nPerfectness property satisfied");
            // }

            // test for *stability property*
            // A matching is not stable if there are elements 
            // in B (boys) and G (girls), such that:
            // (i) the elements b and g are not currently matched with each other,
            // (ii) b prefers g over his current pairing, and
            // (iii) g also prefers b over her current pairing

            // System.out.println("\nTesting one pair at a time for stable matching ...");
            for (int row = 1; row < matchedPairs.length; row++)
                for (int col = 1; col < matchedPairs[row].length; col++)
                    if (matchedPairs[row][col])  {
                        // System.out.println("\nMatched pair is: (" + row + ", " + col + ")");
                        // retrieve boys preferences
                        ArrayList boyPrefs = boysPrefQueue.nthElement(row);
                        // System.out.println("\nBoy's preferences: " + boyPrefs);
                        // find index of the boy's currently matched girl in boyPrefs list
                        int curMatchIndexOfBoy = boyPrefs.indexOf(col);

                        // System.out.println("\nIndex of the currently matched girl in boyPrefs list is: " + curMatchIndexOfBoy);

                        // for each girl who is ranked higher than the currently 
                        // matched girl, determine if such a girl prefers the boy
                        // over her current match 
                        for (int index = curMatchIndexOfBoy-1; index >= 0; index--){
                            int potentialMatchGirl = (int)boyPrefs.get(index);
                            // System.out.println("\nPotential match girl is: " + potentialMatchGirl);
                            // who is she currently matched with?
                            int curMatchOfPotentialGirl = 0;
                            for (int r = 1; r < matchedPairs.length; r++)
                                // if (matchedPairs[r][(int)boyPrefs.get(index)])
                                if (matchedPairs[r][potentialMatchGirl])
                                    curMatchOfPotentialGirl = r;
                            // System.out.println("\nThe current match of potential match girl is: " + curMatchOfPotentialGirl);

                            // retrieve potential match girls preferences list
                            int[] girlPrefs = girlsPrefArray[potentialMatchGirl];
                            // System.out.println("\npotentialMatchGirl preferences are: ");
                            // print1DArray(girlPrefs);

                            // preference rank of potential girl's current match
                            int index1 = findIndex(girlPrefs, curMatchOfPotentialGirl);
                            // preference rank of the boy proposing to the potential girl
                            int index2 = findIndex(girlPrefs, row);
                            // does the potential girl prefer the boy over her current match?
                            if (index2 < index1){
                                // System.out.println("\nThe boy and the potential match girl prefer each other over current matches");
                                System.out.println("\nViolation of stability principle");
                                // System.out.println("\nNot a stable match");
                                System.exit(1);
                            }
                        }
                    }
            System.out.println("\nStable match");
        } // task case: verifying whether a given match is stable

	} // end main()
}
