package pkg;
import static pkg.ProdType.*;
import java.util.ArrayList;
import java.util.List;

public class SyntaxTree<N>{
	//Source: https://stackoverflow.com/questions/3522454/java-tree-data-structure
	//Helped with making a generic class tree
	private Node<N> root;
	private Node<N> currentNode;
	
	public SyntaxTree(N rootData){
		root = new Node<N>();
		root.data = rootData;
		root.children = new ArrayList<Node<N>>();
		currentNode = root;
	}
	
	public static class Node<N>{
		private N data;
		private Node<N> parent;
		private List<Node<N>> children;
		
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
			//else if(this.data == prodType.STATEMENT_LIST || this.data == prodType.CHAR_LIST){  //to show nulls not really needed
			//	return "(e)";
			//}
			return "";
		}
		public boolean hasChildren(){
			if(this.children != null && !this.children.isEmpty()){
				return true;
			}
			return false;
		}
	}
	public void setCurrentNode(Node<N> node){
		this.currentNode = node;
	}
	@SuppressWarnings("unchecked")
	public void addBranchNode(ProdType pEnum){
		Node n = new Node<N>((N)pEnum);
		n.parent = this.currentNode;
		this.currentNode.children.add(n);
		this.currentNode = n;
	}
	@SuppressWarnings("unchecked")
	public void addLeafNode(String term){
		Node n = new Node<N>((N)term);
		n.parent = this.currentNode;
		this.currentNode.children.add(n);
	}
	public void endChildren(){
		this.currentNode = this.currentNode.parent;
	}
	
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

