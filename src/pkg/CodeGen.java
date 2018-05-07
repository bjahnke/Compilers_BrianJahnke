package pkg;
import static pkg.TokenType.*;
import static pkg.ProdType.*;
import static pkg.Type.*;
import static pkg.AbstractProd.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeGen<N> {
	public SyntaxTree<N> ast;
	public SymbolTable<N> symbolTree;
	public String[] runEnv = new String[96];
	public String[] digitMemLocs = new String[10];
	public List<JumpData> jumpTable = new ArrayList<JumpData>();
	public List<StaticData> staticTable = new ArrayList<StaticData>();
	public int codeIndex = 0;
	public int heapIndex = 95;
	public int currentScope = 0;
	
	public CodeGen(SymbolTable<N> sT){
		this.symbolTree = sT;
		this.ast = sT.ast;
		storeDigits();
	}
	public void startCodeGen(){
		processAST(symbolTree.ast.root);
		runEnv[codeIndex] = "00";
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
		if(node.data == BLOCK){
			currentScope = node.nodeNum;
			if(node.hasChildren()){
				for(SyntaxTree.Node<N> n : node.children){
					this.processAST(n);
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
			opCodes = convertAssign(node);
			//addToRunEnvCode(opCodes);
		}
		else if(node.data == PRINT_STATEMENT){
			opCodes = convertPrint(node.children.get(0));
			this.addToRunEnvCode(opCodes);
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
	
	/*-----------------|
	 *                 |
	 * Code Generation |
	 *     Functions   |
	 -----------------*/
	public String[] convertVarDecl(SyntaxTree.Node<N> declNode){
		SyntaxTree.Node<N> idNode = declNode.children.get(1);
		Token idTok = (Token)idNode.data;
		
		String[] varDeclOpCodes = concat(loadAccum_Const("0"), storeAccum_newLoc());
		String[] tempLoc = {varDeclOpCodes[3], varDeclOpCodes[4]};
		StaticData staticVar = new StaticData(tempLoc, idTok.getLit(), currentScope, staticTable.size());
		staticTable.add(staticVar);
		return varDeclOpCodes;
	}
	
	public String[] convertAssign(SyntaxTree.Node<N> node){
		String[] idMemLoc = getTermMemLoc(node.children.get(0));
		SyntaxTree.Node<N> expr = node.children.get(1);
		String[] exprOpCodes = convertExpr(node);
		String[] assignOpCodes = {
				"AD", 
				"",
				"",
				"8D", 
				"T"/* + get_id_index_from_constants*/, 
				"XX"
		};
		return assignOpCodes;	
	}
	
	public String[] convertPrint(SyntaxTree.Node<N> node){
		String[] exprOpCode = convertExpr(node);
		String[] printOpCode = null;
		int l = 0;
		//printOpCode[0] = "AC";
		if(!node.hasChildren()){
			Token nodeTok = (Token)node.data;
			if(nodeTok.getType() == STRINGLITERAL){
				//printOpCode[l+2] = "02";
			}
		}
		if(node.data == ADD){
			String[] storeAdd = storeAccum_newLoc();
			StaticData tempStore = new StaticData(storeAdd, "temp", currentScope, staticTable.size());
			staticTable.add(tempStore);
			exprOpCode = concat(exprOpCode, storeAdd);
			l = exprOpCode.length-1;
			printOpCode = new String[exprOpCode.length+6];
		}
			
		for(int i = 0; i < exprOpCode.length; i++){
			printOpCode[i] = exprOpCode[i];
		}
		printOpCode[l+1] = "AC";
		printOpCode[l+2] = staticTable.get(staticTable.size()-1).temp[1];
		printOpCode[l+3] = staticTable.get(staticTable.size()-1).temp[2];
		printOpCode[l+4] = "A2";
		printOpCode[l+5] = "01";
		printOpCode[l+6] = "FF";
		return printOpCode;
	}		
	
	public String[] convertExpr(SyntaxTree.Node<N> node){
		String opCode[] = null;
		if(node.data == COMPARE_EQ || node.data == COMPARE_NEQ){
			opCode = convertCompare(node);
		}
		else if(node.data == ADD){
			opCode = convertAdd(node);
			opCode[0] = "AD";
		}
		else{
			opCode = getTermMemLoc(node);
		}
		return opCode;
	}
	

	
	public String[] convertCompare(SyntaxTree.Node<N> node){
		String[] leftExprCode;
		String[] rightExprCode;
		String[] opCodes = null;
		if(node.data == COMPARE_EQ){
			String[] eqOpCodes = {
					"AE",
					"",
					"",
					"EC",
					"",
					"",
					"D0",
					"",//j0
				};
			opCodes = eqOpCodes;
		}
		else if(node.data == COMPARE_NEQ){
			String[] neqOpCodes = {
					""
			};
			opCodes = neqOpCodes;
		}
		if(node.children.size() > 1){
			leftExprCode = convertExpr(node.children.get(0));
			rightExprCode = convertExpr(node.children.get(1));
		}
		return opCodes;
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
		int leftDigitVal = Integer.parseInt(leftAddendTok.getLit());
		String[] leftOpCode = {"6D", digitMemLocs[leftDigitVal], "00"}; 
		
		//there will only be add productions so we just
		//have to check if the node has children. If so
		//then it is an add
		if(node.children.get(1).hasChildren()){              
			addList = convertAdd(node.children.get(1));
			return concat(leftOpCode, addList);
		}
		else{
			Token rightAddendTok = (Token)node.children.get(1).data;
			String[] rightOpCode;
			if(rightAddendTok.getType() == ID){
				String[] address = getStaticDataById(rightAddendTok).temp;
				String[] opCode = {
						"6D",
						address[0],
						address[1]	
				};
				rightOpCode = opCode;
			}
			else /*if(addendTok.getType() == DIGIT)*/{
				int rightDigitVal = Integer.parseInt(rightAddendTok.getLit());
				String[] opCode = {						
						"6D",
						digitMemLocs[rightDigitVal],
						"00",
				};
				rightOpCode = opCode;
			}
			return concat(leftOpCode, rightOpCode);
		}
	}
	/*-----------------|
	 *                 |
	 * Pointless       |
	 *                 |
	 -----------------*/
	public String[] loadAccum_Const(String num){
		String[] lda = {"A9", "0"+num};
		return lda;
	}
	
	public String[] storeAccum_newLoc(){
		String[] sta = {"8D", "T"+staticTable.size(), "XX"};
		return sta;
	}
	public String[] getNextConstLoc(){
		String[] address = {"T"+staticTable.size(), "XX"};
		return address;
	}
	
	//takes in a location returns code for unconditional jump to that memory location
	public String[] branchUnconditionally(String memLoc){
		//TODO code it
		return null;
	}
	
	/*-----------------|
	 *                 |
	 *Concatenate Lists|
	 *                 |
	 -----------------*/
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
	/*-----------------|
	 *                 |
	 * Id/MemLoc Search|
	 *                 |
	 -----------------*/
	public String[] getTermMemLoc(SyntaxTree.Node<N> node){
		Token nodeTok = (Token)node.data;
		if(nodeTok.getType() == STRINGLITERAL){
			String strLitMemLoc = stringToHexList(nodeTok.getLit());
			String[] codeMemLoc = {strLitMemLoc, "00"};
			return codeMemLoc;
		}
		else if(nodeTok.getType() == BOOLVAL){
			return getBoolvalMemLoc(nodeTok);
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
	
	public String[] getBoolvalMemLoc(Token boolTok){
		if(boolTok.getLit().equals("true")){
			String[] codeMemLoc = {digitMemLocs[0], "00"};
			return codeMemLoc;
		}
		else if(boolTok.getLit().equals("false")){
			String[] codeMemLoc = {digitMemLocs[1], "00"};
			return codeMemLoc;
		}
		else return null;
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
	
	/*-----------------|
	 *                 |
	 * Hex Conversion/ |
	 * Copy Code to Env|
	 -----------------*/
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
	
	public void storeDigits(){
		String memLocHex;
		for(int i = 9; i >= 0; i--){
			memLocHex = Integer.toHexString(heapIndex);
			digitMemLocs[i] = memLocHex;
			runEnv[heapIndex] = "0"+i;
			heapIndex--;
		}
	}
	
	public void addToRunEnvCode(String[] opCode){
		for(int i = 0; i < opCode.length; i++){
			runEnv[codeIndex] = opCode[i];
			codeIndex++;
		}
	}
	
	/*-----------------|
	 *                 |
	 * Print Run Env   |
	 *                 |
	 -----------------*/
	public void printRunEnv(){
		for(int i = 0; i < this.runEnv.length; i++){
			if(this.runEnv[i] == null){
				System.out.print("00 ");
			}
			else{
				System.out.print(this.runEnv[i]+" ");
			}
			if((i+1) % 5 == 0){
				System.out.print("\n");
			}
		}
		System.out.println("");
	}
	
	/*-----------------|
	 *                 |
	 * Extra/Unused    |
	 *                 |
	 -----------------*/
}
