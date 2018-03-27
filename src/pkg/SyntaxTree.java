package pkg;
import static pkg.ProdType.*;
import static pkg.TokenType.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyntaxTree<N>{
	//Source: https://stackoverflow.com/questions/3522454/java-tree-data-structure
	//Helped with making a generic class tree
	protected Node<N> root;
	protected Node<N> currentNode;
	protected SyntaxTree<N> ASTree;
	private SyntaxTree<N> ast;
	public static List<ProdType> branchSet = Arrays.asList(BLOCK, PRINT_STATEMENT, ASSIGNMENT_STATEMENT, VAR_DECL, WHILE_STATEMENT, IF_STATEMENT);
	//^Important Stuff
	public static List<ProdType> termSet = Arrays.asList(DIGITp, IDp, TYPEp); 
	//^Leaf Nodes only contain the token data not their token type, but since the CST is correct
	//I can just check for Terminals I want by looking at their parent.

	
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
			if((this.children != null && !this.children.isEmpty()) 
			|| this.data == STATEMENT_LIST
			|| this.data == CHAR_LIST){
				return " <" + this.data + "> ";
			}
			else{
				return " [" + this.data + "] ";
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
	
	@SuppressWarnings("unchecked")
	public void addLeafNode(String term){
		Node<N> n = new Node<N>((N)term);
		n.parent = this.currentNode;
		this.currentNode.children.add(n);
	}
	
	public void endChildren(){
		this.currentNode = this.currentNode.parent;
	}
	
	public void initAndGenAST(){
		this.currentNode = this.currentNode.children.get(0);
		this.ast = new SyntaxTree((ProdType)this.currentNode.data);
		this.toAST();
		System.out.println("AST:\n");
		this.ast.printTree3("");
		System.out.println("\n");
	}
	
	public void toAST(){
		String strLit = "";
		if(this.currentNode.hasChildren()){
			if(branchSet.contains(this.currentNode.data) && !this.currentNode.parent.equals(this.root)){
				this.ast.addBranchNode((ProdType)this.currentNode.data);
				this.toASTchildren();
				this.ast.endChildren();
			}
			//if the production leads to a term we want, make an ast leaf node from the prod's child
			else if(termSet.contains(this.currentNode.data)){
				this.ast.addLeafNode((String)this.currentNode.children.get(0).data);  
			}
			else if(this.currentNode.data == STRING_EXPR){
				strLit = this.getStringLit(strLit);
				this.ast.addLeafNode(strLit);
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
	
	//helper function, called when STR_EXPR is found, traverses cst under the expr. Concatenates and returns
	//all chars and spaces found.
	public String getStringLit(String str){
		for(Node<N> n : this.currentNode.children){
			if(n.data == SPACEp){
				str = " ";
			}
			else if(n.data == CHARp){
				str = (String)n.children.get(0).data;
			}
			else if(n.data == CHAR_LIST && n.hasChildren()){
				this.currentNode = n;
				str += getStringLit(str);
				this.endChildren();
			}
		}
		return str;
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

