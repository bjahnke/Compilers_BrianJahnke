
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
	/*------------------------|
	 *                        |
	 * Tree, Node Classes     |
	 *                        |
	 ------------------------*/
//Source: https://stackoverflow.com/questions/3522454/java-tree-data-structure
//Helped with making a generic class tree
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
			public String toString(){
				if((this.children != null && !this.children.isEmpty()) 
				|| this.data == prodType.STATEMENT_LIST
				|| this.data == prodType.CHAR_LIST){
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
		public void addBranchNode(prodType pEnum){
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
	/*------------------------|
	 *                        |
	 * Parse Constructor      |
	 * and Methods            |
	 ------------------------*/
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
		if(this.cTokInd < this.tokList.size()-1){
			nextToken();
		}
	}
	
	public static void parse(List<Lexer.Token> tList){
		System.out.println("parse()");
		Parser p = new Parser(tList);
		
		if(p.parseProg()){
			System.out.println("Parse completed successfully\n\nCST:\n");
			//p.pTree.printTree();
			p.pTree.printTree3("");
			System.out.println("\n");
		}
		else{
			System.out.println("Parse failed\nCST skipped due to parse failure\n");
			
		}
	}
	
	public boolean parseProg(){
		if(match(progList()) != null){
			System.out.println("parseProg()");
			this.pTree.addBranchNode(prodType.BLOCK);
			if(!parseBlock()){                                   //prod 1 
				this.pTree.endChildren();
				return false;
			}
			if(match(termList(Lexer.tokenType.EOP)) != null){    //prod 1
				this.addLeafNextTok(this.currentToken.getLiteralT());
				//this.pTree.endChildren();
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
			if(match(stmtLList()) != null){
				if(!parseStmtList()){                                     //prod 2/4
					this.pTree.endChildren();								
					return false;
				}
			}
			this.pTree.endChildren();
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
			if(!parseStmt()){			
				this.pTree.endChildren();
				return false;
			}
			this.pTree.addBranchNode(prodType.STATEMENT_LIST);
			if(match(stmtLList()) != null){
				if(!parseStmtList()){                         //prod 3
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
				return true;
			}
			else{
				System.out.println("parseStatementList()");
				this.pTree.endChildren();                   //prod 4 
				return true; //because nullable;
			}
		}
		else{
			printError(stmtList());
			return false;             //not sure if this is reachable
		}
	}
	
	public boolean parseStmt(){
		System.out.println("parseStatement()");
		if(match(keywordList()) != null){      //print, if, or, while
			if(this.currentToken.getLiteralT().equals("print")){       //prod 5
				this.pTree.addBranchNode(prodType.PRINT_STATEMENT);     
				if(!parsePrint()){
					this.pTree.endChildren();
					return false;
				}
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
		return false; //not sure if this is right or what error it produces
	}
	
	public boolean parsePrint(){                                 //prod 11
		System.out.println("parsePrint()");
		this.addLeafNextTok(this.currentToken.getLiteralT());
		if(match(termList(Lexer.tokenType.LPAREN)) != null){
			this.addLeafNextTok(this.currentToken.getLiteralT());
			if(match(eList()) != null){
				this.pTree.addBranchNode(prodType.EXPR);
				if(!parseExpr()){
					this.pTree.endChildren();
					return false;
				}
				if(match(termList(Lexer.tokenType.RPAREN)) != null){
					this.addLeafNextTok(this.currentToken.getLiteralT());
					this.pTree.endChildren();
					return true;
				}
				else{
					printError(Lexer.tokenType.RPAREN);
					return false;
				}
			}
			else{
				printError(eList());
				return false;
			}
		}
		else{
			printError(Lexer.tokenType.LPAREN);
			return false;
		}
	}
	
	public boolean parseAssign(){                      //prod 12
		if(match(idList()) != null){
			System.out.println("parseAssign()");
			this.pTree.addBranchNode(prodType.ID);
			if(!parseId()){
				this.pTree.endChildren();
				return false;
			}
			if(match(termList(Lexer.tokenType.ASSIGN)) != null){
				this.addLeafNextTok(this.currentToken.getLiteralT());
				if(match(eList()) != null){
					this.pTree.addBranchNode(prodType.EXPR);
					if(!parseExpr()){
						this.pTree.endChildren();
						return false;
					}
						this.pTree.endChildren();
						return true;
				}
				else{
					printError(eList());
					return false;
				}
			}
			else{
				printError(Lexer.tokenType.ASSIGN);
				return false;
			}
		}
		else{
			printError(idList());
			return false;
		}
	}
	
	public boolean parseVarDecl(){                    //prod 13
		if(match(typeList()) != null){
			System.out.println("parseVarDecl()");
			this.pTree.addBranchNode(prodType.TYPE);
			if(!parseType()){
				this.pTree.endChildren();
				return false;
			}
			if(match(idList()) != null){
				this.pTree.addBranchNode(prodType.ID);
				if(!parseId()){
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(idList());
				return false;
			}
		}
		else{
			printError(typeList());
			return false;
		}
	}
	
	public boolean parseWhile(){                      //prod 14
		System.out.println("parseWhile()");
		this.addLeafNextTok(this.currentToken.getLiteralT());
		if(match(boolEList()) != null){
			this.pTree.addBranchNode(prodType.BOOLEAN_EXPR);
			if(!parseBoolExpr()){                        
				this.pTree.endChildren();
				return false;
			}
			if(match(blockList()) != null){
				this.pTree.addBranchNode(prodType.BLOCK);
				if(!parseBlock()){
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(blockList());
				return false;
			}
			
		}
		else{
			printError(boolEList());
			return false;
		}
	}
	
	public boolean parseIf(){                             //prod 15
		System.out.println("parseIf()");
		this.addLeafNextTok(this.currentToken.getLiteralT());
		if(match(boolEList()) != null){
			this.pTree.addBranchNode(prodType.BOOLEAN_EXPR);
			if(!parseBoolExpr()){
				this.pTree.endChildren();
				return false;
			}
			if(match(blockList()) != null){
				this.pTree.addBranchNode(prodType.BLOCK);
				if(!parseBlock()){
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(blockList());
				return false;
			}
		}
		else{
			printError(boolEList());
			return false;
		}
	}
	
	public boolean parseExpr(){                       
		System.out.println("parseExpr()");
		if(match(intEList()) != null){                       //prod 16
			this.pTree.addBranchNode(prodType.INT_EXPR);
			if(!parseIntExpr()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(strEList()) != null){                 //prod 17
			this.pTree.addBranchNode(prodType.STRING_EXPR);
			if(!parseStrExpr()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(boolEList()) != null){                //prod 18
			this.pTree.addBranchNode(prodType.BOOLEAN_EXPR);
			if(!parseBoolExpr()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(idList()) != null){                   //prod 19
			this.pTree.addBranchNode(prodType.ID);
			if(!parseId()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(eList());
			return false;
		}
	}
	
	public boolean parseIntExpr(){                     //prod 20/21
		System.out.println("parseIntExpr()");
		if(match(digitList()) != null){
			this.pTree.addBranchNode(prodType.DIGIT);
			if(!parseDigit()){
				this.pTree.endChildren();
				return false;
			}
			if(match(intopList()) != null){
				this.pTree.addBranchNode(prodType.INTOP);
				if(!parseIntop()){
					this.pTree.endChildren();
					return false;
				}
				if(match(eList()) != null){
					this.pTree.addBranchNode(prodType.EXPR);
					if(!parseExpr()){
						this.pTree.endChildren();
						return false;
					}
					this.pTree.endChildren();
					return true;
				}
				else{
					printError(eList());
					return false;
				}
			}
			else{   
				this.pTree.endChildren();
				return true;
			}
		}
		else{
			printError(digitList());
			return false;
		}
	}
	
	public boolean parseStrExpr(){                         //prod 22
		System.out.println("parseStrExpr()");
		if(match(strEList()) != null){
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.addBranchNode(prodType.CHAR_LIST);
			if(match(cLList()) != null){
				if(!parseCharList()){
					this.pTree.endChildren();
					return false;
				}
			}
			this.pTree.endChildren();
			if(match(strEList()) != null){       
				this.addLeafNextTok(this.currentToken.getLiteralT());
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(strEList());
				return false;
			}
		}
		else{
			printError(strEList());
			return false;
		}
	}
	
	public boolean parseBoolExpr(){
		System.out.println("parseBoolExpr()");
		if(match(termList(Lexer.tokenType.LPAREN)) != null){    //prod 23
			this.addLeafNextTok(this.currentToken.getLiteralT());
			if(match(eList()) != null){
				this.pTree.addBranchNode(prodType.EXPR);
				if(!parseExpr()){
					this.pTree.endChildren();
					return false;
				}
				if(match(bOpList()) != null){
					this.pTree.addBranchNode(prodType.BOOLOP);
					if(!parseBoolop()){
						this.pTree.endChildren();
						return false;
					}
					if(match(eList()) != null){
						this.pTree.addBranchNode(prodType.EXPR);
						if(!parseExpr()){
							this.pTree.endChildren();
							return false;
						}
						if(match(termList(Lexer.tokenType.RPAREN)) != null){
							this.addLeafNextTok(this.currentToken.getLiteralT());
							this.pTree.endChildren();
							return true;
						}
						else{
							printError(Lexer.tokenType.RPAREN);
							return false;
						}
					}
					else{
						printError(eList());
						return false;
					}
				}
				else{
					printError(bOpList());
					return false;
				}
			}
			else{
				printError(eList());
				return false;
			}
		}
		else if(match(bValList()) != null){            //prod 24
			this.pTree.addBranchNode(prodType.BOOLVAL);
			if(!parseBoolval()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(boolEList());
			return false;
		}
	}
	
	public boolean parseId(){
		if(match(idList()) != null){              //distinction between char and id is made in lexer
			System.out.println("parseId()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(idList());    //not sure if this will reach
			return false;
		}
	}
	
	public boolean parseCharList(){
		System.out.println("parseCharList()");
		if(match(cLList()) != null){
			if(match(cLList()) == Lexer.tokenType.CHAR){
				this.pTree.addBranchNode(prodType.CHAR);
				if(!parseChar()){
					this.pTree.endChildren();
					return false;
				}
			}
			else if(match(cLList()) == Lexer.tokenType.SPACE){
				this.pTree.addBranchNode(prodType.SPACE);
				if(!parseSpace()){
					this.pTree.endChildren();
					return false;
				}
			}
			this.pTree.addBranchNode(prodType.CHAR_LIST);
			if(match(cLList()) != null){
				if(!parseCharList()){
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
				return true;
			}
			else{
				this.pTree.endChildren();
				return true;               //nullable
			}
		}
		else{
			this.pTree.endChildren();
			return true; //because nullable    not sure if it will reach here
		}
	}

	public boolean parseType(){
		if(match(typeList()) != null){
			System.out.println("parseType()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(typeList());
			return false;
		}
	}
	
	public boolean parseChar(){
		if(match(charList()) != null){
			System.out.println("parseChar()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(charList());
			return false;
		}
	}
	public boolean parseSpace(){
		if(match(spaceList()) != null){
			System.out.println("parseSpace()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(spaceList());
			return false;
		}
	}
	
	public boolean parseDigit(){
		if(match(digitList()) != null){
			System.out.println("parseDigit()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(digitList());
			return false;
		}
	}
	
	public boolean parseBoolop(){
		if(match(bOpList()) != null){
			System.out.println("parseBoolop()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(bOpList());
			return false;
		}
	}
	
	public boolean parseBoolval(){
		if(match(bValList()) != null){
			System.out.println("parseBoolval()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(bValList());
			return false;
		}
	}
	
	public boolean parseIntop(){
		if(match(intopList()) != null){
			System.out.println("parseIntop()");
			this.addLeafNextTok(this.currentToken.getLiteralT());
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(intopList());
			return false;
		}
	}
	
	public Lexer.tokenType match(List<Lexer.tokenType> tokenTypesL){
		Lexer.tokenType returnType = null;
		boolean termMatched = false;
		int i = 0;
		while(!termMatched && i < tokenTypesL.size()){
			if(this.currentToken.getType() == tokenTypesL.get(i)){	
				termMatched = true;
			}
			i++;
		}
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
		enumL.add(Lexer.tokenType.BOOLVAL);
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
	/*------------------------|
	 *                        |
	 * Error Print Methods    |
	 *                        |
	 ------------------------*/
	public void printError(Lexer.tokenType expected){
		System.out.println("Error: Expected [" + expected + "] got [" + this.currentToken.getType()
        + "] on line " + this.currentToken.lineNum);
	}
	public void printError(List<Lexer.tokenType> expectedList){
		String error = "Error: Expected [";
		for(int i = 0; i < expectedList.size(); i++){
			error += expectedList.get(i);
			if(i != expectedList.size()-1){
				error += "], or [";
			}
		}
		error += "] got [" + this.currentToken.getType() + "] on line " + this.currentToken.lineNum;
		System.out.println(error);
	}
}

