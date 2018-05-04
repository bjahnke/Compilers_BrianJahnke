package pkg;
import static pkg.TokenType.*;
import java.util.List;
import java.util.ArrayList;
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
	public static List<Token> progTokList = new ArrayList<Token>();
	public static List<SyntaxTree> parsedProgsList = new ArrayList<SyntaxTree>();
	public static int progNum = 1;   //compare with eop symbol number to see if there is a missing symbol.
	public static int progErrorCount = 0;
	public static boolean errorInProg = false;
	public static int lineNum = 1;
	

	//check reserved words as substring in charstring, returns the index of wordlist where the element matches the at
	//index, "index", of the input String
	public static int matchWordList(String input, String[] wordList, int index) {
		for(int x = 0; x < wordList.length; x++){
			if(input.indexOf(wordList[x], index) == index) {
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
	private static List<Token> verifyStringLit(String strLit){
		List<Token> strTokens = new ArrayList<Token>();
		Token newTok;
		int i = 1;
		boolean hasNoError = true;
		while(i < strLit.length()-1){
			char cChar = strLit.charAt(i);
			Matcher charM = charP.matcher(String.valueOf(cChar));
			if(charM.matches()){
				newTok = new Token(CHAR, ""+cChar, lineNum);
				strTokens.add(newTok);
			}
			else if(cChar == ' '){
				newTok = new Token(SPACE, "\\s", lineNum);
				strTokens.add(newTok);
			}
			else{
				if(!charM.matches() && cChar != ' ') {
					String badToken = Character.toString(cChar);
					if(cChar == '\n'){
						badToken = "\\n";
					}
					else if(cChar == '\t'){
						badToken = "\\t";
					}
					TempMain.verbosePrint("Error: Token \'" + badToken + "\' is illegal or not allowed in string");
					progErrorCount++;
					errorInProg = true;
					hasNoError = false;
				}
			}
			i++;
		}
		if(hasNoError){
			return strTokens;
		}
		else {
			return null;
		}
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
	
	/*-----------------|
	 *                 |
	 * Lexer           |
	 *                 |
	 -----------------*/
	public static List<SyntaxTree> lex(String input) {
		List<Token> result = new ArrayList<Token>();
		boolean eop = false;
		String errorMsg = "";
		String warningMsg = "";
		lineNum = 1;
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
					progTokList.clear();
					eop = false;
				}
			}
			if(matchWordList(input, keywords, i) > -1) {   
				int ind = matchWordList(input, keywords, i);
				newTok = new Token(KEYWORD, keywords[ind], lineNum);
				i += keywords[ind].length()-1;
			}
			else if(matchWordList(input, types, i) > -1) {
				int ind = matchWordList(input, types, i);
				newTok = new Token(TYPE, types[ind], lineNum); 
				i += types[ind].length()-1;
			}
			else if(matchWordList(input, boolval, i) > -1) {
				int ind = matchWordList(input, boolval, i);
				newTok = new Token(BOOLVAL, boolval[ind], lineNum);
				i += boolval[ind].length()-1;
			}
			else if(matchWordList(input, boolop, i) > -1) {
				int ind = matchWordList(input, boolop, i);
				newTok = new Token(BOOLOP, boolop[ind], lineNum);
				i += boolop[ind].length()-1;
			}
			else if(cChar == '(') {
				newTok = new Token(LPAREN, "(", lineNum);
			}
			else if(cChar == ')') {
				newTok = new Token(RPAREN, ")", lineNum);
			}
			else if(cChar == '{') {
				newTok = new Token(LCBRACE, "{", lineNum);
			}
			else if(cChar == '}') {
				newTok = new Token(RCBRACE, "}", lineNum);
			}
			else if(cChar == '=') {
				newTok = new Token(ASSIGN, "=", lineNum);
			}
			else if(cChar == '+') {
				newTok = new Token(INTOP, "+", lineNum);
			}
			else if(cChar == '\"') {
				String strLit = getStringLit(input, i);
				if(strLit.charAt(strLit.length()-1) == '\"'){
					List<Token> strToks = verifyStringLit(strLit);
					if(strToks != null){
						Token startQuote = new Token(DQUOTE, ""+input.charAt(i), lineNum);
						progTokList.add(startQuote);
						TempMain.verbosePrint(startQuote.toString());
						for(int ind = 0; ind < strToks.size(); ind++){
							progTokList.add(strToks.get(ind));
							TempMain.verbosePrint(strToks.get(ind).toString());
						}
						i += strLit.length()-1;
						newTok = new Token(DQUOTE, ""+input.charAt(i), lineNum);
					}
					else{
						i += strLit.length()-1;
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
				newTok = new Token(EOP, "$", lineNum);
				eop = true;
			}
			else if(digitM.matches()) {
				newTok = new Token(DIGIT, String.valueOf(cChar), lineNum);
			}
			else if(charM.matches()) {
				newTok = new Token(ID, String.valueOf(cChar), lineNum);
			}
			else if(cChar == '\n') {
				lineNum++;
			}
			else if(cChar != '\t' && cChar != ' ') {
					errorMsg = "Error: Illegal Token \'" + cChar + "\', ln: " + lineNum;
					errorInProg = true;
					progErrorCount++;
					errorList.add(errorMsg);
			}
			if(i == input.length()-1  && result.get(result.size()-1).getType() != EOP) {
				warningMsg = "Warning: $ at file end not present. Added Automatically to prevent failure.";
				newTok = new Token(EOP, "$", lineNum); 
				eop = true;
			}
			
			if(newTok != null){
				result.add(newTok);
				progTokList.add(newTok);
				TempMain.verbosePrint(newTok.toString());
				if(eop){
					if(errorInProg){
						System.out.println("Lex completed with " + progErrorCount + " error(s)\n"
								+ "Parse not ran on program.\n");
						errorInProg = false;
						progErrorCount = 0;
					}
					else{
						System.out.println("Lex completed successfully\n\nParsing Program " + progNum);
						SyntaxTree validParse = Parser.parse(progTokList);
						if(validParse != null){
							parsedProgsList.add(validParse);
						}
						progTokList.clear();
					}
					progNum++;
				}
			}
			else if(errorMsg != ""){
				TempMain.verbosePrint(errorMsg);
				errorMsg = "";
			}
		}
		if(warningMsg != ""){
			TempMain.verbosePrint(warningMsg);
			warningMsg = "";
		}
		return parsedProgsList;
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
}