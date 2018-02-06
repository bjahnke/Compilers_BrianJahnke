import java.util.List;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Lexer {
	
	public static enum tokenType {
		EOP,
		CHAR,
		ID,
		SPACE,
		LCBRACE,
		RCBRACE,
		LPAREN,
		RPAREN,
		EQUALS,
		DQUOTE,
		PLUS,
		TRUE,
		FALSE,
		PRINT,
		WHILE,
		IF;	
	}
	
	
	
	public static List<tokenType> lex(String input) {
		List<tokenType> result = new ArrayList<tokenType>();
		for(int i = 0; i < input.length(); ) {
			switch(input.charAt(i)) {
	//		case "$"
			}
		}
		return null;
	}
	
	public static String trimWhiteSpace(String str) {
		return str.replace("/^\\s+ | \\s+$/g", "");
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