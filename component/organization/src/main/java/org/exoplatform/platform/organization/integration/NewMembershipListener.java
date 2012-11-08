/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.platform.organization.integration;

import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;

/**
 * This Listener is invoked when a Mambership is updated/added. Its purpose
 * is to ensure that OrganizationServiceIntegration don't apply
 * Organization Model Data listeners twice.
 * 
 * @author Boubaker KHANFIR
 */
public class NewMembershipListener extends MembershipEventListener {

  private RepositoryService repositoryService;

  public NewMembershipListener(RepositoryService repositoryService) throws Exception {
    this.repositoryService = repositoryService;
  }

  /**
   * {@inheritDoc}
   */
  public void postSave(Membership m, boolean isNew) throws Exception {
    if (!isNew) {
      return;
    }
    Session session = null;
    try {
      session = repositoryService.getCurrentRepository().getSystemSession(Util.WORKSPACE);
      if (!Util.hasMembershipFolder(session, m)) {
        Util.createMembershipFolder(session, m);
      }
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void postDelete(Membership m) throws Exception {
    Session session = null;
    try {
      session = repositoryService.getCurrentRepository().getSystemSession(Util.WORKSPACE);
      if (Util.hasMembershipFolder(session, m)) {
        Util.deleteMembershipFolder(session, m);
      }
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
}