package pkg;

import static pkg.TokenType.EOP;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class TempMain {
	public static boolean isVerboseOnLP;
	public static boolean verboseLexer = true;
	public static boolean verboseParser = true;
	public static boolean verboseSemantic = true;
	public static boolean verboseCodeGen = true;
	public static int progCount = 1;
	
	public static void main(String[] args) {
		if(args.length > 1){
			if(args[1].equals("0")){
				verboseLexer = false;
			}
			if(args.length > 2){
				if(args[2].equals("0")){
					verboseParser = false;
				}
				if(args.length > 3){
					if(args[3].equals("0")){
						verboseSemantic = false;
					}
					if(args.length > 4){
						if(args[4].equals("0")){
							verboseCodeGen = false;
						}
					}
				}
			}
		}
		//String filePath = args[0];
		File testFile = getTestFile();
		//File testFile = new File(filePath);
		//System.out.println("TempMain");
		isVerboseOnLP = toggleVerbose();
		String cString = scanFileReturnCharString(testFile);
		List<String> progStrings = seperatePrograms(cString);
		//Compilation Start
		compileProgram(progStrings);
		System.out.println("\nHave nice day.");
	}
	
	public static void compileProgram(List<String> pStrList){
		List<Token> progTokList;
		SyntaxTree validParse;
		for(String pString : pStrList){
		//Lexer
			System.out.println("Lexing Program " + progCount);
			progTokList = Lexer.lex(pString);
			
			if(progTokList != null){
		//Parser
				System.out.println("Lex completed successfully\n\nParsing Program " + progCount);
				validParse = Parser.parse(progTokList);
				if(validParse != null){
		//Semantic Analysis
					SymbolTable symbolTable = runSymbolTable(validParse);
					if(symbolTable != null){
		//Code Generation
						CodeGen cG = runCodeGen(symbolTable);
						if(cG != null){
							System.out.println("Program " + progCount + " compiled successfully.");
						}
					}
				}
			}
			progCount++;
		}
	}
	public static List<String> seperatePrograms(String codeString){
		List<String> cStrList = new ArrayList<String>();
		String progString = "";
		for(int i = 0; i < codeString.length(); i++){
			progString += codeString.charAt(i);
			if(codeString.charAt(i) == '$'){
				cStrList.add(progString);
				progString = "";
			}
			else if(i == codeString.length()-1  && progString.trim().length() > 0) {
				System.out.println("Warning: $ at file end not present. Added Automatically to prevent failure.");
				progString += "$";
				cStrList.add(progString);
			}
		}
		return cStrList;
	}
	
	public static SymbolTable runSymbolTable(SyntaxTree pList){
		List<Var> list = new ArrayList<Var>();
		SymbolTable sT = new SymbolTable(list, pList);
		if(sT.buildSymbolTable(sT.ast.root)){
			SymbolTable.printSymbolTable();
			sT.symbolTableWarnings();
			return sT;
		}
		else{
			System.out.println("Program " + progCount + " Symbol Table:"
					+ "\nNot produced due to an error in semantic analysis.\n");
			return null;
		}
	}
	
	public static CodeGen runCodeGen(SymbolTable sT){
		CodeGen cGen = new CodeGen(sT);
		cGen.startCodeGen();
		cGen.printRunEnv();
		return null;
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
