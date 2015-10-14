package lang.c.parse;

import java.io.PrintStream;
import javax.management.openmbean.CompositeType;

import lang.*;
import lang.c.*;

public class Factor extends CParseRule {
	// factor ::= number
	private CParseRule number;
	public Factor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAMP.isFirst(tk); 
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		
		// ここにやってくるときは、必ずisFirst()が満たされている
		if(Number.isFirst(tk)) {// numberなんかfactorampなのか判断する
			number = new Number(pcx);
			number.parse(pcx);
		}
		else {
			number = new FactorAMP(pcx);
			number.parse(pcx);
		}
		
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(number.getCType());		// number の型をそのままコピー
			setConstant(number.isConstant());	// number は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (number != null) { number.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}

class FactorAMP extends CParseRule {
	// factor ::= number
	private CParseRule number;
	public FactorAMP(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AND; 
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		
		number = new Number(pcx);
		number.parse(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(CType.getCType(CType.T_pint));		// 型は*int型
			setConstant(true);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factorAMP starts");
		if (number != null) { number.codeGen(pcx); }
		o.println(";;; factorAMP completes");
	}
}