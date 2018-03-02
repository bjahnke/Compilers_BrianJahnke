import java.io.File;
import java.util.List;

public class TempMain {
	public static void main(String[] args) {
		System.out.println("TempMain");
		File testFile = Lexer.getTestFile();
		String cString = Lexer.ScanFileReturnCharString(testFile);
		List<Lexer.Token> tList = Lexer.lex(cString);
		System.out.println("\nHave nice day.");
	}
	
}
