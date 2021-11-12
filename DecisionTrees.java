/*
 * Milka Murdjeva
 * Decision Trees
 */

import java.util.ArrayList;
import java.util.Arrays;


public class DecisionTree {
    TreeNode root = null;
    
    //Example[] pos/neg are boolean arrays corresponding to whether or not the example
    //has the feature. These arrays are set in the driver TestClassifier.
    public void train(Example[] positive, Example [] negative){
        root = new TreeNode();
        root.setPos(positive);
        root.setNeg(negative);
        root.setParent(null);
        root.setDecision();
        root.setIsLeaf();
        
        if(!root.isLeaf){
            train(root);
        }
        
    }
    
    //The recursive train method that builds a tree at node
    private void train(TreeNode node){
        
        double max = -2;
        double temp;
        int index = -1;
        for (int i = 0; i < node.featuresUsed.length; i ++){
            if (!node.featuresUsed[i]){
                temp = getRemainingInfo(i, node);
                if (temp > max){
                    max = temp;
                    index = i;
                }
            }
            
        }
        if (index > -1){
            node.setFeature(index);
            createSubChildren(node);
            if (!node.trueChild.isLeaf){
                train(node.trueChild);
            }
            if (!node.falseChild.isLeaf){
                train(node.falseChild);
            }
        }
        else{
            node.setIsLeaf();
            node.setDecision();
        }
        
        
        
    }
    
    //Creates the true and false children of node
    private void createSubChildren(TreeNode node){
        Example[] pos = node.getPos();
        Example[] neg = node.getNeg();
        
        TreeNode tru = new TreeNode();
        TreeNode fals = new TreeNode();
        tru.setParent(node);
        fals.setParent(node);
        tru.setFeaturesUsed();
        fals.setFeaturesUsed();
        int offset = 0;
        
        Example[] truPos = new Example[pos.length];
        if (pos[0] != null){
            for (int i = 0; i < pos.length; i ++){
                if (pos[i].get(node.getFeature())){
                    truPos[i - offset] = pos[i];
                }
                else{
                    offset++;
                }
            }
        }
        
        truPos = Arrays.copyOfRange(truPos, 0, pos.length-offset);
        tru.setPos(truPos);
        offset = 0;
        Example[] truNeg = new Example[neg.length];
        if (neg[0] != null){
            for (int i = 0; i < neg.length; i ++){
                if (neg[i].get(node.getFeature())){
                    truNeg[i - offset] = neg[i];
                }
                else{
                    offset++;
                }
            }
        }
        truNeg = Arrays.copyOfRange(truNeg, 0, neg.length-offset);
        tru.setNeg(truNeg);
        offset = 0;
        Example[] falsPos = new Example[pos.length];
        if (pos[0] != null){
            for (int i = 0; i < pos.length; i ++){
                if (!pos[i].get(node.getFeature())){
                    falsPos[i - offset] = pos[i];
                }
                else{
                    offset++;
                }
            }
        }
        falsPos = Arrays.copyOfRange(falsPos, 0, pos.length-offset);
        fals.setPos(falsPos);
        offset = 0;
        Example[] falsNeg = new Example[neg.length];
        if (neg[0] != null){
            for (int i = 0; i < neg.length; i ++){
                if (!neg[i].get(node.getFeature())){
                    falsNeg[i - offset] = neg[i];
                }
                else{
                    offset++;
                }
            }
        }
        falsNeg = Arrays.copyOfRange(falsNeg, 0, neg.length-offset);
        fals.setNeg(falsNeg);
        
        tru.setDecision();
        fals.setDecision();
        tru.setIsLeaf();
        fals.setIsLeaf();
        node.trueChild = tru;
        node.falseChild = fals;
    }
    
    //Computes and returns the remaining info needed if feature is chosen
    //at node.
    private double getRemainingInfo(int feature, TreeNode node){
        double s1 = (node.getPos().length + node.getNeg().length)*getEntropy(node.getPos().length, node.getNeg().length);
        double s2 = 0;
        double s3;
        TreeNode copy = new TreeNode();
        copy.setPos(node.getPos());
        copy.setNeg(node.getNeg());
        copy.setParent(node);
        copy.setFeaturesUsed();
        copy.setFeature(feature);
        copy.setDecision();
        copy.setIsLeaf();
        if (!copy.isLeaf){
            createSubChildren(copy);
            s2 = (copy.trueChild.getPos().length + copy.trueChild.getNeg().length)*getEntropy(copy.trueChild.getPos().length, copy.trueChild.getNeg().length);
            s2 += (copy.falseChild.getPos().length + copy.falseChild.getNeg().length)*getEntropy(copy.falseChild.getPos().length, copy.falseChild.getNeg().length);
        }
        s3 = s1 - s2;
        
        return s3;
        
    }
    
    //Computes and returns the entropy given the number of positive and
    //negative examples.
    private double getEntropy(double numPos, double numNeg){
        if(numPos==0 || numNeg ==0)
            return 0;
        double q = numPos/(numPos + numNeg);
        
        return -q*log2(q)-(1-q)*log2(1-q);    
    }
    
    //Computes and returns the log (base 2) of d. Used by the getEntropy method.
    private double log2(double d){
        return Math.log(d)/Math.log(2);
    }
    
    //Uses the tree to classify the given example as positive (true) or negative (false).
    public boolean classify(Example e){
        TreeNode node = root;
        while(!node.isLeaf){
            if (e.get(node.getFeature())){
                if (node.trueChild != null)
                node = node.trueChild;
            }
            else{
                if (node.falseChild != null)
                node = node.falseChild;
            }
            if (node.falseChild == null && node.trueChild == null)
                node.isLeaf = true;
        }
        return node.decision;
        
    }
    
    
    //Prints the decision tree.
    public void print(){
        printTree(root, 0);
    }
    
    
    //Called by print() to print the decision tree.
    private void printTree(TreeNode node, int indent){
        if(node== null)
            return;
        if(node.isLeaf){
            if(node.decision)
                System.out.println("Positive");
            else
                System.out.println("Negative");
        }
        else{
            System.out.println();
            doIndents(indent);
            System.out.print("Feature "+node.getFeature() + " = True:");
            printTree(node.trueChild, indent+1);
            doIndents(indent);
            System.out.print("Feature "+node.getFeature() + " = False:");
            printTree(node.falseChild, indent+1);
        }
    }
    
    //Called by printTree to print out indentations.
    private void doIndents(int indent){
        for(int i=0; i<indent; i++)
            System.out.print("\t");
    }
}