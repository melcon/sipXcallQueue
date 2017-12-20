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
import org.restlet.resource.Variant;
import static org.restlet.data.MediaType.APPLICATION_ALL_XML;
import static org.restlet.data.MediaType.TEXT_XML;
import static org.restlet.data.MediaType.APPLICATION_JSON;

import org.sipfoundry.sipxconfig.callqueue.CallQueue;
import org.sipfoundry.sipxconfig.callqueue.CallQueueContext;

import com.thoughtworks.xstream.XStream;

public class CallQueuesResource extends Resource {
    private static final Log LOG = LogFactory.getLog(CallQueuesResource.class);

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
    public Representation represent(Variant variant) throws ResourceException {
        Collection<CallQueue> callQueues = m_callQueueContext.getCallQueues();
        Collection<CallQueueRestInfo> data = new ArrayList<CallQueueRestInfo>();
        for (CallQueue callQueue : callQueues) {
            data.add(new CallQueueRestInfo(callQueue));
        }
        return new CallQueuesRepresentation(variant.getMediaType(), data);
    }

    public void setCallQueueContext(CallQueueContext ctx) {
        m_callQueueContext = ctx;
    }

    static class CallQueuesRepresentation extends XStreamRepresentation<Collection<CallQueueRestInfo>> {
       public CallQueuesRepresentation(MediaType mediaType, Collection<CallQueueRestInfo> object) {
           super(mediaType, object);
       }

       public CallQueuesRepresentation(Representation representation) {
           super(representation);
       }

       @Override
       protected void configureXStream(XStream xstream) {
           xstream.alias("queues", List.class);
           xstream.alias("queue", CallQueueRestInfo.class);
       }
    }

    static class CallQueueRestInfo {
        private final Integer m_id;
        private final String m_name;
        private final String m_extension;

        public CallQueueRestInfo(CallQueue callqueue) {
            m_name = callqueue.getName();
            m_extension = callqueue.getExtension();
            m_id = callqueue.getId();
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
    }

}
