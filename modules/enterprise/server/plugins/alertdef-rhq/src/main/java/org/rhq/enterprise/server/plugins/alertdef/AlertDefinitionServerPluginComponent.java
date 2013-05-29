/*
 * RHQ Management Platform
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.enterprise.server.plugins.alertdef;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.rhq.core.domain.alert.AlertCondition;
import org.rhq.core.domain.alert.AlertConditionCategory;
import org.rhq.core.domain.alert.AlertDampening;
import org.rhq.core.domain.alert.AlertDampening.TimeUnits;
import org.rhq.core.domain.alert.AlertDefinition;
import org.rhq.core.domain.alert.AlertPriority;
import org.rhq.core.domain.alert.BooleanExpression;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.criteria.AlertDefinitionCriteria;
import org.rhq.core.domain.criteria.ResourceTypeCriteria;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.enterprise.server.alert.AlertDefinitionManagerLocal;
import org.rhq.enterprise.server.alert.AlertTemplateManagerLocal;
import org.rhq.enterprise.server.auth.SubjectManagerLocal;
import org.rhq.enterprise.server.plugin.pc.ControlFacet;
import org.rhq.enterprise.server.plugin.pc.ControlResults;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;
import org.rhq.enterprise.server.resource.ResourceTypeManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

/**
 * An alert definition server-side plugin component that the server uses to inject "factory-installed" alert definitions.
 * 
 * @author Jay Shaughnessy
 */
public class AlertDefinitionServerPluginComponent implements ServerPluginComponent, ControlFacet {

    private final Log log = LogFactory.getLog(AlertDefinitionServerPluginComponent.class);

    static private final List<InjectedTemplate> injectedTemplates;
    static private final InjectedTemplate storageNodeHighHeapTemplate;

    static {
        storageNodeHighHeapTemplate = new InjectedTemplate(
            "RHQStorage", //
            "VM Memory System", //
            "StorageNodeHighHeapTemplate", //
            "An alert template to notify users of excessive heap use by an RHQ Storage Node. When fired please see documentation for the proper corrective action.");

        injectedTemplates = new ArrayList<InjectedTemplate>();
        injectedTemplates.add(storageNodeHighHeapTemplate);
    }

    private ServerPluginContext context;

    @Override
    public void initialize(ServerPluginContext context) throws Exception {
        this.context = context;
        log.debug("The RHQ AlertDefinition plugin has been initialized!!! : " + this);
    }

    @Override
    public void start() {
        boolean injectAtPluginStartup = Boolean.valueOf(context.getPluginConfiguration().getSimpleValue(
            "injectAtPluginStartup", "true"));
        boolean replaceIfExists = Boolean.valueOf(context.getPluginConfiguration().getSimpleValue("replaceIfExists",
            "false"));

        if (injectAtPluginStartup) {
            injectAllAlertDefs(replaceIfExists);
        }

        log.debug("The RHQ AlertDefinition plugin has started!!! : " + this);

    }

    @Override
    public void stop() {
        log.debug("The RHQ AlertDefinition plugin has stopped!!! : " + this);
    }

    @Override
    public void shutdown() {
        log.debug("The RHQ AlertDefinition plugin has been shut down!!! : " + this);
    }

    @Override
    public ControlResults invoke(String name, Configuration parameters) {
        ControlResults controlResults = new ControlResults();

        try {
            if (name.equals("listInjectedAlertDefinitions")) {
                PropertyList result = new PropertyList("injectedAlertDefinitions");
                for (InjectedTemplate iad : injectedTemplates) {
                    PropertyMap map = new PropertyMap("injectedAlertDefinition");
                    map.put(new PropertySimple(InjectedTemplate.FIELD_PLUGIN_NAME, iad.getPluginName()));
                    map.put(new PropertySimple(InjectedTemplate.FIELD_RESOURCE_TYPE_NAME, iad.getResourceTypeName()));
                    map.put(new PropertySimple(InjectedTemplate.FIELD_NAME, iad.getName()));
                    map.put(new PropertySimple(InjectedTemplate.FIELD_DESCRIPTION, iad.getDescription()));
                    result.add(map);
                }
                controlResults.getComplexResults().put(result);

            } else if (name.equals("injectAllAlertDefinitions")) {
                injectAllAlertDefs(Boolean.valueOf(parameters.getSimpleValue("replaceIfExists")));

            } else if (name.equals("injectAlertDefinition")) {
                InjectedTemplate requestedInjection = new InjectedTemplate( //
                    parameters.getSimpleValue(InjectedTemplate.FIELD_PLUGIN_NAME), //
                    parameters.getSimpleValue(InjectedTemplate.FIELD_RESOURCE_TYPE_NAME), //
                    parameters.getSimpleValue(InjectedTemplate.FIELD_NAME), null);
                boolean injected = false;
                for (InjectedTemplate iad : injectedTemplates) {
                    if (iad.equals(requestedInjection)) {
                        injectAlertDef(iad, Boolean.valueOf(parameters.getSimpleValue("replaceIfExists", "false")));
                        injected = true;
                        break;
                    }
                }

                if (!injected) {
                    controlResults
                        .setError("Unknown requested alert definition. Check spelling: " + requestedInjection);
                }

            } else {
                controlResults.setError("Unknown control name: " + name);
            }
        } catch (Throwable t) {
            controlResults.setError(t);
        }

        return controlResults;
    }

    private List<AlertDefinition> injectAllAlertDefs(boolean replaceIfExists) {

        List<AlertDefinition> result = new ArrayList<AlertDefinition>();

        for (InjectedTemplate iad : injectedTemplates) {
            AlertDefinition newAlertDef = injectAlertDef(iad, replaceIfExists);
            if (null != newAlertDef) {
                result.add(newAlertDef);
            }
        }

        return result;
    }

    private AlertDefinition injectAlertDef(InjectedTemplate injectedAlertDef, boolean replaceIfExists) {

        AlertDefinition result = null;
        ResourceTypeManagerLocal typeManager = LookupUtil.getResourceTypeManager();
        AlertDefinitionManagerLocal alertDefManager = LookupUtil.getAlertDefinitionManager();
        SubjectManagerLocal subjectManager = LookupUtil.getSubjectManager();

        ResourceTypeCriteria rtc = new ResourceTypeCriteria();
        rtc.addFilterPluginName(injectedAlertDef.getPluginName());
        rtc.addFilterName(injectedAlertDef.getResourceTypeName());
        rtc.fetchMetricDefinitions(true);
        List<ResourceType> resourceTypes = typeManager.findResourceTypesByCriteria(subjectManager.getOverlord(), rtc);

        if (resourceTypes.isEmpty()) {
            return result;
        }

        assert 1 == resourceTypes.size() : "Found more than 1 resource type!";
        ResourceType resourceType = resourceTypes.get(0);

        AlertDefinitionCriteria adc = new AlertDefinitionCriteria();
        adc.addFilterName(injectedAlertDef.getName());
        adc.addFilterAlertTemplateResourceTypeId(resourceType.getId());
        List<AlertDefinition> alertDefs = alertDefManager.findAlertDefinitionsByCriteria(subjectManager.getOverlord(),
            adc);

        if (!alertDefs.isEmpty()) {
            assert 1 == alertDefs.size() : "Found more than 1 existing alert def!";

            if (!replaceIfExists) {
                return result;
            }

            int[] alertDefIdArray = new int[1];
            alertDefIdArray[0] = alertDefs.get(0).getId();
            alertDefManager.removeAlertDefinitions(subjectManager.getOverlord(), alertDefIdArray);
        }

        int newAlertDefId = 0;

        if (storageNodeHighHeapTemplate.equals(injectedAlertDef)) {
            newAlertDefId = injectStorageNodeHighHeapTemplate(resourceType);
        }

        adc.addFilterId(newAlertDefId);
        alertDefs = alertDefManager.findAlertDefinitionsByCriteria(subjectManager.getOverlord(), adc);
        assert 1 == alertDefs.size() : "Found more than 1 new alert def!";
        result = alertDefs.get(0);

        return result;
    }

    private int injectStorageNodeHighHeapTemplate(ResourceType resourceType) {
        AlertTemplateManagerLocal alertTemplateManager = LookupUtil.getAlertTemplateManager();
        SubjectManagerLocal subjectManager = LookupUtil.getSubjectManager();

        AlertDefinition newTemplate = new AlertDefinition();
        newTemplate.setName(storageNodeHighHeapTemplate.getName());
        newTemplate.setResourceType(resourceType);
        newTemplate.setPriority(AlertPriority.MEDIUM);
        newTemplate.setConditionExpression(BooleanExpression.ANY);
        newTemplate.setRecoveryId(0);
        newTemplate.setEnabled(true);

        AlertCondition ac = new AlertCondition();
        ac.setCategory(AlertConditionCategory.THRESHOLD);
        ac.setComparator(">");
        ac.setThreshold(0.75D);
        for (MeasurementDefinition d : resourceType.getMetricDefinitions()) {
            if ("Calculated.HeapUsagePercentage".equals(d.getName())) {
                ac.setMeasurementDefinition(d);
                ac.setName(d.getDisplayName());
                break;
            }
        }
        assert null != ac.getMeasurementDefinition() : "Did not find expected measurement definition [Calculated.HeapUsagePercentage] for "
            + resourceType;
        newTemplate.addCondition(ac);

        AlertDampening dampener = new AlertDampening(AlertDampening.Category.PARTIAL_COUNT);
        dampener.setPeriod(15);
        dampener.setPeriodUnits(TimeUnits.MINUTES);
        dampener.setValue(10);
        newTemplate.setAlertDampening(dampener);

        int newTemplateId = alertTemplateManager.createAlertTemplate(subjectManager.getOverlord(), newTemplate,
            resourceType.getId());

        return newTemplateId;
    }

    private static class InjectedTemplate {
        static public final String FIELD_PLUGIN_NAME = "plugin";
        static public final String FIELD_RESOURCE_TYPE_NAME = "type";
        static public final String FIELD_NAME = "name";
        static public final String FIELD_DESCRIPTION = "description";

        private String pluginName;
        private String resourceTypeName;
        private String name;
        private String description;

        public InjectedTemplate(String pluginName, String resourceTypeName, String name, String description) {
            super();
            this.pluginName = pluginName;
            this.resourceTypeName = resourceTypeName;
            this.name = name;
            this.description = description;
        }

        public String getPluginName() {
            return pluginName;
        }

        public String getResourceTypeName() {
            return resourceTypeName;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((pluginName == null) ? 0 : pluginName.hashCode());
            result = prime * result + ((resourceTypeName == null) ? 0 : resourceTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            InjectedTemplate other = (InjectedTemplate) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (pluginName == null) {
                if (other.pluginName != null)
                    return false;
            } else if (!pluginName.equals(other.pluginName))
                return false;
            if (resourceTypeName == null) {
                if (other.resourceTypeName != null)
                    return false;
            } else if (!resourceTypeName.equals(other.resourceTypeName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "InjectedAlertDef [pluginName=" + pluginName + ", resourceTypeName=" + resourceTypeName + ", name="
                + name + "]";
        }
    }

}