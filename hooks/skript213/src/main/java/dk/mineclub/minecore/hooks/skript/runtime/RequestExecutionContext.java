package dk.mineclub.minecore.hooks.skript.runtime;

import dk.mineclub.minecore.api.model.StoreCreatedRequest;
import dk.mineclub.minecore.api.model.StoreProduct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;

/** Stores per-execution MineCore data for Skript syntaxes. */
public final class RequestExecutionContext {
    private static final Logger LOGGER = Logger.getLogger(RequestExecutionContext.class.getName());
    private static final ThreadLocal<@Nullable StoreCreatedRequest> LAST_CREATED_REQUEST =
            ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<@Nullable List<StoreProduct>> PENDING_PRODUCTS =
            ThreadLocal.withInitial(() -> null);

    private RequestExecutionContext() {}

    public static void setLastCreatedRequest(StoreCreatedRequest request) {
        LAST_CREATED_REQUEST.set(request);
    }

    public static void clearLastCreatedRequest() {
        LAST_CREATED_REQUEST.remove();
    }

    public static @Nullable StoreCreatedRequest lastCreatedRequest() {
        return LAST_CREATED_REQUEST.get();
    }

    /** Opens a scope that collects products added via {@link #addPendingProduct(StoreProduct)}. */
    public static ProductScope withPendingProducts() {
        List<StoreProduct> previous = PENDING_PRODUCTS.get();
        List<StoreProduct> current = new ArrayList<>();
        PENDING_PRODUCTS.set(current);
        return new ProductScope(current, previous);
    }

    /**
     * Adds a product to the currently open pending-product scope, if any.
     *
     * @return true if a scope was open and the product was added, false otherwise
     */
    public static boolean addPendingProduct(StoreProduct product) {
        List<StoreProduct> current = PENDING_PRODUCTS.get();
        if (current == null) {
            LOGGER.warning("Attempted to add a pending product outside of a valid scope");
            return false;
        }
        current.add(product);
        return true;
    }

    public static final class ProductScope implements AutoCloseable {
        private final List<StoreProduct> products;
        private final @Nullable List<StoreProduct> previous;

        private ProductScope(List<StoreProduct> products, @Nullable List<StoreProduct> previous) {
            this.products = products;
            this.previous = previous;
        }

        public List<StoreProduct> products() {
            return Collections.unmodifiableList(products);
        }

        @Override
        public void close() {
            PENDING_PRODUCTS.set(previous);
        }
    }
}
