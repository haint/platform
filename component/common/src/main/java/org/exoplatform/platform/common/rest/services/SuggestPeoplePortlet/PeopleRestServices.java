package org.exoplatform.platform.common.rest.services.SuggestPeoplePortlet;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.RuntimeDelegate;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Path("/homepage/intranet/people/")
@Produces("application/json")
public class PeopleRestServices implements ResourceContainer {

    private static Log log = ExoLogger.getLogger(PeopleRestServices.class);

    private static final CacheControl cacheControl;

    static {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
        cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
    }

    @GET
    @Path("contacts/pending")
    public Response getPending(@Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);
            List<Relationship> relations = relationshipManager.getPending(identity);

            JSONArray jsonArray = new JSONArray();

            for (Relationship relation : relations) {

                Identity senderId = relation.getSender();
                Profile senderProfile = senderId.getProfile();
                Identity receiverId = relation.getReceiver();
                Profile receiverProfile = receiverId.getProfile();

                JSONObject json = new JSONObject();
                json.put("senderName", senderProfile.getFullName());
                json.put("senderId", senderId.getId());
                json.put("receiverName", receiverProfile.getFullName());
                json.put("receiverId", receiverId.getId());
                json.put("status", relation.getStatus());
                jsonArray.put(json);
            }

            return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

        } catch (Exception e) {
            log.error("Error in people pending rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }


    @GET
    @Path("contacts/incoming")
    public Response getIncoming(@Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);
            List<Relationship> relations = relationshipManager.getIncoming(identity);

            JSONArray jsonArray = new JSONArray();

            for (Relationship relation : relations) {

                Identity senderId = relation.getSender();
                String avatar = senderId.getProfile().getAvatarImageSource();
                if (avatar == null) {
                    avatar = "/social-resources/skin/ShareImages/Avatar.gif";
                }

                JSONObject json = new JSONObject();
                json.put("senderName", senderId.getProfile().getFullName());
                json.put("relationId", relation.getId());
                json.put("avatar", avatar);
                json.put("profile", senderId.getProfile().getUrl());
                jsonArray.put(json);
            }

            return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            log.error("Error in people incoming rest service: " + e.getMessage(), e);
            return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
        }

    }


    //confirm a request

    @GET
    @Path("contacts/confirm/{relationId}")
    public Response confirm(@PathParam("relationId") String relationId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);

            System.out.println("request accepted.");

            relationshipManager.confirm(relationshipManager.getRelationshipById(relationId));

            return Response.ok("Confirmed", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            log.error("Error in people accept rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }

    @GET
    @Path("contacts/deny/{relationId}")
    public Response deny(@PathParam("relationId") String relationId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);

            relationshipManager.deny(relationshipManager.getRelationshipById(relationId));

            return Response.ok("Denied", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

        } catch (Exception e) {
            log.error("Error in people deny rest service: " + e.getMessage(), e);
            return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
        }
    }

    @GET
    @Path("contacts/connect/{relationId}")
    public Response connect(@PathParam("relationId") String relationId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);

            relationshipManager.invite(identity, identityManager.getIdentity(relationId));

            return Response.ok("Connected", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            log.error("Error in people connect rest service: " + e.getMessage(), e);
            return Response.ok("Error", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        }

    }


    @GET
    @Path("contacts/suggestions")
    public Response getSuggestions(@Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
            RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);

            ProfileFilter profileFilter = new ProfileFilter();
            ListAccess<Identity> connectionList = relationshipManager.getConnections(identity);
            ListAccess<Identity> incomingList = relationshipManager.getIncomingWithListAccess(identity);
            ListAccess<Identity> outgoingList = relationshipManager.getOutgoing(identity);

            List<Identity> allIdentities = new ArrayList<Identity>(Arrays.asList(connectionList.load(0, connectionList.getSize())));
            allIdentities.addAll(Arrays.asList(incomingList.load(0, incomingList.getSize())));
            allIdentities.addAll(Arrays.asList(outgoingList.load(0, outgoingList.getSize())));
            allIdentities.add(identity);

            profileFilter.setExcludedIdentityList(allIdentities);
            List<Identity> suggestions = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, profileFilter);

            JSONArray jsonArray = new JSONArray();

            for (Identity id : suggestions) {
                ListAccess<Identity> SugggestconnectionList = relationshipManager.getConnections(id);
                List<Identity> SuggestallIdentities = new ArrayList<Identity>(Arrays.asList(SugggestconnectionList.load(0, SugggestconnectionList.getSize())));
                int k = 0;
                for (Identity i : SuggestallIdentities) {
                    for (Identity j : allIdentities) {
                        if (j.equals(i)) {
                            k++;
                        }
                    }

                }
                if (id.getRemoteId().equals("root")) continue;

                JSONObject json = new JSONObject();
                String avatar = id.getProfile().getAvatarImageSource();
                if (avatar == null) {
                    avatar = "/social-resources/skin/ShareImages/Avatar.gif";
                }
                String position = id.getProfile().getPosition();
                if (position == null) {
                    position = "";
                }
                json.put("suggestionName", id.getProfile().getFullName());
                json.put("suggestionId", id.getId());
                json.put("contacts", relationshipManager.getConnections(id).getSize());
                json.put("avatar", avatar);
                json.put("profile", id.getProfile().getUrl());
                json.put("title", position);
                json.put("number", k);
                jsonArray.put(json);
            }

            return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            log.error("Error in getting GS progress: " + e.getMessage(), e);
            return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
        }

    }


    private String getUserId(SecurityContext sc, UriInfo uriInfo) {
        try {
            return sc.getUserPrincipal().getName();
        } catch (NullPointerException e) {
            return getViewerId(uriInfo);
        } catch (Exception e) {
            return null;
        }
    }

    private String getViewerId(UriInfo uriInfo) {
        URI uri = uriInfo.getRequestUri();
        String requestString = uri.getQuery();
        if (requestString == null) return null;
        String[] queryParts = requestString.split("&");
        for (String queryPart : queryParts) {
            if (queryPart.startsWith("opensocial_viewer_id")) {
                return queryPart.substring(queryPart.indexOf("=") + 1, queryPart.length());
            }
        }
        return null;
    }

}