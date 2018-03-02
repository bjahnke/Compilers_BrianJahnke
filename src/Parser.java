
import java.util.List;

public class Parser {
	
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
		System.out.println("parse()");
		parseProg();
	}
	public static void parseProg(){
		System.out.println("parseProg()");
		parseBlock();
		match(Lexer.tokenType.EOP);
	}
	public static void parseBlock(){
		System.out.println("parseBlock()");
		match(Lexer.tokenType.LCBRACE);
		parseStmtList();
		match(Lexer.tokenType.RCBRACE);
	}
	public static void parseStmtList(){
		System.out.println("parse()");
		parseStmt();
		parseStmtList();
		//or empty
	}
	public static void parseStmt(){
		System.out.println("parse()");
		parsePrint();
		//OR, assign
		//vardecl
		//while
		//if
		//block
	}
	public static void parsePrint(){
		System.out.println("parse()");
		match(Lexer.tokenType.KEYWORD);
		match(Lexer.tokenType.LPAREN);
		parseExpr();
		match(Lexer.tokenType.RPAREN);
	}
	public static void parseAssign(){
		System.out.println("parse()");
		match(Lexer.tokenType.ID);
		match(Lexer.tokenType.ASSIGN);
		parseExpr();
	}
	public static void parseVarDecl(){
		System.out.println("parse()");
		parseType();
		parseId();
	}
	public static void parseWhile(){
		System.out.println("parse()");
		match(Lexer.tokenType.KEYWORD);
		parseBoolExpr();
		parseBlock();
	}
	public static void parseIf(){
		System.out.println("parse()");
		match(Lexer.tokenType.KEYWORD);
		parseBoolExpr();
		parseBlock();
	}
	public static void parseExpr(){
		System.out.println("parse()");
		parseIntExpr();
		//OR, string
		//boolean
		//Id
	}
	public static void parseIntExpr(){
		System.out.println("parse()");
		match(Lexer.tokenType.DIGIT);
		parseIntop();
		parseExpr();
		//OR, digit
	}
	public static void parseStrExpr(){
		System.out.println("parse()");
		match(Lexer.tokenType.DQUOTE);
		parseCharList();
		match(Lexer.tokenType.DQUOTE);
	}
	public static void parseBoolExpr(){
		System.out.println("parse()");
		match(Lexer.tokenType.LPAREN);
		parseExpr();
		parseBoolop();
		parseExpr();
		match(Lexer.tokenType.LPAREN);
	}
	public static void parseId(){
		System.out.println("parse()");
		parseChar();
	}
	public static void parseCharList(){
		System.out.println("parse()");
		parseChar();
		parseCharList();
		//or parse space charlist, or empty
	}

	public static void parseType(){
		System.out.println("parse()");
		match(Lexer.tokenType.KEYWORD);
	}
	public static void parseChar(){
		System.out.println("parse()");
		match(Lexer.tokenType.CHAR);
	}
	public static void parseSpace(){
		System.out.println("parse()");
		match(Lexer.tokenType.SPACE);
	}
	public static void parseDigit(){
		System.out.println("parse()");
		match(Lexer.tokenType.DIGIT);
	}
	public static void parseBoolop(){
		System.out.println("parse()");
		match(Lexer.tokenType.BOOLOP);
	}
	public static void parseBoolval(){
		System.out.println("parse()");
		match(Lexer.tokenType.BOOLVAL);
	}
	public static void parseIntop(){
		System.out.println("parse()");
		match(Lexer.tokenType.INTOP);
	}
	public static void match(List<Lexer.tokenType> tokens){
	}
	public static void match(Lexer.tokenType tEnum/*,current token*/){
		switch(tEnum){
		case KEYWORD:
			break;
		case TYPE:
			break;
		case BOOLOP:
			break;
		case BOOLVAL:
			break;
		case CHAR:
			break;
		case DIGIT:
			break;
		case SPACE:
			break;
		case INTOP:
			break;
		case LPAREN:
			break;
		case RPAREN:
			break;
		case LCBRACE:
			break;
		case RCBRACE:
			break;
		case EOP:
			break;
		default:
			//hope not
		}
	}
	public static void nextToken(){
		
	}
}
