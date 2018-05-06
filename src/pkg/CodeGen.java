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
	
	public void processAST(SyntaxTree.Node<N> node){
		/*
		 * if the node is a block, if, or while
		 * then, get the node number and make that current scope
		 * also, recall processAST on its children
		 * 
		 * else, processProd on current node.
		 * 
		 * */
		if(node.data == BLOCK || node.data == IF_STATEMENT || node.data == WHILE_STATEMENT){
			currentScope = node.nodeNum;
			if(node.hasChildren()){
				for(SyntaxTree.Node<N> n : node.children){
					processAST(n);
				}
			}
		}
		else{
			processProd(node);
		}
		
	}
	
	public void processProd(SyntaxTree.Node<N> node){
		String[] opCodes;
		if(node.data == VAR_DECL){
			System.out.println("Converting VarDecl to Code");
			opCodes = convertVarDecl(node);
			addToRunEnvCode(opCodes);
		}
		else if(node.data == ASSIGNMENT_STATEMENT){
			String[] idMemLoc = getTermMemLoc(node.children.get(0));
			SyntaxTree.Node<N> expr = node.children.get(1);

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
		String[] exprOpCodes = getTermMemLoc(node);
		String[] assignOpCodes = {
				"A9", 
				"", 
				"8D", 
				"T"/* + get_id_index_from_constants*/, 
				"XX"
		};
		return assignOpCodes;	
	}
	
	public String[] converExpr(SyntaxTree.Node<N> node){
		String opCode[] = null;
		if(node.data == COMPARE_EQ){
			
		}
		else if(node.data == COMPARE_NEQ){
			
		}
		else if(node.data == ADD){
			opCode = convertAdd(node);
		}
		else{
			opCode = getTermMemLoc(node);
		}
		return opCode;
	}
	
	public String[] getTermMemLoc(SyntaxTree.Node<N> node){
		Token nodeTok = (Token)node.data;
		if(nodeTok.getType() == STRINGLITERAL){
			String strLitMemLoc = stringToHexList(nodeTok.getLit());
			String[] codeMemLoc = {strLitMemLoc, "00"};
			return codeMemLoc;
		}
		else if(nodeTok.getType() == BOOLVAL){
			if(nodeTok.getLit().equals("true")){
				String[] codeMemLoc = {boolValMemLocs[0], "00"};
				return codeMemLoc;
			}
			else{
				String[] codeMemLoc = {boolValMemLocs[1], "00"};
				return codeMemLoc;
			}
		}
		else if(nodeTok.getType() == DIGIT){
			int num = Integer.parseInt(nodeTok.getLit());
			String[] codeMemLoc = {digitMemLocs[num], "00"};
			return codeMemLoc;
		}
		else if(nodeTok.getType() == ID){
			StaticData id = getStaticDataById(nodeTok);
			return id.temp;
		}
		return null;
	}
	
	public String[] convertEq(SyntaxTree.Node<N> node){
		return null;
	}
	
	public String[] convertNEq(SyntaxTree.Node<N> node){
		return null;
	}
	
	//we found an add.
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
				String[] address = getStaticDataById(rightAddendTok).temp;
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
		String[] singleAddCode = new String[0];
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
	
	/*
	 * Checks the static table for a initialized ids with the same name as the one passed.
	 * If the static found has the same scope as the current one we are in, 
	 * then we found the static we want. If the scopes don't match, we need to check
	 * if the static is in a parent scope of our current scope. Combines the use of the 
	 * static table and symbol table to only search the symbol table with vars that 
	 * have been reached by code gen
	 * */
	public StaticData getStaticDataById(Token id){
		for(int i = staticTable.size()-1; i >= 0; i--){
			StaticData staticAtIndex = staticTable.get(i);
			if(staticAtIndex.var.equals(id.getLit())){
				if(staticAtIndex.scope == currentScope){
					return staticAtIndex;
				}
				else{
					
					SyntaxTree.Node<N> scopeOfCurrent = this.symbolTree.getScopeByNumber(currentScope, this.symbolTree.root);
					if(isStaticInScope(id, scopeOfCurrent.parent, staticAtIndex)){
						return staticAtIndex;
					}
				}
			}
		}
		return null;
	}
	
	//verifies that the most recently initialized id found,
	//but not in the current scope, is in a parent of the current scope
	public boolean isStaticInScope(Token id, SyntaxTree.Node<N> scope,StaticData staticAtIndex){
		SyntaxTree.Node<N> scopeOfConst = this.symbolTree.getScopeByNumber(staticAtIndex.scope, this.symbolTree.root);
		Var foundVar = this.symbolTree.findId_InEntireScope(id, scope);
		if(foundVar.getscopeNum() == staticAtIndex.scope){
			 return true;
		}
		else if(scope.parent != null){
			return isStaticInScope(id, scope.parent, staticAtIndex);
		}
		else{
			return false;
		}
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
	
	/*-----------------|
	 *                 |
	 * Print Run Env   |
	 *                 |
	 -----------------*/
	public void printRunEnv(){
		for(int i = 0; i < runEnv.length; i++){
			if(runEnv[i].equals("")){
				System.out.print("00 ");
			}
			else{
				System.out.print(runEnv[i]+" ");
			}
			if((i+1) % 8 == 0){
				System.out.print("\b\n");
			}
		}
	}
}
