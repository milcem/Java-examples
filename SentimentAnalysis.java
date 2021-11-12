import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SentAnalysis {

    final static File TRAINFOLDER = new File("db_txt_files");
    static double num_neg = 0;
    static double num_pos = 0;
    static double pos_words = 0;
    static double neg_words = 0;
    static HashMap<String, Double> pos = new HashMap<String, Double>();
    static HashMap<String, Double> neg = new HashMap<String, Double>();
    
    public static void main(String[] args) throws IOException
    {    
        //ArrayList<String> files = readFiles(TRAINFOLDER);        
        train("train");
        
        //if command line argument is "evaluate", runs evaluation mode
        if (args.length==1 && args[0].equals("evaluate")){
            evaluate();
        }
        else{//otherwise, runs interactive mode
            @SuppressWarnings("resource")
            Scanner scan = new Scanner(System.in);
            System.out.print("Text to classify>> ");
            String textToClassify = "";
            textToClassify = scan.nextLine();
            System.out.println("Result: "+classify(textToClassify));
            
        }
        
    }
    

    
    /*
     * Takes as parameter the name of a folder and returns a list of filenames (Strings)
     * in the folder.
     */

    public static ArrayList<String> readFiles(File folder){
        
        System.out.println("Populating list of files");
        //List to store filenames in folder
        ArrayList<String> filelist = new ArrayList<String>();
        
    
        for (File fileEntry : folder.listFiles()) {
            String filename = fileEntry.getName();
            filelist.add(filename);
                //System.out.println(filename);
            }
        
        return filelist;
    }
    
    

    
    /*
     * TO DO
     * Trainer: Reads text from data files in folder datafolder and stores counts
     * to be used to compute probabilities for the Bayesian formula.
     * You may modify the method header (return type, parameters) as you see fit.
     */

    public static void train(String foldername) throws FileNotFoundException
    {
        File folder = new File(foldername);
        ArrayList<String> filenames = readFiles(folder);
        for (int i = 0; i < filenames.size(); i ++){
            File file = new File(filenames.get(i));
            if (file.getName().contains("-5-")){
                Scanner scan = new Scanner(new File(folder+"/"+file));
                num_pos++;
                while(scan.hasNext()){
                    String word = scan.next();
                    if (pos.containsKey(word)){
                        pos.put(word, pos.get(word)+1);
                    }
                    else{
                        pos.put(word,1.0);
                    }
                    pos_words++;
                    
                }
                scan.close();
            }
            else if (file.getName().contains("-1-")){
                Scanner scan = new Scanner(new File(folder+"/"+file));
                num_neg++;
                while(scan.hasNext()){
                    String word = scan.next();
                    if (neg.containsKey(word)){
                        neg.put(word, neg.get(word)+1);
                    }
                    else{
                        neg.put(word,1.0);
                    }
                }
                neg_words++;
                scan.close();
            }
            
            
            
        }
        
    }


    /*
     * TO DO
     * Classifier: Classifies the input text (type: String) as positive or negative
     * You may modify the method header (return type, parameters) as you see fit.
     */
    public static String classify(String text)
    {
        String result="positive";
        String[] words = text.split(" ");
        double prob_pos = num_pos/(num_pos+num_neg);
        double prob_neg = 1-prob_pos;
        double prob_word_p = 0.0;
        double prob_word_n = 0.0;
        double temp;
        double l = 0.0000899;
        for (int i = 0; i < words.length; i ++){
            if (pos.containsKey(words[i])){
                temp = (pos.get(words[i])+l)/(num_pos+pos_words*l);
                prob_word_p+=Math.log(temp);
//                System.out.println("p: "+prob_word_p);
            }
            else{
                prob_word_p+=Math.log(l/(num_pos+pos_words*l));
//                System.out.println("op: "+prob_word_p);
            }
            if (neg.containsKey(words[i])){
                temp = (neg.get(words[i])+l)/(num_neg+neg_words*l);
                prob_word_n+=Math.log(temp);
//                System.out.println("n: "+prob_word_n);
            }
            else{
                prob_word_n+=Math.log(l/(num_neg+neg_words*l));
//                System.out.println("on: "+prob_word_n);
            }
            
        }
        prob_pos *= Math.exp(prob_word_p);
        prob_neg *= Math.exp(prob_word_n);
//        System.out.println(prob_pos+ " " + prob_neg);
        if (prob_pos < prob_neg){
            result = "negative";
        }
        
        
        return result;
    }


    /*
     * TO DO
     * Classifier: Classifies all of the files in the input folder (type: File) as positive or negative
     * You may modify the method header (return type, parameters) as you see fit.
     */

    public static void evaluate() throws FileNotFoundException
    {
        @SuppressWarnings("resource")
        Scanner scan = new Scanner(System.in);
        
        System.out.print("Enter folder name of files to classify: ");
        String foldername = scan.nextLine();
        File folder = new File(foldername);
        
        ArrayList<String> filesToClassify = readFiles(folder);
        
        /*To Do
         * Classify the files in the specified folder as positive or negative.
         * Evaluate your system by outputting the following:
         * Accuracy: number of correctly classified reviews / total number of reviews
         * Precision: number correctly classified as positive / total classified as positive
         *               number correctly classified as negative / total classified as negative
         * You may (and probably should) create other methods to modularize this code.
         */
        String words;
        double fpos = 0;
        double pos = 0;
        double fneg = 0;
        double neg = 0;
        for (int i = 0; i < filesToClassify.size(); i ++){
            words = "";
            scan = new Scanner (new File(folder+"/"+filesToClassify.get(i)));
//            System.out.println(filesToClassify.get(i));
            while(scan.hasNextLine()){
                words+=scan.nextLine()+" ";
            }
            words = classify(words);
            if (words.startsWith("p")){
                if (filesToClassify.get(i).contains("-1-")){
//                    System.out.println("fpos "+filesToClassify.get(i));
                    fpos++;
                }
                else{
//                    System.out.println("pos "+filesToClassify.get(i));
                    pos++;
                }
            }
            else{
                if (filesToClassify.get(i).contains("-5-")){
//                    System.out.println("fneg "+filesToClassify.get(i));
                    fneg++;
                }
                else{
//                    System.out.println("neg "+filesToClassify.get(i));
                    neg++;
                }
            }
            
            scan.close();
        }
        double acc = (pos+neg)/(fpos+fneg+neg+pos);
        double pprec = (pos)/(pos+fpos);
        double nprec = (neg)/(neg+fneg);
        System.out.println("Accuracy:\t"+acc+"\nPrecision:\n  Positive:\t"+pprec+"\n  Negative:\t"+nprec);
    }
    
    
    
}
