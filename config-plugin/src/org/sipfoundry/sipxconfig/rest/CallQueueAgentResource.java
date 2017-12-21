package org.sipfoundry.sipxconfig.rest;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import static org.restlet.data.MediaType.APPLICATION_ALL_XML;
import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.TEXT_XML;

import org.sipfoundry.sipxconfig.callqueue.CallQueueAgent;
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTier;
import org.sipfoundry.sipxconfig.rest.RestUtilities.IntParameterInfo;
import org.sipfoundry.sipxconfig.rest.RestUtilities.ValidationInfo;
import org.sipfoundry.sipxconfig.rest.RestUtilities.ValidationInfo.StringConstraint;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_CREATE_FAILED;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_DELETE_FAILED;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_ID_INVALID;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_MISSING_ID;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_OBJECT_NOT_FOUND;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_READ_FAILED;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.ERROR_UPDATE_FAILED;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.SUCCESS_CREATED;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.SUCCESS_DELETED;
import static org.sipfoundry.sipxconfig.rest.RestUtilities.ResponseCode.SUCCESS_UPDATED;


import com.thoughtworks.xstream.XStream;

public class CallQueueAgentResource extends Resource {
    private static final Log LOG = LogFactory.getLog(CallQueueAgentsResource.class);

    private CallQueueContext m_callQueueContext;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_ALL_XML));
        getVariants().add(new Variant(APPLICATION_JSON));
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        IntParameterInfo parameterInfo;
        CallQueueAgent agent;
        CallQueueAgentRestInfo agentRestInfo;

        parameterInfo = RestUtilities.getIntFromAttribute(getRequest(), "agent");
        if (!parameterInfo.getValid()) {
            return RestUtilities.getResponseError(getResponse(), ERROR_ID_INVALID, parameterInfo.getValueString());
        }
        try {
            agent = m_callQueueContext.loadCallQueueAgent(parameterInfo.getValue());
            if (agent == null) {
                return RestUtilities.getResponseError(getResponse(), ERROR_OBJECT_NOT_FOUND, parameterInfo.getValue());
            }
            agentRestInfo = new CallQueueAgentRestInfo(agent);
        } catch (Exception exception) {
            return RestUtilities.getResponseError(getResponse(), ERROR_READ_FAILED, parameterInfo.getValue(),exception.getLocalizedMessage());
        }
        return new CallQueueAgentRepresentation(variant.getMediaType(), agentRestInfo);
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        IntParameterInfo parameterInfo;

        // get from request body
        CallQueueAgentRepresentation representation = new CallQueueAgentRepresentation(entity);
        CallQueueAgentRestInfo agentRestInfo = representation.getObject();
        CallQueueAgent agent;

        // validate input for update or create
        ValidationInfo validationInfo = validate(agentRestInfo);

        if (!validationInfo.getValid()) {
            RestUtilities.setResponseError(getResponse(), validationInfo.getResponseCode(), validationInfo.getMessage());
            return;
        }

        // if have id then update single item
        parameterInfo = RestUtilities.getIntFromAttribute(getRequest(), "agent");
        if (parameterInfo.getExists()) {
            if (!parameterInfo.getValid()) {
                RestUtilities.setResponseError(getResponse(), ERROR_ID_INVALID, parameterInfo.getValueString());
                return;
            }

            // copy values over to existing item
            try {
                agent = m_callQueueContext.loadCallQueueAgent(parameterInfo.getValue());
                if (agent == null) {
                    RestUtilities.setResponseError(getResponse(), ERROR_OBJECT_NOT_FOUND, parameterInfo.getValue());
                    return;
                }
                updateAgent(agent, agentRestInfo);
                m_callQueueContext.saveCallQueueAgent(agent);
            } catch (Exception exception) {
                RestUtilities.setResponseError(getResponse(), ERROR_UPDATE_FAILED, parameterInfo.getValue(),exception.getLocalizedMessage());
                return;
            }

            RestUtilities.setResponse(getResponse(), SUCCESS_UPDATED, agent.getId());
            return;
        }

        //// otherwise add new item
        //try {
        //    user = createUser(userRestInfo);
        //    m_coreContext.saveUser(user);
        //} catch (Exception exception) {
        //    RestUtilities.setResponseError(getResponse(), ERROR_CREATE_FAILED, exception.getLocalizedMessage());
        //    return;
        //}

        //RestUtilities.setResponse(getResponse(), SUCCESS_CREATED, user.getId());
    }

    private void updateAgent(CallQueueAgent agent, CallQueueAgentRestInfo agentRestInfo) {
        agent.setName(agentRestInfo.getName());
        agent.setExtension(agentRestInfo.getExtension());
        agent.setDescription(agentRestInfo.getDescription());
        Set<CallQueueTier> tiers = new HashSet<CallQueueTier>();
        for (CallQueueAgentTierRestInfo tierinfo : agentRestInfo.getTiers()) {
            CallQueueTier tier = new CallQueueTier();
            tier.setCallQueueId(tierinfo.getQueueId());
            tier.setPosition(tierinfo.getPosition());
            tier.setLevel(tierinfo.getLevel());
            tier.setCallQueueAgentId(agent.getId());
            tiers.add(tier);
        }
        agent.getTiers().setTiers(tiers);
    }

    private ValidationInfo validate(CallQueueAgentRestInfo restInfo) {
        ValidationInfo validationInfo = new ValidationInfo();

        String name = restInfo.getName();
        String extension = restInfo.getExtension();

        validationInfo.checkString(name, "Name", StringConstraint.NOT_EMPTY);
        validationInfo.checkString(extension, "Extension", StringConstraint.NOT_EMPTY);

        return validationInfo;
    }

    public void setCallQueueContext(CallQueueContext ctx) {
        m_callQueueContext = ctx;
    }

    static class CallQueueAgentRepresentation extends XStreamRepresentation<CallQueueAgentRestInfo> {
       public CallQueueAgentRepresentation(MediaType mediaType, CallQueueAgentRestInfo object) {
           super(mediaType, object);
       }

       public CallQueueAgentRepresentation(Representation representation) {
           super(representation);
       }

       @Override
       protected void configureXStream(XStream xstream) {
           xstream.alias("agent", CallQueueAgentRestInfo.class);
           xstream.alias("tier", CallQueueAgentTierRestInfo.class);
           xstream.alias("tiers", List.class);
       }
    }

    static class CallQueueAgentRestInfo {
        private final Integer m_id;
        private final String m_name;
        private final String m_extension;
        private final String m_description;
        private final List<CallQueueAgentTierRestInfo> m_tiers;

        public CallQueueAgentRestInfo(CallQueueAgent callqueueagent) {
            m_id = callqueueagent.getId();
            m_name = callqueueagent.getName();
            m_extension = callqueueagent.getExtension();
            m_description = callqueueagent.getDescription();
            callqueueagent.getTiers().getTiers();
            List<CallQueueAgentTierRestInfo> data = new ArrayList<CallQueueAgentTierRestInfo>();
            for (CallQueueTier callQueueTier : callqueueagent.getTiers().getTiers()) {
                data.add(new CallQueueAgentTierRestInfo(callQueueTier));
            }
            m_tiers = data;
        }

        public Integer getId() {
            return m_id;
        }

        public String getName() {
            return m_name;
        }

        public String getExtension() {
            return m_extension;
        }
        public String getDescription() {
            return m_description;
        }

        public List<CallQueueAgentTierRestInfo> getTiers() {
            return m_tiers;
        }
    }

    static class CallQueueAgentTierRestInfo {
        private final Integer m_level;
        private final Integer m_position;
        private final Integer m_queueid;

        public CallQueueAgentTierRestInfo(CallQueueTier callqueuetier) {
            m_level = callqueuetier.getLevel();
            m_position = callqueuetier.getPosition();
            m_queueid = callqueuetier.getCallQueueId();
        }

        public Integer getLevel() {
            return m_level;
        }

        public Integer getPosition() {
            return m_position;
        }

        public Integer getQueueId() {
            return m_queueid;
        }
    }
}
