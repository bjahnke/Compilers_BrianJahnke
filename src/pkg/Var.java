package pkg;
import static pkg.VarType.*;
public class Var {
	private String id;
	private String val;
	private VarType type;
	private boolean isUsed;
	private boolean isInit;
	
	public Var(){};
	
	public Var(VarType t, String d){
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
}
