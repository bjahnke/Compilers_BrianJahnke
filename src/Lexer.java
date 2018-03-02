import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Lexer {
	public static String[] keywords = {"if", "while", "print"};
	public static String[] types = {"int", "string", "boolean"};
	public static String[] boolval = {"true", "false"};
	public static String[] boolop = {"==", "!="};
	public static String[] comment = {"/*", "*/"};
	public static Pattern charP = Pattern.compile("[a-z]");
	public static Pattern digitP = Pattern.compile("[0-9]");
	public static List<String> errorList = new ArrayList<String>();
	public static List<String> warningList = new ArrayList<String>();
	public static List<String> progList = new ArrayList<String>();
	public static int progNum = 1;   //compare with eop symbol number to see if there is a missing symbol.
	public static int eopNum = 0;
	public static int progErrorCount = 0;
	public static boolean errorInProg = false;
	//Pattern keyWordP = Pattern.compile("[a-z][a-z]+");
	//Pattern 
	
	
	public static enum tokenType {
		EOP,
		DIGIT,
		CHAR,
		ID,
		SPACE,
		LCBRACE,
		RCBRACE,
		LPAREN,
		RPAREN,
		ASSIGN,
		DQUOTE,
		INTOP,
		BOOLVAL,
		BOOLOP,
		TYPE,
		STRINGLITERAL,
	    KEYWORD;
	}
	
	public static class Token {
		public final tokenType t;
		public final String c;
		public final int lineNum;
		
		public Token(tokenType t, String c, int lineNum) {
			this.t = t;
			this.c = c;
			this.lineNum = lineNum;
		}
		@Override public String toString() {
			return this.c + " --> " + this.t + "\tln: " + this.lineNum;
		}
	}
	//check reserved words as substring in charstring, returns the index of wordlist where the element matches the at
	//index, "index", of the input String
	private static int matchWordList(String input, String[] wordList, int index) {
		for(int x = 0; x < wordList.length; x++){
			if(input.indexOf(wordList[x], index) == index) {
				//System.out.println(wordList[x]);
				return x;
			}
		}
		return -1;
	}
	
	//Verifying strings seperate from the lex method because its annoying and messy to 
	//keep tabs on whether or not a quote is closed throughout the method.
	//Hopefully compartmentalizing this makes it more readable despite the reuse of code.
	private static String getStringLit(String input, int cIndex){
		int index = cIndex + 1;
		while(index < input.length()) {
			char cChar = input.charAt(index);
			if(cChar == '\"' || cChar == '$') {
				return (String)input.subSequence(cIndex, index+1);
			}
			index++;
		}
		return (String)input.subSequence(cIndex, index);
	}
	private static boolean verifyStringLit(String strLit){
		int i = 1;
		boolean hasNoError = true;
		while(i < strLit.length()-1){
			char cChar = strLit.charAt(i);
			Matcher charM = charP.matcher(String.valueOf(cChar));
			if(!charM.matches() && cChar != ' ') {
				String badToken = Character.toString(cChar);
				if(cChar == '\n'){
					badToken = "\\n";
				}
				else if(cChar == '\t'){
					badToken = "\\t";
				}
				System.out.println("Error: Token \'" + badToken + "\' is illegal or not allowed in string");
				progErrorCount++;
				errorInProg = true;
				hasNoError = false;
			}
			i++;
		}
		return hasNoError;
	}
	
	private static String removeComments(String input){
		int start = -1; 
		int end = -1;   
		String newInput = input;
		while(newInput.indexOf(comment[0]) != -1 && newInput.indexOf(comment[1]) != -1) {
			if(newInput.indexOf(comment[0]) < newInput.indexOf(comment[1])){
				start = newInput.indexOf(comment[0]);
				end = newInput.indexOf(comment[1])+2;
			}
			String commentStr = (String)newInput.subSequence(start, end);
			newInput = newInput.replace(commentStr, "");
		}
		return newInput;
	}
	
	public static List<Token> lex(String input) {
		List<Token> result = new ArrayList<Token>();
		
		boolean eop = false;
		String errorMsg = "";
		int lineNum = 1;
		input = removeComments(input);
		Token newTok;

		for(int i = 0; i < input.length(); i++) {
			char cChar = input.charAt(i);
			newTok = null;
			Matcher digitM = digitP.matcher(String.valueOf(cChar));
			Matcher charM = charP.matcher(String.valueOf(cChar));
			if(i == 0 || eop){
				if(i != input.length()-1)
				{
					System.out.println("Lexing Program " + progNum);
					eop = false;
				}
			}
			if(matchWordList(input, keywords, i) > -1) {   
				int ind = matchWordList(input, keywords, i);
				newTok = new Token(tokenType.KEYWORD, keywords[ind], lineNum);
				i += keywords[ind].length()-1;
			}
			else if(matchWordList(input, types, i) > -1) {
				int ind = matchWordList(input, types, i);
				newTok = new Token(tokenType.TYPE, types[ind], lineNum); 
				i += types[ind].length()-1;
			}
			else if(matchWordList(input, boolval, i) > -1) {
				int ind = matchWordList(input, boolval, i);
				newTok = new Token(tokenType.BOOLVAL, boolval[ind], lineNum);
				i += boolval[ind].length()-1;
			}
			else if(matchWordList(input, boolop, i) > -1) {
				int ind = matchWordList(input, boolop, i);
				newTok = new Token(tokenType.BOOLOP, boolop[ind], lineNum);
				i += boolop[ind].length()-1;
			}
			else if(cChar == '(') {
				newTok = new Token(tokenType.LPAREN, "(", lineNum);
			}
			else if(cChar == ')') {
				newTok = new Token(tokenType.RPAREN, ")", lineNum);
			}
			else if(cChar == '{') {
				newTok = new Token(tokenType.LCBRACE, "{", lineNum);
			}
			else if(cChar == '}') {
				newTok = new Token(tokenType.RCBRACE, "}", lineNum);
			}
			else if(cChar == ' ') {
				newTok = new Token(tokenType.SPACE, "\\s", lineNum);
			}
			else if(cChar == '=') {
				newTok = new Token(tokenType.ASSIGN, "=", lineNum);
			}
			else if(cChar == '+') {
				newTok = new Token(tokenType.INTOP, "+", lineNum);
			}
			else if(cChar == '\"') {
				String strLit = getStringLit(input, i);
				//System.out.println(strLit);
				if(strLit.charAt(strLit.length()-1) == '\"'){
					i = i + strLit.length();
					if(verifyStringLit(strLit)){
						newTok = new Token(tokenType.STRINGLITERAL, strLit, lineNum);
					}
				}
				else{
					errorMsg = "Error: Unterminated string";
					errorInProg = true;
					progErrorCount++;
					errorList.add(errorMsg);
					i = i + strLit.length()-2;    //-1 to not skip over eop, maybe matters
				}
			}
			else if(cChar == '$') {
				newTok = new Token(tokenType.EOP, "$", lineNum);
				//progList.add((String)input.subSequence(0, i));
				//eopNum++;
				if(i < input.length()-1) {
					progNum++;
				}
				eop = true;
			}
			else if(digitM.matches()) {
				newTok = new Token(tokenType.DIGIT, String.valueOf(cChar), lineNum);
			}
			else if(charM.matches()) {
				newTok = new Token(tokenType.ID, String.valueOf(cChar), lineNum);
			}
			else if(cChar == '\n') {
				lineNum++;
			}
			else if(cChar != '\t') {
					errorMsg = "Error: Illegal Token \'" + cChar + "\', ln: " + lineNum;
					errorInProg = true;
					progErrorCount++;
					errorList.add(errorMsg);
			}
			
			if(newTok != null){
				result.add(newTok);
				System.out.println(newTok.toString());
				if(eop){
					if(errorInProg){
						System.out.println("Lex completed with " + progErrorCount + " error(s)\n");
						errorInProg = false;
						progErrorCount = 0;
					}
					else{
						System.out.println("Lex completed successfully\n");
					}
				}
			}
			else if(errorMsg != ""){
				System.out.println(errorMsg);
				errorMsg = "";
			}
		}	
		if(input.charAt(input.length()-1) != '$') {
			String warning = "Warning: Program does not end with a $.";
			warningList.add(warning);
		}
		return result;
	}
/*-----------------|
 *                 |
 * Lex Report      |
 *                 |
 -----------------*/
	public static void printLexReport(List<Token> tokenList){
		for(int i = 0; i < tokenList.size(); i++){
			String tokenObjInfo = tokenList.get(i).toString();
			System.out.println(tokenObjInfo);
		}
			
	}
	
	
/*------------------------|
 *                        |
 * Test File Load Methods |
 *                        |
 ------------------------*/
	public static String ScanFileReturnCharString(File testFile) {
		String charString = "";
		try {
			Scanner sc = new Scanner(testFile);
			
			String line = "";
			while (sc.hasNextLine()) {
	            line = sc.nextLine();
	            line = line.trim();
	            line = line + "\n";
	            for(int i = 0; i < line.length(); i++) {
	            	charString += line.charAt(i);
	            }	            
			}
			sc.close();
		} catch (InputMismatchException | FileNotFoundException e) {
			e.printStackTrace();
		}	
		return charString;
	}
	
	public static boolean customPathExists(File customFile){
		boolean exists = true;
		try{
			Scanner fileReader = new Scanner(customFile);
			fileReader.close();
		}
		catch (FileNotFoundException e){
			System.out.println("Error: File not found at specified path \n"
					+ "");
			exists = false;
		}
		return exists;
	}
	
	public static File getTestFile(){
		Scanner reader = new Scanner(System.in);
		System.out.println("Options:\n"
				+ "(1) Test file with custom path.\n"
				+ "(2) Default test file. (Only For Brian)");
		while(true){
			System.out.println("Chose 1 or 2: ");
			int option = reader.nextInt();
			if(option == 1){
				System.out.println("Test File Path (include file name): ");
				String testFilePath = reader.next();
				File customFile = new File(testFilePath);
				boolean pathExists = customPathExists(customFile);
				if(pathExists){
					reader.close();
					return customFile;
				}
			}
			else if(option == 2){
				try{
					File codeFile = new File("test_file1.txt");
					Scanner test = new Scanner(codeFile);
					test.close();
					return codeFile;
				}
				catch(FileNotFoundException e){
					System.out.println("FileNotFoundException: told ya so.");
				}
			}
			else {
				System.out.println("Not a valid option");
			}
		}
	}
	/*---------------------|
	 *                     |
	 * Lexer Main Method   |
	 *                     |
	 ---------------------*/
	public static void main(String[] args) {
		
		File testFile = getTestFile();
		String cString = ScanFileReturnCharString(testFile);
		//System.out.println(cString);
		List<Token> tList = lex(cString);
		System.out.println("\nHave nice day.");
		//printLexReport(tList);
		//System.out.println(tList.get(0));
		//System.out.println(tList.get(1));
		//System.out.println(tList.get(2));
		//System.out.println(tList.get(3));
		//String space = " ";
		//System.out.println(Arrays.toString(tList.toArray()));
		////System.out.println(Arrays.toString(progList.toArray()));
		//System.out.println(Arrays.toString(errorList.toArray()));
		
		
	}
}