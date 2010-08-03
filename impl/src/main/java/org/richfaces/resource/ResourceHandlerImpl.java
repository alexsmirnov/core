/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2009 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.richfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.component.StateHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ajax4jsf.cache.Cache;
import org.ajax4jsf.cache.CacheManager;
import org.ajax4jsf.resource.Java2Dresource;
import org.richfaces.application.ServiceTracker;
import org.richfaces.log.RichfacesLogger;
import org.richfaces.util.Util;
import org.richfaces.util.RequestStateManager.BooleanRequestStateVariable;
import org.slf4j.Logger;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class ResourceHandlerImpl extends ResourceHandler {
    public static final String RICHFACES_RESOURCE_IDENTIFIER = "/rfRes/";
    public static final String RESOURCE_CACHE_NAME = ResourceHandlerImpl.class.getName() + ":CACHE";
    public static final String HANDLER_START_TIME_ATTRIBUTE = ResourceHandlerImpl.class.getName() + ":StartTime";

    private static final Logger LOGGER = RichfacesLogger.RESOURCE.getLogger();

    // TODO - review - do we need this?
    static {

        // set in-memory caching ImageIO
        Thread thread = Thread.currentThread();
        ClassLoader initialTCCL = thread.getContextClassLoader();

        try {
            ClassLoader systemCL = ClassLoader.getSystemClassLoader();

            thread.setContextClassLoader(systemCL);
            ImageIO.setUseCache(false);
        } finally {
            thread.setContextClassLoader(initialTCCL);
        }
    }

    private Cache cache;
    private ResourceHandler defaultHandler;

    public ResourceHandlerImpl(ResourceHandler defaultHandler) {
        this.defaultHandler = defaultHandler;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageFormat.format("Instance of {0} resource handler created", getClass().getName()));
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();

        initializeCache(facesContext);
    }

    private void initializeCache(FacesContext facesContext) {
        Map<?, ?> envMap = facesContext.getExternalContext().getInitParameterMap();

        cache = CacheManager.getInstance().getNewCache(RESOURCE_CACHE_NAME, envMap);
    }

    protected static void setResourceCodec(ResourceCodec codec) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        ServiceTracker.setService(facesContext, ResourceCodec.class, codec);
    }

    public static ResourceCodec getResourceCodec(FacesContext context) {
        ResourceCodec resourceCodec = ServiceTracker.getService(context, ResourceCodec.class);
        if (resourceCodec == null) {
            resourceCodec = DefaultResourceCodec.getInstance();
        }

        return resourceCodec;
    }

    protected static String getResourcePathFromRequest(FacesContext context) {
        String resourceName = Util.decodeResourceURL(context);

        if (resourceName != null) {
            if (resourceName.startsWith(RICHFACES_RESOURCE_IDENTIFIER)) {
                return resourceName.substring(RICHFACES_RESOURCE_IDENTIFIER.length());
            } else {
                return null;
            }
        } else {
            LOGGER.warn("Resource key not found" + resourceName);
            return null;
        }
    }

    protected boolean isThisHandlerResourceRequest(FacesContext context) {
        Boolean resourceRequest = BooleanRequestStateVariable.ResourceRequest.get(context);

        if (resourceRequest == null) {
            String resourcePath = getResourcePathFromRequest(context);

            // TODO handle exclusions
            resourceRequest = (resourcePath != null) && (resourcePath.length() > 0);
            BooleanRequestStateVariable.ResourceRequest.set(context, resourceRequest);

            if (LOGGER.isDebugEnabled() && resourceRequest) {
                LOGGER.debug(MessageFormat.format("Resource request detected: {0}", resourcePath));
            }
        }

        return resourceRequest;
    }

    @Override
    public boolean isResourceRequest(FacesContext context) {
        return isThisHandlerResourceRequest(context) || defaultHandler.isResourceRequest(context);
    }

    private Resource lookupInCache(FacesContext context, String resourceKey) {
        Resource resource = (Resource) cache.get(resourceKey);

        if (LOGGER.isDebugEnabled()) {
            if (resource == null) {
                LOGGER.debug("Resource was not located in cache");
            } else {
                LOGGER.debug("Resource was located in cache");
            }
        }

        return resource;
    }

    private static void sendNotModified(FacesContext context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("User agent has actual resource copy - sending 304 status code");
        }

        // TODO send cacheable resource headers (ETag + LastModified)?
        context.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    private void logResourceProblem(FacesContext context, Throwable throwable, String messagePattern,
                                    Object... arguments) {
        boolean isProductionStage = context.isProjectStage(ProjectStage.Production);

        if (LOGGER.isWarnEnabled() || (!isProductionStage && LOGGER.isInfoEnabled())) {
            String formattedMessage = MessageFormat.format(messagePattern, arguments);

            if (throwable != null) {
                LOGGER.warn(formattedMessage, throwable);
            } else {
                if (isProductionStage) {
                    LOGGER.info(formattedMessage);
                } else {
                    LOGGER.warn(formattedMessage);
                }
            }
        }
    }

    private void logMissingResource(FacesContext context, String resourceData) {
        logResourceProblem(context, null, "Resource {0} was not found", resourceData);
    }

    private static void sendResourceNotFound(FacesContext context) {
        context.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public void handleResourceRequest(FacesContext context) throws IOException {
        if (isThisHandlerResourceRequest(context)) {
            ResourceCodec resourceCodec = ResourceHandlerImpl.getResourceCodec(context);

            String resourcePath = getResourcePathFromRequest(context);
            
            assert (resourcePath != null) && (resourcePath.length() != 0);

            String resourceKey = resourceCodec.getResourceKey(context, resourcePath);

            assert (resourceKey != null) && (resourceKey.length() != 0);

            Resource resource = lookupInCache(context, resourceKey);

            if (resource == null) {
                String resourceName = resourceCodec.decodeResourceName(context, resourcePath);

                if ((resourceName == null) || (resourceName.length() == 0)) {
                    logMissingResource(context, resourceKey);
                    sendResourceNotFound(context);

                    return;
                }

                if (resourceName.lastIndexOf(".ecss") != -1) {
                    resource = new CompiledCSSResource(resourceName);
                }

                if (resource == null) {
                    resource = createHandlerDependentResource(resourceName, 
                        context.getExternalContext().getRequestParameterMap());
                }
                
                if (resource == null) {
                    logMissingResource(context, resourceName);
                    sendResourceNotFound(context);

                    return;
                }

                if (resource instanceof VersionedResource) {
                    VersionedResource versionedResource = (VersionedResource) resource;
                    String existingVersion = versionedResource.getVersion();
                    String requestedVersion = resourceCodec.decodeResourceVersion(context, resourcePath);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                                MessageFormat.format(
                                        "Client requested {0} version of resource, server has {1} version",
                                        String.valueOf(requestedVersion), String.valueOf(existingVersion)));
                    }

                    if ((existingVersion != null) && (requestedVersion != null)
                            && !existingVersion.equals(requestedVersion)) {
                        logResourceProblem(context, null, "Resource {0} of version {1} was not found", resourceName,
                                requestedVersion);
                        sendResourceNotFound(context);

                        return;
                    }
                }

                if (resource instanceof StateHolder) {
                    StateHolder stateHolder = (StateHolder) resource;
                    Object decodedData = resourceCodec.decodeResourceData(context, resourcePath);

                    if (LOGGER.isDebugEnabled()) {
                        if (decodedData != null) {
                            LOGGER.debug("Resource state data succesfully decoded");
                        } else {
                            LOGGER.debug("Resource state data decoded as null");
                        }
                    }

                    if (decodedData != null) {
                        stateHolder.restoreState(context, decodedData);
                    } else {

                        // resource was transient and didn't store data
                    }
                }

                if (resource instanceof AbstractCacheableResource) {
                    AbstractCacheableResource cacheableResource = (AbstractCacheableResource) resource;

                    if (cacheableResource.isCacheable(context)) {

                        // TODO - we could move this part of code to ConcurrentMap so that
                        // only single thread does resource put
                        CachedResourceImpl cachedResource = new CachedResourceImpl();

                        cachedResource.initialize(resource);

                        // someone may provided this resource for us
                        // while we were reading it, check once again
                        resource = lookupInCache(context, resourceKey);

                        if (resource == null) {
                            Date cacheExpirationDate = cachedResource.getExpired(context);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(new MessageFormat(
                                        "Storing {0} resource in cache until {1,date,dd MMM yyyy HH:mm:ss zzz}", Locale.US).
                                        format(new Object[]{resourceKey, cacheExpirationDate}));
                            }

                            cache.put(resourceKey, cachedResource, cacheExpirationDate);
                            resource = cachedResource;
                        }
                    }
                }
            }

            if (resource.userAgentNeedsUpdate(context)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("User agent needs resource update, encoding resource");
                }

                ExternalContext externalContext = context.getExternalContext();
                Map<String, String> headers = resource.getResponseHeaders();

                for (Entry<String, String> headerEntry : headers.entrySet()) {
                    String headerName = headerEntry.getKey();
                    String headerValue = headerEntry.getValue();

                    // TODO should external context handles this itself?
                    if ("content-length".equals(headerName.toLowerCase(Locale.US))) {
                        try {
                            externalContext.setResponseContentLength(Integer.parseInt(headerValue));
                        } catch (NumberFormatException e) {

                            // TODO: handle exception
                        }
                    } else {
                        externalContext.setResponseHeader(headerName, headerValue);
                    }
                }

                // TODO null content type?
                String contentType = resource.getContentType();

                if (contentType != null) {
                    externalContext.setResponseContentType(contentType);
                }

                // TODO - portlets
                HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();

                if (!"HEAD".equals(httpServletRequest.getMethod())) {

                    // TODO 'HEAD' HTTP method resources - ?
                    // TODO setup output buffer size according to configuration parameter
                    InputStream is = resource.getInputStream();
                    OutputStream os = externalContext.getResponseOutputStream();

                    try {
                        Util.copyStreamContent(is, os);
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug(e.getMessage(), e);
                                }
                            }
                        }

                        // TODO flush resource
                        // TODO dispose resource
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resource succesfully encoded");
                }
            } else {
                sendNotModified(context);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Passing request to the next resource handler in chain");
            }

            defaultHandler.handleResourceRequest(context);
        }
    }

    /**
     * Should be called only if {@link #isResourceExists(String)} returns <code>true</code>
     *
     * @param resourceName
     * @param params
     * @return
     */
    protected Resource createHandlerDependentResource(String resourceName, Map<String, String> params) {
        Resource resource = null;
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        if (contextClassLoader != null) {
            try {
                Class<?> loadedClass = Class.forName(resourceName, false, contextClassLoader);
                
                Class<? extends Resource> resourceClass = loadedClass.asSubclass(Resource.class);
                
                boolean legitimateResource = false;
                
                DynamicResource annotation = resourceClass.getAnnotation(DynamicResource.class);
                legitimateResource = (annotation != null);
                if (legitimateResource) {
                    LOGGER.debug(
                        MessageFormat.format("Dynamic resource annotation is present on resource class {0}", resourceName));
                } else {
                    LOGGER.debug(
                        MessageFormat.format("Dynamic resource annotation is not present on resource class {0}", resourceName));
                }
                
                if (!legitimateResource) {
                    // TODO resource marker extension name?
                    URL resourceMarkerUrl = contextClassLoader.getResource("META-INF/" + resourceName + ".resource.properties");

                    legitimateResource = resourceMarkerUrl != null;

                    if (LOGGER.isDebugEnabled()) {
                        if (legitimateResource) {
                            LOGGER.debug(
                                MessageFormat.format("Marker file for {0} resource found in classpath", resourceName));
                        } else {
                            LOGGER.debug(
                                MessageFormat.format("Marker file for {0} resource does not exist", resourceName));
                        }
                    }
                }
                
                if (legitimateResource) {
                    resource = (Resource) resourceClass.newInstance();
                    resource.setResourceName(resourceName);
                    if (resource instanceof Java2Dresource) {
                        Java2Dresource dynamicResource = (Java2Dresource) resource;
                        dynamicResource.populateParameters(params);
                        resource = dynamicResource;
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageFormat.format("Successfully created instance of {0} resource",
                                resourceName));
                    }
                }
            } catch (ClassNotFoundException e) {
                //do nothing
            } catch (Exception e) {
                logResourceProblem(FacesContext.getCurrentInstance(), e, "Error creating resource {0}", resourceName);
            } catch (LinkageError e) {
                logResourceProblem(FacesContext.getCurrentInstance(), e, "Error creating resource {0}", resourceName);
            }
        }

        return resource;
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType) {
        Resource result = null;
        Map<String, String> params = Util.parseResourceParameters(resourceName);
        resourceName = extractParametersFromResourceName(resourceName);
        if (resourceName.lastIndexOf(".ecss") != -1) {
            result = new CompiledCSSResource(resourceName);
        } else {
            if ((resourceName != null) && ((libraryName == null) || (libraryName.length() == 0))) {
                result = createHandlerDependentResource(resourceName, params);
            }
            
            if (result == null) {
                result = defaultHandler.createResource(resourceName, libraryName, contentType);
            }
        }

        return result;
    }

    private String extractParametersFromResourceName(String resourceName) {
        if (!(resourceName.lastIndexOf("?") != -1)) {
            return resourceName;
        }
        return resourceName.substring(0, resourceName.lastIndexOf("?"));
    }

    @Override
    public Resource createResource(String resourceName, String libraryName) {
        return createResource(resourceName, libraryName, null);
    }

    @Override
    public Resource createResource(String resourceName) {
        return createResource(resourceName, null, null);
    }

    @Override
    public String getRendererTypeForResourceName(String resourceName) {

        if (resourceName.endsWith(".ecss")) {
            return "javax.faces.resource.Stylesheet";
        }

        return defaultHandler.getRendererTypeForResourceName(resourceName);
    }

    @Override
    public boolean libraryExists(String libraryName) {
        return defaultHandler.libraryExists(libraryName);
    }
}