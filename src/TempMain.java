import java.io.File;
import java.util.List;

public class TempMain {
	public static void main(String[] args) {
		System.out.println("TempMain");
		File testFile = Lexer.getTestFile();
		String cString = Lexer.ScanFileReturnCharString(testFile);
		List<Lexer.Token> tList = Lexer.lex(cString);
		//send a list of valid trees here then call semantic analysis class?
		//Might send to analysis after ast conversion or just have ast there to keep it all in one place. Dunno
		System.out.println("\nHave nice day.");
	}
	
	
}
