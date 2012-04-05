/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
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
package org.rhq.enterprise.gui.coregui.client.inventory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;

import org.rhq.core.domain.authz.Permission;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.resource.ResourceCategory;
import org.rhq.core.domain.resource.group.GroupCategory;
import org.rhq.enterprise.gui.coregui.client.IconEnum;
import org.rhq.enterprise.gui.coregui.client.ImageManager;
import org.rhq.enterprise.gui.coregui.client.PermissionsLoadedListener;
import org.rhq.enterprise.gui.coregui.client.PermissionsLoader;
import org.rhq.enterprise.gui.coregui.client.components.TitleBar;
import org.rhq.enterprise.gui.coregui.client.components.view.AbstractSectionedLeftNavigationView;
import org.rhq.enterprise.gui.coregui.client.components.view.HasViewName;
import org.rhq.enterprise.gui.coregui.client.components.view.NavigationItem;
import org.rhq.enterprise.gui.coregui.client.components.view.NavigationSection;
import org.rhq.enterprise.gui.coregui.client.components.view.ViewFactory;
import org.rhq.enterprise.gui.coregui.client.components.view.ViewName;
import org.rhq.enterprise.gui.coregui.client.inventory.groups.ResourceGroupDataSourceField;
import org.rhq.enterprise.gui.coregui.client.inventory.groups.ResourceGroupListView;
import org.rhq.enterprise.gui.coregui.client.inventory.groups.definitions.GroupDefinitionListView;
import org.rhq.enterprise.gui.coregui.client.inventory.resource.ResourceDataSourceField;
import org.rhq.enterprise.gui.coregui.client.inventory.resource.ResourceDatasource;
import org.rhq.enterprise.gui.coregui.client.inventory.resource.ResourceSearchView;
import org.rhq.enterprise.gui.coregui.client.inventory.resource.discovery.ResourceAutodiscoveryView;
import org.rhq.enterprise.gui.coregui.client.util.selenium.LocatableVLayout;

/**
 * The Inventory top-level view.
 *
 * @author Greg Hinkle
 * @author Joseph Marques
 * @author Ian Springer
 */
public class InventoryView extends AbstractSectionedLeftNavigationView {
    public static final ViewName VIEW_ID = new ViewName("Inventory", MSG.common_title_inventory());

    // view IDs for Resources section
    private static final ViewName RESOURCES_SECTION_VIEW_ID = new ViewName("Resources", MSG.common_title_resources());

    private static final ViewName PAGE_ALL_RESOURCES = new ViewName("AllResources", MSG.view_inventory_allResources(),
        IconEnum.ALL_RESOURCES);
    private static final ViewName PAGE_PLATFORMS = new ViewName("Platforms", MSG.view_inventory_platforms(),
        IconEnum.PLATFORMS);
    private static final ViewName PAGE_SERVERS_TOP = new ViewName("ServersTop", MSG.view_inventory_serversTop(),
        IconEnum.SERVERS);
    private static final ViewName PAGE_SERVERS = new ViewName("Servers", MSG.view_inventory_servers(),
        IconEnum.SERVERS);
    private static final ViewName PAGE_SERVICES = new ViewName("Services", MSG.view_inventory_services(),
        IconEnum.SERVICES);
    private static final ViewName PAGE_UNAVAIL_SERVERS = new ViewName("UnavailableServers",
        MSG.view_inventory_unavailableServers(), IconEnum.UNAVAILABLE_SERVERS);

    // view IDs for Groups section
    private static final ViewName GROUPS_SECTION_VIEW_ID = new ViewName("Groups", MSG.view_inventory_groups());

    private static final ViewName PAGE_DYNAGROUP_DEFINITIONS = new ViewName("DynagroupDefinitions",
        MSG.view_inventory_dynagroupDefs(), IconEnum.DYNAGROUPS);
    private static final ViewName PAGE_ALL_GROUPS = new ViewName("AllGroups", MSG.view_inventory_allGroups(),
        IconEnum.ALL_GROUPS);
    private static final ViewName PAGE_COMPATIBLE_GROUPS = new ViewName("CompatibleGroups",
        MSG.common_title_compatibleGroups(), IconEnum.COMPATIBLE_GROUPS);
    private static final ViewName PAGE_MIXED_GROUPS = new ViewName("MixedGroups", MSG.common_title_mixedGroups(),
        IconEnum.MIXED_GROUPS);
    private static final ViewName PAGE_PROBLEM_GROUPS = new ViewName("ProblemGroups",
        MSG.view_inventory_problemGroups(), IconEnum.PROBLEM_GROUPS);

    private Set<Permission> globalPermissions;

    public InventoryView() {
        // This is a top level view, so our locator id can simply be our view id.
        super(VIEW_ID.getName());
    }

    @Override
    protected void onInit() {
        new PermissionsLoader().loadExplicitGlobalPermissions(new PermissionsLoadedListener() {
            @Override
            public void onPermissionsLoaded(Set<Permission> permissions) {
                globalPermissions = (permissions != null) ? permissions : EnumSet.noneOf(Permission.class);
                InventoryView.super.onInit();
            }
        });
    }

    protected Canvas defaultView() {
        LocatableVLayout vLayout = new LocatableVLayout(this.extendLocatorId("Default"));
        vLayout.setWidth100();

        // TODO: Admin icon.
        TitleBar titleBar = new TitleBar(this, MSG.common_title_inventory(), IconEnum.INVENTORY.getIcon24x24Path());
        vLayout.addMember(titleBar);

        Label label = new Label(MSG.view_inventory_sectionHelp());
        label.setPadding(10);
        vLayout.addMember(label);

        return vLayout;
    }

    @Override
    protected List<NavigationSection> getNavigationSections() {
        List<NavigationSection> sections = new ArrayList<NavigationSection>();

        NavigationSection resourcesSection = buildResourcesSection();
        sections.add(resourcesSection);

        NavigationSection groupsSection = buildGroupsSection();
        sections.add(groupsSection);

        return sections;
    }

    private ResourceSearchView createResourceSearchView(ViewName viewName, Criteria initialCriteria) {
        return new ResourceSearchViewWrapper(viewName, initialCriteria, viewName.getIcon().getIcon24x24Path());
    }

    private NavigationSection buildResourcesSection() {
        NavigationItem autodiscoveryQueueItem = new NavigationItem(ResourceAutodiscoveryView.VIEW_ID,
            new ViewFactory() {
                public Canvas createView() {
                    return new ResourceAutodiscoveryView(extendLocatorId(
                        ResourceAutodiscoveryView.VIEW_ID.getName()));
                }
            }, this.globalPermissions.contains(Permission.MANAGE_INVENTORY));
        autodiscoveryQueueItem.setRefreshRequired(true);

        NavigationItem allResourcesItem = new NavigationItem(PAGE_ALL_RESOURCES, new ViewFactory() {
            public Canvas createView() {
                return new ResourceSearchViewWrapper(PAGE_ALL_RESOURCES, null,
                    ImageManager.getResourceLargeIcon(ResourceCategory.PLATFORM),
                    ImageManager.getResourceLargeIcon(ResourceCategory.SERVER),
                    ImageManager.getResourceLargeIcon(ResourceCategory.SERVICE));
            }
        });

        NavigationItem platformsItem = new NavigationItem(PAGE_PLATFORMS, new ViewFactory() {
            public Canvas createView() {
                Criteria initialCriteria = new Criteria(ResourceDataSourceField.CATEGORY.propertyName(),
                    ResourceCategory.PLATFORM.name());
                return createResourceSearchView(PAGE_PLATFORMS, initialCriteria);
            }
        });

        NavigationItem topLevelServersItem = new NavigationItem(PAGE_SERVERS_TOP, new ViewFactory() {
            public Canvas createView() {
                Criteria initialCriteria = new Criteria(ResourceDataSourceField.CATEGORY.propertyName(),
                    ResourceCategory.SERVER.name());
                initialCriteria.addCriteria(ResourceDatasource.FILTER_PARENT_CATEGORY, ResourceCategory.PLATFORM.name());
                return createResourceSearchView(PAGE_SERVERS_TOP, initialCriteria);
            }
        });

        NavigationItem serversItem = new NavigationItem(PAGE_SERVERS, new ViewFactory() {
            public Canvas createView() {
                Criteria initialCriteria = new Criteria(ResourceDataSourceField.CATEGORY.propertyName(),
                    ResourceCategory.SERVER.name());
                return createResourceSearchView(PAGE_SERVERS, initialCriteria);
            }
        });

        NavigationItem servicesItem = new NavigationItem(PAGE_SERVICES, new ViewFactory() {
            public Canvas createView() {

                Criteria initialCriteria = new Criteria(ResourceDataSourceField.CATEGORY.propertyName(),
                    ResourceCategory.SERVICE.name());
                return createResourceSearchView(PAGE_SERVICES, initialCriteria);
            }
        });

        NavigationItem downServersItem = new NavigationItem(PAGE_UNAVAIL_SERVERS, new ViewFactory() {
            public Canvas createView() {
                Criteria initialCriteria = new Criteria(ResourceDataSourceField.AVAILABILITY.propertyName(),
                    AvailabilityType.DOWN.name());
                initialCriteria.addCriteria(ResourceDataSourceField.CATEGORY.propertyName(),
                    ResourceCategory.SERVER.name());

                return createResourceSearchView(PAGE_UNAVAIL_SERVERS, initialCriteria);
            }
        });

        return new NavigationSection(RESOURCES_SECTION_VIEW_ID, autodiscoveryQueueItem, allResourcesItem,
            platformsItem, topLevelServersItem, serversItem, servicesItem, downServersItem);
    }

    private NavigationSection buildGroupsSection() {
        NavigationItem dynagroupDefinitionsItem = new NavigationItem(PAGE_DYNAGROUP_DEFINITIONS, new ViewFactory() {
            public Canvas createView() {
                return new GroupDefinitionListView(extendLocatorId(PAGE_DYNAGROUP_DEFINITIONS.getName()));
            }
        }, this.globalPermissions.contains(Permission.MANAGE_INVENTORY));

        NavigationItem allGroupsItem = new NavigationItem(PAGE_ALL_GROUPS, new ViewFactory() {
            public Canvas createView() {
                return new ResourceGroupListView(extendLocatorId(PAGE_ALL_GROUPS.getName()));
            }
        });

        NavigationItem compatibleGroupsItem = new NavigationItem(PAGE_COMPATIBLE_GROUPS, new ViewFactory() {
            public Canvas createView() {
                ResourceGroupListView view = new ResourceGroupListView(
                    extendLocatorId(PAGE_COMPATIBLE_GROUPS.getName()), new Criteria(
                        ResourceGroupDataSourceField.CATEGORY.propertyName(), GroupCategory.COMPATIBLE.name()));
                return view;
            }
        });

        NavigationItem mixedGroupsItem = new NavigationItem(PAGE_MIXED_GROUPS, new ViewFactory() {
            public Canvas createView() {
                ResourceGroupListView view = new ResourceGroupListView(extendLocatorId(PAGE_MIXED_GROUPS.getName()),
                    new Criteria(ResourceGroupDataSourceField.CATEGORY.propertyName(), GroupCategory.MIXED.name()));
                return view;
            }
        });

        NavigationItem problemGroupsItem = new NavigationItem(PAGE_PROBLEM_GROUPS, new ViewFactory() {
            public Canvas createView() {
                ResourceGroupListView view = new ResourceGroupListView(extendLocatorId(PAGE_PROBLEM_GROUPS.getName()),
                    new Criteria("downMemberCount", "1"));
                view.setShowNewButton(false);
                return view;
            }
        });

        return new NavigationSection(GROUPS_SECTION_VIEW_ID, allGroupsItem, dynagroupDefinitionsItem,
            compatibleGroupsItem, mixedGroupsItem, problemGroupsItem);
    }

    private class ResourceSearchViewWrapper extends ResourceSearchView implements HasViewName {

        private ViewName viewName;

        public ResourceSearchViewWrapper(ViewName viewName, Criteria criteria, String... headerIcons) {
            super(viewName.getName(), criteria, viewName.getTitle(), headerIcons);
            this.viewName = viewName;
        }

        public ResourceSearchViewWrapper(ViewName viewName, Criteria criteria) {
            super(viewName.getName(), criteria);
            this.viewName = viewName;
        }

        @Override
        public ViewName getViewName() {
            return viewName;
        }
    }

}
