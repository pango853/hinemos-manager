/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.bean.HttpScenarioCheckInfo;
import com.clustercontrol.http.bean.Page;
import com.clustercontrol.http.bean.Pattern;
import com.clustercontrol.http.bean.Variable;
import com.clustercontrol.http.model.MonitorHttpScenarioInfoEntity;
import com.clustercontrol.http.model.MonitorHttpScenarioPageInfoEntity;
import com.clustercontrol.http.model.MonitorHttpScenarioPatternInfoEntity;
import com.clustercontrol.http.model.MonitorHttpScenarioVariableInfoEntity;
import com.clustercontrol.monitor.run.model.MonitorInfoEntity;

/**
 * HTTP監視(シナリオ)判定情報を管理するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ControlHttpScenarioInfo {

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視ID */
	private String m_monitorId;

	/**
	 * コンストラクタ
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視対象ID
	 * @version 5.0.0
	 * @since 5.0.0
	 */
	public ControlHttpScenarioInfo(String monitorId, String monitorTypeId) {
		m_monitorId = monitorId;
		m_monitorTypeId = monitorTypeId;
	}

	/**
	 * HTTP監視(シナリオ)情報を取得します。<BR>
	 * 
	 * @return HTTP監視(シナリオ)情報
	 * @throws MonitorNotFound
	 * @version 5.0.0
	 * @since 5.0.0
	 */
	public HttpScenarioCheckInfo get() throws MonitorNotFound {
		// HTTP監視情報を取得
		MonitorHttpScenarioInfoEntity entity = QueryUtil.getMonitorHttpScenarioInfoPK(m_monitorId);

		HttpScenarioCheckInfo http = new HttpScenarioCheckInfo();
		http.setMonitorId(m_monitorId);
		http.setMonitorTypeId(m_monitorTypeId);
		http.setAuthType(entity.getAuthType());
		http.setAuthUser(entity.getAuthUser());
		http.setAuthPassword(entity.getAuthPassword());
		http.setProxyFlg(ValidConstant.typeToBoolean(entity.getProxyFlg()));
		http.setProxyUrl(entity.getProxyUrl());
		http.setProxyPort(entity.getProxyPort());
		http.setProxyUser(entity.getProxyUser());
		http.setProxyPassword(entity.getProxyPassword());
		http.setMonitoringPerPageFlg(ValidConstant.typeToBoolean(entity.getMonitoringPerPageFlg()));
		http.setUserAgent(entity.getUserAgent());
		http.setConnectTimeout(entity.getConnectTimeout());
		http.setRequestTimeout(entity.getRequestTimeout());

		Page[] pages = new Page[entity.getMonitorHttpScenarioPageInfoEntities().size()];
		for (MonitorHttpScenarioPageInfoEntity pageEntity: entity.getMonitorHttpScenarioPageInfoEntities()) {
			Integer order = pageEntity.getId().getPageOrderNo().intValue();

			Page page = new Page();
			page.setUrl(pageEntity.getUrl());
			page.setDescription(pageEntity.getDescription());
			page.setStatusCode(pageEntity.getStatusCode());
			page.setPost(pageEntity.getPost());
			page.setPriority(pageEntity.getPriority());
			page.setMessage(pageEntity.getMessage());

			com.clustercontrol.http.bean.Pattern[] patterns = new com.clustercontrol.http.bean.Pattern[pageEntity.getMonitorHttpScenarioPatternInfoEntities().size()];
			for (MonitorHttpScenarioPatternInfoEntity patternEntity: pageEntity.getMonitorHttpScenarioPatternInfoEntities()) {
				com.clustercontrol.http.bean.Pattern p = new com.clustercontrol.http.bean.Pattern();
				p.setPattern(patternEntity.getPattern());
				p.setDescription(patternEntity.getDescription());
				p.setCaseSensitivityFlg(ValidConstant.typeToBoolean(patternEntity.getCaseSensitivityFlg()));
				p.setProcessType(patternEntity.getProcessType());
				p.setValidFlg(ValidConstant.typeToBoolean(patternEntity.getValidFlg()));
				patterns[patternEntity.getId().getPatternOrderNo()] = p;
			}
			page.setPatterns(Arrays.asList(patterns));

			List<com.clustercontrol.http.bean.Variable> variables = new ArrayList<com.clustercontrol.http.bean.Variable>();
			for (MonitorHttpScenarioVariableInfoEntity variableEntity: pageEntity.getMonitorHttpScenarioVariableInfoEntities()) {
				com.clustercontrol.http.bean.Variable v = new com.clustercontrol.http.bean.Variable();
				v.setName(variableEntity.getId().getName());
				v.setMatchingWithResponseFlg(ValidConstant.typeToBoolean(variableEntity.getMatchingWithResponseFlg()));
				v.setValue(variableEntity.getValue());
				variables.add(v);
			}
			page.setVariables(variables);

			pages[order] = page;
		}
		http.setPages(Arrays.asList(pages));

		return http;
	}

	/**
	 * HTTP監視(シナリオ)情報を追加します。<BR>
	 * 
	 * @param http HTTP監視(シナリオ)情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean add(HttpScenarioCheckInfo http) throws MonitorNotFound, InvalidRole {

		MonitorInfoEntity monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// HTTP監視情報を追加
		MonitorHttpScenarioInfoEntity entity = new MonitorHttpScenarioInfoEntity(monitorEntity);
		entity.setAuthType(http.getAuthType());
		entity.setAuthUser(http.getAuthUser());
		entity.setAuthPassword(http.getAuthPassword());
		entity.setProxyFlg(ValidConstant.booleanToType(http.getProxyFlg()));
		entity.setProxyUrl(http.getProxyUrl());
		entity.setProxyPort(http.getProxyPort());
		entity.setProxyUser(http.getProxyUser());
		entity.setProxyPassword(http.getProxyPassword());
		entity.setMonitoringPerPageFlg(ValidConstant.booleanToType(http.getMonitoringPerPageFlg()));
		entity.setUserAgent(http.getUserAgent());
		entity.setConnectTimeout(http.getConnectTimeout());
		entity.setRequestTimeout(http.getRequestTimeout());

		int pageOrderNo = 0;
		List<MonitorHttpScenarioPageInfoEntity> pageEntities = new ArrayList<MonitorHttpScenarioPageInfoEntity>();
		for (com.clustercontrol.http.bean.Page page: http.getPages()) {
			MonitorHttpScenarioPageInfoEntity pageEntity = new MonitorHttpScenarioPageInfoEntity(entity, pageOrderNo);
			pageEntity.setUrl(page.getUrl());
			pageEntity.setDescription(page.getDescription());
			pageEntity.setStatusCode(page.getStatusCode());
			pageEntity.setPost(page.getPost());
			pageEntity.setPriority(page.getPriority());
			pageEntity.setMessage(page.getMessage());

			int patternOrderNo = 0;
			List<MonitorHttpScenarioPatternInfoEntity> patternEntities = new ArrayList<MonitorHttpScenarioPatternInfoEntity>();
			for (com.clustercontrol.http.bean.Pattern p: page.getPatterns()) {
				MonitorHttpScenarioPatternInfoEntity patternEntity = new MonitorHttpScenarioPatternInfoEntity(pageEntity, patternOrderNo);
				patternEntity.setPattern(p.getPattern());
				patternEntity.setDescription(p.getDescription());
				patternEntity.setCaseSensitivityFlg(ValidConstant.booleanToType(p.getCaseSensitivityFlg()));
				patternEntity.setProcessType(p.getProcessType());
				patternEntity.setValidFlg(ValidConstant.booleanToType(p.getValidFlg()));
				patternEntities.add(patternEntity);

				patternOrderNo++;
			}
			pageEntity.setMonitorHttpScenarioPatternInfoEntities(patternEntities);

			List<MonitorHttpScenarioVariableInfoEntity> variableEntities = new ArrayList<MonitorHttpScenarioVariableInfoEntity>();
			for (Variable v: page.getVariables()) {
				MonitorHttpScenarioVariableInfoEntity variableEntity = new MonitorHttpScenarioVariableInfoEntity(pageEntity, v.getName());
				variableEntity.setMatchingWithResponseFlg(ValidConstant.booleanToType(v.getMatchingWithResponseFlg()));
				variableEntity.setValue(v.getValue());
				variableEntities.add(variableEntity);
			}
			pageEntity.setMonitorHttpScenarioVariableInfoEntities(variableEntities);

			pageEntities.add(pageEntity);

			pageOrderNo++;
		}
		entity.setMonitorHttpScenarioPageInfoEntities(pageEntities);

		return true;
	}

	/**
	 * HTTP監視(シナリオ)情報を変更します。<BR>
	 * 
	 * @param http HTTP監視(シナリオ)情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(HttpScenarioCheckInfo http) throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		MonitorInfoEntity monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

		// HTTP監視(シナリオ)情報を取得
		MonitorHttpScenarioInfoEntity entity = QueryUtil.getMonitorHttpScenarioInfoPK(m_monitorId);

		// HTTP監視(シナリオ)情報を設定
		entity.setAuthType(http.getAuthType());
		entity.setAuthUser(http.getAuthUser());
		entity.setAuthPassword(http.getAuthPassword());
		entity.setProxyFlg(ValidConstant.booleanToType(http.getProxyFlg()));
		entity.setProxyUrl(http.getProxyUrl());
		entity.setProxyPort(http.getProxyPort());
		entity.setProxyUser(http.getProxyUser());
		entity.setProxyPassword(http.getProxyPassword());
		entity.setMonitoringPerPageFlg(ValidConstant.booleanToType(http.getMonitoringPerPageFlg()));
		entity.setUserAgent(http.getUserAgent());
		entity.setConnectTimeout(http.getConnectTimeout());
		entity.setRequestTimeout(http.getRequestTimeout());
		monitorEntity.setMonitorHttpScenarioInfoEntity(entity);

		List<Page> pList = new ArrayList<Page>(http.getPages());
		Iterator<Page> piter = pList.iterator();
		List<MonitorHttpScenarioPageInfoEntity> peList = new ArrayList<MonitorHttpScenarioPageInfoEntity>(entity.getMonitorHttpScenarioPageInfoEntities());

		int pageOrderNo = 0;
		while (piter.hasNext()) {
			Page p = piter.next();

			Iterator<MonitorHttpScenarioPageInfoEntity> peiter = peList.iterator();
			while (peiter.hasNext()) {
				MonitorHttpScenarioPageInfoEntity pe = peiter.next();
				if (pageOrderNo == pe.getId().getPageOrderNo()) {
					pe.setUrl(p.getUrl());
					pe.setDescription(p.getDescription());
					pe.setStatusCode(p.getStatusCode());
					pe.setPost(p.getPost());
					pe.setPriority(p.getPriority());
					pe.setMessage(p.getMessage());

					List<Pattern> ptList = new ArrayList<Pattern>(p.getPatterns());
					Iterator<Pattern> ptiter = ptList.iterator();
					List<MonitorHttpScenarioPatternInfoEntity> pteList = new ArrayList<MonitorHttpScenarioPatternInfoEntity>(pe.getMonitorHttpScenarioPatternInfoEntities());

					int patternOrderNo = 0;
					while (ptiter.hasNext()) {
						Pattern pt = ptiter.next();

						Iterator<MonitorHttpScenarioPatternInfoEntity> pteiter = pteList.iterator();
						while (pteiter.hasNext()) {
							MonitorHttpScenarioPatternInfoEntity pte = pteiter.next();
							if (patternOrderNo == pte.getId().getPatternOrderNo()) {
								pte.setPattern(pt.getPattern());
								pte.setDescription(pt.getDescription());
								pte.setCaseSensitivityFlg(ValidConstant.booleanToType(pt.getCaseSensitivityFlg()));
								pte.setProcessType(pt.getProcessType());
								pte.setValidFlg(ValidConstant.booleanToType(pt.getValidFlg()));

								pteiter.remove();
								ptiter.remove();
								break;
							}
						}
						patternOrderNo++;
					}

					for (Pattern pt: ptList) {
						MonitorHttpScenarioPatternInfoEntity pte = new MonitorHttpScenarioPatternInfoEntity(pe, p.getPatterns().indexOf(pt));
						pte.setPattern(pt.getPattern());
						pte.setDescription(pt.getDescription());
						pte.setCaseSensitivityFlg(ValidConstant.booleanToType(pt.getCaseSensitivityFlg()));
						pte.setProcessType(pt.getProcessType());
						pte.setValidFlg(ValidConstant.booleanToType(pt.getValidFlg()));
					}

					for (MonitorHttpScenarioPatternInfoEntity pte: pteList) {
						pe.getMonitorHttpScenarioPatternInfoEntities().remove(pte);
						em.remove(pte);
					}

					List<Variable> vList = new ArrayList<Variable>(p.getVariables());
					Iterator<Variable> viter = vList.iterator();
					List<MonitorHttpScenarioVariableInfoEntity> veList = new ArrayList<MonitorHttpScenarioVariableInfoEntity>(pe.getMonitorHttpScenarioVariableInfoEntities());

					while (viter.hasNext()) {
						Variable v = viter.next();

						Iterator<MonitorHttpScenarioVariableInfoEntity> veiter = veList.iterator();
						while (veiter.hasNext()) {
							MonitorHttpScenarioVariableInfoEntity ve = veiter.next();
							if (v.getName().equals(ve.getId().getName())) {
								ve.setMatchingWithResponseFlg(ValidConstant.booleanToType(v.getMatchingWithResponseFlg()));
								ve.setValue(v.getValue());

								veiter.remove();
								viter.remove();
								break;
							}
						}
					}

					for (Variable v: vList) {
						MonitorHttpScenarioVariableInfoEntity ve = new MonitorHttpScenarioVariableInfoEntity(pe, v.getName());
						ve.setMatchingWithResponseFlg(ValidConstant.booleanToType(v.getMatchingWithResponseFlg()));
						ve.setValue(v.getValue());
					}

					for (MonitorHttpScenarioVariableInfoEntity ve: veList) {
						pe.getMonitorHttpScenarioVariableInfoEntities().remove(ve);
						em.remove(ve);
					}

					peiter.remove();
					piter.remove();

					break;
				}
			}
			pageOrderNo++;
		}

		for (Page p: pList) {
			MonitorHttpScenarioPageInfoEntity pe = new MonitorHttpScenarioPageInfoEntity(entity, http.getPages().indexOf(p));
			pe.setUrl(p.getUrl());
			pe.setDescription(p.getDescription());
			pe.setStatusCode(p.getStatusCode());
			pe.setPost(p.getPost());
			pe.setPriority(p.getPriority());
			pe.setMessage(p.getMessage());

			for (Pattern pt: p.getPatterns()) {
				MonitorHttpScenarioPatternInfoEntity pte = new MonitorHttpScenarioPatternInfoEntity(pe, p.getPatterns().indexOf(pt));
				pte.setPattern(pt.getPattern());
				pte.setDescription(pt.getDescription());
				pte.setCaseSensitivityFlg(ValidConstant.booleanToType(pt.getCaseSensitivityFlg()));
				pte.setProcessType(pt.getProcessType());
				pte.setValidFlg(ValidConstant.booleanToType(pt.getValidFlg()));
			}

			for (Variable v: p.getVariables()) {
				MonitorHttpScenarioVariableInfoEntity ve = new MonitorHttpScenarioVariableInfoEntity(pe, v.getName());
				ve.setMatchingWithResponseFlg(ValidConstant.booleanToType(v.getMatchingWithResponseFlg()));
				ve.setValue(v.getValue());
			}
		}

		for (MonitorHttpScenarioPageInfoEntity pe: peList) {
			entity.getMonitorHttpScenarioPageInfoEntities().remove(pe);
			em.remove(pe);
		}

		return true;
	}
}
