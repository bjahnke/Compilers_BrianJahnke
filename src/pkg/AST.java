package pkg;
import java.util.ArrayList;
import java.util.List;

import pkg.Parser.Tree;

public class AST {
	private static List<Parser.Tree> progsListCST;
	private static List<Parser.Tree> progsListAST; 
	
	public AST(){
		progsListCST = Lexer.parsedProgsList;
		progsListAST = new ArrayList<Parser.Tree>();
		generateAST();
	}
	
	public void generateAST(){
		/*
		 * Stuff we care about:
		 * Block
		 * Print
		 * Assign
		 * VarDecl
		 * While 
		 * If
		 * Add (intExpr -> digit intop Expr)
		 * Compare (booleanExpr -> Expr boolop Expr)
		 * type
		 * char/id
		 * space
		 * digit
		 * boolop
		 * boolval
		 * intop
		 */
	}
}
