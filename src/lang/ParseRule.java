package lang;

public abstract class ParseRule<Pctx> {
	public abstract void parse(Pctx pcx) throws FatalErrorException;
}
