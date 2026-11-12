package de.brickforceaurora.restv1.integration.api.webv2.routing;

import java.util.concurrent.atomic.AtomicReference;

import de.brickforceaurora.restv1.integration.api.webv2.IVariableType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class WebNode {
    
    public static record VariableNode(String id, IVariableType type) {}
    
    private final Object2ObjectOpenHashMap<String, WebNode> paths = new Object2ObjectOpenHashMap<>();
    private final AtomicReference<VariableNode> variable = new AtomicReference<>();
    
    

}
