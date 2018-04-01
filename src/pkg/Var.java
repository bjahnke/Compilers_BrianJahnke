package pkg;
import static pkg.Type.*;
public class Var {
	private String id;
	private String val;
	private Type type;
	private boolean isUsed;
	private boolean isInit;
	
	public Var(){};
	
	public Var(Type t, String d){
		this.type = t;
		this.id = d;
		this.val = "";
		this.isUsed = false;
		this.isInit = false;
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
	
	public Type getType(){
		return this.type;
	}
	
	public String getID(){
		return this.id;
	}
}
