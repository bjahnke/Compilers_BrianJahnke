package pkg;
//The 'p' present in some enums stands for production, differentiates from TokenType enum of the same name
//just in case I happen to use them in the same file.
public enum ProdType {
	PROGRAM,
	BLOCK,
	STATEMENT_LIST,
	STATEMENT,
	PRINT_STATEMENT,
	ASSIGNMENT_STATEMENT,
	VAR_DECL,
	WHILE_STATEMENT,
	IF_STATEMENT,
	EXPR,
	INT_EXPR,
	STRING_EXPR,
	BOOLEAN_EXPR,
	IDp,
	CHAR_LIST,
	TYPEp,
	CHARp,
	SPACEp,
	DIGITp,
	BOOLOPp,
	BOOLVALp,
	INTOPp;
}
