package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.*;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	private int			lineNo, colNo;
	private char		backCh;
	private boolean		backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.setRule(rule);
		lineNo = 1; colNo = 1;
	}

	private CTokenRule						rule;
	public void setRule(CTokenRule rule)	{ this.rule = rule; }
	public CTokenRule getRule()				{ return rule; }

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
//		System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}
	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}
	// 新しく次のトークンを読み込んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
//		System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}
	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
			case 0:					// 初期状態
				ch = readChar();
				if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
				} else if (ch == (char) -1) {	// EOF
					startCol = colNo - 1;
					state = 1;
				} else if (ch >= '0' && ch <= '9') {	//数字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					if(ch == '0') {
						state = 14;
					}
					else {
						state = 3;
					}
					
				}
				else if(ch == '/') {	// /を呼んだ
					startCol = colNo - 1;
					state = 6;
				}
				else if (ch == '+') {	//+を呼んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 5;
				}
				else if(ch == '-') {
					startCol = colNo - 1;
					text.append(ch);
					state = 11;
				}
				else if(ch == '&') {
					startCol = colNo - 1;
					text.append(ch);
					state = 12;
				}
				else {			// ヘンな文字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 2;
				}
				break;
			case 1:					// EOFを読んだ
				tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
				accept = true;
				break;
			case 2:					// ヘンな文字を読んだ
				tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 3:					// 数（10進数）の開始
				ch = readChar();
				if (ch >= '0' && ch <= '9') {
					text.append(ch);
				} else {
					backChar(ch);
					state = 4;
				}
				break;
			case 4:	// 数の終わり		
				tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
				if(tk.getIntValue() > 65535 || tk.getIntValue() < 0) {
					state = 2;
				}
				else {
					accept = true;
				}
				break;
				
			case 5:					// +を読んだ
				tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
				accept = true;
				break;
			case 6:
				ch = readChar();
				if(ch == '/') {
					state = 7;
				}
				else if(ch == '*') {
					state = 8;
				}
				else {
					state = 10;
					backChar(ch);
					text.append(ch);
				}
				break;
			case 7:
				ch = readChar();
				if(ch == '\n') {	//文末
					state = 0;
				}
				break;
			case 8:
				ch = readChar();
				if(ch == '*') {
					state = 9;
				}
				break;
			case 9:
				ch = readChar();
				if(ch == '/') {
					state = 0;
				}
				else {
					backChar(ch);
					state = 8;
				}
				break;
			case 10:
				state = 2;
				break;
			case 11:
				tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
				accept = true;
				break;
			case 12:	//&を呼んだ
				tk = new CToken(CToken.TK_AND, lineNo, startCol, "&");
				accept = true;
				break;
			case 13:
				tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 14:
				ch = readChar();
				if(ch == 'x') {
					text.append(ch);
					state = 15;
				}
				else if(ch >= '0' && ch <= '7') {
					text.append(ch);
					state = 16;
				}
				else if(ch >= '8' || ch <= '9') {
					state = 2;
				}
				else{
					backChar(ch);
					state = 4;
				}
				break;
			case 15:
				ch = readChar();
				if( (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')) {
					text.append(ch);
				}
				else {
					backChar(ch);
					state = 4;
				}
				break;
			case 16:
				ch = readChar();
				if( ch >= '0' && ch <= '7') {
					text.append(ch);
				}
				else if(ch >= '8' || ch <= '9') {
					state = 2;
				}
				else {
					backChar(ch);
					state = 4;
				}
				break;
		
			}
		}
		return tk;
	}

	public void skipTo(CParseContext pctx, int t) {
		int i = getCurrentToken(pctx).getType();
		while (i != t && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
	public void skipTo(CParseContext pctx, int t1, int t2) {
		int i = getCurrentToken(pctx).getType();
		while (i != t1 && i != t2 && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
	public void skipTo(CParseContext pctx, int t1, int t2, int t3, int t4, int t5, int t6) {
		int i = getCurrentToken(pctx).getType();
		while (i != t1 && i != t2 && i != t3 && i != t4 && i != t5 && i != t6 && i != CToken.TK_EOF) {
			i = getNextToken(pctx).getType();
		}
		pctx.warning(getCurrentToken(pctx).toExplainString() + "まで読み飛ばしました");
	}
}
