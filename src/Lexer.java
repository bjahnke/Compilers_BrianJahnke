import java.util.List;
import java.util.ArrayList;
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
	
	private static void removeComments(String input){
		int i = 0;
		int start = -1;
		int end = -1;
		//boolean  = true;
		while(input.indexOf(comment[0]) != -1 && input.indexOf(comment[1]) != -1) {
			start = input.indexOf(comment[0]);
			end = input.indexOf(comment[1]);
			input.repla
		}
	}
	
	public static List<tokenType> lex(String input) {
		List<Token> result = new ArrayList<Token>();
		int quoteCounter = 0;
		for(int i = 0; i < input.length(); i++) {
			char cChar = input.charAt(i);
			int resultIndex = matchWordList(input, keywords, i);
			if(resultIndex > -1) {   
				result.add(new Token(tokenType.KEYWORD, keywords[resultIndex], 0)); //need to figure out line num
			}
			else {
				resultIndex = matchWordList(input, types, i);
				if(resultIndex > -1){
					result.add(new Token(tokenType.TYPE, types[resultIndex], 0));  //need to figure out line num
				}
				else {
					resultIndex = matchWordList(input, boolval, i);
					if(resultIndex > -1) {
						result.add(new Token(tokenType.BOOLVAL, boolval[resultIndex], 0)); //need to figure out line num
					}
					else {
						resultIndex = matchWordList(input, boolop, i);
						if(resultIndex > -1) {
							result.add(new Token(tokenType.BOOLOP, boolop[resultIndex], 0));
						}
					}
				}
			}
			if(cChar == '(') {
				result.add(new Token(tokenType.LPAREN, "(", 0));
			}
			if(cChar == ')') {
				result.add(new Token(tokenType.RPAREN, ")", 0));
			}
			if(cChar == '{') {
				result.add(new Token(tokenType.LCBRACE, "{", 0));
			}
			if(cChar == '}') {
				result.add(new Token(tokenType.RCBRACE, "}", 0));
			}
			if(cChar == ' ') {
				result.add(new Token(tokenType.SPACE, "\\s", 0));
			}
			if(cChar == '=') {
				result.add(new Token(tokenType.ASSIGN, "=", 0));
			}
			if(cChar == '+') {
				result.add(new Token(tokenType.INTOP, "+", 0));
			}
			if(cChar == '\"') {
				result.add(new Token(tokenType.DQUOTE, "\"", 0));
			}
			if(cChar == '$') {
				result.add(new Token(tokenType.EOP, "$", 0));
			}
			else {
				Matcher digitM = digitP.matcher(String.valueOf(cChar));
				if(digitM.matches()) {
					result.add(new Token(tokenType.DIGIT, String.valueOf(cChar), 0));
				}
				else {
					Matcher charM = charP.matcher(String.valueOf(cChar));
					if(charM.matches()) {
						result.add(new Token(tokenType.CHAR, String.valueOf(cChar), 0));
					}
				}
			}
		}		
		return null;
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
	            System.out.println(line);	            
			}
			sc.close();
		} catch (InputMismatchException | FileNotFoundException e) {
			e.printStackTrace();
		}	
		return charString;
	}
	
	public static void main(String[] args) {
		String cString = ScanFileReturnCharString("test_file1.txt");
	}
}