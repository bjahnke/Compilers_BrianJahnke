package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class TempMain {
	public static boolean isVerboseOnLP; 
	
	public static void main(String[] args) {
		System.out.println("TempMain");
		File testFile = getTestFile();
		isVerboseOnLP = toggleVerbose();
		String cString = scanFileReturnCharString(testFile);
		List<Lexer.Token> tList = Lexer.lex(cString);
		
		System.out.println("\nHave nice day.");
	}
	
	
	/*------------------------|
	 *                        |
	 * Test File Load Methods |    only for lex and parse
	 *                        |
	 ------------------------*/
	public static void verbosePrint(String parse){   
		if(isVerboseOnLP){
			System.out.println(parse);
		}
	}
	
	public static boolean toggleVerbose(){
		Scanner reader = new Scanner(System.in);	
		while(true){
			System.out.println("Verbose output for Lexer/Parser? (y/n)");
			String input = reader.next();
			if(input.equals("y")){
				reader.close();
				return true;
			}
			if(input.equals("n")){
				reader.close();
				return false;
			}
		}
	}
	
	/*------------------------|
	 *                        |
	 * Test File Load Methods |
	 *                        |
	 ------------------------*/
	public static String scanFileReturnCharString(File testFile) {
		String charString = "";
		try {
			Scanner sc = new Scanner(testFile);
			
			String line = "";
			while (sc.hasNextLine()) {
	            line = sc.nextLine();
	            line = line.trim();
	            line = line + "\n";                       //need for line number
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
			System.out.println("Choose 1 or 2: ");
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
}