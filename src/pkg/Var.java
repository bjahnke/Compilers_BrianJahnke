package pkg;
import static pkg.Type.*;
public class Var {
	private String id;
	private String val;
	private Type type;
	private int scopeNum;
	private int scopeListIndex;
	private int symbolTableIndex;
	private boolean isUsed;
	private boolean isInit;
	private Token idTok;
	
	public Var(){};
	
	public Var(Token t, Token iD, int sNum){
		this.idTok = iD;
		this.type = tokenToTypeEnum(t);
		this.id = iD.getLit();
		this.val = "";
		this.isUsed = false;
		this.isInit = false;
		this.scopeNum = sNum;
		this.scopeListIndex = 0;
		this.symbolTableIndex = 0;
	}
	
	public void setScopeListIndex(int i){
		this.scopeListIndex = i;
	}
	
	public void setSymbolTableIndex(int i){
		this.symbolTableIndex = i;
	}
	
	public void varIsUsed(){
		this.isUsed = true;
	}
	
	public void varIsInit(){
		this.isInit = true;
	}
	
	public void setValue(String value){
		this.val = value;
		this.varIsInit();
	}
	
	public Token getIdTok(){
		return this.idTok;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public String getID(){
		return this.id;
	}
	
	public boolean isInit(){
		return this.isInit;
	}
	
	public boolean isUsed(){
		return this.isUsed;
	}
	
	public int getscopeNum(){
		return this.scopeNum;
	}
	
	public int getScopeListIndex(){
		return this.scopeListIndex;
	}
	
	public int getSymbolTableIndex(){
		return this.symbolTableIndex;
	}
	
	public String toString(){
		String varStr = this.id +"\t"+ this.type +"\t"+ this.scopeNum +"\t"+ this.idTok.getLineNum();
		return varStr;
	}
	
	public static Type tokenToTypeEnum(Token t){
		if(t.getLit().equals("int")){
			return INT;
		}
		else if(t.getLit().equals("string")){
			return STRING;
		}
		else if(t.getLit().equals("boolean")){
			return BOOLEAN;
		}
		else{
			return null;
		}
	}
}
