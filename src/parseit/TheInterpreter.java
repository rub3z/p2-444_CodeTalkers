/*
 * 
 */
package theinterpreter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import theinterpreter.ParseIt.*;

/**
 * TheInterpreter. The final project.
 * Has classes for a scope tree, scope nodes, and a list of entries (or a 
 * symtab) within those nodes as described in lecture. For simplicity's sake
 * all of the method names were ripped straight from lecture too.
 * It runs... an assignment statement. We're really proud of it.
 * 
 * @author Ruben Baerga ID #010366978 in collaboration with
 * @author Eric Aguirre ID #009824605
 */
public class TheInterpreter {
   
   SCT theSCT;

   public class SCT {
      SNode mRoot;
      ArrayList<SNode> sTree;
      
      public SCT() {
         mRoot = new SNode(null, 0);
         sTree = new ArrayList<SNode>();
         sTree.add(mRoot);
      }
   }
   
   public class SNode {
      int num;
      SNode mom;
      ArrayList<SNode> kids;
      
      ArrayList<Entry> symtab;
      
      
      public SNode(SNode newMom, int newNum) {
         num = newNum;
         mom = newMom;
         kids = new ArrayList<>();
         symtab = new ArrayList<>();
      }
   }
   
   public class Entry {
      String id;
      Node decl;
      String value;

      public Entry(String theID, Node theDecl) {
         id = theID;
         decl = theDecl;
      }
   }
   
   public void ast2sct (PST ast) {
      theSCT = new SCT();
      walkAST(ast.mRoot, theSCT.mRoot);
   }
   
   private void walkAST(Node ra, SNode rs) {
      System.out.println("Walk " + ra.s.name + "...");
      if (ra == null) return;
      
      if (isBlock(ra)) 
         doBlock(ra, rs);
      
      else if (isDecl(ra)) 
         doDecl(ra, rs);
      else if (isUse(ra)) 
         doUse(ra, rs);
      else
         ra.kids.forEach((kid) -> {
            walkAST(kid, rs);
         });
   }
   
   public boolean isBlock(Node ra) {
       return ra.s.hasName("brace1");
   }

   public void doBlock(Node ra, SNode sMom) {
      System.out.println("Block! Create New Scope...");
      SNode skid = new SNode(sMom, sMom.num + 1);
      sMom.kids.add(skid);
      theSCT.sTree.add(skid);
      
      ra.kids.forEach((kid) -> {
         walkAST(kid, skid);
      });
      
   }

   public boolean isDecl(Node ra) {
      return 
       ra.s.hasName("\'int\'");
   }

   public void doDecl(Node ra, SNode rs) {
      System.out.print("Decl! Adding entry to symtab in ");
      System.out.println("scope " + rs.num + "...");
      Entry ex = new Entry(ra.kids.get(0).token, ra);
      rs.symtab.add(ex);
      
   }

   public boolean isUse(Node ra) {
      return ra.s.hasName("id");
   }

   public void doUse(Node ra, SNode rs) {
      System.out.println("Use! Linking to symtab entry...");
      
      System.out.print("Searching symtab");   
      for(Entry e : rs.symtab) {
         System.out.print(".");
         if (e.id.equals(ra.token)) {
            System.out.print("\nFound id token... ");
            ra.scopeNum = rs.num;
            System.out.println("Scope is: " + ra.scopeNum);
         }
      }
      
      ra.kids.forEach((kid) -> {
         walkAST(kid, rs);
      });
      
   }
   
   public void runAST(Node ra) {
      if (ra == null) return;
      System.out.println("Run " + ra.s.name + "...");
      ra.kids.forEach((kid) -> {
         runAST(kid);
      });
      
      switch(ra.s.name) {
         case "equal":
            runAssign(ra);
            break;
         default:
            break;
      }
   }
   
   private void runAssign (Node ra) {
      runAST(ra.kids.get(1));
      System.out.println("Assign!");
      
      for(Entry e : theSCT.sTree.get(ra.kids.get(0).scopeNum).symtab)
         if (e.id.equals(ra.kids.get(0).token)) {
            e.value = ra.kids.get(1).token;
            break;
         }
   }

   public void printSCT() {
      System.out.println("\nPrinting the SCT...");
      printSCT(theSCT.mRoot);
   }
   
   private void printSCT(SNode sn) {
      System.out.println("Scope " + sn.num + ":");
      sn.symtab.forEach((i) -> {
         System.out.println("   Entry " + sn.symtab.indexOf(i) + "  "
          + "ID: " + i.id + "  Symbol: " + i.decl.s.name + "  "
           + "Value: " + i.value);
      });
      
      sn.kids.forEach(s -> {printSCT(s);});
   }
   
   
   /**
    * @param args the command line arguments
    * @throws java.io.FileNotFoundException
    */
   public static void main(String[] args) throws FileNotFoundException, IOException {
      BufferedReader input = new BufferedReader(new FileReader("input.txt"));
      
      ParseIt parser = new ParseIt();
      parser.init();
      
      System.out.println(""
       + "Would you like to print out info showing the rules, symbols, the \n"
       + "parse table, and the parse machine run? It's nice if you'd like an\n"
       + "exhaustive view of how the parser class works.\n"
       + "Press 1 for yes and 0 for no.");
      
      Scanner in = new Scanner(System.in);
      if(in.nextInt() == 1) {
         System.out.println("Alrighty then.");
         parser.printInfo();
         parser.printLLTable();
         parser.parseItUpm8(input.readLine());
      }
      else {
         System.out.println("Cool.");
         parser.parseItUpm8noPrintingPls(input.readLine());
      }
        
      parser.printTree();

      parser.pst2ast();      
      
      System.out.println("\nConverting to AST...");
      parser.printTree();
      
      System.out.println("\nWalking AST to make SCT...");
      TheInterpreter scoper = new TheInterpreter();
      
      scoper.ast2sct(parser.theTree);
      
      scoper.printSCT();
      
      
      System.out.println("\n(Attempting to) run the AST...");
      scoper.runAST(parser.theTree.mRoot);
      
      scoper.printSCT();
      
   }
}
