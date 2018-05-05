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
	public String[] runEnv = new String[255];
	public String[] digitMemLocs = new String[10];
	public String[] boolValMemLocs = new String[2];
	public List<StaticData> staticTable = new ArrayList<StaticData>();
	public int codeIndex = 0;
	public int heapIndex = 154;
	public int currentScope = 0;
	
	public CodeGen(SymbolTable sT){
		this.symbolTree = sT;
		this.ast = sT.ast;
	}
	
	public void processAST(){
		/*
		 * if the node is a block, if, or while
		 * then, get the node number and make that current scope
		 * also, recall processAST on its children
		 * 
		 * else, processProd on current node.
		 * 
		 * */
		
		
	}
	
	public void processProd(SyntaxTree.Node<N> node){
		String[] opCodes;
		if(node.data == ADD){
			
		}
		else if(node.data == COMPARE_EQ){
			
		}
		else if(node.data == COMPARE_NEQ){
			
		}
		else if(node.data == VAR_DECL){
			System.out.println("Converting VarDecl to Code");
			opCodes = convertVarDecl(node);
			addToRunEnvCode(opCodes);
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
		StaticData staticVar = new StaticData(tempLoc, idTok.getLit(), currentScope, staticTable.size());
		staticTable.add(staticVar);
		return varDeclOpCodes;
	}
	
	public String[] convertAssign(SyntaxTree.Node<N> node){
		String[] exprOpCodes = convertExpr(node);
		String[] assignOpCodes = {
				"A9", 
				"", 
				"8D", 
				"T"/* + get_id_index_from_constants*/, 
				"XX"
		};
		return assignOpCodes;	
	}
	
	public String[] convertExpr(SyntaxTree.Node<N> node){
		Token nodeTok = (Token)node.data;
		if(nodeTok.getType() == STRINGLITERAL){
			return stringToHexList(nodeTok.getLit());
		}
		else if(nodeTok.getType() == BOOLVAL){
			
		}
		else if(nodeTok.getType() == DIGIT)
		{
			
		}
		else if(nodeTok.getType() == ID){
			
		}
		return null;
	}
	
	//we found an add, 
	public String[] convertAdd(SyntaxTree.Node<N> node){
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
				String[] opCode = {
						"6D",
						address[0],
						address[1]	
				};
				return opCode;
			}
			else /*if(addendTok.getType() == DIGIT)*/{
				String[] address = getNextConstLoc();
				int digitVal = Integer.parseInt(rightAddendTok.getLit());
				String[] opCode = {						
						"6D",
						"0"+digitMemLocs[digitVal],
						"00",
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
		String[] address = {"T"+staticTable.size(), "XX"};
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
	
	public void storeDigits(){
		String memLocHex;
		for(int i = 9; i >= 0; i++){
			memLocHex = Integer.toHexString(heapIndex);
			digitMemLocs[i] = memLocHex;
			runEnv[heapIndex] = "0"+i;
			heapIndex--;
		}
	}
	
	public void storeBooleans(){
		boolValMemLocs[0] = stringToHexList("true");
		boolValMemLocs[1] = stringToHexList("false");
	}
	
	//this would have the effect of dynamic scope
	//cause if the last time an id was used was in
	//a previous but non-parent scope, it will return it
	//need more work on this, just a placeholder for now
	public String[] lookupConstantAddress(String id){
		for(int i = 0; i < staticTable.size(); i++){
			if(staticTable.get(i).var.equals(id)){
				return staticTable.get(i).temp;
			}
		}
		return null;
	}
	
	//Takes a string, stores its 00 terminated hex representation
	//in the env. returns the memory location of the string
	public String stringToHexList(String str){
		String[] hexString = new String[str.length()+1];
		int memStart = heapIndex-(str.length()+1);
		
		for(int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			hexString[i] = Integer.toHexString((int)c);
			runEnv[memStart+i] = hexString[i];
		}
		
		hexString[hexString.length-1] = "00";
		runEnv[heapIndex] = "00";
		heapIndex = memStart-1;
		String memStartHex = Integer.toHexString(memStart);
		return memStartHex;
	}
	
	public void addToRunEnvCode(String[] opCode){
		for(int i = 0; i < opCode.length; i++){
			runEnv[codeIndex] = opCode[i];
			codeIndex++;
		}
	}
	
	public String getBoolLoc(boolean boolval){
		if(boolval == true){
			return boolValMemLocs[0];
		}
		else if(boolval == false){
			return boolValMemLocs[1];
		}
		return null;
	}
}
