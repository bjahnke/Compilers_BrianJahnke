package pkg;
import static pkg.TokenType.*;
import static pkg.ProdType.*;
import static pkg.Type.*;
import static pkg.AbstractProd.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pkg.SyntaxTree.Node;

public class SymbolTable<N> extends SyntaxTree<N>{
	protected int scopeCount;
	protected SyntaxTree<N> ast;
	protected static List<Var> sTable = new ArrayList<Var>();
	
	public SymbolTable(){
		super();
		this.ast = new SyntaxTree<N>();
	}
	
	public SymbolTable(N rootData, SyntaxTree<N> asTree){
		super(rootData);
		this.scopeCount = 0;
		this.root.setNodeNum(this.scopeCount);
		this.ast = asTree;
	}
	
	public void addBranchScope(){
		List<Var> varList = new ArrayList<Var>();
		super.addBranchNode((N)varList);
		this.scopeCount++;
		this.currentNode.setNodeNum(this.scopeCount);	
	}
	
	//Creates a var, then adds to scope
	public void addVar(Token type, Token id, Node<N> scope){
		Var v = new Var(type, id, scope.getNodeNum());
		List<Var> scopeVars = getScopeVars(scope);
		scopeVars.add(v);
		setScopeVars(scopeVars, scope);
		sTable.add(v);
	}
	
	//Cast the current node data from N to List<Vars> and returns result 
	
	public List<Var> getScopeVars(Node scope){
		List<Var> vars = (List<Var>)scope.data;
		return vars;
	}
	
	//For Casting back to N
	public void currentNode_setScopeVars(List<Var> vars){
		this.currentNode.data = (N)vars;
	}
	
	public void setScopeVars(List<Var> vars, Node scope){
		scope.data = (N)vars;
	}

	public boolean assignIdValue(){
		boolean varAssigned = false;
		List<Var> scopeVars = getScopeVars(this.currentNode);
		Token idTok = (Token)this.ast.currentNode.children.get(0).data;          //id token of assignment
		Token valTok = (Token)this.ast.currentNode.children.get(1).data;		 //value token of assignment
		Node scopeOfId = findId_InEntireScope(idTok, this.currentNode); 
		
		if(scopeOfId != null){
			Type idType = inferLiteralType(idTok);
			/* Pseudo:
			 * var a: has the type of the id
			 * var b: something that describes the expr being assigned to the id
			 * if(compare(a, b) such that 
			 * */
			
		}
		return false;
	}
	
	public boolean analyzeNode(Node<N> node){
		
		if(node.data == VAR_DECL){
			Token typeTok = (Token)node.children.get(0).data;
			Token idTok = (Token)node.children.get(1).data;
					
			//If the id is not found in the scope then we add the variable to the current scope node
			if(!findId_InScopeNode(idTok, this.currentNode)){ 
				this.addVar(typeTok, idTok, this.currentNode);
				return true;
			}
			else{
				//Redeclared Identifier in same scope
				return false;
			}
		}
		else if(node.data == ASSIGN){
			Token idTok = (Token)node.children.get(0).data;
			Token SomeTok = (Token)node.children.get(1).data;
			if(assignIdValue()){
				return true;
			}
			else{
				//error prints in assignIdValue() method;
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	public boolean buildSymbolTable(Node<N> parent){
		
		for(Node<N> child : parent.children){
			if(child.data == BLOCK){
				this.addBranchScope();
				return buildSymbolTable(child);
			}
			if(child.hasChildren()){
				if(!analyzeNode(child)){
					return false;
				}
			}
		}
		return true;
	}
	
	/*-----------------|
	 *                 |
	 * Scope Check     |
	 *                 |
	 -----------------*/
	//take an id token and a scope node, returns true if id found in that node or and parent nodes
	public Node<N> findId_InEntireScope(Token id, Node<N> scope){
		if(findId_InScopeNode(id, scope)){
			return scope;
		}
		if(!scope.equals(this.root)){
			return findId_InEntireScope(id, scope.parent);
		}
		else{
			return null;
		}
	}
	
	//takes an id token and a scope node, returns true if id found in that node
	public boolean findId_InScopeNode(Token id, Node<N> scope){
		List<Var> scopeVars = this.getScopeVars(scope);	
		for(Var v : scopeVars){
			String storedId = v.getID();
			if(storedId.equals(id.getLit())){
				return true;
			}
		}
		return false;
	}
	
	
	/*-----------------|
	 *   Print Symbol  |
	 *      Table      |
	 *                 |
	 -----------------*/
	
	public static void printSymbolTable(){
		System.out.println("Program " + Lexer.progNum + " Symbol Table\n"
						+	"-----------------------------\n"
						+	"Name\tType\tScope\tLine\n"
						+	"-----------------------------");
		for(Var v : sTable){
			System.out.println(v.toString());
		}
		System.out.println("\n");
		sTable.clear();
	}
	
	/*-----------------|
	 *                 |
	 * Inference Rules |
	 *                 |
	 -----------------*/
	public static Type inferLiteralType(Token a){
		//if()
			if(a.getType() == DIGIT){
				return INT;
			}
			if(a.getType() == STRINGLITERAL){
				return STRING;
			}
			if(a.getType() == BOOLVAL){
				return BOOLEAN;
			}
			if(a.getType() == ID){
				//if(findID(a) != null)
				//return a.getType;
			}
		return null;
	}
	
	//digit + digit || digit + id
	public static Type inferIntExprType(Node a, Node b){
		if(inferLiteralType((Token)a.data) == INT){
			if(inferLiteralType((Token)b.data) == INT){
				return INT;
			}
		}
		return null;
	}
	
	//digit + id
	public static Type inferIntExprType(Node a, Var b){return null;}
	
	public static Type inferBoolOpType(){
		
		return null;
	}
}
