package pkg;
import static pkg.TokenType.*;
import static pkg.ProdType.*;
import static pkg.Type.*;
import static pkg.AbstractProd.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeGen<N> {
	public SyntaxTree ast;
	public SymbolTable symbolTree;
	public String[] opCodes = new String[255];
	public List<StaticData> staticTable = new ArrayList<StaticData>();
	public int opCodeIndex = 0;
	
	public CodeGen(SymbolTable sT){
		this.symbolTree = sT;
		this.ast = sT.ast;
	}
	
	public String[] convertVarDecl(SyntaxTree.Node<N> declNode){
		SyntaxTree.Node<N> typeNode = declNode.children.get(0);
		SyntaxTree.Node<N> idNode = declNode.children.get(1);
		Token idTok = (Token)idNode.data;
		Token typeTok = (Token)typeNode.data;
		
		String[] varDeclOpCodes = concat(loadAccum_Const(0), storeAccum_newLoc());
		String tempLoc = varDeclOpCodes[3] + varDeclOpCodes[4];
		StaticData staticVar = new StaticData(tempLoc, idTok.getLit(), 0, staticTable.size());
		staticTable.add(staticVar);
		return varDeclOpCodes;
	}
	
	public String[] convertAssign(SyntaxTree.Node<N> node){
		String[] assignOpCodes = {
				"A9", 
				"0+getSum(0, node)", 
				"8D", 
				"T"/* + get_id_index_from_constants*/, 
				"XX"
		};
		return assignOpCodes;	
	}
	
	public String[] loadAccum_Const(int num){
		String[] lda = {"A9", "0"+num};
		return lda;
	}
	
	public String[] storeAccum_newLoc(){
		String[] sta = {"8D", "T"+staticTable.size()};
		return sta;
	}
	
	public String[] storeAccum_existingLoc(String memLoc){
		String[] sta = {"8D", "T"+memLoc};
		return sta;
	}
	
	public int getSum(int sum, SyntaxTree.Node<N> node){
		if(node.data == ADD){
			return getSum(sum, node.children.get(0)) + getSum(sum, node.children.get(1));
		}
		else {
			return getIntValue((Token)node.data);
		}

	}
		
	public int getIntValue(Token t){
		if(t.getType() == DIGIT){
			return Integer.parseInt(t.getLit());
		}
		else if(t.getType() == ID){
			//how do we find the memory location of an id
		}
		return 0;
	}
	
	public String[] concat(String[] first, String[] second){
		String[] both = new String[first.length+second.length];
		both = addToList(both, first, 0);
		both = addToList(both, second, first.length);
		return both;
	}
	
	public String[] addToList(String[] list, String[] newElements, int i){
		int x = 0;
		while(x < newElements.length){
			list[i] = newElements[x];
			i++;
			x++;
		}
		return list;
	}
	
}
