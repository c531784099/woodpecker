package cn.bidlink.framework.search.data.creditScore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreditScoreUtils {
	
	public static final Map<String, String> formatData(String dataStr){
		System.out.println(dataStr);
		Map<String, String> params = new HashMap<String, String>();
		for(String str : dataStr.split(",")){
			params.put(str.split("=")[0], str.split("=")[1]);
		}
		return params;
	}
	
	public static final Date getUpdateTime(String dataStr){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        try {
			return simpleDateFormat.parse(formatData(dataStr).get("updateTime"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static final Long getBusinessId(String dataStr){
		return Long.parseLong(formatData(dataStr).get("companyId"));
	}
	public static final Long getCreditScore(String dataStr){
		return Long.parseLong(formatData(dataStr).get("score"));
	}
	public static final Long getRating(String dataStr){
		return Long.parseLong(formatData(dataStr).get("rating"));
	}

}
