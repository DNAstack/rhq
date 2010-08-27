/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.core.domain.criteria;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.rhq.core.domain.alert.Alert;
import org.rhq.core.domain.alert.AlertPriority;
import org.rhq.core.domain.util.PageOrdering;

/**
 * @author Joseph Marques
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("unused")
public class AlertCriteria extends Criteria {
    private static final long serialVersionUID = 1L;

    // sort fields from the Alert itself
    public static final String SORT_FIELD_CTIME = "ctime";

    // sort fields from the Alert's AlertDefinition
    public static final String SORT_FIELD_NAME = "name";
    public static final String SORT_FIELD_PRIORITY = "priority";
    public static final String SORT_FIELD_RESOURCE_ID = "resourceId";

    private Integer filterId;
    private String filterTriggeredOperationName; // requires overrides
    private Long filterStartTime; // requires overrides
    private Long filterEndTime; // requires overrides
    private String filterName; // requires overrides
    private String filterDescription; // requires overrides
    private AlertPriority filterPriority; // requires overrides
    private String filterResourceTypeId; // requires overrides
    private String filterResourceTypeName; // requires overrides
    private List<Integer> filterResourceIds; // requires overrides
    private List<Integer> filterResourceGroupIds; // requires overrides
    private List<Integer> filterAlertDefinitionIds; // requires overrides
    private List<Integer> filterGroupAlertDefinitionIds; // requires overrides

    private boolean fetchAlertDefinition;
    private boolean fetchConditionLogs;
    private boolean fetchNotificationLogs;
    private boolean fetchRecoveryAlertDefinition;

    private PageOrdering sortCtime;

    private PageOrdering sortName; // requires sort override
    private PageOrdering sortPriority; // requires sort override
    private PageOrdering sortResourceId; // requires sort override

    public AlertCriteria() {

        filterOverrides.put("triggeredOperationName", "triggeredOperation like ?");
        filterOverrides.put("startTime", "ctime >= ?");
        filterOverrides.put("endTime", "ctime <= ?");
        filterOverrides.put("name", "alertDefinition.name like ?");
        filterOverrides.put("description", "alertDefinition.description like ?");
        filterOverrides.put("priority", "alertDefinition.priority = ?");
        filterOverrides.put("resourceTypeId", "alertDefinition.resource.resourceType.id = ?");
        filterOverrides.put("resourceTypeName", "alertDefinition.resource.resourceType.name like ?");
        filterOverrides.put("resourceIds", "alertDefinition.resource.id IN ( ? )");
        filterOverrides.put("resourceGroupIds", "alertDefinition.resource.id IN " //
            + "( SELECT res.id " //
            + "    FROM ResourceGroup rg " //
            + "    JOIN rg.explicitResources res " //
            + "   WHERE rg.id = ? )");
        filterOverrides.put("alertDefinitionIds", "alertDefinition.id IN ( ? )");
        filterOverrides.put("groupAlertDefinitionIds", "alertDefinition.groupAlertDefinition.id IN ( ? )");

        sortOverrides.put(SORT_FIELD_NAME, "alertDefinition.name");
        sortOverrides.put(SORT_FIELD_PRIORITY, "alertDefinition.priority");
        sortOverrides.put(SORT_FIELD_RESOURCE_ID, "alertDefinition.resource.id");
    }

    @Override
    public Class<Alert> getPersistentClass() {
        return Alert.class;
    }

    public void addFilterId(Integer filterId) {
        this.filterId = filterId;
    }

    public void addFilterTriggeredOperationName(String filterTriggeredOperationName) {
        this.filterTriggeredOperationName = filterTriggeredOperationName;
    }

    public void addFilterStartTime(Long filterStartTime) {
        this.filterStartTime = filterStartTime;
    }

    public void addFilterEndTime(Long filterEndTime) {
        this.filterEndTime = filterEndTime;
    }

    public void addFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void addFilterDescription(String filterDescription) {
        this.filterDescription = filterDescription;
    }

    public void addFilterPriority(AlertPriority filterPriority) {
        this.filterPriority = filterPriority;
    }

    public void addFilterResourceTypeId(String filterResourceTypeId) {
        this.filterResourceTypeId = filterResourceTypeId;
    }

    public void addFilterResourceTypeName(String filterResourceTypeName) {
        this.filterResourceTypeName = filterResourceTypeName;
    }

    public void addFilterResourceIds(Integer... filterResourceIds) {
        this.filterResourceIds = Arrays.asList(filterResourceIds);
    }

    public void addFilterResourceGroupIds(Integer... filterResourceGroupIds) {
        this.filterResourceGroupIds = Arrays.asList(filterResourceGroupIds);
    }

    public void addFilterAlertDefinitionIds(Integer... filterAlertDefinitionIds) {
        this.filterAlertDefinitionIds = Arrays.asList(filterAlertDefinitionIds);
    }

    public void addFilterGroupAlertDefinitionIds(Integer... filterGroupAlertDefinitionIds) {
        this.filterGroupAlertDefinitionIds = Arrays.asList(filterGroupAlertDefinitionIds);
    }

    public void fetchAlertDefinition(boolean fetchAlertDefinition) {
        this.fetchAlertDefinition = fetchAlertDefinition;
    }

    public void fetchConditionLogs(boolean fetchConditionLogs) {
        this.fetchConditionLogs = fetchConditionLogs;
    }

    public void fetchNotificationLogs(boolean fetchNotificationLogs) {
        this.fetchNotificationLogs = fetchNotificationLogs;
    }

    public void fetchRecoveryAlertDefinition(boolean fetchRecoveryAlertDefinition) {
        this.fetchRecoveryAlertDefinition = fetchRecoveryAlertDefinition;
    }

    public void addSortCtime(PageOrdering sortCtime) {
        addSortField(SORT_FIELD_CTIME);
        this.sortCtime = sortCtime;
    }

    public void addSortName(PageOrdering sortName) {
        addSortField(SORT_FIELD_NAME);
        this.sortName = sortName;
    }

    public void addSortPriority(PageOrdering sortPriority) {
        addSortField(SORT_FIELD_PRIORITY);
        this.sortPriority = sortPriority;
    }

    public void addSortResourceId(PageOrdering sortResourceId) {
        addSortField(SORT_FIELD_RESOURCE_ID);
        this.sortResourceId = sortResourceId;
    }
}
