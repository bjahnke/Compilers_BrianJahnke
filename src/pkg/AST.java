package pkg;
import java.util.ArrayList;
import java.util.List;

public class AST {
	private static List<SyntaxTree> progsListCST;
	private static List<SyntaxTree> progsListAST; 
	
	public AST(){
		progsListCST = Lexer.parsedProgsList;
		progsListAST = new ArrayList<SyntaxTree>();
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
