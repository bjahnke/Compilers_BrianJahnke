package pkg;
import static pkg.TokenType.*;
import static pkg.ProdType.*;
import static pkg.Type.*;
import static pkg.AbstractProd.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
		v.setScopeListIndex(scopeVars.size());
		scopeVars.add(v);
		setScopeVars(scopeVars, scope);
		v.setSymbolTableIndex(sTable.size());
		sTable.add(v);
	}
	
	public void updateVarIsUsed(Var v){
		v.varIsUsed();
		Node<N> scope = getScopeByNumber(v.getscopeNum(), this.root);
		List<Var> scopeVars = getScopeVars(scope);
		scopeVars.set(v.getScopeListIndex(), v);
		sTable.set(v.getSymbolTableIndex(), v);
	}
	
	public void updateVarIsInit(Var v){
		v.varIsInit();
		Node<N> scope = getScopeByNumber(v.getscopeNum(), this.root);
		List<Var> scopeVars = getScopeVars(scope);
		scopeVars.set(v.getScopeListIndex(), v);
		sTable.set(v.getSymbolTableIndex(), v);
	}
	
	public Node<N> getScopeByNumber(int num, Node<N> scope){	
		for(Node<N> childScope : scope.children){
			if(childScope.getNodeNum() == num){
				return childScope;
			}
			if(childScope.hasChildren()){
				return getScopeByNumber(num, childScope);
			}
		}
		return null;
	}
	
	//Cast the current node data from N to List<Vars> and returns result 
	
	public List<Var> getScopeVars(Node<N> scope){
		List<Var> vars = (List<Var>)scope.data;
		return vars;
	}
	
	//For Casting back to N
	public void currentNode_setScopeVars(List<Var> vars){
		this.currentNode.data = (N)vars;
	}
	
	public void setScopeVars(List<Var> vars, Node<N> scope){
		scope.data = (N)vars;
	}


	
	public boolean analyzeNode(Node<N> node){
		if(node.data == VAR_DECL){
			Token typeTok = (Token)node.children.get(0).data;
			Token idTok = (Token)node.children.get(1).data;
					
			//If the id is not found in the scope then we add the variable to the current scope node
			if(findId_InScopeNode(idTok, this.currentNode) == null){ 
				this.addVar(typeTok, idTok, this.currentNode);
				return true;
			}
			else{
				System.out.println("Error: Redeclared Identifier '" + idTok.getLit() +
									"' , line: " + idTok.getLineNum());
				return false;
			}
		}
		else if(node.data == ASSIGNMENT_STATEMENT){
			Node<N> idNode = node.children.get(0);
			Node<N> assignExpr = node.children.get(1);
			if(assignTypeCheck(idNode, assignExpr)){
				return true;
			}
		}
		else if(node.data == IF_STATEMENT || node.data == WHILE_STATEMENT){
			if(inferExprType(node.children.get(0)) == BOOLEAN){               //boolExpr location
				buildSymbolTable(node.children.get(1)); //return this
				return true;
			}
		}
		else if(node.data == PRINT_STATEMENT){
			Node<N> exprInPrint = node.children.get(0);
			if(inferExprType(exprInPrint) != null){
				return true;
			}
		}
		return false;
	}
	
	public boolean buildSymbolTable(Node<N> parent){
		
		for(Node<N> child : parent.children){
			if(child.data == BLOCK){
				this.addBranchScope();
				if(!buildSymbolTable(child)){
					return false;
				}
				this.endChildren();
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
	//take an id token and a scope node, returns node the id was found in
	public Var findId_InEntireScope(Token id, Node<N> scope){
		Var foundVar = findId_InScopeNode(id, scope);
		if(foundVar != null){
			return foundVar;
		}
		if(!scope.equals(this.root)){
			return findId_InEntireScope(id, scope.parent);
		}
		else{
			return null;
		}
	}
	
	//takes an id token and a scope node, returns true if id found in that node
	public Var findId_InScopeNode(Token id, Node<N> scope){
		List<Var> scopeVars = this.getScopeVars(scope);	
		for(Var v : scopeVars){
			String storedId = v.getID();
			if(storedId.equals(id.getLit())){
				return v;
			}
		}
		return null;
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
	public Type inferLiteralType(Token a){
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
			Var matchedVar = findId_InEntireScope(a, this.currentNode);
			if(matchedVar != null){
				return matchedVar.getType();
			}
			else{
				System.out.println("Error: Id '" +a.getLit()+ "' used but never declared, line: " + a.getLineNum());
				return null;
			}

		}
		return null;
	}
	
	
	//digit+Add || digit + digit || digit + id
	public Type inferAddType(Node<N> a, Node<N> b){
		Type aType = null;
		Type bType = null;
		if(b.data == ADD){
			Node<N> bChild1 = b.children.get(0);
			Node<N> bChild2 = b.children.get(1);
			inferAddType(bChild1, bChild2);
		}
		else{
			aType = inferLiteralType((Token)a.data);
			bType = inferLiteralType((Token)a.data);
			if(aType == INT){
				if(bType == INT){         
					return INT;            
				}
			}	
		}	
		id_typeMismatchError(a, INT, bType);
		return null;
	}
	
	public Type inferBoolOpType(Node<N> a, Node<N> b){
		Type aType = inferExprType(a);
		Type bType = inferExprType(b);
		if(aType == bType){
			return BOOLEAN;
		}
		if(aType != null && bType != null){   //if either are null then we already printed an error
			System.out.println("Type Mismatch Error: Cannot compare "+aType+" to "+bType);
		}
		return null;
	}
	
	public Type inferExprType(Node<N> a){
		Node<N> aChild1;
		Node<N> aChild2;
		if(a.data == ADD){
			aChild1 = a.children.get(0);
			aChild2 = a.children.get(1);
			return inferAddType(aChild1, aChild2);
		}
		else if(a.data == COMPARE_EQ || a.data == COMPARE_NEQ){
			aChild1 = a.children.get(0);
			aChild2 = a.children.get(1);
			return inferBoolOpType(aChild1, aChild2);
		}
		else{
			//shouldn't cause a cast exception unless this is called improperly
			return inferLiteralType((Token)a.data);
		}
	}
	
	public boolean assignTypeCheck(Node<N> idNode, Node<N> assignExprNode){
		Type idNodeType = inferLiteralType((Token)idNode.data);
		Type assignExprNodeType = inferExprType(assignExprNode);
		if(idNodeType == assignExprNodeType){
			Var matchedVar = findId_InEntireScope((Token)idNode.data, this.currentNode);
			updateVarIsInit(matchedVar);
			return true;
		}
		id_typeMismatchError(idNode, idNodeType, assignExprNodeType);
		return false;
	}
	
	
	//Mismatch Error Print
	public void id_typeMismatchError(Node<N> idNode, Type expectedType, Type nodeType){
		Token tok = (Token)idNode.data;
		System.out.println("Type Mismatch Error: " + tok.getLit() 
							+ " is of type " + expectedType +" but was assigned a " + nodeType + ", line: " + tok.getLineNum());
	}
	
	public void symbolTableWarnings(){
		String warningStr = "\nWarnings:\n";
		boolean warnings = false;
		for(Var v : sTable){
			if(v.isInit()){
				if(!v.isUsed()){
					warningStr += "\t-Var " + v.getID() +" is initialized but unused, line of decl: "+v.getIdTok().getLineNum()+"\n";
					warnings = true;
				}
			}
			else if(v.isUsed()){
				warningStr += "\t-Var " + v.getID() +" is not initialized but used, line of decl: "+v.getIdTok().getLineNum()+"\n";
				warnings = true;
			}
			else{
				warningStr = "\t-Var " + v.getID() +" is declared but not initialized, line of decl: "+v.getIdTok().getLineNum()+"\n";
				warnings = true;
			}
		}
		if(warnings){
			System.out.println(warningStr);
		}
		
	}
	
}
