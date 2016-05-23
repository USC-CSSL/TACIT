package edu.usc.cssl.tacit.crawlers.wikipedia.services;
/**
* This class is for writing current information to 
* a json format file. It controls the spacing and
* format. 
*
* @author  Yongshun
* @version 1.0
* @since   2016-05-01 
*/

import java.io.StringWriter;

public class JsonWriter extends StringWriter{
	final static String indentstring = "  "; //define as you wish
    final static String spaceaftercolon = " "; //use "" if you don't want space after colon

    private int indentlevel = 0;

    @Override
    public void write(int c)
    {
            char ch = (char) c;
            if (ch == '[' || ch == '{')
            {
                    super.write(c);
                    super.write('\n');
                    indentlevel++;
                    writeIndentation();
            } else if (ch == ',')
            {
                    super.write(c);
                    super.write('\n');
                    writeIndentation();
            } else if (ch == ']' || ch == '}')
            {
                    super.write('\n');
                    indentlevel--;
                    writeIndentation();
                    super.write(c);
            } else if (ch == ':')
            {
                    super.write(c);
                    super.write(spaceaftercolon);
            } else
            {
                    super.write(c);
            }

    }
    
    public void writeString(String str)
    {
    	for(int i=0;i<str.length();i++)
    		write((int)str.charAt(i));
    }

    private void writeIndentation()
    {
            for (int i = 0; i < indentlevel; i++)
            {
                    super.write(indentstring);
            }
    }
}

