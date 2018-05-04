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
	
	
	public void processProd(SyntaxTree.Node<N> node){
		if(node.data == ADD){
			
		}
		else if(node.data == COMPARE_EQ){
			
		}
		else if(node.data == COMPARE_NEQ){
			
		}
		else if(node.data == VAR_DECL){
			
		}
		else if(node.data == ASSIGNMENT_STATEMENT){
			
		}
		else if(node.data == PRINT_STATEMENT){
			
		}
		else if(node.data == WHILE_STATEMENT){
			
		}	
		else if(node.data == IF_STATEMENT){
			
		}
	}
	
	public void processType(SyntaxTree.Node<N> node){
		if(node.data == INT){
			
		}
		if(node.data == STRING){
			
		}
		if(node.data == BOOLEAN){
			
		}
	}
	
	public String[] convertVarDecl(SyntaxTree.Node<N> declNode){
		SyntaxTree.Node<N> typeNode = declNode.children.get(0);
		SyntaxTree.Node<N> idNode = declNode.children.get(1);
		Token idTok = (Token)idNode.data;
		Token typeTok = (Token)typeNode.data;
		
		String[] varDeclOpCodes = concat(loadAccum_Const("0"), storeAccum_newLoc());
		String[] tempLoc = {varDeclOpCodes[3], varDeclOpCodes[4]};
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
	
	//we found an add, 
	public String[] coifnvertAdd(SyntaxTree.Node<N> node){
		String[] addList;
		
		Token leftAddendTok = (Token)node.children.get(0).data;
		String[] leftAddendCode = {"0"+leftAddendTok.getLit()}; 
		
		//there will only be add productions so we just
		//have to check if the node has children. If so
		//then it is an add
		if(node.hasChildren()){              
			addList = convertAdd(node.children.get(1));
		}
		else{
			Token rightAddendTok = (Token)node.children.get(1).data;
			if(rightAddendTok.getType() == ID){
				String[] address = lookupConstantAddress(rightAddendTok.getLit());
				String opCode = {
						"AD",
						address[0],
						address[1],
						
				}
				return adcCode(constAddress);
			}
			else /*if(addendTok.getType() == DIGIT)*/{
				String[] address = getNextConstLoc();
				String[] opCode = {
						"A9", 
						"0"+rightAddendTok.getLit(),
						"8D",
						address[0],
						address[1], //can do with XX but whatever for now
						"6D"
				};
				StaticData primInt = new StaticData(address, "", 0, staticTable.size());
				staticTable.add(primInt);
				return opCode;
			}
		}
		String[] singleAddCode = new String[""]
		return null;
	}
	
	public String[] loadAccum_Const(String num){
		String[] lda = {"A9", "0"+num};
		return lda;
	}
	
	public String[] adcCode(String[] memLoc){
		String[] adcCode = {"6D", ""+memLoc[0], ""+memLoc[1]};
		return adcCode;
	}
	
	public String[] storeAccum_newLoc(){
		String[] sta = {"8D", "T"+staticTable.size(), "XX"};
		return sta;
	}
	public String[] getNextConstLoc(){
		String[] address = {"T"+staticTable.size(), "XX");
		return address;
	}
	public String[] storeAccum_existingLoc(String memLoc){
		String[] sta = {"8D", "T"+memLoc};
		return sta;
	}
	
	//printing from memory
	public String[] convertPrint(String memLoc, int printType){
		return null;
	}
	
	//printing constants
	public String[] convertPrint(int printValue){
		return null;
	}
	
	//takes in a location returns code for unconditional jump to that memory location
	public String[] branchUnconditionally(String memLoc){
		//TODO code it
		return null;
	}
	
	
	//don't need it/can't use it
	public int getSum(int sum, SyntaxTree.Node<N> node){
		if(node.data == ADD){
			return getSum(sum, node.children.get(0)) + getSum(sum, node.children.get(1));
		}
		else {
			return getIntValue((Token)node.data);
		}

	}
	//may not need this
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
	
	public String[] lookupConstantAddress(String id){
		for(int i = 0; i < staticTable.size(); i++){
			if(staticTable.get(i).var.equals(id)){
				return staticTable.get(i).temp;
			}
		}
		return null;
	}
	
}
