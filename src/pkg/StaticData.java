package pkg;

public class StaticData {
	protected final String[] temp;
	protected final String var;
	protected final int scope;
	protected final int offset;
	protected int[] indexLoc = new int[0];
	
	public StaticData(String[] t, String v, int s, int o){
		this.temp = t;
		this.var = v;
		this.scope = s;
		this.offset = o;
	}
	
	public void addIndexLoc(int newLoc){
		int[] updatedList = new int[this.indexLoc.length+1];
		for(int i = 0; i < this.indexLoc.length; i++){
			updatedList[i] = this.indexLoc[i];
		}
		updatedList[updatedList.length-1] = newLoc;
		this.indexLoc = updatedList;
	}
}
