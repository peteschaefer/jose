package de.jose.db.query;



/**
 * this TokenManager is built upon the default token manager
 * it accepts token in different languages
 *
 * @deprecated
 */

public class LanguageTokenManager
	//	extends QueryTokenManager
{

	/**	maps translated strings to token types	 *
	protected HashMap tokenMap;
			  
	public LanguageTokenManager (ASCII_CharStream stream)
	{
		super(stream);
		tokenMap = new HashMap();
		
		for (int kind = 0; kind < tokenImage.length; kind++)
		{
			String img = tokenImage[kind];
			if (!img.startsWith("\"") || !img.endsWith("\"")) continue;
			img = img.substring(1,img.length()-1);
			
			String translated = Language.get("token."+img.toLowerCase(), null);
			if (translated != null)
				tokenMap.put(translated.toLowerCase(), new Integer(kind));
		}
	}
	
	public LanguageTokenManager (Reader dstream)
	{
		this(new ASCII_CharStream(dstream, 1, 1, 4096));
	}
		
	public LanguageTokenManager (String dstream)
	{
		this(new StringReader(dstream));
	}

	public Token getNextToken()
	{
		Token token = super.getNextToken();
		
		if (token.kind == VALUE) {
			//	might be translatable
			Integer kind = (Integer)tokenMap.get(token.image.toLowerCase());
			if (kind != null)
			{
				token.kind = kind.intValue();
				token.image = tokenImage[token.kind];
			}
		}
		
		return token;
	}

	*/
}
