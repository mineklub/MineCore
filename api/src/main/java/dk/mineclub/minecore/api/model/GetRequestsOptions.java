package dk.mineclub.minecore.api.model;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
@Builder
public class GetRequestsOptions {
    @Nullable private Integer page;
    @Nullable private Integer limit;
    @Nullable private RequestStatusQuery clientStatus;
    @Nullable private RequestStatusQuery serverStatus;
    @Nullable private RequestSortByQuery sortBy;
    @Nullable private RequestSortOrderQuery order;
    @Nullable private String mcaccount;
    @Nullable private Date from;
    @Nullable private Date to;
    @Nullable private Boolean withMeta;
    @Nullable private Boolean includeProducts;
}
