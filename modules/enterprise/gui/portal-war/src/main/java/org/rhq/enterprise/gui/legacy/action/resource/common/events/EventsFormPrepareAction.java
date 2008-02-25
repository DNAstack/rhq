/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
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

package org.rhq.enterprise.gui.legacy.action.resource.common.events;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.event.EventSeverity;
import org.rhq.core.domain.event.composite.EventComposite;
import org.rhq.core.domain.util.PageControl;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.gui.legacy.ParamConstants;
import org.rhq.enterprise.gui.legacy.WebUser;
import org.rhq.enterprise.gui.legacy.action.resource.common.monitor.visibility.MetricsControlAction;
import org.rhq.enterprise.gui.legacy.util.MonitorUtils;
import org.rhq.enterprise.gui.legacy.util.SessionUtils;
import org.rhq.enterprise.gui.util.WebUtility;
import org.rhq.enterprise.server.event.EventManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

/**
 * @author Heiko W. Rupp
 *
 */
public class EventsFormPrepareAction extends MetricsControlAction {

    EventManagerLocal eventManager;

    Log log = LogFactory.getLog(EventsFormPrepareAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        EventsForm eForm = (EventsForm) form;

        eventManager = LookupUtil.getEventManager();

        int eventId = WebUtility.getOptionalIntRequestParameter(request, "eventId", -1);
        int resourceId = WebUtility.getOptionalIntRequestParameter(request, ParamConstants.RESOURCE_ID_PARAM, -1);
        // TODO enhance for groups
        WebUser user = SessionUtils.getWebUser(request.getSession());
        Subject subject = user.getSubject();

        // Get metric range defaults
        Map pref = user.getMetricRangePreference(true);
        long begin = (Long) pref.get(MonitorUtils.BEGIN);
        long end = (Long) pref.get(MonitorUtils.END);

        PageControl pc = getPageControlFromRequest(request);

        EventSeverity severityFilter = getSeverityFromString(eForm.getSevFilter());
        String sourceFilter = eForm.getSourceFilter();
        String searchString = eForm.getSearchString();

        PageList<EventComposite> events = eventManager.getEvents(subject, resourceId, begin, end, severityFilter,
            eventId, sourceFilter, searchString, pc);
        for (EventComposite event : events) {
            event.setEventDetail(htmlFormat(event.getEventDetail(), eForm.getSearchString()));
            event.setSourceLocation(htmlFormat(event.getSourceLocation(), null));
        }

        eForm.setEvents(events);

        super.execute(mapping, form, request, response);
        return null;
    }

    private PageControl getPageControlFromRequest(HttpServletRequest request) {

        String pns = WebUtility.getOptionalRequestParameter(request, "pn", ""); // page number
        String pss = WebUtility.getOptionalRequestParameter(request, "ps", "15"); // page size 
        String sc = WebUtility.getOptionalRequestParameter(request, "sc", ""); // sort column

        PageControl pc = new PageControl();
        if (!pns.equals(""))
            pc.setPageNumber(Integer.valueOf(pns));
        if (!pss.equals(""))
            pc.setPageSize(Integer.valueOf(pss));
        if (!sc.equals(""))
            pc.sortBy(sc);

        return pc;
    }

    public ActionForward getDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        System.out.println("AJAX!");

        return null;
    }

    /**
     * Try to parse the passed String an return an appropriate severity value
     * @param sevFilter
     * @return
     */
    private EventSeverity getSeverityFromString(String sevFilter) {

        if (sevFilter == null || sevFilter.equals(""))
            return null;
        try {
            EventSeverity sev = EventSeverity.valueOf(sevFilter);
            return sev;
        } catch (IllegalArgumentException iae) {
            log.warn("Illegal EventSeverity passed: " + sevFilter);
            return null;
        }
    }

    /**
     * Format the input so that CR becomes a html-break and
     * a searchResult will be highlighted
     * 
     * TODO extend and put in a Util class together with the version from {@link OneEventDetailAction}
     */
    private String htmlFormat(String input, String searchResult) {
        String output;
        output = input.replaceAll("\\n", "<br/>\n");
        if (searchResult != null && !searchResult.equals("")) {
            output = output.replaceAll("(" + searchResult + ")", "<b>$1</b>");
        }
        return output;
    }
}
