package pkg;
import static pkg.ProdType.*;
import static pkg.TokenType.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AST<N> extends SyntaxTree<N>{
	private static List<SyntaxTree> progsListCST;
	private static List<SyntaxTree> progsListAST;
	public static List<ProdType> branchSet = Arrays.asList(BLOCK, PRINT_STATEMENT, ASSIGNMENT_STATEMENT, VAR_DECL, WHILE_STATEMENT, IF_STATEMENT);
	public static List<TokenType> branchSetTerms = Arrays.asList(INTOP, BOOLOP);   //if we find these terminals then we must have ADD/COMPARE in the AST
	
	public AST(){	
		super();
	}
	
	public void processProgList(){
		for(SyntaxTree prog : this.progsListCST){
			progsListAST.add(this.generateAST(prog));
		}
	}
	
	public SyntaxTree generateAST(SyntaxTree tree){
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
		 * typep
		 * charp/idp
		 * spacep
		 * digitp
		 * boolopp
		 * boolvalp
		 * intopp
		 */
		if(tree.currentNode.data == PROGRAM){
			
			tree.currentNode = (Node<N>) tree.currentNode.children.get(0);       //If we are at program set current node to block node
			System.out.println(tree.currentNode.data);
		}
		if(branchSet.contains(this.currentNode.data)){
			System.out.println(tree.currentNode.data);
			//astTree.addBranchNode(this.currentNode.data);
		}
		return null;
	}
	
	//public void toAST(){
		//if(tree.currentNode.data == PROGRAM){
		//	this.setCurrentNode(this.currentNode.children.get(0));
		//}
	//	if(AST.branchSet.equals(this.currentNode.data)){
		//	System.out.println(this.currentNode.data);
			//astTree.addBranchNode(this.currentNode.data);
		//}
	//}
	
	public int match(ProdType cProd){
		
		if(branchSet.contains(cProd)){
			return branchSet.indexOf(cProd);
	
		}
		return 0;
	}
}
