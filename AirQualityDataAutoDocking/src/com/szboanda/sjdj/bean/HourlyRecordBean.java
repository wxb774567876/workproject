package com.szboanda.sjdj.bean;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;

import com.szboanda.platform.util.BeanUtils;
import com.szboanda.sjdj.utils.CommonUtils;

public class HourlyRecordBean {
	/**序号：XH */
	private String xh;
	
	/**监测编号：JCBH */
	private String monitorNum;
	
	/**监测点位名称：JCDWMC */
	private String pointName;
	
	/**监测点位编号：JCDBH */
	private String pointCode;
	
	/**二氧化硫浓度：SO2ND */
	private Double so2Nd;
	
	/**二氧化硫分指数：SO2FZS */
	private Double so2Fzs;
	
	/**二氧化氮浓度：NO2ND */
	private Double no2Nd;
	
	/**二氧化氮分指数：NO2FZS */
	private Double no2Fzs;
	
	/**颗粒物粒径小于10小时平均浓度：KLWXY10XSPJND*/
	private Double pm10Xspjnd;
	
	/**颗粒物粒径小于10滑动平均浓度：KLWXY10HDPJND */
	private Double pm10Hdpjnd;
	
	/**颗粒物粒径小于10滑动平均分指数：KLWXY10HDPJFZS */
	private Double pm10Hdpjzs;
	
	/**一氧化碳浓度：COND */
	private Double coNd;
	
	/**一氧化碳分指数：COFZS */
	private Double coFzs;
	
	/**臭氧浓度：O3XSND */
	private Double o3Nd;
	
	/**臭氧分指数：O3XSFZS */
	private Double o3Fzs;
	
	/**臭氧八小时滑动平均： O3BXSHDPJ */
	private Double o3bHdnd;
	
	/**臭氧八小时滑动分指数：O3BXSHDFZS */
	private Double o3bHdzs;
	
	/**颗粒物小于2.5浓度：KLWXY25ND */
	private Double pm2_5Nd;
	
	/**颗粒物小于2.5滑动浓度：KLWXY25HDND */
	private Double pm2_5Hdnd;
	
	/**颗粒物小于2.5滑动分指数：KLWXY25HDFZS */
	private Double pm2_5hdFzs;
	
	/**空气质量指数：AQI */
	private String aqi;
	
	/**首要污染物：SYWRW */
	private String primaryPollutant;
	
	/**空气质量指数级别：KQZLZSJB */
	private String airQualityIndexLevel;
	
	/**空气质量类别：KQZLLB */
	private String airQualityType;
	
	/**空气质量颜色：KQZLYS */
	private String airQualityColor;
	
	/**监测时间：JCSJ */
	private String monitorTime;
	
	/**执行标准编号：ZXBZBH */
	private String executeStandardNum;
	
	/**排序号：PXH */
	private Integer pxh;
	
	/**组织机构编号：ORGID */
	private String orgid;

	public String getXh() {
		return xh;
	}

	public void setXh(String xh) {
		this.xh = xh;
	}

	public String getMonitorNum() {
		return monitorNum;
	}

	public void setMonitorNum(String monitorNum) {
		this.monitorNum = monitorNum;
	}

	public String getPointName() {
		return pointName;
	}

	public void setPointName(String pointName) {
		this.pointName = pointName;
	}

	public String getPointCode() {
		return pointCode;
	}

	public void setPointCode(String pointCode) {
		this.pointCode = pointCode;
	}

	public Double getSo2Nd() {
		return so2Nd;
	}

	public void setSo2Nd(Double so2Nd) {
		this.so2Nd = so2Nd;
	}

	public Double getSo2Fzs() {
		return so2Fzs;
	}

	public void setSo2Fzs(Double so2Fzs) {
		this.so2Fzs = so2Fzs;
	}

	public Double getNo2Nd() {
		return no2Nd;
	}

	public void setNo2Nd(Double no2Nd) {
		this.no2Nd = no2Nd;
	}

	public Double getNo2Fzs() {
		return no2Fzs;
	}

	public void setNo2Fzs(Double no2Fzs) {
		this.no2Fzs = no2Fzs;
	}

	public Double getPm10Xspjnd() {
		return pm10Xspjnd;
	}

	public void setPm10Xspjnd(Double pm10Xspjnd) {
		this.pm10Xspjnd = pm10Xspjnd;
	}

	public Double getPm10Hdpjnd() {
		return pm10Hdpjnd;
	}

	public void setPm10Hdpjnd(Double pm10Hdpjnd) {
		this.pm10Hdpjnd = pm10Hdpjnd;
	}

	public Double getPm10Hdpjzs() {
		return pm10Hdpjzs;
	}

	public void setPm10Hdpjzs(Double pm10Hdpjzs) {
		this.pm10Hdpjzs = pm10Hdpjzs;
	}

	public Double getCoNd() {
		return coNd;
	}

	public void setCoNd(Double coNd) {
		this.coNd = coNd;
	}

	public Double getCoFzs() {
		return coFzs;
	}

	public void setCoFzs(Double coFzs) {
		this.coFzs = coFzs;
	}

	public Double getO3Nd() {
		return o3Nd;
	}

	public void setO3Nd(Double o3Nd) {
		this.o3Nd = o3Nd;
	}

	public Double getO3Fzs() {
		return o3Fzs;
	}

	public void setO3Fzs(Double o3Fzs) {
		this.o3Fzs = o3Fzs;
	}

	public Double getO3bHdnd() {
		return o3bHdnd;
	}

	public void setO3bHdnd(Double o3bHdnd) {
		this.o3bHdnd = o3bHdnd;
	}

	public Double getO3bHdzs() {
		return o3bHdzs;
	}

	public void setO3bHdzs(Double o3bHdzs) {
		this.o3bHdzs = o3bHdzs;
	}

	public Double getPm2_5Nd() {
		return pm2_5Nd;
	}

	public void setPm2_5Nd(Double pm2_5Nd) {
		this.pm2_5Nd = pm2_5Nd;
	}

	public Double getPm2_5Hdnd() {
		return pm2_5Hdnd;
	}

	public void setPm2_5Hdnd(Double pm2_5Hdnd) {
		this.pm2_5Hdnd = pm2_5Hdnd;
	}

	public Double getPm2_5hdFzs() {
		return pm2_5hdFzs;
	}

	public void setPm2_5hdFzs(Double pm2_5hdFzs) {
		this.pm2_5hdFzs = pm2_5hdFzs;
	}

	public String getAqi() {
		return aqi;
	}

	public void setAqi(String aqi) {
		this.aqi = aqi;
	}

	public String getPrimaryPollutant() {
		return primaryPollutant;
	}

	public void setPrimaryPollutant(String primaryPollutant) {
		this.primaryPollutant = primaryPollutant;
	}

	public String getAirQualityIndexLevel() {
		return airQualityIndexLevel;
	}

	public void setAirQualityIndexLevel(String airQualityIndexLevel) {
		this.airQualityIndexLevel = airQualityIndexLevel;
	}

	public String getAirQualityType() {
		return airQualityType;
	}

	public void setAirQualityType(String airQualityType) {
		this.airQualityType = airQualityType;
	}

	public String getAirQualityColor() {
		return airQualityColor;
	}

	public void setAirQualityColor(String airQualityColor) {
		this.airQualityColor = airQualityColor;
	}

	public String getMonitorTime() {
		return monitorTime;
	}

	public void setMonitorTime(String monitorTime) {
		this.monitorTime = monitorTime;
	}

	public String getExecuteStandardNum() {
		return executeStandardNum;
	}

	public void setExecuteStandardNum(String executeStandardNum) {
		this.executeStandardNum = executeStandardNum;
	}

	public Integer getPxh() {
		return pxh;
	}

	public void setPxh(Integer pxh) {
		this.pxh = pxh;
	}

	public String getOrgid() {
		return orgid;
	}

	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}
	/**
	*
	* 把HourlyDataBean实体类转化成Map对象
	* @param	hourlyDataBean
	*
	* @author	王贤炳
	* @version	2015-12-25
	* @since	V0.1.1
	*/
	public Map<String, Object> toMap(){
		Map<String,Object> map = new LinkedHashMap<String, Object>();
		map.put("XH", this.getXh());
		map.put("JCBH", this.getMonitorNum());
		map.put("JCDWMC", this.getPointName());
		map.put("JCDBH", this.getPointCode());
		map.put("SO2ND", this.getSo2Nd());
		map.put("SO2FZS", this.getSo2Fzs());
		map.put("NO2ND", this.getNo2Nd());
		map.put("NO2FZS", this.getNo2Fzs());
		map.put("KLWXY10XSPJND", this.getPm10Xspjnd());
		map.put("KLWXY10HDPJND", this.getPm10Hdpjnd());
		map.put("KLWXY10HDPJFZS", this.getPm10Hdpjzs());
		map.put("COND", this.getCoNd());
		map.put("COFZS", this.getCoFzs());
		map.put("O3XSND", this.getO3Nd());
		map.put("O3XSFZS", this.getO3Fzs());
		map.put("O3BXSHDPJ", this.getO3bHdnd());
		map.put("O3BXSHDFZS", this.getO3bHdzs());
		map.put("KLWXY25ND", this.getPm2_5Nd());
		map.put("KLWXY25HDND", this.getPm2_5Hdnd());
		map.put("KLWXY25HDFZS", this.getPm2_5hdFzs());
		map.put("AQI", this.getAqi());
		map.put("SYWRW", this.getPrimaryPollutant());
		map.put("KQZLZSJB", this.getAirQualityIndexLevel());
		map.put("KQZLLB", this.getAirQualityType());
		map.put("KQZLYS", this.getAirQualityColor());
		map.put("JCSJ", this.getMonitorTime());
		map.put("ZXBZBH", this.getExecuteStandardNum());
		map.put("PXH", this.getPxh());
		map.put("ORGID", this.getOrgid());
		return map;
	}
	
	/**
	*
	* 把DynaBean转化成HourlyDataBean
	* @param	bean
	*
	* @author	王贤炳
	* @version	2015-12-25
	* @since	V0.1.1
	*/
	public HourlyRecordBean fromDynaBean(DynaBean bean){
		if(bean != null){
			this.setXh(BeanUtils.getString(bean, "XH"));
			this.setMonitorNum(BeanUtils.getString(bean, "JCBH"));
			this.setPointName(BeanUtils.getString(bean, "JCDWMC"));
			this.setPointCode(BeanUtils.getString(bean, "JCDBH"));
			this.setSo2Nd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "SO2ND"), false));
			this.setSo2Fzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "SO2FZS"), false));
			this.setNo2Nd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "NO2ND"), false));
			this.setNo2Fzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "NO2FZS"), false));
			this.setPm10Xspjnd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "KLWXY10XSPJND"), false));
			this.setPm10Hdpjnd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "KLWXY10HDPJND"), false));
			this.setPm10Hdpjzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "KLWXY10HDPJFZS"), false));
			this.setCoNd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "COND"), false));
			this.setCoFzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "COFZS"), false));
			this.setO3Nd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "O3XSND"), false));
			this.setO3Fzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "O3XSFZS"), false));
			this.setO3bHdnd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "O3BXSHDPJ"), false));
			this.setO3bHdzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "O3BXSHDFZS"), false));
			this.setPm2_5Nd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "KLWXY25ND"), false));
			this.setPm2_5Hdnd(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "KLWXY25HDND"), false));
			this.setPm2_5hdFzs(CommonUtils.getDoubleValue(BeanUtils.getString(bean, "KLWXY25HDFZS"), false));
			this.setAqi(BeanUtils.getString(bean, "AQI"));
			this.setPrimaryPollutant(BeanUtils.getString(bean, "SYWRW"));
			this.setAirQualityIndexLevel(BeanUtils.getString(bean, "KQZLZSJB"));
			this.setAirQualityType(BeanUtils.getString(bean, "KQZLLB"));
			this.setAirQualityColor(BeanUtils.getString(bean, "KQZLYS"));
			this.setMonitorTime(BeanUtils.getString(bean, "JCSJ"));
			this.setExecuteStandardNum(BeanUtils.getString(bean, "ZXBZBH"));
			this.setPxh(Integer.parseInt(BeanUtils.getString(bean, "PXH")));
			this.setOrgid(BeanUtils.getString(bean, "ORGID"));
		}
		return this;
	}
}