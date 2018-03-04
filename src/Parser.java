
import java.util.ArrayList;
import java.util.List;


public class Parser {
	private Tree pTree;
	private List<Lexer.Token> tokList;
	private List<Lexer.tokenType> matchList = new ArrayList<Lexer.tokenType>();
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
	public void addLeafNextTok(String term){
		this.pTree.addLeafNode(term);
		nextToken();
	}
	public static void parse(List<Lexer.Token> tList){
		System.out.println("parse()");
		Parser p = new Parser(tList);
		
		if(p.parseProg()){
			System.out.println("Parse completed successfully");
		}
		else{
			System.out.println("Parse failed");
		}
	}
	
	public boolean parseProg(){
		if(match(progList()) != null){
			System.out.println("parseProg()");
			this.pTree.addBranchNode(prodType.BLOCK);
			if(!parseBlock()){                                   //prod 1 
				return false;
			}
			if(match(termList(Lexer.tokenType.EOP)) != null){    //prod 1
				this.addLeafNextTok(this.currentToken.getLiteralT());
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(Lexer.tokenType.EOP);
				return false;
			}
		}
		else{
			printError(Lexer.tokenType.LCBRACE);
			return false;
		}
	}
	public boolean parseBlock(){
		System.out.println("parseBlock()");
		if(match(blockList()) != null){                                //prod 2
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.addBranchNode(prodType.STATEMENT_LIST);
			if(!parseStmtList()){                                     //prod 2
				return false;
			}
			if(match(termList(Lexer.tokenType.RCBRACE)) != null){      //prod 2
				this.addLeafNextTok(this.currentToken.getLiteralT());
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(Lexer.tokenType.RCBRACE);
				return false;
			}
		}
		else{
			printError(Lexer.tokenType.LCBRACE);
			return false;
		}
	}
	public boolean parseStmtList(){
		System.out.println("parseStatementList()");
		if(match(stmtList()) != null){                    //prod 3
			this.pTree.addBranchNode(prodType.STATEMENT);
			if(!parseStmt()){					//first of stmt is first of stmtList without null so we need match before parsing
				return false;
			}
			this.pTree.addBranchNode(prodType.STATEMENT_LIST);
			if(!parseStmtList()){                         //prod 3
				return false;
			}
			this.pTree.endChildren();
		}
		return true;                           //prod 4
	}
	public boolean parseStmt(){
		if(match(keywordList()) != null){      //print, if, or, while
			System.out.println("parseStatement()");
			if(this.currentToken.getLiteralT().equals("print")){       //prod 5
				this.pTree.addBranchNode(prodType.PRINT_STATEMENT);     
				if(!parsePrint()){
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
			}
			else if(this.currentToken.getLiteralT().equals("if")){     //prod 9
				this.pTree.addBranchNode(prodType.IF_STATEMENT);
				if(!parseIf()){
					this.pTree.endChildren();
					return false;
				}
			}
			else if(this.currentToken.getLiteralT().equals("while")){  //prod 8
				this.pTree.addBranchNode(prodType.WHILE_STATEMENT);
				if(!parseWhile()){
					this.pTree.endChildren();
					return false;
				}
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(assignList()) != null){                          //prod 6
			this.pTree.addBranchNode(prodType.ASSIGNMENT_STATEMENT);
			if(!parseAssign()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(varDList()) != null){                             //prod 7
			this.pTree.addBranchNode(prodType.VAR_DECL);
			if(!parseVarDecl()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(blockList()) != null){                         //prod 10
			this.pTree.addBranchNode(prodType.BLOCK);
			if(!parseBlock()){
				this.pTree.endChildren();
				return false;
			}
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
		if(match(termList(Lexer.tokenType.DQUOTE)) != null){
			this.addLeafNextTok(this.currentToken.getLiteralT());
			parseCharList();
		}
		
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
		if(match(boolEList()) == Lexer.tokenType.LPAREN){
			this.addLeafNextTok(this.currentToken.getLiteralT());
			if(match(eList()) != null){
				this.pTree.addBranchNode(prodType.EXPR);
				parseExpr();
				if(match(bOpList()) != null){
					this.pTree.addBranchNode(prodType.BOOLOP);
					parseBoolop();
					if(match())
				}
			}
			
			this.pTree.addBranchNode(prodType.EXPR);
			parseExpr();
			if(match(termList(Lexer.tokenType.RPAREN)) != null){
				this.addLeafNextTok(this.currentToken.getLiteralT());
				this.pTree.endChildren();
				return true;
			}
			this.pTree.endChildren();
			return false;
		}
		else if(match(bValList()) != null){
			this.pTree.addBranchNode(prodType.BOOLVAL);
			parseBoolval();
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseId(){
		if(match(idList()) != null){   //distinction between char and id is made in lexer
			System.out.println("parseId()");
			this.pTree.addBranchNode(prodType.ID);
			this.pTree.endChildren();
			return true;
		}
		return false;
	}
	public boolean parseCharList(){
		System.out.println("parseCharList()");
		if(match(cLList()) != null){
			if(match(cLList()) == Lexer.tokenType.CHAR){
				this.pTree.addBranchNode(prodType.CHAR);
				parseChar();
			}
			else if(match(cLList()) == Lexer.tokenType.SPACE){
				this.pTree.addBranchNode(prodType.SPACE);
				parseSpace();			
			}
			this.pTree.addBranchNode(prodType.CHAR_LIST);
			parseCharList();
			this.pTree.endChildren();
		}
		return true; //because empty is valid?
	}

	public boolean parseType(){
		if(match(typeList()) != null){
			System.out.println("parseType()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
	}
	public boolean parseChar(){
		if(match(charList()) != null){
			System.out.println("parseChar()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
		
	}
	public boolean parseSpace(){
		if(match(spaceList()) != null){
			System.out.println("parseSpace()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
	}
	public boolean parseDigit(){
		if(match(digitList()) != null){
			System.out.println("parseDigit()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
	}
	public boolean parseBoolop(){
		if(match(bOpList()) != null){
			System.out.println("parseBoolop()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
	}
	public boolean parseBoolval(){
		if(match(bValList()) != null){
			System.out.println("parseBoolval()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
	}
	public boolean parseIntop(){
		if(match(intopList()) != null){
			System.out.println("parseIntop()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			return true;
		}
		return false;
	}
	public Lexer.tokenType match(List<Lexer.tokenType> tokenTypesL){
		Lexer.Token cT = this.currentToken;
		Lexer.tokenType returnType = null;
		boolean termMatched = false;
		int i = 0;
		while(!termMatched || i < tokenTypesL.size()){
			if(this.currentToken.getType() == tokenTypesL.get(i)){	
				switch(this.currentToken.getType()){
					case KEYWORD:      
						if(this.pTree.currentNode.data == prodType.PRINT_STATEMENT){
							if(cT.getLiteralT().equals("print")){
								termMatched = true;
							}
							else{
								//Production error-- we expect print because the current node is parsingPrint but is not present
							}
						}
						else if(this.pTree.currentNode.data == prodType.WHILE_STATEMENT){
							if(cT.getLiteralT().equals("while")){
								termMatched = true;
							}
							else{
								//Production error
							}
						}
						else if(this.pTree.currentNode.data == prodType.IF_STATEMENT){
							if(cT.getLiteralT().equals("if")){
								termMatched = true;
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
						termMatched = true;                         //since we already know that the current token equals the one requested,
						break;                                       //if not a keyword, then we just send the literal token of the requested tokentype
				}
			}
		}
		//else{
			//prod error, type requested is not the type of the current token.
		//}
		if(termMatched){
			returnType = this.currentToken.getType();
		}
		return returnType;
	}
	
	/*------------------------|
	 *                        |
	 * First Sets             |
	 *                        |
	 ------------------------*/
	public static List<Lexer.tokenType> intopList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.INTOP);
		return enumL;
	}
	public static List<Lexer.tokenType> bValList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.BOOLVAL);
		return enumL;
	}
	public static List<Lexer.tokenType> bOpList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.BOOLOP);
		return enumL;
	}
	public static List<Lexer.tokenType> digitList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.DIGIT);
		return enumL;
	}
	public static List<Lexer.tokenType> spaceList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.SPACE);
		return enumL;
	}
	public static List<Lexer.tokenType> charList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.CHAR);
		return enumL;
	}
	public static List<Lexer.tokenType> typeList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.TYPE);
		return enumL;
	}
	public static List<Lexer.tokenType> cLList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.CHAR);
		enumL.add(Lexer.tokenType.SPACE);
		return enumL;
	}
	public static List<Lexer.tokenType> idList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.ID);
		return enumL;
	}
	public static List<Lexer.tokenType> boolEList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.LPAREN);
		enumL.addAll(bValList());
		return enumL;
	}
	public static List<Lexer.tokenType> strEList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.DQUOTE);
		return enumL;
	}
	public static List<Lexer.tokenType> intEList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.DIGIT);
		return enumL;
	}
	public static List<Lexer.tokenType> eList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.DIGIT);
		enumL.add(Lexer.tokenType.DQUOTE);
		enumL.add(Lexer.tokenType.LPAREN);
		enumL.add(Lexer.tokenType.ID);
		return enumL;
	}
	public static List<Lexer.tokenType> keywordList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.KEYWORD);
		return enumL;
	}
	public static List<Lexer.tokenType> varDList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.addAll(typeList());
		return enumL;
	}
	public static List<Lexer.tokenType> assignList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.ID);
		return enumL;
	}
	public static List<Lexer.tokenType> stmtList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.addAll(varDList());
		enumL.addAll(keywordList()); //if while print
		enumL.addAll(typeList());
		enumL.addAll(assignList());
		enumL.addAll(blockList());
		return enumL;
	}
	public static List<Lexer.tokenType> stmtLList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.addAll(stmtList());
		return enumL;
	}
	public static List<Lexer.tokenType> blockList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(Lexer.tokenType.LCBRACE);
		return enumL;
	}
	public static List<Lexer.tokenType> progList(){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.addAll(blockList());
		return enumL;
	}
	//for other random terminals i have to check for
	public static List<Lexer.tokenType> termList(Lexer.tokenType termEnum){
		List<Lexer.tokenType> enumL = new ArrayList<Lexer.tokenType>();
		enumL.add(termEnum);
		return enumL;
	}
	
	public void printError(Lexer.tokenType expected){
		System.out.println("Error: Expected [" + expected + "] got [" + this.currentToken.getType()
        + "on line " + this.currentToken.lineNum);
	}
}
