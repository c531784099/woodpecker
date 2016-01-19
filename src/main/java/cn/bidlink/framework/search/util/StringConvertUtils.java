package cn.bidlink.framework.search.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import cn.bidlink.framework.search.Constants;
import cn.bidlink.framework.util.holder.PropertiesHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;

public abstract class StringConvertUtils {

    public static String leachHTML(String origin) {
        StringBuilder out = new StringBuilder();
        StringReader strReader = new StringReader(origin);
        try {
          HTMLStripCharFilter html = new HTMLStripCharFilter(strReader.markSupported() ? strReader : new BufferedReader(strReader));
          char[] cbuf = new char[1024 * 10];
          while (true) {
            int count = html.read(cbuf, 0 , 1024 * 10);
            if (count == -1)
              break; // end of stream mark is -1
            if (count > 0)
              out.append(cbuf, 0, count);
          }
          html.close();
        } catch (IOException e) {
        	return null;
        }
        return out.toString();
        //return origin.replaceAll("</?[\\w\"'= ]*/?>", "");
    }

    public static String textAbstract(String origin) {
        String lengthStr = PropertiesHolder.getProperty(Constants.CONVERT_TEXT_LENGTH);
        int length = NumberUtils.toInt(lengthStr, Constants.CONVERT_TEXT_LENGTH_DEFAULT);
        origin = textAbstract(origin, length);
        return origin;
    }

    public static String textAbstract(String origin, int length) {
        origin = StringUtils.substring(origin, NumberUtils.INTEGER_ZERO, length);
        return origin;
    }

    public static void main(String... strings) {
        String test = "<html><head>head</head><body>bodymain<table><tbody><tr><tb>tbtbtbt<br/>12312312</tb></tr></tbody></table></body></html>";

//		test = test.replaceAll("</?[\\w\"'= ]*/?>", ""); 
//		test = test.replaceAll("</?[\\w\"'= ]*/>", "");
        test = textAbstract(test);
    }
}
