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
	public String[] runEnv = new String[255];
	public String[] digitMemLocs = new String[10];
	public List<JumpData> jumpTable = new ArrayList<JumpData>();
	public List<StaticData> staticTable = new ArrayList<StaticData>(); //first 2 locs are reserved for comparison
	public List<StaticData> tempTable = new ArrayList<StaticData>();
	public List<StaticData> stringTable = new ArrayList<StaticData>();
	public int codeIndex = 0;
	public int heapIndex = 254;
	public int currentScope = 0;
	public int previousScope = -1;
	
	public CodeGen(SymbolTable<N> sT){
		this.symbolTree = sT;
		this.ast = sT.ast;
		storeDigits();
	}
	public void startCodeGen(){
		int i = 0;
		while(i <= heapIndex){
			runEnv[i] = "00";
			i++;
		}
		processBlock(symbolTree.ast.root);
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
			this.printVerbose("VAR_DECL");
			opCodes = convertVarDecl(node);
			addToRunEnvCode(opCodes);
		}
		else if(node.data == ASSIGNMENT_STATEMENT){
			this.printVerbose("ASSIGNMENT_STATEMENT");
			opCodes = convertAssign(node);
			addToRunEnvCode(opCodes);
		}
		else if(node.data == PRINT_STATEMENT){
			this.printVerbose("PRINT_STATEMENT");
			opCodes = convertPrint(node.children.get(0));
			this.addToRunEnvCode(opCodes);
		}
		else if(node.data == WHILE_STATEMENT){
			this.printVerbose("WHILE_STATEMENT");
			opCodes = convertIf(node.children.get(0));
			int jumpTo = codeIndex-opCodes.length;
			this.addToRunEnvCode(opCodes);
			int jumpIndex = codeIndex;
			
			this.backPatchJump(jumpIndex, jumpTo);
			opCodes = convertWhile(node.children.get(0));
			
			this.addToRunEnvCode(opCodes);
			processBlock(node.children.get(1));
			ifBackPatchJump();
		}	
		else if(node.data == IF_STATEMENT){
			this.printVerbose("IF_STATEMENT");
			opCodes = convertIf(node.children.get(0));
			this.addToRunEnvCode(opCodes);
			int jumpIndex = codeIndex-1;
			processBlock(node.children.get(1));
			int jumpTo = codeIndex;
			backPatchJump(jumpIndex, jumpTo);
			
		}
	}
	
	public void backPatchJump(int index, int jumpTo){
		String hex;
		int dist;
		if(runEnv[index].charAt(0) == 'J'){
			if(index <= jumpTo){
				dist = jumpTo-(index+1);
				hex = Integer.toHexString(dist);
				if(hex.length() < 2){
					hex = "0"+hex;
				}
			}
			else {
				dist = runEnv.length-(index-jumpTo);
				hex = Integer.toHexString(dist);
			}
			runEnv[index] = hex;
		}
	}
	
	public void ifBackPatchJump(){
		for(int i = 0; i < runEnv.length; i++){
			if(runEnv[i].charAt(0) == 'J'){
				String hex = Integer.toHexString(codeIndex-(i+1));
				if(hex.length() != 2){
					hex = "0"+hex;
				}
				runEnv[i] = hex;
			}
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
		String[] load;
		String[] idMemLoc = getTermMemLoc(node.children.get(0));
		SyntaxTree.Node<N> expr = node.children.get(1);
		String[] exprOpCodes = convertExpr(expr);
		
		String[] loadMem = {
				"AD",
				exprOpCodes[exprOpCodes.length-2],
				exprOpCodes[exprOpCodes.length-1],
		};
		
		String[] loadPointer = {
				"A9",
				exprOpCodes[exprOpCodes.length-2]
		};
		
		String[] store = {
				"8D", 
				idMemLoc[0],
				idMemLoc[1]
		};
		TokenType tType = this.isString(node.children.get(1));
		if(tType == STRINGLITERAL){
			load = loadPointer;
		}
		else if(tType == ID){
			load = loadMem;
		}
		else{
			load = concat(exprOpCodes, loadMem);
		}
		String[] assignOpCodes = concat(load, store);
		return assignOpCodes;
	}
	/*-----------------|
	 *       !         |
	 * Print Code Gen  |
	 *                 |
	 -----------------*/
	public String[] convertPrint(SyntaxTree.Node<N> node){
		String[] exprOpCode = convertExpr(node);
		String[] loadYRegMem = {
				"AC", 
				exprOpCode[exprOpCode.length-2],
				exprOpCode[exprOpCode.length-1]
		};
		//can be a constant but for me it will only be pointers
		String[] loadYRegPointer = {
				"A0",
				exprOpCode[exprOpCode.length-2],
		};
		String[] printOpCode = {
				"A2",
				"01",
				"FF"
		};
		String[] loadYReg = loadYRegMem;
		TokenType tType = this.isString(node);
		if(tType == STRINGLITERAL){
			printOpCode[1] = "02";
			loadYReg = loadYRegPointer;
		}
		else if(tType == ID){
			printOpCode[1] = "02";
		}
		else{
			loadYReg = concat(exprOpCode, loadYReg);
		}
		printOpCode = concat(loadYReg, printOpCode);
		return printOpCode;
	}		
	/*-----------------|
	 *        !        |
	 * Recursively gen |
	 *    exprs        |
	 -----------------*/
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
		//Build op code sequence for left compare--------------------------------
		String[] leftExprCode = this.convertExpr(node.children.get(0));
		TokenType leftTType = this.isString(node.children.get(0));
		
		String[] xRegMemory = {
				"AE",
				leftExprCode[leftExprCode.length-2],
				leftExprCode[leftExprCode.length-1]
		};
		String[] xRegPointer =  {
				"A2",
				leftExprCode[leftExprCode.length-2]
		};
		
		String[] leftCode = xRegMemory;
		
		if(leftTType == STRINGLITERAL){
			leftCode = xRegPointer;
		}
		else if(leftTType == null){
			leftCode = concat(leftExprCode, leftCode);
		}
		//-------------------------------------------------------------------------
		//Build op code sequence for right compare
		String[] rightExprCode = this.convertExpr(node.children.get(1));
		TokenType rightTType = this.isString(node.children.get(1));
		String[] tempStorage = this.createNewTempLoc().temp;
		//copy the memory location to a t(n'th) temp location, then compare x reg with t
		String[] rightCode = {
				"8D",
				tempStorage[0],
				tempStorage[1],
				"EC",
				tempStorage[0],
				tempStorage[1]		
		};
		String[] loadPointer = {
				"A9",
				rightExprCode[rightExprCode.length-2]
		};
		String[] loadMemLoc = {
				"AD",
				rightExprCode[rightExprCode.length-2],
				rightExprCode[rightExprCode.length-1]
		};
		
		if(rightTType == STRINGLITERAL){
			rightCode = concat(loadPointer, rightCode);
		}
		else if(rightTType == null){
			loadMemLoc = concat(rightExprCode, loadMemLoc);
			rightCode = concat(loadMemLoc, rightCode);
		}
		else{
			rightCode = concat(loadMemLoc, rightCode);
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
	 * If Code Gen     |
	 *                 |
	 -----------------*/
	
	public String[] convertIf(SyntaxTree.Node<N> node){
		String[] exprOpCodes = this.convertExpr(node);
		String jumpTemp = this.createNewJump().temp;
		String[] ifOpCodes = {
				"D0", jumpTemp
		};
		return concat(exprOpCodes, ifOpCodes);
	}
	
	/*-----------------|
	 *                 |
	 * While Code Gen  |
	 *                 |
	 -----------------*/
	
	public String[] convertWhile(SyntaxTree.Node<N> node){
		//String[] exprOpCodes = this.convertExpr(node);
		//this.addToRunEnvCode(exprOpCodes);
		String jumpTemp = this.createNewJump().temp;
		String[] t1 = this.createNewTempLoc().temp;
		String[] bucOpCode = {
				"A9", "00",                  //load accum 0
				"8D", t1[0], t1[1],          //store to t1
				"A2", "01",                  //x reg = 1
				"EC", t1[0], t1[1],          //compare x reg to t1 (always false)
				"D0", jumpTemp               //jump to while
		};
		return bucOpCode;
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
		tempTable.clear();
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
			String[] memLoc;
			String pointer = this.getExistingStringPointer(nodeTok.getLit());
			if(pointer != null){
				memLoc = new String[]{pointer, "00"};
			}
			else {
				memLoc = new String[]{stringToHexList(nodeTok.getLit()), "00"};
			}
			return memLoc;
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
	
	public String getExistingStringPointer(String strLit){
		for(StaticData sData : stringTable){
			if(strLit.equals(sData.var)){
				return sData.temp[0];
			}
		}
		return null;
	}
	public void addToStringTable(String memHex, String strLit){
		StaticData stringStatic = new StaticData(new String[]{memHex}, strLit, 0, 0);
		stringTable.add(stringStatic);
	}
	
	/*-----------------|
	 *                 |
	 * Hex Conversion/ |
	 * Copy Code to Env|
	 -----------------*/
	//Takes a string, stores its 00 terminated hex representation
	//in the env. returns the memory location of the string
	public String stringToHexList(String str){
		int strEnvStart = heapIndex-(str.length());
		runEnv[heapIndex] = "00";
		heapIndex--;
		for(int i = heapIndex; i >= strEnvStart; i--){
			char c = str.charAt(i-strEnvStart);
			String charHex = Integer.toHexString((int)c);
			runEnv[i] = charHex;
		}
		String memStartHex = Integer.toHexString(strEnvStart);
		if(memStartHex.length() < 2){
			memStartHex = "0"+memStartHex;
		}
		this.addToStringTable(memStartHex, str);
		heapIndex = strEnvStart-1;
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
		System.out.println("");
		for(int i = 0; i < this.runEnv.length; i++){
			System.out.print(this.runEnv[i]+" ");
			if((i+1) % 8 == 0){
				System.out.print("\n");
			}
		}
		System.out.println("\n");
	}
	
	/*-----------------|
	 *                 |
	 * Extra/Unused    |
	 *                 |
	 -----------------*/
	public TokenType isString(SyntaxTree.Node<N> node){

		if(node.hasChildren()){
			return null;
		}
		else{
			Token nodeTok = (Token)node.data;
			if(nodeTok.getType() == STRINGLITERAL){
				return STRINGLITERAL;
			}
			else if(nodeTok.getType() == ID){
				SyntaxTree.Node<N> scope = this.symbolTree.getScopeByNumber(currentScope, this.symbolTree.root);
				Var foundVar = this.symbolTree.findId_InEntireScope(nodeTok, scope);
				if(foundVar.getType() == STRING){
					return ID;
				}
			}
		}
		return null;
	}
	public void printVerbose(String str){
		System.out.println("Generating: "+str);
	}
}
