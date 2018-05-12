package com.code.services;

import com.code.dal.CustomSession;

public abstract class BaseService {
//	private static final int REFERENCE_YEAR = 2000;
//	private static GregorianCalendar referenceDate;
//	static{
//		referenceDate = new GregorianCalendar();
//		referenceDate.setLenient(false);
//		referenceDate.set(GregorianCalendar.YEAR, REFERENCE_YEAR);
//		referenceDate.set(GregorianCalendar.MONTH, 1);
//		referenceDate.set(GregorianCalendar.DATE, 1);
//		referenceDate.set(GregorianCalendar.HOUR, 0);
//		referenceDate.set(GregorianCalendar.MINUTE, 0);
//		referenceDate.set(GregorianCalendar.SECOND, 0);
//		referenceDate.set(GregorianCalendar.MILLISECOND, 1);
//	}
	
    protected BaseService() {
    }
    
	protected static boolean isSessionOpened(CustomSession[] sessions) {
		if (sessions != null && sessions.length > 0)
			return true;

		return false;
	}
	
//	/**
//	 * Generate pass code number
//	 * 
//	 * @return pass code number
//	 * @throws BusinessException
//	 */
//	public static synchronized String getPassCode() throws BusinessException {
//		try {
//			Thread.sleep(10);
//			Long longNumber = (System.currentTimeMillis() - referenceDate.getTimeInMillis()) / 10;
//			return Long.toString(longNumber, 36).toUpperCase();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			throw new BusinessException("error_general");
//		}
//	}
}
