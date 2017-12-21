package org.sipfoundry.sipxconfig.rest;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

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
import static org.restlet.data.MediaType.TEXT_XML;

import org.sipfoundry.sipxconfig.callqueue.CallQueueAgent;
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;
import org.sipfoundry.sipxconfig.callqueue.CallQueueTier;

import com.thoughtworks.xstream.XStream;

public class CallQueueAgentsResource extends Resource {
    private static final Log LOG = LogFactory.getLog(CallQueueAgentsResource.class);

    private CallQueueContext m_callQueueContext;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_ALL_XML));
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Collection<CallQueueAgent> callQueueAgents = m_callQueueContext.getCallQueueAgents();
        Collection<CallQueueAgentRestInfo> data = new ArrayList<CallQueueAgentRestInfo>();
        for (CallQueueAgent callQueueAgent : callQueueAgents) {
            data.add(new CallQueueAgentRestInfo(callQueueAgent));
        }
        return new CallQueueAgentsRepresentation(variant.getMediaType(), data);
    }

    public void setCallQueueContext(CallQueueContext ctx) {
        m_callQueueContext = ctx;
    }

    static class CallQueueAgentsRepresentation extends XStreamRepresentation<Collection<CallQueueAgentRestInfo>> {
       public CallQueueAgentsRepresentation(MediaType mediaType, Collection<CallQueueAgentRestInfo> object) {
           super(mediaType, object);
       }

       public CallQueueAgentsRepresentation(Representation representation) {
           super(representation);
       }

       @Override
       protected void configureXStream(XStream xstream) {
           xstream.alias("agents", List.class);
           xstream.alias("agent", CallQueueAgentRestInfo.class);
           xstream.alias("tier", CallQueueAgentTierRestInfo.class);
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
