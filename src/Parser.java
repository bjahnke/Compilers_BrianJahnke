import java.util.ArrayList;
import java.util.List;

public class Parser {
	private static List<Lexer.tokenType> matchList = new ArrayList<Lexer.tokenType>();
	private static List<Lexer.tokenType> typeList = new ArrayList<Lexer.tokenType>();
	
	public static enum productionType {
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
	
	public static void cstTree(){
		
	}
	
	public static void parse(List<Lexer.Token> tList){
		parseProg();
	}
	public static void parseProg(){
		parseBlock();
		match(Lexer.tokenType.EOP);
	}
	public static void parseBlock(){
		match(Lexer.tokenType.LCBRACE);
		parseStmtList();
		match(Lexer.tokenType.RCBRACE);
	}
	public static void parseStmtList(){
		parseStmt();
		parseStmtList();
		//or empty
	}
	public static void parseStmt(){
		parsePrint();
		//OR, assign
		//vardecl
		//while
		//if
		//block
	}
	public static void parsePrint(){
		match(Lexer.tokenType.KEYWORD);
		match(Lexer.tokenType.LPAREN);
		parseExpr();
		match(Lexer.tokenType.RPAREN);
	}
	public static void parseAssign(){
		match(Lexer.tokenType.ID);
		match(Lexer.tokenType.ASSIGN);
		parseExpr();
	}
	public static void parseVarDecl(){
		parseType();
		parseId();
	}
	public static void parseWhile(){
		match(Lexer.tokenType.KEYWORD);
		parseBoolExpr();
		parseBlock();
	}
	public static void parseIf(){
		match(Lexer.tokenType.KEYWORD);
		parseBoolExpr();
		parseBlock();
	}
	public static void parseExpr(){
		parseIntExpr();
		//OR, string
		//boolean
		//Id
	}
	public static void parseIntExpr(){
		match(Lexer.tokenType.DIGIT);
		parseIntop();
		parseExpr();
		//OR, digit
	}
	public static void parseStrExpr(){
		match(Lexer.tokenType.STRINGLITERAL);
		//need to move function for verifying contents of string to parser
	}
	public static void parseBoolExpr(){
		match(Lexer.tokenType.LPAREN);
		parseExpr();
		parseBoolop();
		parseExpr();
		match(Lexer.tokenType.LPAREN);
	}
	public static void parseId(){
		parseChar();
	}
	public static void parseCharList(){
		parseChar();
		parseCharList();
		//or parse space charlist, or empty
	}

	public static void parseType(){
		//typeList.add(Lexer.tokenType.)
	}
	public static void parseChar(){
		
	}
	public static void parseSpace(){
		
	}
	public static void parseDigit(){
		
	}
	public static void parseBoolop(){
		
	}
	public static void parseBoolval(){
	
	}
	public static void parseIntop(){
	
	}
	public static void match(List<Lexer.tokenType> tokens){
	}
	public static void match(Lexer.tokenType tEnum){
		
	}
	public static void nextToken(){
		
	}
}
