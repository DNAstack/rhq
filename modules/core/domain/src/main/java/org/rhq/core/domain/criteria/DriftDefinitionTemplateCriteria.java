/*
 * RHQ Management Platform
 * Copyright (C) 2011 Red Hat, Inc.
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

package org.rhq.core.domain.criteria;

import org.rhq.core.domain.drift.DriftDefinitionTemplate;

public class DriftDefinitionTemplateCriteria extends Criteria {
    private static final long serialVersionUID = 1L;

    private Integer filterId;
    private String filterName;
    private Integer filterResourceTypeId;

    public DriftDefinitionTemplateCriteria() {
        filterOverrides.put("filterResourceTypeId", "resourceType.id");
    }

    @Override
    public Class<?> getPersistentClass() {
        return DriftDefinitionTemplate.class;
    }

    public void addFilterId(Integer id) {
        filterId = id;
    }

    public void addFilterName(String name) {
        filterName = name;
    }

    public void addFilterResourceTypeId(Integer resourceTypeId) {
        filterResourceTypeId = resourceTypeId;
    }
}
