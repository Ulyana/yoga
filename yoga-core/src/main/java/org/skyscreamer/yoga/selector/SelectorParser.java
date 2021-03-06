package org.skyscreamer.yoga.selector;

public class SelectorParser
{
    public static final String HREF = "href";
    public static final String DEFINITION = "definition";

    public static Selector parseSelector( String selectorStr )
    {
        Selector selector = new CoreSelector();
        if ( selectorStr != null )
        {
            try
            {
                selector = new CombinedSelector( selector, parse( selectorStr ) );
            }
            catch ( ParseSelectorException e )
            {
                // TODO: Add logging here.
                throw new IllegalArgumentException( "Could not parse selector", e );
            }
        }
        return selector;
    }

    public static DefinedSelectorImpl parse( String selectorExpression ) throws ParseSelectorException
    {
       DefinedSelectorImpl selector = new DefinedSelectorImpl();
        if ( selectorExpression.equals( ":" ) )
        {
            return selector;
        }

        if ( !selectorExpression.startsWith( ":(" ) )
        {
            throw new ParseSelectorException( "Selector must start with ':('" );
        }

        StringBuilder selectorBuff = new StringBuilder( selectorExpression );
        int matchIndex = getMatchingParenthesesIndex( selectorBuff, 1 );

        selectorBuff.delete( matchIndex, selectorBuff.length() );
        selectorBuff.delete( 0, 2 );

        while ( selectorBuff.length() > 0 )
        {
            processNextSelectorField( selector, selectorBuff );
        }
        return selector;
    }

    private static int getMatchingParenthesesIndex( StringBuilder selector, int index ) throws ParseSelectorException
    {
        int parenthesesCount = 1;
        while ( parenthesesCount > 0 && index < selector.length() - 1 )
        {
            index++;
            if ( selector.charAt( index ) == '(' )
            {
                parenthesesCount++;
            }
            if ( selector.charAt( index ) == ')' )
            {
                parenthesesCount--;
            }
        }

        if ( parenthesesCount > 0 )
        {
            throw new ParseSelectorException( "More opening parentheses than closing parentheses" );
        }
        return index;
    }

    private static void processNextSelectorField( DefinedSelectorImpl selector, StringBuilder selectorBuff ) throws ParseSelectorException
    {
        int index = 0;
        boolean done = false;
        StringBuilder fieldNameBuilder = new StringBuilder();
        DefinedSelectorImpl subSelector = new DefinedSelectorImpl();

        while ( !done )
        {
            if ( selectorBuff.charAt( index ) == ',' )
            {
                done = true;
            }
            else if ( selectorBuff.charAt( index ) == ':' )
            {
                done = true;
                int matchIndex = getMatchingParenthesesIndex( selectorBuff, index + 1 );
                subSelector = parse( selectorBuff.substring( index, matchIndex + 1 ) );

                if ( selectorBuff.length() > matchIndex + 1 && selectorBuff.charAt( matchIndex + 1 ) != ',' )
                {
                    throw new ParseSelectorException();  // TODO: Add informative message here
                }
                index = matchIndex + 1;
            }
            else
            {
                fieldNameBuilder.append( selectorBuff.charAt( index ) );
            }

            index++;
            if ( index == selectorBuff.length() )
            {
                done = true;
            }
        }

        selectorBuff.delete( 0, index );
        String fieldName = fieldNameBuilder.toString();
        if ( fieldName.equals( HREF ) )
        {
            throw new IllegalArgumentException( HREF + " is a reserved keyword for selectors" );
        }
        selector._fields.put( fieldName, subSelector);
    }
}

