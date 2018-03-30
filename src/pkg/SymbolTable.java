package pkg;
import pkg.TokenType.*;
import pkg.ProdType.*;
import java.util.ArrayList;
import java.util.List;

public class SymbolTable<N> extends SyntaxTree<N>{
	public SyntaxTree<N> ast;
	
	public SymbolTable(){
		super();
		this.ast = new SyntaxTree<N>();
	}
	
	public SymbolTable(N rootData, SyntaxTree<N> asTree){
		super(rootData);
		this.ast = asTree;
	}
	
	
	
	public static class Scope<N> extends Node<N>{
		public List<Var> vars;
		
		public Scope(){
			super();
			this.vars = new ArrayList<Var>();
		}
	}
	
	
}
