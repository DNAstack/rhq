/*
 * RHQ Management Platform
 * Copyright (C) 2010 Red Hat, Inc.
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
package org.rhq.enterprise.gui.coregui.client.inventory.resource.detail.alert;

import com.smartgwt.client.data.DSRequest;
import org.rhq.core.domain.criteria.AlertCriteria;
import org.rhq.enterprise.gui.coregui.client.alert.AbstractAlertDataSource;

import java.util.Arrays;

/**
 * @author Ian Springer
 */
public class ResourceAlertDataSource extends AbstractAlertDataSource {
    private Integer resourceId;

    public ResourceAlertDataSource(int resourceId) {
        super();
        this.resourceId = resourceId;
    }

    @Override
    protected AlertCriteria getCriteria(DSRequest request) {
        AlertCriteria criteria = super.getCriteria(request);
        criteria.addFilterResourceIds(this.resourceId);
        return criteria;
    }
}
