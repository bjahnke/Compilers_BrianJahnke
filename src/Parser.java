
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
			n.parent = this.currentNode;
			this.currentNode.children.add(n);
			this.currentNode = n;
		}
		public void addLeafNode(String term){
			Node n = new Node<N>((N)term);
			n.parent = this.currentNode;
			this.currentNode.children.add(n);
		}
		public void endChildren(){
			this.currentNode = this.currentNode.parent;
		}
	}
	
	public Parser(List<Lexer.Token> tList){
		this.cTokInd = 0;
		this.pTree = new Tree(prodType.PROGRAM);
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
	
	public boolean parseProg(){
		if(parseBlock() && match(Lexer.tokenType.EOP)){
			System.out.println("parseProg()");
			this.pTree.addBranchNode(prodType.BLOCK);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseBlock(){
		if(match(Lexer.tokenType.LCBRACE) && parseStmtList() && match(Lexer.tokenType.RCBRACE)){
			System.out.println("parseBlock()");
			this.pTree.addBranchNode(prodType.STATEMENT_LIST);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseStmtList(){
		System.out.println("parseStatementList()");
		if(parseStmt()){  //parseStmtList() would always be true cause of E
			this.pTree.addBranchNode(prodType.STATEMENT);
			//addBranchNode(prodType.STATEMENT_LIST)???
			parseStmtList();
			this.pTree.endChildren();
		}
		return true;
	}
	public boolean parseStmt(){
		if(parsePrint() || parseAssign() || parseVarDecl() || parseWhile() 
		|| parseIf() || parseBlock()){
			System.out.println("parseStatement()");
			this.pTree.addBranchNode(prodType.STATEMENT);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parsePrint(){
		if(match(Lexer.tokenType.KEYWORD) && match(Lexer.tokenType.LPAREN)
		&& parseExpr() && match(Lexer.tokenType.RPAREN)){
			System.out.println("parsePrint()");
			this.pTree.addBranchNode(prodType.EXPR);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseAssign(){
		if(parseId() && match(Lexer.tokenType.ASSIGN) && parseExpr()){
			System.out.println("parseAssign()");
			this.pTree.addBranchNode(prodType.EXPR);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseVarDecl(){
		if(parseType() && parseId()){
			System.out.println("parseVarDecl()");
			this.pTree.addBranchNode(prodType.VAR_DECL);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseWhile(){
		if(match(Lexer.tokenType.KEYWORD) && parseBoolExpr() && parseBlock()){
			System.out.println("parseWhile()");
			this.pTree.addBranchNode(prodType.WHILE_STATEMENT);
			this.pTree.endChildren();
			return true;
		}
		return false;	
	}
	public boolean parseIf(){
		if(match(Lexer.tokenType.KEYWORD) && parseBoolExpr() && parseBlock()){
			System.out.println("parseIf()");
			this.pTree.addBranchNode(prodType.IF_STATEMENT);
			this.pTree.endChildren();
			return true;
		}
		return false;	
	}
	public boolean parseExpr(){
		if(parseIntExpr() || parseStrExpr() || parseBoolExpr() || parseId()){
			System.out.println("parseExpr()");
			this.pTree.addBranchNode(prodType.EXPR);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseIntExpr(){
		if((parseDigit() && parseIntop() && parseExpr()) 
		|| parseDigit()){
			System.out.println("parseIntExpr()");
			this.pTree.addBranchNode(prodType.INT_EXPR);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseStrExpr(){
		if(match(Lexer.tokenType.DQUOTE) && parseCharList() 
		&& match(Lexer.tokenType.DQUOTE)){
			System.out.println("parse()");
			this.pTree.addBranchNode(prodType.STRING_EXPR);
			this.pTree.endChildren();
			return true;
		}
		return false;
		
		
	}
	public boolean parseBoolExpr(){
		if((match(Lexer.tokenType.LPAREN) && parseExpr() 
		&& parseBoolop() && parseExpr() && match(Lexer.tokenType.LPAREN))
		|| parseBoolval()){
			System.out.println("parseBoolExpr()");
			this.pTree.addBranchNode(prodType.BOOLEAN_EXPR);
			this.pTree.endChildren();
			return true;		
		}
		return false;
	}
	public boolean parseId(){
		if(match(Lexer.tokenType.ID)){   //distinction between char and id is made in lexer
			System.out.println("parseId()");
			this.pTree.addBranchNode(prodType.ID);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseCharList(){
		if((parseChar() && parseCharList()) 
		|| (parseSpace() && parseCharList()) ){
			System.out.println("parseCharList()");
			this.pTree.addBranchNode(prodType.CHAR_LIST);
			this.pTree.endChildren();
			return true;
		}
		return true; //because empty is valid?
		//or empty?
	}

	public boolean parseType(){
		if(match(Lexer.tokenType.TYPE)){
			System.out.println("parseType()");
			this.pTree.addBranchNode(prodType.TYPE);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseChar(){
		if(match(Lexer.tokenType.CHAR)){
			System.out.println("parseChar()");
			this.pTree.addBranchNode(prodType.CHAR);
			this.pTree.endChildren();
			return true;
		}
		return false;
		
	}
	public boolean parseSpace(){
		if(match(Lexer.tokenType.SPACE)){
			System.out.println("parseSpace()");
			this.pTree.addBranchNode(prodType.SPACE);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseDigit(){
		if(match(Lexer.tokenType.DIGIT)){
			System.out.println("parseDigit()");
			this.pTree.addBranchNode(prodType.DIGIT);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseBoolop(){
		if(match(Lexer.tokenType.BOOLOP)){
			System.out.println("parseBoolop()");
			this.pTree.addBranchNode(prodType.BOOLOP);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseBoolval(){
		if(match(Lexer.tokenType.BOOLVAL)){
			System.out.println("parseBoolval()");
			this.pTree.addBranchNode(prodType.BOOLVAL);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseIntop(){
		if(match(Lexer.tokenType.INTOP)){
			System.out.println("parseIntop()");
			this.pTree.addBranchNode(prodType.INTOP);
			this.pTree.endChildren();
			return true;
		}
		return false;
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
			this.nextToken();
		}
		return termVerified;
	}
}
