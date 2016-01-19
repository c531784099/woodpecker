package cn.bidlink.framework.search.data.creditScore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import cn.bidlink.framework.search.engine.OperationType;
import cn.bidlink.search.datasource.DataConverter;

/**
 * version=0.0.1，companyId=123456，score=，updateTime=
 * */
public class CreditScoreMessageConverter implements DataConverter<Message, CreditScore>{

	protected final Logger logger = Logger.getLogger(CreditScoreMessageConverter.class);
	
	@Override
	public CreditScore convert(Message data, Class<CreditScore> clazz) {
		CreditScore obj = null;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		String dataStr = null;
		try {
			dataStr = ((TextMessage)data).getText();
			obj.setBusinessId(CreditScoreUtils.getBusinessId(dataStr));
			obj.setCreateTime(CreditScoreUtils.getUpdateTime(dataStr));
			obj.setOperationType(OperationType.UPDATE.ordinal());
			obj.setCreditScoreMessage(dataStr);
		} catch (Exception e) {
			logger.error("类型抓换错误["+dataStr+"]");
			return null;
		}
		return obj;
	}
	
	public static void main(String[] args) {
		
	}
}
