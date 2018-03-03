
import java.util.ArrayList;
import java.util.List;


public class Parser {
	private Tree pTree;
	private List<Lexer.Token> tokList;
	private Lexer.Token currentToken;
	private int cTokInd;
	
	public static enum prodType {
		PROGRAM,
		BLOCK,
		STATEMENT_LIST,
		STATEMENT,
		PRINT_STATEMENT,
		ASSIGNMENT_STATEMENT,
		VAR_DECL,
		WHILE_STATEMENT,
		IF_STATEMENT,
		EXPR,
		INT_EXPR,
		STRING_EXPR,
		BOOLEAN_EXPR,
		ID,
		CHAR_LIST,
		TYPE,
		CHAR,
		SPACE,
		DIGIT,
		BOOLOP,
		BOOLVAL,
		INTOP;
	}
	
	public static class Tree<N>{
		private Node<N> root;
		private Node<N> currentNode;
		
		public Tree(N rootData){
			root = new Node<N>();
			root.data = rootData;
			root.children = new ArrayList<Node<N>>();
			currentNode = root;
		}
		public Tree(N rootData, int current){
			
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
		}
		public void setCurrentNode(Node<N> node){
			this.currentNode = node;
		}
		public void addBranchNode(prodType pEnum){
			Node n = new Node<N>((N)pEnum); 
		}
		public void addBranchNode(Lexer.Token token){
			Node n = new Node<N>((N)token);
		}
		public void addLeafNode(String term){
			
		}
		public void endChildren(){
			
		}
	}
	
	public Parser(List<Lexer.Token> tList){
		this.cTokInd = 0;
		this.pTree = new Tree(null);
		this.tokList = tList;
		this.currentToken = tokList.get(0);
	}
	public void nextToken(){
		this.cTokInd++;
		this.currentToken = this.tokList.get(cTokInd);
	}
	
	public static void parse(List<Lexer.Token> tList){
		System.out.println("parse()");
		Parser p = new Parser(tList);
		p.parseProg();
	}
	
	public void parseProg(){
		System.out.println("parseProg()");
		this.pTree.addBranchNode(prodType.PROGRAM); //this right?
		parseBlock();
		match(Lexer.tokenType.EOP);
	}
	public void parseBlock(){
		System.out.println("parseBlock()");
		this.pTree.addBranchNode(prodType.BLOCK);
		match(Lexer.tokenType.LCBRACE); //first token
		parseStmtList();
		match(Lexer.tokenType.RCBRACE);
	}
	public void parseStmtList(){
		System.out.println("parseStatementList()");
		this.pTree.addBranchNode(prodType.STATEMENT_LIST);
		parseStmt();
		parseStmtList();
		//or empty
	}
	public void parseStmt(){
		System.out.println("parseStatement()");
		this.pTree.addBranchNode(prodType.STATEMENT);
		parsePrint();
		//OR, assign
		//vardecl
		//while
		//if
		//block
	}
	public void parsePrint(){
		System.out.println("parsePrint()");
		this.pTree.addBranchNode(prodType.PRINT_STATEMENT);
		match(Lexer.tokenType.KEYWORD);
		match(Lexer.tokenType.LPAREN);
		parseExpr();
		match(Lexer.tokenType.RPAREN);
	}
	public void parseAssign(){
		System.out.println("parseAssign()");
		this.pTree.addBranchNode(prodType.ASSIGNMENT_STATEMENT);
		match(Lexer.tokenType.ID);
		match(Lexer.tokenType.ASSIGN);
		parseExpr();
	}
	public void parseVarDecl(){
		System.out.println("parseVarDecl()");
		this.pTree.addBranchNode(prodType.VAR_DECL);
		parseType();
		parseId();
	}
	public void parseWhile(){
		System.out.println("parseWhile()");
		this.pTree.addBranchNode(prodType.WHILE_STATEMENT);
		match(Lexer.tokenType.KEYWORD);
		parseBoolExpr();
		parseBlock();
	}
	public void parseIf(){
		System.out.println("parseIf()");
		this.pTree.addBranchNode(prodType.IF_STATEMENT);
		match(Lexer.tokenType.KEYWORD);
		parseBoolExpr();
		parseBlock();
	}
	public void parseExpr(){
		System.out.println("parseExpr()");
		this.pTree.addBranchNode(prodType.EXPR);
		parseIntExpr();
		//OR, string
		//boolean
		//Id
	}
	public void parseIntExpr(){
		System.out.println("parseIntExpr()");
		this.pTree.addBranchNode(prodType.INT_EXPR);
		match(Lexer.tokenType.DIGIT);
		parseIntop();
		parseExpr();
		//OR, digit
	}
	public void parseStrExpr(){
		System.out.println("parse()");
		this.pTree.addBranchNode(prodType.STRING_EXPR);
		match(Lexer.tokenType.DQUOTE);
		parseCharList();
		match(Lexer.tokenType.DQUOTE);
	}
	public void parseBoolExpr(){
		System.out.println("parseBoolExpr()");
		this.pTree.addBranchNode(prodType.BOOLEAN_EXPR);
		match(Lexer.tokenType.LPAREN);
		parseExpr();
		parseBoolop();
		parseExpr();
		match(Lexer.tokenType.LPAREN);
	}
	public void parseId(){
		System.out.println("parseId()");
		this.pTree.addBranchNode(prodType.ID);
		parseChar();
	}
	public void parseCharList(){
		System.out.println("parseCharList()");
		this.pTree.addBranchNode(prodType.CHAR_LIST);
		parseChar();
		parseCharList();
		//or parse space charlist, or empty
	}

	public void parseType(){
		System.out.println("parseType()");
		this.pTree.addBranchNode(prodType.TYPE);
		match(Lexer.tokenType.TYPE);
	}
	public void parseChar(){
		System.out.println("parseChar()");
		this.pTree.addBranchNode(prodType.CHAR);
		match(Lexer.tokenType.CHAR);
	}
	public void parseSpace(){
		System.out.println("parseSpace()");
		this.pTree.addBranchNode(prodType.SPACE);
		match(Lexer.tokenType.SPACE);
	}
	public void parseDigit(){
		System.out.println("parseDigit()");
		this.pTree.addBranchNode(prodType.DIGIT);
		match(Lexer.tokenType.DIGIT);
	}
	public void parseBoolop(){
		System.out.println("parseBoolop()");
		this.pTree.addBranchNode(prodType.BOOLOP);
		match(Lexer.tokenType.BOOLOP);
	}
	public void parseBoolval(){
		System.out.println("parseBoolval()");
		this.pTree.addBranchNode(prodType.BOOLVAL);
		match(Lexer.tokenType.BOOLVAL);
	}
	public void parseIntop(){
		System.out.println("parseIntop()");
		this.pTree.addBranchNode(prodType.INTOP);
		match(Lexer.tokenType.INTOP);
	}
	public void match(List<Lexer.tokenType> tokens){
	}
	public boolean match(Lexer.tokenType tEnum){
		Lexer.Token cT = this.currentToken;
		boolean termVerified = false;
		
		if(this.currentToken.getType() == tEnum){	
			switch(this.currentToken.getType()){
				case KEYWORD:      
					if(this.pTree.currentNode.data == prodType.PRINT_STATEMENT){
						if(cT.getLiteralT().equals("print")){
							termVerified = true;
						}
						else{
							//Production error-- we expect print because the current node is parsingPrint but is not present
						}
					}
					else if(this.pTree.currentNode.data == prodType.WHILE_STATEMENT){
						if(cT.getLiteralT().equals("while")){
							termVerified = true;
						}
						else{
							//Production error
						}
					}
					else if(this.pTree.currentNode.data == prodType.IF_STATEMENT){
						if(cT.getLiteralT().equals("if")){
							termVerified = true;
						}
						else{
							//Production error
						}
					}
					else{
						//error?
					}
					break;
				default:
					termVerified = true;                         //since we already know that the current token equals the one requested,
					break;                                       //if not a keyword, then we just send the literal token of the requested tokentype
			}
		}
		else{
			//prod error, type requested is not the type of the current token.
		}
		if(termVerified){
			this.pTree.addLeafNode(cT.getLiteralT());
		}
		return termVerified;
	}
}
