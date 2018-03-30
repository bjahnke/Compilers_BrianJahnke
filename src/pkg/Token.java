package pkg;
import static pkg.TokenType.*;

public class Token {
	private TokenType type;
	private String lit;
	private int lineNum;
	
	public Token(){
		this.type = STRINGLITERAL;     //only use default for stringliteral
		this.lit = "";
		this.lineNum = -1;
	}
	
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
	
	public String getLit(){
		return this.lit;
	}
	
	public int getLineNum(){
		return this.lineNum;
	}
	
	public void setLineNum(int num){
		this.lineNum = num;
	}
	
	public void setLit(String s){
		this.lit = s;
	}
	
	public void setType(TokenType t){
		this.type = t;
	}
}