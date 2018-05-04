package pkg;

public class StaticData {
	protected final String[] temp;
	protected final String var;
	protected final int scope;
	protected final int offset;
	
	public StaticData(String[] t, String v, int s, int o){
		this.temp = t;
		this.var = v;
		this.scope = s;
		this.offset = o;
	}
}
