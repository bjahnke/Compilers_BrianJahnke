package pkg;
import static pkg.ProdType.*;
import pkg.TokenType;
import static pkg.TokenType.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
	private SyntaxTree pTree;
	private List<Token> tokList;
	private List<TokenType> matchList = new ArrayList<TokenType>();
	private Token currentToken;
	private int cTokInd;

	/*------------------------|
	 *                        |
	 * Parse Constructor      |
	 * and Methods            |
	 ------------------------*/
	public Parser(List<Token> tList){
		this.cTokInd = 0;
		this.pTree = new SyntaxTree(PROGRAM);
		this.tokList = tList;
		this.currentToken = tokList.get(0);
	}
	
	public void nextToken(){
			this.cTokInd++;
			this.currentToken = this.tokList.get(cTokInd);
	}
	
	public void addLeafNextTok(Token term){
		this.pTree.addLeafNode(term);
		if(this.cTokInd < this.tokList.size()-1){
			nextToken();
		}
	}
	
	public static SyntaxTree parse(List<Token> tList){
		TempMain.verbosePrint("parse()");
		Parser p = new Parser(tList);
		
		if(p.parseProg()){
			System.out.println("Parse completed successfully\n");
			//if(TempMain.isVerboseOnLP){
				System.out.println("\nCST:\n");
				p.pTree.printTree3("");
				System.out.println("\n");
			//}
			p.pTree.initAndGenAST();
			List<Var> list = new ArrayList<Var>();
			SymbolTable sT = new SymbolTable(list, p.pTree.ast);
			if(sT.buildSymbolTable(sT.ast.root)){
				SymbolTable.printSymbolTable();
				sT.symbolTableWarnings();
				return sT;
			}
			else{
				System.out.println("Program " + Lexer.progNum + " Symbol Table:"
						+ "\nNot produced due to an error in semantic analysis.\n");
				return null;
			}
			
		}
		else{
				System.out.println("Parse failed\n");
				if(TempMain.isVerboseOnLP){
					System.out.println("CST skipped due to parse failure\n");
				}
				return null;
		}
	}
	
	public boolean parseProg(){
		if(match(progList()) != null){
			TempMain.verbosePrint("parseProg()");
			this.pTree.addBranchNode(BLOCK);
			if(!parseBlock()){                                   //prod 1 
				this.pTree.endChildren();
				return false;
			}
			if(match(termList(EOP)) != null){    //prod 1
				this.addLeafNextTok(this.currentToken);
				return true;
			}
			else{
				printError(EOP);
				return false;
			}
		}
		else{
			printError(LCBRACE);
			return false;
		}
	}
	
	public boolean parseBlock(){
		TempMain.verbosePrint("parseBlock()");
		if(match(blockList()) != null){                                //prod 2
			this.addLeafNextTok(this.currentToken);
			this.pTree.addBranchNode(STATEMENT_LIST);
			if(match(stmtLList()) != null){
				if(!parseStmtList()){                                     //prod 2/4
					this.pTree.endChildren();								
					return false;
				}
			}
			this.pTree.endChildren();
			if(match(termList(RCBRACE)) != null){      //prod 2
				this.addLeafNextTok(this.currentToken);
				this.pTree.endChildren();
				return true;
			}
			else{
				printError(RCBRACE);
				return false;
			}
		}
		else{
			printError(LCBRACE);
			return false;
		}
	}
	
	public boolean parseStmtList(){
		TempMain.verbosePrint("parseStatementList()");
		if(match(stmtList()) != null){                    //prod 3
			this.pTree.addBranchNode(STATEMENT);
			if(!parseStmt()){			
				this.pTree.endChildren();
				return false;
			}
			this.pTree.addBranchNode(STATEMENT_LIST);
			if(match(stmtLList()) != null){
				if(!parseStmtList()){                         //prod 3
					this.pTree.endChildren();
					return false;
				}
				this.pTree.endChildren();
				return true;
			}
			else{
				TempMain.verbosePrint("parseStatementList()");
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
		TempMain.verbosePrint("parseStatement()");
		if(match(keywordList()) != null){      //print, if, or, while
			if(this.currentToken.getLit().equals("print")){       //prod 5
				this.pTree.addBranchNode(PRINT_STATEMENT);     
				if(!parsePrint()){
					this.pTree.endChildren();
					return false;
				}
			}
			else if(this.currentToken.getLit().equals("if")){     //prod 9
				this.pTree.addBranchNode(IF_STATEMENT);
				if(!parseIf()){
					this.pTree.endChildren();
					return false;
				}
			}
			else if(this.currentToken.getLit().equals("while")){  //prod 8
				this.pTree.addBranchNode(WHILE_STATEMENT);
				if(!parseWhile()){
					this.pTree.endChildren();
					return false;
				}
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(assignList()) != null){                          //prod 6
			this.pTree.addBranchNode(ASSIGNMENT_STATEMENT);
			if(!parseAssign()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(varDList()) != null){                             //prod 7
			this.pTree.addBranchNode(VAR_DECL);
			if(!parseVarDecl()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(blockList()) != null){                         //prod 10
			this.pTree.addBranchNode(BLOCK);
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
		TempMain.verbosePrint("parsePrint()");
		this.addLeafNextTok(this.currentToken);
		if(match(termList(LPAREN)) != null){
			this.addLeafNextTok(this.currentToken);
			if(match(eList()) != null){
				this.pTree.addBranchNode(EXPR);
				if(!parseExpr()){
					this.pTree.endChildren();
					return false;
				}
				if(match(termList(RPAREN)) != null){
					this.addLeafNextTok(this.currentToken);
					this.pTree.endChildren();
					return true;
				}
				else{
					printError(RPAREN);
					return false;
				}
			}
			else{
				printError(eList());
				return false;
			}
		}
		else{
			printError(LPAREN);
			return false;
		}
	}
	
	public boolean parseAssign(){                      //prod 12
		if(match(idList()) != null){
			TempMain.verbosePrint("parseAssign()");
			this.pTree.addBranchNode(IDp);
			if(!parseId()){
				this.pTree.endChildren();
				return false;
			}
			if(match(termList(ASSIGN)) != null){
				this.addLeafNextTok(this.currentToken);
				if(match(eList()) != null){
					this.pTree.addBranchNode(EXPR);
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
				printError(ASSIGN);
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
			TempMain.verbosePrint("parseVarDecl()");
			this.pTree.addBranchNode(TYPEp);
			if(!parseType()){
				this.pTree.endChildren();
				return false;
			}
			if(match(idList()) != null){
				this.pTree.addBranchNode(IDp);
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
		TempMain.verbosePrint("parseWhile()");
		this.addLeafNextTok(this.currentToken);
		if(match(boolEList()) != null){
			this.pTree.addBranchNode(BOOLEAN_EXPR);
			if(!parseBoolExpr()){                        
				this.pTree.endChildren();
				return false;
			}
			if(match(blockList()) != null){
				this.pTree.addBranchNode(BLOCK);
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
		TempMain.verbosePrint("parseIf()");
		this.addLeafNextTok(this.currentToken);
		if(match(boolEList()) != null){
			this.pTree.addBranchNode(BOOLEAN_EXPR);
			if(!parseBoolExpr()){
				this.pTree.endChildren();
				return false;
			}
			if(match(blockList()) != null){
				this.pTree.addBranchNode(BLOCK);
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
		TempMain.verbosePrint("parseExpr()");
		if(match(intEList()) != null){                       //prod 16
			this.pTree.addBranchNode(INT_EXPR);
			if(!parseIntExpr()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(strEList()) != null){                 //prod 17
			this.pTree.addBranchNode(STRING_EXPR);
			if(!parseStrExpr()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(boolEList()) != null){                //prod 18
			this.pTree.addBranchNode(BOOLEAN_EXPR);
			if(!parseBoolExpr()){
				this.pTree.endChildren();
				return false;
			}
			this.pTree.endChildren();
			return true;
		}
		else if(match(idList()) != null){                   //prod 19
			this.pTree.addBranchNode(IDp);
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
		TempMain.verbosePrint("parseIntExpr()");
		if(match(digitList()) != null){
			this.pTree.addBranchNode(DIGITp);
			if(!parseDigit()){
				this.pTree.endChildren();
				return false;
			}
			if(match(intopList()) != null){
				this.pTree.addBranchNode(INTOPp);
				if(!parseIntop()){
					this.pTree.endChildren();
					return false;
				}
				if(match(eList()) != null){
					this.pTree.addBranchNode(EXPR);
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
		TempMain.verbosePrint("parseStrExpr()");
		if(match(strEList()) != null){
			this.addLeafNextTok(this.currentToken);
			this.pTree.addBranchNode(CHAR_LIST);
			if(match(cLList()) != null){
				if(!parseCharList()){
					this.pTree.endChildren();
					return false;
				}
			}
			this.pTree.endChildren();
			if(match(strEList()) != null){       
				this.addLeafNextTok(this.currentToken);
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
		TempMain.verbosePrint("parseBoolExpr()");
		if(match(termList(LPAREN)) != null){    //prod 23
			this.addLeafNextTok(this.currentToken);
			if(match(eList()) != null){
				this.pTree.addBranchNode(EXPR);
				if(!parseExpr()){
					this.pTree.endChildren();
					return false;
				}
				if(match(bOpList()) != null){
					this.pTree.addBranchNode(BOOLOPp);
					if(!parseBoolop()){
						this.pTree.endChildren();
						return false;
					}
					if(match(eList()) != null){
						this.pTree.addBranchNode(EXPR);
						if(!parseExpr()){
							this.pTree.endChildren();
							return false;
						}
						if(match(termList(RPAREN)) != null){
							this.addLeafNextTok(this.currentToken);
							this.pTree.endChildren();
							return true;
						}
						else{
							printError(RPAREN);
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
			this.pTree.addBranchNode(BOOLVALp);
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
			TempMain.verbosePrint("parseId()");
			this.addLeafNextTok(this.currentToken);
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(idList());    //not sure if this will reach
			return false;
		}
	}
	
	public boolean parseCharList(){
		TempMain.verbosePrint("parseCharList()");
		if(match(cLList()) != null){
			if(match(cLList()) == CHAR){
				this.pTree.addBranchNode(CHARp);
				if(!parseChar()){
					this.pTree.endChildren();
					return false;
				}
			}
			else if(match(cLList()) == SPACE){
				this.pTree.addBranchNode(SPACEp);
				if(!parseSpace()){
					this.pTree.endChildren();
					return false;
				}
			}
			this.pTree.addBranchNode(CHAR_LIST);
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
			TempMain.verbosePrint("parseType()");
			this.addLeafNextTok(this.currentToken);
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
			TempMain.verbosePrint("parseChar()");
			this.addLeafNextTok(this.currentToken);
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
			TempMain.verbosePrint("parseSpace()");
			this.addLeafNextTok(this.currentToken);
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
			TempMain.verbosePrint("parseDigit()");
			this.addLeafNextTok(this.currentToken);
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
			TempMain.verbosePrint("parseBoolop()");
			this.addLeafNextTok(this.currentToken);
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
			TempMain.verbosePrint("parseBoolval()");
			this.addLeafNextTok(this.currentToken);
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
			TempMain.verbosePrint("parseIntop()");
			this.addLeafNextTok(this.currentToken);
			this.pTree.endChildren();
			return true;
		}
		else{
			printError(intopList());
			return false;
		}
	}
	
	public TokenType match(List<TokenType> tokenTypesL){
		TokenType returnType = null;
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
	public static List<TokenType> intopList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(INTOP);
		return enumL;
	}
	public static List<TokenType> bValList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(BOOLVAL);
		return enumL;
	}
	public static List<TokenType> bOpList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(BOOLOP);
		return enumL;
	}
	public static List<TokenType> digitList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(DIGIT);
		return enumL;
	}
	public static List<TokenType> spaceList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(SPACE);
		return enumL;
	}
	public static List<TokenType> charList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(CHAR);
		return enumL;
	}
	public static List<TokenType> typeList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(TYPE);
		return enumL;
	}
	public static List<TokenType> cLList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(CHAR);
		enumL.add(SPACE);
		return enumL;
	}
	public static List<TokenType> idList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(ID);
		return enumL;
	}
	public static List<TokenType> boolEList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(LPAREN);
		enumL.addAll(bValList());
		return enumL;
	}
	public static List<TokenType> strEList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(DQUOTE);
		return enumL;
	}
	public static List<TokenType> intEList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(DIGIT);
		return enumL;
	}
	public static List<TokenType> eList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(DIGIT);
		enumL.add(DQUOTE);
		enumL.add(LPAREN);
		enumL.add(ID);
		enumL.add(BOOLVAL);
		return enumL;
	}
	public static List<TokenType> keywordList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(KEYWORD);
		return enumL;
	}
	public static List<TokenType> varDList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.addAll(typeList());
		return enumL;
	}
	public static List<TokenType> assignList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(ID);
		return enumL;
	}
	public static List<TokenType> stmtList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.addAll(varDList());
		enumL.addAll(keywordList()); //if while print
		enumL.addAll(typeList());
		enumL.addAll(assignList());
		enumL.addAll(blockList());
		return enumL;
	}
	public static List<TokenType> stmtLList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.addAll(stmtList());
		return enumL;
	}
	public static List<TokenType> blockList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(LCBRACE);
		return enumL;
	}
	public static List<TokenType> progList(){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.addAll(blockList());
		return enumL;
	}
	//for other random terminals i have to check for
	public static List<TokenType> termList(TokenType termEnum){
		List<TokenType> enumL = new ArrayList<TokenType>();
		enumL.add(termEnum);
		return enumL;
	}
	/*------------------------|
	 *                        |
	 * Error Print Methods    |
	 *                        |
	 ------------------------*/
	public void printError(TokenType expected){
		if(TempMain.isVerboseOnLP){
			System.out.println("Error: Expected [" + expected + "] got [" + this.currentToken.getType()
			+ "] on line " + this.currentToken.getLineNum());
		}
	}
	public void printError(List<TokenType> expectedList){
		if(TempMain.isVerboseOnLP){
			String error = "Error: Expected [";
			for(int i = 0; i < expectedList.size(); i++){
				error += expectedList.get(i);
				if(i != expectedList.size()-1){
					error += "], or [";
				}
			}
			error += "] got [" + this.currentToken.getType() + "] on line " + this.currentToken.getLineNum();
			System.out.println(error);
		}
	}
}

