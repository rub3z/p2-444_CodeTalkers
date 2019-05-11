package theinterpreter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

/**
 * The ParseIt java class, modified to suit the needs of TheInterpreter.
 * For the instructor's convenience, new and newly modified methods have
 * been brought up towards the top of the file just below the ParseIt()
 * slots and constructor
 * 
 * These methods include a handy initializer for TheInterpreter class to use;
 * parse tree to abstract syntax tree conversion methods; a new-and-improved
 * parse machine method that actually creates a parse tree (!!!) and 
 * 
 * @author Ruben Baerga ID #010366978 in collaboration with
 * @author Eric Aguirre ID #009824605
 */
public class ParseIt {
   ArrayList<String> grammar;
   
   ArrayList<Rule> rules;
   ArrayList<Symbol> terminals;
   ArrayList<Symbol> nonTerminals;
   Rule[][] parseTable;

   Stack<Node> theStack;
   
   PST theTree;
   
   final Symbol STAHP = new Symbol("STAHP");
   
   public ParseIt() {
      grammar = new ArrayList<>();
      rules = new ArrayList<>();
      terminals = new ArrayList<>();
      nonTerminals = new ArrayList<>();
      
   }
   
////////////////////////////////////////////////////////////////////////
// ALL NEW!!! METHODS FOR PROJECT #3                                  //
////////////////////////////////////////////////////////////////////////

   // A handy-dandy initializer. It calls all of the necessary methods to
   // populate the data structures of the ParseIt class and sets up the 
   // LL table for parsing.
   public void init() throws FileNotFoundException {
      BufferedReader gin = new BufferedReader(new FileReader("grammar.txt"));
      
      gin.lines().forEach(g -> {grammar.add(g);});
      
      // Let's make our parser.
      makeNonTerminalSymbolsAndRules();
      makeTerminalSymbols();
      makeLLTable();
      
   }
   
   
   public void pst2ast() {
      pst2ast(theTree.mRoot);
   }
   
   private void pst2ast(Node rn) {
      if (rn == null) return;
      rn.kids.stream().forEach(i -> pst2ast(i));
      p2a_fix(rn);
   }
   
   private void p2a_fix(Node rmom) {
      if(rmom.s.name.equals("Pgm")) {
         p2aFix0(rmom);
      }
      if(rmom.s.name.equals("Vars")) {
         p2aFix1(rmom);
      }
      if(rmom.s.name.equals("Decl")) {
         p2aFix2(rmom);
      }
      if(rmom.s.name.equals("Stasgn")) {
         p2aFix3(rmom);
      }
      if(rmom.s.name.equals("Stmt")) {
         p2aFix4(rmom);
      }
      if(rmom.s.name.equals("Stprint")) {
         p2aFix5(rmom);
      }
      
   }
   
   // AST conversion for Pgm Non-T symbol.
   private void p2aFix0(Node rmom) {
      Node kid0 = rmom.kids.get(0);
      Node kid1 = rmom.kids.get(1);
      Node kid2 = rmom.kids.get(2);
      Node kid3 = rmom.kids.get(3);
      Node kid4 = rmom.kids.get(4);
      
      
      // brace1 is Vars' mom now.
      kid1.kids.add(kid2);
      kid2.mom = kid1;
      
      // brace1 is Stasgn's mama now.
      kid1.kids.add(kid3);
      kid3.mom = kid1;
      
      // Hoist brace1 to Pgm's spot.
      copyGuts(rmom, kid1);
   }
   
   // AST conversion for Vars Non-T symbol.
   private void p2aFix1(Node rmom) {
      Node kid0 = rmom.kids.get(0);
      Node kid1 = rmom.kids.get(1);
      Node kid2 = rmom.kids.get(2);
      
      // kwdvars is Decl's mommy now.
      kid0.kids.add(kid2);
      kid2.mom = kid0;
      
      // Hoist kwdvars to Vars' spot.
      copyGuts(rmom, kid0);
      
   }
   
   // AST conversion for Decl Non-T symbol.
   private void p2aFix2(Node rmom) {
      Node kid0 = rmom.kids.get(0);
      Node kid1 = rmom.kids.get(1);
      Node kid2 = rmom.kids.get(2);
      
      // 'int' is id's female parent now.
      kid0.kids.add(kid1);
      kid1.mom = kid0;
      
      // semicolon is 'int''s mama now.
      kid2.kids.add(kid0);
      kid0.mom = kid2;
      
      // Hoist semi to Decl's spot.
      copyGuts(rmom, kid2);
      
   }
   
   // AST conversion for Stasgn Non-T symbol.
   private void p2aFix3(Node rmom) {
      Node kid0 = rmom.kids.get(0);
      Node kid1 = rmom.kids.get(1);
      Node kid2 = rmom.kids.get(2);
      
      // equal is id's mother now.
      kid1.kids.add(kid0);
      kid0.mom = kid1;
      
      // equal is int's parental unit now.
      kid1.kids.add(kid2);
      kid2.mom = kid1;
      
      // Hoist equal to Stasgn's spot.
      copyGuts(rmom, kid1);
      
   }
   // For Stmt     
   private void p2aFix4(Node rmom) {
      Node kid0 = rmom.kids.get(0);
//      Node kid1 = rmom.kids.get(1);
//      Node kid2 = rmom.kids.get(2);

      // Hoist the only child to Stmt's spot
      copyGuts(rmom, kid0);

   }
   
   // For Stprint
   private void p2aFix5(Node rmom) {
      Node kid0 = rmom.kids.get(0);
      Node kid1 = rmom.kids.get(1);
      Node kid2 = rmom.kids.get(2);

   }
   
   
   private void copyGuts(Node rn, Node kn) {
      rn.kids = kn.kids;
      rn.mom = kn.mom;
      rn.token = kn.token;
      rn.s = kn.s;
   }
   
   // The parse machine method, modified to add Nodes to a tree.
   public void parseItUpm8(String line) {
      
      // SETUP: Make a stack...
      
      // ... and push the start symbol on to the tree as a Node.
      System.out.println("\nInitializing parse tree...");
      
      Node mama = new Node(nonTerminals.get(0));
      
      
      theStack = new Stack<>();
      theStack.push(new Node(STAHP));
      theStack.push(mama);
      
      theTree = new PST(mama);
      String token;
      
      System.out.println("Start symbol is: " + theStack.peek().s.name);
      
      String[] input = line.split(" ");
      
      int i = 0, j = 0;
      System.out.println("LET'S PARSE THIS BRO!!!");
      while(!theStack.empty()) {
         System.out.println("\nIteration: " + j++);
         System.out.println("Symbol at top of stack: " +theStack.peek().s.name);
         System.out.println("Token at front of input: " + input[i]);
         
         
         // M1: Check if the top of the stack matches the front.
         // If so, pop off the stack and move to the next token of input.
         if(input[i].matches(theStack.peek().s.token)) {
            System.out.println("M1: MATCH!!! Poppin' and droppin'...");
            
            mama = theStack.pop();
            token = input[i];
            i++;
            mama.token = token;
            
         } else {
            // M2: If the top of the stack is a terminal symbol after M1 didn't
            // happen, it means we've goofed up.
            if(terminals.contains(theStack.peek().s)) {
               System.out.println("M2: FAIL");
               return;
            }

            // The following could be done with less lines, but it'd probably
            // look no less - or much more - ugly. And of course, Rule #1.
            int tableRow = nonTerminals.indexOf(theStack.peek().s);
            int tableColumn = searchBySymbolToken(input[i]);
            System.out.println("Table row and column: " 
             + tableRow + " " + tableColumn);
            Rule cell = parseTable[tableRow][tableColumn];
            
            // M3: If the cell at the intersection of our symbol on top of the
            // stack and the symbol for the given token is empty; then we've
            // failed to parse.
            if(cell == null) {
               System.out.println("Code M3: NOPE");
               return;
            } else {

            // M4: If we didn't match anything and nothing went wrong; we pop
            // and toss the right-hand symbols of the cell rule on top of the
            // stack... in backwards order.
            System.out.println("M4: Poppin' and pushin', "
             + "Rule Number: " + rules.indexOf(cell));
            mama = theStack.pop();
            Node n;
               
            for(int k = cell.rightHand.size() - 1; k >= 0; k--) {
               // Our kind of naive search makes it necessary to check whether
               // the name of the symbol is uppercase - if it is, then it's
               // a non-terminal symbol. So we use the index returned from our
               // search to push from that list of symbols.
               
               if (Character.isUpperCase(cell.rightHand.get(k).charAt(0))) {
                  n = new Node(
                   nonTerminals.get(searchBySymbolName(cell.rightHand.get(k))));
                  System.out.println("Pushing " + theStack.push(n).s.name);
               }
                  
               else {
                  n = new Node(
                   terminals.get(searchBySymbolName(cell.rightHand.get(k))));
                  System.out.println("Pushing " + theStack.push(n).s.name);
               }
               n.mom = mama;
               mama.kids.add(0, n);
            }
            
            if (Character.isUpperCase(cell.leftHand.charAt(0))) {
               n = new Node(
                nonTerminals.get(searchBySymbolName(cell.leftHand)));
               System.out.println("Pushing " + theStack.push(n).s.name);
            }
            else {
               n = new Node(
                terminals.get(searchBySymbolName(cell.leftHand)));
               System.out.println("Pushing " + theStack.push(n).s.name);
            }
            
            n.mom = mama;
            mama.kids.add(0, n);
            
            }
         }
      }
      System.out.println("HOLY PARSING BATMAN! IT WORKED!");
   }
   
   // The parse machine method, modified to add Nodes to a tree.
   // This one is modified to not have any of the fun and exciting printouts.
   // Comments have also been removed, showing how sparse it actually is.
   // Still some unbelievably long and ugly lines towards the end though.
   // Oh well.
   public void parseItUpm8noPrintingPls(String line) {
      
      Node mama = new Node(nonTerminals.get(0));
      
      theStack = new Stack<>();
      theStack.push(new Node(STAHP));
      theStack.push(mama);
      
      theTree = new PST(mama);
      String token;
      String[] input = line.split(" ");
      
      int i = 0, j = 0;
      while(!theStack.empty()) {
         j++;
         if(input[i].matches(theStack.peek().s.token)) {
            mama = theStack.pop();
            token = input[i];
            i++;
            mama.token = token;
         } else {
            if(terminals.contains(theStack.peek().s)) return;
            
            int tableRow = nonTerminals.indexOf(theStack.peek().s);
            int tableColumn = searchBySymbolToken(input[i]);
            Rule cell = parseTable[tableRow][tableColumn];
            
            if(cell == null) return;
            else {

            mama = theStack.pop();
            Node n;
               
            for(int k = cell.rightHand.size() - 1; k >= 0; k--) {
               if (Character.isUpperCase(cell.rightHand.get(k).charAt(0))) {
                  n = new Node(
                   nonTerminals.get(searchBySymbolName(cell.rightHand.get(k))));
                  theStack.push(n);
               }
                  
               else {
                  n = new Node(
                   terminals.get(searchBySymbolName(cell.rightHand.get(k))));
                  theStack.push(n);
               }
               n.mom = mama;
               mama.kids.add(0, n);
            }
            
            if (Character.isUpperCase(cell.leftHand.charAt(0))) {
               n = new Node(
                nonTerminals.get(searchBySymbolName(cell.leftHand)));
               theStack.push(n);
            }
            else {
               n = new Node(
                terminals.get(searchBySymbolName(cell.leftHand)));
               theStack.push(n);
            }
            
            n.mom = mama;
            mama.kids.add(0, n);
            
            }
         }
      }
      System.out.println("Parsing done.");
   }
   
   
   // Now that we're actually making tree structures, it's pretty useful
   // to print out what they look like.
   public void printTree() {
      System.out.println("\nPrinting the tree...");
      printTree(theTree.mRoot);
   }
   
   private void printTree(Node n) {
      if (n == null) return;
      System.out.println(n.s.name + " " + "Token: " + n.token);
      for (int i = 0; i < n.kids.size(); i++) {
         System.out.print(n.s.name + " Kid " + i + ": ");
         printTree(n.kids.get(i));
      }
   }
 
///////////////////////////////////////////////////////////////////////////
// OLD METHODS AND CLASSES: UNCHANGED FROM PROJECT #2                    //
///////////////////////////////////////////////////////////////////////////
   
   public class PST {
      Node mRoot;
      
      public PST(Node n) {
         mRoot = n;
     }
   }
   
   public class Node {
      Symbol s;
      
      String token;
      Node mom;
      ArrayList<Node> kids;
      
      // A sneaky way of linking to a scope.
      int scopeNum;
      
      public Node(Symbol ns) {
         s = ns;
         kids = new ArrayList<>();
      }
   }
   
   // The Symbol class has Strings for its name and token,
   // as well as a list of Rules.
   public class Symbol {
      String token;     // Only necessary for terminal Symbol
      String name;      
      ArrayList<Rule> kidRules; // Only for non-terminal Symbol
      
      // Non-terminal symbol constructor takes in a line of grammar and an
      // otherwise useless int to distinguish it from the terminal constructor.
      public Symbol(String grammarLine, int dummy) {
         token = "";
         kidRules = new ArrayList<>();
         name = grammarLine.split(" ")[0];
         
         String[] theRules = grammarLine.split(" \\= | \\| ");
         
//         This snippet was used for testing the constructor.
//         System.out.println("Printing the line of grammar passed to Symbol"
//          + "constructor; should be:\n" + grammarLine 
//          + "\nResult after split:");
//         for (String i : theRules) System.out.print(i + " ");
//         System.out.println("");
         
         // The non-terminal Symbol constructor also calls the Rule
         // constructor, adding its new Rules to the list at the top.
         for(int i = 1; i < theRules.length; i++) {
            // Have to make this Rule to ensure it's the same Rule stuffed
            // into the lists. This way; a call to find the index of
            // a kidRule in the rules list won't return -1.
            Rule newRule = new Rule(theRules[i]);
            kidRules.add(newRule);
            rules.add(newRule);
         }
      }
      
      // Terminal Symbol constructor needs a name.
      public Symbol(String tName) {
         name = tName;
         switch (tName) {
            case "kwdprog"    :  token = "prog";   break;
            case "brace1"     :  token = "\\{";    break;
            case "brace2"     :  token = "\\}";    break;
            case "kwdvars"    :  token = "vars" ; break;      
//            case "eps"        :  token =  "blah" ; break;      
            case "parens1"    :  token =  "\\("; break;      
            case "parens2"    :  token =  "\\)"; break;      
            case "semi"       :  token =  ";" ; break;      
            case "\'int\'"    :  token =  "int" ; break;      
            case "\'float\'"  :  token =  "float" ; break;      
            case "\'string\'" :  token =  "string"; break;      
            case "id"     :  token =  "(_|[a-zA-Z])(_|[a-zA-Z]|[0-9])*"; break;      
            case "equal"      :  token =  "\\="; break;      
            case "kprint"     :  token =  "print"; break;      
            case "kwdwhile"   :  token =  "while"; break;      
            case "comma"      :  token =  "\\,"; break;      
            case "int"        :  token = "[0-9]+"; break;
            case "float"      :  token = "[0-9]+\\.[0-9]+";    break;
            case "string"     :  token = "\".*\""; break;
            case "opeq"       :  token = "\\=\\="; break;      
            case "opne"       :  token = "\\!\\="; break;      
            case "ople"       :  token = "\\<\\="; break;      
            case "opge"       :  token = "\\>\\="; break;      
            case "angle1"     :  token = "\\<"; break;      
            case "angle2"     :  token = "\\>"; break;      
            case "plus"       :  token = "\\+"; break;      
            case "minus"      :  token = "\\-"; break;    
            case "aster"      :  token = "\\*";    break;
            case "slash"      :  token = "/";    break;
            case "caret"      :  token = "\\^";    break;
            case "STAHP"      :  token = "\\$"; break;
            default: break;
         }
      }
      
      // Helper methods to identify Symbols by name or by token.
      public boolean hasName(String sName) {
         return sName.equals(name);
      }
      
      public boolean hasToken(String sToken) {
         return sToken.matches(token);
      }
      
   }
   
   // A Rule has a set of Strings which are the LHS and RHS Symbols.
   public class Rule {
      String leftHand;
      ArrayList<String> rightHand;
      
      public Rule(String ruleString) {
         rightHand = new ArrayList<>();

         String[] symbols = ruleString.split(" ");
         
         leftHand = symbols[0];
         
         for(int i = 1; i < symbols.length; i++)
            rightHand.add(symbols[i]); 
      }
   }
   
   // More private helper methods.
   // A simple way of finding whether a Symbol is non-terminal or not.
   private boolean isNonTerminalSymbol(String sName) {
      return Character.isUpperCase(sName.charAt(0));
   }
   
   // These are used to search for the index of a Symbol in either list
   // by providing its name or its token.
   private int searchBySymbolName(String sName) {
      for(Symbol i : terminals) 
         if (i.hasName(sName))
            return terminals.indexOf(i);
      for(Symbol i : nonTerminals) 
         if (i.hasName(sName))
            return nonTerminals.indexOf(i);
      return -1;
   }
   
   private int searchBySymbolToken(String sToken) {
      for(Symbol i : terminals) 
         if (i.hasToken(sToken))
            return terminals.indexOf(i);
      return -1;
   }
   
   // It does what it says.
   public void makeNonTerminalSymbolsAndRules() {
      for(int i = 0; i < grammar.size(); i++) {
         nonTerminals.add(new Symbol(grammar.get(i), i));
      }
   }
   
   // This is a weird one.
   // makeTerminalSymbols turns the entire grammar into one long String,
   // then splits it into a StringStream.
   // Using stream operators, it pulls it apart until a stream of distinct,
   // lowercase, non-operator Strings remain... the names of the terminal
   // symbols in the grammar; which are then added to the terminal Symbols list.
   public void makeTerminalSymbols() {
      String flattenedGrammar = "";
      for(String g : grammar) flattenedGrammar += g + " ";
      
      Arrays.stream(flattenedGrammar.split(" "))
       .filter(s -> !Character.isUpperCase(s.charAt(0)))
       .filter(s -> !s.equals("|") && !s.equals("="))
       .distinct()
       .forEach(t -> {
          terminals.add(new Symbol(t));
       });
   }
   
   // It does what it says.
   // It creates an LL parse table by looking at the LHS symbol of all of
   // the kidRules and checking if they match the given terminal symbol of
   // that column. It is capable of searching the kidRules of a LHS symbol
   // if it happens to be non-terminal, but if the LHS of its rule(s) are also
   // non-terminal it does nothing. So it's pretty naive in spite of its
   // potential for producing beautiful output.
   public void makeLLTable() {
      int numRows = grammar.size();
      int numColumns = terminals.size();
        
      parseTable = new Rule[numRows][numColumns];
      
      for(int i = 0; i < numRows; i++)
         for(int j = 0; j < numColumns; j++)
            for(Rule k : nonTerminals.get(i).kidRules)
               parseTable[i][j] = 
                digForTerminalSymbol(k, terminals.get(j).name);

      for(int i = 0; i < numRows; i++)
         for(int j = 0; j < numColumns; j++)
            for(Rule k : nonTerminals.get(i).kidRules)
               for(int l = 0; l < numRows; l++)
                  if(k.leftHand.equals(nonTerminals.get(l).name))
                     for(Rule m : nonTerminals.get(l).kidRules)
                        if(m.leftHand.equals(terminals.get(j).name))
                           parseTable[i][j] = k;
      
      for(int i = 0; i < numRows; i++)
         for(int j = 0; j < numColumns; j++)
            for(Rule k : nonTerminals.get(i).kidRules) 
               if (k.leftHand.equals(terminals.get(j).name))
                  parseTable[i][j] = k;
      
   }
   
   // A half-baked, recursive(!!!) attempt at solving the problem described 
   // for the method above. It works - it doesn't break everything - but 
   // apparently only in some special cases. A bit holistic. 
   public Rule digForTerminalSymbol(Rule rr, String tname) {
      if(rr.leftHand.equals(tname))
         return rr;
      if(isNonTerminalSymbol(rr.leftHand)) 
      {
         for(Rule i : 
          nonTerminals.get(searchBySymbolName(rr.leftHand)).kidRules) {
               return digForTerminalSymbol(i, tname);
         }
      }
      return null;
   }
   
   // It does what it says, and does so beautifully.
   public void printLLTable() {
      System.out.println("\nPrint parse table:");
      System.out.print("Term ID#  : ");
      for (int i = 0; i < terminals.size(); i++) 
         System.out.printf("%2d ", i);
      
      for (int i = 0; i < nonTerminals.size(); i++) {
         System.out.printf("\n%-10s: ", nonTerminals.get(i).name);
         for(int j = 0; j < terminals.size(); j++)
            System.out.printf("%2d ", rules.indexOf(parseTable[i][j]));
      }
      System.out.println("");
   }
   
   public void printInfo() {
      System.out.println("\nPrinting info for parser...");
      System.out.println("Printing list of Rules...");
      rules.stream().forEach(r -> {
         System.out.printf("Rule %-2d:\nLeft-Hand Symbol: %-10s;"
          + " Right-Hand Symbols: ", rules.indexOf(r), r.leftHand);
         r.rightHand.stream().forEach(rh -> {
            System.out.print(rh + " ");
         });
         System.out.println("");
      });
      
      System.out.println("\nPrinting list of non-terminal Symbols...");
      nonTerminals.stream().forEach(nt -> {
         System.out.printf("Symbol name: %-10s; Non-Terminal ID#: %-2d" 
          + "; Kid Rules: ", nt.name, + nonTerminals.indexOf(nt));
         nt.kidRules.stream().forEach(kr -> {
            System.out.print(rules.indexOf(kr) + " ");
         });
         System.out.println("");
      });
      
      System.out.println("\nPrinting list of terminal Symbols...");
      terminals.stream().forEach(t -> {
         System.out.printf("Symbol name: %-10s; Terminal ID#: %-2d; "
          + "Token: %-10s\n", t.name, terminals.indexOf(t), t.token);
      });
   }
   
// This was used to make a fake mini-AST to test out SCT methods.
//   public void fakeTree() {
//      theTree = new PST(new Node(nonTerminals.get(0)));
//      thePST.mRoot.mom = null;
//      thePST.mRoot.token = "prog";
//      
//      theTree.mRoot.kids.add(new Node(terminals.get(0)));
//      theTree.mRoot.kids.get(0).mom = theTree.mRoot;
//      theTree.mRoot.kids.get(0).token = "prog";
//      
//      theTree.mRoot.kids.add(new Node(terminals.get(1)));
//      theTree.mRoot.kids.get(1).mom = theTree.mRoot;
//      theTree.mRoot.kids.get(1).token = "{";
//      
//      theTree.mRoot.kids.add(new Node(terminals.get(2)));
//      theTree.mRoot.kids.get(2).mom = theTree.mRoot;
//      theTree.mRoot.kids.get(2).token = "7";
//      
//      theTree.mRoot.kids.add(new Node(terminals.get(3)));
//      theTree.mRoot.kids.get(3).mom = theTree.mRoot;
//      theTree.mRoot.kids.get(3).token = "}";
//      
//      
//   }
//   
}