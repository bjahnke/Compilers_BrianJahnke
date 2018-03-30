package pkg;

public class Token {
	private final TokenType type;
	private final String lit;
	private final int lineNum;
	
	public Token(TokenType t, String c, int lineNum) {
		this.type = t;
		this.lit = c;
		this.lineNum = lineNum;
	}
	@Override public String toString() {
		return this.lit + " --> " + this.type + "\tln: " + this.lineNum;
	}
	public TokenType getType(){
		return this.type;
	}
	public String getLiteralT(){
		return this.lit;
	}
	public int getLineNum(){
		return this.lineNum;
	}
}