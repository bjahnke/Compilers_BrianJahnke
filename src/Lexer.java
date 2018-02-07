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
			return this.t + ", " + this.c + ", " + this.lineNum + "||";
		}
	}
	//check reserved words as substring in charstring, returns the index of wordlist where the element matches the at
	//index, "index", of the input String
	private static int matchWordList(String input, String[] wordList, int index) {
		for(int x = 0; x < wordList.length; x++){
			if(input.indexOf(wordList[x]) == index) {
				return x;
			}
		}
		return -1;
	}
	
	private static String removeComments(String input){
		int start = -1; 
		int end = -1;   
		String newInput = input;
		while(newInput.indexOf(comment[0]) != -1 && newInput.indexOf(comment[1]) != -1) {
			start = newInput.indexOf(comment[0]);
				end = newInput.indexOf(comment[1])+2;
			String commentStr = (String)newInput.subSequence(start, end);
			newInput = newInput.replace(commentStr, "");
		}
		return newInput;
	}
	
	public static List<Token> lex(String input) {
		List<Token> result = new ArrayList<Token>();
		int quoteCounter = 0;
		
		input = removeComments(input);
		for(int i = 0; i < input.length(); i++) {
			
			char cChar = input.charAt(i);
			Matcher digitM = digitP.matcher(String.valueOf(cChar));
			Matcher charM = charP.matcher(String.valueOf(cChar));
			
			if(matchWordList(input, keywords, i) > -1) {   
				int ind = matchWordList(input, keywords, i);
				result.add(new Token(tokenType.KEYWORD, keywords[ind], 0)); //need to figure out line num
				i += keywords[ind].length()-1;
			}
			else if(matchWordList(input, types, i) > -1) {
				int ind = matchWordList(input, types, i);
				result.add(new Token(tokenType.TYPE, types[ind], 0));  //need to figure out line num				
				i += types[ind].length()-1;
			}
			else if(matchWordList(input, boolval, i) > -1) {
				int ind = matchWordList(input, boolval, i);
				result.add(new Token(tokenType.BOOLVAL, boolval[ind], 0)); //need to figure out line num
				i += boolval[ind].length()-1;
			}
			else if(matchWordList(input, boolop, i) > -1) {
				int ind = matchWordList(input, boolop, i);
				result.add(new Token(tokenType.BOOLOP, boolop[ind], 0));
				i += boolop[ind].length()-1;
			}
			if(cChar == '(') {
				result.add(new Token(tokenType.LPAREN, "(", 0));
			}
			else if(cChar == ')') {
				result.add(new Token(tokenType.RPAREN, ")", 0));
			}
			else if(cChar == '{') {
				result.add(new Token(tokenType.LCBRACE, "{", 0));
			}
			else if(cChar == '}') {
				result.add(new Token(tokenType.RCBRACE, "}", 0));
			}
			else if(cChar == ' ') {
				result.add(new Token(tokenType.SPACE, "\\s", 0));
			}
			else if(cChar == '=') {
				result.add(new Token(tokenType.ASSIGN, "=", 0));
			}
			else if(cChar == '+') {
				result.add(new Token(tokenType.INTOP, "+", 0));
			}
			else if(cChar == '\"') {
				result.add(new Token(tokenType.DQUOTE, "\"", 0));
			}
			else if(cChar == '$') {
				result.add(new Token(tokenType.EOP, "$", 0));
			}
			else if(digitM.matches()) {
				result.add(new Token(tokenType.DIGIT, String.valueOf(cChar), 0));
			}
			else if(charM.matches()) {
				result.add(new Token(tokenType.CHAR, String.valueOf(cChar), 0));
			}
			else {
				String error = "Error: illegal Token" + cChar + "on line: " + 0; // not a real line
			}
		}		
		return result;
	}
	
	//Scans file specified in folder test, returns a charstring of entire file
	public static String ScanFileReturnCharString(String fileName) {
		String charString = "";
		String workingDir = System.getProperty("user.dir") + "\\test";  //finds the test folder within the working directory
		File codeFile = new File(workingDir + "\\" + fileName); //scans the specified file at the given directory
	    System.out.print(workingDir + "\\" + "test_file1.txt\n");
		try {
			Scanner sc = new Scanner(codeFile);
			
			String line = "";
			while (sc.hasNextLine()) {
	            line = sc.nextLine();
	            line = line.trim();
	            for(int i = 0; i < line.length(); i++) {
	            	charString += line;
	            }
	            //System.out.println(line);	            
			}
			sc.close();
		} catch (InputMismatchException | FileNotFoundException e) {
			e.printStackTrace();
		}	
		return charString;
	}
	
	public static void main(String[] args) {
		String cString = ScanFileReturnCharString("test_file1.txt");
		List<Token> tList = lex(cString);
		System.out.println(Arrays.toString(tList.toArray()));
		
		
	}
}