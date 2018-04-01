package pkg;
import static pkg.TokenType.*;
import static pkg.ProdType.*;
import static pkg.Type.*;
import java.util.ArrayList;
import java.util.List;

public class SymbolTable<N> extends SyntaxTree<N>{
	public SyntaxTree<N> ast;
	public static List<Var> varList = new ArrayList<Var>();
	
	public SymbolTable(){
		super();
		this.ast = new SyntaxTree<N>();
	}
	
	public SymbolTable(N rootData, SyntaxTree<N> asTree){
		super(rootData);
		this.ast = asTree;
	}
	
	//adds a var to the current scope node
	public void currentNode_addVar(Type type, String id){
		Var v = new Var(type, id);
		List<Var> scopeVars = this.currentNode_getScopeVars();
		scopeVars.add(v);
		this.currentNode_setScopeVars(scopeVars);
	}
	
	public void addVar(Type type, String id, Node scope){
		Var v = new Var(type, id);
		List<Var> scopeVars = getScopeVars(scope);
		scopeVars.add(v);
		setScopeVars(scopeVars, scope);
	}
	
	//Cast the current node data from N to List<Vars> and returns result 
	public List<Var> currentNode_getScopeVars(){
		List<Var> vars = (List<Var>)this.currentNode.data;
		return vars;
	}
	
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
	
	public void compare(){
		
	}
	
	//take an id token and a scope node, returns true if id found in that node or and parent nodes
	public Node findId_InEntireScope(Token id, Node scope){
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
	public boolean findId_InScopeNode(Token id, Node scope){
		List<Var> scopeVars = this.getScopeVars(scope);	
		for(Var v : scopeVars){
			String storedId = v.getID();
			if(storedId.equals(id.getLit())){
				return true;
			}
		}
		return false;
	}
	
	public boolean assignIdValue(){
		boolean varAssigned = false;
		List<Var> scopeVars = this.currentNode_getScopeVars();
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
	}
	
	public void buildSymbolTable(){
		if(this.ast.currentNode.data == BLOCK){
			this.addBranchNode((N)varList);
		}
		else if(this.ast.currentNode.data == VAR_DECL){
			Token idTok = (Token)this.ast.currentNode.children.get(1).data;
			Token typeTok = (Token)this.ast.currentNode.children.get(0).data;
			
			//If the id is not found in the entire scope then we add the variable to the current scope node
			if(findId_InEntireScope(idTok, this.currentNode) == null){ 
				String id = idTok.getLit();
				Type type = null;
				if(typeTok.getLit().equals("int")){
					type = INT;
				}
				else if(typeTok.getLit().equals("string")){
					type = STRING;
				}
				else if(typeTok.getLit().equals("boolean")){
					type = BOOLEAN;
				}
				this.addVar(type, id, this.currentNode);
			}
		}
		else if(this.ast.currentNode.data == ASSIGN){
			
		}
	}
	/*-----------------|
	 *                 |
	 * Inference Rules |
	 *                 |
	 -----------------*/
	public static Type inferLiteralType(Token a){
		if(a.getType() == DIGIT){
			return INT;
		}
		if(a.getType() == STRINGLITERAL){
			return STRING;
		}
		if(a.getType() == BOOLVAL){
			return BOOLEAN;
		}
		return null;
	}
	
	//digit + digit
	public static Type inferIntExprType(List<Token> intExprList){
		if(inferLiteralType(a) == INT){
			if(inferLiteralType(b) == INT){
				return INT;
			}
		}
		return null;
	}
	
	//digit + id
	public static Type inferBoolOpType(){
		
		return null;
	}
}
