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
	public List<StaticData> staticTable = new ArrayList<StaticData>(); //first 2 locs are reserved for comparison
	public List<StaticData> tempTable = new ArrayList<StaticData>();
	public int codeIndex = 0;
	public int heapIndex = 95;
	public int currentScope = 0;
	public int previousScope = -1;
	
	public CodeGen(SymbolTable<N> sT){
		this.symbolTree = sT;
		this.ast = sT.ast;
		storeDigits();
	}
	public void startCodeGen(){
		processBlock(symbolTree.ast.root);
		int i = codeIndex;
		while(i <= heapIndex){
			runEnv[i] = "00";
			i++;
		}
		backPatch();
	}
	public void processBlock(SyntaxTree.Node<N> node){
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
					this.processBlock(n);
				}
			}
			int swap = currentScope;
			currentScope = previousScope;
			previousScope = swap;
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
			addToRunEnvCode(opCodes);
		}
		else if(node.data == PRINT_STATEMENT){
			opCodes = convertPrint(node.children.get(0));
			this.addToRunEnvCode(opCodes);
		}
		else if(node.data == WHILE_STATEMENT){
			//if the "if" statement is true first time, c
		}	
		else if(node.data == IF_STATEMENT){
			//if the if statement is true, call processBlock
		}
	}
	
	/*-----------------|
	 *                 |
	 * Code Generation |----------------------------------------------------------
	 *     Functions   |
	 -----------------*/
	/*-----------------|
	 *      !          |
	 * VarDecl Code Gen|
	 *                 |
	 -----------------*/
	public String[] convertVarDecl(SyntaxTree.Node<N> declNode){
		SyntaxTree.Node<N> idNode = declNode.children.get(1);
		Token idTok = (Token)idNode.data;
		StaticData staticVar = this.createNewConstant(idTok.getLit());
		String[] storeVar = {"8D", staticVar.temp[0], staticVar.temp[1]};
		String[] varDeclOpCodes = concat(loadAccum_Const("0"), storeVar);
		return varDeclOpCodes;
	}
	
	/*-----------------|
	 *      !          |
	 * Assign Code Gen |
	 *                 |
	 -----------------*/
	public String[] convertAssign(SyntaxTree.Node<N> node){
		String[] idMemLoc = getTermMemLoc(node.children.get(0));
		SyntaxTree.Node<N> expr = node.children.get(1);
		String[] exprOpCodes = convertExpr(expr);
		String[] assignOpCodes = {
				"AD", 
				exprOpCodes[exprOpCodes.length-2],
				exprOpCodes[exprOpCodes.length-1],
				"8D", 
				idMemLoc[0],
				idMemLoc[1]
		};
		return concat(exprOpCodes, assignOpCodes);
	}
	/*-----------------|
	 *       !         |
	 * Print Code Gen  |
	 *                 |
	 -----------------*/
	public String[] convertPrint(SyntaxTree.Node<N> node){
		String[] exprOpCode = convertExpr(node);
		String[] printOpCode = {
				"AC", 
				exprOpCode[exprOpCode.length-2],
				exprOpCode[exprOpCode.length-1],
				"A2",
				"01",
				"FF"
		};
		if(!node.hasChildren()){
			Token nodeTok = (Token)node.data;
			if(nodeTok.getType() == STRINGLITERAL){
				printOpCode[4] = "02";
			}
		}
		if(node.data == ADD){
			printOpCode = concat(exprOpCode, printOpCode);
		}
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
			String[] tempStore = {"8D", this.createNewTempLoc().temp[0], "XX"};
			opCode = concat(opCode, tempStore);
		}
		else{
			opCode = getTermMemLoc(node);
		}
		return opCode;
	}
	

	/*-----------------|
	 *        !        |
	 * Compare Code Gen|
	 *                 |
	 -----------------*/
	public String[] convertCompare(SyntaxTree.Node<N> node){
		String[] opCodes = null;
		String[] leftExprCode = this.convertExpr(node.children.get(0));
		String[] leftCode = {
				"AE",
				leftExprCode[leftExprCode.length-2],
				leftExprCode[leftExprCode.length-1]
		};

		String[] rightExprCode = this.convertExpr(node.children.get(1));
		String[] tempStorage = this.createNewTempLoc().temp;
		//copy the memory location to a t(n'th) temp location, then compare x reg with t
		String[] rightCode = {
				"AD",
				rightExprCode[rightExprCode.length-2],
				rightExprCode[rightExprCode.length-1],
				"8D",
				tempStorage[0],
				tempStorage[1],
				"EC",
				tempStorage[0],
				tempStorage[1]		
		};
		if(node.children.get(1).data == ADD){
			rightCode = concat(rightExprCode, rightCode);
		}
		
		if(node.data == COMPARE_NEQ){
			String[] neqOpCode = {
					"A9","00",    //load accum with 0
					"D0","02",    //jump over next intruct if not equal
					"A9","01",    //load accum with 1
					"A2","00",    //load x reg with 0
					"8D",tempStorage[0],tempStorage[1], //store accum to temp memory loc
					"EC",tempStorage[0],tempStorage[1], 
					//^if original compares were not equal, accum and x reg would be equal meaning 
					//the compare would be considered true. if they are equal, accum and x reg would 
					//be unequal, and the compare would be considered false.
			};
			rightCode = concat(rightCode, neqOpCode);
		}
		//condition fulfilled? store true. condition not fulfilled? store false.
		String[] trueLoc = this.getBoolvalMemLoc(true);
		String[] falseLoc = this.getBoolvalMemLoc(false);
		String[] lastPortion = {
				"AD", falseLoc[0], falseLoc[1], //load false's location
				"D0", "03",        //if false then jump to false storage
				"AD", trueLoc[0], trueLoc[1],   //load true's location
				"8D", tempStorage[0], tempStorage[1]	//store whatever was loaded in accum to temp
		};
		rightCode = concat(rightCode, lastPortion);
		opCodes = concat(leftCode, rightCode);
		return opCodes;
	}
	
	/*-----------------|
	 *       !         |
	 * Add Code Gen    |
	 *                 |
	 -----------------*/
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
	
	public JumpData createNewJump(){
		JumpData jData = new JumpData("J"+jumpTable.size());
		jumpTable.add(jData);
		return jData;
	}
	
	//creates new constant, adds adds it to constants list, and returns it
	public StaticData createNewConstant(String name){
		String[] loc = {"T"+staticTable.size(), "XX"};
		StaticData constant = new StaticData(loc, name, currentScope, staticTable.size());
		staticTable.add(constant);
		return constant;
	}
	
	//anything farther than a direct comparison becomes boolval comparison
	//want to save these separately because we don't want them to be overwriten by other 
	//direct comparisons, <---haha this is wrong. I thought I would only need 4 values
	//with this idea but that wrong.
	
	//WHAT I NEED: n pairs of temp storage for each nested compare there is. <--- no just n locs, x reg helps us out
	//Every time convertCompare is called, we allocate one more memory location as a temp storage value.
	public StaticData createNewTempLoc(){
		int tempOffset = staticTable.size()+tempTable.size();
		String[] loc = {"T"+tempOffset, "XX"};
		StaticData tempConst = new StaticData(loc, "temp", currentScope, tempOffset);
		tempTable.add(tempConst);
		return tempConst;
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
			String[] codeMemLoc = {digitMemLocs[1], "00"};
			return codeMemLoc;
		}
		else if(boolTok.getLit().equals("false")){
			String[] codeMemLoc = {digitMemLocs[0], "00"};
			return codeMemLoc;
		}
		else return null;
	}
	
	public String[] getBoolvalMemLoc(boolean boolval){
		if(boolval){
			return new String[]{digitMemLocs[1], "00"};
		}
		else{
			return new String[]{digitMemLocs[0], "00"};
		}
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
	
	public StaticData getConstByTemp(String temp){
		for(StaticData sData : tempTable){
			if(sData.temp[0].equals(temp)){
				return sData;
			}
		}
		for(StaticData sData : staticTable){
			if(sData.temp[0].equals(temp)){
				return sData;
			}
		}
		return null;
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
	
	public void backPatch(){
		for(int i = 0; i < runEnv.length; i++){
			if(runEnv[i].equals("XX")){
				int loc = (codeIndex+1) + this.getConstByTemp(runEnv[i-1]).offset;
				String hexLoc = Integer.toHexString(loc);
				runEnv[i-1] = hexLoc;
				runEnv[i] = "00";
			}
		}
	}
	
	/*-----------------|
	 *                 |
	 * Print Run Env   |
	 *                 |
	 -----------------*/
	public void printRunEnv(){
		for(int i = 0; i < this.runEnv.length; i++){
			System.out.print(this.runEnv[i]+" ");
			if((i+1) % 8 == 0){
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
