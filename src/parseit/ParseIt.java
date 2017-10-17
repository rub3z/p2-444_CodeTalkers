package parseit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

/**
 *
 * @author rubes
 */
public class ParseIt {
   ArrayList<String> grammar;
   
   ArrayList<Rule> rules;
   ArrayList<Symbol> terminals;
   ArrayList<Symbol> nonTerminals;
   Rule[][] parseTable;

   Stack<Symbol> theStack;
   String input;
   
   public ParseIt() {
      grammar = new ArrayList<>();
      rules = new ArrayList<>();
      terminals = new ArrayList<>();
      nonTerminals = new ArrayList<>();
      
      
   }
   
   public class Node {
      ArrayList<Symbol> kids;
   }
   
   public class Symbol {
      String token;     // Only necessary for terminal Symbol
      String name;      
      ArrayList<Rule> kidRules; // Only for non-terminal Symbol
      
      // Non-terminal symbol constructor takes in a line of grammar
      public Symbol(String grammarLine, int num, int previousRuleNum) {
         kidRules = new ArrayList<>();
         name = grammarLine.split(" ")[0];
         
         String[] theRules = grammarLine.split(" \\= | \\| ");
         
         //Test by printing out
//         System.out.println("Printing the line of grammar passed to Symbol"
//          + "constructor; should be:\n" + grammarLine 
//          + "\nResult after split:");
//         for (String i : theRules) System.out.print(i + " ");
         System.out.println("");
         
         for(int i = 1; i < theRules.length; i++) {
            kidRules.add(new Rule(theRules[i], previousRuleNum + i));
            rules.add(new Rule(theRules[i], previousRuleNum + i));
         }
      }
      
      // Terminal Symbol constructor needs a name and token
      public Symbol(String tName) {
         name = tName;
         switch (tName) {
            case "kwdprog": token = "prog"; break;
            case "brace1": token = "{"; break;
            case "brace2": token = "}"; break;
            case "int": token = "[0-9]+"; break;
            
            default: break;
         }
      }
      
   }
   
   public class Rule {
      int rID;
      String leftHand;
      ArrayList<String> rightHand;
      
      public Rule(String ruleString, int ruleNum) {
         rightHand = new ArrayList<>();
         rID = ruleNum;
//         Print outs used for debugging 
//         System.out.println("String passed to Rule constructor:"
//          + ruleString + "\nNumber passed to Rule constructor: " + ruleNum);
         
         String[] symbols = ruleString.split(" ");
         
         leftHand = symbols[0];
//         Used for debugging
//         System.out.println(leftHand);
         
         for(int i = 1; i < symbols.length; i++) {
//            For debugging
//            System.out.println(symbols[i]);
            rightHand.add(symbols[i]);
         }
      }
   }
   
   public void makeNonTerminalSymbolsAndRules() {
      for(int i = 0; i < grammar.size(); i++) {
         nonTerminals.add(new Symbol(grammar.get(i), i, 0));
      }
   }
   
   public void makeTerminalSymbols() {
      String flattenedGrammar = "";
      for(String g : grammar) flattenedGrammar += g;
      
      Arrays.stream(flattenedGrammar.split(" "))
       .filter(s -> !Character.isUpperCase(s.charAt(0)))
       .filter(s -> !s.equals("|") && !s.equals("="))
       .distinct()
       .forEach(t -> {
          terminals.add(new Symbol(t));
       });
      
   }
   
   public void makeTable() {
      int numRows = grammar.size();
      int numColumns = 0;
      String flattenedGrammar = "";
      
      for(String g : grammar) flattenedGrammar += g;
      
      //numColumns = (int) 
//       Arrays.stream(flattenedGrammar.split(" "))
//       .filter(s -> !Character.isUpperCase(s.charAt(0)))
//       .filter(s -> !s.equals("|") && !s.equals("="))
//       .distinct()
//       .forEach(System.out::println);      
      
      
      parseTable = new Rule[numRows][numColumns];
      
      
      
      
   }
   
   public void printInfo() {
      System.out.println("Printing info for parser...");
      System.out.println("Printing list of Rules...");
      rules.stream().forEach(r -> {
         System.out.println("Rule number: " + r.rID 
          + "; Left Side Symbol for Rule: " + r.leftHand
          + "; Right-Hand Symbols: ");
         r.rightHand.stream().forEach(rh -> {
            System.out.print(rh + " ");
         });
         System.out.println("");
      });
      
      System.out.println("\nPrinting list of non-terminal Symbols...");
      nonTerminals.stream().forEach(nt -> {
         System.out.println("Symbol name: " + nt.name
          + "; Non-Terminal ID#: " + nonTerminals.indexOf(nt)
          + "; Kid Rule Numbers: ");
         nt.kidRules.stream().forEach(kr -> {
            System.out.print(kr.rID + " ");
         });
         System.out.println("");
      });
      
      System.out.println("\nPrinting list of terminal Symbols...");
      terminals.stream().forEach(t -> {
         System.out.println("Symbol name: " + t.name
          + "; Terminal ID#: " + terminals.indexOf(t)
          + "; Token: " + t.token);
      });
   }
   
   /**
    * @param args the command line arguments
    * @throws java.io.IOException
    */
   public static void main(String[] args) throws IOException {
      BufferedReader gin = new BufferedReader(new FileReader("grammar.txt"));
      
      ParseIt parser = new ParseIt();
      
      gin.lines().forEach(g -> {parser.grammar.add(g);});
      parser.makeTable();
      parser.makeNonTerminalSymbolsAndRules();
      parser.makeTerminalSymbols();
      parser.printInfo();
   }
}