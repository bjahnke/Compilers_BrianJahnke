package pkg;
import static pkg.ProdType.*;
import static pkg.TokenType.*;
import static pkg.AbstractProd.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyntaxTree<N>{
	//Source: https://stackoverflow.com/questions/3522454/java-tree-data-structure
	//Helped with making a generic class tree
	protected Node<N> root;
	protected Node<N> currentNode;
	protected SyntaxTree<N> ast;
	
	//Important Stuff
	public static List<ProdType> branchSet = Arrays.asList(BLOCK, PRINT_STATEMENT, ASSIGNMENT_STATEMENT, VAR_DECL, WHILE_STATEMENT, IF_STATEMENT);
	
	public static List<ProdType> termSet = Arrays.asList(DIGITp, IDp, TYPEp, BOOLVALp); 
	//^Leaf Nodes only contain the token data not their token type, but since the CST is correct
	//I can just check for Terminals I want by looking at their parent.
	//^this comment is no longer true, but for now I will keep this code in use as it is still functional

	
	public SyntaxTree(){
		root = new Node<N>();
		root.data = null;
		root.children = new ArrayList<Node<N>>();
		currentNode = root;
	}
	public SyntaxTree(N rootData){
		root = new Node<N>();
		root.data = rootData;
		root.children = new ArrayList<Node<N>>();
		currentNode = root;
	}
	
	public static class Node<N>{
		protected N data;
		protected Node<N> parent;
		protected List<Node<N>> children;
		
		public Node(){
			this.data = null;
			this.parent = null;
			this.children = new ArrayList<Node<N>>();
		}
		
		public Node(N nType){
			this.data = nType;
			this.parent = null;
			this.children = new ArrayList<Node<N>>();
		}
		
		public String getNodeInfo(){
			return "" + this.data + ", " + this.parent + ", " + this.childrenToString();
			
		}
		public List<Node<N>> getChildren(){
			return this.children;
		}
		
		public N getData(){
			return this.data;
		}
		
		public Node<N> getParent(){
			return this.parent;
		}
		
		public void setChild(Node<N> node){
			this.children.add(node);
		}
		
		public void assignParent(Node<N> par){
			this.parent = par;
		}
		
		public String toString(){
			if(this.hasChildren() 
			|| this.data == STATEMENT_LIST
			|| this.data == CHAR_LIST
			|| this.data == BLOCK                  //may have no children in ast, Ex: {} since these tokens are not included
			|| this.data == PRINT_STATEMENT){      //may have no children in ast, Ex: "" since we are dropping the quotes
				return " <" + this.data + "> ";
			}
			else{
				Token tok = (Token)this.data;
				return " [" + tok.getLit() + "] ";
			}
		}
		
		public String childrenToString(){
			if(this.hasChildren()){
				String childrenStr = "(";
				for(Node<N> node : this.children){
					childrenStr += node.toString();
				}
				return childrenStr + ")";
			}
			return "";
		}
		
		public boolean hasChildren(){
			if(this.children != null && !this.children.isEmpty()){
				return true;
			}
			return false;
		}
	}
	
	public Node<N> getCurrentNode(){
		return this.currentNode;
	}
	
	public void setCurrentNode(Node<N> node){
		this.currentNode = node;
	}
	
	@SuppressWarnings("unchecked")
	public void addBranchNode(ProdType pEnum){
		Node<N> n = new Node<N>((N)pEnum);
		n.parent = this.currentNode;
		this.currentNode.children.add(n);
		this.currentNode = n;
	}
	
	public void addBranchNode(N genType){
		Node<N> n = new Node<N>(genType);
		n.parent = this.currentNode;
		this.currentNode.children.add(n);
		this.currentNode = n;
	}
	
	@SuppressWarnings("unchecked")
	public void addLeafNode(Token term){
		Node<N> n = new Node<N>((N)term);
		n.parent = this.currentNode;
		this.currentNode.children.add(n);
	}
	
	public void endChildren(){
		this.currentNode = this.currentNode.parent;
	}
	
	
	/*-----------------|
	 *                 |
	 * AST Generator   |
	 *                 |
	 -----------------*/
	public void initAndGenAST(){
		this.currentNode = this.currentNode.children.get(0);
		this.ast = new SyntaxTree((ProdType)this.currentNode.data);
		this.toAST();
		System.out.println("AST:\n");
		this.ast.printTree3("");
		this.ast.currentNode = this.ast.root;
		System.out.println("\n");
	}

	public void toAST(){
		Token strLitTok = new Token();
		if(this.currentNode.hasChildren()){
			if(branchSet.contains(this.currentNode.data) && !this.currentNode.parent.equals(this.root)){
				this.ast.addBranchNode((ProdType)this.currentNode.data);
				this.toASTchildren();
				this.ast.endChildren();
			}
			//if the production leads to a term we want, make an ast leaf node from the prod's child
			else if(termSet.contains(this.currentNode.data)){
				this.ast.addLeafNode((Token)this.currentNode.children.get(0).data);  
			}
			else if(this.currentNode.data == STRING_EXPR){
				strLitTok = this.getStringLit(strLitTok);
				this.ast.addLeafNode(strLitTok);
			}
			else if(this.currentNode.data == INT_EXPR || this.currentNode.data == BOOLEAN_EXPR){
				if(this.current_isAddingOrComparing()){
					if(operatorNode.data == INTOPp){
						this.ast.addBranchNode((N)ADD);
					}
					else if(operatorNode.data == BOOLOPp){
						Token operator = (Token)operatorNode.children.get(0).data;
						if(operator.getLit().equals("==")){
							this.ast.addBranchNode((N)COMPARE_EQ);
						}
						else if(operator.getLit().equals("!=")){
							this.ast.addBranchNode((N)COMPARE_NEQ);
						}
					}
					this.toASTchildren();
					this.ast.endChildren();
				}
				else{
					this.toASTchildren();
				}
			}
			else{
				this.toASTchildren();
			}
		}
		else{
			this.endChildren();
		}	
	}
	
	//helper function, applies toAST() to each child of the current node.
	public void toASTchildren(){
		for(Node<N> n : this.currentNode.children){
			this.currentNode = n;
			this.toAST();
		}
	}
	
	//if INT_EXPR or BOOL_EXPR children.size greater than 1 then we know that it must be adding/comparing.
	//might have been a good idea to store productions in cst as objects containing its rule number
	//so I can just it look up instead of trying to figure out.
	public boolean current_isAddingOrComparing(){
		if(this.currentNode.children.size() > 1){
			return true;
		}
		return false;
	}
	
	public Node<N> getOperatorNode(){
		for(Node<N> n : this.currentNode.children){
			if(n.data == INTOPp || n.data == BOOLOPp){
				return n;
			}
		}
		return null;
	}
	
	//helper function, called when STR_EXPR is found, traverses cst under the expr. Concatenates and returns
	//all chars and spaces found into a single token.
	public Token getStringLit(Token strTok){
		for(Node<N> n : this.currentNode.children){
			if(n.hasChildren()){
				if(n.data == CHAR_LIST){
					this.currentNode = n;
					this.getStringLit(strTok);
					this.endChildren();
				}
				else{
					Token childTok = (Token)n.children.get(0).data;
					if(n.data == SPACEp){
						strTok.setLit(strTok.getLit()+" ");
						strTok.setLineNum(childTok.getLineNum());
					}
					else if(n.data == CHARp){
						strTok.setLit(strTok.getLit()+childTok.getLit());
						strTok.setLineNum(childTok.getLineNum());
					}
				}
			}
		}
		return strTok;
	}
	/*-----------------|
	 *                 |
	 * Print Tree      |
	 *                 |
	 -----------------*/
	public void printTree(){
		List<Node<N>> root = new ArrayList<Node<N>>();
		root.add(this.currentNode);
		System.out.println(root.get(0).toString());
		printTree2(root);
	}
	
	//Takes a list of non terminals and prints their children
	public void printTree2(List<Node<N>> nTerms){ 
		List<Node<N>> nonTerms = nTerms;
		List<Node<N>> childNTerms = new ArrayList<Node<N>>();
		for(Node<N> node : nonTerms){
			//if(node.hasChildren()){
				System.out.print(node.childrenToString());
			//}
			if(node.hasChildren()){
				childNTerms.addAll(node.children);
			}
		}
		System.out.println("");
		if(!childNTerms.isEmpty()){
			printTree2(childNTerms);
		}
	}
	
	public void printTree3(String depth){
		if(this.currentNode.hasChildren()){
			System.out.println(depth + this.currentNode.toString());
			depth += "-";
			for(Node<N> node : this.currentNode.children){	
				this.setCurrentNode(node);
				printTree3(depth);
			}
		}
		else{
			System.out.println(depth + this.currentNode.toString());
			this.endChildren();
		}
	}
}	

